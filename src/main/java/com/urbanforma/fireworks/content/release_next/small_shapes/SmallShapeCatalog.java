package com.urbanforma.fireworks.content.release_next.small_shapes;

import java.util.List;
import java.util.Map;

/**
 * Isolated integration manifest for fifteen compact, deliberately non-spherical firework programs.
 *
 * <p>This is metadata only: registration, items, payload routing, recipes, and server behavior remain owned by the
 * integration coordinator.</p>
 */
public final class SmallShapeCatalog {
    public static final String MANIFEST_RESOURCE =
            "assets/urbanforma_fireworks/release_next/small_shapes/small_shapes_manifest.json";
    public static final int REQUIRED_ENTRY_COUNT = 15;
    public static final int MAX_PARTICLES_PER_TICK = 64;
    public static final int MAX_OWNED_PARTICLES = 448;
    public static final double MAX_RADIUS = 9.0D;

    private static final List<Definition> VALUES = List.of(
            definition("small_shape_amber_short_ray", "琥珀短放射烟花", "Amber Short-Ray Firework", Family.SHORT_RAY, 36, 6, "#FF8A26", "#FFD05A", "#FFF2BA"),
            definition("small_shape_cinnabar_short_ray", "朱砂短放射烟花", "Cinnabar Short-Ray Firework", Family.SHORT_RAY, 32, 7, "#D93732", "#F68A45", "#FFE2A8"),
            definition("small_shape_jade_ring", "青玉环形烟花", "Jade Ring Firework", Family.RING, 48, 5, "#26B79A", "#82E6BF", "#E3FFF1"),
            definition("small_shape_violet_double_ring", "紫晶双环烟花", "Violet Double-Ring Firework", Family.RING, 40, 6, "#7C4DCE", "#C49BFF", "#F3E7FF"),
            definition("small_shape_gold_comet", "鎏金彗星烟花", "Gilded Comet Firework", Family.COMET, 30, 7, "#E8831E", "#FFD761", "#FFF5C9"),
            definition("small_shape_azure_comet", "湛蓝彗星烟花", "Azure Comet Firework", Family.COMET, 28, 7, "#1787D1", "#6BD5FF", "#E9FAFF"),
            definition("small_shape_coral_cross", "珊瑚十字烟花", "Coral Cross Firework", Family.CROSS, 36, 6, "#F25850", "#FFAC76", "#FFF0C9"),
            definition("small_shape_silver_cross", "银星十字烟花", "Silver Cross Firework", Family.CROSS, 40, 6, "#A5BCD2", "#E7F2FF", "#FFFFFF"),
            definition("small_shape_willow_ember", "余烬短柳烟花", "Ember Short-Willow Firework", Family.SHORT_WILLOW, 32, 7, "#D94827", "#FF9B3F", "#FFE9B4"),
            definition("small_shape_willow_moon", "月白短柳烟花", "Moon Short-Willow Firework", Family.SHORT_WILLOW, 36, 7, "#6E8DB5", "#BFD9F2", "#F4FAFF"),
            definition("small_shape_copper_mine", "赤铜矿井烟花", "Copper Mine Firework", Family.MINE, 30, 7, "#B85824", "#E99B48", "#FFE3A6"),
            definition("small_shape_emerald_mine", "翠绿矿井烟花", "Emerald Mine Firework", Family.MINE, 28, 7, "#178B67", "#61D8A2", "#E1FFED"),
            definition("small_shape_saffron_pulse", "藏红脉冲烟花", "Saffron Pulse Firework", Family.PULSE, 48, 6, "#F28516", "#FFD24A", "#FFF6C7"),
            definition("small_shape_lilac_pulse", "丁香脉冲烟花", "Lilac Pulse Firework", Family.PULSE, 42, 6, "#9A63D7", "#D7B8FF", "#F8EFFF"),
            definition("small_shape_rose_split", "玫瑰分裂烟花", "Rose Split Firework", Family.SPLIT, 40, 7, "#DB4B83", "#FF9BBC", "#FFF0F5"));
    private static final Map<String, Definition> BY_ID = VALUES.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Definition::id, value -> value));

    static {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || VALUES.stream().map(Definition::family).distinct().count() != Family.values().length) {
            throw new IllegalStateException("Small-shape catalog coverage drifted");
        }
        for (Definition value : VALUES) {
            if (value.totalParticles() > MAX_OWNED_PARTICLES || value.particlesPerTick() > MAX_PARTICLES_PER_TICK || value.maxRadius() > MAX_RADIUS) {
                throw new IllegalStateException("Small-shape local budget drifted for " + value.id());
            }
        }
    }

    private SmallShapeCatalog() { }

    public static List<Definition> values() { return VALUES; }
    public static Definition byId(String id) { return BY_ID.get(id); }

    private static Definition definition(String id, String zhName, String enName, Family family, int branches, int segments, String primary, String secondary, String accent) {
        return new Definition(id, zhName, enName, family, branches, segments, new Palette(primary, secondary, accent), 15, 30, 8.8D);
    }

    public enum Family { SHORT_RAY, RING, COMET, CROSS, SHORT_WILLOW, MINE, PULSE, SPLIT }

    public record Definition(String id, String zhName, String enName, Family family, int branchCount, int emissionTicks, Palette palette, int minLifetimeTicks, int maxLifetimeTicks, double maxRadius) {
        public Definition {
            if (id == null || !id.matches("small_shape_[a-z0-9_]+") || zhName == null || zhName.isBlank() || enName == null || enName.isBlank()
                    || family == null || branchCount <= 0 || emissionTicks <= 0 || palette == null || minLifetimeTicks <= 0
                    || maxLifetimeTicks < minLifetimeTicks || !Double.isFinite(maxRadius) || maxRadius <= 0.0D) {
                throw new IllegalArgumentException("Invalid small-shape definition");
            }
        }
        public int particlesPerTick() { return branchCount; }
        public int totalParticles() { return branchCount * emissionTicks; }
        public String clientProgramClass() { return "com.urbanforma.fireworks.client.release_next.small_shapes.SmallShapeClientProgram"; }
        public boolean clientOnly() { return true; }
        public boolean createsServerSimulation() { return false; }
    }

    public record Palette(String primary, String secondary, String accent) {
        public Palette {
            if (!hex(primary) || !hex(secondary) || !hex(accent)) throw new IllegalArgumentException("Small-shape palette must use #RRGGBB");
        }
        private static boolean hex(String value) { return value != null && value.matches("#[0-9a-fA-F]{6}"); }
    }
}
