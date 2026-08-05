package com.urbanforma.fireworks.content.batch_other;

import com.urbanforma.fireworks.content.EffectCategory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Fifteen isolated ordinary fireworks with explicit route and handoff contracts. */
public final class BatchOtherCatalog {
    public static final String SERIES_ID = "other";
    public static final String BATCH_ID = "batch_other";
    public static final int REQUIRED_ENTRY_COUNT = 15;
    public static final int ORDINARY_COOL_COLOR_LIMIT = 20;
    public static final int EXISTING_COOL_COLOR_COUNT = 5;
    public static final int BATCH_COOL_COLOR_COUNT = 0;
    public static final int COOL_COLOR_COUNT_AFTER_BATCH = EXISTING_COOL_COLOR_COUNT + BATCH_COOL_COLOR_COUNT;
    public static final String PARTICLE_TYPE = "urbanforma_fireworks:hd_firework_spark";
    public static final String PARTICLE_ENGINE = "GrandFireworkClientEffects.ActiveBurst";
    public static final String MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final String RECIPE_TEMPLATE = "normal_firework_rocket_3x3";
    public static final int ORDINARY_MAXIMUM_ENVELOPE = 120;
    public static final String INTEGRATION_STATUS = "PENDING_PUBLIC_INTEGRATION";

    private static final BatchOtherFirework.ModelContract MODEL_CONTRACT =
            new BatchOtherFirework.ModelContract(
                    MODEL_PARENT,
                    "assets/urbanforma_fireworks/models/item/{id}.json",
                    "vanilla_firework_rocket");
    private static final List<String> RECIPE_PATTERN = List.of(" P ", "FGF", " P ");
    private static final Map<String, String> RECIPE_INGREDIENTS = Map.of(
            "P", "minecraft:paper",
            "F", "minecraft:firework_star",
            "G", "minecraft:gunpowder");

