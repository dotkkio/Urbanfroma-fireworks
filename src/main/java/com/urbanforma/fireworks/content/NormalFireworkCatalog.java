package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.batch01.Batch01RadiantCatalog;
import com.urbanforma.fireworks.content.batch02.Batch02RadiantWillowCatalog;
import com.urbanforma.fireworks.content.batch03.Batch03SphereRadiantCatalog;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnCatalog;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework;
import com.urbanforma.fireworks.content.batch05.Batch05EffectProfiles;
import com.urbanforma.fireworks.content.batch05.Batch05FireworkCatalog;
import com.urbanforma.fireworks.content.batch05.Batch05FireworkDefinition;
import com.urbanforma.fireworks.content.colorchange.ColorChangeBallProgram;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Public integration adapter for the five completed ordinary-firework batches.
 *
 * <p>The source batch types remain their own source of truth. This class is the single place where their typed
 * contracts become append-only shared styles, recipes, localization data, and client program lookups. It never
 * creates a separate color-change series and never includes either giant queue in the normal-100 budget.</p>
 */
public final class NormalFireworkCatalog {
    public static final int FIRST_STYLE_INDEX = 46;
    public static final int EXISTING_ORDINARY_STYLE_COUNT = 2;
    public static final int NEW_ORDINARY_STYLE_COUNT = 98;
    public static final int NORMAL_100_STYLE_COUNT = EXISTING_ORDINARY_STYLE_COUNT + NEW_ORDINARY_STYLE_COUNT;
    public static final int COOL_COLOR_STYLE_COUNT = 3;
    public static final int COOL_COLOR_STYLE_CAP = 20;

    private static final FireworkStyle.RadiantProfile RADIANT_PROFILE =
            new FireworkStyle.RadiantProfile(3.5D, 48.0D, 0.94D, 0.38D, 0.46D, 9.0D);
    private static final FireworkStyle.RadiantWillowProfile RADIANT_WILLOW_PROFILE =
            new FireworkStyle.RadiantWillowProfile(RADIANT_PROFILE, 100, 140, 18.0D, 0.28D, 0.42D, 66.0D, 7.5D);
    private static final FireworkStyle.LayerShares RADIANT_SHARES = new FireworkStyle.LayerShares(470, 410, 120);
    private static final FireworkStyle.LayerShares SATURN_SHARES = new FireworkStyle.LayerShares(570, 300, 130);

    private static List<Entry> entries;
    private static Map<String, Entry> byId;
    private static Map<String, SaturnProgram> saturnPrograms;
    private static Map<String, ColorChangeBallProgram.Profile> colorChangeProfiles;

    private NormalFireworkCatalog() {
    }

    /** Called exactly once by {@link FireworkStyle} after its 46 pre-existing styles have been initialized. */
    public static synchronized List<FireworkStyle> stylesFrom(int firstStyleIndex) {
        return initialize(firstStyleIndex).stream().map(Entry::style).toList();
    }

    public static List<Entry> entries() {
        return initialize(FIRST_STYLE_INDEX);
    }

