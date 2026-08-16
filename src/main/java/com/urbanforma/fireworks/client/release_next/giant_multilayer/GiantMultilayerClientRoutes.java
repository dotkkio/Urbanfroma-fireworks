package com.urbanforma.fireworks.client.release_next.giant_multilayer;

import java.util.Map;
import java.util.Objects;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerItemDefinitions;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerItemDefinitions.Definition;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerItemDefinitions.StableItem;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerTrajectory;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerTrajectory.Profile;

/**
 * Typed, client-only route from a staged stable item to its deterministic visual profile.
 * This class owns no packet listener, server queue, server particle, or server-side timer.
 */
public final class GiantMultilayerClientRoutes {
    private static final Map<StableItem, Profile> ROUTES = Map.ofEntries(
            Map.entry(StableItem.STARFALL_REGALIA, Profile.AURORA_CROWN),
            Map.entry(StableItem.CINDERFALL_PROCESSION, Profile.EMBER_CASCADE),
            Map.entry(StableItem.VERDANT_BLOOM, Profile.JADE_LOTUS),
            Map.entry(StableItem.IVORY_BELFRY, Profile.PEARL_CATHEDRAL),
            Map.entry(StableItem.BRAZEN_VORTEX, Profile.COPPER_HELIX),
            Map.entry(StableItem.AMETHYST_LUMINARIA, Profile.VIOLET_LANTERN),
            Map.entry(StableItem.HELIOS_FANFARE, Profile.SOLAR_FAN),
            Map.entry(StableItem.TIDAL_NORTHSTAR, Profile.AZURE_COMPASS),
            Map.entry(StableItem.VERMILION_PEONY_PAGEANT, Profile.CRIMSON_PETAL),
            Map.entry(StableItem.IMPERIAL_SUNRISE, Profile.GOLDEN_PAGODA));

    static {
        if (!staticContractHolds()) {
            throw new IllegalStateException("Giant multilayer client route drifted from staged item definitions");
        }
    }

    private GiantMultilayerClientRoutes() {
    }

    public static Profile profileFor(StableItem item) {
        return Objects.requireNonNull(ROUTES.get(item), "No giant multilayer client route for " + item);
    }

    public static GiantMultilayerClientProgram.Request requestFor(StableItem item, double x, double y, double z, long seed) {
        return new GiantMultilayerClientProgram.Request(x, y, z, seed, profileFor(item));
    }

    public static void enqueue(GiantMultilayerClientQueue queue, StableItem item, double x, double y, double z, long seed) {
        Objects.requireNonNull(queue, "queue").enqueue(requestFor(item, x, y, z, seed));
    }

    public static boolean staticContractHolds() {
        if (ROUTES.size() != GiantMultilayerItemDefinitions.REQUIRED_NEW_ITEM_COUNT) {
            return false;
        }
        for (Definition definition : GiantMultilayerItemDefinitions.values()) {
            if (ROUTES.get(definition.item()) != definition.profile()) {
                return false;
            }
        }
        return true;
    }
}
