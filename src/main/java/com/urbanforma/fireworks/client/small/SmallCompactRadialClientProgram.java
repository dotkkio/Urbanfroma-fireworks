package com.urbanforma.fireworks.client.small;

import com.urbanforma.fireworks.content.small.SmallFireworkCatalog;
import com.urbanforma.fireworks.content.small.SmallFireworkDefinition;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

/**
 * Caller-driven, deterministic client program for a short and full three-dimensional radial bloom.
 *
 * <p>Every branch is seeded from one burst seed, with no line-shape shortcut, server geometry work, scheduler,
 * listener, queue, terrain query, or network registration.</p>
 */
public final class SmallCompactRadialClientProgram {
    public static final String EFFECT_ID = "small_compact_radial_firework";
    public static final int BRANCH_COUNT = 80;
    public static final int SEGMENTS_PER_BRANCH = 8;
    public static final int IGNITION_SEGMENT_COUNT = 2;
    public static final int TERMINAL_SEGMENT_START = 6;
    public static final int EMISSION_TICKS = SEGMENTS_PER_BRANCH;
    public static final int PARTICLES_PER_EMISSION_TICK = BRANCH_COUNT;
    public static final int TOTAL_PARTICLES = BRANCH_COUNT * SEGMENTS_PER_BRANCH;
    public static final int MIN_PARTICLE_LIFETIME = 16;
    public static final int MAX_PARTICLE_LIFETIME = 38;
    public static final int TOTAL_VISUAL_TICKS = EMISSION_TICKS + MAX_PARTICLE_LIFETIME;
    public static final int LOCAL_TICK_BUDGET = 96;
    public static final int LOCAL_OWNED_PARTICLE_BUDGET = 672;
    public static final double MAX_RADIUS = 9.6D;
    public static final double FULL_ENVELOPE_BLOCKS = MAX_RADIUS * 2.0D;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double EPSILON = 1.0E-9D;
    private static final long BRANCH_SALT = 0x9D1E6A7B43C2F805L;
    private static final long PHASE_SALT = 0xC4A87F162D39B0E1L;
    private static final long REACH_SALT = 0x5B2E14F8C7D963A0L;
    private static final long CURL_SALT = 0xE07A3C5912BF684DL;
    private static final long LIFETIME_SALT = 0x31C9D57E8A40F2B6L;
    private static final long BRILLIANCE_SALT = 0xF1B6A0493CDE7528L;
    private static final long TWINKLE_SALT = 0x2A7CE951B84D603FL;

    static {
        if (TOTAL_PARTICLES != 640
                || PARTICLES_PER_EMISSION_TICK > LOCAL_TICK_BUDGET
                || TOTAL_PARTICLES > LOCAL_OWNED_PARTICLE_BUDGET
                || IGNITION_SEGMENT_COUNT >= TERMINAL_SEGMENT_START
                || TERMINAL_SEGMENT_START >= SEGMENTS_PER_BRANCH
                || Math.abs(FULL_ENVELOPE_BLOCKS - 19.2D) > EPSILON) {
            throw new IllegalStateException("Small compact-radial local contract drifted");
        }
    }

    private final Request request;
    private final Branch[] branches;
    private int age;
    private int nextSegment;
    private int requestedParticles;
    private int createdParticles;