    public static Entry require(String id) {
        Entry entry = initialize(FIRST_STYLE_INDEX).stream()
                .filter(candidate -> candidate.style().id().equals(id))
                .findFirst()
                .orElse(null);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown normal firework " + id);
        }
        return entry;
    }

    public static SaturnProgram saturnProgramFor(String id) {
        initialize(FIRST_STYLE_INDEX);
        return saturnPrograms.get(id);
    }

    public static ColorChangeBallProgram.Profile colorChangeProfileFor(String id) {
        initialize(FIRST_STYLE_INDEX);
        return colorChangeProfiles.get(id);
    }

    public static Map<String, String> englishTranslations() {
        Map<String, String> translations = new LinkedHashMap<>();
        for (Entry entry : entries()) {
            translations.put(entry.style().translationKey(), entry.style().enName());
        }
        return Map.copyOf(translations);
    }

    public static Map<String, String> chineseTranslations() {
        Map<String, String> translations = new LinkedHashMap<>();
        for (Entry entry : entries()) {
            translations.put(entry.style().translationKey(), entry.style().zhName());
        }
        return Map.copyOf(translations);
    }

    private static List<Entry> initialize(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) {
            throw new IllegalArgumentException("Normal styles must start after the 46 stable pre-existing styles");
        }
        if (entries != null) {
            return entries;
        }

        List<Entry> mapped = new ArrayList<>(NEW_ORDINARY_STYLE_COUNT);
        int nextIndex = firstStyleIndex;
        nextIndex = appendBatch01(mapped, nextIndex);
        nextIndex = appendBatch02(mapped, nextIndex);
        nextIndex = appendBatch03(mapped, nextIndex);
        nextIndex = appendBatch04(mapped, nextIndex);
        appendBatch05(mapped, nextIndex);

        validate(mapped, firstStyleIndex);
        entries = List.copyOf(mapped);
        Map<String, Entry> indexedEntries = new HashMap<>();
        Map<String, SaturnProgram> indexedSaturnPrograms = new HashMap<>();
        Map<String, ColorChangeBallProgram.Profile> indexedColorChanges = new HashMap<>();
        for (Entry entry : entries) {
            indexedEntries.put(entry.style().id(), entry);
            if (entry.saturnProgram() != null) {
                indexedSaturnPrograms.put(entry.style().id(), entry.saturnProgram());
            }
            if (entry.colorChangeProfile() != null) {
                indexedColorChanges.put(entry.style().id(), entry.colorChangeProfile());
            }
        }
        byId = Map.copyOf(indexedEntries);
        saturnPrograms = Map.copyOf(indexedSaturnPrograms);
        colorChangeProfiles = Map.copyOf(indexedColorChanges);
        return entries;
    }

    private static int appendBatch01(List<Entry> mapped, int nextIndex) {
        for (Batch01RadiantCatalog.Definition definition : Batch01RadiantCatalog.definitions()) {
            FireworkStyle style = radiantStyle(
                    nextIndex++,
                    definition.id(),
                    definition.zhName(),
                    definition.enName(),
                    definition.palette() == Batch01RadiantCatalog.Palette.PURPLE_ACCENT
                            ? FireworkStyle.Family.JEWEL : FireworkStyle.Family.WARM,
                    FireworkStyle.Rgb.fromHex(definition.colors().primaryHex()),
                    FireworkStyle.Rgb.fromHex(definition.colors().secondaryHex()),
                    FireworkStyle.Rgb.fromHex(definition.colors().accentHex()));
            mapped.add(new Entry(
                    Batch01RadiantCatalog.BATCH_ID,
                    style,
                    visual(definition.visualIdentity().structuralSignature(), definition.visualDifference(),
                            definition.visualIdentity().differingAxes().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())),
                    new RecipeDefinition(
                            definition.recipeFields().recipeKey(),
                            List.of(),
                            Map.of(),
                            definition.recipeFields().generateDataRecipe()),
                    definition.reuseContract().itemModelParent(),
                    null,
                    null));
        }
        return nextIndex;
    }

    private static int appendBatch02(List<Entry> mapped, int nextIndex) {
        for (Batch02RadiantWillowCatalog.Definition definition : Batch02RadiantWillowCatalog.definitions()) {
            FireworkStyle style = radiantWillowStyle(
                    nextIndex++,
                    definition.id(), definition.zhName(), definition.enName(), FireworkStyle.Family.WARM,
                    FireworkStyle.Rgb.fromHex(definition.palette().primary()),
                    FireworkStyle.Rgb.fromHex(definition.palette().secondary()),
                    FireworkStyle.Rgb.fromHex(definition.palette().accent()));
            Batch02RadiantWillowCatalog.VisualDifference visual = definition.visualDifference();
            mapped.add(new Entry(
                    Batch02RadiantWillowCatalog.BATCH_ID,
                    style,
                    visual(visual.structureSignature(), String.join("; ", visual.form(), visual.core(), visual.tail(),
                            visual.layering(), visual.cadence(), visual.density()),
                            Set.of("SHAPE", "CORE", "TRAIL", "LAYERING", "CADENCE", "DENSITY")),
                    new RecipeDefinition(
                            "urbanforma_fireworks:" + definition.id(),
                            definition.recipe().pattern(),
                            stringKey(definition.recipe().key()),
                            true),
                    definition.reuseContract().itemModelParent(),
                    null,
                    null));
        }
        return nextIndex;
    }

    private static int appendBatch03(List<Entry> mapped, int nextIndex) {
        for (Batch03SphereRadiantCatalog.Definition definition : Batch03SphereRadiantCatalog.definitions()) {
            FireworkStyle style = hybridStyle(
                    nextIndex++,
                    definition.id(), definition.zhName(), definition.enName(), FireworkStyle.Family.JEWEL,
                    definition.palette().primary(), definition.palette().secondary(), definition.palette().accent());
            Batch03SphereRadiantCatalog.VisualDifference visual = definition.visualDifference();
            mapped.add(new Entry(
                    Batch03SphereRadiantCatalog.BATCH_ID,
                    style,
                    visual(visual.signature(), String.join("; ", visual.form(), visual.core(), visual.tail(),
                            visual.layering(), visual.rhythm(), visual.density()),
                            visual.structuralAxes().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())),
                    new RecipeDefinition(
                            "urbanforma_fireworks:" + definition.recipe().recipeId(),
                            definition.recipe().pattern(),
                            definition.recipe().ingredients(),
                            true),
                    definition.modelParent(),
                    null,
                    null));
        }
        return nextIndex;
    }

    private static int appendBatch04(List<Entry> mapped, int nextIndex) {
        for (Batch04SaturnFirework definition : Batch04SaturnCatalog.all()) {
            FireworkStyle style = saturnStyle(
                    nextIndex++,
                    definition.id(), definition.zhName(), definition.enName(),
                    definition.colorFamily() == Batch04SaturnFirework.ColorFamily.PURPLE
                            ? FireworkStyle.Family.JEWEL : FireworkStyle.Family.METALLIC,
                    definition.primaryColor(), definition.secondaryColor(), definition.accentColor(),
                    definition.boundary().maxEnvelopeBlocks());
            mapped.add(new Entry(
                    "batch04",
                    style,
                    visual(definition.structureSignature(), definition.visualDifference().description(),
                            definition.visualDifference().structuralAxes().stream()
                                    .map(Enum::name).collect(java.util.stream.Collectors.toSet())),
                    new RecipeDefinition(
                            "urbanforma_fireworks:" + definition.id(),
                            definition.recipe().pattern(),
                            ingredientKey(definition.recipe().ingredients()),
                            true),
                    definition.modelContract().parent(),
                    definition.program(),
                    null));
        }
        return nextIndex;
    }

    private static int appendBatch05(List<Entry> mapped, int nextIndex) {
        for (Batch05FireworkDefinition definition : Batch05FireworkCatalog.values()) {
            FireworkStyle style = switch (definition.effectType()) {
                case RADIANT -> radiantStyle(
                        nextIndex++, definition.id(), definition.zhName(), definition.enName(),
                        FireworkStyle.Family.WARM, definition.palette().primary(), definition.palette().secondary(),
                        definition.palette().accent());
                case RADIANT_WILLOW -> radiantWillowStyle(
                        nextIndex++, definition.id(), definition.zhName(), definition.enName(),
                        FireworkStyle.Family.WARM, definition.palette().primary(), definition.palette().secondary(),
                        definition.palette().accent());
                case HYBRID_SPHERE_RADIANT -> hybridStyle(
                        nextIndex++, definition.id(), definition.zhName(), definition.enName(),
                        FireworkStyle.Family.JEWEL, definition.palette().primary(), definition.palette().secondary(),
                        definition.palette().accent());
                case SATURN -> saturnStyle(
                        nextIndex++, definition.id(), definition.zhName(), definition.enName(),
                        FireworkStyle.Family.METALLIC, definition.palette().primary(), definition.palette().secondary(),
                        definition.palette().accent(), (int) definition.expectedBoundary().fullEnvelopeBlocks());
            };
            Batch05FireworkDefinition.VisualSignature visual = definition.visualSignature();
            SaturnProgram saturn = definition.effectType() == Batch05FireworkDefinition.EffectType.SATURN
                    ? Batch05EffectProfiles.saturnProgram(definition) : null;
            ColorChangeBallProgram.Profile colorChange = definition.colorChange() == null ? null
                    : definition.colorChange().profileFor(definition.palette());
            mapped.add(new Entry(
                    Batch05FireworkCatalog.BATCH_ID,
                    style,
                    visual(visual.structuralSignature(), visual.visualDifference(),
                            visual.structuralAxes().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())),
                    new RecipeDefinition(
                            definition.recipe().resultId(), definition.recipe().pattern(), definition.recipe().key(), true),
                    definition.reuseContract().itemModel(),
                    saturn,
                    colorChange));
        }
        return nextIndex;
    }

    private static FireworkStyle radiantStyle(
            int index, String id, String zhName, String enName, FireworkStyle.Family family,
            FireworkStyle.Rgb primary, FireworkStyle.Rgb secondary, FireworkStyle.Rgb accent) {
        return ordinaryStyle(index, id, zhName, enName, family, FireworkStyle.Shape.RADIANT, primary, secondary, accent,
                108, 108, 4_800, RADIANT_SHARES, 160, RADIANT_PROFILE, null);
    }

    private static FireworkStyle radiantWillowStyle(
            int index, String id, String zhName, String enName, FireworkStyle.Family family,
            FireworkStyle.Rgb primary, FireworkStyle.Rgb secondary, FireworkStyle.Rgb accent) {
        return ordinaryStyle(index, id, zhName, enName, family, FireworkStyle.Shape.RADIANT_WILLOW,
                primary, secondary, accent, 108, 220, 4_800, RADIANT_SHARES, 160, null, RADIANT_WILLOW_PROFILE);
    }

    private static FireworkStyle hybridStyle(
            int index, String id, String zhName, String enName, FireworkStyle.Family family,
            FireworkStyle.Rgb primary, FireworkStyle.Rgb secondary, FireworkStyle.Rgb accent) {
        return ordinaryStyle(index, id, zhName, enName, family, FireworkStyle.Shape.HYBRID_SPHERE_RADIANT,
                primary, secondary, accent, 112, 112, 4_080, RADIANT_SHARES, 340, RADIANT_PROFILE, null);
    }

    private static FireworkStyle saturnStyle(
            int index, String id, String zhName, String enName, FireworkStyle.Family family,
            FireworkStyle.Rgb primary, FireworkStyle.Rgb secondary, FireworkStyle.Rgb accent, int fullEnvelope) {
        return ordinaryStyle(index, id, zhName, enName, family, FireworkStyle.Shape.SATURN,
                primary, secondary, accent, Math.min(144, fullEnvelope), fullEnvelope, 4_000, SATURN_SHARES,
                192, null, null);
    }

    private static FireworkStyle ordinaryStyle(
            int index, String id, String zhName, String enName, FireworkStyle.Family family, FireworkStyle.Shape shape,
            FireworkStyle.Rgb primary, FireworkStyle.Rgb secondary, FireworkStyle.Rgb accent, int diameter,
            int fullEnvelope, int totalStars, FireworkStyle.LayerShares shares, int starsPerTick,
            FireworkStyle.RadiantProfile radiantProfile, FireworkStyle.RadiantWillowProfile radiantWillowProfile) {
        return new FireworkStyle(
                index, id, zhName, enName, family, shape, primary, secondary, accent,
                100, diameter, fullEnvelope, 0, totalStars, shares, starsPerTick, FireworkStyle.TrailTier.GRAND,
                102, 84, 68, 0.35F, 0.60F, null, radiantProfile, radiantWillowProfile, GiantTier.NONE);
    }

    private static VisualIdentity visual(String signature, String difference, Set<String> axes) {
        return new VisualIdentity(signature, difference, axes);
    }

    private static Map<String, String> stringKey(Map<Character, String> key) {
        Map<String, String> normalized = new LinkedHashMap<>();
        key.forEach((symbol, item) -> normalized.put(String.valueOf(symbol), item));
        return Map.copyOf(normalized);
    }

    private static Map<String, String> ingredientKey(List<Batch04SaturnFirework.Ingredient> ingredients) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Batch04SaturnFirework.Ingredient ingredient : ingredients) {
            if (normalized.put(ingredient.symbol(), ingredient.itemId()) != null) {
                throw new IllegalStateException("Duplicate batch04 recipe symbol " + ingredient.symbol());
            }
        }
        return Map.copyOf(normalized);
    }

    private static void validate(List<Entry> mapped, int firstStyleIndex) {
        if (mapped.size() != NEW_ORDINARY_STYLE_COUNT) {
            throw new IllegalStateException("Normal catalog must integrate exactly 98 new ordinary styles");
        }
        Set<String> ids = new HashSet<>();
        Set<String> bilingualNames = new HashSet<>();
        int coldEntries = 1; // The pre-existing Saturn teal accent is the normal-series baseline.
        int generatedRecipes = 0;
        for (int offset = 0; offset < mapped.size(); offset++) {
            Entry entry = mapped.get(offset);
            FireworkStyle style = entry.style();
            if (style.index() != firstStyleIndex + offset || !ids.add(style.id())
                    || !bilingualNames.add(style.zhName() + "\u0000" + style.enName())
                    || entry.visualIdentity().visualDifference().isBlank()
                    || entry.visualIdentity().structuralAxes().isEmpty()
                    || !"minecraft:item/firework_rocket".equals(entry.itemModelParent())) {
                throw new IllegalStateException("Normal integration contract drifted for " + style.id());
            }
            if (entry.recipe().generateDataRecipe()) {
                generatedRecipes++;
                if (!entry.recipe().resultId().equals("urbanforma_fireworks:" + style.id())
                        || entry.recipe().pattern().isEmpty() || entry.recipe().key().isEmpty()) {
                    throw new IllegalStateException("Normal recipe contract drifted for " + style.id());
                }
            }
            if (entry.saturnProgram() != null
                    && (style.shape() != FireworkStyle.Shape.SATURN
                    || entry.saturnProgram().budget().maxOwnedParticles() > 4_000
                    || entry.saturnProgram().budget().maxPerTick() > 480)) {
                throw new IllegalStateException("Normal Saturn program exceeds its approved contract");
            }
            if (entry.colorChangeProfile() != null) {
                ColorChangeBallProgram.validateSwitchDelayTicks(entry.colorChangeProfile().switchDelayTicks());
            }
            if (style.id().equals("batch05_teal_opal_hybrid") || style.id().equals("batch05_aqua_platinum_saturn")) {
                coldEntries++;
            }
        }
        if (generatedRecipes != 78 || coldEntries != COOL_COLOR_STYLE_COUNT || coldEntries > COOL_COLOR_STYLE_CAP) {
            throw new IllegalStateException("Normal recipe or cool-color budget drifted");
        }
    }

    public record Entry(
            String batchId,
            FireworkStyle style,
            VisualIdentity visualIdentity,
            RecipeDefinition recipe,
            String itemModelParent,
            SaturnProgram saturnProgram,
            ColorChangeBallProgram.Profile colorChangeProfile) {
        public Entry {
            Objects.requireNonNull(batchId, "batchId");
            Objects.requireNonNull(style, "style");
            Objects.requireNonNull(visualIdentity, "visualIdentity");
            Objects.requireNonNull(recipe, "recipe");
            Objects.requireNonNull(itemModelParent, "itemModelParent");
        }
    }

    public record VisualIdentity(String structuralSignature, String visualDifference, Set<String> structuralAxes) {
        public VisualIdentity {
            if (structuralSignature == null || structuralSignature.isBlank()
                    || visualDifference == null || visualDifference.isBlank()
                    || structuralAxes == null || structuralAxes.isEmpty()) {
                throw new IllegalArgumentException("Normal entries need a non-color visual identity");
            }
            structuralAxes = Set.copyOf(structuralAxes);
        }
    }

    public record RecipeDefinition(String resultId, List<String> pattern, Map<String, String> key, boolean generateDataRecipe) {
        public RecipeDefinition {
            Objects.requireNonNull(resultId, "resultId");
            pattern = List.copyOf(pattern);
            key = Map.copyOf(key);
            if (resultId.isBlank()) {
                throw new IllegalArgumentException("Normal recipe id must not be blank");
            }
        }
    }
}
