package com.urbanforma.fireworks.content.giant.spiral;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, unregistered geometry for a three-dimensional giant aurora spiral shell.
 *
 * <p>Each layer distributes complete spherical branches, then precesses every branch around its own perpendicular
 * 3D axis. The result is a volumetric shell rather than a planar spiral, ring, or symbolic outline. This common
 * geometry owns no particle creation, entity construction, registry, network, or server-side trajectory work.</p>
 */
public final class GiantSpiralTrajectory {
    public static final String STABLE_ID = "giant_aurora_spiral_firework";
    public static final String DEFAULT_ENGLISH_NAME = "Giant Aurora Three-Dimensional Spiral Firework";
    public static final String DEFAULT_CHINESE_NAME = "\u5de8\u578b\u6781\u5149\u4e09\u7ef4\u87ba\u65cb\u5c42\u653e\u5c04\u70df\u82b1";
    /** Suggested future enum value only. This isolated class intentionally does not import GiantTier. */
    public static final String SUGGESTED_GIANT_TIER = "SPIRAL";

    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 124.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.29F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;
    /** One future entity request maps to exactly one visual detonation plan. */
    public static final int DETONATIONS_PER_REQUEST = 1;

    public static final int LAYER_COUNT = Layer.values().length;
    public static final int TOTAL_PARTICLES = totalParticles();
    public static final int LAST_EMISSION_TICK = lastEmissionTick();
    public static final int MAX_PARTICLES_PER_EMISSION_TICK = maximumParticlesPerEmissionTick();
    public static final int MIN_PARTICLE_LIFETIME = 112;
    public static final int MAX_PARTICLE_LIFETIME = 134;
    public static final int UNWIND_START_TICK = LAST_EMISSION_TICK + 1;
    public static final int UNWIND_TICKS = 58;
    public static final int TOTAL_VISUAL_TICKS = LAST_EMISSION_TICK + MAX_PARTICLE_LIFETIME + 1;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 16;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 26;

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final long BRANCH_SALT = 0x748F91B6E2A54D31L;
    private static final long DIRECTION_SALT = 0x9E3779B97F4A7C15L;
    private static final long AXIS_SALT = 0xD6E8FEB86659FD93L;
    private static final long LIFETIME_SALT = 0x94D049BB133111EBL;
    private static final long BRIGHTNESS_SALT = 0xBF58476D1CE4E5B9L;

    private GiantSpiralTrajectory() {
    }

    /** Three separated radii, start times, turn counts, and unwind amounts form the giant's time structure. */
    public enum Layer {
        INNER(96, 18, 0, 48.0D, 0.82D, 0.24D),
        MIDDLE(128, 22, 8, 84.0D, 1.18D, 0.42D),
        OUTER(160, 28, 18, 122.0D, 1.66D, 0.62D);

        private final int branchCount;
        private final int segmentsPerBranch;
        private final int startTick;
        private final double maximumRadius;
        private final double turns;
        private final double unwindTurns;

        Layer(
                int branchCount,
                int segmentsPerBranch,
                int startTick,
                double maximumRadius,
                double turns,
                double unwindTurns) {
            this.branchCount = branchCount;
            this.segmentsPerBranch = segmentsPerBranch;
            this.startTick = startTick;
            this.maximumRadius = maximumRadius;
            this.turns = turns;
            this.unwindTurns = unwindTurns;
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

        public double maximumRadius() {
            return this.maximumRadius;
        }

        public double turns() {
            return this.turns;
        }

        public double unwindTurns() {
            return this.unwindTurns;
        }

        public int particleCount() {
            return this.branchCount * this.segmentsPerBranch;
        }
    }

