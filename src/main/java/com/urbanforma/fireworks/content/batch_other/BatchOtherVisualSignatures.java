package com.urbanforma.fireworks.content.batch_other;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Six-axis visual ledger for the fifteen route-specific client programs. */
public final class BatchOtherVisualSignatures {
    private static final List<String> ALL_AXES = List.of(
            "SHAPE", "CORE", "TRAIL", "LAYERING", "CADENCE", "DENSITY");
    private static final Map<String, BatchOtherFirework.VisualDifference> VALUES = create();

    private BatchOtherVisualSignatures() {
    }

    public static BatchOtherFirework.VisualDifference forId(String id) {
        BatchOtherFirework.VisualDifference difference = VALUES.get(id);
        if (difference == null) {
            throw new IllegalArgumentException("Missing batch_other visual signature for " + id);
        }
        return difference;
    }

    public static void validateAdjacent(List<BatchOtherFirework> entries) {
        Set<String> signatures = new HashSet<>();
        Set<String> ids = new HashSet<>();
        if (entries.size() != BatchOtherCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("batch_other visual ledger must cover all fifteen entries");
        }
        for (BatchOtherFirework entry : entries) {
            if (!ids.add(entry.id()) || !signatures.add(entry.visualDifference().structureSignature())) {
                throw new IllegalStateException("Duplicate batch_other visual identity: " + entry.id());
            }
        }
        for (int index = 1; index < entries.size(); index++) {
            BatchOtherFirework previous = entries.get(index - 1);
            BatchOtherFirework current = entries.get(index);
            BatchOtherFirework.VisualDifference previousDifference = previous.visualDifference();
            BatchOtherFirework.VisualDifference currentDifference = current.visualDifference();
            if (previousDifference.structureSignature().equals(currentDifference.structureSignature())
                    && previous.palette().equals(current.palette())) {
                throw new IllegalStateException("Adjacent batch_other entries repeat structure and palette: "
                        + previous.id() + " / " + current.id());
            }
        }
    }

    private static Map<String, BatchOtherFirework.VisualDifference> create() {
        Map<String, BatchOtherFirework.VisualDifference> values = new LinkedHashMap<>();
        add(values, "other_ember_chrysanthemum", "other_direct_axis_beams", "other_pinpoint_origin",
                "other_short_linear_tips", "other_single_axis_layer", "other_same_tick_release", "other_dense_spokes",
                "Pure radial lines leave the center on straight bounded paths with no spherical shell.");
        add(values, "other_saffron_crossfire", "other_sparse_meridian_lances", "other_empty_center_seed",
                "other_long_isolated_lances", "other_open_meridian_field", "other_three_step_stagger", "other_low_negative_space",
                "A small number of long rays travel farther than the dense entry while leaving broad empty gaps.");
        add(values, "other_coral_pearl_split", "other_offset_dual_belts", "other_delayed_pearl",
                "other_belt_edge_sparks", "other_two_plane_belts", "other_phase_offset_pair", "other_open_belt_spacing",
                "Two annuli appear at different phases and stay visibly separate through the burst.");
        add(values, "other_copper_comet_crown", "other_triple_radial_belts", "other_axial_dot",
                "other_thin_circular_traces", "other_inner_mid_outer_belts", "other_three_beat_release", "other_even_belt_density",
                "Three radii are emitted as discrete concentric rings instead of a filled volume.");
        add(values, "other_ruby_ember_lattice", "other_annular_needle_core", "other_radial_needle_origin",
                "other_ring_terminal_sparks", "other_core_to_annulus_layers", "other_core_first_cadence", "other_mixed_mid_density",
                "A compact radial core is enclosed by a later annulus, giving the burst a ring-core profile.");
        add(values, "other_rose_champagne_petal", "other_hooked_downfall_paths", "other_low_held_origin",
                "other_long_downward_tails", "other_upper_to_lower_curtain", "other_slow_tail_reveal", "other_open_hanging_density",
                "The outer rays bend downward after their reach and finish as a visibly hanging curtain.");
        add(values, "other_plum_garnet_crown", "other_tiered_willow_steps", "other_three_tiered_beads",
                "other_stacked_hanging_threads", "other_upper_mid_lower_tiers", "other_tier_gap_cadence", "other_tiered_negative_space",
                "Separate willow tiers open at different heights, retaining clear gaps between the hanging layers.");
        add(values, "other_amethyst_sunwheel", "other_helical_spoke_corkscrew", "other_offset_axis_seed",
                "other_twisting_spoke_traces", "other_continuous_angular_drift", "other_continuous_turn_cadence", "other_mid_spiral_spacing",
                "Each ray advances outward while its angle turns deterministically around the launch axis.");
        add(values, "other_teal_ice_lantern", "other_odd_even_pulses", "other_two_beat_origin",
                "other_short_pulse_traces", "other_alternating_reach_layers", "other_odd_even_timing", "other_flashing_gap_density",
                "Odd and even branch groups alternate, producing two visible radial pulses rather than one bloom.");
        add(values, "other_cobalt_silver_cross", "other_compact_thick_layers", "other_wide_luminous_origin",
                "other_dense_short_lances", "other_three_thick_ray_layers", "other_near_sync_stack", "other_high_thickness_density",
                "Several short radial layers are emitted close together so the result is broad and dense, not round.");
        add(values, "other_gold_leaf_mosaic", "other_hold_then_shell", "other_late_inner_kernel",
                "other_shell_after_hold_traces", "other_core_hold_outer_release", "other_delayed_shell_cadence", "other_split_density",
                "The inner kernel is held before the outer shell starts, leaving a deliberate empty interval.");
        add(values, "other_vermilion_pearl_blossom", "other_tilted_orbit_belt", "other_axial_micro_core",
                "other_inclined_belt_traces", "other_core_inside_orbit", "other_orbit_after_core", "other_medium_belt_spacing",
                "A tilted orbital belt surrounds a small axial core and reads as a ringed object in motion.");
        add(values, "other_marigold_strobe_crown", "other_cross_track_orbits", "other_paired_axis_origins",
                "other_perpendicular_orbit_traces", "other_twin_track_interleave", "other_track_alternating_cadence", "other_open_cross_density",
                "Two tilted orbital paths run at distinct phases and form a sparse three-dimensional pair of rings.");
        add(values, "other_cerise_lattice_double", "other_gapped_long_lances", "other_broken_origin_bead",
                "other_fixed_segment_lances", "other_spatial_segment_gaps", "other_segment_tick_cadence", "other_low_fill_spacing",
                "Long rays are broken into fixed visible segments so gaps remain readable along every direction.");
        add(values, "other_bronze_sunburst_sphere", "other_chroma_bead_chain", "other_three_bead_origin",
                "other_beaded_radial_traces", "other_successive_bead_radii", "other_per_bead_color_phase", "other_dotted_chain_density",
                "Successive beads move along each ray and change palette stage by segment, creating a dotted chain.");
        if (values.size() != BatchOtherCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("batch_other visual signatures must cover every stable id");
        }
        return Map.copyOf(values);
    }

    private static void add(
            Map<String, BatchOtherFirework.VisualDifference> values,
            String id,
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String description) {
        BatchOtherFirework.VisualDifference difference = new BatchOtherFirework.VisualDifference(
                shape,
                core,
                trail,
                layering,
                cadence,
                density,
                ALL_AXES,
                String.join("|", shape, core, trail, layering, cadence, density),
                description);
        if (values.put(id, difference) != null) {
            throw new IllegalStateException("Duplicate batch_other visual signature " + id);
        }
    }
}
