package com.urbanforma.fireworks.entity;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import com.urbanforma.fireworks.registry.FireworksEntities;
import com.urbanforma.fireworks.registry.FireworksItems;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Server-authoritative rocket shared by every series item. The style index is synced with tracking clients, while
 * the burst payload is only sent once at detonation.
 */
public final class GrandFireworkRocketEntity extends Projectile implements ItemSupplier {
    /** Compatibility alias for tests and integrations that refer to the original golden demonstration profile. */
    public static final int FLIGHT_TICKS = FireworkStyle.GRAND_GOLDEN_SPHERE.flightTicks();
    public static final float LAUNCH_SPEED = 1.45F;
    public static final int TRACKING_RANGE_CHUNKS = 16;
    public static final int TRACKING_RANGE_BLOCKS = TRACKING_RANGE_CHUNKS * 16;
    public static final double EFFECT_RADIUS = TRACKING_RANGE_BLOCKS;

    private static final String STYLE_INDEX_TAG = "StyleIndex";
    private static final EntityDataAccessor<Integer> DATA_STYLE_INDEX =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.INT);

    private int life;
    private long explosionSeed;
    private boolean launchSoundPlayed;

    public GrandFireworkRocketEntity(EntityType<GrandFireworkRocketEntity> entityType, Level level) {
        super(entityType, level);
        this.explosionSeed = level.random.nextLong();
    }

    public GrandFireworkRocketEntity(Level level, double x, double y, double z, @Nullable Entity owner) {
        this(level, x, y, z, owner, FireworkStyle.GRAND_GOLDEN_SPHERE);
    }

    public GrandFireworkRocketEntity(
            Level level, double x, double y, double z, @Nullable Entity owner, FireworkStyle style) {
        this(FireworksEntities.GRAND_FIREWORK_ROCKET.get(), level);
        this.setPos(x, y, z);
        this.setOwner(owner);
        this.setStyle(style);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_STYLE_INDEX, FireworkStyle.GRAND_GOLDEN_SPHERE.index());
    }

    public void launchVertically() {
        this.shoot(0.0D, 1.0D, 0.0D, LAUNCH_SPEED, 0.0F);
    }

    public int life() {
        return this.life;
    }

    public long explosionSeed() {
        return this.explosionSeed;
    }

    public int styleIndex() {
        return this.entityData.get(DATA_STYLE_INDEX);
    }

    public FireworkStyle style() {
        return FireworkStyle.fromIndex(this.styleIndex());
    }

    public void setStyle(FireworkStyle style) {
        this.entityData.set(DATA_STYLE_INDEX, Objects.requireNonNull(style, "style").index());
    }

    @Override
    public ItemStack getItem() {
        return FireworksItems.itemFor(this.style()).getDefaultInstance();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // The server sends a correction every tick; this fills the interval between them.
            this.setPos(this.position().add(this.getDeltaMovement()));
            this.updateRotation();
            return;
        }

        if (!this.launchSoundPlayed) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.AMBIENT,
                    3.0F,
                    1.0F);
            this.launchSoundPlayed = true;
        }

        Vec3 currentPosition = this.position();
        Vec3 velocity = this.getDeltaMovement();
        HitResult collision = this.level().clip(new ClipContext(
                currentPosition,
                currentPosition.add(velocity),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this));
        if (collision.getType() == HitResult.Type.BLOCK) {
            this.setPos(collision.getLocation());
            this.explode();
            return;
        }

        this.setPos(currentPosition.add(velocity));
        this.updateRotation();
        this.life++;
        if (this.life >= this.style().flightTicks()) {
            this.explode();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < EFFECT_RADIUS * EFFECT_RADIUS;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", this.life);
        tag.putLong("Seed", this.explosionSeed);
        tag.putBoolean("LaunchSoundPlayed", this.launchSoundPlayed);
        tag.putInt(STYLE_INDEX_TAG, this.style().index());
        Vec3 velocity = this.getDeltaMovement();
        tag.putDouble("VelocityX", velocity.x);
        tag.putDouble("VelocityY", velocity.y);
        tag.putDouble("VelocityZ", velocity.z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.life = Math.max(0, tag.getInt("Life"));
        this.explosionSeed = tag.getLong("Seed");
        this.launchSoundPlayed = tag.getBoolean("LaunchSoundPlayed");
        this.setStyle(FireworkStyle.fromIndex(tag.getInt(STYLE_INDEX_TAG)));
        if (tag.contains("VelocityX") && tag.contains("VelocityY") && tag.contains("VelocityZ")) {
            this.setDeltaMovement(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        }
    }

    private void explode() {
        if (this.level() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(
                    serverLevel,
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    EFFECT_RADIUS,
                    new GrandFireworkBurstPayload(
                            this.getX(), this.getY(), this.getZ(), this.explosionSeed, this.style().index()));
        }
        this.discard();
    }
}
