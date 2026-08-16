package com.urbanforma.fireworks.client.small;

import com.urbanforma.fireworks.content.small.SmallFireworkCatalog;
import com.urbanforma.fireworks.content.small.SmallFireworkDefinition;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

/**
 * Caller-driven, deterministic client program for the small layered-sphere prototype.
 *
 * <p>It owns one finite burst instance only. The class registers no event listener, static queue, scheduler,
 * payload, server particle loop, terrain query, or server trajectory.</p>
 */
public final class SmallLayeredSphereClientProgram {
    public static final String EFFECT_ID = "small_layered_sphere_firework";
    public static final int BRANCH_COUNT = 56;
    public static final int SEGMENTS_PER_BRANCH = 11;
    public static final int CORE_SEGMENT_COUNT = 3;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    public static final int MIN_PARTICLE_LIFETIME = 18;
    public static final int MAX_PARTICLE_LIFETIME = 42;
    public static final int TOTAL_VISUAL_TICKS = EMISSION_TICKS + MAX_PARTICLE_LIFETIME;
    public static final int LOCAL_TICK_BUDGET = 64;
    public static final int LOCAL_OWNED_PARTICLE_BUDGET = 640;
    public static final double MAX_RADIUS = 8.0D;
    public static final double FULL_ENVELOPE_BLOCKS = MAX_RADIUS * 2.0D;
    public static final double CORE_OUTER_RADIUS = 2.8D;
    public static final double SHELL_INNER_RADIUS = 4.2D;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double EPSILON = 1.0E-9D;
    private static final long BRANCH_SALT = 0x6B5FCA7A1C7E3D11L;
    private static final long PHASE_SALT = 0xB8A3D12C4E6F9071L;
    private static final long REACH_SALT = 0x11F0D3A74BC82965L;
    private static final long PETAL_SALT = 0xA217C8F35D4E901BL;
    private static final long LIFETIME_SALT = 0xE1C4A6D2937B5F08L;
    private static final long BRILLIANCE_SALT = 0x0C9D5E71A3B4F620L;
    private static final long TWINKLE_SALT = 0xF23489AB67CD102EL;

    static {
        if (TOTAL_PARTICLES != 616
                || PARTICLES_PER_EMISSION_TICK > LOCAL_TICK_BUDGET
                || TOTAL_PARTICLES > LOCAL_OWNED_PARTICLE_BUDGET
                || CORE_SEGMENT_COUNT >= SEGMENTS_PER_BRANCH
                || SHELL_INNER_RADIUS <= CORE_OUTER_RADIUS
                || Math.abs(FULL_ENVELOPE_BLOCKS - 16.0D) > EPSILON) {
            throw new IllegalStateException("Small layered-sphere local contract drifted");
        }
    }

    private final Request request;
    private final Branch[] branches;
    private int age;
    private int nextSegment;
    private int requestedParticles;
    private int createdParticles;

    public SmallLayeredSphereClientProgram(Request request) {
        this.request = Objects.requireNonNull(request, "request");
        verifyCatalogContract();
        this.branches = new Branch[BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = branch(this.request.seed(), index);
        }
    }

    /** Emits at most one fixed 56-particle ring and reports whether this finite program has completed. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft == null ? null : minecraft.level;
        if (level == null) {
            return false;
        }
        if (this.age >= TOTAL_VISUAL_TICKS) {
            return true;
        }
        if (this.nextSegment < EMISSION_TICKS) {
            emitRing(minecraft, this.nextSegment);
            this.nextSegment++;
        }
        this.age++;
        return this.age >= TOTAL_VISUAL_TICKS;
    }

    public Request request() {
        return this.request;
    }

    public int age() {
        return this.age;
    }

    public int emittedSegments() {
        return this.nextSegment;
    }

    /** Requested is deterministic even if a client reload temporarily rejects an individual particle allocation. */
    public int requestedParticleCount() {
        return this.requestedParticles;
    }

    public int createdParticleCount() {
        return this.createdParticles;
    }

    public int plannedParticleCount() {
        return TOTAL_PARTICLES;
    }

    public boolean isEmitting() {
        return this.nextSegment < EMISSION_TICKS;
    }

    public static int scheduledParticleCountAtTick(int tick) {
        return tick >= 0 && tick < EMISSION_TICKS ? PARTICLES_PER_EMISSION_TICK : 0;
    }

    public static Bounds conservativeBounds() {
        return new Bounds(-MAX_RADIUS, -MAX_RADIUS, -MAX_RADIUS, MAX_RADIUS, MAX_RADIUS, MAX_RADIUS);
    }

    public static boolean hasVisibleCoreShellGap() {
        return SHELL_INNER_RADIUS > CORE_OUTER_RADIUS;
    }

