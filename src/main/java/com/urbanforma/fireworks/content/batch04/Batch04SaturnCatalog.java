package com.urbanforma.fireworks.content.batch04;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.Boundary;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.ColorFamily;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.EffectType;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.Ingredient;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.ModelContract;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.ParticleContract;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.RecipeFields;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.RingTopology;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.VisualAxis;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework.VisualDifference;
import com.urbanforma.fireworks.content.saturn.SaturnColorBand;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import com.urbanforma.fireworks.content.saturn.SaturnRingConfiguration;
import com.urbanforma.fireworks.content.saturn.SaturnRingSpec;
import com.urbanforma.fireworks.content.saturn.SaturnSphereLayer;
import com.urbanforma.fireworks.content.saturn.SaturnSphereSampler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.Vec3;

/**
 * Twenty unregistered ordinary Saturn-ring programs for batch04.
 *
 * <p>All entries are warm or purple palettes, so this batch consumes zero of the ordinary-series green/blue/cyan
 * cold-color allocation. Registry ids, numeric style indices, item models, recipes, language maps, and client event
 * wiring remain owned by the integration thread.</p>
 */
public final class Batch04SaturnCatalog {
    public static final int ORDINARY_ENTRY_COUNT = 20;
    /** Current ordinary baseline: hybrid amber plus Saturn amber, whose teal accent consumes one cold slot. */
    public static final int EXISTING_ORDINARY_ENTRY_COUNT = 2;
    public static final int EXISTING_COLD_COLOR_ENTRY_COUNT = 1;
    public static final int ORDINARY_COLD_COLOR_LIMIT = 20;
    public static final int COLD_COLOR_REMAINING_BEFORE_BATCH =
            ORDINARY_COLD_COLOR_LIMIT - EXISTING_COLD_COLOR_ENTRY_COUNT;
    public static final String CREATIVE_TARGET = "urbanforma_fireworks:fireworks/saturn";

