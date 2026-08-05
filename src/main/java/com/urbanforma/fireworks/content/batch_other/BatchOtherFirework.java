package com.urbanforma.fireworks.content.batch_other;

import com.urbanforma.fireworks.content.FireworkStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable common-side contract for one ordinary batch_other firework. */
public record BatchOtherFirework(
        String id,
        String zhName,
        String enName,
        EffectType effectType,
        String effectPath,
        String clientProgram,
        Family family,
        Palette palette,
        StyleParameters style,
        VisualDifference visualDifference,
        RecipeContract recipe,
        CreativeContract creative,
        ModelContract model,
        ParticleContract particle,
        ExpectedBoundary expectedBoundary) {

    /** Kept only so the currently frozen shared adapter can compile until its integration pass is applied. */
    public enum EffectType {
        SPHERE,
        DOUBLE_SPHERE,
        CROWN_SPHERE
    }

    /** Legacy shared-shape hint; the actual route is {@link #clientProgram()} and {@link #effectPath()}. */
    public enum ProgramRoute {
        RADIAL_STRAIGHT("radial_straight", "RADIANT", EffectType.SPHERE),
        SPARSE_LONG_RAYS("sparse_long_rays", "RADIANT", EffectType.SPHERE),
        OFFSET_DOUBLE_RING("offset_double_ring", "SATURN", EffectType.DOUBLE_SPHERE),
        THREE_CONCENTRIC_RINGS("three_concentric_rings", "SATURN", EffectType.DOUBLE_SPHERE),
        RING_CORE_HYBRID("ring_core_hybrid", "HYBRID_SPHERE_RADIANT", EffectType.DOUBLE_SPHERE),
        DROOPING_TAILS("drooping_tails", "RADIANT_WILLOW", EffectType.CROWN_SPHERE),
        LAYERED_WILLOW("layered_willow", "RADIANT_WILLOW", EffectType.CROWN_SPHERE),
        HELICAL_RADIATION("helical_radiation", "RADIANT", EffectType.SPHERE),
        ALTERNATING_PULSES("alternating_pulses", "RADIANT", EffectType.SPHERE),
        THICK_MULTILAYER_RAYS("thick_multilayer_rays", "RADIANT", EffectType.DOUBLE_SPHERE),
        DELAYED_CORE_SHELL("delayed_core_shell", "HYBRID_SPHERE_RADIANT", EffectType.DOUBLE_SPHERE),
        ORBITAL_SATURN("orbital_saturn", "SATURN", EffectType.CROWN_SPHERE),
        TWIN_CROSS_ORBITS("twin_cross_orbits", "SATURN", EffectType.DOUBLE_SPHERE),
        SEGMENTED_RAYS("segmented_rays", "RADIANT", EffectType.SPHERE),
        COLOR_SHIFT_BEADS("color_shift_beads", "COLOR_CHANGE", EffectType.SPHERE);

        private final String id;
        private final String sharedEffectPath;
        private final EffectType compatibilityShape;

        ProgramRoute(String id, String sharedEffectPath, EffectType compatibilityShape) {
            this.id = id;
            this.sharedEffectPath = sharedEffectPath;
            this.compatibilityShape = compatibilityShape;
        }

        public String id() {
            return this.id;
        }

        public String sharedEffectPath() {
            return this.sharedEffectPath;
        }

        public EffectType compatibilityShape() {
            return this.compatibilityShape;
        }

        public String clientProgramId() {
            return "batch_other:" + this.id;
        }
    }

    public enum Family {
        WARM,
        JEWEL,
        METALLIC,
        COOL
    }

    public enum TrailTier {
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

    public record Palette(
            String primaryHex,
            String secondaryHex,
            String accentHex,
            boolean consumesCoolColorQuota) {
        public Palette {
            FireworkStyle.Rgb primary = FireworkStyle.Rgb.fromHex(primaryHex);
            FireworkStyle.Rgb secondary = FireworkStyle.Rgb.fromHex(secondaryHex);
            FireworkStyle.Rgb accent = FireworkStyle.Rgb.fromHex(accentHex);
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("batch_other palette layers must be distinct");
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

    public record StyleParameters(
            int flightTicks,
            int diameterBlocks,
            int fullEnvelopeBlocks,
            int phaseDelayTicks,
            int totalStarCount,
            int mainLayerPermille,
            int secondaryLayerPermille,
            int accentLayerPermille,
            int starsPerTick,
            TrailTier trailTier,
            int outerLifetime,
            int innerLifetime,
            int accentLifetime,
            float twinkleChanceMin,
            float twinkleChanceMax) {
        public StyleParameters {
            if (flightTicks <= 0 || flightTicks > 120 || diameterBlocks <= 0 || fullEnvelopeBlocks <= 0
                    || diameterBlocks > fullEnvelopeBlocks || fullEnvelopeBlocks > 120
                    || phaseDelayTicks < 0 || totalStarCount <= 0 || totalStarCount > 15_000
                    || starsPerTick <= 0 || starsPerTick > 720 || mainLayerPermille < 0
                    || secondaryLayerPermille < 0 || accentLayerPermille < 0
                    || mainLayerPermille + secondaryLayerPermille + accentLayerPermille != 1_000
                    || outerLifetime <= 0 || outerLifetime > 720 || innerLifetime <= 0 || innerLifetime > 720
                    || accentLifetime <= 0 || accentLifetime > 720
                    || twinkleChanceMin < 0.0F || twinkleChanceMax > 1.0F
                    || twinkleChanceMin > twinkleChanceMax) {
                throw new IllegalArgumentException("Invalid batch_other ordinary style parameters");
            }
            Objects.requireNonNull(trailTier, "trailTier");
        }
    }

    public record VisualDifference(
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            List<String> structuralAxes,
            String structureSignature,
            String description) {
        public VisualDifference {
            if (shape == null || shape.isBlank() || core == null || core.isBlank()
                    || trail == null || trail.isBlank() || layering == null || layering.isBlank()
                    || cadence == null || cadence.isBlank() || density == null || density.isBlank()
                    || structuralAxes == null || structuralAxes.isEmpty()
                    || structureSignature == null || structureSignature.isBlank()
                    || description == null || description.isBlank()) {
                throw new IllegalArgumentException("Every batch_other entry needs a complete visual difference");
            }
            structuralAxes = List.copyOf(structuralAxes);
            if (!structuralAxes.equals(List.of("SHAPE", "CORE", "TRAIL", "LAYERING", "CADENCE", "DENSITY"))) {
                throw new IllegalArgumentException("Unknown batch_other structural axis");
            }
        }

        public String computedSignature() {
            return String.join("|", this.shape, this.core, this.trail, this.layering, this.cadence, this.density);
        }
    }

    public record RecipeContract(
            String template,
            List<String> pattern,
            Map<String, String> ingredients,
            String result,
            int count) {
        public RecipeContract {
            Objects.requireNonNull(template, "template");
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(ingredients, "ingredients");
            Objects.requireNonNull(result, "result");
            if (!template.equals("normal_firework_rocket_3x3")
                    || !pattern.equals(List.of(" P ", "FGF", " P "))
                    || !ingredients.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || !result.startsWith("urbanforma_fireworks:other_") || count != 1) {
                throw new IllegalArgumentException("batch_other recipes must reuse the approved normal template");
            }
        }
    }

    public record CreativeContract(String sectionKey, String orderGroup, int stableOrder) {
        public CreativeContract {
            Objects.requireNonNull(sectionKey, "sectionKey");
            Objects.requireNonNull(orderGroup, "orderGroup");
            if (!sectionKey.startsWith("gui.urbanforma_fireworks.section.fireworks.")
                    || !orderGroup.equals("other") || stableOrder < 1 || stableOrder > 15) {
                throw new IllegalArgumentException("Invalid batch_other creative contract");
            }
        }
    }

    public record ModelContract(String parent, String pathTemplate, String reuseContract) {
        public ModelContract {
            if (!parent.equals("minecraft:item/firework_rocket")
                    || !pathTemplate.equals("assets/urbanforma_fireworks/models/item/{id}.json")
                    || !reuseContract.equals("vanilla_firework_rocket")) {
                throw new IllegalArgumentException("batch_other must reuse the vanilla rocket model contract");
            }
        }
    }

    public record ParticleContract(
            String particleType,
            String engine,
            String category,
            int peakPerBurst) {
        public ParticleContract {
            if (!particleType.equals("urbanforma_fireworks:hd_firework_spark")
                    || !engine.equals("GrandFireworkClientEffects.ActiveBurst")
                    || !category.equals("STANDARD") || peakPerBurst <= 0 || peakPerBurst > 15_000) {
                throw new IllegalArgumentException("Invalid batch_other particle contract");
            }
        }
    }

    public record ExpectedBoundary(
            int nominalDiameterBlocks,
            int fullEnvelopeBlocks,
            int ordinaryMaximumBlocks,
            String proofContract) {
        public ExpectedBoundary {
            if (nominalDiameterBlocks <= 0 || fullEnvelopeBlocks < nominalDiameterBlocks
                    || fullEnvelopeBlocks > ordinaryMaximumBlocks || ordinaryMaximumBlocks != 120
                    || proofContract == null || proofContract.isBlank()) {
                throw new IllegalArgumentException("Invalid batch_other ordinary envelope");
            }
        }
    }
}
