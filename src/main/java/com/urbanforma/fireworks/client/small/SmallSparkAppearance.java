package com.urbanforma.fireworks.client.small;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;

/** Client-local appearance helper that prefers the existing HD spark and falls back to vanilla FIREWORK. */
final class SmallSparkAppearance {
    private SmallSparkAppearance() {
    }

    static Particle create(Minecraft minecraft, double x, double y, double z) {
        if (minecraft == null) {
            return null;
        }
        return FireworkParticleAppearance.createSpark(minecraft, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    static void apply(
            Particle particle,
            float red,
            float green,
            float blue,
            float brilliance,
            float baseScale,
            int lifetime,
            boolean core,
            boolean twinkles) {
        if (particle == null
                || !Float.isFinite(red)
                || !Float.isFinite(green)
                || !Float.isFinite(blue)
                || !Float.isFinite(brilliance)
                || brilliance <= 0.0F
                || !Float.isFinite(baseScale)
                || baseScale <= 0.0F
                || lifetime <= 0) {
            throw new IllegalArgumentException("Invalid small-firework particle appearance");
        }
        particle.setParticleSpeed(0.0D, 0.0D, 0.0D);
        FireworkParticleAppearance.applyVividColor(
                particle,
                red,
                green,
                blue,
                brilliance,
                core ? 0.18F : 0.06F);
        FireworkParticleAppearance.applyVisibilityScale(particle, baseScale, core);
        particle.setLifetime(lifetime);
        if (twinkles && particle instanceof FireworkParticles.SparkParticle spark) {
            spark.setTwinkle(true);
        }
    }
}
