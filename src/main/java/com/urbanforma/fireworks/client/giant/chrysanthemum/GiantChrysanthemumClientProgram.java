package com.urbanforma.fireworks.client.giant.chrysanthemum;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.chrysanthemum.GiantChrysanthemumTrajectory;
import com.urbanforma.fireworks.content.giant.chrysanthemum.GiantChrysanthemumTrajectory.Branch;
import com.urbanforma.fireworks.content.giant.chrysanthemum.GiantChrysanthemumTrajectory.BranchSample;
import com.urbanforma.fireworks.content.giant.chrysanthemum.GiantChrysanthemumTrajectory.ColorBand;
import com.urbanforma.fireworks.content.giant.chrysanthemum.GiantChrysanthemumTrajectory.RetirementFlicker;
import com.urbanforma.fireworks.content.giant.chrysanthemum.GiantChrysanthemumTrajectory.Shell;
import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only visual program for one giant chrysanthemum request.
 *
 * <p>The only allocation loop is the finite shell reveal. The retirement phase updates a bounded subset of existing
 * sparks and never allocates new particles, entities, packets, or explosion points.</p>
 */
public final class GiantChrysanthemumClientProgram {
    private final Request request;
    private final Branch[][] branches;
    private final ArrayDeque<RetirementSpark> retirementSparks = new ArrayDeque<>(
            GiantChrysanthemumTrajectory.MAX_TRACKED_RETIREMENT_SPARKS);
    private int age;
    private int createdParticles;

    public GiantChrysanthemumClientProgram(Request request) {
        this.request = request;
        Shell[] shells = Shell.values();
        this.branches = new Branch[shells.length][];
        for (Shell shell : shells) {
            Branch[] shellBranches = new Branch[shell.branchCount()];
            for (int index = 0; index < shellBranches.length; index++) {
                shellBranches[index] = GiantChrysanthemumTrajectory.branch(request.seed(), shell, index);
            }
            this.branches[shell.ordinal()] = shellBranches;
        }
    }

    /** Returns true after the bounded particle population and its late retirement state have elapsed. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }
        this.updateRetirementFlicker();
        if (this.age <= GiantChrysanthemumTrajectory.MAX_EMISSION_TICK) {
            this.emitShellsAtTick(minecraft, this.age);
        }
        this.age++;
        return this.age >= GiantChrysanthemumTrajectory.TOTAL_VISUAL_TICKS;
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

    public int trackedRetirementSparkCount() {
        return this.retirementSparks.size();
    }

    public boolean isEmitting() {
        return this.age <= GiantChrysanthemumTrajectory.MAX_EMISSION_TICK;
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantChrysanthemumTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    private void emitShellsAtTick(Minecraft minecraft, int emissionTick) {
        for (Shell shell : Shell.values()) {
            int segmentIndex = emissionTick - shell.startTick();
            if (segmentIndex < 0 || segmentIndex >= shell.segmentsPerBranch()) {
                continue;
            }
            for (Branch branch : this.branches[shell.ordinal()]) {
                this.emitSpark(minecraft, GiantChrysanthemumTrajectory.sample(branch, segmentIndex));
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
        this.applyAppearance(spark, sample);
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setLifetime(sample.lifetime());
        if (GiantChrysanthemumTrajectory.tracksRetirement(sample)
                && this.retirementSparks.size() < GiantChrysanthemumTrajectory.MAX_TRACKED_RETIREMENT_SPARKS) {
            RetirementFlicker flicker = GiantChrysanthemumTrajectory.retirementFlicker(sample);
            this.trackRetirement(spark, flicker, sample.lifetime());
        }
        this.createdParticles++;
    }

    private void trackRetirement(Particle spark, RetirementFlicker flicker, int originalLifetime) {
        if (this.retirementSparks.size() >= GiantChrysanthemumTrajectory.MAX_TRACKED_RETIREMENT_SPARKS) {
            return;
        }
        this.retirementSparks.addLast(new RetirementSpark(
                spark, flicker.startAge(), flicker.cadencePhase(), originalLifetime));
    }

    private void applyAppearance(Particle spark, BranchSample sample) {
        ColorBand colorBand = sample.colorBand();
        GiantChrysanthemumTrajectory.Rgb rgb = colorBand.rgb();
        if (colorBand == ColorBand.HEART_PEARL) {
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
        FireworkParticleAppearance.applyVisibilityScale(spark, colorBand.scale());
    }

    /** Starts twinkle only near retirement on a finite subset of existing sparks. */
    private void updateRetirementFlicker() {
        Iterator<RetirementSpark> iterator = this.retirementSparks.iterator();
        while (iterator.hasNext()) {
            RetirementSpark tracked = iterator.next();
            if (!tracked.spark().isAlive()) {
                iterator.remove();
                continue;
            }
            if (tracked.flickerStarted() || tracked.spark().age < tracked.startAge()) {
                continue;
            }
            if (tracked.spark() instanceof FireworkParticles.SparkParticle spark) {
                spark.setTwinkle(true);
                if (tracked.cadencePhase() == 1) {
                    tracked.spark().age++;
                    tracked.spark().setLifetime(tracked.originalLifetime() + 1);
                }
            }
            tracked.markFlickerStarted();
        }
    }

    private static final class RetirementSpark {
        private final Particle spark;
        private final int startAge;
        private final int cadencePhase;
        private final int originalLifetime;
        private boolean flickerStarted;

        private RetirementSpark(Particle spark, int startAge, int cadencePhase, int originalLifetime) {
            this.spark = spark;
            this.startAge = startAge;
            this.cadencePhase = cadencePhase;
            this.originalLifetime = originalLifetime;
        }

        private Particle spark() {
            return this.spark;
        }

        private int startAge() {
            return this.startAge;
        }

        private int cadencePhase() {
            return this.cadencePhase;
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
                throw new IllegalArgumentException("Chrysanthemum request position must be finite");
            }
        }
    }
}
