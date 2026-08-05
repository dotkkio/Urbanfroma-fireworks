package com.urbanforma.fireworks.content.giant.multiradial;

import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * Deterministic common-side contract for the fourth giant firework.
 *
 * <p>The effect is one burst made from three intentionally different radial shells. The outer shell is a dense
 * full-radius sphere, the middle shell is a sparser phase-shifted shell with a longer body, and the core is a
 * compact high-brightness starburst. All three layers are emitted as complete branch rings; no later particle
 * stage or random child explosion exists.</p>
 */
public final class GiantMultiRadialTrajectory {
    public static final double LAUNCH_HEIGHT = 0.0D;
    public static final double DETONATION_HEIGHT = 200.0D;
    /** The shared giant ascent reaches exactly 200 blocks at the last sample. */
    public static final int ASCENT_TICKS = 138;

    public static final double MAX_RADIUS = 130.0D;
    /** Shared client allowance from the current Fireworks handoff; this prototype consumes at most 384/tick. */
    public static final int MAX_CLIENT_PARTICLES_PER_TICK = 720;
    public static final float DETONATION_SOUND_VOLUME = 16.0F;
    public static final float DETONATION_SOUND_PITCH = 0.27F;
    public static final int DETONATION_SOUND_MAX_PLAYS = 1;

    public static final int TOTAL_PARTICLES = 12_288;
    public static final int PROTOTYPE_MAX_ALIVE_PARTICLES = TOTAL_PARTICLES;
    public static final int TOTAL_VISUAL_TICKS;

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final long BRANCH_SALT = 0xA4093822299F31D0L;
    private static final long PHASE_SALT = 0x13198A2E03707344L;
    private static final long LIFETIME_SALT = 0x452821E638D01377L;
    private static final long BRIGHTNESS_SALT = 0xBE5466CF34E90C6CL;

    public enum Layer {
        /** Full-size shell: dense, warm-white/gold, slight terminal droop. */
        OUTER(0, 192, 48, 130.0D, 0, 48, 132, 156, 1.18F),
        /** Offset shell: fewer, wider gaps and a distinct phase/curvature profile. */
        MIDDLE(1, 128, 16, 88.0D, 8, 16, 104, 128, 1.30F),
        /** Compact bright core: sparse branches, fast expansion, visibly higher brightness. */
        CORE(2, 64, 16, 36.0D, 14, 16, 84, 104, 1.58F);

        private final int index;
        private final int branchCount;
        private final int samplesPerBranch;
        private final double radius;
        private final int startTick;
        private final int emissionTicks;
        private final int minimumLifetime;
        private final int maximumLifetime;
        private final float brightnessScale;

        Layer(
                int index,
                int branchCount,
                int samplesPerBranch,
                double radius,
                int startTick,
                int emissionTicks,
                int minimumLifetime,
                int maximumLifetime,
                float brightnessScale) {
            this.index = index;
            this.branchCount = branchCount;
            this.samplesPerBranch = samplesPerBranch;
            this.radius = radius;
            this.startTick = startTick;
            this.emissionTicks = emissionTicks;
            this.minimumLifetime = minimumLifetime;
            this.maximumLifetime = maximumLifetime;
            this.brightnessScale = brightnessScale;
        }

        public int index() {
            return this.index;
        }

        public int branchCount() {
            return this.branchCount;
        }

        public int samplesPerBranch() {
            return this.samplesPerBranch;
        }

        public int sampleCount() {
            return this.branchCount * this.samplesPerBranch;
        }

        public double radius() {
            return this.radius;
        }

        public int startTick() {
            return this.startTick;
        }

        public int emissionTicks() {
            return this.emissionTicks;
        }

        public int endTickExclusive() {
            return this.startTick + this.emissionTicks;
        }

        public int minimumLifetime() {
            return this.minimumLifetime;
        }

        public int maximumLifetime() {
            return this.maximumLifetime;
        }

        public float brightnessScale() {
            return this.brightnessScale;
        }
    }

    public enum ColorBand {
        WARM_WHITE(new Rgb(1.0F, 0.94F, 0.72F), 1.36F),
        GOLD(new Rgb(1.0F, 0.67F, 0.10F), 1.20F),
        PEARL(new Rgb(1.0F, 0.99F, 0.86F), 1.48F),
        COOL_EDGE(new Rgb(0.62F, 0.84F, 1.0F), 1.10F);

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
            if (!finiteChannel(red) || !finiteChannel(green) || !finiteChannel(blue)) {
                throw new IllegalArgumentException("RGB channels must be finite and between 0 and 1");
            }
        }

