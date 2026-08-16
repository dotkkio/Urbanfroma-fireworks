package com.urbanforma.fireworks.content.giant.chrysanthemum;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side contract for one giant, full-volume chrysanthemum.
 *
 * <p>Four concentric petal shells open from one origin within one bounded detonation window. The shell start
 * offsets only reveal the layers of that one event; they do not describe another launch, entity, request, or
 * explosion location.</p>
 */
public final class GiantChrysanthemumTrajectory {
    public static final String STABLE_ID = "giant_chrysanthemum_multishell_firework";
    public static final String ENGLISH_NAME = "Giant Chrysanthemum Multi-Shell Firework";
    public static final String CHINESE_NAME = "巨型三维千轮菊烟花";
    /** Name for the integrator to add to the shared enum; this isolated source does not modify that enum. */
    public static final String SUGGESTED_TIER_ID = "CHRYSANTHEMUM_MULTI_SHELL";

    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 129.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.26F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** The four shells are geometrically different petal volumes, not recolors of one branch set. */
    public enum Shell {
        HEART(36, 24, 0, 25.0D, 1.1D, 0.8D, 1.4D, 0.28D, ColorBand.HEART_PEARL),
        INNER(60, 32, 4, 57.0D, 2.5D, 1.7D, 3.0D, 0.55D, ColorBand.INNER_GOLD),
        MIDDLE(90, 40, 9, 90.0D, 4.7D, 3.1D, 5.6D, 0.91D, ColorBand.MIDDLE_AMBER),
        OUTER(132, 50, 14, 124.0D, 6.8D, 4.6D, 8.2D, 1.37D, ColorBand.OUTER_ROSE);

        private final int branchCount;
        private final int segmentsPerBranch;
        private final int startTick;
        private final double maximumPathRadius;
        private final double petalCurl;
        private final double rippleRadius;
        private final double cupLift;
        private final double curlTurns;
        private final ColorBand colorBand;

        Shell(
                int branchCount,
                int segmentsPerBranch,
                int startTick,
                double maximumPathRadius,
                double petalCurl,
                double rippleRadius,
                double cupLift,
                double curlTurns,
                ColorBand colorBand) {
            this.branchCount = branchCount;
            this.segmentsPerBranch = segmentsPerBranch;
            this.startTick = startTick;
            this.maximumPathRadius = maximumPathRadius;
            this.petalCurl = petalCurl;
            this.rippleRadius = rippleRadius;
            this.cupLift = cupLift;
            this.curlTurns = curlTurns;
            this.colorBand = colorBand;
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

        public double maximumPathRadius() {
            return this.maximumPathRadius;
        }

        public double petalCurl() {
            return this.petalCurl;
        }

        public double rippleRadius() {
            return this.rippleRadius;
        }

        public double cupLift() {
            return this.cupLift;
        }

        public double curlTurns() {
            return this.curlTurns;
        }

        public ColorBand colorBand() {
            return this.colorBand;
        }

        public int finalEmissionTick() {
            return this.startTick + this.segmentsPerBranch - 1;
        }

        public int particleCount() {
            return this.branchCount * this.segmentsPerBranch;
        }
    }

    public enum ColorBand {
        HEART_PEARL(new Rgb(1.0F, 0.96F, 0.80F), 1.48F),
        INNER_GOLD(new Rgb(1.0F, 0.76F, 0.24F), 1.39F),
        MIDDLE_AMBER(new Rgb(1.0F, 0.48F, 0.10F), 1.31F),
        OUTER_ROSE(new Rgb(0.96F, 0.20F, 0.34F), 1.34F);

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

