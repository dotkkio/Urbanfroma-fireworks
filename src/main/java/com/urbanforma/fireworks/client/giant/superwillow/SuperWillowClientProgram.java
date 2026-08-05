package com.urbanforma.fireworks.client.giant.superwillow;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.superwillow.SuperWillowTrajectory;
import com.urbanforma.fireworks.content.giant.superwillow.SuperWillowTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.superwillow.SuperWillowTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.superwillow.SuperWillowTrajectory.RetirementFlicker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/** Client-only single-stage super-willow program. The tail phase moves existing sparks and does not allocate more. */
public final class SuperWillowClientProgram {
    private final Request request;
    private final Branch[] branches;
    private final List<RetainedTail> retainedTails = new ArrayList<>(
            SuperWillowTrajectory.BRANCH_COUNT * SuperWillowTrajectory.RETAINED_TAIL_SEGMENT_COUNT);
    private int age;
    private int nextSegment;
    private int createdParticles;

    public SuperWillowClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[SuperWillowTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = SuperWillowTrajectory.branch(request.seed(), index);
        }
    }

    /** Returns true only after the bounded long-tail window and its branch-local retirement flicker have ended. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }

        this.updateRetainedTails();
        if (this.nextSegment < SuperWillowTrajectory.EMISSION_TICKS) {
            this.emitRing(minecraft);
            this.nextSegment++;
        }
        this.age++;
        return this.age >= SuperWillowTrajectory.TOTAL_VISUAL_TICKS;
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
        return this.nextSegment < SuperWillowTrajectory.EMISSION_TICKS;
    }

    public int retainedTailCount() {
        return this.retainedTails.size();
    }

    private void emitRing(Minecraft minecraft) {
        int segmentIndex = this.nextSegment;
        for (Branch branch : this.branches) {
            BranchSample sample = SuperWillowTrajectory.sample(branch, segmentIndex);
            Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
            if (spark == null) {
                continue;
            }

            SuperWillowTrajectory.Rgb rgb = sample.colorBand().rgb();
            boolean coreHighlight = SuperWillowTrajectory.isCoreSegment(segmentIndex);
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
                        1.02F + sample.colorTone() * 0.15F,
                        FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
                FireworkParticleAppearance.applyVisibilityScale(spark, sample.colorBand().scale());
            }
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            spark.setLifetime(sample.lifetime());
            if (sample.retainedTail()) {
                this.retainedTails.add(new RetainedTail(
                        spark,
                        branch,
                        segmentIndex,
                        SuperWillowTrajectory.retirementFlicker(branch, segmentIndex)));
            }
            this.createdParticles++;
        }
    }

    /**
     * Extends only retained particles created by {@link #emitRing(Minecraft)}. Keeping allocation out of this method
     * makes the long fall one continuous willow body rather than a secondary burst or a field of child fireworks.
     */
    private void updateRetainedTails() {
        int continuationAge = Math.max(0, this.age - SuperWillowTrajectory.EMISSION_TICKS);
        int tailAge = Math.min(SuperWillowTrajectory.TAIL_EXTENSION_TICKS, continuationAge);
        Iterator<RetainedTail> iterator = this.retainedTails.iterator();
        while (iterator.hasNext()) {
            RetainedTail retained = iterator.next();
            Particle spark = retained.particle();
            RetirementFlicker flicker = retained.flicker();
            if (!spark.isAlive() || continuationAge > flicker.endTick()) {
                if (spark.isAlive()) {
                    spark.remove();
                }
                iterator.remove();
                continue;
            }

            Vec3 position = SuperWillowTrajectory.tailSample(
                    retained.branch(), retained.segmentIndex(), tailAge).position();
            position = position.add(this.request.x(), this.request.y(), this.request.z());
            spark.setPos(position.x, position.y, position.z);
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            int remaining = Math.max(1, flicker.endTick() - continuationAge + 1);
            if (flicker.activeAt(continuationAge)
                    && spark instanceof FireworkParticles.SparkParticle sparkParticle) {
                sparkParticle.setTwinkle(true);
                // A seeded cadence phase changes the blink rhythm but never extends the bounded end tick.
                spark.setLifetime(spark.age + remaining + flicker.cadencePhase());
            } else {
                spark.setLifetime(spark.age + remaining);
            }
        }
    }

    private record RetainedTail(
            Particle particle, Branch branch, int segmentIndex, RetirementFlicker flicker) {
    }

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Super-willow request position must be finite");
            }
        }
    }
}
