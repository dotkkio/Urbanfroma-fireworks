package com.urbanforma.fireworks.content.batch03;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.hybrid.HybridSphereRadiantTrajectory;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Integration-ready source catalog for the ordinary batch03 sphere-plus-radiant family.
 *
 * <p>This catalog owns no registry index, event listener, language aggregate, recipe aggregate, or scheduler.
 * The integration thread can map each definition into the shared style and item systems without changing this
 * batch's stable identifiers or its fixed hybrid particle contract.</p>
 */
public final class Batch03SphereRadiantCatalog {
    public static final String BATCH_ID = "batch03";
    public static final int EXPECTED_SIZE = 20;
    public static final String EFFECT_TYPE = "sphere_radiant_hybrid";
    public static final String MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final String PARTICLE_TYPE = "urbanforma_fireworks:hd_firework_spark";
    public static final String CREATIVE_TARGET =
            "gui.urbanforma_fireworks.section.fireworks.hybrid";
    public static final String COLOR_CHANGE_POLICY =
            "existing_capability_only; no independent batch03 series";
    /** The existing Saturn teal entry occupies one of the ordinary green/blue/cyan color slots. */
    public static final int ORDINARY_COLD_COLOR_LIMIT = 20;
    public static final int ORDINARY_COLD_COLOR_BASELINE_OCCUPIED = 1;
    public static final int REMAINING_COLD_COLOR_ENTRIES_FOR_NEW_BATCHES =
            ORDINARY_COLD_COLOR_LIMIT - ORDINARY_COLD_COLOR_BASELINE_OCCUPIED;
    /** Batch03 deliberately leaves all nineteen newly available cold-color slots to the wider production plan. */
    public static final int BATCH03_COLD_COLOR_ENTRY_COUNT = 0;

    /** The v0.3.0 radiant geometry accepted by the existing hybrid prototype. */
    public static final FireworkStyle.RadiantProfile RADIAL_PROFILE =
            new FireworkStyle.RadiantProfile(3.5D, 48.0D, 0.94D, 0.38D, 0.46D, 9.0D);

    public static final ParticleContract PARTICLE_CONTRACT = new ParticleContract(
            PARTICLE_TYPE,
            HybridSphereRadiantTrajectory.SPHERE_OUTER_COUNT,
            HybridSphereRadiantTrajectory.SPHERE_CORE_COUNT,
            HybridSphereRadiantTrajectory.RADIAL_BRANCH_COUNT,
            HybridSphereRadiantTrajectory.RADIAL_RING_COUNT,
            HybridSphereRadiantTrajectory.TOTAL_EMISSION_TICKS,
            HybridSphereRadiantTrajectory.maxEmissionPerTick(),
            HybridSphereRadiantTrajectory.MAX_LIVE_PARTICLES);

    public static final ExpectedBoundary EXPECTED_BOUNDARY = new ExpectedBoundary(
            HybridSphereRadiantTrajectory.SPHERE_CORE_RADIUS,
            HybridSphereRadiantTrajectory.SPHERE_OUTER_RADIUS,
            HybridSphereRadiantTrajectory.RADIAL_OUTER_ENVELOPE_RADIUS,
            HybridSphereRadiantTrajectory.APPROVED_FULL_ENVELOPE);