    /** Color moves from pearl/aqua at the core through violet and rose before an outer silver departure. */
    public enum ColorBand {
        PEARL(new Rgb(0.94F, 0.99F, 1.0F), 1.48F),
        AURORA_AQUA(new Rgb(0.14F, 0.90F, 0.93F), 1.36F),
        DEEP_AZURE(new Rgb(0.20F, 0.42F, 1.0F), 1.34F),
        VIOLET(new Rgb(0.66F, 0.28F, 1.0F), 1.36F),
        ROSE(new Rgb(1.0F, 0.28F, 0.66F), 1.32F),
        SILVER(new Rgb(0.84F, 0.90F, 1.0F), 1.24F);

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
                    || progress < 0.0D || progress > 1.0D || !finite(position) || !finite(velocity)) {
                throw new IllegalArgumentException("Invalid giant spiral ascent sample");
            }
        }
    }

    /** The axis is perpendicular to the initial direction, guaranteeing genuine precession for every branch. */
    public record SpiralBranch(Layer layer, int index, long seed, Vec3 initialDirection, Vec3 axis, double phase) {
        public SpiralBranch {
            if (layer == null || index < 0 || index >= layer.branchCount() || !finite(initialDirection)
                    || !finite(axis) || !Double.isFinite(phase) || Math.abs(initialDirection.dot(axis)) > 1.0E-5D) {
                throw new IllegalArgumentException("Invalid giant spiral branch");
            }
        }
    }

    public record SpiralSample(
            Layer layer,
            SpiralBranch branch,
            int segmentIndex,
            int emissionTick,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime) {
        public SpiralSample {
            if (layer == null || branch == null || branch.layer() != layer || segmentIndex < 0
                    || segmentIndex >= layer.segmentsPerBranch() || emissionTick != layer.startTick() + segmentIndex
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || !finite(position)
                    || colorBand == null || !Float.isFinite(brightness) || brightness < 1.0F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME || !fitsRadius(position)) {
                throw new IllegalArgumentException("Invalid giant spiral sample");
            }
        }
    }

    public record RetirementFlicker(int startAge, int phase) {
        public RetirementFlicker {
            if (startAge < 0 || phase < 0 || phase > 2) {
                throw new IllegalArgumentException("Invalid giant spiral retirement cadence");
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
                throw new IllegalArgumentException("Giant spiral bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius > 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Per-instance proof accounting; it never consumes an ordinary or shared giant scheduler budget. */
    public record ParticlePlan(
            int tick, int createdThisTick, int cumulativeCreated, int activeUpperBound, int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0 || cumulativeCreated > TOTAL_PARTICLES
                    || activeUpperBound > PROTOTYPE_MAX_ALIVE_PARTICLES) {
                throw new IllegalArgumentException("Invalid giant spiral particle accounting");
            }
        }
    }

    /** Returns exact ascent proof from height 0 on tick 0 to height 200 on tick 137. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Giant spiral ascent tick is outside the 0-to-200 path");
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

    public static SpiralBranch branch(long payloadSeed, Layer layer, int branchIndex) {
        if (layer == null) {
            throw new IllegalArgumentException("Giant spiral layer may not be null");
        }
        validateBranchIndex(layer, branchIndex);
        long seed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) layer.ordinal() << 48)
                ^ (long) branchIndex * 0x9E3779B97F4A7C15L);
        int permutation = Math.floorMod(branchIndex * 37, layer.branchCount());
        double sphereY = 1.0D - 2.0D * (permutation + 0.5D) / layer.branchCount();
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ DIRECTION_SALT ^ layer.ordinal()) * TWO_PI
                + centered(seed, DIRECTION_SALT) * 0.045D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - sphereY * sphereY));
        Vec3 initial = new Vec3(horizontal * Math.cos(azimuth), sphereY, horizontal * Math.sin(azimuth)).normalize();
        Vec3 reference = Math.abs(initial.y) < 0.85D ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 perpendicular = initial.cross(reference).normalize();
        Vec3 secondPerpendicular = initial.cross(perpendicular).normalize();
        double axisPhase = randomUnit(seed ^ AXIS_SALT) * TWO_PI;
        Vec3 axis = perpendicular.scale(Math.cos(axisPhase)).add(secondPerpendicular.scale(Math.sin(axisPhase))).normalize();
        double phase = randomUnit(seed ^ (AXIS_SALT + 1L)) * TWO_PI;
        return new SpiralBranch(layer, branchIndex, seed, initial, axis, phase);
    }

    public static SpiralSample sample(long payloadSeed, Layer layer, int branchIndex, int segmentIndex) {
        SpiralBranch branch = branch(payloadSeed, layer, branchIndex);
        validateSegmentIndex(branch.layer(), segmentIndex);
        double progress = (double) segmentIndex / (branch.layer().segmentsPerBranch() - 1);
        Vec3 position = position(branch, segmentIndex, 0);
        int lifetime = randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, MIN_PARTICLE_LIFETIME, MAX_PARTICLE_LIFETIME);
        return new SpiralSample(
                branch.layer(),
                branch,
                segmentIndex,
                branch.layer().startTick() + segmentIndex,
                progress,
                position,
                colorBand(branch, segmentIndex),
                brightness(branch.seed(), segmentIndex),
                lifetime);
    }

    /** Repositions an existing spark during the finite reverse-precession departure; it creates no new sample. */
    public static Vec3 positionDuringUnwind(SpiralSample sample, int unwindAge) {
        if (sample == null) {
            throw new IllegalArgumentException("Giant spiral sample may not be null");
        }
        return position(sample.branch(), sample.segmentIndex(), unwindAge);
    }

    public static boolean isEmitting(Layer layer, int tick) {
        return layer != null && tick >= layer.startTick() && tick < layer.endTickExclusive();
    }

    public static boolean isUnwinding(int tick) {
        return tick >= UNWIND_START_TICK && tick < UNWIND_START_TICK + UNWIND_TICKS;
    }

    public static int particlesCreatedThisTick(int tick) {
        int count = 0;
        for (Layer layer : Layer.values()) {
            if (isEmitting(layer, tick)) {
                count += layer.branchCount();
            }
        }
        return count;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int count = 0;
        for (Layer layer : Layer.values()) {
            int emittedSegments = Math.max(0, Math.min(layer.segmentsPerBranch(), tick - layer.startTick() + 1));
            count += emittedSegments * layer.branchCount();
        }
        return count;
    }

    /** Conservative: every emitted spiral sample is retained for the full maximum lifetime. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int firstLiveTick = Math.max(0, tick - MAX_PARTICLE_LIFETIME + 1);
        return particlesCreatedThroughTick(tick) - particlesCreatedThroughTick(firstLiveTick - 1);
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Giant spiral particle-plan tick may not be negative");
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

    public static RetirementFlicker retirementFlicker(SpiralSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Giant spiral sample may not be null");
        }
        int spread = RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1;
        int owner = sample.layer().ordinal() * 47 + sample.branch().index() * 29 + sample.segmentIndex() * 13;
        int lead = RETIREMENT_FLICKER_MIN_LEAD_TICKS + Math.floorMod(owner, spread);
        return new RetirementFlicker(sample.lifetime() - lead, Math.floorMod(owner * 5, 3));
    }

    /** Exhaustively includes each static sample and all finite reverse-precession positions. */
    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator bounds = new BoundsAccumulator();
        for (Layer layer : Layer.values()) {
            for (int branchIndex = 0; branchIndex < layer.branchCount(); branchIndex++) {
                for (int segment = 0; segment < layer.segmentsPerBranch(); segment++) {
                    SpiralSample sample = sample(payloadSeed, layer, branchIndex, segment);
                    for (int unwindAge = 0; unwindAge <= UNWIND_TICKS; unwindAge++) {
                        bounds.include(positionDuringUnwind(sample, unwindAge));
                    }
                }
            }
        }
        return bounds.toBounds();
    }

    /** Static proof for the 0-to-200 flight, one detonation, three distinct 3D layers, and bounded exit path. */
    public static boolean staticContractHolds(long payloadSeed) {
        if (!ascentFitsDeclaredHeight() || DETONATION_SOUND_MAX_PLAYS != 1 || DETONATIONS_PER_REQUEST != 1
                || LAYER_COUNT != 3
                || TOTAL_PARTICLES != 9_024 || MAX_PARTICLES_PER_EMISSION_TICK != 288
                || Layer.INNER.maximumRadius() >= Layer.MIDDLE.maximumRadius()
                || Layer.MIDDLE.maximumRadius() >= Layer.OUTER.maximumRadius()
                || Layer.OUTER.maximumRadius() > MAX_RADIUS
                || Layer.INNER.startTick() >= Layer.MIDDLE.startTick()
                || Layer.MIDDLE.startTick() >= Layer.OUTER.startTick()
                || particlesCreatedThroughTick(TOTAL_VISUAL_TICKS) != TOTAL_PARTICLES
                || particlesCreatedThisTick(TOTAL_VISUAL_TICKS) != 0
                || maximumAliveParticleUpperBound() != TOTAL_PARTICLES) {
            return false;
        }
        Bounds bounds = conservativeBounds(payloadSeed);
        if (!bounds.fitsRadius(MAX_RADIUS)
                || !(bounds.minX() < 0.0D && bounds.maxX() > 0.0D
                && bounds.minY() < 0.0D && bounds.maxY() > 0.0D
                && bounds.minZ() < 0.0D && bounds.maxZ() > 0.0D)) {
            return false;
        }
        for (Layer layer : Layer.values()) {
            SpiralBranch first = branch(payloadSeed, layer, 0);
            SpiralBranch last = branch(payloadSeed, layer, layer.branchCount() - 1);
            double directionDeltaX = first.initialDirection().x - last.initialDirection().x;
            double directionDeltaY = first.initialDirection().y - last.initialDirection().y;
            double directionDeltaZ = first.initialDirection().z - last.initialDirection().z;
            double directionDeltaSqr = directionDeltaX * directionDeltaX
                    + directionDeltaY * directionDeltaY + directionDeltaZ * directionDeltaZ;
            if (Math.abs(first.initialDirection().dot(first.axis())) > 1.0E-5D
                    || directionDeltaSqr < 1.0D) {
                return false;
            }
        }
        return true;
    }

    /** Throws if a future adapter mutates the isolated one-flight, one-detonation multi-layer contract. */
    public static void validateContract() {
        if (!staticContractHolds(0x2F6D9A81C4E73B05L)) {
            throw new IllegalStateException("Giant spiral contract is inconsistent");
        }
    }

    public static boolean fitsRadius(Vec3 position) {
        return finite(position) && position.lengthSqr() <= MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON;
    }

    private static Vec3 position(SpiralBranch branch, int segmentIndex, int unwindAge) {
        validateSegmentIndex(branch.layer(), segmentIndex);
        if (unwindAge < 0 || unwindAge > UNWIND_TICKS) {
            throw new IllegalArgumentException("Giant spiral unwind age is outside the fixed window");
        }
        Layer layer = branch.layer();
        double progress = (double) segmentIndex / (layer.segmentsPerBranch() - 1);
        double unwindProgress = (double) unwindAge / UNWIND_TICKS;
        double turnAngle = branch.phase() + layer.turns() * TWO_PI * progress
                - layer.unwindTurns() * TWO_PI * smoothStep(unwindProgress);
        Vec3 direction = rotateAroundAxis(branch.initialDirection(), branch.axis(), turnAngle);
        double baseRadius = 2.0D + (layer.maximumRadius() - 2.0D) * smoothStep(progress);
        double radius = baseRadius * (1.0D - 0.14D * smoothStep(unwindProgress));
        Vec3 position = direction.scale(radius);
        if (!fitsRadius(position)) {
            throw new IllegalStateException("Giant spiral escaped its declared envelope");
        }
        return position;
    }

    private static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return vector.scale(cosine)
                .add(axis.cross(vector).scale(sine))
                .add(axis.scale(axis.dot(vector) * (1.0D - cosine)))
                .normalize();
    }

    private static ColorBand colorBand(SpiralBranch branch, int segmentIndex) {
        Layer layer = branch.layer();
        if (layer == Layer.INNER) {
            return segmentIndex < 6 ? ColorBand.PEARL : ColorBand.AURORA_AQUA;
        }
        if (layer == Layer.MIDDLE) {
            return segmentIndex < 8 ? ColorBand.AURORA_AQUA
                    : (segmentIndex < 16 ? ColorBand.DEEP_AZURE : ColorBand.VIOLET);
        }
        return segmentIndex < 8 ? ColorBand.VIOLET
                : (segmentIndex < 21 ? ColorBand.ROSE : ColorBand.SILVER);
    }

    private static float brightness(long seed, int segmentIndex) {
        return (float) (1.02D + randomUnit(seed ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.17D);
    }

    private static int totalParticles() {
        int count = 0;
        for (Layer layer : Layer.values()) {
            count += layer.particleCount();
        }
        return count;
    }

    private static int lastEmissionTick() {
        int last = 0;
        for (Layer layer : Layer.values()) {
            last = Math.max(last, layer.endTickExclusive() - 1);
        }
        return last;
    }

    private static int maximumParticlesPerEmissionTick() {
        int maximum = 0;
        for (int tick = 0; tick <= lastEmissionTick(); tick++) {
            maximum = Math.max(maximum, particlesCreatedThisTick(tick));
        }
        return maximum;
    }

    private static void validateBranchIndex(Layer layer, int branchIndex) {
        if (branchIndex < 0 || branchIndex >= layer.branchCount()) {
            throw new IllegalArgumentException("Giant spiral branch index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(Layer layer, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= layer.segmentsPerBranch()) {
            throw new IllegalArgumentException("Giant spiral segment index is outside the configured count");
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
