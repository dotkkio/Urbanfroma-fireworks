package com.urbanforma.fireworks.client;

import com.urbanforma.fireworks.registry.FireworksEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only registration for the Fireworks add-on. */
public final class FireworksClient {
    private FireworksClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FireworksClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, GrandFireworkClientEffects::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class,
                GrandFireworkClientEffects::onLoggingOut);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FireworksEntities.GRAND_FIREWORK_ROCKET.get(),
                GrandFireworkRocketRenderer::new);
    }
}
