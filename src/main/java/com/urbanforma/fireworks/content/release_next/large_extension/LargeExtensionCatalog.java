package com.urbanforma.fireworks.content.release_next.large_extension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Isolated, append-only handoff catalog for twenty large fireworks.
 *
 * <p>This common-side type intentionally contains no registry access, packets, server particle loops, or trajectory
 * simulation. The integration owner supplies the stable style/item mapping and calls the matching physical-client
 * program with the compact burst id, center, and seed.</p>
 */
public final class LargeExtensionCatalog {
    public static final String BATCH_ID = "release_next:large_extension";
    public static final String INTEGRATION_STATUS = "ISOLATED_NOT_REGISTERED";
    public static final String CREATIVE_SECTION = "large";
    public static final int REQUIRED_ENTRY_COUNT = 20;
    public static final int MAX_PARTICLES_PER_TICK = 144;
    public static final int MAX_PLANNED_PARTICLES = 2_880;
    public static final int MAX_VISUAL_TICKS = 124;

    private static final List<Entry> VALUES = List.of(
            entry(1, "large_extension_crimson_fan_palm", "赤红扇面棕榈烟花", "Crimson Fan Palm Firework", Form.FAN_PALM, "#D72835", "#FF8A3D", "#FFE1AB", 20, 120, 34, 58, 52),
            entry(2, "large_extension_gold_crown_fountain", "金冠喷泉烟花", "Gold Crown Fountain Firework", Form.CROWN_FOUNTAIN, "#F0A51F", "#FFE17A", "#FFF7D6", 22, 112, 38, 64, 54),
            entry(3, "large_extension_amber_scallop_shell", "琥珀扇贝壳烟花", "Amber Scallop Shell Firework", Form.SCALLOP_SHELL, "#D96B12", "#FFB12E", "#FFF0B9", 18, 128, 34, 56, 50),
            entry(4, "large_extension_coral_petal_lace", "珊瑚花瓣蕾丝烟花", "Coral Petal Lace Firework", Form.PETAL_LACE, "#F06458", "#FFAA83", "#FFF0DD", 24, 96, 40, 66, 55),
            entry(5, "large_extension_rose_heart_bloom", "玫瑰心形绽放烟花", "Rose Heart Bloom Firework", Form.HEART_BLOOM, "#D74D7E", "#F69BB4", "#FFF0F4", 26, 90, 42, 68, 53),
            entry(6, "large_extension_violet_spiderweb", "紫晶蛛网烟花", "Violet Spiderweb Firework", Form.SPIDERWEB, "#7440B8", "#B486E8", "#F1E6FF", 24, 108, 38, 62, 56),
            entry(7, "large_extension_sapphire_comet_wheel", "蓝宝石彗星轮烟花", "Sapphire Comet Wheel Firework", Form.COMET_WHEEL, "#1E58B9", "#5DB4FF", "#E7F5FF", 22, 120, 36, 60, 54),
            entry(8, "large_extension_teal_double_helix", "青绿双螺旋烟花", "Teal Double Helix Firework", Form.DOUBLE_HELIX, "#087F82", "#38D4C0", "#E6FFF8", 26, 96, 42, 68, 52),
            entry(9, "large_extension_silver_white_snowflake", "银白雪花烟花", "Silver White Snowflake Firework", Form.SNOWFLAKE, "#B8C9D9", "#EEF7FF", "#FFFFFF", 20, 120, 38, 62, 54),
            entry(10, "large_extension_black_gold_eclipse", "黑金日蚀烟花", "Black Gold Eclipse Firework", Form.ECLIPSE, "#25232D", "#C99424", "#FFE5A5", 24, 96, 40, 66, 56),
            entry(11, "large_extension_ruby_chrysanthemum", "红宝石千轮菊烟花", "Ruby Chrysanthemum Firework", Form.CHRYSANTHEMUM, "#B82035", "#F0524C", "#FFE0B0", 18, 144, 34, 58, 58),
            entry(12, "large_extension_champagne_gold_diamond", "香槟金钻石烟花", "Champagne Gold Diamond Firework", Form.DIAMOND, "#D9AE55", "#FFE9A5", "#FFFCEC", 20, 120, 38, 64, 52),
            entry(13, "large_extension_honey_amber_lantern", "蜜琥珀灯笼烟花", "Honey Amber Lantern Firework", Form.LANTERN, "#E48615", "#FFC44C", "#FFF1C8", 22, 112, 40, 66, 54),
            entry(14, "large_extension_coral_reef_branch", "珊瑚礁分枝烟花", "Coral Reef Branch Firework", Form.REEF_BRANCH, "#E95C58", "#FF9D7C", "#FFF1DB", 24, 108, 40, 66, 55),
            entry(15, "large_extension_rose_garden_maze", "玫瑰花园迷宫烟花", "Rose Garden Maze Firework", Form.GARDEN_MAZE, "#C84275", "#EF84A5", "#FFE9F0", 26, 96, 42, 68, 54),
            entry(16, "large_extension_amethyst_orbit_lattice", "紫水晶轨道晶格烟花", "Amethyst Orbit Lattice Firework", Form.ORBIT_LATTICE, "#713AA9", "#A976DC", "#F0E8FF", 22, 120, 40, 64, 56),
            entry(17, "large_extension_azure_polar_cross", "蔚蓝极光十字烟花", "Azure Polar Cross Firework", Form.POLAR_CROSS, "#1E77CF", "#62CFFF", "#E9FBFF", 20, 128, 38, 62, 57),
            entry(18, "large_extension_jade_tide_arc", "青玉潮汐弧烟花", "Jade Tide Arc Firework", Form.TIDE_ARC, "#168A72", "#63D8B8", "#EDFFF8", 24, 108, 40, 66, 53),
            entry(19, "large_extension_platinum_starburst", "铂金星爆烟花", "Platinum Starburst Firework", Form.STARBURST, "#BEC7D3", "#F5DE91", "#FFFFFF", 18, 144, 36, 60, 58),
            entry(20, "large_extension_onyx_gilded_gate", "缟玛瑙镀金门烟花", "Onyx Gilded Gate Firework", Form.GILDED_GATE, "#211F27", "#B88424", "#FFE7A7", 26, 96, 42, 68, 55));
    private static final Map<String, Entry> BY_ID = byId();

