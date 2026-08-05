package com.urbanforma.fireworks.content.batch01;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Integration-ready source of truth for the first ordinary radiant batch.
 *
 * <p>This catalog intentionally owns no {@code FireworkStyle} index, registry holder, language map, recipe JSON,
 * model JSON, or creative-tab mutation. The designated integration owner consumes these definitions in one shared
 * wiring change after all ordinary batches are complete.</p>
 */
public final class Batch01RadiantCatalog {
    public static final String BATCH_ID = "batch01";
    public static final int ORDINARY_STYLE_COUNT = 20;
    /** The first two ordinary additions are hybrid and Saturn; only Saturn's #4FD4D0 teal consumes cold capacity. */
    public static final List<String> ORDINARY_BASELINE_IDS = List.of(
            "hybrid_amber_sphere_radiant", "saturn_amber_double_sphere");
    public static final int ORDINARY_BASELINE_STYLE_COUNT = 2;
    public static final int ORDINARY_COLD_COLOR_BASELINE = 1;
    public static final String ORDINARY_COLD_COLOR_BASELINE_SOURCE =
            "saturn_amber_double_sphere accent #4FD4D0 teal";
    public static final int ORDINARY_COLD_COLOR_CAP = 20;
    /** All five ordinary batches together may consume no more than this many further cold-color style slots. */
    public static final int FIVE_BATCH_REMAINING_COLD_COLOR_CAP =
            ORDINARY_COLD_COLOR_CAP - ORDINARY_COLD_COLOR_BASELINE;
    /** batch01 contains no green, blue, or cyan in any primary, secondary, or accent role. */
    public static final int COLD_COLOR_STYLE_COUNT = 0;
    public static final int PURPLE_ACCENT_STYLE_COUNT = 5;
    public static final int VISUAL_IDENTITY_COUNT = ORDINARY_STYLE_COUNT;
    public static final String CREATIVE_SECTION_KEY = "gui.urbanforma_fireworks.section.fireworks.radiant";
    public static final String ITEM_MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final String PARTICLE_TYPE = "minecraft:firework";
    public static final String RADIANT_TRAJECTORY_CONTRACT =
            "com.urbanforma.fireworks.content.RadiantTrajectory";
    public static final String ORDINARY_CLIENT_SCHEDULER_CONTRACT =
            "com.urbanforma.fireworks.client.GrandFireworkClientEffects";

    private static final ParticlePlan STANDARD_RADIANT_PLAN = new ParticlePlan(
            160, 30, 4_800, 160, 30, 58, 62, 720, 15_000, 108);
    private static final Boundary STANDARD_RADIANT_BOUNDARY = new Boundary(
            100, 108, 108, 48.0D, 9.0D, 0.94D, 0.38D, 0.46D, 4_800, 160, 30);
    private static final ReuseContract STANDARD_REUSE_CONTRACT = new ReuseContract(
            ITEM_MODEL_PARENT,
            PARTICLE_TYPE,
            RADIANT_TRAJECTORY_CONTRACT,
            ORDINARY_CLIENT_SCHEDULER_CONTRACT,
            "STANDARD",
            false,
            false);

