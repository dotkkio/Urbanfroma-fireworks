package com.urbanforma.fireworks.client.giant.cometfield;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory.ColorBand;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory.Comet;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory.CometSample;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory.CoreSample;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory.Current;
import com.urbanforma.fireworks.content.giant.cometfield.GiantCometfieldTrajectory.RetirementFlicker;
import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only visual program for one giant interlaced comet-field request.
 *
 * <p>Its bounded nucleus and curved traces are reconstructed only from the one received seed. The retirement phase
 * changes a finite subset of existing sparks and creates no new particles, entities, packets, or explosion points.</p>
 */
public final class GiantCometfieldClientProgram {
    private final Request request;
    private final Comet[][] comets;
    private final ArrayDeque<RetirementSpark> retirementSparks = new ArrayDeque<>(
            GiantCometfieldTrajectory.MAX_TRACKED_RETIREMENT_SPARKS);
    private int age;
    private int createdParticles;

    public GiantCometfieldClientProgram(Request request) {
        this.request = request;
        Current[] currents = Current.values();
        this.comets = new Comet[currents.length][];
        for (Current current : currents) {
            Comet[] currentComets = new Comet[current.cometCount()];
            for (int index = 0; index < currentComets.length; index++) {
                currentComets[index] = GiantCometfieldTrajectory.comet(request.seed(), current, index);
            }
            this.comets[current.ordinal()] = currentComets;
        }
    }

    /** Returns true after the finite nucleus, traces, and retirement window have elapsed. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }
        this.updateRetirementFlicker();
        if (this.age <= GiantCometfieldTrajectory.MAX_EMISSION_TICK) {
            this.emitAtTick(minecraft, this.age);
        }
        this.age++;
        return this.age >= GiantCometfieldTrajectory.TOTAL_VISUAL_TICKS;
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
        return this.age <= GiantCometfieldTrajectory.MAX_EMISSION_TICK;
    }

    public int expectedCreatedParticleCountAtAge() {
        return GiantCometfieldTrajectory.particlesCreatedThroughTick(this.age - 1);
    }

    private void emitAtTick(Minecraft minecraft, int emissionTick) {
        if (emissionTick < GiantCometfieldTrajectory.CORE_EMISSION_TICKS) {
            int firstCoreIndex = emissionTick * GiantCometfieldTrajectory.CORE_PARTICLES_PER_TICK;
            int finalCoreIndex = firstCoreIndex + GiantCometfieldTrajectory.CORE_PARTICLES_PER_TICK;
            for (int coreIndex = firstCoreIndex; coreIndex < finalCoreIndex; coreIndex++) {
                this.emitCore(minecraft, GiantCometfieldTrajectory.coreSample(this.request.seed(), coreIndex));
            }
        }
        for (Current current : Current.values()) {
            for (Comet comet : this.comets[current.ordinal()]) {
                int segmentIndex = emissionTick - comet.emissionStartTick();
                if (segmentIndex >= 0 && segmentIndex < current.segmentsPerComet()) {
                    this.emitComet(minecraft, GiantCometfieldTrajectory.sample(comet, segmentIndex));
                }
            }
        }
    }

    private void emitCore(Minecraft minecraft, CoreSample sample) {
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
        GiantCometfieldTrajectory.Rgb rgb = ColorBand.NUCLEUS_PEARL.rgb();
        FireworkParticleAppearance.applyCoreColor(spark, rgb.red(), rgb.green(), rgb.blue());
        FireworkParticleAppearance.applyVisibilityScale(spark, FireworkParticleAppearance.CORE_BASE_SCALE, true);
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setLifetime(sample.lifetime());
        if (GiantCometfieldTrajectory.tracksRetirement(sample)
                && this.retirementSparks.size() < GiantCometfieldTrajectory.MAX_TRACKED_RETIREMENT_SPARKS) {
            this.trackRetirement(spark, GiantCometfieldTrajectory.retirementFlicker(sample), sample.lifetime());
        }
        this.createdParticles++;
    }

    private void emitComet(Minecraft minecraft, CometSample sample) {
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
        ColorBand colorBand = sample.colorBand();
        GiantCometfieldTrajectory.Rgb rgb = colorBand.rgb();
        FireworkParticleAppearance.applyVividColor(
                spark,
                rgb.red(),
                rgb.green(),
                rgb.blue(),
                sample.brilliance(),
                FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
        FireworkParticleAppearance.applyVisibilityScale(spark, colorBand.scale());
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setLifetime(sample.lifetime());
        if (GiantCometfieldTrajectory.tracksRetirement(sample)
                && this.retirementSparks.size() < GiantCometfieldTrajectory.MAX_TRACKED_RETIREMENT_SPARKS) {
            this.trackRetirement(spark, GiantCometfieldTrajectory.retirementFlicker(sample), sample.lifetime());
        }
        this.createdParticles++;
    }

    private void trackRetirement(Particle spark, RetirementFlicker flicker, int originalLifetime) {
        if (this.retirementSparks.size() >= GiantCometfieldTrajectory.MAX_TRACKED_RETIREMENT_SPARKS) {
            return;
        }
        this.retirementSparks.addLast(new RetirementSpark(
                spark, flicker.startAge(), flicker.cadencePhase(), originalLifetime));
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
                throw new IllegalArgumentException("Comet-field request position must be finite");
            }
        }
    }
}