    private void emitRing(Minecraft minecraft, int segmentIndex) {
        for (Branch branch : this.branches) {
            Sample sample = sample(branch, segmentIndex);
            Point position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
            this.requestedParticles++;
            Particle spark = SmallSparkAppearance.create(minecraft, position.x(), position.y(), position.z());
            if (spark == null) {
                continue;
            }
            Rgb color = sample.colorBand().rgb();
            SmallSparkAppearance.apply(
                    spark,
                    color.red(),
                    color.green(),
                    color.blue(),
                    sample.brilliance(),
                    sample.colorBand().baseScale(),
                    sample.lifetime(),
                    sample.colorBand() == ColorBand.CORE,
                    sample.twinkles());
            this.createdParticles++;
        }
        if (this.requestedParticles > TOTAL_PARTICLES || this.createdParticles > LOCAL_OWNED_PARTICLE_BUDGET) {
            throw new IllegalStateException("Small layered sphere exceeded its instance-owned budget");
        }
    }

    private static void verifyCatalogContract() {
        SmallFireworkDefinition definition = SmallFireworkCatalog.byId(EFFECT_ID);
        if (definition == null
                || definition.effectType() != SmallFireworkDefinition.EffectType.LAYERED_SPHERE
                || !definition.clientEffectPath().clientProgramClass().equals(
                        SmallLayeredSphereClientProgram.class.getName())) {
            throw new IllegalStateException("Small layered-sphere catalog mapping is missing");
        }
        SmallFireworkDefinition.LocalParticlePlan plan = definition.clientEffectPath().particlePlan();
        if (plan.totalParticles() != TOTAL_PARTICLES
                || plan.peakParticlesPerTick() != PARTICLES_PER_EMISSION_TICK
                || plan.emissionTicks() != EMISSION_TICKS
                || plan.minLifetimeTicks() != MIN_PARTICLE_LIFETIME
                || plan.maxLifetimeTicks() != MAX_PARTICLE_LIFETIME
                || plan.localTickBudget() != LOCAL_TICK_BUDGET
                || plan.localOwnedParticleBudget() != LOCAL_OWNED_PARTICLE_BUDGET
                || Math.abs(plan.maxRadius() - MAX_RADIUS) > EPSILON
                || Math.abs(plan.fullEnvelopeBlocks() - FULL_ENVELOPE_BLOCKS) > EPSILON) {
            throw new IllegalStateException("Small layered-sphere program constants drifted from the catalog");
        }
    }