    private static final List<Definition> DEFINITIONS = List.of(
            radiant(
                    "vermilion_imperial_gold_radiant",
                    "朱红御金放射烟花",
                    "Vermilion Imperial Gold Radiant Firework",
                    "#E5261F", "#FF8A1D", "#FFE7B0",
                    visual("outward_petal_shell", "外张花瓣壳、短亮核、稠密中层与短金尾",
                            VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.DENSITY, VisualAxis.TRAIL),
                    "minecraft:red_dye", "minecraft:orange_dye", "minecraft:yellow_dye"),
            radiant(
                    "scarlet_champagne_radiant",
                    "绯红香槟放射烟花",
                    "Scarlet Champagne Radiant Firework",
                    "#F0152F", "#FFB45B", "#FFF3D6",
                    visual("double_break_shell", "双层错峰开爆：内环先亮、外环后开、暖白尾端收束",
                            VisualAxis.LAYERING, VisualAxis.CADENCE, VisualAxis.CORE, VisualAxis.TRAIL),
                    "minecraft:red_dye", "minecraft:orange_dye", "minecraft:white_dye"),
            radiant(
                    "cinnabar_sunfire_radiant",
                    "朱砂日焰放射烟花",
                    "Cinnabar Sunfire Radiant Firework",
                    "#D61F1E", "#FF6C19", "#FFD45A",
                    visual("needle_sunburst_shell", "细针日芒从紧凑核心直射，外圈稀疏并快速熄止",
                            VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.DENSITY, VisualAxis.TRAIL),
                    "minecraft:red_dye", "minecraft:orange_dye", "minecraft:yellow_dye"),
            radiant(
                    "ruby_gilded_radiant",
                    "鎏金红宝放射烟花",
                    "Gilded Ruby Radiant Firework",
                    "#C9183E", "#F44D24", "#FFE1A3",
                    visual("dense_heart_shell", "高密红宝心核向外渐疏，金色只留在最远端",
                            VisualAxis.CORE, VisualAxis.DENSITY, VisualAxis.LAYERING, VisualAxis.COLOR_PAIRING),
                    "minecraft:red_dye", "minecraft:orange_dye", "minecraft:yellow_dye"),
            radiant(
                    "crimson_ivory_radiant",
                    "赤霞象牙白放射烟花",
                    "Crimson Ivory Radiant Firework",
                    "#E91E38", "#FF7440", "#FFF5DE",
                    visual("split_crown_shell", "上下分层冠形外壳：顶端更亮、下缘更疏、象牙白尾端",
                            VisualAxis.SHAPE, VisualAxis.LAYERING, VisualAxis.DENSITY, VisualAxis.TRAIL),
                    "minecraft:red_dye", "minecraft:orange_dye", "minecraft:white_dye"),
            radiant(
                    "ember_honey_radiant",
                    "余烬蜜金放射烟花",
                    "Ember Honey Radiant Firework",
                    "#F04A1D", "#FF9B21", "#FFD96A",
                    visual("falling_comet_shell", "粗短放射束在末端轻坠，保留蜜金彗尾而不进入垂柳队列",
                            VisualAxis.TRAIL, VisualAxis.SHAPE, VisualAxis.CADENCE, VisualAxis.COLOR_PAIRING),
                    "minecraft:orange_dye", "minecraft:yellow_dye", "minecraft:white_dye"),
            radiant(
                    "coral_topaz_radiant",
                    "珊瑚黄玉放射烟花",
                    "Coral Topaz Radiant Firework",
                    "#FF5A42", "#FF9A20", "#FFE2A8",
                    visual("peony_ring_shell", "紧凑暖核外包一层中密芍药环，最外圈留出呼吸间隙",
                            VisualAxis.CORE, VisualAxis.LAYERING, VisualAxis.DENSITY, VisualAxis.SHAPE),
                    "minecraft:orange_dye", "minecraft:yellow_dye", "minecraft:white_dye"),
            radiant(
                    "saffron_sunrise_radiant",
                    "藏红日出放射烟花",
                    "Saffron Sunrise Radiant Firework",
                    "#FF870C", "#FFBE1D", "#FFF0A4",
                    visual("rising_halo_shell", "中心先凝成小光环，再扩为完整球壳；亮度由内向外递减",
                            VisualAxis.CADENCE, VisualAxis.LAYERING, VisualAxis.CORE, VisualAxis.DENSITY),
                    "minecraft:orange_dye", "minecraft:yellow_dye", "minecraft:white_dye"),
            radiant(
                    "amber_crown_radiant",
                    "琥珀金冠放射烟花",
                    "Amber Crown Radiant Firework",
                    "#FF7417", "#FFB51F", "#FFF1C1",
                    visual("tiered_crown_shell", "三层金冠由短核、厚中层和细长外缘组成，层间节奏清晰",
                            VisualAxis.LAYERING, VisualAxis.CORE, VisualAxis.DENSITY, VisualAxis.CADENCE),
                    "minecraft:orange_dye", "minecraft:yellow_dye", "minecraft:white_dye"),
            radiant(
                    "tangerine_pearl_radiant",
                    "橘金珍珠放射烟花",
                    "Tangerine Pearl Radiant Firework",
                    "#FF651B", "#FFA83A", "#FFF6D8",
                    visual("pearl_tip_shell", "中等密度橘金放射束以暖白珍珠端点收尾，核心刻意留空",
                            VisualAxis.TRAIL, VisualAxis.DENSITY, VisualAxis.CORE, VisualAxis.COLOR_PAIRING),
                    "minecraft:orange_dye", "minecraft:yellow_dye", "minecraft:white_dye"),
            radiant(
                    "marigold_copper_radiant",
                    "金盏铜辉放射烟花",
                    "Marigold Copper Radiant Firework",
                    "#F58A18", "#D36A2A", "#FFE3A3",
                    visual("copper_palm_tip_shell", "铜色粗束从低密内层推出，金盏亮点集中在掌状外端",
                            VisualAxis.SHAPE, VisualAxis.DENSITY, VisualAxis.TRAIL, VisualAxis.LAYERING),
                    "minecraft:orange_dye", "minecraft:brown_dye", "minecraft:yellow_dye"),
            radiant(
                    "golden_azalea_radiant",
                    "金蕊杜鹃放射烟花",
                    "Golden Azalea Radiant Firework",
                    "#FFAC18", "#FF6C4A", "#FFF0B8",
                    visual("azalea_cluster_shell", "明亮金蕊分成不等大小的外簇，珊瑚过渡层打破均匀球面",
                            VisualAxis.SHAPE, VisualAxis.LAYERING, VisualAxis.DENSITY, VisualAxis.COLOR_PAIRING),
                    "minecraft:yellow_dye", "minecraft:orange_dye", "minecraft:white_dye"),
            radiant(
                    "solar_white_gold_radiant",
                    "日耀白金放射烟花",
                    "Solar White Gold Radiant Firework",
                    "#F5AA18", "#FFE06D", "#FFFFFF",
                    visual("white_hot_core_shell", "暖白高亮核心先冲出，金色主壳随后铺开，尾端极短以保持锐利",
                            VisualAxis.CORE, VisualAxis.CADENCE, VisualAxis.TRAIL, VisualAxis.LAYERING),
                    "minecraft:yellow_dye", "minecraft:orange_dye", "minecraft:white_dye"),
            radiant(
                    "rose_gold_dawn_radiant",
                    "玫瑰金曙光放射烟花",
                    "Rose Gold Dawn Radiant Firework",
                    "#E97668", "#FFB05B", "#FFF1D7",
                    visual("rose_petal_shell", "玫瑰金长短交错的花瓣束围绕柔和核心展开，外缘低密",
                            VisualAxis.SHAPE, VisualAxis.CORE, VisualAxis.DENSITY, VisualAxis.TRAIL),
                    "minecraft:pink_dye", "minecraft:orange_dye", "minecraft:white_dye"),
            radiant(
                    "garnet_champagne_radiant",
                    "石榴石香槟放射烟花",
                    "Garnet Champagne Radiant Firework",
                    "#A91945", "#E96B38", "#FFE6B6",
                    visual("garnet_ring_shell", "深红短核、橙金中环、香槟外环按三拍展开，环间留黑",
                            VisualAxis.LAYERING, VisualAxis.CADENCE, VisualAxis.CORE, VisualAxis.DENSITY),
                    "minecraft:red_dye", "minecraft:orange_dye", "minecraft:white_dye"),
            radiant(
                    Palette.PURPLE_ACCENT,
                    "amethyst_gold_radiant",
                    "紫晶鎏金放射烟花",
                    "Amethyst Gilded Radiant Firework",
                    "#7941C9", "#E58A2A", "#FFE8B4",
                    visual("amethyst_lantern_shell", "紫晶内灯芯包在金色外壳中，外壳密度高于核心并有短尾",
                            VisualAxis.CORE, VisualAxis.LAYERING, VisualAxis.DENSITY, VisualAxis.TRAIL),
                    "minecraft:purple_dye", "minecraft:orange_dye", "minecraft:yellow_dye"),
            radiant(
                    Palette.PURPLE_ACCENT,
                    "violet_champagne_radiant",
                    "紫罗兰香槟放射烟花",
                    "Violet Champagne Radiant Firework",
                    "#8E46CF", "#E7856A", "#FFF1DA",
                    visual("violet_chrysanthemum_shell", "紫罗兰细丝形成菊形壳，香槟端点在最后一拍才显现",
                            VisualAxis.SHAPE, VisualAxis.TRAIL, VisualAxis.CADENCE, VisualAxis.DENSITY),
                    "minecraft:purple_dye", "minecraft:pink_dye", "minecraft:white_dye"),
            radiant(
                    Palette.PURPLE_ACCENT,
                    "orchid_amber_radiant",
                    "兰紫琥珀放射烟花",
                    "Orchid Amber Radiant Firework",
                    "#B04AD2", "#FF9A27", "#FFEAB5",
                    visual("orchid_halo_shell", "兰紫核心外设琥珀晕环，外层疏于中层，形成显著环隙",
                            VisualAxis.CORE, VisualAxis.LAYERING, VisualAxis.DENSITY, VisualAxis.SHAPE),
                    "minecraft:magenta_dye", "minecraft:orange_dye", "minecraft:yellow_dye"),
            radiant(
                    Palette.PURPLE_ACCENT,
                    "plum_sunstone_radiant",
                    "梅紫日光石放射烟花",
                    "Plum Sunstone Radiant Firework",
                    "#6D2E89", "#D96A35", "#FFE0A4",
                    visual("plum_droplet_shell", "梅紫主束在外缘拆为低密小滴，日光石色只提亮断点",
                            VisualAxis.TRAIL, VisualAxis.DENSITY, VisualAxis.CADENCE, VisualAxis.COLOR_PAIRING),
                    "minecraft:purple_dye", "minecraft:red_dye", "minecraft:yellow_dye"),
            radiant(
                    Palette.PURPLE_ACCENT,
                    "lilac_gold_radiant",
                    "丁香金辉放射烟花",
                    "Lilac Gold Radiant Firework",
                    "#A566D1", "#F2B63E", "#FFF2CC",
                    visual("lilac_lace_shell", "丁香色细密内壳托起金辉外壳，暖白尾端构成轻薄蕾丝边",
                            VisualAxis.LAYERING, VisualAxis.DENSITY, VisualAxis.TRAIL, VisualAxis.SHAPE),
                    "minecraft:purple_dye", "minecraft:yellow_dye", "minecraft:white_dye"));

