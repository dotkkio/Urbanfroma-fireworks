package com.urbanforma.fireworks.client.batch03;

import com.urbanforma.fireworks.client.hybrid.HybridSphereRadiantParticleProgram;
import com.urbanforma.fireworks.content.batch03.Batch03SphereRadiantCatalog;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Client-only adapter for batch03. It creates the existing hybrid program but owns no listener, queue, or shared
 * scheduler registration; the integration owner supplies lifecycle and concurrency wiring.
 */
public final class Batch03SphereRadiantClientContract {
    public static final String MODEL_PARENT = Batch03SphereRadiantCatalog.MODEL_PARENT;
    public static final String PARTICLE_TYPE = Batch03SphereRadiantCatalog.PARTICLE_TYPE;

    private Batch03SphereRadiantClientContract() {
    }

    public static HybridSphereRadiantParticleProgram createProgram(
            Batch03SphereRadiantCatalog.Definition definition,
            Minecraft minecraft,
            double x,
            double y,
            double z,
            long payloadSeed) {
        validate(definition);
        Objects.requireNonNull(minecraft, "minecraft");
        Batch03SphereRadiantCatalog.Palette palette = definition.palette();
        return new HybridSphereRadiantParticleProgram(
                minecraft,
                x,
                y,
                z,
                payloadSeed,
                Batch03SphereRadiantCatalog.RADIAL_PROFILE,
                palette.primary(),
                palette.secondary(),
                palette.accent());
    }

    public static void validate(Batch03SphereRadiantCatalog.Definition definition) {
        Objects.requireNonNull(definition, "definition");
        if (Batch03SphereRadiantCatalog.byId(definition.id()) != definition
                || definition.particleContract() != Batch03SphereRadiantCatalog.PARTICLE_CONTRACT
                || definition.expectedBoundary() != Batch03SphereRadiantCatalog.EXPECTED_BOUNDARY
                || definition.visualDifference().structuralAxes().isEmpty()) {
            throw new IllegalArgumentException("Definition is not a batch03 catalog entry: " + definition.id());
        }
    }
}
