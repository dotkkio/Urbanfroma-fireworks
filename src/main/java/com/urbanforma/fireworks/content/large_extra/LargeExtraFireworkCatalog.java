package com.urbanforma.fireworks.content.large_extra;

import com.urbanforma.fireworks.content.EffectCategory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ten isolated, unregistered Large Fireworks definitions for coordinator-owned shared integration. */
public final class LargeExtraFireworkCatalog {
    public static final String SERIES_ID = "large";
    public static final String BATCH_ID = LargeExtraFireworkDefinition.BATCH_ID;
    public static final String MANIFEST_RESOURCE =
            "assets/urbanforma_fireworks/large_extra/large_extra_manifest.json";
    public static final String INTEGRATION_STATUS = "ISOLATED_NOT_REGISTERED";
    public static final int REQUIRED_ENTRY_COUNT = 10;
    public static final int COOL_COLOR_ENTRY_COUNT = 4;

    private static final List<LargeExtraFireworkDefinition> VALUES = List.of(
            entry(
                    1,
                    "large_extra_aurora_globe_shell",
                    "\u6781\u5149\u5168\u7403\u58f3\u70df\u82b1",
                    "Aurora Globe Shell Firework",
                    LargeExtraFireworkDefinition.EffectPath.GLOBE_SHELL,
                    "#19A8C9", "#64E2D4", "#F0FFF8", true,
                    18, 120, 38, 68, 104, 112),
            entry(
                    2,
                    "large_extra_cinnabar_triple_tier_radiance",
                    "\u6731\u7802\u4e09\u5c42\u653e\u5c04\u70df\u82b1",
                    "Cinnabar Triple-Tier Radiance Firework",
                    LargeExtraFireworkDefinition.EffectPath.TRIPLE_TIER_RADIANCE,
                    "#C9332C", "#F06B32", "#FFE1A3", false,
                    24, 108, 40, 72, 108, 116),
            entry(
                    3,
                    "large_extra_jade_dual_break",
                    "\u7fe1\u7fe0\u53cc\u6bb5\u7834\u88c2\u70df\u82b1",
                    "Jade Dual-Break Firework",
                    LargeExtraFireworkDefinition.EffectPath.DUAL_BREAK,
                    "#1E8E68", "#8CD6A1", "#ECFFF0", true,
                    26, 96, 42, 74, 108, 116),
            entry(
                    4,
                    "large_extra_cobalt_world_grid",
                    "\u94b4\u84dd\u7ecf\u7eac\u5168\u7403\u70df\u82b1",
                    "Cobalt World-Grid Firework",
                    LargeExtraFireworkDefinition.EffectPath.WORLD_GRID,
                    "#2159B6", "#5AA9F5", "#E4F4FF", true,
                    20, 96, 40, 70, 100, 108),
            entry(
                    5,
                    "large_extra_amber_stout_comet",
                    "\u7425\u73c0\u5bc6\u96c6\u77ed\u7c97\u5c04\u70df\u82b1",
                    "Amber Dense Stout-Comet Firework",
                    LargeExtraFireworkDefinition.EffectPath.STOUT_COMET,
                    "#D96B16", "#FFAF2E", "#FFF0B8", false,
                    18, 144, 34, 64, 94, 102),
            entry(
                    6,
                    "large_extra_violet_aperture_hex_reveal",
                    "\u7d2b\u6676\u516d\u74e3\u5f00\u7a97\u70df\u82b1",
                    "Violet Aperture-Hex Reveal Firework",
                    LargeExtraFireworkDefinition.EffectPath.APERTURE_HEX_REVEAL,
                    "#7840B8", "#B57AE7", "#F3E6FF", false,
                    28, 84, 42, 76, 104, 112),
            entry(
                    7,
                    "large_extra_teal_orbital_nucleus",
                    "\u9752\u78a7\u8f68\u9053\u6838\u70df\u82b1",
                    "Teal Orbital-Nucleus Firework",
                    LargeExtraFireworkDefinition.EffectPath.ORBITAL_NUCLEUS,
                    "#0B9E9B", "#57DED0", "#E5FFF9", true,
                    24, 90, 40, 74, 102, 110),
            entry(
                    8,
                    "large_extra_rose_interwoven_radiance",
                    "\u73ab\u7470\u4ea4\u7ec7\u653e\u5c04\u70df\u82b1",
                    "Rose Interwoven-Radiance Firework",
                    LargeExtraFireworkDefinition.EffectPath.INTERWOVEN_RADIANCE,
                    "#D95C8A", "#F39AB5", "#FFF0E9", false,
                    24, 108, 42, 76, 106, 114),
            entry(
                    9,
                    "large_extra_platinum_polar_lantern",
                    "\u94c2\u91d1\u6781\u51a0\u706f\u7b3c\u70df\u82b1",
                    "Platinum Polar-Lantern Firework",
                    LargeExtraFireworkDefinition.EffectPath.POLAR_LANTERN,
                    "#BCC9D7", "#F5E8A8", "#FFFFFF", false,
                    22, 96, 38, 70, 98, 106),
            entry(
                    10,
                    "large_extra_copper_eclipse_arc_split",
                    "\u8d64\u94dc\u8680\u73af\u5206\u88c2\u70df\u82b1",
                    "Copper Eclipse-Arc Split Firework",
                    LargeExtraFireworkDefinition.EffectPath.ECLIPSE_ARC_SPLIT,
                    "#A94D27", "#E28B39", "#FFE2AE", false,
                    26, 84, 40, 72, 100, 108));
    private static final Map<String, LargeExtraFireworkDefinition> BY_ID = indexById();

    static {
        validateCatalog();
    }

