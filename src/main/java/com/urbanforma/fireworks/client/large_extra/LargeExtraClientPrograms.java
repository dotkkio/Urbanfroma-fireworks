package com.urbanforma.fireworks.client.large_extra;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.large_extra.LargeExtraFireworkCatalog;
import com.urbanforma.fireworks.content.large_extra.LargeExtraFireworkDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;

/**
 * Physical-client-only, caller-driven programs for the isolated Large Fireworks batch.
 *
 * <p>Each instance receives one already-authorized compact burst request, samples at most its own declared local
 * per-tick budget, clamps every placement to its item's envelope, and creates particles only through the existing
 * HD-spark/vanilla-fallback adapter. It owns no listener, queue, network handler, server computation, or shared
 * global particle limiter.</p>
 */
public final class LargeExtraClientPrograms {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double ENVELOPE_EPSILON = 1.0E-9D;
    private static final long DIRECTION_SALT = 0x6A09E667F3BCC909L;
    private static final long LAYER_SALT = 0xBB67AE8584CAA73BL;
    private static final long LIFETIME_SALT = 0x3C6EF372FE94F82BL;
    private static final long TWINKLE_SALT = 0xA54FF53A5F1D36F1L;

    private LargeExtraClientPrograms() {
    }

    public static Program start(Request request) {
        return new Program(request, LargeExtraFireworkCatalog.require(request.fireworkId()));
    }

    /** Returns one deterministic visual frame without allocating a Minecraft particle. */
    public static List<Emission> emissionsAtTick(Request request, int tick) {
        return emissionsAtTick(LargeExtraFireworkCatalog.require(request.fireworkId()), request, tick);
    }

    /** Static validation hook for an integration build or isolated verifier; it never touches a client instance. */
    public static void validateAll() {
        Set<String> programIds = new HashSet<>();
        for (LargeExtraFireworkDefinition definition : LargeExtraFireworkCatalog.values()) {
            Request request = new Request(definition.id(), 0.0D, 0.0D, 0.0D, 0x3F1D5B79A0C4E267L);
            int planned = 0;
            int peak = 0;
            for (int tick = 0; tick < definition.particleBudget().emissionTicks(); tick++) {
                List<Emission> emissions = emissionsAtTick(definition, request, tick);
                planned += emissions.size();
                peak = Math.max(peak, emissions.size());
                for (Emission emission : emissions) {
                    double distance = Math.sqrt(
                            emission.x() * emission.x() + emission.y() * emission.y() + emission.z() * emission.z());
                    if (distance > definition.envelope().maximumRadiusBlocks() + ENVELOPE_EPSILON) {
                        throw new IllegalStateException("Large Extra emission escaped its declared envelope");
                    }
                }
            }
            if (!programIds.add(definition.reuseContract().clientProgramId())
                    || planned != definition.particleBudget().plannedParticles()
                    || peak != definition.particleBudget().particlesPerTick()
                    || peak > LargeExtraFireworkDefinition.MAX_LOCAL_PARTICLES_PER_TICK) {
                throw new IllegalStateException("Large Extra client program budget drifted for " + definition.id());
            }
        }
        if (programIds.size() != LargeExtraFireworkCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Large Extra client program count drifted");
        }
    }

