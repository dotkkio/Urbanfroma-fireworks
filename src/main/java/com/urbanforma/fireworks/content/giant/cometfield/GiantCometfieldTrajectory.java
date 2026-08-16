package com.urbanforma.fireworks.content.giant.cometfield;

import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side contract for a broad, interlaced giant comet field.
 *
 * <p>One origin produces a nucleus and three curved current families inside one bounded detonation window. Each
 * comet is a preplanned trace through a three-dimensional volume; no trace starts a new entity, payload, or
 * explosion location.</p>
 */
public final class GiantCometfieldTrajectory {
    public static final String STABLE_ID = "giant_interlaced_cometfield_firework";
    public static final String ENGLISH_NAME = "Giant Interlaced Comet Field Firework";
    public static final String CHINESE_NAME = "巨型交错彗星场烟花";
    /** Name for the integrator to add to the shared enum; this isolated source does not modify that enum. */
    public static final String SUGGESTED_TIER_ID = "INTERLACED_COMET_FIELD";

    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 129.0D;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.32F;
    public static final double DETONATION_SOUND_BROADCAST_RADIUS = DETONATION_SOUND_VOLUME * 16.0D;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    /** Each family has a separate spatial curve and timing, giving the field depth without straight spokes. */
    public enum Current {
        CROSSWIND(44, 46, 0, 6, 106.0D, 15.0D, 7.0D, ColorBand.CROSSWIND_CYAN),
        ORBITAL_WEAVE(48, 48, 4, 7, 112.0D, 13.0D, 11.0D, ColorBand.ORBITAL_VIOLET),
        RECURVE(34, 54, 9, 6, 118.0D, 17.0D, 9.0D, ColorBand.RECURVE_APRICOT);

        private final int cometCount;
        private final int segmentsPerComet;
        private final int startTick;
        private final int startStaggerTicks;
        private final double maximumEndpointRadius;
        private final double primaryCurveRadius;
        private final double secondaryCurveRadius;
        private final ColorBand colorBand;

        Current(
                int cometCount,
                int segmentsPerComet,
                int startTick,
                int startStaggerTicks,
                double maximumEndpointRadius,
                double primaryCurveRadius,
                double secondaryCurveRadius,
                ColorBand colorBand) {
            this.cometCount = cometCount;
            this.segmentsPerComet = segmentsPerComet;
            this.startTick = startTick;
            this.startStaggerTicks = startStaggerTicks;
            this.maximumEndpointRadius = maximumEndpointRadius;
            this.primaryCurveRadius = primaryCurveRadius;
            this.secondaryCurveRadius = secondaryCurveRadius;
            this.colorBand = colorBand;
        }

        public int cometCount() {
            return this.cometCount;
        }

        public int segmentsPerComet() {
            return this.segmentsPerComet;
        }

        public int startTick() {
            return this.startTick;
        }

        public int startStaggerTicks() {
            return this.startStaggerTicks;
        }

        public double maximumEndpointRadius() {
            return this.maximumEndpointRadius;
        }

        public double primaryCurveRadius() {
            return this.primaryCurveRadius;
        }

        public double secondaryCurveRadius() {
            return this.secondaryCurveRadius;
        }

        public ColorBand colorBand() {
            return this.colorBand;
        }

        public int particleCount() {
            return this.cometCount * this.segmentsPerComet;
        }
    }

    public enum ColorBand {
        NUCLEUS_PEARL(new Rgb(1.0F, 0.96F, 0.80F), 1.48F),
        CROSSWIND_CYAN(new Rgb(0.18F, 0.82F, 1.0F), 1.32F),
        ORBITAL_VIOLET(new Rgb(0.67F, 0.30F, 1.0F), 1.34F),
        RECURVE_APRICOT(new Rgb(1.0F, 0.43F, 0.18F), 1.36F);

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