    static {
        if (DEFINITIONS.size() != ORDINARY_STYLE_COUNT) {
            throw new IllegalStateException("batch01 must contain exactly 20 ordinary radiant definitions");
        }
        if (ORDINARY_BASELINE_IDS.size() != ORDINARY_BASELINE_STYLE_COUNT
                || FIVE_BATCH_REMAINING_COLD_COLOR_CAP != 19) {
            throw new IllegalStateException("ordinary cold-color baseline drifted");
        }
        Set<String> ids = new HashSet<>();
        Set<String> structuralSignatures = new HashSet<>();
        Set<String> visualDifferences = new HashSet<>();
        Definition previous = null;
        for (Definition definition : DEFINITIONS) {
            if (!ids.add(definition.id())) {
                throw new IllegalStateException("Duplicate batch01 firework id " + definition.id());
            }
            if (!structuralSignatures.add(definition.visualIdentity().structuralSignature())
                    || !visualDifferences.add(definition.visualDifference())) {
                throw new IllegalStateException("Batch01 visual identity must be unique: " + definition.id());
            }
            if (previous != null
                    && previous.visualIdentity().structuralSignature()
                            .equals(definition.visualIdentity().structuralSignature())
                    && previous.colors().equals(definition.colors())) {
                throw new IllegalStateException("Adjacent batch01 entries may not repeat form and palette");
            }
            previous = definition;
        }
        if (structuralSignatures.size() != VISUAL_IDENTITY_COUNT) {
            throw new IllegalStateException("batch01 visual identity count drifted");
        }
        long purpleCount = DEFINITIONS.stream()
                .filter(definition -> definition.palette() == Palette.PURPLE_ACCENT)
                .count();
        if (purpleCount != PURPLE_ACCENT_STYLE_COUNT) {
            throw new IllegalStateException("batch01 purple accent count drifted");
        }
        long coldCount = DEFINITIONS.stream()
                .filter(definition -> definition.palette().countsTowardColdColorCap())
                .count();
        if (coldCount != COLD_COLOR_STYLE_COUNT
                || ORDINARY_COLD_COLOR_BASELINE + coldCount > ORDINARY_COLD_COLOR_CAP) {
            throw new IllegalStateException("batch01 cold-color budget drifted");
        }
    }

