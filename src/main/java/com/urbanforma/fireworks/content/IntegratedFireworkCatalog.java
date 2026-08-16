package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.large_extra.LargeExtraFireworkCatalog;
import com.urbanforma.fireworks.content.large_extra.LargeExtraFireworkDefinition;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialFireworkCatalog;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialFireworkDefinition;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereCatalog;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition;
import com.urbanforma.fireworks.content.small.SmallFireworkCatalog;
import com.urbanforma.fireworks.content.small.SmallFireworkDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Coordinator-owned, append-only bridge from the isolated content contracts to the shared style/item registry.
 *
 * <p>This class is deliberately common-side only. It names no client program and does not create particles,
 * packets, queues, or server calculations. The physical-client dispatcher resolves {@link Kind} through the
 * corresponding typed contract before it invokes a client program.</p>
 */
public final class IntegratedFireworkCatalog {
    /** The established 180 entries remain persisted at indices zero through 179. */
    public static final int FIRST_STYLE_INDEX = MidsizeFireworkCatalog.FIRST_STYLE_INDEX
            + MidsizeFireworkCatalog.MIDSIZE_STYLE_COUNT;
    public static final int SMALL_STYLE_COUNT = SmallFireworkCatalog.REQUIRED_ENTRY_COUNT;
    public static final int MEDIUM_SPHERE_STYLE_COUNT = MediumSphereCatalog.REQUIRED_ENTRY_COUNT;
    public static final int MEDIUM_RADIAL_STYLE_COUNT = MidsizeRadialFireworkCatalog.REQUIRED_ENTRY_COUNT;
    public static final int LARGE_EXTRA_STYLE_COUNT = LargeExtraFireworkCatalog.REQUIRED_ENTRY_COUNT;
    public static final int ADDITIONAL_GIANT_STYLE_COUNT = 4;
    public static final int TOTAL_STYLE_COUNT = SMALL_STYLE_COUNT
            + MEDIUM_SPHERE_STYLE_COUNT
            + MEDIUM_RADIAL_STYLE_COUNT
            + LARGE_EXTRA_STYLE_COUNT
            + ADDITIONAL_GIANT_STYLE_COUNT;

    private static final FireworkStyle.LayerShares SPECIAL_SHARES = new FireworkStyle.LayerShares(520, 350, 130);
    private static List<Entry> entries;
    private static Map<String, Entry> entriesById;

    private IntegratedFireworkCatalog() {
    }

    public enum Kind {
        SMALL,
        MEDIUM_SPHERE,
        MEDIUM_RADIAL,
        LARGE_EXTRA,
        ADDITIONAL_GIANT
    }

    /** A shared style plus the typed catalog family that owns its client route. */
    public record Entry(Kind kind, FireworkStyle style) {
        public Entry {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(style, "style");
        }
    }

    public static synchronized List<FireworkStyle> stylesFrom(int firstStyleIndex) {
        return initialize(firstStyleIndex).stream().map(Entry::style).toList();
    }

    public static List<Entry> entries() {
        return initialize(FIRST_STYLE_INDEX);
    }

    public static boolean contains(String id) {
        return id != null && entriesById().containsKey(id);
    }