    private LargeExtraFireworkCatalog() {
    }

    public static List<LargeExtraFireworkDefinition> values() {
        return VALUES;
    }

    public static LargeExtraFireworkDefinition require(String id) {
        LargeExtraFireworkDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown Large Extra firework " + id);
        }
        return definition;
    }

    public static LargeExtraFireworkDefinition byId(String id) {
        return BY_ID.get(id);
    }

    private static LargeExtraFireworkDefinition entry(
            int stableOrder,
            String id,
            String zhName,
            String enName,
            LargeExtraFireworkDefinition.EffectPath effectPath,
            String primary,
            String secondary,
            String accent,
            boolean cool,
            int emissionTicks,
            int particlesPerTick,
            int minLifetimeTicks,
            int maxLifetimeTicks,
            int nominalDiameterBlocks,
            int fullEnvelopeBlocks) {
        return new LargeExtraFireworkDefinition(
                stableOrder,
                id,
                zhName,
                enName,
                effectPath,
                new LargeExtraFireworkDefinition.Palette(
                        new LargeExtraFireworkDefinition.Rgb(primary),
                        new LargeExtraFireworkDefinition.Rgb(secondary),
                        new LargeExtraFireworkDefinition.Rgb(accent),
                        cool),
                new LargeExtraFireworkDefinition.RecipeContract(
                        List.of(" P ", "FGF", " P "),
                        Map.of(
                                "P", "minecraft:paper",
                                "F", "minecraft:firework_star",
                                "G", "minecraft:gunpowder"),
                        LargeExtraFireworkDefinition.MOD_ID + ":" + id,
                        1),
                new LargeExtraFireworkDefinition.CreativeTarget(
                        LargeExtraFireworkDefinition.LARGE_CREATIVE_SECTION,
                        LargeExtraFireworkDefinition.LARGE_CREATIVE_TRANSLATION_KEY,
                        stableOrder),
                new LargeExtraFireworkDefinition.ReuseContract(
                        LargeExtraFireworkDefinition.MODEL_PARENT,
                        LargeExtraFireworkDefinition.HD_SPARK,
                        LargeExtraFireworkDefinition.VANILLA_FIREWORK_PARTICLE,
                        LargeExtraFireworkDefinition.RECIPE_TEMPLATE,
                        effectPath.clientProgramId(),
                        EffectCategory.STANDARD,
                        false,
                        false,
                        false,
                        false),
                new LargeExtraFireworkDefinition.ParticleBudget(
                        emissionTicks,
                        particlesPerTick,
                        emissionTicks * particlesPerTick,
                        minLifetimeTicks,
                        maxLifetimeTicks),
                new LargeExtraFireworkDefinition.Envelope(
                        LargeExtraFireworkDefinition.CURRENT_LARGE_FLIGHT_TICKS,
                        LargeExtraFireworkDefinition.CURRENT_LARGE_BURST_HEIGHT_BLOCKS,
                        nominalDiameterBlocks,
                        fullEnvelopeBlocks,
                        fullEnvelopeBlocks / 2.0D,
                        "LargeExtraClientPrograms.clampToEnvelope(offset, maximumRadiusBlocks)"),
                LargeExtraVisualSignatures.forId(id));
    }

    private static Map<String, LargeExtraFireworkDefinition> indexById() {
        Map<String, LargeExtraFireworkDefinition> valuesById = new HashMap<>();
        for (LargeExtraFireworkDefinition definition : VALUES) {
            if (valuesById.put(definition.id(), definition) != null) {
                throw new IllegalStateException("Duplicate Large Extra stable id " + definition.id());
            }
        }
        return Map.copyOf(valuesById);
    }

    private static void validateCatalog() {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || LargeExtraVisualSignatures.count() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Large Extra catalog must contain exactly ten entries");
        }
        Set<Integer> orders = new HashSet<>();
        Set<String> zhNames = new HashSet<>();
        Set<String> enNames = new HashSet<>();
        Set<String> palettes = new HashSet<>();
        Set<String> clientPrograms = new HashSet<>();
        Set<LargeExtraFireworkDefinition.EffectPath> paths = new HashSet<>();
        int coolCount = 0;
        for (LargeExtraFireworkDefinition definition : VALUES) {
            if (!orders.add(definition.stableOrder())
                    || !zhNames.add(definition.zhName())
                    || !enNames.add(definition.enName())
                    || !palettes.add(definition.palette().signature())
                    || !clientPrograms.add(definition.reuseContract().clientProgramId())
                    || !paths.add(definition.effectPath())
                    || definition.particleBudget().particlesPerTick()
                            > LargeExtraFireworkDefinition.MAX_LOCAL_PARTICLES_PER_TICK
                    || definition.particleBudget().maxLiveParticles()
                            > LargeExtraFireworkDefinition.MAX_LOCAL_LIVE_PARTICLES
                    || definition.envelope().fullEnvelopeBlocks()
                            > LargeExtraFireworkDefinition.LARGE_MAXIMUM_FULL_ENVELOPE) {
                throw new IllegalStateException("Large Extra uniqueness or local budget contract drifted");
            }
            if (definition.palette().countsTowardCoolColorLedger()) {
                coolCount++;
            }
        }
        for (int expectedOrder = 1; expectedOrder <= REQUIRED_ENTRY_COUNT; expectedOrder++) {
            if (!orders.contains(expectedOrder)) {
                throw new IllegalStateException("Large Extra stable order is not contiguous");
            }
        }
        if (coolCount != COOL_COLOR_ENTRY_COUNT) {
            throw new IllegalStateException("Large Extra cool-color ledger drifted");
        }
        LargeExtraVisualSignatures.validateDefinitions(VALUES);
    }
}
