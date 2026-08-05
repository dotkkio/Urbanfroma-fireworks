package com.urbanforma.fireworks.content.batch02;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.RadiantWillowTrajectory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Isolated source catalog for the second ordinary-firework batch.
 *
 * <p>This is intentionally data only: public registration, translation tables, recipe files, and client scheduling
 * remain owned by the ordinary-firework integration task. Every entry reuses the fixed radiant-willow trajectory,
 * rather than defining an uncapped second effect path.</p>
 */
public final class Batch02RadiantWillowCatalog {
    public static final String BATCH_ID = "batch02";
    public static final int ENTRY_COUNT = 20;
    public static final int ORDINARY_BUDGET_CONSUMPTION = 20;
    public static final int ORDINARY_BUDGET_CAP = 100;
    /** Saturn's existing #4FD4D0 teal is the one ordinary cold-tone baseline entry. */
    public static final int COLD_TONE_BASELINE_USED = 1;
    public static final String COLD_TONE_BASELINE_SOURCE_ID = "saturn_amber_double_sphere";
    public static final int COLD_TONE_ENTRY_COUNT = 0;
    public static final int COLD_TONE_ENTRY_CAP = 20;
    public static final int COLD_TONE_REMAINING_ACROSS_BATCHES =
            COLD_TONE_ENTRY_CAP - COLD_TONE_BASELINE_USED;
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final String CREATIVE_SECTION_KEY = "gui.urbanforma_fireworks.section.fireworks.radiant_willow";

    public static final ReuseContract SHARED_RADIANT_WILLOW_CONTRACT = new ReuseContract(
            "minecraft:item/firework_rocket",
            "urbanforma_fireworks:hd_firework_spark",
            "com.urbanforma.fireworks.content.RadiantWillowTrajectory",
            EffectCategory.RADIANT_WILLOW,
            RadiantWillowTrajectory.RADIANT_NODE_COUNT,
            RadiantWillowTrajectory.MANAGED_NODE_COUNT,
            RadiantWillowTrajectory.NEW_EXTENSION_NODE_COUNT,
            RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE);

    private static final RecipeSpec STANDARD_RECIPE = new RecipeSpec(
            List.of(" P ", "FGF", " P "),
            orderedKey(),
            1);
    private static final ExpectedBounds RADIANT_WILLOW_BOUNDS = new ExpectedBounds(
            RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE,
            RadiantWillowTrajectory.MIN_EXTENSION_TICKS,
            RadiantWillowTrajectory.MAX_EXTENSION_TICKS,
            RadiantWillowTrajectory.MANAGED_NODE_COUNT,
            RadiantWillowTrajectory.MAX_TERMINAL_RETIREMENTS_PER_BRANCH);

