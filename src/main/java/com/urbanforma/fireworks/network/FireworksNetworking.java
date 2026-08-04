package com.urbanforma.fireworks.network;

import com.urbanforma.fireworks.UrbanformaFireworks;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class FireworksNetworking {
    /** v0.2.9 changes the radiant-willow client behavior, so peers must use the same protocol. */
    public static final String NETWORK_VERSION = "7";

    private FireworksNetworking() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(FireworksNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(UrbanformaFireworks.MOD_ID).versioned(NETWORK_VERSION);
        registrar.playToClient(
                GrandFireworkBurstPayload.TYPE,
                GrandFireworkBurstPayload.STREAM_CODEC,
                GrandFireworkBurstPayload::handle);
    }
}