    public static final List<Definition> DEFINITIONS = List.of(
            definition(1, "hybrid_crimson_crown_radiant", "朱红金冠球放射烟花", "Crimson Gold-Crown Sphere-Radiant Firework", "#E31B23", "#F7B32B", "#FFF2C6", visual("crown_shell", "pinpoint_gold_core", "short_sunray_tail", "outer crown around an amber hub", "simultaneous flare", "dense rim")),
            definition(2, "hybrid_amber_annular_radiant", "琥珀环层球放射烟花", "Amber Annular Sphere-Radiant Firework", "#F47C20", "#F9C74F", "#FFF4CA", visual("annular_shell", "pearl_core", "segmented_ray_tail", "concentric bands expand from a pearl center", "core-led aperture", "medium dense")),
            definition(3, "hybrid_saffron_sunburst_radiant", "藏红日芒球放射烟花", "Saffron Sunburst Sphere-Radiant Firework", "#FF8C00", "#FFD23F", "#FFF7D1", visual("sunburst_shell", "solar_disc_core", "needle_ray_tail", "two-stage radial halo", "fast outer release", "open high density")),
            definition(4, "hybrid_coral_champagne_radiant", "珊瑚香槟球放射烟花", "Coral Champagne Sphere-Radiant Firework", "#FF6F61", "#F6BD60", "#FFF0D8", visual("champagne_bubble_shell", "warm_white_core", "soft_comet_tail", "wide shell then pale inner ring", "slow inner reveal", "airy")),
            definition(5, "hybrid_vermilion_gold_radiant", "朱砂鎏金球放射烟花", "Vermilion Gold Sphere-Radiant Firework", "#D72631", "#F9C74F", "#FFF6D5", visual("gilded_chrysanthemum_shell", "ember_core", "forked_gold_tail", "crimson shell above gold stamen", "staggered bloom", "dense")),
            definition(6, "hybrid_ruby_peony_radiant", "红宝牡丹球放射烟花", "Ruby Peony Sphere-Radiant Firework", "#C91F37", "#FF9F1C", "#FFE8B0", visual("peony_shell", "ruby_bead_core", "curved_petal_tail", "round peony shell with a tight core", "pulse outward", "medium")),
            definition(7, "hybrid_mandarin_pearl_radiant", "橘霞珍珠球放射烟花", "Mandarin Pearl Sphere-Radiant Firework", "#F15A24", "#FFD6A5", "#FFF4E0", visual("lantern_shell", "pearl_seed_core", "draped_spark_tail", "orange shell framed by a pearl center", "soft delayed center", "medium")),
            definition(8, "hybrid_copper_rose_radiant", "赤铜玫瑰球放射烟花", "Copper Rose Sphere-Radiant Firework", "#B7410E", "#E86A92", "#FFE5C2", visual("rose_shell", "copper_nucleus", "twist_ray_tail", "rose ring around a copper heat core", "spiral release", "compact")),
            definition(9, "hybrid_garnet_lantern_radiant", "石榴灯火球放射烟花", "Garnet Lantern Sphere-Radiant Firework", "#A4161A", "#F4A261", "#FFE7BA", visual("lantern_burst_shell", "garnet_core", "long_stamen_tail", "dark red bowl with amber spokes", "core-to-shell", "dense")),
            definition(10, "hybrid_persimmon_spark_radiant", "柿橙星火球放射烟花", "Persimmon Spark Sphere-Radiant Firework", "#F05D23", "#FFB347", "#FFF2D6", visual("persimmon_ring_shell", "star_seed_core", "flicker_tail", "orange ring with scattered inner sparks", "staccato wave", "open")),
            definition(11, "hybrid_topaz_solar_radiant", "黄玉日耀球放射烟花", "Topaz Solar Sphere-Radiant Firework", "#D97706", "#FBBF24", "#FFF8D0", visual("topaz_halo_shell", "sun_core", "long_straight_tail", "yellow halo with a white inner disc", "broad crescendo", "medium")),
            definition(12, "hybrid_magenta_orchid_radiant", "洋红兰辉球放射烟花", "Magenta Orchid Sphere-Radiant Firework", "#D6288A", "#F77FBE", "#FFE0F4", visual("orchid_shell", "pink_lantern_core", "feather_tail", "pink petals beneath a magenta outer spray", "double pulse", "medium")),
            definition(13, "hybrid_violet_gold_radiant", "紫罗金辉球放射烟花", "Violet Gold Sphere-Radiant Firework", "#6F2DBD", "#F6C453", "#FFF0C8", visual("violet_crown_shell", "gold_pip_core", "diamond_tail", "violet crown over a gold core", "inward-to-outward", "open")),
            definition(14, "hybrid_amethyst_sun_radiant", "紫晶日轮球放射烟花", "Amethyst Sun Sphere-Radiant Firework", "#8B3FD1", "#FFB703", "#FFF1B8", visual("amethyst_orb_shell", "solar_core", "tapered_ray_tail", "purple orb surrounded by gold rays", "late radiant extension", "dense")),
            definition(15, "hybrid_rose_silver_radiant", "玫瑰银辉球放射烟花", "Rose Silver Sphere-Radiant Firework", "#E76F8A", "#E8DED4", "#FFF4F1", visual("rose_silver_shell", "ivory_core", "fine_silver_tail", "rose shell around warm ivory", "soft overlap", "airy")),
            definition(16, "hybrid_cherry_gold_radiant", "樱绯鎏金球放射烟花", "Cherry Gold Sphere-Radiant Firework", "#D81B60", "#F4B400", "#FFF1C2", visual("cherry_cascade_shell", "gold_bead_core", "split_tail", "cherry petals breaking into gold branches", "quick split", "dense")),
            definition(17, "hybrid_cinnabar_ivory_radiant", "朱砂象牙球放射烟花", "Cinnabar Ivory Sphere-Radiant Firework", "#C1121F", "#EAD7B7", "#FFF8E8", visual("cinnabar_lantern_shell", "ivory_lamp_core", "diverging_ray_tail", "cinnabar envelope with an ivory lamp center", "center hold then radial release", "compact")),
            definition(18, "hybrid_honey_orange_radiant", "蜂蜜橙辉球放射烟花", "Honey Orange Sphere-Radiant Firework", "#F59E0B", "#F97316", "#FFF5CF", visual("honey_orange_shell", "amber_core", "comet_ray_tail", "honey core inside an orange outer wave", "rising sweep", "medium")),
            definition(19, "hybrid_peach_copper_radiant", "桃霞铜辉球放射烟花", "Peach Copper Sphere-Radiant Firework", "#FF8C78", "#C65D27", "#FFE3C2", visual("peach_copper_shell", "peach_pearl_core", "ribbon_tail", "peach outer bloom with copper edge", "two-step bloom", "open")),
            definition(20, "hybrid_flame_opal_radiant", "焰彩欧泊球放射烟花", "Flame Opal Sphere-Radiant Firework", "#EF476F", "#E9C46A", "#FFF4D8", visual("opal_bloom_shell", "cream_fire_core", "sparkling_tail", "pink opal disc with a gold rim", "final burst release", "dense")));

