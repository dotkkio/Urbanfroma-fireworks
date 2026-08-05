package com.urbanforma.fireworks.content.batch05;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.FireworkStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The eighteen ordinary, unregistered cross-series candidates owned by batch05.
 *
 * <p>No entry contains a global numeric style index. All catalogue entries retain their declared model, particle,
 * recipe, section, budget, and boundary contracts so the integration owner can attach them without guessing.</p>
 */
public final class Batch05FireworkCatalog {
    public static final String BATCH_ID = "batch05";
    public static final int REQUIRED_ENTRY_COUNT = 18;
    /** Existing ordinary baseline already consumes one of the twenty cool-color slots. */
    public static final int EXISTING_COOL_COLOR_BASELINE = 1;
    public static final int COOL_COLOR_BUDGET_USE = 2;
    public static final int COOL_COLOR_ADDITIONAL_CAP = 20 - EXISTING_COOL_COLOR_BASELINE;

    private static final String ITEM_MODEL = "minecraft:item/firework_rocket";
    private static final String HD_FIREWORK_SPARK = "urbanforma_fireworks:hd_firework_spark";
    private static final String ORDER_GROUP = "batch05_cross_series";

    private static final List<Batch05FireworkDefinition> VALUES = List.of(
            radiant(
                    "batch05_saffron_sunburst_radiant",
                    "藏红日耀放射烟花",
                    "Saffron Sunburst Radiant Firework",
                    "#FF8A05", "#FFD23A", "#FFF1B8", false),
            radiant(
                    "batch05_cinnabar_comet_radiant",
                    "朱砂彗尾放射烟花",
                    "Cinnabar Comet Radiant Firework",
                    "#E33824", "#FF6A22", "#FFD6A0", false),
            radiant(
                    "batch05_amethyst_orbit_radiant",
                    "紫晶环辉放射烟花",
                    "Amethyst Orbit Radiant Firework",
                    "#8D49D8", "#C46BFF", "#FFE1FF", false),
            radiant(
                    "batch05_platinum_aurora_radiant",
                    "铂金极辉放射烟花",
                    "Platinum Aurora Radiant Firework",
                    "#DDE6EE", "#FFE4A3", "#FFFFFF", false),
            radiantWillow(
                    "batch05_copper_sunset_radiant_willow",
                    "赤铜晚霞放射垂柳烟花",
                    "Copper Sunset Radiant Willow Firework",
                    "#C8502B", "#FF9A3D", "#FFE0A0", false),
            radiantWillow(
                    "batch05_rose_champagne_radiant_willow",
                    "玫瑰香槟放射垂柳烟花",
                    "Rose Champagne Radiant Willow Firework",
                    "#E36673", "#F8C77D", "#FFF4D5", false),
            radiantWillow(
                    "batch05_orchid_moon_radiant_willow",
                    "兰紫月辉放射垂柳烟花",
                    "Orchid Moon Radiant Willow Firework",
                    "#9B52D5", "#D6A3FF", "#FFF0FF", false),
            hybrid(
                    "batch05_solar_amber_hybrid",
                    "日曜琥珀球放射结合烟花",
                    "Solar Amber Sphere-Radiant Hybrid Firework",
                    "#FFB414", "#FF6B1A", "#FFF1A6", false, null),
            hybrid(
                    "batch05_coral_flare_hybrid",
                    "珊瑚耀焰球放射结合烟花",
                    "Coral Flare Sphere-Radiant Hybrid Firework",
                    "#FF5A50", "#FF9865", "#FFE4C6", false, null),
            hybrid(
                    "batch05_gilded_platinum_hybrid",
                    "鎏金铂辉球放射结合烟花",
                    "Gilded Platinum Sphere-Radiant Hybrid Firework",
                    "#D6A145", "#F1E6D2", "#FFF9C9", false, null),
            hybrid(
                    "batch05_teal_opal_hybrid",
                    "青绿欧泊球放射结合烟花",
                    "Teal Opal Sphere-Radiant Hybrid Firework",
                    "#0FB6B0", "#46DBD3", "#E1FFFF", true, null),
            saturn(
                    "batch05_copper_crown_saturn",
                    "赤铜冠冕土星环烟花",
                    "Copper Crown Saturn Ring Firework",
                    "#C45B32", "#E8A44B", "#FFF0B4", false),
            saturn(
                    "batch05_rose_garnet_saturn",
                    "玫瑰石榴土星环烟花",
                    "Rose Garnet Saturn Ring Firework",
                    "#D95768", "#A91F3B", "#FFE1C0", false),
            saturn(
                    "batch05_violet_opal_saturn",
                    "紫罗兰欧泊土星环烟花",
                    "Violet Opal Saturn Ring Firework",
                    "#7641BF", "#C57AFF", "#FFE7FF", false),
            saturn(
                    "batch05_aqua_platinum_saturn",
                    "碧青铂金土星环烟花",
                    "Aqua Platinum Saturn Ring Firework",
                    "#1FAEC1", "#9CECF5", "#F2FFFF", true),
            radiant(
                    "batch05_ember_twilight_radiant",
                    "余烬暮色变色放射烟花",
                    "Ember Twilight Color-Shift Radiant Firework",
                    "#FF5A18", "#FF9E34", "#FFE0A8", false,
                    colorChange("#6A46D8", "#B379FF", "#F1DDFF", 10)),
            radiantWillow(
                    "batch05_sunset_orchid_willow",
                    "晚霞兰紫变色垂柳烟花",
                    "Sunset Orchid Color-Shift Radiant Willow Firework",
                    "#F06A3D", "#FFBD6D", "#FFE8C5", false,
                    colorChange("#9D55D8", "#D4A4FF", "#FFF1FF", 12)),
            hybrid(
                    "batch05_aurora_pearl_hybrid",
                    "极光珍珠变色球放射结合烟花",
                    "Aurora Pearl Color-Shift Sphere-Radiant Hybrid Firework",
                    "#FFC441", "#FF874A", "#FFECCE", false,
                    colorChange("#2CBED0", "#6BE7E8", "#E4FFFF", 9)));