    public static Entry require(String id) {
        Entry entry = entriesById().get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown integrated firework " + id);
        }
        return entry;
    }

    private static synchronized Map<String, Entry> entriesById() {
        initialize(FIRST_STYLE_INDEX);
        return entriesById;
    }

    private static List<Entry> initialize(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) {
            throw new IllegalArgumentException("Integrated styles must append after the stable 180-style catalog");
        }
        if (entries != null) {
            return entries;
        }

        List<Entry> mapped = new ArrayList<>(TOTAL_STYLE_COUNT);
        int nextIndex = firstStyleIndex;
        for (SmallFireworkDefinition definition : SmallFireworkCatalog.values()) {
            mapped.add(new Entry(Kind.SMALL, smallStyle(nextIndex++, definition)));
        }
        for (MediumSphereDefinition definition : MediumSphereCatalog.values()) {
            mapped.add(new Entry(Kind.MEDIUM_SPHERE, mediumSphereStyle(nextIndex++, definition)));
        }
        for (MidsizeRadialFireworkDefinition definition : MidsizeRadialFireworkCatalog.values()) {
            mapped.add(new Entry(Kind.MEDIUM_RADIAL, mediumRadialStyle(nextIndex++, definition)));
        }
        for (LargeExtraFireworkDefinition definition : LargeExtraFireworkCatalog.values()) {
            mapped.add(new Entry(Kind.LARGE_EXTRA, largeExtraStyle(nextIndex++, definition)));
        }
        for (AdditionalGiantDefinition definition : additionalGiantDefinitions()) {
            mapped.add(new Entry(Kind.ADDITIONAL_GIANT, giantStyle(nextIndex++, definition)));
        }

        validate(mapped, firstStyleIndex);
        Map<String, Entry> indexed = new HashMap<>();
        for (Entry entry : mapped) {
            if (indexed.put(entry.style().id(), entry) != null) {
                throw new IllegalStateException("Duplicate integrated firework id " + entry.style().id());
            }
        }
        entries = List.copyOf(mapped);
        entriesById = Map.copyOf(indexed);
        return entries;
    }

    private static FireworkStyle smallStyle(int index, SmallFireworkDefinition definition) {
        SmallFireworkDefinition.LocalParticlePlan plan = definition.clientEffectPath().particlePlan();
        return standardStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                definition.effectType() == SmallFireworkDefinition.EffectType.LAYERED_SPHERE
                        ? FireworkStyle.Family.COOL
                        : FireworkStyle.Family.WARM,
                definition.palette().primary(),
                definition.palette().secondary(),
                definition.palette().accent(),
                definition.launchTarget().flightTicks(),
                plan.fullEnvelopeBlocks(),
                plan.totalParticles(),
                plan.peakParticlesPerTick(),
                plan.maxLifetimeTicks());
    }

    private static FireworkStyle mediumSphereStyle(int index, MediumSphereDefinition definition) {
        MediumSphereDefinition.ParticleBudget plan = definition.particleBudget();
        return standardStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                familyFor(definition.palette().family()),
                definition.palette().primary().hex(),
                definition.palette().secondary().hex(),
                definition.palette().accent().hex(),
                definition.boundary().ascentTicks(),
                definition.boundary().fullEnvelopeBlocks(),
                plan.totalParticles(),
                plan.particlesPerTick(),
                plan.maxLifetimeTicks());
    }

    private static FireworkStyle mediumRadialStyle(int index, MidsizeRadialFireworkDefinition definition) {
        MidsizeRadialFireworkDefinition.ParticlePlan plan = definition.particlePlan();
        return standardStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                definition.countsTowardCoolColorBudget() ? FireworkStyle.Family.COOL : FireworkStyle.Family.WARM,
                hex(definition.palette().primary()),
                hex(definition.palette().secondary()),
                hex(definition.palette().accent()),
                definition.boundary().ascentTicks(),
                definition.boundary().fullEnvelopeBlocks(),
                plan.totalParticles(),
                plan.localPeakParticlesPerTick(),
                plan.maximumLifetimeTicks());
    }

    private static FireworkStyle largeExtraStyle(int index, LargeExtraFireworkDefinition definition) {
        LargeExtraFireworkDefinition.ParticleBudget plan = definition.particleBudget();
        return standardStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                definition.palette().countsTowardCoolColorLedger()
                        ? FireworkStyle.Family.COOL
                        : FireworkStyle.Family.WARM,
                definition.palette().primary().hex(),
                definition.palette().secondary().hex(),
                definition.palette().accent().hex(),
                definition.envelope().flightTicks(),
                definition.envelope().fullEnvelopeBlocks(),
                plan.plannedParticles(),
                plan.particlesPerTick(),
                plan.maxLifetimeTicks());
    }

    private static FireworkStyle standardStyle(
            int index,
            String id,
            String zhName,
            String enName,
            FireworkStyle.Family family,
            String primary,
            String secondary,
            String accent,
            int flightTicks,
            double fullEnvelopeBlocks,
            int totalParticles,
            int particlesPerTick,
            int maxLifetimeTicks) {
        int fullEnvelope = (int) Math.ceil(fullEnvelopeBlocks);
        return new FireworkStyle(
                index,
                id,
                zhName,
                enName,
                family,
                FireworkStyle.Shape.OTHER,
                FireworkStyle.Rgb.fromHex(primary),
                FireworkStyle.Rgb.fromHex(secondary),
                FireworkStyle.Rgb.fromHex(accent),
                flightTicks,
                fullEnvelope,
                fullEnvelope,
                0,
                totalParticles,
                SPECIAL_SHARES,
                particlesPerTick,
                FireworkStyle.TrailTier.STANDARD,
                maxLifetimeTicks,
                Math.max(1, maxLifetimeTicks - 4),
                Math.max(1, maxLifetimeTicks - 8),
                0.35F,
                0.60F,
                null,
                null,
                null,
                GiantTier.NONE);
    }

    private static FireworkStyle giantStyle(int index, AdditionalGiantDefinition definition) {
        return new FireworkStyle(
                index,
                definition.id(),
                definition.zhName(),
                definition.enName(),
                definition.family(),
                FireworkStyle.Shape.GIANT_RADIANT,
                FireworkStyle.Rgb.fromHex(definition.primary()),
                FireworkStyle.Rgb.fromHex(definition.secondary()),
                FireworkStyle.Rgb.fromHex(definition.accent()),
                138,
                definition.fullEnvelope(),
                definition.fullEnvelope(),
                0,
                definition.totalParticles(),
                SPECIAL_SHARES,
                definition.maxParticlesPerTick(),
                FireworkStyle.TrailTier.GRAND,
                definition.maxLifetimeTicks(),
                Math.max(1, definition.maxLifetimeTicks() - 12),
                Math.max(1, definition.maxLifetimeTicks() - 24),
                0.35F,
                0.60F,
                null,
                null,
                null,
                definition.tier());
    }

    private static List<AdditionalGiantDefinition> additionalGiantDefinitions() {
        return List.of(
                new AdditionalGiantDefinition(
                        "giant_jade_gold_palm_firework",
                        "巨型翡翠金棕榈垂爆烟花",
                        "Giant Jade-Gold Palm Break Firework",
                        FireworkStyle.Family.JEWEL,
                        "#178C68", "#D9A832", "#FFF2C2",
                        252, 10_944, 576, 172, GiantTier.PALM),
                new AdditionalGiantDefinition(
                        "giant_aurora_spiral_firework",
                        "巨型极光三维螺旋层放射烟花",
                        "Giant Aurora Three-Dimensional Spiral Firework",
                        FireworkStyle.Family.COOL,
                        "#216CDA", "#66DFFF", "#F2FCFF",
                        248, 9_024, 288, 134, GiantTier.SPIRAL),
                new AdditionalGiantDefinition(
                        "giant_chrysanthemum_multishell_firework",
                        "巨型三维千轮菊烟花",
                        "Giant Chrysanthemum Multi-Shell Firework",
                        FireworkStyle.Family.METALLIC,
                        "#F0B93F", "#FFE186", "#FFF7DF",
                        258, 12_984, 318, 172, GiantTier.CHRYSANTHEMUM_MULTI_SHELL),
                new AdditionalGiantDefinition(
                        "giant_interlaced_cometfield_firework",
                        "巨型交错彗星场烟花",
                        "Giant Interlaced Comet Field Firework",
                        FireworkStyle.Family.COOL,
                        "#2F77BF", "#74E4F4", "#F1FCFF",
                        258, 6_308, 126, 164, GiantTier.INTERLACED_COMET_FIELD));
    }

    private static FireworkStyle.Family familyFor(MediumSphereDefinition.PaletteFamily family) {
        return switch (family) {
            case WARM -> FireworkStyle.Family.WARM;
            case COOL -> FireworkStyle.Family.COOL;
            case RED_PURPLE -> FireworkStyle.Family.JEWEL;
            case METALLIC -> FireworkStyle.Family.METALLIC;
        };
    }

    private static String hex(MidsizeRadialFireworkDefinition.Rgb color) {
        return String.format("#%02X%02X%02X",
                Math.round(color.red() * 255.0F),
                Math.round(color.green() * 255.0F),
                Math.round(color.blue() * 255.0F));
    }

    private static void validate(List<Entry> mapped, int firstStyleIndex) {
        if (mapped.size() != TOTAL_STYLE_COUNT
                || SmallFireworkCatalog.values().size() != SMALL_STYLE_COUNT
                || MediumSphereCatalog.values().size() != MEDIUM_SPHERE_STYLE_COUNT
                || MidsizeRadialFireworkCatalog.values().size() != MEDIUM_RADIAL_STYLE_COUNT
                || LargeExtraFireworkCatalog.values().size() != LARGE_EXTRA_STYLE_COUNT) {
            throw new IllegalStateException("Integrated batch count drifted");
        }
        Set<String> ids = new HashSet<>();
        int expectedIndex = firstStyleIndex;
        for (Entry entry : mapped) {
            FireworkStyle style = entry.style();
            if (!ids.add(style.id()) || style.index() != expectedIndex++ || style.id().isBlank()
                    || style.zhName().isBlank() || style.enName().isBlank()) {
                throw new IllegalStateException("Integrated style id or append-only index drifted for " + style.id());
            }
            if (entry.kind() == Kind.ADDITIONAL_GIANT) {
                if (style.giantTier() == GiantTier.NONE || style.shape() != FireworkStyle.Shape.GIANT_RADIANT) {
                    throw new IllegalStateException("Additional giant must retain a typed giant tier");
                }
            } else if (style.giantTier() != GiantTier.NONE || style.shape() != FireworkStyle.Shape.OTHER) {
                throw new IllegalStateException("Non-giant integrated styles must use their typed client route");
            }
        }
    }

    private record AdditionalGiantDefinition(
            String id,
            String zhName,
            String enName,
            FireworkStyle.Family family,
            String primary,
            String secondary,
            String accent,
            int fullEnvelope,
            int totalParticles,
            int maxParticlesPerTick,
            int maxLifetimeTicks,
            GiantTier tier) {
        private AdditionalGiantDefinition {
            if (id == null || !id.startsWith("giant_") || zhName == null || zhName.isBlank()
                    || enName == null || enName.isBlank() || fullEnvelope <= 0 || fullEnvelope > 260
                    || totalParticles <= 0 || maxParticlesPerTick <= 0 || maxLifetimeTicks <= 0
                    || tier == null || tier == GiantTier.NONE) {
                throw new IllegalArgumentException("Invalid additional giant definition " + id);
            }
        }
    }
}
