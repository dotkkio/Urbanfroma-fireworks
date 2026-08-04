package com.urbanforma.fireworks.content;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, common-side geometry for the v0.2.5 spherical long-willow programs.
 *
 * <p>Every value is derived from the burst payload seed and branch index. The client only needs to schedule the
 * returned samples; the same samples are available to common-side GameTests without loading particle classes.</p>
 */
public final class WillowTrajectory {
    public static final int BRANCH_COUNT = FireworkStyle.WillowProfile.BRANCH_COUNT;
    public static final int SEGMENTS_PER_BRANCH = FireworkStyle.WillowProfile.SEGMENTS_PER_BRANCH;
    public static final int NODES_PER_BURST = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    /** Segment zero is emitted at tick zero, then each following ring is released two ticks later. */
    public static final int SEGMENT_INTERVAL_TICKS = 2;
    /** Inclusive expansion window: 30 rings at ticks 0, 2, ..., 58 (about sixty ticks). */
    public static final int EMISSION_TICKS = (SEGMENTS_PER_BRANCH - 1) * SEGMENT_INTERVAL_TICKS + 1;

    /**
     * The first nine branch rings briefly form the visible break sphere, then expire before
     * the long curtains dominate. This deliberately reuses normal branch nodes instead of
     * creating a persistent center core.
     */
    public static final int SHORT_LIVED_SEGMENT_COUNT = 9;
    public static final int SHORT_LIFETIME_MIN = 14;
    public static final int SHORT_LIFETIME_MAX = 22;
    public static final double INITIAL_RADIUS = 5.0D;
    public static final double VERTICAL_SPHERE_SCALE = 0.80D;
    public static final double CENTER_CLEARANCE_RADIUS = INITIAL_RADIUS * VERTICAL_SPHERE_SCALE + 0.25D;
    public static final double MIN_TANGENT_SPEED = 0.06D;
    public static final double MAX_TANGENT_SPEED = 0.10D;
    public static final double CONSERVATIVE_TANGENT_MARGIN = 3.0D;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double MIN_ELEVATION = -Math.PI / 2.0D + 0.01D;
    private static final double MAX_ELEVATION = Math.PI / 2.0D - 0.01D;
    private static final double AZIMUTH_JITTER = 0.13D;
    private static final double ELEVATION_JITTER = 0.07D;
    private static final double CURTAIN_LATERAL_FALL_FACTOR = 0.10D;
    private static final double TANGENT_DIFFERENCE = 1.0D / 120.0D;
    private static final int ENVELOPE_SAMPLES_PER_BRANCH = 121;

    private static final long BRANCH_SEED_SALT = 0x4F1BBCDCBFA53E7DL;
    private static final long INDEX_SALT = 0x9E3779B97F4A7C15L;
    private static final long PHASE_SALT = 0x6A09E667F3BCC909L;
    private static final long AZIMUTH_SALT = 0xBB67AE8584CAA73BL;
    private static final long ELEVATION_SALT = 0x3C6EF372FE94F82BL;
    private static final long REACH_SALT = 0xA54FF53A5F1D36F1L;
    private static final long RISE_SALT = 0x510E527FADE682D1L;
    private static final long DROP_SALT = 0x9B05688C2B3E6C1FL;
    private static final long BEND_SALT = 0x1F83D9ABFB41BD6BL;
    private static final long SWAY_AMPLITUDE_SALT = 0x5BE0CD19137E2179L;
    private static final long SWAY_PHASE_SALT = 0xCBBB9D5DC1059ED8L;
    private static final long SWAY_FREQUENCY_SALT = 0x629A292A367CD507L;
    private static final long TANGENT_SPEED_SALT = 0x9159015A3070DD17L;
    private static final long LIFETIME_SALT = 0x152FECD8F70E5939L;
    private static final long FLICKER_SALT = 0x67332667FFC00B31L;
    private static final long COLOR_TONE_SALT = 0xA4093822299F31D0L;

    private WillowTrajectory() {
    }

