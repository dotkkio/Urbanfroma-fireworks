package com.urbanforma.fireworks.content.midsize;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, common-side geometry for the midsize dense sphere trial.
 *
 * <p>The geometry is deliberately caller-driven. It owns no registry, style, network, scheduler, or particle
 * object. One complete 96-node ring is sampled per tick, with a petal modulation and a four-ring bead core so this
 * is a compact dahlia rather than a uniformly scaled copy of the large sphere.</p>
 */
public final class MidsizeDenseSphereTrajectory {
    public static final String EFFECT_ID = "midsize_dense_sphere_firework";
    public static final String REFERENCE_EFFECT_ID = "grand_golden_sphere_firework";

    public static final int REFERENCE_TOTAL_PARTICLES = 2_160;
    public static final double REFERENCE_FULL_ENVELOPE = 105.0D;
    public static final int REFERENCE_ASCENT_TICKS = 86;
    public static final double SHARED_LAUNCH_SPEED = 1.45D;

    public static final int BRANCH_COUNT = 96;
    public static final int SEGMENTS_PER_BRANCH = 18;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    public static final int CORE_SEGMENT_COUNT = 4;
    public static final int BODY_END_SEGMENT = 13;

    public static final double MAX_RADIUS = REFERENCE_FULL_ENVELOPE * 0.25D;
    public static final double APPROVED_FULL_ENVELOPE = REFERENCE_FULL_ENVELOPE * 0.50D;
    public static final int ASCENT_TICKS = 73;
    public static final double REFERENCE_DETONATION_HEIGHT = REFERENCE_ASCENT_TICKS * SHARED_LAUNCH_SPEED;
    public static final double DETONATION_HEIGHT = ASCENT_TICKS * SHARED_LAUNCH_SPEED;
    public static final double PARTICLE_RATIO = (double) TOTAL_PARTICLES / REFERENCE_TOTAL_PARTICLES;
    public static final double ENVELOPE_RATIO = APPROVED_FULL_ENVELOPE / REFERENCE_FULL_ENVELOPE;
    public static final double HEIGHT_RATIO = DETONATION_HEIGHT / REFERENCE_DETONATION_HEIGHT;

    public static final int MIN_PARTICLE_LIFETIME = 34;
    public static final int MAX_PARTICLE_LIFETIME = 72;
    public static final int TOTAL_VISUAL_TICKS = EMISSION_TICKS + MAX_PARTICLE_LIFETIME;
    public static final int SHARED_MAX_PARTICLES_PER_TICK = 720;
    public static final int MAX_OWNED_PARTICLES = TOTAL_PARTICLES;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double PETAL_AMPLITUDE = 0.06D;
    private static final double PETAL_PHASE_AMPLITUDE = 0.12D;
    private static final double CORE_RADIUS_MIN = 2.4D;
    private static final double CORE_RADIUS_MAX = 6.5D;
    private static final double BODY_RADIUS_START = 7.0D;
    private static final double AZIMUTH_JITTER = 0.075D;
    private static final double ELEVATION_JITTER = 0.045D;
    private static final double RADIUS_EPSILON = 1.0E-9D;
    private static final long BRANCH_SALT = 0x68E31DA4C9B2F705L;
    private static final long PHASE_SALT = 0xD6E8FEB86659FD93L;
    private static final long AZIMUTH_SALT = 0xA4093822299F31D0L;
    private static final long ELEVATION_SALT = 0x13198A2E03707344L;
    private static final long PETAL_SALT = 0x243F6A8885A308D3L;
    private static final long LIFETIME_SALT = 0x452821E638D01377L;
    private static final long BRIGHTNESS_SALT = 0xBE5466CF34E90C6CL;
    private static final long TWINKLE_SALT = 0xC6BC279692B5CC83L;

    static {
        if (TOTAL_PARTICLES != 1_728 || PARTICLES_PER_EMISSION_TICK > SHARED_MAX_PARTICLES_PER_TICK
                || Math.abs(PARTICLE_RATIO - 0.80D) > 1.0E-12D
                || Math.abs(ENVELOPE_RATIO - 0.50D) > 1.0E-12D
                || HEIGHT_RATIO < 0.80D || HEIGHT_RATIO > 0.90D
                || Math.abs(MAX_RADIUS * 2.0D - APPROVED_FULL_ENVELOPE) > RADIUS_EPSILON) {
            throw new IllegalStateException("Midsize dense sphere contract drifted");
        }
    }

