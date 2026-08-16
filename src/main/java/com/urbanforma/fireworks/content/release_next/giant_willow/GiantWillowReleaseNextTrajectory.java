package com.urbanforma.fireworks.content.release_next.giant_willow;

import java.util.Arrays;
import net.minecraft.world.phys.Vec3;

/**
 * Pure deterministic geometry for the release-next giant/super willow set.
 *
 * <p>This common-side type deliberately has no clock, particle allocation, network access, or server tick work.
 * A client program samples its finite branches after receiving a normal explosion payload.  Every profile is a
 * volumetric, downward-curving canopy rather than a planar fan or a moving A-to-B trail.</p>
 */
public final class GiantWillowReleaseNextTrajectory {
    /** Horizontal canopy radius; vertical fall is intentionally budgeted separately. */
    public static final double MAX_HORIZONTAL_RADIUS = 126.0D;
    /** Largest possible three-dimensional displacement from the detonation origin. */
    public static final double MAX_ENVELOPE_DISTANCE = 182.0D;
    public static final int MAX_CLIENT_PARTICLES_PER_TICK = 160;
    public static final int MIN_LIFETIME = 72;
    public static final int MAX_LIFETIME = 128;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

    private GiantWillowReleaseNextTrajectory() {
    }

    public enum Profile {
        IMPERIAL_CANOPY("giant_imperial_canopy_willow", 144, 26, 0, 114.0D, 88.0D, 0.35D, 0.0D, 3744),
        SILVER_WATERFALL("giant_silver_waterfall_willow", 120, 30, 6, 102.0D, 108.0D, 0.08D, -8.0D, 3600),
        EMERALD_BRAID("giant_emerald_braided_willow", 96, 34, 12, 112.0D, 92.0D, 2.75D, 4.0D, 3264),
        AMBER_FOUNTAIN("giant_amber_fountain_willow", 128, 24, 3, 98.0D, 70.0D, 0.85D, 26.0D, 3072),
        VIOLET_RAIN_CURTAIN("giant_violet_rain_curtain_willow", 160, 24, 18, 120.0D, 116.0D, 0.18D, -14.0D, 3840);

        private final String stableId;
        private final int branches;
        private final int segments;
        private final int startTick;
        private final double radius;
        private final double drop;
        private final double twistTurns;
        private final double lift;
        private final int declaredParticles;

        Profile(String stableId, int branches, int segments, int startTick, double radius, double drop,
                double twistTurns, double lift, int declaredParticles) {
            this.stableId = stableId;
            this.branches = branches;
            this.segments = segments;
            this.startTick = startTick;
            this.radius = radius;
            this.drop = drop;
            this.twistTurns = twistTurns;
            this.lift = lift;
            this.declaredParticles = declaredParticles;
        }

        public String stableId() { return stableId; }
        public int branches() { return branches; }
        public int segments() { return segments; }
        public int startTick() { return startTick; }
        public double radius() { return radius; }
        public double drop() { return drop; }
        public double twistTurns() { return twistTurns; }
        public double lift() { return lift; }
        public int declaredParticles() { return declaredParticles; }
    }

    public enum ColorBand {
        IMPERIAL_GOLD(1.0F, 0.66F, 0.16F, 1.42F),
        SILVER(0.82F, 0.93F, 1.0F, 1.34F),
        EMERALD(0.20F, 1.0F, 0.58F, 1.37F),
        AMBER(1.0F, 0.42F, 0.08F, 1.38F),
        VIOLET(0.70F, 0.34F, 1.0F, 1.35F),
        PEARL(1.0F, 0.94F, 0.76F, 1.48F);

        private final float red;
        private final float green;
        private final float blue;
        private final float scale;

        ColorBand(float red, float green, float blue, float scale) {
            this.red = red; this.green = green; this.blue = blue; this.scale = scale;
        }
        public float red() { return red; }
        public float green() { return green; }
        public float blue() { return blue; }
        public float scale() { return scale; }
    }

    public record Sample(Vec3 position, ColorBand color, int lifetime, boolean core) {
        public Sample {
            if (position == null || position.lengthSqr() > MAX_ENVELOPE_DISTANCE * MAX_ENVELOPE_DISTANCE + 1.0E-7D
                    || lifetime < MIN_LIFETIME || lifetime > MAX_LIFETIME) {
                throw new IllegalArgumentException("Invalid giant willow sample");
            }
        }
    }

    public static boolean emitsAt(Profile profile, int tick) {
        return tick >= profile.startTick() && tick < profile.startTick() + profile.segments();
    }

