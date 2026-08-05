package com.urbanforma.fireworks.client.giant.willow;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.RetirementFlicker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/** Client-only visual program for the second giant: one pure-radial umbrella ring per tick. */
public final class GiantWillowClientProgram {
    private final Request request;
    private final Branch[] branches;
    private final List<RetainedTail> retainedTails = new ArrayList<>(
            GiantWillowTrajectory.BRANCH_COUNT * GiantWillowTrajectory.RETAINED_TAIL_SEGMENT_COUNT);
    private int age;
    private int nextSegment;
    private int createdParticles;

    public GiantWillowClientProgram(Request request) {
        this.request = request;
        this.branches = new Branch[GiantWillowTrajectory.BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = GiantWillowTrajectory.branch(request.seed(), index);
        }
    }

    /** Returns true only after the 200-tick tail and its final staggered flicker window are over. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }
        this.updateRetainedTails();
        if (this.nextSegment < GiantWillowTrajectory.EMISSION_TICKS) {
            this.emitRing(minecraft);
            this.nextSegment++;
        }
        this.age++;
        return this.age >= GiantWillowTrajectory.TOTAL_VISUAL_TICKS;
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
        return this.nextSegment < GiantWillowTrajectory.EMISSION_TICKS;
    }

    public int retainedTailCount() {
        return this.retainedTails.size();
    }

    private void emitRing(Minecraft minecraft) {
        int segmentIndex = this.nextSegment;
        for (Branch branch : this.branches) {
            BranchSample sample = GiantWillowTrajectory.sample(branch, segmentIndex);
            Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
            if (spark == null) {
                continue;
            }

            GiantWillowTrajectory.Rgb rgb = sample.colorBand().rgb();
            boolean coreHighlight = segmentIndex < 8;
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
                        1.02F + sample.colorTone() * 0.14F,
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
                        GiantWillowTrajectory.retirementFlicker(branch, segmentIndex)));
            }
            this.createdParticles++;
        }
    }

    /** Moves existing outer sparks only; this phase never allocates new particles. */
    private void updateRetainedTails() {
        int continuationAge = Math.max(0, this.age - GiantWillowTrajectory.EMISSION_TICKS);
        int tailAge = Math.min(
                GiantWillowTrajectory.TAIL_EXTENSION_TICKS,
                continuationAge);
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

            Vec3 position = GiantWillowTrajectory.tailSample(
                    retained.branch(), retained.segmentIndex(), tailAge).position();
            position = position.add(this.request.x(), this.request.y(), this.request.z());
            spark.setPos(position.x, position.y, position.z);
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            int remaining = Math.max(1, flicker.endTick() - continuationAge + 1);
            if (flicker.activeAt(continuationAge)
                    && spark instanceof FireworkParticles.SparkParticle sparkParticle) {
                sparkParticle.setTwinkle(true);
                // A three-tick offset changes the final cadence without moving the disappearance boundary.
                spark.setLifetime(spark.age + remaining + flicker.cadencePhase() * 3);
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
                throw new IllegalArgumentException("Giant willow request position must be finite");
            }
        }
    }
}
