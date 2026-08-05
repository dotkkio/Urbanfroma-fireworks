package com.urbanforma.fireworks.client.batch_other_extra;

import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraCatalog;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraFirework;
import java.util.List;
import java.util.Objects;

/** Explicit client handoff checks; public scheduler and particle adaptation remain coordinator-owned. */
public final class BatchOtherExtraClientContracts {
    public static final String MODEL_PARENT = BatchOtherExtraFirework.MODEL_PARENT;
    public static final String PARTICLE_TYPE = BatchOtherExtraFirework.PARTICLE_TYPE;
    public static final String SHARED_ENGINE = BatchOtherExtraFirework.PARTICLE_ENGINE;
    public static final String INTEGRATION_STATUS = BatchOtherExtraCatalog.INTEGRATION_STATUS;

    private BatchOtherExtraClientContracts() {
    }

    public static List<BatchOtherExtraFirework> definitions() {
        return BatchOtherExtraCatalog.values();
    }

    public static BatchOtherExtraFirework require(String id) {
        return BatchOtherExtraCatalog.byId(id);
    }

    public static BatchOtherExtraClientPrograms.Program program(String id) {
        BatchOtherExtraFirework definition = require(id);
        validate(definition);
        return BatchOtherExtraClientPrograms.require(definition.clientProgram());
    }

    public static void validate(BatchOtherExtraFirework definition) {
        Objects.requireNonNull(definition, "definition");
        if (BatchOtherExtraCatalog.byId(definition.id()) != definition
                || !MODEL_PARENT.equals(definition.model().parent())
                || !PARTICLE_TYPE.equals(definition.particle().particleType())
                || !SHARED_ENGINE.equals(definition.particle().engine())
                || !definition.clientProgram().equals(definition.effectPath().clientProgramId())
                || definition.trajectory().route() != definition.effectPath()
                || definition.style().maxParticlesPerTick() > BatchOtherExtraFirework.MAX_PER_TICK
                || definition.style().ownedParticles() > BatchOtherExtraFirework.MAX_OWNED_PARTICLES) {
            throw new IllegalArgumentException("Definition is not an Extra Other handoff contract: " + definition.id());
        }
        BatchOtherExtraClientPrograms.Program program = BatchOtherExtraClientPrograms.require(definition.clientProgram());
        if (program.route() != definition.effectPath()
                || program.peakParticles() != definition.style().totalParticleCount()
                || program.maximumScheduledParticles() > definition.style().maxParticlesPerTick()
                || !program.fitsWithin(definition.expectedBoundary().fullEnvelopeBlocks())) {
            throw new IllegalArgumentException("Extra Other client trajectory drifted for " + definition.id());
        }
    }

    public static void validateAll() {
        for (BatchOtherExtraFirework definition : definitions()) {
            validate(definition);
        }
        BatchOtherExtraClientPrograms.validateAll();
    }
}
