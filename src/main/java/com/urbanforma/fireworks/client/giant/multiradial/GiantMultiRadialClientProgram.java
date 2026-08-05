package com.urbanforma.fireworks.client.giant.multiradial;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.multiradial.GiantMultiRadialTrajectory;
import com.urbanforma.fireworks.content.giant.multiradial.GiantMultiRadialTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.multiradial.GiantMultiRadialTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.multiradial.GiantMultiRadialTrajectory.Layer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only visual program for the fourth giant.
 *
 * <p>Each layer creates its complete deterministic branch rings during its own emission window. After a spark is
 * created, this program only changes its vanilla particle appearance/lifetime; it never allocates a continuation
 * burst, child explosion, random sub-firework, or non-spark visual.</p>
 */
public final class GiantMultiRadialClientProgram {
    private final Request request;
    private final Branch[][] branches;
    private final List<TrackedSpark> trackedSparks = new ArrayList<>(GiantMultiRadialTrajectory.TOTAL_PARTICLES);
    private int age;
    private int createdParticles;

    public GiantMultiRadialClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[Layer.values().length][];
        for (Layer layer : Layer.values()) {
            Branch[] layerBranches = new Branch[layer.branchCount()];
            for (int index = 0; index < layerBranches.length; index++) {
                layerBranches[index] = GiantMultiRadialTrajectory.branch(request.seed(), layer, index);
            }
            this.branches[layer.index()] = layerBranches;
        }
    }

    /** Returns true only after every layer's bounded lifetime has elapsed. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }

        this.updateRetirementFlicker();
        for (Layer layer : Layer.values()) {
            if (GiantMultiRadialTrajectory.isLayerEmitting(layer, this.age)) {
                this.emitLayerRing(minecraft, layer, this.age - layer.startTick());
            }
        }
        this.age++;
        return this.age >= GiantMultiRadialTrajectory.TOTAL_VISUAL_TICKS;
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
        for (Layer layer : Layer.values()) {
            if (GiantMultiRadialTrajectory.isLayerEmitting(layer, this.age)) {
                return true;
            }
        }
        return false;
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantMultiRadialTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    private void emitLayerRing(Minecraft minecraft, Layer layer, int segmentIndex) {
        Branch[] layerBranches = this.branches[layer.index()];
        for (Branch branch : layerBranches) {
            BranchSample sample = GiantMultiRadialTrajectory.sample(
                    this.request.seed(), branch, segmentIndex);
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

            GiantMultiRadialTrajectory.Rgb rgb = sample.colorBand().rgb();
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
                    sample.lifetime() - retirementFlickerLead(sample),
                    retirementFlickerPhase(sample),
                    sample.lifetime()));
            this.createdParticles++;
        }
    }

    /** Enables vanilla spark twinkle only in a deterministic final window; it never creates another particle. */
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

    private static int retirementFlickerLead(BranchSample sample) {
        return 18 + Math.floorMod(
                sample.branch().index() * 31 + sample.segmentIndex() * 17 + sample.layer().index() * 11,
                7);
    }

    private static int retirementFlickerPhase(BranchSample sample) {
        return Math.floorMod(
                sample.branch().index() * 17 + sample.segmentIndex() * 29 + sample.layer().index(),
                2);
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
                throw new IllegalArgumentException("Multiradial request position must be finite");
            }
        }
    }
}
