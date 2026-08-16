package com.urbanforma.fireworks.content.small;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The exact two unregistered small-firework contracts for coordinator integration. */
public final class SmallFireworkCatalog {
    public static final String BATCH_ID = "small";
    public static final String MANIFEST_RESOURCE = "assets/urbanforma_fireworks/small/small_manifest.json";
    public static final int REQUIRED_ENTRY_COUNT = 2;
    public static final double SHARED_LAUNCH_SPEED = 1.45D;
    public static final int FLIGHT_TICKS = 30;
    public static final double DETONATION_HEIGHT = FLIGHT_TICKS * SHARED_LAUNCH_SPEED;
    public static final double MINIMUM_NON_GROUND_HEIGHT = 32.0D;
    public static final double FIRST_MIDSIZE_DETONATION_HEIGHT = 105.85D;

    private static final List<SmallFireworkDefinition> VALUES = List.of(
            layeredSphere(),
            compactRadial());
    private static final Map<String, SmallFireworkDefinition> BY_ID = indexById();

    static {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || SmallVisualSignatures.count() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Small catalog must contain exactly the sphere and radial prototypes");
        }
        Set<String> ids = new HashSet<>();
        Set<String> zhNames = new HashSet<>();
        Set<String> enNames = new HashSet<>();
        Set<String> paletteSignatures = new HashSet<>();
        Set<String> structuralSignatures = new HashSet<>();
        Set<SmallFireworkDefinition.EffectType> forms = new HashSet<>();
        for (SmallFireworkDefinition definition : VALUES) {
            SmallFireworkDefinition.LocalParticlePlan plan = definition.clientEffectPath().particlePlan();
            if (!ids.add(definition.id())
                    || !zhNames.add(definition.zhName())
                    || !enNames.add(definition.enName())
                    || !paletteSignatures.add(definition.palette().signature())
                    || !structuralSignatures.add(definition.visualSignature().structuralSignature())
                    || !forms.add(definition.effectType())
                    || plan.peakParticlesPerTick() > plan.localTickBudget()
                    || plan.totalParticles() > plan.localOwnedParticleBudget()
                    || definition.launchTarget().detonationHeight() >= FIRST_MIDSIZE_DETONATION_HEIGHT
                    || definition.launchTarget().detonationHeight() < MINIMUM_NON_GROUND_HEIGHT
                    || definition.clientEffectPath().createsServerParticleLoop()
                    || definition.clientEffectPath().createsServerTerrainCalculation()
                    || definition.clientEffectPath().createsServerTrajectoryCalculation()
                    || definition.clientEffectPath().createsNewNetworkPayload()) {
                throw new IllegalStateException("Small catalog uniqueness, boundary, or client-only contract drifted");
            }
        }
        if (forms.size() != REQUIRED_ENTRY_COUNT
                || paletteSignatures.size() != REQUIRED_ENTRY_COUNT
                || structuralSignatures.size() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Small prototypes must differ in form, palette, and structural signature");
        }
    }

    private SmallFireworkCatalog() {
    }

    public static List<SmallFireworkDefinition> values() {
        return VALUES;
    }

    public static SmallFireworkDefinition byId(String id) {
        return BY_ID.get(id);
    }

    private static SmallFireworkDefinition layeredSphere() {
        String id = "small_layered_sphere_firework";
        return definition(
                id,
                "小型层叠球形烟花",
                "Small Layered Sphere Firework",
                SmallFireworkDefinition.EffectType.LAYERED_SPHERE,
                new SmallFireworkDefinition.Palette("#2EA9FF", "#7FE8FF", "#F0FCFF"),
                new SmallFireworkDefinition.LocalParticlePlan(
                        SmallFireworkDefinition.HD_PARTICLE_ID,
                        SmallFireworkDefinition.VANILLA_FALLBACK_PARTICLE,
                        616,
                        56,
                        11,
                        18,
                        42,
                        64,
                        640,
                        8.0D,
                        16.0D,
                        false,
                        false));
    }

    private static SmallFireworkDefinition compactRadial() {
        String id = "small_compact_radial_firework";
        return definition(
                id,
                "小型饱满放射烟花",
                "Small Compact Radial Firework",
                SmallFireworkDefinition.EffectType.COMPACT_RADIAL,
                new SmallFireworkDefinition.Palette("#FF582E", "#FFB135", "#FFF1B0"),
                new SmallFireworkDefinition.LocalParticlePlan(
                        SmallFireworkDefinition.HD_PARTICLE_ID,
                        SmallFireworkDefinition.VANILLA_FALLBACK_PARTICLE,
                        640,
                        80,
                        8,
                        16,
                        38,
                        96,
                        672,
                        9.6D,
                        19.2D,
                        false,
                        false));
    }

    private static SmallFireworkDefinition definition(
            String id,
            String zhName,
            String enName,
            SmallFireworkDefinition.EffectType effectType,
            SmallFireworkDefinition.Palette palette,
            SmallFireworkDefinition.LocalParticlePlan particlePlan) {
        return new SmallFireworkDefinition(
                id,
                zhName,
                enName,
                effectType,
                palette,
                new SmallFireworkDefinition.RecipeTarget(
                        List.of(" P ", "FGF", " P "),
                        Map.of(
                                "P", "minecraft:paper",
                                "F", "minecraft:firework_star",
                                "G", "minecraft:gunpowder"),
                        SmallFireworkDefinition.MOD_ID + ":" + id,
                        1,
                        false,
                        "data/urbanforma_fireworks/recipes/" + id + ".json"),
                new SmallFireworkDefinition.CreativeTarget(
                        SmallFireworkDefinition.CATEGORY,
                        SmallFireworkDefinition.CATEGORY,
                        SmallFireworkDefinition.ORDER_GROUP,
                        Set.of("midsize", "giant")),
                new SmallFireworkDefinition.RenderTarget(
                        SmallFireworkDefinition.HD_PARTICLE_ID,
                        SmallFireworkDefinition.HD_PARTICLE_DEFINITION,
                        SmallFireworkDefinition.VANILLA_FALLBACK_PARTICLE,
                        SmallFireworkDefinition.VANILLA_ROCKET_MODEL,
                        false,
                        false,
                        false,
                        false,
                        false),
                new SmallFireworkDefinition.ClientEffectPath(
                        effectType.effectForm(),
                        effectType.clientProgramClass(),
                        "tick(Minecraft)",
                        "Request(double x, double y, double z, long seed)",
                        SmallFireworkDefinition.EXISTING_BURST_PAYLOAD,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        particlePlan),
                new SmallFireworkDefinition.LaunchTarget(
                        "midsize_dense_sphere_firework",
                        FIRST_MIDSIZE_DETONATION_HEIGHT,
                        FLIGHT_TICKS,
                        SHARED_LAUNCH_SPEED,
                        DETONATION_HEIGHT,
                        MINIMUM_NON_GROUND_HEIGHT,
                        false),
                SmallVisualSignatures.forId(id));
    }

    private static Map<String, SmallFireworkDefinition> indexById() {
        Map<String, SmallFireworkDefinition> indexed = new HashMap<>();
        for (SmallFireworkDefinition definition : VALUES) {
            SmallFireworkDefinition previous = indexed.put(definition.id(), definition);
            if (previous != null) {
                throw new IllegalStateException("Duplicate small-firework id " + definition.id());
            }
        }
        return Map.copyOf(indexed);
    }
}
