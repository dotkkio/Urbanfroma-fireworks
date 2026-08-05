package com.urbanforma.fireworks.network;

import com.urbanforma.fireworks.UrbanformaFireworks;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class FireworksNetworking {
    /** v0.3.0 adds prototype styles and category-routed client visual scheduling. */
    public static final String NETWORK_VERSION = "8";

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