    private static final int MAX_PER_TICK = 480;
    private static final int MAX_OWNED_PARTICLES = 3_600;
    private static final ModelContract VANILLA_ROCKET_MODEL =
            new ModelContract("vanilla_firework_rocket", "minecraft:item/firework_rocket");
    private static final ParticleContract STANDARD_SATURN_PARTICLES = new ParticleContract(
            "minecraft:firework",
            "SaturnClientPlan",
            EffectCategory.STANDARD,
            MAX_PER_TICK,
            MAX_OWNED_PARTICLES,
            false,
            false);
    private static final SaturnSphereSampler FIBONACCI_SAMPLER = (seed, sampleIndex, sampleCount) -> {
        double y = 1.0D - 2.0D * ((double) sampleIndex + 0.5D) / sampleCount;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double phase = (seed & 0xFFFFL) * 0.0001D;
        double angle = sampleIndex * Math.PI * (3.0D - Math.sqrt(5.0D)) + phase;
        return new Vec3(horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
    };

    private static final List<Ingredient> RECIPE_INGREDIENTS_BASE = List.of(
            new Ingredient("P", "minecraft:paper"),
            new Ingredient("F", "minecraft:firework_star"),
            new Ingredient("S", "minecraft:gold_nugget"),
            new Ingredient("G", "minecraft:gunpowder"));

    public static final List<Batch04SaturnFirework> ALL = List.of(
            firework(
                    "batch04_saturn_amber_single_halo",
                    "琥珀单球光环土星环烟花",
                    "Amber Halo Single-Sphere Saturn Ring Firework",
                    EffectType.SINGLE_SPHERE_SATURN,
                    RingTopology.SINGLE_EQUATORIAL,
                    48.0D,
                    52.0D,
                    110,
                    "#FF8A1A", "#FFB347", "#FFF1B8", "minecraft:orange_dye"),
            firework(
                    "batch04_saturn_cinnabar_single_orbit",
                    "朱砂单球双倾轨道土星环烟花",
                    "Cinnabar Twin-Orbit Single-Sphere Saturn Ring Firework",
                    EffectType.SINGLE_SPHERE_SATURN,
                    RingTopology.TWIN_TILTED,
                    49.0D,
                    55.0D,
                    126,
                    "#E33226", "#FF8A1A", "#FFE0A3", "minecraft:red_dye"),
            firework(
                    "batch04_saturn_saffron_solar_cross",
                    "藏红日耀交叉轨道土星环烟花",
                    "Saffron Solar Cross-Orbit Saturn Ring Firework",
                    EffectType.SINGLE_SPHERE_SATURN,
                    RingTopology.CROSSED_PAIR,
                    50.0D,
                    54.0D,
                    122,
                    "#FF9D16", "#F6532C", "#FFF0B7", "minecraft:yellow_dye"),
            firework(
                    "batch04_saturn_rose_gold_single_belt",
                    "玫瑰金单球宽带土星环烟花",
                    "Rose Gold Wide-Belt Single-Sphere Saturn Ring Firework",
                    EffectType.SINGLE_SPHERE_SATURN,
                    RingTopology.WIDE_EQUATORIAL,
                    47.0D,
                    51.0D,
                    116,
                    "#D88782", "#F2B8A0", "#FFF4DE", "minecraft:pink_dye"),
            purpleFirework(
                    "batch04_saturn_violet_lantern_narrow_belt",
                    "紫罗兰灯辉窄带土星环烟花",
                    "Violet Lantern Narrow-Belt Single-Sphere Saturn Ring Firework",
                    EffectType.SINGLE_SPHERE_SATURN,
                    RingTopology.NARROW_BELT,
                    46.0D,
                    48.0D,
                    100,
                    "#7A36C9", "#D95DEA", "#FFE0FF", "minecraft:purple_dye"),
            firework(
                    "batch04_saturn_coral_champagne_offset_double",
                    "珊瑚香槟双球偏转双环土星环烟花",
                    "Coral Champagne Offset Double-Sphere Saturn Ring Firework",
                    EffectType.DOUBLE_SPHERE_SATURN,
                    RingTopology.OFFSET_TWIN,
                    51.0D,
                    57.0D,
                    128,
                    "#FF6C4A", "#FFD48A", "#FFF6D6", "minecraft:orange_dye"),
            firework(
                    "batch04_saturn_ruby_gilded_crown_double",
                    "红宝石鎏金双球冠环土星环烟花",
                    "Ruby Gilded Crown Double-Sphere Saturn Ring Firework",
                    EffectType.DOUBLE_SPHERE_SATURN,
                    RingTopology.CROWNED_TRIPLE,
                    53.0D,
                    58.0D,
                    136,
                    "#D51B38", "#F4B22B", "#FFF0B0", "minecraft:red_dye"),
            purpleFirework(
                    "batch04_saturn_orchid_gold_lattice_double",
                    "兰花金双球网格土星环烟花",
                    "Orchid Gold Lattice Double-Sphere Saturn Ring Firework",
                    EffectType.DOUBLE_SPHERE_SATURN,
                    RingTopology.LATTICE_FOUR,
                    52.0D,
                    57.0D,
                    138,
                    "#9C4FD4", "#FFC14D", "#FFF0BF", "minecraft:magenta_dye"),
            firework(
                    "batch04_saturn_crimson_amethyst_split_double",
                    "绯红紫晶双球分离环土星环烟花",
                    "Crimson Amethyst Split-Double Saturn Ring Firework",
                    EffectType.DOUBLE_SPHERE_SATURN,
                    RingTopology.SPLIT_DOUBLE,
                    49.0D,
                    56.0D,
                    128,
                    "#E63B50", "#A64EE6", "#FFE3FF", "minecraft:magenta_dye"),
            firework(
                    "batch04_saturn_peony_pearl_diagonal_double",
                    "牡丹珍珠双球斜向三环土星环烟花",
                    "Peony Pearl Diagonal Triple-Ring Double-Sphere Saturn Ring Firework",
                    EffectType.DOUBLE_SPHERE_SATURN,
                    RingTopology.DIAGONAL_TRIPLE,
                    50.0D,
                    58.0D,
                    138,
                    "#EF5478", "#F5D8B6", "#FFF9EA", "minecraft:pink_dye"),
            firework(
                    "batch04_saturn_ember_sunstone_concentric",
                    "余烬日长石三层同心土星环烟花",
                    "Ember Sunstone Concentric Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.TRIPLE_CONCENTRIC,
                    54.0D,
                    60.0D,
                    142,
                    "#F05A2B", "#FFB228", "#FFE3AB", "minecraft:orange_dye"),
            firework(
                    "batch04_saturn_copper_rose_offset",
                    "赤铜玫瑰三层偏移土星环烟花",
                    "Copper Rose Offset Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.DUAL_OFFSET,
                    53.0D,
                    59.0D,
                    140,
                    "#B65338", "#E77D86", "#FFE5CD", "minecraft:brown_dye"),
            purpleFirework(
                    "batch04_saturn_amethyst_pearl_cross",
                    "紫晶珍珠三层交叉四环土星环烟花",
                    "Amethyst Pearl Cross-Quad Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.CROSS_QUAD,
                    52.0D,
                    58.0D,
                    140,
                    "#8B46C9", "#D9C2F0", "#FFF0F9", "minecraft:purple_dye"),
            firework(
                    "batch04_saturn_scarlet_chrysanthemum_nested",
                    "猩红菊花三层嵌套土星环烟花",
                    "Scarlet Chrysanthemum Nested Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.NESTED_TRIPLE,
                    55.0D,
                    61.0D,
                    148,
                    "#E42635", "#F4C236", "#FFF1BE", "minecraft:red_dye"),
            firework(
                    "batch04_saturn_golden_orchid_five_band",
                    "金兰三层五带土星环烟花",
                    "Golden Orchid Five-Band Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.FIVE_BAND,
                    54.0D,
                    60.0D,
                    150,
                    "#F8B51E", "#C34FC6", "#FFF2C5", "minecraft:yellow_dye"),
            firework(
                    "batch04_saturn_coral_violet_polar",
                    "珊瑚紫罗兰三层极环土星环烟花",
                    "Coral Violet Polar-Ring Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.POLAR_RING_PAIR,
                    51.0D,
                    57.0D,
                    122,
                    "#FF7258", "#934ED2", "#FFE4F5", "minecraft:pink_dye"),
            firework(
                    "batch04_saturn_vermilion_champagne_orbital_crown",
                    "朱红香槟三层轨道冠冕土星环烟花",
                    "Vermilion Champagne Orbital-Crown Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.ORBITAL_CROWN,
                    53.0D,
                    60.0D,
                    142,
                    "#E53A27", "#F2B77C", "#FFF5D6", "minecraft:red_dye"),
            firework(
                    "batch04_saturn_ruby_topaz_wide_cross",
                    "红宝石黄玉三层宽交叉土星环烟花",
                    "Ruby Topaz Wide-Cross Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.WIDE_CROSS,
                    52.0D,
                    59.0D,
                    136,
                    "#C92142", "#F0A427", "#FFF1BD", "minecraft:red_dye"),
            firework(
                    "batch04_saturn_rose_amethyst_cascade",
                    "玫瑰紫晶三层级联土星环烟花",
                    "Rose Amethyst Cascading Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.CASCADING_QUAD,
                    55.0D,
                    61.0D,
                    152,
                    "#D95879", "#A951D1", "#FFE3F8", "minecraft:magenta_dye"),
            firework(
                    "batch04_saturn_cinnabar_gilded_crowned_five",
                    "朱砂鎏金三层五冠土星环烟花",
                    "Cinnabar Gilded Crowned-Five Multi-Layer Saturn Ring Firework",
                    EffectType.MULTI_LAYER_SPHERE_SATURN,
                    RingTopology.CROWNED_FIVE,
                    56.0D,
                    62.0D,
                    150,
                    "#E13B2A", "#FFC034", "#FFF0B2", "minecraft:orange_dye"));

    /** Batch04 intentionally uses only warm/purple palettes and therefore consumes zero of the remaining 19 slots. */
    public static final int BATCH_COLD_COLOR_ENTRY_COUNT =
            Math.toIntExact(ALL.stream().filter(Batch04SaturnFirework::consumesColdColorQuota).count());
    public static final int COLD_COLOR_REMAINING_AFTER_BATCH =
            COLD_COLOR_REMAINING_BEFORE_BATCH - BATCH_COLD_COLOR_ENTRY_COUNT;
    private static final Map<String, Batch04SaturnFirework> BY_ID = indexById();

    private Batch04SaturnCatalog() {
    }

    public static List<Batch04SaturnFirework> all() {
        return ALL;
    }

    public static Batch04SaturnFirework require(String id) {
        Batch04SaturnFirework firework = BY_ID.get(id);
        if (firework == null) {
            throw new IllegalArgumentException("Unknown batch04 Saturn id " + id);
        }
        return firework;
    }

    private static Batch04SaturnFirework firework(
            String id,
            String zhName,
            String enName,
            EffectType effectType,
            RingTopology ringTopology,
            double outerSphereRadius,
            double ringRadius,
            int maxEnvelopeBlocks,
            String primaryHex,
            String secondaryHex,
            String accentHex,
            String pigmentItem) {
        return firework(
                ColorFamily.WARM,
                id,
                zhName,
                enName,
                effectType,
                ringTopology,
                outerSphereRadius,
                ringRadius,
                maxEnvelopeBlocks,
                primaryHex,
                secondaryHex,
                accentHex,
                pigmentItem);
    }

    private static Batch04SaturnFirework purpleFirework(
            String id,
            String zhName,
            String enName,
            EffectType effectType,
            RingTopology ringTopology,
            double outerSphereRadius,
            double ringRadius,
            int maxEnvelopeBlocks,
            String primaryHex,
            String secondaryHex,
            String accentHex,
            String pigmentItem) {
        return firework(
                ColorFamily.PURPLE,
                id,
                zhName,
                enName,
                effectType,
                ringTopology,
                outerSphereRadius,
                ringRadius,
                maxEnvelopeBlocks,
                primaryHex,
                secondaryHex,
                accentHex,
                pigmentItem);
    }

    private static Batch04SaturnFirework firework(
            ColorFamily colorFamily,
            String id,
            String zhName,
            String enName,
            EffectType effectType,
            RingTopology ringTopology,
            double outerSphereRadius,
            double ringRadius,
            int maxEnvelopeBlocks,
            String primaryHex,
            String secondaryHex,
            String accentHex,
            String pigmentItem) {
        FireworkStyle.Rgb primary = FireworkStyle.Rgb.fromHex(primaryHex);
        FireworkStyle.Rgb secondary = FireworkStyle.Rgb.fromHex(secondaryHex);
        FireworkStyle.Rgb accent = FireworkStyle.Rgb.fromHex(accentHex);
        SaturnProgram program = new SaturnProgram(
                sphereLayers(effectType, outerSphereRadius),
                ringConfiguration(ringTopology, ringRadius),
                new SaturnProgram.SaturnPalette(primary, secondary, accent),
                new SaturnProgram.SaturnParticleBudget(MAX_PER_TICK, MAX_OWNED_PARTICLES));
        return new Batch04SaturnFirework(
                id,
                zhName,
                enName,
                effectType,
                ringTopology,
                primary,
                secondary,
                accent,
                colorFamily,
                visualDifferenceFor(ringTopology),
                recipe(pigmentItem),
                CREATIVE_TARGET,
                VANILLA_ROCKET_MODEL,
                STANDARD_SATURN_PARTICLES,
                new Boundary(maxEnvelopeBlocks),
                program);
    }

    private static List<SaturnSphereLayer> sphereLayers(EffectType effectType, double outerRadius) {
        List<SaturnSphereLayer> layers = new ArrayList<>();
        layers.add(new SaturnSphereLayer(
                "outer", outerRadius, effectType == EffectType.SINGLE_SPHERE_SATURN ? 1_728 : 1_600,
                0, SaturnColorBand.PRIMARY, 0, 12, 96, FIBONACCI_SAMPLER));
        if (effectType != EffectType.SINGLE_SPHERE_SATURN) {
            layers.add(new SaturnSphereLayer(
                    "inner", outerRadius * 0.48D, effectType == EffectType.DOUBLE_SPHERE_SATURN ? 576 : 640,
                    1, SaturnColorBand.SECONDARY, 2, effectType == EffectType.DOUBLE_SPHERE_SATURN ? 8 : 10,
                    84, FIBONACCI_SAMPLER));
        }
        if (effectType == EffectType.MULTI_LAYER_SPHERE_SATURN) {
            layers.add(new SaturnSphereLayer(
                    "core", outerRadius * 0.22D, 256, 2, SaturnColorBand.ACCENT, 4, 6, 72,
                    FIBONACCI_SAMPLER));
        }
        return List.copyOf(layers);
    }

    private static SaturnRingConfiguration ringConfiguration(RingTopology topology, double radius) {
        return new SaturnRingConfiguration(switch (topology) {
            case SINGLE_EQUATORIAL -> List.of(ring(0, radius, 5.0D, 0.0D, 3, SaturnColorBand.ACCENT, 180, 0, 14));
            case TWIN_TILTED -> List.of(
                    ring(0, radius - 3.0D, 4.8D, 20.0D, 3, SaturnColorBand.PRIMARY, 132, 0, 14),
                    ring(1, radius + 5.0D, 4.5D, -20.0D, 4, SaturnColorBand.ACCENT, 132, 1, 14));
            case CROSSED_PAIR -> List.of(
                    ring(0, radius - 2.0D, 4.5D, 34.0D, 3, SaturnColorBand.SECONDARY, 144, 0, 14),
                    ring(1, radius + 4.0D, 4.5D, -34.0D, 4, SaturnColorBand.ACCENT, 144, 0, 14));
            case WIDE_EQUATORIAL -> List.of(ring(0, radius, 14.0D, 0.0D, 3, SaturnColorBand.ACCENT, 300, 0, 16));
            case NARROW_BELT -> List.of(ring(0, radius, 3.0D, 0.0D, 3, SaturnColorBand.ACCENT, 132, 0, 12));
            case OFFSET_TWIN -> List.of(
                    ring(0, radius - 5.0D, 4.5D, 42.0D, 3, SaturnColorBand.SECONDARY, 128, 0, 14),
                    ring(1, radius + 4.0D, 4.5D, -12.0D, 4, SaturnColorBand.ACCENT, 128, 2, 14));
            case CROWNED_TRIPLE -> List.of(
                    ring(0, radius - 6.0D, 4.5D, 55.0D, 3, SaturnColorBand.PRIMARY, 112, 0, 14),
                    ring(1, radius, 4.5D, 20.0D, 4, SaturnColorBand.SECONDARY, 112, 1, 14),
                    ring(2, radius + 7.0D, 4.5D, -20.0D, 5, SaturnColorBand.ACCENT, 112, 2, 14));
            case LATTICE_FOUR -> List.of(
                    ring(0, radius - 9.0D, 4.5D, 14.0D, 3, SaturnColorBand.PRIMARY, 100, 0, 16),
                    ring(1, radius - 3.0D, 4.5D, -24.0D, 4, SaturnColorBand.SECONDARY, 100, 1, 16),
                    ring(2, radius + 3.0D, 4.5D, 42.0D, 5, SaturnColorBand.ACCENT, 100, 2, 16),
                    ring(3, radius + 9.0D, 4.5D, -52.0D, 6, SaturnColorBand.PRIMARY, 100, 3, 16));
            case SPLIT_DOUBLE -> List.of(
                    ring(0, radius - 3.0D, 7.0D, 8.0D, 3, SaturnColorBand.SECONDARY, 160, 0, 14),
                    ring(1, radius + 6.0D, 3.0D, -42.0D, 4, SaturnColorBand.ACCENT, 120, 2, 12));
            case DIAGONAL_TRIPLE -> List.of(
                    ring(0, radius - 8.0D, 4.5D, 30.0D, 3, SaturnColorBand.PRIMARY, 112, 0, 14),
                    ring(1, radius, 4.5D, 50.0D, 4, SaturnColorBand.SECONDARY, 112, 1, 14),
                    ring(2, radius + 8.0D, 4.5D, -30.0D, 5, SaturnColorBand.ACCENT, 112, 2, 14));
            case TRIPLE_CONCENTRIC -> List.of(
                    ring(0, radius - 8.0D, 4.5D, 18.0D, 3, SaturnColorBand.PRIMARY, 128, 0, 14),
                    ring(1, radius, 4.5D, 18.0D, 4, SaturnColorBand.SECONDARY, 128, 1, 14),
                    ring(2, radius + 8.0D, 4.5D, 18.0D, 5, SaturnColorBand.ACCENT, 128, 2, 14));
            case DUAL_OFFSET -> List.of(
                    ring(0, radius - 4.0D, 4.5D, -48.0D, 3, SaturnColorBand.SECONDARY, 144, 0, 14),
                    ring(1, radius + 8.0D, 4.5D, 22.0D, 4, SaturnColorBand.ACCENT, 144, 2, 14));
            case CROSS_QUAD -> List.of(
                    ring(0, radius - 9.0D, 4.5D, 28.0D, 3, SaturnColorBand.PRIMARY, 104, 0, 16),
                    ring(1, radius - 3.0D, 4.5D, -28.0D, 4, SaturnColorBand.SECONDARY, 104, 1, 16),
                    ring(2, radius + 3.0D, 4.5D, 58.0D, 5, SaturnColorBand.ACCENT, 104, 2, 16),
                    ring(3, radius + 9.0D, 4.5D, -58.0D, 6, SaturnColorBand.PRIMARY, 104, 3, 16));
            case NESTED_TRIPLE -> List.of(
                    ring(0, radius - 10.0D, 4.5D, 12.0D, 3, SaturnColorBand.PRIMARY, 120, 0, 14),
                    ring(1, radius, 4.5D, -18.0D, 4, SaturnColorBand.SECONDARY, 120, 1, 14),
                    ring(2, radius + 10.0D, 4.5D, 30.0D, 5, SaturnColorBand.ACCENT, 120, 2, 14));
            case FIVE_BAND -> List.of(
                    ring(0, radius - 12.0D, 3.5D, 10.0D, 3, SaturnColorBand.PRIMARY, 112, 0, 16),
                    ring(1, radius - 6.0D, 3.5D, 10.0D, 4, SaturnColorBand.SECONDARY, 112, 1, 16),
                    ring(2, radius, 3.5D, 10.0D, 5, SaturnColorBand.ACCENT, 112, 2, 16),
                    ring(3, radius + 6.0D, 3.5D, 10.0D, 6, SaturnColorBand.PRIMARY, 112, 3, 16),
                    ring(4, radius + 12.0D, 3.5D, 10.0D, 7, SaturnColorBand.SECONDARY, 112, 4, 16));
            case POLAR_RING_PAIR -> List.of(
                    ring(0, radius - 2.0D, 4.5D, 70.0D, 3, SaturnColorBand.SECONDARY, 144, 0, 14),
                    ring(1, radius + 1.0D, 4.5D, -70.0D, 4, SaturnColorBand.ACCENT, 144, 2, 14));
            case ORBITAL_CROWN -> List.of(
                    ring(0, radius - 8.0D, 4.5D, 60.0D, 3, SaturnColorBand.PRIMARY, 120, 0, 14),
                    ring(1, radius, 4.5D, 0.0D, 4, SaturnColorBand.SECONDARY, 120, 1, 14),
                    ring(2, radius + 8.0D, 4.5D, -60.0D, 5, SaturnColorBand.ACCENT, 120, 2, 14));
            case WIDE_CROSS -> List.of(
                    ring(0, radius - 4.0D, 12.0D, 38.0D, 3, SaturnColorBand.SECONDARY, 180, 0, 16),
                    ring(1, radius + 3.0D, 12.0D, -38.0D, 4, SaturnColorBand.ACCENT, 180, 2, 16));
            case CASCADING_QUAD -> List.of(
                    ring(0, radius - 12.0D, 4.0D, 8.0D, 3, SaturnColorBand.PRIMARY, 108, 0, 16),
                    ring(1, radius - 4.0D, 4.0D, 24.0D, 4, SaturnColorBand.SECONDARY, 108, 1, 16),
                    ring(2, radius + 4.0D, 4.0D, 40.0D, 5, SaturnColorBand.ACCENT, 108, 2, 16),
                    ring(3, radius + 12.0D, 4.0D, 56.0D, 6, SaturnColorBand.PRIMARY, 108, 3, 16));
            case CROWNED_FIVE -> List.of(
                    ring(0, radius - 10.0D, 4.0D, 50.0D, 3, SaturnColorBand.PRIMARY, 96, 0, 16),
                    ring(1, radius - 5.0D, 4.0D, 25.0D, 4, SaturnColorBand.SECONDARY, 96, 1, 16),
                    ring(2, radius, 4.0D, 0.0D, 5, SaturnColorBand.ACCENT, 96, 2, 16),
                    ring(3, radius + 5.0D, 4.0D, -25.0D, 6, SaturnColorBand.PRIMARY, 96, 3, 16),
                    ring(4, radius + 10.0D, 4.0D, -50.0D, 7, SaturnColorBand.SECONDARY, 96, 4, 16));
        });
    }

    private static SaturnRingSpec ring(
            int index,
            double radius,
            double width,
            double tilt,
            int visualLayer,
            SaturnColorBand colorBand,
            int samples,
            int startTick,
            int emissionTicks) {
        return new SaturnRingSpec(
                index, radius, width, tilt, visualLayer, colorBand, samples, startTick, emissionTicks, 108);
    }

    private static RecipeFields recipe(String pigmentItem) {
        List<Ingredient> ingredients = new ArrayList<>(RECIPE_INGREDIENTS_BASE);
        ingredients.add(new Ingredient("D", pigmentItem));
        return new RecipeFields(pigmentItem, List.of("PDP", "FSF", " G "), ingredients);
    }

    private static Map<String, Batch04SaturnFirework> indexById() {
        if (ALL.size() != ORDINARY_ENTRY_COUNT) {
            throw new IllegalStateException("Batch04 must contain exactly 20 ordinary Saturn entries");
        }
        Map<String, Batch04SaturnFirework> byId = new HashMap<>();
        Batch04SaturnFirework previous = null;
        for (Batch04SaturnFirework firework : ALL) {
            if (byId.put(firework.id(), firework) != null) {
                throw new IllegalStateException("Duplicate batch04 Saturn id " + firework.id());
            }
            if (previous != null
                    && previous.structureSignature().equals(firework.structureSignature())
                    && previous.paletteSignature().equals(firework.paletteSignature())) {
                throw new IllegalStateException("Adjacent batch04 entries may not be color-only variants");
            }
            previous = firework;
        }
        if (BATCH_COLD_COLOR_ENTRY_COUNT > COLD_COLOR_REMAINING_BEFORE_BATCH
                || COLD_COLOR_REMAINING_AFTER_BATCH < 0) {
            throw new IllegalStateException("Batch04 exceeds the remaining ordinary cold-color quota");
        }
        return Map.copyOf(byId);
    }

    private static VisualDifference visualDifferenceFor(RingTopology topology) {
        return switch (topology) {
            case SINGLE_EQUATORIAL -> new VisualDifference(
                    "单一高密度球壳先亮起，赤道光环与明亮内核同步显现，形成紧凑光晕。",
                    List.of(VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.DENSITY));
            case TWIN_TILTED -> new VisualDifference(
                    "双倾轨环相差一拍展开，球壳保持完整，形成先后交错的轨道节奏。",
                    List.of(VisualAxis.SHAPE, VisualAxis.CADENCE));
            case CROSSED_PAIR -> new VisualDifference(
                    "两道交叉斜环切过单球表面，以交叉结构替代单一平环。",
                    List.of(VisualAxis.SHAPE, VisualAxis.LAYERING));
            case WIDE_EQUATORIAL -> new VisualDifference(
                    "宽粒子带覆盖赤道而非细线，外球较疏以凸显横向密度差。",
                    List.of(VisualAxis.SHAPE, VisualAxis.DENSITY));
            case NARROW_BELT -> new VisualDifference(
                    "窄亮带压缩在球壳中心，低环密度让紫色内核轮廓更清晰。",
                    List.of(VisualAxis.CORE, VisualAxis.DENSITY));
            case OFFSET_TWIN -> new VisualDifference(
                    "内外双球在偏移双环之间错时出现，外层与内层的亮度层次分明。",
                    List.of(VisualAxis.LAYERING, VisualAxis.CADENCE));
            case CROWNED_TRIPLE -> new VisualDifference(
                    "三道环由低到高构成冠冕，双球的暖白核心在第二拍补亮。",
                    List.of(VisualAxis.CORE, VisualAxis.LAYERING, VisualAxis.CADENCE));
            case LATTICE_FOUR -> new VisualDifference(
                    "四道不同倾角环织成网格，双球以均匀密度承托交叉面。",
                    List.of(VisualAxis.SHAPE, VisualAxis.DENSITY));
            case SPLIT_DOUBLE -> new VisualDifference(
                    "一宽一窄的分离环分两拍打开，内球保持短而亮的核心层。",
                    List.of(VisualAxis.CORE, VisualAxis.CADENCE, VisualAxis.DENSITY));
            case DIAGONAL_TRIPLE -> new VisualDifference(
                    "三道斜环呈阶梯方向，珍珠内球与外球通过层次而非色差区分。",
                    List.of(VisualAxis.SHAPE, VisualAxis.LAYERING));
            case TRIPLE_CONCENTRIC -> new VisualDifference(
                    "三层球壳外扩时，同倾同心环依次点亮，形成稳定的多层开花节奏。",
                    List.of(VisualAxis.LAYERING, VisualAxis.CADENCE));
            case DUAL_OFFSET -> new VisualDifference(
                    "两道偏移轨环不对称地切开三层球，较亮核心在后段显现。",
                    List.of(VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.CADENCE));
            case CROSS_QUAD -> new VisualDifference(
                    "四环以两组交叉角度包裹三层球，环面密度递增而核心保持紧凑。",
                    List.of(VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.DENSITY));
            case NESTED_TRIPLE -> new VisualDifference(
                    "半径逐级拉开的三环与三层球同向嵌套，强调由内到外的层级深度。",
                    List.of(VisualAxis.LAYERING, VisualAxis.DENSITY));
            case FIVE_BAND -> new VisualDifference(
                    "五条细带以连续但错拍的方式横贯球壳，制造高频层次而非单色填充。",
                    List.of(VisualAxis.LAYERING, VisualAxis.CADENCE, VisualAxis.DENSITY));
            case POLAR_RING_PAIR -> new VisualDifference(
                    "近极角双环竖向包住三层球，改变传统赤道环的轮廓方向。",
                    List.of(VisualAxis.SHAPE, VisualAxis.LAYERING));
            case ORBITAL_CROWN -> new VisualDifference(
                    "上下冠环围绕平赤道环，三层球的暖白中心在中段形成明确亮核。",
                    List.of(VisualAxis.CORE, VisualAxis.LAYERING, VisualAxis.CADENCE));
            case WIDE_CROSS -> new VisualDifference(
                    "两道宽斜环相交形成厚实光带，外层球壳较密、内核较短。",
                    List.of(VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.DENSITY));
            case CASCADING_QUAD -> new VisualDifference(
                    "四环倾角逐级抬升，依次展开的层面构成级联开花节奏。",
                    List.of(VisualAxis.LAYERING, VisualAxis.CADENCE));
            case CROWNED_FIVE -> new VisualDifference(
                    "五道冠环沿垂向轨迹分层错位排布，最大三层球提供饱满外壳与明亮中心。",
                    List.of(VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.LAYERING));
        };
    }
}