    private static final Shell[] SHELLS = Shell.values();
    public static final int SHELL_COUNT = SHELLS.length;
    public static final int TOTAL_PARTICLES = totalParticles();
    public static final int MAX_EMISSION_TICK = maximumEmissionTick();
    public static final int EMISSION_TICKS = MAX_EMISSION_TICK + 1;
    public static final int MAX_PARTICLES_PER_EMISSION_TICK = maximumParticlesPerEmissionTick();
    public static final int MIN_PARTICLE_LIFETIME = 116;
    public static final int MAX_PARTICLE_LIFETIME = 172;
    public static final int TOTAL_VISUAL_TICKS = MAX_EMISSION_TICK + MAX_PARTICLE_LIFETIME + 1;
    /** Local per-program allocation ceiling. It is independent of any shared scheduler allowance. */
    public static final int LOCAL_PARTICLE_LIMIT = TOTAL_PARTICLES;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    /** Only a fixed subset needs a late retirement update. */
    public static final int MAX_TRACKED_RETIREMENT_SPARKS = 2_048;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 16;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 30;

    private static final double INITIAL_RADIUS = 2.0D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double RADIUS_LIMIT = MAX_RADIUS - 0.25D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long BRANCH_SALT = 0x3D17C8A3B4E56F91L;
    private static final long AZIMUTH_SALT = 0x12A4D5E6F7093BC1L;
    private static final long ELEVATION_SALT = 0x89BADCFE10293847L;
    private static final long PHASE_SALT = 0x5E2D58D8B3A9581FL;
    private static final long LIFETIME_SALT = 0xD1B54A32D192ED03L;
    private static final long BRILLIANCE_SALT = 0x94D049BB133111EBL;
    private static final long FLICKER_SALT = 0xC6BC279692B5CC83L;

    private GiantChrysanthemumTrajectory() {
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
                throw new IllegalArgumentException("Invalid chrysanthemum ascent sample");
            }
        }
    }

    /** One branch owns exactly one shell and an orthogonal local petal frame. */
    public record Branch(Shell shell, int index, long seed, Vec3 direction, Vec3 lateralAxis, Vec3 verticalAxis, double phase) {
        public Branch {
            if (shell == null || index < 0 || index >= shell.branchCount() || direction == null
                    || lateralAxis == null || verticalAxis == null || !Double.isFinite(phase)
                    || !finiteUnit(direction) || !finiteUnit(lateralAxis) || !finiteUnit(verticalAxis)
                    || Math.abs(direction.dot(lateralAxis)) > RADIUS_EPSILON
                    || Math.abs(direction.dot(verticalAxis)) > RADIUS_EPSILON
                    || Math.abs(lateralAxis.dot(verticalAxis)) > RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid chrysanthemum branch");
            }
        }
    }

