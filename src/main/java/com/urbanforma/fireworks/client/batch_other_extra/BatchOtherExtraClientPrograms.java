package com.urbanforma.fireworks.client.batch_other_extra;

import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraCatalog;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraFirework;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraTrajectory;
import java.util.List;

/**
 * Client-side handoff programs for the additional Other routes.
 *
 * <p>The class only returns one bounded tick of deterministic samples. It creates no particle, queue, event listener,
 * server callback, wall-clock value, or mutable seed source. The shared client integrator owns the final particle
 * adapter and scheduling call.</p>
 */
public final class BatchOtherExtraClientPrograms {
    private static final List<Program> PROGRAMS = createPrograms();

    private BatchOtherExtraClientPrograms() {
    }

    public static List<Program> all() {
        return PROGRAMS;
    }

    public static Program require(String programId) {
        for (Program program : PROGRAMS) {
            if (program.id().equals(programId)) {
                return program;
            }
        }
        throw new IllegalArgumentException("Unknown Extra Other client program " + programId);
    }

    public static Program forEntry(String id) {
        BatchOtherExtraFirework definition = BatchOtherExtraCatalog.byId(id);
        return require(definition.clientProgram());
    }

    public static void validateAll() {
        if (PROGRAMS.size() != BatchOtherExtraCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Extra Other client program count drifted");
        }
        for (Program program : PROGRAMS) {
            BatchOtherExtraFirework definition = BatchOtherExtraCatalog.byId(program.definitionId());
            if (!program.id().equals(definition.clientProgram())
                    || program.route() != definition.effectPath()
                    || program.peakParticles() != definition.style().totalParticleCount()
                    || program.maxParticlesPerTick() != definition.style().maxParticlesPerTick()
                    || program.ownedParticles() != definition.style().ownedParticles()
                    || !program.fitsWithin(definition.expectedBoundary().fullEnvelopeBlocks())) {
                throw new IllegalStateException("Extra Other client mapping drifted for " + program.definitionId());
            }
        }
    }

    private static List<Program> createPrograms() {
        Program[] programs = new Program[BatchOtherExtraCatalog.REQUIRED_ENTRY_COUNT];
        int index = 0;
        for (BatchOtherExtraFirework definition : BatchOtherExtraCatalog.values()) {
            if (index >= programs.length) {
                throw new IllegalStateException("Extra Other client program array overflow");
            }
            programs[index++] = new Program(definition);
        }
        if (index != programs.length) {
            throw new IllegalStateException("Extra Other client program array underflow");
        }
        return List.of(programs);
    }

    public record Program(
            String definitionId,
            String id,
            BatchOtherExtraFirework.EffectPath route,
            BatchOtherExtraFirework.TrajectoryContract trajectory,
            int peakParticles,
            int maxParticlesPerTick,
            int ownedParticles,
            int envelopeBlocks,
            int lifecycleTicks) {
        public Program(BatchOtherExtraFirework definition) {
            this(
                    definition.id(),
                    definition.clientProgram(),
                    definition.effectPath(),
                    definition.trajectory(),
                    definition.style().totalParticleCount(),
                    definition.style().maxParticlesPerTick(),
                    definition.style().ownedParticles(),
                    definition.expectedBoundary().fullEnvelopeBlocks(),
                    definition.trajectory().lifecycleTicks());
        }

        public Program {
            if (definitionId == null || definitionId.isBlank() || id == null || route == null || trajectory == null
                    || !id.equals(route.clientProgramId()) || trajectory.route() != route
                    || peakParticles != trajectory.plannedParticles()
                    || maxParticlesPerTick != trajectory.maximumParticlesPerTick()
                    || maxParticlesPerTick <= 0 || maxParticlesPerTick > BatchOtherExtraFirework.MAX_PER_TICK
                    || ownedParticles < peakParticles || ownedParticles > BatchOtherExtraFirework.MAX_OWNED_PARTICLES
                    || envelopeBlocks <= 0 || envelopeBlocks > BatchOtherExtraFirework.ORDINARY_MAXIMUM_ENVELOPE
                    || BatchOtherExtraTrajectory.conservativeEnvelopeBlocks(trajectory) > envelopeBlocks
                    || lifecycleTicks != trajectory.lifecycleTicks() || lifecycleTicks > 240) {
                throw new IllegalArgumentException("Invalid bounded Extra Other client program");
            }
        }

        public List<BatchOtherExtraTrajectory.Sample> emissionsAtTick(long burstSeed, int tick) {
            return BatchOtherExtraTrajectory.samplesAtTick(this.trajectory, burstSeed, tick);
        }

        public BatchOtherExtraTrajectory.Point sample(long burstSeed, int component, int branch, int segment) {
            return BatchOtherExtraTrajectory.samplePoint(this.trajectory, burstSeed, component, branch, segment);
        }

        public int maximumScheduledParticles() {
            return BatchOtherExtraTrajectory.maximumParticlesPerTick(this.trajectory);
        }

        public boolean fitsWithin(int envelope) {
            return this.envelopeBlocks <= envelope
                    && envelope <= BatchOtherExtraFirework.ORDINARY_MAXIMUM_ENVELOPE;
        }
    }
}
