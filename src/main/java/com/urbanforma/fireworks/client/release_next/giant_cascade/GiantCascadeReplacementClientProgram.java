package com.urbanforma.fireworks.client.release_next.giant_cascade;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.giant_cascade.GiantCascadeReplacementTrajectory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;

/** Client-only, bounded realization of the cascade replacement contract. */
public final class GiantCascadeReplacementClientProgram {
    private final Request request;
    private final List<TrackedSpark> tracked = new ArrayList<>(GiantCascadeReplacementTrajectory.TOTAL_PARTICLES);
    private int age;
    private int createdParticles;

    public GiantCascadeReplacementClientProgram(Request request) { this.request = request; }

    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) return false;
        updateFlicker();
        if (GiantCascadeReplacementTrajectory.parentEmitsAt(age)) {
            for (int branch = 0; branch < GiantCascadeReplacementTrajectory.PARENT_BRANCHES; branch++) {
                emit(minecraft, GiantCascadeReplacementTrajectory.parentSample(request.seed(), branch, age));
            }
        }
        for (int burst = 0; burst < GiantCascadeReplacementTrajectory.CHILD_BURSTS; burst++) {
            if (GiantCascadeReplacementTrajectory.childEmitsAt(age, burst)) emitCompleteChildShell(minecraft, burst);
        }
        age++;
        return age >= GiantCascadeReplacementTrajectory.TOTAL_VISUAL_TICKS;
    }

    public int createdParticleCount() { return createdParticles; }
    public int expectedCreatedParticleCount() { return GiantCascadeReplacementTrajectory.particlesCreatedThroughTick(age - 1); }
    public int trackedParticleCount() { return tracked.size(); }

    private void emitCompleteChildShell(Minecraft minecraft, int burst) {
        for (int branch = 0; branch < GiantCascadeReplacementTrajectory.CHILD_BRANCHES; branch++) {
            for (int depth = 0; depth < GiantCascadeReplacementTrajectory.CHILD_DEPTHS; depth++) {
                emit(minecraft, GiantCascadeReplacementTrajectory.childSample(request.seed(), burst, branch, depth));
            }
        }
    }

    private void emit(Minecraft minecraft, GiantCascadeReplacementTrajectory.Sample sample) {
        Particle spark = FireworkParticleAppearance.createSpark(minecraft, request.x() + sample.position().x,
                request.y() + sample.position().y, request.z() + sample.position().z, 0.0D, 0.0D, 0.0D);
        if (spark == null) return;
        if (sample.core()) {
            FireworkParticleAppearance.applyCoreColor(spark, sample.color().red(), sample.color().green(), sample.color().blue());
            FireworkParticleAppearance.applyVisibilityScale(spark, FireworkParticleAppearance.CORE_BASE_SCALE, true);
        } else {
            FireworkParticleAppearance.applyVividColor(spark, sample.color().red(), sample.color().green(), sample.color().blue(),
                    1.08F, FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
            FireworkParticleAppearance.applyVisibilityScale(spark, sample.color().scale());
        }
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setLifetime(sample.lifetime());
        tracked.add(new TrackedSpark(spark, sample.lifetime() - 18, sample.lifetime()));
        createdParticles++;
    }

    private void updateFlicker() {
        Iterator<TrackedSpark> iterator = tracked.iterator();
        while (iterator.hasNext()) {
            TrackedSpark spark = iterator.next();
            if (!spark.particle.isAlive()) { iterator.remove(); continue; }
            if (!spark.flicker && spark.particle.age >= spark.flickerStart && spark.particle instanceof FireworkParticles.SparkParticle firework) {
                firework.setTwinkle(true);
                spark.flicker = true;
            }
        }
    }

    private static final class TrackedSpark {
        private final Particle particle;
        private final int flickerStart;
        @SuppressWarnings("unused") private final int lifetime;
        private boolean flicker;
        private TrackedSpark(Particle particle, int flickerStart, int lifetime) {
            this.particle = particle; this.flickerStart = flickerStart; this.lifetime = lifetime;
        }
    }

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Non-finite cascade request");
        }
    }
}
