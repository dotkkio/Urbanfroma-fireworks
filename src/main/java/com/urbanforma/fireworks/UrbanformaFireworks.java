package com.urbanforma.fireworks;

import com.urbanforma.fireworks.client.FireworksClient;
import com.urbanforma.fireworks.content.MidsizeFireworkCatalog;
import com.urbanforma.fireworks.gametest.FireworksGameTests;
import com.urbanforma.fireworks.registry.FireworkCreativeSections;
import com.urbanforma.fireworks.registry.FireworksEntities;
import com.urbanforma.fireworks.registry.FireworksItems;
import com.urbanforma.fireworks.registry.FireworksParticles;
import com.urbanforma.fireworks.network.FireworksNetworking;
import com.urbanforma.neo.api.FunctionalCreativeCategoryRegistry;
import com.urbanforma.neo.registry.UrbanformaCreativeTabs;
import java.util.function.Consumer;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(UrbanformaFireworks.MOD_ID)
public final class UrbanformaFireworks {
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final ResourceLocation FUNCTIONAL_CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "fireworks");
    public static final ResourceLocation MIDSIZE_FUNCTIONAL_CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "midsize_fireworks");

    public UrbanformaFireworks(IEventBus modBus) {
        FireworksItems.register(modBus);
        FireworksEntities.register(modBus);
        FireworksParticles.register(modBus);
        FireworksNetworking.register(modBus);
        FunctionalCreativeCategoryRegistry.registerSectioned(
                MIDSIZE_FUNCTIONAL_CATEGORY_ID,
                "gui.urbanforma_fireworks.category.midsize",
                () -> FireworksItems.itemFor(MidsizeFireworkCatalog.entries().getFirst().style()).getDefaultInstance(),
                FireworkCreativeSections::midsizeSections);
        FunctionalCreativeCategoryRegistry.registerSectioned(
                FUNCTIONAL_CATEGORY_ID,
                "gui.urbanforma_fireworks.category.fireworks",
                () -> FireworksItems.GRAND_GOLDEN_SPHERE_FIREWORK.get().getDefaultInstance(),
                FireworkCreativeSections::sections);

        modBus.addListener(UrbanformaFireworks::addFunctionalTabItem);
        modBus.addListener(UrbanformaFireworks::registerDispenserBehavior);
        modBus.addListener(UrbanformaFireworks::registerBuiltInResourcePacks);
        modBus.addListener(FireworksGameTests::register);
        NeoForge.EVENT_BUS.addListener(FireworksGameTests::registerTemplate);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            FireworksClient.init(modBus);
        }
    }

    private static void addFunctionalTabItem(BuildCreativeModeTabContentsEvent event) {
        appendFunctionalTabItem(event.getTabKey(), event::accept);
    }

    /** Shared by the creative-tab event and the dedicated-server GameTest. */
    public static void appendFunctionalTabItem(
            ResourceKey<CreativeModeTab> tabKey, Consumer<ItemStack> output) {
        if (tabKey.equals(UrbanformaCreativeTabs.FUNCTIONAL_TAB_KEY)) {
            FireworksItems.defaultInstances().forEach(output);
        }
    }

    private static void registerDispenserBehavior(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> FireworksItems.all().forEach(holder -> {
            var firework = holder.get();
            DispenserBlock.registerBehavior(firework, new ProjectileDispenseBehavior(firework) {
                @Override
                protected void playSound(BlockSource blockSource) {
                    // The projectile itself emits the only ascent sound on its first server tick.
                }
            });
        }));
    }

    private static void registerBuiltInResourcePacks(AddPackFindersEvent event) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "resourcepacks/urbanforma_fireworks_hd"),
                PackType.CLIENT_RESOURCES,
                Component.translatable("pack.urbanforma_fireworks.hd.name"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP);
    }
}
