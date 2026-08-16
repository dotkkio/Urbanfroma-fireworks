package com.urbanforma.fireworks.content.midsize.radial;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Typed, unregistered integration contract for one member of the second medium radial batch.
 *
 * <p>This type deliberately carries no style index, registry holder, language-map write, recipe file, creative-tab
 * mutation, packet, server particle loop, or shared scheduler. The integration owner maps these records after the
 * independently owned medium/small category is available.</p>
 */
public record MidsizeRadialFireworkDefinition(
        String id,
        String zhName,
        String enName,
        MidsizeRadialTrajectory.Path path,
        Palette palette,
        RecipeContract recipe,
        CreativeTarget creativeTarget,
        ReuseContract reuseContract,
        ParticlePlan particlePlan,
        Boundary boundary,
        VisualSignature visualSignature,
        boolean countsTowardCoolColorBudget) {
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final String ITEM_MODEL = "minecraft:item/firework_rocket";
    public static final String HD_FIREWORK_SPARK = "urbanforma_fireworks:hd_firework_spark";
    public static final String FUTURE_CATEGORY = "medium_small_fireworks";
    public static final String FUTURE_SUBSECTION = "radial";
    public static final String ORDER_GROUP = "midsize_radial_second_batch";
    private static final Pattern STABLE_ID = Pattern.compile("midsize_radial_[a-z0-9_]+_firework");

    public MidsizeRadialFireworkDefinition {
        if (id == null || !STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Medium radial ids must use stable lower-snake identifiers");
        }
        requireText(zhName, "zhName");
        requireText(enName, "enName");
        if (!zhName.startsWith("\u4e2d\u578b") || !enName.startsWith("Medium ")) {
            throw new IllegalArgumentException("Display names must use only the Medium label");
        }
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(creativeTarget, "creativeTarget");
        Objects.requireNonNull(reuseContract, "reuseContract");
        Objects.requireNonNull(particlePlan, "particlePlan");
        Objects.requireNonNull(boundary, "boundary");
        Objects.requireNonNull(visualSignature, "visualSignature");
        if (!recipe.resultId().equals(MOD_ID + ":" + id)
                || !creativeTarget.futureCategory().equals(FUTURE_CATEGORY)
                || !creativeTarget.subsection().equals(FUTURE_SUBSECTION)
                || !creativeTarget.orderGroup().equals(ORDER_GROUP)
                || !reuseContract.itemModel().equals(ITEM_MODEL)
                || !reuseContract.particleType().equals(HD_FIREWORK_SPARK)
                || particlePlan.path() != path
                || particlePlan.totalParticles() != path.totalParticles()
                || particlePlan.localPeakParticlesPerTick() != path.maxParticlesPerTick()
                || particlePlan.maxOwnedParticles() != path.totalParticles()
                || particlePlan.minimumLifetimeTicks() != path.minimumLifetimeTicks()
                || particlePlan.maximumLifetimeTicks() != path.maximumLifetimeTicks()
                || particlePlan.totalVisualTicks() != path.totalVisualTicks()
                || !boundary.pathId().equals(path.pathId())
                || !boundary.proofContract().equals("MidsizeRadialTrajectory.staticContractHolds(" + path.name() + ", seed)")) {
            throw new IllegalArgumentException("Medium radial definition fields are not mutually consistent");
        }
    }

    public record Rgb(float red, float green, float blue) {
        public Rgb {
            validateChannel(red, "red");
            validateChannel(green, "green");
            validateChannel(blue, "blue");
        }

        public static Rgb fromHex(String value) {
            if (value == null || !value.matches("#[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("Palette colors must be #RRGGBB");
            }
            int packed = Integer.parseInt(value.substring(1), 16);
            return new Rgb(
                    ((packed >>> 16) & 0xFF) / 255.0F,
                    ((packed >>> 8) & 0xFF) / 255.0F,
                    (packed & 0xFF) / 255.0F);
        }

        private static void validateChannel(float value, String name) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(name + " must be between zero and one");
            }
        }
    }

    public record Palette(Rgb primary, Rgb secondary, Rgb accent) {
        public Palette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Medium radial palettes require three distinct colors");
            }
        }

        public Rgb colorFor(MidsizeRadialTrajectory.ColorBand band) {
            return switch (Objects.requireNonNull(band, "band")) {
                case CORE -> primary;
                case BODY -> secondary;
                case EDGE -> accent;
            };
        }
    }

    /** Metadata-only mirror of the shared ordinary rocket recipe. No data file is written by this batch. */
    public record RecipeContract(
            List<String> pattern,
            Map<String, String> key,
            String resultId,
            int count,
            boolean generatesRecipeFile,
            String coordinatorDataPath) {
        public RecipeContract {
            pattern = List.copyOf(Objects.requireNonNull(pattern, "pattern"));
            key = Map.copyOf(Objects.requireNonNull(key, "key"));
            if (!pattern.equals(List.of(" P ", "FGF", " P "))
                    || !key.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || resultId == null || resultId.isBlank() || count != 1 || generatesRecipeFile
                    || coordinatorDataPath == null
                    || !coordinatorDataPath.matches(
                            "data/urbanforma_fireworks/recipes/midsize_radial_[a-z0-9_]+_firework\\.json")) {
                throw new IllegalArgumentException("Medium radial recipes must remain explicit integration metadata");
            }
        }
    }

    /** Future category location only. The shared creative-section owner creates that category separately. */
    public record CreativeTarget(
            String futureCategory,
            String subsection,
            String orderGroup,
            boolean coordinatorOwnsSharedClassification) {
        public CreativeTarget {
            if (!FUTURE_CATEGORY.equals(futureCategory) || !FUTURE_SUBSECTION.equals(subsection)
                    || !ORDER_GROUP.equals(orderGroup) || !coordinatorOwnsSharedClassification) {
                throw new IllegalArgumentException("Medium radial creative target must remain an integration hand-off");
            }
        }
    }

    /** Reuses the existing rocket model and HD spark without adding assets, registry entries, or loaders. */
    public record ReuseContract(
            String itemModel,
            String particleType,
            String geometryClass,
            String clientProgramClass,
            boolean createsModelJson,
            boolean createsTexture,
            boolean createsParticleType,
            boolean createsCustomLoader) {
        public ReuseContract {
            if (!ITEM_MODEL.equals(itemModel) || !HD_FIREWORK_SPARK.equals(particleType)
                    || !"com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory".equals(geometryClass)
                    || !"com.urbanforma.fireworks.client.midsize.radial.MidsizeRadialClientProgram".equals(clientProgramClass)
                    || createsModelJson || createsTexture || createsParticleType || createsCustomLoader) {
                throw new IllegalArgumentException("Medium radial effects must reuse the declared shared contracts");
            }
        }
    }

    /** Per-instance finite budget. None of these fields reserve or hide behind an old shared particle cap. */
    public record ParticlePlan(
            MidsizeRadialTrajectory.Path path,
            int totalParticles,
            int localPeakParticlesPerTick,
            int maxOwnedParticles,
            int minimumLifetimeTicks,
            int maximumLifetimeTicks,
            int totalVisualTicks,
            boolean emitsParticlesOnClient,
            boolean createsServerParticleLoop,
            boolean calculatesServerTrajectory,
            boolean createsNetworkPayload,
            boolean createsGlobalLimiter,
            boolean dependsOnSharedParticleLimit) {
        public ParticlePlan {
            Objects.requireNonNull(path, "path");
            if (totalParticles <= 0 || totalParticles != path.totalParticles()
                    || localPeakParticlesPerTick <= 0 || localPeakParticlesPerTick != path.maxParticlesPerTick()
                    || localPeakParticlesPerTick > MidsizeRadialTrajectory.LOCAL_MAX_PARTICLES_PER_TICK
                    || maxOwnedParticles != totalParticles || minimumLifetimeTicks != path.minimumLifetimeTicks()
                    || maximumLifetimeTicks != path.maximumLifetimeTicks()
                    || totalVisualTicks != path.totalVisualTicks() || !emitsParticlesOnClient
                    || createsServerParticleLoop || calculatesServerTrajectory || createsNetworkPayload
                    || createsGlobalLimiter || dependsOnSharedParticleLimit) {
                throw new IllegalArgumentException("Medium radial particle plan must stay finite and client-local");
            }
        }
    }

    /** Shared large-radial reference plus the approved medium size, height, and local envelope. */
    public record Boundary(
            String pathId,
            String referenceEffectId,
            int referenceTotalParticles,
            double particleRatio,
            double referenceFullEnvelopeBlocks,
            double fullEnvelopeBlocks,
            double maximumRadius,
            int referenceAscentTicks,
            int ascentTicks,
            double referenceDetonationHeight,
            double detonationHeight,
            double heightRatio,
            String proofContract) {
        public Boundary {
            requireText(pathId, "pathId");
            if (!MidsizeRadialTrajectory.REFERENCE_EFFECT_ID.equals(referenceEffectId)
                    || referenceTotalParticles != MidsizeRadialTrajectory.REFERENCE_TOTAL_PARTICLES
                    || particleRatio < 0.79D || particleRatio > 0.85D
                    || referenceFullEnvelopeBlocks != MidsizeRadialTrajectory.REFERENCE_FULL_ENVELOPE
                    || fullEnvelopeBlocks != MidsizeRadialTrajectory.APPROVED_FULL_ENVELOPE
                    || maximumRadius <= 0.0D || maximumRadius > MidsizeRadialTrajectory.MAX_RADIUS
                    || referenceAscentTicks != MidsizeRadialTrajectory.REFERENCE_ASCENT_TICKS
                    || ascentTicks != MidsizeRadialTrajectory.ASCENT_TICKS
                    || referenceDetonationHeight != MidsizeRadialTrajectory.REFERENCE_DETONATION_HEIGHT
                    || detonationHeight != MidsizeRadialTrajectory.DETONATION_HEIGHT
                    || heightRatio < 0.80D || heightRatio > 0.90D
                    || proofContract == null || proofContract.isBlank()) {
                throw new IllegalArgumentException("Medium radial boundary contract drifted");
            }
        }
    }

    /** Six-axis structural record. The catalog rejects duplicate and adjacent palette-plus-structure signatures. */
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
            requireText(shape, "shape");
            requireText(core, "core");
            requireText(trail, "trail");
            requireText(layering, "layering");
            requireText(cadence, "cadence");
            requireText(density, "density");
            requireText(paletteToken, "paletteToken");
            requireText(visualDifference, "visualDifference");
            structuralAxes = Set.copyOf(Objects.requireNonNull(structuralAxes, "structuralAxes"));
            if (!structuralAxes.containsAll(EnumSet.allOf(VisualAxis.class))) {
                throw new IllegalArgumentException("Every medium radial signature needs all six visual axes");
            }
        }

        public String structuralSignature() {
            return String.join("|", shape, core, trail, layering, cadence, density);
        }
    }

    public enum VisualAxis {
        SHAPE,
        CORE,
        TRAIL,
        LAYERING,
        CADENCE,
        DENSITY
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
