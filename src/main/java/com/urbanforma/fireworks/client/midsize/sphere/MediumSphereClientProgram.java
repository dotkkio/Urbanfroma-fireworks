package com.urbanforma.fireworks.client.midsize.sphere;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereCatalog;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereTrajectory;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereTrajectory.Sample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Caller-driven physical-client program for one finite medium sphere request.
 *
 * <p>The program has no listener, static queue, shared scheduler, network handler, or server-side particle path.
 * Its per-request peak comes directly from the selected immutable definition.</p>
 */
public final class MediumSphereClientProgram {
    private final Request request;
    private final MediumSphereDefinition definition;
    private final MediumSphereTrajectory.Branch[] branches;
    private int age;
    private int nextEmissionTick;
    private int createdParticles;

    public MediumSphereClientProgram(Request request) {
        this.request = requireRequest(request);
        this.definition = MediumSphereCatalog.require(request.id());
        this.branches = new MediumSphereTrajectory.Branch[this.definition.particleBudget().branchCount()];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = MediumSphereTrajectory.branch(this.definition, request.seed(), index);
        }
    }

    /** Returns true once the caller may discard this program. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft == null ? null : minecraft.level;
        if (level == null) {
            return false;
        }
        if (this.nextEmissionTick < this.definition.particleBudget().emissionTicks()) {
            emitTick(minecraft, this.nextEmissionTick);
            this.nextEmissionTick++;
        }
        this.age++;
        return this.age >= this.definition.particleBudget().totalVisualTicks();
    }

    public Request request() {
        return this.request;
    }

    public MediumSphereDefinition definition() {
        return this.definition;
    }

    public int age() {
        return this.age;
    }

    public int emittedTicks() {
        return this.nextEmissionTick;
    }

    public int createdParticleCount() {
        return this.createdParticles;
    }

    public int plannedParticleCount() {
        return this.definition.particleBudget().totalParticles();
    }

    public int peakParticlesPerTick() {
        return this.definition.particleBudget().particlesPerTick();
    }

    public int maxOwnedParticles() {
        return this.definition.particleBudget().maxOwnedParticles();
    }

    public boolean isEmitting() {
        return this.nextEmissionTick < this.definition.particleBudget().emissionTicks();
    }

    private void emitTick(Minecraft minecraft, int emissionTick) {
        for (MediumSphereTrajectory.Branch branch : this.branches) {
            Sample sample = MediumSphereTrajectory.sample(this.definition, this.request.seed(), branch, emissionTick);
            Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
            if (spark == null) {
                continue;
            }
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            FireworkParticleAppearance.applyVividColor(
                    spark,
                    sample.color().red(),
                    sample.color().green(),
                    sample.color().blue(),
                    sample.coreHighlight()
                            ? FireworkParticleAppearance.CORE_COLOR_BRILLIANCE
                            : 1.04F,
                    sample.coreHighlight()
                            ? FireworkParticleAppearance.CORE_COLOR_WHITE_LIFT
                            : FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
            FireworkParticleAppearance.applyVisibilityScale(spark, sample.scale(), sample.coreHighlight());
            spark.setLifetime(sample.lifetimeTicks());
            if (sample.twinkles() && spark instanceof FireworkParticles.SparkParticle sparkParticle) {
                sparkParticle.setTwinkle(true);
            }
            this.createdParticles++;
        }
    }

    public record Request(String id, double x, double y, double z, long seed) {
        public Request {
            if (id == null || id.isBlank() || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Medium sphere client request must contain a stable id and finite position");
            }
        }
    }

    private static Request requireRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Medium sphere client request may not be null");
        }
        return request;
    }
}
