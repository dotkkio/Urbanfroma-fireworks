package com.urbanforma.fireworks.content.giant.willow;

import net.minecraft.world.phys.Vec3;

/**
 * Common-side deterministic geometry for the second giant firework.
 *
 * <p>This is a dome/umbrella of radial branches, not a spherical shell: every branch keeps one azimuth and
 * its horizontal radius only grows outward. The only curvature is the deterministic vertical crown and droop.</p>
 */
public final class GiantWillowTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    /** Matches the existing giant rocket's 0-to-200 ascent contract. */
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.30F;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** 192 branches x 64 rings preserves the audited 12,288-particle giant allowance. */
    public static final int BRANCH_COUNT = 192;
    public static final int SEGMENTS_PER_BRANCH = 64;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH;

    public static final int RETAINED_TAIL_FIRST_SEGMENT = 40;
    public static final int RETAINED_TAIL_SEGMENT_COUNT = SEGMENTS_PER_BRANCH - RETAINED_TAIL_FIRST_SEGMENT;
    public static final int TAIL_EXTENSION_TICKS = 200;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 18;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 24;
    /** A small deterministic end spread prevents every retained tail from disappearing on one tick. */
    public static final int RETIREMENT_END_STAGGER_MAX_TICKS = 6;
    public static final int MIN_PARTICLE_LIFETIME = 72;
    public static final int MAX_PARTICLE_LIFETIME = 252;
    public static final int RETAINED_TAIL_LIFETIME_MIN = 236;
    public static final int RETAINED_TAIL_LIFETIME_MAX = 252;
    public static final int TOTAL_VISUAL_TICKS =
            EMISSION_TICKS + TAIL_EXTENSION_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS + 1;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;

    private static final double INITIAL_RADIUS = 2.5D;
    private static final double OUTER_RADIUS = 112.0D;
    private static final double TAIL_OUTWARD_EXTENSION = 3.5D;
    private static final double VERTICAL_SCALE = 0.98D;
    private static final double CROWN_LIFT = 8.0D;
    private static final double DROOP_START = 0.46D;
    private static final double TERMINAL_DROOP = 46.0D;
    private static final double TAIL_EXTRA_DROP = 10.0D;
    private static final double MIN_ELEVATION = 0.18D;
    private static final double MAX_ELEVATION = 1.08D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final int ENVELOPE_SAMPLES = 101;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long BRANCH_SALT = 0xD6E8FEB86659FD93L;
    private static final long AZIMUTH_SALT = 0xA4093822299F31D0L;
    private static final long ELEVATION_SALT = 0x13198A2E03707344L;
    private static final long TONE_SALT = 0x452821E638D01377L;
    private static final long LIFETIME_SALT = 0xBE5466CF34E90C6CL;

    private GiantWillowTrajectory() {
    }

    public enum ColorBand {
        WARM_WHITE(new Rgb(1.0F, 0.94F, 0.72F), 1.34F),
        GOLD_WHITE(new Rgb(1.0F, 0.82F, 0.32F), 1.16F),
        PEARL_WHITE(new Rgb(1.0F, 0.99F, 0.88F), 1.48F);

        private final Rgb rgb;
        private final float scale;

        ColorBand(Rgb rgb, float scale) {
            this.rgb = rgb;
            this.scale = scale;
        }

        public Rgb rgb() {
            return this.rgb;
        }

        public float scale() {
            return this.scale;
        }
    }

    public record Rgb(float red, float green, float blue) {
        public Rgb {
            validateChannel(red, "red");
            validateChannel(green, "green");
            validateChannel(blue, "blue");
        }

        private static void validateChannel(float value, String name) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(name + " must be between 0 and 1");
            }
        }
    }

    public record AscentSample(int tick, double progress, Vec3 position, Vec3 velocity) {
        public AscentSample {
            if (tick < 0 || tick >= ASCENT_TICKS || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || velocity == null) {
                throw new IllegalArgumentException("Invalid giant willow ascent sample");
            }
        }
    }

    /** One radial umbrella branch. Elevation is always above the horizon, so there is no lower spherical shell. */
    public record Branch(int index, long seed, double azimuth, double elevation, Vec3 direction) {
        public Branch {
            if (index < 0 || index >= BRANCH_COUNT || !Double.isFinite(azimuth)
                    || !Double.isFinite(elevation) || elevation < MIN_ELEVATION || elevation > MAX_ELEVATION
                    || direction == null || Math.abs(direction.lengthSqr() - 1.0D) > 1.0E-8D) {
                throw new IllegalArgumentException("Invalid giant willow radial branch");
            }
        }
    }

    public record BranchSample(
            Branch branch,
            int segmentIndex,
            double progress,
            double radius,
            Vec3 position,
            ColorBand colorBand,
            float colorTone,
            int lifetime,
            boolean retainedTail) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(radius) || radius < 0.0D || radius > MAX_RADIUS + RADIUS_EPSILON
                    || position == null || colorBand == null || !Float.isFinite(colorTone)
                    || colorTone < 0.0F || colorTone > 1.0F || lifetime < MIN_PARTICLE_LIFETIME
                    || lifetime > MAX_PARTICLE_LIFETIME
                    || retainedTail != isRetainedTailSegment(segmentIndex)) {
                throw new IllegalArgumentException("Invalid giant willow branch sample");
            }
        }
    }

    public record TailSample(Branch branch, int segmentIndex, int age, double progress, Vec3 position) {
        public TailSample {
            if (branch == null || !isRetainedTailSegment(segmentIndex) || age < 0 || age > TAIL_EXTENSION_TICKS
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || position == null) {
                throw new IllegalArgumentException("Invalid giant willow tail sample");
            }
        }
    }

    public record Bounds(
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double maxDistance) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || !Double.isFinite(maxDistance) || minX > maxX || minY > maxY || minZ > maxZ
                    || maxDistance < 0.0D) {
                throw new IllegalArgumentException("Giant willow bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius > 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    public record ParticlePlan(
            int tick, int createdThisTick, int cumulativeCreated, int activeUpperBound, int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0 || cumulativeCreated > TOTAL_PARTICLES
                    || activeUpperBound > PROTOTYPE_MAX_ALIVE_PARTICLES) {
                throw new IllegalArgumentException("Invalid giant willow particle accounting");
            }
        }
    }

    public record RetirementFlicker(int startTick, int endTick, int cadencePhase) {
        public RetirementFlicker {
            if (startTick < 0 || endTick < startTick
                    || endTick > TAIL_EXTENSION_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS
                    || cadencePhase < 0 || cadencePhase > 1) {
                throw new IllegalArgumentException("Invalid giant willow retirement flicker");
            }
        }

        public boolean activeAt(int tailAge) {
            return tailAge >= this.startTick && tailAge < this.endTick;
        }
    }

    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Giant willow ascent tick is outside the 0-to-200 path");
        }
        double progress = (double) tick / (ASCENT_TICKS - 1);
        double height = LAUNCH_HEIGHT + (DETONATION_HEIGHT - LAUNCH_HEIGHT) * progress;
        return new AscentSample(
                tick,
                progress,
                new Vec3(0.0D, height, 0.0D),
                new Vec3(0.0D, (DETONATION_HEIGHT - LAUNCH_HEIGHT) / (ASCENT_TICKS - 1), 0.0D));
    }

    public static boolean ascentFitsDeclaredHeight() {
        return ascentAtTick(0).position().y == LAUNCH_HEIGHT
                && ascentAtTick(ASCENT_TICKS - 1).position().y == DETONATION_HEIGHT;
    }

    public static Branch branch(long payloadSeed, int branchIndex) {
        validateBranchIndex(branchIndex);
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double layer = ((double) ((branchIndex * 37) % BRANCH_COUNT) + 0.5D) / BRANCH_COUNT;
        double elevation = MIN_ELEVATION + (MAX_ELEVATION - MIN_ELEVATION) * Math.sqrt(layer)
                + centered(branchSeed, ELEVATION_SALT) * 0.035D;
        elevation = clamp(elevation, MIN_ELEVATION, MAX_ELEVATION);
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ AZIMUTH_SALT) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * 0.045D;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(horizontal * Math.cos(azimuth), Math.sin(elevation),
                horizontal * Math.sin(azimuth)).normalize();
        return new Branch(branchIndex, branchSeed, azimuth, elevation, direction);
    }

    public static BranchSample sample(long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(branch(payloadSeed, branchIndex), segmentIndex);
    }

    public static BranchSample sample(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant willow branch may not be null");
        }
        validateSegmentIndex(segmentIndex);
        double progress = segmentIndex / (double) (SEGMENTS_PER_BRANCH - 1);
        boolean retained = isRetainedTailSegment(segmentIndex);
        int lifetime = retained
                ? randomInt(branch.seed(), LIFETIME_SALT + segmentIndex,
                        RETAINED_TAIL_LIFETIME_MIN, RETAINED_TAIL_LIFETIME_MAX)
                : randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, MIN_PARTICLE_LIFETIME, 124);
        return new BranchSample(
                branch,
                segmentIndex,
                progress,
                radiusAt(progress),
                position(branch, progress),
                colorBand(segmentIndex),
                (float) randomUnit(branch.seed() ^ (TONE_SALT + segmentIndex)),
                lifetime,
                retained);
    }

    public static Vec3 position(Branch branch, double progress) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant willow branch may not be null");
        }
        double bounded = clamp(progress, 0.0D, 1.0D);
        double radius = radiusAt(bounded);
        double crown = CROWN_LIFT * Math.sin(Math.PI * bounded);
        double fallProgress = bounded <= DROOP_START ? 0.0D
                : (bounded - DROOP_START) / (1.0D - DROOP_START);
        double drop = TERMINAL_DROOP * smoothStep(fallProgress);
        return new Vec3(
                branch.direction().x * radius,
                branch.direction().y * radius * VERTICAL_SCALE + crown - drop,
                branch.direction().z * radius);
    }

    /** Continues only retained outer nodes; no new particles are allocated during this 200-tick phase. */
    public static TailSample tailSample(Branch branch, int segmentIndex, int tailAge) {
        if (branch == null || !isRetainedTailSegment(segmentIndex)) {
            throw new IllegalArgumentException("Only retained giant willow tail segments may continue");
        }
        if (tailAge < 0 || tailAge > TAIL_EXTENSION_TICKS) {
            throw new IllegalArgumentException("Giant willow tail age is outside the 200-tick window");
        }
        double progress = tailAge / (double) TAIL_EXTENSION_TICKS;
        double extension = TAIL_OUTWARD_EXTENSION * smoothStep(progress);
        Vec3 endpoint = position(branch, 1.0D);
        double horizontal = Math.sqrt(endpoint.x * endpoint.x + endpoint.z * endpoint.z);
        double scale = (horizontal + extension) / horizontal;
        Vec3 tailPosition = new Vec3(
                endpoint.x * scale,
                endpoint.y - TAIL_EXTRA_DROP * smoothStep(progress),
                endpoint.z * scale);
        return new TailSample(branch, segmentIndex, tailAge, progress, tailPosition);
    }

    public static ColorBand colorBand(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        if (segmentIndex < 12) {
            return ColorBand.WARM_WHITE;
        }
        return segmentIndex < 48 ? ColorBand.GOLD_WHITE : ColorBand.PEARL_WHITE;
    }

    public static boolean isRetainedTailSegment(int segmentIndex) {
        return segmentIndex >= RETAINED_TAIL_FIRST_SEGMENT && segmentIndex < SEGMENTS_PER_BRANCH;
    }

    public static boolean isPureRadialSample(Branch branch, Vec3 sample) {
        if (branch == null || sample == null) {
            return false;
        }
        double horizontal = Math.sqrt(sample.x * sample.x + sample.z * sample.z);
        return horizontal < RADIUS_EPSILON
                || Math.abs(sample.x * branch.direction().z - sample.z * branch.direction().x)
                        <= RADIUS_EPSILON * Math.max(1.0D, horizontal);
    }

    public static RetirementFlicker retirementFlicker(Branch branch, int segmentIndex) {
        if (branch == null || !isRetainedTailSegment(segmentIndex)) {
            throw new IllegalArgumentException("Only retained giant willow tail segments may flicker");
        }
        int spread = RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1;
        int lead = RETIREMENT_FLICKER_MIN_LEAD_TICKS
                + Math.floorMod(branch.index() * 31 + segmentIndex * 17, spread);
        int endStagger = Math.floorMod(branch.index() * 13 + segmentIndex * 7,
                RETIREMENT_END_STAGGER_MAX_TICKS + 1);
        int phase = Math.floorMod(branch.index() * 17 + segmentIndex * 29, 2);
        int endTick = TAIL_EXTENSION_TICKS + endStagger;
        return new RetirementFlicker(endTick - lead, endTick, phase);
    }

    public static int particlesCreatedThisTick(int tick) {
        return tick >= 0 && tick < EMISSION_TICKS ? PARTICLES_PER_EMISSION_TICK : 0;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        return Math.min(tick + 1, EMISSION_TICKS) * PARTICLES_PER_EMISSION_TICK;
    }

    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        return Math.min(TOTAL_PARTICLES, particlesCreatedThroughTick(tick));
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Giant willow particle-plan tick may not be negative");
        }
        int cumulative = particlesCreatedThroughTick(tick);
        return new ParticlePlan(
                tick,
                particlesCreatedThisTick(tick),
                cumulative,
                activeParticleUpperBoundAtTick(tick),
                TOTAL_PARTICLES - cumulative);
    }

    public static int maximumAliveParticleUpperBound() {
        return PROTOTYPE_MAX_ALIVE_PARTICLES;
    }

    public static int finalParticleDisappearanceTick() {
        return TOTAL_VISUAL_TICKS;
    }

    /** Exhaustive deterministic proof over the fixed branch/ring/tail lattice for a payload seed. */
    public static boolean staticContractHolds(long payloadSeed) {
        if (!ascentFitsDeclaredHeight() || TOTAL_PARTICLES != 12_288
                || TAIL_EXTENSION_TICKS != 200 || DETONATION_SOUND_MAX_PLAYS != 1
                || maximumAliveParticleUpperBound() != TOTAL_PARTICLES) {
            return false;
        }
        int firstFlickerEnd = -1;
        boolean hasStaggeredFlickerEnds = false;
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            for (int segment = 0; segment < SEGMENTS_PER_BRANCH; segment++) {
                BranchSample sample = sample(branch, segment);
                if (!isPureRadialSample(branch, sample.position()) || !fitsRadius(sample.position())) {
                    return false;
                }
                if (isRetainedTailSegment(segment)) {
                    for (int age = 0; age <= TAIL_EXTENSION_TICKS; age++) {
                        Vec3 tailPosition = tailSample(branch, segment, age).position();
                        if (!isPureRadialSample(branch, tailPosition) || !fitsRadius(tailPosition)) {
                            return false;
                        }
                    }
                    RetirementFlicker flicker = retirementFlicker(branch, segment);
                    if (firstFlickerEnd < 0) {
                        firstFlickerEnd = flicker.endTick();
                    } else if (firstFlickerEnd != flicker.endTick()) {
                        hasStaggeredFlickerEnds = true;
                    }
                }
            }
        }
        return hasStaggeredFlickerEnds && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS);
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        double maxDistance = 0.0D;
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            for (int point = 0; point < ENVELOPE_SAMPLES; point++) {
                double progress = point / (double) (ENVELOPE_SAMPLES - 1);
                Vec3 value = position(branch, progress);
                minX = Math.min(minX, value.x);
                minY = Math.min(minY, value.y);
                minZ = Math.min(minZ, value.z);
                maxX = Math.max(maxX, value.x);
                maxY = Math.max(maxY, value.y);
                maxZ = Math.max(maxZ, value.z);
                maxDistance = Math.max(maxDistance, value.length());
            }
            for (int tailAge = 0; tailAge <= TAIL_EXTENSION_TICKS; tailAge++) {
                for (int segment = RETAINED_TAIL_FIRST_SEGMENT; segment < SEGMENTS_PER_BRANCH; segment++) {
                    Vec3 value = tailSample(branch, segment, tailAge).position();
                    minX = Math.min(minX, value.x);
                    minY = Math.min(minY, value.y);
                    minZ = Math.min(minZ, value.z);
                    maxX = Math.max(maxX, value.x);
                    maxY = Math.max(maxY, value.y);
                    maxZ = Math.max(maxZ, value.z);
                    maxDistance = Math.max(maxDistance, value.length());
                }
            }
        }
        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ, maxDistance);
    }

    public static boolean fitsRadius(Vec3 position) {
        return position != null && position.lengthSqr() <= MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON;
    }

    private static double radiusAt(double progress) {
        return INITIAL_RADIUS + (OUTER_RADIUS - INITIAL_RADIUS) * smoothStep(progress);
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Giant willow branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Giant willow segment index is outside the configured count");
        }
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
    }

    private static int randomInt(long seed, long salt, int min, int max) {
        return min + (int) Math.floor(randomUnit(seed ^ salt) * (max - min + 1));
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
