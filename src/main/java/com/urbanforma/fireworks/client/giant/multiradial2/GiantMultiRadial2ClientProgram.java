package com.urbanforma.fireworks.client.giant.multiradial2;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.multiradial2.GiantMultiRadial2Trajectory;
import com.urbanforma.fireworks.content.giant.multiradial2.GiantMultiRadial2Trajectory.Branch;
import com.urbanforma.fireworks.content.giant.multiradial2.GiantMultiRadial2Trajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.multiradial2.GiantMultiRadial2Trajectory.Layer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only program for one fifth-giant detonation.
 *
 * <p>All four layers are emitted by this one program from one request origin. Layer start ticks only organize the
 * interior of that one burst; they never create another request, rocket, explosion point, or post-burst emitter.</p>
 */
public final class GiantMultiRadial2ClientProgram {
    private final Request request;
    private final Branch[][] branches;
    private final List<RetirementSpark> retirementSparks = new ArrayList<>(GiantMultiRadial2Trajectory.TOTAL_PARTICLES);
    private int age;
    private int createdParticles;

    public GiantMultiRadial2ClientProgram(Request request) {
        this.request = request;
        Layer[] layers = Layer.values();
        this.branches = new Branch[layers.length][];
        for (Layer layer : layers) {
            Branch[] layerBranches = new Branch[layer.branchCount()];
            for (int index = 0; index < layerBranches.length; index++) {
                layerBranches[index] = GiantMultiRadial2Trajectory.branch(request.seed(), layer, index);
            }
            this.branches[layer.ordinal()] = layerBranches;
        }
    }

    /** Returns true only after the one bounded visual population has retired. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }

        this.updateRetirementFlicker();
        if (this.age <= GiantMultiRadial2Trajectory.MAX_EMISSION_TICK) {
            this.emitLayersAtTick(minecraft, this.age);
        }
        this.age++;
        return this.age >= GiantMultiRadial2Trajectory.TOTAL_VISUAL_TICKS;
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

    public boolean isEmitting() {
        return this.age <= GiantMultiRadial2Trajectory.MAX_EMISSION_TICK;
    }

    private void emitLayersAtTick(Minecraft minecraft, int emissionTick) {
        for (Layer layer : Layer.values()) {
            int segmentIndex = emissionTick - layer.startTick();
            if (segmentIndex < 0 || segmentIndex >= layer.segmentsPerBranch()) {
                continue;
            }
            for (Branch branch : this.branches[layer.ordinal()]) {
                this.emitSpark(minecraft, GiantMultiRadial2Trajectory.sample(branch, segmentIndex));
            }
        }
    }

    private void emitSpark(Minecraft minecraft, BranchSample sample) {
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
            return;
        }

        GiantMultiRadial2Trajectory.Rgb rgb = sample.colorBand().rgb();
        if (GiantMultiRadial2Trajectory.isCoreSegment(sample)) {
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
        this.retirementSparks.add(new RetirementSpark(
                spark,
                sample.lifetime() - GiantMultiRadial2Trajectory.retirementFlickerLeadTicks(sample),
                GiantMultiRadial2Trajectory.retirementFlickerPhase(sample),
                sample.lifetime()));
        this.createdParticles++;
    }

    /** Turns on vanilla twinkle late in an existing spark's life without creating a trailing effect. */
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
                throw new IllegalArgumentException("Multi-radial request position must be finite");
            }
        }
    }
}
