package com.urbanforma.fireworks.content.batch_other_extra;

import java.util.List;

/** Six-axis visual ledger for the additional Other routes. */
public final class BatchOtherExtraVisualSignatures {
    private static final List<String> ALL_AXES = List.of(
            "SHAPE", "CORE", "TRAIL", "LAYERING", "CADENCE", "DENSITY");
    private static final List<Definition> DEFINITIONS = List.of(
            definition("other_extra_axis_weave", "orthogonal weaving axes", "three-axis junction",
                    "sinusoidal cross-drift", "x-y-z beams", "alternating component beats", "medium interlaced",
                    "Open tracks leave the center along three perpendicular axes while transverse offsets weave through space."),
            definition("other_extra_triad_step", "stepped triad planes", "three-point seed",
                    "discrete radial steps", "three canted planes", "three-beat stagger", "layered medium",
                    "Three tilted planes advance in visible steps, with each plane taking a different release beat."),
            definition("other_extra_tetra_twist", "tetrahedral twisting spines", "four-way junction",
                    "rotating transverse drag", "four skewed struts", "paired twist pulses", "dense skewed",
                    "Four skewed spines expand from a tetrahedral junction and carry a controlled transverse twist."),
            definition("other_extra_polar_staircase", "polar staircase tracks", "dual axial seed",
                    "stair-step fall", "opposed vertical tracks", "eight-step climb", "open tiered",
                    "Opposed vertical tracks climb in fixed steps, leaving a readable gap between each height tier."),
            definition("other_extra_prism_return", "prism return paths", "held central node",
                    "out-and-back snap", "three skewed returns", "split then reverse", "medium split",
                    "Three skew paths reach a prism-like extent and then return toward the launch junction in a second phase."),
            definition("other_extra_counter_twist_braid", "counter-twist braid", "paired offset kernel",
                    "opposed helix drag", "two interlaced strands", "counterphase release", "braided medium",
                    "Two strands rotate in opposite directions and cross their depth offsets without forming a closed loop."),
            definition("other_extra_octant_gap", "octant diagonal lattice", "eight-way seed",
                    "gapped diagonal tails", "alternating octant groups", "gap-four rhythm", "sparse volumetric",
                    "Diagonal tracks occupy alternating octants with deliberate vacancies that keep the depth structure legible."),
            definition("other_extra_canted_columns", "canted column stack", "three-axis post",
                    "sheared vertical tails", "three tilted columns", "crossbar fifth beat", "thick columns",
                    "Three tilted columns rise through separate height bands and briefly shear at fixed crossbar beats."),
            definition("other_extra_axial_tunnel", "axial tunnel tracks", "inner transit pulse",
                    "elliptic echo tails", "near and far axial tracks", "echo every two", "deep medium",
                    "Near and far elliptical tracks alternate along the depth axis, creating a tunnel-like 3D passage."),
            definition("other_extra_diagonal_ladder", "diagonal ladder planes", "paired diagonal seed",
                    "rung-separated traces", "two crossing ladders", "rung-four cadence", "gapped medium",
                    "Two diagonal planes are joined only at scheduled rungs, preserving visible separation between their tracks."),
            definition("other_extra_triple_torsion", "triple torsion tracks", "three phase nodes",
                    "breathing torsion drag", "three counterpitched tracks", "four-step phase", "dense oscillatory",
                    "Three tracks breathe in and out while their pitch directions counter-rotate across successive phases."),
            definition("other_extra_split_merge", "split-merge corridors", "single launch junction",
                    "lobe rejoin tails", "separated then convergent tracks", "midpoint separation", "paired medium",
                    "Paired corridors separate at mid-flight and converge again, producing a depth-aware two-stage profile."),
            definition("other_extra_cubic_offset", "cubic offset edges", "offset corner seed",
                    "edge-turn glints", "cycling axis edges", "four-segment turns", "sparse framework",
                    "Open edge tracks cycle through the three coordinate axes with offset corners and no closed perimeter."),
            definition("other_extra_vertical_fracture", "vertical fractured bundles", "stacked axial seed",
                    "broken rebound tails", "four delayed vertical groups", "fracture-six rhythm", "segmented medium",
                    "Four vertical groups are released with fixed fractures and rebound offsets, leaving gaps in every bundle."),
            definition("other_extra_phased_spindle", "phased spindle tracks", "three phase junction",
                    "tapered axial tails", "three rotating spindle bands", "continuous phase sweep", "dense tapered",
                    "Three rotating bands taper toward both ends while their phase offsets keep the long axis readable."));

    private BatchOtherExtraVisualSignatures() {
    }

    public static BatchOtherExtraFirework.VisualDifference forId(String id, String paletteSignature) {
        for (Definition definition : DEFINITIONS) {
            if (definition.id().equals(id)) {
                return new BatchOtherExtraFirework.VisualDifference(
                        definition.shape(),
                        definition.core(),
                        definition.trail(),
                        definition.layering(),
                        definition.cadence(),
                        definition.density(),
                        ALL_AXES,
                        definition.structureSignature(),
                        paletteSignature,
                        definition.description());
            }
        }
        throw new IllegalArgumentException("Missing Extra Other visual signature for " + id);
    }

    public static List<String> ids() {
        return DEFINITIONS.stream().map(Definition::id).toList();
    }

    private static Definition definition(
            String id,
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String description) {
        return new Definition(
                id,
                shape,
                core,
                trail,
                layering,
                cadence,
                density,
                String.join("|", shape, core, trail, layering, cadence, density),
                description);
    }

    private record Definition(
            String id,
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String structureSignature,
            String description) {
    }
}