    private static final List<BatchOtherFirework> VALUES = List.of(
            entry(1, "other_ember_chrysanthemum", "赤焰直束烟花", "Ember Straight-Ray Firework",
                    BatchOtherFirework.ProgramRoute.RADIAL_STRAIGHT,
                    palette("#D73524", "#FF8B30", "#FFE0A1"),
                    style(54, 72, 86, 0, 1_400, 160, 640, 200, 126,
                            BatchOtherFirework.TrailTier.COMPACT, 72, 60, 54, 0.35F, 0.55F), 86),
            entry(2, "other_saffron_crossfire", "金线长射烟花", "Saffron Sparse Long-Ray Firework",
                    BatchOtherFirework.ProgramRoute.SPARSE_LONG_RAYS,
                    palette("#F18F01", "#FFD23F", "#FFF2B8"),
                    style(58, 68, 98, 1, 1_500, 100, 700, 200, 108,
                            BatchOtherFirework.TrailTier.COMPACT, 86, 64, 56, 0.35F, 0.58F), 98),
            entry(3, "other_coral_pearl_split", "珊瑚错相双环烟花", "Coral Offset Double-Ring Firework",
                    BatchOtherFirework.ProgramRoute.OFFSET_DOUBLE_RING,
                    palette("#E85D75", "#F4A261", "#FFF1DD"),
                    style(56, 82, 96, 5, 1_700, 350, 500, 150, 144,
                            BatchOtherFirework.TrailTier.STANDARD, 78, 66, 58, 0.35F, 0.60F), 96),
            entry(4, "other_copper_comet_crown", "琥珀三重同心环烟花", "Amber Three-Concentric-Ring Firework",
                    BatchOtherFirework.ProgramRoute.THREE_CONCENTRIC_RINGS,
                    palette("#B94E2D", "#F08A45", "#FFE2B8"),
                    style(60, 88, 108, 3, 2_100, 260, 540, 200, 162,
                            BatchOtherFirework.TrailTier.STANDARD, 84, 70, 62, 0.35F, 0.60F), 108),
            entry(5, "other_ruby_ember_lattice", "玫红环核混合烟花", "Ruby Ring-Core Hybrid Firework",
                    BatchOtherFirework.ProgramRoute.RING_CORE_HYBRID,
                    palette("#B5162C", "#EF6C35", "#FFD08A"),
                    style(57, 86, 102, 4, 1_900, 420, 440, 140, 144,
                            BatchOtherFirework.TrailTier.STANDARD, 82, 68, 60, 0.35F, 0.60F), 102),
            entry(6, "other_rose_champagne_petal", "铜红下垂长尾烟花", "Copper Rose Drooping-Tail Firework",
                    BatchOtherFirework.ProgramRoute.DROOPING_TAILS,
                    palette("#D7657B", "#F2C078", "#FFF4D6"),
                    style(62, 90, 112, 2, 2_200, 180, 600, 220, 180,
                            BatchOtherFirework.TrailTier.GRAND, 92, 76, 68, 0.35F, 0.58F), 112),
            entry(7, "other_plum_garnet_crown", "紫金分层柳条烟花", "Plum Garnet Layered-Willow Firework",
                    BatchOtherFirework.ProgramRoute.LAYERED_WILLOW,
                    palette("#7C2D68", "#C84D6B", "#FFE1C5"),
                    style(66, 84, 116, 7, 2_100, 240, 500, 260, 162,
                            BatchOtherFirework.TrailTier.GRAND, 96, 78, 70, 0.35F, 0.60F), 116),
            entry(8, "other_amethyst_sunwheel", "靛紫螺旋放射烟花", "Amethyst Helical-Radiation Firework",
                    BatchOtherFirework.ProgramRoute.HELICAL_RADIATION,
                    palette("#7E43BF", "#F2B84B", "#FFF0BF"),
                    style(59, 80, 96, 3, 1_800, 220, 580, 200, 144,
                            BatchOtherFirework.TrailTier.STANDARD, 80, 68, 60, 0.35F, 0.60F), 96),
            entry(9, "other_teal_ice_lantern", "朱砂交替脉冲烟花", "Vermilion Alternating-Pulse Firework",
                    BatchOtherFirework.ProgramRoute.ALTERNATING_PULSES,
                    palette("#C44B35", "#E9A23B", "#FFF0B0"),
                    style(56, 78, 90, 6, 1_600, 380, 420, 200, 126,
                            BatchOtherFirework.TrailTier.COMPACT, 76, 62, 56, 0.35F, 0.55F), 90),
            entry(10, "other_cobalt_silver_cross", "银金短厚多层射线烟花", "Silver Gold Thick-Multilayer-Ray Firework",
                    BatchOtherFirework.ProgramRoute.THICK_MULTILAYER_RAYS,
                    palette("#A9792C", "#E7C15B", "#FFF4C2"),
                    style(52, 92, 104, 1, 2_200, 600, 300, 100, 216,
                            BatchOtherFirework.TrailTier.GRAND, 84, 70, 60, 0.35F, 0.60F), 104),
            entry(11, "other_gold_leaf_mosaic", "红玉延迟内核烟花", "Ruby Delayed-Core Shell Firework",
                    BatchOtherFirework.ProgramRoute.DELAYED_CORE_SHELL,
                    palette("#A8324B", "#D97A72", "#FFF0D0"),
                    style(64, 94, 118, 12, 2_300, 350, 500, 150, 180,
                            BatchOtherFirework.TrailTier.GRAND, 90, 74, 64, 0.35F, 0.60F), 118),
            entry(12, "other_vermilion_pearl_blossom", "铂金轨道环烟花", "Platinum Orbital-Ring Firework",
                    BatchOtherFirework.ProgramRoute.ORBITAL_SATURN,
                    palette("#B9C7D6", "#F2D18A", "#FFFFFF"),
                    style(55, 86, 100, 4, 1_900, 300, 550, 150, 162,
                            BatchOtherFirework.TrailTier.STANDARD, 82, 68, 60, 0.35F, 0.60F), 100),
            entry(13, "other_marigold_strobe_crown", "绯红双轨交织烟花", "Crimson Twin-Cross-Orbit Firework",
                    BatchOtherFirework.ProgramRoute.TWIN_CROSS_ORBITS,
                    palette("#C73545", "#E06D92", "#FFE0EA"),
                    style(61, 90, 110, 8, 2_100, 250, 540, 210, 180,
                            BatchOtherFirework.TrailTier.GRAND, 88, 72, 64, 0.35F, 0.60F), 110),
            entry(14, "other_cerise_lattice_double", "金橙分段长射烟花", "Golden Segmented-Ray Firework",
                    BatchOtherFirework.ProgramRoute.SEGMENTED_RAYS,
                    palette("#D58B18", "#F3C14B", "#FFF3BC"),
                    style(58, 74, 88, 2, 1_500, 180, 600, 220, 126,
                            BatchOtherFirework.TrailTier.COMPACT, 78, 64, 56, 0.35F, 0.55F), 88),
            entry(15, "other_bronze_sunburst_sphere", "青铜变色珠链烟花", "Bronze Color-Shift Bead-Chain Firework",
                    BatchOtherFirework.ProgramRoute.COLOR_SHIFT_BEADS,
                    palette("#A95A24", "#E9A23B", "#FFF0B0"),
                    style(63, 88, 114, 10, 2_200, 300, 520, 180, 180,
                            BatchOtherFirework.TrailTier.GRAND, 90, 74, 64, 0.35F, 0.60F), 114));