        private static boolean finiteChannel(float value) {
            return Float.isFinite(value) && value >= 0.0F && value <= 1.0F;
        }
    }

    public record AscentSample(int tick, double progress, Vec3 position, Vec3 velocity) {
        public AscentSample {
            if (tick < 0 || tick >= ASCENT_TICKS || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || velocity == null) {
                throw new IllegalArgumentException("Invalid multiradial ascent sample");
            }
        }
    }

    public record Branch(Layer layer, int index, long seed, Vec3 direction) {
        public Branch {
            if (layer == null || index < 0 || index >= layer.branchCount() || direction == null
                    || !finite(direction) || direction.lengthSqr() < 1.0E-12D) {
                throw new IllegalArgumentException("Invalid multiradial branch");
            }
        }
    }

    public record BranchSample(
            Layer layer,
            Branch branch,
            int segmentIndex,
            double progress,
            double radius,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime,
            boolean coreHighlight) {
        public BranchSample {
            if (layer == null || branch == null || branch.layer() != layer
                    || segmentIndex < 0 || segmentIndex >= layer.samplesPerBranch()
                    || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(radius) || radius < 0.0D || radius > layer.radius() + RADIUS_EPSILON
                    || position == null || !finite(position) || colorBand == null || !Float.isFinite(brightness)
                    || brightness < 1.0F || lifetime < layer.minimumLifetime() || lifetime > layer.maximumLifetime()) {
                throw new IllegalArgumentException("Invalid multiradial branch sample");
            }
        }
    }

    public record LayerPlan(
            Layer layer,
            int branches,
            int samplesPerBranch,
            int totalSamples,
            double radius,
            int startTick,
            int emissionTicks,
            int minimumLifetime,
            int maximumLifetime) {
        public LayerPlan {
            if (layer == null || branches != layer.branchCount() || samplesPerBranch != layer.samplesPerBranch()
                    || totalSamples != layer.sampleCount() || radius != layer.radius()
                    || startTick != layer.startTick() || emissionTicks != layer.emissionTicks()
                    || minimumLifetime != layer.minimumLifetime() || maximumLifetime != layer.maximumLifetime()) {
                throw new IllegalArgumentException("Layer plan does not match the fixed multiradial contract");
            }
        }
    }

    public record ParticlePlan(int tick, int createdThisTick, int cumulativeCreated, int activeUpperBound,
                               int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0 || cumulativeCreated > TOTAL_PARTICLES
                    || activeUpperBound > PROTOTYPE_MAX_ALIVE_PARTICLES) {
                throw new IllegalArgumentException("Invalid multiradial particle accounting");
            }
        }
    }

    static {
        int end = 0;
        for (Layer layer : Layer.values()) {
            end = Math.max(end, layer.endTickExclusive() + layer.maximumLifetime());
        }
        TOTAL_VISUAL_TICKS = end;
        validateContract();
    }

    private GiantMultiRadialTrajectory() {
    }

    public static List<LayerPlan> layerPlans() {
        return List.of(
                plan(Layer.OUTER),
                plan(Layer.MIDDLE),
                plan(Layer.CORE));
    }

    private static LayerPlan plan(Layer layer) {
        return new LayerPlan(
                layer,
                layer.branchCount(),
                layer.samplesPerBranch(),
                layer.sampleCount(),
                layer.radius(),
                layer.startTick(),
                layer.emissionTicks(),
                layer.minimumLifetime(),
                layer.maximumLifetime());
    }

    /** Throws if the isolated prototype ever drifts from the shared giant matrix or particle boundary. */
    public static void validateContract() {
        int total = 0;
        int peak = 0;
        double previousRadius = MAX_RADIUS + 1.0D;
        for (Layer layer : Layer.values()) {
            total = Math.addExact(total, layer.sampleCount());
            previousRadius = Math.min(previousRadius, layer.radius());
            for (int tick = layer.startTick(); tick < layer.endTickExclusive(); tick++) {
                peak = Math.max(peak, particlesCreatedThisTick(tick));
            }
        }
        if (total != TOTAL_PARTICLES || peak > MAX_CLIENT_PARTICLES_PER_TICK
                || Layer.OUTER.radius() != MAX_RADIUS || Layer.OUTER.startTick() != 0
                || previousRadius <= 0.0D || TOTAL_VISUAL_TICKS <= 0 || !hasDistinctLayerProfiles()) {
            throw new IllegalStateException("Multiradial giant contract is inconsistent");
        }
    }

    public static AscentSample ascentAtTick(int tick) {
        if (tick < 0 || tick >= ASCENT_TICKS) {
            throw new IllegalArgumentException("Multiradial ascent tick is outside the 0-to-200 path");
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
        if (layer == null || branchIndex < 0 || branchIndex >= layer.branchCount()) {
            throw new IllegalArgumentException("Multiradial branch index is outside the configured layer");
        }
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT
                ^ ((long) layer.index() * 0x9E3779B97F4A7C15L)
                ^ ((long) branchIndex * 0xD6E8FEB86659FD93L));
        double fraction = ((double) branchIndex + 0.5D) / layer.branchCount();
        double latitude;
        double azimuth;
        if (layer == Layer.OUTER) {
            latitude = Math.asin(clamp(1.0D - 2.0D * fraction, -1.0D, 1.0D));
            azimuth = branchIndex * GOLDEN_ANGLE + randomUnit(payloadSeed ^ PHASE_SALT) * TWO_PI;
        } else if (layer == Layer.MIDDLE) {
            // A phase-shifted latitude lattice gives the middle shell a readable stagger rather than a scaled copy.
            latitude = -0.78D + 1.56D * fraction;
            azimuth = branchIndex * GOLDEN_ANGLE * 1.17D + 0.43D
                    + randomUnit(payloadSeed ^ PHASE_SALT ^ 0x55AA55AAL) * TWO_PI;
        } else {
            // The core uses a denser polar bias and a separate azimuth phase so its bright center reads independently.
            latitude = Math.asin(clamp(0.72D * (1.0D - 2.0D * fraction), -1.0D, 1.0D));
            azimuth = branchIndex * GOLDEN_ANGLE * 0.73D + 1.11D
                    + randomUnit(payloadSeed ^ PHASE_SALT ^ 0xCC33CC33L) * TWO_PI;
        }
        double jitter = centered(branchSeed, 0xA4093822299F31D0L) * (layer == Layer.OUTER ? 0.025D : 0.045D);
        latitude = clamp(latitude + jitter, -Math.PI * 0.49D, Math.PI * 0.49D);
        azimuth += centered(branchSeed, 0x13198A2E03707344L) * (layer == Layer.CORE ? 0.08D : 0.045D);
        double horizontal = Math.cos(latitude);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth),
                Math.sin(latitude),
                horizontal * Math.sin(azimuth)).normalize();
        return new Branch(layer, branchIndex, branchSeed, direction);
    }

    public static BranchSample sample(long payloadSeed, Layer layer, int branchIndex, int segmentIndex) {
        return sample(payloadSeed, branch(payloadSeed, layer, branchIndex), segmentIndex);
    }

    public static BranchSample sample(long payloadSeed, Branch branch, int segmentIndex) {
        if (branch == null) {
            throw new IllegalArgumentException("Multiradial branch may not be null");
        }
        Layer layer = branch.layer();
        if (segmentIndex < 0 || segmentIndex >= layer.samplesPerBranch()) {
            throw new IllegalArgumentException("Multiradial segment index is outside the configured layer");
        }
        double progress = layer.samplesPerBranch() == 1
                ? 1.0D
                : (double) segmentIndex / (layer.samplesPerBranch() - 1);
        double radius = radialRadius(layer, progress);
        Vec3 direction = layerDirection(branch.direction(), layer, progress);
        Vec3 position = direction.scale(radius);
        ColorBand colorBand = colorBand(layer, segmentIndex);
        int lifetime = randomInt(
                branch.seed() ^ payloadSeed,
                LIFETIME_SALT + segmentIndex,
                layer.minimumLifetime(),
                layer.maximumLifetime());
        float brightness = layer.brightnessScale()
                * (1.0F + (float) randomUnit(branch.seed() ^ payloadSeed ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.12F);
        boolean coreHighlight = layer == Layer.CORE || segmentIndex < coreSegmentCount(layer);
        return new BranchSample(layer, branch, segmentIndex, progress, radius, position, colorBand, brightness,
                lifetime, coreHighlight);
    }

    public static ColorBand colorBand(Layer layer, int segmentIndex) {
        if (layer == null || segmentIndex < 0 || segmentIndex >= layer.samplesPerBranch()) {
            throw new IllegalArgumentException("Multiradial color segment is outside the configured layer");
        }
        if (layer == Layer.CORE) {
            return segmentIndex < 5 ? ColorBand.WARM_WHITE : (segmentIndex < 12 ? ColorBand.PEARL : ColorBand.GOLD);
        }
        if (layer == Layer.MIDDLE) {
            return segmentIndex < 3 ? ColorBand.WARM_WHITE : (segmentIndex == 11 ? ColorBand.COOL_EDGE : ColorBand.GOLD);
        }
        if (segmentIndex < 7) {
            return ColorBand.WARM_WHITE;
        }
        return segmentIndex < 38 ? ColorBand.GOLD : ColorBand.PEARL;
    }

    public static int coreSegmentCount(Layer layer) {
        return layer == Layer.CORE ? 5 : layer == Layer.MIDDLE ? 3 : 7;
    }

    public static boolean isLayerEmitting(Layer layer, int tick) {
        return layer != null && tick >= layer.startTick() && tick < layer.endTickExclusive();
    }

    public static int particlesCreatedThisTick(Layer layer, int tick) {
        return isLayerEmitting(layer, tick) ? layer.branchCount() : 0;
    }

    public static int particlesCreatedThisTick(int tick) {
        int total = 0;
        for (Layer layer : Layer.values()) {
            total += particlesCreatedThisTick(layer, tick);
        }
        return total;
    }

    public static int particlesCreatedThroughTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int total = 0;
        for (Layer layer : Layer.values()) {
            int localTicks = Math.max(0, Math.min(layer.emissionTicks(), tick - layer.startTick() + 1));
            total += localTicks * layer.branchCount();
        }
        return Math.min(TOTAL_PARTICLES, total);
    }

    /** Conservative live count using every layer's maximum lifetime. */
    public static int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        int total = 0;
        for (Layer layer : Layer.values()) {
            int firstLiveTick = Math.max(layer.startTick(), tick - layer.maximumLifetime() + 1);
            int lastEmissionTick = Math.min(tick, layer.endTickExclusive() - 1);
            if (firstLiveTick <= lastEmissionTick) {
                total += (lastEmissionTick - firstLiveTick + 1) * layer.branchCount();
            }
        }
        return Math.min(PROTOTYPE_MAX_ALIVE_PARTICLES, total);
    }

    public static ParticlePlan particlePlanAtTick(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Multiradial particle-plan tick may not be negative");
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

    public static boolean fitsRadius(BranchSample sample) {
        return sample != null && sample.position().lengthSqr() <= MAX_RADIUS * MAX_RADIUS + RADIUS_EPSILON;
    }

    public static boolean layerUsesDistinctProfile(Layer layer) {
        if (layer == null) {
            return false;
        }
        return layer == Layer.OUTER
                ? layer.branchCount() != Layer.MIDDLE.branchCount()
                : layer == Layer.MIDDLE
                        ? layer.radius() != Layer.CORE.radius() && layer.branchCount() != Layer.CORE.branchCount()
                        : layer.radius() != Layer.OUTER.radius();
    }

    /** Structural proof that no two layers are merely a scaled copy of the same branch program. */
    public static boolean hasDistinctLayerProfiles() {
        return layerUsesDistinctProfile(Layer.OUTER)
                && layerUsesDistinctProfile(Layer.MIDDLE)
                && layerUsesDistinctProfile(Layer.CORE)
                && Layer.OUTER.startTick() != Layer.MIDDLE.startTick()
                && Layer.MIDDLE.startTick() != Layer.CORE.startTick();
    }

    private static double radialRadius(Layer layer, double progress) {
        double eased;
        if (layer == Layer.OUTER) {
            eased = smoothStep(progress);
            return 2.5D + (layer.radius() - 2.5D) * eased;
        }
        if (layer == Layer.MIDDLE) {
            eased = 1.0D - Math.pow(1.0D - progress, 1.65D);
            return 1.5D + (layer.radius() - 1.5D) * eased;
        }
        eased = smoothStep(Math.min(1.0D, progress * 1.12D));
        return 0.75D + (layer.radius() - 0.75D) * eased;
    }

    private static Vec3 layerDirection(Vec3 base, Layer layer, double progress) {
        if (layer == Layer.OUTER) {
            double droop = 0.18D * smoothStep((progress - 0.68D) / 0.32D);
            return new Vec3(base.x, base.y - droop, base.z).normalize();
        }
        if (layer == Layer.MIDDLE) {
            double orbit = 0.36D + progress * 0.88D;
            double cosine = Math.cos(orbit);
            double sine = Math.sin(orbit);
            Vec3 rotated = new Vec3(
                    base.x * cosine - base.z * sine,
                    base.y + 0.10D * Math.sin(progress * TWO_PI),
                    base.x * sine + base.z * cosine);
            return rotated.normalize();
        }
        return new Vec3(base.x, base.y * 1.14D, base.z).normalize();
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static int randomInt(long seed, long salt, int min, int max) {
        return min + (int) Math.floor(randomUnit(seed ^ salt) * (max - min + 1));
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
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

    private static boolean finite(Vec3 value) {
        return value != null && Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
    }
}
