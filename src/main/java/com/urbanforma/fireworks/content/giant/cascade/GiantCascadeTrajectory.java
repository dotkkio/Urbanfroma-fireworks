package com.urbanforma.fireworks.content.giant.cascade;

import net.minecraft.world.phys.Vec3;

/**
 * Common-side deterministic geometry for the seventh giant firework.
 *
 * <p>One rocket reaches the shared 0-to-200 flight endpoint and detonates once. The first stage is a single
 * center, full-sphere shell. The second stage is a bounded set of medium radial blooms whose centers are
 * stratified across four spherical distance bands. Every value is derived from the payload seed; this class does
 * not use wall-clock time or an uncontrolled random source.</p>
 */
public final class GiantCascadeTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.26F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    public static final int MAIN_BRANCH_COUNT = 192;
    public static final int MAIN_SEGMENTS_PER_BRANCH = 32;
    public static final double MAIN_MAX_RADIUS = 118.0D;
    public static final int MAIN_START_TICK = 0;
    public static final int MAIN_END_TICK_EXCLUSIVE = MAIN_START_TICK + MAIN_SEGMENTS_PER_BRANCH;

    public static final int CHILD_BURST_COUNT = 32;
    public static final int CHILD_LAYER_COUNT = 4;
    public static final int CHILD_BURSTS_PER_LAYER = CHILD_BURST_COUNT / CHILD_LAYER_COUNT;
    public static final int CHILD_BRANCH_COUNT = 16;
    public static final int CHILD_SEGMENTS_PER_BRANCH = 10;
    /** Starts after the main shell's emission window, while its last arcs are in their short retirement tail. */
    public static final int STAGE_TWO_START_TICK = 88;
    public static final int CHILD_START_STAGGER_TICKS = 2;
    public static final double CHILD_MIN_RADIUS = 18.0D;
    public static final double CHILD_MAX_RADIUS = 20.0D;
    public static final int CHILD_MIN_CENTER_RADIUS = 14;
    public static final int CHILD_MAX_CENTER_RADIUS = 109;

    public static final int MAIN_PARTICLES = MAIN_BRANCH_COUNT * MAIN_SEGMENTS_PER_BRANCH;
    public static final int CHILD_PARTICLES = CHILD_BURST_COUNT * CHILD_BRANCH_COUNT * CHILD_SEGMENTS_PER_BRANCH;
    public static final int TOTAL_PARTICLES = MAIN_PARTICLES + CHILD_PARTICLES;
    public static final int MAX_PARTICLES_PER_EMISSION_TICK = MAIN_BRANCH_COUNT;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    public static final int MIN_PARTICLE_LIFETIME = 48;
    public static final int MAX_PARTICLE_LIFETIME = 112;
    public static final int LAST_CHILD_EMISSION_TICK =
            STAGE_TWO_START_TICK + (CHILD_BURST_COUNT - 1) * CHILD_START_STAGGER_TICKS
                    + CHILD_SEGMENTS_PER_BRANCH - 1;
    public static final int TOTAL_VISUAL_TICKS = LAST_CHILD_EMISSION_TICK + MAX_PARTICLE_LIFETIME + 1;

    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long MAIN_SALT = 0x4D595DF4D0F33173L;
    private static final long CHILD_SALT = 0x7F4A7C159E3779B9L;
    private static final long DIRECTION_SALT = 0xD1B54A32D192ED03L;
    private static final long CENTER_SALT = 0x94D049BB133111EBL;
    private static final long RADIUS_SALT = 0xC6BC279692B5CC83L;
    private static final long LIFETIME_SALT = 0xA35F568D90D0A4E1L;
    private static final long BRIGHTNESS_SALT = 0x7E4E4A3F12C22C7BL;
    private static final long ACCENT_SALT = 0x5E2D58D8B3A9581FL;

    private GiantCascadeTrajectory() {
    }

    /** Warm white and gold are the dominant layers; ember is deliberately sparse contrast. */
    public enum ColorBand {
        WARM_WHITE(new Rgb(1.0F, 0.95F, 0.78F), 1.48F),
        WARM_GOLD(new Rgb(1.0F, 0.72F, 0.18F), 1.38F),
        PALE_GOLD(new Rgb(1.0F, 0.88F, 0.45F), 1.44F),
        EMBER_ACCENT(new Rgb(1.0F, 0.31F, 0.10F), 1.22F);

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
            if (!Float.isFinite(red) || !Float.isFinite(green) || !Float.isFinite(blue)
                    || red < 0.0F || red > 1.0F || green < 0.0F || green > 1.0F
                    || blue < 0.0F || blue > 1.0F) {
                throw new IllegalArgumentException("RGB channels must be finite and between zero and one");
            }
        }
    }

    public record AscentSample(int tick, double progress, Vec3 position, Vec3 velocity) {
        public AscentSample {
            if (tick < 0 || tick >= ASCENT_TICKS || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || velocity == null) {
                throw new IllegalArgumentException("Invalid cascade ascent sample");
            }
        }
    }

    public record MainBranch(int index, long seed, Vec3 direction) {
        public MainBranch {
            if (index < 0 || index >= MAIN_BRANCH_COUNT || direction == null || !finite(direction)
                    || direction.lengthSqr() < RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid cascade main branch");
            }
        }
    }

    public record ChildBurst(int index, int layer, long seed, Vec3 center, double centerRadius,
                             double maximumRadius, int startTick) {
        public ChildBurst {
            if (index < 0 || index >= CHILD_BURST_COUNT || layer < 0 || layer >= CHILD_LAYER_COUNT
                    || center == null || !finite(center) || !Double.isFinite(centerRadius)
                    || centerRadius < CHILD_MIN_CENTER_RADIUS || centerRadius > CHILD_MAX_CENTER_RADIUS
                    || maximumRadius < CHILD_MIN_RADIUS || maximumRadius > CHILD_MAX_RADIUS
                    || centerRadius + maximumRadius > MAX_RADIUS + RADIUS_EPSILON
                    || startTick != STAGE_TWO_START_TICK + index * CHILD_START_STAGGER_TICKS) {
                throw new IllegalArgumentException("Invalid cascade child burst");
            }
        }

        public int branchCount() {
            return CHILD_BRANCH_COUNT;
        }

        public int segmentsPerBranch() {
            return CHILD_SEGMENTS_PER_BRANCH;
        }

        public int finalEmissionTick() {
            return this.startTick + CHILD_SEGMENTS_PER_BRANCH - 1;
        }

        public int particleCount() {
            return CHILD_BRANCH_COUNT * CHILD_SEGMENTS_PER_BRANCH;
        }
    }

    public record BranchSample(boolean mainStage, int ownerIndex, int branchIndex, int segmentIndex,
                               int emissionTick, double progress, Vec3 position, ColorBand colorBand,
                               float brightness, int lifetime, boolean coreHighlight) {
        public BranchSample {
            if (branchIndex < 0 || segmentIndex < 0 || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || !finite(position)
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                    || colorBand == null || !Float.isFinite(brightness) || brightness < 1.0F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME) {
                throw new IllegalArgumentException("Invalid cascade branch sample");
            }
        }
    }

    public record ParticlePlan(int tick, int createdThisTick, int cumulativeCreated,
                               int activeUpperBound, int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0 || cumulativeCreated > TOTAL_PARTICLES
                    || activeUpperBound > PROTOTYPE_MAX_ALIVE_PARTICLES) {
                throw new IllegalArgumentException("Invalid cascade particle plan");
            }
        }
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                         double maxDistance) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || !Double.isFinite(maxDistance) || minX > maxX || minY > maxY || minZ > maxZ
                    || maxDistance < 0.0D) {
                throw new IllegalArgumentException("Cascade bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    static {
        validateContract();
    }

    /** The flight is exactly one straight, server-owned 0-to-200 path. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Cascade ascent tick is outside the 0-to-200 path");
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

    public static MainBranch mainBranch(long payloadSeed, int branchIndex) {
        if (branchIndex < 0 || branchIndex >= MAIN_BRANCH_COUNT) {
            throw new IllegalArgumentException("Cascade main branch index is outside the contract");
        }
        long seed = mix64(payloadSeed ^ MAIN_SALT ^ (long) branchIndex * 0xD1342543DE82EF95L);
        double fraction = ((double) branchIndex + 0.5D) / MAIN_BRANCH_COUNT;
        double latitude = Math.asin(clamp(1.0D - 2.0D * fraction, -1.0D, 1.0D))
                + centered(seed, DIRECTION_SALT) * 0.018D;
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ DIRECTION_SALT) * TWO_PI
                + centered(seed, CENTER_SALT) * 0.035D;
        double horizontal = Math.cos(latitude);
        Vec3 direction = new Vec3(horizontal * Math.cos(azimuth), Math.sin(latitude),
                horizontal * Math.sin(azimuth)).normalize();
        return new MainBranch(branchIndex, seed, direction);
    }

    /** Four radial bands each visit every octant once, then receive deterministic seed jitter. */
    public static ChildBurst childBurst(long payloadSeed, int burstIndex) {
        if (burstIndex < 0 || burstIndex >= CHILD_BURST_COUNT) {
            throw new IllegalArgumentException("Cascade child index is outside the contract");
        }
        int layer = burstIndex / CHILD_BURSTS_PER_LAYER;
        long seed = mix64(payloadSeed ^ CHILD_SALT ^ (long) burstIndex * 0xD6E8FEB86659FD93L);
        int octant = burstIndex % CHILD_BURSTS_PER_LAYER;
        double x = signedOctantComponent(octant, 1, randomUnit(seed ^ DIRECTION_SALT));
        double y = signedOctantComponent(octant, 2, randomUnit(seed ^ (DIRECTION_SALT + 1L)));
        double z = signedOctantComponent(octant, 4, randomUnit(seed ^ (DIRECTION_SALT + 2L)));
        Vec3 direction = new Vec3(x, y, z).normalize();
        int[] bandRadii = {18, 47, 76, 105};
        double centerRadius = clamp(bandRadii[layer] + centered(seed, RADIUS_SALT) * 5.0D,
                CHILD_MIN_CENTER_RADIUS, CHILD_MAX_CENTER_RADIUS);
        double maximumRadius = CHILD_MIN_RADIUS
                + randomUnit(seed ^ RADIUS_SALT) * (CHILD_MAX_RADIUS - CHILD_MIN_RADIUS);
        return new ChildBurst(burstIndex, layer, seed, direction.scale(centerRadius), centerRadius,
                maximumRadius, STAGE_TWO_START_TICK + burstIndex * CHILD_START_STAGGER_TICKS);
    }

    public static BranchSample mainSample(long payloadSeed, int branchIndex, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= MAIN_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Cascade main segment is outside the contract");
        }
        MainBranch branch = mainBranch(payloadSeed, branchIndex);
        double progress = (double) segmentIndex / (MAIN_SEGMENTS_PER_BRANCH - 1);
        double radius = 2.0D + (MAIN_MAX_RADIUS - 2.0D) * smoothStep(progress);
        Vec3 direction = branch.direction();
        Vec3 position = direction.scale(radius);
        return new BranchSample(true, 0, branchIndex, segmentIndex, MAIN_START_TICK + segmentIndex,
                progress, position, mainColor(segmentIndex),
                brightness(branch.seed(), segmentIndex), segmentLifetime(branch.seed(), segmentIndex, true),
                segmentIndex < 5);
    }

    public static BranchSample childSample(long payloadSeed, int burstIndex, int branchIndex, int segmentIndex) {
        if (branchIndex < 0 || branchIndex >= CHILD_BRANCH_COUNT
                || segmentIndex < 0 || segmentIndex >= CHILD_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Cascade child branch sample is outside the contract");
        }
        ChildBurst burst = childBurst(payloadSeed, burstIndex);
        long seed = mix64(burst.seed() ^ (long) branchIndex * 0x9E3779B97F4A7C15L);
        double fraction = ((double) branchIndex + 0.5D) / CHILD_BRANCH_COUNT;
        double latitude = Math.asin(clamp(1.0D - 2.0D * fraction, -1.0D, 1.0D))
                + centered(seed, DIRECTION_SALT) * 0.035D;
        double azimuth = branchIndex * GOLDEN_ANGLE + randomUnit(burst.seed() ^ CENTER_SALT) * TWO_PI
                + centered(seed, DIRECTION_SALT ^ 0x7F4A7C159E3779B9L) * 0.06D;
        double horizontal = Math.cos(latitude);
        Vec3 direction = new Vec3(horizontal * Math.cos(azimuth), Math.sin(latitude),
                horizontal * Math.sin(azimuth)).normalize();
        double progress = (double) segmentIndex / (CHILD_SEGMENTS_PER_BRANCH - 1);
        double radius = 1.5D + (burst.maximumRadius() - 1.5D) * (1.0D - Math.pow(1.0D - progress, 1.35D));
        Vec3 position = burst.center().add(direction.scale(radius));
        boolean accent = Math.floorMod((int) (seed ^ ACCENT_SALT), 11) == 0 && segmentIndex >= 6;
        ColorBand color = accent ? ColorBand.EMBER_ACCENT : (segmentIndex < 3 ? ColorBand.WARM_WHITE : ColorBand.WARM_GOLD);
        return new BranchSample(false, burstIndex, branchIndex, segmentIndex,
                burst.startTick() + segmentIndex, progress, position, color,
                brightness(seed, segmentIndex), segmentLifetime(seed, segmentIndex, false), segmentIndex < 2);
    }

    public static boolean isMainEmitting(int tick) {
        return tick >= MAIN_START_TICK && tick < MAIN_END_TICK_EXCLUSIVE;
    }

    public static boolean isChildEmitting(int tick, int burstIndex) {
        if (burstIndex < 0 || burstIndex >= CHILD_BURST_COUNT) {
            return false;
        }
        ChildBurst burst = childBurst(0L, burstIndex);
        return tick >= burst.startTick() && tick <= burst.finalEmissionTick();
    }

    public static int particlesCreatedThisTick(int tick) {
        int count = isMainEmitting(tick) ? MAIN_BRANCH_COUNT : 0;
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            if (isChildEmitting(tick, index)) {
                count += CHILD_BRANCH_COUNT;
            }
        }
        return count;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int count = Math.min(MAIN_PARTICLES, Math.max(0, Math.min(MAIN_SEGMENTS_PER_BRANCH, tick + 1))
                * MAIN_BRANCH_COUNT);
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            ChildBurst burst = childBurst(0L, index);
            int emitted = Math.max(0, Math.min(CHILD_SEGMENTS_PER_BRANCH, tick - burst.startTick() + 1));
            count += emitted * CHILD_BRANCH_COUNT;
        }
        return Math.min(TOTAL_PARTICLES, count);
    }

    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int count = 0;
        int mainFirst = Math.max(MAIN_START_TICK, tick - MAX_PARTICLE_LIFETIME + 1);
        int mainLast = Math.min(tick, MAIN_END_TICK_EXCLUSIVE - 1);
        if (mainFirst <= mainLast) {
            count += (mainLast - mainFirst + 1) * MAIN_BRANCH_COUNT;
        }
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            ChildBurst burst = childBurst(0L, index);
            int first = Math.max(burst.startTick(), tick - MAX_PARTICLE_LIFETIME + 1);
            int last = Math.min(tick, burst.finalEmissionTick());
            if (first <= last) {
                count += (last - first + 1) * CHILD_BRANCH_COUNT;
            }
        }
        return Math.min(PROTOTYPE_MAX_ALIVE_PARTICLES, count);
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Cascade particle-plan tick may not be negative");
        }
        int cumulative = particlesCreatedThroughTick(tick);
        return new ParticlePlan(tick, particlesCreatedThisTick(tick), cumulative,
                activeParticleUpperBoundAtTick(tick), TOTAL_PARTICLES - cumulative);
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator bounds = new BoundsAccumulator();
        for (int branch = 0; branch < MAIN_BRANCH_COUNT; branch++) {
            for (int segment = 0; segment < MAIN_SEGMENTS_PER_BRANCH; segment++) {
                bounds.include(mainSample(payloadSeed, branch, segment).position());
            }
        }
        for (int burst = 0; burst < CHILD_BURST_COUNT; burst++) {
            for (int branch = 0; branch < CHILD_BRANCH_COUNT; branch++) {
                for (int segment = 0; segment < CHILD_SEGMENTS_PER_BRANCH; segment++) {
                    bounds.include(childSample(payloadSeed, burst, branch, segment).position());
                }
            }
        }
        return bounds.toBounds();
    }

    /** Layer and octant counts are a static coverage proof for the seeded child placement. */
    public static boolean hasFullChildCoverage(long payloadSeed) {
        int[] layers = new int[CHILD_LAYER_COUNT];
        int[] octants = new int[8];
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            ChildBurst burst = childBurst(payloadSeed, index);
            layers[burst.layer()]++;
            Vec3 center = burst.center();
            int octant = (center.x >= 0.0D ? 1 : 0) | (center.y >= 0.0D ? 2 : 0) | (center.z >= 0.0D ? 4 : 0);
            octants[octant]++;
        }
        for (int count : layers) {
            if (count != CHILD_BURSTS_PER_LAYER) {
                return false;
            }
        }
        for (int count : octants) {
            if (count == 0) {
                return false;
            }
        }
        return true;
    }

    /** Coverage is structural: each of the four distance bands contains one child center in every octant. */
    public static boolean hasSeedIndependentChildCoverage() {
        return CHILD_LAYER_COUNT == 4
                && CHILD_BURSTS_PER_LAYER == 8
                && CHILD_MIN_CENTER_RADIUS > 0
                && CHILD_MAX_CENTER_RADIUS + CHILD_MAX_RADIUS <= MAX_RADIUS;
    }

    /** Each child is a genuine medium radial, never a single point or a short noise trail. */
    public static boolean hasMediumChildBursts(long payloadSeed) {
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            ChildBurst burst = childBurst(payloadSeed, index);
            if (burst.branchCount() < 12 || burst.segmentsPerBranch() < 8
                    || burst.maximumRadius() < CHILD_MIN_RADIUS || burst.particleCount() < 128) {
                return false;
            }
        }
        return true;
    }

    public static int retirementFlickerLeadTicks(BranchSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Cascade sample may not be null");
        }
        return 14 + Math.floorMod(sample.ownerIndex() * 31 + sample.branchIndex() * 17
                + sample.segmentIndex() * 13 + (sample.mainStage() ? 0 : 7), 13);
    }

    public static int retirementFlickerPhase(BranchSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Cascade sample may not be null");
        }
        return Math.floorMod(sample.ownerIndex() * 19 + sample.branchIndex() * 11
                + sample.segmentIndex() * 23 + (sample.mainStage() ? 0 : 5), 3);
    }

    public static boolean hasSingleDetonationPlan(long payloadSeed) {
        return ascentFitsDeclaredHeight()
                && MAIN_START_TICK == 0
                && STAGE_TWO_START_TICK >= MAIN_END_TICK_EXCLUSIVE
                && STAGE_TWO_START_TICK <= MAIN_END_TICK_EXCLUSIVE + MAX_PARTICLE_LIFETIME
                && particlesCreatedThroughTick(TOTAL_VISUAL_TICKS) == TOTAL_PARTICLES
                && particlesCreatedThisTick(TOTAL_VISUAL_TICKS) == 0
                && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS)
                && hasFullChildCoverage(payloadSeed)
                && hasSeedIndependentChildCoverage()
                && hasMediumChildBursts(payloadSeed);
    }

    /** Throws if this isolated prototype drifts from its one-flight, two-stage bounded contract. */
    public static void validateContract() {
        if (CHILD_BURST_COUNT % CHILD_LAYER_COUNT != 0
                || MAIN_PARTICLES != 6144 || CHILD_PARTICLES != 5120 || TOTAL_PARTICLES != 11264
                || MAX_PARTICLES_PER_EMISSION_TICK != MAIN_BRANCH_COUNT
                || CHILD_MIN_RADIUS < 14.0D || CHILD_BRANCH_COUNT < 12
                || CHILD_MAX_CENTER_RADIUS + CHILD_MAX_RADIUS > MAX_RADIUS
                || LAST_CHILD_EMISSION_TICK >= TOTAL_VISUAL_TICKS
                || !hasSingleDetonationPlan(0x1234ABCD5678EF90L)) {
            throw new IllegalStateException("Cascade giant contract is inconsistent");
        }
    }

    private static ColorBand mainColor(int segmentIndex) {
        return segmentIndex < 5 ? ColorBand.WARM_WHITE
                : (segmentIndex < 23 ? ColorBand.WARM_GOLD : ColorBand.PALE_GOLD);
    }

    private static int segmentLifetime(long seed, int segmentIndex, boolean main) {
        int minimum = main ? 84 : MIN_PARTICLE_LIFETIME;
        int maximum = main ? MAX_PARTICLE_LIFETIME : 72;
        return minimum + (int) Math.floor(randomUnit(seed ^ (LIFETIME_SALT + segmentIndex)) * (maximum - minimum + 1));
    }

    private static float brightness(long seed, int segmentIndex) {
        return (float) (1.02D + randomUnit(seed ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.16D);
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
    }

    private static double signedOctantComponent(int octant, int bit, double random) {
        double magnitude = 0.45D + random * 0.55D;
        return (octant & bit) == 0 ? -magnitude : magnitude;
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double smoothStep(double value) {
        double t = clamp(value, 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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