    private static final Map<String, Definition> BY_ID = indexById();

    static {
        validateCatalog();
    }

    private Batch03SphereRadiantCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static Definition byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    /** Geometry, particle, and resource facts the integrator must preserve for every item. */
    public record ParticleContract(
            String particleType,
            int sphereOuterCount,
            int sphereCoreCount,
            int radialBranchCount,
            int radialRingCount,
            int emissionTicks,
            int frameNodeCount,
            int totalNodeCount) {
        public ParticleContract {
            Objects.requireNonNull(particleType, "particleType");
            if (sphereOuterCount <= 0 || sphereCoreCount <= 0 || radialBranchCount <= 0
                    || radialRingCount <= 0 || emissionTicks <= 0 || frameNodeCount <= 0
                    || totalNodeCount < frameNodeCount) {
                throw new IllegalArgumentException("Invalid batch03 particle contract");
            }
        }
    }

    public record ExpectedBoundary(
            double sphereCoreRadius,
            double sphereOuterRadius,
            double radialEnvelopeRadius,
            double fullEnvelope) {
        public ExpectedBoundary {
            if (!Double.isFinite(sphereCoreRadius) || !Double.isFinite(sphereOuterRadius)
                    || !Double.isFinite(radialEnvelopeRadius) || !Double.isFinite(fullEnvelope)
                    || sphereCoreRadius <= 0.0D || sphereOuterRadius <= sphereCoreRadius
                    || radialEnvelopeRadius <= sphereOuterRadius || fullEnvelope <= 0.0D) {
                throw new IllegalArgumentException("Invalid batch03 boundary contract");
            }
        }
    }