    private static final Map<String, VisualDifference> VISUAL_DIFFERENCES = Map.ofEntries(
            Map.entry("batch02_vermilion_gilt_curtain", visual("batch02_vermilion_gilt_curtain", "wide_spherical_curtain", "pearl_seed", "gilt_dots", "three_tier", "slow_open", "medium")),
            Map.entry("batch02_ember_copper_cascade", visual("batch02_ember_copper_cascade", "narrow_curtain", "amber_ring", "copper_filament", "two_tier", "staggered", "dense")),
            Map.entry("batch02_cinnabar_champagne_curtain", visual("batch02_cinnabar_champagne_curtain", "low_arc_cascade", "champagne_core", "short_flicker", "late_secondary", "even", "light")),
            Map.entry("batch02_saffron_coral_curtain", visual("batch02_saffron_coral_curtain", "high_halo_drop", "coral_pin", "split_filament", "primary_lead", "fast_open", "medium")),
            Map.entry("batch02_amber_pearl_curtain", visual("batch02_amber_pearl_curtain", "draped_ribbon", "golden_seed", "pearl_beads", "dense_center", "slow", "dense")),
            Map.entry("batch02_rose_gold_curtain", visual("batch02_rose_gold_curtain", "twin_curtain", "rose_core", "warm_white_stems", "double_band", "alternating", "light")),
            Map.entry("batch02_garnet_topaz_curtain", visual("batch02_garnet_topaz_curtain", "deep_spherical_curtain", "garnet_core", "topaz_flares", "heavy_primary", "delayed_drop", "dense")),
            Map.entry("batch02_ruby_solar_curtain", visual("batch02_ruby_solar_curtain", "sunburst_drop", "ruby_core", "solar_sparks", "accent_endcap", "quick_drop", "medium")),
            Map.entry("batch02_tangerine_ivory_curtain", visual("batch02_tangerine_ivory_curtain", "wide_lantern", "ivory_core", "tangerine_trails", "warm_split", "rolling", "light")),
            Map.entry("batch02_copper_lantern_curtain", visual("batch02_copper_lantern_curtain", "copper_waterfall", "lantern_core", "ember_chain", "dense_outer", "slow_wave", "dense")),
            Map.entry("batch02_carmine_champagne_curtain", visual("batch02_carmine_champagne_curtain", "rose_drape", "carmine_core", "champagne_ticks", "thin_center", "syncopated", "medium")),
            Map.entry("batch02_apricot_gold_curtain", visual("batch02_apricot_gold_curtain", "apricot_spherical_cascade", "gold_core", "peach_filaments", "bright_secondary", "rising", "light")),
            Map.entry("batch02_persimmon_pearl_curtain", visual("batch02_persimmon_pearl_curtain", "pearlescent_drop", "persimmon_core", "white_beads", "three_step", "calm", "medium")),
            Map.entry("batch02_fire_opal_cascade", visual("batch02_fire_opal_cascade", "opal_split", "opal_core", "amber_flicks", "split_outer", "double_pulse", "dense")),
            Map.entry("batch02_magenta_gilt_curtain", visual("batch02_magenta_gilt_curtain", "magenta_bell", "gilt_core", "rose_dots", "magenta_lead", "slow_bloom", "light")),
            Map.entry("batch02_amethyst_rose_curtain", visual("batch02_amethyst_rose_curtain", "amethyst_lace", "rose_pearl", "violet_filaments", "violet_outer", "soft_bloom", "medium")),
            Map.entry("batch02_orchid_champagne_curtain", visual("batch02_orchid_champagne_curtain", "orchid_swoop", "champagne_seed", "orchid_ticks", "late_core", "sweeping", "dense")),
            Map.entry("batch02_wine_gold_curtain", visual("batch02_wine_gold_curtain", "wine_rain", "garnet_seed", "gold_drops", "deep_primary", "measured", "light")),
            Map.entry("batch02_coral_rose_curtain", visual("batch02_coral_rose_curtain", "coral_spherical_curtain", "rose_seed", "coral_filaments", "coral_lead", "bright_bloom", "medium")),
            Map.entry("batch02_sunset_amber_curtain", visual("batch02_sunset_amber_curtain", "sunset_taper", "amber_seed", "golden_taper", "tapered_end", "long_release", "dense")));