    private Batch01RadiantCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    private static Definition radiant(
            String id,
            String zhName,
            String enName,
            String primaryHex,
            String secondaryHex,
            String accentHex,
            VisualIdentity visualIdentity,
            String primaryDye,
            String secondaryDye,
            String accentDye) {
        return radiant(
                Palette.WARM,
                id,
                zhName,
                enName,
                primaryHex,
                secondaryHex,
                accentHex,
                visualIdentity,
                primaryDye,
                secondaryDye,
                accentDye);
    }

    private static Definition radiant(
            Palette palette,
            String id,
            String zhName,
            String enName,
            String primaryHex,
            String secondaryHex,
            String accentHex,
            VisualIdentity visualIdentity,
            String primaryDye,
            String secondaryDye,
            String accentDye) {
        return new Definition(
                id,
                zhName,
                enName,
                EffectType.RADIANT,
                palette,
                new ColorPalette(primaryHex, secondaryHex, accentHex),
                visualIdentity.visualDifference(),
                visualIdentity,
                new RecipeFields(
                        "urbanforma_fireworks:batch01/" + id,
                        List.of("minecraft:paper", "minecraft:gunpowder", primaryDye, secondaryDye, accentDye),
                        false),
                CREATIVE_SECTION_KEY,
                STANDARD_REUSE_CONTRACT,
                STANDARD_RADIANT_PLAN,
                STANDARD_RADIANT_BOUNDARY);
    }

