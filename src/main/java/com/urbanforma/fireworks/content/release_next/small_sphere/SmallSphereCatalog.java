package com.urbanforma.fireworks.content.release_next.small_sphere;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.urbanforma.fireworks.content.release_next.small_sphere.SmallSphereDefinition.ShapeVariant.*;
import static com.urbanforma.fireworks.content.release_next.small_sphere.SmallSphereDefinition.StructuralFeature.*;

/** Fifteen unregistered, vanilla-size sphere contracts for coordinator integration. */
public final class SmallSphereCatalog {
    public static final String CATEGORY = "release_next_small_sphere";
    public static final int REQUIRED_ENTRY_COUNT = 15;
    public static final double LAUNCH_SPEED = 1.30D;
    public static final int FLIGHT_TICKS = 24;
    public static final double DETONATION_HEIGHT = LAUNCH_SPEED * FLIGHT_TICKS;
    public static final int LOCAL_TICK_BUDGET = 64;
    public static final int LOCAL_OWNED_PARTICLE_BUDGET = 384;
    private static final SmallSphereDefinition.ClientPlan PLAN = new SmallSphereDefinition.ClientPlan(48, 8, 48, 28, 6.4D);

    private static final List<SmallSphereDefinition> VALUES = List.of(
            entry("azure_pearl", "蔚蓝珍珠球形烟花", "Azure Pearl Sphere", "#168DFF", "#78D8FF", "#F3FEFF", PEARL_KERNEL, INNER_CORE, PETAL_OFFSET),
            entry("cinnabar_lantern", "朱砂灯笼球形烟花", "Cinnabar Lantern Sphere", "#D63228", "#FF8A42", "#FFF0B5", HOLLOW_LANTERN, HOLLOW_CENTER, EQUATOR_RING),
            entry("emerald_twin", "翡翠双层球形烟花", "Emerald Twin Sphere", "#0C9A66", "#68E6A6", "#EEFFD7", TWIN_SHELL, DOUBLE_SHELL, INNER_CORE),
            entry("violet_petal", "紫罗兰花瓣球形烟花", "Violet Petal Sphere", "#7E45DB", "#D98CFF", "#FFE6FF", PETAL_WEAVE, PETAL_OFFSET, DOUBLE_SHELL),
            entry("golden_orbit", "鎏金环带球形烟花", "Golden Orbit Sphere", "#E69417", "#FFD365", "#FFFFD0", EQUATORIAL_ORBIT, EQUATOR_RING, HOLLOW_CENTER),
            entry("teal_spiral", "青绿螺旋球形烟花", "Teal Spiral Sphere", "#0B9E9C", "#61F0DB", "#DFFFF6", SPIRAL_LATITUDE, SPIRAL_LATITUDES, INNER_CORE),
            entry("scarlet_star", "猩红星芒球形烟花", "Scarlet Star Sphere", "#E32636", "#FF7B5D", "#FFF3C2", STAR_TIP, TERMINAL_STARS, DOUBLE_SHELL),
            entry("amethyst_crown", "紫晶冠冕球形烟花", "Amethyst Crown Sphere", "#6330B7", "#B774FF", "#F9E8FF", CROWNED_CORE, SPARSE_CROWN, INNER_CORE),
            entry("coral_delayed", "珊瑚迟放球形烟花", "Coral Delayed Sphere", "#F0505A", "#FFAA76", "#FFF2CE", DELAYED_OUTER_RING, DELAYED_RING, HOLLOW_CENTER),
            entry("silver_lace", "银白蕾丝球形烟花", "Silver Lace Sphere", "#9CB4C8", "#E6F4FF", "#FFFFFF", LACE_GLOBE, LACE_GAPS, EQUATOR_RING),
            entry("amber_comet", "琥珀彗尾球形烟花", "Amber Comet Sphere", "#DF7009", "#FFA52A", "#FFF0AF", COMET_SHELL, SHORT_COMET_TRAILS, TERMINAL_STARS),
            entry("sapphire_crystal", "蓝宝晶格球形烟花", "Sapphire Crystal Sphere", "#1B55C9", "#5EA9FF", "#DCF9FF", SmallSphereDefinition.ShapeVariant.CRYSTAL_GRID, SmallSphereDefinition.StructuralFeature.CRYSTAL_GRID, HOLLOW_CENTER),
            entry("rose_ripple", "玫瑰涟漪球形烟花", "Rose Ripple Sphere", "#D63878", "#FF90B9", "#FFF1F7", RIPPLE_SPHERE, RIPPLE_BANDS, DOUBLE_SHELL),
            entry("jade_offset", "碧玉错层球形烟花", "Jade Offset Sphere", "#138C63", "#7DE0A5", "#E7FFE9", SmallSphereDefinition.ShapeVariant.OFFSET_HEMISPHERES, SmallSphereDefinition.StructuralFeature.OFFSET_HEMISPHERES, INNER_CORE),
            entry("aurora_veil", "极光帷幕球形烟花", "Aurora Veil Sphere", "#3867D9", "#75E7DD", "#E7FFE6", AURORA_VEIL, VEIL_ARCS, SPIRAL_LATITUDES));

    static {
        Set<String> ids = new HashSet<>();
        Set<String> palettes = new HashSet<>();
        Set<SmallSphereDefinition.ShapeVariant> variants = new HashSet<>();
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || DETONATION_HEIGHT < 24.0D || DETONATION_HEIGHT >= 40.0D) {
            throw new IllegalStateException("Small sphere count or low-altitude launch bound drifted");
        }
        for (SmallSphereDefinition definition : VALUES) {
            if (!ids.add(definition.id()) || !variants.add(definition.variant())
                    || !palettes.add(definition.palette().primary() + definition.palette().secondary() + definition.palette().accent())
                    || definition.clientPlan().particlesPerTick() > LOCAL_TICK_BUDGET
                    || definition.clientPlan().totalParticles() > LOCAL_OWNED_PARTICLE_BUDGET) {
                throw new IllegalStateException("Small sphere uniqueness or bounded-client contract drifted");
            }
        }
    }

    private SmallSphereCatalog() { }

    public static List<SmallSphereDefinition> values() { return VALUES; }

    private static SmallSphereDefinition entry(String token, String zh, String en, String primary, String secondary,
            String accent, SmallSphereDefinition.ShapeVariant variant, SmallSphereDefinition.StructuralFeature first,
            SmallSphereDefinition.StructuralFeature second) {
        return new SmallSphereDefinition("release_next_small_" + token + "_sphere_firework", zh, en,
                new SmallSphereDefinition.Palette(primary, secondary, accent), variant, Set.of(first, second), PLAN);
    }
}
