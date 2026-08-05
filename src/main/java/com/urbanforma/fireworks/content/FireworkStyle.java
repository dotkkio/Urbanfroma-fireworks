package com.urbanforma.fireworks.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable program data for the v0.2 firework series.
 *
 * <p>The style index is synchronized and persisted by the shared rocket entity. {@code diameter} is the main burst
 * shell in blocks, while {@code fullEnvelope} is the complete visible span including a crown or willow curtain.
 * Index {@code 0} is the accepted golden demonstration firework; the remaining entries deliberately have no
 * legacy-id aliases.</p>
 */
public record FireworkStyle(
        int index,
        String id,
        String zhName,
        String enName,
        Family family,
        Shape shape,
        Rgb primaryColor,
        Rgb secondaryColor,
        Rgb accentColor,
        int flightTicks,
        int diameter,
        int fullEnvelope,
        int phaseDelayTicks,
        int totalStarCount,
        LayerShares layerShares,
        int starsPerTick,
        TrailTier trailTier,
        int outerLifetime,
        int innerLifetime,
        int accentLifetime,
        float twinkleChanceMin,
        float twinkleChanceMax,
        WillowProfile willowProfile,
        RadiantProfile radiantProfile,
        RadiantWillowProfile radiantWillowProfile,
        GiantTier giantTier) {
    public enum Family {
        DEMONSTRATION,
        WARM,
        COOL,
        JEWEL,
        METALLIC,
        LED_MONOCHROME
    }

    public enum Shape {
        SPHERE,
        DOUBLE_SPHERE,
        CROWN_SPHERE,
        WILLOW_SPHERE,
        RADIANT,
        RADIANT_WILLOW,
        GIANT_RADIANT,
        HYBRID_SPHERE_RADIANT,
        SATURN,
        OTHER
    }

    /** Launch-tail budgets; all non-golden styles use the 10/12/14 and 22/24/26 approved tiers. */
    public enum TrailTier {
        GOLDEN(18, 26),
        COMPACT(10, 22),
        STANDARD(12, 24),
        GRAND(14, 26);

        private final int starsPerTick;
        private final int lifetime;

        TrailTier(int starsPerTick, int lifetime) {
            this.starsPerTick = starsPerTick;
            this.lifetime = lifetime;
        }

        public int starsPerTick() {
            return this.starsPerTick;
        }

        public int lifetime() {
            return this.lifetime;
        }
    }

    /** RGB channels are normalized so client particle code can consume the approved hexadecimal colors exactly. */
    public record Rgb(float red, float green, float blue) {
        public Rgb {
            validateChannel(red, "red");
            validateChannel(green, "green");
            validateChannel(blue, "blue");
        }

        public static Rgb fromHex(String hex) {
            if (hex == null || !hex.matches("#[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("Expected #RRGGBB color");
            }
            return new Rgb(
                    Integer.parseInt(hex.substring(1, 3), 16) / 255.0F,
                    Integer.parseInt(hex.substring(3, 5), 16) / 255.0F,
                    Integer.parseInt(hex.substring(5, 7), 16) / 255.0F);
        }

        private static void validateChannel(float value, String name) {
            if (value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(name + " must be between 0 and 1");
            }
        }
    }

    /** Immutable per-mille layer shares. The three values must sum to 1,000. */
    public record LayerShares(int mainPermille, int secondaryPermille, int accentPermille) {
        public LayerShares {
            if (mainPermille < 0 || secondaryPermille < 0 || accentPermille < 0
                    || mainPermille + secondaryPermille + accentPermille != 1_000) {
                throw new IllegalArgumentException("Layer shares must be non-negative and sum to 1000");
            }
        }
    }

    /**
     * Immutable geometry program for a long willow shell.
     *
     * <p>The per-style values control horizontal reach, initial rise, and terminal fall. Branch count, segment
     * count, zero core stars, and branch lifetime are deliberately fixed across the six approved willow styles.</p>
     */
    public record WillowProfile(int horizontalReach, int rise, int drop) {
        public static final int BRANCH_COUNT = 160;
        public static final int SEGMENTS_PER_BRANCH = 30;
        public static final int CORE_STAR_COUNT = 0;
        public static final int MIN_LIFETIME = 160;
        public static final int MAX_LIFETIME = 180;

        public WillowProfile {
            if (horizontalReach <= 0 || rise < 0 || drop <= 0) {
                throw new IllegalArgumentException("Willow profile values must describe a positive arc");
            }
        }

        public int branchCount() {
            return BRANCH_COUNT;
        }

        public int segmentsPerBranch() {
            return SEGMENTS_PER_BRANCH;
        }

        public int coreStarCount() {
            return CORE_STAR_COUNT;
        }

        public int minLifetime() {
            return MIN_LIFETIME;
        }

        public int maxLifetime() {
            return MAX_LIFETIME;
        }

        public int upwardRise() {
            return this.rise;
        }

        public int downwardFall() {
            return this.drop;
        }
    }

    /** Immutable geometry settings for the first dense, gently drooping radiant shell. */
    public record RadiantProfile(
            double initialRadius,
            double maximumRadius,
            double verticalScale,
            double bendStartMin,
            double bendStartMax,
            double terminalDrop) {
        public static final int BRANCH_COUNT = 160;
        public static final int SEGMENTS_PER_BRANCH = 30;
        public static final int CORE_SEGMENT_COUNT = 3;
        public static final int MIN_LIFETIME = 58;
        public static final int MAX_LIFETIME = 62;

        public RadiantProfile {
            if (!Double.isFinite(initialRadius) || !Double.isFinite(maximumRadius)
                    || !Double.isFinite(verticalScale) || !Double.isFinite(bendStartMin)
                    || !Double.isFinite(bendStartMax) || !Double.isFinite(terminalDrop)
                    || initialRadius <= 0.0D || maximumRadius <= initialRadius
                    || verticalScale <= 0.0D || verticalScale > 1.0D
                    || bendStartMin < 0.0D || bendStartMax > 1.0D || bendStartMin > bendStartMax
                    || terminalDrop <= 0.0D) {
                throw new IllegalArgumentException("Invalid radiant profile");
            }
        }

        public int branchCount() {
            return BRANCH_COUNT;
        }

        public int segmentsPerBranch() {
            return SEGMENTS_PER_BRANCH;
        }

        public int coreSegmentCount() {
            return CORE_SEGMENT_COUNT;
        }

        public int minLifetime() {
            return MIN_LIFETIME;
        }

        public int maxLifetime() {
            return MAX_LIFETIME;
        }
    }

    /**
     * Immutable data for a radiant shell whose already-created outer sparks continuously bend into willow arcs.
     *
     * <p>The first three radiant rings are the brief break center. The remaining twenty-seven rings are retained
     * and client-driven through the extension; this profile deliberately contains no second-stage particle count
     * or clear-delay setting.</p>
     */
    public record RadiantWillowProfile(
            RadiantProfile radiantProfile,
            int minExtensionTicks,
            int maxExtensionTicks,
            double additionalRadialExtension,
            double bendStartMin,
            double bendStartMax,
            double terminalDrop,
            double maximumLateralSway) {
        public static final int MANAGED_FIRST_RADIANT_SEGMENT = RadiantProfile.CORE_SEGMENT_COUNT;
        public static final int MANAGED_SEGMENTS_PER_BRANCH =
                RadiantProfile.SEGMENTS_PER_BRANCH - MANAGED_FIRST_RADIANT_SEGMENT;

        public RadiantWillowProfile {
            if (radiantProfile == null
                    || minExtensionTicks < 100
                    || maxExtensionTicks > 140
                    || minExtensionTicks > maxExtensionTicks
                    || !Double.isFinite(additionalRadialExtension)
                    || !Double.isFinite(bendStartMin)
                    || !Double.isFinite(bendStartMax)
                    || !Double.isFinite(terminalDrop)
                    || !Double.isFinite(maximumLateralSway)
                    || additionalRadialExtension <= 0.0D
                    || bendStartMin < 0.0D
                    || bendStartMax > 1.0D
                    || bendStartMin > bendStartMax
                    || terminalDrop <= 0.0D
                    || maximumLateralSway <= 0.0D) {
                throw new IllegalArgumentException("Invalid radiant willow profile");
            }
        }

        public int branchCount() {
            return this.radiantProfile.branchCount();
        }

        public int radiantSegmentsPerBranch() {
            return this.radiantProfile.segmentsPerBranch();
        }

        public int managedFirstRadiantSegment() {
            return MANAGED_FIRST_RADIANT_SEGMENT;
        }

        public int managedSegmentsPerBranch() {
            return MANAGED_SEGMENTS_PER_BRANCH;
        }
    }

    private static final LayerShares GOLDEN_SHARES = new LayerShares(889, 111, 0);
    private static final LayerShares SPHERE_SHARES = new LayerShares(640, 260, 100);
    private static final LayerShares DOUBLE_SHARES = new LayerShares(520, 380, 100);
    private static final LayerShares CROWN_SHARES = new LayerShares(580, 320, 100);
    private static final LayerShares WILLOW_SHARES = new LayerShares(420, 460, 120);
    private static final LayerShares RADIANT_SHARES = new LayerShares(200, 600, 200);

    /**
     * Immutable source data for the LED palette sphere expansion.
     *
     * <p>Each entry deliberately keeps the source LED color alongside its richer three-color firework palette.
     * The five neutral LED colors are intentionally excluded from this table.</p>
     */
    public record LedMonochromeDefinition(
            int index,
            String id,
            String zhName,
            String enName,
            Rgb ledReferenceColor,
            Rgb primaryColor,
        Rgb secondaryColor,
        Rgb accentColor) {
        public LedMonochromeDefinition {
            if (index < 26 || index > 39 || id == null || id.isBlank()) {
                throw new IllegalArgumentException("Invalid LED monochrome firework definition " + id);
            }
            Objects.requireNonNull(zhName, "zhName");
            Objects.requireNonNull(enName, "enName");
            Objects.requireNonNull(ledReferenceColor, "ledReferenceColor");
            Objects.requireNonNull(primaryColor, "primaryColor");
            Objects.requireNonNull(secondaryColor, "secondaryColor");
            Objects.requireNonNull(accentColor, "accentColor");
            if (primaryColor.equals(secondaryColor) || primaryColor.equals(accentColor)
                    || secondaryColor.equals(accentColor)) {
                throw new IllegalArgumentException("LED monochrome palettes require three distinct colors");
            }
        }
    }

    private static final List<LedMonochromeDefinition> LED_MONOCHROME_DEFINITIONS = List.of(
            ledMonochrome(
                    26, "led_scarlet_sphere", "猩红辉耀球形烟花", "Scarlet Radiance Sphere Firework",
                    "#BC4040", "#E01B1B", "#FF3415", "#FFD1D1"),
            ledMonochrome(
                    27, "led_coral_sphere", "珊瑚曙光球形烟花", "Coral Dawn Sphere Firework",
                    "#DA8971", "#FE5D2E", "#FF7324", "#FFDCD1"),
            ledMonochrome(
                    28, "led_amber_sphere", "琥珀熔光球形烟花", "Amber Emberglow Sphere Firework",
                    "#D09F40", "#F4A815", "#FFCD0C", "#FFEFD1"),
            ledMonochrome(
                    29, "led_lemon_sphere", "柠檬日耀球形烟花", "Lemon Sunflare Sphere Firework",
                    "#E2D458", "#FFEA2B", "#F8FF21", "#FFFAD1"),
            ledMonochrome(
                    30, "led_chartreuse_sphere", "黄绿春辉球形烟花", "Chartreuse Springlight Sphere Firework",
                    "#9AC952", "#A0ED2B", "#BEFF24", "#EDFFD1"),
            ledMonochrome(
                    31, "led_mint_sphere", "薄荷极光球形烟花", "Mint Aurora Sphere Firework",
                    "#6EC992", "#2BED77", "#24FF65", "#D1FFE3"),
            ledMonochrome(
                    32, "led_teal_sphere", "青绿潮光球形烟花", "Teal Tideglow Sphere Firework",
                    "#4EACA5", "#25D0C3", "#24FFD9", "#D1FFFC"),
            ledMonochrome(
                    33, "led_cyan_sphere", "青蓝冰辉球形烟花", "Cyan Iceglow Sphere Firework",
                    "#4CA5C9", "#25B3ED", "#1ED5FF", "#D1F2FF"),
            ledMonochrome(
                    34, "led_azure_sphere", "蔚蓝天穹球形烟花", "Azure Skyglow Sphere Firework",
                    "#5184D1", "#297AF5", "#2163FF", "#D1E3FF"),
            ledMonochrome(
                    35, "led_cobalt_sphere", "钴蓝深辉球形烟花", "Cobalt Deepglow Sphere Firework",
                    "#5365CF", "#2C49F3", "#242EFF", "#D1D8FF"),
            ledMonochrome(
                    36, "led_violet_sphere", "紫罗兰星辉球形烟花", "Violet Starlight Sphere Firework",
                    "#7B5ECB", "#5F2BEF", "#4C24FF", "#DDD1FF"),
            ledMonochrome(
                    37, "led_lilac_sphere", "丁香月辉球形烟花", "Lilac Moonlight Sphere Firework",
                    "#9C6DD1", "#8A2CF5", "#7824FF", "#E7D1FF"),
            ledMonochrome(
                    38, "led_magenta_sphere", "洋红霓彩球形烟花", "Magenta Neon Glow Sphere Firework",
                    "#BE5FCA", "#D82BEE", "#F924FF", "#FAD1FF"),
            ledMonochrome(
                    39, "led_rose_sphere", "玫瑰晨辉球形烟花", "Rose Dawnfire Sphere Firework",
                    "#CC6F74", "#F02B36", "#FF2724", "#FFD1D4"));

    public static final FireworkStyle GRAND_GOLDEN_SPHERE = style(
            0,
            "grand_golden_sphere_firework",
            "巨型金色球形烟花",
            "Grand Golden Sphere Firework",
            Family.DEMONSTRATION,
            Shape.SPHERE,
            "#FFCC1A",
            "#FFE14D",
            "#FFF0B5",
            86,
            105,
            105,
            0,
            2_160,
            GOLDEN_SHARES,
            180,
            TrailTier.GOLDEN,
            82,
            64,
            64,
            1.0F,
            1.0F);

    public static final FireworkStyle CINNABAR_AMBER_SPHERE = style(
            1, "cinnabar_amber_sphere", "朱砂琥珀球形烟花", "Cinnabar Amber Sphere Firework",
            Family.WARM, Shape.SPHERE, "#F42D1A", "#FF9A1A", "#FFF0B5", 52, 60, 60, 0,
            1_600, SPHERE_SHARES, 144, TrailTier.COMPACT, 72, 64, 56, 0.35F, 0.60F);
    public static final FireworkStyle SAFFRON_CORAL_SPHERE = style(
            2, "saffron_coral_sphere", "藏红珊瑚球形烟花", "Saffron Coral Sphere Firework",
            Family.WARM, Shape.SPHERE, "#FF8A05", "#FF4F4F", "#FFE3A1", 57, 68, 68, 0,
            1_800, SPHERE_SHARES, 156, TrailTier.COMPACT, 76, 66, 58, 0.35F, 0.60F);
    public static final FireworkStyle RUBY_SOLAR_SPHERE = style(
            3, "ruby_solar_sphere", "红宝石日耀球形烟花", "Ruby Solar Sphere Firework",
            Family.WARM, Shape.SPHERE, "#E71943", "#FFB31A", "#FFF3B2", 64, 76, 76, 0,
            1_920, SPHERE_SHARES, 168, TrailTier.COMPACT, 82, 72, 60, 0.35F, 0.60F);
    public static final FireworkStyle EMBER_CHAMPAGNE_DOUBLE_SPHERE = style(
            4, "ember_champagne_double_sphere", "余烬香槟双层球形烟花", "Ember Champagne Double Sphere Firework",
            Family.WARM, Shape.DOUBLE_SPHERE, "#FF5A19", "#FFD080", "#FFF5D0", 70, 88, 88, 0,
            2_160, DOUBLE_SHARES, 180, TrailTier.STANDARD, 86, 76, 62, 0.35F, 0.60F);
    public static final FireworkStyle VERMILION_GOLD_DOUBLE_SPHERE = style(
            5, "vermilion_gold_double_sphere", "朱红鎏金双层球形烟花", "Vermilion Gold Double Sphere Firework",
            Family.WARM, Shape.DOUBLE_SPHERE, "#F21F2B", "#F6B512", "#FFEEC2", 76, 96, 96, 0,
            2_304, DOUBLE_SHARES, 192, TrailTier.STANDARD, 92, 80, 66, 0.35F, 0.60F);
    public static final FireworkStyle CORAL_ROSE_CROWN_SPHERE = style(
            6, "coral_rose_crown_sphere", "珊瑚玫瑰冠顶球形烟花", "Coral Rose Crown Sphere Firework",
            Family.WARM, Shape.CROWN_SPHERE, "#FF694F", "#FF4C86", "#FFD0AF", 82, 94, 102, 4,
            2_304, CROWN_SHARES, 192, TrailTier.STANDARD, 90, 78, 68, 0.35F, 0.60F);
    public static final FireworkStyle AMBER_SUNSTONE_WILLOW = style(
            7, "amber_sunstone_willow", "琥珀日光长垂帘柳烟花", "Amber Sunstone Long Willow Firework",
            Family.WARM, Shape.WILLOW_SPHERE, "#FF9B16", "#FFCC4D", "#FFEFC5", 90, 86, 112, 4,
            2_400, WILLOW_SHARES, 204, TrailTier.GRAND, 90, 160, 150, 0.35F, 0.60F,
            willowProfile(34, 10, 23));
    public static final FireworkStyle SCARLET_COPPER_WILLOW = style(
            8, "scarlet_copper_willow", "绯红赤铜长垂帘柳烟花", "Scarlet Copper Long Willow Firework",
            Family.WARM, Shape.WILLOW_SPHERE, "#EC1735", "#CF5E25", "#FFE8AF", 117, 94, 120, 4,
            2_592, WILLOW_SHARES, 216, TrailTier.GRAND, 98, 180, 170, 0.35F, 0.60F,
            willowProfile(39, 13, 28));

    public static final FireworkStyle AQUA_ICE_SPHERE = style(
            9, "aqua_ice_sphere", "碧青冰晶球形烟花", "Aqua Ice Sphere Firework",
            Family.COOL, Shape.SPHERE, "#10DBE8", "#7ADFFF", "#E8FFFF", 58, 70, 70, 0,
            1_800, SPHERE_SHARES, 156, TrailTier.COMPACT, 78, 68, 60, 0.35F, 0.60F);
    public static final FireworkStyle COBALT_AZURE_SPHERE = style(
            10, "cobalt_azure_sphere", "钴蓝天青球形烟花", "Cobalt Azure Sphere Firework",
            Family.COOL, Shape.SPHERE, "#155DFF", "#27A7FF", "#DFF6FF", 74, 84, 84, 0,
            2_040, SPHERE_SHARES, 180, TrailTier.STANDARD, 84, 74, 64, 0.35F, 0.60F);
    public static final FireworkStyle CYAN_PLATINUM_DOUBLE_SPHERE = style(
            11, "cyan_platinum_double_sphere", "青蓝铂银双层球形烟花", "Cyan Platinum Double Sphere Firework",
            Family.COOL, Shape.DOUBLE_SPHERE, "#08B9E8", "#D4E3F7", "#FFFFFF", 86, 100, 100, 0,
            2_400, DOUBLE_SHARES, 204, TrailTier.STANDARD, 96, 84, 68, 0.35F, 0.60F);
    public static final FireworkStyle GLACIER_TEAL_CROWN_SPHERE = style(
            12, "glacier_teal_crown_sphere", "冰川青绿冠顶球形烟花", "Glacier Teal Crown Sphere Firework",
            Family.COOL, Shape.CROWN_SPHERE, "#70E6F5", "#15CAA6", "#E6FAFF", 101, 102, 109, 4,
            2_496, CROWN_SHARES, 208, TrailTier.GRAND, 100, 88, 72, 0.35F, 0.60F);
    public static final FireworkStyle POLAR_SILVER_WILLOW = style(
            13, "polar_silver_willow", "极光银白长垂帘柳烟花", "Polar Silver Long Willow Firework",
            Family.COOL, Shape.WILLOW_SPHERE, "#44D7C3", "#D8E8F0", "#FFFFFF", 114, 93, 118, 4,
            2_496, WILLOW_SHARES, 208, TrailTier.GRAND, 96, 174, 164, 0.35F, 0.60F,
            willowProfile(37, 12, 26));

    public static final FireworkStyle EMERALD_PERIDOT_SPHERE = style(
            14, "emerald_peridot_sphere", "翡翠橄榄石球形烟花", "Emerald Peridot Sphere Firework",
            Family.JEWEL, Shape.SPHERE, "#05B955", "#B8E342", "#FFF2B0", 64, 76, 76, 0,
            1_920, SPHERE_SHARES, 168, TrailTier.COMPACT, 82, 72, 60, 0.35F, 0.60F);
    public static final FireworkStyle AMETHYST_ORCHID_SPHERE = style(
            15, "amethyst_orchid_sphere", "紫晶兰花球形烟花", "Amethyst Orchid Sphere Firework",
            Family.JEWEL, Shape.SPHERE, "#7741E8", "#D758EF", "#FFE1FF", 78, 90, 90, 0,
            2_160, SPHERE_SHARES, 180, TrailTier.STANDARD, 88, 78, 66, 0.35F, 0.60F);
    public static final FireworkStyle SAPPHIRE_VIOLET_DOUBLE_SPHERE = style(
            16, "sapphire_violet_double_sphere", "蓝宝石紫晶双层球形烟花", "Sapphire Violet Double Sphere Firework",
            Family.JEWEL, Shape.DOUBLE_SPHERE, "#1B65E8", "#7D44EF", "#DFEFFF", 92, 102, 102, 0,
            2_496, DOUBLE_SHARES, 208, TrailTier.STANDARD, 98, 86, 70, 0.35F, 0.60F);
    public static final FireworkStyle GARNET_TOPAZ_DOUBLE_SPHERE = style(
            17, "garnet_topaz_double_sphere", "石榴石黄玉双层球形烟花", "Garnet Topaz Double Sphere Firework",
            Family.JEWEL, Shape.DOUBLE_SPHERE, "#A51644", "#F2AE21", "#FFE8B0", 99, 108, 108, 0,
            2_592, DOUBLE_SHARES, 216, TrailTier.GRAND, 102, 90, 72, 0.35F, 0.60F);
    public static final FireworkStyle OPAL_ROSE_CROWN_SPHERE = style(
            18, "opal_rose_crown_sphere", "欧泊蔷薇冠顶球形烟花", "Opal Rose Crown Sphere Firework",
            Family.JEWEL, Shape.CROWN_SPHERE, "#E7D3FF", "#F35A9C", "#FFF5D6", 106, 102, 110, 4,
            2_496, CROWN_SHARES, 208, TrailTier.GRAND, 102, 88, 72, 0.35F, 0.60F);
    public static final FireworkStyle JADE_PEARL_WILLOW = style(
            19, "jade_pearl_willow", "碧玉珍珠长垂帘柳烟花", "Jade Pearl Long Willow Firework",
            Family.JEWEL, Shape.WILLOW_SPHERE, "#0AA874", "#F5E5C5", "#E5FFF1", 117, 96, 120, 4,
            2_592, WILLOW_SHARES, 216, TrailTier.GRAND, 100, 180, 170, 0.35F, 0.60F,
            willowProfile(39, 13, 28));

    public static final FireworkStyle CHAMPAGNE_WHITE_GOLD_SPHERE = style(
            20, "champagne_white_gold_sphere", "香槟白金球形烟花", "Champagne White Gold Sphere Firework",
            Family.METALLIC, Shape.SPHERE, "#F7C66A", "#FFF5CC", "#FFFFFF", 70, 82, 82, 0,
            1_980, SPHERE_SHARES, 180, TrailTier.STANDARD, 84, 74, 62, 0.35F, 0.60F);
    public static final FireworkStyle COBALT_TITANIUM_DOUBLE_SPHERE = style(
            21, "cobalt_titanium_double_sphere", "钴蓝钛银双层球形烟花", "Cobalt Titanium Double Sphere Firework",
            Family.METALLIC, Shape.DOUBLE_SPHERE, "#1E66D0", "#BAC7D8", "#FFFFFF", 88, 105, 105, 0,
            2_496, DOUBLE_SHARES, 208, TrailTier.STANDARD, 100, 88, 70, 0.35F, 0.60F);
    public static final FireworkStyle PLATINUM_ONYX_CROWN_SPHERE = style(
            22, "platinum_onyx_crown_sphere", "铂银曜石冠顶球形烟花", "Platinum Onyx Crown Sphere Firework",
            Family.METALLIC, Shape.CROWN_SPHERE, "#E3EBF2", "#26357A", "#FFFFFF", 100, 103, 111, 4,
            2_496, CROWN_SHARES, 208, TrailTier.GRAND, 102, 88, 72, 0.35F, 0.60F);
    public static final FireworkStyle EMERALD_SILVER_CROWN_SPHERE = style(
            23, "emerald_silver_crown_sphere", "翡翠银冠顶球形烟花", "Emerald Silver Crown Sphere Firework",
            Family.METALLIC, Shape.CROWN_SPHERE, "#0EB978", "#D9E5ED", "#F7FFF5", 108, 106, 114, 4,
            2_592, CROWN_SHARES, 216, TrailTier.GRAND, 104, 90, 74, 0.35F, 0.60F);
    public static final FireworkStyle ROSE_GOLD_PEARL_WILLOW = style(
            24, "rose_gold_pearl_willow", "玫瑰金珍珠长垂帘柳烟花", "Rose Gold Pearl Long Willow Firework",
            Family.METALLIC, Shape.WILLOW_SPHERE, "#D88782", "#FFE6D5", "#FFF8E8", 110, 92, 116, 4,
            2_496, WILLOW_SHARES, 208, TrailTier.GRAND, 98, 170, 160, 0.35F, 0.60F,
            willowProfile(36, 11, 25));
    public static final FireworkStyle AMETHYST_PLATINUM_WILLOW = style(
            25, "amethyst_platinum_willow", "紫晶铂银长垂帘柳烟花", "Amethyst Platinum Long Willow Firework",
            Family.METALLIC, Shape.WILLOW_SPHERE, "#9B59D7", "#D8E4F0", "#FAF7FF", 117, 96, 120, 4,
            2_592, WILLOW_SHARES, 216, TrailTier.GRAND, 100, 180, 170, 0.35F, 0.60F,
            willowProfile(39, 13, 28));

    public static final FireworkStyle LED_SCARLET_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(0));
    public static final FireworkStyle LED_CORAL_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(1));
    public static final FireworkStyle LED_AMBER_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(2));
    public static final FireworkStyle LED_LEMON_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(3));
    public static final FireworkStyle LED_CHARTREUSE_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(4));
    public static final FireworkStyle LED_MINT_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(5));
    public static final FireworkStyle LED_TEAL_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(6));
    public static final FireworkStyle LED_CYAN_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(7));
    public static final FireworkStyle LED_AZURE_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(8));
    public static final FireworkStyle LED_COBALT_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(9));
    public static final FireworkStyle LED_VIOLET_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(10));
    public static final FireworkStyle LED_LILAC_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(11));
    public static final FireworkStyle LED_MAGENTA_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(12));
    public static final FireworkStyle LED_ROSE_SPHERE = ledMonochromeStyle(LED_MONOCHROME_DEFINITIONS.get(13));

    public static final FireworkStyle AMBER_RADIANT_FIREWORK = style(
            40,
            "amber_radiant_firework",
            "琥珀放射烟花",
            "Amber Radiant Firework",
            Family.WARM,
            Shape.RADIANT,
            "#FF6B19",
            "#FFA424",
            "#FFE1A6",
            100,
            108,
            108,
            0,
            4_800,
            RADIANT_SHARES,
            160,
            TrailTier.GRAND,
            62,
            60,
            58,
            0.35F,
            0.60F,
            radiantProfile(3.5D, 48.0D, 0.94D, 0.38D, 0.46D, 9.0D));

    public static final FireworkStyle AMBER_RADIANT_WILLOW_FIREWORK = style(
            41,
            "amber_radiant_willow_firework",
            "琥珀放射长垂柳烟花",
            "Amber Radiant Long Willow Firework",
            Family.WARM,
            Shape.RADIANT_WILLOW,
            "#FF6B19",
            "#FFA424",
            "#FFE1A6",
            100,
            108,
            220,
            0,
            4_800,
            RADIANT_SHARES,
            160,
            TrailTier.GRAND,
            62,
            60,
            58,
            0.35F,
            0.60F,
            radiantWillowProfile(radiantProfile(3.5D, 48.0D, 0.94D, 0.38D, 0.46D, 9.0D)));

    public static final FireworkStyle GIANT_AMBER_RADIANT_FIREWORK = style(
            42,
            "giant_amber_radiant_firework",
            "巨型琥珀放射烟花",
            "Giant Amber Radiant Firework",
            Family.WARM,
            Shape.GIANT_RADIANT,
            "#FF2919",
            "#FF9E1A",
            "#FFF0A8",
            138,
            260,
            260,
            0,
            12_288,
            new LayerShares(167, 667, 166),
            256,
            TrailTier.GRAND,
            116,
            104,
            96,
            0.35F,
            0.60F,
            GiantTier.LARGE);

    public static final FireworkStyle HYBRID_AMBER_SPHERE_RADIANT = style(
            43,
            "hybrid_amber_sphere_radiant",
            "琥珀球形放射结合烟花",
            "Amber Sphere-Radiant Hybrid Firework",
            Family.JEWEL,
            Shape.HYBRID_SPHERE_RADIANT,
            "#FF6B19",
            "#FFA424",
            "#FFE1A6",
            100,
            112,
            112,
            0,
            4_080,
            new LayerShares(470, 410, 120),
            340,
            TrailTier.GRAND,
            102,
            84,
            68,
            0.35F,
            0.60F,
            radiantProfile(3.5D, 48.0D, 0.94D, 0.38D, 0.46D, 9.0D));

    public static final FireworkStyle SATURN_AMBER_DOUBLE_SPHERE = style(
            44,
            "saturn_amber_double_sphere",
            "琥珀双球土星环烟花",
            "Amber Double-Sphere Saturn Ring Firework",
            Family.METALLIC,
            Shape.SATURN,
            "#FF7A1A",
            "#4FD4D0",
            "#FFF0B5",
            110,
            144,
            144,
            0,
            3_040,
            new LayerShares(570, 300, 130),
            192,
            TrailTier.GRAND,
            104,
            88,
            72,
            0.35F,
            0.60F);

    /** The second giant prototype uses the independent EXTRA_LARGE willow queue. */
    public static final FireworkStyle GIANT_GOLDEN_WHITE_RADIAL_WILLOW_FIREWORK = style(
            45,
            "giant_golden_white_radial_willow_firework",
            "\u5de8\u578b\u91d1\u767d\u7eaf\u653e\u5c04\u5782\u67f3\u70df\u82b1",
            "Giant Golden White Radial Willow Firework",
            Family.METALLIC,
            Shape.GIANT_RADIANT,
            "#FFF2C7",
            "#FFD15A",
            "#FFFDF0",
            138,
            260,
            260,
            0,
            12_288,
            new LayerShares(167, 667, 166),
            256,
            TrailTier.GRAND,
            252,
            236,
            236,
            0.35F,
            0.60F,
            GiantTier.EXTRA_LARGE);

    /** The 46 stable pre-expansion styles retain their persisted network indices without renumbering. */
    private static final List<FireworkStyle> BASE_VALUES = List.of(
            GRAND_GOLDEN_SPHERE,
            CINNABAR_AMBER_SPHERE,
            SAFFRON_CORAL_SPHERE,
            RUBY_SOLAR_SPHERE,
            EMBER_CHAMPAGNE_DOUBLE_SPHERE,
            VERMILION_GOLD_DOUBLE_SPHERE,
            CORAL_ROSE_CROWN_SPHERE,
            AMBER_SUNSTONE_WILLOW,
            SCARLET_COPPER_WILLOW,
            AQUA_ICE_SPHERE,
            COBALT_AZURE_SPHERE,
            CYAN_PLATINUM_DOUBLE_SPHERE,
            GLACIER_TEAL_CROWN_SPHERE,
            POLAR_SILVER_WILLOW,
            EMERALD_PERIDOT_SPHERE,
            AMETHYST_ORCHID_SPHERE,
            SAPPHIRE_VIOLET_DOUBLE_SPHERE,
            GARNET_TOPAZ_DOUBLE_SPHERE,
            OPAL_ROSE_CROWN_SPHERE,
            JADE_PEARL_WILLOW,
            CHAMPAGNE_WHITE_GOLD_SPHERE,
            COBALT_TITANIUM_DOUBLE_SPHERE,
            PLATINUM_ONYX_CROWN_SPHERE,
            EMERALD_SILVER_CROWN_SPHERE,
            ROSE_GOLD_PEARL_WILLOW,
            AMETHYST_PLATINUM_WILLOW,
            LED_SCARLET_SPHERE,
            LED_CORAL_SPHERE,
            LED_AMBER_SPHERE,
            LED_LEMON_SPHERE,
            LED_CHARTREUSE_SPHERE,
            LED_MINT_SPHERE,
            LED_TEAL_SPHERE,
            LED_CYAN_SPHERE,
            LED_AZURE_SPHERE,
            LED_COBALT_SPHERE,
            LED_VIOLET_SPHERE,
            LED_LILAC_SPHERE,
            LED_MAGENTA_SPHERE,
            LED_ROSE_SPHERE,
            AMBER_RADIANT_FIREWORK,
            AMBER_RADIANT_WILLOW_FIREWORK,
            GIANT_AMBER_RADIANT_FIREWORK,
            HYBRID_AMBER_SPHERE_RADIANT,
            SATURN_AMBER_DOUBLE_SPHERE,
            GIANT_GOLDEN_WHITE_RADIAL_WILLOW_FIREWORK);
    /** Integrated ordinary and giant styles are append-only so the stable 46 indices remain compatible. */
    private static final List<FireworkStyle> VALUES = appendIntegratedStyles();
    private static final Map<String, FireworkStyle> BY_ID = indexById();

    public FireworkStyle {
        if (index < 0 || id == null || id.isBlank() || flightTicks <= 0 || diameter <= 0 || fullEnvelope <= 0
                || ((shape == Shape.RADIANT_WILLOW ? fullEnvelope > 220
                        : shape == Shape.GIANT_RADIANT ? fullEnvelope > 260
                        : shape == Shape.SATURN ? fullEnvelope > 160 : fullEnvelope > 130))
                || diameter > fullEnvelope || phaseDelayTicks < 0 || totalStarCount <= 0
                || starsPerTick <= 0 || starsPerTick > 340 || outerLifetime <= 0 || innerLifetime <= 0
                || accentLifetime <= 0 || twinkleChanceMin < 0.0F || twinkleChanceMax > 1.0F
                || twinkleChanceMin > twinkleChanceMax) {
            throw new IllegalArgumentException("Invalid firework style " + id);
        }
        if ((shape == Shape.WILLOW_SPHERE) != (willowProfile != null)
                || (shape == Shape.RADIANT || shape == Shape.HYBRID_SPHERE_RADIANT) != (radiantProfile != null)
                || (shape == Shape.RADIANT_WILLOW) != (radiantWillowProfile != null)
                || (shape == Shape.GIANT_RADIANT) != (giantTier != GiantTier.NONE)
                || (shape != Shape.GIANT_RADIANT && giantTier != GiantTier.NONE)
                || (willowProfile != null ? 1 : 0)
                        + (radiantProfile != null ? 1 : 0)
                        + (radiantWillowProfile != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("Branch profiles must match the style shape");
        }
        Objects.requireNonNull(zhName, "zhName");
        Objects.requireNonNull(enName, "enName");
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(primaryColor, "primaryColor");
        Objects.requireNonNull(secondaryColor, "secondaryColor");
        Objects.requireNonNull(accentColor, "accentColor");
        Objects.requireNonNull(layerShares, "layerShares");
        Objects.requireNonNull(trailTier, "trailTier");
        Objects.requireNonNull(giantTier, "giantTier");
    }

    public static List<FireworkStyle> values() {
        return VALUES;
    }

    private static List<FireworkStyle> appendIntegratedStyles() {
        if (BASE_VALUES.size() != NormalFireworkCatalog.FIRST_STYLE_INDEX) {
            throw new IllegalStateException("The normal batch must append after the stable 46-style baseline");
        }
        java.util.ArrayList<FireworkStyle> styles = new java.util.ArrayList<>(
                BASE_VALUES.size() + NormalFireworkCatalog.NEW_ORDINARY_STYLE_COUNT
                        + GiantFireworkCatalog.INTEGRATED_GIANT_COUNT
                        + OtherFireworkCatalog.OTHER_ORDINARY_STYLE_COUNT
                        + OtherExtraFireworkCatalog.OTHER_EXTRA_STYLE_COUNT
                        + MidsizeFireworkCatalog.MIDSIZE_STYLE_COUNT);
        styles.addAll(BASE_VALUES);
        styles.addAll(NormalFireworkCatalog.stylesFrom(BASE_VALUES.size()));
        styles.addAll(GiantFireworkCatalog.stylesFrom(styles.size()));
        styles.addAll(OtherFireworkCatalog.stylesFrom(styles.size()));
        styles.addAll(OtherExtraFireworkCatalog.stylesFrom(styles.size()));
        styles.addAll(MidsizeFireworkCatalog.stylesFrom(styles.size()));
        return List.copyOf(styles);
    }

    public static int count() {
        return VALUES.size();
    }

    /** Returns the immutable non-neutral Urbanforma LED palette used by the LED monochrome sphere family. */
    public static List<LedMonochromeDefinition> ledMonochromeDefinitions() {
        return LED_MONOCHROME_DEFINITIONS;
    }

    /** Returns the golden demonstration style for malformed or pre-series index values. */
    public static FireworkStyle fromIndex(int index) {
        return index >= 0 && index < VALUES.size() ? VALUES.get(index) : GRAND_GOLDEN_SPHERE;
    }

    /** Old v0.2 IDs intentionally have no aliases and therefore resolve to the golden fallback. */
    public static FireworkStyle fromId(String id) {
        return id == null ? GRAND_GOLDEN_SPHERE : BY_ID.getOrDefault(id, GRAND_GOLDEN_SPHERE);
    }

    public int mainStarCount() {
        return this.totalStarCount * this.layerShares.mainPermille() / 1_000;
    }

    public int accentStarCount() {
        return this.totalStarCount * this.layerShares.accentPermille() / 1_000;
    }

    public int secondaryStarCount() {
        return this.totalStarCount - this.mainStarCount() - this.accentStarCount();
    }

    public int trailStarsPerTick() {
        return this.trailTier.starsPerTick();
    }

    public int trailLifetime() {
        return this.trailTier.lifetime();
    }

    public String translationKey() {
        return "item.urbanforma_fireworks." + this.id;
    }

    public EffectCategory effectCategory() {
        if (this.giantTier != GiantTier.NONE) {
            return this.giantTier.effectCategory();
        }
        return this.shape == Shape.RADIANT_WILLOW
                ? EffectCategory.RADIANT_WILLOW
                : EffectCategory.STANDARD;
    }

    public boolean isPrototype() {
        return this.shape == Shape.GIANT_RADIANT
                || this.shape == Shape.HYBRID_SPHERE_RADIANT
                || this.shape == Shape.SATURN;
    }

    private static FireworkStyle style(
            int index,
            String id,
            String zhName,
            String enName,
            Family family,
            Shape shape,
            String primaryColor,
            String secondaryColor,
            String accentColor,
            int flightTicks,
            int diameter,
            int fullEnvelope,
            int phaseDelayTicks,
            int totalStarCount,
            LayerShares layerShares,
            int starsPerTick,
            TrailTier trailTier,
            int outerLifetime,
            int innerLifetime,
            int accentLifetime,
            float twinkleChanceMin,
            float twinkleChanceMax,
            Object... branchProfiles) {
        if (branchProfiles.length > 2) {
            throw new IllegalArgumentException("A firework style may define at most one branch profile and one giant tier");
        }
        WillowProfile willowProfile = null;
        RadiantProfile radiantProfile = null;
        RadiantWillowProfile radiantWillowProfile = null;
        GiantTier giantTier = GiantTier.NONE;
        for (Object profile : branchProfiles) {
            if (profile instanceof GiantTier tier) {
                if (giantTier != GiantTier.NONE) {
                    throw new IllegalArgumentException("A firework style may define only one giant tier");
                }
                giantTier = tier;
                continue;
            }
            if (profile instanceof WillowProfile willow) {
                willowProfile = willow;
            } else if (profile instanceof RadiantProfile radiant) {
                radiantProfile = radiant;
            } else if (profile instanceof RadiantWillowProfile radiantWillow) {
                radiantWillowProfile = radiantWillow;
            } else {
                throw new IllegalArgumentException("Unsupported firework branch profile " + profile);
            }
        }
        return new FireworkStyle(
                index,
                id,
                zhName,
                enName,
                family,
                shape,
                Rgb.fromHex(primaryColor),
                Rgb.fromHex(secondaryColor),
                Rgb.fromHex(accentColor),
                flightTicks,
                diameter,
                fullEnvelope,
                phaseDelayTicks,
                totalStarCount,
                layerShares,
                starsPerTick,
                trailTier,
                outerLifetime,
                innerLifetime,
                accentLifetime,
                twinkleChanceMin,
                twinkleChanceMax,
                willowProfile,
                radiantProfile,
                radiantWillowProfile,
                giantTier);
    }

    private static WillowProfile willowProfile(int horizontalReach, int rise, int drop) {
        return new WillowProfile(horizontalReach, rise, drop);
    }

    private static RadiantProfile radiantProfile(
            double initialRadius,
            double maximumRadius,
            double verticalScale,
            double bendStartMin,
            double bendStartMax,
            double terminalDrop) {
        return new RadiantProfile(
                initialRadius, maximumRadius, verticalScale, bendStartMin, bendStartMax, terminalDrop);
    }

    private static RadiantWillowProfile radiantWillowProfile(RadiantProfile radiantProfile) {
        return new RadiantWillowProfile(radiantProfile, 100, 140, 18.0D, 0.28D, 0.42D, 66.0D, 7.5D);
    }

    private static LedMonochromeDefinition ledMonochrome(
            int index,
            String id,
            String zhName,
            String enName,
            String ledReferenceColor,
            String primaryColor,
            String secondaryColor,
            String accentColor) {
        return new LedMonochromeDefinition(
                index,
                id,
                zhName,
                enName,
                Rgb.fromHex(ledReferenceColor),
                Rgb.fromHex(primaryColor),
                Rgb.fromHex(secondaryColor),
                Rgb.fromHex(accentColor));
    }

    private static FireworkStyle ledMonochromeStyle(LedMonochromeDefinition definition) {
        return new FireworkStyle(
                definition.index(),
                definition.id(),
                definition.zhName(),
                definition.enName(),
                Family.LED_MONOCHROME,
                Shape.SPHERE,
                definition.primaryColor(),
                definition.secondaryColor(),
                definition.accentColor(),
                84,
                96,
                96,
                0,
                2_160,
                SPHERE_SHARES,
                180,
                TrailTier.STANDARD,
                90,
                78,
                66,
                0.35F,
                0.60F,
                null,
                null,
                null,
                GiantTier.NONE);
    }

    private static Map<String, FireworkStyle> indexById() {
        Map<String, FireworkStyle> stylesById = new HashMap<>();
        for (int expectedIndex = 0; expectedIndex < VALUES.size(); expectedIndex++) {
            FireworkStyle style = VALUES.get(expectedIndex);
            if (style.index != expectedIndex || stylesById.put(style.id, style) != null) {
                throw new IllegalStateException("Firework style indices and ids must be unique and stable");
            }
        }
        return Map.copyOf(stylesById);
    }
}
