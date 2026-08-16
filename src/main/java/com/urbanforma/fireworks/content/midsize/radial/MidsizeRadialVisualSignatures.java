package com.urbanforma.fireworks.content.midsize.radial;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** Six-axis structural ledger for the second Medium radial batch. */
public final class MidsizeRadialVisualSignatures {
    private static final Map<String, MidsizeRadialFireworkDefinition.VisualSignature> VALUES = Map.ofEntries(
            entry("midsize_radial_sun_lance_firework", "straight lance star", "tight pin core", "clean pearl lances", "pin then spoke then crown", "early pin and late crown", "three layers, 10,176 nodes", "sun amber", "Unbroken lances create a direct radial read."),
            entry("midsize_radial_ember_broadbeam_firework", "thick radiant bloom", "ember bead center", "broad warm terminals", "wide body with compressed edge", "short core under a long body", "three layers, 10,112 nodes", "ember copper", "Side-thickened beams replace needle-like spokes."),
            entry("midsize_radial_golden_segment_beam_firework", "segmented beam shell", "golden bead core", "gapped pearl terminals", "seven-step beam body", "late terminal run", "three layers, 10,176 nodes", "gold amber", "Quantized radial travel makes luminous segments rather than continuous rays."),
            entry("midsize_radial_cinnabar_staggered_ring_firework", "staggered ring radial", "offset ring core", "round ring-edge sparks", "three vertically offset rings", "body first, rings overlap later", "three layers, 9,968 nodes", "cinnabar coral", "Alternating ring elevations break the normal spherical outline."),
            entry("midsize_radial_orchid_phase_shell_firework", "phase-separated shell", "violet inner phase", "pale orchid edge", "three azimuth phase shells", "inner, middle, outer phase delays", "three layers, 10,032 nodes", "orchid pearl", "Layer phase shifts turn the shell into an inside-out rotation."),
            entry("midsize_radial_saffron_short_ray_firework", "short-ray dense shell", "saffron compact core", "clipped gold tips", "dense close body and clipped edge", "fast compact release", "three layers, 9,968 nodes", "saffron gold", "The rays end early and leave a dense compact silhouette."),
            entry("midsize_radial_ruby_delayed_core_firework", "opening radial with delayed core", "late ruby nucleus", "open pearl terminals", "outer body opens before a core response", "core starts after the opening ring", "three layers, 10,048 nodes", "ruby ember", "A delayed center beat reverses the normal core-first chronology."),
            entry("midsize_radial_coral_pulse_firework", "pulsed radial bloom", "coral pulse core", "breathing ivory tips", "three concentric pulse bands", "three radial expansions", "three layers, 9,920 nodes", "coral peach", "Radial distance repeatedly compresses and releases across the burst."),
            entry("midsize_radial_amethyst_rotary_wheel_firework", "rotary wheel radial", "amethyst hub", "curved lavender spokes", "rotating hub, body, and wheel rim", "wheel turn accelerates through the body", "three layers, 9,952 nodes", "amethyst lilac", "Continuous azimuth rotation produces a wheel instead of a fixed star."),
            entry("midsize_radial_copper_crown_lance_firework", "crowned lance bloom", "copper crown seed", "upward pearl lances", "high crown over a lower radial body", "raised outer lances release last", "three layers, 9,904 nodes", "copper champagne", "Vertical lift builds a crown rather than a uniform radial edge."),
            entry("midsize_radial_amber_split_fan_firework", "split fan radial", "amber fan core", "flattened warm tips", "two shallow fan planes", "near-simultaneous fan release", "three layers, 10,000 nodes", "amber honey", "Flattened vertical spread creates opposing fan wings."),
            entry("midsize_radial_garnet_lattice_firework", "lattice radiant shell", "garnet lattice center", "crossing red-gold filaments", "woven body under a small crown", "interleaved weave cadence", "three layers, 10,080 nodes", "garnet rose", "A second lateral frequency crosses each beam into a lattice."),
            entry("midsize_radial_plum_helix_firework", "spiral helix bloom", "plum spiral core", "wound violet tips", "single helix body with a trailing rim", "spiral completes during middle phase", "three layers, 9,856 nodes", "plum orchid", "The whole radial field twists along one continuous helix."),
            entry("midsize_radial_mandarin_comet_firework", "swept comet radial", "mandarin comet head", "drawn-back orange tails", "forward-lifted body and tail", "tail follows the body sweep", "three layers, 9,984 nodes", "mandarin pearl", "A directional azimuth sweep and lift bias the burst like a comet."),
            entry("midsize_radial_rose_twin_helix_firework", "twin-helix radial", "rose double seed", "paired blush strands", "alternating left-right helix strands", "two helixes meet at the rim", "three layers, 9,856 nodes", "rose champagne", "Parity-driven strands form two distinct counter-running helices."),
            entry("midsize_radial_vermilion_ring_bloom_firework", "ring-bloom radial", "vermilion flower core", "rounded amber petals", "pulsed rings expand into a bloom", "ring body then blooming edge", "three layers, 9,856 nodes", "vermilion gold", "Ring modulation produces petal-like circular growth."),
            entry("midsize_radial_violet_pinwheel_firework", "phased pinwheel", "violet hub", "five curved pale blades", "five phase-offset radial blades", "blade offsets arrive in a turn", "three layers, 10,032 nodes", "violet pearl", "Branch groups reserve five visible pinwheel phases."),
            entry("midsize_radial_champagne_hollow_firework", "hollow-core radial", "champagne negative center", "bright cream perimeter", "open center, dense shell, wide rim", "body opens before inner ring", "three layers, 9,936 nodes", "champagne ivory", "A deliberate inner void keeps the center dark."),
            entry("midsize_radial_crimson_fracture_firework", "fractured ray burst", "crimson shard core", "split ember fragments", "alternating full and shortened rays", "fracture tips enter after body", "three layers, 9,856 nodes", "crimson coral", "Alternating ray lengths visibly split the bloom into shards."),
            entry("midsize_radial_topaz_diamond_firework", "diamond star radial", "topaz diamond core", "faceted warm points", "four-facet radial distortion", "facets sharpen through the rim", "three layers, 9,952 nodes", "topaz gold", "Azimuth-dependent radius produces a faceted diamond outline."),
            entry("midsize_radial_solar_crossweave_firework", "solar crossweave", "solar knot core", "crossed yellow filaments", "two oscillating weave axes", "rapid crossed middle cadence", "three layers, 10,080 nodes", "solar white", "Lateral and vertical waves cross into a woven solar mesh."),
            entry("midsize_radial_persimmon_wave_firework", "wave radial", "persimmon wave core", "rolling amber tips", "vertical wave body and edge", "two rolling crests", "three layers, 10,016 nodes", "persimmon cream", "Tall sinusoidal motion makes a rolling radial perimeter."),
            entry("midsize_radial_aurora_triple_pulse_firework", "triple-pulse aurora", "aqua pulse core", "cool cyan flashes", "three timed breathing shells", "three quick pulse peaks", "three layers, 9,968 nodes", "aurora cyan", "Three radial pulses and cool flashes create a staccato aurora rhythm."),
            entry("midsize_radial_teal_aurora_firework", "aurora arc radial", "teal aurora seed", "high mint arcs", "undulating curtain arcs over a radial body", "long arcing final phase", "three layers, 9,920 nodes", "teal ice", "Two coupled vertical waves bend the shell into aurora-like arcs."),
            entry("midsize_radial_cobalt_icewheel_firework", "ice-wheel radial", "cobalt ice hub", "blue-white rotating teeth", "fast wheel body with frozen outer teeth", "wheel turns into a late rim", "three layers, 9,984 nodes", "cobalt frost", "A high-turn wheel and icy outer teeth form a rotating snowflake read."));

