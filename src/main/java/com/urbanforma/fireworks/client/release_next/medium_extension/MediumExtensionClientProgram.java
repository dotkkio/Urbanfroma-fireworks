package com.urbanforma.fireworks.client.release_next.medium_extension;

import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionTrajectory;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;

/** Caller-driven client-only emitter. Integration owns scheduling, payload routing, and invocation. */
public abstract class MediumExtensionClientProgram {
    private final MediumExtensionDefinition definition;
    protected MediumExtensionClientProgram(MediumExtensionDefinition definition) { this.definition = Objects.requireNonNull(definition); }
    public final MediumExtensionDefinition definition() { return definition; }
    public final boolean tick(Minecraft minecraft, Request request, int elapsedTicks) {
        if (elapsedTicks < 0 || elapsedTicks >= definition.emissionTicks() || minecraft.level == null) return false;
        for (int ordinal = 0; ordinal < definition.particlesPerTick(); ordinal++) {
            MediumExtensionTrajectory.Sample sample = MediumExtensionTrajectory.sample(definition, request.seed(), elapsedTicks, ordinal);
            Particle spark = minecraft.particleEngine.createParticle(ParticleTypes.FIREWORK,
                    request.x() + sample.position().x, request.y() + sample.position().y, request.z() + sample.position().z,
                    0.0D, 0.0D, 0.0D);
            if (spark != null) applyAppearance(spark, sample);
        }
        return elapsedTicks + 1 < definition.emissionTicks();
    }

    private void applyAppearance(Particle spark, MediumExtensionTrajectory.Sample sample) {
        float[] color = rgb(sample.colorBand() == 0 ? definition.primary()
                : sample.colorBand() == 1 ? definition.secondary() : definition.accent());
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setColor(color[0], color[1], color[2]);
        spark.setLifetime(sample.lifetime());
        spark.scale(sample.colorBand() == 0 ? 1.08F : sample.colorBand() == 2 ? 0.92F : 1.0F);
        if (sample.colorBand() == 2 && spark instanceof FireworkParticles.SparkParticle fireworkSpark) {
            fireworkSpark.setTwinkle(true);
        }
    }

    private static float[] rgb(String hex) {
        int value = Integer.parseInt(hex.substring(1), 16);
        return new float[] { ((value >>> 16) & 0xFF) / 255.0F, ((value >>> 8) & 0xFF) / 255.0F, (value & 0xFF) / 255.0F };
    }
    public record Request(double x, double y, double z, long seed) {}
}
