package com.urbanforma.fireworks.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.urbanforma.fireworks.UrbanformaFireworks;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.RadiantTrajectory;
import com.urbanforma.fireworks.content.RadiantWillowTrajectory;
import com.urbanforma.fireworks.content.WillowTrajectory;
import com.urbanforma.fireworks.entity.GrandFireworkRocketEntity;
import com.urbanforma.fireworks.network.FireworksNetworking;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import com.urbanforma.fireworks.registry.FireworkCreativeSections;
import com.urbanforma.fireworks.registry.FireworksEntities;
import com.urbanforma.fireworks.registry.FireworksItems;
import com.urbanforma.fireworks.world.item.FireworkRocketItem;
import com.urbanforma.neo.api.FunctionalCreativeCategory;
import com.urbanforma.neo.api.FunctionalCreativeCategoryRegistry;
import com.urbanforma.neo.client.UrbanformaCreativeCategory;
import com.urbanforma.neo.registry.UrbanformaCreativeTabs;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class FireworksGameTests {
    private static final String EMPTY_TEMPLATE_PATH = "empty";
    private static final ResourceLocation EMPTY_TEMPLATE =
            ResourceLocation.fromNamespaceAndPath(UrbanformaFireworks.MOD_ID, EMPTY_TEMPLATE_PATH);
    private static final String[] EXPECTED_IDS = {
            "grand_golden_sphere_firework", "cinnabar_amber_sphere", "saffron_coral_sphere", "ruby_solar_sphere",
            "ember_champagne_double_sphere", "vermilion_gold_double_sphere", "coral_rose_crown_sphere",
            "amber_sunstone_willow", "scarlet_copper_willow", "aqua_ice_sphere", "cobalt_azure_sphere",
            "cyan_platinum_double_sphere", "glacier_teal_crown_sphere", "polar_silver_willow",
            "emerald_peridot_sphere", "amethyst_orchid_sphere", "sapphire_violet_double_sphere",
            "garnet_topaz_double_sphere", "opal_rose_crown_sphere", "jade_pearl_willow",
            "champagne_white_gold_sphere", "cobalt_titanium_double_sphere", "platinum_onyx_crown_sphere",
            "emerald_silver_crown_sphere", "rose_gold_pearl_willow", "amethyst_platinum_willow",
            "led_scarlet_sphere", "led_coral_sphere", "led_amber_sphere", "led_lemon_sphere",
            "led_chartreuse_sphere", "led_mint_sphere", "led_teal_sphere", "led_cyan_sphere",
            "led_azure_sphere", "led_cobalt_sphere", "led_violet_sphere", "led_lilac_sphere",
            "led_magenta_sphere", "led_rose_sphere", "amber_radiant_firework",
            "amber_radiant_willow_firework"};
    private static final String[] EXPECTED_ZH_NAMES = {
            "巨型金色球形烟花", "朱砂琥珀球形烟花", "藏红珊瑚球形烟花", "红宝石日耀球形烟花", "余烬香槟双层球形烟花",
            "朱红鎏金双层球形烟花", "珊瑚玫瑰冠顶球形烟花", "琥珀日光长垂帘柳烟花", "绯红赤铜长垂帘柳烟花",
            "碧青冰晶球形烟花", "钴蓝天青球形烟花", "青蓝铂银双层球形烟花", "冰川青绿冠顶球形烟花",
            "极光银白长垂帘柳烟花", "翡翠橄榄石球形烟花", "紫晶兰花球形烟花", "蓝宝石紫晶双层球形烟花",
            "石榴石黄玉双层球形烟花", "欧泊蔷薇冠顶球形烟花", "碧玉珍珠长垂帘柳烟花", "香槟白金球形烟花",
            "钴蓝钛银双层球形烟花", "铂银曜石冠顶球形烟花", "翡翠银冠顶球形烟花", "玫瑰金珍珠长垂帘柳烟花",
            "紫晶铂银长垂帘柳烟花", "猩红辉耀球形烟花", "珊瑚曙光球形烟花", "琥珀熔光球形烟花",
            "柠檬日耀球形烟花", "黄绿春辉球形烟花", "薄荷极光球形烟花", "青绿潮光球形烟花",
            "青蓝冰辉球形烟花", "蔚蓝天穹球形烟花", "钴蓝深辉球形烟花", "紫罗兰星辉球形烟花",
            "丁香月辉球形烟花", "洋红霓彩球形烟花", "玫瑰晨辉球形烟花", "琥珀放射烟花",
            "琥珀放射长垂柳烟花"};
    private static final String[] EXPECTED_EN_NAMES = {
            "Grand Golden Sphere Firework", "Cinnabar Amber Sphere Firework", "Saffron Coral Sphere Firework",
            "Ruby Solar Sphere Firework", "Ember Champagne Double Sphere Firework", "Vermilion Gold Double Sphere Firework",
            "Coral Rose Crown Sphere Firework", "Amber Sunstone Long Willow Firework", "Scarlet Copper Long Willow Firework",
            "Aqua Ice Sphere Firework", "Cobalt Azure Sphere Firework", "Cyan Platinum Double Sphere Firework",
            "Glacier Teal Crown Sphere Firework", "Polar Silver Long Willow Firework", "Emerald Peridot Sphere Firework",
            "Amethyst Orchid Sphere Firework", "Sapphire Violet Double Sphere Firework", "Garnet Topaz Double Sphere Firework",
            "Opal Rose Crown Sphere Firework", "Jade Pearl Long Willow Firework", "Champagne White Gold Sphere Firework",
            "Cobalt Titanium Double Sphere Firework", "Platinum Onyx Crown Sphere Firework", "Emerald Silver Crown Sphere Firework",
            "Rose Gold Pearl Long Willow Firework", "Amethyst Platinum Long Willow Firework",
            "Scarlet Radiance Sphere Firework", "Coral Dawn Sphere Firework", "Amber Emberglow Sphere Firework",
            "Lemon Sunflare Sphere Firework", "Chartreuse Springlight Sphere Firework", "Mint Aurora Sphere Firework",
            "Teal Tideglow Sphere Firework", "Cyan Iceglow Sphere Firework", "Azure Skyglow Sphere Firework",
            "Cobalt Deepglow Sphere Firework", "Violet Starlight Sphere Firework", "Lilac Moonlight Sphere Firework",
            "Magenta Neon Glow Sphere Firework", "Rose Dawnfire Sphere Firework", "Amber Radiant Firework",
            "Amber Radiant Long Willow Firework"};
    private static final List<FireworkStyle.Shape> EXPECTED_SECTION_SHAPES = List.of(
            FireworkStyle.Shape.SPHERE,
            FireworkStyle.Shape.DOUBLE_SPHERE,
            FireworkStyle.Shape.CROWN_SPHERE,
            FireworkStyle.Shape.WILLOW_SPHERE,
            FireworkStyle.Shape.RADIANT,
            FireworkStyle.Shape.RADIANT_WILLOW);
    private static final List<Integer> EXPECTED_SECTION_ITEM_COUNTS = List.of(23, 6, 5, 6, 1, 1);
    private static final int SECTIONED_DISPLAY_SLOT_COUNT = 126;
    private static final int EXPECTED_LANGUAGE_KEY_COUNT = 49;
    private static final List<LedColorExpectation> EXPECTED_LED_PALETTE = List.of(
            new LedColorExpectation("led_scarlet_sphere", "#BC4040", "#E01B1B", "#FF3415", "#FFD1D1"),
            new LedColorExpectation("led_coral_sphere", "#DA8971", "#FE5D2E", "#FF7324", "#FFDCD1"),
            new LedColorExpectation("led_amber_sphere", "#D09F40", "#F4A815", "#FFCD0C", "#FFEFD1"),
            new LedColorExpectation("led_lemon_sphere", "#E2D458", "#FFEA2B", "#F8FF21", "#FFFAD1"),
            new LedColorExpectation("led_chartreuse_sphere", "#9AC952", "#A0ED2B", "#BEFF24", "#EDFFD1"),
            new LedColorExpectation("led_mint_sphere", "#6EC992", "#2BED77", "#24FF65", "#D1FFE3"),
            new LedColorExpectation("led_teal_sphere", "#4EACA5", "#25D0C3", "#24FFD9", "#D1FFFC"),
            new LedColorExpectation("led_cyan_sphere", "#4CA5C9", "#25B3ED", "#1ED5FF", "#D1F2FF"),
            new LedColorExpectation("led_azure_sphere", "#5184D1", "#297AF5", "#2163FF", "#D1E3FF"),
            new LedColorExpectation("led_cobalt_sphere", "#5365CF", "#2C49F3", "#242EFF", "#D1D8FF"),
            new LedColorExpectation("led_violet_sphere", "#7B5ECB", "#5F2BEF", "#4C24FF", "#DDD1FF"),
            new LedColorExpectation("led_lilac_sphere", "#9C6DD1", "#8A2CF5", "#7824FF", "#E7D1FF"),
            new LedColorExpectation("led_magenta_sphere", "#BE5FCA", "#D82BEE", "#F924FF", "#FAD1FF"),
            new LedColorExpectation("led_rose_sphere", "#CC6F74", "#F02B36", "#FF2724", "#FFD1D4"));
    private static final List<String> EXPECTED_SECTION_KEYS = List.of(
            "gui.urbanforma_fireworks.section.fireworks.sphere",
            "gui.urbanforma_fireworks.section.fireworks.double_sphere",
            "gui.urbanforma_fireworks.section.fireworks.crown_sphere",
            "gui.urbanforma_fireworks.section.fireworks.willow",
            "gui.urbanforma_fireworks.section.fireworks.radiant",
            "gui.urbanforma_fireworks.section.fireworks.radiant_willow");
    private static final Set<String> LEGACY_IDS = Set.of(
            "scarlet_compact_sphere", "tangerine_sphere", "lemon_sphere", "lime_sphere", "emerald_sphere",
            "aqua_sphere", "sapphire_sphere", "amethyst_sphere", "rose_sphere", "ice_sphere", "pearl_sphere",
            "magenta_sphere", "cobalt_grand_sphere", "crimson_gold_double_sphere", "amber_pearl_double_sphere",
            "emerald_gold_double_sphere", "cyan_silver_double_sphere", "violet_rose_double_sphere",
            "gold_teal_crown_sphere", "coral_champagne_crown_sphere", "blue_silver_crown_sphere",
            "cobalt_silver_willow_sphere", "emerald_pearl_willow_sphere", "scarlet_gold_willow_sphere");
    private static final long[] WILLOW_SAMPLE_SEEDS = {
            0x31E9A2C4D5F60718L,
            0x7ACCE5512B43906DL,
            0x0F14D3B8C26E957AL};
    private static final long[] RADIANT_SAMPLE_SEEDS = {
            0x6B11F94EA1D28C37L,
            0x17A5D3CE8F4B2906L,
            0xC4816E2A35B709FDL};
    private static final long[] RADIANT_WILLOW_SAMPLE_SEEDS = {
            0x49C7A6D3E5B8012FL,
            0x83D1F0247BAE65C9L,
            0x1F76C95A2DEB4083L};
    private static final double WILLOW_EPSILON = 1.0E-9D;
    private static final List<WillowExpectation> WILLOW_EXPECTATIONS = List.of(
            new WillowExpectation("amber_sunstone_willow", 34, 10, 23),
            new WillowExpectation("scarlet_copper_willow", 39, 13, 28),
            new WillowExpectation("polar_silver_willow", 37, 12, 26),
            new WillowExpectation("jade_pearl_willow", 39, 13, 28),
            new WillowExpectation("rose_gold_pearl_willow", 36, 11, 25),
            new WillowExpectation("amethyst_platinum_willow", 39, 13, 28));

    private FireworksGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(FireworksGameTests.class);
    }

    public static void registerTemplate(ServerStartingEvent event) {
        if (!GameTestHooks.isGametestEnabled()) {
            return;
        }

        var server = event.getServer();
        server.getStructureManager()
                .getOrCreate(EMPTY_TEMPLATE)
                .load(server.registryAccess().lookupOrThrow(Registries.BLOCK), emptyStructureTag());
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void registrationsCategoriesAndCreativeOrder(GameTestHelper helper) {
        List<FireworkStyle> styles = FireworkStyle.values();
        helper.assertTrue(FireworkStyle.count() == 42 && styles.size() == EXPECTED_IDS.length,
                "Fireworks v0.2.9 must expose exactly 42 stable styles");
        helper.assertTrue(styles.stream().filter(style -> style.family() == FireworkStyle.Family.WARM).count() == 10
                        && styles.stream().filter(style -> style.family() == FireworkStyle.Family.COOL).count() == 5
                        && styles.stream().filter(style -> style.family() == FireworkStyle.Family.JEWEL).count() == 6
                        && styles.stream().filter(style -> style.family() == FireworkStyle.Family.METALLIC).count() == 6
                        && styles.stream().filter(style -> style.family() == FireworkStyle.Family.LED_MONOCHROME).count() == 14,
                "Non-demonstration family distribution must be warm 10, cool 5, jewel 6, metallic 6, LED 14");
        helper.assertTrue(styles.stream().filter(style -> style.shape() == FireworkStyle.Shape.SPHERE).count() == 23
                        && styles.stream().filter(style -> style.shape() == FireworkStyle.Shape.DOUBLE_SPHERE).count() == 6
                        && styles.stream().filter(style -> style.shape() == FireworkStyle.Shape.CROWN_SPHERE).count() == 5
                        && styles.stream().filter(style -> style.shape() == FireworkStyle.Shape.WILLOW_SPHERE).count() == 6
                        && styles.stream().filter(style -> style.shape() == FireworkStyle.Shape.RADIANT).count() == 1
                        && styles.stream().filter(style -> style.shape() == FireworkStyle.Shape.RADIANT_WILLOW).count() == 1,
                "Shape distribution must be sphere 23, double 6, crown 5, willow 6, radiant 1, radiant willow 1");
        helper.assertTrue(FireworksNetworking.NETWORK_VERSION.equals("7"),
                "v0.2.9 must reject older peers through network protocol 7");
        helper.assertTrue(styles.stream().noneMatch(style -> LEGACY_IDS.contains(style.id())),
                "The v0.2.6 catalog must not retain legacy series ids");
        helper.assertTrue(FireworksItems.ITEMS.getEntries().size() == FireworkStyle.count()
                        && FireworksItems.all().size() == FireworkStyle.count(),
                "Every style must have exactly one registered item in creative order");
        helper.assertTrue(FireworksEntities.ENTITY_TYPES.getEntries().size() == 1,
                "The series must continue to use one shared rocket entity type");

        Set<String> styleIds = new HashSet<>();
        for (int index = 0; index < styles.size(); index++) {
            FireworkStyle style = styles.get(index);
            FireworkRocketItem item = FireworksItems.itemFor(style);
            ResourceLocation expectedId = ResourceLocation.fromNamespaceAndPath(UrbanformaFireworks.MOD_ID, style.id());
            helper.assertTrue(style.index() == index && styleIds.add(style.id())
                            && style.id().equals(EXPECTED_IDS[index])
                            && style.zhName().equals(EXPECTED_ZH_NAMES[index])
                            && style.enName().equals(EXPECTED_EN_NAMES[index]),
                    "Firework style indices and ids must be unique and ordered");
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).equals(expectedId)
                            && FireworksItems.all().get(index).get() == item
                            && FireworksItems.holderFor(style).get() == item
                            && item.style() == style,
                    "Registered firework item must retain its matching stable style");
        }
        helper.assertTrue(FireworksItems.all().stream().allMatch(holder ->
                        DispenserBlock.DISPENSER_REGISTRY.get(holder.get()) instanceof ProjectileDispenseBehavior),
                "Every series item must register its projectile dispenser behavior");
        assertLedMonochromePalette(helper, styles);
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(FireworksEntities.GRAND_FIREWORK_ROCKET.get()).equals(
                        ResourceLocation.fromNamespaceAndPath(UrbanformaFireworks.MOD_ID, "grand_firework_rocket"))
                        && FireworksEntities.GRAND_FIREWORK_ROCKET.get().getWidth() == 0.25F
                        && FireworksEntities.GRAND_FIREWORK_ROCKET.get().getHeight() == 0.25F
                        && FireworksEntities.GRAND_FIREWORK_ROCKET.get().clientTrackingRange()
                        == GrandFireworkRocketEntity.TRACKING_RANGE_CHUNKS
                        && FireworksEntities.GRAND_FIREWORK_ROCKET.get().updateInterval() == 1,
                "Shared rocket entity lifecycle settings drifted");
        helper.assertTrue(GrandFireworkRocketEntity.LAUNCH_SPEED == 1.45F
                        && GrandFireworkRocketEntity.TRACKING_RANGE_BLOCKS == 256
                        && FireworksEntities.GRAND_FIREWORK_ROCKET.get().create(helper.getLevel())
                        instanceof GrandFireworkRocketEntity,
                "Shared rocket entity must retain the required launch and tracking settings");

        List<FunctionalCreativeCategory> categories = FunctionalCreativeCategoryRegistry.categories().stream()
                .filter(category -> category.id().equals(UrbanformaFireworks.FUNCTIONAL_CATEGORY_ID))
                .toList();
        helper.assertTrue(categories.size() == 1,
                "Fireworks functional category must be registered exactly once");
        FunctionalCreativeCategory category = categories.getFirst();
        helper.assertTrue(category.translationKey().equals("gui.urbanforma_fireworks.category.fireworks")
                        && category.icon().is(FireworksItems.GRAND_GOLDEN_SPHERE_FIREWORK.get()),
                "Fireworks category must retain its original golden icon and translation key");
        assertShapeOnlySections(helper, category, styles);

        UrbanformaCreativeCategory[] builtInCategories =
                UrbanformaCreativeCategory.categoriesFor(UrbanformaCreativeTabs.FUNCTIONAL_TAB.get());
        List<FunctionalCreativeCategory> addOnCategories =
                UrbanformaCreativeCategory.addOnFunctionalCategoriesFor(UrbanformaCreativeTabs.FUNCTIONAL_TAB.get());
        helper.assertTrue(builtInCategories.length == 2
                        && builtInCategories[0] == UrbanformaCreativeCategory.FUNCTIONAL_PLATFORMS
                        && builtInCategories[1] == UrbanformaCreativeCategory.PHYSICS
                        && addOnCategories.size() == 1
                        && addOnCategories.getFirst().id().equals(UrbanformaFireworks.FUNCTIONAL_CATEGORY_ID),
                "Functional category order must remain platforms, physics, then fireworks");
        List<ItemStack> functionalTabItems = new ArrayList<>();
        List<ItemStack> nonFunctionalTabItems = new ArrayList<>();
        UrbanformaFireworks.appendFunctionalTabItem(UrbanformaCreativeTabs.FUNCTIONAL_TAB_KEY, functionalTabItems::add);
        UrbanformaFireworks.appendFunctionalTabItem(UrbanformaCreativeTabs.MAIN_TAB_KEY, nonFunctionalTabItems::add);
        assertStacksMatchStyles(helper, functionalTabItems, styles,
                "Functional tab must append every series item after its existing entries");
        helper.assertTrue(nonFunctionalTabItems.isEmpty(),
                "Firework items must not be appended to a non-functional Urbanforma tab");
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void packagedItemResourcesMirrorStyleCatalog(GameTestHelper helper) {
        JsonObject english = readJsonObject("assets/urbanforma_fireworks/lang/en_us.json");
        JsonObject chinese = readJsonObject("assets/urbanforma_fireworks/lang/zh_cn.json");
        List<FireworkStyle> styles = FireworkStyle.values();

        helper.assertTrue(english.size() == EXPECTED_LANGUAGE_KEY_COUNT
                        && chinese.size() == EXPECTED_LANGUAGE_KEY_COUNT,
                "Both language files must contain exactly the stable item and category keys");
        for (FireworkStyle style : styles) {
            String translationKey = "item." + UrbanformaFireworks.MOD_ID + "." + style.id();
            JsonObject model = readJsonObject(
                    "assets/urbanforma_fireworks/models/item/" + style.id() + ".json");
            helper.assertTrue(english.has(translationKey)
                            && english.get(translationKey).getAsString().equals(style.enName())
                            && chinese.has(translationKey)
                            && chinese.get(translationKey).getAsString().equals(style.zhName()),
                    "Every stable firework style must retain matching bilingual item names");
            helper.assertTrue(model.size() == 1
                            && model.has("parent")
                            && model.get("parent").getAsString().equals("minecraft:item/firework_rocket"),
                    "Every stable firework item model must inherit the vanilla rocket model only");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH, timeoutTicks = 30)
    public static void everyStyleLaunchesFromHandAndBlock(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 playerPosition = helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D));
        player.setPos(playerPosition);
        BlockPos launchBlock = new BlockPos(6, 1, 6);
        helper.setBlock(launchBlock, Blocks.STONE);
        BlockPos absoluteLaunchBlock = helper.absolutePos(launchBlock);

        for (FireworkStyle style : FireworkStyle.values()) {
            FireworkRocketItem item = FireworksItems.itemFor(style);
            ItemStack handStack = new ItemStack(item, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, handStack);
            item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(handStack.getCount() == 1 && hasRocketWithStyle(helper, playerPosition, 8.0D, style),
                    "Every survival hand launch must consume one item and preserve its style");

            ItemStack blockStack = new ItemStack(item, 1);
            player.setItemInHand(InteractionHand.MAIN_HAND, blockStack);
            item.useOn(new UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(absoluteLaunchBlock), Direction.UP, absoluteLaunchBlock, false)));
            helper.assertTrue(blockStack.isEmpty() && hasRocketWithStyle(helper, Vec3.atCenterOf(absoluteLaunchBlock), 8.0D, style),
                    "Every block launch must consume one item and preserve its style");
        }

        Player creativePlayer = helper.makeMockPlayer(GameType.CREATIVE);
        creativePlayer.getAbilities().instabuild = true;
        creativePlayer.setPos(playerPosition);
        FireworkRocketItem creativeItem = FireworksItems.itemFor(FireworkStyle.values().get(1));
        ItemStack creativeHand = new ItemStack(creativeItem, 1);
        creativePlayer.setItemInHand(InteractionHand.MAIN_HAND, creativeHand);
        creativeItem.use(helper.getLevel(), creativePlayer, InteractionHand.MAIN_HAND);
        helper.assertTrue(creativeHand.getCount() == 1,
                "Creative launches must not consume the selected firework");
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH, timeoutTicks = 190)
    public static void everyStyleLaunchesFromUpwardDispenser(GameTestHelper helper) {
        BlockPos dispenserPosition = new BlockPos(3, 1, 3);
        helper.setBlock(dispenserPosition, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        DispenserBlockEntity dispenser = helper.getBlockEntity(dispenserPosition);
        BlockPos absoluteDispenserPosition = helper.absolutePos(dispenserPosition);
        BlockSource source = new BlockSource(
                helper.getLevel(),
                absoluteDispenserPosition,
                helper.getLevel().getBlockState(absoluteDispenserPosition),
                dispenser);
        List<FireworkStyle> styles = FireworkStyle.values();
        helper.assertTrue(source.state().getValue(DispenserBlock.FACING) == Direction.UP,
                "The GameTest dispenser source must face upward");

        for (int index = 0; index < styles.size(); index++) {
            FireworkStyle style = styles.get(index);
            long triggerTick = 1L + index * 4L;
            helper.runAtTickTime(triggerTick, () -> {
                FireworkRocketItem item = FireworksItems.itemFor(style);
                ItemStack stack = item.getDefaultInstance();
                dispenser.setItem(0, stack);
                dispenser.setChanged();
                DispenseItemBehavior behavior = DispenserBlock.DISPENSER_REGISTRY.get(item);
                behavior.dispense(source, stack);
                helper.assertTrue(stack.isEmpty(),
                        "The registered upward dispenser behavior must consume one selected firework item");

                Projectile projectile = item.asProjectile(
                        helper.getLevel(), source.center(), item.getDefaultInstance(), Direction.UP);
                helper.assertTrue(projectile instanceof GrandFireworkRocketEntity rocket && rocket.style() == style,
                        "The registered dispenser item must create the matching shared rocket style");
                var config = item.createDispenseConfig();
                item.shoot(
                        projectile,
                        Direction.UP.getStepX(),
                        Direction.UP.getStepY(),
                        Direction.UP.getStepZ(),
                        config.power(),
                        config.uncertainty());
                helper.assertTrue(Math.abs(projectile.getDeltaMovement().x) < 0.001D
                                && projectile.getDeltaMovement().y >= 1.44D
                                && Math.abs(projectile.getDeltaMovement().z) < 0.001D,
                        "An upward dispenser item must configure the required vertical launch speed");
                if (style == styles.getLast()) {
                    helper.succeed();
                }
            });
        }
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void stylesPersistAndBurstPayloadsRoundTrip(GameTestHelper helper) {
        for (FireworkStyle style : FireworkStyle.values()) {
            GrandFireworkRocketEntity original = new GrandFireworkRocketEntity(
                    helper.getLevel(), 2.5D, 2.0D, 2.5D, null, style);
            original.launchVertically();
            CompoundTag saved = original.saveWithoutId(new CompoundTag());
            helper.assertTrue(saved.getInt("StyleIndex") == style.index(),
                    "Rocket NBT must save the style index");
            GrandFireworkRocketEntity restored = new GrandFireworkRocketEntity(
                    helper.getLevel(), 2.5D, 2.0D, 2.5D, null);
            restored.load(saved);
            helper.assertTrue(restored.style() == style && restored.styleIndex() == style.index(),
                    "Rocket NBT must restore the exact style");

            GrandFireworkBurstPayload payload = new GrandFireworkBurstPayload(12.5D, 64.0D, -7.25D, 42L, style.index());
            RegistryFriendlyByteBuf encoded = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            GrandFireworkBurstPayload.STREAM_CODEC.encode(encoded, payload);
            RegistryFriendlyByteBuf decodedBuffer = new RegistryFriendlyByteBuf(encoded.copy(), helper.getLevel().registryAccess());
            GrandFireworkBurstPayload decoded = GrandFireworkBurstPayload.STREAM_CODEC.decode(decodedBuffer);
            helper.assertTrue(decoded.equals(payload) && decoded.style() == style,
                    "Burst payload encoding must preserve every style index and field");
        }
        helper.assertTrue(new GrandFireworkBurstPayload(1.0D, 2.0D, 3.0D, 4L).style()
                        == FireworkStyle.GRAND_GOLDEN_SPHERE
                        && new GrandFireworkBurstPayload(1.0D, 2.0D, 3.0D, 4L, -1).style()
                        == FireworkStyle.GRAND_GOLDEN_SPHERE,
                "Legacy and malformed payload style indices must safely fall back to gold");
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void willowProfilesRespectCurveBudgetAndEnvelope(GameTestHelper helper) {
        List<FireworkStyle> willowStyles = FireworkStyle.values().stream()
                .filter(style -> style.shape() == FireworkStyle.Shape.WILLOW_SPHERE)
                .toList();
        helper.assertTrue(willowStyles.size() == WILLOW_EXPECTATIONS.size(),
                "Exactly six v0.2.6 styles must use the spherical long-willow profile");
        helper.assertTrue(WillowTrajectory.BRANCH_COUNT == 160
                        && WillowTrajectory.SEGMENTS_PER_BRANCH == 30
                        && WillowTrajectory.NODES_PER_BURST == 4_800
                        && WillowTrajectory.SEGMENT_INTERVAL_TICKS == 2
                        && WillowTrajectory.EMISSION_TICKS
                                == (WillowTrajectory.SEGMENTS_PER_BRANCH - 1)
                                * WillowTrajectory.SEGMENT_INTERVAL_TICKS + 1
                        && WillowTrajectory.EMISSION_TICKS >= 58
                        && WillowTrajectory.EMISSION_TICKS <= 60
                        && WillowTrajectory.SHORT_LIVED_SEGMENT_COUNT == 9
                        && WillowTrajectory.SHORT_LIFETIME_MIN == 14
                        && WillowTrajectory.SHORT_LIFETIME_MAX == 22
                        && WillowTrajectory.INITIAL_RADIUS == 5.0D
                        && WillowTrajectory.VERTICAL_SPHERE_SCALE == 0.80D,
                "The shared v0.2.6 willow trajectory must retain its fixed branch, node, interval, and inner-node contract");
        helper.assertTrue(hasNoBranchProgramConcurrencyOrFifoCap(),
                "Branch rendering must not expose a mod-side concurrency, occupancy, or FIFO cap");

        for (WillowExpectation expectation : WILLOW_EXPECTATIONS) {
            FireworkStyle style = FireworkStyle.fromId(expectation.id());
            var profile = style.willowProfile();
            helper.assertTrue(style.shape() == FireworkStyle.Shape.WILLOW_SPHERE
                            && profile != null
                            && profile.branchCount() == 160
                            && profile.segmentsPerBranch() == 30
                            && profile.coreStarCount() == 0
                            && profile.horizontalReach() == expectation.horizontalReach()
                            && profile.upwardRise() == expectation.rise()
                            && profile.downwardFall() == expectation.drop()
                            && profile.minLifetime() == 160
                            && profile.maxLifetime() == 180,
                    "Each approved willow style must retain its exact profile parameters");
            helper.assertTrue(profile.branchCount() * profile.segmentsPerBranch() + profile.coreStarCount()
                            == WillowTrajectory.NODES_PER_BURST,
                    "Each willow profile must emit 160 branches times 30 segments with no central core nodes");
            helper.assertTrue(style.fullEnvelope() <= 120
                            && hasThreeColorGradient(style)
                            && supportsApprovedWillowScheduling(profile),
                    "Each willow must fit the two-tick expansion window and shared particle-budget contract");
            for (long payloadSeed : WILLOW_SAMPLE_SEEDS) {
                helper.assertTrue(samplesApprovedWillowTrajectory(style, payloadSeed),
                        "Willow branches must be deterministic, three-dimensional, center-free, and envelope-safe");
            }
        }

        for (FireworkStyle style : FireworkStyle.values()) {
            if (style.shape() != FireworkStyle.Shape.WILLOW_SPHERE) {
                helper.assertTrue(style.willowProfile() == null,
                        "Only willow styles may declare a willow profile");
            }
            if (style.shape() != FireworkStyle.Shape.RADIANT) {
                helper.assertTrue(style.radiantProfile() == null,
                        "Only radiant styles may declare a radiant profile");
            }
            if (style.shape() != FireworkStyle.Shape.RADIANT_WILLOW) {
                helper.assertTrue(style.radiantWillowProfile() == null,
                        "Only radiant willow styles may declare a radiant willow profile");
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void radiantProfileRespectsCurveBudgetAndEnvelope(GameTestHelper helper) {
        FireworkStyle style = FireworkStyle.AMBER_RADIANT_FIREWORK;
        FireworkStyle.RadiantProfile profile = style.radiantProfile();
        helper.assertTrue(style.index() == 40
                        && style.id().equals("amber_radiant_firework")
                        && style.family() == FireworkStyle.Family.WARM
                        && style.shape() == FireworkStyle.Shape.RADIANT
                        && style.flightTicks() == 100
                        && style.diameter() == 108
                        && style.fullEnvelope() == 108
                        && style.totalStarCount() == 4_800
                        && style.mainStarCount() == 960
                        && style.secondaryStarCount() == 2_880
                        && style.accentStarCount() == 960
                        && style.starsPerTick() == 160
                        && style.trailTier() == FireworkStyle.TrailTier.GRAND
                        && style.primaryColor().equals(FireworkStyle.Rgb.fromHex("#FF6B19"))
                        && style.secondaryColor().equals(FireworkStyle.Rgb.fromHex("#FFA424"))
                        && style.accentColor().equals(FireworkStyle.Rgb.fromHex("#FFE1A6"))
                        && profile != null,
                "The v0.2.7 radiant item must retain its exact stable style definition");
        helper.assertTrue(profile.branchCount() == 160
                        && profile.segmentsPerBranch() == 30
                        && profile.coreSegmentCount() == 3
                        && profile.minLifetime() == 58
                        && profile.maxLifetime() == 62
                        && profile.initialRadius() == 3.5D
                        && profile.maximumRadius() == 48.0D
                        && profile.verticalScale() == 0.94D
                        && profile.bendStartMin() == 0.38D
                        && profile.bendStartMax() == 0.46D
                        && profile.terminalDrop() == 9.0D,
                "The radiant profile must retain its approved dense three-dimensional curve");
        helper.assertTrue(RadiantTrajectory.BRANCH_COUNT == 160
                        && RadiantTrajectory.SEGMENTS_PER_BRANCH == 30
                        && RadiantTrajectory.NODES_PER_BURST == 4_800
                        && RadiantTrajectory.EMISSION_TICKS == 30
                        && RadiantTrajectory.BRANCHES_PER_TICK == 160
                        && RadiantTrajectory.CORE_SEGMENT_COUNT == 3
                        && RadiantTrajectory.CORE_LIFETIME_MIN == 8
                        && RadiantTrajectory.CORE_LIFETIME_MAX == 12
                        && RadiantTrajectory.STAR_LIFETIME_MIN == 58
                        && RadiantTrajectory.STAR_LIFETIME_MAX == 62
                        && RadiantTrajectory.BRANCH_COUNT <= 216
                        && RadiantTrajectory.BRANCH_COUNT * 4 <= 720
                        && hasNoBranchProgramConcurrencyOrFifoCap(),
                "Radiant rings must fit the shared complete-ring budget with no active-burst cap");
        for (long payloadSeed : RADIANT_SAMPLE_SEEDS) {
            helper.assertTrue(samplesApprovedRadiantTrajectory(style, payloadSeed),
                    "Radiant branches must be deterministic, dense, three-dimensional, briefly centered, and envelope-safe");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void radiantWillowProfileRespectsContinuousCurveBudgetAndEnvelope(GameTestHelper helper) {
        FireworkStyle style = FireworkStyle.AMBER_RADIANT_WILLOW_FIREWORK;
        FireworkStyle.RadiantWillowProfile profile = style.radiantWillowProfile();
        helper.assertTrue(style.index() == 41
                        && style.id().equals("amber_radiant_willow_firework")
                        && style.family() == FireworkStyle.Family.WARM
                        && style.shape() == FireworkStyle.Shape.RADIANT_WILLOW
                        && style.flightTicks() == 100
                        && style.diameter() == 108
                        && style.fullEnvelope() == 220
                        && style.totalStarCount() == 4_800
                        && style.mainStarCount() == 960
                        && style.secondaryStarCount() == 2_880
                        && style.accentStarCount() == 960
                        && style.starsPerTick() == 160
                        && style.trailTier() == FireworkStyle.TrailTier.GRAND
                        && style.primaryColor().equals(FireworkStyle.Rgb.fromHex("#FF6B19"))
                        && style.secondaryColor().equals(FireworkStyle.Rgb.fromHex("#FFA424"))
                        && style.accentColor().equals(FireworkStyle.Rgb.fromHex("#FFE1A6"))
                        && style.willowProfile() == null
                        && style.radiantProfile() == null
                        && profile != null,
                "The v0.2.9 radiant willow item must retain its stable continuous profile");
        helper.assertTrue(profile.branchCount() == 160
                        && profile.radiantSegmentsPerBranch() == 30
                        && profile.managedFirstRadiantSegment() == 3
                        && profile.managedSegmentsPerBranch() == 27
                        && profile.minExtensionTicks() == 100
                        && profile.maxExtensionTicks() == 140
                        && profile.additionalRadialExtension() == 18.0D
                        && profile.bendStartMin() == 0.28D
                        && profile.bendStartMax() == 0.42D
                        && profile.terminalDrop() == 66.0D
                        && profile.maximumLateralSway() == 7.5D,
                "The radiant willow profile must preserve endpoint continuity and the long curved drop");
        helper.assertTrue(RadiantWillowTrajectory.BRANCH_COUNT == 160
                        && RadiantWillowTrajectory.RADIANT_NODE_COUNT == 4_800
                        && RadiantWillowTrajectory.MANAGED_NODE_COUNT == 4_320
                        && RadiantWillowTrajectory.NEW_EXTENSION_NODE_COUNT == 0
                        && RadiantWillowTrajectory.TOTAL_NODE_COUNT == 4_800
                        && RadiantWillowTrajectory.MANAGED_FIRST_RADIANT_SEGMENT == 3
                        && RadiantWillowTrajectory.MANAGED_SEGMENTS_PER_BRANCH == 27
                        && RadiantWillowTrajectory.MIN_EXTENSION_TICKS == 100
                        && RadiantWillowTrajectory.MAX_EXTENSION_TICKS == 140
                        && RadiantWillowTrajectory.BRANCHES_PER_TICK == 160
                        && RadiantWillowTrajectory.MAX_PARTICLES_PER_TICK == 160
                        && RadiantWillowTrajectory.MAX_GLOBAL_PARTICLES_PER_TICK == 640
                        && RadiantWillowTrajectory.MAX_GLOBAL_PARTICLES_PER_TICK <= 720
                        && RadiantWillowTrajectory.MAX_TERMINAL_RETIREMENTS_PER_BRANCH == 5
                        && RadiantWillowTrajectory.TERMINAL_RETIREMENT_START_PROGRESS == 0.35D
                        && RadiantWillowTrajectory.TERMINAL_RETIREMENT_INTERVAL_MIN_TICKS == 12
                        && RadiantWillowTrajectory.TERMINAL_RETIREMENT_INTERVAL_MAX_TICKS == 17
                        && RadiantWillowTrajectory.RETIREMENT_FLICKER_MIN_TICKS == 5
                        && RadiantWillowTrajectory.RETIREMENT_FLICKER_MAX_TICKS == 9
                        && RadiantWillowTrajectory.FINAL_DRAIN_FLICKER_MAX_DELAY_TICKS == 6
                        && RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE == 220.0D
                        && RadiantWillowTrajectory.extensionOutrunsTerminalRetirement(100)
                        && RadiantWillowTrajectory.extensionOutrunsTerminalRetirement(140)
                        && hasNoBranchProgramConcurrencyOrFifoCap()
                        && hasApprovedRadiantWillowClientBytecode(),
                "The continuous radiant willow must reuse whole rings without a mod-side concurrency cap");
        for (long payloadSeed : RADIANT_WILLOW_SAMPLE_SEEDS) {
            helper.assertTrue(samplesApprovedRadiantWillowTrajectory(style, payloadSeed),
                    "Radiant willow branches must preserve same-particle continuity, curves, determinism, and envelope safety");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH)
    public static void radiantWillowRetirementFlickerIsSeededAndParticleBound(GameTestHelper helper) {
        FireworkStyle style = FireworkStyle.AMBER_RADIANT_WILLOW_FIREWORK;
        for (long payloadSeed : RADIANT_WILLOW_SAMPLE_SEEDS) {
            helper.assertTrue(samplesApprovedRadiantWillowRetirementFlicker(style, payloadSeed),
                    "Radiant willow retirement flicker must be stable, staggered, visible in-window, and particle-bound");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH, timeoutTicks = 135)
    public static void longFlightBurstsWithoutDamageOrBlockChanges(GameTestHelper helper) {
        BlockPos marker = new BlockPos(5, 1, 5);
        BlockPos bystanderSupport = new BlockPos(6, 1, 6);
        helper.setBlock(marker, Blocks.GOLD_BLOCK);
        helper.setBlock(bystanderSupport, Blocks.STONE);
        LivingEntity bystander = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(6, 2, 6));
        float startingHealth = bystander.getHealth();

        Vec3 launchPosition = helper.absoluteVec(new Vec3(2.5D, 12.0D, 2.5D));
        FireworkStyle style = FireworkStyle.values().stream()
                .max(java.util.Comparator.comparingInt(FireworkStyle::flightTicks))
                .orElseThrow();
        GrandFireworkRocketEntity rocket = new GrandFireworkRocketEntity(
                helper.getLevel(), launchPosition.x, launchPosition.y, launchPosition.z, null, style);
        rocket.launchVertically();
        helper.getLevel().addFreshEntity(rocket);

        helper.runAtTickTime(115L, () -> helper.assertTrue(
                rocket.isAlive()
                        && rocket.life() < style.flightTicks()
                        && rocket.getY() - launchPosition.y >= 160.0D,
                "The tallest series firework must remain in flight near its configured height"));
        helper.runAtTickTime(121L, () -> {
            helper.assertTrue(!rocket.isAlive(), "The tallest series firework must burst on its configured flight tick");
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, marker);
            helper.assertTrue(bystander.isAlive() && bystander.getHealth() == startingHealth,
                    "Series fireworks must neither damage entities nor alter nearby blocks");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH, timeoutTicks = 115)
    public static void radiantTemplatesBurstAtOneHundredTicks(GameTestHelper helper) {
        Vec3 launchPosition = helper.absoluteVec(new Vec3(2.5D, 12.0D, 2.5D));
        Vec3 willowLaunchPosition = helper.absoluteVec(new Vec3(5.5D, 12.0D, 2.5D));
        GrandFireworkRocketEntity radiantRocket = new GrandFireworkRocketEntity(
                helper.getLevel(),
                launchPosition.x,
                launchPosition.y,
                launchPosition.z,
                null,
                FireworkStyle.AMBER_RADIANT_FIREWORK);
        GrandFireworkRocketEntity willowRocket = new GrandFireworkRocketEntity(
                helper.getLevel(),
                willowLaunchPosition.x,
                willowLaunchPosition.y,
                willowLaunchPosition.z,
                null,
                FireworkStyle.AMBER_RADIANT_WILLOW_FIREWORK);
        radiantRocket.launchVertically();
        willowRocket.launchVertically();
        helper.getLevel().addFreshEntity(radiantRocket);
        helper.getLevel().addFreshEntity(willowRocket);

        helper.runAtTickTime(98L, () -> helper.assertTrue(
                radiantRocket.isAlive()
                        && willowRocket.isAlive()
                        && radiantRocket.life() < FireworkStyle.AMBER_RADIANT_FIREWORK.flightTicks()
                        && willowRocket.life() < FireworkStyle.AMBER_RADIANT_WILLOW_FIREWORK.flightTicks()
                        && radiantRocket.getY() - launchPosition.y >= 140.0D
                        && willowRocket.getY() - willowLaunchPosition.y >= 140.0D,
                "Both radiant templates must still be rising near their configured 145-block burst height"));
        helper.runAtTickTime(104L, () -> {
            helper.assertTrue(!radiantRocket.isAlive() && !willowRocket.isAlive(),
                    "Both radiant templates must burst after their configured 100 ticks");
            helper.succeed();
        });
    }

    private static JsonObject readJsonObject(String resourcePath) {
        try (InputStream resource = FireworksGameTests.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (resource == null) {
                throw new AssertionError("Missing packaged resource: " + resourcePath);
            }
            try (Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException exception) {
            throw new AssertionError("Unable to read packaged resource: " + resourcePath, exception);
        }
    }

    @GameTest(templateNamespace = UrbanformaFireworks.MOD_ID, template = EMPTY_TEMPLATE_PATH, timeoutTicks = 40)
    public static void obstacleCollisionBurstsSafelyWithoutBreakingTheObstacle(GameTestHelper helper) {
        BlockPos obstacle = new BlockPos(2, 10, 2);
        BlockPos bystanderSupport = new BlockPos(2, 1, 2);
        helper.setBlock(obstacle, Blocks.GOLD_BLOCK);
        helper.setBlock(bystanderSupport, Blocks.STONE);
        LivingEntity bystander = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 2, 2));
        float startingHealth = bystander.getHealth();

        Vec3 launchPosition = helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D));
        GrandFireworkRocketEntity rocket = new GrandFireworkRocketEntity(
                helper.getLevel(), launchPosition.x, launchPosition.y, launchPosition.z, null,
                FireworkStyle.values().get(1));
        rocket.launchVertically();
        helper.getLevel().addFreshEntity(rocket);

        helper.runAtTickTime(10L, () -> {
            helper.assertTrue(!rocket.isAlive(), "Collision with a block must burst every series rocket immediately");
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, obstacle);
            helper.assertTrue(bystander.isAlive() && bystander.getHealth() == startingHealth,
                    "A collision burst must not damage entities or break blocks");
            helper.succeed();
        });
    }

    private static void assertStacksMatchStyles(
            GameTestHelper helper, List<ItemStack> stacks, List<FireworkStyle> styles, String failureMessage) {
        helper.assertTrue(stacks.size() == styles.size(), failureMessage);
        for (int index = 0; index < styles.size(); index++) {
            helper.assertTrue(stacks.get(index).is(FireworksItems.itemFor(styles.get(index))), failureMessage);
        }
    }

    private static void assertLedMonochromePalette(GameTestHelper helper, List<FireworkStyle> styles) {
        List<FireworkStyle.LedMonochromeDefinition> definitions = FireworkStyle.ledMonochromeDefinitions();
        helper.assertTrue(definitions.size() == EXPECTED_LED_PALETTE.size(),
                "The LED sphere catalog must contain exactly the fourteen non-neutral LED colors");

        Set<FireworkStyle.Rgb> neutralColors = Set.of(
                FireworkStyle.Rgb.fromHex("#FFFFFF"),
                FireworkStyle.Rgb.fromHex("#E3E3E3"),
                FireworkStyle.Rgb.fromHex("#8E8E8E"),
                FireworkStyle.Rgb.fromHex("#555555"),
                FireworkStyle.Rgb.fromHex("#000000"));
        for (int offset = 0; offset < EXPECTED_LED_PALETTE.size(); offset++) {
            LedColorExpectation expected = EXPECTED_LED_PALETTE.get(offset);
            FireworkStyle.LedMonochromeDefinition definition = definitions.get(offset);
            FireworkStyle style = styles.get(26 + offset);
            helper.assertTrue(definition.index() == 26 + offset
                            && definition.id().equals(expected.id())
                            && definition.ledReferenceColor().equals(FireworkStyle.Rgb.fromHex(expected.ledReference()))
                            && definition.primaryColor().equals(FireworkStyle.Rgb.fromHex(expected.primary()))
                            && definition.secondaryColor().equals(FireworkStyle.Rgb.fromHex(expected.secondary()))
                            && definition.accentColor().equals(FireworkStyle.Rgb.fromHex(expected.accent()))
                            && !neutralColors.contains(definition.ledReferenceColor())
                            && style == FireworkStyle.fromId(expected.id())
                            && style.family() == FireworkStyle.Family.LED_MONOCHROME
                            && style.shape() == FireworkStyle.Shape.SPHERE
                            && style.willowProfile() == null
                            && style.flightTicks() == 84
                            && style.diameter() == 96
                            && style.fullEnvelope() == 96
                            && style.totalStarCount() == 2_160
                            && style.starsPerTick() == 180
                            && style.trailTier() == FireworkStyle.TrailTier.STANDARD
                            && style.outerLifetime() == 90
                            && style.innerLifetime() == 78
                            && style.accentLifetime() == 66
                            && hasThreeColorGradient(style),
                    "Each LED sphere must retain its exact vivid three-color definition and shared giant-sphere profile");
        }
    }

    private static void assertShapeOnlySections(
            GameTestHelper helper, FunctionalCreativeCategory category, List<FireworkStyle> styles) {
        List<UrbanformaCreativeCategory.Section> sections = category.sections();
        helper.assertTrue(category.hasSections() && sections.size() == EXPECTED_SECTION_SHAPES.size(),
                "Fireworks must expose exactly six labeled shape sections");

        Set<String> sectionPaths = new HashSet<>();
        int expectedBannerRow = 0;
        List<UrbanformaCreativeCategory.SectionBanner> banners = category.sectionBanners();
        helper.assertTrue(banners.size() == sections.size(),
                "Every non-empty firework shape section must expose one panel banner");
        for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++) {
            FireworkStyle.Shape shape = EXPECTED_SECTION_SHAPES.get(sectionIndex);
            List<FireworkStyle> expectedStyles = stylesForShape(styles, shape);
            UrbanformaCreativeCategory.Section section = sections.get(sectionIndex);
            UrbanformaCreativeCategory.SectionBanner banner = banners.get(sectionIndex);
            helper.assertTrue(expectedStyles.size() == EXPECTED_SECTION_ITEM_COUNTS.get(sectionIndex)
                            && section.translationKey().equals(EXPECTED_SECTION_KEYS.get(sectionIndex))
                            && section.theme().tone() == UrbanformaCreativeCategory.SectionTone.DARK_PANEL
                            && section.theme().pattern()
                            == UrbanformaCreativeCategory.SectionPattern.HORIZONTAL_BARS
                            && section.paths().equals(expectedStyles.stream().map(FireworkStyle::id).toList()),
                    "Firework sections must remain ordered only by shape with one shared neutral dark theme");
            assertStacksMatchStyles(helper, section.stacks(), expectedStyles,
                    "Each firework section must contain only its matching shape styles");

            Set<FireworkStyle.Family> families = new HashSet<>();
            for (FireworkStyle style : expectedStyles) {
                if (style.family() != FireworkStyle.Family.DEMONSTRATION) {
                    families.add(style.family());
                }
                helper.assertTrue(sectionPaths.add(style.id()),
                        "A firework item must belong to one shape section only");
            }
            int expectedFamilyCount = switch (shape) {
                case SPHERE -> 5;
                case DOUBLE_SPHERE, CROWN_SPHERE, WILLOW_SPHERE -> 4;
                case RADIANT, RADIANT_WILLOW -> 1;
            };
            helper.assertTrue(families.size() == expectedFamilyCount,
                    "Each shape section must mix the internal families without creating a color-based section");
            helper.assertTrue(banner.row() == expectedBannerRow
                            && banner.translationKey().equals(section.translationKey())
                            && banner.theme().equals(section.theme()),
                    "Shape section banners must follow the same order and row layout as their sections");
            expectedBannerRow += 1 + rowsFor(section.stacks().size());
        }

        helper.assertTrue(sectionPaths.size() == styles.size()
                        && styles.stream().allMatch(style -> sectionPaths.contains(style.id())),
                "Shape sections must partition all 42 firework styles without a color-family section");
        assertSectionedDisplayStacks(helper, category.stacks(), sections,
                "Fireworks sectioned stacks must reserve one complete banner row before each shape group");

        List<UrbanformaCreativeCategory.Section> directSections = FireworkCreativeSections.sections();
        helper.assertTrue(directSections.size() == sections.size()
                        && directSections.stream().map(UrbanformaCreativeCategory.Section::translationKey).toList()
                        .equals(EXPECTED_SECTION_KEYS),
                "Fireworks creative section helper must expose the registered shape order");
    }

    private static List<FireworkStyle> stylesForShape(
            List<FireworkStyle> styles, FireworkStyle.Shape shape) {
        return styles.stream()
                .filter(style -> style.shape() == shape)
                .toList();
    }

    private static void assertSectionedDisplayStacks(
            GameTestHelper helper,
            List<ItemStack> displayStacks,
            List<UrbanformaCreativeCategory.Section> sections,
            String failureMessage) {
        int expectedSize = 0;
        for (UrbanformaCreativeCategory.Section section : sections) {
            expectedSize += 9 + rowsFor(section.stacks().size()) * 9;
        }
        helper.assertTrue(expectedSize == SECTIONED_DISPLAY_SLOT_COUNT
                        && displayStacks.size() == SECTIONED_DISPLAY_SLOT_COUNT,
                failureMessage);

        int offset = 0;
        for (UrbanformaCreativeCategory.Section section : sections) {
            for (int headerSlot = 0; headerSlot < 9; headerSlot++) {
                helper.assertTrue(displayStacks.get(offset + headerSlot).isEmpty(), failureMessage);
            }
            offset += 9;
            for (int itemIndex = 0; itemIndex < section.stacks().size(); itemIndex++) {
                helper.assertTrue(displayStacks.get(offset + itemIndex).is(section.stacks().get(itemIndex).getItem()),
                        failureMessage);
            }
            int nextOffset = offset + rowsFor(section.stacks().size()) * 9;
            for (int paddingSlot = offset + section.stacks().size(); paddingSlot < nextOffset; paddingSlot++) {
                helper.assertTrue(displayStacks.get(paddingSlot).isEmpty(), failureMessage);
            }
            offset = nextOffset;
        }
        helper.assertTrue(offset == displayStacks.size(), failureMessage);
    }

    private static int rowsFor(int itemCount) {
        return (itemCount + 8) / 9;
    }

    private static boolean hasRocketWithStyle(GameTestHelper helper, Vec3 center, double radius, FireworkStyle style) {
        return rocketsNear(helper, center, radius).stream().anyMatch(rocket -> rocket.style() == style);
    }

    private static boolean hasThreeColorGradient(FireworkStyle style) {
        return !style.primaryColor().equals(style.secondaryColor())
                && !style.primaryColor().equals(style.accentColor())
                && !style.secondaryColor().equals(style.accentColor());
    }

    /** Samples the common trajectory only; GameTests must never load client particle classes. */
    private static boolean samplesApprovedWillowTrajectory(FireworkStyle style, long payloadSeed) {
        FireworkStyle.WillowProfile profile = style.willowProfile();
        if (profile == null || !WillowTrajectory.fitsEnvelope(style, payloadSeed)) {
            return false;
        }

        WillowTrajectory.Bounds bounds = WillowTrajectory.conservativeBounds(profile, payloadSeed);
        if (!bounds.fitsWithin(style.fullEnvelope()) || bounds.maxSpan() > 120.0D + WILLOW_EPSILON) {
            return false;
        }

        int primaryNodes = 0;
        int secondaryNodes = 0;
        int accentNodes = 0;
        int shortLivedNodes = 0;
        int primaryTwinkles = 0;
        int secondaryTwinkles = 0;
        int accentTwinkles = 0;
        boolean hasPositiveX = false;
        boolean hasNegativeX = false;
        boolean hasPositiveY = false;
        boolean hasNegativeY = false;
        boolean hasPositiveZ = false;
        boolean hasNegativeZ = false;
        boolean hasSeedVariation = false;
        double centerExclusionRadius = WillowTrajectory.CENTER_CLEARANCE_RADIUS;

        for (int branchIndex = 0; branchIndex < profile.branchCount(); branchIndex++) {
            WillowTrajectory.Branch branch = WillowTrajectory.branch(profile, payloadSeed, branchIndex);
            if (!hasApprovedBranchRandomization(branch, profile)) {
                return false;
            }
            hasPositiveX |= branch.direction().x > 0.25D;
            hasNegativeX |= branch.direction().x < -0.25D;
            hasPositiveY |= branch.direction().y > 0.25D;
            hasNegativeY |= branch.direction().y < -0.25D;
            hasPositiveZ |= branch.direction().z > 0.25D;
            hasNegativeZ |= branch.direction().z < -0.25D;

            for (int segmentIndex = 0; segmentIndex < profile.segmentsPerBranch(); segmentIndex++) {
                WillowTrajectory.BranchSample sample =
                        WillowTrajectory.sample(profile, branch, segmentIndex);
                WillowTrajectory.BranchSample repeated =
                        WillowTrajectory.sample(profile, payloadSeed, branchIndex, segmentIndex);
                if (!sameSample(sample, repeated)
                        || sample.position().length() + WILLOW_EPSILON < centerExclusionRadius
                        || sample.tangent().length() < WillowTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                        || sample.tangent().length() > WillowTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON
                        || sample.colorTone() < 0.0F
                        || sample.colorTone() >= 1.0F
                        || sample.colorBand() != expectedColorBand(segmentIndex)) {
                    return false;
                }

                if (WillowTrajectory.isShortLivedSegment(segmentIndex)) {
                    shortLivedNodes++;
                    if (sample.lifetime() < WillowTrajectory.SHORT_LIFETIME_MIN
                            || sample.lifetime() > WillowTrajectory.SHORT_LIFETIME_MAX
                            || sample.twinkles()) {
                        return false;
                    }
                } else if (sample.lifetime() < profile.minLifetime() || sample.lifetime() > profile.maxLifetime()) {
                    return false;
                }

                switch (sample.colorBand()) {
                    case PRIMARY -> {
                        primaryNodes++;
                        if (sample.twinkles()) {
                            primaryTwinkles++;
                        }
                    }
                    case SECONDARY -> {
                        secondaryNodes++;
                        if (sample.twinkles()) {
                            secondaryTwinkles++;
                        }
                    }
                    case ACCENT -> {
                        accentNodes++;
                        if (sample.twinkles()) {
                            accentTwinkles++;
                        }
                    }
                }
            }

            WillowTrajectory.BranchSample alternateSeed = WillowTrajectory.sample(
                    profile, payloadSeed ^ 0x9E3779B97F4A7C15L, branchIndex, profile.segmentsPerBranch() / 2);
            WillowTrajectory.BranchSample currentSeed = WillowTrajectory.sample(
                    profile, payloadSeed, branchIndex, profile.segmentsPerBranch() / 2);
            hasSeedVariation |= !sameVec(currentSeed.position(), alternateSeed.position());
        }

        int expectedPrimaryNodes = profile.branchCount() * 6;
        int expectedSecondaryNodes = profile.branchCount() * 18;
        int expectedAccentNodes = profile.branchCount() * 6;
        int expectedShortLivedNodes = WillowTrajectory.BRANCH_COUNT * WillowTrajectory.SHORT_LIVED_SEGMENT_COUNT;
        return primaryNodes == expectedPrimaryNodes
                && secondaryNodes == expectedSecondaryNodes
                && accentNodes == expectedAccentNodes
                && shortLivedNodes == expectedShortLivedNodes
                && primaryTwinkles == 0
                && secondaryTwinkles > 0
                && accentTwinkles > 0
                && hasPositiveX
                && hasNegativeX
                && hasPositiveY
                && hasNegativeY
                && hasPositiveZ
                && hasNegativeZ
                && hasSeedVariation;
    }

    /** Samples only common-side radiant geometry; this must remain safe in a dedicated-server GameTest. */
    private static boolean samplesApprovedRadiantTrajectory(FireworkStyle style, long payloadSeed) {
        FireworkStyle.RadiantProfile profile = style.radiantProfile();
        if (profile == null || !RadiantTrajectory.fitsEnvelope(profile, payloadSeed, style.fullEnvelope())) {
            return false;
        }

        RadiantTrajectory.Bounds bounds = RadiantTrajectory.conservativeBounds(profile, payloadSeed);
        if (!bounds.fitsWithin(style.fullEnvelope()) || bounds.maxSpan() > 108.0D + WILLOW_EPSILON) {
            return false;
        }

        int primaryNodes = 0;
        int secondaryNodes = 0;
        int accentNodes = 0;
        int coreNodes = 0;
        int twinklingNormalNodes = 0;
        boolean hasEarlyTwinklePhase = false;
        boolean hasLateTwinklePhase = false;
        boolean hasPositiveX = false;
        boolean hasNegativeX = false;
        boolean hasPositiveY = false;
        boolean hasNegativeY = false;
        boolean hasPositiveZ = false;
        boolean hasNegativeZ = false;
        boolean hasSeedVariation = false;

        for (int branchIndex = 0; branchIndex < profile.branchCount(); branchIndex++) {
            RadiantTrajectory.Branch branch = RadiantTrajectory.branch(profile, payloadSeed, branchIndex);
            if (!hasApprovedRadiantBranchRandomization(branch, profile)) {
                return false;
            }
            hasPositiveX |= branch.direction().x > 0.25D;
            hasNegativeX |= branch.direction().x < -0.25D;
            hasPositiveY |= branch.direction().y > 0.25D;
            hasNegativeY |= branch.direction().y < -0.25D;
            hasPositiveZ |= branch.direction().z > 0.25D;
            hasNegativeZ |= branch.direction().z < -0.25D;

            for (int segmentIndex = 0; segmentIndex < profile.segmentsPerBranch(); segmentIndex++) {
                RadiantTrajectory.BranchSample sample =
                        RadiantTrajectory.sample(profile, branch, segmentIndex);
                RadiantTrajectory.BranchSample repeated =
                        RadiantTrajectory.sample(profile, payloadSeed, branchIndex, segmentIndex);
                if (!sameRadiantSample(sample, repeated)
                        || sample.tangent().length() < RadiantTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                        || sample.tangent().length() > RadiantTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON
                        || sample.colorTone() < 0.0F
                        || sample.colorTone() >= 1.0F
                        || sample.twinklePhase() < 0.0F
                        || sample.twinklePhase() >= 1.0F
                        || sample.colorBand() != expectedRadiantColorBand(segmentIndex)) {
                    return false;
                }

                if (RadiantTrajectory.isCoreSegment(segmentIndex)) {
                    coreNodes++;
                    if (sample.lifetime() < RadiantTrajectory.CORE_LIFETIME_MIN
                            || sample.lifetime() > RadiantTrajectory.CORE_LIFETIME_MAX
                            || sample.twinkles()) {
                        return false;
                    }
                } else {
                    if (sample.position().length() <= profile.initialRadius() + 2.0D
                            || sample.lifetime() < profile.minLifetime()
                            || sample.lifetime() > profile.maxLifetime()) {
                        return false;
                    }
                    if (sample.twinkles()) {
                        twinklingNormalNodes++;
                        hasEarlyTwinklePhase |= sample.twinklePhase() < 0.5F;
                        hasLateTwinklePhase |= sample.twinklePhase() >= 0.5F;
                    }
                }

                switch (sample.colorBand()) {
                    case PRIMARY -> primaryNodes++;
                    case SECONDARY -> secondaryNodes++;
                    case ACCENT -> accentNodes++;
                }
            }

            RadiantTrajectory.BranchSample endpoint =
                    RadiantTrajectory.sample(profile, branch, profile.segmentsPerBranch() - 1);
            double unbentEndpointY = branch.direction().y * branch.radialReach() * profile.verticalScale();
            double terminalDrop = unbentEndpointY - endpoint.position().y;
            if (terminalDrop < profile.terminalDrop() * 0.92D - WILLOW_EPSILON
                    || terminalDrop > profile.terminalDrop() * 1.05D + WILLOW_EPSILON) {
                return false;
            }

            RadiantTrajectory.BranchSample alternateSeed = RadiantTrajectory.sample(
                    profile, payloadSeed ^ 0x9E3779B97F4A7C15L, branchIndex, profile.segmentsPerBranch() / 2);
            RadiantTrajectory.BranchSample currentSeed =
                    RadiantTrajectory.sample(profile, payloadSeed, branchIndex, profile.segmentsPerBranch() / 2);
            hasSeedVariation |= !sameVec(currentSeed.position(), alternateSeed.position());
        }

        return primaryNodes == profile.branchCount() * 6
                && secondaryNodes == profile.branchCount() * 18
                && accentNodes == profile.branchCount() * 6
                && coreNodes == profile.branchCount() * profile.coreSegmentCount()
                && twinklingNormalNodes > 0
                && hasEarlyTwinklePhase
                && hasLateTwinklePhase
                && hasPositiveX
                && hasNegativeX
                && hasPositiveY
                && hasNegativeY
                && hasPositiveZ
                && hasNegativeZ
                && hasSeedVariation;
    }

    /** Samples only common-side geometry; dedicated-server GameTests must not load client particle classes. */
    private static boolean samplesApprovedRadiantWillowTrajectory(FireworkStyle style, long payloadSeed) {
        FireworkStyle.RadiantWillowProfile profile = style.radiantWillowProfile();
        if (profile == null) {
            return false;
        }
        int extensionDuration = RadiantWillowTrajectory.extensionDurationTicks(profile, payloadSeed);
        if (extensionDuration < profile.minExtensionTicks()
                || extensionDuration > profile.maxExtensionTicks()
                || !RadiantWillowTrajectory.fitsEnvelope(profile, payloadSeed, extensionDuration)) {
            return false;
        }

        RadiantWillowTrajectory.Bounds bounds =
                RadiantWillowTrajectory.conservativeBounds(profile, payloadSeed, extensionDuration);
        if (!bounds.fitsWithin(style.fullEnvelope())
                || bounds.maxSpan() > RadiantWillowTrajectory.APPROVED_FULL_ENVELOPE + WILLOW_EPSILON) {
            return false;
        }

        int coreNodes = 0;
        int managedNodes = 0;
        int primaryNodes = 0;
        int secondaryNodes = 0;
        int accentNodes = 0;
        int managedPrimaryNodes = 0;
        int managedSecondaryNodes = 0;
        int managedAccentNodes = 0;
        boolean hasPositiveX = false;
        boolean hasNegativeX = false;
        boolean hasPositiveY = false;
        boolean hasNegativeY = false;
        boolean hasPositiveZ = false;
        boolean hasNegativeZ = false;
        boolean hasSeedVariation = false;
        boolean hasTerminalRecycling = false;
        int firstEligibleRetirementTick = (int) Math.ceil(
                extensionDuration * RadiantWillowTrajectory.TERMINAL_RETIREMENT_START_PROGRESS);

        for (int branchIndex = 0; branchIndex < profile.branchCount(); branchIndex++) {
            RadiantWillowTrajectory.Branch branch =
                    RadiantWillowTrajectory.branch(profile, payloadSeed, branchIndex);
            if (!hasApprovedRadiantWillowBranchRandomization(branch, profile)) {
                return false;
            }
            hasPositiveX |= branch.radiantBranch().direction().x > 0.25D;
            hasNegativeX |= branch.radiantBranch().direction().x < -0.25D;
            hasPositiveY |= branch.radiantBranch().direction().y > 0.25D;
            hasNegativeY |= branch.radiantBranch().direction().y < -0.25D;
            hasPositiveZ |= branch.radiantBranch().direction().z > 0.25D;
            hasNegativeZ |= branch.radiantBranch().direction().z < -0.25D;

            int retirementStart = RadiantWillowTrajectory.terminalRetirementStartTick(
                    profile, branch, extensionDuration);
            int finalRetirementCount = RadiantWillowTrajectory.terminalRetirementCount(
                    profile, branch, extensionDuration, extensionDuration);
            if (RadiantWillowTrajectory.terminalRetirementCount(
                            profile,
                            branch,
                            Math.max(0, firstEligibleRetirementTick - 1),
                            extensionDuration)
                    != 0
                    || retirementStart < firstEligibleRetirementTick
                    || retirementStart > firstEligibleRetirementTick + 3
                    || finalRetirementCount < 1
                    || finalRetirementCount > RadiantWillowTrajectory.MAX_TERMINAL_RETIREMENTS_PER_BRANCH
                    || RadiantWillowTrajectory.terminalRetirementTick(
                                    profile,
                                    branch,
                                    RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH - 1,
                                    extensionDuration)
                            != RadiantWillowTrajectory.NEVER_RETIRES
                    || !RadiantWillowTrajectory.extensionOutrunsTerminalRetirement(extensionDuration)
                    || RadiantWillowTrajectory.extensionToTerminalRetirementSpeedRatio(extensionDuration) < 3.0D) {
                return false;
            }
            hasTerminalRecycling = true;

            for (int retirementOrdinal = 0;
                    retirementOrdinal < RadiantWillowTrajectory.MAX_TERMINAL_RETIREMENTS_PER_BRANCH - 1;
                    retirementOrdinal++) {
                int interval = RadiantWillowTrajectory.terminalRetirementIntervalTicks(branch, retirementOrdinal);
                if (interval < RadiantWillowTrajectory.TERMINAL_RETIREMENT_INTERVAL_MIN_TICKS
                        || interval > RadiantWillowTrajectory.TERMINAL_RETIREMENT_INTERVAL_MAX_TICKS) {
                    return false;
                }
            }

            Vec3 outerRadiantPosition = RadiantWillowTrajectory.radiantEndpoint(profile, branch);
            for (int radiantSegment = 0;
                    radiantSegment < RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH;
                    radiantSegment++) {
                RadiantTrajectory.ColorBand expectedColorBand = expectedRadiantColorBand(radiantSegment);
                switch (expectedColorBand) {
                    case PRIMARY -> primaryNodes++;
                    case SECONDARY -> secondaryNodes++;
                    case ACCENT -> accentNodes++;
                }

                if (!RadiantWillowTrajectory.isManagedRadiantSegment(radiantSegment)) {
                    RadiantTrajectory.BranchSample core = RadiantTrajectory.sample(
                            profile.radiantProfile(), branch.radiantBranch(), radiantSegment);
                    if (core.colorBand() != expectedColorBand
                            || core.lifetime() < RadiantTrajectory.CORE_LIFETIME_MIN
                            || core.lifetime() > RadiantTrajectory.CORE_LIFETIME_MAX
                            || core.twinkles()) {
                        return false;
                    }
                    coreNodes++;
                    continue;
                }

                RadiantWillowTrajectory.ManagedParticleSample initial = RadiantWillowTrajectory.managedParticle(
                        profile, branch, radiantSegment, 0, extensionDuration);
                int middleAge = extensionDuration / 2;
                RadiantWillowTrajectory.ManagedParticleSample middle = RadiantWillowTrajectory.managedParticle(
                        profile, branch, radiantSegment, middleAge, extensionDuration);
                RadiantWillowTrajectory.ManagedParticleSample finished = RadiantWillowTrajectory.managedParticle(
                        profile, branch, radiantSegment, extensionDuration, extensionDuration);
                RadiantWillowTrajectory.ManagedParticleSample repeated = RadiantWillowTrajectory.managedParticle(
                        profile, branch, radiantSegment, middleAge, extensionDuration);
                Vec3 radiantPosition = RadiantWillowTrajectory.radiantPosition(profile, branch, radiantSegment);
                int expectedRetirementTick = RadiantWillowTrajectory.terminalRetirementTick(
                        profile, branch, radiantSegment, extensionDuration);
                if (!sameManagedRadiantParticleSample(middle, repeated)
                        || !sameManagedParticleIdentity(initial, middle)
                        || !sameManagedParticleIdentity(middle, finished)
                        || initial.managedSegmentIndex()
                                != RadiantWillowTrajectory.managedSegmentIndex(radiantSegment)
                        || initial.colorBand() != expectedColorBand
                        || initial.extensionAgeTicks() != 0
                        || middle.extensionAgeTicks() != middleAge
                        || finished.extensionAgeTicks() != extensionDuration
                        || initial.extensionDurationTicks() != extensionDuration
                        || middle.extensionDurationTicks() != extensionDuration
                        || finished.extensionDurationTicks() != extensionDuration
                        || !sameVec(initial.position(), radiantPosition)
                        || !sameVec(initial.position(), RadiantWillowTrajectory.position(profile, branch, radiantSegment, 0.0D))
                        || !sameVec(middle.position(), RadiantWillowTrajectory.position(
                                profile,
                                branch,
                                radiantSegment,
                                RadiantWillowTrajectory.extensionProgress(middleAge, extensionDuration)))
                        || !sameVec(finished.position(), RadiantWillowTrajectory.position(
                                profile, branch, radiantSegment, 1.0D))
                        || initial.tangent().length() < RadiantWillowTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                        || initial.tangent().length() > RadiantWillowTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON
                        || middle.tangent().length() < RadiantWillowTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                        || middle.tangent().length() > RadiantWillowTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON
                        || initial.terminalRetirementTick() != expectedRetirementTick
                        || finished.terminalRetirementTick() != expectedRetirementTick
                        || finished.retired()
                                != RadiantWillowTrajectory.isTerminalNodeRetired(
                                        profile, branch, radiantSegment, extensionDuration, extensionDuration)
                        || RadiantWillowTrajectory.remainingLifetimeTicks(
                                        profile, branch, radiantSegment, extensionDuration)
                                != (expectedRetirementTick == RadiantWillowTrajectory.NEVER_RETIRES
                                        ? extensionDuration + 1
                                        : expectedRetirementTick + 1)) {
                    return false;
                }
                if (radiantSegment == RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH - 1
                        && (finished.retired() || !sameVec(initial.position(), outerRadiantPosition))) {
                    return false;
                }

                managedNodes++;
                switch (initial.colorBand()) {
                    case PRIMARY -> managedPrimaryNodes++;
                    case SECONDARY -> managedSecondaryNodes++;
                    case ACCENT -> managedAccentNodes++;
                }
            }

            long alternateSeed = payloadSeed ^ 0x9E3779B97F4A7C15L;
            int alternateDuration = RadiantWillowTrajectory.extensionDurationTicks(profile, alternateSeed);
            RadiantWillowTrajectory.Branch alternateBranch =
                    RadiantWillowTrajectory.branch(profile, alternateSeed, branchIndex);
            int probeSegment = RadiantWillowTrajectory.MANAGED_FIRST_RADIANT_SEGMENT
                    + RadiantWillowTrajectory.MANAGED_SEGMENTS_PER_BRANCH / 2;
            RadiantWillowTrajectory.ManagedParticleSample currentSeed = RadiantWillowTrajectory.managedParticle(
                    profile, branch, probeSegment, extensionDuration / 2, extensionDuration);
            RadiantWillowTrajectory.ManagedParticleSample alternateSeedSample =
                    RadiantWillowTrajectory.managedParticle(
                            profile, alternateBranch, probeSegment, alternateDuration / 2, alternateDuration);
            hasSeedVariation |= !sameVec(currentSeed.position(), alternateSeedSample.position());
        }

        return coreNodes
                        == RadiantWillowTrajectory.BRANCH_COUNT
                                * RadiantWillowTrajectory.SHORT_LIVED_RADIANT_SEGMENTS
                && managedNodes == RadiantWillowTrajectory.MANAGED_NODE_COUNT
                && coreNodes + managedNodes == RadiantWillowTrajectory.RADIANT_NODE_COUNT
                && primaryNodes == profile.branchCount() * 6
                && secondaryNodes == profile.branchCount() * 18
                && accentNodes == profile.branchCount() * 6
                && managedPrimaryNodes == profile.branchCount() * 3
                && managedSecondaryNodes == profile.branchCount() * 18
                && managedAccentNodes == profile.branchCount() * 6
                && hasPositiveX
                && hasNegativeX
                && hasPositiveY
                && hasNegativeY
                && hasPositiveZ
                && hasNegativeZ
                        && hasSeedVariation
                        && hasTerminalRecycling;
    }

    /** Common-side proof for the per-particle retirement windows; this deliberately does not load client classes. */
    private static boolean samplesApprovedRadiantWillowRetirementFlicker(
            FireworkStyle style, long payloadSeed) {
        FireworkStyle.RadiantWillowProfile profile = style.radiantWillowProfile();
        if (profile == null) {
            return false;
        }
        int extensionDuration = RadiantWillowTrajectory.extensionDurationTicks(profile, payloadSeed);
        Set<RadiantWillowTrajectory.RetirementFlicker> finalWindows = new HashSet<>();
        Set<Integer> finalWindowStarts = new HashSet<>();
        boolean hasPhaseZero = false;
        boolean hasPhaseOne = false;
        boolean hasTerminalWindow = false;
        int managedCount = 0;

        for (int branchIndex = 0; branchIndex < profile.branchCount(); branchIndex++) {
            RadiantWillowTrajectory.Branch branch =
                    RadiantWillowTrajectory.branch(profile, payloadSeed, branchIndex);
            for (int radiantSegment = RadiantWillowTrajectory.MANAGED_FIRST_RADIANT_SEGMENT;
                    radiantSegment < RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH;
                    radiantSegment++) {
                RadiantWillowTrajectory.RetirementFlicker finalWindow =
                        RadiantWillowTrajectory.finalRetirementFlicker(branch, radiantSegment);
                RadiantWillowTrajectory.RetirementFlicker repeatedFinalWindow =
                        RadiantWillowTrajectory.finalRetirementFlicker(branch, radiantSegment);
                if (!sameRetirementFlicker(finalWindow, repeatedFinalWindow)
                        || !validRetirementFlicker(finalWindow)
                        || finalWindow.endTick()
                                > RadiantWillowTrajectory.FINAL_DRAIN_FLICKER_MAX_DELAY_TICKS
                                        + RadiantWillowTrajectory.RETIREMENT_FLICKER_MAX_TICKS) {
                    return false;
                }
                finalWindows.add(finalWindow);
                finalWindowStarts.add(finalWindow.startTick());
                hasPhaseZero |= finalWindow.cadencePhase() == 0;
                hasPhaseOne |= finalWindow.cadencePhase() == 1;

                RadiantWillowTrajectory.ManagedParticleSample sample =
                        RadiantWillowTrajectory.managedParticle(
                                profile, branch, radiantSegment, 0, extensionDuration);
                RadiantWillowTrajectory.RetirementFlicker terminalWindow = sample.terminalFlicker();
                RadiantWillowTrajectory.RetirementFlicker repeatedTerminalWindow =
                        RadiantWillowTrajectory.terminalRetirementFlicker(
                                profile, branch, radiantSegment, extensionDuration);
                if (!sameRetirementFlicker(terminalWindow, repeatedTerminalWindow)
                        || sample.terminalRetirementTick()
                                != RadiantWillowTrajectory.terminalRetirementTick(
                                        profile, branch, radiantSegment, extensionDuration)) {
                    return false;
                }
                if (terminalWindow.enabled()) {
                    if (!validRetirementFlicker(terminalWindow)
                            || terminalWindow.endTick() != sample.terminalRetirementTick()
                            || !terminalWindow.activeAt(terminalWindow.startTick())) {
                        return false;
                    }
                    hasTerminalWindow = true;
                } else if (sample.terminalRetirementTick() != RadiantWillowTrajectory.NEVER_RETIRES) {
                    return false;
                }
                managedCount++;
            }
        }

        return managedCount == RadiantWillowTrajectory.MANAGED_NODE_COUNT
                && finalWindows.size() > 1
                && finalWindowStarts.size() > 1
                && hasPhaseZero
                && hasPhaseOne
                && hasTerminalWindow
                && RadiantWillowTrajectory.NEW_EXTENSION_NODE_COUNT == 0
                && RadiantWillowTrajectory.TOTAL_NODE_COUNT == RadiantWillowTrajectory.RADIANT_NODE_COUNT
                && RadiantWillowTrajectory.RADIANT_NODE_COUNT
                        == RadiantWillowTrajectory.BRANCH_COUNT * RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH;
    }

    private static boolean validRetirementFlicker(RadiantWillowTrajectory.RetirementFlicker flicker) {
        return flicker.enabled()
                && flicker.startTick() >= 0
                && flicker.endTick() > flicker.startTick()
                && (flicker.cadencePhase() == 0 || flicker.cadencePhase() == 1)
                && flicker.activeAt(flicker.startTick())
                && !flicker.activeAt(flicker.endTick());
    }

    private static boolean sameRetirementFlicker(
            RadiantWillowTrajectory.RetirementFlicker first,
            RadiantWillowTrajectory.RetirementFlicker second) {
        return first.startTick() == second.startTick()
                && first.endTick() == second.endTick()
                && first.cadencePhase() == second.cadencePhase();
    }

    private static boolean supportsApprovedWillowScheduling(FireworkStyle.WillowProfile profile) {
        int firstSegmentTick = 0;
        int lastSegmentTick = (profile.segmentsPerBranch() - 1) * WillowTrajectory.SEGMENT_INTERVAL_TICKS;
        int ninthRingEmissionTick = (WillowTrajectory.SHORT_LIVED_SEGMENT_COUNT - 1)
                * WillowTrajectory.SEGMENT_INTERVAL_TICKS;
        int latestShortLivedDissipationTick = ninthRingEmissionTick + WillowTrajectory.SHORT_LIFETIME_MAX;
        int expectedShortLivedNodes = WillowTrajectory.BRANCH_COUNT * WillowTrajectory.SHORT_LIVED_SEGMENT_COUNT;
        int completeRingsPerTickBudget = WillowTrajectory.BRANCH_COUNT * 4;
        return profile.branchCount() == WillowTrajectory.BRANCH_COUNT
                && profile.segmentsPerBranch() == WillowTrajectory.SEGMENTS_PER_BRANCH
                && profile.coreStarCount() == 0
                && firstSegmentTick == 0
                && lastSegmentTick == 58
                && WillowTrajectory.EMISSION_TICKS == lastSegmentTick + 1
                && expectedShortLivedNodes == 160 * 9
                && ninthRingEmissionTick == 16
                && latestShortLivedDissipationTick == 38
                && WillowTrajectory.BRANCH_COUNT <= 216
                && completeRingsPerTickBudget <= 720;
    }

    /** Reads client bytecode as a resource so dedicated-server GameTests never load client-only classes. */
    private static boolean hasNoBranchProgramConcurrencyOrFifoCap() {
        byte[] classBytes = readClassBytes("/com/urbanforma/fireworks/client/GrandFireworkClientEffects.class");
        if (classBytes == null) {
            return false;
        }
        String bytecode = new String(classBytes, StandardCharsets.ISO_8859_1);
        return !bytecode.contains("MAX_ACTIVE_")
                && !bytecode.contains("ACTIVE_WILLOW_OCCUPANCIES")
                && !bytecode.contains("ACTIVE_OCCUPANCY_TICKS")
                && !bytecode.contains("PENDING_")
                && !bytecode.contains("ArrayDeque")
                && !bytecode.contains("willowElapsedTicks");
    }

    /**
     * Proves the retained-spark phase cannot silently turn back into a second burst. This reads class resources rather
     * than loading the client-only classes on a dedicated GameTest server.
     */
    private static boolean hasApprovedRadiantWillowClientBytecode() {
        String[] classResources = {
            "/com/urbanforma/fireworks/client/GrandFireworkClientEffects.class",
            "/com/urbanforma/fireworks/client/GrandFireworkClientEffects$WillowBranchProgram.class",
            "/com/urbanforma/fireworks/client/GrandFireworkClientEffects$RadiantBranchProgram.class",
            "/com/urbanforma/fireworks/client/GrandFireworkClientEffects$RadiantWillowBranchProgram.class",
            "/com/urbanforma/fireworks/client/GrandFireworkClientEffects$RadiantWillowBranchProgram$ManagedRadiantSpark.class"
        };
        String[] forbiddenSymbols = {"CLEARING", "emitWillowNode", "setTrail", "FLASH", "Starter", "TWINKLE"};
        byte[] radiantWillowProgram = null;
        for (String classResource : classResources) {
            byte[] classBytes = readClassBytes(classResource);
            if (classBytes == null) {
                return false;
            }
            String bytecode = new String(classBytes, StandardCharsets.ISO_8859_1);
            for (String forbiddenSymbol : forbiddenSymbols) {
                if (bytecode.contains(forbiddenSymbol)) {
                    return false;
                }
            }
            if (classResource.endsWith("$ManagedRadiantSpark.class") && bytecode.contains("createParticle")) {
                return false;
            }
            if (classResource.endsWith("$RadiantWillowBranchProgram.class")) {
                radiantWillowProgram = classBytes;
            }
        }
        return radiantWillowProgram != null
                && methodDoesNotInvoke(radiantWillowProgram, "updateManagedSparks", "createParticle")
                && methodDoesNotInvoke(radiantWillowProgram, "updateDrainingSparks", "createParticle");
    }

    private static byte[] readClassBytes(String resourcePath) {
        try (InputStream classBytes = FireworksGameTests.class.getResourceAsStream(resourcePath)) {
            return classBytes == null ? null : classBytes.readAllBytes();
        } catch (IOException exception) {
            return null;
        }
    }

    /** Extracts one method's Code attribute without resolving or loading the client class. */
    private static boolean methodDoesNotInvoke(byte[] classFile, String methodName, String forbiddenMethodName) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classFile))) {
            if (input.readInt() != 0xCAFEBABE) {
                return false;
            }
            input.readUnsignedShort();
            input.readUnsignedShort();
            ClassFileConstantPool constantPool = readClassConstantPool(input);
            input.readUnsignedShort();
            input.readUnsignedShort();
            input.readUnsignedShort();
            int interfaceCount = input.readUnsignedShort();
            for (int index = 0; index < interfaceCount; index++) {
                input.readUnsignedShort();
            }
            int fieldCount = input.readUnsignedShort();
            for (int index = 0; index < fieldCount; index++) {
                skipClassMember(input);
            }
            int methodCount = input.readUnsignedShort();
            for (int methodIndex = 0; methodIndex < methodCount; methodIndex++) {
                input.readUnsignedShort();
                String currentMethodName = constantPool.utf8(input.readUnsignedShort());
                input.readUnsignedShort();
                int attributeCount = input.readUnsignedShort();
                for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
                    String attributeName = constantPool.utf8(input.readUnsignedShort());
                    byte[] attribute = readAttributeBytes(input);
                    if (methodName.equals(currentMethodName) && "Code".equals(attributeName)) {
                        return !codeInvokesMethod(attribute, constantPool, forbiddenMethodName);
                    }
                }
            }
            return false;
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    private static ClassFileConstantPool readClassConstantPool(DataInputStream input) throws IOException {
        int count = input.readUnsignedShort();
        String[] utf8 = new String[count];
        int[] tags = new int[count];
        int[] first = new int[count];
        int[] second = new int[count];
        for (int index = 1; index < count; index++) {
            int tag = input.readUnsignedByte();
            tags[index] = tag;
            switch (tag) {
                case 1 -> {
                    int length = input.readUnsignedShort();
                    byte[] bytes = input.readNBytes(length);
                    if (bytes.length != length) {
                        throw new IOException("Truncated class-file UTF-8 entry");
                    }
                    utf8[index] = new String(bytes, StandardCharsets.UTF_8);
                }
                case 3, 4 -> input.readInt();
                case 5, 6 -> {
                    input.readLong();
                    index++;
                }
                case 7, 8, 16, 19, 20 -> first[index] = input.readUnsignedShort();
                case 9, 10, 11, 12, 17, 18 -> {
                    first[index] = input.readUnsignedShort();
                    second[index] = input.readUnsignedShort();
                }
                case 15 -> {
                    first[index] = input.readUnsignedByte();
                    second[index] = input.readUnsignedShort();
                }
                default -> throw new IOException("Unsupported class-file constant-pool tag " + tag);
            }
        }
        return new ClassFileConstantPool(utf8, tags, first, second);
    }

    private static void skipClassMember(DataInputStream input) throws IOException {
        input.readUnsignedShort();
        input.readUnsignedShort();
        input.readUnsignedShort();
        int attributeCount = input.readUnsignedShort();
        for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
            input.readUnsignedShort();
            readAttributeBytes(input);
        }
    }

    private static byte[] readAttributeBytes(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Oversized class-file attribute");
        }
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new IOException("Truncated class-file attribute");
        }
        return bytes;
    }

    private static boolean codeInvokesMethod(
            byte[] codeAttribute, ClassFileConstantPool constantPool, String forbiddenMethodName) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(codeAttribute))) {
            input.readUnsignedShort();
            input.readUnsignedShort();
            int codeLength = input.readInt();
            if (codeLength < 0) {
                throw new IOException("Oversized class-file code attribute");
            }
            byte[] code = input.readNBytes(codeLength);
            if (code.length != codeLength) {
                throw new IOException("Truncated class-file code attribute");
            }
            for (int offset = 0; offset < code.length; ) {
                int opcode = Byte.toUnsignedInt(code[offset]);
                if (opcode >= 0xB6 && opcode <= 0xB9) {
                    int methodReference = unsignedShort(code, offset + 1);
                    if (constantPool.referencesMethodNamed(methodReference, forbiddenMethodName)) {
                        return true;
                    }
                }
                offset = nextInstructionOffset(code, offset);
            }
            return false;
        }
    }

    private static int nextInstructionOffset(byte[] code, int offset) throws IOException {
        int opcode = Byte.toUnsignedInt(code[offset]);
        if ((opcode >= 0x15 && opcode <= 0x19)
                || (opcode >= 0x36 && opcode <= 0x3A)
                || opcode == 0x10
                || opcode == 0x12
                || opcode == 0xA9
                || opcode == 0xBC) {
            return checkedInstructionEnd(code, offset, 2);
        }
        if ((opcode >= 0x99 && opcode <= 0xA8)
                || (opcode >= 0xB2 && opcode <= 0xB8)
                || opcode == 0x11
                || opcode == 0x13
                || opcode == 0x14
                || opcode == 0x84
                || opcode == 0xBD
                || opcode == 0xC0
                || opcode == 0xC1
                || opcode == 0xC6
                || opcode == 0xC7) {
            return checkedInstructionEnd(code, offset, 3);
        }
        return switch (opcode) {
            case 0xAA -> tableSwitchEnd(code, offset);
            case 0xAB -> lookupSwitchEnd(code, offset);
            case 0xB9, 0xBA, 0xC8, 0xC9 -> checkedInstructionEnd(code, offset, 5);
            case 0xC4 -> wideInstructionEnd(code, offset);
            case 0xC5 -> checkedInstructionEnd(code, offset, 4);
            default -> checkedInstructionEnd(code, offset, 1);
        };
    }

    private static int tableSwitchEnd(byte[] code, int offset) throws IOException {
        int dataOffset = alignedSwitchDataOffset(code, offset);
        int low = codeInt(code, dataOffset + 4);
        int high = codeInt(code, dataOffset + 8);
        long entryCount = (long) high - low + 1L;
        if (entryCount < 0L) {
            throw new IOException("Invalid tableswitch range");
        }
        return checkedInstructionEnd(code, offset, dataOffset - offset + 12L + entryCount * 4L);
    }

    private static int lookupSwitchEnd(byte[] code, int offset) throws IOException {
        int dataOffset = alignedSwitchDataOffset(code, offset);
        int pairCount = codeInt(code, dataOffset + 4);
        if (pairCount < 0) {
            throw new IOException("Invalid lookupswitch pair count");
        }
        return checkedInstructionEnd(code, offset, dataOffset - offset + 8L + (long) pairCount * 8L);
    }

    private static int alignedSwitchDataOffset(byte[] code, int offset) throws IOException {
        int dataOffset = offset + 1;
        while ((dataOffset & 3) != 0) {
            dataOffset++;
        }
        if (dataOffset + 8 > code.length) {
            throw new IOException("Truncated switch instruction");
        }
        return dataOffset;
    }

    private static int wideInstructionEnd(byte[] code, int offset) throws IOException {
        if (offset + 1 >= code.length) {
            throw new IOException("Truncated wide instruction");
        }
        return checkedInstructionEnd(code, offset, Byte.toUnsignedInt(code[offset + 1]) == 0x84 ? 6 : 4);
    }

    private static int checkedInstructionEnd(byte[] code, int offset, long length) throws IOException {
        long end = offset + length;
        if (length <= 0L || end > code.length) {
            throw new IOException("Invalid class-file instruction length");
        }
        return (int) end;
    }

    private static int unsignedShort(byte[] bytes, int offset) throws IOException {
        if (offset < 0 || offset + 2 > bytes.length) {
            throw new IOException("Truncated class-file operand");
        }
        return (Byte.toUnsignedInt(bytes[offset]) << 8) | Byte.toUnsignedInt(bytes[offset + 1]);
    }

    private static int codeInt(byte[] bytes, int offset) throws IOException {
        if (offset < 0 || offset + 4 > bytes.length) {
            throw new IOException("Truncated class-file operand");
        }
        return (Byte.toUnsignedInt(bytes[offset]) << 24)
                | (Byte.toUnsignedInt(bytes[offset + 1]) << 16)
                | (Byte.toUnsignedInt(bytes[offset + 2]) << 8)
                | Byte.toUnsignedInt(bytes[offset + 3]);
    }

    private static boolean hasApprovedBranchRandomization(
            WillowTrajectory.Branch branch, FireworkStyle.WillowProfile profile) {
        return Math.abs(branch.direction().length() - 1.0D) <= WILLOW_EPSILON
                && branch.reach() >= profile.horizontalReach() * 0.90D - WILLOW_EPSILON
                && branch.reach() <= profile.horizontalReach() + WILLOW_EPSILON
                && branch.riseMultiplier() >= 0.92D - WILLOW_EPSILON
                && branch.riseMultiplier() <= 1.06D + WILLOW_EPSILON
                && branch.fallMultiplier() >= 0.90D - WILLOW_EPSILON
                && branch.fallMultiplier() <= 1.04D + WILLOW_EPSILON
                && branch.bendProgress() >= 0.52D - WILLOW_EPSILON
                && branch.bendProgress() <= 0.62D + WILLOW_EPSILON
                && branch.swayAmplitude() > 0.0D
                && branch.tangentSpeed() >= WillowTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                && branch.tangentSpeed() <= WillowTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON;
    }

    private static boolean hasApprovedRadiantBranchRandomization(
            RadiantTrajectory.Branch branch, FireworkStyle.RadiantProfile profile) {
        return Math.abs(branch.direction().length() - 1.0D) <= WILLOW_EPSILON
                && branch.radialReach() >= profile.maximumRadius() * 0.90D - WILLOW_EPSILON
                && branch.radialReach() <= profile.maximumRadius() + WILLOW_EPSILON
                && branch.dropStartProgress() >= profile.bendStartMin() - WILLOW_EPSILON
                && branch.dropStartProgress() <= profile.bendStartMax() + WILLOW_EPSILON
                && branch.dropMultiplier() >= 0.92D - WILLOW_EPSILON
                && branch.dropMultiplier() <= 1.05D + WILLOW_EPSILON
                && branch.swayAmplitude() >= 0.18D - WILLOW_EPSILON
                && branch.swayAmplitude() <= 0.65D + WILLOW_EPSILON
                && branch.tangentSpeed() >= RadiantTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                && branch.tangentSpeed() <= RadiantTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON;
    }

    private static boolean hasApprovedRadiantWillowBranchRandomization(
            RadiantWillowTrajectory.Branch branch, FireworkStyle.RadiantWillowProfile profile) {
        return branch.index() >= 0
                && branch.index() < profile.branchCount()
                && Math.abs(branch.radiantBranch().direction().length() - 1.0D) <= WILLOW_EPSILON
                && branch.radialMultiplier() >= 0.90D - WILLOW_EPSILON
                && branch.radialMultiplier() <= 1.0D + WILLOW_EPSILON
                && branch.dropMultiplier() >= 0.92D - WILLOW_EPSILON
                && branch.dropMultiplier() <= 1.06D + WILLOW_EPSILON
                && branch.bendStart() >= profile.bendStartMin() - WILLOW_EPSILON
                && branch.bendStart() <= profile.bendStartMax() + WILLOW_EPSILON
                && branch.swayAmplitude() >= 2.25D - WILLOW_EPSILON
                && branch.swayAmplitude() <= profile.maximumLateralSway() + WILLOW_EPSILON
                && branch.swayFrequency() >= 1.20D - WILLOW_EPSILON
                && branch.swayFrequency() <= 2.65D + WILLOW_EPSILON
                && branch.swayPhase() >= 0.0D
                && branch.swayPhase() < Math.PI * 2.0D
                && branch.secondarySwayPhase() >= 0.0D
                && branch.secondarySwayPhase() < Math.PI * 2.0D
                && branch.tangentSpeed() >= RadiantWillowTrajectory.MIN_TANGENT_SPEED - WILLOW_EPSILON
                && branch.tangentSpeed() <= RadiantWillowTrajectory.MAX_TANGENT_SPEED + WILLOW_EPSILON;
    }

    private static WillowTrajectory.ColorBand expectedColorBand(int segmentIndex) {
        if (segmentIndex <= 5) {
            return WillowTrajectory.ColorBand.PRIMARY;
        }
        return segmentIndex <= 23 ? WillowTrajectory.ColorBand.SECONDARY : WillowTrajectory.ColorBand.ACCENT;
    }

    private static RadiantTrajectory.ColorBand expectedRadiantColorBand(int segmentIndex) {
        if (segmentIndex <= 5) {
            return RadiantTrajectory.ColorBand.PRIMARY;
        }
        return segmentIndex <= 23 ? RadiantTrajectory.ColorBand.SECONDARY : RadiantTrajectory.ColorBand.ACCENT;
    }

    private static boolean sameSample(
            WillowTrajectory.BranchSample first, WillowTrajectory.BranchSample second) {
        return first.segmentIndex() == second.segmentIndex()
                && first.colorBand() == second.colorBand()
                && Math.abs(first.colorTone() - second.colorTone()) <= WILLOW_EPSILON
                && first.lifetime() == second.lifetime()
                && first.twinkles() == second.twinkles()
                && Math.abs(first.progress() - second.progress()) <= WILLOW_EPSILON
                && sameVec(first.position(), second.position())
                && sameVec(first.tangent(), second.tangent());
    }

    private static boolean sameRadiantSample(
            RadiantTrajectory.BranchSample first, RadiantTrajectory.BranchSample second) {
        return first.segmentIndex() == second.segmentIndex()
                && first.colorBand() == second.colorBand()
                && Math.abs(first.colorTone() - second.colorTone()) <= WILLOW_EPSILON
                && Math.abs(first.twinklePhase() - second.twinklePhase()) <= WILLOW_EPSILON
                && first.lifetime() == second.lifetime()
                && first.twinkles() == second.twinkles()
                && Math.abs(first.progress() - second.progress()) <= WILLOW_EPSILON
                && sameVec(first.position(), second.position())
                && sameVec(first.tangent(), second.tangent());
    }

    private static boolean sameManagedParticleIdentity(
            RadiantWillowTrajectory.ManagedParticleSample first,
            RadiantWillowTrajectory.ManagedParticleSample second) {
        return first.branch().equals(second.branch())
                && first.radiantSegmentIndex() == second.radiantSegmentIndex()
                && first.managedSegmentIndex() == second.managedSegmentIndex()
                && first.extensionDurationTicks() == second.extensionDurationTicks()
                && first.colorBand() == second.colorBand();
    }

    private static boolean sameManagedRadiantParticleSample(
            RadiantWillowTrajectory.ManagedParticleSample first,
            RadiantWillowTrajectory.ManagedParticleSample second) {
        return sameManagedParticleIdentity(first, second)
                && first.extensionAgeTicks() == second.extensionAgeTicks()
                && Math.abs(first.extensionProgress() - second.extensionProgress()) <= WILLOW_EPSILON
                && sameVec(first.position(), second.position())
                && sameVec(first.tangent(), second.tangent())
                && sameVec(first.velocity(), second.velocity())
                && first.terminalFlicker().equals(second.terminalFlicker())
                && first.terminalRetirementTick() == second.terminalRetirementTick()
                && first.retired() == second.retired();
    }

    private static boolean sameVec(Vec3 first, Vec3 second) {
        return Math.abs(first.x - second.x) <= WILLOW_EPSILON
                && Math.abs(first.y - second.y) <= WILLOW_EPSILON
                && Math.abs(first.z - second.z) <= WILLOW_EPSILON;
    }

    private static List<GrandFireworkRocketEntity> rocketsNear(GameTestHelper helper, Vec3 center, double radius) {
        return helper.getLevel().getEntitiesOfClass(
                GrandFireworkRocketEntity.class,
                new AABB(
                        center.x - radius,
                        center.y - radius,
                        center.z - radius,
                        center.x + radius,
                        center.y + radius,
                        center.z + radius));
    }

    private static CompoundTag emptyStructureTag() {
        CompoundTag structure = new CompoundTag();
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(8));
        size.add(IntTag.valueOf(4));
        size.add(IntTag.valueOf(8));
        structure.put("size", size);
        structure.put("blocks", new ListTag());
        structure.put("palette", new ListTag());
        structure.put("entities", new ListTag());
        return structure;
    }

    private record ClassFileConstantPool(String[] utf8Entries, int[] tags, int[] first, int[] second) {
        private String utf8(int index) {
            return index > 0 && index < this.utf8Entries.length ? this.utf8Entries[index] : null;
        }

        private boolean referencesMethodNamed(int methodReference, String methodName) {
            if (methodReference <= 0 || methodReference >= this.tags.length) {
                return false;
            }
            int tag = this.tags[methodReference];
            if (tag != 10 && tag != 11) {
                return false;
            }
            int nameAndType = this.second[methodReference];
            if (nameAndType <= 0 || nameAndType >= this.tags.length || this.tags[nameAndType] != 12) {
                return false;
            }
            return methodName.equals(this.utf8(this.first[nameAndType]));
        }
    }

    private record WillowExpectation(String id, int horizontalReach, int rise, int drop) {
    }

    private record LedColorExpectation(
            String id, String ledReference, String primary, String secondary, String accent) {
    }
}
