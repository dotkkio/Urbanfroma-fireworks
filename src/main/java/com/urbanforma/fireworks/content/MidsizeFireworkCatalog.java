package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.midsize.MidsizeFireworkDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shared typed adapter for the two midsize trial programs. */
public final class MidsizeFireworkCatalog {
    public static final int FIRST_STYLE_INDEX = OtherExtraFireworkCatalog.FIRST_STYLE_INDEX
            + OtherExtraFireworkCatalog.OTHER_EXTRA_STYLE_COUNT;
    public static final int MIDSIZE_STYLE_COUNT = 2;

    private static List<Entry> entries;

    private MidsizeFireworkCatalog() {
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
                .orElseThrow(() -> new IllegalArgumentException("Unknown midsize firework " + id));
    }

    private static List<Entry> initialize(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) {
            throw new IllegalArgumentException("Midsize trial styles must follow the Other batches");
        }
        if (entries != null) {
            return entries;
        }

        List<Entry> mapped = new ArrayList<>(MIDSIZE_STYLE_COUNT);
        int nextIndex = firstStyleIndex;
        for (MidsizeFireworkDefinition definition : com.urbanforma.fireworks.content.midsize.MidsizeFireworkCatalog.values()) {
            mapped.add(new Entry(definition, style(nextIndex++, definition)));
        }
        validate(mapped, firstStyleIndex);
        entries = List.copyOf(mapped);
        return entries;
    }

    private static FireworkStyle style(int index, MidsizeFireworkDefinition definition) {
        MidsizeFireworkDefinition.ParticlePlan particles = definition.particlePlan();
        return new FireworkStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                FireworkStyle.Family.WARM,
                FireworkStyle.Shape.OTHER,
                FireworkStyle.Rgb.fromHex(definition.palette().primary()),
                FireworkStyle.Rgb.fromHex(definition.palette().secondary()),
                FireworkStyle.Rgb.fromHex(definition.palette().accent()),
                definition.boundary().ascentTicks(),
                (int) Math.round(definition.boundary().fullEnvelopeBlocks()),
                (int) Math.round(definition.boundary().fullEnvelopeBlocks()),
                0,
                particles.totalParticles(),
                new FireworkStyle.LayerShares(520, 320, 160),
                particles.particlesPerTick(),
                FireworkStyle.TrailTier.STANDARD,
                particles.maxLifetimeTicks(),
                particles.minLifetimeTicks(),
                particles.minLifetimeTicks(),
                0.35F,
                0.60F,
                null,
                null,
                null,
                GiantTier.NONE);
    }

    private static void validate(List<Entry> mapped, int firstStyleIndex) {
        if (mapped.size() != MIDSIZE_STYLE_COUNT) {
            throw new IllegalStateException("The midsize adapter must map exactly two styles");
        }
        Set<String> ids = new HashSet<>();
        Set<String> clientPrograms = new HashSet<>();
        for (int offset = 0; offset < mapped.size(); offset++) {
            Entry entry = mapped.get(offset);
            MidsizeFireworkDefinition source = entry.source();
            FireworkStyle style = entry.style();
            if (style.index() != firstStyleIndex + offset
                    || style.shape() != FireworkStyle.Shape.OTHER
                    || !ids.add(style.id())
                    || !clientPrograms.add(source.effectPath().clientProgramClass())
                    || !source.recipe().resultId().equals("urbanforma_fireworks:" + style.id())
                    || !"minecraft:item/firework_rocket".equals(source.model().itemModelParent())
                    || style.fullEnvelope() != (int) Math.round(source.boundary().fullEnvelopeBlocks())) {
                throw new IllegalStateException("Midsize integration contract drifted for " + style.id());
            }
        }
    }

    public record Entry(MidsizeFireworkDefinition source, FireworkStyle style) {
    }
}
