package com.urbanforma.fireworks.content.release_next.medium_extension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Twenty-four distinct, unregistered medium effects for coordinator-owned integration. */
public final class MediumExtensionCatalog {
    public static final String BATCH_ID = "release_next_medium_extension";
    public static final int REQUIRED_ENTRY_COUNT = 24;
    public static final int MAX_PARTICLES_PER_TICK = 480;
    private static final List<MediumExtensionDefinition> VALUES = List.of(
            e("amber_dahlia", MediumExtensionDefinition.Category.SPHERE, "#FFB31A", "#FF7D17", "#FFF0B7", 1728, 96, 18, 34, 72, 26.0, 4),
            e("ruby_twin_orb", MediumExtensionDefinition.Category.SPHERE, "#D8223C", "#FF6E49", "#FFD36F", 1800, 100, 18, 36, 74, 27.0, 6),
            e("teal_moon", MediumExtensionDefinition.Category.SPHERE, "#159A91", "#55CBBE", "#E8FFF6", 1600, 100, 16, 38, 76, 25.0, 5),
            e("violet_braid", MediumExtensionDefinition.Category.SPHERE, "#7C3DB8", "#B06BE3", "#F4D7FF", 1920, 120, 16, 37, 75, 28.0, 7),
            e("golden_lance", MediumExtensionDefinition.Category.RADIAL, "#F4B41C", "#D97710", "#FFF5C5", 7680, 240, 32, 42, 82, 60.0, 4),
            e("coral_wave", MediumExtensionDefinition.Category.RADIAL, "#FF684F", "#FF9F69", "#FFE1C6", 7200, 240, 30, 40, 80, 58.0, 6),
            e("aurora_arc", MediumExtensionDefinition.Category.RADIAL, "#0B9E9C", "#4ED0C4", "#E6FFFF", 8160, 240, 34, 44, 88, 62.0, 8),
            e("crimson_fracture", MediumExtensionDefinition.Category.RADIAL, "#B51F34", "#ED4D3F", "#FFD3BD", 7440, 240, 31, 43, 86, 59.0, 5),
            e("amethyst_ring", MediumExtensionDefinition.Category.RING_CORE, "#A855D8", "#7A4EBD", "#F0D8FF", 1440, 90, 16, 35, 71, 24.0, 4),
            e("champagne_hollow", MediumExtensionDefinition.Category.RING_CORE, "#D7B66B", "#F4DFA5", "#FFF8DD", 1536, 96, 16, 36, 72, 25.0, 5),
            e("cobalt_ice", MediumExtensionDefinition.Category.RING_CORE, "#285EB6", "#64A7E7", "#E8F6FF", 1600, 100, 16, 38, 78, 26.0, 6),
            e("copper_annular", MediumExtensionDefinition.Category.RING_CORE, "#C96A2B", "#E5A34A", "#FFF1CF", 1344, 84, 16, 34, 70, 23.0, 3),
            e("saffron_short_ray", MediumExtensionDefinition.Category.SHORT_WILLOW, "#FF9205", "#FFCA35", "#FFF0B5", 2880, 120, 24, 42, 88, 34.0, 4),
            e("rose_short_drop", MediumExtensionDefinition.Category.SHORT_WILLOW, "#D95573", "#F39AA9", "#FFE5CE", 3000, 125, 24, 44, 90, 36.0, 6),
            e("jade_short_palm", MediumExtensionDefinition.Category.SHORT_WILLOW, "#25A56A", "#70D89D", "#E5F7DF", 3120, 130, 24, 43, 86, 35.0, 5),
            e("plum_short_cascade", MediumExtensionDefinition.Category.SHORT_WILLOW, "#6C326F", "#B266BA", "#F1D4F7", 2760, 115, 24, 45, 92, 33.0, 7),
            e("coral_pulse", MediumExtensionDefinition.Category.PULSE, "#FF684F", "#FF9F69", "#FFE1C6", 2160, 120, 18, 32, 70, 30.0, 3),
            e("triple_aurora_pulse", MediumExtensionDefinition.Category.PULSE, "#2B92AA", "#65CBD4", "#E5FFFF", 2400, 120, 20, 35, 76, 32.0, 6),
            e("amber_strobe_pulse", MediumExtensionDefinition.Category.PULSE, "#F28B16", "#FFC14E", "#FFF0B4", 2048, 128, 16, 30, 68, 29.0, 4),
            e("ruby_delayed_pulse", MediumExtensionDefinition.Category.PULSE, "#B71936", "#F05248", "#FFD6C8", 2280, 120, 19, 34, 74, 31.0, 5),
            e("solar_crossweave", MediumExtensionDefinition.Category.INTERLEAVED_SHELL, "#F27A12", "#FFBF31", "#FFF2AC", 3840, 160, 24, 40, 84, 42.0, 6),
            e("orchid_phase_shell", MediumExtensionDefinition.Category.INTERLEAVED_SHELL, "#8D49D8", "#D28BFF", "#FFE5FF", 3600, 150, 24, 42, 88, 40.0, 8),
            e("garnet_lattice", MediumExtensionDefinition.Category.INTERLEAVED_SHELL, "#8D253B", "#CC4951", "#FFD4BB", 3960, 165, 24, 43, 86, 44.0, 5),
            e("icewheel_interleave", MediumExtensionDefinition.Category.INTERLEAVED_SHELL, "#285EB6", "#64A7E7", "#E8F6FF", 4080, 170, 24, 44, 90, 45.0, 7));

    private MediumExtensionCatalog() {}
    public static List<MediumExtensionDefinition> values() { return VALUES; }
    public static MediumExtensionDefinition byId(String id) { return VALUES.stream().filter(v -> v.id().equals(id)).findFirst().orElse(null); }
    public static boolean staticContractHolds() {
        Set<String> ids = new HashSet<>();
        Set<String> categories = new HashSet<>();
        for (MediumExtensionDefinition v : VALUES) { ids.add(v.id()); categories.add(v.normalizedCategory()); }
        return VALUES.size() == REQUIRED_ENTRY_COUNT && ids.size() == REQUIRED_ENTRY_COUNT && categories.size() == 6;
    }
    private static MediumExtensionDefinition e(String suffix, MediumExtensionDefinition.Category c, String p, String s, String a, int total, int perTick, int ticks, int min, int max, double radius, int beats) {
        return new MediumExtensionDefinition("medium_extension_" + suffix, c, p, s, a, total, perTick, ticks, min, max, radius, beats);
    }
}
