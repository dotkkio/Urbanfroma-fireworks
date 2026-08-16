package com.urbanforma.fireworks.content.midsize.sphere;

import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Cadence;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.CoreForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.LayerForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Rgb;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.SphereForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.TrailForm;
import net.minecraft.world.phys.Vec3;

/** Deterministic common-side sampling for the isolated medium spherical families. */
public final class MediumSphereTrajectory {
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double EPSILON = 1.0E-9D;
    private static final long BRANCH_SALT = 0x7F4A7C159E3779B9L;
    private static final long PHASE_SALT = 0xD1B54A32D192ED03L;
    private static final long AZIMUTH_SALT = 0x94D049BB133111EBL;
    private static final long LIFETIME_SALT = 0xA24BAED4963EE407L;
    private static final long TWINKLE_SALT = 0x9FB21C651E98DF25L;

    private MediumSphereTrajectory() {
    }

    public record AscentSample(int tick, double progress, double height) {
        public AscentSample {
            if (tick < 0 || !Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(height) || height < 0.0D) {
                throw new IllegalArgumentException("Invalid medium sphere ascent sample");
            }
        }
    }

    public record Branch(int index, long seed, Vec3 direction, Vec3 tangent, Vec3 bitangent, double phase) {
        public Branch {
            if (index < 0 || direction == null || tangent == null || bitangent == null || !Double.isFinite(phase)
                    || Math.abs(direction.lengthSqr() - 1.0D) > 1.0E-6D
                    || Math.abs(tangent.lengthSqr() - 1.0D) > 1.0E-6D
                    || Math.abs(bitangent.lengthSqr() - 1.0D) > 1.0E-6D) {
                throw new IllegalArgumentException("Invalid deterministic medium sphere branch");
            }
        }
    }

