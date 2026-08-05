package com.urbanforma.fireworks.client.midsize;

import com.urbanforma.fireworks.content.midsize.MidsizeFireworkCatalog;
import com.urbanforma.fireworks.content.midsize.MidsizeFireworkDefinition;
import java.util.List;

/** Client hand-off records only; this class deliberately creates no listener or scheduler. */
public final class MidsizeClientContracts {
    private MidsizeClientContracts() {
    }

    public static List<ClientContract> values() {
        return MidsizeFireworkCatalog.values().stream().map(MidsizeClientContracts::forDefinition).toList();
    }

    public static ClientContract forDefinition(MidsizeFireworkDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Midsize client contract requires a definition");
        }
        MidsizeFireworkDefinition.EffectPath path = definition.effectPath();
        return new ClientContract(
                definition.id(),
                definition.creativeTarget().section(),
                definition.particlePlan().particleType(),
                path.trajectoryClass(),
                path.clientProgramClass(),
                path.clientEntryPoint(),
                path.requestShape(),
                path.coordinatorOwnsSharedWiring(),
                false,
                false);
    }

    public record ClientContract(
            String id,
            String creativeSection,
            String particleType,
            String trajectoryClass,
            String clientProgramClass,
            String entryPoint,
            String requestShape,
            boolean coordinatorOwnsSharedWiring,
            boolean createsClientListener,
            boolean createsSharedScheduler) {
        public ClientContract {
            if (id == null || id.isBlank() || creativeSection == null || creativeSection.isBlank()
                    || !"minecraft:firework".equals(particleType) || trajectoryClass == null || trajectoryClass.isBlank()
                    || clientProgramClass == null || clientProgramClass.isBlank() || entryPoint == null || entryPoint.isBlank()
                    || requestShape == null || requestShape.isBlank() || !coordinatorOwnsSharedWiring
                    || createsClientListener || createsSharedScheduler) {
                throw new IllegalArgumentException("Invalid isolated midsize client contract");
            }
        }
    }
}
