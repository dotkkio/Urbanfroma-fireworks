package com.urbanforma.fireworks.client.giant.willow;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.RetirementFlicker;
import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory.Stage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/** Client-only program for one stable EXTRA_LARGE giant burst. */
public final class GiantWillowClientProgram {
    private final Request request;
    private final Branch[][] branches = new Branch[Stage.values().length][];
    private final List<RetainedTail> retainedTails = new ArrayList<>(GiantWillowTrajectory.MAX_RETAINED_TAILS);
    private int age;
    private int createdParticles;

    public GiantWillowClientProgram(Request request) {
        this.request = request;
        for (Stage stage : Stage.values()) {
            Branch[] stageBranches = new Branch[stage.branchCount()];
            for (int index = 0; index < stageBranches.length; index++) {
                stageBranches[index] = GiantWillowTrajectory.branch(request.seed(), stage, index);
            }
            this.branches[stage.index()] = stageBranches;
        }
    }

    /** Returns true after the finite 271-tick visual lifecycle has reached its final retirement frame. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }
        this.updateRetainedTails();
        for (Stage stage : Stage.values()) {
            if (stage.emitsAt(this.age)) {
                this.emitStage(minecraft, stage, this.age - stage.startTick());
            }
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

    /** Compatibility view of the completed outer-shell radial samples. */
    public int emittedSegments() {
        return Math.min(this.age, GiantWillowTrajectory.EMISSION_TICKS);
    }

    public int createdParticleCount() {
        return this.createdParticles;
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantWillowTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    public boolean isEmitting() {
        return this.age < GiantWillowTrajectory.EMISSION_TICKS;
    }

    public int retainedTailCount() {
        return this.retainedTails.size();
    }

    private void emitStage(Minecraft minecraft, Stage stage, int segmentIndex) {
        Branch[] stageBranches = this.branches[stage.index()];
        for (Branch branch : stageBranches) {
            BranchSample sample = GiantWillowTrajectory.sample(branch, segmentIndex);
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

    private static void applyAppearance(Particle spark, BranchSample sample) {
        GiantWillowTrajectory.Rgb rgb = sample.colorBand().rgb();
        if (sample.branch().stage() == Stage.WHITE_CORE) {
            FireworkParticleAppearance.applyCoreColor(spark, rgb.red(), rgb.green(), rgb.blue());
            FireworkParticleAppearance.applyVisibilityScale(
                    spark, FireworkParticleAppearance.CORE_BASE_SCALE, true);
            return;
        }
        float brilliance = sample.branch().stage() == Stage.RADIANT_CROWN
                ? 1.10F + sample.colorTone() * 0.16F
                : 1.02F + sample.colorTone() * 0.18F;
        FireworkParticleAppearance.applyVividColor(
                spark,
                rgb.red(),
                rgb.green(),
                rgb.blue(),
                brilliance,
                FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
        FireworkParticleAppearance.applyVisibilityScale(spark, sample.colorBand().scale());
    }

    /** The tail phase only repositions already-created outer strands and creates no particles. */
    private void updateRetainedTails() {
        int continuationAge = Math.max(0, this.age - GiantWillowTrajectory.TAIL_START_TICK);
        int tailAge = Math.min(GiantWillowTrajectory.TAIL_EXTENSION_TICKS, continuationAge);
        Iterator<RetainedTail> iterator = this.retainedTails.iterator();
        while (iterator.hasNext()) {
            RetainedTail retained = iterator.next();
            Particle spark = retained.particle();
            RetirementFlicker flicker = retained.flicker();
            if (!spark.isAlive() || continuationAge >= flicker.endTick()
                    || this.age + 1 >= GiantWillowTrajectory.TOTAL_VISUAL_TICKS) {
                if (spark.isAlive()) {
                    spark.remove();
                }
                iterator.remove();
                continue;
            }
            if (this.age < GiantWillowTrajectory.TAIL_START_TICK) {
                continue;
            }
            Vec3 position = GiantWillowTrajectory.tailSample(
                    retained.branch(), retained.segmentIndex(), tailAge).position();
            position = position.add(this.request.x(), this.request.y(), this.request.z());
            spark.setPos(position.x, position.y, position.z);
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            int remaining = Math.max(1, flicker.endTick() - continuationAge);
            if (flicker.activeAt(continuationAge)
                    && spark instanceof FireworkParticles.SparkParticle sparkParticle) {
                sparkParticle.setTwinkle(true);
                spark.setLifetime(spark.age + remaining + flicker.cadencePhase() * 2);
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
