package com.urbanforma.fireworks.content.midsize.radial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Twenty-five typed, unregistered Medium radial definitions for a future medium/small radial subsection. */
public final class MidsizeRadialFireworkCatalog {
    public static final String BATCH_ID = "midsize_radial_second_batch";
    public static final String MANIFEST_RESOURCE =
            "assets/urbanforma_fireworks/midsize/radial/midsize_radial_second_batch_manifest.json";
    public static final int REQUIRED_ENTRY_COUNT = 25;
    public static final int COOL_COLOR_CAP = 10;
    public static final int COOL_COLOR_ENTRY_COUNT = 7;

    private static final List<MidsizeRadialFireworkDefinition> VALUES = List.of(
            definition("midsize_radial_sun_lance_firework", "\u65e5\u8292", "Sun Lance", MidsizeRadialTrajectory.Path.SUN_LANCE,
                    "#FFB21A", "#FF6C14", "#FFF2B8", false),
            definition("midsize_radial_ember_broadbeam_firework", "\u4f59\u70ec\u7c97\u675f", "Ember Broad Beam", MidsizeRadialTrajectory.Path.BROAD_BEAM,
                    "#E94A24", "#FF942B", "#FFE0A3", false),
            definition("midsize_radial_golden_segment_beam_firework", "\u9540\u91d1\u5206\u8282\u5149\u675f", "Golden Segment Beam", MidsizeRadialTrajectory.Path.SEGMENT_BEAM,
                    "#F4B41C", "#D97710", "#FFF5C5", false),
            definition("midsize_radial_cinnabar_staggered_ring_firework", "\u6731\u7802\u4ea4\u9519\u73af", "Cinnabar Staggered Ring", MidsizeRadialTrajectory.Path.STAGGERED_RING,
                    "#D83B29", "#FF7250", "#FFD6B2", false),
            definition("midsize_radial_orchid_phase_shell_firework", "\u5170\u7d2b\u76f8\u4f4d\u58f3", "Orchid Phase Shell", MidsizeRadialTrajectory.Path.PHASE_SHELL,
                    "#8D49D8", "#D28BFF", "#FFE5FF", true),
            definition("midsize_radial_saffron_short_ray_firework", "\u85cf\u7ea2\u77ed\u5c04\u7ebf", "Saffron Short Ray", MidsizeRadialTrajectory.Path.SHORT_RAY,
                    "#FF9205", "#FFCA35", "#FFF0B5", false),
            definition("midsize_radial_ruby_delayed_core_firework", "\u7ea2\u5b9d\u5ef6\u8fdf\u5185\u6838", "Ruby Delayed Core", MidsizeRadialTrajectory.Path.DELAYED_CORE,
                    "#B71936", "#F05248", "#FFD6C8", false),
            definition("midsize_radial_coral_pulse_firework", "\u73ca\u745a\u8109\u51b2", "Coral Pulse", MidsizeRadialTrajectory.Path.PULSE_RADIAL,
                    "#FF684F", "#FF9F69", "#FFE1C6", false),
            definition("midsize_radial_amethyst_rotary_wheel_firework", "\u7d2b\u6676\u65cb\u8f6c\u5149\u8f6e", "Amethyst Rotary Wheel", MidsizeRadialTrajectory.Path.ROTARY_WHEEL,
                    "#6E43C5", "#AD76EF", "#F2DEFF", true),
            definition("midsize_radial_copper_crown_lance_firework", "\u8d64\u94dc\u51a0\u67aa", "Copper Crown Lance", MidsizeRadialTrajectory.Path.CROWN_LANCE,
                    "#BA5A30", "#E99B45", "#FFE0AE", false),
            definition("midsize_radial_amber_split_fan_firework", "\u7425\u73c0\u88c2\u6247", "Amber Split Fan", MidsizeRadialTrajectory.Path.SPLIT_FAN,
                    "#F28B16", "#FFC14E", "#FFF0B4", false),
            definition("midsize_radial_garnet_lattice_firework", "\u77f3\u69b4\u683c\u7f51", "Garnet Lattice", MidsizeRadialTrajectory.Path.LATTICE_WEAVE,
                    "#8D253B", "#CC4951", "#FFD4BB", false),
            definition("midsize_radial_plum_helix_firework", "\u6885\u7d2b\u87ba\u65cb", "Plum Helix", MidsizeRadialTrajectory.Path.SPIRAL_HELIX,
                    "#6C326F", "#B266BA", "#F1D4F7", true),
            definition("midsize_radial_mandarin_comet_firework", "\u67d1\u6a58\u5f57\u5c3e", "Mandarin Comet", MidsizeRadialTrajectory.Path.COMET_SWEEP,
                    "#EA601E", "#FFA23A", "#FFE0AE", false),
            definition("midsize_radial_rose_twin_helix_firework", "\u73ab\u7470\u53cc\u87ba\u65cb", "Rose Twin Helix", MidsizeRadialTrajectory.Path.TWIN_HELIX,
                    "#D95573", "#F39AA9", "#FFE5CE", false),
            definition("midsize_radial_vermilion_ring_bloom_firework", "\u6731\u7ea2\u73af\u7efd", "Vermilion Ring Bloom", MidsizeRadialTrajectory.Path.RING_BLOOM,
                    "#D83622", "#FF7E35", "#FFE1A3", false),
            definition("midsize_radial_violet_pinwheel_firework", "\u7d2b\u7f57\u98ce\u8f66", "Violet Pinwheel", MidsizeRadialTrajectory.Path.PHASED_PINWHEEL,
                    "#6B44B8", "#AB7BE5", "#F0E0FF", true),
            definition("midsize_radial_champagne_hollow_firework", "\u9999\u69df\u7a7a\u5fc3", "Champagne Hollow Core", MidsizeRadialTrajectory.Path.HOLLOW_CORE,
                    "#D7B66B", "#F4DFA5", "#FFF8DD", false),
            definition("midsize_radial_crimson_fracture_firework", "\u7eef\u7ea2\u88c2\u5c04", "Crimson Fracture Ray", MidsizeRadialTrajectory.Path.FRACTURE_RAY,
                    "#B51F34", "#ED4D3F", "#FFD3BD", false),
            definition("midsize_radial_topaz_diamond_firework", "\u9ec4\u7389\u83f1\u661f", "Topaz Diamond Star", MidsizeRadialTrajectory.Path.DIAMOND_STAR,
                    "#D98A12", "#F6C63E", "#FFF0B1", false),
            definition("midsize_radial_solar_crossweave_firework", "\u65e5\u66dc\u4ea4\u7ec7", "Solar Crossweave", MidsizeRadialTrajectory.Path.CROSSWEAVE,
                    "#F27A12", "#FFBF31", "#FFF2AC", false),
            definition("midsize_radial_persimmon_wave_firework", "\u67ff\u8272\u6ce2\u6f9c", "Persimmon Wave", MidsizeRadialTrajectory.Path.WAVE_RADIANT,
                    "#E55222", "#FF9A45", "#FFE1B9", false),
            definition("midsize_radial_aurora_triple_pulse_firework", "\u6781\u5149\u4e09\u8109\u51b2", "Aurora Triple Pulse", MidsizeRadialTrajectory.Path.TRIPLE_PULSE,
                    "#2B92AA", "#65CBD4", "#E5FFFF", true),
            definition("midsize_radial_teal_aurora_firework", "\u9752\u7fe0\u6781\u5149\u5f27", "Teal Aurora Arc", MidsizeRadialTrajectory.Path.AURORA_ARC,
                    "#0B9E9C", "#4ED0C4", "#E6FFFF", true),
            definition("midsize_radial_cobalt_icewheel_firework", "\u94b4\u84dd\u51b0\u8f6e", "Cobalt Ice Wheel", MidsizeRadialTrajectory.Path.ICE_WHEEL,
                    "#285EB6", "#64A7E7", "#E8F6FF", true));
    private static final Map<String, MidsizeRadialFireworkDefinition> BY_ID = indexById();

