package com.urbanforma.fireworks.content.midsize.sphere;

import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Boundary;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Cadence;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.CoreForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Density;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.LayerForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Palette;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.PaletteFamily;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.ParticleBudget;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.Rgb;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.SphereForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.TrailForm;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.VisualAxis;
import com.urbanforma.fireworks.content.midsize.sphere.MediumSphereDefinition.VisualSignature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Twenty-five unregistered medium spherical definitions awaiting coordinator-owned integration. */
public final class MediumSphereCatalog {
    public static final String BATCH_ID = "medium_sphere_batch_01";
    public static final String MANIFEST_RESOURCE =
            "assets/urbanforma_fireworks/midsize/sphere/medium_sphere_manifest.json";
    public static final String INTEGRATION_STATUS = "unregistered";
    public static final int REQUIRED_ENTRY_COUNT = 25;
    public static final int MAX_COOL_COLOR_COUNT = 10;

    private static final List<MediumSphereDefinition> VALUES = List.of(
            entry(
                    "medium_amber_dahlia_sphere_firework",
                    "中型琥珀大丽花球形烟花",
                    "Medium Amber Dahlia Sphere Firework",
                    palette("#FFB31A", "#FF7D17", "#FFF0B7", PaletteFamily.WARM, 0),
                    SphereForm.DAHLIA, CoreForm.AMBER_BEADS, TrailForm.PEARL_TIPS, LayerForm.BEAD_TO_CROWN,
                    Cadence.CORE_OUTWARD, Density.DENSE, 108, 17, 34, 72, 26.25D, 73),
            entry(
                    "medium_copper_hollow_sphere_firework",
                    "中型铜辉空心球形烟花",
                    "Medium Copper Hollow Sphere Firework",
                    palette("#C96A2B", "#E5A34A", "#FFF1CF", PaletteFamily.METALLIC, 0),
                    SphereForm.HOLLOW_CHRYSANTHEMUM, CoreForm.HOLLOW_VOID, TrailForm.BRONZE_STREAMERS,
                    LayerForm.HOLLOW_SHELL, Cadence.OUTSIDE_IN, Density.FULL, 120, 16, 36, 70, 25.50D, 72),
            entry(
                    "medium_ruby_double_sphere_firework",
                    "中型红宝石双球形烟花",
                    "Medium Ruby Double Sphere Firework",
                    palette("#D8223C", "#FF6E49", "#FFD36F", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.TWIN_ORB, CoreForm.DUAL_NUCLEI, TrailForm.CRACKLE_EDGES, LayerForm.TWIN_LOBES,
                    Cadence.TWIN_ALTERNATE, Density.RICH, 128, 15, 36, 74, 26.00D, 74),
            entry(
                    "medium_amethyst_ring_core_sphere_firework",
                    "中型紫晶环核球形烟花",
                    "Medium Amethyst Ring-Core Sphere Firework",
                    palette("#A855D8", "#7A4EBD", "#F0D8FF", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.RING_CORE, CoreForm.ORBITAL_RING, TrailForm.VIOLET_COMETS, LayerForm.RING_TO_SHELL,
                    Cadence.RING_PULSE, Density.DENSE, 112, 17, 35, 71, 25.25D, 71),
            entry(
                    "medium_champagne_pulse_shell_sphere_firework",
                    "中型香槟脉冲壳球形烟花",
                    "Medium Champagne Pulse Shell Sphere Firework",
                    palette("#E7C78C", "#FFF3CE", "#FFFFFF", PaletteFamily.METALLIC, 0),
                    SphereForm.PULSE_SHELL, CoreForm.PEARL_SEED, TrailForm.CHAMPAGNE_TWINKLE, LayerForm.PULSE_LAYERS,
                    Cadence.THREE_BEAT, Density.COMPACT, 96, 19, 38, 76, 24.75D, 70),
            entry(
                    "medium_cinnabar_crystal_sphere_firework",
                    "中型朱砂碎晶球形烟花",
                    "Medium Cinnabar Crystal Sphere Firework",
                    palette("#D73626", "#F2793E", "#FFD670", PaletteFamily.WARM, 0),
                    SphereForm.CRYSTAL, CoreForm.DIAMOND_HEART, TrailForm.GOLD_NEEDLES, LayerForm.FACET_BANDS,
                    Cadence.FACET_STAGGER, Density.RICH, 120, 15, 34, 70, 26.25D, 75),
            entry(
                    "medium_rose_lace_sphere_firework",
                    "中型玫瑰蕾丝球形烟花",
                    "Medium Rose Lace Sphere Firework",
                    palette("#E95C87", "#FF9DB6", "#FFE1EC", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.LACE, CoreForm.ROSE_KNOT, TrailForm.LACE_GLINTS, LayerForm.WOVEN_SHELL,
                    Cadence.SPIRAL_WAVE, Density.DENSE, 108, 18, 37, 75, 25.50D, 73),
            entry(
                    "medium_saffron_crown_sphere_firework",
                    "中型藏红冠球形烟花",
                    "Medium Saffron Crown Sphere Firework",
                    palette("#F6A30A", "#FFCB49", "#FFF2B0", PaletteFamily.WARM, 0),
                    SphereForm.CROWN, CoreForm.SUN_KERNEL, TrailForm.CROWN_SPARKS, LayerForm.RISING_CROWN,
                    Cadence.CROWN_RELEASE, Density.BRIGHT, 132, 14, 34, 68, 25.75D, 74),
            entry(
                    "medium_garnet_orbit_sphere_firework",
                    "中型石榴石轨核球形烟花",
                    "Medium Garnet Orbit Sphere Firework",
                    palette("#8D1936", "#D43A4E", "#FFB344", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.ORBIT, CoreForm.EMBER_ORBIT, TrailForm.GARNET_TRACERS, LayerForm.ORBITAL_NEST,
                    Cadence.ORBITAL_PULSE, Density.FULL, 112, 18, 39, 78, 26.00D, 72),
            entry(
                    "medium_vermilion_split_sphere_firework",
                    "中型朱红分裂球形烟花",
                    "Medium Vermilion Split Sphere Firework",
                    palette("#E63228", "#FF7240", "#FFF0CA", PaletteFamily.WARM, 0),
                    SphereForm.SPLIT, CoreForm.SPLIT_HEART, TrailForm.PEARL_SPLITS, LayerForm.SPLIT_LOBES,
                    Cadence.SPLIT_BEAT, Density.RICH, 128, 14, 34, 69, 25.00D, 71),
            entry(
                    "medium_emerald_layered_sphere_firework",
                    "中型翡翠分层球形烟花",
                    "Medium Emerald Layered Sphere Firework",
                    palette("#25A56A", "#70D89D", "#E5F7DF", PaletteFamily.COOL, 1),
                    SphereForm.LAYERED, CoreForm.JADE_KERNEL, TrailForm.SILVER_TIPS, LayerForm.TRIPLE_SHELL,
                    Cadence.LAYER_SWEEP, Density.FULL, 120, 16, 38, 76, 26.25D, 75),
            entry(
                    "medium_sunset_petal_sphere_firework",
                    "中型落日花瓣球形烟花",
                    "Medium Sunset Petal Sphere Firework",
                    palette("#F26D2D", "#FF9F5C", "#FFE0A3", PaletteFamily.WARM, 0),
                    SphereForm.PETAL, CoreForm.PETAL_HEART, TrailForm.PEACH_PETALS, LayerForm.PETAL_BANDS,
                    Cadence.PETAL_STAGGER, Density.BRIGHT, 144, 13, 34, 67, 25.50D, 73),
            entry(
                    "medium_platinum_mirror_sphere_firework",
                    "中型铂金镜映球形烟花",
                    "Medium Platinum Mirror Sphere Firework",
                    palette("#D5D9E0", "#FFFFFF", "#EBCF8B", PaletteFamily.METALLIC, 0),
                    SphereForm.MIRROR, CoreForm.MIRROR_PEARL, TrailForm.MIRROR_PINS, LayerForm.MIRRORED_SHELL,
                    Cadence.MIRROR_SWAP, Density.DENSE, 108, 17, 37, 73, 26.00D, 74),
            entry(
                    "medium_crimson_strobe_shell_sphere_firework",
                    "中型深红闪爆壳球形烟花",
                    "Medium Crimson Strobe Shell Sphere Firework",
                    palette("#B91F3A", "#F04D59", "#FFE3AF", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.STROBE, CoreForm.STROBE_HEART, TrailForm.STROBE_FLASHES, LayerForm.FLASH_SHELL,
                    Cadence.STROBE_BEAT, Density.RICH, 120, 15, 36, 72, 25.25D, 72),
            entry(
                    "medium_topaz_comet_sphere_firework",
                    "中型黄玉彗尾球形烟花",
                    "Medium Topaz Comet Sphere Firework",
                    palette("#E99812", "#FFBA3C", "#FFF0A4", PaletteFamily.WARM, 0),
                    SphereForm.COMET, CoreForm.GOLD_KERNEL, TrailForm.COMET_TAILS, LayerForm.COMET_ARCS,
                    Cadence.COMET_CASCADE, Density.FULL, 128, 15, 40, 80, 26.25D, 75),
            entry(
                    "medium_violet_braid_sphere_firework",
                    "中型紫罗兰编织球形烟花",
                    "Medium Violet Braid Sphere Firework",
                    palette("#7C3DB8", "#B06BE3", "#F4D7FF", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.BRAID, CoreForm.ORCHID_KNOT, TrailForm.LILAC_WEAVE, LayerForm.BRAIDED_SHELL,
                    Cadence.TORSION_WAVE, Density.BRIGHT, 132, 14, 37, 75, 25.50D, 73),
            entry(
                    "medium_coral_annular_sphere_firework",
                    "中型珊瑚环幕球形烟花",
                    "Medium Coral Annular Sphere Firework",
                    palette("#F06958", "#FF9A7D", "#FFF0D3", PaletteFamily.WARM, 0),
                    SphereForm.ANNULAR, CoreForm.CORAL_RING, TrailForm.IVORY_HALO, LayerForm.ANNULAR_BODY,
                    Cadence.ANNULAR_SWEEP, Density.DENSE, 112, 17, 35, 71, 25.00D, 72),
            entry(
                    "medium_obsidian_aurora_sphere_firework",
                    "中型曜石极光球形烟花",
                    "Medium Obsidian Aurora Sphere Firework",
                    palette("#33284A", "#A64BC5", "#F2A8FF", PaletteFamily.RED_PURPLE, 1),
                    SphereForm.AURORA, CoreForm.AURORA_PIN, TrailForm.MAGENTA_VEIL, LayerForm.VEIL_SHELL,
                    Cadence.AURORA_SHIMMER, Density.DENSE, 108, 18, 40, 78, 26.00D, 74),
            entry(
                    "medium_azure_ice_sphere_firework",
                    "中型湛蓝冰晶球形烟花",
                    "Medium Azure Ice Sphere Firework",
                    palette("#31A9E0", "#73D8F2", "#DDF8FF", PaletteFamily.COOL, 3),
                    SphereForm.ICE, CoreForm.ICE_BEADS, TrailForm.AQUA_FRINGE, LayerForm.ICE_FACETS,
                    Cadence.FROST_PULSE, Density.FULL, 120, 16, 38, 77, 25.75D, 73),
            entry(
                    "medium_teal_moon_sphere_firework",
                    "中型青碧月环球形烟花",
                    "Medium Teal Moon Sphere Firework",
                    palette("#159A91", "#55CBBE", "#E8FFF6", PaletteFamily.COOL, 2),
                    SphereForm.MOON, CoreForm.MOON_SEED, TrailForm.MOON_HALO, LayerForm.MOON_BANDS,
                    Cadence.MOON_ORBIT, Density.COMPACT, 96, 19, 39, 79, 25.00D, 71),
            entry(
                    "medium_scarlet_lantern_sphere_firework",
                    "中型猩红灯笼球形烟花",
                    "Medium Scarlet Lantern Sphere Firework",
                    palette("#D9282D", "#F26A42", "#FFCF58", PaletteFamily.WARM, 0),
                    SphereForm.LANTERN, CoreForm.LANTERN_HEART, TrailForm.GILDED_RIBS, LayerForm.RIBBED_SHELL,
                    Cadence.LANTERN_BEAT, Density.BRIGHT, 144, 13, 35, 68, 26.25D, 74),
            entry(
                    "medium_orchid_peony_sphere_firework",
                    "中型兰花牡丹球形烟花",
                    "Medium Orchid Peony Sphere Firework",
                    palette("#B04FC4", "#E595DE", "#FFF0CB", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.PEONY, CoreForm.PEONY_HEART, TrailForm.CHAMPAGNE_BLOOM, LayerForm.BLOOM_LAYERS,
                    Cadence.BLOOM_WAVE, Density.RICH, 128, 14, 37, 74, 25.50D, 72),
            entry(
                    "medium_bronze_mosaic_sphere_firework",
                    "中型青铜马赛克球形烟花",
                    "Medium Bronze Mosaic Sphere Firework",
                    palette("#8E5A31", "#C98B4C", "#F6D494", PaletteFamily.METALLIC, 0),
                    SphereForm.MOSAIC, CoreForm.MOSAIC_CORE, TrailForm.COPPER_FLECKS, LayerForm.MOSAIC_TILES,
                    Cadence.TILE_WAVE, Density.RICH, 120, 15, 36, 72, 25.25D, 73),
            entry(
                    "medium_magenta_cascade_sphere_firework",
                    "中型洋红瀑落球形烟花",
                    "Medium Magenta Cascade Sphere Firework",
                    palette("#B72A82", "#EC69AD", "#FFE0EE", PaletteFamily.RED_PURPLE, 0),
                    SphereForm.CASCADE, CoreForm.CASCADE_PEARL, TrailForm.ROSE_CASCADE, LayerForm.CASCADE_SHELL,
                    Cadence.FALLING_CASCADE, Density.BRIGHT, 132, 14, 41, 80, 26.00D, 74),
            entry(
                    "medium_silver_solar_sphere_firework",
                    "中型银耀日冕球形烟花",
                    "Medium Silver Solar Sphere Firework",
                    palette("#E4E9EE", "#FFFFFF", "#FFD45D", PaletteFamily.METALLIC, 0),
                    SphereForm.SOLAR, CoreForm.SOLAR_CORE, TrailForm.SOLAR_TIPS, LayerForm.SOLAR_CORONA,
                    Cadence.SOLAR_RADIATE, Density.FULL, 112, 18, 38, 78, 26.25D, 75));

    private static final Map<String, MediumSphereDefinition> BY_ID = indexById();

    static {
        validate();
    }

    private MediumSphereCatalog() {
    }

    public static List<MediumSphereDefinition> values() {
        return VALUES;
    }

    public static MediumSphereDefinition require(String id) {
        MediumSphereDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown medium sphere firework " + id);
        }
        return definition;
    }

    public static int coolColorCount() {
        return VALUES.stream().mapToInt(definition -> definition.palette().coolColorCount()).sum();
    }

    public static int maximumPeakParticlesPerTick() {
        return VALUES.stream().mapToInt(definition -> definition.particleBudget().particlesPerTick()).max().orElse(0);
    }

    public static int maximumOwnedParticles() {
        return VALUES.stream().mapToInt(definition -> definition.particleBudget().maxOwnedParticles()).max().orElse(0);
    }

    private static MediumSphereDefinition entry(
            String id,
            String zhName,
            String enName,
            Palette palette,
            SphereForm shape,
            CoreForm core,
            TrailForm trail,
            LayerForm layering,
            Cadence cadence,
            Density density,
            int branches,
            int emissionTicks,
            int minLifetimeTicks,
            int maxLifetimeTicks,
            double maxRadius,
            int ascentTicks) {
        return new MediumSphereDefinition(
                id,
                zhName,
                enName,
                palette,
                new VisualSignature(shape, core, trail, layering, cadence, density),
                new ParticleBudget(branches, emissionTicks, minLifetimeTicks, maxLifetimeTicks),
                new Boundary(maxRadius, ascentTicks),
                MediumSphereDefinition.hdSparkContract(),
                MediumSphereDefinition.vanillaRocketModel(),
                MediumSphereDefinition.standardRecipe(id),
                MediumSphereDefinition.coordinatorOwnedSphereTarget(),
                MediumSphereDefinition.clientOnlyEffect());
    }

    private static Palette palette(
            String primary, String secondary, String accent, PaletteFamily family, int coolColorCount) {
        return new Palette(Rgb.hex(primary), Rgb.hex(secondary), Rgb.hex(accent), family, coolColorCount);
    }

    private static Map<String, MediumSphereDefinition> indexById() {
        Map<String, MediumSphereDefinition> valuesById = new HashMap<>();
        for (MediumSphereDefinition definition : VALUES) {
            if (valuesById.put(definition.id(), definition) != null) {
                throw new IllegalStateException("Duplicate medium sphere id " + definition.id());
            }
        }
        return Map.copyOf(valuesById);
    }

    private static void validate() {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || coolColorCount() > MAX_COOL_COLOR_COUNT
                || maximumPeakParticlesPerTick() > MediumSphereDefinition.LOCAL_PEAK_PARTICLE_CAP
                || maximumOwnedParticles() > MediumSphereDefinition.LOCAL_OWNED_PARTICLE_CAP) {
            throw new IllegalStateException("Medium sphere batch size or local budget contract drifted");
        }

        Set<String> zhNames = new HashSet<>();
        Set<String> enNames = new HashSet<>();
        Set<String> structuralSignatures = new HashSet<>();
        Set<String> paletteSignatures = new HashSet<>();
        Map<PaletteFamily, Integer> paletteFamilies = new HashMap<>();
        String previousAdjacentSignature = null;
        for (MediumSphereDefinition definition : VALUES) {
            if (!zhNames.add(definition.zhName()) || !enNames.add(definition.enName())
                    || !structuralSignatures.add(definition.visualSignature().structuralSignature())
                    || !paletteSignatures.add(definition.palette().paletteSignature())
                    || definition.zhName().contains("中" + "小型") || !definition.zhName().startsWith("中型")
                    || !definition.enName().startsWith("Medium ")) {
                throw new IllegalStateException("Medium sphere name, palette, or structural signature is not unique");
            }
            for (VisualAxis axis : VisualAxis.values()) {
                if (definition.visualSignature().value(axis).isBlank()) {
                    throw new IllegalStateException("Missing medium sphere visual axis " + axis);
                }
            }
            String adjacentSignature = definition.visualSignature().structuralSignature()
                    + "|" + definition.palette().paletteSignature();
            if (adjacentSignature.equals(previousAdjacentSignature)) {
                throw new IllegalStateException("Adjacent medium sphere definitions repeat structure and palette");
            }
            previousAdjacentSignature = adjacentSignature;
            paletteFamilies.merge(definition.palette().family(), 1, Integer::sum);
        }
        if (paletteFamilies.getOrDefault(PaletteFamily.WARM, 0) < 7
                || paletteFamilies.getOrDefault(PaletteFamily.METALLIC, 0) < 4
                || paletteFamilies.getOrDefault(PaletteFamily.RED_PURPLE, 0) < 5
                || paletteFamilies.getOrDefault(PaletteFamily.COOL, 0) < 2) {
            throw new IllegalStateException("Medium sphere palette coverage drifted");
        }
    }
}
