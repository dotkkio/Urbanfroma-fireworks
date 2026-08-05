package com.urbanforma.fireworks.content.giant.superwillow;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side geometry for the third giant firework: one dense, continuously falling super willow.
 *
 * <p>The only emitted topology is a set of radiating willow branches. Their outer segments are retained and moved
 * farther down by the client; the tail phase has no node creation and therefore cannot become a second burst.</p>
 */
public final class SuperWillowTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.28F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** 160 continuous branches with 72 nodes each stay below the existing 12,288-particle giant ceiling. */
    public static final int BRANCH_COUNT = 160;
    public static final int SEGMENTS_PER_BRANCH = 72;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    public static final int SHORT_LIVED_ROOT_SEGMENTS = 16;
    public static final int RETAINED_TAIL_FIRST_SEGMENT = SHORT_LIVED_ROOT_SEGMENTS;
    public static final int RETAINED_TAIL_SEGMENT_COUNT = SEGMENTS_PER_BRANCH - RETAINED_TAIL_FIRST_SEGMENT;

    /** Recommended super-willow extension: 280 ticks, or 14 seconds, using the already-emitted outer sparks only. */
    public static final int TAIL_EXTENSION_TICKS = 280;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 22;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 38;
    public static final int RETIREMENT_END_STAGGER_MAX_TICKS = 14;
    public static final int MIN_PARTICLE_LIFETIME = 74;
    public static final int MAX_PARTICLE_LIFETIME = 374;
    public static final int RETAINED_TAIL_LIFETIME_MIN = 350;
    public static final int RETAINED_TAIL_LIFETIME_MAX = 374;
    public static final int TOTAL_VISUAL_TICKS =
            EMISSION_TICKS + TAIL_EXTENSION_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS + 1;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;

    /** The highest terminal lobes descend by up to 90 blocks rather than triggering a late explosion. */
    public static final double MAX_TERMINAL_TAIL_DROP = 90.0D;

    private static final double INITIAL_RADIUS = 2.5D;
    private static final double EXPANSION_RADIUS = 88.0D;
    private static final double INITIAL_VERTICAL_SCALE = 0.87D;
    private static final double CANOPY_LIFT = 8.0D;
    private static final double OUTWARD_TAIL_BASE = 4.0D;
    private static final double OUTWARD_TAIL_BY_SEGMENT = 10.0D;
    private static final double TAIL_DROP_BASE = 22.0D;
    private static final double TAIL_DROP_RANGE = MAX_TERMINAL_TAIL_DROP - TAIL_DROP_BASE;
    private static final double TAIL_SWAY = 2.4D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double RADIUS_LIMIT = MAX_RADIUS - 0.5D;
    private static final int ENVELOPE_SAMPLES = 49;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double AZIMUTH_JITTER = 0.047D;
    private static final double ELEVATION_JITTER = 0.032D;
    private static final long BRANCH_SALT = 0x96F64356348A61C5L;
    private static final long AZIMUTH_SALT = 0xA1B2C3D4E5F60718L;
    private static final long ELEVATION_SALT = 0x18F6E5D4C3B2A190L;
    private static final long TONE_SALT = 0xD1342543DE82EF95L;
    private static final long LIFETIME_SALT = 0x9E3779B97F4A7C15L;
    private static final long FLICKER_SALT = 0xC2B2AE3D27D4EB4FL;
    private static final long SWAY_SALT = 0x165667B19E3779F9L;

    private SuperWillowTrajectory() {
    }

    /** All three bands remain gold-white or warm-white; they are positions on one willow, not separate effects. */
    public enum ColorBand {
        GOLDEN_WHITE(new Rgb(1.0F, 0.91F, 0.67F), 1.34F),
        WARM_WHITE(new Rgb(1.0F, 0.82F, 0.43F), 1.40F),
        PEARL_WHITE(new Rgb(1.0F, 0.97F, 0.84F), 1.48F);

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
                throw new IllegalArgumentException(name + " must be between zero and one");
            }
        }
    }

    public record AscentSample(int tick, double progress, Vec3 position, Vec3 velocity) {
        public AscentSample {
            if (tick < 0 || tick >= ASCENT_TICKS || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || velocity == null) {
                throw new IllegalArgumentException("Invalid super-willow ascent sample");
            }
        }
    }

    /** A Fibonacci-sphere direction makes this a full three-dimensional willow rather than a flat umbrella. */
    public record Branch(int index, long seed, double azimuth, Vec3 direction) {
        public Branch {
            if (index < 0 || index >= BRANCH_COUNT || !Double.isFinite(azimuth) || direction == null
                    || direction.lengthSqr() < RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid super-willow branch");
            }
        }
    }

    /** A single initially-emitted node in a single willow branch. */
    public record BranchSample(
            Branch branch,
            int segmentIndex,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float colorTone,
            int lifetime,
            boolean retainedTail) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || position == null
                    || colorBand == null || !Float.isFinite(colorTone) || colorTone < 0.0F || colorTone > 1.0F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid super-willow branch sample");
            }
        }
    }

    /** Position of the same retained node after a number of tail-extension ticks. */
    public record TailSample(Branch branch, int segmentIndex, int age, double progress, Vec3 position) {
        public TailSample {
            if (branch == null || segmentIndex < RETAINED_TAIL_FIRST_SEGMENT || segmentIndex >= SEGMENTS_PER_BRANCH
                    || age < 0 || age > TAIL_EXTENSION_TICKS || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid super-willow tail sample");
            }
        }
    }

    /** Deterministic, branch-local retirement window for an existing retained spark. */
    public record RetirementFlicker(int startTick, int endTick, int cadencePhase) {
        public RetirementFlicker {
            if (startTick < 0 || endTick < startTick || endTick > TAIL_EXTENSION_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS
                    || cadencePhase < 0 || cadencePhase > 3) {
                throw new IllegalArgumentException("Invalid super-willow retirement flicker");
            }
        }

        public boolean activeAt(int tailAge) {
            return tailAge >= this.startTick && tailAge <= this.endTick;
        }
    }

    public record Bounds(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            double maxDistance) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || !Double.isFinite(maxDistance) || minX > maxX || minY > maxY || minZ > maxZ
                    || maxDistance < 0.0D) {
                throw new IllegalArgumentException("Super-willow bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Per-tick accounting for this isolated giant program, never for the shared ordinary scheduler. */
    public record ParticlePlan(
            int tick,
            int createdThisTick,
            int cumulativeCreated,
            int activeUpperBound,
            int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0 || cumulativeCreated > TOTAL_PARTICLES
                    || activeUpperBound > PROTOTYPE_MAX_ALIVE_PARTICLES) {
                throw new IllegalArgumentException("Invalid super-willow particle accounting");
            }
        }
    }

    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Super-willow ascent tick is outside the 0-to-200 path");
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
        double sphereY = 0.92D - 1.84D * ((double) branchIndex + 0.5D) / BRANCH_COUNT;
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ AZIMUTH_SALT) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * AZIMUTH_JITTER;
        double elevation = Math.asin(clamp(sphereY, -0.96D, 0.96D))
                + centered(branchSeed, ELEVATION_SALT) * ELEVATION_JITTER;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(elevation), horizontal * Math.sin(azimuth)).normalize();
        return new Branch(branchIndex, branchSeed, azimuth, direction);
    }

    public static BranchSample sample(long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(branch(payloadSeed, branchIndex), segmentIndex);
    }

    public static BranchSample sample(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Super-willow branch may not be null");
        }
        validateSegmentIndex(segmentIndex);
        double progress = (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
        boolean retained = segmentIndex >= RETAINED_TAIL_FIRST_SEGMENT;
        int lifetime = retained
                ? randomInt(branch.seed(), LIFETIME_SALT + segmentIndex,
                        RETAINED_TAIL_LIFETIME_MIN, RETAINED_TAIL_LIFETIME_MAX)
                : randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, MIN_PARTICLE_LIFETIME, 116);
        return new BranchSample(
                branch,
                segmentIndex,
                progress,
                initialPosition(branch, progress),
                colorBand(segmentIndex),
                (float) randomUnit(branch.seed() ^ (TONE_SALT + segmentIndex)),
                lifetime,
                retained);
    }

    /** Samples only a retained branch node. It has no counterpart that creates a new terminal particle. */
    public static TailSample tailSample(Branch branch, int segmentIndex, int tailAge) {
        if (branch == null) {
            throw new IllegalArgumentException("Super-willow branch may not be null");
        }
        if (segmentIndex < RETAINED_TAIL_FIRST_SEGMENT || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Only retained super-willow nodes have a tail sample");
        }
        if (tailAge < 0 || tailAge > TAIL_EXTENSION_TICKS) {
            throw new IllegalArgumentException("Super-willow tail age is outside the bounded extension window");
        }
        double progress = (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
        double tailProgress = (double) tailAge / TAIL_EXTENSION_TICKS;
        Vec3 initial = initialPosition(branch, progress);
        Vec3 horizontalDirection = horizontalDirection(branch);
        double initialHorizontal = Math.sqrt(initial.x * initial.x + initial.z * initial.z);
        double outward = (OUTWARD_TAIL_BASE + OUTWARD_TAIL_BY_SEGMENT * progress) * smoothStep(tailProgress);
        double elevationWeight = 0.34D + 0.66D * ((branch.direction().y + 1.0D) * 0.5D);
        double dropMaximum = TAIL_DROP_BASE + TAIL_DROP_RANGE * (0.35D + 0.65D * progress);
        double drop = dropMaximum * elevationWeight * longTailEase(tailProgress);
        double swayPhase = randomUnit(branch.seed() ^ SWAY_SALT) * TWO_PI;
        double sway = TAIL_SWAY * (0.25D + 0.75D * progress) * Math.sin(swayPhase + tailProgress * TWO_PI * 1.35D)
                * Math.sin(Math.PI * tailProgress);
        Vec3 tangent = new Vec3(-horizontalDirection.z, 0.0D, horizontalDirection.x);
        Vec3 raw = horizontalDirection.scale(initialHorizontal + outward)
                .add(tangent.scale(sway))
                .add(0.0D, initial.y - drop, 0.0D);
        return new TailSample(branch, segmentIndex, tailAge, tailProgress, radiusSafe(raw));
    }

    public static ColorBand colorBand(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        if (segmentIndex < 12) {
            return ColorBand.GOLDEN_WHITE;
        }
        return segmentIndex < 58 ? ColorBand.WARM_WHITE : ColorBand.PEARL_WHITE;
    }

    /** Core highlighting is visual treatment of the first branch nodes only, never an additional core effect. */
    public static boolean isCoreSegment(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        return segmentIndex < 8;
    }

    public static RetirementFlicker retirementFlicker(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Super-willow branch may not be null");
        }
        if (segmentIndex < RETAINED_TAIL_FIRST_SEGMENT || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Only retained nodes receive super-willow retirement flicker");
        }
        long token = mix64(branch.seed() ^ FLICKER_SALT ^ ((long) segmentIndex * 0xD1342543DE82EF95L));
        int endTick = TAIL_EXTENSION_TICKS + Math.floorMod((int) token, RETIREMENT_END_STAGGER_MAX_TICKS + 1);
        int lead = RETIREMENT_FLICKER_MIN_LEAD_TICKS
                + Math.floorMod((int) (token >>> 17),
                        RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1);
        return new RetirementFlicker(Math.max(0, endTick - lead), endTick, Math.floorMod((int) (token >>> 33), 4));
    }

    /** There is exactly one initial node-emission window: the tail phase is guaranteed to emit zero particles. */
    public static int particlesCreatedThisTick(int tick) {
        return tick >= 0 && tick < EMISSION_TICKS ? PARTICLES_PER_EMISSION_TICK : 0;
    }

    public static int tailParticlesCreatedThisTick(int tailAge) {
        if (tailAge < 0 || tailAge > TAIL_EXTENSION_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS) {
            throw new IllegalArgumentException("Super-willow tail tick is outside its bounded visual lifetime");
        }
        return 0;
    }

    public static boolean hasNoSecondaryBurst() {
        return TOTAL_PARTICLES == BRANCH_COUNT * SEGMENTS_PER_BRANCH
                && particlesCreatedThisTick(EMISSION_TICKS) == 0
                && tailParticlesCreatedThisTick(0) == 0
                && tailParticlesCreatedThisTick(TAIL_EXTENSION_TICKS) == 0;
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
        int firstLiveEmission = Math.max(0, tick - MAX_PARTICLE_LIFETIME);
        int lastLiveEmission = Math.min(tick, EMISSION_TICKS - 1);
        if (firstLiveEmission > lastLiveEmission) {
            return 0;
        }
        return (lastLiveEmission - firstLiveEmission + 1) * PARTICLES_PER_EMISSION_TICK;
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Super-willow particle budget tick may not be negative");
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

    public static boolean isSingleWillowNode(BranchSample sample) {
        return sample != null && sample.position().lengthSqr() <= MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON;
    }

    /** Every retained node has a terminal position at or below its first-emitted branch position. */
    public static boolean tailFallsDown(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Super-willow branch may not be null");
        }
        if (segmentIndex < RETAINED_TAIL_FIRST_SEGMENT || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Only retained nodes can be checked for super-willow descent");
        }
        return tailSample(branch, segmentIndex, TAIL_EXTENSION_TICKS).position().y
                <= sample(branch, segmentIndex).position().y + RADIUS_EPSILON;
    }

    /** Deterministically proves that the tail population cannot all retire on one tick for a supplied payload seed. */
    public static boolean hasStaggeredRetirement(long payloadSeed) {
        int earliestEnd = Integer.MAX_VALUE;
        int latestEnd = Integer.MIN_VALUE;
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            for (int segment = RETAINED_TAIL_FIRST_SEGMENT; segment < SEGMENTS_PER_BRANCH; segment++) {
                int endTick = retirementFlicker(branch, segment).endTick();
                earliestEnd = Math.min(earliestEnd, endTick);
                latestEnd = Math.max(latestEnd, endTick);
            }
        }
        return earliestEnd < latestEnd;
    }

    public static boolean staticContractHolds(long payloadSeed) {
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            for (int segment = 0; segment < SEGMENTS_PER_BRANCH; segment++) {
                BranchSample sample = sample(branch, segment);
                if (!isSingleWillowNode(sample)) {
                    return false;
                }
                if (sample.retainedTail()) {
                    TailSample terminal = tailSample(branch, segment, TAIL_EXTENSION_TICKS);
                    if (terminal.position().lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                            || !tailFallsDown(branch, segment)) {
                        return false;
                    }
                }
            }
        }
        return ascentFitsDeclaredHeight()
                && hasNoSecondaryBurst()
                && hasStaggeredRetirement(payloadSeed)
                && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS);
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator accumulator = new BoundsAccumulator();
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            for (int segment = 0; segment < SEGMENTS_PER_BRANCH; segment++) {
                accumulator.include(sample(branch, segment).position());
                if (segment < RETAINED_TAIL_FIRST_SEGMENT) {
                    continue;
                }
                for (int sampleIndex = 0; sampleIndex < ENVELOPE_SAMPLES; sampleIndex++) {
                    int tailAge = Math.round((float) TAIL_EXTENSION_TICKS * sampleIndex / (ENVELOPE_SAMPLES - 1));
                    accumulator.include(tailSample(branch, segment, tailAge).position());
                }
            }
        }
        return accumulator.toBounds();
    }

    private static Vec3 initialPosition(Branch branch, double progress) {
        double radius = INITIAL_RADIUS + EXPANSION_RADIUS * smoothStep(progress);
        Vec3 horizontalDirection = horizontalDirection(branch);
        double horizontal = Math.sqrt(branch.direction().x * branch.direction().x
                + branch.direction().z * branch.direction().z);
        double canopy = CANOPY_LIFT * Math.sin(Math.PI * progress) * (0.35D + 0.65D * horizontal);
        Vec3 raw = horizontalDirection.scale(radius * horizontal)
                .add(0.0D, branch.direction().y * radius * INITIAL_VERTICAL_SCALE + canopy, 0.0D);
        return radiusSafe(raw);
    }

    private static Vec3 horizontalDirection(Branch branch) {
        double horizontalLength = Math.sqrt(branch.direction().x * branch.direction().x
                + branch.direction().z * branch.direction().z);
        if (horizontalLength < RADIUS_EPSILON) {
            return new Vec3(Math.cos(branch.azimuth()), 0.0D, Math.sin(branch.azimuth()));
        }
        return new Vec3(branch.direction().x / horizontalLength, 0.0D, branch.direction().z / horizontalLength);
    }

    private static Vec3 radiusSafe(Vec3 value) {
        double lengthSqr = value.lengthSqr();
        double limitSqr = RADIUS_LIMIT * RADIUS_LIMIT;
        return lengthSqr <= limitSqr ? value : value.scale(RADIUS_LIMIT / Math.sqrt(lengthSqr));
    }

    private static double longTailEase(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Super-willow branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Super-willow segment index is outside the configured count");
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

    private static final class BoundsAccumulator {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;
        private double maxDistance;

        private void include(Vec3 value) {
            this.minX = Math.min(this.minX, value.x);
            this.minY = Math.min(this.minY, value.y);
            this.minZ = Math.min(this.minZ, value.z);
            this.maxX = Math.max(this.maxX, value.x);
            this.maxY = Math.max(this.maxY, value.y);
            this.maxZ = Math.max(this.maxZ, value.z);
            this.maxDistance = Math.max(this.maxDistance, value.length());
        }

        private Bounds toBounds() {
            return new Bounds(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.maxDistance);
        }
    }
}
