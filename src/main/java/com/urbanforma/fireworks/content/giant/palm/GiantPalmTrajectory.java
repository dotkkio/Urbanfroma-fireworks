package com.urbanforma.fireworks.content.giant.palm;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, unregistered geometry for a giant jade-and-gold palm break.
 *
 * <p>The 0-to-200 ascent is only proof data for the later entity adapter. This class creates no particles,
 * starts no entity, and depends on no registry, network, or shared scheduler type. The visual is a crown of
 * upward stems followed by five separately curved, falling fronds from every crown anchor.</p>
 */
public final class GiantPalmTrajectory {
    public static final String STABLE_ID = "giant_jade_gold_palm_firework";
    public static final String DEFAULT_ENGLISH_NAME = "Giant Jade-Gold Palm Break Firework";
    public static final String DEFAULT_CHINESE_NAME = "\u5de8\u578b\u7fe1\u7fe0\u91d1\u68d5\u6988\u5782\u7206\u70df\u82b1";
    /** Suggested future enum value only. This isolated class intentionally does not import GiantTier. */
    public static final String SUGGESTED_GIANT_TIER = "PALM";

    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 126.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.24F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;
    /** One future entity request maps to exactly one visual detonation plan. */
    public static final int DETONATIONS_PER_REQUEST = 1;

    public static final int BRANCH_COUNT = 96;
    public static final int STEM_SEGMENTS_PER_BRANCH = 24;
    public static final int FRONDS_PER_BRANCH = 5;
    public static final int FROND_SEGMENTS_PER_FROND = 18;
    public static final int STEM_START_TICK = 0;
    public static final int STEM_END_TICK_EXCLUSIVE = STEM_START_TICK + STEM_SEGMENTS_PER_BRANCH;
    public static final int FROND_START_TICK = 20;
    public static final int FROND_END_TICK_EXCLUSIVE = FROND_START_TICK + FROND_SEGMENTS_PER_FROND;
    public static final int LAST_EMISSION_TICK = FROND_END_TICK_EXCLUSIVE - 1;

    public static final int STEM_PARTICLES = BRANCH_COUNT * STEM_SEGMENTS_PER_BRANCH;
    public static final int FROND_PARTICLES = BRANCH_COUNT * FRONDS_PER_BRANCH * FROND_SEGMENTS_PER_FROND;
    public static final int TOTAL_PARTICLES = STEM_PARTICLES + FROND_PARTICLES;
    /** Local effect peak, derived from overlapping stem and frond phases, not a shared scheduler allowance. */
    public static final int MAX_PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT * (FRONDS_PER_BRANCH + 1);
    public static final int MIN_STEM_LIFETIME = 104;
    public static final int MAX_STEM_LIFETIME = 126;
    public static final int MIN_FROND_LIFETIME = 156;
    public static final int MAX_FROND_LIFETIME = 172;
    public static final int MAX_PARTICLE_LIFETIME = MAX_FROND_LIFETIME;
    public static final int DESCENT_TICKS = 126;
    public static final int TOTAL_VISUAL_TICKS = LAST_EMISSION_TICK + MAX_PARTICLE_LIFETIME + 1;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;

    /** Triangle inequality proves every moving frond stays inside 44 + 58 + 16 = 118 blocks. */
    public static final double CROWN_ANCHOR_RADIUS = 44.0D;
    public static final double FROND_MIN_EXTENSION = 4.0D;
    public static final double FROND_MAX_EXTENSION = 58.0D;
    public static final double TERMINAL_DROP = 16.0D;
    public static final double PROVEN_FROND_DISTANCE_BOUND = CROWN_ANCHOR_RADIUS + FROND_MAX_EXTENSION + TERMINAL_DROP;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 18;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 28;

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double MIN_CROWN_ELEVATION = Math.toRadians(16.0D);
    private static final double MAX_CROWN_ELEVATION = Math.toRadians(60.0D);
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final Vec3 DOWN = new Vec3(0.0D, -1.0D, 0.0D);
    private static final long BRANCH_SALT = 0x5A4B1E7D94C2F311L;
    private static final long AZIMUTH_SALT = 0xC2B2AE3D27D4EB4FL;
    private static final long LEAF_SALT = 0xD6E8FEB86659FD93L;
    private static final long LIFETIME_SALT = 0x94D049BB133111EBL;
    private static final long BRIGHTNESS_SALT = 0xBF58476D1CE4E5B9L;

