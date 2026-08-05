package com.urbanforma.fireworks.client.batch_other;

import com.urbanforma.fireworks.content.batch_other.BatchOtherCatalog;
import com.urbanforma.fireworks.content.batch_other.BatchOtherFirework;
import java.util.List;
import java.util.Objects;

/** Client handoff contract; public scheduler registration remains owned by the integration thread. */
public final class BatchOtherClientContracts {
    public static final String MODEL_PARENT = BatchOtherCatalog.MODEL_PARENT;
    public static final String PARTICLE_TYPE = BatchOtherCatalog.PARTICLE_TYPE;
    public static final String SHARED_ENGINE = BatchOtherCatalog.PARTICLE_ENGINE;
    public static final String INTEGRATION_STATUS = BatchOtherCatalog.INTEGRATION_STATUS;

    private BatchOtherClientContracts() {
    }

    public static List<BatchOtherFirework> definitions() {
        return BatchOtherCatalog.values();
    }

    public static BatchOtherFirework require(String id) {
        return BatchOtherCatalog.byId(id);
    }

    public static BatchOtherClientPrograms.Program program(String id) {
        BatchOtherFirework definition = require(id);
        validate(definition);
        return BatchOtherClientPrograms.require(definition.clientProgram());
    }

    /** Scheduler-facing convenience path; it only returns the finite route samples for one client tick. */
    public static List<BatchOtherClientPrograms.Emission> emissionsAtTick(String id, long burstSeed, int tick) {
        return program(id).emissionsAtTick(burstSeed, tick);
    }

    /** Exposes the fixed route lifetime without allocating particles or touching a shared scheduler. */
    public static BatchOtherClientPrograms.Lifecycle lifecycle(String id) {
        return program(id).lifecycle();
    }

    public static void validate(BatchOtherFirework definition) {
        Objects.requireNonNull(definition, "definition");
        if (BatchOtherCatalog.byId(definition.id()) != definition
                || !definition.model().parent().equals(MODEL_PARENT)
                || !definition.particle().particleType().equals(PARTICLE_TYPE)
                || !definition.particle().engine().equals(SHARED_ENGINE)
                || !definition.clientProgram().startsWith("batch_other:")
                || !definition.effectPath().matches("RADIANT|RADIANT_WILLOW|HYBRID_SPHERE_RADIANT|SATURN|COLOR_CHANGE")) {
            throw new IllegalArgumentException("Definition is not a batch_other handoff contract: "
                    + definition.id());
        }
        BatchOtherClientPrograms.Program program = BatchOtherClientPrograms.require(definition.clientProgram());
        if (!program.route().sharedEffectPath().equals(definition.effectPath())
                || program.peakParticles() != definition.style().totalStarCount()
                || program.totalScheduledParticles() != definition.style().totalStarCount()
                || program.maximumScheduledParticles() > definition.style().starsPerTick()
                || program.maximumScheduledParticles() > BatchOtherClientPrograms.MAX_PER_TICK
                || program.ownedParticleBudget() > BatchOtherClientPrograms.MAX_OWNED_PARTICLES
                || program.totalTicks() > 120
                || !program.trajectory().id().equals(program.route().id())
                || !program.fitsWithin(definition.expectedBoundary().fullEnvelopeBlocks())) {
            throw new IllegalArgumentException("Definition program drifted from the batch_other runtime contract: "
                    + definition.id());
        }
    }

    public static void validateAll() {
        for (BatchOtherFirework definition : definitions()) {
            validate(definition);
        }
        BatchOtherClientPrograms.validateAll();
    }
}