    public enum EffectType {
        RADIANT
    }

    /** Purple is deliberately tracked separately from the user-defined cool-color cap. */
    public enum Palette {
        WARM(false),
        PURPLE_ACCENT(false);

        private final boolean countsTowardColdColorCap;

        Palette(boolean countsTowardColdColorCap) {
            this.countsTowardColdColorCap = countsTowardColdColorCap;
        }

        public boolean countsTowardColdColorCap() {
            return this.countsTowardColdColorCap;
        }
    }

    /** Non-color visual axes that the integrating renderer must preserve for each ordinary style. */
    public enum VisualAxis {
        SHAPE,
        CORE,
        TRAIL,
        LAYERING,
        CADENCE,
        DENSITY,
        COLOR_PAIRING
    }

    /** A style is rejected when its only asserted difference is a color pairing. */
    public record VisualIdentity(String structuralSignature, String visualDifference, List<VisualAxis> differingAxes) {
        public VisualIdentity {
            if (structuralSignature == null || !structuralSignature.matches("[a-z0-9_]+")) {
                throw new IllegalArgumentException("Visual structural signature must be a lowercase resource key");
            }
            requireText(visualDifference, "visualDifference");
            differingAxes = List.copyOf(Objects.requireNonNull(differingAxes, "differingAxes"));
            if (differingAxes.isEmpty() || new HashSet<>(differingAxes).size() != differingAxes.size()
                    || differingAxes.stream().noneMatch(axis -> axis != VisualAxis.COLOR_PAIRING)) {
                throw new IllegalArgumentException("Batch01 visual identity requires a non-color difference");
            }
        }
    }

    private static VisualIdentity visual(String structuralSignature, String visualDifference, VisualAxis... differingAxes) {
        return new VisualIdentity(structuralSignature, visualDifference, List.of(differingAxes));
    }

    public record Definition(
            String id,
            String zhName,
            String enName,
            EffectType effectType,
            Palette palette,
            ColorPalette colors,
            String visualDifference,
            VisualIdentity visualIdentity,
            RecipeFields recipeFields,
            String creativeSectionKey,
            ReuseContract reuseContract,
            ParticlePlan particlePlan,
            Boundary expectedBoundary) {
        public Definition {
            if (id == null || !id.matches("[a-z0-9_]+")) {
                throw new IllegalArgumentException("Batch01 id must be a lowercase resource path");
            }
            requireText(zhName, "zhName");
            requireText(enName, "enName");
            Objects.requireNonNull(effectType, "effectType");
            Objects.requireNonNull(palette, "palette");
            Objects.requireNonNull(colors, "colors");
            requireText(visualDifference, "visualDifference");
            Objects.requireNonNull(visualIdentity, "visualIdentity");
            if (!visualDifference.equals(visualIdentity.visualDifference())) {
                throw new IllegalArgumentException("Definition visual difference must match its visual identity");
            }
            Objects.requireNonNull(recipeFields, "recipeFields");
            if (!CREATIVE_SECTION_KEY.equals(creativeSectionKey)) {
                throw new IllegalArgumentException("Batch01 styles belong only in the radiant creative section");
            }
            Objects.requireNonNull(reuseContract, "reuseContract");
            Objects.requireNonNull(particlePlan, "particlePlan");
            Objects.requireNonNull(expectedBoundary, "expectedBoundary");
        }
    }