    private static final Map<String, BatchOtherFirework> BY_ID = indexById();

    static {
        validateCatalog();
    }

    private BatchOtherCatalog() {
    }

    public static List<BatchOtherFirework> values() {
        return VALUES;
    }

    public static BatchOtherFirework byId(String id) {
        BatchOtherFirework value = BY_ID.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Unknown batch_other firework " + id);
        }
        return value;
    }

    private static BatchOtherFirework entry(
            int stableOrder,
            String id,
            String zhName,
            String enName,
            BatchOtherFirework.ProgramRoute route,
            BatchOtherFirework.Palette palette,
            BatchOtherFirework.StyleParameters style,
            int fullEnvelopeBlocks) {
        return new BatchOtherFirework(
                id,
                zhName,
                enName,
                route.compatibilityShape(),
                route.sharedEffectPath(),
                route.clientProgramId(),
                family(stableOrder),
                palette,
                style,
                BatchOtherVisualSignatures.forId(id),
                new BatchOtherFirework.RecipeContract(
                        RECIPE_TEMPLATE, RECIPE_PATTERN, RECIPE_INGREDIENTS, "urbanforma_fireworks:" + id, 1),
                new BatchOtherFirework.CreativeContract(creativeSection(route), "other", stableOrder),
                MODEL_CONTRACT,
                new BatchOtherFirework.ParticleContract(
                        PARTICLE_TYPE,
                        PARTICLE_ENGINE,
                        EffectCategory.STANDARD.name(),
                        style.totalStarCount()),
                new BatchOtherFirework.ExpectedBoundary(
                        style.diameterBlocks(),
                        fullEnvelopeBlocks,
                        ORDINARY_MAXIMUM_ENVELOPE,
                        "BatchOtherClientPrograms." + route.id() + ".fitsWithin(" + fullEnvelopeBlocks + ")"));
    }

    private static BatchOtherFirework.Family family(int stableOrder) {
        return switch ((stableOrder - 1) % 4) {
            case 0 -> BatchOtherFirework.Family.WARM;
            case 1 -> BatchOtherFirework.Family.JEWEL;
            case 2 -> BatchOtherFirework.Family.METALLIC;
            default -> BatchOtherFirework.Family.WARM;
        };
    }

    private static String creativeSection(BatchOtherFirework.ProgramRoute route) {
        return switch (route) {
            case RADIAL_STRAIGHT, SPARSE_LONG_RAYS, HELICAL_RADIATION, ALTERNATING_PULSES, SEGMENTED_RAYS,
                    COLOR_SHIFT_BEADS -> "gui.urbanforma_fireworks.section.fireworks.sphere";
            case OFFSET_DOUBLE_RING, THREE_CONCENTRIC_RINGS, RING_CORE_HYBRID, THICK_MULTILAYER_RAYS,
                    DELAYED_CORE_SHELL, TWIN_CROSS_ORBITS -> "gui.urbanforma_fireworks.section.fireworks.double_sphere";
            case DROOPING_TAILS, LAYERED_WILLOW, ORBITAL_SATURN ->
                    "gui.urbanforma_fireworks.section.fireworks.crown_sphere";
        };
    }

    private static BatchOtherFirework.Palette palette(String primary, String secondary, String accent) {
        return new BatchOtherFirework.Palette(primary, secondary, accent, false);
    }

    private static BatchOtherFirework.StyleParameters style(
            int flightTicks,
            int diameterBlocks,
            int fullEnvelopeBlocks,
            int phaseDelayTicks,
            int totalStarCount,
            int mainLayerPermille,
            int secondaryLayerPermille,
            int accentLayerPermille,
            int starsPerTick,
            BatchOtherFirework.TrailTier trailTier,
            int outerLifetime,
            int innerLifetime,
            int accentLifetime,
            float twinkleChanceMin,
            float twinkleChanceMax) {
        return new BatchOtherFirework.StyleParameters(
                flightTicks, diameterBlocks, fullEnvelopeBlocks, phaseDelayTicks, totalStarCount,
                mainLayerPermille, secondaryLayerPermille, accentLayerPermille, starsPerTick, trailTier,
                outerLifetime, innerLifetime, accentLifetime, twinkleChanceMin, twinkleChanceMax);
    }

    private static Map<String, BatchOtherFirework> indexById() {
        Map<String, BatchOtherFirework> values = new HashMap<>();
        for (BatchOtherFirework value : VALUES) {
            if (values.put(value.id(), value) != null) {
                throw new IllegalStateException("Duplicate batch_other id " + value.id());
            }
        }
        return Map.copyOf(values);
    }

    private static void validateCatalog() {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || BY_ID.size() != REQUIRED_ENTRY_COUNT
                || COOL_COLOR_COUNT_AFTER_BATCH > ORDINARY_COOL_COLOR_LIMIT
                || BATCH_COOL_COLOR_COUNT != 0) {
            throw new IllegalStateException("batch_other count or cool-color budget is invalid");
        }

        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<BatchOtherFirework.ProgramRoute> routes = new HashSet<>();
        for (int index = 0; index < VALUES.size(); index++) {
            BatchOtherFirework value = VALUES.get(index);
            BatchOtherFirework.ProgramRoute route = route(value.clientProgram());
            if (!value.id().matches("other_[a-z0-9_]+") || !ids.add(value.id())
                    || !names.add(value.zhName()) || !names.add(value.enName())
                    || !routes.add(route) || value.creative().stableOrder() != index + 1
                    || value.effectPath().equals("SPHERE") || value.effectPath().equals("DOUBLE_SPHERE")
                    || value.effectPath().equals("CROWN_SPHERE")
                    || !value.clientProgram().equals(route.clientProgramId())
                    || !value.effectPath().equals(route.sharedEffectPath())
                    || value.visualDifference().structuralAxes().size() != 6
                    || !value.visualDifference().computedSignature().equals(value.visualDifference().structureSignature())
                    || value.particle().peakPerBurst() != value.style().totalStarCount()
                    || value.style().flightTicks() > 120
                    || value.style().totalStarCount() > 15_000
                    || value.style().starsPerTick() > 720
                    || value.expectedBoundary().fullEnvelopeBlocks() != value.style().fullEnvelopeBlocks()
                    || value.expectedBoundary().fullEnvelopeBlocks() > ORDINARY_MAXIMUM_ENVELOPE) {
                throw new IllegalStateException("Invalid batch_other entry contract " + value.id());
            }
        }
        if (routes.size() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("batch_other requires one distinct route per stable id");
        }
        BatchOtherVisualSignatures.validateAdjacent(VALUES);
    }

    private static BatchOtherFirework.ProgramRoute route(String programId) {
        for (BatchOtherFirework.ProgramRoute route : BatchOtherFirework.ProgramRoute.values()) {
            if (route.clientProgramId().equals(programId)) {
                return route;
            }
        }
        throw new IllegalStateException("Unknown batch_other client program " + programId);
    }
}
