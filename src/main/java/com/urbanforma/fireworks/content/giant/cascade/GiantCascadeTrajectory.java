package com.urbanforma.fireworks.content.giant.cascade;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side plan for the seventh giant firework.
 *
 * <p>There is one 0-to-200 launch and one detonation request. The parent shell is followed, near its
 * retirement, by a compact client-side wave of full child shells. A child shell's radial samples share one
 * trigger tick; they are spatial shell-depth samples, not a line emitted from A to B over multiple ticks.</p>
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

    public static final int MAIN_BRANCH_COUNT = 144;
    public static final int MAIN_SEGMENTS_PER_BRANCH = 24;
    public static final double MAIN_MAX_RADIUS = 116.0D;
    public static final int MAIN_START_TICK = 0;
    public static final int MAIN_END_TICK_EXCLUSIVE = MAIN_START_TICK + MAIN_SEGMENTS_PER_BRANCH;
    public static final int MAIN_MIN_PARTICLE_LIFETIME = 72;
    public static final int MAIN_MAX_PARTICLE_LIFETIME = 96;

    /** Forty-eight medium shells: two deterministic centers in every octant of each of three radial bands. */
    public static final int CHILD_BURST_COUNT = 48;
    public static final int CHILD_LAYER_COUNT = 3;
    public static final int CHILD_BURSTS_PER_LAYER = CHILD_BURST_COUNT / CHILD_LAYER_COUNT;
    public static final int CHILD_BURSTS_PER_OCTANT_PER_LAYER = 2;
    public static final int CHILD_BRANCH_COUNT = 32;
    /** Four simultaneous depth samples make a thick, complete shell rather than a moving branch trail. */
    public static final int CHILD_SEGMENTS_PER_BRANCH = 4;
    public static final int CHILD_PARTICLES_PER_BURST = CHILD_BRANCH_COUNT * CHILD_SEGMENTS_PER_BRANCH;
    public static final int CHILD_BURSTS_PER_TRIGGER_TICK = 6;
    public static final int CHILD_TRIGGER_WINDOW_TICKS = 8;
    /** The parent shell is in its seeded retirement region when this near-simultaneous child wave begins. */
    public static final int STAGE_TWO_START_TICK = 82;
    public static final int CHILD_START_STAGGER_TICKS = 1;
    public static final double CHILD_MIN_RADIUS = 15.0D;
    public static final double CHILD_MAX_RADIUS = 18.0D;
    public static final int CHILD_MIN_CENTER_RADIUS = 20;
    public static final int CHILD_MAX_CENTER_RADIUS = 109;
    public static final int CHILD_MIN_PARTICLE_LIFETIME = 44;
    public static final int CHILD_MAX_PARTICLE_LIFETIME = 68;

    public static final int MAIN_PARTICLES = MAIN_BRANCH_COUNT * MAIN_SEGMENTS_PER_BRANCH;
    public static final int CHILD_PARTICLES = CHILD_BURST_COUNT * CHILD_PARTICLES_PER_BURST;
    public static final int TOTAL_PARTICLES = MAIN_PARTICLES + CHILD_PARTICLES;
    /** Per-program local peak: six complete 128-node child shells on the same client tick. */
    public static final int MAX_PARTICLES_PER_EMISSION_TICK =
            CHILD_BURSTS_PER_TRIGGER_TICK * CHILD_PARTICLES_PER_BURST;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    public static final int MIN_PARTICLE_LIFETIME = CHILD_MIN_PARTICLE_LIFETIME;
    public static final int MAX_PARTICLE_LIFETIME = MAIN_MAX_PARTICLE_LIFETIME;
    public static final int LAST_MAIN_EMISSION_TICK = MAIN_END_TICK_EXCLUSIVE - 1;
    public static final int LAST_CHILD_EMISSION_TICK = STAGE_TWO_START_TICK + CHILD_TRIGGER_WINDOW_TICKS - 1;
    public static final int TOTAL_VISUAL_TICKS = Math.max(
            LAST_MAIN_EMISSION_TICK + MAIN_MAX_PARTICLE_LIFETIME,
            LAST_CHILD_EMISSION_TICK + CHILD_MAX_PARTICLE_LIFETIME) + 1;

    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double[] CHILD_CENTER_RADII = {24.0D, 64.0D, 105.0D};
    private static final double[] CHILD_SHELL_DEPTHS = {0.58D, 0.76D, 0.91D, 1.0D};
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

    public enum ColorBand {
        WARM_WHITE(new Rgb(1.0F, 0.95F, 0.78F), 1.48F),
        PALE_GOLD(new Rgb(1.0F, 0.88F, 0.45F), 1.44F),
        WARM_GOLD(new Rgb(1.0F, 0.72F, 0.18F), 1.38F),
        SCARLET_ACCENT(new Rgb(0.96F, 0.16F, 0.20F), 1.28F);

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

    /** One child center and its single full-shell trigger. */
    public record ChildBurst(
            int index,
            int layer,
            int octant,
            int altitudeVariant,
            long seed,
            Vec3 center,
            double centerRadius,
            double maximumRadius,
            int startTick) {
        public ChildBurst {
            if (index < 0 || index >= CHILD_BURST_COUNT || layer < 0 || layer >= CHILD_LAYER_COUNT
                    || octant < 0 || octant >= 8 || altitudeVariant < 0 || altitudeVariant > 1
                    || center == null || !finite(center) || !Double.isFinite(centerRadius)
                    || centerRadius < CHILD_MIN_CENTER_RADIUS || centerRadius > CHILD_MAX_CENTER_RADIUS
                    || maximumRadius < CHILD_MIN_RADIUS || maximumRadius > CHILD_MAX_RADIUS
                    || centerRadius + maximumRadius > MAX_RADIUS + RADIUS_EPSILON
                    || startTick != childStartTick(index)) {
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
            return this.startTick;
        }

        public int particleCount() {
            return CHILD_PARTICLES_PER_BURST;
        }
    }

    public record BranchSample(
            boolean mainStage,
            int ownerIndex,
            int branchIndex,
            int segmentIndex,
            int emissionTick,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime,
            boolean coreHighlight) {
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
                throw new IllegalArgumentException("Invalid cascade particle plan");
            }
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
                throw new IllegalArgumentException("Cascade bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** The public flight contract is one straight 0-to-200 path. It does not create visual particles. */
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
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(latitude), horizontal * Math.sin(azimuth)).normalize();
        return new MainBranch(branchIndex, seed, direction);
    }

    /**
     * Places two children in every octant of each radial band. The trigger permutation gives every trigger tick
     * six distinct octants across all three bands, so the near-simultaneous wave cannot sweep from one side to
     * the other as a line.
     */
    public static ChildBurst childBurst(long payloadSeed, int burstIndex) {
        if (burstIndex < 0 || burstIndex >= CHILD_BURST_COUNT) {
            throw new IllegalArgumentException("Cascade child index is outside the contract");
        }
        int layer = burstIndex / CHILD_BURSTS_PER_LAYER;
        int indexInLayer = burstIndex % CHILD_BURSTS_PER_LAYER;
        int octant = indexInLayer % 8;
        int altitudeVariant = indexInLayer / 8;
        long seed = mix64(payloadSeed ^ CHILD_SALT ^ (long) burstIndex * 0xD6E8FEB86659FD93L);
        Vec3 direction = childCenterDirection(seed, octant, altitudeVariant);
        double centerJitter = layer == 0 ? 3.0D : 4.0D;
        double centerRadius = clamp(
                CHILD_CENTER_RADII[layer] + centered(seed, RADIUS_SALT) * centerJitter * 2.0D,
                CHILD_MIN_CENTER_RADIUS,
                CHILD_MAX_CENTER_RADIUS);
        double maximumRadius = CHILD_MIN_RADIUS
                + randomUnit(seed ^ RADIUS_SALT) * (CHILD_MAX_RADIUS - CHILD_MIN_RADIUS);
        return new ChildBurst(
                burstIndex,
                layer,
                octant,
                altitudeVariant,
                seed,
                direction.scale(centerRadius),
                centerRadius,
                maximumRadius,
                childStartTick(burstIndex));
    }

    public static BranchSample mainSample(long payloadSeed, int branchIndex, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= MAIN_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Cascade main segment is outside the contract");
        }
        MainBranch branch = mainBranch(payloadSeed, branchIndex);
        double progress = (double) segmentIndex / (MAIN_SEGMENTS_PER_BRANCH - 1);
        double radius = 2.0D + (MAIN_MAX_RADIUS - 2.0D) * smoothStep(progress);
        Vec3 position = branch.direction().scale(radius);
        return new BranchSample(
                true,
                0,
                branchIndex,
                segmentIndex,
                MAIN_START_TICK + segmentIndex,
                progress,
                position,
                mainColor(segmentIndex),
                brightness(branch.seed(), segmentIndex),
                segmentLifetime(branch.seed(), segmentIndex, true),
                segmentIndex < 4);
    }

    /** All depth samples of one child branch share {@link ChildBurst#startTick()}. */
    public static BranchSample childSample(long payloadSeed, int burstIndex, int branchIndex, int segmentIndex) {
        if (branchIndex < 0 || branchIndex >= CHILD_BRANCH_COUNT
                || segmentIndex < 0 || segmentIndex >= CHILD_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Cascade child branch sample is outside the contract");
        }
        ChildBurst burst = childBurst(payloadSeed, burstIndex);
        long branchSeed = mix64(burst.seed() ^ (long) branchIndex * 0x9E3779B97F4A7C15L);
        Vec3 direction = childShellDirection(burst, branchIndex, branchSeed);
        double shellDepth = CHILD_SHELL_DEPTHS[segmentIndex];
        Vec3 position = burst.center().add(direction.scale(burst.maximumRadius() * shellDepth));
        boolean scarlet = segmentIndex == CHILD_SEGMENTS_PER_BRANCH - 1
                && Math.floorMod((int) (branchSeed ^ ACCENT_SALT), 13) == 0;
        ColorBand color = scarlet ? ColorBand.SCARLET_ACCENT : switch (segmentIndex) {
            case 0 -> ColorBand.WARM_WHITE;
            case 1 -> ColorBand.PALE_GOLD;
            default -> ColorBand.WARM_GOLD;
        };
        return new BranchSample(
                false,
                burstIndex,
                branchIndex,
                segmentIndex,
                burst.startTick(),
                shellDepth,
                position,
                color,
                brightness(branchSeed, segmentIndex),
                segmentLifetime(branchSeed, segmentIndex, false),
                segmentIndex == 0 && Math.floorMod(branchIndex, 4) == 0);
    }

    public static boolean isMainEmitting(int tick) {
        return tick >= MAIN_START_TICK && tick < MAIN_END_TICK_EXCLUSIVE;
    }

    public static boolean isChildEmitting(int tick, int burstIndex) {
        return burstIndex >= 0 && burstIndex < CHILD_BURST_COUNT && tick == childStartTick(burstIndex);
    }

    public static int childStartTick(int burstIndex) {
        if (burstIndex < 0 || burstIndex >= CHILD_BURST_COUNT) {
            throw new IllegalArgumentException("Cascade child index is outside the trigger window");
        }
        int layer = burstIndex / CHILD_BURSTS_PER_LAYER;
        int indexInLayer = burstIndex % CHILD_BURSTS_PER_LAYER;
        int octant = indexInLayer % 8;
        int altitudeVariant = indexInLayer / 8;
        int triggerSlot = Math.floorMod(octant + layer + altitudeVariant * 4, CHILD_TRIGGER_WINDOW_TICKS);
        return STAGE_TWO_START_TICK + triggerSlot * CHILD_START_STAGGER_TICKS;
    }

    public static int childBurstsTriggeredAtTick(int tick) {
        int count = 0;
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            if (isChildEmitting(tick, index)) {
                count++;
            }
        }
        return count;
    }

    public static int particlesCreatedThisTick(int tick) {
        int count = isMainEmitting(tick) ? MAIN_BRANCH_COUNT : 0;
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            if (isChildEmitting(tick, index)) {
                count += CHILD_PARTICLES_PER_BURST;
            }
        }
        return count;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int emittedMainSegments = Math.max(0, Math.min(MAIN_SEGMENTS_PER_BRANCH, tick + 1));
        int count = emittedMainSegments * MAIN_BRANCH_COUNT;
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            if (tick >= childStartTick(index)) {
                count += CHILD_PARTICLES_PER_BURST;
            }
        }
        return Math.min(TOTAL_PARTICLES, count);
    }

    /** Conservative local count: every created particle is allowed to live for its stage's maximum lifetime. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int count = 0;
        int mainFirst = Math.max(MAIN_START_TICK, tick - MAIN_MAX_PARTICLE_LIFETIME + 1);
        int mainLast = Math.min(tick, LAST_MAIN_EMISSION_TICK);
        if (mainFirst <= mainLast) {
            count += (mainLast - mainFirst + 1) * MAIN_BRANCH_COUNT;
        }
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            int childTick = childStartTick(index);
            if (tick >= childTick && tick < childTick + CHILD_MAX_PARTICLE_LIFETIME) {
                count += CHILD_PARTICLES_PER_BURST;
            }
        }
        return Math.min(PROTOTYPE_MAX_ALIVE_PARTICLES, count);
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Cascade particle-plan tick may not be negative");
        }
        int cumulative = particlesCreatedThroughTick(tick);
        return new ParticlePlan(
                tick,
                particlesCreatedThisTick(tick),
                cumulative,
                activeParticleUpperBoundAtTick(tick),
                TOTAL_PARTICLES - cumulative);
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

    /** Every radial band has two centers in every octant, with upper, lower, and inner-height coverage. */
    public static boolean hasFullChildCoverage(long payloadSeed) {
        int[][] layerOctants = new int[CHILD_LAYER_COUNT][8];
        int upper = 0;
        int lower = 0;
        int central = 0;
        for (int index = 0; index < CHILD_BURST_COUNT; index++) {
            ChildBurst burst = childBurst(payloadSeed, index);
            layerOctants[burst.layer()][burst.octant()]++;
            if (burst.center().y >= 30.0D) {
                upper++;
            } else if (burst.center().y <= -30.0D) {
                lower++;
            }
            if (Math.abs(burst.center().y) <= 20.0D) {
                central++;
            }
        }
        for (int layer = 0; layer < CHILD_LAYER_COUNT; layer++) {
            for (int octant = 0; octant < 8; octant++) {
                if (layerOctants[layer][octant] != CHILD_BURSTS_PER_OCTANT_PER_LAYER) {
                    return false;
                }
            }
        }
        return upper >= 4 && lower >= 4 && central >= 8;
    }

    /** The trigger permutation has six child shells per tick and no left-to-right placement sweep. */
    public static boolean hasNearSimultaneousChildWave() {
        if (CHILD_TRIGGER_WINDOW_TICKS != 8 || CHILD_BURSTS_PER_TRIGGER_TICK != 6) {
            return false;
        }
        for (int slot = 0; slot < CHILD_TRIGGER_WINDOW_TICKS; slot++) {
            int tick = STAGE_TWO_START_TICK + slot * CHILD_START_STAGGER_TICKS;
            if (childBurstsTriggeredAtTick(tick) != CHILD_BURSTS_PER_TRIGGER_TICK) {
                return false;
            }
            boolean[] seenOctants = new boolean[8];
            for (int burstIndex = 0; burstIndex < CHILD_BURST_COUNT; burstIndex++) {
                if (!isChildEmitting(tick, burstIndex)) {
                    continue;
                }
                int octant = childBurst(0L, burstIndex).octant();
                if (seenOctants[octant]) {
                    return false;
                }
                seenOctants[octant] = true;
            }
        }
        return childBurstsTriggeredAtTick(STAGE_TWO_START_TICK - 1) == 0
                && childBurstsTriggeredAtTick(LAST_CHILD_EMISSION_TICK + 1) == 0;
    }

    /** Every child has a 32-direction, four-depth full shell, and all four depths allocate on one tick. */
    public static boolean hasFullChildShells(long payloadSeed) {
        if (CHILD_BRANCH_COUNT < 32 || CHILD_SEGMENTS_PER_BRANCH < 4 || CHILD_PARTICLES_PER_BURST < 128) {
            return false;
        }
        for (int burstIndex = 0; burstIndex < CHILD_BURST_COUNT; burstIndex++) {
            ChildBurst burst = childBurst(payloadSeed, burstIndex);
            if (burst.particleCount() != CHILD_PARTICLES_PER_BURST) {
                return false;
            }
            for (int branchIndex = 0; branchIndex < CHILD_BRANCH_COUNT; branchIndex++) {
                for (int segmentIndex = 0; segmentIndex < CHILD_SEGMENTS_PER_BRANCH; segmentIndex++) {
                    if (childSample(payloadSeed, burstIndex, branchIndex, segmentIndex).emissionTick()
                            != burst.startTick()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Seed checks cover centers and all child-shell nodes without using wall-clock or mutable random state. */
    public static boolean hasDeterministicChildPlan(long payloadSeed) {
        for (int burstIndex = 0; burstIndex < CHILD_BURST_COUNT; burstIndex++) {
            ChildBurst first = childBurst(payloadSeed, burstIndex);
            ChildBurst second = childBurst(payloadSeed, burstIndex);
            if (!sameChildBurst(first, second)) {
                return false;
            }
            for (int branchIndex = 0; branchIndex < CHILD_BRANCH_COUNT; branchIndex++) {
                for (int segmentIndex = 0; segmentIndex < CHILD_SEGMENTS_PER_BRANCH; segmentIndex++) {
                    BranchSample firstSample = childSample(payloadSeed, burstIndex, branchIndex, segmentIndex);
                    BranchSample secondSample = childSample(payloadSeed, burstIndex, branchIndex, segmentIndex);
                    if (!sameBranchSample(firstSample, secondSample)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static int retirementFlickerLeadTicks(BranchSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Cascade sample may not be null");
        }
        return 14 + Math.floorMod(
                sample.ownerIndex() * 31 + sample.branchIndex() * 17
                        + sample.segmentIndex() * 13 + (sample.mainStage() ? 0 : 7),
                13);
    }

    public static int retirementFlickerPhase(BranchSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Cascade sample may not be null");
        }
        return Math.floorMod(
                sample.ownerIndex() * 19 + sample.branchIndex() * 11
                        + sample.segmentIndex() * 23 + (sample.mainStage() ? 0 : 5),
                3);
    }

    public static boolean hasSingleDetonationPlan(long payloadSeed) {
        return ascentFitsDeclaredHeight()
                && MAIN_START_TICK == 0
                && STAGE_TWO_START_TICK > MAIN_END_TICK_EXCLUSIVE
                && STAGE_TWO_START_TICK < LAST_MAIN_EMISSION_TICK + MAIN_MAX_PARTICLE_LIFETIME
                && particlesCreatedThroughTick(TOTAL_VISUAL_TICKS) == TOTAL_PARTICLES
                && particlesCreatedThisTick(TOTAL_VISUAL_TICKS) == 0
                && MAX_PARTICLES_PER_EMISSION_TICK == 768
                && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS)
                && hasFullChildCoverage(payloadSeed)
                && hasNearSimultaneousChildWave()
                && hasFullChildShells(payloadSeed)
                && hasDeterministicChildPlan(payloadSeed);
    }

    /**
     * Offline/static contract hook. It is deliberately not invoked from a common-side static initializer, so a
     * server catalog lookup cannot turn child geometry validation into server visual work.
     */
    public static void validateContract() {
        if (MAIN_PARTICLES != 3456 || CHILD_PARTICLES != 6144 || TOTAL_PARTICLES != 9600
                || CHILD_BURST_COUNT != 48 || CHILD_LAYER_COUNT != 3 || CHILD_BURSTS_PER_LAYER != 16
                || CHILD_BURSTS_PER_OCTANT_PER_LAYER != 2 || CHILD_BRANCH_COUNT != 32
                || CHILD_SEGMENTS_PER_BRANCH != 4 || CHILD_PARTICLES_PER_BURST != 128
                || CHILD_BURSTS_PER_TRIGGER_TICK != 6 || CHILD_TRIGGER_WINDOW_TICKS != 8
                || MAX_PARTICLES_PER_EMISSION_TICK != 768
                || CHILD_MAX_CENTER_RADIUS + CHILD_MAX_RADIUS > MAX_RADIUS
                || LAST_CHILD_EMISSION_TICK >= TOTAL_VISUAL_TICKS
                || !hasSingleDetonationPlan(0x1234ABCD5678EF90L)) {
            throw new IllegalStateException("Cascade giant contract is inconsistent");
        }
    }

    private static ColorBand mainColor(int segmentIndex) {
        return segmentIndex < 4 ? ColorBand.WARM_WHITE
                : (segmentIndex < 16 ? ColorBand.PALE_GOLD : ColorBand.WARM_GOLD);
    }

    private static Vec3 childCenterDirection(long seed, int octant, int altitudeVariant) {
        boolean highAltitude = altitudeVariant == 0;
        double verticalMagnitude = highAltitude
                ? 0.72D + randomUnit(seed ^ (CENTER_SALT + 1L)) * 0.20D
                : 0.10D + randomUnit(seed ^ (CENTER_SALT + 1L)) * 0.16D;
        double horizontalMinimum = highAltitude ? 0.25D : 0.58D;
        double xMagnitude = horizontalMinimum
                + randomUnit(seed ^ (DIRECTION_SALT + 3L)) * (1.0D - horizontalMinimum);
        double zMagnitude = horizontalMinimum
                + randomUnit(seed ^ (DIRECTION_SALT + 4L)) * (1.0D - horizontalMinimum);
        return new Vec3(
                signedMagnitude(octant, 1, xMagnitude),
                signedMagnitude(octant, 2, verticalMagnitude),
                signedMagnitude(octant, 4, zMagnitude)).normalize();
    }

    private static Vec3 childShellDirection(ChildBurst burst, int branchIndex, long branchSeed) {
        double fraction = ((double) branchIndex + 0.5D) / CHILD_BRANCH_COUNT;
        double latitude = Math.asin(clamp(1.0D - 2.0D * fraction, -1.0D, 1.0D))
                + centered(branchSeed, DIRECTION_SALT) * 0.025D;
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(burst.seed() ^ CENTER_SALT) * TWO_PI
                + centered(branchSeed, RADIUS_SALT) * 0.045D;
        double horizontal = Math.cos(latitude);
        return new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(latitude), horizontal * Math.sin(azimuth)).normalize();
    }

    private static int segmentLifetime(long seed, int segmentIndex, boolean mainStage) {
        int minimum = mainStage ? MAIN_MIN_PARTICLE_LIFETIME : CHILD_MIN_PARTICLE_LIFETIME;
        int maximum = mainStage ? MAIN_MAX_PARTICLE_LIFETIME : CHILD_MAX_PARTICLE_LIFETIME;
        return minimum + (int) Math.floor(
                randomUnit(seed ^ (LIFETIME_SALT + segmentIndex)) * (maximum - minimum + 1));
    }

    private static float brightness(long seed, int segmentIndex) {
        return (float) (1.02D + randomUnit(seed ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.16D);
    }

    private static boolean sameChildBurst(ChildBurst first, ChildBurst second) {
        return first.index() == second.index() && first.layer() == second.layer() && first.octant() == second.octant()
                && first.altitudeVariant() == second.altitudeVariant() && first.seed() == second.seed()
                && sameVec3(first.center(), second.center())
                && Double.doubleToLongBits(first.centerRadius()) == Double.doubleToLongBits(second.centerRadius())
                && Double.doubleToLongBits(first.maximumRadius()) == Double.doubleToLongBits(second.maximumRadius())
                && first.startTick() == second.startTick();
    }

    private static boolean sameBranchSample(BranchSample first, BranchSample second) {
        return first.mainStage() == second.mainStage() && first.ownerIndex() == second.ownerIndex()
                && first.branchIndex() == second.branchIndex() && first.segmentIndex() == second.segmentIndex()
                && first.emissionTick() == second.emissionTick()
                && Double.doubleToLongBits(first.progress()) == Double.doubleToLongBits(second.progress())
                && sameVec3(first.position(), second.position()) && first.colorBand() == second.colorBand()
                && Float.floatToIntBits(first.brightness()) == Float.floatToIntBits(second.brightness())
                && first.lifetime() == second.lifetime() && first.coreHighlight() == second.coreHighlight();
    }

    private static boolean sameVec3(Vec3 first, Vec3 second) {
        return first != null && second != null
                && Double.doubleToLongBits(first.x) == Double.doubleToLongBits(second.x)
                && Double.doubleToLongBits(first.y) == Double.doubleToLongBits(second.y)
                && Double.doubleToLongBits(first.z) == Double.doubleToLongBits(second.z);
    }

    private static double signedMagnitude(int octant, int bit, double magnitude) {
        return (octant & bit) == 0 ? -magnitude : magnitude;
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
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
