package com.urbanforma.fireworks.content.release_next.giant_cascade;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic replacement contract for {@code giant_cascade_firework}.
 *
 * <p>The server need only communicate one detonation position and seed. This class allocates nothing and creates
 * no particles. The client derives one parent shell followed by forty-eight full, radial child shells. A child is
 * never represented as an A-to-B trail: all thirty-two directions and four shell depths are sampled on its one
 * trigger tick.</p>
 */
public final class GiantCascadeReplacementTrajectory {
    public static final String REPLACED_ID = "giant_cascade_firework";
    public static final String STABLE_PROGRAM_ID = "giant_cascade_radiant_children_v2";
    public static final double MAX_RADIUS = 130.0D;
    public static final int ASCENT_TICKS = 138;
    public static final int DETONATION_HEIGHT = 200;

    public static final int PARENT_BRANCHES = 144;
    public static final int PARENT_SEGMENTS = 24;
    public static final int PARENT_END_TICK = PARENT_SEGMENTS;
    public static final int CHILD_BURSTS = 48;
    public static final int CHILD_BRANCHES = 32;
    public static final int CHILD_DEPTHS = 4;
    public static final int CHILD_NODES_PER_BURST = CHILD_BRANCHES * CHILD_DEPTHS;
    public static final int CHILD_WAVE_START_TICK = 82;
    /**
     * The original eight-tick plan emitted six complete child shells per tick (768 particles), which exceeded the
     * shared client budget. The replacement retains every shell, uses a fixed spatial permutation to interleave
     * child centers, then emits five complete shells on ticks 82..89 and four on ticks 90..91.
     */
    public static final int CHILD_WAVE_TICKS = 10;
    public static final int FULL_CHILD_WAVE_TICKS = 8;
    public static final int MAX_CHILD_BURSTS_PER_TICK = 5;
    public static final int MIN_CHILD_BURSTS_PER_TICK = 4;
    public static final int PARENT_PARTICLES = PARENT_BRANCHES * PARENT_SEGMENTS;
    public static final int CHILD_PARTICLES = CHILD_BURSTS * CHILD_NODES_PER_BURST;
    public static final int TOTAL_PARTICLES = PARENT_PARTICLES + CHILD_PARTICLES;
    public static final int LEGACY_MAX_PARTICLES_PER_TICK = 768;
    public static final int MAX_PARTICLES_PER_TICK = MAX_CHILD_BURSTS_PER_TICK * CHILD_NODES_PER_BURST;
    public static final int MIN_LIFETIME = 44;
    public static final int MAX_LIFETIME = 96;
    public static final int LAST_EMISSION_TICK = CHILD_WAVE_START_TICK + CHILD_WAVE_TICKS - 1;
    public static final int TOTAL_VISUAL_TICKS = LAST_EMISSION_TICK + MAX_LIFETIME + 1;

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double[] CHILD_CENTER_RADII = {24.0D, 64.0D, 105.0D};
    private static final double[] CHILD_DEPTH_FRACTIONS = {0.58D, 0.76D, 0.91D, 1.0D};
    private static final long PARENT_SALT = 0x4D595DF4D0F33173L;
    private static final long CHILD_SALT = 0x7F4A7C159E3779B9L;

    private GiantCascadeReplacementTrajectory() {
    }

    public enum ColorBand {
        WARM_WHITE(1.0F, 0.95F, 0.78F, 1.48F),
        PALE_GOLD(1.0F, 0.88F, 0.45F, 1.44F),
        WARM_GOLD(1.0F, 0.72F, 0.18F, 1.38F),
        SCARLET(0.96F, 0.16F, 0.20F, 1.28F);

        private final float red;
        private final float green;
        private final float blue;
        private final float scale;

        ColorBand(float red, float green, float blue, float scale) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.scale = scale;
        }