    private MidsizeDenseSphereTrajectory() {
    }

    public enum ColorBand {
        CORE(new Rgb(1.0F, 0.89F, 0.42F), 1.34F),
        BODY(new Rgb(1.0F, 0.63F, 0.09F), 1.22F),
        EDGE(new Rgb(1.0F, 0.94F, 0.70F), 1.30F);

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

    public record AscentSample(int tick, double progress, double height) {
        public AscentSample {
            if (tick < 0 || tick >= ASCENT_TICKS || !Double.isFinite(progress) || progress < 0.0D
                    || progress > 1.0D || !Double.isFinite(height) || height < 0.0D
                    || height > DETONATION_HEIGHT + RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid midsize sphere ascent sample");
            }
        }
    }

    public record Branch(int index, long seed, Vec3 direction, double azimuth, double phase) {
        public Branch {
            if (index < 0 || index >= BRANCH_COUNT || direction == null || !Double.isFinite(azimuth)
                    || !Double.isFinite(phase)) {
                throw new IllegalArgumentException("Invalid midsize sphere branch");
            }
        }
    }

    public record BranchSample(
            Branch branch,
            int segmentIndex,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime,
            boolean twinkles) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || position == null
                    || colorBand == null || !Float.isFinite(brightness) || brightness < 1.0F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid midsize sphere sample");
            }
        }

        public boolean core() {
            return this.colorBand == ColorBand.CORE;
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
                throw new IllegalArgumentException("Midsize sphere bounds must be finite and ordered");
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

        public boolean fitsEnvelope(double envelope) {
            return envelope > 0.0D && this.maxSpan() <= envelope + RADIUS_EPSILON;
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
                    || activeUpperBound > MAX_OWNED_PARTICLES) {
                throw new IllegalArgumentException("Invalid midsize sphere particle accounting");
            }
        }
    }

    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Midsize sphere ascent tick is outside the configured path");
        }
        double progress = (double) tick / (ASCENT_TICKS - 1);
        return new AscentSample(tick, progress, DETONATION_HEIGHT * progress);
    }

    public static boolean ascentFitsDeclaredHeight() {
        return ascentAtTick(0).height() == 0.0D
                && Math.abs(ascentAtTick(ASCENT_TICKS - 1).height() - DETONATION_HEIGHT) <= RADIUS_EPSILON;
    }

    public static Branch branch(long payloadSeed, int branchIndex) {
        validateBranchIndex(branchIndex);
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double latitude = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / BRANCH_COUNT;
        double phase = randomUnit(payloadSeed ^ PHASE_SALT) * TWO_PI;
        double azimuth = branchIndex * GOLDEN_ANGLE + phase
                + centered(branchSeed, AZIMUTH_SALT) * AZIMUTH_JITTER;
        double elevation = Math.asin(clamp(latitude, -1.0D, 1.0D))
                + centered(branchSeed, ELEVATION_SALT) * ELEVATION_JITTER;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(elevation), horizontal * Math.sin(azimuth)).normalize();
        return new Branch(branchIndex, branchSeed, direction, azimuth, randomUnit(branchSeed ^ PETAL_SALT) * TWO_PI);
    }

    public static BranchSample sample(long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(payloadSeed, branch(payloadSeed, branchIndex), segmentIndex);
    }

    public static BranchSample sample(long payloadSeed, Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Midsize sphere branch may not be null");
        }
        validateSegmentIndex(segmentIndex);
        double progress = (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
        double radius = radiusFor(branch, segmentIndex, progress);
        Vec3 side = horizontalSide(branch.direction());
        double weave = Math.sin(branch.phase() + TWO_PI * progress) * Math.sin(Math.PI * progress);
        Vec3 raw = branch.direction().scale(radius).add(side.scale(0.85D * weave));
        Vec3 position = raw.scale(Math.min(1.0D, MAX_RADIUS / Math.max(MAX_RADIUS, raw.length())));
        ColorBand colorBand = colorBand(segmentIndex);
        int lifetime = lifetime(branch, segmentIndex, colorBand);
        float brightness = (float) (1.02D + randomUnit(branch.seed() ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.16D);
        boolean twinkles = !isCoreSegment(segmentIndex)
                && randomUnit(branch.seed() ^ (TWINKLE_SALT + segmentIndex)) < twinkleChance(colorBand);
        return new BranchSample(branch, segmentIndex, progress, position, colorBand, brightness, lifetime, twinkles);
    }

    public static ColorBand colorBand(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        return segmentIndex < CORE_SEGMENT_COUNT
                ? ColorBand.CORE
                : segmentIndex < BODY_END_SEGMENT ? ColorBand.BODY : ColorBand.EDGE;
    }

    public static boolean isCoreSegment(int segmentIndex) {
        return colorBand(segmentIndex) == ColorBand.CORE;
    }

    public static double twinkleChance(ColorBand colorBand) {
        if (colorBand == null) {
            throw new IllegalArgumentException("Midsize sphere color band may not be null");
        }
        return switch (colorBand) {
            case CORE -> 0.0D;
            case BODY -> 0.32D;
            case EDGE -> 0.56D;
        };
    }

    public static int particlesCreatedThisTick(int tick) {
        return tick >= 0 && tick < EMISSION_TICKS ? PARTICLES_PER_EMISSION_TICK : 0;
    }

    public static int particlesCreatedThroughTick(int tick) {
        return tick < 0 ? 0 : Math.min(tick + 1, EMISSION_TICKS) * PARTICLES_PER_EMISSION_TICK;
    }

    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int firstLiveEmission = Math.max(0, tick - MAX_PARTICLE_LIFETIME);
        int lastLiveEmission = Math.min(tick, EMISSION_TICKS - 1);
        return firstLiveEmission > lastLiveEmission
                ? 0
                : (lastLiveEmission - firstLiveEmission + 1) * PARTICLES_PER_EMISSION_TICK;
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Midsize sphere particle-plan tick may not be negative");
        }
        int cumulative = particlesCreatedThroughTick(tick);
        return new ParticlePlan(
                tick,
                particlesCreatedThisTick(tick),
                cumulative,
                activeParticleUpperBoundAtTick(tick),
                TOTAL_PARTICLES - cumulative);
    }

    public static Bounds conservativeBounds() {
        return new Bounds(-MAX_RADIUS, -MAX_RADIUS, -MAX_RADIUS, MAX_RADIUS, MAX_RADIUS, MAX_RADIUS, MAX_RADIUS);
    }

    public static boolean fitsEnvelope() {
        return conservativeBounds().fitsEnvelope(APPROVED_FULL_ENVELOPE);
    }

    private static double radiusFor(Branch branch, int segmentIndex, double progress) {
        if (segmentIndex < CORE_SEGMENT_COUNT) {
            return CORE_RADIUS_MIN + (CORE_RADIUS_MAX - CORE_RADIUS_MIN) * smoothStep(progress);
        }
        double shellProgress = (progress - (double) CORE_SEGMENT_COUNT / (SEGMENTS_PER_BRANCH - 1))
                / (1.0D - (double) CORE_SEGMENT_COUNT / (SEGMENTS_PER_BRANCH - 1));
        double petal = Math.sin(4.0D * branch.azimuth() + branch.phase() + PETAL_PHASE_AMPLITUDE * segmentIndex);
        double modulation = 1.0D + PETAL_AMPLITUDE * petal * Math.sin(Math.PI * shellProgress);
        double shell = BODY_RADIUS_START + (MAX_RADIUS - BODY_RADIUS_START) * smoothStep(shellProgress);
        return Math.min(MAX_RADIUS, Math.max(BODY_RADIUS_START, shell * modulation));
    }

    private static int lifetime(Branch branch, int segmentIndex, ColorBand colorBand) {
        int min = colorBand == ColorBand.CORE ? 34 : colorBand == ColorBand.BODY ? 54 : 44;
        int max = colorBand == ColorBand.CORE ? 42 : colorBand == ColorBand.BODY ? 72 : 60;
        return randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, min, max);
    }

    private static Vec3 horizontalSide(Vec3 direction) {
        double horizontalLength = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        return horizontalLength < RADIUS_EPSILON
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(-direction.z / horizontalLength, 0.0D, direction.x / horizontalLength);
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Midsize sphere branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Midsize sphere segment index is outside the configured count");
        }
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static int randomInt(long seed, long salt, int min, int max) {
        return min + (int) Math.floor(randomUnit(seed ^ salt) * (max - min + 1));
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
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
