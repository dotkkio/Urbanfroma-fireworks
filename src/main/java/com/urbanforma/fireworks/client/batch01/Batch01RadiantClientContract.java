package com.urbanforma.fireworks.client.batch01;

import com.urbanforma.fireworks.content.batch01.Batch01RadiantCatalog;
import java.util.Objects;

/**
 * Client-only integration checklist expressed as code for batch01's ordinary radiant entries.
 *
 * <p>This class does not subscribe to an event, allocate particles, or alter a shared queue. It makes the required
 * reuse of the existing bounded radiant path mechanically checkable by the integration owner.</p>
 */
public final class Batch01RadiantClientContract {
    public static final int BRANCH_COUNT = 160;
    public static final int SEGMENTS_PER_BRANCH = 30;
    public static final int PARTICLES_PER_BURST = 4_800;
    public static final int PARTICLES_PER_TICK = 160;
    public static final int EMISSION_TICKS = 30;
    public static final int MAX_FULL_ENVELOPE = 108;
    public static final String PARTICLE_TYPE = "minecraft:firework";
    public static final String EFFECT_CATEGORY = "STANDARD";

    private Batch01RadiantClientContract() {
    }

    /** Fails closed if a future integration attempts to route a batch01 entry into a new or unbounded client path. */
    public static void validate(Batch01RadiantCatalog.Definition definition) {
        Objects.requireNonNull(definition, "definition");
        Batch01RadiantCatalog.ReuseContract reuse = definition.reuseContract();
        Batch01RadiantCatalog.ParticlePlan plan = definition.particlePlan();
        Batch01RadiantCatalog.Boundary boundary = definition.expectedBoundary();
        if (definition.effectType() != Batch01RadiantCatalog.EffectType.RADIANT
                || !PARTICLE_TYPE.equals(reuse.particleType())
                || !EFFECT_CATEGORY.equals(reuse.effectCategory())
                || reuse.createsParticleType()
                || reuse.usesRadiantWillowQueue()
                || definition.visualDifference().isBlank()
                || definition.visualIdentity().differingAxes().stream()
                        .noneMatch(axis -> axis != Batch01RadiantCatalog.VisualAxis.COLOR_PAIRING)
                || plan.branches() != BRANCH_COUNT
                || plan.segmentsPerBranch() != SEGMENTS_PER_BRANCH
                || plan.totalParticles() != PARTICLES_PER_BURST
                || plan.particlesPerTick() != PARTICLES_PER_TICK
                || plan.emissionTicks() != EMISSION_TICKS
                || boundary.fullEnvelope() > MAX_FULL_ENVELOPE) {
            throw new IllegalArgumentException("Batch01 entry violates the ordinary radiant client contract");
        }
    }

    public static void validateAll() {
        Batch01RadiantCatalog.definitions().forEach(Batch01RadiantClientContract::validate);
    }
}
