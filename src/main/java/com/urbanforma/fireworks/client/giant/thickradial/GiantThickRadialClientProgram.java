package com.urbanforma.fireworks.client.giant.thickradial;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.thickradial.GiantThickRadialTrajectory;
import com.urbanforma.fireworks.content.giant.thickradial.GiantThickRadialTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.thickradial.GiantThickRadialTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.thickradial.GiantThickRadialTrajectory.ParticleLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only visual program for one sparse, thick-radial giant detonation.
 *
 * <p>The finite 72-tick loop is the only particle allocation path. It creates at most 160 particles per tick and
 * never schedules a second burst or a new child visual.</p>
 */
public final class GiantThickRadialClientProgram {
    private final Request request;
    private final Branch[] branches;
    private int age;
    private int nextSegment;
    private int createdParticles;

    public GiantThickRadialClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[GiantThickRadialTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = GiantThickRadialTrajectory.branch(request.seed(), index);
        }
    }

    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }
        if (this.nextSegment < GiantThickRadialTrajectory.EMISSION_TICKS) {
            this.emitCrossSection(minecraft, this.nextSegment);
            this.nextSegment++;
        }
        this.age++;
        return this.age >= GiantThickRadialTrajectory.TOTAL_VISUAL_TICKS;
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
        return this.nextSegment < GiantThickRadialTrajectory.EMISSION_TICKS;
    }

    private void emitCrossSection(Minecraft minecraft, int segmentIndex) {
        for (Branch branch : this.branches) {
            for (ParticleLayer particleLayer : ParticleLayer.values()) {
                BranchSample sample = GiantThickRadialTrajectory.sample(branch, segmentIndex, particleLayer);
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
                applyAppearance(spark, sample);
                spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
                spark.setLifetime(sample.lifetime());
                this.createdParticles++;
            }
        }
    }

    private static void applyAppearance(Particle spark, BranchSample sample) {
        GiantThickRadialTrajectory.Rgb rgb = sample.particleLayer().rgb();
        if (GiantThickRadialTrajectory.isCoreLayer(sample.particleLayer())) {
            FireworkParticleAppearance.applyCoreColor(spark, rgb.red(), rgb.green(), rgb.blue());
            FireworkParticleAppearance.applyVisibilityScale(
                    spark, FireworkParticleAppearance.CORE_BASE_SCALE, true);
            return;
        }
        FireworkParticleAppearance.applyVividColor(
                spark,
                rgb.red(),
                rgb.green(),
                rgb.blue(),
                sample.brilliance(),
                FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
        FireworkParticleAppearance.applyVisibilityScale(spark, sample.particleLayer().quadScale());
    }

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Thick-radial request position must be finite");
            }
        }
    }
}