    private static final Current[] CURRENTS = Current.values();
    public static final int CURRENT_COUNT = CURRENTS.length;
    public static final int CORE_PARTICLE_COUNT = 144;
    public static final int CORE_EMISSION_TICKS = 6;
    public static final int CORE_PARTICLES_PER_TICK = CORE_PARTICLE_COUNT / CORE_EMISSION_TICKS;
    public static final int TOTAL_COMET_PARTICLES = totalCometParticles();
    public static final int TOTAL_PARTICLES = CORE_PARTICLE_COUNT + TOTAL_COMET_PARTICLES;
    public static final int MAX_EMISSION_TICK = maximumEmissionTick();
    public static final int EMISSION_TICKS = MAX_EMISSION_TICK + 1;
    public static final int MAX_PARTICLES_PER_EMISSION_TICK = maximumParticlesPerEmissionTick();
    public static final int MIN_PARTICLE_LIFETIME = 102;
    public static final int MAX_PARTICLE_LIFETIME = 164;
    public static final int TOTAL_VISUAL_TICKS = MAX_EMISSION_TICK + MAX_PARTICLE_LIFETIME + 1;
    /** Local per-program allocation ceiling. It is independent of any shared scheduler allowance. */
    public static final int LOCAL_PARTICLE_LIMIT = TOTAL_PARTICLES;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    public static final int MAX_TRACKED_RETIREMENT_SPARKS = 1_408;
    public static final int RETIREMENT_FLICKER_MIN_LEAD_TICKS = 14;
    public static final int RETIREMENT_FLICKER_MAX_LEAD_TICKS = 28;

    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final double RADIUS_LIMIT = MAX_RADIUS - 0.25D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long CORE_SALT = 0xA8F4B2537491DCE1L;
    private static final long COMET_SALT = 0x1C69B3E7D42F850AL;
    private static final long ENDPOINT_SALT = 0xF1357AEA2E62A9C5L;
    private static final long ORIGIN_SALT = 0x6E5D4C3B2A190817L;
    private static final long PHASE_SALT = 0x0F1E2D3C4B5A6978L;
    private static final long LIFETIME_SALT = 0xC6BC279692B5CC83L;
    private static final long BRILLIANCE_SALT = 0xD1B54A32D192ED03L;
    private static final long FLICKER_SALT = 0x94D049BB133111EBL;

