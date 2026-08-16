package com.urbanforma.fireworks.content.giant.willow;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side geometry for the stable EXTRA_LARGE giant willow style.
 *
 * <p>The burst is a single event made from a complete Fibonacci-sphere outer shell, an offset gold-white
 * radial crown, and a compact pearl-white core. The outer shell retains only its final strands; those existing
 * client particles descend during the willow phase and no second burst is scheduled.</p>
 */
public final class GiantWillowTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    /** Preserves the shared 0-to-200 giant ascent contract. */
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.30F;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** Local program budget, deliberately independent of any shared particle limiter. */
    public static final int MAX_CLIENT_PARTICLES_PER_TICK = 352;
    public static final int MAX_CLIENT_ACTIVE_PROGRAMS = 1;
    public static final int MAX_CLIENT_PENDING_REQUESTS = 2;

    public static final int OUTER_BRANCH_COUNT = 192;
    public static final int OUTER_SEGMENTS_PER_BRANCH = 44;
    public static final int RADIANT_CROWN_BRANCH_COUNT = 96;
    public static final int RADIANT_CROWN_SEGMENTS_PER_BRANCH = 24;
    public static final int WHITE_CORE_BRANCH_COUNT = 64;
    public static final int WHITE_CORE_SEGMENTS_PER_BRANCH = 24;

    /** Compatibility names for the outer full-sphere shell. */
    public static final int BRANCH_COUNT = OUTER_BRANCH_COUNT;
    public static final int SEGMENTS_PER_BRANCH = OUTER_SEGMENTS_PER_BRANCH;
    public static final int EMISSION_TICKS = OUTER_SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = OUTER_BRANCH_COUNT;

    public static final int OUTER_PARTICLES = OUTER_BRANCH_COUNT * OUTER_SEGMENTS_PER_BRANCH;
    public static final int RADIANT_CROWN_PARTICLES = RADIANT_CROWN_BRANCH_COUNT * RADIANT_CROWN_SEGMENTS_PER_BRANCH;
    public static final int WHITE_CORE_PARTICLES = WHITE_CORE_BRANCH_COUNT * WHITE_CORE_SEGMENTS_PER_BRANCH;
    public static final int TOTAL_PARTICLES = OUTER_PARTICLES + RADIANT_CROWN_PARTICLES + WHITE_CORE_PARTICLES;

    public static final int RETAINED_TAIL_FIRST_SEGMENT = 32;
    public static final int RETAINED_TAIL_SEGMENT_COUNT =
            OUTER_SEGMENTS_PER_BRANCH - RETAINED_TAIL_FIRST_SEGMENT;
    public static final int MAX_RETAINED_TAILS = OUTER_BRANCH_COUNT * RETAINED_TAIL_SEGMENT_COUNT;
    public static final int TAIL_START_TICK = OUTER_SEGMENTS_PER_BRANCH;
    public static final int TAIL_EXTENSION_TICKS = 200;
    public static final int POST_TAIL_FADE_TICKS = 21;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 18;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 24;
    public static final int RETIREMENT_END_STAGGER_MAX_TICKS = 6;
    public static final int TOTAL_VISUAL_TICKS = TAIL_START_TICK
            + TAIL_EXTENSION_TICKS + POST_TAIL_FADE_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS;
    public static final int MIN_PARTICLE_LIFETIME = 54;
    public static final int MAX_PARTICLE_LIFETIME = 242;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;

    private static final double OUTER_INITIAL_RADIUS = 2.5D;
    private static final double TAIL_OUTWARD_EXTENSION = 3.0D;
    private static final double TAIL_EXTRA_DROP = 10.0D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final int ENVELOPE_SAMPLES = 101;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long BRANCH_SALT = 0xD6E8FEB86659FD93L;
    private static final long DIRECTION_SALT = 0xA4093822299F31D0L;
    private static final long VERTICAL_SALT = 0x13198A2E03707344L;
    private static final long TONE_SALT = 0x452821E638D01377L;
    private static final long LIFETIME_SALT = 0xBE5466CF34E90C6CL;

    private GiantWillowTrajectory() {
    }

    /** Three overlapping radial stages form one complete three-dimensional break. */
    public enum Stage {
        OUTER(0, OUTER_BRANCH_COUNT, OUTER_SEGMENTS_PER_BRANCH, 0, 98.0D, 18.0D, 0.44D, 82, 108),
        RADIANT_CROWN(1, RADIANT_CROWN_BRANCH_COUNT, RADIANT_CROWN_SEGMENTS_PER_BRANCH,
                8, 66.0D, 4.0D, 0.62D, 68, 90),
        WHITE_CORE(2, WHITE_CORE_BRANCH_COUNT, WHITE_CORE_SEGMENTS_PER_BRANCH,
                16, 32.0D, 0.0D, 1.0D, 54, 76);

        private final int index;
        private final int branchCount;
        private final int segmentsPerBranch;
        private final int startTick;
        private final double maximumRadius;
        private final double terminalDroop;
        private final double droopStart;
        private final int minimumLifetime;
        private final int maximumLifetime;

        Stage(
                int index,
                int branchCount,
                int segmentsPerBranch,
                int startTick,
                double maximumRadius,
                double terminalDroop,
                double droopStart,
                int minimumLifetime,
                int maximumLifetime) {
            this.index = index;
            this.branchCount = branchCount;
            this.segmentsPerBranch = segmentsPerBranch;
            this.startTick = startTick;
            this.maximumRadius = maximumRadius;
            this.terminalDroop = terminalDroop;
            this.droopStart = droopStart;
            this.minimumLifetime = minimumLifetime;
            this.maximumLifetime = maximumLifetime;
        }

        public int index() {
            return this.index;
        }

        public int branchCount() {
            return this.branchCount;
        }

        public int segmentsPerBranch() {
            return this.segmentsPerBranch;
        }

        public int startTick() {
            return this.startTick;
        }

        public int endTickExclusive() {
            return this.startTick + this.segmentsPerBranch;
        }

        public int particleCount() {
            return this.branchCount * this.segmentsPerBranch;
        }

        public boolean emitsAt(int tick) {
            return tick >= this.startTick && tick < endTickExclusive();
        }

        private double radiusAt(double progress) {
            return OUTER_INITIAL_RADIUS + (this.maximumRadius - OUTER_INITIAL_RADIUS) * smoothStep(progress);
        }

        private double droopAt(double progress) {
            if (progress <= this.droopStart || this.terminalDroop == 0.0D) {
                return 0.0D;
            }
            double droopProgress = (progress - this.droopStart) / (1.0D - this.droopStart);
            return this.terminalDroop * smoothStep(droopProgress);
        }
    }

    public enum ColorBand {
        GOLDEN_AMBER(new Rgb(1.0F, 0.70F, 0.18F), 1.26F),
        GOLD_WHITE(new Rgb(1.0F, 0.84F, 0.48F), 1.36F),
        WARM_WHITE(new Rgb(1.0F, 0.94F, 0.75F), 1.45F),
        PEARL_WHITE(new Rgb(1.0F, 0.99F, 0.89F), 1.56F);

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
                throw new IllegalArgumentException("Invalid giant willow ascent sample");
            }
        }
    }

    /** A deterministic full-sphere branch, never a restricted positive-elevation umbrella ray. */
    public record Branch(Stage stage, int index, long seed, Vec3 direction) {
        public Branch {
            if (stage == null || index < 0 || index >= stage.branchCount()
                    || direction == null || !finite(direction)
                    || Math.abs(direction.lengthSqr() - 1.0D) > 1.0E-8D) {
                throw new IllegalArgumentException("Invalid giant willow full-sphere branch");
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
            if (branch == null || segmentIndex < 0 || segmentIndex >= branch.stage().segmentsPerBranch()
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(radius) || radius < 0.0D || radius > MAX_RADIUS
                    || position == null || !finite(position) || colorBand == null || !Float.isFinite(colorTone)
                    || colorTone < 0.0F || colorTone > 1.0F || lifetime < MIN_PARTICLE_LIFETIME
                    || lifetime > MAX_PARTICLE_LIFETIME
                    || retainedTail != (branch.stage() == Stage.OUTER && isRetainedTailSegment(segmentIndex))) {
                throw new IllegalArgumentException("Invalid giant willow branch sample");
            }
        }
    }

    public record TailSample(Branch branch, int segmentIndex, int age, double progress, Vec3 position) {
        public TailSample {
            if (branch == null || branch.stage() != Stage.OUTER || !isRetainedTailSegment(segmentIndex)
                    || age < 0 || age > TAIL_EXTENSION_TICKS || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || !finite(position)) {
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
            return radius > 0.0D && this.maxDistance < radius;
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

    /** Relative to the tail phase start; endTick is exclusive. */
    public record RetirementFlicker(int startTick, int endTick, int cadencePhase) {
        public RetirementFlicker {
            if (startTick < 0 || endTick < startTick
                    || endTick > TAIL_EXTENSION_TICKS + POST_TAIL_FADE_TICKS + RETIREMENT_END_STAGGER_MAX_TICKS
                    || cadencePhase < 0 || cadencePhase > 1) {
                throw new IllegalArgumentException("Invalid giant willow retirement flicker");
            }
        }

        public boolean activeAt(int tailAge) {
            return tailAge >= this.startTick && tailAge < this.endTick;
        }
    }

    static {
        validateStaticConfiguration();
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

    /** Compatibility accessor for the outer shell. */
    public static Branch branch(long payloadSeed, int branchIndex) {
        return branch(payloadSeed, Stage.OUTER, branchIndex);
    }

    public static Branch branch(long payloadSeed, Stage stage, int branchIndex) {
        if (stage == null || branchIndex < 0 || branchIndex >= stage.branchCount()) {
            throw new IllegalArgumentException("Giant willow branch index is outside the configured stage");
        }
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT
                ^ ((long) stage.index() * 0x9E3779B97F4A7C15L)
                ^ ((long) branchIndex * 0xD1342543DE82EF95L));
        double fraction = ((double) branchIndex + 0.5D) / stage.branchCount();
        double vertical = clamp(
                1.0D - 2.0D * fraction + centered(branchSeed, VERTICAL_SALT) * 0.012D,
                -0.998D,
                0.998D);
        double azimuth = branchIndex * GOLDEN_ANGLE
                + stage.index() * 0.517D
                + randomUnit(payloadSeed ^ DIRECTION_SALT ^ stage.index()) * TWO_PI
                + centered(branchSeed, DIRECTION_SALT) * 0.040D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - vertical * vertical));
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth),
                vertical,
                horizontal * Math.sin(azimuth)).normalize();
        return new Branch(stage, branchIndex, branchSeed, direction);
    }

    /** Compatibility accessor for one outer-shell sample. */
    public static BranchSample sample(long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(branch(payloadSeed, Stage.OUTER, branchIndex), segmentIndex);
    }

    public static BranchSample sample(long payloadSeed, Stage stage, int branchIndex, int segmentIndex) {
        return sample(branch(payloadSeed, stage, branchIndex), segmentIndex);
    }

    public static BranchSample sample(Branch branch, int segmentIndex) {
        if (branch == null || segmentIndex < 0 || segmentIndex >= branch.stage().segmentsPerBranch()) {
            throw new IllegalArgumentException("Giant willow segment is outside the configured stage");
        }
        double progress = segmentIndex / (double) (branch.stage().segmentsPerBranch() - 1);
        boolean retained = branch.stage() == Stage.OUTER && isRetainedTailSegment(segmentIndex);
        return new BranchSample(
                branch,
                segmentIndex,
                progress,
                branch.stage().radiusAt(progress),
                position(branch, progress),
                colorBand(branch, segmentIndex),
                (float) randomUnit(branch.seed() ^ (TONE_SALT + segmentIndex)),
                lifetime(branch, segmentIndex, retained),
                retained);
    }

    /** Complete radial geometry with a bounded downward willow sag, never an elevation-limited dome. */
    public static Vec3 position(Branch branch, double progress) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant willow branch may not be null");
        }
        double boundedProgress = clamp(progress, 0.0D, 1.0D);
        Stage stage = branch.stage();
        double radius = stage.radiusAt(boundedProgress);
        return branch.direction().scale(radius).add(0.0D, -stage.droopAt(boundedProgress), 0.0D);
    }

    /** Moves only pre-existing final outer strands through a bounded hanging-tail phase. */
    public static TailSample tailSample(Branch branch, int segmentIndex, int tailAge) {
        if (branch == null || branch.stage() != Stage.OUTER || !isRetainedTailSegment(segmentIndex)) {
            throw new IllegalArgumentException("Only retained outer giant willow segments may continue");
        }
        if (tailAge < 0 || tailAge > TAIL_EXTENSION_TICKS) {
            throw new IllegalArgumentException("Giant willow tail age is outside the 200-tick window");
        }
        double tailProgress = tailAge / (double) TAIL_EXTENSION_TICKS;
        double segmentProgress = segmentIndex / (double) (OUTER_SEGMENTS_PER_BRANCH - 1);
        Vec3 base = position(branch, segmentProgress);
        double horizontal = Math.sqrt(base.x * base.x + base.z * base.z);
        double extension = TAIL_OUTWARD_EXTENSION * smoothStep(tailProgress);
        double x = base.x;
        double z = base.z;
        if (horizontal > RADIUS_EPSILON) {
            x += base.x / horizontal * extension;
            z += base.z / horizontal * extension;
        }
        return new TailSample(
                branch,
                segmentIndex,
                tailAge,
                tailProgress,
                new Vec3(x, base.y - TAIL_EXTRA_DROP * smoothStep(tailProgress), z));
    }

    /** Compatibility coloring for a segment of the outer shell. */
    public static ColorBand colorBand(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= OUTER_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Giant willow outer segment is outside the configured count");
        }
        return colorBand(branch(0L, Stage.OUTER, 0), segmentIndex);
    }

    public static ColorBand colorBand(Branch branch, int segmentIndex) {
        if (branch == null || segmentIndex < 0 || segmentIndex >= branch.stage().segmentsPerBranch()) {
            throw new IllegalArgumentException("Giant willow color sample is outside the configured stage");
        }
        if (branch.stage() == Stage.WHITE_CORE) {
            return ColorBand.PEARL_WHITE;
        }
        int phase = Math.floorMod(branch.index() * 17 + segmentIndex * 11 + branch.stage().index() * 7, 12);
        if (branch.stage() == Stage.RADIANT_CROWN) {
            return phase < 5 ? ColorBand.WARM_WHITE : ColorBand.GOLD_WHITE;
        }
        if (phase < 3) {
            return ColorBand.PEARL_WHITE;
        }
        return phase < 8 ? ColorBand.GOLD_WHITE : ColorBand.GOLDEN_AMBER;
    }

    public static boolean isRetainedTailSegment(int segmentIndex) {
        return segmentIndex >= RETAINED_TAIL_FIRST_SEGMENT && segmentIndex < OUTER_SEGMENTS_PER_BRANCH;
    }

    /** Verifies the radial horizontal bearing remains stable while the point sags vertically. */
    public static boolean isPureRadialSample(Branch branch, Vec3 sample) {
        if (branch == null || sample == null) {
            return false;
        }
        double horizontal = Math.sqrt(sample.x * sample.x + sample.z * sample.z);
        return horizontal < RADIUS_EPSILON
                || Math.abs(sample.x * branch.direction().z - sample.z * branch.direction().x)
                        <= RADIUS_EPSILON * Math.max(1.0D, horizontal);
    }

    /** Static proof that the outer break covers both hemispheres and all eight spatial octants. */
    public static boolean fullSphereCoverageHolds(long payloadSeed) {
        int octants = 0;
        double minimumY = Double.POSITIVE_INFINITY;
        double maximumY = Double.NEGATIVE_INFINITY;
        for (int branchIndex = 0; branchIndex < OUTER_BRANCH_COUNT; branchIndex++) {
            Vec3 direction = branch(payloadSeed, Stage.OUTER, branchIndex).direction();
            minimumY = Math.min(minimumY, direction.y);
            maximumY = Math.max(maximumY, direction.y);
            int octant = (direction.x >= 0.0D ? 1 : 0)
                    | (direction.y >= 0.0D ? 2 : 0)
                    | (direction.z >= 0.0D ? 4 : 0);
            octants |= 1 << octant;
        }
        return minimumY < -0.90D && maximumY > 0.90D && octants == 0xFF;
    }

    public static RetirementFlicker retirementFlicker(Branch branch, int segmentIndex) {
        if (branch == null || branch.stage() != Stage.OUTER || !isRetainedTailSegment(segmentIndex)) {
            throw new IllegalArgumentException("Only retained outer giant willow segments may flicker");
        }
        int spread = RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1;
        int lead = RETIREMENT_FLICKER_MIN_LEAD_TICKS
                + Math.floorMod(branch.index() * 31 + segmentIndex * 17, spread);
        int endStagger = Math.floorMod(branch.index() * 13 + segmentIndex * 7,
                RETIREMENT_END_STAGGER_MAX_TICKS + 1);
        int endTick = TAIL_EXTENSION_TICKS + POST_TAIL_FADE_TICKS + endStagger;
        return new RetirementFlicker(
                endTick - lead,
                endTick,
                Math.floorMod(branch.index() * 17 + segmentIndex * 29, 2));
    }

    public static int particlesCreatedThisTick(int tick) {
        int created = 0;
        for (Stage stage : Stage.values()) {
            if (stage.emitsAt(tick)) {
                created += stage.branchCount();
            }
        }
        return created;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int created = 0;
        for (Stage stage : Stage.values()) {
            int emittedSegments = Math.max(0, Math.min(stage.segmentsPerBranch(), tick - stage.startTick() + 1));
            created += emittedSegments * stage.branchCount();
        }
        return created;
    }

    public static int activeParticleUpperBoundAtTick(int tick) {
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

    public static int maximumCreatedPerTick() {
        int maximum = 0;
        for (int tick = 0; tick < OUTER_SEGMENTS_PER_BRANCH; tick++) {
            maximum = Math.max(maximum, particlesCreatedThisTick(tick));
        }
        return maximum;
    }

    public static int finalParticleDisappearanceTick() {
        return TOTAL_VISUAL_TICKS;
    }

    /** Exhaustive deterministic proof over the full-sphere stages and all retained-tail samples. */
    public static boolean staticContractHolds(long payloadSeed) {
        if (!ascentFitsDeclaredHeight()
                || TOTAL_PARTICLES != 12_288
                || maximumCreatedPerTick() != MAX_CLIENT_PARTICLES_PER_TICK
                || maximumAliveParticleUpperBound() != TOTAL_PARTICLES
                || !fullSphereCoverageHolds(payloadSeed)
                || DETONATION_SOUND_MAX_PLAYS != 1) {
            return false;
        }
        int firstFlickerEnd = -1;
        boolean hasStaggeredFlickerEnds = false;
        for (Stage stage : Stage.values()) {
            for (int branchIndex = 0; branchIndex < stage.branchCount(); branchIndex++) {
                Branch branch = branch(payloadSeed, stage, branchIndex);
                for (int segment = 0; segment < stage.segmentsPerBranch(); segment++) {
                    BranchSample sample = sample(branch, segment);
                    if (!isPureRadialSample(branch, sample.position()) || !fitsRadius(sample.position())) {
                        return false;
                    }
                    if (sample.retainedTail()) {
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
        }
        return hasStaggeredFlickerEnds && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS);
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        double[] extrema = {
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                0.0D};
        for (Stage stage : Stage.values()) {
            for (int branchIndex = 0; branchIndex < stage.branchCount(); branchIndex++) {
                Branch branch = branch(payloadSeed, stage, branchIndex);
                for (int point = 0; point < ENVELOPE_SAMPLES; point++) {
                    include(extrema, position(branch, point / (double) (ENVELOPE_SAMPLES - 1)));
                }
                if (stage == Stage.OUTER) {
                    for (int segment = RETAINED_TAIL_FIRST_SEGMENT;
                            segment < OUTER_SEGMENTS_PER_BRANCH;
                            segment++) {
                        for (int tailAge = 0; tailAge <= TAIL_EXTENSION_TICKS; tailAge++) {
                            include(extrema, tailSample(branch, segment, tailAge).position());
                        }
                    }
                }
            }
        }
        return new Bounds(
                extrema[0], extrema[1], extrema[2], extrema[3], extrema[4], extrema[5], extrema[6]);
    }

    public static boolean fitsRadius(Vec3 position) {
        return position != null && finite(position)
                && position.lengthSqr() < MAX_RADIUS * MAX_RADIUS - RADIUS_EPSILON;
    }

    private static int lifetime(Branch branch, int segmentIndex, boolean retainedTail) {
        if (retainedTail) {
            RetirementFlicker flicker = retirementFlicker(branch, segmentIndex);
            int emissionTick = branch.stage().startTick() + segmentIndex;
            return TAIL_START_TICK + flicker.endTick() - emissionTick + 2;
        }
        Stage stage = branch.stage();
        return randomInt(
                branch.seed(),
                LIFETIME_SALT + segmentIndex,
                stage.minimumLifetime,
                stage.maximumLifetime);
    }

    private static void validateStaticConfiguration() {
        if (OUTER_PARTICLES != 8_448 || RADIANT_CROWN_PARTICLES != 2_304 || WHITE_CORE_PARTICLES != 1_536
                || TOTAL_PARTICLES != 12_288 || MAX_RETAINED_TAILS != 2_304
                || MAX_CLIENT_PARTICLES_PER_TICK != 352 || maximumCreatedPerTick() != MAX_CLIENT_PARTICLES_PER_TICK
                || TOTAL_VISUAL_TICKS != 271 || TAIL_EXTENSION_TICKS != 200) {
            throw new IllegalStateException("Giant willow deterministic budget drifted");
        }
    }

    private static void include(double[] extrema, Vec3 value) {
        extrema[0] = Math.min(extrema[0], value.x);
        extrema[1] = Math.min(extrema[1], value.y);
        extrema[2] = Math.min(extrema[2], value.z);
        extrema[3] = Math.max(extrema[3], value.x);
        extrema[4] = Math.max(extrema[4], value.y);
        extrema[5] = Math.max(extrema[5], value.z);
        extrema[6] = Math.max(extrema[6], value.length());
    }

    private static boolean finite(Vec3 value) {
        return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
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

    private static int randomInt(long seed, long salt, int minimum, int maximum) {
        return minimum + (int) Math.floor(randomUnit(seed ^ salt) * (maximum - minimum + 1));
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
