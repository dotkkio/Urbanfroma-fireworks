package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.batch_other.BatchOtherCatalog;
import com.urbanforma.fireworks.content.batch_other.BatchOtherFirework;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Append-only shared adapter for the fifteen completed ordinary batch_other definitions.
 *
 * <p>The isolated catalog owns visual, palette, recipe, model, and particle contracts. This adapter only assigns
 * stable shared style indices after the integrated giant entries, so these ordinary effects retain the existing
 * standard {@code GrandFireworkClientEffects.ActiveBurst} dispatch path.</p>
 */
public final class OtherFireworkCatalog {
    public static final int FIRST_STYLE_INDEX = GiantFireworkCatalog.FIRST_STYLE_INDEX
            + GiantFireworkCatalog.INTEGRATED_GIANT_COUNT;
    public static final int OTHER_ORDINARY_STYLE_COUNT = BatchOtherCatalog.REQUIRED_ENTRY_COUNT;
    public static final int TOTAL_ORDINARY_STYLE_COUNT = NormalFireworkCatalog.NORMAL_100_STYLE_COUNT
            + OTHER_ORDINARY_STYLE_COUNT;
    public static final int COOL_COLOR_STYLE_COUNT = BatchOtherCatalog.COOL_COLOR_COUNT_AFTER_BATCH;
    public static final int COOL_COLOR_STYLE_CAP = BatchOtherCatalog.ORDINARY_COOL_COLOR_LIMIT;

    private static List<Entry> entries;

    private OtherFireworkCatalog() {
    }

    public static synchronized List<FireworkStyle> stylesFrom(int firstStyleIndex) {
        return initialize(firstStyleIndex).stream().map(Entry::style).toList();
    }

    public static List<Entry> entries() {
        return initialize(FIRST_STYLE_INDEX);
    }

    private static List<Entry> initialize(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) {
            throw new IllegalArgumentException("batch_other styles must follow the integrated giant styles");
        }
        if (entries != null) {
            return entries;
        }

        List<Entry> mapped = new ArrayList<>(OTHER_ORDINARY_STYLE_COUNT);
        int nextIndex = firstStyleIndex;
        for (BatchOtherFirework definition : BatchOtherCatalog.values()) {
            mapped.add(new Entry(definition, style(nextIndex++, definition)));
        }
        validate(mapped, firstStyleIndex);
        entries = List.copyOf(mapped);
        return entries;
    }

    private static FireworkStyle style(int index, BatchOtherFirework definition) {
        BatchOtherFirework.StyleParameters source = definition.style();
        return new FireworkStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                family(definition.family()),
                shape(definition.effectType()),
                definition.palette().primary(),
                definition.palette().secondary(),
                definition.palette().accent(),
                source.flightTicks(),
                source.diameterBlocks(),
                source.fullEnvelopeBlocks(),
                source.phaseDelayTicks(),
                source.totalStarCount(),
                new FireworkStyle.LayerShares(
                        source.mainLayerPermille(), source.secondaryLayerPermille(), source.accentLayerPermille()),
                source.starsPerTick(),
                trailTier(source.trailTier()),
                source.outerLifetime(),
                source.innerLifetime(),
                source.accentLifetime(),
                source.twinkleChanceMin(),
                source.twinkleChanceMax(),
                null,
                null,
                null,
                GiantTier.NONE);
    }

    private static FireworkStyle.Family family(BatchOtherFirework.Family family) {
        return switch (family) {
            case WARM -> FireworkStyle.Family.WARM;
            case JEWEL -> FireworkStyle.Family.JEWEL;
            case METALLIC -> FireworkStyle.Family.METALLIC;
            case COOL -> FireworkStyle.Family.COOL;
        };
    }

    private static FireworkStyle.Shape shape(BatchOtherFirework.EffectType effectType) {
        // The legacy compatibility hint is deliberately not used for presentation. Actual route/program mapping
        // is carried by the isolated typed contract and consumed by the client dispatch adapter.
        return FireworkStyle.Shape.OTHER;
    }

    private static FireworkStyle.TrailTier trailTier(BatchOtherFirework.TrailTier trailTier) {
        return switch (trailTier) {
            case COMPACT -> FireworkStyle.TrailTier.COMPACT;
            case STANDARD -> FireworkStyle.TrailTier.STANDARD;
            case GRAND -> FireworkStyle.TrailTier.GRAND;
        };
    }

    private static void validate(List<Entry> mapped, int firstStyleIndex) {
        if (mapped.size() != OTHER_ORDINARY_STYLE_COUNT) {
            throw new IllegalStateException("batch_other must integrate exactly fifteen ordinary styles");
        }
        Set<String> ids = new HashSet<>();
        Set<String> bilingualNames = new HashSet<>();
        int coolColors = 0;
        for (int offset = 0; offset < mapped.size(); offset++) {
            Entry entry = mapped.get(offset);
            BatchOtherFirework source = entry.source();
            FireworkStyle style = entry.style();
            if (style.index() != firstStyleIndex + offset
                    || !ids.add(style.id())
                    || !bilingualNames.add(style.zhName() + "\u0000" + style.enName())
                    || style.effectCategory() != EffectCategory.STANDARD
                    || style.giantTier() != GiantTier.NONE
                    || source.visualDifference().description().isBlank()
                    || source.visualDifference().structuralAxes().isEmpty()
                    || !"minecraft:item/firework_rocket".equals(source.model().parent())
                    || !BatchOtherCatalog.PARTICLE_ENGINE.equals(source.particle().engine())
                    || style.fullEnvelope() > BatchOtherCatalog.ORDINARY_MAXIMUM_ENVELOPE
                    || !source.recipe().result().equals("urbanforma_fireworks:" + style.id())) {
                throw new IllegalStateException("batch_other integration contract drifted for " + style.id());
            }
            if (source.palette().consumesCoolColorQuota()) {
                coolColors++;
            }
        }
        if (coolColors != BatchOtherCatalog.BATCH_COOL_COLOR_COUNT
                || COOL_COLOR_STYLE_COUNT > COOL_COLOR_STYLE_CAP
                || TOTAL_ORDINARY_STYLE_COUNT != 115) {
            throw new IllegalStateException("batch_other ordinary count or cool-color budget drifted");
        }
    }

    public record Entry(BatchOtherFirework source, FireworkStyle style) {
    }
}
