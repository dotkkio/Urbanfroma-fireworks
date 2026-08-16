package com.urbanforma.fireworks.content;

import net.minecraft.world.phys.Vec3;

/**
 * Bounded, seed-derived launch path shared by the rocket authority and the physical client.
 *
 * <p>The profile is created once when a rocket is launched. It has no particle state and no mutable random source:
 * a tracking client can reconstruct the same point for a given normalized flight age from the synchronized seed.
 * The height mapping deliberately gives a 40-block envelope a 30-50 block launch band and every 180-block-or-larger
 * envelope a 150-190 block launch band.</p>
 */
public final class FireworkAscentTrajectory {
    private static final double LOW_BASE_HEIGHT = 40.0D;
    private static final double HIGH_BASE_HEIGHT = 180.0D;
    private static final double LOW_MIN_HEIGHT = 30.0D;
    private static final double LOW_HEIGHT_SPAN = 20.0D;
    private static final double HIGH_MIN_HEIGHT = 150.0D;
    private static final double HIGH_HEIGHT_SPAN = 40.0D;
    private static final long HEIGHT_SALT = 0x2D358DCCAA6C78A5L;
    private static final long LANDING_ANGLE_SALT = 0x8EBC6AF09C88C6E3L;
    private static final long LANDING_RADIUS_SALT = 0x4CF5AD432745937FL;
    private static final long SWAY_PHASE_SALT = 0x9E3779B97F4A7C15L;
    private static final long SWAY_FREQUENCY_SALT = 0xD6E8FEB86659FD93L;

    private FireworkAscentTrajectory() {
    }

    public record Profile(float targetHeight, float landingOffsetX, float landingOffsetZ, float swayPhase, float swayFrequency) {
        public Profile {
            if (!Float.isFinite(targetHeight) || targetHeight <= 0.0F
                    || !Float.isFinite(landingOffsetX) || !Float.isFinite(landingOffsetZ)
                    || !Float.isFinite(swayPhase) || !Float.isFinite(swayFrequency) || swayFrequency <= 0.0F) {
                throw new IllegalArgumentException("Firework ascent profile must be finite and positive");
            }
        }
    }

    public static Profile profile(FireworkStyle style, long seed) {
        if (style == null) {
            throw new IllegalArgumentException("Firework style is required");
        }
        double baseHeight = baseHeight(style);
        boolean highAltitude = baseHeight == HIGH_BASE_HEIGHT;
        // The seeded unit interval is deliberately bounded: high launches are always in 150..190 and
        // low launches in 30..50. These bands are content classes, not visual-envelope/lifetime scaling.
        double targetHeight = highAltitude
                ? HIGH_MIN_HEIGHT + HIGH_HEIGHT_SPAN * unit(seed ^ HEIGHT_SALT)
                : LOW_MIN_HEIGHT + LOW_HEIGHT_SPAN * unit(seed ^ HEIGHT_SALT);
        double angle = unit(seed ^ LANDING_ANGLE_SALT) * Math.PI * 2.0D;
        double heightScale = highAltitude ? 1.0D : 0.0D;
        double landingRadius = (0.35D + 2.25D * heightScale)
                * (0.72D + 0.28D * unit(seed ^ LANDING_RADIUS_SALT));
        return new Profile(
                (float) targetHeight,
                (float) (Math.cos(angle) * landingRadius),
                (float) (Math.sin(angle) * landingRadius),
                (float) (unit(seed ^ SWAY_PHASE_SALT) * Math.PI * 2.0D),
                (float) (0.72D + 0.55D * unit(seed ^ SWAY_FREQUENCY_SALT)));
    }

    /**
     * Height is selected from the content class, never from a visual envelope or particle lifetime. Large and giant
     * launch programs share the approved 180-block class; ordinary and small programs use the 40-block class.
     */
    public static double baseHeight(FireworkStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("Firework style is required");
        }
        String id = style.id();
        return style.giantTier() != com.urbanforma.fireworks.content.GiantTier.NONE
                || style.shape() == FireworkStyle.Shape.GIANT_RADIANT
                || id.startsWith("large_")
                ? HIGH_BASE_HEIGHT
                : LOW_BASE_HEIGHT;
    }

    /** Number of adjacent arc segments used for one server collision sweep. */
    public static int collisionSegments(Profile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Firework ascent profile is required");
        }
        return profile.targetHeight() >= HIGH_MIN_HEIGHT ? 8 : 3;
    }

    /** Returns a launch point relative to the initial rocket position for {@code progress} in [0, 1]. */
    public static Vec3 offset(Profile profile, double progress) {
        if (profile == null) {
            throw new IllegalArgumentException("Firework ascent profile is required");
        }
        double t = clamp(progress, 0.0D, 1.0D);
        double rise = smootherStep(t);
        double heightBand = profile.targetHeight() >= HIGH_MIN_HEIGHT ? 1.0D : 0.0D;
        double sideAmplitude = 0.08D + 0.42D * heightBand;
        double sideWave = Math.sin((Math.PI * 2.0D * profile.swayFrequency() * t) + profile.swayPhase())
                * Math.sin(Math.PI * t);
        double verticalWave = Math.sin((Math.PI * 2.0D * (profile.swayFrequency() * 0.75D) * t)
                        + profile.swayPhase() * 1.37D)
                * Math.sin(Math.PI * t)
                * (0.08D + 0.38D * heightBand);
        double lateralProgress = rise + sideWave * 0.035D;
        return new Vec3(
                profile.landingOffsetX() * lateralProgress - profile.landingOffsetZ() * sideWave * sideAmplitude,
                profile.targetHeight() * rise + verticalWave,
                profile.landingOffsetZ() * lateralProgress + profile.landingOffsetX() * sideWave * sideAmplitude);
    }

    private static double smootherStep(double value) {
        double t = clamp(value, 0.0D, 1.0D);
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }

    private static double unit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
