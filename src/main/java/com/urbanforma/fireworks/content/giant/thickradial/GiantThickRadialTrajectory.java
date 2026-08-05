package com.urbanforma.fireworks.content.giant.thickradial;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side contract for the sixth giant firework.
 *
 * <p>This is deliberately a sparse radial composition: 32 paths, rather than a dense shell of fine lines. Each
 * path carries five co-located cross-section layers so its body reads as one thick, continuous feather of sparks.
 * All samples belong to one detonation window; there are no child shells, later launches, or follow-up bursts.</p>
 */
public final class GiantThickRadialTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.28F;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** Far fewer paths than the first giant's 256, compensated by a five-spark cross-section at every sample. */
    public static final int BRANCH_COUNT = 32;
    public static final int SEGMENTS_PER_BRANCH = 72;
    public static final int PARTICLE_LAYERS_PER_SEGMENT = 5;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT * PARTICLE_LAYERS_PER_SEGMENT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH * PARTICLE_LAYERS_PER_SEGMENT;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    public static final int MIN_PARTICLE_LIFETIME = 112;
    public static final int MAX_PARTICLE_LIFETIME = 156;
    public static final int TOTAL_VISUAL_TICKS = EMISSION_TICKS + MAX_PARTICLE_LIFETIME;

    /** The terminal cross-section must stay visibly broad without exceeding the 130-block burst radius. */
    public static final double MIN_BRANCH_HALF_THICKNESS = 0.42D;
    public static final double MAX_BRANCH_HALF_THICKNESS = 2.25D;
    public static final double MIN_VISIBLE_TERMINAL_THICKNESS = 2.20D;
    public static final double MAX_CONTINUOUS_SAMPLE_GAP = 4.0D;

    private static final double INITIAL_RADIUS = 2.0D;
    private static final double OUTER_PATH_RADIUS = 126.60D;
    private static final double TERMINAL_DROOP_BIAS = 0.13D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long BRANCH_SALT = 0xED0F67B5B0F34591L;
    private static final long AZIMUTH_SALT = 0xA4093822299F31D0L;
    private static final long ELEVATION_SALT = 0x13198A2E03707344L;
    private static final long LIFETIME_SALT = 0x452821E638D01377L;
    private static final long BRILLIANCE_SALT = 0xBE5466CF34E90C6CL;

    private GiantThickRadialTrajectory() {
    }

    /** Five fixed positions across one branch body; warm material stays central while blue remains an inner contrast. */
    public enum ParticleLayer {
        WARM_WHITE_CORE(new Rgb(1.0F, 0.91F, 0.72F), 1.48F, 0.0D, 0.0D, 0.00D, true),
        PEACH_LEFT(new Rgb(1.0F, 0.68F, 0.45F), 1.42F, -0.62D, 0.30D, -0.16D, false),
        PEACH_RIGHT(new Rgb(1.0F, 0.78F, 0.55F), 1.38F, 0.62D, -0.30D, 0.16D, false),
        DEEP_BLUE_LEFT(new Rgb(0.16F, 0.34F, 0.68F), 1.22F, -1.00D, 0.72D, -0.08D, false),
        DEEP_BLUE_RIGHT(new Rgb(0.24F, 0.48F, 0.84F), 1.26F, 1.00D, -0.72D, 0.08D, false);

        private final Rgb rgb;
        private final float quadScale;
        private final double lateralOffset;
        private final double verticalOffset;
        private final double longitudinalOffset;
        private final boolean coreHighlight;

        ParticleLayer(
                Rgb rgb,
                float quadScale,
                double lateralOffset,
                double verticalOffset,
                double longitudinalOffset,
                boolean coreHighlight) {
            this.rgb = rgb;
            this.quadScale = quadScale;
            this.lateralOffset = lateralOffset;
            this.verticalOffset = verticalOffset;
            this.longitudinalOffset = longitudinalOffset;
            this.coreHighlight = coreHighlight;
        }

        public Rgb rgb() {
            return this.rgb;
        }

        public float quadScale() {
            return this.quadScale;
        }

        public double lateralOffset() {
            return this.lateralOffset;
        }

        public double verticalOffset() {
            return this.verticalOffset;
        }

        public double longitudinalOffset() {
            return this.longitudinalOffset;
        }

        public boolean coreHighlight() {
            return this.coreHighlight;
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
                throw new IllegalArgumentException("Invalid thick-radial ascent sample");
            }
        }
    }

    /** A sparse, deterministic radial direction plus an orthogonal pair used to give it a real cross-section. */
    public record Branch(int index, long seed, Vec3 direction, Vec3 lateralAxis, Vec3 verticalAxis) {
        public Branch {
            if (index < 0 || index >= BRANCH_COUNT || direction == null || lateralAxis == null || verticalAxis == null
                    || !finiteUnit(direction) || !finiteUnit(lateralAxis) || !finiteUnit(verticalAxis)
                    || Math.abs(direction.dot(lateralAxis)) > 1.0E-8D
                    || Math.abs(direction.dot(verticalAxis)) > 1.0E-8D
                    || Math.abs(lateralAxis.dot(verticalAxis)) > 1.0E-8D) {
                throw new IllegalArgumentException("Invalid thick-radial branch");
            }
        }
    }

    /** One bounded particle in the five-layer cross-section of one continuous branch path. */
    public record BranchSample(
            Branch branch,
            int segmentIndex,
            ParticleLayer particleLayer,
            double progress,
            double pathRadius,
            double halfThickness,
            Vec3 position,
            float brilliance,
            int lifetime) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH || particleLayer == null
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(pathRadius) || pathRadius < 0.0D || pathRadius > MAX_RADIUS
                    || !Double.isFinite(halfThickness) || halfThickness < MIN_BRANCH_HALF_THICKNESS
                    || halfThickness > MAX_BRANCH_HALF_THICKNESS || position == null
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                    || !Float.isFinite(brilliance) || brilliance < 1.0F || brilliance > 1.24F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME) {
                throw new IllegalArgumentException("Invalid thick-radial branch sample");
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
                throw new IllegalArgumentException("Invalid thick-radial particle accounting");
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
                throw new IllegalArgumentException("Thick-radial bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Returns the exact common 0-to-200 launch path; this prototype declares no second ascent. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Thick-radial ascent tick is outside the 0-to-200 path");
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
                + randomUnit(payloadSeed ^ AZIMUTH_SALT) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * 0.075D;
        double elevation = Math.asin(clamp(sphereY, -1.0D, 1.0D))
                + centered(branchSeed, ELEVATION_SALT) * 0.045D;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(elevation), horizontal * Math.sin(azimuth)).normalize();
        Vec3 lateralAxis = perpendicular(direction);
        Vec3 verticalAxis = direction.cross(lateralAxis).normalize();
        return new Branch(branchIndex, branchSeed, direction, lateralAxis, verticalAxis);
    }

    public static BranchSample sample(long payloadSeed, int branchIndex, int segmentIndex, ParticleLayer particleLayer) {
        return sample(branch(payloadSeed, branchIndex), segmentIndex, particleLayer);
    }

    public static BranchSample sample(Branch branch, int segmentIndex, ParticleLayer particleLayer) {
        if (branch == null || particleLayer == null) {
            throw new IllegalArgumentException("Thick-radial branch and particle layer are required");
        }
        validateSegmentIndex(segmentIndex);
        double progress = (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
        double radius = pathRadius(progress);
        double halfThickness = branchHalfThickness(progress);
        Vec3 curvedDirection = droopedDirection(branch.direction(), progress);
        Vec3 base = curvedDirection.scale(radius + particleLayer.longitudinalOffset() * halfThickness);
        Vec3 offset = branch.lateralAxis().scale(particleLayer.lateralOffset() * halfThickness)
                .add(branch.verticalAxis().scale(particleLayer.verticalOffset() * halfThickness));
        Vec3 position = radiusSafe(base.add(offset));
        int lifetime = randomInt(
                branch.seed(),
                LIFETIME_SALT + ((long) segmentIndex << 4) + particleLayer.ordinal(),
                MIN_PARTICLE_LIFETIME,
                MAX_PARTICLE_LIFETIME);
        float brilliance = (float) (1.03D + randomUnit(
                branch.seed() ^ BRILLIANCE_SALT ^ ((long) segmentIndex << 8) ^ particleLayer.ordinal()) * 0.17D);
        return new BranchSample(
                branch,
                segmentIndex,
                particleLayer,
                progress,
                radius,
                halfThickness,
                position,
                brilliance,
                lifetime);
    }

    public static boolean isCoreLayer(ParticleLayer particleLayer) {
        return particleLayer != null && particleLayer.coreHighlight();
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

    /** Conservative count: every created particle is assumed to live for the configured maximum lifetime. */
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
            throw new IllegalArgumentException("Thick-radial particle budget tick may not be negative");
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

    /** The only allocation window is the first 72 ticks after the single detonation. */
    public static boolean hasSingleDetonationPlan() {
        return DETONATION_SOUND_MAX_PLAYS == 1
                && particlesCreatedThroughTick(EMISSION_TICKS - 1) == TOTAL_PARTICLES
                && particlesCreatedThisTick(EMISSION_TICKS) == 0
                && particlesCreatedThisTick(EMISSION_TICKS + MAX_PARTICLE_LIFETIME) == 0;
    }

    /** Verifies that the five layers occupy a visibly wide cross-section rather than a color-only duplicate path. */
    public static boolean hasThickCrossSection(long payloadSeed) {
        Branch branch = branch(payloadSeed, 0);
        int terminalSegment = SEGMENTS_PER_BRANCH - 1;
        Vec3 core = sample(branch, terminalSegment, ParticleLayer.WARM_WHITE_CORE).position();
        Vec3 blueLeft = sample(branch, terminalSegment, ParticleLayer.DEEP_BLUE_LEFT).position();
        Vec3 blueRight = sample(branch, terminalSegment, ParticleLayer.DEEP_BLUE_RIGHT).position();
        return core.distanceTo(blueLeft) >= MIN_VISIBLE_TERMINAL_THICKNESS
                && core.distanceTo(blueRight) >= MIN_VISIBLE_TERMINAL_THICKNESS
                && blueLeft.distanceTo(blueRight) >= MIN_VISIBLE_TERMINAL_THICKNESS * 2.0D;
    }

    /** Center samples must remain close enough to make each sparse path visually continuous. */
    public static boolean hasContinuousInteriorSampling(long payloadSeed) {
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            Vec3 previous = null;
            for (int segmentIndex = 0; segmentIndex < SEGMENTS_PER_BRANCH; segmentIndex++) {
                Vec3 current = sample(branch, segmentIndex, ParticleLayer.WARM_WHITE_CORE).position();
                if (previous != null && previous.distanceTo(current) > MAX_CONTINUOUS_SAMPLE_GAP) {
                    return false;
                }
                previous = current;
            }
        }
        return true;
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator accumulator = new BoundsAccumulator();
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(payloadSeed, branchIndex);
            for (int segmentIndex = 0; segmentIndex < SEGMENTS_PER_BRANCH; segmentIndex++) {
                for (ParticleLayer particleLayer : ParticleLayer.values()) {
                    accumulator.include(sample(branch, segmentIndex, particleLayer).position());
                }
            }
        }
        return accumulator.toBounds();
    }

    /** Exhaustive static proof over the sparse-path and five-layer lattice for a deterministic payload seed. */
    public static boolean staticContractHolds(long payloadSeed) {
        if (!ascentFitsDeclaredHeight()
                || BRANCH_COUNT >= 256
                || PARTICLE_LAYERS_PER_SEGMENT != ParticleLayer.values().length
                || PARTICLES_PER_EMISSION_TICK != 160
                || TOTAL_PARTICLES != 11_520
                || maximumAliveParticleUpperBound() != TOTAL_PARTICLES
                || !hasSingleDetonationPlan()
                || !hasThickCrossSection(payloadSeed)
                || !hasContinuousInteriorSampling(payloadSeed)) {
            return false;
        }
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch first = branch(payloadSeed, branchIndex);
            if (!first.equals(branch(payloadSeed, branchIndex))) {
                return false;
            }
            for (int segmentIndex = 0; segmentIndex < SEGMENTS_PER_BRANCH; segmentIndex++) {
                for (ParticleLayer particleLayer : ParticleLayer.values()) {
                    BranchSample sample = sample(first, segmentIndex, particleLayer);
                    if (sample.position().lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                            || sample.lifetime() > MAX_PARTICLE_LIFETIME) {
                        return false;
                    }
                }
            }
        }
        return conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS)
                && particlePlanAtTick(EMISSION_TICKS - 1).activeUpperBound() <= PROTOTYPE_MAX_ALIVE_PARTICLES;
    }

    private static double pathRadius(double progress) {
        return INITIAL_RADIUS + (OUTER_PATH_RADIUS - INITIAL_RADIUS) * smoothStep(progress);
    }

    private static double branchHalfThickness(double progress) {
        return MIN_BRANCH_HALF_THICKNESS
                + (MAX_BRANCH_HALF_THICKNESS - MIN_BRANCH_HALF_THICKNESS) * smoothStep(progress);
    }

    private static Vec3 droopedDirection(Vec3 direction, double progress) {
        double terminalProgress = smoothStep((progress - 0.58D) / 0.42D);
        return new Vec3(direction.x, direction.y - TERMINAL_DROOP_BIAS * terminalProgress, direction.z).normalize();
    }

    private static Vec3 perpendicular(Vec3 direction) {
        Vec3 candidate = Math.abs(direction.y) < 0.85D
                ? new Vec3(-direction.z, 0.0D, direction.x)
                : new Vec3(0.0D, -direction.z, direction.y);
        return candidate.normalize();
    }

    private static Vec3 radiusSafe(Vec3 position) {
        double maximumRadius = MAX_RADIUS - RADIUS_EPSILON;
        double lengthSqr = position.lengthSqr();
        double maximumLengthSqr = maximumRadius * maximumRadius;
        return lengthSqr <= maximumLengthSqr ? position : position.scale(maximumRadius / Math.sqrt(lengthSqr));
    }

    private static boolean finiteUnit(Vec3 value) {
        return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z)
                && Math.abs(value.lengthSqr() - 1.0D) <= 1.0E-8D;
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Thick-radial branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Thick-radial segment index is outside the configured count");
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