    /** The fixed color ranges are shared by all six approved willow styles. */
    public enum ColorBand {
        PRIMARY,
        SECONDARY,
        ACCENT
    }

    /**
     * Seeded branch parameters. The branch direction spans the entire Fibonacci sphere, never a horizontal ring.
     */
    public record Branch(
            int index,
            long seed,
            Vec3 direction,
            Vec3 sideDirection,
            double reach,
            double riseMultiplier,
            double fallMultiplier,
            double bendProgress,
            double swayAmplitude,
            double swayPhase,
            double swayFrequency,
            double tangentSpeed) {
    }

    /** One particle node, with all visual decisions already derived from the same deterministic branch seed. */
    public record BranchSample(
            Branch branch,
            int segmentIndex,
            double progress,
            Vec3 position,
            Vec3 tangent,
            ColorBand colorBand,
            float colorTone,
            int lifetime,
            boolean twinkles) {
    }

    /** Conservative axis-aligned local bounds, including a bounded spark tangent-travel margin. */
    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Willow bounds must be finite and ordered");
            }
        }

        public double spanX() {
            return this.maxX - this.minX;
        }

        public double spanY() {
            return this.maxY - this.minY;
        }

        public double spanZ() {
            return this.maxZ - this.minZ;
        }

        public double maxSpan() {
            return Math.max(this.spanX(), Math.max(this.spanY(), this.spanZ()));
        }

        public boolean fitsWithin(double envelope) {
            return envelope > 0.0D && this.maxSpan() <= envelope + 1.0E-9D;
        }
    }

    /** Builds one deterministic, globally distributed branch from the payload seed. */
    public static Branch branch(FireworkStyle.WillowProfile profile, long payloadSeed, int branchIndex) {
        validateProfile(profile);
        if (branchIndex < 0 || branchIndex >= profile.branchCount()) {
            throw new IllegalArgumentException("Willow branch index is outside the configured branch count");
        }

        long branchSeed = mix64(payloadSeed ^ BRANCH_SEED_SALT ^ ((long) branchIndex * INDEX_SALT));
        double sphereY = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / profile.branchCount();
        double phase = randomUnit(payloadSeed ^ PHASE_SALT) * TWO_PI;
        double azimuth = branchIndex * GOLDEN_ANGLE + phase
                + centered(branchSeed, AZIMUTH_SALT) * AZIMUTH_JITTER;
        double elevation = clamp(
                Math.asin(sphereY) + centered(branchSeed, ELEVATION_SALT) * ELEVATION_JITTER,
                MIN_ELEVATION,
                MAX_ELEVATION);
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth),
                Math.sin(elevation),
                horizontal * Math.sin(azimuth));
        Vec3 sideDirection = horizontalSide(direction);

        return new Branch(
                branchIndex,
                branchSeed,
                direction,
                sideDirection,
                profile.horizontalReach() * between(branchSeed, REACH_SALT, 0.90D, 1.00D),
                between(branchSeed, RISE_SALT, 0.92D, 1.06D),
                between(branchSeed, DROP_SALT, 0.90D, 1.04D),
                between(branchSeed, BEND_SALT, 0.52D, 0.62D),
                between(branchSeed, SWAY_AMPLITUDE_SALT, 0.28D, 0.84D),
                randomUnit(branchSeed ^ SWAY_PHASE_SALT) * TWO_PI,
                between(branchSeed, SWAY_FREQUENCY_SALT, 1.35D, 2.60D),
                between(branchSeed, TANGENT_SPEED_SALT, MIN_TANGENT_SPEED, MAX_TANGENT_SPEED));
    }

    /** Returns a single visible branch node. Positions are local to the explosion center. */
    public static BranchSample sample(
            FireworkStyle.WillowProfile profile, long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(profile, branch(profile, payloadSeed, branchIndex), segmentIndex);
    }

    /** Returns a single visible branch node using an already-created branch. */
    public static BranchSample sample(
            FireworkStyle.WillowProfile profile, Branch branch, int segmentIndex) {
        validateProfile(profile);
        validateSegment(profile, segmentIndex);
        double progress = progress(profile, segmentIndex);
        ColorBand colorBand = colorBand(segmentIndex);
        boolean shortLived = isShortLivedSegment(segmentIndex);
        int lifetime = shortLived
                ? randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, SHORT_LIFETIME_MIN, SHORT_LIFETIME_MAX)
                : randomInt(
                        branch.seed(),
                        LIFETIME_SALT + segmentIndex,
                        profile.minLifetime(),
                        profile.maxLifetime());
        boolean twinkles = !shortLived && randomUnit(branch.seed() ^ (FLICKER_SALT + segmentIndex))
                < twinkleChance(colorBand);
        return new BranchSample(
                branch,
                segmentIndex,
                progress,
                position(profile, branch, progress),
                tangent(profile, branch, progress),
                colorBand,
                (float) randomUnit(branch.seed() ^ (COLOR_TONE_SALT + segmentIndex)),
                lifetime,
                twinkles);
    }

    /** Samples one local branch position at a normalized progress value. */
    public static Vec3 position(FireworkStyle.WillowProfile profile, Branch branch, double progress) {
        validateProfile(profile);
        double boundedProgress = clamp(progress, 0.0D, 1.0D);
        double radius = INITIAL_RADIUS + (Math.max(INITIAL_RADIUS, branch.reach()) - INITIAL_RADIUS) * boundedProgress;
        double temporaryRise = profile.upwardRise() * branch.riseMultiplier() * Math.sin(Math.PI * boundedProgress);
        double fallProgress = boundedProgress <= branch.bendProgress()
                ? 0.0D
                : (boundedProgress - branch.bendProgress()) / (1.0D - branch.bendProgress());
        double smoothFall = smoothStep(fallProgress);
        double rapidFall = profile.downwardFall() * branch.fallMultiplier() * smoothFall * smoothFall;
        double verticalSphereRadius = INITIAL_RADIUS + (radius - INITIAL_RADIUS) * VERTICAL_SPHERE_SCALE;
        double sway = branch.swayAmplitude()
                * Math.sin(branch.swayPhase() + TWO_PI * branch.swayFrequency() * boundedProgress)
                * Math.sin(Math.PI * boundedProgress);
        // High branches must curve past, rather than back through, the burst center as their curtains fall.
        double curtainDeflection = profile.downwardFall() * CURTAIN_LATERAL_FALL_FACTOR * smoothFall * smoothFall;
        double lateralOffset = sway + curtainDeflection;
        Vec3 position = new Vec3(
                branch.direction().x * radius + branch.sideDirection().x * lateralOffset,
                branch.direction().y * verticalSphereRadius + temporaryRise - rapidFall,
                branch.direction().z * radius + branch.sideDirection().z * lateralOffset);
        if (position.lengthSqr() < CENTER_CLEARANCE_RADIUS * CENTER_CLEARANCE_RADIUS) {
            Vec3 outward = position.lengthSqr() < 1.0E-12D ? branch.direction() : position.normalize();
            position = outward.scale(CENTER_CLEARANCE_RADIUS);
        }
        return position;
    }

    /**
     * Derives a branch's spark velocity from a finite difference of the same position function.
     * The speed is kept in the approved {@code 0.06-0.10} range.
     */
    public static Vec3 tangent(FireworkStyle.WillowProfile profile, Branch branch, double progress) {
        validateProfile(profile);
        double boundedProgress = clamp(progress, 0.0D, 1.0D);
        double before = Math.max(0.0D, boundedProgress - TANGENT_DIFFERENCE);
        double after = Math.min(1.0D, boundedProgress + TANGENT_DIFFERENCE);
        Vec3 difference = position(profile, branch, after).subtract(position(profile, branch, before));
        if (difference.lengthSqr() < 1.0E-12D) {
            difference = branch.direction();
        }
        return difference.normalize().scale(branch.tangentSpeed());
    }

    public static ColorBand colorBand(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Willow segment index is outside the configured segment count");
        }
        if (segmentIndex <= 5) {
            return ColorBand.PRIMARY;
        }
        return segmentIndex <= 23 ? ColorBand.SECONDARY : ColorBand.ACCENT;
    }

    public static boolean isShortLivedSegment(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Willow segment index is outside the configured segment count");
        }
        return segmentIndex < SHORT_LIVED_SEGMENT_COUNT;
    }

    public static double twinkleChance(ColorBand colorBand) {
        return switch (colorBand) {
            case PRIMARY -> 0.35D;
            case SECONDARY -> 0.40D;
            case ACCENT -> 0.55D;
        };
    }

    /** Samples every deterministic branch curve and adds a conservative capped spark-motion margin. */
    public static Bounds conservativeBounds(FireworkStyle.WillowProfile profile, long payloadSeed) {
        validateProfile(profile);
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int branchIndex = 0; branchIndex < profile.branchCount(); branchIndex++) {
            Branch branch = branch(profile, payloadSeed, branchIndex);
            for (int point = 0; point < ENVELOPE_SAMPLES_PER_BRANCH; point++) {
                Vec3 sample = position(profile, branch, (double) point / (ENVELOPE_SAMPLES_PER_BRANCH - 1));
                minX = Math.min(minX, sample.x);
                minY = Math.min(minY, sample.y);
                minZ = Math.min(minZ, sample.z);
                maxX = Math.max(maxX, sample.x);
                maxY = Math.max(maxY, sample.y);
                maxZ = Math.max(maxZ, sample.z);
            }
        }
        return new Bounds(
                minX - CONSERVATIVE_TANGENT_MARGIN,
                minY - CONSERVATIVE_TANGENT_MARGIN,
                minZ - CONSERVATIVE_TANGENT_MARGIN,
                maxX + CONSERVATIVE_TANGENT_MARGIN,
                maxY + CONSERVATIVE_TANGENT_MARGIN,
                maxZ + CONSERVATIVE_TANGENT_MARGIN);
    }

    /** Convenience check for a willow style's own complete visible-envelope limit. */
    public static boolean fitsEnvelope(FireworkStyle style, long payloadSeed) {
        return style != null
                && style.shape() == FireworkStyle.Shape.WILLOW_SPHERE
                && style.willowProfile() != null
                && conservativeBounds(style.willowProfile(), payloadSeed).fitsWithin(style.fullEnvelope());
    }

    private static double progress(FireworkStyle.WillowProfile profile, int segmentIndex) {
        return profile.segmentsPerBranch() == 1
                ? 0.0D
                : (double) segmentIndex / (profile.segmentsPerBranch() - 1);
    }

    private static void validateProfile(FireworkStyle.WillowProfile profile) {
        if (profile == null || profile.branchCount() != BRANCH_COUNT || profile.segmentsPerBranch() != SEGMENTS_PER_BRANCH
                || profile.coreStarCount() != 0) {
            throw new IllegalArgumentException("WillowTrajectory requires the fixed v0.2.5 willow profile");
        }
    }

    private static void validateSegment(FireworkStyle.WillowProfile profile, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= profile.segmentsPerBranch()) {
            throw new IllegalArgumentException("Willow segment index is outside the configured segment count");
        }
    }

    private static Vec3 horizontalSide(Vec3 direction) {
        double horizontalLength = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        return horizontalLength < 1.0E-9D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(-direction.z / horizontalLength, 0.0D, direction.x / horizontalLength);
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static double between(long branchSeed, long salt, double min, double max) {
        return min + randomUnit(branchSeed ^ salt) * (max - min);
    }

    private static double centered(long branchSeed, long salt) {
        return randomUnit(branchSeed ^ salt) - 0.5D;
    }

    private static int randomInt(long branchSeed, long salt, int min, int max) {
        return min + (int) Math.floor(randomUnit(branchSeed ^ salt) * (max - min + 1));
    }

    private static double randomUnit(long value) {
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