    public record ColorPalette(String primaryHex, String secondaryHex, String accentHex) {
        public ColorPalette {
            validateHex(primaryHex, "primaryHex");
            validateHex(secondaryHex, "secondaryHex");
            validateHex(accentHex, "accentHex");
            if (primaryHex.equalsIgnoreCase(secondaryHex)
                    || primaryHex.equalsIgnoreCase(accentHex)
                    || secondaryHex.equalsIgnoreCase(accentHex)) {
                throw new IllegalArgumentException("Radiant palettes require three distinct colors");
            }
        }
    }

    /** Metadata fields only: the accepted product contract creates no data-pack recipe for these items. */
    public record RecipeFields(String recipeKey, List<String> ingredients, boolean generateDataRecipe) {
        public RecipeFields {
            requireText(recipeKey, "recipeKey");
            ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients"));
            if (ingredients.size() != 5 || ingredients.stream().anyMatch(ingredient -> !ingredient.startsWith("minecraft:"))) {
                throw new IllegalArgumentException("Batch01 recipe metadata requires paper, gunpowder, and three dyes");
            }
            if (generateDataRecipe) {
                throw new IllegalArgumentException("Batch01 must not create recipe JSON without product approval");
            }
        }
    }

    public record ReuseContract(
            String itemModelParent,
            String particleType,
            String trajectoryContract,
            String clientSchedulerContract,
            String effectCategory,
            boolean createsParticleType,
            boolean usesRadiantWillowQueue) {
        public ReuseContract {
            if (!ITEM_MODEL_PARENT.equals(itemModelParent)
                    || !PARTICLE_TYPE.equals(particleType)
                    || !RADIANT_TRAJECTORY_CONTRACT.equals(trajectoryContract)
                    || !ORDINARY_CLIENT_SCHEDULER_CONTRACT.equals(clientSchedulerContract)
                    || !"STANDARD".equals(effectCategory)
                    || createsParticleType
                    || usesRadiantWillowQueue) {
                throw new IllegalArgumentException("Batch01 may only reuse the bounded ordinary radiant contracts");
            }
        }
    }

    public record ParticlePlan(
            int branches,
            int segmentsPerBranch,
            int totalParticles,
            int particlesPerTick,
            int emissionTicks,
            int minLifetimeTicks,
            int maxLifetimeTicks,
            int sharedMaxParticlesPerTick,
            int sharedMaxOwnedParticles,
            int approvedFullEnvelope) {
        public ParticlePlan {
            if (branches != 160 || segmentsPerBranch != 30 || totalParticles != 4_800
                    || particlesPerTick != 160 || emissionTicks != 30 || minLifetimeTicks != 58
                    || maxLifetimeTicks != 62 || sharedMaxParticlesPerTick != 720
                    || sharedMaxOwnedParticles != 15_000 || approvedFullEnvelope != 108) {
                throw new IllegalArgumentException("Batch01 radiant particle plan must reuse the approved bounded template");
            }
        }
    }

    public record Boundary(
            int flightTicks,
            int nominalDiameter,
            int fullEnvelope,
            double maximumRadius,
            double terminalDrop,
            double verticalScale,
            double bendStartMin,
            double bendStartMax,
            int totalParticles,
            int branches,
            int segmentsPerBranch) {
        public Boundary {
            if (flightTicks != 100 || nominalDiameter != 108 || fullEnvelope != 108
                    || maximumRadius != 48.0D || terminalDrop != 9.0D || verticalScale != 0.94D
                    || bendStartMin != 0.38D || bendStartMax != 0.46D || totalParticles != 4_800
                    || branches != 160 || segmentsPerBranch != 30) {
                throw new IllegalArgumentException("Batch01 radiant boundary must equal the approved radiant template");
            }
        }
    }

    private static void validateHex(String value, String field) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException(field + " must be a #RRGGBB value");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
