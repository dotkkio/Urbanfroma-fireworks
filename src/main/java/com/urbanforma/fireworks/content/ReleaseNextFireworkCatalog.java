package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerItemDefinitions;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerTrajectory.ColorBand;
import com.urbanforma.fireworks.content.release_next.giant_radial.GiantRadialReleaseNext;
import com.urbanforma.fireworks.content.release_next.giant_willow.GiantWillowReleaseNextCatalog;
import com.urbanforma.fireworks.content.release_next.large_extension.LargeExtensionCatalog;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionCatalog;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
import com.urbanforma.fireworks.content.release_next.small_shapes.SmallShapeCatalog;
import com.urbanforma.fireworks.content.release_next.small_sphere.SmallSphereCatalog;
import com.urbanforma.fireworks.content.release_next.small_sphere.SmallSphereDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Append-only public bridge for the reviewed release-next item contracts. */
public final class ReleaseNextFireworkCatalog {
    public static final int FIRST_STYLE_INDEX = IntegratedFireworkCatalog.FIRST_STYLE_INDEX
            + IntegratedFireworkCatalog.TOTAL_STYLE_COUNT;
    public static final int TOTAL_STYLE_COUNT = 95;
    private static final FireworkStyle.LayerShares SHARES = new FireworkStyle.LayerShares(520, 350, 130);
    private static final Map<String, LocalizedName> MEDIUM_NAMES = Map.ofEntries(
            Map.entry("medium_extension_amber_dahlia", new LocalizedName("\u7425\u73c0\u5927\u4e3d\u82b1\u4e2d\u578b\u70df\u82b1", "Amber Dahlia Medium Firework")),
            Map.entry("medium_extension_ruby_twin_orb", new LocalizedName("\u7ea2\u5b9d\u77f3\u53cc\u73e0\u4e2d\u578b\u70df\u82b1", "Ruby Twin Orb Medium Firework")),
            Map.entry("medium_extension_teal_moon", new LocalizedName("\u9752\u7fe0\u6708\u73af\u4e2d\u578b\u70df\u82b1", "Teal Moon Medium Firework")),
            Map.entry("medium_extension_violet_braid", new LocalizedName("\u7d2b\u7f57\u5170\u7f16\u7ec7\u4e2d\u578b\u70df\u82b1", "Violet Braid Medium Firework")),
            Map.entry("medium_extension_golden_lance", new LocalizedName("\u9540\u91d1\u5149\u67aa\u4e2d\u578b\u70df\u82b1", "Golden Lance Medium Firework")),
            Map.entry("medium_extension_coral_wave", new LocalizedName("\u73ca\u745a\u6ce2\u6d9b\u4e2d\u578b\u70df\u82b1", "Coral Wave Medium Firework")),
            Map.entry("medium_extension_aurora_arc", new LocalizedName("\u6781\u5149\u5f27\u7ebf\u4e2d\u578b\u70df\u82b1", "Aurora Arc Medium Firework")),
            Map.entry("medium_extension_crimson_fracture", new LocalizedName("\u7eef\u7ea2\u88c2\u53d8\u4e2d\u578b\u70df\u82b1", "Crimson Fracture Medium Firework")),
            Map.entry("medium_extension_amethyst_ring", new LocalizedName("\u7d2b\u6c34\u6676\u73af\u5fc3\u4e2d\u578b\u70df\u82b1", "Amethyst Ring Medium Firework")),
            Map.entry("medium_extension_champagne_hollow", new LocalizedName("\u9999\u69df\u7a7a\u5fc3\u4e2d\u578b\u70df\u82b1", "Champagne Hollow Medium Firework")),
            Map.entry("medium_extension_cobalt_ice", new LocalizedName("\u94b4\u84dd\u51b0\u6676\u4e2d\u578b\u70df\u82b1", "Cobalt Ice Medium Firework")),
            Map.entry("medium_extension_copper_annular", new LocalizedName("\u8d64\u94dc\u73af\u5e55\u4e2d\u578b\u70df\u82b1", "Copper Annular Medium Firework")),
            Map.entry("medium_extension_saffron_short_ray", new LocalizedName("\u85cf\u7ea2\u77ed\u5c04\u7ebf\u4e2d\u578b\u70df\u82b1", "Saffron Short-Ray Medium Firework")),
            Map.entry("medium_extension_rose_short_drop", new LocalizedName("\u73ab\u7470\u77ed\u5782\u843d\u4e2d\u578b\u70df\u82b1", "Rose Short-Drop Medium Firework")),
            Map.entry("medium_extension_jade_short_palm", new LocalizedName("\u7fe1\u7fe0\u77ed\u68d5\u6988\u4e2d\u578b\u70df\u82b1", "Jade Short-Palm Medium Firework")),
            Map.entry("medium_extension_plum_short_cascade", new LocalizedName("\u6885\u7d2b\u77ed\u7011\u5e03\u4e2d\u578b\u70df\u82b1", "Plum Short-Cascade Medium Firework")),
            Map.entry("medium_extension_coral_pulse", new LocalizedName("\u73ca\u745a\u8109\u51b2\u4e2d\u578b\u70df\u82b1", "Coral Pulse Medium Firework")),
            Map.entry("medium_extension_triple_aurora_pulse", new LocalizedName("\u4e09\u91cd\u6781\u5149\u8109\u51b2\u4e2d\u578b\u70df\u82b1", "Triple Aurora Pulse Medium Firework")),
            Map.entry("medium_extension_amber_strobe_pulse", new LocalizedName("\u7425\u73c0\u95ea\u70c1\u8109\u51b2\u4e2d\u578b\u70df\u82b1", "Amber Strobe Pulse Medium Firework")),
            Map.entry("medium_extension_ruby_delayed_pulse", new LocalizedName("\u7ea2\u5b9d\u77f3\u5ef6\u65f6\u8109\u51b2\u4e2d\u578b\u70df\u82b1", "Ruby Delayed Pulse Medium Firework")),
            Map.entry("medium_extension_solar_crossweave", new LocalizedName("\u65e5\u8000\u4ea4\u7ec7\u4e2d\u578b\u70df\u82b1", "Solar Crossweave Medium Firework")),
            Map.entry("medium_extension_orchid_phase_shell", new LocalizedName("\u5170\u82b1\u76f8\u4f4d\u5c42\u4e2d\u578b\u70df\u82b1", "Orchid Phase Shell Medium Firework")),
            Map.entry("medium_extension_garnet_lattice", new LocalizedName("\u77f3\u69b4\u77f3\u683c\u7f51\u4e2d\u578b\u70df\u82b1", "Garnet Lattice Medium Firework")),
            Map.entry("medium_extension_icewheel_interleave", new LocalizedName("\u51b0\u8f6e\u4ea4\u7ec7\u4e2d\u578b\u70df\u82b1", "Icewheel Interleave Medium Firework")));
    private static List<Entry> entries;
    private static Map<String, Entry> byId;

