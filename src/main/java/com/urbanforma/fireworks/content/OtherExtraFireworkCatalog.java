package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraCatalog;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraFirework;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shared typed adapter for the second Other batch. */
public final class OtherExtraFireworkCatalog {
    public static final int FIRST_STYLE_INDEX = OtherFireworkCatalog.FIRST_STYLE_INDEX
            + OtherFireworkCatalog.OTHER_ORDINARY_STYLE_COUNT;
    public static final int OTHER_EXTRA_STYLE_COUNT = BatchOtherExtraCatalog.REQUIRED_ENTRY_COUNT;
    public static final int TOTAL_OTHER_STYLE_COUNT = OtherFireworkCatalog.OTHER_ORDINARY_STYLE_COUNT
            + OTHER_EXTRA_STYLE_COUNT;
    public static final int COOL_COLOR_STYLE_COUNT = BatchOtherExtraCatalog.COOL_COLOR_COUNT_AFTER_BATCH;
    public static final int COOL_COLOR_STYLE_CAP = BatchOtherExtraCatalog.COOL_COLOR_LIMIT;

    private static List<Entry> entries;

    private OtherExtraFireworkCatalog() {
    }

    public static synchronized List<FireworkStyle> stylesFrom(int firstStyleIndex) {
        return initialize(firstStyleIndex).stream().map(Entry::style).toList();
    }

    public static List<Entry> entries() {
        return initialize(FIRST_STYLE_INDEX);
    }

    public static Entry require(String id) {
        return entries().stream()
                .filter(entry -> entry.style().id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown extra Other firework " + id));
    }

    private static List<Entry> initialize(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) {
            throw new IllegalArgumentException("Extra Other styles must follow the first Other batch");
        }
        if (entries != null) {
            return entries;
        }

        List<Entry> mapped = new ArrayList<>(OTHER_EXTRA_STYLE_COUNT);
        int nextIndex = firstStyleIndex;
        for (BatchOtherExtraFirework definition : BatchOtherExtraCatalog.values()) {
            mapped.add(new Entry(definition, style(nextIndex++, definition)));
        }
        validate(mapped, firstStyleIndex);
        entries = List.copyOf(mapped);
        return entries;
    }

    private static FireworkStyle style(int index, BatchOtherExtraFirework definition) {
        BatchOtherExtraFirework.StyleParameters source = definition.style();
        return new FireworkStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                family(definition.family()),
                FireworkStyle.Shape.OTHER,
                definition.palette().primary(),
                definition.palette().secondary(),
                definition.palette().accent(),
                source.flightTicks(),
                source.nominalDiameterBlocks(),
                source.fullEnvelopeBlocks(),
                source.phaseDelayTicks(),
                source.totalParticleCount(),
                new FireworkStyle.LayerShares(
                        source.primaryLayerPermille(), source.secondaryLayerPermille(), source.accentLayerPermille()),
                source.maxParticlesPerTick(),
                FireworkStyle.TrailTier.STANDARD,
                source.trailLifetimeTicks(),
                source.coreLifetimeTicks(),
                source.accentLifetimeTicks(),
                source.twinkleChanceMin(),
                source.twinkleChanceMax(),
                null,
                null,
                null,
                GiantTier.NONE);
    }

    private static FireworkStyle.Family family(BatchOtherExtraFirework.Family family) {
        return switch (family) {
            case WARM -> FireworkStyle.Family.WARM;
            case JEWEL -> FireworkStyle.Family.JEWEL;
            case METALLIC -> FireworkStyle.Family.METALLIC;
            case COOL -> FireworkStyle.Family.COOL;
        };
    }

    private static void validate(List<Entry> mapped, int firstStyleIndex) {
        if (mapped.size() != OTHER_EXTRA_STYLE_COUNT) {
            throw new IllegalStateException("The extra Other adapter must map exactly fifteen styles");
        }
        Set<String> ids = new HashSet<>();
        Set<String> programs = new HashSet<>();
        int coolColors = 0;
        for (int offset = 0; offset < mapped.size(); offset++) {
            Entry entry = mapped.get(offset);
            BatchOtherExtraFirework source = entry.source();
            FireworkStyle style = entry.style();
            if (style.index() != firstStyleIndex + offset
                    || style.shape() != FireworkStyle.Shape.OTHER
                    || style.giantTier() != GiantTier.NONE
                    || !ids.add(style.id())
                    || !programs.add(source.clientProgram())
                    || source.effectPath() != source.trajectory().route()
                    || !source.clientProgram().equals(source.effectPath().clientProgramId())
                    || !source.recipe().result().equals("urbanforma_fireworks:" + style.id())
                    || !"minecraft:item/firework_rocket".equals(source.model().parent())
                    || style.fullEnvelope() != source.expectedBoundary().fullEnvelopeBlocks()) {
                throw new IllegalStateException("Extra Other integration contract drifted for " + style.id());
            }
            if (source.palette().consumesCoolColorQuota()) {
                coolColors++;
            }
        }
        if (coolColors != BatchOtherExtraCatalog.COOL_COLOR_DELTA
                || COOL_COLOR_STYLE_COUNT > COOL_COLOR_STYLE_CAP) {
            throw new IllegalStateException("Extra Other cool-color budget drifted");
        }
    }

    public record Entry(BatchOtherExtraFirework source, FireworkStyle style) {
    }
}