    public record Sample(
            int branchIndex,
            int emissionTick,
            int segmentIndex,
            Vec3 position,
            Rgb color,
            float scale,
            int lifetimeTicks,
            boolean coreHighlight,
            boolean twinkles) {
        public Sample {
            if (branchIndex < 0 || emissionTick < 0 || segmentIndex < 0 || position == null || color == null
                    || !Float.isFinite(scale) || scale <= 0.0F || lifetimeTicks <= 0) {
                throw new IllegalArgumentException("Invalid medium sphere particle sample");
            }
        }
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Medium sphere bounds must be finite and ordered");
            }
        }

        public double maxSpan() {
            return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
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
                    || remainingToEmit < 0) {
                throw new IllegalArgumentException("Invalid medium sphere particle plan");
            }
        }
    }

    public static AscentSample ascentAtTick(MediumSphereDefinition definition, int tick) {
        requireDefinition(definition);
        if (tick < 0 || tick >= definition.boundary().ascentTicks()) {
            throw new IllegalArgumentException("Medium sphere ascent tick is outside the configured path");
        }
        int lastTick = definition.boundary().ascentTicks() - 1;
        double progress = lastTick == 0 ? 1.0D : (double) tick / lastTick;
        return new AscentSample(tick, progress, definition.boundary().detonationHeight() * progress);
    }

    public static boolean ascentFitsDeclaredHeight(MediumSphereDefinition definition) {
        requireDefinition(definition);
        int lastTick = definition.boundary().ascentTicks() - 1;
        return ascentAtTick(definition, 0).height() == 0.0D
                && Math.abs(ascentAtTick(definition, lastTick).height() - definition.boundary().detonationHeight()) <= EPSILON;
    }

    public static Branch branch(MediumSphereDefinition definition, long payloadSeed, int branchIndex) {
        requireDefinition(definition);
        int branchCount = definition.particleBudget().branchCount();
        if (branchIndex < 0 || branchIndex >= branchCount) {
            throw new IllegalArgumentException("Medium sphere branch index is outside the configured count");
        }
        long branchSeed = mix64(payloadSeed ^ BRANCH_SALT ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double latitude = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / branchCount;
        double azimuth = branchIndex * GOLDEN_ANGLE + randomUnit(branchSeed ^ AZIMUTH_SALT) * TWO_PI;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - latitude * latitude));
        Vec3 direction = new Vec3(horizontal * Math.cos(azimuth), latitude, horizontal * Math.sin(azimuth)).normalize();
        Vec3 reference = Math.abs(direction.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 tangent = reference.cross(direction).normalize();
        Vec3 bitangent = direction.cross(tangent).normalize();
        return new Branch(branchIndex, branchSeed, direction, tangent, bitangent, randomUnit(branchSeed ^ PHASE_SALT) * TWO_PI);
    }

    public static Sample sample(MediumSphereDefinition definition, long payloadSeed, int branchIndex, int emissionTick) {
        return sample(definition, payloadSeed, branch(definition, payloadSeed, branchIndex), emissionTick);
    }

    public static Sample sample(
            MediumSphereDefinition definition, long payloadSeed, Branch branch, int emissionTick) {
        requireDefinition(definition);
        if (branch == null || branch.index() >= definition.particleBudget().branchCount()) {
            throw new IllegalArgumentException("Medium sphere branch does not match the definition");
        }
        int emissionTicks = definition.particleBudget().emissionTicks();
        if (emissionTick < 0 || emissionTick >= emissionTicks) {
            throw new IllegalArgumentException("Medium sphere emission tick is outside the configured count");
        }
        Cadence cadence = definition.visualSignature().cadence();
        int segmentIndex = cadence.segmentAtTick(emissionTick, emissionTicks);
        double progress = ((double) segmentIndex + 0.5D) / emissionTicks;
        boolean core = isCore(definition.visualSignature().core(), progress);
        double radius = radiusFor(definition, branch, progress, core);
        Vec3 position = clampToRadius(shapePosition(definition, branch, progress, radius, core), definition.boundary().maxRadius());
        if (position.lengthSqr() > square(definition.boundary().maxRadius()) + EPSILON) {
            throw new IllegalStateException("Medium sphere sample escaped its declared envelope");
        }
        return new Sample(
                branch.index(),
                emissionTick,
                segmentIndex,
                position,
                colorFor(definition, branch, progress, core),
                scaleFor(definition, progress, core),
                lifetimeFor(definition, branch, segmentIndex),
                core,
                twinkles(definition.visualSignature().trail(), branch.seed(), segmentIndex, core));
    }

    public static int particlesCreatedThisTick(MediumSphereDefinition definition, int tick) {
        requireDefinition(definition);
        return tick >= 0 && tick < definition.particleBudget().emissionTicks()
                ? definition.particleBudget().particlesPerTick()
                : 0;
    }

    public static int particlesCreatedThroughTick(MediumSphereDefinition definition, int tick) {
        requireDefinition(definition);
        if (tick < 0) {
            return 0;
        }
        return Math.min(tick + 1, definition.particleBudget().emissionTicks()) * definition.particleBudget().particlesPerTick();
    }

    public static int activeParticleUpperBoundAtTick(MediumSphereDefinition definition, int tick) {
        requireDefinition(definition);
        if (tick < 0) {
            return 0;
        }
        int firstLiveEmission = Math.max(0, tick - definition.particleBudget().maxLifetimeTicks());
        int lastLiveEmission = Math.min(tick, definition.particleBudget().emissionTicks() - 1);
        return firstLiveEmission > lastLiveEmission
                ? 0
                : (lastLiveEmission - firstLiveEmission + 1) * definition.particleBudget().particlesPerTick();
    }

    public static ParticlePlan particlePlanAtTick(MediumSphereDefinition definition, int tick) {
        requireDefinition(definition);
        if (tick < 0) {
            throw new IllegalArgumentException("Medium sphere particle-plan tick may not be negative");
        }
        int cumulative = particlesCreatedThroughTick(definition, tick);
        return new ParticlePlan(
                tick,
                particlesCreatedThisTick(definition, tick),
                cumulative,
                activeParticleUpperBoundAtTick(definition, tick),
                definition.particleBudget().totalParticles() - cumulative);
    }

    public static Bounds conservativeBounds(MediumSphereDefinition definition) {
        requireDefinition(definition);
        double radius = definition.boundary().maxRadius();
        return new Bounds(-radius, -radius, -radius, radius, radius, radius);
    }

    public static boolean fitsEnvelope(MediumSphereDefinition definition) {
        requireDefinition(definition);
        return conservativeBounds(definition).maxSpan() <= definition.boundary().fullEnvelopeBlocks() + EPSILON;
    }

    private static boolean isCore(CoreForm core, double progress) {
        return core != CoreForm.HOLLOW_VOID && progress < 0.22D;
    }

    private static double radiusFor(MediumSphereDefinition definition, Branch branch, double progress, boolean core) {
        double limit = definition.boundary().maxRadius();
        if (core) {
            double coreProgress = clamp(progress / 0.22D, 0.0D, 1.0D);
            return switch (definition.visualSignature().core()) {
                case DUAL_NUCLEI, SPLIT_HEART -> limit * (0.08D + 0.16D * coreProgress);
                case ORBITAL_RING, EMBER_ORBIT, CORAL_RING, MOON_SEED -> limit * (0.18D + 0.12D * coreProgress);
                case DIAMOND_HEART, ICE_BEADS, MOSAIC_CORE -> limit * (0.11D + 0.22D * coreProgress);
                default -> limit * (0.07D + 0.25D * coreProgress);
            };
        }

        double shellProgress = clamp((progress - 0.18D) / 0.82D, 0.0D, 1.0D);
        double smooth = smoothStep(shellProgress);
        return Math.min(limit, Math.max(limit * 0.16D, limit * layerRadius(definition.visualSignature().layering(), smooth, branch.phase())));
    }

    private static double layerRadius(LayerForm layering, double progress, double phase) {
        return switch (layering) {
            case HOLLOW_SHELL -> 0.56D + 0.42D * progress;
            case TWIN_LOBES, SPLIT_LOBES -> 0.18D + 0.78D * progress;
            case RING_TO_SHELL, ORBITAL_NEST, ANNULAR_BODY, MOON_BANDS -> 0.28D + 0.68D * progress;
            case PULSE_LAYERS -> 0.20D + 0.72D * progress + 0.045D * Math.sin(TWO_PI * (3.0D * progress) + phase);
            case FACET_BANDS, ICE_FACETS, MOSAIC_TILES -> 0.20D + 0.75D * progress;
            case WOVEN_SHELL, BRAIDED_SHELL, VEIL_SHELL -> 0.18D + 0.76D * progress;
            case RISING_CROWN, SOLAR_CORONA -> 0.15D + 0.80D * progress;
            case TRIPLE_SHELL, BLOOM_LAYERS -> layeredRadius(progress);
            case PETAL_BANDS, COMET_ARCS, CASCADE_SHELL -> 0.16D + 0.80D * progress;
            case MIRRORED_SHELL, FLASH_SHELL, RIBBED_SHELL -> 0.18D + 0.77D * progress;
            case BEAD_TO_CROWN -> 0.17D + 0.79D * progress;
        };
    }

    private static double layeredRadius(double progress) {
        if (progress < 0.36D) {
            return 0.30D + progress * 0.45D;
        }
        if (progress < 0.68D) {
            return 0.55D + (progress - 0.36D) * 0.72D;
        }
        return 0.78D + (progress - 0.68D) * 0.55D;
    }

    private static Vec3 shapePosition(
            MediumSphereDefinition definition, Branch branch, double progress, double radius, boolean core) {
        SphereForm form = definition.visualSignature().shape();
        double wave = Math.sin(branch.phase() + TWO_PI * progress);
        double twist = Math.cos(branch.phase() * 0.5D + 7.0D * progress);
        Vec3 radial = branch.direction().scale(radius);
        Vec3 tangent = branch.tangent().scale(radius);
        Vec3 bitangent = branch.bitangent().scale(radius);
        Vec3 corePosition = corePosition(definition.visualSignature().core(), branch, radius, wave);
        if (core) {
            return switch (form) {
                case TWIN_ORB, SPLIT -> corePosition.add(new Vec3((branch.index() & 1) == 0 ? radius * 0.55D : -radius * 0.55D, 0.0D, 0.0D));
                case RING_CORE, ORBIT, ANNULAR, MOON -> corePosition;
                case CRYSTAL, ICE, MOSAIC -> corePosition.add(tangent.scale(0.14D * Math.signum(wave)));
                case LANTERN -> corePosition.add(new Vec3(0.0D, radius * 0.18D * wave, 0.0D));
                default -> corePosition;
            };
        }
        return switch (form) {
            case DAHLIA -> radial.add(tangent.scale(0.085D * Math.sin(6.0D * branch.phase() + 5.0D * progress)));
            case HOLLOW_CHRYSANTHEMUM -> radial.add(bitangent.scale(0.055D * wave));
            case TWIN_ORB -> radial.scale(0.72D).add(new Vec3((branch.index() & 1) == 0 ? radius * 0.28D : -radius * 0.28D, 0.0D, 0.0D));
            case RING_CORE -> radial.add(tangent.scale(0.07D * Math.sin(4.0D * progress + branch.phase())));
            case PULSE_SHELL -> radial.scale(0.90D + 0.08D * Math.sin(TWO_PI * 3.0D * progress + branch.phase()));
            case CRYSTAL -> radial.scale(0.83D + 0.13D * Math.abs(Math.sin(6.0D * branch.phase() + 4.0D * progress)));
            case LACE -> radial.add(tangent.scale(0.10D * wave)).add(bitangent.scale(0.06D * twist));
            case CROWN -> radial.add(new Vec3(0.0D, radius * 0.09D * smoothStep(progress), 0.0D));
            case ORBIT -> radial.add(tangent.scale(0.10D * Math.sin(TWO_PI * 2.0D * progress + branch.phase())));
            case SPLIT -> radial.scale(0.74D).add(new Vec3(0.0D, (branch.index() & 1) == 0 ? radius * 0.25D : -radius * 0.25D, 0.0D));
            case LAYERED -> radial.scale(progress < 0.45D ? 0.58D : progress < 0.75D ? 0.79D : 0.96D);
            case PETAL -> radial.add(tangent.scale(0.12D * Math.sin(5.0D * branch.phase() + TWO_PI * progress)));
            case MIRROR -> ((branch.index() & 1) == 0 ? radial : radial.scale(-1.0D)).add(bitangent.scale(0.045D * wave));
            case STROBE -> radial.scale(0.86D + ((branch.index() + (int) Math.floor(progress * 10.0D)) & 1) * 0.10D);
            case COMET -> radial.add(tangent.scale(0.16D * (1.0D - progress))).add(branch.direction().scale(radius * 0.06D * wave));
            case BRAID -> radial.add(tangent.scale(0.12D * Math.sin(9.0D * progress + branch.phase())));
            case ANNULAR -> radial.add(bitangent.scale(0.09D * Math.cos(4.0D * progress + branch.phase())));
            case AURORA -> radial.add(new Vec3(0.0D, radius * 0.13D * Math.sin(branch.phase() + TWO_PI * progress), 0.0D));
            case ICE -> radial.scale(0.84D + 0.12D * Math.abs(Math.cos(7.0D * branch.phase())));
            case MOON -> radial.add(tangent.scale(0.075D * Math.sin(TWO_PI * progress + branch.phase())));
            case LANTERN -> radial.add(new Vec3(0.0D, radius * 0.06D * Math.sin(8.0D * branch.phase()), 0.0D));
            case PEONY -> radial.add(tangent.scale(0.11D * Math.sin(5.0D * branch.phase() + 3.0D * progress)));
            case MOSAIC -> radial.scale(0.82D + 0.13D * Math.abs(Math.sin(4.0D * branch.phase())));
            case CASCADE -> radial.add(new Vec3(0.0D, -radius * 0.15D * smoothStep(progress), 0.0D));
            case SOLAR -> radial.add(branch.direction().scale(radius * 0.10D * smoothStep(progress))).add(tangent.scale(0.04D * wave));
        };
    }

    private static Vec3 corePosition(CoreForm core, Branch branch, double radius, double wave) {
        double angle = branch.phase() + branch.index() * GOLDEN_ANGLE;
        return switch (core) {
            case HOLLOW_VOID -> branch.direction().scale(radius * 2.2D);
            case ORBITAL_RING, EMBER_ORBIT, CORAL_RING, MOON_SEED -> new Vec3(
                    Math.cos(angle) * radius * 1.20D,
                    radius * 0.18D * wave,
                    Math.sin(angle) * radius * 1.20D);
            case DUAL_NUCLEI, SPLIT_HEART -> branch.direction().scale(radius).add(
                    new Vec3((branch.index() & 1) == 0 ? radius * 0.80D : -radius * 0.80D, 0.0D, 0.0D));
            case DIAMOND_HEART, ICE_BEADS, MOSAIC_CORE -> branch.direction().scale(radius * (0.85D + 0.20D * Math.abs(wave)));
            default -> branch.direction().scale(radius).add(branch.tangent().scale(radius * 0.16D * wave));
        };
    }

    private static Rgb colorFor(MediumSphereDefinition definition, Branch branch, double progress, boolean core) {
        Rgb primary = definition.palette().primary();
        Rgb secondary = definition.palette().secondary();
        Rgb accent = definition.palette().accent();
        if (core) {
            return blend(primary, accent, 0.30D + 0.15D * randomUnit(branch.seed() ^ PHASE_SALT));
        }
        double outerBlend = smoothStep((progress - 0.32D) / 0.68D);
        Rgb body = blend(primary, secondary, outerBlend);
        return switch (definition.visualSignature().trail()) {
            case PEARL_TIPS, CRACKLE_EDGES, VIOLET_COMETS, CHAMPAGNE_TWINKLE, GOLD_NEEDLES,
                    CROWN_SPARKS, PEARL_SPLITS, SILVER_TIPS, PEACH_PETALS, MIRROR_PINS,
                    STROBE_FLASHES, COMET_TAILS, IVORY_HALO, AQUA_FRINGE, MOON_HALO,
                    GILDED_RIBS, CHAMPAGNE_BLOOM, SOLAR_TIPS -> blend(body, accent, outerBlend * 0.82D);
            case BRONZE_STREAMERS, LACE_GLINTS, GARNET_TRACERS, LILAC_WEAVE, MAGENTA_VEIL,
                    COPPER_FLECKS, ROSE_CASCADE -> blend(body, accent, 0.30D + outerBlend * 0.48D);
        };
    }

    private static float scaleFor(MediumSphereDefinition definition, double progress, boolean core) {
        double densityScale = switch (definition.visualSignature().density()) {
            case COMPACT -> 0.82D;
            case RICH -> 0.88D;
            case DENSE -> 0.93D;
            case BRIGHT -> 0.98D;
            case FULL -> 1.03D;
        };
        double edgeLift = progress > 0.78D ? 0.10D : 0.0D;
        return (float) (densityScale + edgeLift + (core ? 0.22D : 0.0D));
    }

    private static int lifetimeFor(MediumSphereDefinition definition, Branch branch, int segmentIndex) {
        int min = definition.particleBudget().minLifetimeTicks();
        int max = definition.particleBudget().maxLifetimeTicks();
        int base = min + (int) Math.floor(randomUnit(branch.seed() ^ LIFETIME_SALT ^ segmentIndex) * (max - min + 1));
        int lift = switch (definition.visualSignature().trail()) {
            case BRONZE_STREAMERS, COMET_TAILS, MAGENTA_VEIL, ROSE_CASCADE -> 4;
            case CHAMPAGNE_TWINKLE, STROBE_FLASHES, AQUA_FRINGE -> 2;
            default -> 0;
        };
        return Math.min(max, base + lift);
    }

    private static boolean twinkles(TrailForm trail, long seed, int segmentIndex, boolean core) {
        if (core) {
            return false;
        }
        double chance = switch (trail) {
            case CHAMPAGNE_TWINKLE, STROBE_FLASHES, GOLD_NEEDLES, AQUA_FRINGE -> 0.72D;
            case COMET_TAILS, MAGENTA_VEIL, ROSE_CASCADE, PEARL_TIPS, PEARL_SPLITS -> 0.56D;
            case BRONZE_STREAMERS, LACE_GLINTS, GARNET_TRACERS, LILAC_WEAVE, IVORY_HALO,
                    MOON_HALO, GILDED_RIBS, CHAMPAGNE_BLOOM, COPPER_FLECKS, SOLAR_TIPS,
                    CRACKLE_EDGES, VIOLET_COMETS, CROWN_SPARKS, SILVER_TIPS, PEACH_PETALS,
                    MIRROR_PINS -> 0.38D;
        };
        return randomUnit(seed ^ TWINKLE_SALT ^ segmentIndex) < chance;
    }

    private static Vec3 clampToRadius(Vec3 raw, double maxRadius) {
        double length = raw.length();
        return length <= maxRadius || length < EPSILON ? raw : raw.scale(maxRadius / length);
    }

    private static Rgb blend(Rgb first, Rgb second, double factor) {
        double bounded = clamp(factor, 0.0D, 1.0D);
        return new Rgb(
                (float) (first.red() + (second.red() - first.red()) * bounded),
                (float) (first.green() + (second.green() - first.green()) * bounded),
                (float) (first.blue() + (second.blue() - first.blue()) * bounded));
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double square(double value) {
        return value * value;
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

    private static void requireDefinition(MediumSphereDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Medium sphere definition may not be null");
        }
    }
}
