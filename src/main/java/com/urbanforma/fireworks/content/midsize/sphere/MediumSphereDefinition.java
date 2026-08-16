package com.urbanforma.fireworks.content.midsize.sphere;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Typed, unregistered integration contract for one medium spherical firework.
 *
 * <p>This package intentionally owns no registry, language entry, recipe file, creative section, network payload,
 * server emitter, or shared scheduler. The coordinator maps accepted definitions to those shared surfaces.</p>
 */
public record MediumSphereDefinition(
        String id,
        String zhName,
        String enName,
        Palette palette,
        VisualSignature visualSignature,
        ParticleBudget particleBudget,
        Boundary boundary,
        ParticleContract particleContract,
        ModelContract modelContract,
        RecipeContract recipeContract,
        CreativeContract creativeContract,
        EffectContract effectContract) {
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final String REFERENCE_EFFECT_ID = "grand_golden_sphere_firework";
    public static final int REFERENCE_TOTAL_PARTICLES = 2_160;
    public static final double REFERENCE_FULL_ENVELOPE = 105.0D;
    public static final int REFERENCE_ASCENT_TICKS = 86;
    public static final double SHARED_LAUNCH_SPEED = 1.45D;
    public static final double REFERENCE_DETONATION_HEIGHT = REFERENCE_ASCENT_TICKS * SHARED_LAUNCH_SPEED;
    public static final int MIN_TOTAL_PARTICLES = (int) Math.ceil(REFERENCE_TOTAL_PARTICLES * 0.80D);
    public static final int LOCAL_PEAK_PARTICLE_CAP = 192;
    public static final int LOCAL_OWNED_PARTICLE_CAP = REFERENCE_TOTAL_PARTICLES;
    public static final String HD_FIREWORK_SPARK_FIELD = "HD_FIREWORK_SPARK";
    public static final String HD_FIREWORK_SPARK_KEY = MOD_ID + ":hd_firework_spark";
    public static final String VANILLA_FIREWORK_FALLBACK = "minecraft:firework";
    public static final String VANILLA_ROCKET_MODEL = "minecraft:item/firework_rocket";
    public static final String ORDER_GROUP = "medium_sphere_unregistered";
    public static final String COMMON_TRAJECTORY_CLASS =
            "com.urbanforma.fireworks.content.midsize.sphere.MediumSphereTrajectory";
    public static final String CLIENT_PROGRAM_CLASS =
            "com.urbanforma.fireworks.client.midsize.sphere.MediumSphereClientProgram";

    private static final Pattern STABLE_ID = Pattern.compile("medium_[a-z0-9_]+_sphere_firework");
    private static final List<String> STANDARD_RECIPE_PATTERN = List.of(" P ", "FGF", " P ");
    private static final Map<String, String> STANDARD_RECIPE_KEY = Map.of(
            "P", "minecraft:paper",
            "F", "minecraft:firework_star",
            "G", "minecraft:gunpowder");

    public MediumSphereDefinition {
        if (id == null || !STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Medium sphere id must be a stable medium_*_sphere_firework id");
        }
        requireName(zhName, "zhName");
        requireName(enName, "enName");
        if (!zhName.startsWith("中型") || zhName.contains("中" + "小型") || !enName.startsWith("Medium ")
                || enName.toLowerCase().contains("small-medium")) {
            throw new IllegalArgumentException("Item names must use only the approved Medium or 中型 wording");
        }
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(visualSignature, "visualSignature");
        Objects.requireNonNull(particleBudget, "particleBudget");
        Objects.requireNonNull(boundary, "boundary");
        Objects.requireNonNull(particleContract, "particleContract");
        Objects.requireNonNull(modelContract, "modelContract");
        Objects.requireNonNull(recipeContract, "recipeContract");
        Objects.requireNonNull(creativeContract, "creativeContract");
        Objects.requireNonNull(effectContract, "effectContract");

        if (particleBudget.totalParticles() < MIN_TOTAL_PARTICLES
                || particleBudget.particlesPerTick() > LOCAL_PEAK_PARTICLE_CAP
                || particleBudget.maxOwnedParticles() > LOCAL_OWNED_PARTICLE_CAP
                || !boundary.fitsMediumEnvelope()
                || !boundary.fitsMediumHeight()
                || !particleContract.isExistingClientOnlyHdSpark()
                || !modelContract.isVanillaRocketOnly()
                || !recipeContract.isStandardRocketRecipeFor(id)
                || !creativeContract.isCoordinatorOwnedSphereTarget()
                || !effectContract.isCallerDrivenClientOnly()) {
            throw new IllegalArgumentException("Medium sphere contract drifted for " + id);
        }
    }

    public enum VisualAxis {
        SHAPE,
        CORE,
        TRAIL,
        LAYERING,
        CADENCE,
        DENSITY
    }

    /** Every form remains recognizably spherical while changing the actual deterministic geometry. */
    public enum SphereForm {
        DAHLIA("dahlia sphere"),
        HOLLOW_CHRYSANTHEMUM("hollow chrysanthemum sphere"),
        TWIN_ORB("double sphere"),
        RING_CORE("ring-core sphere"),
        PULSE_SHELL("pulse-shell sphere"),
        CRYSTAL("crystalline sphere"),
        LACE("lace sphere"),
        CROWN("crown sphere"),
        ORBIT("orbit-core sphere"),
        SPLIT("split sphere"),
        LAYERED("layered sphere"),
        PETAL("petal sphere"),
        MIRROR("mirror sphere"),
        STROBE("strobe-shell sphere"),
        COMET("comet-shell sphere"),
        BRAID("braided sphere"),
        ANNULAR("annular sphere"),
        AURORA("aurora sphere"),
        ICE("ice-crystal sphere"),
        MOON("moon-band sphere"),
        LANTERN("lantern-rib sphere"),
        PEONY("peony sphere"),
        MOSAIC("mosaic sphere"),
        CASCADE("cascade sphere"),
        SOLAR("solar corona sphere");

        private final String label;

        SphereForm(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    public enum CoreForm {
        AMBER_BEADS("amber bead rings"),
        HOLLOW_VOID("open hollow center"),
        DUAL_NUCLEI("paired ruby nuclei"),
        ORBITAL_RING("amethyst orbit ring"),
        PEARL_SEED("champagne pearl seed"),
        DIAMOND_HEART("cinnabar diamond heart"),
        ROSE_KNOT("rose lace knot"),
        SUN_KERNEL("saffron sun kernel"),
        EMBER_ORBIT("garnet ember orbit"),
        SPLIT_HEART("vermilion split heart"),
        JADE_KERNEL("emerald jade kernel"),
        PETAL_HEART("sunset petal heart"),
        MIRROR_PEARL("platinum mirror pearl"),
        STROBE_HEART("crimson strobe heart"),
        GOLD_KERNEL("topaz gold kernel"),
        ORCHID_KNOT("violet orchid knot"),
        CORAL_RING("coral annular ring"),
        AURORA_PIN("aurora violet pin"),
        ICE_BEADS("azure ice beads"),
        MOON_SEED("teal moon seed"),
        LANTERN_HEART("scarlet lantern heart"),
        PEONY_HEART("orchid peony heart"),
        MOSAIC_CORE("bronze mosaic core"),
        CASCADE_PEARL("magenta cascade pearl"),
        SOLAR_CORE("silver solar core");

        private final String label;

        CoreForm(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    public enum TrailForm {
        PEARL_TIPS("short pearl tips"),
        BRONZE_STREAMERS("bronze streamer fringe"),
        CRACKLE_EDGES("gold crackle edges"),
        VIOLET_COMETS("violet comet tips"),
        CHAMPAGNE_TWINKLE("champagne twinkle fringe"),
        GOLD_NEEDLES("gold needle trails"),
        LACE_GLINTS("rose lace glints"),
        CROWN_SPARKS("solar crown sparks"),
        GARNET_TRACERS("garnet orbit tracers"),
        PEARL_SPLITS("pearl split terminals"),
        SILVER_TIPS("silver terminal tips"),
        PEACH_PETALS("peach petal terminals"),
        MIRROR_PINS("mirror silver pins"),
        STROBE_FLASHES("champagne strobe flashes"),
        COMET_TAILS("amber comet tails"),
        LILAC_WEAVE("lilac braided glints"),
        IVORY_HALO("ivory annular halo"),
        MAGENTA_VEIL("magenta aurora veil"),
        AQUA_FRINGE("aqua ice fringe"),
        MOON_HALO("pearl moon halo"),
        GILDED_RIBS("gilded lantern ribs"),
        CHAMPAGNE_BLOOM("champagne bloom tips"),
        COPPER_FLECKS("copper mosaic flecks"),
        ROSE_CASCADE("rose cascade terminals"),
        SOLAR_TIPS("gold solar tips");

        private final String label;

        TrailForm(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    public enum LayerForm {
        BEAD_TO_CROWN("bead core, petal shell, clipped crown"),
        HOLLOW_SHELL("void core, hollow shell, pearl rim"),
        TWIN_LOBES("paired inner lobes and shared outer sphere"),
        RING_TO_SHELL("orbit ring, middle shell, outer sphere"),
        PULSE_LAYERS("seed, three pulse layers, bright shell"),
        FACET_BANDS("diamond core, faceted bands, crystal edge"),
        WOVEN_SHELL("lace knot, woven middle, fine rim"),
        RISING_CROWN("sun kernel, rising shell, crown terminals"),
        ORBITAL_NEST("ember orbit, nested shell, tracer rim"),
        SPLIT_LOBES("split core, bifurcated middle, merged rim"),
        TRIPLE_SHELL("jade kernel with three spaced spherical shells"),
        PETAL_BANDS("petal heart, petal bands, peach tips"),
        MIRRORED_SHELL("mirror pearl, reflected shell, silver pins"),
        FLASH_SHELL("strobe heart, flashing shell, warm edge"),
        COMET_ARCS("gold kernel, comet arcs, amber tail rim"),
        BRAIDED_SHELL("orchid knot, torsion braid, lilac rim"),
        ANNULAR_BODY("coral ring, annular body, ivory halo"),
        VEIL_SHELL("aurora pin, veil shell, magenta fringe"),
        ICE_FACETS("ice beads, faceted shell, aqua fringe"),
        MOON_BANDS("moon seed, banded shell, pearl halo"),
        RIBBED_SHELL("lantern heart, ribbed sphere, gilded tips"),
        BLOOM_LAYERS("peony heart, bloom layers, champagne rim"),
        MOSAIC_TILES("mosaic core, tile shell, copper flecks"),
        CASCADE_SHELL("cascade pearl, falling shell, rose rim"),
        SOLAR_CORONA("solar core, corona shell, gold terminals");

        private final String label;

        LayerForm(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    public enum Cadence {
        CORE_OUTWARD("core-outward beats"),
        OUTSIDE_IN("outside-in shell sweep"),
        TWIN_ALTERNATE("alternating twin beats"),
        RING_PULSE("ring-pulse cadence"),
        THREE_BEAT("three-beat pulse cadence"),
        FACET_STAGGER("faceted stagger cadence"),
        SPIRAL_WAVE("spiral lace wave"),
        CROWN_RELEASE("rising crown release"),
        ORBITAL_PULSE("orbital pulse cadence"),
        SPLIT_BEAT("split-and-merge beats"),
        LAYER_SWEEP("triple-layer sweep"),
        PETAL_STAGGER("petal stagger cadence"),
        MIRROR_SWAP("mirrored inward-outward swap"),
        STROBE_BEAT("strobe beat cadence"),
        COMET_CASCADE("comet cascade cadence"),
        TORSION_WAVE("torsion wave cadence"),
        ANNULAR_SWEEP("annular ring sweep"),
        AURORA_SHIMMER("aurora shimmer cadence"),
        FROST_PULSE("frost pulse cadence"),
        MOON_ORBIT("moon orbit cadence"),
        LANTERN_BEAT("lantern beat cadence"),
        BLOOM_WAVE("peony bloom wave"),
        TILE_WAVE("mosaic tile wave"),
        FALLING_CASCADE("falling cascade cadence"),
        SOLAR_RADIATE("solar radiate cadence");

        private final String label;

        Cadence(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }

        public int segmentAtTick(int emissionTick, int emissionTicks) {
            if (emissionTick < 0 || emissionTick >= emissionTicks) {
                throw new IllegalArgumentException("Emission tick is outside the configured cadence");
            }
            return switch (this) {
                case OUTSIDE_IN, MIRROR_SWAP, FALLING_CASCADE -> emissionTicks - 1 - emissionTick;
                case TWIN_ALTERNATE, SPLIT_BEAT -> alternatingIndex(emissionTick, emissionTicks);
                case RING_PULSE, THREE_BEAT, ORBITAL_PULSE, FROST_PULSE, LANTERN_BEAT -> pulseIndex(emissionTick, emissionTicks);
                case FACET_STAGGER, SPIRAL_WAVE, PETAL_STAGGER, TORSION_WAVE, AURORA_SHIMMER,
                        BLOOM_WAVE, TILE_WAVE -> staggeredIndex(emissionTick, emissionTicks);
                default -> emissionTick;
            };
        }

        private static int alternatingIndex(int tick, int count) {
            int evenCount = (count + 1) / 2;
            return tick % 2 == 0 ? tick / 2 : evenCount + tick / 2;
        }

        private static int pulseIndex(int tick, int count) {
            int firstGroup = (count + 2) / 3;
            int secondGroup = (count + 1) / 3;
            int group = tick % 3;
            int offset = tick / 3;
            return switch (group) {
                case 0 -> offset;
                case 1 -> firstGroup + offset;
                default -> firstGroup + secondGroup + offset;
            };
        }

        private static int staggeredIndex(int tick, int count) {
            int stride = count % 2 == 0 ? count - 1 : count - 2;
            return Math.floorMod(tick * Math.max(1, stride), count);
        }
    }

    public enum Density {
        COMPACT("compact full shell"),
        RICH("rich shell density"),
        DENSE("dense shell density"),
        BRIGHT("bright shell density"),
        FULL("full shell density");

        private final String label;

        Density(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    public enum PaletteFamily {
        WARM,
        METALLIC,
        RED_PURPLE,
        COOL
    }

    public record Rgb(float red, float green, float blue) {
        public Rgb {
            validateChannel(red, "red");
            validateChannel(green, "green");
            validateChannel(blue, "blue");
        }

        public static Rgb hex(String hex) {
            if (hex == null || !hex.matches("#[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("Color must be a #RRGGBB value");
            }
            return new Rgb(
                    Integer.parseInt(hex.substring(1, 3), 16) / 255.0F,
                    Integer.parseInt(hex.substring(3, 5), 16) / 255.0F,
                    Integer.parseInt(hex.substring(5, 7), 16) / 255.0F);
        }

        public String hex() {
            return String.format("#%02X%02X%02X", Math.round(red * 255.0F), Math.round(green * 255.0F),
                    Math.round(blue * 255.0F));
        }

        private static void validateChannel(float value, String label) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(label + " channel must be between zero and one");
            }
        }
    }

    public record Palette(Rgb primary, Rgb secondary, Rgb accent, PaletteFamily family, int coolColorCount) {
        public Palette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
            Objects.requireNonNull(family, "family");
            if (coolColorCount < 0 || coolColorCount > 3) {
                throw new IllegalArgumentException("Cool color count must be between zero and three");
            }
            if (family == PaletteFamily.COOL && coolColorCount == 0) {
                throw new IllegalArgumentException("Cool palette must record its cool colors");
            }
            if (family != PaletteFamily.COOL && coolColorCount > 1) {
                throw new IllegalArgumentException("Non-cool palettes may only record a cool accent");
            }
        }

        public String paletteSignature() {
            return primary.hex() + "/" + secondary.hex() + "/" + accent.hex();
        }
    }

    public record VisualSignature(
            SphereForm shape,
            CoreForm core,
            TrailForm trail,
            LayerForm layering,
            Cadence cadence,
            Density density) {
        public VisualSignature {
            Objects.requireNonNull(shape, "shape");
            Objects.requireNonNull(core, "core");
            Objects.requireNonNull(trail, "trail");
            Objects.requireNonNull(layering, "layering");
            Objects.requireNonNull(cadence, "cadence");
            Objects.requireNonNull(density, "density");
        }

        public String value(VisualAxis axis) {
            return switch (Objects.requireNonNull(axis, "axis")) {
                case SHAPE -> shape.label();
                case CORE -> core.label();
                case TRAIL -> trail.label();
                case LAYERING -> layering.label();
                case CADENCE -> cadence.label();
                case DENSITY -> density.label();
            };
        }

        public String structuralSignature() {
            return String.join("|", shape.name(), core.name(), trail.name(), layering.name(), cadence.name(), density.name());
        }
    }

    /** One compact client effect owns this finite budget without relying on a global limiter. */
    public record ParticleBudget(int branchCount, int emissionTicks, int minLifetimeTicks, int maxLifetimeTicks) {
        public ParticleBudget {
            if (branchCount <= 0 || branchCount > LOCAL_PEAK_PARTICLE_CAP || emissionTicks <= 0
                    || minLifetimeTicks < emissionTicks || maxLifetimeTicks < minLifetimeTicks
                    || total(branchCount, emissionTicks) > LOCAL_OWNED_PARTICLE_CAP) {
                throw new IllegalArgumentException("Invalid finite medium sphere particle budget");
            }
        }

        public int totalParticles() {
            return total(branchCount, emissionTicks);
        }

        public int particlesPerTick() {
            return branchCount;
        }

        public int maxOwnedParticles() {
            return totalParticles();
        }

        public int totalVisualTicks() {
            return emissionTicks + maxLifetimeTicks;
        }

        private static int total(int branches, int ticks) {
            try {
                return Math.multiplyExact(branches, ticks);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("Particle total overflow", exception);
            }
        }
    }

    public record Boundary(double maxRadius, int ascentTicks) {
        public Boundary {
            if (!Double.isFinite(maxRadius) || maxRadius <= 0.0D || ascentTicks <= 0) {
                throw new IllegalArgumentException("Medium sphere boundary must be finite and positive");
            }
        }

        public double fullEnvelopeBlocks() {
            return maxRadius * 2.0D;
        }

        public double detonationHeight() {
            return ascentTicks * SHARED_LAUNCH_SPEED;
        }

        public double envelopeRatio() {
            return fullEnvelopeBlocks() / REFERENCE_FULL_ENVELOPE;
        }

        public double heightRatio() {
            return detonationHeight() / REFERENCE_DETONATION_HEIGHT;
        }

        public boolean fitsMediumEnvelope() {
            return envelopeRatio() >= 0.45D && envelopeRatio() <= 0.50D;
        }

        public boolean fitsMediumHeight() {
            return heightRatio() >= 0.80D && heightRatio() < 0.90D && detonationHeight() < REFERENCE_DETONATION_HEIGHT;
        }
    }

    public record ParticleContract(
            String primaryField,
            String primaryRegistryKey,
            String fallbackRegistryKey,
            boolean clientOnly,
            boolean createsParticleType,
            boolean createsServerParticleLoop,
            boolean createsGlobalLimiter) {
        public ParticleContract {
            requireText(primaryField, "primaryField");
            requireText(primaryRegistryKey, "primaryRegistryKey");
            requireText(fallbackRegistryKey, "fallbackRegistryKey");
        }

        public boolean isExistingClientOnlyHdSpark() {
            return HD_FIREWORK_SPARK_FIELD.equals(primaryField) && HD_FIREWORK_SPARK_KEY.equals(primaryRegistryKey)
                    && VANILLA_FIREWORK_FALLBACK.equals(fallbackRegistryKey) && clientOnly && !createsParticleType
                    && !createsServerParticleLoop && !createsGlobalLimiter;
        }
    }

    public record ModelContract(
            String itemModelParent,
            boolean createsModelJson,
            boolean createsTexture,
            boolean customLoader) {
        public ModelContract {
            requireText(itemModelParent, "itemModelParent");
        }

        public boolean isVanillaRocketOnly() {
            return VANILLA_ROCKET_MODEL.equals(itemModelParent) && !createsModelJson && !createsTexture && !customLoader;
        }
    }

    public record RecipeContract(
            List<String> pattern,
            Map<String, String> key,
            String resultId,
            int resultCount,
            boolean createsRecipeFile) {
        public RecipeContract {
            pattern = List.copyOf(Objects.requireNonNull(pattern, "pattern"));
            key = Map.copyOf(Objects.requireNonNull(key, "key"));
            requireText(resultId, "resultId");
            if (resultCount != 1) {
                throw new IllegalArgumentException("Medium rocket recipe result count must be one");
            }
        }

        public boolean isStandardRocketRecipeFor(String id) {
            return STANDARD_RECIPE_PATTERN.equals(pattern) && STANDARD_RECIPE_KEY.equals(key)
                    && (MOD_ID + ":" + id).equals(resultId) && !createsRecipeFile;
        }
    }

    public record CreativeContract(String sphereSegment, String orderGroup, boolean createsCreativeSection) {
        public CreativeContract {
            requireText(sphereSegment, "sphereSegment");
            requireText(orderGroup, "orderGroup");
        }

        public boolean isCoordinatorOwnedSphereTarget() {
            return "sphere".equals(sphereSegment) && ORDER_GROUP.equals(orderGroup) && !createsCreativeSection;
        }
    }

    public record EffectContract(
            String commonTrajectoryClass,
            String clientProgramClass,
            String clientEntryPoint,
            String requestShape,
            boolean clientOnlyEmission,
            boolean createsNetworkPayload,
            boolean createsSharedScheduler,
            boolean createsServerEmitter) {
        public EffectContract {
            requireText(commonTrajectoryClass, "commonTrajectoryClass");
            requireText(clientProgramClass, "clientProgramClass");
            requireText(clientEntryPoint, "clientEntryPoint");
            requireText(requestShape, "requestShape");
        }

        public boolean isCallerDrivenClientOnly() {
            return COMMON_TRAJECTORY_CLASS.equals(commonTrajectoryClass) && CLIENT_PROGRAM_CLASS.equals(clientProgramClass)
                    && "tick(Minecraft)".equals(clientEntryPoint)
                    && "Request(String id, double x, double y, double z, long seed)".equals(requestShape)
                    && clientOnlyEmission && !createsNetworkPayload && !createsSharedScheduler && !createsServerEmitter;
        }
    }

    public static ParticleContract hdSparkContract() {
        return new ParticleContract(
                HD_FIREWORK_SPARK_FIELD,
                HD_FIREWORK_SPARK_KEY,
                VANILLA_FIREWORK_FALLBACK,
                true,
                false,
                false,
                false);
    }

    public static ModelContract vanillaRocketModel() {
        return new ModelContract(VANILLA_ROCKET_MODEL, false, false, false);
    }

    public static RecipeContract standardRecipe(String id) {
        return new RecipeContract(STANDARD_RECIPE_PATTERN, STANDARD_RECIPE_KEY, MOD_ID + ":" + id, 1, false);
    }

    public static CreativeContract coordinatorOwnedSphereTarget() {
        return new CreativeContract("sphere", ORDER_GROUP, false);
    }

    public static EffectContract clientOnlyEffect() {
        return new EffectContract(
                COMMON_TRAJECTORY_CLASS,
                CLIENT_PROGRAM_CLASS,
                "tick(Minecraft)",
                "Request(String id, double x, double y, double z, long seed)",
                true,
                false,
                false,
                false);
    }

    private static void requireName(String value, String field) {
        requireText(value, field);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