    private GiantCometfieldTrajectory() {
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
                throw new IllegalArgumentException("Invalid comet-field ascent sample");
            }
        }
    }

    /** A 3D curved trace with a local frame, preplanned from the one payload seed. */
    public record Comet(
            Current current,
            int index,
            long seed,
            int emissionStartTick,
            Vec3 origin,
            Vec3 endpointDirection,
            Vec3 lateralAxis,
            Vec3 verticalAxis,
            double endpointDistance,
            double phase) {
        public Comet {
            if (current == null || index < 0 || index >= current.cometCount() || emissionStartTick < current.startTick()
                    || emissionStartTick >= current.startTick() + current.startStaggerTicks() || origin == null
                    || endpointDirection == null || lateralAxis == null || verticalAxis == null
                    || !Double.isFinite(endpointDistance) || !Double.isFinite(phase)
                    || endpointDistance <= 0.0D || endpointDistance > current.maximumEndpointRadius()
                    || origin.lengthSqr() > MAX_RADIUS * MAX_RADIUS || !finiteUnit(endpointDirection)
                    || !finiteUnit(lateralAxis) || !finiteUnit(verticalAxis)
                    || Math.abs(endpointDirection.dot(lateralAxis)) > RADIUS_EPSILON
                    || Math.abs(endpointDirection.dot(verticalAxis)) > RADIUS_EPSILON
                    || Math.abs(lateralAxis.dot(verticalAxis)) > RADIUS_EPSILON) {
                throw new IllegalArgumentException("Invalid interlaced comet");
            }
        }
    }

    public record CoreSample(int index, Vec3 position, int lifetime, float brilliance) {
        public CoreSample {
            if (index < 0 || index >= CORE_PARTICLE_COUNT || position == null
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME
                    || !Float.isFinite(brilliance) || brilliance < 1.0F || brilliance > 1.24F) {
                throw new IllegalArgumentException("Invalid comet-field nucleus sample");
            }
        }
    }

    public record CometSample(
            Comet comet,
            int segmentIndex,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            int lifetime,
            float brilliance) {
        public CometSample {
            if (comet == null || segmentIndex < 0 || segmentIndex >= comet.current().segmentsPerComet()
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D || position == null
                    || colorBand == null || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON
                    || lifetime < MIN_PARTICLE_LIFETIME || lifetime > MAX_PARTICLE_LIFETIME
                    || !Float.isFinite(brilliance) || brilliance < 1.0F || brilliance > 1.24F) {
                throw new IllegalArgumentException("Invalid comet-field trace sample");
            }
        }
    }

    /** Deterministic retirement timing for a fixed subset of existing field sparks. */
    public record RetirementFlicker(int startAge, int cadencePhase) {
        public RetirementFlicker {
            if (startAge < 0 || cadencePhase < 0 || cadencePhase > 3) {
                throw new IllegalArgumentException("Invalid comet-field retirement flicker");
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
                throw new IllegalArgumentException("Invalid comet-field particle accounting");
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
                throw new IllegalArgumentException("Comet-field bounds must be finite and ordered");
            }
        }

        public boolean fitsRadius(double radius) {
            return radius >= 0.0D && this.maxDistance <= radius + RADIUS_EPSILON;
        }
    }

    /** Returns the declared common 0-to-200 ascent path. Client programs only render the detonation payload. */
    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Comet-field ascent tick is outside the 0-to-200 path");
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

    public static CoreSample coreSample(long payloadSeed, int index) {
        if (index < 0 || index >= CORE_PARTICLE_COUNT) {
            throw new IllegalArgumentException("Comet-field nucleus index is outside the configured count");
        }
        long token = mix64(payloadSeed ^ CORE_SALT ^ ((long) index * 0x9E3779B97F4A7C15L));
        Vec3 direction = fibonacciDirection(index, CORE_PARTICLE_COUNT, payloadSeed ^ CORE_SALT);
        double radius = 1.5D + randomUnit(token ^ ORIGIN_SALT) * 18.5D;
        int lifetime = randomInt(token, LIFETIME_SALT, MIN_PARTICLE_LIFETIME, MAX_PARTICLE_LIFETIME);
        float brilliance = (float) (1.03D + randomUnit(token ^ BRILLIANCE_SALT) * 0.19D);
        return new CoreSample(index, direction.scale(radius), lifetime, brilliance);
    }

    public static Comet comet(long payloadSeed, Current current, int cometIndex) {
        if (current == null) {
            throw new IllegalArgumentException("Comet-field current is required");
        }
        validateCometIndex(current, cometIndex);
        long seed = mix64(payloadSeed ^ COMET_SALT ^ ((long) current.ordinal() << 48)
                ^ ((long) cometIndex * 0x9E3779B97F4A7C15L));
        Vec3 endpointDirection = fibonacciDirection(cometIndex, current.cometCount(), payloadSeed ^ ENDPOINT_SALT ^ current.ordinal());
        Vec3 lateralAxis = perpendicular(endpointDirection);
        Vec3 verticalAxis = endpointDirection.cross(lateralAxis).normalize();
        double originDistance = 6.0D + randomUnit(seed ^ ORIGIN_SALT) * 18.0D;
        Vec3 originDirection = endpointDirection.scale(0.34D + randomUnit(seed ^ PHASE_SALT) * 0.20D)
                .add(lateralAxis.scale(centered(seed, ORIGIN_SALT) * 1.20D))
                .add(verticalAxis.scale(centered(seed, ENDPOINT_SALT) * 1.20D))
                .normalize();
        double endpointDistance = current.maximumEndpointRadius() - randomUnit(seed ^ ENDPOINT_SALT) * 14.0D;
        return new Comet(
                current,
                cometIndex,
                seed,
                emissionStartTick(current, cometIndex),
                originDirection.scale(originDistance),
                endpointDirection,
                lateralAxis,
                verticalAxis,
                endpointDistance,
                randomUnit(seed ^ PHASE_SALT) * TWO_PI);
    }

    public static CometSample sample(long payloadSeed, Current current, int cometIndex, int segmentIndex) {
        return sample(comet(payloadSeed, current, cometIndex), segmentIndex);
    }

    public static CometSample sample(Comet comet, int segmentIndex) {
        if (comet == null) {
            throw new IllegalArgumentException("Comet-field comet is required");
        }
        validateSegmentIndex(comet.current(), segmentIndex);
        double progress = (double) segmentIndex / (comet.current().segmentsPerComet() - 1);
        double eased = smoothStep(progress);
        Vec3 chord = comet.origin().scale(1.0D - eased).add(comet.endpointDirection().scale(comet.endpointDistance() * eased));
        double bell = Math.sin(Math.PI * progress);
        Vec3 curve = curveOffset(comet, progress, bell);
        int lifetime = randomInt(
                comet.seed(),
                LIFETIME_SALT + ((long) segmentIndex << 7),
                MIN_PARTICLE_LIFETIME,
                MAX_PARTICLE_LIFETIME);
        float brilliance = (float) (1.01D + randomUnit(
                comet.seed() ^ BRILLIANCE_SALT ^ ((long) segmentIndex << 20)) * 0.22D);
        return new CometSample(
                comet,
                segmentIndex,
                progress,
                radiusSafe(chord.add(curve)),
                comet.current().colorBand(),
                lifetime,
                brilliance);
    }

    /** The selected retirement state belongs to an existing spark and never allocates a follow-up population. */
    public static RetirementFlicker retirementFlicker(CometSample sample) {
        if (!tracksRetirement(sample)) {
            throw new IllegalArgumentException("Only selected comet-field sparks have a retirement window");
        }
        int lead = randomInt(
                sample.comet().seed(),
                FLICKER_SALT + ((long) sample.segmentIndex() << 9),
                RETIREMENT_FLICKER_MIN_LEAD_TICKS,
                RETIREMENT_FLICKER_MAX_LEAD_TICKS);
        int phase = Math.floorMod((int) (mix64(sample.comet().seed() ^ FLICKER_SALT ^ sample.segmentIndex()) >>> 37), 4);
        return new RetirementFlicker(Math.max(0, sample.lifetime() - lead), phase);
    }

    public static RetirementFlicker retirementFlicker(CoreSample sample) {
        if (!tracksRetirement(sample)) {
            throw new IllegalArgumentException("Only selected comet-field nucleus sparks have a retirement window");
        }
        long token = mix64(((long) sample.index() << 32) ^ FLICKER_SALT);
        int lead = randomInt(token, FLICKER_SALT, RETIREMENT_FLICKER_MIN_LEAD_TICKS, RETIREMENT_FLICKER_MAX_LEAD_TICKS);
        int phase = Math.floorMod((int) (token >>> 37), 4);
        return new RetirementFlicker(Math.max(0, sample.lifetime() - lead), phase);
    }

    public static boolean tracksRetirement(CoreSample sample) {
        return sample != null && sample.index() % 4 == 0;
    }

    public static boolean tracksRetirement(CometSample sample) {
        return sample != null && Math.floorMod(sample.comet().index() + sample.segmentIndex() * 2, 5) == 0;
    }

    public static int particlesCreatedThisTick(int tick) {
        if (tick < 0 || tick > MAX_EMISSION_TICK) {
            return 0;
        }
        int created = tick < CORE_EMISSION_TICKS ? CORE_PARTICLES_PER_TICK : 0;
        for (Current current : CURRENTS) {
            for (int cometIndex = 0; cometIndex < current.cometCount(); cometIndex++) {
                int segmentIndex = tick - emissionStartTick(current, cometIndex);
                if (segmentIndex >= 0 && segmentIndex < current.segmentsPerComet()) {
                    created++;
                }
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
            throw new IllegalArgumentException("Comet-field particle budget tick may not be negative");
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

    /** A broad curved field is never accepted as a set of straight radial segments. */
    public static boolean hasNonLinearCometPaths(long payloadSeed) {
        for (Current current : CURRENTS) {
            for (int cometIndex = 0; cometIndex < current.cometCount(); cometIndex++) {
                Comet comet = comet(payloadSeed, current, cometIndex);
                boolean curved = false;
                for (int segmentIndex = 1; segmentIndex < current.segmentsPerComet() - 1; segmentIndex++) {
                    CometSample sample = sample(comet, segmentIndex);
                    double eased = smoothStep(sample.progress());
                    Vec3 straightPosition = comet.origin().scale(1.0D - eased)
                            .add(comet.endpointDirection().scale(comet.endpointDistance() * eased));
                    if (sample.position().subtract(straightPosition).lengthSqr() > 0.01D) {
                        curved = true;
                        break;
                    }
                }
                if (!curved) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean hasSingleOriginBurstPlan() {
        return CURRENT_COUNT == 3
                && CORE_PARTICLE_COUNT == CORE_EMISSION_TICKS * CORE_PARTICLES_PER_TICK
                && MAX_EMISSION_TICK < 80
                && particlesCreatedThroughTick(MAX_EMISSION_TICK) == TOTAL_PARTICLES
                && particlesCreatedThisTick(MAX_EMISSION_TICK + 1) == 0;
    }

    /** Exhaustive deterministic proof over every nucleus and comet trace sample of one payload seed. */
    public static boolean staticContractHolds(long payloadSeed) {
        int sampledParticles = 0;
        int retirementTracks = 0;
        for (int index = 0; index < CORE_PARTICLE_COUNT; index++) {
            CoreSample sample = coreSample(payloadSeed, index);
            if (sample.position().lengthSqr() > MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON) {
                return false;
            }
            sampledParticles++;
            if (tracksRetirement(sample)) {
                retirementTracks++;
            }
        }
        for (Current current : CURRENTS) {
            for (int cometIndex = 0; cometIndex < current.cometCount(); cometIndex++) {
                Comet comet = comet(payloadSeed, current, cometIndex);
                for (int segmentIndex = 0; segmentIndex < current.segmentsPerComet(); segmentIndex++) {
                    CometSample sample = sample(comet, segmentIndex);
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
                && hasNonLinearCometPaths(payloadSeed)
                && sampledParticles == TOTAL_PARTICLES
                && particlePlanAtTick(MAX_EMISSION_TICK).cumulativeCreated() == TOTAL_PARTICLES
                && maximumAliveParticleUpperBound() <= LOCAL_PARTICLE_LIMIT
                && retirementTracks <= MAX_TRACKED_RETIREMENT_SPARKS
                && conservativeBounds(payloadSeed).fitsRadius(MAX_RADIUS);
    }

    public static Bounds conservativeBounds(long payloadSeed) {
        BoundsAccumulator accumulator = new BoundsAccumulator();
        for (int index = 0; index < CORE_PARTICLE_COUNT; index++) {
            accumulator.include(coreSample(payloadSeed, index).position());
        }
        for (Current current : CURRENTS) {
            for (int cometIndex = 0; cometIndex < current.cometCount(); cometIndex++) {
                Comet comet = comet(payloadSeed, current, cometIndex);
                for (int segmentIndex = 0; segmentIndex < current.segmentsPerComet(); segmentIndex++) {
                    accumulator.include(sample(comet, segmentIndex).position());
                }
            }
        }
        return accumulator.toBounds();
    }

    private static int totalCometParticles() {
        int total = 0;
        for (Current current : CURRENTS) {
            total += current.particleCount();
        }
        return total;
    }

    private static int maximumEmissionTick() {
        int maximum = CORE_EMISSION_TICKS - 1;
        for (Current current : CURRENTS) {
            for (int cometIndex = 0; cometIndex < current.cometCount(); cometIndex++) {
                maximum = Math.max(maximum,
                        emissionStartTick(current, cometIndex) + current.segmentsPerComet() - 1);
            }
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

    private static int emissionStartTick(Current current, int cometIndex) {
        int stagger = Math.floorMod(cometIndex * 5 + current.ordinal() * 3, current.startStaggerTicks());
        return current.startTick() + stagger;
    }

    private static Vec3 curveOffset(Comet comet, double progress, double bell) {
        Current current = comet.current();
        return switch (current) {
            case CROSSWIND -> comet.lateralAxis().scale(
                    Math.sin(comet.phase() + Math.PI * 1.35D * progress) * current.primaryCurveRadius() * bell)
                    .add(comet.verticalAxis().scale(
                            Math.cos(comet.phase() * 0.65D + TWO_PI * progress)
                                    * current.secondaryCurveRadius() * bell * 0.62D));
            case ORBITAL_WEAVE -> comet.lateralAxis().scale(
                    Math.sin(comet.phase() + TWO_PI * 1.25D * progress) * current.primaryCurveRadius() * bell)
                    .add(comet.verticalAxis().scale(
                            Math.cos(comet.phase() + TWO_PI * 1.25D * progress)
                                    * current.secondaryCurveRadius() * bell));
            case RECURVE -> comet.lateralAxis().scale(
                    (Math.sin(comet.phase() + Math.PI * 1.8D * progress) * current.primaryCurveRadius()
                            - current.primaryCurveRadius() * 0.35D * progress) * bell)
                    .add(comet.verticalAxis().scale(
                            Math.sin(comet.phase() * 0.5D + TWO_PI * 0.72D * progress)
                                    * current.secondaryCurveRadius() * bell));
        };
    }

    private static Vec3 fibonacciDirection(int index, int count, long salt) {
        double y = 1.0D - 2.0D * ((double) index + 0.5D) / count;
        double azimuth = index * GOLDEN_ANGLE + randomUnit(salt) * TWO_PI;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        return new Vec3(horizontal * Math.cos(azimuth), y, horizontal * Math.sin(azimuth)).normalize();
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

    private static void validateCometIndex(Current current, int cometIndex) {
        if (cometIndex < 0 || cometIndex >= current.cometCount()) {
            throw new IllegalArgumentException("Comet-field comet index is outside the configured count");
        }
    }

    private static void validateSegmentIndex(Current current, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= current.segmentsPerComet()) {
            throw new IllegalArgumentException("Comet-field segment index is outside the configured count");
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
