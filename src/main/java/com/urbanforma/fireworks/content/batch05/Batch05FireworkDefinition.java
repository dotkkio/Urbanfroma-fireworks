package com.urbanforma.fireworks.content.batch05;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.colorchange.ColorChangeBallProgram;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Integration-ready, unregistered description of one ordinary batch05 firework.
 *
 * <p>This type deliberately carries no global style index, registry holder, language-map write, or creative-tab
 * mutation. The integration owner maps these records to those shared surfaces after resolving every concurrent
 * batch.</p>
 */
public record Batch05FireworkDefinition(
        String id,
        String zhName,
        String enName,
        EffectType effectType,
        Palette palette,
        RecipeSpec recipe,
        CreativeTarget creativeTarget,
        ReuseContract reuseContract,
        ParticlePlan particlePlan,
        ExpectedBoundary expectedBoundary,
        VisualSignature visualSignature,
        ColorChangeSpec colorChange,
        boolean countsTowardCoolColorBudget) {
    private static final Pattern STABLE_ID = Pattern.compile("batch05_[a-z0-9_]+");

    public Batch05FireworkDefinition {
        if (id == null || !STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("batch05 ids must be stable lower-snake identifiers");
        }
        if (zhName == null || zhName.isBlank() || enName == null || enName.isBlank()) {
            throw new IllegalArgumentException("batch05 names must not be blank");
        }
        Objects.requireNonNull(effectType, "effectType");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(creativeTarget, "creativeTarget");
        Objects.requireNonNull(reuseContract, "reuseContract");
        Objects.requireNonNull(particlePlan, "particlePlan");
        Objects.requireNonNull(expectedBoundary, "expectedBoundary");
        Objects.requireNonNull(visualSignature, "visualSignature");
        if (!recipe.resultId().equals("urbanforma_fireworks:" + id)) {
            throw new IllegalArgumentException("Recipe result must match the firework id");
        }
        if (particlePlan.category() != effectType.effectCategory()) {
            throw new IllegalArgumentException("Particle category must match the effect type");
        }
        if (expectedBoundary.fullEnvelopeBlocks() > effectType.maximumEnvelopeBlocks()) {
            throw new IllegalArgumentException("Effect exceeds its approved bounded envelope");
        }
        if (colorChange != null && !effectType.supportsColorChange()) {
            throw new IllegalArgumentException("Color-change capability must augment a supported ordinary effect");
        }
    }

    public enum EffectType {
        RADIANT("radiant", EffectCategory.STANDARD, 108.0D, true),
        RADIANT_WILLOW("radiant_willow", EffectCategory.RADIANT_WILLOW, 220.0D, true),
        HYBRID_SPHERE_RADIANT("hybrid", EffectCategory.STANDARD, 112.0D, true),
        SATURN("saturn", EffectCategory.STANDARD, 144.0D, false);

        private final String creativeSection;
        private final EffectCategory effectCategory;
        private final double maximumEnvelopeBlocks;
        private final boolean supportsColorChange;

        EffectType(
                String creativeSection,
                EffectCategory effectCategory,
                double maximumEnvelopeBlocks,
                boolean supportsColorChange) {
            this.creativeSection = creativeSection;
            this.effectCategory = effectCategory;
            this.maximumEnvelopeBlocks = maximumEnvelopeBlocks;
            this.supportsColorChange = supportsColorChange;
        }

        public String creativeSection() {
            return creativeSection;
        }

        public EffectCategory effectCategory() {
            return effectCategory;
        }

        public double maximumEnvelopeBlocks() {
            return maximumEnvelopeBlocks;
        }

        public boolean supportsColorChange() {
            return supportsColorChange;
        }
    }

    public record Palette(
            FireworkStyle.Rgb primary,
            FireworkStyle.Rgb secondary,
            FireworkStyle.Rgb accent) {
        public Palette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Each batch05 palette needs three distinct colors");
            }
        }

        public ColorChangeBallProgram.Palette asColorChangePalette() {
            return new ColorChangeBallProgram.Palette(primary, secondary, accent);
        }
    }

    /** Mirrors the approved existing rocket template without creating a recipe file in this isolated batch. */
    public record RecipeSpec(List<String> pattern, Map<String, String> key, String resultId, int count) {
        public RecipeSpec {
            pattern = List.copyOf(pattern);
            key = Map.copyOf(key);
            if (!pattern.equals(List.of(" P ", "FGF", " P "))
                    || !key.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || resultId == null
                    || resultId.isBlank()
                    || count != 1) {
                throw new IllegalArgumentException("batch05 must use the approved ordinary rocket recipe template");
            }
        }
    }

    /** Target section and a stable append-only grouping hint for the shared creative-section owner. */
    public record CreativeTarget(String section, String orderGroup) {
        public CreativeTarget {
            if (section == null || section.isBlank() || orderGroup == null || orderGroup.isBlank()) {
                throw new IllegalArgumentException("Creative target must be explicit");
            }
        }
    }

    /** Reused model, particle type, and unregistered client program contract. */
    public record ReuseContract(
            String itemModel,
            String particleType,
            String geometryContract,
            String clientProgram) {
        public ReuseContract {
            if (itemModel == null || itemModel.isBlank()
                    || particleType == null || particleType.isBlank()
                    || geometryContract == null || geometryContract.isBlank()
                    || clientProgram == null || clientProgram.isBlank()) {
                throw new IllegalArgumentException("Reuse contracts must name every shared dependency");
            }
        }
    }

    /** Routes a finite, style-defined client frame without reserving a shared particle quota or queue slot. */
    public record ParticlePlan(EffectCategory category) {
        public ParticlePlan {
            Objects.requireNonNull(category, "category");
        }
    }

    /** Bounded local visual envelope that the integration path must retain. */
    public record ExpectedBoundary(double fullEnvelopeBlocks, String proofContract) {
        public ExpectedBoundary {
            if (!Double.isFinite(fullEnvelopeBlocks) || fullEnvelopeBlocks <= 0.0D
                    || proofContract == null || proofContract.isBlank()) {
                throw new IllegalArgumentException("Expected boundary must be finite and documented");
            }
        }
    }

    /** Structural/temporal cue that prevents a palette-only entry from entering the batch. */
    public record VisualSignature(
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            String paletteToken,
            String visualDifference,
            Set<VisualAxis> structuralAxes) {
        public VisualSignature {
            if (shape == null || shape.isBlank() || core == null || core.isBlank()
                    || trail == null || trail.isBlank() || layering == null || layering.isBlank()
                    || cadence == null || cadence.isBlank() || density == null || density.isBlank()
                    || paletteToken == null || paletteToken.isBlank()
                    || visualDifference == null || visualDifference.isBlank()
                    || structuralAxes == null || structuralAxes.isEmpty()) {
                throw new IllegalArgumentException("Each batch05 entry needs an independent visual signature");
            }
            structuralAxes = Set.copyOf(structuralAxes);
        }

        public String structuralSignature() {
            return String.join("|", shape, core, trail, layering, cadence, density);
        }

        public enum VisualAxis {
            SHAPE,
            CORE,
            TRAIL,
            LAYERING,
            CADENCE,
            DENSITY
        }
    }

    /** Optional one-shot color transition applied only to particles already emitted by the base effect. */
    public record ColorChangeSpec(Palette targetPalette, int switchDelayTicks) {
        public ColorChangeSpec {
            Objects.requireNonNull(targetPalette, "targetPalette");
            ColorChangeBallProgram.validateSwitchDelayTicks(switchDelayTicks);
        }

        public ColorChangeBallProgram.Profile profileFor(Palette initialPalette) {
            Objects.requireNonNull(initialPalette, "initialPalette");
            return new ColorChangeBallProgram.Profile(
                    initialPalette.asColorChangePalette(),
                    targetPalette.asColorChangePalette(),
                    switchDelayTicks);
        }
    }
}