    public record Palette(String primaryHex, String secondaryHex, String accentHex) {
        public Palette {
            FireworkStyle.Rgb primary = FireworkStyle.Rgb.fromHex(primaryHex);
            FireworkStyle.Rgb secondary = FireworkStyle.Rgb.fromHex(secondaryHex);
            FireworkStyle.Rgb accent = FireworkStyle.Rgb.fromHex(accentHex);
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Batch03 palette layers must be distinct");
            }
        }

        public FireworkStyle.Rgb primary() {
            return FireworkStyle.Rgb.fromHex(this.primaryHex);
        }

        public FireworkStyle.Rgb secondary() {
            return FireworkStyle.Rgb.fromHex(this.secondaryHex);
        }

        public FireworkStyle.Rgb accent() {
            return FireworkStyle.Rgb.fromHex(this.accentHex);
        }
    }

    public record RecipeContract(
            String recipeId,
            String type,
            List<String> pattern,
            Map<String, String> ingredients) {
        public RecipeContract {
            Objects.requireNonNull(recipeId, "recipeId");
            Objects.requireNonNull(type, "type");
            if (pattern.size() != 3 || !pattern.equals(List.of(" P ", "FGF", " P "))
                    || !ingredients.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))) {
                throw new IllegalArgumentException("Batch03 recipes must use the approved hybrid template");
            }
        }
    }

    /**
     * Required non-palette difference for an individual ordinary firework. The integrator must retain at least
     * one listed distinction in the registered visual program; all six fields are present to prevent a color-only
     * catalog expansion.
     */
    public enum StructuralAxis {
        SHAPE,
        CORE,
        TRAIL,
        LAYERING,
        CADENCE,
        DENSITY
    }

    public record VisualDifference(
            String form,
            String core,
            String tail,
            String layering,
            String rhythm,
            String density) {
        public VisualDifference {
            if (isBlank(form) || isBlank(core) || isBlank(tail) || isBlank(layering)
                    || isBlank(rhythm) || isBlank(density)) {
                throw new IllegalArgumentException("Batch03 visual variation fields must all be explicit");
            }
        }

        public String signature() {
            return String.join("|", this.form, this.core, this.tail, this.layering, this.rhythm, this.density);
        }

        public Set<StructuralAxis> structuralAxes() {
            return Set.of(
                    StructuralAxis.SHAPE,
                    StructuralAxis.CORE,
                    StructuralAxis.TRAIL,
                    StructuralAxis.LAYERING,
                    StructuralAxis.CADENCE,
                    StructuralAxis.DENSITY);
        }
    }

    public record Definition(
            int batchSequence,
            String id,
            String zhName,
            String enName,
            String effectType,
            FireworkStyle.Shape shape,
            EffectCategory effectCategory,
            Palette palette,
            boolean containsGreenBlueCyan,
            VisualDifference visualDifference,
            RecipeContract recipe,
            String modelParent,
            String creativeTarget,
            ParticleContract particleContract,
            ExpectedBoundary expectedBoundary) {
        public Definition {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(zhName, "zhName");
            Objects.requireNonNull(enName, "enName");
            Objects.requireNonNull(effectType, "effectType");
            Objects.requireNonNull(shape, "shape");
            Objects.requireNonNull(effectCategory, "effectCategory");
            Objects.requireNonNull(palette, "palette");
            Objects.requireNonNull(visualDifference, "visualDifference");
            Objects.requireNonNull(recipe, "recipe");
            Objects.requireNonNull(modelParent, "modelParent");
            Objects.requireNonNull(creativeTarget, "creativeTarget");
            Objects.requireNonNull(particleContract, "particleContract");
            Objects.requireNonNull(expectedBoundary, "expectedBoundary");
            if (batchSequence <= 0 || id.isBlank() || zhName.isBlank() || enName.isBlank()
                    || !id.matches("hybrid_[a-z0-9_]+_radiant")
                    || !recipe.recipeId().equals(id)) {
                throw new IllegalArgumentException("Invalid batch03 definition " + id);
            }
        }
    }

    private static Definition definition(
            int sequence,
            String id,
            String zhName,
            String enName,
            String primary,
            String secondary,
            String accent,
            VisualDifference visualDifference) {
        return new Definition(
                sequence,
                id,
                zhName,
                enName,
                EFFECT_TYPE,
                FireworkStyle.Shape.HYBRID_SPHERE_RADIANT,
                EffectCategory.STANDARD,
                new Palette(primary, secondary, accent),
                false,
                visualDifference,
                new RecipeContract(
                        id,
                        "minecraft:crafting_shaped",
                        List.of(" P ", "FGF", " P "),
                        Map.of(
                                "P", "minecraft:paper",
                                "F", "minecraft:firework_star",
                                "G", "minecraft:gunpowder")),
                MODEL_PARENT,
                CREATIVE_TARGET,
                PARTICLE_CONTRACT,
                EXPECTED_BOUNDARY);
    }

    private static VisualDifference visual(
            String form,
            String core,
            String tail,
            String layering,
            String rhythm,
            String density) {
        return new VisualDifference(form, core, tail, layering, rhythm, density);
    }

    private static Map<String, Definition> indexById() {
        return DEFINITIONS.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Definition::id,
                definition -> definition));
    }

    private static void validateCatalog() {
        if (DEFINITIONS.size() != EXPECTED_SIZE
                || ORDINARY_COLD_COLOR_BASELINE_OCCUPIED + BATCH03_COLD_COLOR_ENTRY_COUNT
                        > ORDINARY_COLD_COLOR_LIMIT) {
            throw new IllegalStateException("Batch03 size or cold-color catalog contract is invalid");
        }
        Set<String> ids = new HashSet<>();
        Set<String> visualForms = new HashSet<>();
        Set<String> visualSignatures = new HashSet<>();
        int coldColorEntries = 0;
        Definition previous = null;
        for (int index = 0; index < DEFINITIONS.size(); index++) {
            Definition definition = DEFINITIONS.get(index);
            coldColorEntries += definition.containsGreenBlueCyan() ? 1 : 0;
            if (definition.batchSequence() != index + 1
                    || !ids.add(definition.id())
                    || !definition.effectType().equals(EFFECT_TYPE)
                    || definition.shape() != FireworkStyle.Shape.HYBRID_SPHERE_RADIANT
                    || definition.effectCategory() != EffectCategory.STANDARD
                    || !definition.modelParent().equals(MODEL_PARENT)
                    || !definition.creativeTarget().equals(CREATIVE_TARGET)
                    || definition.particleContract() != PARTICLE_CONTRACT
                    || definition.expectedBoundary() != EXPECTED_BOUNDARY
                    || definition.visualDifference().structuralAxes().isEmpty()
                    || !visualForms.add(definition.visualDifference().form())
                    || !visualSignatures.add(definition.visualDifference().signature())
                    || (previous != null
                            && previous.visualDifference().form().equals(definition.visualDifference().form())
                            && previous.palette().equals(definition.palette()))) {
                throw new IllegalStateException("Batch03 definition contract drift: " + definition.id());
            }
            previous = definition;
        }
        if (coldColorEntries != BATCH03_COLD_COLOR_ENTRY_COUNT) {
            throw new IllegalStateException("Batch03 cold-color accounting drift");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
