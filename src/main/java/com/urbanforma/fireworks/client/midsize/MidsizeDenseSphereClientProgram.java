package com.urbanforma.fireworks.client.midsize;

import com.urbanforma.fireworks.content.midsize.MidsizeDenseSphereTrajectory;
import com.urbanforma.fireworks.content.midsize.MidsizeDenseSphereTrajectory.Branch;
import com.urbanforma.fireworks.content.midsize.MidsizeDenseSphereTrajectory.BranchSample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Caller-driven physical-client program for one midsize dense sphere event.
 *
 * <p>Integration constructs this program from one already-authorized burst request and advances it once per client
 * tick. It owns no event listener, static queue, network handler, server work, or global particle limiter.</p>
 */
public final class MidsizeDenseSphereClientProgram {
    private final Request request;
    private final Branch[] branches;
    private int age;
    private int nextSegment;
    private int createdParticles;

    public MidsizeDenseSphereClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[MidsizeDenseSphereTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = MidsizeDenseSphereTrajectory.branch(request.seed(), index);
        }
    }

    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft == null ? null : minecraft.level;
        if (level == null) {
            return false;
        }
        if (this.nextSegment < MidsizeDenseSphereTrajectory.EMISSION_TICKS) {
            emitRing(minecraft, this.nextSegment);
            this.nextSegment++;
        }
        this.age++;
        return this.age >= MidsizeDenseSphereTrajectory.TOTAL_VISUAL_TICKS;
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

    public int plannedParticleCount() {
        return MidsizeDenseSphereTrajectory.TOTAL_PARTICLES;
    }

    public boolean isEmitting() {
        return this.nextSegment < MidsizeDenseSphereTrajectory.EMISSION_TICKS;
    }

    private void emitRing(Minecraft minecraft, int segmentIndex) {
        for (Branch branch : this.branches) {
            BranchSample sample = MidsizeDenseSphereTrajectory.sample(this.request.seed(), branch, segmentIndex);
            Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            Particle spark = MidsizeSparkAppearance.create(minecraft, position.x, position.y, position.z);
            if (spark == null) {
                continue;
            }
            MidsizeDenseSphereTrajectory.Rgb color = sample.colorBand().rgb();
            MidsizeSparkAppearance.apply(
                    spark,
                    color.red(),
                    color.green(),
                    color.blue(),
                    sample.brightness(),
                    sample.colorBand().scale(),
                    sample.lifetime(),
                    sample.core(),
                    sample.twinkles());
            this.createdParticles++;
        }
    }

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Midsize sphere request position must be finite");
            }
        }
    }
}
