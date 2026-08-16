package com.urbanforma.fireworks.client.giant.cascade;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.cascade.GiantCascadeTrajectory;
import com.urbanforma.fireworks.content.giant.cascade.GiantCascadeTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.cascade.GiantCascadeTrajectory.ChildBurst;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;

/**
 * Client-only visual program for one seventh-giant request.
 *
 * <p>The only allocation path is {@link #emit}. Each child is emitted as its complete 32-direction,
 * four-depth shell on one trigger tick. No child creates an entity, a payload, or a follow-up program.</p>
 */
public final class GiantCascadeClientProgram {
    private final Request request;
    private final ChildBurst[] childBursts = new ChildBurst[GiantCascadeTrajectory.CHILD_BURST_COUNT];
    private final List<TrackedSpark> trackedSparks = new ArrayList<>(GiantCascadeTrajectory.TOTAL_PARTICLES);
    private int age;
    private int createdParticles;

    public GiantCascadeClientProgram(Request request) {
        this.request = request;
        for (int index = 0; index < this.childBursts.length; index++) {
            this.childBursts[index] = GiantCascadeTrajectory.childBurst(request.seed(), index);
        }
    }

    /** Returns true only after the one bounded visual population and every retirement window have elapsed. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }

        this.updateRetirementFlicker();
        if (GiantCascadeTrajectory.isMainEmitting(this.age)) {
            for (int branch = 0; branch < GiantCascadeTrajectory.MAIN_BRANCH_COUNT; branch++) {
                this.emit(minecraft, GiantCascadeTrajectory.mainSample(this.request.seed(), branch, this.age));
            }
        }
        for (ChildBurst burst : this.childBursts) {
            if (this.age == burst.startTick()) {
                this.emitCompleteChildShell(minecraft, burst);
            }
        }
        this.age++;
        return this.age >= GiantCascadeTrajectory.TOTAL_VISUAL_TICKS;
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

    public int trackedSparkCount() {
        return this.trackedSparks.size();
    }

    public boolean isEmitting() {
        if (GiantCascadeTrajectory.isMainEmitting(this.age)) {
            return true;
        }
        for (ChildBurst burst : this.childBursts) {
            if (this.age == burst.startTick()) {
                return true;
            }
        }
        return false;
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantCascadeTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    /** Allocates all 128 shell nodes of one child before the next client tick. */
    private void emitCompleteChildShell(Minecraft minecraft, ChildBurst burst) {
        for (int branch = 0; branch < GiantCascadeTrajectory.CHILD_BRANCH_COUNT; branch++) {
            for (int shellDepth = 0; shellDepth < GiantCascadeTrajectory.CHILD_SEGMENTS_PER_BRANCH; shellDepth++) {
                this.emit(minecraft, GiantCascadeTrajectory.childSample(
                        this.request.seed(), burst.index(), branch, shellDepth));
            }
        }
    }

    private void emit(Minecraft minecraft, BranchSample sample) {
        Particle spark = FireworkParticleAppearance.createSpark(
                minecraft,
                this.request.x() + sample.position().x,
                this.request.y() + sample.position().y,
                this.request.z() + sample.position().z,
                0.0D,
                0.0D,
                0.0D);
        if (spark == null) {
            return;
        }
        GiantCascadeTrajectory.Rgb rgb = sample.colorBand().rgb();
        if (sample.coreHighlight()) {
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
        this.trackedSparks.add(new TrackedSpark(
                spark,
                sample.lifetime() - GiantCascadeTrajectory.retirementFlickerLeadTicks(sample),
                GiantCascadeTrajectory.retirementFlickerPhase(sample),
                sample.lifetime()));
        this.createdParticles++;
    }

    private void updateRetirementFlicker() {
        Iterator<TrackedSpark> iterator = this.trackedSparks.iterator();
        while (iterator.hasNext()) {
            TrackedSpark tracked = iterator.next();
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
                    tracked.particle().age++;
                    tracked.particle().setLifetime(tracked.originalLifetime() + 1);
                }
            }
            tracked.markFlickerStarted();
        }
    }

    private static final class TrackedSpark {
        private final Particle particle;
        private final int flickerStartAge;
        private final int phase;
        private final int originalLifetime;
        private boolean flickerStarted;

        private TrackedSpark(Particle particle, int flickerStartAge, int phase, int originalLifetime) {
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
                throw new IllegalArgumentException("Cascade request position must be finite");
            }
        }
    }
}
