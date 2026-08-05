package com.urbanforma.fireworks.client;

import com.urbanforma.fireworks.registry.FireworksParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;

/** Shared visibility adjustment for the existing vanilla FIREWORK particle only. */
public final class FireworkParticleAppearance {
    /** Fixed visual styling for finite effect definitions; this is not a runtime particle quota. */
    public static final float VISIBILITY_SCALE = 1.35F;
    public static final float CORE_BASE_SCALE = 1.48F;
    public static final float CORE_COLOR_BRILLIANCE = 1.08F;
    public static final float CORE_COLOR_WHITE_LIFT = 0.20F;
    public static final float OUTER_COLOR_WHITE_LIFT = 0.05F;

    /* A malformed or not-yet-reloaded sprite set must never crash the client tick loop. */
    private static boolean hdSparkUnavailable;

    private FireworkParticleAppearance() {
    }

    public static void applyVisibilityScale(Particle particle, float baseScale) {
        applyVisibilityScale(particle, baseScale, false);
    }

    public static void applyVisibilityScale(Particle particle, float baseScale, boolean coreHighlight) {
        if (particle == null || !Float.isFinite(baseScale) || baseScale <= 0.0F) {
            throw new IllegalArgumentException("FIREWORK particle base scale must be finite and positive");
        }
        particle.scale(baseScale * VISIBILITY_SCALE);
    }

    /** Applies bounded vertex-color lift without changing the approved hue or particle type. */
    public static void applyVividColor(
            Particle particle,
            float red,
            float green,
            float blue,
            float brilliance,
            float whiteLift) {
        if (particle == null || !Float.isFinite(brilliance) || brilliance <= 0.0F
                || !Float.isFinite(whiteLift) || whiteLift < 0.0F || whiteLift > 1.0F) {
            throw new IllegalArgumentException("Invalid FIREWORK vertex-color lift");
        }
        particle.setColor(
                vividChannel(red, brilliance, whiteLift),
                vividChannel(green, brilliance, whiteLift),
                vividChannel(blue, brilliance, whiteLift));
    }

    public static void applyCoreColor(Particle particle, float red, float green, float blue) {
        applyVividColor(
                particle,
                red,
                green,
                blue,
                CORE_COLOR_BRILLIANCE,
                CORE_COLOR_WHITE_LIFT);
    }

    /**
     * Allocates a custom spark only while its registered sprite set is healthy. If a resource reload leaves either
     * provider without sprites, the caller receives no particle rather than letting the client tick thread fail.
     */
    public static Particle createSpark(
            Minecraft minecraft, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (!hdSparkUnavailable) {
            try {
                Particle custom = minecraft.particleEngine.createParticle(
                        FireworksParticles.HD_FIREWORK_SPARK.get(), x, y, z, velocityX, velocityY, velocityZ);
                if (custom != null) {
                    return custom;
                }
                hdSparkUnavailable = true;
            } catch (RuntimeException ignored) {
                hdSparkUnavailable = true;
            }
        }
        try {
            return minecraft.particleEngine.createParticle(
                    ParticleTypes.FIREWORK, x, y, z, velocityX, velocityY, velocityZ);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static void resetSparkProviderAvailability() {
        hdSparkUnavailable = false;
    }

    private static float vividChannel(float channel, float brilliance, float whiteLift) {
        if (!Float.isFinite(channel)) {
            throw new IllegalArgumentException("FIREWORK color channels must be finite");
        }
        return Math.max(0.0F, Math.min(1.0F, channel * brilliance + (1.0F - channel) * whiteLift));
    }
}
