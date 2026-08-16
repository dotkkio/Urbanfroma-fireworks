package com.urbanforma.fireworks.client.midsize.radial;

import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialFireworkCatalog;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialFireworkDefinition;
import com.urbanforma.fireworks.content.midsize.radial.MidsizeRadialTrajectory;
import java.util.List;

/** Client hand-off records only; the shared client bootstrap owns listener and scheduler wiring. */
public final class MidsizeRadialClientContracts {
    private MidsizeRadialClientContracts() {
    }

    public static List<ClientContract> values() {
        return MidsizeRadialFireworkCatalog.values().stream().map(MidsizeRadialClientContracts::forDefinition).toList();
    }

    public static ClientContract forDefinition(MidsizeRadialFireworkDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Medium radial client contract requires a definition");
        }
        return new ClientContract(
                definition.id(),
                definition.path(),
                definition.reuseContract().particleType(),
                definition.reuseContract().clientProgramClass(),
                "Request(MidsizeRadialFireworkDefinition,double,double,double,long)",
                "tick(Minecraft)",
                definition.particlePlan().localPeakParticlesPerTick(),
                definition.particlePlan().maxOwnedParticles(),
                true,
                false,
                false,
                false);
    }

    public record ClientContract(
            String id,
            MidsizeRadialTrajectory.Path path,
            String particleType,
            String clientProgramClass,
            String requestShape,
            String entryPoint,
            int localPeakParticlesPerTick,
            int maxOwnedParticles,
            boolean clientOnly,
            boolean createsClientListener,
            boolean createsSharedScheduler,
            boolean createsNetworkPayload) {
        public ClientContract {
            if (id == null || id.isBlank() || path == null
                    || !MidsizeRadialFireworkDefinition.HD_FIREWORK_SPARK.equals(particleType)
                    || !"com.urbanforma.fireworks.client.midsize.radial.MidsizeRadialClientProgram".equals(clientProgramClass)
                    || requestShape == null || requestShape.isBlank() || entryPoint == null || entryPoint.isBlank()
                    || localPeakParticlesPerTick != path.maxParticlesPerTick()
                    || maxOwnedParticles != path.totalParticles() || !clientOnly || createsClientListener
                    || createsSharedScheduler || createsNetworkPayload) {
                throw new IllegalArgumentException("Medium radial client contract must remain caller-driven and local");
            }
        }
    }
}
