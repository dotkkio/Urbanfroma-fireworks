package com.urbanforma.fireworks.world.item;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.entity.GrandFireworkRocketEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Shared launch behavior for every series firework. */
public class FireworkRocketItem extends Item implements ProjectileItem {
    private static final double BLOCK_LAUNCH_HEIGHT = 1.15D;

    private final FireworkStyle style;

    public FireworkRocketItem(FireworkStyle style, Item.Properties properties) {
        super(properties);
        this.style = style;
    }

    public FireworkStyle style() {
        return this.style;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            Vec3 launchPos = new Vec3(
                    context.getClickedPos().getX() + 0.5D,
                    context.getClickedPos().getY() + BLOCK_LAUNCH_HEIGHT,
                    context.getClickedPos().getZ() + 0.5D);
            if (!launchVertically(level, launchPos, context.getPlayer())) {
                return InteractionResult.FAIL;
            }
            context.getItemInHand().consume(1, context.getPlayer());
            if (context.getPlayer() != null) {
                context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (!launchVertically(level, new Vec3(player.getX(), player.getEyeY(), player.getZ()), player)) {
                return InteractionResultHolder.fail(stack);
            }
            stack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new GrandFireworkRocketEntity(level, pos.x(), pos.y(), pos.z(), null, this.style);
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder()
                .uncertainty(0.0F)
                .power(GrandFireworkRocketEntity.LAUNCH_SPEED)
                .build();
    }

    private boolean launchVertically(Level level, Vec3 pos, @Nullable Entity owner) {
        GrandFireworkRocketEntity rocket =
                new GrandFireworkRocketEntity(level, pos.x, pos.y, pos.z, owner, this.style);
        rocket.launchVertically();
        return level.addFreshEntity(rocket);
    }
}