    static { validate(); }
    private LargeExtensionCatalog() { }
    public static List<Entry> values() { return VALUES; }
    public static Entry require(String id) {
        Entry entry = BY_ID.get(id);
        if (entry == null) throw new IllegalArgumentException("Unknown large extension id " + id);
        return entry;
    }

    private static Entry entry(int order, String id, String zh, String en, Form form, String primary, String secondary,
            String accent, int emissionTicks, int perTick, int minLifetime, int maxLifetime, int radius) {
        return new Entry(order, id, zh, en, form, new Palette(primary, secondary, accent),
                new Budget(emissionTicks, perTick, minLifetime, maxLifetime), radius);
    }
    private static Map<String, Entry> byId() {
        Map<String, Entry> indexed = new HashMap<>();
        for (Entry value : VALUES) if (indexed.put(value.id(), value) != null) throw new IllegalStateException("Duplicate id");
        return Map.copyOf(indexed);
    }
    public static void validate() {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT) throw new IllegalStateException("Large extension count drifted");
        Set<Integer> orders = new HashSet<>(); Set<Form> forms = new HashSet<>(); Set<String> palettes = new HashSet<>();
        for (Entry value : VALUES) {
            if (!orders.add(value.order()) || !forms.add(value.form()) || !palettes.add(value.palette().signature())
                    || value.budget().perTick() > MAX_PARTICLES_PER_TICK || value.budget().plannedParticles() > MAX_PLANNED_PARTICLES
                    || value.budget().totalVisualTicks() > MAX_VISUAL_TICKS) throw new IllegalStateException("Large extension contract drifted: " + value.id());
        }
        for (int expected = 1; expected <= REQUIRED_ENTRY_COUNT; expected++) if (!orders.contains(expected)) throw new IllegalStateException("Non-contiguous order");
    }

    public enum Form { FAN_PALM, CROWN_FOUNTAIN, SCALLOP_SHELL, PETAL_LACE, HEART_BLOOM, SPIDERWEB, COMET_WHEEL, DOUBLE_HELIX, SNOWFLAKE, ECLIPSE, CHRYSANTHEMUM, DIAMOND, LANTERN, REEF_BRANCH, GARDEN_MAZE, ORBIT_LATTICE, POLAR_CROSS, TIDE_ARC, STARBURST, GILDED_GATE }
    public record Entry(int order, String id, String zhName, String enName, Form form, Palette palette, Budget budget, int radiusBlocks) {
        public Entry { if (order < 1 || id == null || !id.matches("large_extension_[a-z0-9_]+") || zhName == null || zhName.isBlank() || enName == null || enName.isBlank() || form == null || palette == null || budget == null || radiusBlocks < 48 || radiusBlocks > 60) throw new IllegalArgumentException("Invalid large extension entry"); }
        public String clientProgramId() { return BATCH_ID + ":" + form.name().toLowerCase(); }
        public int fullEnvelopeBlocks() { return radiusBlocks * 2; }
    }
    public record Palette(String primary, String secondary, String accent) {
        public Palette { if (!hex(primary) || !hex(secondary) || !hex(accent) || primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) throw new IllegalArgumentException("Invalid palette"); }
        public String signature() { return primary + "/" + secondary + "/" + accent; }
        private static boolean hex(String value) { return value != null && value.matches("#[0-9A-Fa-f]{6}"); }
    }
    public record Budget(int emissionTicks, int perTick, int minLifetimeTicks, int maxLifetimeTicks) {
        public Budget { if (emissionTicks < 18 || perTick < 1 || maxLifetimeTicks < minLifetimeTicks || minLifetimeTicks < 30 || emissionTicks + maxLifetimeTicks > MAX_VISUAL_TICKS) throw new IllegalArgumentException("Invalid finite budget"); }
        public int plannedParticles() { return emissionTicks * perTick; }
        public int totalVisualTicks() { return emissionTicks + maxLifetimeTicks; }
    }
}
