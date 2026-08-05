package com.urbanforma.fireworks.content;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, common-side geometry for the first three-dimensional radiant shell.
 *
 * <p>Every branch decision is derived from the payload seed and its Fibonacci branch index. Client code only
 * schedules {@link BranchSample samples}; common-side GameTests can inspect exactly the same geometry without
 * loading any particle or client-rendering classes.</p>
 */
public final class RadiantTrajectory {
    public static final int BRANCH_COUNT = 160;
    public static final int SEGMENTS_PER_BRANCH = 30;
    public static final int NODES_PER_BURST = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    /** One complete branch ring is released per tick. */
    public static final int SEGMENT_INTERVAL_TICKS = 1;
    /** Thirty rings at ticks 0 through 29 form the full radiant shell. */
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int BRANCHES_PER_TICK = BRANCH_COUNT;

    /** The first three branch rings form a visible center without adding a separate particle type. */
    public static final int CORE_SEGMENT_COUNT = 3;
    public static final int CORE_LIFETIME_MIN = 36;
    public static final int CORE_LIFETIME_MAX = 44;
    public static final int STAR_LIFETIME_MIN = 58;
    public static final int STAR_LIFETIME_MAX = 62;

    public static final double INITIAL_RADIUS = 3.5D;
    public static final double MAX_RADIUS = 48.0D;
    public static final double VERTICAL_SCALE = 0.94D;
    public static final double MIN_LENGTH_MULTIPLIER = 0.90D;
    public static final double MAX_LENGTH_MULTIPLIER = 1.00D;
    public static final double DROP_START_MIN = 0.38D;
    public static final double DROP_START_MAX = 0.46D;
    public static final double TERMINAL_DROP = 9.0D;
    public static final double MIN_DROP_MULTIPLIER = 0.92D;
    public static final double MAX_DROP_MULTIPLIER = 1.05D;
    public static final double MIN_SWAY_AMPLITUDE = 0.18D;
    public static final double MAX_SWAY_AMPLITUDE = 0.65D;
    public static final double MIN_TANGENT_SPEED = 0.045D;
    public static final double MAX_TANGENT_SPEED = 0.060D;
    /** The approved complete visible span, including a capped spark-travel allowance. */
    public static final double APPROVED_FULL_ENVELOPE = 108.0D;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double MIN_ELEVATION = -Math.PI / 2.0D + 0.01D;
    private static final double MAX_ELEVATION = Math.PI / 2.0D - 0.01D;
    private static final double AZIMUTH_JITTER = 0.10D;
    private static final double ELEVATION_JITTER = 0.055D;
    private static final double MIN_SWAY_FREQUENCY = 0.90D;
    private static final double MAX_SWAY_FREQUENCY = 1.80D;
    private static final double TANGENT_DIFFERENCE = 1.0D / 180.0D;
    /* 0.060 blocks/tick for 62 ticks is 3.72; 4.0 also covers numeric and render-tick slack. */
    private static final double CONSERVATIVE_TANGENT_MARGIN = 4.0D;

    private static final long BRANCH_SEED_SALT = 0x68E31DA4C9B2F705L;
    private static final long INDEX_SALT = 0x9E3779B97F4A7C15L;
    private static final long PHASE_SALT = 0xD6E8FEB86659FD93L;
    private static final long AZIMUTH_SALT = 0xA4093822299F31D0L;
    private static final long ELEVATION_SALT = 0x13198A2E03707344L;
    private static final long REACH_SALT = 0x243F6A8885A308D3L;
    private static final long DROP_START_SALT = 0x3BD39E10CB0EF593L;
    private static final long DROP_SALT = 0xC0AC29B7C97C50DDL;
    private static final long SWAY_AMPLITUDE_SALT = 0xB7E151628AED2A6BL;
    private static final long SWAY_PHASE_SALT = 0xBF58476D1CE4E5B9L;
    private static final long SWAY_FREQUENCY_SALT = 0x94D049BB133111EBL;
    private static final long TANGENT_SPEED_SALT = 0x4CF5AD432745937FL;
    private static final long LIFETIME_SALT = 0x452821E638D01377L;
    private static final long FLICKER_SALT = 0xBE5466CF34E90C6CL;
    private static final long TWINKLE_PHASE_SALT = 0xC6BC279692B5CC83L;
    private static final long COLOR_TONE_SALT = 0x9DDfea08eb382d69L;

    private RadiantTrajectory() {
    }

    /** The three color ranges are deliberately stable across the first radiant style. */
    public enum ColorBand {
        PRIMARY,
        SECONDARY,
        ACCENT
    }

    /** Seeded parameters for one globally distributed three-dimensional radiant branch. */
    public record Branch(
            int index,
            long seed,
            Vec3 direction,
            Vec3 sideDirection,
            double radialReach,
            double dropStartProgress,
            double dropMultiplier,
            double swayAmplitude,
            double swayPhase,
            double swayFrequency,
            double tangentSpeed,
            double twinklePhase) {
    }

