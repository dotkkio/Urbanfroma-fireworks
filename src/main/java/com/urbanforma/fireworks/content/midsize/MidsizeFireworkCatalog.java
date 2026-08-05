package com.urbanforma.fireworks.content.midsize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The exact two unregistered midsize trial contracts for coordinator integration. */
public final class MidsizeFireworkCatalog {
    public static final String BATCH_ID = "midsize";
    public static final String MANIFEST_RESOURCE = "assets/urbanforma_fireworks/midsize/midsize_manifest.json";
    public static final int REQUIRED_ENTRY_COUNT = 2;

    private static final List<MidsizeFireworkDefinition> VALUES = List.of(
            denseSphere(),
            denseRadial());
    private static final Map<String, MidsizeFireworkDefinition> BY_ID = indexById();

    static {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || MidsizeVisualSignatures.count() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Midsize catalog must contain exactly the sphere and radial trial");
        }
        Set<String> zhNames = new HashSet<>();
        Set<String> enNames = new HashSet<>();
        Set<String> structuralSignatures = new HashSet<>();
        for (MidsizeFireworkDefinition definition : VALUES) {
            if (!zhNames.add(definition.zhName()) || !enNames.add(definition.enName())
                    || !structuralSignatures.add(definition.visualSignature().structuralSignature())
                    || definition.particlePlan().particlesPerTick() > 720
                    || definition.particlePlan().maxOwnedParticles() > 15_000
                    || !definition.boundary().proofContract().contains("fitsEnvelope")) {
                throw new IllegalStateException("Midsize catalog uniqueness or finite-budget contract drifted");
            }
        }
    }

    private MidsizeFireworkCatalog() {
    }

    public static List<MidsizeFireworkDefinition> values() {
        return VALUES;
    }

    public static MidsizeFireworkDefinition byId(String id) {
        return BY_ID.get(id);
    }

    private static MidsizeFireworkDefinition denseSphere() {
        String id = MidsizeDenseSphereTrajectory.EFFECT_ID;
        return definition(
                id,
                "中型密集球形烟花",
                "Medium Dense Sphere Firework",
                MidsizeFireworkDefinition.EffectType.DENSE_SPHERE,
                new MidsizeFireworkDefinition.Palette("#FFB31A", "#FF7D17", "#FFF0B7"),
                new MidsizeFireworkDefinition.ParticlePlan(
                        MidsizeFireworkDefinition.EffectType.DENSE_SPHERE.trajectoryClass(),
                        MidsizeFireworkDefinition.VANILLA_FIREWORK_PARTICLE,
                        MidsizeDenseSphereTrajectory.TOTAL_PARTICLES,
                        MidsizeDenseSphereTrajectory.PARTICLES_PER_EMISSION_TICK,
                        MidsizeDenseSphereTrajectory.EMISSION_TICKS,
                        MidsizeDenseSphereTrajectory.MIN_PARTICLE_LIFETIME,
                        MidsizeDenseSphereTrajectory.MAX_PARTICLE_LIFETIME,
                        MidsizeDenseSphereTrajectory.SHARED_MAX_PARTICLES_PER_TICK,
                        MidsizeDenseSphereTrajectory.MAX_OWNED_PARTICLES,
                        "fixed_zero_velocity",
                        false,
                        false,
                        false),
                new MidsizeFireworkDefinition.Boundary(
                        MidsizeDenseSphereTrajectory.REFERENCE_EFFECT_ID,
                        MidsizeDenseSphereTrajectory.REFERENCE_TOTAL_PARTICLES,
                        MidsizeDenseSphereTrajectory.PARTICLE_RATIO,
                        MidsizeDenseSphereTrajectory.REFERENCE_FULL_ENVELOPE,
                        MidsizeDenseSphereTrajectory.APPROVED_FULL_ENVELOPE,
                        MidsizeDenseSphereTrajectory.ENVELOPE_RATIO,
                        MidsizeDenseSphereTrajectory.MAX_RADIUS,
                        MidsizeDenseSphereTrajectory.REFERENCE_ASCENT_TICKS,
                        MidsizeDenseSphereTrajectory.ASCENT_TICKS,
                        MidsizeDenseSphereTrajectory.REFERENCE_DETONATION_HEIGHT,
                        MidsizeDenseSphereTrajectory.DETONATION_HEIGHT,
                        MidsizeDenseSphereTrajectory.HEIGHT_RATIO,
                        "MidsizeDenseSphereTrajectory.fitsEnvelope()"));
    }

    private static MidsizeFireworkDefinition denseRadial() {
        String id = MidsizeDenseRadialTrajectory.EFFECT_ID;
        return definition(
                id,
                "中型密集放射烟花",
                "Medium Dense Radial Firework",
                MidsizeFireworkDefinition.EffectType.DENSE_RADIAL,
                new MidsizeFireworkDefinition.Palette("#FF4B24", "#FF9A2B", "#FFE3A3"),
                new MidsizeFireworkDefinition.ParticlePlan(
                        MidsizeFireworkDefinition.EffectType.DENSE_RADIAL.trajectoryClass(),
                        MidsizeFireworkDefinition.VANILLA_FIREWORK_PARTICLE,
                        MidsizeDenseRadialTrajectory.TOTAL_PARTICLES,
                        MidsizeDenseRadialTrajectory.PARTICLES_PER_EMISSION_TICK,
                        MidsizeDenseRadialTrajectory.EMISSION_TICKS,
                        MidsizeDenseRadialTrajectory.MIN_PARTICLE_LIFETIME,
                        MidsizeDenseRadialTrajectory.MAX_PARTICLE_LIFETIME,
                        MidsizeDenseRadialTrajectory.SHARED_MAX_PARTICLES_PER_TICK,
                        MidsizeDenseRadialTrajectory.MAX_OWNED_PARTICLES,
                        "fixed_zero_velocity",
                        false,
                        false,
                        false),
                new MidsizeFireworkDefinition.Boundary(
                        MidsizeDenseRadialTrajectory.REFERENCE_EFFECT_ID,
                        MidsizeDenseRadialTrajectory.REFERENCE_TOTAL_PARTICLES,
                        MidsizeDenseRadialTrajectory.PARTICLE_RATIO,
                        MidsizeDenseRadialTrajectory.REFERENCE_FULL_ENVELOPE,
                        MidsizeDenseRadialTrajectory.APPROVED_FULL_ENVELOPE,
                        MidsizeDenseRadialTrajectory.ENVELOPE_RATIO,
                        MidsizeDenseRadialTrajectory.MAX_RADIUS,
                        MidsizeDenseRadialTrajectory.REFERENCE_ASCENT_TICKS,
                        MidsizeDenseRadialTrajectory.ASCENT_TICKS,
                        MidsizeDenseRadialTrajectory.REFERENCE_DETONATION_HEIGHT,
                        MidsizeDenseRadialTrajectory.DETONATION_HEIGHT,
                        MidsizeDenseRadialTrajectory.HEIGHT_RATIO,
                        "MidsizeDenseRadialTrajectory.fitsEnvelope()"));
    }

    private static MidsizeFireworkDefinition definition(
            String id,
            String zhName,
            String enName,
            MidsizeFireworkDefinition.EffectType effectType,
            MidsizeFireworkDefinition.Palette palette,
            MidsizeFireworkDefinition.ParticlePlan particlePlan,
            MidsizeFireworkDefinition.Boundary boundary) {
        return new MidsizeFireworkDefinition(
                id,
                zhName,
                enName,
                effectType,
                palette,
                new MidsizeFireworkDefinition.RecipeContract(
                        List.of(" P ", "FGF", " P "),
                        Map.of(
                                "P", "minecraft:paper",
                                "F", "minecraft:firework_star",
                                "G", "minecraft:gunpowder"),
                        MidsizeFireworkDefinition.MOD_ID + ":" + id,
                        1,
                        false,
                        "data/urbanforma_fireworks/recipes/" + id + ".json"),
                new MidsizeFireworkDefinition.CreativeTarget(effectType.creativeSection(),
                        MidsizeFireworkDefinition.ORDER_GROUP),
                new MidsizeFireworkDefinition.ModelContract(
                        MidsizeFireworkDefinition.ITEM_MODEL_PARENT, false, false, false),
                new MidsizeFireworkDefinition.EffectPath(
                        "GrandFireworkRocketEntity one-shot detonation payload",
                        "coordinator maps the stable id to one compact burst payload",
                        effectType.trajectoryClass(),
                        effectType.clientProgramClass(),
                        "tick(Minecraft)",
                        "Request(double x, double y, double z, long seed)",
                        true),
                particlePlan,
                boundary,
                MidsizeVisualSignatures.forId(id));
    }

    private static Map<String, MidsizeFireworkDefinition> indexById() {
        Map<String, MidsizeFireworkDefinition> valuesById = new HashMap<>();
        for (MidsizeFireworkDefinition definition : VALUES) {
            if (valuesById.put(definition.id(), definition) != null) {
                throw new IllegalStateException("Duplicate midsize firework id " + definition.id());
            }
        }
        return Map.copyOf(valuesById);
    }
}
