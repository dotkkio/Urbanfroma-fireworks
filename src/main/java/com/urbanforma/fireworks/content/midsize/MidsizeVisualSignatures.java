package com.urbanforma.fireworks.content.midsize;

import java.util.EnumSet;
import java.util.Map;

/** Isolated visual-difference ledger for the two midsize trial definitions. */
public final class MidsizeVisualSignatures {
    private static final Map<String, MidsizeFireworkDefinition.VisualSignature> VALUES = Map.of(
            "midsize_dense_sphere_firework",
            signature(
                    "compact dahlia sphere",
                    "four short amber bead rings keep the center hollow",
                    "short pearl tips end alternating petals",
                    "bead core, braided middle shell, clipped outer crown",
                    "four compact core beats followed by fourteen complete shell rings",
                    "1,728 nodes in a 52.5-block envelope at 96 nodes per tick",
                    "amber pearl",
                    "Petal-modulated bead layers and a clipped crown make this a compact dahlia, not a uniformly scaled sphere."),
            "midsize_dense_radial_firework",
            signature(
                    "braided radial chrysanthemum",
                    "eight ember rings form a tight hollow pin core",
                    "late pearl terminals split from the warm radial body",
                    "dense core, helical middle lattice, delayed terminal corona",
                    "eight core rings, thirty-two braid rings, then twelve terminal rings",
                    "9,984 nodes in a 130-block envelope at 192 nodes per tick",
                    "ember gold pearl",
                    "Seeded helical spokes and a delayed terminal corona change the radial silhouette beyond a simple half-scale giant."));

    private MidsizeVisualSignatures() {
    }

    public static MidsizeFireworkDefinition.VisualSignature forId(String id) {
        MidsizeFireworkDefinition.VisualSignature signature = VALUES.get(id);
        if (signature == null) {
            throw new IllegalArgumentException("Missing midsize visual signature for " + id);
        }
        return signature;
    }

    public static int count() {
        return VALUES.size();
    }

    private static MidsizeFireworkDefinition.VisualSignature signature(
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String paletteToken,
            String visualDifference) {
        return new MidsizeFireworkDefinition.VisualSignature(
                shape,
                core,
                trail,
                layering,
                cadence,
                density,
                paletteToken,
                visualDifference,
                EnumSet.allOf(MidsizeFireworkDefinition.VisualAxis.class));
    }
}
