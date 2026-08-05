package com.urbanforma.fireworks.content.midsize;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Typed, unregistered integration contract for one midsize trial firework.
 *
 * <p>No field here mutates a global style index, registry, language map, creative section, recipe data, network
 * handler, or client scheduler. The coordinator maps this contract to those shared surfaces after accepting the
 * two-prototype trial.</p>
 */
public record MidsizeFireworkDefinition(
        String id,
        String zhName,
        String enName,
        EffectType effectType,
        Palette palette,
        RecipeContract recipe,
        CreativeTarget creativeTarget,
        ModelContract model,
        EffectPath effectPath,
        ParticlePlan particlePlan,
        Boundary boundary,
        VisualSignature visualSignature) {
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final String ITEM_MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final String VANILLA_FIREWORK_PARTICLE = "minecraft:firework";
    public static final String ORDER_GROUP = "midsize_trial";
    private static final Pattern STABLE_ID = Pattern.compile("midsize_dense_(sphere|radial)_firework");

    public MidsizeFireworkDefinition {
        if (id == null || !STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Midsize ids must be the two approved stable identifiers");
        }
        requireText(zhName, "zhName");
        requireText(enName, "enName");
        Objects.requireNonNull(effectType, "effectType");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(creativeTarget, "creativeTarget");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(effectPath, "effectPath");
        Objects.requireNonNull(particlePlan, "particlePlan");
        Objects.requireNonNull(boundary, "boundary");
        Objects.requireNonNull(visualSignature, "visualSignature");

        if (!recipe.resultId().equals(MOD_ID + ":" + id)
                || !creativeTarget.section().equals(effectType.creativeSection())
                || !creativeTarget.orderGroup().equals(ORDER_GROUP)
                || !effectPath.trajectoryClass().equals(effectType.trajectoryClass())
                || !effectPath.clientProgramClass().equals(effectType.clientProgramClass())
                || !particlePlan.trajectoryClass().equals(effectType.trajectoryClass())
                || !particlePlan.particleType().equals(VANILLA_FIREWORK_PARTICLE)
                || particlePlan.totalParticles() <= 0
                || particlePlan.totalParticles() != particlePlan.particlesPerTick() * particlePlan.emissionTicks()
                || Math.abs(((double) particlePlan.totalParticles() / boundary.referenceTotalParticles())
                        - boundary.particleRatio()) > 1.0E-12D
                || !boundary.referenceEffectId().equals(effectType.referenceEffectId())) {
            throw new IllegalArgumentException("Midsize definition fields are not mutually consistent");
        }
    }

    public enum EffectType {
        DENSE_SPHERE(
                "sphere",
                MidsizeDenseSphereTrajectory.REFERENCE_EFFECT_ID,
                "com.urbanforma.fireworks.content.midsize.MidsizeDenseSphereTrajectory",
                "com.urbanforma.fireworks.client.midsize.MidsizeDenseSphereClientProgram"),
        DENSE_RADIAL(
                "radiant",
                MidsizeDenseRadialTrajectory.REFERENCE_EFFECT_ID,
                "com.urbanforma.fireworks.content.midsize.MidsizeDenseRadialTrajectory",
                "com.urbanforma.fireworks.client.midsize.MidsizeDenseRadialClientProgram");

        private final String creativeSection;
        private final String referenceEffectId;
        private final String trajectoryClass;
        private final String clientProgramClass;

        EffectType(
                String creativeSection,
                String referenceEffectId,
                String trajectoryClass,
                String clientProgramClass) {
            this.creativeSection = creativeSection;
            this.referenceEffectId = referenceEffectId;
            this.trajectoryClass = trajectoryClass;
            this.clientProgramClass = clientProgramClass;
        }

        public String creativeSection() {
            return this.creativeSection;
        }

        public String referenceEffectId() {
            return this.referenceEffectId;
        }

        public String trajectoryClass() {
            return this.trajectoryClass;
        }

        public String clientProgramClass() {
            return this.clientProgramClass;
        }
    }

    public record Palette(String primary, String secondary, String accent) {
        public Palette {
            validateColor(primary, "primary");
            validateColor(secondary, "secondary");
            validateColor(accent, "accent");
            if (primary.equalsIgnoreCase(secondary) || primary.equalsIgnoreCase(accent)
                    || secondary.equalsIgnoreCase(accent)) {
                throw new IllegalArgumentException("Midsize palettes require three distinct colors");
            }
        }
    }

    /** A future recipe file uses this exact template; this isolated prototype deliberately creates none. */
    public record RecipeContract(
            List<String> pattern,
            Map<String, String> key,
            String resultId,
            int count,
            boolean generateDataRecipe,
            String coordinatorDataPath) {
        public RecipeContract {
            pattern = List.copyOf(Objects.requireNonNull(pattern, "pattern"));
            key = Map.copyOf(Objects.requireNonNull(key, "key"));
            if (!pattern.equals(List.of(" P ", "FGF", " P "))
                    || !key.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || resultId == null || resultId.isBlank() || count != 1 || generateDataRecipe
                    || coordinatorDataPath == null || !coordinatorDataPath.matches(
                            "data/urbanforma_fireworks/recipes/midsize_dense_(sphere|radial)_firework\\.json")) {
                throw new IllegalArgumentException("Midsize recipe contract must remain metadata-only and explicit");
            }
        }
    }

    /** Existing shape-only creative section plus a stable append-only integration hint. */
    public record CreativeTarget(String section, String orderGroup) {
        public CreativeTarget {
            requireText(section, "creative section");
            requireText(orderGroup, "creative order group");
        }
    }

    /** Existing vanilla item model; no model JSON, texture, or custom loader is created in this isolated path. */
    public record ModelContract(String itemModelParent, boolean createsModelJson, boolean createsTexture, boolean customLoader) {
        public ModelContract {
            if (!ITEM_MODEL_PARENT.equals(itemModelParent) || createsModelJson || createsTexture || customLoader) {
                throw new IllegalArgumentException("Midsize prototypes must reuse the vanilla rocket model contract");
            }
        }
    }

    /** The coordinator owns the single payload listener and invokes the named caller-driven client program once. */
    public record EffectPath(
            String serverTrigger,
            String payloadContract,
            String trajectoryClass,
            String clientProgramClass,
            String clientEntryPoint,
            String requestShape,
            boolean coordinatorOwnsSharedWiring) {
        public EffectPath {
            requireText(serverTrigger, "serverTrigger");
            requireText(payloadContract, "payloadContract");
            requireText(trajectoryClass, "trajectoryClass");
            requireText(clientProgramClass, "clientProgramClass");
            requireText(clientEntryPoint, "clientEntryPoint");
            requireText(requestShape, "requestShape");
            if (!coordinatorOwnsSharedWiring) {
                throw new IllegalArgumentException("Midsize programs must not claim shared network or scheduler wiring");
            }
        }
    }

    /** A finite client-local plan; it neither reserves a global slot nor adds a server particle loop. */
    public record ParticlePlan(
            String trajectoryClass,
            String particleType,
            int totalParticles,
            int particlesPerTick,
            int emissionTicks,
            int minLifetimeTicks,
            int maxLifetimeTicks,
            int sharedMaxParticlesPerTick,
            int maxOwnedParticles,
            String velocityContract,
            boolean createsParticleType,
            boolean createsServerParticleLoop,
            boolean createsGlobalLimiter) {
        public ParticlePlan {
            requireText(trajectoryClass, "trajectoryClass");
            if (!VANILLA_FIREWORK_PARTICLE.equals(particleType) || totalParticles <= 0 || particlesPerTick <= 0
                    || emissionTicks <= 0 || totalParticles != particlesPerTick * emissionTicks
                    || minLifetimeTicks <= 0 || maxLifetimeTicks < minLifetimeTicks
                    || sharedMaxParticlesPerTick != 720 || particlesPerTick > sharedMaxParticlesPerTick
                    || maxOwnedParticles != totalParticles || !"fixed_zero_velocity".equals(velocityContract)
                    || createsParticleType || createsServerParticleLoop || createsGlobalLimiter) {
                throw new IllegalArgumentException("Midsize particle plan exceeds the isolated finite contract");
            }
        }
    }

    /** Half-envelope and 80-90% ascent proof derived from the named current large effect. */
    public record Boundary(
            String referenceEffectId,
            int referenceTotalParticles,
            double particleRatio,
            double referenceFullEnvelopeBlocks,
            double fullEnvelopeBlocks,
            double envelopeRatio,
            double maxRadius,
            int referenceAscentTicks,
            int ascentTicks,
            double referenceDetonationHeight,
            double detonationHeight,
            double heightRatio,
            String proofContract) {
        public Boundary {
            requireText(referenceEffectId, "referenceEffectId");
            requireText(proofContract, "proofContract");
            if (referenceTotalParticles <= 0 || particleRatio < 0.75D || particleRatio > 0.85D
                    || !Double.isFinite(referenceFullEnvelopeBlocks) || !Double.isFinite(fullEnvelopeBlocks)
                    || referenceFullEnvelopeBlocks <= 0.0D || fullEnvelopeBlocks <= 0.0D
                    || Math.abs(envelopeRatio - 0.50D) > 1.0E-12D
                    || Math.abs(fullEnvelopeBlocks / referenceFullEnvelopeBlocks - envelopeRatio) > 1.0E-12D
                    || Math.abs(maxRadius * 2.0D - fullEnvelopeBlocks) > 1.0E-9D
                    || referenceAscentTicks <= 0 || ascentTicks <= 0
                    || !Double.isFinite(referenceDetonationHeight) || !Double.isFinite(detonationHeight)
                    || referenceDetonationHeight <= 0.0D || detonationHeight <= 0.0D
                    || heightRatio < 0.80D || heightRatio > 0.90D
                    || Math.abs(detonationHeight / referenceDetonationHeight - heightRatio) > 1.0E-12D) {
                throw new IllegalArgumentException("Midsize boundary must remain half-size and slightly lower");
            }
        }
    }

    /** Full non-palette structural signature required for each of the two trial shapes. */
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
                throw new IllegalArgumentException("Midsize visual signature must cover every structural axis");
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

    private static void validateColor(String value, String field) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException(field + " must be a #RRGGBB value");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