    /** One visible node, with all deterministic visual decisions resolved for client scheduling. */
    public record BranchSample(
            Branch branch,
            int segmentIndex,
            double progress,
            Vec3 position,
            Vec3 tangent,
            ColorBand colorBand,
            float colorTone,
            int lifetime,
            boolean twinkles,
            float twinklePhase) {
    }

    /** Conservative axis-aligned local bounds, including the capped spark-motion allowance. */
    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Radiant bounds must be finite and ordered");
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

    /** Builds one deterministic Fibonacci-distributed branch from the shared burst payload seed. */
    public static Branch branch(FireworkStyle.RadiantProfile profile, long payloadSeed, int branchIndex) {
        validateProfile(profile);
        validateBranchIndex(branchIndex);

        long branchSeed = mix64(payloadSeed ^ BRANCH_SEED_SALT ^ ((long) branchIndex * INDEX_SALT));
        double sphereY = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / BRANCH_COUNT;
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

        return new Branch(
                branchIndex,
                branchSeed,
                direction,
                horizontalSide(direction),
                profile.maximumRadius() * between(branchSeed, REACH_SALT, MIN_LENGTH_MULTIPLIER, MAX_LENGTH_MULTIPLIER),
                between(branchSeed, DROP_START_SALT, profile.bendStartMin(), profile.bendStartMax()),
                between(branchSeed, DROP_SALT, MIN_DROP_MULTIPLIER, MAX_DROP_MULTIPLIER),
                between(branchSeed, SWAY_AMPLITUDE_SALT, MIN_SWAY_AMPLITUDE, MAX_SWAY_AMPLITUDE),
                randomUnit(branchSeed ^ SWAY_PHASE_SALT) * TWO_PI,
                between(branchSeed, SWAY_FREQUENCY_SALT, MIN_SWAY_FREQUENCY, MAX_SWAY_FREQUENCY),
                between(branchSeed, TANGENT_SPEED_SALT, MIN_TANGENT_SPEED, MAX_TANGENT_SPEED),
                randomUnit(branchSeed ^ TWINKLE_PHASE_SALT));
    }

    /** Returns a single visible branch node from an index without requiring callers to cache its branch. */
    public static BranchSample sample(
            FireworkStyle.RadiantProfile profile, long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(profile, branch(profile, payloadSeed, branchIndex), segmentIndex);
    }

