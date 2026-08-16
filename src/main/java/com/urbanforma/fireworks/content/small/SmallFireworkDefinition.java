package com.urbanforma.fireworks.content.small;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Typed, unregistered contract for one small-firework prototype.
 *
 * <p>This common-side type only describes future integration. It contains no client class references, particle
 * allocation, scheduler registration, payload registration, or server trajectory calculation.</p>
 */
public record SmallFireworkDefinition(
        String id,
        String zhName,
        String enName,
        EffectType effectType,
        Palette palette,
        RecipeTarget recipeTarget,
        CreativeTarget creativeTarget,
        RenderTarget renderTarget,
        ClientEffectPath clientEffectPath,
        LaunchTarget launchTarget,
        VisualSignature visualSignature) {
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final String CATEGORY = "small";
    public static final String ORDER_GROUP = "small_trial";
    public static final String VANILLA_ROCKET_MODEL = "minecraft:item/firework_rocket";
    public static final String HD_PARTICLE_ID = "urbanforma_fireworks:hd_firework_spark";
    public static final String HD_PARTICLE_DEFINITION =
            "assets/urbanforma_fireworks/particles/hd_firework_spark.json";
    public static final String VANILLA_FALLBACK_PARTICLE = "minecraft:firework";
    public static final String EXISTING_BURST_PAYLOAD = "GrandFireworkBurstPayload";
    private static final Pattern STABLE_ID =
            Pattern.compile("small_(layered_sphere|compact_radial)_firework");

    public SmallFireworkDefinition {
        if (id == null || !STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Small-firework ids must be the two approved stable identifiers");
        }
        requireText(zhName, "zhName");
        requireText(enName, "enName");
        Objects.requireNonNull(effectType, "effectType");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(recipeTarget, "recipeTarget");
        Objects.requireNonNull(creativeTarget, "creativeTarget");
        Objects.requireNonNull(renderTarget, "renderTarget");
        Objects.requireNonNull(clientEffectPath, "clientEffectPath");
        Objects.requireNonNull(launchTarget, "launchTarget");
        Objects.requireNonNull(visualSignature, "visualSignature");

        if (!recipeTarget.resultId().equals(MOD_ID + ":" + id)
                || !creativeTarget.category().equals(CATEGORY)
                || !creativeTarget.section().equals(CATEGORY)
                || !creativeTarget.orderGroup().equals(ORDER_GROUP)
                || !clientEffectPath.effectForm().equals(effectType.effectForm())
                || !clientEffectPath.clientProgramClass().equals(effectType.clientProgramClass())
                || !clientEffectPath.clientEntryPoint().equals("tick(Minecraft)")
                || !clientEffectPath.requestShape().equals("Request(double x, double y, double z, long seed)")
                || !clientEffectPath.usesExistingBurstPayload()
                || !launchTarget.referenceMidsizeId().equals("midsize_dense_sphere_firework")) {
            throw new IllegalArgumentException("Small-firework integration contract fields are inconsistent");
        }
    }

    public enum EffectType {
        LAYERED_SPHERE(
                "layered_sphere",
                "com.urbanforma.fireworks.client.small.SmallLayeredSphereClientProgram"),
        COMPACT_RADIAL(
                "compact_radial",
                "com.urbanforma.fireworks.client.small.SmallCompactRadialClientProgram");

        private final String effectForm;
        private final String clientProgramClass;

        EffectType(String effectForm, String clientProgramClass) {
            this.effectForm = effectForm;
            this.clientProgramClass = clientProgramClass;
        }

        public String effectForm() {
            return this.effectForm;
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
            if (primary.equalsIgnoreCase(secondary)
                    || primary.equalsIgnoreCase(accent)
                    || secondary.equalsIgnoreCase(accent)) {
                throw new IllegalArgumentException("Small-firework palettes require three distinct colors");
            }
        }

        public String signature() {
            return String.join("|", primary.toUpperCase(), secondary.toUpperCase(), accent.toUpperCase());
        }
    }

    /**
     * Future recipe metadata only. The coordinator owns data generation and this prototype creates no recipe JSON.
     */
    public record RecipeTarget(
            List<String> pattern,
            Map<String, String> key,
            String resultId,
            int count,
            boolean generateDataRecipe,
            String coordinatorDataPath) {
        public RecipeTarget {
            pattern = List.copyOf(Objects.requireNonNull(pattern, "pattern"));
            key = Map.copyOf(Objects.requireNonNull(key, "key"));
            if (!pattern.equals(List.of(" P ", "FGF", " P "))
                    || !key.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || resultId == null
                    || resultId.isBlank()
                    || count != 1
                    || generateDataRecipe
                    || coordinatorDataPath == null
                    || !coordinatorDataPath.matches(
                            "data/urbanforma_fireworks/recipes/small_(layered_sphere|compact_radial)_firework\\.json")) {
                throw new IllegalArgumentException("Small-firework recipe target must stay explicit and metadata-only");
            }
        }
    }

    /** A future small-only creative category target; it cannot claim midsize or giant placement. */
    public record CreativeTarget(String category, String section, String orderGroup, Set<String> excludedCategories) {
        public CreativeTarget {
            requireText(category, "category");
            requireText(section, "section");
            requireText(orderGroup, "orderGroup");
            excludedCategories = Set.copyOf(Objects.requireNonNull(excludedCategories, "excludedCategories"));
            if (!CATEGORY.equals(category)
                    || !CATEGORY.equals(section)
                    || !ORDER_GROUP.equals(orderGroup)
                    || !excludedCategories.equals(Set.of("midsize", "giant"))) {
                throw new IllegalArgumentException("Small-firework creative target must remain separate from larger classes");
            }
        }
    }

    /** Reuses the established HD spark with a vanilla FIREWORK fallback and the vanilla rocket item model. */
    public record RenderTarget(
            String preferredHdParticle,
            String hdParticleDefinition,
            String vanillaFallbackParticle,
            String itemModelParent,
            boolean createsParticleType,
            boolean createsParticleProvider,
            boolean createsModelJson,
            boolean createsTexture,
            boolean customLoader) {
        public RenderTarget {
            if (!HD_PARTICLE_ID.equals(preferredHdParticle)
                    || !HD_PARTICLE_DEFINITION.equals(hdParticleDefinition)
                    || !VANILLA_FALLBACK_PARTICLE.equals(vanillaFallbackParticle)
                    || !VANILLA_ROCKET_MODEL.equals(itemModelParent)
                    || createsParticleType
                    || createsParticleProvider
                    || createsModelJson
                    || createsTexture
                    || customLoader) {
                throw new IllegalArgumentException("Small-firework render target must reuse existing client assets only");
            }
        }
    }

    /**
     * Client-only execution handoff. It names the existing compact burst payload but adds neither a payload nor a
     * server particle/terrain/trajectory loop.
     */
    public record ClientEffectPath(
            String effectForm,
            String clientProgramClass,
            String clientEntryPoint,
            String requestShape,
            String existingBurstPayload,
            boolean usesExistingBurstPayload,
            boolean clientOnly,
            boolean createsClientListener,
            boolean createsSharedScheduler,
            boolean createsNewNetworkPayload,
            boolean createsServerParticleLoop,
            boolean createsServerTerrainCalculation,
            boolean createsServerTrajectoryCalculation,
            LocalParticlePlan particlePlan) {
        public ClientEffectPath {
            requireText(effectForm, "effectForm");
            requireText(clientProgramClass, "clientProgramClass");
            requireText(clientEntryPoint, "clientEntryPoint");
            requireText(requestShape, "requestShape");
            Objects.requireNonNull(particlePlan, "particlePlan");
            if (!EXISTING_BURST_PAYLOAD.equals(existingBurstPayload)
                    || !usesExistingBurstPayload
                    || !clientOnly
                    || createsClientListener
                    || createsSharedScheduler
                    || createsNewNetworkPayload
                    || createsServerParticleLoop
                    || createsServerTerrainCalculation
                    || createsServerTrajectoryCalculation) {
                throw new IllegalArgumentException("Small-firework effects must stay caller-driven and client-only");
            }
        }
    }

    /**
     * Exact finite work owned by one client program instance. These local caps are not a shared global limiter.
     */
    public record LocalParticlePlan(
            String preferredParticle,
            String fallbackParticle,
            int totalParticles,
            int peakParticlesPerTick,
            int emissionTicks,
            int minLifetimeTicks,
            int maxLifetimeTicks,
            int localTickBudget,
            int localOwnedParticleBudget,
            double maxRadius,
            double fullEnvelopeBlocks,
            boolean createsParticleType,
            boolean createsGlobalLimiter) {
        public LocalParticlePlan {
            if (!HD_PARTICLE_ID.equals(preferredParticle)
                    || !VANILLA_FALLBACK_PARTICLE.equals(fallbackParticle)
                    || totalParticles <= 0
                    || peakParticlesPerTick <= 0
                    || emissionTicks <= 0
                    || totalParticles != peakParticlesPerTick * emissionTicks
                    || minLifetimeTicks <= 0
                    || maxLifetimeTicks < minLifetimeTicks
                    || localTickBudget < peakParticlesPerTick
                    || localOwnedParticleBudget < totalParticles
                    || !Double.isFinite(maxRadius)
                    || maxRadius <= 0.0D
                    || !Double.isFinite(fullEnvelopeBlocks)
                    || fullEnvelopeBlocks <= 0.0D
                    || Math.abs(maxRadius * 2.0D - fullEnvelopeBlocks) > 1.0E-9D
                    || createsParticleType
                    || createsGlobalLimiter) {
                throw new IllegalArgumentException("Small-firework local particle plan is invalid");
            }
        }
    }

    /**
     * Declarative launch target for a future shared rocket mapping. It does not implement a server trajectory.
     */
    public record LaunchTarget(
            String referenceMidsizeId,
            double referenceMidsizeDetonationHeight,
            int flightTicks,
            double sharedLaunchSpeed,
            double detonationHeight,
            double minimumNonGroundHeight,
            boolean requiresAdditionalServerTrajectoryCalculation) {
        public LaunchTarget {
            requireText(referenceMidsizeId, "referenceMidsizeId");
            if (!Double.isFinite(referenceMidsizeDetonationHeight)
                    || referenceMidsizeDetonationHeight <= 0.0D
                    || flightTicks <= 0
                    || !Double.isFinite(sharedLaunchSpeed)
                    || sharedLaunchSpeed <= 0.0D
                    || !Double.isFinite(detonationHeight)
                    || detonationHeight <= 0.0D
                    || !Double.isFinite(minimumNonGroundHeight)
                    || minimumNonGroundHeight <= 0.0D
                    || Math.abs(detonationHeight - flightTicks * sharedLaunchSpeed) > 1.0E-9D
                    || detonationHeight < minimumNonGroundHeight
                    || detonationHeight >= referenceMidsizeDetonationHeight
                    || requiresAdditionalServerTrajectoryCalculation) {
                throw new IllegalArgumentException("Small-firework launch target must be low but safely above ground");
            }
        }
    }

    /** Structural data prevents palette-only variants from passing the two-prototype trial. */
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
                throw new IllegalArgumentException("Small-firework visual signature must cover each structural axis");
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