    static {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || MidsizeRadialVisualSignatures.count() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Medium radial catalog must contain exactly twenty-five entries");
        }
        int coolCount = 0;
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<String> structuralSignatures = new HashSet<>();
        for (MidsizeRadialFireworkDefinition definition : VALUES) {
            if (!ids.add(definition.id()) || !names.add(definition.enName())
                    || !structuralSignatures.add(definition.visualSignature().structuralSignature())
                    || definition.particlePlan().localPeakParticlesPerTick()
                            > MidsizeRadialTrajectory.LOCAL_MAX_PARTICLES_PER_TICK
                    || definition.particlePlan().maxOwnedParticles() != definition.particlePlan().totalParticles()
                    || !MidsizeRadialTrajectory.fitsEnvelope(definition.path())) {
                throw new IllegalStateException("Medium radial catalog uniqueness or local contract drifted");
            }
            if (definition.countsTowardCoolColorBudget()) {
                coolCount++;
            }
        }
        if (coolCount != COOL_COLOR_ENTRY_COUNT || coolCount > COOL_COLOR_CAP) {
            throw new IllegalStateException("Medium radial cool-color count is outside the approved cap");
        }
        MidsizeRadialVisualSignatures.validateAdjacent(VALUES);
    }

    private MidsizeRadialFireworkCatalog() {
    }

    public static List<MidsizeRadialFireworkDefinition> values() {
        return VALUES;
    }

    public static MidsizeRadialFireworkDefinition byId(String id) {
        return BY_ID.get(id);
    }

    private static MidsizeRadialFireworkDefinition definition(
            String id,
            String zhDescriptor,
            String enDescriptor,
            MidsizeRadialTrajectory.Path path,
            String primary,
            String secondary,
            String accent,
            boolean cool) {
        return new MidsizeRadialFireworkDefinition(
                id,
                "\u4e2d\u578b" + zhDescriptor + "\u653e\u5c04\u70df\u82b1",
                "Medium " + enDescriptor + " Radial Firework",
                path,
                palette(primary, secondary, accent),
                new MidsizeRadialFireworkDefinition.RecipeContract(
                        List.of(" P ", "FGF", " P "),
                        Map.of(
                                "P", "minecraft:paper",
                                "F", "minecraft:firework_star",
                                "G", "minecraft:gunpowder"),
                        MidsizeRadialFireworkDefinition.MOD_ID + ":" + id,
                        1,
                        false,
                        "data/urbanforma_fireworks/recipes/" + id + ".json"),
                new MidsizeRadialFireworkDefinition.CreativeTarget(
                        MidsizeRadialFireworkDefinition.FUTURE_CATEGORY,
                        MidsizeRadialFireworkDefinition.FUTURE_SUBSECTION,
                        MidsizeRadialFireworkDefinition.ORDER_GROUP,
                        true),
                new MidsizeRadialFireworkDefinition.ReuseContract(
                        MidsizeRadialFireworkDefinition.ITEM_MODEL,
                        MidsizeRadialFireworkDefinition.HD_FIREWORK_SPARK,
                        "com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory",
                        "com.urbanforma.fireworks.client.midsize.radial.MidsizeRadialClientProgram",
                        false,
                        false,
                        false,
                        false),
                new MidsizeRadialFireworkDefinition.ParticlePlan(
                        path,
                        path.totalParticles(),
                        path.maxParticlesPerTick(),
                        path.totalParticles(),
                        path.minimumLifetimeTicks(),
                        path.maximumLifetimeTicks(),
                        path.totalVisualTicks(),
                        true,
                        false,
                        false,
                        false,
                        false,
                        false),
                new MidsizeRadialFireworkDefinition.Boundary(
                        path.pathId(),
                        MidsizeRadialTrajectory.REFERENCE_EFFECT_ID,
                        MidsizeRadialTrajectory.REFERENCE_TOTAL_PARTICLES,
                        (double) path.totalParticles() / MidsizeRadialTrajectory.REFERENCE_TOTAL_PARTICLES,
                        MidsizeRadialTrajectory.REFERENCE_FULL_ENVELOPE,
                        MidsizeRadialTrajectory.APPROVED_FULL_ENVELOPE,
                        path.maximumRadius(),
                        MidsizeRadialTrajectory.REFERENCE_ASCENT_TICKS,
                        MidsizeRadialTrajectory.ASCENT_TICKS,
                        MidsizeRadialTrajectory.REFERENCE_DETONATION_HEIGHT,
                        MidsizeRadialTrajectory.DETONATION_HEIGHT,
                        MidsizeRadialTrajectory.HEIGHT_RATIO,
                        "MidsizeRadialTrajectory.staticContractHolds(" + path.name() + ", seed)"),
                MidsizeRadialVisualSignatures.forId(id),
                cool);
    }

    private static MidsizeRadialFireworkDefinition.Palette palette(String primary, String secondary, String accent) {
        return new MidsizeRadialFireworkDefinition.Palette(
                MidsizeRadialFireworkDefinition.Rgb.fromHex(primary),
                MidsizeRadialFireworkDefinition.Rgb.fromHex(secondary),
                MidsizeRadialFireworkDefinition.Rgb.fromHex(accent));
    }

    private static Map<String, MidsizeRadialFireworkDefinition> indexById() {
        Map<String, MidsizeRadialFireworkDefinition> valuesById = new HashMap<>();
        for (MidsizeRadialFireworkDefinition definition : VALUES) {
            if (valuesById.put(definition.id(), definition) != null) {
                throw new IllegalStateException("Duplicate medium radial id " + definition.id());
            }
        }
        return Map.copyOf(valuesById);
    }
}