    private static List<Emission> emissionsAtTick(
            LargeExtraFireworkDefinition definition, Request request, int tick) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        if (tick < 0 || tick >= budget.emissionTicks()) {
            return List.of();
        }
        List<Emission> emissions = new ArrayList<>(budget.particlesPerTick());
        for (int index = 0; index < budget.particlesPerTick(); index++) {
            long sampleSeed = sampleSeed(request.seed(), definition.id(), tick, index);
            Offset rawOffset = sampleOffset(definition, sampleSeed, tick, index);
            Offset offset = clampToEnvelope(rawOffset, definition.envelope().maximumRadiusBlocks());
            ColorLayer layer = colorLayer(definition.effectPath(), tick, index, budget.emissionTicks());
            boolean core = isCore(definition.effectPath(), tick, index, budget.emissionTicks());
            int lifetime = lifetime(budget, sampleSeed);
            boolean twinkles = layer == ColorLayer.ACCENT
                    || unit(sampleSeed ^ TWINKLE_SALT) < (core ? 0.0D : 0.31D);
            emissions.add(new Emission(
                    request.x() + offset.x(),
                    request.y() + offset.y(),
                    request.z() + offset.z(),
                    layer,
                    core ? 1.32F : scaleFor(layer),
                    lifetime,
                    core,
                    twinkles));
        }
        if (emissions.size() != budget.particlesPerTick()
                || emissions.size() > LargeExtraFireworkDefinition.MAX_LOCAL_PARTICLES_PER_TICK) {
            throw new IllegalStateException("Large Extra program exceeded its local per-tick budget");
        }
        return List.copyOf(emissions);
    }

    private static Offset sampleOffset(
            LargeExtraFireworkDefinition definition, long sampleSeed, int tick, int index) {
        return switch (definition.effectPath()) {
            case GLOBE_SHELL -> globeShell(definition, sampleSeed, tick, index);
            case TRIPLE_TIER_RADIANCE -> tripleTierRadiance(definition, sampleSeed, tick, index);
            case DUAL_BREAK -> dualBreak(definition, sampleSeed, tick, index);
            case WORLD_GRID -> worldGrid(definition, sampleSeed, tick, index);
            case STOUT_COMET -> stoutComet(definition, sampleSeed, tick, index);
            case APERTURE_HEX_REVEAL -> apertureHexReveal(definition, sampleSeed, tick, index);
            case ORBITAL_NUCLEUS -> orbitalNucleus(definition, sampleSeed, tick, index);
            case INTERWOVEN_RADIANCE -> interwovenRadiance(definition, sampleSeed, tick, index);
            case POLAR_LANTERN -> polarLantern(definition, sampleSeed, tick, index);
            case ECLIPSE_ARC_SPLIT -> eclipseArcSplit(definition, sampleSeed, tick, index);
        };
    }

    private static Offset globeShell(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int globalIndex = tick * budget.particlesPerTick() + index;
        Offset direction = fibonacciDirection(globalIndex, budget.plannedParticles(), seed);
        double frame = frameProgress(tick, budget.emissionTicks());
        double latitudeGap = 0.91D + 0.07D * Math.abs(Math.sin(direction.y() * 9.0D + unit(seed) * TWO_PI));
        return direction.scale(definition.envelope().maximumRadiusBlocks() * (0.16D + frame * 0.80D) * latitudeGap);
    }

    private static Offset tripleTierRadiance(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int tier = Math.floorMod(index + tick, 3);
        int tierSample = tick * (budget.particlesPerTick() / 3) + index / 3;
        Offset direction = rotateAroundY(
                fibonacciDirection(tierSample, budget.emissionTicks() * (budget.particlesPerTick() / 3), seed),
                tier * TWO_PI / 9.0D + frameProgress(tick, budget.emissionTicks()) * 0.34D);
        double tierReach = switch (tier) {
            case 0 -> 0.42D;
            case 1 -> 0.68D;
            default -> 0.94D;
        };
        return direction.scale(definition.envelope().maximumRadiusBlocks()
                * tierReach
                * (0.24D + 0.76D * frameProgress(tick, budget.emissionTicks())));
    }

    private static Offset dualBreak(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int breakTick = budget.emissionTicks() / 2;
        double radius = definition.envelope().maximumRadiusBlocks();
        if (tick < breakTick) {
            Offset direction = fibonacciDirection(tick * budget.particlesPerTick() + index, budget.plannedParticles(), seed);
            return direction.scale(radius * 0.82D * frameProgress(tick, breakTick));
        }
        int daughter = index & 1;
        Offset center = new Offset(daughter == 0 ? -radius * 0.22D : radius * 0.22D, radius * 0.03D, 0.0D);
        Offset direction = rotateAroundY(
                fibonacciDirection(tick * (budget.particlesPerTick() / 2) + index / 2,
                        (budget.emissionTicks() - breakTick) * (budget.particlesPerTick() / 2), seed),
                daughter == 0 ? -0.36D : 0.36D);
        double daughterProgress = frameProgress(tick - breakTick, budget.emissionTicks() - breakTick);
        return center.add(direction.scale(radius * 0.64D * daughterProgress));
    }

    private static Offset worldGrid(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int globalIndex = tick * budget.particlesPerTick() + index;
        int line = Math.floorMod(globalIndex, 14);
        double radius = definition.envelope().maximumRadiusBlocks()
                * (0.36D + 0.57D * frameProgress(tick, budget.emissionTicks()));
        if (line < 6) {
            double latitude = -0.92D + line * 0.368D;
            double longitude = TWO_PI * (globalIndex / 14 + unit(seed) * 0.35D) / 12.0D;
            return spherical(radius, latitude, longitude);
        }
        double longitude = TWO_PI * (line - 6) / 8.0D;
        double latitude = -1.26D + TWO_PI * (globalIndex / 14 + unit(seed) * 0.22D) / 18.0D;
        return spherical(radius, latitude, longitude);
    }

    private static Offset stoutComet(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        int bundle = index % 18;
        int bead = index / 18;
        Offset direction = fibonacciDirection(bundle, 18, seed);
        Offset side = perpendicular(direction);
        double reach = definition.envelope().maximumRadiusBlocks()
                * (0.28D + 0.50D * frameProgress(tick, definition.particleBudget().emissionTicks()))
                * (0.88D + bead * 0.018D);
        double spread = (bead - 3.5D) * definition.envelope().maximumRadiusBlocks() * 0.016D;
        return direction.scale(reach).add(side.scale(spread));
    }

    private static Offset apertureHexReveal(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int revealStart = 16;
        double radius = definition.envelope().maximumRadiusBlocks();
        if (tick < revealStart) {
            int petal = index % 6;
            int bead = index / 6;
            double angle = petal * TWO_PI / 6.0D + unit(seed) * 0.12D;
            double opening = radius * (0.18D + 0.55D * frameProgress(tick, revealStart));
            double tangent = ((bead % 14) - 6.5D) * radius * 0.030D;
            double vertical = Math.sin((bead / 14) * 0.78D + unit(seed) * TWO_PI) * radius * 0.14D;
            return new Offset(
                    Math.cos(angle) * opening - Math.sin(angle) * tangent,
                    vertical,
                    Math.sin(angle) * opening + Math.cos(angle) * tangent);
        }
        int spoke = index % 6;
        double angle = spoke * TWO_PI / 6.0D + unit(seed) * 0.16D;
        double innerProgress = frameProgress(tick - revealStart, budget.emissionTicks() - revealStart);
        double localRadius = radius * (0.10D + 0.34D * innerProgress);
        double vertical = Math.sin((index / 6) * 0.74D + unit(seed) * TWO_PI) * radius * 0.11D;
        return new Offset(Math.cos(angle) * localRadius, vertical, Math.sin(angle) * localRadius);
    }

    private static Offset orbitalNucleus(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int orbit = index % 3;
        int point = tick * (budget.particlesPerTick() / 3) + index / 3;
        double phase = unit(seed ^ DIRECTION_SALT) * TWO_PI;
        double angle = TWO_PI * point / (budget.emissionTicks() * (budget.particlesPerTick() / 3))
                + phase
                + tick * (0.18D + orbit * 0.025D);
        double radius = definition.envelope().maximumRadiusBlocks();
        Offset loop = new Offset(
                Math.cos(angle) * radius * 0.58D,
                Math.sin(angle * 2.0D + phase) * radius * 0.18D,
                Math.sin(angle) * radius * 0.34D);
        Offset centeredLoop = rotateAroundY(loop, -0.62D + orbit * 0.62D);
        Offset nucleusOffset = fibonacciDirection(orbit, 3, seed ^ LAYER_SALT).scale(radius * 0.10D);
        return centeredLoop.add(nucleusOffset);
    }

    private static Offset interwovenRadiance(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int strand = index & 1;
        int globalIndex = tick * (budget.particlesPerTick() / 2) + index / 2;
        Offset direction = fibonacciDirection(globalIndex, budget.emissionTicks() * (budget.particlesPerTick() / 2), seed);
        Offset side = perpendicular(direction);
        double radius = definition.envelope().maximumRadiusBlocks()
                * (0.20D + 0.74D * frameProgress(tick, budget.emissionTicks()));
        double weave = Math.sin(TWO_PI * (frameProgress(tick, budget.emissionTicks()) * 1.5D + globalIndex / 17.0D))
                * definition.envelope().maximumRadiusBlocks()
                * 0.12D;
        return direction.scale(radius).add(side.scale(strand == 0 ? weave : -weave));
    }

    private static Offset polarLantern(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int lobe = index % 8;
        int bead = tick * (budget.particlesPerTick() / 8) + index / 8;
        double frame = frameProgress(tick, budget.emissionTicks());
        double angle = lobe * TWO_PI / 8.0D + unit(seed) * 0.18D;
        double sign = (bead & 1) == 0 ? 1.0D : -1.0D;
        double radius = definition.envelope().maximumRadiusBlocks();
        double horizontal = radius * (0.16D + 0.40D * Math.sin(Math.PI * frame));
        double vertical = sign * radius * (0.22D + 0.50D * frame);
        return new Offset(Math.cos(angle) * horizontal, vertical, Math.sin(angle) * horizontal);
    }

    private static Offset eclipseArcSplit(
            LargeExtraFireworkDefinition definition, long seed, int tick, int index) {
        LargeExtraFireworkDefinition.ParticleBudget budget = definition.particleBudget();
        int splitStart = 17;
        double radius = definition.envelope().maximumRadiusBlocks();
        if (tick < splitStart) {
            int arc = index & 1;
            double arcProgress = (index / 2 + frameProgress(tick, splitStart) * (budget.particlesPerTick() / 2))
                    / (budget.particlesPerTick() / 2.0D);
            double angle = (arc == 0 ? 0.30D : Math.PI - 0.30D) + arcProgress * Math.PI * 0.82D;
            Offset center = new Offset(arc == 0 ? -radius * 0.16D : radius * 0.16D, 0.0D, 0.0D);
            Offset crescent = new Offset(
                    Math.cos(angle) * radius * 0.55D,
                    Math.sin(angle) * radius * 0.31D,
                    Math.sin(angle * 1.8D + unit(seed) * 0.18D) * radius * 0.10D);
            return center.add(crescent);
        }
        int fragment = index % 4;
        double signX = (fragment & 1) == 0 ? -1.0D : 1.0D;
        double signY = fragment < 2 ? -1.0D : 1.0D;
        Offset endpoint = new Offset(signX * radius * 0.49D, signY * radius * 0.19D, 0.0D);
        Offset spread = fibonacciDirection(index / 4, budget.particlesPerTick() / 4, seed)
                .scale(radius * 0.23D * frameProgress(tick - splitStart, budget.emissionTicks() - splitStart));
        return endpoint.add(spread);
    }

    private static ColorLayer colorLayer(
            LargeExtraFireworkDefinition.EffectPath path, int tick, int index, int emissionTicks) {
        return switch (path) {
            case GLOBE_SHELL -> tick < 3 ? ColorLayer.ACCENT : tick % 4 == 0 ? ColorLayer.SECONDARY : ColorLayer.PRIMARY;
            case TRIPLE_TIER_RADIANCE -> switch (Math.floorMod(index + tick, 3)) {
                case 0 -> ColorLayer.PRIMARY;
                case 1 -> ColorLayer.SECONDARY;
                default -> ColorLayer.ACCENT;
            };
            case DUAL_BREAK -> tick < emissionTicks / 2
                    ? ColorLayer.PRIMARY
                    : (index & 1) == 0 ? ColorLayer.SECONDARY : ColorLayer.ACCENT;
            case WORLD_GRID -> index % 14 < 6 ? ColorLayer.PRIMARY : index % 3 == 0 ? ColorLayer.ACCENT : ColorLayer.SECONDARY;
            case STOUT_COMET -> index / 18 >= 6 ? ColorLayer.ACCENT : index / 18 >= 3 ? ColorLayer.SECONDARY : ColorLayer.PRIMARY;
            case APERTURE_HEX_REVEAL -> tick >= 16 ? ColorLayer.ACCENT : tick % 3 == 0 ? ColorLayer.SECONDARY : ColorLayer.PRIMARY;
            case ORBITAL_NUCLEUS -> index % 3 == 0 ? ColorLayer.PRIMARY : index % 3 == 1 ? ColorLayer.SECONDARY : ColorLayer.ACCENT;
            case INTERWOVEN_RADIANCE -> (index & 1) == 0 ? ColorLayer.PRIMARY : ColorLayer.SECONDARY;
            case POLAR_LANTERN -> index % 8 == 0 ? ColorLayer.ACCENT : (index & 1) == 0 ? ColorLayer.PRIMARY : ColorLayer.SECONDARY;
            case ECLIPSE_ARC_SPLIT -> tick >= 17 ? ColorLayer.ACCENT : (index & 1) == 0 ? ColorLayer.PRIMARY : ColorLayer.SECONDARY;
        };
    }

    private static boolean isCore(
            LargeExtraFireworkDefinition.EffectPath path, int tick, int index, int emissionTicks) {
        return switch (path) {
            case GLOBE_SHELL -> tick == 0 && index < 12;
            case TRIPLE_TIER_RADIANCE -> tick < 3 && Math.floorMod(index + tick, 3) == 0;
            case DUAL_BREAK -> tick >= emissionTicks / 2 && index % 48 == 0;
            case WORLD_GRID -> index % 96 == 0;
            case STOUT_COMET -> tick < 2 && index % 18 == 0;
            case APERTURE_HEX_REVEAL -> tick >= 16;
            case ORBITAL_NUCLEUS -> index % 30 == 0;
            case INTERWOVEN_RADIANCE -> tick < 2 && index < 8;
            case POLAR_LANTERN -> index % 24 == 0;
            case ECLIPSE_ARC_SPLIT -> tick >= 17 && index % 21 == 0;
        };
    }

    private static int lifetime(LargeExtraFireworkDefinition.ParticleBudget budget, long sampleSeed) {
        int range = budget.maxLifetimeTicks() - budget.minLifetimeTicks();
        return budget.minLifetimeTicks() + (int) Math.floor(unit(sampleSeed ^ LIFETIME_SALT) * (range + 1));
    }

    private static float scaleFor(ColorLayer layer) {
        return switch (layer) {
            case PRIMARY -> 1.08F;
            case SECONDARY -> 1.14F;
            case ACCENT -> 1.20F;
        };
    }

    private static LargeExtraFireworkDefinition.Rgb colorFor(
            LargeExtraFireworkDefinition.Palette palette, ColorLayer layer) {
        return switch (layer) {
            case PRIMARY -> palette.primary();
            case SECONDARY -> palette.secondary();
            case ACCENT -> palette.accent();
        };
    }

    static Offset clampToEnvelope(Offset offset, double maximumRadiusBlocks) {
        double length = offset.length();
        if (length <= maximumRadiusBlocks || length <= ENVELOPE_EPSILON) {
            return offset;
        }
        return offset.scale(maximumRadiusBlocks / length);
    }

    private static Offset fibonacciDirection(int index, int count, long seed) {
        if (count <= 0) {
            throw new IllegalArgumentException("Large Extra direction count must be positive");
        }
        int boundedIndex = Math.floorMod(index, count);
        double y = 1.0D - 2.0D * ((boundedIndex + 0.5D) / count);
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double angle = boundedIndex * GOLDEN_ANGLE + unit(seed ^ DIRECTION_SALT) * TWO_PI;
        return new Offset(horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
    }

    private static Offset spherical(double radius, double latitude, double longitude) {
        double horizontal = Math.cos(latitude);
        return new Offset(
                horizontal * Math.cos(longitude) * radius,
                Math.sin(latitude) * radius,
                horizontal * Math.sin(longitude) * radius);
    }

    private static Offset rotateAroundY(Offset value, double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return new Offset(
                value.x() * cosine - value.z() * sine,
                value.y(),
                value.x() * sine + value.z() * cosine);
    }

    private static Offset perpendicular(Offset direction) {
        double horizontalLength = Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z());
        return horizontalLength <= ENVELOPE_EPSILON
                ? new Offset(1.0D, 0.0D, 0.0D)
                : new Offset(-direction.z() / horizontalLength, 0.0D, direction.x() / horizontalLength);
    }

    private static double frameProgress(int tick, int totalTicks) {
        if (totalTicks <= 0) {
            throw new IllegalArgumentException("Large Extra frame count must be positive");
        }
        return Math.max(0.0D, Math.min(1.0D, (tick + 1.0D) / totalTicks));
    }

    private static long sampleSeed(long burstSeed, String id, int tick, int index) {
        long idSalt = (long) id.hashCode() << 32 ^ id.hashCode();
        return mix64(burstSeed ^ idSalt ^ ((long) tick * 0x9E3779B97F4A7C15L)
                ^ ((long) index * 0xD1B54A32D192ED03L));
    }

    private static double unit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        return mixed ^ (mixed >>> 31);
    }

    public enum ColorLayer {
        PRIMARY,
        SECONDARY,
        ACCENT
    }

    /** One world-relative spark placement. The caller uses zero velocity, so this program never extends its envelope. */
    public record Emission(
            double x,
            double y,
            double z,
            ColorLayer layer,
            float scale,
            int lifetimeTicks,
            boolean core,
            boolean twinkles) {
        public Emission {
            if (!Double.isFinite(x)
                    || !Double.isFinite(y)
                    || !Double.isFinite(z)
                    || layer == null
                    || !Float.isFinite(scale)
                    || scale <= 0.0F
                    || lifetimeTicks <= 0) {
                throw new IllegalArgumentException("Large Extra emission fields must be finite and positive");
            }
        }
    }

    /** The integration owner passes an existing compact burst's id, coordinates, and seed into this client-only type. */
    public record Request(String fireworkId, double x, double y, double z, long seed) {
        public Request {
            if (fireworkId == null || fireworkId.isBlank()
                    || !Double.isFinite(x)
                    || !Double.isFinite(y)
                    || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Large Extra client request must have a stable id and finite position");
            }
            LargeExtraFireworkCatalog.require(fireworkId);
        }
    }

    /**
     * One finite effect instance. The coordinator calls {@link #tick(Minecraft)} once per physical-client tick and
     * removes the instance once it returns {@code true}.
     */
    public static final class Program {
        private final Request request;
        private final LargeExtraFireworkDefinition definition;
        private int age;
        private int scheduledParticles;
        private int createdParticles;

        private Program(Request request, LargeExtraFireworkDefinition definition) {
            this.request = request;
            this.definition = definition;
        }

        public boolean tick(Minecraft minecraft) {
            if (minecraft == null || minecraft.level == null) {
                return false;
            }
            if (this.age < this.definition.particleBudget().emissionTicks()) {
                List<Emission> emissions = emissionsAtTick(this.definition, this.request, this.age);
                this.scheduledParticles += emissions.size();
                if (this.scheduledParticles > this.definition.particleBudget().plannedParticles()) {
                    throw new IllegalStateException("Large Extra program exceeded its declared total particle budget");
                }
                for (Emission emission : emissions) {
                    createParticle(minecraft, emission);
                }
            }
            this.age++;
            return this.complete();
        }

        public Request request() {
            return this.request;
        }

        public LargeExtraFireworkDefinition definition() {
            return this.definition;
        }

        public int age() {
            return this.age;
        }

        public int scheduledParticles() {
            return this.scheduledParticles;
        }

        public int createdParticles() {
            return this.createdParticles;
        }

        public boolean complete() {
            return this.age >= this.definition.particleBudget().totalVisualTicks();
        }

        private void createParticle(Minecraft minecraft, Emission emission) {
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft, emission.x(), emission.y(), emission.z(), 0.0D, 0.0D, 0.0D);
            if (spark == null) {
                return;
            }
            LargeExtraFireworkDefinition.Rgb color = colorFor(this.definition.palette(), emission.layer());
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            FireworkParticleAppearance.applyVisibilityScale(spark, emission.scale(), emission.core());
            FireworkParticleAppearance.applyVividColor(
                    spark,
                    color.red(),
                    color.green(),
                    color.blue(),
                    emission.core() ? 1.08F : 1.02F,
                    emission.core() ? 0.20F : emission.layer() == ColorLayer.ACCENT ? 0.12F : 0.06F);
            spark.setLifetime(emission.lifetimeTicks());
            if (emission.twinkles() && spark instanceof FireworkParticles.SparkParticle fireworkSpark) {
                fireworkSpark.setTwinkle(true);
            }
            this.createdParticles++;
        }
    }

    private record Offset(double x, double y, double z) {
        private Offset {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Large Extra offset must be finite");
            }
        }

        private Offset add(Offset other) {
            return new Offset(this.x + other.x, this.y + other.y, this.z + other.z);
        }

        private Offset scale(double multiplier) {
            return new Offset(this.x * multiplier, this.y * multiplier, this.z * multiplier);
        }

        private double length() {
            return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }
    }
}