    public static int segmentAt(Profile profile, int tick) {
        if (!emitsAt(profile, tick)) throw new IllegalArgumentException("Tick outside giant willow emission window");
        return tick - profile.startTick();
    }

    public static int particlesAtTick(Profile profile, int tick) { return emitsAt(profile, tick) ? profile.branches() : 0; }
    public static int totalParticles(Profile profile) { return profile.branches() * profile.segments(); }
    public static int lastEmissionTick(Profile profile) { return profile.startTick() + profile.segments() - 1; }
    public static int totalVisualTicks(Profile profile) { return lastEmissionTick(profile) + MAX_LIFETIME + 1; }

    /** Samples a whole falling branch at a fixed emission phase; it never advances a server-side trajectory. */
    public static Sample sample(long seed, Profile profile, int branchIndex, int segmentIndex) {
        if (branchIndex < 0 || branchIndex >= profile.branches() || segmentIndex < 0 || segmentIndex >= profile.segments()) {
            throw new IllegalArgumentException("Giant willow index outside profile contract");
        }
        double progress = profile.segments() == 1 ? 1.0D : (double) segmentIndex / (profile.segments() - 1);
        long branchSeed = mix64(seed ^ ((long) profile.ordinal() + 1L) * 0xD6E8FEB86659FD93L
                ^ (long) branchIndex * 0x9E3779B97F4A7C15L);
        double latitude = Math.asin(clamp(1.0D - 2.0D * ((branchIndex + 0.5D) / profile.branches()), -0.82D, 0.82D));
        double azimuth = branchIndex * GOLDEN_ANGLE + unit(branchSeed) * 0.26D
                + profile.twistTurns() * TWO_PI * progress;
        double canopy = profile.radius() * (0.20D + 0.80D * smooth(progress));
        double horizontalFactor = Math.cos(latitude) * (0.78D + 0.22D * Math.sin(Math.PI * progress));
        double horizontal = canopy * horizontalFactor;
        double profileLift = profile == Profile.AMBER_FOUNTAIN ? profile.lift() * Math.sin(Math.PI * progress) : profile.lift() * progress;
        double droop = profile.drop() * progress * progress * (0.64D + 0.36D * Math.abs(Math.sin(latitude)));
        double y = Math.sin(latitude) * canopy * 0.34D + profileLift - droop;
        Vec3 position = new Vec3(Math.cos(azimuth) * horizontal, y, Math.sin(azimuth) * horizontal);
        ColorBand color = color(profile, progress);
        int lifetime = MIN_LIFETIME + bounded(branchSeed ^ (long) segmentIndex * 0x94D049BB133111EBL,
                MAX_LIFETIME - MIN_LIFETIME + 1);
        return new Sample(position, color, lifetime, segmentIndex < 3 && Math.floorMod(branchIndex, 4) == 0);
    }

    public static void validateAllProfiles() { Arrays.stream(Profile.values()).forEach(GiantWillowReleaseNextTrajectory::validateProfile); }
    public static void validateProfile(Profile profile) {
        if (profile == null || profile.branches() > MAX_CLIENT_PARTICLES_PER_TICK || totalParticles(profile) != profile.declaredParticles()
                || profile.radius() > MAX_HORIZONTAL_RADIUS || profile.drop() > MAX_ENVELOPE_DISTANCE || totalVisualTicks(profile) > 190) {
            throw new IllegalStateException("Giant willow profile contract drifted: " + profile);
        }
        for (int branch = 0; branch < profile.branches(); branch++) {
            for (int segment = 0; segment < profile.segments(); segment++) sample(0x1A2B3C4D5E6F7081L, profile, branch, segment);
        }
    }

    private static ColorBand color(Profile profile, double progress) {
        if (progress < 0.12D) return ColorBand.PEARL;
        return switch (profile) {
            case IMPERIAL_CANOPY -> ColorBand.IMPERIAL_GOLD;
            case SILVER_WATERFALL -> ColorBand.SILVER;
            case EMERALD_BRAID -> ColorBand.EMERALD;
            case AMBER_FOUNTAIN -> ColorBand.AMBER;
            case VIOLET_RAIN_CURTAIN -> ColorBand.VIOLET;
        };
    }
    private static double smooth(double value) { return value * value * (3.0D - 2.0D * value); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    private static int bounded(long value, int bound) { return (int) Math.floor(unit(value) * bound); }
    private static double unit(long value) { return (mix64(value) >>> 11) * 0x1.0p-53D; }
    private static long mix64(long value) {
        value += 0x9E3779B97F4A7C15L;
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }
}