    private MidsizeRadialVisualSignatures() {
    }

    public static MidsizeRadialFireworkDefinition.VisualSignature forId(String id) {
        MidsizeRadialFireworkDefinition.VisualSignature signature = VALUES.get(id);
        if (signature == null) {
            throw new IllegalArgumentException("Missing medium radial visual signature for " + id);
        }
        return signature;
    }

    public static int count() {
        return VALUES.size();
    }

    public static void validateAdjacent(List<MidsizeRadialFireworkDefinition> entries) {
        if (entries == null || entries.size() != MidsizeRadialFireworkCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalArgumentException("Medium radial adjacency check requires the complete batch");
        }
        for (int index = 1; index < entries.size(); index++) {
            MidsizeRadialFireworkDefinition.VisualSignature previous = entries.get(index - 1).visualSignature();
            MidsizeRadialFireworkDefinition.VisualSignature current = entries.get(index).visualSignature();
            if (previous.structuralSignature().equals(current.structuralSignature())
                    && previous.paletteToken().equals(current.paletteToken())) {
                throw new IllegalStateException("Adjacent medium radial entries repeat their combined signature");
            }
        }
    }

    private static Map.Entry<String, MidsizeRadialFireworkDefinition.VisualSignature> entry(
            String id,
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String paletteToken,
            String visualDifference) {
        return Map.entry(id, new MidsizeRadialFireworkDefinition.VisualSignature(
                shape,
                core,
                trail,
                layering,
                cadence,
                density,
                paletteToken,
                visualDifference,
                EnumSet.allOf(MidsizeRadialFireworkDefinition.VisualAxis.class)));
    }
}