    private static final Map<String, Batch05FireworkDefinition> BY_ID = indexById();

    static {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("batch05 must contain exactly eighteen ordinary fireworks");
        }
        int coolColorUse = (int) VALUES.stream().filter(Batch05FireworkDefinition::countsTowardCoolColorBudget).count();
        if (coolColorUse != COOL_COLOR_BUDGET_USE || coolColorUse > COOL_COLOR_ADDITIONAL_CAP) {
            throw new IllegalStateException("batch05 cool-color budget is not within the approved cap");
        }
        Batch05VisualSignatures.validateAdjacent(VALUES);
    }

    private Batch05FireworkCatalog() {
    }

    public static List<Batch05FireworkDefinition> values() {
        return VALUES;
    }

    public static Batch05FireworkDefinition byId(String id) {
        return BY_ID.get(id);
    }

    private static Batch05FireworkDefinition radiant(
            String id, String zhName, String enName, String primary, String secondary, String accent, boolean cool) {
        return radiant(id, zhName, enName, primary, secondary, accent, cool, null);
    }

    private static Batch05FireworkDefinition radiant(
            String id,
            String zhName,
            String enName,
            String primary,
            String secondary,
            String accent,
            boolean cool,
            Batch05FireworkDefinition.ColorChangeSpec colorChange) {
        return definition(
                id, zhName, enName, Batch05FireworkDefinition.EffectType.RADIANT,
                primary, secondary, accent, cool, colorChange,
                "RadiantTrajectory: 160 branches x 30 segments; fixed 160-node emission frame",
                "GrandFireworkClientEffects.RadiantEffect",
                108.0D,
                "RadiantTrajectory.fitsEnvelope(profile, seed, 108.0D)");
    }

    private static Batch05FireworkDefinition radiantWillow(
            String id, String zhName, String enName, String primary, String secondary, String accent, boolean cool) {
        return radiantWillow(id, zhName, enName, primary, secondary, accent, cool, null);
    }

    private static Batch05FireworkDefinition radiantWillow(
            String id,
            String zhName,
            String enName,
            String primary,
            String secondary,
            String accent,
            boolean cool,
            Batch05FireworkDefinition.ColorChangeSpec colorChange) {
        return definition(
                id, zhName, enName, Batch05FireworkDefinition.EffectType.RADIANT_WILLOW,
                primary, secondary, accent, cool, colorChange,
                "RadiantWillowTrajectory: retained radiant nodes only; no second-stage allocations",
                "GrandFireworkClientEffects.RadiantWillowEffect",
                220.0D,
                "RadiantWillowTrajectory.fitsEnvelope(profile, seed, duration); category cap remains 3");
    }

    private static Batch05FireworkDefinition hybrid(
            String id,
            String zhName,
            String enName,
            String primary,
            String secondary,
            String accent,
            boolean cool,
            Batch05FireworkDefinition.ColorChangeSpec colorChange) {
        return definition(
                id, zhName, enName, Batch05FireworkDefinition.EffectType.HYBRID_SPHERE_RADIANT,
                primary, secondary, accent, cool, colorChange,
                "HybridSphereRadiantTrajectory: 2,160 sphere nodes plus 1,920 shared radiant nodes",
                "HybridSphereRadiantParticleProgram",
                112.0D,
                "HybridSphereRadiantTrajectory.fitsEnvelope(profile, seed)");
    }

    private static Batch05FireworkDefinition saturn(
            String id, String zhName, String enName, String primary, String secondary, String accent, boolean cool) {
        return definition(
                id, zhName, enName, Batch05FireworkDefinition.EffectType.SATURN,
                primary, secondary, accent, cool, null,
                "Batch05EffectProfiles.saturnProgram: bounded sphere and inclined ring samples",
                "SaturnClientPlan",
                144.0D,
                "SaturnGeometry.conservativeBounds().fitsWithin(144.0D)");
    }

    private static Batch05FireworkDefinition definition(
            String id,
            String zhName,
            String enName,
            Batch05FireworkDefinition.EffectType effectType,
            String primary,
            String secondary,
            String accent,
            boolean cool,
            Batch05FireworkDefinition.ColorChangeSpec colorChange,
            String geometryContract,
            String clientProgram,
            double fullEnvelopeBlocks,
            String proofContract) {
        return new Batch05FireworkDefinition(
                id,
                zhName,
                enName,
                effectType,
                palette(primary, secondary, accent),
                new Batch05FireworkDefinition.RecipeSpec(
                        List.of(" P ", "FGF", " P "),
                        Map.of(
                                "P", "minecraft:paper",
                                "F", "minecraft:firework_star",
                                "G", "minecraft:gunpowder"),
                        "urbanforma_fireworks:" + id,
                        1),
                new Batch05FireworkDefinition.CreativeTarget(effectType.creativeSection(), ORDER_GROUP),
                new Batch05FireworkDefinition.ReuseContract(
                        ITEM_MODEL, HD_FIREWORK_SPARK, geometryContract, clientProgram),
                new Batch05FireworkDefinition.ParticlePlan(effectType.effectCategory()),
                new Batch05FireworkDefinition.ExpectedBoundary(fullEnvelopeBlocks, proofContract),
                Batch05VisualSignatures.forId(id),
                colorChange,
                cool);
    }

    private static Batch05FireworkDefinition.Palette palette(String primary, String secondary, String accent) {
        return new Batch05FireworkDefinition.Palette(
                FireworkStyle.Rgb.fromHex(primary),
                FireworkStyle.Rgb.fromHex(secondary),
                FireworkStyle.Rgb.fromHex(accent));
    }

    private static Batch05FireworkDefinition.ColorChangeSpec colorChange(
            String targetPrimary, String targetSecondary, String targetAccent, int switchDelayTicks) {
        return new Batch05FireworkDefinition.ColorChangeSpec(
                palette(targetPrimary, targetSecondary, targetAccent), switchDelayTicks);
    }

    private static Map<String, Batch05FireworkDefinition> indexById() {
        Map<String, Batch05FireworkDefinition> definitions = new HashMap<>();
        for (Batch05FireworkDefinition definition : VALUES) {
            if (definitions.put(definition.id(), definition) != null) {
                throw new IllegalStateException("Duplicate batch05 firework id " + definition.id());
            }
        }
        return Map.copyOf(definitions);
    }
}
