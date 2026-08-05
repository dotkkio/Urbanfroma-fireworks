package com.urbanforma.fireworks.content.batch05;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** Visual-difference ledger for batch05; it is intentionally independent from shared style/registry code. */
public final class Batch05VisualSignatures {
    private static final Map<String, Batch05FireworkDefinition.VisualSignature> VALUES = Map.ofEntries(
            entry("batch05_saffron_sunburst_radiant", "wide radial spherical bloom", "warm pin core", "short ember tips", "three radial bands", "single pulse", "dense center", "saffron-gold", "broad spherical bloom with a tight bright core"),
            entry("batch05_cinnabar_comet_radiant", "forward comet burst", "red-orange spear", "long swept terminals", "offset outer layer", "staggered pulse", "medium", "cinnabar-amber", "one-sided comet sweep shifts the outer radial balance"),
            entry("batch05_amethyst_orbit_radiant", "orbital radial loops", "violet ring core", "fine curved tips", "nested orbit bands", "double beat", "light", "amethyst-lilac", "open orbital loops replace the solid radial density"),
            entry("batch05_platinum_aurora_radiant", "needle starburst", "white diamond core", "cool-white needles", "thin three-tier shell", "rapid staccato", "sparse", "platinum-champagne", "sparse needle spacing creates a crisp metallic read"),
            entry("batch05_copper_sunset_radiant_willow", "radiant willow curtain", "dark copper seed", "long hooked drops", "retained curtain layers", "slow release", "dense lower skirt", "copper-sunset", "hooked terminal drops make a curtain instead of a sphere"),
            entry("batch05_rose_champagne_radiant_willow", "soft willow umbrella", "pink pearl core", "wide soft arcs", "high crown then skirt", "two-stage fall", "medium lower veil", "rose-champagne", "a high crown and soft veil separate the layers"),
            entry("batch05_orchid_moon_radiant_willow", "narrow moon willow", "violet crescent core", "thin hanging threads", "deep inner veil", "late lull", "sparse threads", "orchid-moon", "narrow hanging threads leave deliberate negative space"),
            entry("batch05_solar_amber_hybrid", "sphere crossing radial", "golden inner sphere", "radial crossover trails", "solid sphere plus outer ring", "overlapped pulse", "high center density", "solar-amber", "radial lines visibly cross the smaller sphere shell"),
            entry("batch05_coral_flare_hybrid", "coral burst hybrid", "coral double core", "curved crossover arcs", "inner and outer shells", "quick then drag", "medium-high", "coral-peach", "a double core gives the hybrid a two-beat center"),
            entry("batch05_gilded_platinum_hybrid", "metallic halo hybrid", "platinum bead core", "thin halo spokes", "halo between sphere and radial", "measured pulse", "sparse halo", "gilded-platinum", "an empty halo gap separates the two geometries"),
            entry("batch05_teal_opal_hybrid", "opal tide hybrid", "teal pearl core", "rippling crossover", "offset translucent-feel shells", "rolling pulse", "medium ripple", "teal-opal", "rippled crossover rhythm breaks the straight spokes"),
            entry("batch05_copper_crown_saturn", "crowned Saturn", "copper outer shell", "three level rings", "sphere then crown rings", "ascending cadence", "dense crown", "copper-gold", "rings rise in a crown above the shell"),
            entry("batch05_rose_garnet_saturn", "tilted garnet Saturn", "garnet inner sphere", "two broad tilted rings", "outer shell plus wide pair", "slow double beat", "medium", "rose-garnet", "a broad opposing ring pair gives a tilted axis"),
            entry("batch05_violet_opal_saturn", "opal ring stack", "violet bead shell", "four fine rings", "many thin layers", "even ticking", "light multi-ring", "violet-opal", "four fine rings favor rhythm over particle mass"),
            entry("batch05_aqua_platinum_saturn", "single halo Saturn", "aqua outer shell", "one wide inclined halo", "single outer layer", "long hold", "sparse halo", "aqua-platinum", "one wide halo leaves the sphere visibly exposed"),
            entry("batch05_ember_twilight_radiant", "twilight radial bloom", "ember-to-violet core", "split-color terminal rays", "warm first layer then target layer", "delayed switch", "dense then airy", "ember-violet-shift", "a timed core color switch changes the second half"),
            entry("batch05_sunset_orchid_willow", "blooming willow", "sunset bead core", "orchid droplet tips", "warm crown to purple veil", "delayed fall", "medium veil", "sunset-orchid-shift", "the color switch is paired with a bloom-shaped veil"),
            entry("batch05_aurora_pearl_hybrid", "pearl tide hybrid", "pearl sphere core", "aurora crossover arcs", "warm sphere to aqua radial", "delayed rolling switch", "medium ripple", "amber-aqua-shift", "the target palette is reserved for the outer crossover arcs"));

    static {
        if (VALUES.size() != Batch05FireworkCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Visual ledger must cover every batch05 entry");
        }
        List<Batch05FireworkDefinition.VisualSignature> signatures = VALUES.values().stream().toList();
        for (Batch05FireworkDefinition.VisualSignature signature : signatures) {
            if (signature.visualDifference().isBlank() || signature.structuralAxes().isEmpty()) {
                throw new IllegalStateException("A batch05 visual signature cannot be palette-only");
            }
        }
    }

    private Batch05VisualSignatures() {
    }

    public static Batch05FireworkDefinition.VisualSignature forId(String id) {
        Batch05FireworkDefinition.VisualSignature signature = VALUES.get(id);
        if (signature == null) {
            throw new IllegalArgumentException("Missing batch05 visual signature for " + id);
        }
        return signature;
    }

    public static void validateAdjacent(List<Batch05FireworkDefinition> entries) {
        for (int index = 1; index < entries.size(); index++) {
            Batch05FireworkDefinition previous = entries.get(index - 1);
            Batch05FireworkDefinition current = entries.get(index);
            Batch05FireworkDefinition.VisualSignature previousSignature = previous.visualSignature();
            Batch05FireworkDefinition.VisualSignature currentSignature = current.visualSignature();
            if (previousSignature.structuralSignature().equals(currentSignature.structuralSignature())
                    && previousSignature.paletteToken().equals(currentSignature.paletteToken())) {
                throw new IllegalStateException("Adjacent batch05 entries repeat shape and palette: "
                        + previous.id() + " / " + current.id());
            }
        }
    }

    private static Map.Entry<String, Batch05FireworkDefinition.VisualSignature> entry(
            String id,
            String shape,
            String core,
            String tail,
            String layering,
            String rhythm,
            String density,
            String paletteToken,
            String difference) {
        return Map.entry(id, new Batch05FireworkDefinition.VisualSignature(
                shape, core, tail, layering, rhythm, density, paletteToken, difference,
                EnumSet.of(
                        Batch05FireworkDefinition.VisualSignature.VisualAxis.SHAPE,
                        Batch05FireworkDefinition.VisualSignature.VisualAxis.CORE,
                        Batch05FireworkDefinition.VisualSignature.VisualAxis.TRAIL,
                        Batch05FireworkDefinition.VisualSignature.VisualAxis.LAYERING,
                        Batch05FireworkDefinition.VisualSignature.VisualAxis.CADENCE,
                        Batch05FireworkDefinition.VisualSignature.VisualAxis.DENSITY)));
    }
}
