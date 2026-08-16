package com.urbanforma.fireworks.client.giant.palm;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.palm.GiantPalmTrajectory;
import com.urbanforma.fireworks.content.giant.palm.GiantPalmTrajectory.PalmSample;
import com.urbanforma.fireworks.content.giant.palm.GiantPalmTrajectory.RetirementFlicker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only program for one giant palm request.
 *
 * <p>Particle allocation is deliberately isolated in {@link #createParticle(Minecraft, Vec3)}. The two update
 * passes below move or retire only already-created particles, so the falling frond stage cannot create a second
 * burst, a small rocket, or server-side work.</p>
 */
public final class GiantPalmClientProgram {
    private final Request request;
    private final GiantPalmTrajectory.PalmBranch[] branches;
    private final List<TrackedParticle> trackedParticles = new ArrayList<>(GiantPalmTrajectory.TOTAL_PARTICLES);
    private final List<DescendingFrond> descendingFronds = new ArrayList<>(GiantPalmTrajectory.FROND_PARTICLES);
    private int age;
    private int createdParticles;

    public GiantPalmClientProgram(Request request) {
        this.request = request;
        this.branches = new GiantPalmTrajectory.PalmBranch[GiantPalmTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = GiantPalmTrajectory.branch(request.seed(), index);
        }
    }

    /** Returns true only once every finite emission and retirement window has elapsed. */
    public boolean tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            return false;
        }

        updateDescendingFronds();
        updateRetirementFlicker();
        if (GiantPalmTrajectory.isStemEmitting(this.age)) {
            int segment = this.age - GiantPalmTrajectory.STEM_START_TICK;
            for (int branch = 0; branch < this.branches.length; branch++) {
                emit(minecraft, GiantPalmTrajectory.stemSample(this.request.seed(), branch, segment));
            }
        }
        if (GiantPalmTrajectory.isFrondEmitting(this.age)) {
            int segment = this.age - GiantPalmTrajectory.FROND_START_TICK;
            for (int branch = 0; branch < this.branches.length; branch++) {
                for (int leaf = 0; leaf < GiantPalmTrajectory.FRONDS_PER_BRANCH; leaf++) {
                    emit(minecraft, GiantPalmTrajectory.frondSample(this.request.seed(), branch, leaf, segment));
                }
            }
        }

        this.age++;
        return this.age >= GiantPalmTrajectory.TOTAL_VISUAL_TICKS;
    }

    public Request request() {
        return this.request;
    }

    public int age() {
        return this.age;
    }

    public int createdParticleCount() {
        return this.createdParticles;
    }

    public int trackedParticleCount() {
        return this.trackedParticles.size();
    }

    public int descendingFrondCount() {
        return this.descendingFronds.size();
    }

    public boolean isEmitting() {
        return GiantPalmTrajectory.isStemEmitting(this.age) || GiantPalmTrajectory.isFrondEmitting(this.age);
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantPalmTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    private void emit(Minecraft minecraft, PalmSample sample) {
        Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
        Particle spark = createParticle(minecraft, position);
        if (spark == null) {
            return;
        }

        GiantPalmTrajectory.Rgb rgb = sample.colorBand().rgb();
        if (sample.stage() == GiantPalmTrajectory.Stage.STEM && sample.segmentIndex() < 6) {
            FireworkParticleAppearance.applyCoreColor(spark, rgb.red(), rgb.green(), rgb.blue());
            FireworkParticleAppearance.applyVisibilityScale(
                    spark, FireworkParticleAppearance.CORE_BASE_SCALE, true);
        } else {
            FireworkParticleAppearance.applyVividColor(
                    spark,
                    rgb.red(),
                    rgb.green(),
                    rgb.blue(),
                    sample.brightness(),
                    FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
            FireworkParticleAppearance.applyVisibilityScale(spark, sample.colorBand().scale());
        }
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setLifetime(sample.lifetime());
        RetirementFlicker flicker = GiantPalmTrajectory.retirementFlicker(sample);
        this.trackedParticles.add(new TrackedParticle(spark, flicker.startAge(), flicker.phase(), sample.lifetime()));
        if (sample.stage() == GiantPalmTrajectory.Stage.FROND) {
            this.descendingFronds.add(new DescendingFrond(
                    spark,
                    sample.branch(),
                    sample.leafIndex(),
                    sample.segmentIndex(),
                    sample.emissionTick()));
        }
        this.createdParticles++;
    }

    /** The sole local allocation boundary. Every caller receives only an existing vanilla-compatible spark. */
    private Particle createParticle(Minecraft minecraft, Vec3 position) {
        return FireworkParticleAppearance.createSpark(
                minecraft, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
    }

    /** Repositions existing leaf particles along their fixed descent path and never allocates a particle. */
    private void updateDescendingFronds() {
        Iterator<DescendingFrond> iterator = this.descendingFronds.iterator();
        while (iterator.hasNext()) {
            DescendingFrond frond = iterator.next();
            Particle spark = frond.particle();
            if (!spark.isAlive()) {
                iterator.remove();
                continue;
            }
            int tailAge = Math.min(
                    GiantPalmTrajectory.DESCENT_TICKS,
                    Math.max(0, this.age - frond.emissionTick()));
            Vec3 position = GiantPalmTrajectory.frondPosition(
                    frond.branch(), frond.leafIndex(), frond.segmentIndex(), tailAge)
                    .add(this.request.x(), this.request.y(), this.request.z());
            spark.setPos(position.x, position.y, position.z);
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        }
    }

    /** Enables only the final seeded twinkle; this update pass never changes the planned population. */
    private void updateRetirementFlicker() {
        Iterator<TrackedParticle> iterator = this.trackedParticles.iterator();
        while (iterator.hasNext()) {
            TrackedParticle tracked = iterator.next();
            Particle spark = tracked.particle();
            if (!spark.isAlive()) {
                iterator.remove();
                continue;
            }
            if (tracked.flickerStarted() || spark.age < tracked.flickerStartAge()) {
                continue;
            }
            if (spark instanceof FireworkParticles.SparkParticle fireworkSpark) {
                fireworkSpark.setTwinkle(true);
                if (tracked.phase() == 1) {
                    spark.age++;
                    spark.setLifetime(tracked.originalLifetime() + 1);
                } else if (tracked.phase() == 2) {
                    spark.setLifetime(tracked.originalLifetime() - 1);
                }
            }
            tracked.markFlickerStarted();
        }
    }

    private static final class TrackedParticle {
        private final Particle particle;
        private final int flickerStartAge;
        private final int phase;
        private final int originalLifetime;
        private boolean flickerStarted;

        private TrackedParticle(Particle particle, int flickerStartAge, int phase, int originalLifetime) {
            this.particle = particle;
            this.flickerStartAge = flickerStartAge;
            this.phase = phase;
            this.originalLifetime = originalLifetime;
        }

        private Particle particle() {
            return this.particle;
        }

        private int flickerStartAge() {
            return this.flickerStartAge;
        }

        private int phase() {
            return this.phase;
        }

        private int originalLifetime() {
            return this.originalLifetime;
        }

        private boolean flickerStarted() {
            return this.flickerStarted;
        }

        private void markFlickerStarted() {
            this.flickerStarted = true;
        }
    }

    private record DescendingFrond(
            Particle particle,
            GiantPalmTrajectory.PalmBranch branch,
            int leafIndex,
            int segmentIndex,
            int emissionTick) {
    }

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Giant palm request position must be finite");
            }
        }
    }
}
