package com.urbanforma.fireworks.client.batch02;

import com.urbanforma.fireworks.content.RadiantWillowTrajectory;
import com.urbanforma.fireworks.content.batch02.Batch02RadiantWillowCatalog;

/**
 * Client-side integration gate for batch02.
 *
 * <p>The class deliberately creates no particles and owns no queue. The shared client scheduler remains the only
 * runtime owner; an integrator may use this gate before mapping the batch's styles into that scheduler.</p>
 */
public final class Batch02RadiantWillowClientContract {
    public static final String PARTICLE_TYPE_ID = "urbanforma_fireworks:hd_firework_spark";
    public static final String MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final int RADIANT_NODE_COUNT = 4_800;
    public static final int NEW_PARTICLES_DURING_EXTENSION = 0;
    public static final double MAX_FULL_ENVELOPE_BLOCKS = 220.0D;

    private Batch02RadiantWillowClientContract() {
    }

    public static void validate() {
        if (!PARTICLE_TYPE_ID.equals(Batch02RadiantWillowCatalog.SHARED_RADIANT_WILLOW_CONTRACT.particleTypeId())
                || !MODEL_PARENT.equals(Batch02RadiantWillowCatalog.SHARED_RADIANT_WILLOW_CONTRACT.itemModelParent())
                || RADIANT_NODE_COUNT != RadiantWillowTrajectory.RADIANT_NODE_COUNT
                || NEW_PARTICLES_DURING_EXTENSION != RadiantWillowTrajectory.NEW_EXTENSION_NODE_COUNT
                || MAX_FULL_ENVELOPE_BLOCKS != RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE) {
            throw new IllegalStateException("Batch02 radiant-willow geometry contract drifted");
        }
        Batch02RadiantWillowCatalog.validateCatalog(Batch02RadiantWillowCatalog.definitions());
    }
}
