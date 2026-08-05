package com.urbanforma.fireworks.client.batch04;

import com.urbanforma.fireworks.client.saturn.SaturnClientPlan;
import com.urbanforma.fireworks.client.saturn.SaturnEmission;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnCatalog;
import com.urbanforma.fireworks.content.batch04.Batch04SaturnFirework;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-neutral batch04 plan lookup.
 *
 * <p>This class creates neither particles nor event listeners. Shared client scheduling stays untouched until the
 * integration owner explicitly adopts one of these immutable plans.</p>
 */
public final class Batch04SaturnClientPlans {
    private static final Map<String, SaturnClientPlan> PLANS = createPlans();

    private Batch04SaturnClientPlans() {
    }

    public static SaturnClientPlan require(String id) {
        SaturnClientPlan plan = PLANS.get(id);
        if (plan == null) {
            throw new IllegalArgumentException("Unknown batch04 Saturn client plan " + id);
        }
        return plan;
    }

    public static List<SaturnEmission> emissionsAtTick(String id, long payloadSeed, int tick) {
        return require(id).emissionsAtTick(payloadSeed, tick);
    }

    public static SaturnClientPlan.BudgetProof budgetProof(String id, long payloadSeed) {
        return require(id).budgetProof(payloadSeed);
    }

    private static Map<String, SaturnClientPlan> createPlans() {
        Map<String, SaturnClientPlan> plans = new HashMap<>();
        for (Batch04SaturnFirework firework : Batch04SaturnCatalog.all()) {
            SaturnClientPlan previous = plans.put(firework.id(), new SaturnClientPlan(firework.program()));
            if (previous != null) {
                throw new IllegalStateException("Duplicate batch04 Saturn client plan " + firework.id());
            }
        }
        return Map.copyOf(plans);
    }
}
