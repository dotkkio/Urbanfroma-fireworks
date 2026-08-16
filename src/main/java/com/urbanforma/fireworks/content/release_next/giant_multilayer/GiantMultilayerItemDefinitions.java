package com.urbanforma.fireworks.content.release_next.giant_multilayer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerTrajectory.ColorBand;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerTrajectory.Profile;

/**
 * Private release-next integration contract for the ten new giant multilayer items.
 *
 * <p>It deliberately owns no registry, language map, creative-tab mutation, recipe registration, or network path.
 * The coordinator can promote each definition as one stable item without using its internal visual profile as the
 * item id. Existing multiradial replacement profiles remain trajectory-only compatibility entries.</p>
 */
public final class GiantMultilayerItemDefinitions {
    public static final String ASSET_STAGING_ROOT = "assets/urbanforma_fireworks/release_next/giant_multilayer";
    public static final int REQUIRED_NEW_ITEM_COUNT = 10;
    public static final String REQUIRED_CREATIVE_GROUP = "giant";

    private static final List<Definition> VALUES = List.of(
            definition(StableItem.STARFALL_REGALIA, Profile.AURORA_CROWN, "Starfall Regalia Giant Firework", "星瀑冠冕巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_CROWN),
            definition(StableItem.CINDERFALL_PROCESSION, Profile.EMBER_CASCADE, "Cinderfall Procession Giant Firework", "余烬瀑流巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_CASCADE),
            definition(StableItem.VERDANT_BLOOM, Profile.JADE_LOTUS, "Verdant Bloom Giant Firework", "翠玉莲华巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_FLORAL),
            definition(StableItem.IVORY_BELFRY, Profile.PEARL_CATHEDRAL, "Ivory Belfry Giant Firework", "珍珠钟楼巨型烟花", SuggestedTier.GIANT_SOVEREIGN, SuggestedCategory.MULTILAYER_ARCHITECTURAL),
            definition(StableItem.BRAZEN_VORTEX, Profile.COPPER_HELIX, "Brazen Vortex Giant Firework", "铜色螺旋巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_HELIX),
            definition(StableItem.AMETHYST_LUMINARIA, Profile.VIOLET_LANTERN, "Amethyst Luminaria Giant Firework", "紫晶天灯巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_LANTERN),
            definition(StableItem.HELIOS_FANFARE, Profile.SOLAR_FAN, "Helios Fanfare Giant Firework", "赫利俄斯扇辉巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_FAN),
            definition(StableItem.TIDAL_NORTHSTAR, Profile.AZURE_COMPASS, "Tidal Northstar Giant Firework", "苍澜北辰巨型烟花", SuggestedTier.GIANT_PRESTIGE, SuggestedCategory.MULTILAYER_COMPASS),
            definition(StableItem.VERMILION_PEONY_PAGEANT, Profile.CRIMSON_PETAL, "Vermilion Peony Pageant Giant Firework", "朱红牡丹盛典巨型烟花", SuggestedTier.GIANT_SOVEREIGN, SuggestedCategory.MULTILAYER_PETAL),
            definition(StableItem.IMPERIAL_SUNRISE, Profile.GOLDEN_PAGODA, "Imperial Sunrise Giant Firework", "帝国晨曦巨型烟花", SuggestedTier.GIANT_SOVEREIGN, SuggestedCategory.MULTILAYER_PAGODA));
    private static final Map<StableItem, Definition> BY_ITEM = index();

    static {
        if (VALUES.size() != REQUIRED_NEW_ITEM_COUNT || BY_ITEM.size() != REQUIRED_NEW_ITEM_COUNT) {
            throw new IllegalStateException("Giant multilayer item count drifted");
        }
        for (Definition definition : VALUES) {
            GiantMultilayerTrajectory.validateProfile(definition.profile());
            if (definition.item().path().equals(definition.profile().id()) || isReplacementProfile(definition.profile())) {
                throw new IllegalStateException("A staged item must not expose a profile or replacement id: " + definition.item());
            }
            if (definition.totalParticleBudget() != GiantMultilayerTrajectory.totalParticles(definition.profile())
                    || definition.peakParticlesPerTick() != GiantMultilayerTrajectory.peakParticlesPerTick(definition.profile())
                    || definition.layerStartTicks().size() != definition.profile().layerCount()
                    || definition.palette().size() != definition.profile().layerCount()) {
                throw new IllegalStateException("Giant multilayer budget metadata drifted for " + definition.item());
            }
        }
    }

    private GiantMultilayerItemDefinitions() {
    }

    public static List<Definition> values() {
        return VALUES;
    }

    public static Definition require(StableItem item) {
        return Objects.requireNonNull(BY_ITEM.get(item), "Unknown giant multilayer item: " + item);
    }

    public static boolean contains(StableItem item) {
        return item != null && BY_ITEM.containsKey(item);
    }

    private static Definition definition(StableItem item, Profile profile, String enName, String zhName,
                                         SuggestedTier tier, SuggestedCategory category) {
        List<GiantMultilayerTrajectory.Layer> layers = GiantMultilayerTrajectory.layers(profile);
        List<Integer> starts = layers.stream().map(GiantMultilayerTrajectory.Layer::startTick).toList();
        List<ColorBand> palette = layers.stream().map(GiantMultilayerTrajectory.Layer::color).toList();
        return new Definition(item, profile, enName, zhName, CreativeGroup.GIANT, tier, category, palette, starts,
                GiantMultilayerTrajectory.totalParticles(profile), GiantMultilayerTrajectory.peakParticlesPerTick(profile),
                new ModelStaging(item), new ShapedRecipeStaging(item));
    }

    private static Map<StableItem, Definition> index() {
        Map<StableItem, Definition> values = new EnumMap<>(StableItem.class);
        for (Definition value : VALUES) {
            if (values.put(value.item(), value) != null) {
                throw new IllegalStateException("Duplicate stable giant multilayer item: " + value.item());
            }
        }
        return Map.copyOf(values);
    }

    private static boolean isReplacementProfile(Profile profile) {
        return profile == Profile.MULTIRADIAL_REPLACEMENT || profile == Profile.MULTIRADIAL2_REPLACEMENT;
    }

    public enum StableItem {
        STARFALL_REGALIA("giant_starfall_regalia_firework"),
        CINDERFALL_PROCESSION("giant_cinderfall_procession_firework"),
        VERDANT_BLOOM("giant_verdant_bloom_firework"),
        IVORY_BELFRY("giant_ivory_belfry_firework"),
        BRAZEN_VORTEX("giant_brazen_vortex_firework"),
        AMETHYST_LUMINARIA("giant_amethyst_luminaria_firework"),
        HELIOS_FANFARE("giant_helios_fanfare_firework"),
        TIDAL_NORTHSTAR("giant_tidal_northstar_firework"),
        VERMILION_PEONY_PAGEANT("giant_vermilion_peony_pageant_firework"),
        IMPERIAL_SUNRISE("giant_imperial_sunrise_firework");

        private final String path;

        StableItem(String path) {
            this.path = path;
        }

        public String path() {
            return this.path;
        }

        public String itemId() {
            return "urbanforma_fireworks:" + this.path;
        }

        public String translationKey() {
            return "item.urbanforma_fireworks." + this.path;
        }
    }

    public enum CreativeGroup {
        GIANT;

        public String id() {
            return REQUIRED_CREATIVE_GROUP;
        }
    }

    /** Suggested integration tier; this intentionally does not depend on a shared tier type. */
    public enum SuggestedTier { GIANT_PRESTIGE, GIANT_SOVEREIGN }

    /** Suggested integration category; this intentionally does not depend on a shared category type. */
    public enum SuggestedCategory {
        MULTILAYER_CROWN, MULTILAYER_CASCADE, MULTILAYER_FLORAL, MULTILAYER_ARCHITECTURAL,
        MULTILAYER_HELIX, MULTILAYER_LANTERN, MULTILAYER_FAN, MULTILAYER_COMPASS,
        MULTILAYER_PETAL, MULTILAYER_PAGODA
    }

    public record Definition(StableItem item, Profile profile, String enUsName, String zhCnName,
                             CreativeGroup creativeGroup, SuggestedTier suggestedTier, SuggestedCategory suggestedCategory,
                             List<ColorBand> palette, List<Integer> layerStartTicks, int totalParticleBudget,
                             int peakParticlesPerTick, ModelStaging model, ShapedRecipeStaging recipe) {
        public Definition {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(profile, "profile");
            if (enUsName == null || enUsName.isBlank() || zhCnName == null || zhCnName.isBlank()) {
                throw new IllegalArgumentException("Bilingual names are required for " + item);
            }
            Objects.requireNonNull(creativeGroup, "creativeGroup");
            Objects.requireNonNull(suggestedTier, "suggestedTier");
            Objects.requireNonNull(suggestedCategory, "suggestedCategory");
            palette = List.copyOf(palette);
            layerStartTicks = List.copyOf(layerStartTicks);
            if (palette.isEmpty() || layerStartTicks.isEmpty() || totalParticleBudget <= 0 || peakParticlesPerTick <= 0) {
                throw new IllegalArgumentException("Invalid bounded metadata for " + item);
            }
            Objects.requireNonNull(model, "model");
            Objects.requireNonNull(recipe, "recipe");
            if (model.item() != item || recipe.item() != item) {
                throw new IllegalArgumentException("Staged resources must target their stable item: " + item);
            }
        }
    }

    /** Exact staged model target, kept private until the integration owner promotes it. */
    public record ModelStaging(StableItem item) {
        public String integrationTarget() {
            return "assets/urbanforma_fireworks/models/item/" + item.path() + ".json";
        }

        public String stagingPath() {
            return ASSET_STAGING_ROOT + "/models/item/" + item.path() + ".json";
        }
    }

    /** Exact staged shaped-recipe target; every pattern is deliberately a full 3 by 3 grid. */
    public record ShapedRecipeStaging(StableItem item) {
        public List<String> pattern() {
            return List.of("ABA", "CDC", "EFE");
        }

        public String integrationTarget() {
            return "data/urbanforma_fireworks/recipe/" + item.path() + ".json";
        }

        public String stagingPath() {
            return ASSET_STAGING_ROOT + "/recipes/" + item.path() + ".json";
        }
    }
}
