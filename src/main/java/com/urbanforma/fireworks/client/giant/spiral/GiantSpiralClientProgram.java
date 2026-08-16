package com.urbanforma.fireworks.client.giant.spiral;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.spiral.GiantSpiralTrajectory;
import com.urbanforma.fireworks.content.giant.spiral.GiantSpiralTrajectory.Layer;
import com.urbanforma.fireworks.content.giant.spiral.GiantSpiralTrajectory.RetirementFlicker;
import com.urbanforma.fireworks.content.giant.spiral.GiantSpiralTrajectory.SpiralSample;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only program for one giant three-dimensional spiral request.
 *
 * <p>The only allocation boundary is {@link #createParticle(Minecraft, Vec3)}. Reverse precession and twinkle
 * are update-only operations on tracked particles, so the departure never spawns a second stage or a new rocket.</p>
 */
public final class GiantSpiralClientProgram {
    private final Request request;
    private final GiantSpiralTrajectory.SpiralBranch[][] branches;
    private final List<TrackedSpiral> trackedSpirals = new ArrayList<>(GiantSpiralTrajectory.TOTAL_PARTICLES);
    private int age;
    private int createdParticles;

    public GiantSpiralClientProgram(Request request) {
        this.request = request;
        this.branches = new GiantSpiralTrajectory.SpiralBranch[GiantSpiralTrajectory.LAYER_COUNT][];
        for (Layer layer : Layer.values()) {
            GiantSpiralTrajectory.SpiralBranch[] layerBranches =
                    new GiantSpiralTrajectory.SpiralBranch[layer.branchCount()];
            for (int index = 0; index < layerBranches.length; index++) {
                layerBranches[index] = GiantSpiralTrajectory.branch(request.seed(), layer, index);
            }
            this.branches[layer.ordinal()] = layerBranches;
        }
    }

    /** Returns true only after all three bounded layers and their finite unwind/retirement windows end. */
    public boolean tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            return false;
        }

        updateExistingSpirals();
        updateRetirementFlicker();
        for (Layer layer : Layer.values()) {
            if (!GiantSpiralTrajectory.isEmitting(layer, this.age)) {
                continue;
            }
            int segment = this.age - layer.startTick();
            for (int branch = 0; branch < this.branches[layer.ordinal()].length; branch++) {
                emit(minecraft, GiantSpiralTrajectory.sample(this.request.seed(), layer, branch, segment));
            }
        }

        this.age++;
        return this.age >= GiantSpiralTrajectory.TOTAL_VISUAL_TICKS;
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
        return this.trackedSpirals.size();
    }

    public boolean isEmitting() {
        for (Layer layer : Layer.values()) {
            if (GiantSpiralTrajectory.isEmitting(layer, this.age)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUnwinding() {
        return GiantSpiralTrajectory.isUnwinding(this.age);
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantSpiralTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    private void emit(Minecraft minecraft, SpiralSample sample) {
        Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
        Particle spark = createParticle(minecraft, position);
        if (spark == null) {
            return;
        }

        GiantSpiralTrajectory.Rgb rgb = sample.colorBand().rgb();
        if (sample.layer() == Layer.INNER && sample.segmentIndex() < 6) {
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
        RetirementFlicker flicker = GiantSpiralTrajectory.retirementFlicker(sample);
        this.trackedSpirals.add(new TrackedSpiral(
                spark, sample, flicker.startAge(), flicker.phase(), sample.lifetime()));
        this.createdParticles++;
    }

    /** The sole local allocation boundary. Every caller receives only an existing vanilla-compatible spark. */
    private Particle createParticle(Minecraft minecraft, Vec3 position) {
        return FireworkParticleAppearance.createSpark(
                minecraft, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
    }

    /** Rewinds existing three-dimensional paths for a fixed interval. This method has no particle creation path. */
    private void updateExistingSpirals() {
        if (!GiantSpiralTrajectory.isUnwinding(this.age)) {
            return;
        }
        int unwindAge = this.age - GiantSpiralTrajectory.UNWIND_START_TICK;
        for (TrackedSpiral tracked : this.trackedSpirals) {
            Particle spark = tracked.particle();
            if (!spark.isAlive()) {
                continue;
            }
            Vec3 position = GiantSpiralTrajectory.positionDuringUnwind(tracked.sample(), unwindAge)
                    .add(this.request.x(), this.request.y(), this.request.z());
            spark.setPos(position.x, position.y, position.z);
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        }
    }

    /** Staggers the finite final twinkle without increasing the planned particle population. */
    private void updateRetirementFlicker() {
        Iterator<TrackedSpiral> iterator = this.trackedSpirals.iterator();
        while (iterator.hasNext()) {
            TrackedSpiral tracked = iterator.next();
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

    private static final class TrackedSpiral {
        private final Particle particle;
        private final SpiralSample sample;
        private final int flickerStartAge;
        private final int phase;
        private final int originalLifetime;
        private boolean flickerStarted;

        private TrackedSpiral(
                Particle particle,
                SpiralSample sample,
                int flickerStartAge,
                int phase,
                int originalLifetime) {
            this.particle = particle;
            this.sample = sample;
            this.flickerStartAge = flickerStartAge;
            this.phase = phase;
            this.originalLifetime = originalLifetime;
        }

        private Particle particle() {
            return this.particle;
        }

        private SpiralSample sample() {
            return this.sample;
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

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Giant spiral request position must be finite");
            }
        }
    }
}
