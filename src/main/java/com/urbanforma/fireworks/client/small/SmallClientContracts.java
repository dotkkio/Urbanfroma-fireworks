package com.urbanforma.fireworks.client.small;

import com.urbanforma.fireworks.content.small.SmallFireworkCatalog;
import com.urbanforma.fireworks.content.small.SmallFireworkDefinition;
import java.util.List;

/** Client hand-off data only. This class deliberately creates no listener, queue, scheduler, or network payload. */
public final class SmallClientContracts {
    private SmallClientContracts() {
    }

    public static List<ClientContract> values() {
        return SmallFireworkCatalog.values().stream().map(SmallClientContracts::forDefinition).toList();
    }

    public static ClientContract forDefinition(SmallFireworkDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Small client contract requires a definition");
        }
        SmallFireworkDefinition.ClientEffectPath path = definition.clientEffectPath();
        SmallFireworkDefinition.LocalParticlePlan plan = path.particlePlan();
        return new ClientContract(
                definition.id(),
                definition.creativeTarget().category(),
                path.effectForm(),
                path.clientProgramClass(),
                path.clientEntryPoint(),
                path.requestShape(),
                plan.preferredParticle(),
                plan.fallbackParticle(),
                plan.totalParticles(),
                plan.peakParticlesPerTick(),
                plan.localTickBudget(),
                plan.localOwnedParticleBudget(),
                path.usesExistingBurstPayload(),
                path.createsClientListener(),
                path.createsSharedScheduler(),
                path.createsNewNetworkPayload(),
                path.createsServerParticleLoop(),
                path.createsServerTerrainCalculation(),
                path.createsServerTrajectoryCalculation());
    }

    public record ClientContract(
            String id,
            String category,
            String effectForm,
            String clientProgramClass,
            String entryPoint,
            String requestShape,
            String preferredParticle,
            String fallbackParticle,
            int totalParticles,
            int peakParticlesPerTick,
            int localTickBudget,
            int localOwnedParticleBudget,
            boolean usesExistingBurstPayload,
            boolean createsClientListener,
            boolean createsSharedScheduler,
            boolean createsNewNetworkPayload,
            boolean createsServerParticleLoop,
            boolean createsServerTerrainCalculation,
            boolean createsServerTrajectoryCalculation) {
        public ClientContract {
            if (id == null
                    || id.isBlank()
                    || !"small".equals(category)
                    || effectForm == null
                    || effectForm.isBlank()
                    || clientProgramClass == null
                    || clientProgramClass.isBlank()
                    || !"tick(Minecraft)".equals(entryPoint)
                    || !"Request(double x, double y, double z, long seed)".equals(requestShape)
                    || !"urbanforma_fireworks:hd_firework_spark".equals(preferredParticle)
                    || !"minecraft:firework".equals(fallbackParticle)
                    || totalParticles <= 0
                    || peakParticlesPerTick <= 0
                    || peakParticlesPerTick > localTickBudget
                    || totalParticles > localOwnedParticleBudget
                    || !usesExistingBurstPayload
                    || createsClientListener
                    || createsSharedScheduler
                    || createsNewNetworkPayload
                    || createsServerParticleLoop
                    || createsServerTerrainCalculation
                    || createsServerTrajectoryCalculation) {
                throw new IllegalArgumentException("Invalid isolated small-firework client contract");
            }
        }
    }
}
