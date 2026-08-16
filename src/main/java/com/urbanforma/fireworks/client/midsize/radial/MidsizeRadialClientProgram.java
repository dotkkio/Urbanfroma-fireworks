package com.urbanforma.fireworks.client.midsize.radial;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialFireworkDefinition;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory.Branch;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory.BranchSample;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory.Layer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only, caller-driven program for one finite Medium radial detonation.
 *
 * <p>The coordinator supplies one request after its existing compact detonation path. This class creates neither a
 * listener nor a queue, sends no packet, and has no server-side particle or trajectory path.</p>
 */
public final class MidsizeRadialClientProgram {
    private final Request request;
    private final Branch[][] branches;
    private int age;
    private int createdParticles;

    public MidsizeRadialClientProgram(Request request) {
        this.request = request;
        MidsizeRadialTrajectory.Path path = request.definition().path();
        this.branches = new Branch[path.layers().size()][];
        for (int layerIndex = 0; layerIndex < this.branches.length; layerIndex++) {
            Layer layer = path.layer(layerIndex);
            Branch[] layerBranches = new Branch[layer.branchCount()];
            for (int branchIndex = 0; branchIndex < layerBranches.length; branchIndex++) {
                layerBranches[branchIndex] = MidsizeRadialTrajectory.branch(
                        request.seed(), path, layerIndex, branchIndex);
            }
            this.branches[layerIndex] = layerBranches;
        }
    }

    /** Returns true only after the finite local population has reached its configured lifetime. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft == null ? null : minecraft.level;
        if (level == null) {
            return false;
        }
        MidsizeRadialTrajectory.Path path = this.request.definition().path();
        if (this.age <= path.maximumEmissionTick()) {
            emitAtTick(minecraft, this.age);
        }
        this.age++;
        return this.age >= path.totalVisualTicks();
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

    public int plannedParticleCount() {
        return this.request.definition().particlePlan().totalParticles();
    }

    public int localPeakParticlesPerTick() {
        return this.request.definition().particlePlan().localPeakParticlesPerTick();
    }

    public boolean isEmitting() {
        return this.age <= this.request.definition().path().maximumEmissionTick();
    }

    private void emitAtTick(Minecraft minecraft, int emissionTick) {
        MidsizeRadialTrajectory.Path path = this.request.definition().path();
        for (int layerIndex = 0; layerIndex < this.branches.length; layerIndex++) {
            Layer layer = path.layer(layerIndex);
            int segmentIndex = emissionTick - layer.startTick();
            if (segmentIndex < 0 || segmentIndex >= layer.segmentsPerBranch()) {
                continue;
            }
            for (Branch branch : this.branches[layerIndex]) {
                emitSpark(minecraft, MidsizeRadialTrajectory.sample(branch, segmentIndex));
            }
        }
    }

    private void emitSpark(Minecraft minecraft, BranchSample sample) {
        Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
        Particle spark = FireworkParticleAppearance.createSpark(
                minecraft, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
        if (spark == null) {
            return;
        }
        MidsizeRadialFireworkDefinition.Rgb rgb = this.request.definition().palette().colorFor(sample.colorBand());
        if (sample.colorBand() == MidsizeRadialTrajectory.ColorBand.CORE) {
            FireworkParticleAppearance.applyCoreColor(spark, rgb.red(), rgb.green(), rgb.blue());
            FireworkParticleAppearance.applyVisibilityScale(
                    spark, sample.colorBand().scale(), true);
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
        if (sample.twinkles() && spark instanceof FireworkParticles.SparkParticle fireworkSpark) {
            fireworkSpark.setTwinkle(true);
        }
        this.createdParticles++;
    }

    public record Request(MidsizeRadialFireworkDefinition definition, double x, double y, double z, long seed) {
        public Request {
            if (definition == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Medium radial client request must be finite and typed");
            }
        }
    }
}
