package com.urbanforma.fireworks.client.giant;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.GiantRadiantTrajectory;
import com.urbanforma.fireworks.content.giant.GiantRadiantTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.GiantRadiantTrajectory.BranchSample;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * One isolated giant radiant visual instance.
 *
 * <p>The program creates only vanilla {@code FIREWORK} particles. It emits one complete deterministic ring per
 * tick and never calls a shared client scheduler. Zero particle velocity keeps every visible sample inside the
 * proven 130-block radius; the expanding ring positions provide the radiating motion.</p>
 */
public final class GiantRadiantClientProgram {
    private final Request request;
    private final Branch[] branches;
    private final List<RetirementSpark> retirementSparks = new ArrayList<>(GiantRadiantTrajectory.TOTAL_PARTICLES);
    private int age;
    private int nextSegment;
    private int createdParticles;

    public GiantRadiantClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[GiantRadiantTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = GiantRadiantTrajectory.branch(request.seed(), index);
        }
    }

    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }

        this.updateRetirementFlicker();
        if (this.nextSegment < GiantRadiantTrajectory.EMISSION_TICKS) {
            this.emitRing(minecraft);
            this.nextSegment++;
        }

        this.age++;
        return this.age >= GiantRadiantTrajectory.TOTAL_VISUAL_TICKS;
    }

    public Request request() {
        return this.request;
    }

    public int age() {
        return this.age;
    }

    public int emittedSegments() {
        return this.nextSegment;
    }

    public int createdParticleCount() {
        return this.createdParticles;
    }

    public boolean isEmitting() {
        return this.nextSegment < GiantRadiantTrajectory.EMISSION_TICKS;
    }

    private void emitRing(Minecraft minecraft) {
        int segmentIndex = this.nextSegment;
        for (Branch branch : this.branches) {
            BranchSample sample = GiantRadiantTrajectory.sample(this.request.seed(), branch, segmentIndex);
            Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    position.x,
                    position.y,
                    position.z,
                    0.0D,
                    0.0D,
                    0.0D);
            if (spark == null) {
                continue;
            }

            // Deliberately brighten only the existing spark particle; no screen effect or custom resource is used.
            GiantRadiantTrajectory.Rgb rgb = sample.colorBand().rgb();
            float brightness = sample.brightness();
            boolean coreHighlight = GiantRadiantTrajectory.isCoreSegment(sample.segmentIndex());
            if (coreHighlight) {
                FireworkParticleAppearance.applyCoreColor(spark, rgb.red(), rgb.green(), rgb.blue());
                FireworkParticleAppearance.applyVisibilityScale(
                        spark, FireworkParticleAppearance.CORE_BASE_SCALE, true);
            } else {
                FireworkParticleAppearance.applyVividColor(
                        spark,
                        rgb.red(),
                        rgb.green(),
                        rgb.blue(),
                        brightness,
                        FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
                FireworkParticleAppearance.applyVisibilityScale(spark, sample.colorBand().scale());
            }
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            spark.setLifetime(sample.lifetime());
            this.retirementSparks.add(new RetirementSpark(
                    spark,
                    sample.lifetime() - GiantRadiantTrajectory.retirementFlickerLeadTicks(
                            sample.branch(), sample.segmentIndex()),
                    GiantRadiantTrajectory.retirementFlickerPhase(sample.branch(), sample.segmentIndex()),
                    sample.lifetime()));
            this.createdParticles++;
        }
    }

    /** Enables vanilla SparkParticle twinkle only during each spark's seeded retirement window. */
    private void updateRetirementFlicker() {
        Iterator<RetirementSpark> iterator = this.retirementSparks.iterator();
        while (iterator.hasNext()) {
            RetirementSpark tracked = iterator.next();
            if (!tracked.particle().isAlive()) {
                iterator.remove();
                continue;
            }
            if (tracked.flickerStarted() || tracked.particle().age < tracked.flickerStartAge()) {
                continue;
            }
            if (tracked.particle() instanceof FireworkParticles.SparkParticle spark) {
                spark.setTwinkle(true);
                if (tracked.phase() == 1) {
                    // Shift both counters together so the final disappearance tick stays unchanged.
                    tracked.particle().age++;
                    tracked.particle().setLifetime(tracked.originalLifetime() + 1);
                }
            }
            tracked.markFlickerStarted();
        }
    }

    private static final class RetirementSpark {
        private final Particle particle;
        private final int flickerStartAge;
        private final int phase;
        private final int originalLifetime;
        private boolean flickerStarted;

        private RetirementSpark(Particle particle, int flickerStartAge, int phase, int originalLifetime) {
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

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Giant radiant request position must be finite");
            }
        }
    }
}
