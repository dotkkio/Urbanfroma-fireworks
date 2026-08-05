package com.urbanforma.fireworks.content.giant;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic geometry and proof data for the first giant radiant prototype.
 *
 * <p>This class deliberately contains no registry, style, network, or shared giant-contract types. A coordinator
 * can adapt its output to the shared {@code GiantTier}, {@code EffectCategory}, and budget contracts once those
 * contracts exist. The prototype itself is fixed at a 130-block radius and a 0-to-200-block ascent.</p>
 */
public final class GiantRadiantTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    /** Linear ascent reaches exactly 200 blocks on the last ascent sample. */
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    /** Server-side one-shot detonation broadcast; volume*16 is the vanilla audible radius. */
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.32F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    private static final double INITIAL_RADIUS = 4.0D;
    private static final double TERMINAL_DROOP_BIAS = 0.24D;

    /** One complete ring is emitted per client tick. */
    public static final int BRANCH_COUNT = 256;
    public static final int SEGMENTS_PER_BRANCH = 48;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    /** The retained tail is longer, but all 12,288 particles are still the same bounded population. */
    public static final int MIN_PARTICLE_LIFETIME = 120;
    public static final int MAX_PARTICLE_LIFETIME = 144;
    public static final int TOTAL_VISUAL_TICKS = EMISSION_TICKS + MAX_PARTICLE_LIFETIME;

    /** Final retirement flicker is confined to the last seeded window of each existing spark. */
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 18;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 24;

    /** The plan's own upper bound; it is not the ordinary-effect or shared giant scheduler budget. */
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double AZIMUTH_JITTER = 0.055D;
    private static final double ELEVATION_JITTER = 0.035D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final long BRANCH_SALT = 0x68E31DA4C9B2F705L;
    private static final long AZIMUTH_SALT = 0xA4093822299F31D0L;
    private static final long ELEVATION_SALT = 0x13198A2E03707344L;
    private static final long LIFETIME_SALT = 0x452821E638D01377L;
    private static final long BRIGHTNESS_SALT = 0xBE5466CF34E90C6CL;

    private GiantRadiantTrajectory() {
    }

    /** Three fixed warm-spectrum layers keep the prototype vivid without introducing a new resource. */
    public enum ColorBand {
        PRIMARY(new Rgb(1.0F, 0.86F, 0.52F), 1.40F),
        SECONDARY(new Rgb(1.0F, 0.62F, 0.055F), 1.32F),
        ACCENT(new Rgb(1.0F, 0.94F, 0.64F), 1.48F);

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
                throw new IllegalArgumentException("Invalid giant ascent sample");
            }
        }
    }

    public record Branch(int index, long seed, Vec3 direction) {
        public Branch {
            if (index < 0 || index >= BRANCH_COUNT || direction == null) {
                throw new IllegalArgumentException("Invalid giant radiant branch");
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
            float brightness,
            int lifetime) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(radius) || radius < 0.0D || radius > MAX_RADIUS + RADIUS_EPSILON
                    || position == null || colorBand == null || !Float.isFinite(brightness)
                    || brightness < 1.0F || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME) {
                throw new IllegalArgumentException("Invalid giant radiant branch sample");
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
                throw new IllegalArgumentException("Giant bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Per-tick accounting for this prototype only; it never consumes the ordinary scheduler's budget. */
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
                throw new IllegalArgumentException("Invalid giant particle accounting");
            }
        }
    }

    /** Returns the exact launch-to-detonation sample, including the endpoints 0 and 200. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Giant ascent tick is outside the 0-to-200 path");
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
        double sphereY = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / BRANCH_COUNT;
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ 0xD6E8FEB86659FD93L) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * AZIMUTH_JITTER;
        double elevation = Math.asin(clamp(sphereY, -1.0D, 1.0D))
                + centered(branchSeed, ELEVATION_SALT) * ELEVATION_JITTER;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth),
                Math.sin(elevation),
                horizontal * Math.sin(azimuth)).normalize();
        return new Branch(branchIndex, branchSeed, direction);
    }

    public static BranchSample sample(long payloadSeed, int branchIndex, int segmentIndex) {
        return sample(payloadSeed, branch(payloadSeed, branchIndex), segmentIndex);
    }

    public static BranchSample sample(long payloadSeed, Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant radiant branch may not be null");
        }
        validateSegmentIndex(segmentIndex);
        double progress = (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
        double radius = INITIAL_RADIUS + (MAX_RADIUS - INITIAL_RADIUS) * smoothStep(progress);
        Vec3 curvedDirection = droopedDirection(branch.direction(), progress);
        Vec3 position = curvedDirection.scale(radius);
        ColorBand colorBand = colorBand(segmentIndex);
        int lifetime = randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, MIN_PARTICLE_LIFETIME,
                MAX_PARTICLE_LIFETIME);
        float brightness = (float) (1.04D + randomUnit(branch.seed() ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.14D);
        return new BranchSample(branch, segmentIndex, progress, radius, position, colorBand, brightness, lifetime);
    }

    public static ColorBand colorBand(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        if (segmentIndex < 8) {
            return ColorBand.PRIMARY;
        }
        return segmentIndex < 36 ? ColorBand.SECONDARY : ColorBand.ACCENT;
    }

    public static boolean isCoreSegment(int segmentIndex) {
        validateSegmentIndex(segmentIndex);
        return segmentIndex < 8;
    }

    /** Returns a deterministic 18-to-24 tick retirement lead so particles do not flicker in lockstep. */
    public static int retirementFlickerLeadTicks(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant radiant branch may not be null");
        }
        validateSegmentIndex(segmentIndex);
        int spread = RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1;
        return RETIREMENT_FLICKER_MIN_LEAD_TICKS
                + Math.floorMod(branch.index() * 31 + segmentIndex * 17, spread);
    }

    /** Deterministic SparkParticle cadence phase; adjacent branches are intentionally not synchronized. */
    public static int retirementFlickerPhase(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Giant radiant branch may not be null");
        }
        validateSegmentIndex(segmentIndex);
        return Math.floorMod(branch.index() * 17 + segmentIndex * 29, 2);
    }

    public static int finalParticleDisappearanceTick() {
        return EMISSION_TICKS - 1 + MAX_PARTICLE_LIFETIME;
    }

    public static boolean retirementFlickerStartsAfterEmission() {
        return MIN_PARTICLE_LIFETIME - RETIREMENT_FLICKER_MAX_LEAD_TICKS >= EMISSION_TICKS;
    }

    /** The rings are emitted from the center outward, so the visual radiates without a second particle stage. */
    public static int particlesCreatedThisTick(int tick) {
        return tick >= 0 && tick < EMISSION_TICKS ? PARTICLES_PER_EMISSION_TICK : 0;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        return Math.min(tick + 1, EMISSION_TICKS) * PARTICLES_PER_EMISSION_TICK;
    }

    /** Conservative active count: every emitted particle is assumed to live for the maximum lifetime. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        // Keep the boundary conservative because a particle can be observed before its client tick advances age.
        int firstLiveEmission = Math.max(0, tick - MAX_PARTICLE_LIFETIME);
        int lastLiveEmission = Math.min(tick, EMISSION_TICKS - 1);
        if (firstLiveEmission > lastLiveEmission) {
            return 0;
        }
        return (lastLiveEmission - firstLiveEmission + 1) * PARTICLES_PER_EMISSION_TICK;
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Giant particle budget tick may not be negative");
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

    public static Bounds conservativeBounds() {
        return new Bounds(
                -MAX_RADIUS,
                -MAX_RADIUS,
                -MAX_RADIUS,
                MAX_RADIUS,
                MAX_RADIUS,
                MAX_RADIUS,
                MAX_RADIUS);
    }

    public static boolean fitsRadius(BranchSample sample) {
        return sample != null && sample.position().lengthSqr() <= MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON;
    }

    private static Vec3 droopedDirection(Vec3 direction, double progress) {
        double droopProgress = smoothStep((progress - 0.58D) / 0.42D);
        return new Vec3(direction.x, direction.y - TERMINAL_DROOP_BIAS * droopProgress, direction.z).normalize();
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Giant radiant branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Giant radiant segment index is outside the configured count");
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
