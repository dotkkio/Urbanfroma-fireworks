package com.urbanforma.fireworks.client.midsize;

import com.urbanforma.fireworks.content.midsize.MidsizeDenseRadialTrajectory;
import com.urbanforma.fireworks.content.midsize.MidsizeDenseRadialTrajectory.Branch;
import com.urbanforma.fireworks.content.midsize.MidsizeDenseRadialTrajectory.BranchSample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/** Caller-driven physical-client program for one midsize dense radial event. */
public final class MidsizeDenseRadialClientProgram {
    private final Request request;
    private final Branch[] branches;
    private int age;
    private int nextSegment;
    private int createdParticles;

    public MidsizeDenseRadialClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[MidsizeDenseRadialTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = MidsizeDenseRadialTrajectory.branch(request.seed(), index);
        }
    }

    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft == null ? null : minecraft.level;
        if (level == null) {
            return false;
        }
        if (this.nextSegment < MidsizeDenseRadialTrajectory.EMISSION_TICKS) {
            emitRing(minecraft, this.nextSegment);
            this.nextSegment++;
        }
        this.age++;
        return this.age >= MidsizeDenseRadialTrajectory.TOTAL_VISUAL_TICKS;
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
        return MidsizeDenseRadialTrajectory.TOTAL_PARTICLES;
    }

    public boolean isEmitting() {
        return this.nextSegment < MidsizeDenseRadialTrajectory.EMISSION_TICKS;
    }

    private void emitRing(Minecraft minecraft, int segmentIndex) {
        for (Branch branch : this.branches) {
            BranchSample sample = MidsizeDenseRadialTrajectory.sample(this.request.seed(), branch, segmentIndex);
            Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            Particle spark = MidsizeSparkAppearance.create(minecraft, position.x, position.y, position.z);
            if (spark == null) {
                continue;
            }
            MidsizeDenseRadialTrajectory.Rgb color = sample.colorBand().rgb();
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
                throw new IllegalArgumentException("Midsize radial request position must be finite");
            }
        }
    }
}
