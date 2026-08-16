package com.urbanforma.fireworks.registry;

import com.urbanforma.fireworks.UrbanformaFireworks;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.NormalFireworkCatalog;
import com.urbanforma.fireworks.content.OtherFireworkCatalog;
import com.urbanforma.fireworks.content.OtherExtraFireworkCatalog;
import com.urbanforma.fireworks.content.MidsizeFireworkCatalog;
import com.urbanforma.fireworks.content.IntegratedFireworkCatalog;
import com.urbanforma.fireworks.content.ReleaseNextFireworkCatalog;
import com.urbanforma.fireworks.world.item.FireworkRocketItem;
import com.urbanforma.fireworks.world.item.GrandGoldenSphereFireworkItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Stable creative order: accepted v0.2.5 styles, the v0.2.6 LED sphere spectrum, then radiant templates. */
public final class FireworksItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UrbanformaFireworks.MOD_ID);

    public static final DeferredItem<GrandGoldenSphereFireworkItem> GRAND_GOLDEN_SPHERE_FIREWORK =
            ITEMS.register(
                    FireworkStyle.GRAND_GOLDEN_SPHERE.id(),
                    () -> new GrandGoldenSphereFireworkItem(new net.minecraft.world.item.Item.Properties()));

    public static final DeferredItem<FireworkRocketItem> CINNABAR_AMBER_SPHERE = register(FireworkStyle.CINNABAR_AMBER_SPHERE);
    public static final DeferredItem<FireworkRocketItem> SAFFRON_CORAL_SPHERE = register(FireworkStyle.SAFFRON_CORAL_SPHERE);
    public static final DeferredItem<FireworkRocketItem> RUBY_SOLAR_SPHERE = register(FireworkStyle.RUBY_SOLAR_SPHERE);
    public static final DeferredItem<FireworkRocketItem> EMBER_CHAMPAGNE_DOUBLE_SPHERE = register(FireworkStyle.EMBER_CHAMPAGNE_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> VERMILION_GOLD_DOUBLE_SPHERE = register(FireworkStyle.VERMILION_GOLD_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> CORAL_ROSE_CROWN_SPHERE = register(FireworkStyle.CORAL_ROSE_CROWN_SPHERE);
    public static final DeferredItem<FireworkRocketItem> AMBER_SUNSTONE_WILLOW = register(FireworkStyle.AMBER_SUNSTONE_WILLOW);
    public static final DeferredItem<FireworkRocketItem> SCARLET_COPPER_WILLOW = register(FireworkStyle.SCARLET_COPPER_WILLOW);

    public static final DeferredItem<FireworkRocketItem> AQUA_ICE_SPHERE = register(FireworkStyle.AQUA_ICE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> COBALT_AZURE_SPHERE = register(FireworkStyle.COBALT_AZURE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> CYAN_PLATINUM_DOUBLE_SPHERE = register(FireworkStyle.CYAN_PLATINUM_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> GLACIER_TEAL_CROWN_SPHERE = register(FireworkStyle.GLACIER_TEAL_CROWN_SPHERE);
    public static final DeferredItem<FireworkRocketItem> POLAR_SILVER_WILLOW = register(FireworkStyle.POLAR_SILVER_WILLOW);

    public static final DeferredItem<FireworkRocketItem> EMERALD_PERIDOT_SPHERE = register(FireworkStyle.EMERALD_PERIDOT_SPHERE);
    public static final DeferredItem<FireworkRocketItem> AMETHYST_ORCHID_SPHERE = register(FireworkStyle.AMETHYST_ORCHID_SPHERE);
    public static final DeferredItem<FireworkRocketItem> SAPPHIRE_VIOLET_DOUBLE_SPHERE = register(FireworkStyle.SAPPHIRE_VIOLET_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> GARNET_TOPAZ_DOUBLE_SPHERE = register(FireworkStyle.GARNET_TOPAZ_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> OPAL_ROSE_CROWN_SPHERE = register(FireworkStyle.OPAL_ROSE_CROWN_SPHERE);
    public static final DeferredItem<FireworkRocketItem> JADE_PEARL_WILLOW = register(FireworkStyle.JADE_PEARL_WILLOW);

    public static final DeferredItem<FireworkRocketItem> CHAMPAGNE_WHITE_GOLD_SPHERE = register(FireworkStyle.CHAMPAGNE_WHITE_GOLD_SPHERE);
    public static final DeferredItem<FireworkRocketItem> COBALT_TITANIUM_DOUBLE_SPHERE = register(FireworkStyle.COBALT_TITANIUM_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> PLATINUM_ONYX_CROWN_SPHERE = register(FireworkStyle.PLATINUM_ONYX_CROWN_SPHERE);
    public static final DeferredItem<FireworkRocketItem> EMERALD_SILVER_CROWN_SPHERE = register(FireworkStyle.EMERALD_SILVER_CROWN_SPHERE);
    public static final DeferredItem<FireworkRocketItem> ROSE_GOLD_PEARL_WILLOW = register(FireworkStyle.ROSE_GOLD_PEARL_WILLOW);
    public static final DeferredItem<FireworkRocketItem> AMETHYST_PLATINUM_WILLOW = register(FireworkStyle.AMETHYST_PLATINUM_WILLOW);

    public static final DeferredItem<FireworkRocketItem> LED_SCARLET_SPHERE = register(FireworkStyle.LED_SCARLET_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_CORAL_SPHERE = register(FireworkStyle.LED_CORAL_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_AMBER_SPHERE = register(FireworkStyle.LED_AMBER_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_LEMON_SPHERE = register(FireworkStyle.LED_LEMON_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_CHARTREUSE_SPHERE = register(FireworkStyle.LED_CHARTREUSE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_MINT_SPHERE = register(FireworkStyle.LED_MINT_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_TEAL_SPHERE = register(FireworkStyle.LED_TEAL_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_CYAN_SPHERE = register(FireworkStyle.LED_CYAN_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_AZURE_SPHERE = register(FireworkStyle.LED_AZURE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_COBALT_SPHERE = register(FireworkStyle.LED_COBALT_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_VIOLET_SPHERE = register(FireworkStyle.LED_VIOLET_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_LILAC_SPHERE = register(FireworkStyle.LED_LILAC_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_MAGENTA_SPHERE = register(FireworkStyle.LED_MAGENTA_SPHERE);
    public static final DeferredItem<FireworkRocketItem> LED_ROSE_SPHERE = register(FireworkStyle.LED_ROSE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> AMBER_RADIANT_FIREWORK =
            register(FireworkStyle.AMBER_RADIANT_FIREWORK);
    public static final DeferredItem<FireworkRocketItem> AMBER_RADIANT_WILLOW_FIREWORK =
            register(FireworkStyle.AMBER_RADIANT_WILLOW_FIREWORK);
    public static final DeferredItem<FireworkRocketItem> GIANT_AMBER_RADIANT_FIREWORK =
            register(FireworkStyle.GIANT_AMBER_RADIANT_FIREWORK);
    public static final DeferredItem<FireworkRocketItem> HYBRID_AMBER_SPHERE_RADIANT =
            register(FireworkStyle.HYBRID_AMBER_SPHERE_RADIANT);
    public static final DeferredItem<FireworkRocketItem> SATURN_AMBER_DOUBLE_SPHERE =
            register(FireworkStyle.SATURN_AMBER_DOUBLE_SPHERE);
    public static final DeferredItem<FireworkRocketItem> GIANT_GOLDEN_WHITE_RADIAL_WILLOW_FIREWORK =
            register(FireworkStyle.GIANT_GOLDEN_WHITE_RADIAL_WILLOW_FIREWORK);

    /** Shared registration surface for the 98 typed ordinary batch definitions; each retains its style index. */
    private static final List<DeferredItem<FireworkRocketItem>> NORMAL_100_FIREWORKS =
            FireworkStyle.values().subList(
                            NormalFireworkCatalog.FIRST_STYLE_INDEX,
                            NormalFireworkCatalog.FIRST_STYLE_INDEX
                                    + NormalFireworkCatalog.NEW_ORDINARY_STYLE_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();
    private static final List<DeferredItem<FireworkRocketItem>> INTEGRATED_GIANT_FIREWORKS =
            FireworkStyle.values().subList(
                            com.urbanforma.fireworks.content.GiantFireworkCatalog.FIRST_STYLE_INDEX,
                            com.urbanforma.fireworks.content.GiantFireworkCatalog.FIRST_STYLE_INDEX
                                    + com.urbanforma.fireworks.content.GiantFireworkCatalog.INTEGRATED_GIANT_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();
    private static final List<DeferredItem<FireworkRocketItem>> OTHER_ORDINARY_FIREWORKS =
            FireworkStyle.values().subList(
                            OtherFireworkCatalog.FIRST_STYLE_INDEX,
                            OtherFireworkCatalog.FIRST_STYLE_INDEX
                    + OtherFireworkCatalog.OTHER_ORDINARY_STYLE_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();
    private static final List<DeferredItem<FireworkRocketItem>> OTHER_EXTRA_FIREWORKS =
            FireworkStyle.values().subList(
                            OtherExtraFireworkCatalog.FIRST_STYLE_INDEX,
                            OtherExtraFireworkCatalog.FIRST_STYLE_INDEX
                                    + OtherExtraFireworkCatalog.OTHER_EXTRA_STYLE_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();
    private static final List<DeferredItem<FireworkRocketItem>> MIDSIZE_FIREWORKS =
            FireworkStyle.values().subList(
                            MidsizeFireworkCatalog.FIRST_STYLE_INDEX,
                            MidsizeFireworkCatalog.FIRST_STYLE_INDEX + MidsizeFireworkCatalog.MIDSIZE_STYLE_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();
    private static final List<DeferredItem<FireworkRocketItem>> INTEGRATED_EXPANSION_FIREWORKS =
            FireworkStyle.values().subList(
                            IntegratedFireworkCatalog.FIRST_STYLE_INDEX,
                            IntegratedFireworkCatalog.FIRST_STYLE_INDEX
                                    + IntegratedFireworkCatalog.TOTAL_STYLE_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();
    private static final List<DeferredItem<FireworkRocketItem>> RELEASE_NEXT_FIREWORKS =
            FireworkStyle.values().subList(
                            ReleaseNextFireworkCatalog.FIRST_STYLE_INDEX,
                            ReleaseNextFireworkCatalog.FIRST_STYLE_INDEX
                                    + ReleaseNextFireworkCatalog.TOTAL_STYLE_COUNT)
                    .stream()
                    .map(FireworksItems::register)
                    .toList();

    private static final List<DeferredItem<? extends FireworkRocketItem>> ALL_FIREWORKS = allFireworks();

    private static List<DeferredItem<? extends FireworkRocketItem>> allFireworks() {
        List<DeferredItem<? extends FireworkRocketItem>> registered = new ArrayList<>(List.of(
            GRAND_GOLDEN_SPHERE_FIREWORK,
            CINNABAR_AMBER_SPHERE,
            SAFFRON_CORAL_SPHERE,
            RUBY_SOLAR_SPHERE,
            EMBER_CHAMPAGNE_DOUBLE_SPHERE,
            VERMILION_GOLD_DOUBLE_SPHERE,
            CORAL_ROSE_CROWN_SPHERE,
            AMBER_SUNSTONE_WILLOW,
            SCARLET_COPPER_WILLOW,
            AQUA_ICE_SPHERE,
            COBALT_AZURE_SPHERE,
            CYAN_PLATINUM_DOUBLE_SPHERE,
            GLACIER_TEAL_CROWN_SPHERE,
            POLAR_SILVER_WILLOW,
            EMERALD_PERIDOT_SPHERE,
            AMETHYST_ORCHID_SPHERE,
            SAPPHIRE_VIOLET_DOUBLE_SPHERE,
            GARNET_TOPAZ_DOUBLE_SPHERE,
            OPAL_ROSE_CROWN_SPHERE,
            JADE_PEARL_WILLOW,
            CHAMPAGNE_WHITE_GOLD_SPHERE,
            COBALT_TITANIUM_DOUBLE_SPHERE,
            PLATINUM_ONYX_CROWN_SPHERE,
            EMERALD_SILVER_CROWN_SPHERE,
            ROSE_GOLD_PEARL_WILLOW,
            AMETHYST_PLATINUM_WILLOW,
            LED_SCARLET_SPHERE,
            LED_CORAL_SPHERE,
            LED_AMBER_SPHERE,
            LED_LEMON_SPHERE,
            LED_CHARTREUSE_SPHERE,
            LED_MINT_SPHERE,
            LED_TEAL_SPHERE,
            LED_CYAN_SPHERE,
            LED_AZURE_SPHERE,
            LED_COBALT_SPHERE,
            LED_VIOLET_SPHERE,
            LED_LILAC_SPHERE,
            LED_MAGENTA_SPHERE,
            LED_ROSE_SPHERE,
            AMBER_RADIANT_FIREWORK,
            AMBER_RADIANT_WILLOW_FIREWORK,
            GIANT_AMBER_RADIANT_FIREWORK,
            HYBRID_AMBER_SPHERE_RADIANT,
            SATURN_AMBER_DOUBLE_SPHERE,
            GIANT_GOLDEN_WHITE_RADIAL_WILLOW_FIREWORK));
        registered.addAll(NORMAL_100_FIREWORKS);
        registered.addAll(INTEGRATED_GIANT_FIREWORKS);
        registered.addAll(OTHER_ORDINARY_FIREWORKS);
        registered.addAll(OTHER_EXTRA_FIREWORKS);
        registered.addAll(MIDSIZE_FIREWORKS);
        registered.addAll(INTEGRATED_EXPANSION_FIREWORKS);
        registered.addAll(RELEASE_NEXT_FIREWORKS);
        return List.copyOf(registered);
    }

    private FireworksItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static List<DeferredItem<? extends FireworkRocketItem>> all() {
        return ALL_FIREWORKS;
    }

    public static FireworkRocketItem itemFor(FireworkStyle style) {
        return holderFor(style).get();
    }

    public static DeferredItem<? extends FireworkRocketItem> holderFor(FireworkStyle style) {
        int styleIndex = style == null ? FireworkStyle.GRAND_GOLDEN_SPHERE.index() : style.index();
        return ALL_FIREWORKS.get(FireworkStyle.fromIndex(styleIndex).index());
    }

    public static List<ItemStack> defaultInstances() {
        return ALL_FIREWORKS.stream().map(holder -> holder.get().getDefaultInstance()).toList();
    }

    private static DeferredItem<FireworkRocketItem> register(FireworkStyle style) {
        return ITEMS.register(style.id(), () -> new FireworkRocketItem(style, new net.minecraft.world.item.Item.Properties()));
    }
}
