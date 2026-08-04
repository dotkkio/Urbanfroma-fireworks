package com.urbanforma.fireworks.registry;

import com.urbanforma.fireworks.UrbanformaFireworks;
import com.urbanforma.fireworks.entity.GrandFireworkRocketEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FireworksEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, UrbanformaFireworks.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<GrandFireworkRocketEntity>> GRAND_FIREWORK_ROCKET =
            ENTITY_TYPES.register("grand_firework_rocket", id -> EntityType.Builder
                    .<GrandFireworkRocketEntity>of(GrandFireworkRocketEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(GrandFireworkRocketEntity.TRACKING_RANGE_CHUNKS)
                    .updateInterval(1)
                    .build(id.toString()));

    private FireworksEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
