package com.urbanforma.fireworks.content.batch04;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.saturn.SaturnGeometry;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import java.util.List;
import java.util.Objects;

/**
 * Isolated catalog entry for the batch04 ordinary Saturn-ring family.
 *
 * <p>This record intentionally has no registry, item, recipe, language, or client-event ownership. The integration
 * owner consumes its stable id and contracts when it is ready to wire the shared surfaces.</p>
 */
public record Batch04SaturnFirework(
        String id,
        String zhName,
        String enName,
        EffectType effectType,
        RingTopology ringTopology,
        FireworkStyle.Rgb primaryColor,
        FireworkStyle.Rgb secondaryColor,
        FireworkStyle.Rgb accentColor,
        ColorFamily colorFamily,
        VisualDifference visualDifference,
        RecipeFields recipe,
        String creativeTarget,
        ModelContract modelContract,
        ParticleContract particleContract,
        Boundary boundary,
        SaturnProgram program) {
    public static final int ORDINARY_MAX_ENVELOPE_BLOCKS = 160;

    public Batch04SaturnFirework {
        if (id == null || !id.matches("batch04_saturn_[a-z0-9_]+")) {
            throw new IllegalArgumentException("Batch04 Saturn ids must use the batch04_saturn_ prefix");
        }
        requireText(zhName, "zhName");
        requireText(enName, "enName");
        Objects.requireNonNull(effectType, "effectType");
        Objects.requireNonNull(ringTopology, "ringTopology");
        Objects.requireNonNull(primaryColor, "primaryColor");
        Objects.requireNonNull(secondaryColor, "secondaryColor");
        Objects.requireNonNull(accentColor, "accentColor");
        if (primaryColor.equals(secondaryColor) || primaryColor.equals(accentColor) || secondaryColor.equals(accentColor)) {
            throw new IllegalArgumentException("Batch04 Saturn palettes require three distinct colors");
        }
        Objects.requireNonNull(colorFamily, "colorFamily");
        boolean containsColdColor = isColdColor(primaryColor) || isColdColor(secondaryColor) || isColdColor(accentColor);
        if (containsColdColor != colorFamily.consumesColdColorQuota()) {
            throw new IllegalArgumentException("Batch04 cold-color classification must include every palette accent");
        }
        Objects.requireNonNull(visualDifference, "visualDifference");
        Objects.requireNonNull(recipe, "recipe");
        requireText(creativeTarget, "creativeTarget");
        if (!creativeTarget.equals("urbanforma_fireworks:fireworks/saturn")) {
            throw new IllegalArgumentException("Batch04 Saturn entries must target the existing Saturn creative section");
        }
        Objects.requireNonNull(modelContract, "modelContract");
        Objects.requireNonNull(particleContract, "particleContract");
        Objects.requireNonNull(boundary, "boundary");
        Objects.requireNonNull(program, "program");
        if (program.spheres().size() != effectType.sphereLayerCount()) {
            throw new IllegalArgumentException("Sphere layer count does not match the declared batch04 effect type");
        }
        if (program.rings().ringCount() != ringTopology.ringCount()) {
            throw new IllegalArgumentException("Ring count does not match the declared batch04 topology");
        }
        if (particleContract.effectCategory() != EffectCategory.STANDARD
                || particleContract.createsParticleType()
                || particleContract.altersRadiantWillowLimit()) {
            throw new IllegalArgumentException("Batch04 Saturn effects must reuse the ordinary particle contract");
        }
        if (program.budget().maxPerTick() != particleContract.maxPerTick()
                || program.budget().maxOwnedParticles() != particleContract.maxOwnedParticles()) {
            throw new IllegalArgumentException("Program and manifest particle budgets must agree");
        }
        SaturnGeometry.Bounds conservativeBounds = new SaturnGeometry(program).conservativeBounds();
        if (!conservativeBounds.fitsWithin(boundary.maxEnvelopeBlocks())) {
            throw new IllegalArgumentException("Batch04 Saturn program exceeds its declared ordinary envelope");
        }
    }

    public enum EffectType {
        SINGLE_SPHERE_SATURN(1),
        DOUBLE_SPHERE_SATURN(2),
        MULTI_LAYER_SPHERE_SATURN(3);

        private final int sphereLayerCount;

        EffectType(int sphereLayerCount) {
            this.sphereLayerCount = sphereLayerCount;
        }

        public int sphereLayerCount() {
            return this.sphereLayerCount;
        }
    }

    /** Batch04 explicitly classifies every palette for the ordinary-series cold-color budget. */
    public enum ColorFamily {
        WARM(false),
        PURPLE(false),
        COLD(true);

        private final boolean consumesColdColorQuota;

        ColorFamily(boolean consumesColdColorQuota) {
            this.consumesColdColorQuota = consumesColdColorQuota;
        }

        public boolean consumesColdColorQuota() {
            return this.consumesColdColorQuota;
        }
    }

    /** A non-color visual distinction required for every ordinary batch entry. */
    public enum VisualAxis {
        SHAPE,
        CORE,
        TRAIL,
        LAYERING,
        CADENCE,
        DENSITY
    }

    /** Each named topology has one exact ring count and a distinct geometry in {@link Batch04SaturnCatalog}. */
    public enum RingTopology {
        SINGLE_EQUATORIAL(1),
        TWIN_TILTED(2),
        CROSSED_PAIR(2),
        WIDE_EQUATORIAL(1),
        NARROW_BELT(1),
        OFFSET_TWIN(2),
        CROWNED_TRIPLE(3),
        LATTICE_FOUR(4),
        SPLIT_DOUBLE(2),
        DIAGONAL_TRIPLE(3),
        TRIPLE_CONCENTRIC(3),
        DUAL_OFFSET(2),
        CROSS_QUAD(4),
        NESTED_TRIPLE(3),
        FIVE_BAND(5),
        POLAR_RING_PAIR(2),
        ORBITAL_CROWN(3),
        WIDE_CROSS(2),
        CASCADING_QUAD(4),
        CROWNED_FIVE(5);

        private final int ringCount;

        RingTopology(int ringCount) {
            this.ringCount = ringCount;
        }

        public int ringCount() {
            return this.ringCount;
        }
    }

    /** Human-readable, non-color art direction plus at least one visual structure axis. */
    public record VisualDifference(String description, List<VisualAxis> structuralAxes) {
        public VisualDifference {
            requireText(description, "visualDifference.description");
            structuralAxes = List.copyOf(structuralAxes);
            if (structuralAxes.isEmpty()) {
                throw new IllegalArgumentException("Batch04 visual differences must include a non-color structure axis");
            }
            for (VisualAxis axis : structuralAxes) {
                Objects.requireNonNull(axis, "visualDifference.structuralAxis");
            }
        }
    }

    /** Exact crafting fields for later shared recipe generation; no recipe is registered by this isolated batch. */
    public record RecipeFields(String pigmentItem, List<String> pattern, List<Ingredient> ingredients) {
        public RecipeFields {
            requireText(pigmentItem, "pigmentItem");
            pattern = List.copyOf(pattern);
            ingredients = List.copyOf(ingredients);
            if (!pattern.equals(List.of("PDP", "FSF", " G ")) || ingredients.size() != 5) {
                throw new IllegalArgumentException("Batch04 Saturn recipe must use the approved five-field pattern");
            }
            boolean hasPigment = false;
            for (Ingredient ingredient : ingredients) {
                Objects.requireNonNull(ingredient, "ingredient");
                hasPigment |= ingredient.symbol().equals("D") && ingredient.itemId().equals(pigmentItem);
            }
            if (!hasPigment) {
                throw new IllegalArgumentException("Batch04 Saturn recipe pigment must occupy the D field");
            }
        }
    }

    public record Ingredient(String symbol, String itemId) {
        public Ingredient {
            if (symbol == null || !symbol.matches("[PDFSG]") || itemId == null || !itemId.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+")) {
                throw new IllegalArgumentException("Invalid batch04 Saturn recipe ingredient");
            }
        }
    }

    /** Model parent is intentionally reused; per-item model JSON stays a shared integration responsibility. */
    public record ModelContract(String id, String parent) {
        public ModelContract {
            requireText(id, "id");
            if (!"minecraft:item/firework_rocket".equals(parent)) {
                throw new IllegalArgumentException("Batch04 Saturn entries must reuse the vanilla firework rocket model");
            }
        }
    }

    /** Only vanilla FIREWORK particles and the pre-existing Saturn client plan are permitted for this batch. */
    public record ParticleContract(
            String particleType,
            String clientPlan,
            EffectCategory effectCategory,
            int maxPerTick,
            int maxOwnedParticles,
            boolean createsParticleType,
            boolean altersRadiantWillowLimit) {
        public ParticleContract {
            if (!"minecraft:firework".equals(particleType) || !"SaturnClientPlan".equals(clientPlan)
                    || effectCategory != EffectCategory.STANDARD || maxPerTick <= 0
                    || maxOwnedParticles < maxPerTick || createsParticleType || altersRadiantWillowLimit) {
                throw new IllegalArgumentException("Invalid ordinary batch04 Saturn particle contract");
            }
        }
    }

    /** Conservative local AABB diameter supplied to integration and kept below the ordinary Saturn limit. */
    public record Boundary(int maxEnvelopeBlocks) {
        public Boundary {
            if (maxEnvelopeBlocks <= 0 || maxEnvelopeBlocks > ORDINARY_MAX_ENVELOPE_BLOCKS) {
                throw new IllegalArgumentException("Batch04 Saturn boundary must remain ordinary-sized");
            }
        }
    }

    public boolean consumesColdColorQuota() {
        return this.colorFamily.consumesColdColorQuota();
    }

    /** Used by the catalog to prevent neighboring rows from becoming a color-only variant. */
    public String structureSignature() {
        return this.effectType + ":" + this.ringTopology + ":" + this.visualDifference.structuralAxes();
    }

    /** Exact RGB signature independent of localized names or later item-registration names. */
    public String paletteSignature() {
        return rgbSignature(this.primaryColor) + "/" + rgbSignature(this.secondaryColor) + "/" + rgbSignature(this.accentColor);
    }

    private static String rgbSignature(FireworkStyle.Rgb color) {
        return Math.round(color.red() * 255.0F)
                + "-" + Math.round(color.green() * 255.0F)
                + "-" + Math.round(color.blue() * 255.0F);
    }

    /** Green/cyan/blue hue range used for the ordinary-series quota; neutral and purple colors are excluded. */
    private static boolean isColdColor(FireworkStyle.Rgb color) {
        float red = color.red();
        float green = color.green();
        float blue = color.blue();
        float maximum = Math.max(red, Math.max(green, blue));
        float minimum = Math.min(red, Math.min(green, blue));
        float chroma = maximum - minimum;
        if (chroma < 0.08F) {
            return false;
        }
        float hue = maximum == red
                ? 60.0F * (((green - blue) / chroma + 6.0F) % 6.0F)
                : maximum == green
                        ? 60.0F * ((blue - red) / chroma + 2.0F)
                        : 60.0F * ((red - green) / chroma + 4.0F);
        return hue >= 150.0F && hue <= 250.0F;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