    private static final List<Definition> DEFINITIONS = List.of(
            definition("batch02_vermilion_gilt_curtain", "丹朱鎏金放射垂柳", "Vermilion Gilt Curtain Radiant Willow Firework", "#D73522", "#FFAA24", "#FFF0C2"),
            definition("batch02_ember_copper_cascade", "焰铜流瀑放射垂柳", "Ember Copper Cascade Radiant Willow Firework", "#D94A1D", "#D88935", "#FFE0A3"),
            definition("batch02_cinnabar_champagne_curtain", "朱砂香槟放射垂柳", "Cinnabar Champagne Curtain Radiant Willow Firework", "#C83224", "#F2B77B", "#FFF3DC"),
            definition("batch02_saffron_coral_curtain", "藏红珊瑚放射垂柳", "Saffron Coral Curtain Radiant Willow Firework", "#E58B21", "#FF6F3B", "#FFE1AD"),
            definition("batch02_amber_pearl_curtain", "琥珀珠光放射垂柳", "Amber Pearl Curtain Radiant Willow Firework", "#D87A22", "#FFC548", "#FFF4D2"),
            definition("batch02_rose_gold_curtain", "玫瑰金帘放射垂柳", "Rose Gold Curtain Radiant Willow Firework", "#D98A7B", "#FFD9A8", "#FFF4E3"),
            definition("batch02_garnet_topaz_curtain", "石榴黄玉放射垂柳", "Garnet Topaz Curtain Radiant Willow Firework", "#9E2227", "#F59B24", "#FFE4AC"),
            definition("batch02_ruby_solar_curtain", "红宝日冕放射垂柳", "Ruby Solar Curtain Radiant Willow Firework", "#B41E25", "#FF741A", "#FFF0B0"),
            definition("batch02_tangerine_ivory_curtain", "橘霞象牙放射垂柳", "Tangerine Ivory Curtain Radiant Willow Firework", "#E66225", "#FFB15B", "#FFF2D5"),
            definition("batch02_copper_lantern_curtain", "铜灯暖帘放射垂柳", "Copper Lantern Curtain Radiant Willow Firework", "#A84B25", "#F09A47", "#FFE0AC"),
            definition("batch02_carmine_champagne_curtain", "胭脂香槟放射垂柳", "Carmine Champagne Curtain Radiant Willow Firework", "#BA3350", "#F2B66B", "#FFF0D4"),
            definition("batch02_apricot_gold_curtain", "杏金暮帘放射垂柳", "Apricot Gold Curtain Radiant Willow Firework", "#E9944D", "#FFD265", "#FFF4CD"),
            definition("batch02_persimmon_pearl_curtain", "柿红珍珠放射垂柳", "Persimmon Pearl Curtain Radiant Willow Firework", "#D65328", "#F3A06B", "#FFF3D7"),
            definition("batch02_fire_opal_cascade", "火欧泊流瀑放射垂柳", "Fire Opal Cascade Radiant Willow Firework", "#EE5725", "#F5A15B", "#FFF1B8"),
            definition("batch02_magenta_gilt_curtain", "洋红鎏金放射垂柳", "Magenta Gilt Curtain Radiant Willow Firework", "#B52A6C", "#FA9D31", "#FFEAC1"),
            definition("batch02_amethyst_rose_curtain", "紫晶玫瑰放射垂柳", "Amethyst Rose Curtain Radiant Willow Firework", "#833A92", "#F070A1", "#FFEFCA"),
            definition("batch02_orchid_champagne_curtain", "兰花香槟放射垂柳", "Orchid Champagne Curtain Radiant Willow Firework", "#A35291", "#EDB36F", "#FFF0D3"),
            definition("batch02_wine_gold_curtain", "酒红流金放射垂柳", "Wine Gold Curtain Radiant Willow Firework", "#7F233D", "#DB8B45", "#FFE9C5"),
            definition("batch02_coral_rose_curtain", "珊瑚玫瑰放射垂柳", "Coral Rose Curtain Radiant Willow Firework", "#E55752", "#FF9D7C", "#FFF0D8"),
            definition("batch02_sunset_amber_curtain", "晚霞琥珀放射垂柳", "Sunset Amber Curtain Radiant Willow Firework", "#D65A24", "#FFB238", "#FFF3D2"));

    static {
        validateCatalog(DEFINITIONS);
    }