    private GiantPalmTrajectory() {
    }

    public enum Stage {
        STEM,
        FROND
    }

    /** The color cadence moves from ivory heart through gold trunk to jade, gold, and ember leaf tips. */
    public enum ColorBand {
        CORE_IVORY(new Rgb(1.0F, 0.96F, 0.80F), 1.50F),
        STEM_GOLD(new Rgb(1.0F, 0.72F, 0.18F), 1.38F),
        STEM_AMBER(new Rgb(1.0F, 0.44F, 0.06F), 1.28F),
        LEAF_JADE(new Rgb(0.20F, 0.88F, 0.55F), 1.34F),
        LEAF_GOLD(new Rgb(1.0F, 0.76F, 0.20F), 1.36F),
        LEAF_EMBER(new Rgb(1.0F, 0.28F, 0.05F), 1.22F);

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
                throw new IllegalArgumentException("Invalid giant palm ascent sample");
            }
        }
    }

    /** Each branch owns a raised crown direction and two perpendicular axes for its five fronds. */
    public record PalmBranch(int index, long seed, double azimuth, Vec3 direction, Vec3 lateral, Vec3 normal) {
        public PalmBranch {
            if (index < 0 || index >= BRANCH_COUNT || !Double.isFinite(azimuth)
                    || !finite(direction) || !finite(lateral) || !finite(normal) || direction.y <= 0.0D) {
                throw new IllegalArgumentException("Invalid giant palm branch");
            }
        }
    }

    public record PalmSample(
            Stage stage,
            PalmBranch branch,
            int leafIndex,
            int segmentIndex,
            int emissionTick,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime) {
        public PalmSample {
            boolean stem = stage == Stage.STEM;
            int limit = stem ? STEM_SEGMENTS_PER_BRANCH : FROND_SEGMENTS_PER_FROND;
            int expectedTick = stem ? STEM_START_TICK + segmentIndex : FROND_START_TICK + segmentIndex;
            int minimumLifetime = stem ? MIN_STEM_LIFETIME : MIN_FROND_LIFETIME;
            int maximumLifetime = stem ? MAX_STEM_LIFETIME : MAX_FROND_LIFETIME;
            if (stage == null || branch == null || (stem ? leafIndex != -1 : leafIndex < 0 || leafIndex >= FRONDS_PER_BRANCH)
                    || segmentIndex < 0 || segmentIndex >= limit || emissionTick != expectedTick
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || !finite(position)
                    || colorBand == null || !Float.isFinite(brightness) || brightness < 1.0F
                    || lifetime < minimumLifetime || lifetime > maximumLifetime || !fitsRadius(position)) {
                throw new IllegalArgumentException("Invalid giant palm sample");
            }
        }
    }

    public record RetirementFlicker(int startAge, int phase) {
        public RetirementFlicker {
            if (startAge < 0 || phase < 0 || phase > 2) {
                throw new IllegalArgumentException("Invalid giant palm retirement cadence");
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
                throw new IllegalArgumentException("Giant palm bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius > 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Per-instance proof accounting; it is deliberately independent from shared client particle budgets. */
    public record ParticlePlan(
            int tick, int createdThisTick, int cumulativeCreated, int activeUpperBound, int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0 || cumulativeCreated > TOTAL_PARTICLES
                    || activeUpperBound > PROTOTYPE_MAX_ALIVE_PARTICLES) {
                throw new IllegalArgumentException("Invalid giant palm particle accounting");
            }
        }
    }

    /** Returns the exact 0-to-200 flight proof; actual launch ownership remains with the future adapter. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Giant palm ascent tick is outside the 0-to-200 path");
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

    public static PalmBranch branch(long payloadSeed, int branchIndex) {
        validateBranchIndex(branchIndex);
        long seed = mix64(payloadSeed ^ BRANCH_SALT ^ (long) branchIndex * 0x9E3779B97F4A7C15L);
        double layer = ((branchIndex * 37) % BRANCH_COUNT + 0.5D) / BRANCH_COUNT;
        double elevation = MIN_CROWN_ELEVATION + (MAX_CROWN_ELEVATION - MIN_CROWN_ELEVATION) * layer;
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ AZIMUTH_SALT) * TWO_PI
                + centered(seed, AZIMUTH_SALT) * 0.055D;
        Vec3 direction = new Vec3(
                Math.cos(elevation) * Math.cos(azimuth),
                Math.sin(elevation),
                Math.cos(elevation) * Math.sin(azimuth)).normalize();
        Vec3 lateral = new Vec3(-Math.sin(azimuth), 0.0D, Math.cos(azimuth)).normalize();
        Vec3 normal = direction.cross(lateral).normalize();
        return new PalmBranch(branchIndex, seed, azimuth, direction, lateral, normal);
    }

    public static PalmSample stemSample(long payloadSeed, int branchIndex, int segmentIndex) {
        PalmBranch branch = branch(payloadSeed, branchIndex);
        validateStemSegmentIndex(segmentIndex);
        double progress = (double) segmentIndex / (STEM_SEGMENTS_PER_BRANCH - 1);
        Vec3 position = branch.direction().scale(stemRadius(progress));
        int lifetime = randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, MIN_STEM_LIFETIME, MAX_STEM_LIFETIME);
        return new PalmSample(
                Stage.STEM,
                branch,
                -1,
                segmentIndex,
                STEM_START_TICK + segmentIndex,
                progress,
                position,
                stemColorBand(segmentIndex),
                brightness(branch.seed(), segmentIndex),
                lifetime);
    }

    public static PalmSample frondSample(long payloadSeed, int branchIndex, int leafIndex, int segmentIndex) {
        PalmBranch branch = branch(payloadSeed, branchIndex);
        validateLeafIndex(leafIndex);
        validateFrondSegmentIndex(segmentIndex);
        double progress = (double) segmentIndex / (FROND_SEGMENTS_PER_FROND - 1);
        Vec3 position = frondPosition(branch, leafIndex, segmentIndex, 0);
        int lifetime = randomInt(
                branch.seed() ^ ((long) leafIndex * 0xD6E8FEB86659FD93L),
                LIFETIME_SALT + segmentIndex,
                MIN_FROND_LIFETIME,
                MAX_FROND_LIFETIME);
        return new PalmSample(
                Stage.FROND,
                branch,
                leafIndex,
                segmentIndex,
                FROND_START_TICK + segmentIndex,
                progress,
                position,
                frondColorBand(leafIndex, segmentIndex),
                brightness(branch.seed() ^ (long) leafIndex * LEAF_SALT, segmentIndex),
                lifetime);
    }

    /** Moves an existing leaf spark only; the tail path allocates neither particles nor child explosions. */
    public static Vec3 frondPosition(PalmBranch branch, int leafIndex, int segmentIndex, int tailAge) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant palm branch may not be null");
        }
        validateLeafIndex(leafIndex);
        validateFrondSegmentIndex(segmentIndex);
        if (tailAge < 0 || tailAge > DESCENT_TICKS) {
            throw new IllegalArgumentException("Giant palm tail age is outside the fixed descent window");
        }
        double progress = (double) segmentIndex / (FROND_SEGMENTS_PER_FROND - 1);
        double tailProgress = (double) tailAge / DESCENT_TICKS;
        Vec3 anchor = crownAnchor(branch);
        Vec3 leafDirection = leafDirection(branch, leafIndex, progress, tailProgress);
        double extension = FROND_MIN_EXTENSION
                + (FROND_MAX_EXTENSION - FROND_MIN_EXTENSION) * smoothStep(progress);
        double drop = TERMINAL_DROP * smoothStep(tailProgress);
        Vec3 position = anchor.add(leafDirection.scale(extension)).add(DOWN.scale(drop));
        if (!fitsRadius(position)) {
            throw new IllegalStateException("Giant palm frond escaped its declared envelope");
        }
        return position;
    }

    public static boolean isStemEmitting(int tick) {
        return tick >= STEM_START_TICK && tick < STEM_END_TICK_EXCLUSIVE;
    }

    public static boolean isFrondEmitting(int tick) {
        return tick >= FROND_START_TICK && tick < FROND_END_TICK_EXCLUSIVE;
    }

    public static int particlesCreatedThisTick(int tick) {
        int count = isStemEmitting(tick) ? BRANCH_COUNT : 0;
        if (isFrondEmitting(tick)) {
            count += BRANCH_COUNT * FRONDS_PER_BRANCH;
        }
        return count;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int stemTicks = Math.max(0, Math.min(STEM_SEGMENTS_PER_BRANCH, tick - STEM_START_TICK + 1));
        int frondTicks = Math.max(0, Math.min(FROND_SEGMENTS_PER_FROND, tick - FROND_START_TICK + 1));
        return stemTicks * BRANCH_COUNT + frondTicks * BRANCH_COUNT * FRONDS_PER_BRANCH;
    }

    /** Conservative: every created particle is assumed to survive for the maximum 172-tick lifetime. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int firstLiveTick = Math.max(0, tick - MAX_PARTICLE_LIFETIME + 1);
        return particlesCreatedThroughTick(tick) - particlesCreatedThroughTick(firstLiveTick - 1);
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Giant palm particle-plan tick may not be negative");
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

    public static RetirementFlicker retirementFlicker(PalmSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Giant palm sample may not be null");
        }
        int spread = RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1;
        int owner = sample.branch().index() * 31 + (sample.leafIndex() + 1) * 17 + sample.segmentIndex() * 13;
        int lead = RETIREMENT_FLICKER_MIN_LEAD_TICKS + Math.floorMod(owner, spread);
        return new RetirementFlicker(sample.lifetime() - lead, Math.floorMod(owner * 7, 3));
    }

    /** Exhaustive finite bounds over every stem, leaf, and moving leaf-tail sample for one payload seed. */
    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator bounds = new BoundsAccumulator();
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            PalmBranch branch = branch(payloadSeed, branchIndex);
            for (int segment = 0; segment < STEM_SEGMENTS_PER_BRANCH; segment++) {
                bounds.include(stemSample(payloadSeed, branchIndex, segment).position());
            }
            for (int leaf = 0; leaf < FRONDS_PER_BRANCH; leaf++) {
                for (int segment = 0; segment < FROND_SEGMENTS_PER_FROND; segment++) {
                    for (int tailAge = 0; tailAge <= DESCENT_TICKS; tailAge++) {
                        bounds.include(frondPosition(branch, leaf, segment, tailAge));
                    }
                }
            }
        }
        return bounds.toBounds();
    }

    /** Verifies the raised crown, five-leaf fan, falling endpoints, and one-shot scheduled population. */
    public static boolean staticContractHolds(long payloadSeed) {
        if (!ascentFitsDeclaredHeight() || DETONATION_SOUND_MAX_PLAYS != 1 || DETONATIONS_PER_REQUEST != 1
                || STEM_PARTICLES != 2_304
                || FROND_PARTICLES != 8_640 || TOTAL_PARTICLES != 10_944
                || MAX_PARTICLES_PER_EMISSION_TICK != 576 || PROVEN_FROND_DISTANCE_BOUND > MAX_RADIUS
                || particlesCreatedThroughTick(TOTAL_VISUAL_TICKS) != TOTAL_PARTICLES
                || particlesCreatedThisTick(TOTAL_VISUAL_TICKS) != 0
                || maximumAliveParticleUpperBound() != TOTAL_PARTICLES) {
            return false;
        }
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            PalmBranch branch = branch(payloadSeed, branchIndex);
            Vec3 anchor = crownAnchor(branch);
            if (branch.direction().y <= 0.0D || anchor.length() > CROWN_ANCHOR_RADIUS + RADIUS_EPSILON) {
                return false;
            }
            for (int leaf = 0; leaf < FRONDS_PER_BRANCH; leaf++) {
                Vec3 terminal = frondPosition(branch, leaf, FROND_SEGMENTS_PER_FROND - 1, DESCENT_TICKS);
                if (terminal.y >= anchor.y || !fitsRadius(terminal)) {
                    return false;
                }
            }
        }
        return conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS);
    }

    /** Throws if a future integration changes the isolated one-flight, one-detonation geometry contract. */
    public static void validateContract() {
        if (!staticContractHolds(0x145A9D3BC76E2F01L)) {
            throw new IllegalStateException("Giant palm contract is inconsistent");
        }
    }

    public static boolean fitsRadius(Vec3 position) {
        return finite(position) && position.lengthSqr() <= MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON;
    }

    private static Vec3 crownAnchor(PalmBranch branch) {
        return branch.direction().scale(CROWN_ANCHOR_RADIUS);
    }

    private static Vec3 leafDirection(PalmBranch branch, int leafIndex, double progress, double tailProgress) {
        double angle = TWO_PI * leafIndex / FRONDS_PER_BRANCH
                + centered(branch.seed(), LEAF_SALT + leafIndex) * 0.12D;
        Vec3 aroundAxis = branch.lateral().scale(Math.cos(angle)).add(branch.normal().scale(Math.sin(angle)));
        Vec3 initial = branch.direction().scale(0.90D).add(aroundAxis.scale(0.50D)).normalize();
        double bend = 0.72D * smoothStep((progress - 0.18D) / 0.82D)
                + 0.16D * smoothStep(tailProgress);
        return interpolateDirection(initial, DOWN, bend);
    }

    private static Vec3 interpolateDirection(Vec3 first, Vec3 second, double amount) {
        double bounded = clamp(amount, 0.0D, 0.94D);
        return first.scale(1.0D - bounded).add(second.scale(bounded)).normalize();
    }

    private static ColorBand stemColorBand(int segmentIndex) {
        return segmentIndex < 6 ? ColorBand.CORE_IVORY
                : (segmentIndex < 17 ? ColorBand.STEM_GOLD : ColorBand.STEM_AMBER);
    }

    private static ColorBand frondColorBand(int leafIndex, int segmentIndex) {
        int offset = Math.floorMod(leafIndex * 3, 5);
        if (segmentIndex < 4 + offset % 2) {
            return ColorBand.LEAF_JADE;
        }
        return segmentIndex < 13 ? ColorBand.LEAF_GOLD : ColorBand.LEAF_EMBER;
    }

    private static double stemRadius(double progress) {
        return 2.0D + (CROWN_ANCHOR_RADIUS - 2.0D) * smoothStep(progress);
    }

    private static float brightness(long seed, int segmentIndex) {
        return (float) (1.03D + randomUnit(seed ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.16D);
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Giant palm branch index is outside the configured count");
        }
    }

    private static void validateLeafIndex(int leafIndex) {
        if (leafIndex < 0 || leafIndex >= FRONDS_PER_BRANCH) {
            throw new IllegalArgumentException("Giant palm leaf index is outside the configured count");
        }
    }

    private static void validateStemSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= STEM_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Giant palm stem segment is outside the configured count");
        }
    }

    private static void validateFrondSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= FROND_SEGMENTS_PER_FROND) {
            throw new IllegalArgumentException("Giant palm frond segment is outside the configured count");
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

    private static boolean finite(Vec3 value) {
        return value != null && Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
    }

    private static final class BoundsAccumulator {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;
        private double maxDistance;

        private void include(Vec3 position) {
            this.minX = Math.min(this.minX, position.x);
            this.minY = Math.min(this.minY, position.y);
            this.minZ = Math.min(this.minZ, position.z);
            this.maxX = Math.max(this.maxX, position.x);
            this.maxY = Math.max(this.maxY, position.y);
            this.maxZ = Math.max(this.maxZ, position.z);
            this.maxDistance = Math.max(this.maxDistance, position.length());
        }

        private Bounds toBounds() {
            return new Bounds(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.maxDistance);
        }
    }
}