        public float red() { return red; }
        public float green() { return green; }
        public float blue() { return blue; }
        public float scale() { return scale; }
    }

    public record Sample(Vec3 position, ColorBand color, int lifetime, boolean core) {
        public Sample {
            if (position == null || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + 1.0E-7D
                    || lifetime < MIN_LIFETIME || lifetime > MAX_LIFETIME) {
                throw new IllegalArgumentException("Invalid cascade replacement sample");
            }
        }
    }

    public static boolean parentEmitsAt(int tick) {
        return tick >= 0 && tick < PARENT_END_TICK;
    }

    public static int childTriggerTick(int burstIndex) {
        requireBurst(burstIndex);
        int spatiallyInterleavedSlot = Math.floorMod(burstIndex * 17, CHILD_BURSTS);
        int fullWaveCapacity = FULL_CHILD_WAVE_TICKS * MAX_CHILD_BURSTS_PER_TICK;
        int waveTick = spatiallyInterleavedSlot < fullWaveCapacity
                ? spatiallyInterleavedSlot / MAX_CHILD_BURSTS_PER_TICK
                : FULL_CHILD_WAVE_TICKS + (spatiallyInterleavedSlot - fullWaveCapacity) / MIN_CHILD_BURSTS_PER_TICK;
        return CHILD_WAVE_START_TICK + waveTick;
    }

    public static boolean childEmitsAt(int tick, int burstIndex) {
        return tick == childTriggerTick(burstIndex);
    }

    public static Sample parentSample(long payloadSeed, int branchIndex, int segmentIndex) {
        if (branchIndex < 0 || branchIndex >= PARENT_BRANCHES || segmentIndex < 0 || segmentIndex >= PARENT_SEGMENTS) {
            throw new IllegalArgumentException("Parent index outside cascade replacement contract");
        }
        long seed = mix64(payloadSeed ^ PARENT_SALT ^ (long) branchIndex * 0xD1342543DE82EF95L);
        Vec3 direction = fibonacciDirection(branchIndex, PARENT_BRANCHES, seed);
        double progress = (double) segmentIndex / (PARENT_SEGMENTS - 1);
        return new Sample(direction.scale(2.0D + 114.0D * smoothStep(progress)),
                segmentIndex < 4 ? ColorBand.WARM_WHITE : (segmentIndex < 16 ? ColorBand.PALE_GOLD : ColorBand.WARM_GOLD),
                72 + bounded(seed ^ segmentIndex, 25), segmentIndex < 4);
    }

    /** A full spherical child shell: branch direction varies across the sphere, depth varies only radially. */
    public static Sample childSample(long payloadSeed, int burstIndex, int branchIndex, int depthIndex) {
        requireBurst(burstIndex);
        if (branchIndex < 0 || branchIndex >= CHILD_BRANCHES || depthIndex < 0 || depthIndex >= CHILD_DEPTHS) {
            throw new IllegalArgumentException("Child index outside cascade replacement contract");
        }
        long burstSeed = mix64(payloadSeed ^ CHILD_SALT ^ (long) burstIndex * 0xD6E8FEB86659FD93L);
        Vec3 center = childCenter(burstSeed, burstIndex);
        Vec3 direction = fibonacciDirection(branchIndex, CHILD_BRANCHES, burstSeed ^ 0x94D049BB133111EBL);
        double maximumRadius = 15.0D + unit(burstSeed ^ 0xC6BC279692B5CC83L) * 3.0D;
        Vec3 position = center.add(direction.scale(maximumRadius * CHILD_DEPTH_FRACTIONS[depthIndex]));
        long branchSeed = mix64(burstSeed ^ (long) branchIndex * 0x9E3779B97F4A7C15L);
        ColorBand color = depthIndex == CHILD_DEPTHS - 1 && Math.floorMod((int) branchSeed, 13) == 0
                ? ColorBand.SCARLET : (depthIndex == 0 ? ColorBand.WARM_WHITE
                : (depthIndex == 1 ? ColorBand.PALE_GOLD : ColorBand.WARM_GOLD));
        return new Sample(position, color, 44 + bounded(branchSeed ^ depthIndex, 25),
                depthIndex == 0 && Math.floorMod(branchIndex, 4) == 0);
    }

    public static int particlesCreatedThisTick(int tick) {
        int count = parentEmitsAt(tick) ? PARENT_BRANCHES : 0;
        for (int burst = 0; burst < CHILD_BURSTS; burst++) {
            if (childEmitsAt(tick, burst)) count += CHILD_NODES_PER_BURST;
        }
        return count;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) return 0;
        int count = Math.min(PARENT_SEGMENTS, tick + 1) * PARENT_BRANCHES;
        for (int burst = 0; burst < CHILD_BURSTS; burst++) if (tick >= childTriggerTick(burst)) count += CHILD_NODES_PER_BURST;
        return Math.min(TOTAL_PARTICLES, count);
    }

    public static boolean hasNearSimultaneousRadialChildWave(long seed) {
        for (int tick = CHILD_WAVE_START_TICK; tick < CHILD_WAVE_START_TICK + CHILD_WAVE_TICKS; tick++) {
            int expectedBursts = tick < CHILD_WAVE_START_TICK + FULL_CHILD_WAVE_TICKS
                    ? MAX_CHILD_BURSTS_PER_TICK : MIN_CHILD_BURSTS_PER_TICK;
            if (particlesCreatedThisTick(tick) != expectedBursts * CHILD_NODES_PER_BURST) return false;
        }
        for (int burst = 0; burst < CHILD_BURSTS; burst++) {
            int trigger = childTriggerTick(burst);
            for (int branch = 0; branch < CHILD_BRANCHES; branch++) {
                Vec3 previous = null;
                for (int depth = 0; depth < CHILD_DEPTHS; depth++) {
                    Sample sample = childSample(seed, burst, branch, depth);
                    if (!childEmitsAt(trigger, burst) || previous != null && sample.position().subtract(previous).lengthSqr() < 1.0E-9D) return false;
                    previous = sample.position();
                }
            }
        }
        return true;
    }

    public static void validateContract() {
        if (PARENT_PARTICLES != 3456 || CHILD_PARTICLES != 6144 || TOTAL_PARTICLES != 9600
                || FULL_CHILD_WAVE_TICKS != 8 || CHILD_WAVE_TICKS != 10
                || MAX_CHILD_BURSTS_PER_TICK != 5 || MIN_CHILD_BURSTS_PER_TICK != 4
                || LEGACY_MAX_PARTICLES_PER_TICK != 768 || MAX_PARTICLES_PER_TICK != 640
                || MAX_PARTICLES_PER_TICK > 720 || TOTAL_VISUAL_TICKS != 188
                || maxPlannedParticlesPerTick() != MAX_PARTICLES_PER_TICK
                || particlesCreatedThroughTick(TOTAL_VISUAL_TICKS) != TOTAL_PARTICLES
                || !hasNearSimultaneousRadialChildWave(0x1234ABCD5678EF90L)) {
            throw new IllegalStateException("Cascade replacement contract drifted");
        }
    }

    /** Returns the maximum plan demand before any shared-budget reservation can truncate a shell. */
    public static int maxPlannedParticlesPerTick() {
        int maximum = 0;
        for (int tick = 0; tick <= LAST_EMISSION_TICK; tick++) maximum = Math.max(maximum, particlesCreatedThisTick(tick));
        return maximum;
    }

    private static Vec3 childCenter(long seed, int burstIndex) {
        int layer = burstIndex / 16;
        int inLayer = burstIndex % 16;
        int octant = inLayer % 8;
        int altitudeVariant = inLayer / 8;
        double x = 0.58D + unit(seed ^ 1L) * 0.42D;
        double y = (altitudeVariant == 0 ? 0.72D : 0.10D) + unit(seed ^ 2L) * (altitudeVariant == 0 ? 0.20D : 0.16D);
        double z = 0.58D + unit(seed ^ 3L) * 0.42D;
        Vec3 direction = new Vec3((octant & 1) == 0 ? -x : x, (octant & 2) == 0 ? -y : y,
                (octant & 4) == 0 ? -z : z).normalize();
        double radius = CHILD_CENTER_RADII[layer] + (unit(seed ^ 4L) - 0.5D) * (layer == 0 ? 6.0D : 8.0D);
        return direction.scale(radius);
    }

    private static Vec3 fibonacciDirection(int index, int count, long seed) {
        double fraction = ((double) index + 0.5D) / count;
        double y = 1.0D - 2.0D * fraction;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double azimuth = index * GOLDEN_ANGLE + unit(seed) * TWO_PI;
        return new Vec3(horizontal * Math.cos(azimuth), y, horizontal * Math.sin(azimuth));
    }

    private static void requireBurst(int burstIndex) {
        if (burstIndex < 0 || burstIndex >= CHILD_BURSTS) throw new IllegalArgumentException("Child burst outside contract");
    }

    private static int bounded(long seed, int exclusive) { return (int) Math.floor(unit(seed) * exclusive); }
    private static double unit(long value) { return (mix64(value) >>> 11) * 0x1.0p-53D; }
    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }
    private static double smoothStep(double value) { return value * value * (3.0D - 2.0D * value); }
}