    /** One stationary client spark along a curved petal path. */
    public record BranchSample(
            Branch branch,
            int segmentIndex,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brilliance,
            int lifetime) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= branch.shell().segmentsPerBranch()
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || position == null
                    || colorBand == null || !Float.isFinite(brilliance) || brilliance < 1.0F || brilliance > 1.26F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid chrysanthemum petal sample");
            }
        }
    }

    /** Deterministic retirement timing for already-created outer and middle petal sparks. */
    public record RetirementFlicker(int startAge, int cadencePhase) {
        public RetirementFlicker {
            if (startAge < 0 || cadencePhase < 0 || cadencePhase > 3) {
                throw new IllegalArgumentException("Invalid chrysanthemum retirement flicker");
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
                throw new IllegalArgumentException("Invalid chrysanthemum particle accounting");
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
                throw new IllegalArgumentException("Chrysanthemum bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Returns the declared common 0-to-200 ascent path. Client programs only render the detonation payload. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Chrysanthemum ascent tick is outside the 0-to-200 path");
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

    public static Branch branch(long payloadSeed, Shell shell, int branchIndex) {
        if (shell == null) {
            throw new IllegalArgumentException("Chrysanthemum shell is required");
        }
        validateBranchIndex(shell, branchIndex);
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) shell.ordinal() << 48)
                ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double sphereY = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / shell.branchCount();
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ AZIMUTH_SALT ^ shell.ordinal()) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * 0.09D;
        double elevation = Math.asin(clamp(sphereY, -1.0D, 1.0D))
                + centered(branchSeed, ELEVATION_SALT) * 0.055D;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(elevation), horizontal * Math.sin(azimuth)).normalize();
        Vec3 lateralAxis = perpendicular(direction);
        Vec3 verticalAxis = direction.cross(lateralAxis).normalize();
        return new Branch(shell, branchIndex, branchSeed, direction, lateralAxis, verticalAxis,
                randomUnit(branchSeed ^ PHASE_SALT) * TWO_PI);
    }

    public static BranchSample sample(long payloadSeed, Shell shell, int branchIndex, int segmentIndex) {
        return sample(branch(payloadSeed, shell, branchIndex), segmentIndex);
    }

    public static BranchSample sample(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Chrysanthemum branch is required");
        }
        validateSegmentIndex(branch.shell(), segmentIndex);
        Shell shell = branch.shell();
        double progress = (double) segmentIndex / (shell.segmentsPerBranch() - 1);
        double petalProgress = smoothStep(progress);
        double radius = INITIAL_RADIUS + (shell.maximumPathRadius() - INITIAL_RADIUS) * petalProgress;
        double bell = Math.sin(Math.PI * progress);
        double curl = shell.petalCurl() * bell * (0.40D + 0.60D * petalProgress);
        double ripple = Math.sin(branch.phase() + TWO_PI * shell.curlTurns() * progress)
                * shell.rippleRadius() * bell;
        double cup = shell.cupLift() * bell * (0.25D + 0.75D * petalProgress)
                * Math.cos(branch.phase() * 0.5D + progress * Math.PI);
        Vec3 raw = branch.direction().scale(radius)
                .add(branch.lateralAxis().scale(curl + ripple))
                .add(branch.verticalAxis().scale(cup));
        int lifetime = randomInt(
                branch.seed(),
                LIFETIME_SALT + ((long) segmentIndex << 8),
                MIN_PARTICLE_LIFETIME,
                MAX_PARTICLE_LIFETIME);
        float brilliance = (float) (1.02D + randomUnit(
                branch.seed() ^ BRILLIANCE_SALT ^ ((long) segmentIndex << 20)) * 0.22D);
        return new BranchSample(
                branch,
                segmentIndex,
                progress,
                radiusSafe(raw),
                shell.colorBand(),
                brilliance,
                lifetime);
    }

    /** The late flicker is a retirement state of an existing spark, not a new particle population. */
    public static RetirementFlicker retirementFlicker(BranchSample sample) {
        if (!tracksRetirement(sample)) {
            throw new IllegalArgumentException("Only selected chrysanthemum sparks have a retirement window");
        }
        int lead = randomInt(
                sample.branch().seed(),
                FLICKER_SALT + ((long) sample.segmentIndex() << 7),
                RETIREMENT_FLICKER_MIN_LEAD_TICKS,
                RETIREMENT_FLICKER_MAX_LEAD_TICKS);
        int phase = Math.floorMod((int) (mix64(sample.branch().seed() ^ FLICKER_SALT ^ sample.segmentIndex()) >>> 37), 4);
        return new RetirementFlicker(Math.max(0, sample.lifetime() - lead), phase);
    }

    public static boolean tracksRetirement(BranchSample sample) {
        if (sample == null) {
            return false;
        }
        Shell shell = sample.branch().shell();
        return (shell == Shell.OUTER && Math.floorMod(sample.branch().index() + sample.segmentIndex(), 4) == 0)
                || (shell == Shell.MIDDLE && sample.branch().index() % 5 == 0 && sample.segmentIndex() % 3 == 0);
    }

    public static int particlesCreatedThisTick(int tick) {
        if (tick < 0 || tick > MAX_EMISSION_TICK) {
            return 0;
        }
        int created = 0;
        for (Shell shell : SHELLS) {
            int segmentIndex = tick - shell.startTick();
            if (segmentIndex >= 0 && segmentIndex < shell.segmentsPerBranch()) {
                created += shell.branchCount();
            }
        }
        return created;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int created = 0;
        int finalTick = Math.min(tick, MAX_EMISSION_TICK);
        for (int emissionTick = 0; emissionTick <= finalTick; emissionTick++) {
            created += particlesCreatedThisTick(emissionTick);
        }
        return created;
    }

    /** Conservative local cap: every created spark is assumed to live for the configured maximum. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int created = 0;
        int firstLiveEmission = Math.max(0, tick - MAX_PARTICLE_LIFETIME);
        int lastLiveEmission = Math.min(tick, MAX_EMISSION_TICK);
        for (int emissionTick = firstLiveEmission; emissionTick <= lastLiveEmission; emissionTick++) {
            created += particlesCreatedThisTick(emissionTick);
        }
        return created;
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Chrysanthemum particle budget tick may not be negative");
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

    public static boolean hasSingleOriginBurstPlan() {
        return SHELL_COUNT == 4
                && SHELLS[0].startTick() == 0
                && MAX_EMISSION_TICK < 80
                && particlesCreatedThroughTick(MAX_EMISSION_TICK) == TOTAL_PARTICLES
                && particlesCreatedThisTick(MAX_EMISSION_TICK + 1) == 0;
    }

    /** Exhaustive deterministic proof over every sample of one payload seed. */
    public static boolean staticContractHolds(long payloadSeed) {
        int sampledParticles = 0;
        int retirementTracks = 0;
        for (Shell shell : SHELLS) {
            for (int branchIndex = 0; branchIndex < shell.branchCount(); branchIndex++) {
                Branch branch = branch(payloadSeed, shell, branchIndex);
                for (int segmentIndex = 0; segmentIndex < shell.segmentsPerBranch(); segmentIndex++) {
                    BranchSample sample = sample(branch, segmentIndex);
                    if (sample.position().lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                        return false;
                    }
                    sampledParticles++;
                    if (tracksRetirement(sample)) {
                        retirementTracks++;
                    }
                }
            }
        }
        return ascentFitsDeclaredHeight()
                && hasSingleOriginBurstPlan()
                && sampledParticles == TOTAL_PARTICLES
                && particlePlanAtTick(MAX_EMISSION_TICK).cumulativeCreated() == TOTAL_PARTICLES
                && maximumAliveParticleUpperBound() <= LOCAL_PARTICLE_LIMIT
                && retirementTracks <= MAX_TRACKED_RETIREMENT_SPARKS
                && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS);
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator accumulator = new BoundsAccumulator();
        for (Shell shell : SHELLS) {
            for (int branchIndex = 0; branchIndex < shell.branchCount(); branchIndex++) {
                Branch branch = branch(payloadSeed, shell, branchIndex);
                for (int segmentIndex = 0; segmentIndex < shell.segmentsPerBranch(); segmentIndex++) {
                    accumulator.include(sample(branch, segmentIndex).position());
                }
            }
        }
        return accumulator.toBounds();
    }

    private static int totalParticles() {
        int total = 0;
        for (Shell shell : SHELLS) {
            total += shell.particleCount();
        }
        return total;
    }

    private static int maximumEmissionTick() {
        int maximum = 0;
        for (Shell shell : SHELLS) {
            maximum = Math.max(maximum, shell.finalEmissionTick());
        }
        return maximum;
    }

    private static int maximumParticlesPerEmissionTick() {
        int maximum = 0;
        for (int tick = 0; tick <= MAX_EMISSION_TICK; tick++) {
            maximum = Math.max(maximum, particlesCreatedThisTick(tick));
        }
        return maximum;
    }

    private static Vec3 perpendicular(Vec3 direction) {
        Vec3 reference = Math.abs(direction.y) < 0.86D ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
        return direction.cross(reference).normalize();
    }

    private static Vec3 radiusSafe(Vec3 value) {
        double lengthSqr = value.lengthSqr();
        double limitSqr = RADIUS_LIMIT * RADIUS_LIMIT;
        return lengthSqr <= limitSqr ? value : value.scale(RADIUS_LIMIT / Math.sqrt(lengthSqr));
    }

    private static boolean finiteUnit(Vec3 value) {
        return value.lengthSqr() > RADIUS_EPSILON && Math.abs(value.lengthSqr() - 1.0D) < 1.0E-6D;
    }

    private static void validateBranchIndex(Shell shell, int branchIndex) {
        if (branchIndex < 0 || branchIndex >= shell.branchCount()) {
            throw new IllegalArgumentException("Chrysanthemum branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(Shell shell, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= shell.segmentsPerBranch()) {
            throw new IllegalArgumentException("Chrysanthemum segment index is outside the configured count");
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
