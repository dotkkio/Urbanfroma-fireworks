package com.urbanforma.fireworks.client.large_extra;

import com.urbanforma.fireworks.content.large_extra.LargeExtraFireworkCatalog;
import com.urbanforma.fireworks.content.large_extra.LargeExtraFireworkDefinition;
import java.util.List;

/** Explicit integration handoff for the client-only Large Extra programs. */
public final class LargeExtraClientContracts {
    private LargeExtraClientContracts() {
    }

    public static List<ClientContract> values() {
        return LargeExtraFireworkCatalog.values().stream().map(LargeExtraClientContracts::forDefinition).toList();
    }

    public static ClientContract forDefinition(LargeExtraFireworkDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Large Extra client contract requires a definition");
        }
        return new ClientContract(
                definition.id(),
                definition.creativeTarget().sectionKey(),
                definition.reuseContract().clientProgramId(),
                LargeExtraFireworkDefinition.CLIENT_PROGRAM_OWNER,
                definition.particleBudget().plannedParticles(),
                definition.particleBudget().particlesPerTick(),
                definition.particleBudget().maxLiveParticles(),
                definition.particleBudget().totalVisualTicks(),
                definition.envelope().fullEnvelopeBlocks(),
                true,
                false,
                false,
                false,
                false);
    }

    /** Convenience bridge for the coordinator's existing physical-client burst dispatch. */
    public static LargeExtraClientPrograms.Program start(
            String id, double x, double y, double z, long seed) {
        return LargeExtraClientPrograms.start(new LargeExtraClientPrograms.Request(id, x, y, z, seed));
    }

    public static void validateAll() {
        List<ClientContract> contracts = values();
        if (contracts.size() != LargeExtraFireworkCatalog.REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Large Extra client contract count drifted");
        }
        for (ClientContract contract : contracts) {
            LargeExtraFireworkDefinition definition = LargeExtraFireworkCatalog.require(contract.id());
            if (!contract.clientProgramId().equals(definition.effectPath().clientProgramId())
                    || !contract.creativeSection().equals(LargeExtraFireworkDefinition.LARGE_CREATIVE_SECTION)
                    || contract.maxPerTick() != definition.particleBudget().particlesPerTick()
                    || contract.maxOwnedParticles() != definition.particleBudget().maxLiveParticles()
                    || contract.fullEnvelopeBlocks() != definition.envelope().fullEnvelopeBlocks()) {
                throw new IllegalStateException("Large Extra client contract drifted for " + contract.id());
            }
        }
        LargeExtraClientPrograms.validateAll();
    }

    public record ClientContract(
            String id,
            String creativeSection,
            String clientProgramId,
            String clientProgramOwner,
            int plannedParticles,
            int maxPerTick,
            int maxOwnedParticles,
            int totalVisualTicks,
            int fullEnvelopeBlocks,
            boolean physicalClientOnly,
            boolean createsServerParticleLoop,
            boolean createsServerTrajectory,
            boolean createsNetworkPayload,
            boolean createsGlobalScheduler) {
        public ClientContract {
            if (id == null
                    || id.isBlank()
                    || !LargeExtraFireworkDefinition.LARGE_CREATIVE_SECTION.equals(creativeSection)
                    || clientProgramId == null
                    || clientProgramId.isBlank()
                    || !LargeExtraFireworkDefinition.CLIENT_PROGRAM_OWNER.equals(clientProgramOwner)
                    || plannedParticles <= 0
                    || maxPerTick <= 0
                    || maxPerTick > LargeExtraFireworkDefinition.MAX_LOCAL_PARTICLES_PER_TICK
                    || maxOwnedParticles != plannedParticles
                    || maxOwnedParticles > LargeExtraFireworkDefinition.MAX_LOCAL_LIVE_PARTICLES
                    || totalVisualTicks <= 0
                    || totalVisualTicks > LargeExtraFireworkDefinition.MAX_LOCAL_VISUAL_TICKS
                    || fullEnvelopeBlocks <= 0
                    || fullEnvelopeBlocks > LargeExtraFireworkDefinition.LARGE_MAXIMUM_FULL_ENVELOPE
                    || !physicalClientOnly
                    || createsServerParticleLoop
                    || createsServerTrajectory
                    || createsNetworkPayload
                    || createsGlobalScheduler) {
                throw new IllegalArgumentException("Invalid Large Extra client-only integration contract");
            }
        }
    }
}
