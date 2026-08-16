package com.urbanforma.fireworks.content.small;

import java.util.EnumSet;
import java.util.Map;

/** Isolated structural-difference ledger for the two small-firework prototypes. */
public final class SmallVisualSignatures {
    private static final Map<String, SmallFireworkDefinition.VisualSignature> VALUES = Map.of(
            "small_layered_sphere_firework",
            signature(
                    "layered peony sphere",
                    "three cool-white bead rings form a bright inner kernel",
                    "short ice-blue tips finish the outer petals",
                    "pearl kernel, cyan petal shell, pale outer edge",
                    "three inner beats followed by eight complete shell rings",
                    "616 nodes in a 16-block envelope at 56 nodes per tick",
                    "azure ice pearl",
                    "A visible inner kernel, deliberate gap, and petal-modulated shell make this a compact sphere instead of a uniformly scaled burst."),
            "small_compact_radial_firework",
            signature(
                    "short full three-dimensional radial bloom",
                    "two ember ignition rings establish a warm pin core",
                    "two pearl-gold terminal flares cap every short spoke",
                    "ember core, gold spoke body, pearl terminal flare",
                    "two ignition rings, four full spoke rings, then two terminal rings",
                    "640 nodes in a 19.2-block envelope at 80 nodes per tick",
                    "ember gold pearl",
                    "Eighty seeded directions span every axis with curled short spokes and terminal flares, so this is a full radial bloom rather than a single straight line."));

    private SmallVisualSignatures() {
    }

    public static SmallFireworkDefinition.VisualSignature forId(String id) {
        SmallFireworkDefinition.VisualSignature signature = VALUES.get(id);
        if (signature == null) {
            throw new IllegalArgumentException("Missing small-firework visual signature for " + id);
        }
        return signature;
    }

    public static int count() {
        return VALUES.size();
    }

    private static SmallFireworkDefinition.VisualSignature signature(
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String paletteToken,
            String visualDifference) {
        return new SmallFireworkDefinition.VisualSignature(
                shape,
                core,
                trail,
                layering,
                cadence,
                density,
                paletteToken,
                visualDifference,
                EnumSet.allOf(SmallFireworkDefinition.VisualAxis.class));
    }
}