    private Batch02RadiantWillowCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static Definition byId(String id) {
        Objects.requireNonNull(id, "id");
        return DEFINITIONS.stream()
                .filter(definition -> definition.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown batch02 firework " + id));
    }

    public static void validateCatalog(List<Definition> definitions) {
        if (definitions == null || definitions.size() != ENTRY_COUNT
                || ORDINARY_BUDGET_CONSUMPTION != ENTRY_COUNT
                || COLD_TONE_BASELINE_USED + COLD_TONE_ENTRY_COUNT > COLD_TONE_ENTRY_CAP
                || COLD_TONE_REMAINING_ACROSS_BATCHES != 19) {
            throw new IllegalArgumentException("Batch02 budget declaration is invalid");
        }
        Map<String, Definition> byId = new LinkedHashMap<>();
        VisualDifference previousVisual = null;
        Palette previousPalette = null;
        for (Definition definition : definitions) {
            if (definition == null || byId.putIfAbsent(definition.id(), definition) != null) {
                throw new IllegalArgumentException("Batch02 requires unique non-null definitions");
            }
            validateDefinition(definition);
            if (previousVisual != null
                    && (previousVisual.structureSignature().equals(definition.visualDifference().structureSignature())
                            || previousPalette.equals(definition.palette()))) {
                throw new IllegalArgumentException(
                        "Adjacent batch02 entries must change structure and palette: " + definition.id());
            }
            previousVisual = definition.visualDifference();
            previousPalette = definition.palette();
        }
    }

    private static Definition definition(
            String id, String zhName, String enName, String primary, String secondary, String accent) {
        return new Definition(
                id,
                zhName,
                enName,
                EffectType.RADIANT_WILLOW,
                new Palette(primary, secondary, accent),
                visualDifferenceFor(id),
                STANDARD_RECIPE,
                CreativeTabTarget.RADIANT_WILLOW_SECTION,
                SHARED_RADIANT_WILLOW_CONTRACT,
                RADIANT_WILLOW_BOUNDS);
    }

    private static void validateDefinition(Definition definition) {
        if (!definition.id().matches("batch02_[a-z0-9_]+")
                || definition.zhName().isBlank()
                || definition.enName().isBlank()
                || definition.effectType() != EffectType.RADIANT_WILLOW
                || definition.creativeTabTarget() != CreativeTabTarget.RADIANT_WILLOW_SECTION
                || definition.visualDifference() == null
                || !definition.reuseContract().equals(SHARED_RADIANT_WILLOW_CONTRACT)
                || !definition.expectedBounds().equals(RADIANT_WILLOW_BOUNDS)) {
            throw new IllegalArgumentException("Batch02 definition violates the radiant-willow contract: " + definition.id());
        }
    }

    private static VisualDifference visual(
            String id, String form, String core, String tail, String layering, String cadence, String density) {
        return new VisualDifference(id, form, core, tail, layering, cadence, density);
    }

    private static VisualDifference visualDifferenceFor(String id) {
        VisualDifference visual = VISUAL_DIFFERENCES.get(id);
        if (visual == null) {
            throw new IllegalArgumentException("Missing batch02 visual difference for " + id);
        }
        return visual;
    }

    private static Map<Character, String> orderedKey() {
        Map<Character, String> key = new LinkedHashMap<>();
        key.put('P', "minecraft:paper");
        key.put('F', "minecraft:firework_star");
        key.put('G', "minecraft:gunpowder");
        return Map.copyOf(key);
    }

    public enum EffectType {
        RADIANT_WILLOW
    }

    public enum CreativeTabTarget {
        RADIANT_WILLOW_SECTION
    }

    public record Definition(
            String id,
            String zhName,
            String enName,
            EffectType effectType,
            Palette palette,
            VisualDifference visualDifference,
            RecipeSpec recipe,
            CreativeTabTarget creativeTabTarget,
            ReuseContract reuseContract,
            ExpectedBounds expectedBounds) {
        public Definition {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(zhName, "zhName");
            Objects.requireNonNull(enName, "enName");
            Objects.requireNonNull(effectType, "effectType");
            Objects.requireNonNull(palette, "palette");
            Objects.requireNonNull(visualDifference, "visualDifference");
            Objects.requireNonNull(recipe, "recipe");
            Objects.requireNonNull(creativeTabTarget, "creativeTabTarget");
            Objects.requireNonNull(reuseContract, "reuseContract");
            Objects.requireNonNull(expectedBounds, "expectedBounds");
        }
    }

    public record Palette(String primary, String secondary, String accent) {
        public Palette {
            if (!isHex(primary) || !isHex(secondary) || !isHex(accent)
                    || primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Batch02 palettes require three distinct #RRGGBB colors");
            }
        }
    }

    /**
     * Per-item art direction for the shared trajectory. At least one non-color structural axis must be recorded;
     * this batch records all six so integration cannot collapse the series into palette-only variants.
     */
    public record VisualDifference(
            String id,
            String form,
            String core,
            String tail,
            String layering,
            String cadence,
            String density) {
        public VisualDifference {
            if (id == null || form == null || core == null || tail == null || layering == null
                    || cadence == null || density == null
                    || id.isBlank() || form.isBlank() || core.isBlank() || tail.isBlank()
                    || layering.isBlank() || cadence.isBlank() || density.isBlank()) {
                throw new IllegalArgumentException("Every batch02 entry needs a non-empty visual difference");
            }
        }

        public String structureSignature() {
            return String.join("|", form, core, tail, layering, cadence, density);
        }
    }

    public record RecipeSpec(List<String> pattern, Map<Character, String> key, int resultCount) {
        public RecipeSpec {
            pattern = List.copyOf(pattern);
            key = Map.copyOf(key);
            if (!pattern.equals(List.of(" P ", "FGF", " P "))
                    || !key.equals(orderedKey()) || resultCount != 1) {
                throw new IllegalArgumentException("Batch02 must reuse the approved shaped-recipe template");
            }
        }
    }

    public record ReuseContract(
            String itemModelParent,
            String particleTypeId,
            String trajectoryClass,
            EffectCategory effectCategory,
            int allocatedSparkCount,
            int managedSparkCount,
            int newExtensionParticleCount,
            double fullEnvelopeBlocks) {
        public ReuseContract {
            if (!"minecraft:item/firework_rocket".equals(itemModelParent)
                    || !"urbanforma_fireworks:hd_firework_spark".equals(particleTypeId)
                    || !"com.urbanforma.fireworks.content.RadiantWillowTrajectory".equals(trajectoryClass)
                    || effectCategory != EffectCategory.RADIANT_WILLOW
                    || allocatedSparkCount != RadiantWillowTrajectory.RADIANT_NODE_COUNT
                    || managedSparkCount != RadiantWillowTrajectory.MANAGED_NODE_COUNT
                    || newExtensionParticleCount != 0
                    || fullEnvelopeBlocks != RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE) {
                throw new IllegalArgumentException("Batch02 may only reuse the bounded radiant-willow contract");
            }
        }
    }

    public record ExpectedBounds(
            double fullEnvelopeBlocks,
            int minimumExtensionTicks,
            int maximumExtensionTicks,
            int managedSparkCount,
            int maximumTerminalRetirementsPerBranch) {
        public ExpectedBounds {
            if (fullEnvelopeBlocks != RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE
                    || minimumExtensionTicks != RadiantWillowTrajectory.MIN_EXTENSION_TICKS
                    || maximumExtensionTicks != RadiantWillowTrajectory.MAX_EXTENSION_TICKS
                    || managedSparkCount != RadiantWillowTrajectory.MANAGED_NODE_COUNT
                    || maximumTerminalRetirementsPerBranch != RadiantWillowTrajectory.MAX_TERMINAL_RETIREMENTS_PER_BRANCH) {
                throw new IllegalArgumentException("Batch02 bounds must match the shared radiant-willow proof envelope");
            }
        }
    }

    private static boolean isHex(String value) {
        return value != null && value.matches("#[0-9A-F]{6}");
    }
}