    public enum Kind { SMALL_SPHERE, SMALL_SHAPE, MEDIUM, LARGE, GIANT_RADIAL, GIANT_WILLOW, GIANT_MULTILAYER }
    public record Entry(Kind kind, FireworkStyle style) { }

    private ReleaseNextFireworkCatalog() { }

    public static synchronized List<FireworkStyle> stylesFrom(int firstStyleIndex) {
        return initialize(firstStyleIndex).stream().map(Entry::style).toList();
    }
    public static List<Entry> entries() { return initialize(FIRST_STYLE_INDEX); }
    public static boolean contains(String id) { return id != null && index().containsKey(id); }
    public static Entry require(String id) {
        Entry entry = index().get(id);
        if (entry == null) throw new IllegalArgumentException("Unknown release-next firework " + id);
        return entry;
    }
    private static synchronized Map<String, Entry> index() { initialize(FIRST_STYLE_INDEX); return byId; }

    private static List<Entry> initialize(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) throw new IllegalArgumentException("Release-next styles must append after the established catalog");
        if (entries != null) return entries;
        List<Entry> mapped = new ArrayList<>(TOTAL_STYLE_COUNT);
        int[] next = {firstStyleIndex};
        for (SmallSphereDefinition value : SmallSphereCatalog.values()) mapped.add(entry(Kind.SMALL_SPHERE, standard(next[0]++, value.id(), value.zhName(), value.enName(), value.palette().primary(), value.palette().secondary(), value.palette().accent(), 24, 13, 384, 48, 28)));
        for (SmallShapeCatalog.Definition value : SmallShapeCatalog.values()) mapped.add(entry(Kind.SMALL_SHAPE, standard(next[0]++, value.id(), value.zhName(), value.enName(), value.palette().primary(), value.palette().secondary(), value.palette().accent(), 24, 18, value.totalParticles(), value.particlesPerTick(), value.maxLifetimeTicks())));
        for (MediumExtensionDefinition value : MediumExtensionCatalog.values()) {
            LocalizedName name = mediumName(value.id());
            mapped.add(entry(Kind.MEDIUM, standard(next[0]++, value.id(), name.zhCn(), name.enUs(), value.primary(), value.secondary(), value.accent(), 72, (int) Math.ceil(value.maximumRadius() * 2.0D), value.totalParticles(), value.particlesPerTick(), value.maximumLifetime())));
        }
        for (LargeExtensionCatalog.Entry value : LargeExtensionCatalog.values()) mapped.add(entry(Kind.LARGE, standard(next[0]++, value.id(), value.zhName(), value.enName(), value.palette().primary(), value.palette().secondary(), value.palette().accent(), 96, value.fullEnvelopeBlocks(), value.budget().plannedParticles(), value.budget().perTick(), value.budget().maxLifetimeTicks())));
        for (GiantRadialReleaseNext.ItemMetadata value : GiantRadialReleaseNext.items()) mapped.add(entry(Kind.GIANT_RADIAL, giant(next[0]++, value.stableId(), value.chineseName(), value.englishName(), value.palette().primary(), value.palette().secondary(), value.palette().accent(), (int) Math.ceil(value.radiusBlocks() * 2.0D), value.totalParticles(), value.peakParticlesPerTick(), value.maxLifetimeTicks(), tier(value.suggestedGiantTier()))));
        for (GiantWillowReleaseNextCatalog.Entry value : GiantWillowReleaseNextCatalog.values()) mapped.add(entry(Kind.GIANT_WILLOW, giant(next[0]++, value.stableId(), value.zhName(), value.enName(), value.palette().primary(), value.palette().secondary(), value.palette().accent(), (int) Math.ceil(value.envelope().maximumDistance()), value.budget().totalParticles(), value.budget().peakParticlesPerTick(), value.budget().maximumLifetimeTicks(), value.suggestedTier() == GiantWillowReleaseNextCatalog.SuggestedTier.SUPER_WILLOW ? GiantTier.SUPER_WILLOW : GiantTier.EXTRA_LARGE)));
        for (GiantMultilayerItemDefinitions.Definition value : GiantMultilayerItemDefinitions.values()) mapped.add(entry(Kind.GIANT_MULTILAYER, giant(next[0]++, value.item().path(), value.zhCnName(), value.enUsName(), color(value.palette().get(0)), color(value.palette().get(Math.min(1, value.palette().size() - 1))), color(value.palette().get(value.palette().size() - 1)), 260, value.totalParticleBudget(), value.peakParticlesPerTick(), 144, GiantTier.MULTI_RADIAL_II)));
        if (mapped.size() != TOTAL_STYLE_COUNT) throw new IllegalStateException("Release-next item count drifted: " + mapped.size());
        Map<String, Entry> indexed = new HashMap<>();
        for (Entry value : mapped) if (indexed.put(value.style().id(), value) != null) throw new IllegalStateException("Duplicate release-next id " + value.style().id());
        entries = List.copyOf(mapped); byId = Map.copyOf(indexed); return entries;
    }
    private static Entry entry(Kind kind, FireworkStyle style) { return new Entry(kind, style); }
    private static FireworkStyle standard(int index, String id, String zh, String en, String p, String s, String a, int flight, int envelope, int total, int perTick, int lifetime) {
        return new FireworkStyle(index, id, zh, en, FireworkStyle.Family.JEWEL, FireworkStyle.Shape.OTHER, FireworkStyle.Rgb.fromHex(p), FireworkStyle.Rgb.fromHex(s), FireworkStyle.Rgb.fromHex(a), flight, envelope, envelope, 0, total, SHARES, perTick, FireworkStyle.TrailTier.STANDARD, lifetime, Math.max(1, lifetime - 4), Math.max(1, lifetime - 8), .35F, .60F, null, null, null, GiantTier.NONE);
    }
    private static FireworkStyle giant(int index, String id, String zh, String en, String p, String s, String a, int envelope, int total, int perTick, int lifetime, GiantTier tier) {
        return new FireworkStyle(index, id, zh, en, FireworkStyle.Family.JEWEL, FireworkStyle.Shape.GIANT_RADIANT, FireworkStyle.Rgb.fromHex(p), FireworkStyle.Rgb.fromHex(s), FireworkStyle.Rgb.fromHex(a), 138, envelope, envelope, 0, total, SHARES, perTick, FireworkStyle.TrailTier.GRAND, lifetime, Math.max(1, lifetime - 12), Math.max(1, lifetime - 24), .35F, .60F, null, null, null, tier);
    }
    private static GiantTier tier(String value) { return switch (value) { case "THICK_RADIAL" -> GiantTier.THICK_RADIAL; case "MULTI_RADIAL_II" -> GiantTier.MULTI_RADIAL_II; default -> GiantTier.LARGE; }; }
    private static String color(ColorBand band) { return switch (band) { case PEARL -> "#FFF5C7"; case GOLD -> "#FFB021"; case AMBER -> "#FF5912"; case JADE -> "#3DFFA1"; case AZURE -> "#59BAFF"; case VIOLET -> "#C26BFF"; case CRIMSON -> "#FF3038"; }; }
    private static LocalizedName mediumName(String id) {
        LocalizedName name = MEDIUM_NAMES.get(id);
        if (name == null) throw new IllegalStateException("Missing localized medium name for " + id);
        return name;
    }
    private record LocalizedName(String zhCn, String enUs) { }
    private static String title(String id) { return id.replace("medium_extension_", "").replace('_', ' ') + " medium firework"; }
    private static String chinese(String id) { return "中型延展烟花 " + id.substring("medium_extension_".length()); }
}