    public SmallCompactRadialClientProgram(Request request) {
        this.request = Objects.requireNonNull(request, "request");
        verifyCatalogContract();
        this.branches = new Branch[BRANCH_COUNT];
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = branch(this.request.seed(), index);
        }
    }

    /** Emits at most one fixed 80-particle three-dimensional radial ring per client tick. */
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

    /** Requested is fixed by seed and tick even if a particle allocation is unavailable during client reload. */
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

    /** Static client-side proof that the deterministic branch set spans both signs of every world axis. */
    public static boolean hasThreeDimensionalSpread(long payloadSeed) {
        boolean positiveX = false;
        boolean negativeX = false;
        boolean positiveY = false;
        boolean negativeY = false;
        boolean positiveZ = false;
        boolean negativeZ = false;
        for (int index = 0; index < BRANCH_COUNT; index++) {
            Point direction = branch(payloadSeed, index).direction();
            positiveX |= direction.x() > 0.0D;
            negativeX |= direction.x() < 0.0D;
            positiveY |= direction.y() > 0.0D;
            negativeY |= direction.y() < 0.0D;
            positiveZ |= direction.z() > 0.0D;
            negativeZ |= direction.z() < 0.0D;
        }
        return positiveX && negativeX && positiveY && negativeY && positiveZ && negativeZ;
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
                    sample.colorBand() == ColorBand.IGNITION,
                    sample.twinkles());
            this.createdParticles++;
        }
        if (this.requestedParticles > TOTAL_PARTICLES || this.createdParticles > LOCAL_OWNED_PARTICLE_BUDGET) {
            throw new IllegalStateException("Small compact radial exceeded its instance-owned budget");
        }
    }

    private static void verifyCatalogContract() {
        SmallFireworkDefinition definition = SmallFireworkCatalog.byId(EFFECT_ID);
        if (definition == null
                || definition.effectType() != SmallFireworkDefinition.EffectType.COMPACT_RADIAL
                || !definition.clientEffectPath().clientProgramClass().equals(
                        SmallCompactRadialClientProgram.class.getName())) {
            throw new IllegalStateException("Small compact-radial catalog mapping is missing");
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
            throw new IllegalStateException("Small compact-radial program constants drifted from the catalog");
        }
    }

    private static Branch branch(long payloadSeed, int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Small compact-radial branch index is outside the configured count");
        }
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double latitude = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / BRANCH_COUNT;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - latitude * latitude));
        double azimuth = branchIndex * GOLDEN_ANGLE
                + unit(payloadSeed ^ PHASE_SALT) * TWO_PI
                + centered(branchSeed ^ PHASE_SALT) * 0.16D;
        Point direction = new Point(
                horizontal * Math.cos(azimuth),
                latitude,
                horizontal * Math.sin(azimuth)).normalized();
        Point side = horizontalSide(direction);
        Point crown = direction.cross(side).normalized();
        return new Branch(
                branchIndex,
                branchSeed,
                direction,
                side,
                crown,
                MAX_RADIUS * (0.90D + unit(branchSeed ^ REACH_SALT) * 0.07D),
                unit(branchSeed ^ CURL_SALT) * TWO_PI);
    }

    private static Sample sample(Branch branch, int segmentIndex) {
        if (segmentIndex < 0 || segmentIndex >= SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Small compact-radial segment index is outside the configured count");
        }
        double progress = (double) segmentIndex / (SEGMENTS_PER_BRANCH - 1);
        boolean ignition = segmentIndex < IGNITION_SEGMENT_COUNT;
        double radius = ignition
                ? 0.8D + 1.4D * segmentIndex
                : 2.9D + (branch.reach() - 2.9D) * smoothStep(
                        (double) (segmentIndex - IGNITION_SEGMENT_COUNT)
                                / (SEGMENTS_PER_BRANCH - IGNITION_SEGMENT_COUNT - 1));
        double curlEnvelope = Math.sin(Math.PI * progress);
        double lateralCurl = 0.64D
                * Math.sin(branch.curlPhase() + TWO_PI * 1.35D * progress)
                * curlEnvelope;
        double crownCurl = 0.42D
                * Math.cos(branch.curlPhase() * 0.75D + TWO_PI * progress)
                * curlEnvelope;
        double terminalProgress = segmentIndex < TERMINAL_SEGMENT_START
                ? 0.0D
                : (double) (segmentIndex - TERMINAL_SEGMENT_START)
                        / (SEGMENTS_PER_BRANCH - TERMINAL_SEGMENT_START - 1);
        double terminalDrop = 0.78D * smoothStep(terminalProgress);
        Point raw = branch.direction().scale(radius)
                .add(branch.sideDirection().scale(lateralCurl))
                .add(branch.crownDirection().scale(crownCurl))
                .add(0.0D, -terminalDrop, 0.0D);
        ColorBand colorBand = ignition
                ? ColorBand.IGNITION
                : segmentIndex >= TERMINAL_SEGMENT_START ? ColorBand.TERMINAL : ColorBand.BODY;
        int lifetime = lifetime(branch.seed(), segmentIndex, colorBand);
        float brilliance = (float) (1.02D + unit(branch.seed() ^ (BRILLIANCE_SALT + segmentIndex)) * 0.18D);
        boolean twinkles = colorBand == ColorBand.TERMINAL
                && unit(branch.seed() ^ (TWINKLE_SALT + segmentIndex)) < 0.68D;
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
            case IGNITION -> randomInt(seed, LIFETIME_SALT + segmentIndex, 16, 22);
            case BODY -> randomInt(seed, LIFETIME_SALT + segmentIndex, 24, 34);
            case TERMINAL -> randomInt(seed, LIFETIME_SALT + segmentIndex, 28, 38);
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
                throw new IllegalArgumentException("Small compact-radial request position must be finite");
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
                throw new IllegalArgumentException("Small compact-radial bounds must be finite and ordered");
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
        IGNITION(new Rgb(1.000F, 0.345F, 0.180F), 1.10F),
        BODY(new Rgb(1.000F, 0.694F, 0.208F), 1.00F),
        TERMINAL(new Rgb(1.000F, 0.945F, 0.690F), 1.04F);

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
            Point crownDirection,
            double reach,
            double curlPhase) {
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
                throw new IllegalArgumentException("Invalid small compact-radial sample");
            }
        }
    }

    private record Point(double x, double y, double z) {
        private Point normalized() {
            double length = Math.sqrt(lengthSqr());
            if (!Double.isFinite(length) || length < EPSILON) {
                throw new IllegalArgumentException("Small compact-radial direction must be finite and non-zero");
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

        private Point cross(Point other) {
            return new Point(
                    y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x);
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