    /** Returns a single visible branch node using an already-created deterministic branch. */
    public static BranchSample sample(FireworkStyle.RadiantProfile profile, Branch branch, int segmentIndex) {
        validateProfile(profile);
        if (branch == null) {
            throw new IllegalArgumentException("Radiant branch may not be null");
        }
        validateSegmentIndex(segmentIndex);

        double progress = progress(segmentIndex);
        ColorBand colorBand = colorBand(segmentIndex);
        boolean core = isCoreSegment(segmentIndex);
        int lifetime = core
                ? randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, CORE_LIFETIME_MIN, CORE_LIFETIME_MAX)
                : randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, profile.minLifetime(), profile.maxLifetime());
        boolean twinkles = !core && randomUnit(branch.seed() ^ (FLICKER_SALT + segmentIndex))
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
                twinkles,
                (float) fractional(branch.twinklePhase() + randomUnit(branch.seed() ^ (TWINKLE_PHASE_SALT + segmentIndex))));
    }

    /** Samples a local branch position. The downward arc starts per branch at 38-46 percent progress. */
    public static Vec3 position(FireworkStyle.RadiantProfile profile, Branch branch, double progress) {
        validateProfile(profile);
        if (branch == null) {
            throw new IllegalArgumentException("Radiant branch may not be null");
        }
        double boundedProgress = clamp(progress, 0.0D, 1.0D);
        double radius = profile.initialRadius() + (branch.radialReach() - profile.initialRadius()) * boundedProgress;
        double fallProgress = boundedProgress <= branch.dropStartProgress()
                ? 0.0D
                : (boundedProgress - branch.dropStartProgress()) / (1.0D - branch.dropStartProgress());
        double drop = profile.terminalDrop() * branch.dropMultiplier() * smoothStep(fallProgress);
        double sway = branch.swayAmplitude()
                * Math.sin(branch.swayPhase() + TWO_PI * branch.swayFrequency() * boundedProgress)
                * Math.sin(Math.PI * boundedProgress);
        return new Vec3(
                branch.direction().x * radius + branch.sideDirection().x * sway,
                branch.direction().y * radius * profile.verticalScale() - drop,
                branch.direction().z * radius + branch.sideDirection().z * sway);
    }

    /** Derives a low motion velocity from the same curved path used for the node position. */
    public static Vec3 tangent(FireworkStyle.RadiantProfile profile, Branch branch, double progress) {
        validateProfile(profile);
        if (branch == null) {
            throw new IllegalArgumentException("Radiant branch may not be null");
        }
        double boundedProgress = clamp(progress, 0.0D, 1.0D);
        double before = Math.max(0.0D, boundedProgress - TANGENT_DIFFERENCE);
        double after = Math.min(1.0D, boundedProgress + TANGENT_DIFFERENCE);
        Vec3 difference = position(profile, branch, after).subtract(position(profile, branch, before));
        if (difference.lengthSqr() < 1.0E-12D) {
            difference = branch.direction();
        }
        return difference.normalize().scale(branch.tangentSpeed());
    }

    /** Segment 0-5 are primary, 6-23 secondary, and 24-29 accent. */
    public static ColorBand colorBand(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        if (segmentIndex <= 5) {
            return ColorBand.PRIMARY;
        }
        return segmentIndex <= 23 ? ColorBand.SECONDARY : ColorBand.ACCENT;
    }

    /** The first three branch rings form the persistent early center while the shell expands. */
    public static boolean isCoreSegment(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        return segmentIndex < CORE_SEGMENT_COUNT;
    }

    /** Compatibility-friendly alias for common-side tests phrased in terms of short-lived rings. */
    public static boolean isShortLivedSegment(int segmentIndex) {
        return isCoreSegment(segmentIndex);
    }

    public static double twinkleChance(ColorBand colorBand) {
        if (colorBand == null) {
            throw new IllegalArgumentException("Radiant color band may not be null");
        }
        return switch (colorBand) {
            case PRIMARY -> 0.35D;
            case SECONDARY -> 0.40D;
            case ACCENT -> 0.55D;
        };
    }

    /** Returns a proof bound valid for every payload seed and every allowed randomized branch value. */
    public static Bounds conservativeBounds(FireworkStyle.RadiantProfile profile, long payloadSeed) {
        validateProfile(profile);
        // The seed remains in the API so callers can use the same signature as other trajectory programs.
        // This particular proof bound intentionally covers every legal seed at once.
        return maximumBounds(profile);
    }

    /** Checks the first radiant shell against its fixed 108-block complete visible envelope. */
    public static boolean fitsEnvelope(FireworkStyle.RadiantProfile profile, long payloadSeed) {
        return conservativeBounds(profile, payloadSeed).fitsWithin(APPROVED_FULL_ENVELOPE);
    }

    /** Allows static validation to check a style's declared envelope independently of the approved default. */
    public static boolean fitsEnvelope(FireworkStyle.RadiantProfile profile, long payloadSeed, double envelope) {
        return conservativeBounds(profile, payloadSeed).fitsWithin(envelope);
    }

    private static double progress(int segmentIndex) {
        return (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
    }

    private static void validateProfile(FireworkStyle.RadiantProfile profile) {
        if (profile == null || profile.branchCount() != BRANCH_COUNT
                || profile.segmentsPerBranch() != SEGMENTS_PER_BRANCH
                || profile.coreSegmentCount() != CORE_SEGMENT_COUNT
                || profile.minLifetime() != STAR_LIFETIME_MIN || profile.maxLifetime() != STAR_LIFETIME_MAX) {
            throw new IllegalArgumentException("RadiantTrajectory requires a radiant profile");
        }
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Radiant branch index is outside the configured branch count");
        }
    }

    private static void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Radiant segment index is outside the configured segment count");
        }
    }

    private static Bounds maximumBounds(FireworkStyle.RadiantProfile profile) {
        Bounds bounds = new Bounds(
                -(profile.maximumRadius() + MAX_SWAY_AMPLITUDE + CONSERVATIVE_TANGENT_MARGIN),
                -(profile.maximumRadius() * profile.verticalScale()
                        + profile.terminalDrop() * MAX_DROP_MULTIPLIER + CONSERVATIVE_TANGENT_MARGIN),
                -(profile.maximumRadius() + MAX_SWAY_AMPLITUDE + CONSERVATIVE_TANGENT_MARGIN),
                profile.maximumRadius() + MAX_SWAY_AMPLITUDE + CONSERVATIVE_TANGENT_MARGIN,
                profile.maximumRadius() * profile.verticalScale() + CONSERVATIVE_TANGENT_MARGIN,
                profile.maximumRadius() + MAX_SWAY_AMPLITUDE + CONSERVATIVE_TANGENT_MARGIN);
        if (!bounds.fitsWithin(APPROVED_FULL_ENVELOPE)) {
            throw new IllegalArgumentException("Radiant trajectory exceeds its approved envelope");
        }
        return bounds;
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

    private static double between(long seed, long salt, double min, double max) {
        return min + randomUnit(seed ^ salt) * (max - min);
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
    }

    private static int randomInt(long seed, long salt, int min, int max) {
        return min + (int) Math.floor(randomUnit(seed ^ salt) * (max - min + 1));
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

    private static double fractional(double value) {
        return value - Math.floor(value);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
