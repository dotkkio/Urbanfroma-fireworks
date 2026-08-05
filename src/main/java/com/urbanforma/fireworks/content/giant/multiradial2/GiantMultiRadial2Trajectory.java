package com.urbanforma.fireworks.content.giant.multiradial2;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side geometry for the fifth giant firework prototype.
 *
 * <p>The effect is one launch and one detonation. Four nested radial layers are sampled from that single
 * detonation seed and staggered only inside the same burst window; there is no second rocket, payload, or
 * explosion point. Its four-layer, twisted-shell composition is intentionally distinct from a uniform
 * multi-radial shell.</p>
 */
public final class GiantMultiRadial2Trajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.30F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** Four distinct layers share one origin and one bounded explosion interval. */
    public enum Layer {
        CORE(192, 10, 0, 34.0D, 0.92D, 0.36D, 2.5D),
        INNER_SHELL(256, 20, 3, 82.0D, 0.68D, 1.35D, 5.0D),
        OUTER_SHELL(128, 28, 9, 112.0D, 0.50D, 0.82D, 7.0D),
        LANCE_CROWN(64, 20, 14, 130.0D, 0.34D, 2.20D, 3.0D);

        private final int branchCount;
        private final int segmentsPerBranch;
        private final int startTick;
        private final double maximumRadius;
        private final double verticalScale;
        private final double twistTurns;
        private final double lift;

        Layer(
                int branchCount,
                int segmentsPerBranch,
                int startTick,
                double maximumRadius,
                double verticalScale,
                double twistTurns,
                double lift) {
            this.branchCount = branchCount;
            this.segmentsPerBranch = segmentsPerBranch;
            this.startTick = startTick;
            this.maximumRadius = maximumRadius;
            this.verticalScale = verticalScale;
            this.twistTurns = twistTurns;
            this.lift = lift;
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

        public double maximumRadius() {
            return this.maximumRadius;
        }

        public double verticalScale() {
            return this.verticalScale;
        }

        public double twistTurns() {
            return this.twistTurns;
        }

        public double lift() {
            return this.lift;
        }

        public int finalEmissionTick() {
            return this.startTick + this.segmentsPerBranch - 1;
        }

        public int particleCount() {
            return this.branchCount * this.segmentsPerBranch;
        }
    }

    public static final int LAYER_COUNT = Layer.values().length;
    public static final int TOTAL_PARTICLES = totalParticles();
    public static final int MAX_EMISSION_TICK = maximumEmissionTick();
    public static final int EMISSION_TICKS = MAX_EMISSION_TICK + 1;
    public static final int MAX_PARTICLES_PER_EMISSION_TICK = maximumParticlesPerEmissionTick();
    public static final int MIN_PARTICLE_LIFETIME = 88;
    public static final int MAX_PARTICLE_LIFETIME = 136;
    public static final int TOTAL_VISUAL_TICKS = MAX_EMISSION_TICK + MAX_PARTICLE_LIFETIME + 1;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;

    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 14;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 26;

    private static final double INITIAL_RADIUS = 2.0D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long BRANCH_SALT = 0x5E2D58D8B3A9581FL;
    private static final long AZIMUTH_SALT = 0xA35F568D90D0A4E1L;
    private static final long ELEVATION_SALT = 0x7E4E4A3F12C22C7BL;
    private static final long LIFETIME_SALT = 0xD1B54A32D192ED03L;
    private static final long BRIGHTNESS_SALT = 0x94D049BB133111EBL;
    private static final long PHASE_SALT = 0xC6BC279692B5CC83L;

    private GiantMultiRadial2Trajectory() {
    }

    /** Gold, ember, and pearl bands stay legible while avoiding a second visual effect family. */
    public enum ColorBand {
        CORE_WHITE(new Rgb(1.0F, 0.95F, 0.72F), 1.48F),
        INNER_GOLD(new Rgb(1.0F, 0.76F, 0.25F), 1.38F),
        OUTER_AMBER(new Rgb(1.0F, 0.49F, 0.08F), 1.30F),
        CROWN_PEARL(new Rgb(1.0F, 0.91F, 0.66F), 1.46F);

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
                throw new IllegalArgumentException("Invalid multi-radial ascent sample");
            }
        }
    }

    /** A branch is owned by exactly one layer, so its seed cannot describe another burst. */
    public record Branch(Layer layer, int index, long seed, double azimuth, Vec3 direction) {
        public Branch {
            if (layer == null || index < 0 || index >= layer.branchCount() || !Double.isFinite(azimuth)
                    || direction == null || direction.lengthSqr() < RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid multi-radial branch");
            }
        }
    }

    /** One spark in the single detonation window. */
    public record BranchSample(
            Layer layer,
            Branch branch,
            int segmentIndex,
            int emissionTick,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime) {
        public BranchSample {
            if (layer == null || branch == null || branch.layer() != layer
                    || segmentIndex < 0 || segmentIndex >= layer.segmentsPerBranch()
                    || emissionTick != layer.startTick() + segmentIndex
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || position == null || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                    || colorBand == null || !Float.isFinite(brightness) || brightness < 1.0F
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME) {
                throw new IllegalArgumentException("Invalid multi-radial branch sample");
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
                throw new IllegalArgumentException("Multi-radial bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Per-tick accounting belongs only to this prototype, not the ordinary or shared giant scheduler. */
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
                throw new IllegalArgumentException("Invalid multi-radial particle accounting");
            }
        }
    }

    /** Returns the exact 0-to-200 launch path; the prototype never defines a second ascent. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Multi-radial ascent tick is outside the 0-to-200 path");
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

    public static Branch branch(long payloadSeed, Layer layer, int branchIndex) {
        if (layer == null) {
            throw new IllegalArgumentException("Multi-radial layer may not be null");
        }
        if (branchIndex < 0 || branchIndex >= layer.branchCount()) {
            throw new IllegalArgumentException("Multi-radial branch index is outside the configured count");
        }
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) layer.ordinal() * 0x9E3779B97F4A7C15L)
                ^ ((long) branchIndex * 0xD1342543DE82EF95L));
        double sphereY = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / layer.branchCount();
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(payloadSeed ^ AZIMUTH_SALT ^ layer.ordinal()) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * 0.05D;
        double elevation = Math.asin(clamp(sphereY, -1.0D, 1.0D))
                + centered(branchSeed, ELEVATION_SALT) * 0.03D;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(elevation), horizontal * Math.sin(azimuth)).normalize();
        return new Branch(layer, branchIndex, branchSeed, azimuth, direction);
    }

    public static BranchSample sample(long payloadSeed, Layer layer, int branchIndex, int segmentIndex) {
        return sample(branch(payloadSeed, layer, branchIndex), segmentIndex);
    }

    public static BranchSample sample(Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Multi-radial branch may not be null");
        }
        Layer layer = branch.layer();
        if (segmentIndex < 0 || segmentIndex >= layer.segmentsPerBranch()) {
            throw new IllegalArgumentException("Multi-radial segment index is outside the configured count");
        }
        double progress = (double) segmentIndex / (layer.segmentsPerBranch() - 1);
        Vec3 position = positionFor(branch, progress);
        int lifetime = randomInt(
                branch.seed(), LIFETIME_SALT + segmentIndex, lifetimeMin(layer), lifetimeMax(layer));
        float brightness = (float) (1.02D + randomUnit(branch.seed() ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.16D);
        return new BranchSample(
                layer,
                branch,
                segmentIndex,
                layer.startTick() + segmentIndex,
                progress,
                position,
                colorBand(layer, segmentIndex),
                brightness,
                lifetime);
    }

    public static ColorBand colorBand(Layer layer, int segmentIndex) {
        if (layer == null || segmentIndex < 0 || segmentIndex >= layer.segmentsPerBranch()) {
            throw new IllegalArgumentException("Invalid multi-radial color segment");
        }
        return switch (layer) {
            case CORE -> ColorBand.CORE_WHITE;
            case INNER_SHELL -> segmentIndex < 5 ? ColorBand.CORE_WHITE : ColorBand.INNER_GOLD;
            case OUTER_SHELL -> segmentIndex < 8 ? ColorBand.INNER_GOLD : ColorBand.OUTER_AMBER;
            case LANCE_CROWN -> ColorBand.CROWN_PEARL;
        };
    }

    public static boolean isCoreSegment(BranchSample sample) {
        return sample != null && (sample.layer() == Layer.CORE
                || (sample.layer() == Layer.INNER_SHELL && sample.segmentIndex() < 5));
    }

    public static int retirementFlickerLeadTicks(BranchSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Multi-radial sample may not be null");
        }
        int spread = RETIREMENT_FLICKER_MAX_LEAD_TICKS - RETIREMENT_FLICKER_MIN_LEAD_TICKS + 1;
        return RETIREMENT_FLICKER_MIN_LEAD_TICKS + Math.floorMod(
                sample.branch().index() * 31 + sample.segmentIndex() * 17 + sample.layer().ordinal() * 13,
                spread);
    }

    public static int retirementFlickerPhase(BranchSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Multi-radial sample may not be null");
        }
        return Math.floorMod(sample.branch().index() * 11 + sample.segmentIndex() * 23
                + sample.layer().ordinal() * 7, 2);
    }

    /** Every layer is part of the same first burst window; no tick can allocate a secondary burst. */
    public static int particlesCreatedThisTick(int tick) {
        if (tick < 0 || tick > MAX_EMISSION_TICK) {
            return 0;
        }
        int count = 0;
        for (Layer layer : Layer.values()) {
            if (tick >= layer.startTick() && tick <= layer.finalEmissionTick()) {
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

    /** Conservative maximum: an emitted spark may live until its configured maximum lifetime. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int count = 0;
        int firstLiveEmission = tick - MAX_PARTICLE_LIFETIME;
        for (Layer layer : Layer.values()) {
            int first = Math.max(layer.startTick(), firstLiveEmission);
            int last = Math.min(tick, layer.finalEmissionTick());
            if (first <= last) {
                count += (last - first + 1) * layer.branchCount();
            }
        }
        return count;
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Multi-radial particle budget tick may not be negative");
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

    public static boolean hasSingleDetonationPlan() {
        return LAYER_COUNT == 4
                && TOTAL_PARTICLES == 11_904
                && particlesCreatedThroughTick(MAX_EMISSION_TICK) == TOTAL_PARTICLES
                && particlesCreatedThisTick(MAX_EMISSION_TICK + 1) == 0
                && particlesCreatedThisTick(MAX_EMISSION_TICK + MAX_PARTICLE_LIFETIME) == 0;
    }

    public static boolean hasNoSecondaryBurst() {
        return hasSingleDetonationPlan() && maximumAliveParticleUpperBound() == TOTAL_PARTICLES;
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator accumulator = new BoundsAccumulator();
        for (Layer layer : Layer.values()) {
            for (int branchIndex = 0; branchIndex < layer.branchCount(); branchIndex++) {
                Branch branch = branch(payloadSeed, layer, branchIndex);
                for (int segment = 0; segment < layer.segmentsPerBranch(); segment++) {
                    accumulator.include(sample(branch, segment).position());
                }
            }
        }
        return accumulator.toBounds();
    }

    /** Static proof used before shared integration. It covers determinism, the exact flight path, radius, and cap. */
    public static boolean staticContractHolds(long payloadSeed) {
        if (!ascentFitsDeclaredHeight() || !hasNoSecondaryBurst()
                || MAX_PARTICLES_PER_EMISSION_TICK != 576
                || conservativeBounds(payloadSeed).maxDistance() > MAX_RADIUS + RADIUS_EPSILON) {
            return false;
        }
        for (Layer layer : Layer.values()) {
            for (int branchIndex = 0; branchIndex < layer.branchCount(); branchIndex++) {
                Branch first = branch(payloadSeed, layer, branchIndex);
                Branch second = branch(payloadSeed, layer, branchIndex);
                if (!first.equals(second)) {
                    return false;
                }
                for (int segment = 0; segment < layer.segmentsPerBranch(); segment++) {
                    BranchSample sample = sample(first, segment);
                    if (sample.emissionTick() < 0 || sample.emissionTick() > MAX_EMISSION_TICK
                            || sample.position().lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                        return false;
                    }
                }
            }
        }
        return particlePlanAtTick(MAX_EMISSION_TICK).cumulativeCreated() == TOTAL_PARTICLES
                && particlePlanAtTick(MAX_EMISSION_TICK).activeUpperBound() <= PROTOTYPE_MAX_ALIVE_PARTICLES;
    }

    private static Vec3 positionFor(Branch branch, double progress) {
        Layer layer = branch.layer();
        double baseRadius = INITIAL_RADIUS + (layer.maximumRadius() - INITIAL_RADIUS) * smoothStep(progress);
        double phase = randomUnit(branch.seed() ^ PHASE_SALT) * TWO_PI;
        double twist = Math.sin(progress * Math.PI) * layer.twistTurns() * 0.23D
                + Math.sin(progress * TWO_PI + phase) * 0.07D;
        double azimuth = branch.azimuth() + twist;
        double horizontalLength = Math.sqrt(branch.direction().x * branch.direction().x
                + branch.direction().z * branch.direction().z);
        double horizontal = baseRadius * horizontalLength;
        double vertical = branch.direction().y * baseRadius * layer.verticalScale()
                + layer.lift() * Math.sin(Math.PI * progress)
                - baseRadius * 0.08D * smoothStep((progress - 0.76D) / 0.24D);
        Vec3 raw = new Vec3(horizontal * Math.cos(azimuth), vertical, horizontal * Math.sin(azimuth));
        return radiusSafe(raw, layer.maximumRadius());
    }

    private static int lifetimeMin(Layer layer) {
        return switch (layer) {
            case CORE -> 88;
            case INNER_SHELL -> 96;
            case OUTER_SHELL -> 108;
            case LANCE_CROWN -> 118;
        };
    }

    private static int lifetimeMax(Layer layer) {
        return switch (layer) {
            case CORE -> 104;
            case INNER_SHELL -> 118;
            case OUTER_SHELL -> 128;
            case LANCE_CROWN -> MAX_PARTICLE_LIFETIME;
        };
    }

    private static int totalParticles() {
        int count = 0;
        for (Layer layer : Layer.values()) {
            count += layer.particleCount();
        }
        return count;
    }

    private static int maximumEmissionTick() {
        int maximum = 0;
        for (Layer layer : Layer.values()) {
            maximum = Math.max(maximum, layer.finalEmissionTick());
        }
        return maximum;
    }

    private static int maximumParticlesPerEmissionTick() {
        int maximum = 0;
        for (int tick = 0; tick <= maximumEmissionTick(); tick++) {
            maximum = Math.max(maximum, particlesCreatedAtTickUnbounded(tick));
        }
        return maximum;
    }

    private static int particlesCreatedAtTickUnbounded(int tick) {
        int count = 0;
        for (Layer layer : Layer.values()) {
            if (tick >= layer.startTick() && tick <= layer.finalEmissionTick()) {
                count += layer.branchCount();
            }
        }
        return count;
    }

    private static Vec3 radiusSafe(Vec3 value, double layerMaximumRadius) {
        double limit = Math.min(MAX_RADIUS, layerMaximumRadius);
        double lengthSqr = value.lengthSqr();
        double limitSqr = limit * limit;
        return lengthSqr <= limitSqr ? value : value.scale(limit / Math.sqrt(lengthSqr));
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