    private static Branch branch(long payloadSeed, int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Small layered-sphere branch index is outside the configured count");
        }
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double latitude = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / BRANCH_COUNT;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - latitude * latitude));
        double azimuth = branchIndex * GOLDEN_ANGLE
                + unit(payloadSeed ^ PHASE_SALT) * TWO_PI
                + centered(branchSeed ^ PHASE_SALT) * 0.12D;
        Point direction = new Point(
                horizontal * Math.cos(azimuth),
                latitude,
                horizontal * Math.sin(azimuth)).normalized();
        return new Branch(
                branchIndex,
                branchSeed,
                direction,
                horizontalSide(direction),
                MAX_RADIUS * (0.91D + unit(branchSeed ^ REACH_SALT) * 0.06D),
                unit(branchSeed ^ PETAL_SALT) * TWO_PI);
    }

    private static Sample sample(Branch branch, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Small layered-sphere segment index is outside the configured count");
        }
        boolean core = segmentIndex < CORE_SEGMENT_COUNT;
        double radius;
        double petalOffset;
        if (core) {
            double progress = (double) segmentIndex / (CORE_SEGMENT_COUNT - 1);
            radius = 0.9D + (CORE_OUTER_RADIUS - 0.9D) * progress;
            petalOffset = 0.0D;
        } else {
            double shellProgress = (double) (segmentIndex - CORE_SEGMENT_COUNT)
                    / (SEGMENTS_PER_BRANCH - CORE_SEGMENT_COUNT - 1);
            radius = SHELL_INNER_RADIUS
                    + (branch.shellReach() - SHELL_INNER_RADIUS) * smoothStep(shellProgress);
            petalOffset = 0.34D
                    * Math.sin(branch.petalPhase() + TWO_PI * 2.0D * shellProgress)
                    * Math.sin(Math.PI * shellProgress);
        }
        Point raw = branch.direction().scale(radius)
                .add(branch.sideDirection().scale(petalOffset))
                .add(0.0D, Math.sin(branch.petalPhase() + segmentIndex) * 0.10D * (core ? 0.4D : 1.0D), 0.0D);
        ColorBand colorBand = core
                ? ColorBand.CORE
                : segmentIndex >= SEGMENTS_PER_BRANCH - 2 ? ColorBand.EDGE : ColorBand.SHELL;
        int lifetime = lifetime(branch.seed(), segmentIndex, colorBand);
        float brilliance = (float) (1.00D + unit(branch.seed() ^ (BRILLIANCE_SALT + segmentIndex)) * 0.16D);
        boolean twinkles = colorBand == ColorBand.EDGE
                && unit(branch.seed() ^ (TWINKLE_SALT + segmentIndex)) < 0.55D;
        return new Sample(
                segmentIndex,
                raw.clampedTo(MAX_RADIUS),
                colorBand,
                brilliance,
                lifetime,
                twinkles);
    }

    private static int lifetime(long seed, int segmentIndex, ColorBand colorBand) {
        return switch (colorBand) {
            case CORE -> randomInt(seed, LIFETIME_SALT + segmentIndex, 18, 24);
            case SHELL -> randomInt(seed, LIFETIME_SALT + segmentIndex, 28, 42);
            case EDGE -> randomInt(seed, LIFETIME_SALT + segmentIndex, 24, 34);
        };
    }

    private static Point horizontalSide(Point direction) {
        double horizontalLength = Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z());
        return horizontalLength < EPSILON
                ? new Point(1.0D, 0.0D, 0.0D)
                : new Point(-direction.z() / horizontalLength, 0.0D, direction.x() / horizontalLength);
    }

    private static double smoothStep(double value) {
        double bounded = Math.max(0.0D, Math.min(1.0D, value));
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static int randomInt(long seed, long salt, int min, int max) {
        return min + (int) Math.floor(unit(seed ^ salt) * (max - min + 1));
    }

    private static double centered(long value) {
        return unit(value) - 0.5D;
    }

    private static double unit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    public record Request(double x, double y, double z, long seed) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Small layered-sphere request position must be finite");
            }
        }
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX)
                    || !Double.isFinite(minY)
                    || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX)
                    || !Double.isFinite(maxY)
                    || !Double.isFinite(maxZ)
                    || minX > maxX
                    || minY > maxY
                    || minZ > maxZ) {
                throw new IllegalArgumentException("Small layered-sphere bounds must be finite and ordered");
            }
        }

        public double maxSpan() {
            return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        }

        public boolean fitsEnvelope(double envelope) {
            return envelope > 0.0D && maxSpan() <= envelope + EPSILON;
        }
    }

    private record Rgb(float red, float green, float blue) {
    }

    private enum ColorBand {
        CORE(new Rgb(0.941F, 0.988F, 1.000F), 1.10F),
        SHELL(new Rgb(0.180F, 0.663F, 1.000F), 1.00F),
        EDGE(new Rgb(0.498F, 0.910F, 1.000F), 0.96F);

        private final Rgb rgb;
        private final float baseScale;

        ColorBand(Rgb rgb, float baseScale) {
            this.rgb = rgb;
            this.baseScale = baseScale;
        }

        public Rgb rgb() {
            return this.rgb;
        }

        public float baseScale() {
            return this.baseScale;
        }
    }

    private record Branch(
            int index,
            long seed,
            Point direction,
            Point sideDirection,
            double shellReach,
            double petalPhase) {
    }

    private record Sample(
            int segmentIndex,
            Point position,
            ColorBand colorBand,
            float brilliance,
            int lifetime,
            boolean twinkles) {
        private Sample {
            if (segmentIndex < 0
                    || segmentIndex >= SEGMENTS_PER_BRANCH
                    || position == null
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + EPSILON
                    || colorBand == null
                    || !Float.isFinite(brilliance)
                    || brilliance <= 0.0F
                    || lifetime < MIN_PARTICLE_LIFETIME
                    || lifetime > MAX_PARTICLE_LIFETIME) {
                throw new IllegalArgumentException("Invalid small layered-sphere sample");
            }
        }
    }

    private record Point(double x, double y, double z) {
        private Point normalized() {
            double length = Math.sqrt(lengthSqr());
            if (!Double.isFinite(length) || length < EPSILON) {
                throw new IllegalArgumentException("Small layered-sphere direction must be finite and non-zero");
            }
            return scale(1.0D / length);
        }

        private Point add(Point other) {
            return add(other.x, other.y, other.z);
        }

        private Point add(double addX, double addY, double addZ) {
            return new Point(x + addX, y + addY, z + addZ);
        }

        private Point scale(double scalar) {
            return new Point(x * scalar, y * scalar, z * scalar);
        }

        private Point clampedTo(double maxLength) {
            double lengthSqr = lengthSqr();
            double maxLengthSqr = maxLength * maxLength;
            return lengthSqr <= maxLengthSqr ? this : scale(maxLength / Math.sqrt(lengthSqr));
        }

        private double lengthSqr() {
            return x * x + y * y + z * z;
        }
    }
}
