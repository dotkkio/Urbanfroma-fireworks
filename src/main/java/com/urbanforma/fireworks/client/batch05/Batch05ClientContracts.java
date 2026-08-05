package com.urbanforma.fireworks.client.batch05;

import com.urbanforma.fireworks.content.EffectCategory;
import com.urbanforma.fireworks.content.batch05.Batch05EffectProfiles;
import com.urbanforma.fireworks.content.batch05.Batch05FireworkCatalog;
import com.urbanforma.fireworks.content.batch05.Batch05FireworkDefinition;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import java.util.List;

/**
 * Client-neutral hand-off contract for batch05. It intentionally creates no listener and does not touch the shared
 * client dispatcher; integration attaches these plans to the established scheduler.
 */
public final class Batch05ClientContracts {
    private Batch05ClientContracts() {
    }

    public static List<ClientContract> values() {
        return Batch05FireworkCatalog.values().stream().map(Batch05ClientContracts::forDefinition).toList();
    }

    public static ClientContract forDefinition(Batch05FireworkDefinition definition) {
        SaturnProgram saturnProgram = definition.effectType() == Batch05FireworkDefinition.EffectType.SATURN
                ? Batch05EffectProfiles.saturnProgram(definition)
                : null;
        return new ClientContract(
                definition.id(),
                definition.particlePlan().category(),
                definition.reuseContract().particleType(),
                definition.reuseContract().clientProgram(),
                definition.colorChange() != null,
                definition.colorChange() == null ? -1 : definition.colorChange().switchDelayTicks(),
                saturnProgram == null ? 0 : saturnProgram.totalSampleCount());
    }

    public record ClientContract(
            String id,
            EffectCategory category,
            String reusedParticleType,
            String clientProgram,
            boolean appliesColorChange,
            int colorChangeDelayTicks,
            int saturnSampleCount) {
    }
}
