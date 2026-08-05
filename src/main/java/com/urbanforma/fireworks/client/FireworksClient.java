package com.urbanforma.fireworks.client;

import com.urbanforma.fireworks.registry.FireworksEntities;
import com.urbanforma.fireworks.registry.FireworksParticles;
import net.minecraft.client.particle.FireworkParticles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only registration for the Fireworks add-on. */
public final class FireworksClient {
    private FireworksClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FireworksClient::registerRenderers);
        modBus.addListener(FireworksClient::registerParticleProviders);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, GrandFireworkClientEffects::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class,
                GrandFireworkClientEffects::onLoggingOut);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FireworksEntities.GRAND_FIREWORK_ROCKET.get(),
                GrandFireworkRocketRenderer::new);
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FireworksParticles.HD_FIREWORK_SPARK.get(), FireworkParticles.SparkProvider::new);
    }
}
