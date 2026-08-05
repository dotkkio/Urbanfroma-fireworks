package com.urbanforma.fireworks.registry;

import com.urbanforma.fireworks.UrbanformaFireworks;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Common particle registry; the sprite provider is registered from the client-only bootstrap. */
public final class FireworksParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, UrbanformaFireworks.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HD_FIREWORK_SPARK =
            PARTICLE_TYPES.register("hd_firework_spark", () -> new SimpleParticleType(false));

    private FireworksParticles() {
    }

    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
