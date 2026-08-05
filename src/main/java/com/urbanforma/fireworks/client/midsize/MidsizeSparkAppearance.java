package com.urbanforma.fireworks.client.midsize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;

/** Local vanilla-FIREWORK-only appearance helper for the isolated midsize programs. */
final class MidsizeSparkAppearance {
    private static final float CORE_SCALE_MULTIPLIER = 1.14F;

    private MidsizeSparkAppearance() {
    }

    static Particle create(Minecraft minecraft, double x, double y, double z) {
        if (minecraft == null) {
            return null;
        }
        try {
            return minecraft.particleEngine.createParticle(ParticleTypes.FIREWORK, x, y, z, 0.0D, 0.0D, 0.0D);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static void apply(
            Particle particle,
            float red,
            float green,
            float blue,
            float brightness,
            float scale,
            int lifetime,
            boolean core,
            boolean twinkles) {
        if (particle == null || !Float.isFinite(red) || !Float.isFinite(green) || !Float.isFinite(blue)
                || !Float.isFinite(brightness) || brightness < 1.0F || !Float.isFinite(scale) || scale <= 0.0F
                || lifetime <= 0) {
            throw new IllegalArgumentException("Invalid midsize FIREWORK particle appearance");
        }
        particle.setParticleSpeed(0.0D, 0.0D, 0.0D);
        particle.setColor(
                vividChannel(red, brightness, core ? 0.16F : 0.05F),
                vividChannel(green, brightness, core ? 0.16F : 0.05F),
                vividChannel(blue, brightness, core ? 0.16F : 0.05F));
        particle.scale(scale * (core ? CORE_SCALE_MULTIPLIER : 1.0F));
        particle.setLifetime(lifetime);
        if (twinkles && particle instanceof FireworkParticles.SparkParticle spark) {
            spark.setTwinkle(true);
        }
    }

    private static float vividChannel(float channel, float brightness, float whiteLift) {
        return Math.max(0.0F, Math.min(1.0F, channel * brightness + (1.0F - channel) * whiteLift));
    }
}
