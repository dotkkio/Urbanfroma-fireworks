package com.urbanforma.fireworks.content.large_extra;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Six-axis, non-palette visual-difference ledger for the isolated Large Fireworks batch. */
public final class LargeExtraVisualSignatures {
    private static final Map<String, LargeExtraFireworkDefinition.VisualDifference> VALUES = Map.ofEntries(
            Map.entry(
                    "large_extra_aurora_globe_shell",
                    signature(
                            "fibonacci globe shell with alternating latitude gaps",
                            "quiet hollow pearl nucleus",
                            "one continuous meridian shell",
                            "eighteen uniform globe frames",
                            "latitudinal outward sweep",
                            "pale polar-cap twinkle",
                            "A complete globe is read from interrupted latitude bands rather than from a conventional flower core.")),
            Map.entry(
                    "large_extra_cinnabar_triple_tier_radiance",
                    signature(
                            "three staggered radial tiers",
                            "amber three-point ignition",
                            "low middle and high radial crowns",
                            "tier rotation on every frame",
                            "outward fan release by tier",
                            "upper-rim spark closure",
                            "Three separated radial crowns rotate through time, so the result is not a scaled single radiant shell.")),
            Map.entry(
                    "large_extra_jade_dual_break",
                    signature(
                            "parent globe opening into opposed daughter shells",
                            "brief twin jade cores",
                            "one parent shell then two offset children",
                            "thirteen parent frames followed by thirteen split frames",
                            "two-center lateral bifurcation",
                            "paired white closure points",
                            "The visible break changes centers halfway through the effect rather than merely adding another colored layer.")),
            Map.entry(
                    "large_extra_cobalt_world_grid",
                    signature(
                            "latitude-longitude globe lattice",
                            "fixed sapphire pin",
                            "six latitude belts crossed by eight meridians",
                            "alternating meridian and parallel frames",
                            "pole-to-equator grid closure",
                            "equator flare",
                            "A sampled world-grid shell exposes geographic linework and deliberate negative space instead of a solid ball.")),
            Map.entry(
                    "large_extra_amber_stout_comet",
                    signature(
                            "eighteen thick short comet bundles",
                            "dense sunstone knot",
                            "compressed radial shafts with blunt tips",
                            "rapid full-density barrage",
                            "short forward pulse without long tails",
                            "pearl-ended bundle caps",
                            "This burst uses compact heavy bundles and no long radial trajectories, separating it from the existing giant thick radial.")),
            Map.entry(
                    "large_extra_violet_aperture_hex_reveal",
                    signature(
                            "six aperture petals framing a hexagonal reveal",
                            "late central hex beacon",
                            "sliding aperture rim then exposed core",
                            "sixteen aperture frames then twelve reveal frames",
                            "shutters unlock from rim to center",
                            "lavender hex fade",
                            "Six radial aperture petals open a central window; this is neither a delayed-core shell nor a conventional flower burst.")),
            Map.entry(
                    "large_extra_teal_orbital_nucleus",
                    signature(
                            "three eccentric orbital knots around one nucleus",
                            "stationary aqua nucleus",
                            "three inclined off-center loops",
                            "eight orbit passes with phase offsets",
                            "counter-precessing node drift",
                            "three satellite flashes",
                            "The structure is a set of offset orbital knots, not a concentric Saturn ring or a conventional spherical shell.")),
            Map.entry(
                    "large_extra_rose_interwoven_radiance",
                    signature(
                            "paired counter-phased spherical weaving nets",
                            "rose bridge seed",
                            "two intersecting latitude-free meshes",
                            "alternating weave turns",
                            "opposed lateral crossings",
                            "interlock flashes at mesh crossings",
                            "Two whole spherical nets weave through each other, distinct from the existing axial braid and any single-spoke radiant.")),
            Map.entry(
                    "large_extra_platinum_polar_lantern",
                    signature(
                            "mirrored polar lantern lobes",
                            "dual north-south anchors",
                            "eight pinched vertical lobes",
                            "top-bottom alternation",
                            "pole-to-equator unfurling",
                            "equatorial silver bead belt",
                            "Symmetric pinched lobes create a lantern silhouette without the stepped tracks used by the existing polar effect.")),
            Map.entry(
                    "large_extra_copper_eclipse_arc_split",
                    signature(
                            "two eclipsing crescent arcs that split at their endpoints",
                            "off-axis copper eclipse knot",
                            "overlapping crescents then four endpoint fragments",
                            "seventeen arc frames then nine endpoint-split frames",
                            "opposed arc closure",
                            "four amber split tips",
                            "The endpoints break only after the crescents overlap, producing an eclipse sequence rather than a static ring or split shell.")));

    private LargeExtraVisualSignatures() {
    }

    public static LargeExtraFireworkDefinition.VisualDifference forId(String id) {
        LargeExtraFireworkDefinition.VisualDifference value = VALUES.get(id);
        if (value == null) {
            throw new IllegalArgumentException("Missing Large Extra visual signature for " + id);
        }
        return value;
    }

    public static int count() {
        return VALUES.size();
    }

    static void validateDefinitions(List<LargeExtraFireworkDefinition> definitions) {
        if (definitions.size() != VALUES.size()) {
            throw new IllegalStateException("Large Extra signature count drifted");
        }
        Set<String> structureSignatures = new HashSet<>();
        for (LargeExtraFireworkDefinition definition : definitions) {
            LargeExtraFireworkDefinition.VisualDifference expected = forId(definition.id());
            if (!definition.visualDifference().equals(expected)
                    || !structureSignatures.add(expected.structureSignature())) {
                throw new IllegalStateException("Large Extra visual signature drifted for " + definition.id());
            }
        }
        for (int first = 0; first < definitions.size(); first++) {
            for (int second = first + 1; second < definitions.size(); second++) {
                LargeExtraFireworkDefinition.VisualDifference left = definitions.get(first).visualDifference();
                LargeExtraFireworkDefinition.VisualDifference right = definitions.get(second).visualDifference();
                if (left.differingAxisCount(right) < 3) {
                    throw new IllegalStateException("Large Extra items cannot be palette-only variants");
                }
            }
        }
    }

    private static LargeExtraFireworkDefinition.VisualDifference signature(
            String geometry,
            String core,
            String layering,
            String cadence,
            String motion,
            String terminal,
            String description) {
        String structuralSignature = String.join(" | ", List.of(geometry, core, layering, cadence, motion, terminal));
        return new LargeExtraFireworkDefinition.VisualDifference(
                geometry, core, layering, cadence, motion, terminal, structuralSignature, description);
    }
}
