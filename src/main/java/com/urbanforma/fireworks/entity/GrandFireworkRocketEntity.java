package com.urbanforma.fireworks.entity;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.FireworkAscentTrajectory;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import com.urbanforma.fireworks.registry.FireworksEntities;
import com.urbanforma.fireworks.registry.FireworksItems;
import java.util.Objects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
    private static final EntityDataAccessor<Integer> DATA_LAUNCH_AGE =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_LAUNCH_SEED =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> DATA_TARGET_HEIGHT =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_LANDING_OFFSET_X =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_LANDING_OFFSET_Z =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SWAY_PHASE =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SWAY_FREQUENCY =
            SynchedEntityData.defineId(GrandFireworkRocketEntity.class, EntityDataSerializers.FLOAT);

    private int life;
    private long explosionSeed;
    private boolean launchSoundPlayed;
    private boolean explosionDispatched;
    private int burstDispatchCount;
    private Vec3 launchOrigin;

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
        // Dispensers construct the projectile directly, so initialize before their configured velocity is applied.
        this.launchOrigin = this.position();
        this.configureAscentProfile();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_STYLE_INDEX, FireworkStyle.GRAND_GOLDEN_SPHERE.index());
        builder.define(DATA_LAUNCH_AGE, 0);
        builder.define(DATA_LAUNCH_SEED, 0L);
        builder.define(DATA_TARGET_HEIGHT, 40.0F);
        builder.define(DATA_LANDING_OFFSET_X, 0.0F);
        builder.define(DATA_LANDING_OFFSET_Z, 0.0F);
        builder.define(DATA_SWAY_PHASE, 0.0F);
        builder.define(DATA_SWAY_FREQUENCY, 1.0F);
    }

    public void launchVertically() {
        this.launchOrigin = this.position();
        this.configureAscentProfile();
        this.setDeltaMovement(this.initialServerVelocity());
    }

    public int life() {
        return this.life;
    }

    public long explosionSeed() {
        return this.explosionSeed;
    }

    /** Immutable launch seed synchronized once with entity tracking data. */
    public long launchSeed() {
        return this.entityData.get(DATA_LAUNCH_SEED);
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

    public boolean explosionDispatched() {
        return this.explosionDispatched;
    }

    /** A detonation has exactly one compact client payload; no server particle state is retained. */
    public int burstDispatchCount() {
        return this.burstDispatchCount;
    }

    @Override
    public ItemStack getItem() {
        return FireworksItems.itemFor(this.style()).getDefaultInstance();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.advanceClientVisualPath();
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

        if (this.life == 0) {
            // ProjectileItem may apply its dispenser velocity after construction; normalize all launch paths once.
            this.setDeltaMovement(this.initialServerVelocity());
        }
        int flightTicks = Math.max(1, this.style().flightTicks());
        int currentAge = Math.min(flightTicks, Math.max(0, this.life));
        int nextAge = Math.min(flightTicks, currentAge + 1);
        Vec3 currentPosition = this.arcPointForAge(currentAge, flightTicks);
        Vec3 nextPosition = this.arcPointForAge(nextAge, flightTicks);
        // Sweep consecutive pieces of the same seed-derived curve the client reconstructs. A straight chord from
        // tick endpoints cuts below a curved large/giant ascent and can incorrectly detonate near the ground.
        int collisionSegments = FireworkAscentTrajectory.collisionSegments(this.launchProfile());
        Vec3 segmentStart = currentPosition;
        for (int segment = 1; segment <= collisionSegments; segment++) {
            double age = currentAge + (nextAge - currentAge) * (segment / (double) collisionSegments);
            Vec3 segmentEnd = this.arcPointForProgress(age / flightTicks);
            HitResult collision = this.level().clip(new ClipContext(
                    segmentStart,
                    segmentEnd,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this));
            if (collision.getType() == HitResult.Type.BLOCK) {
                this.setPos(collision.getLocation());
                this.explode();
                return;
            }
            segmentStart = segmentEnd;
        }

        this.setPos(nextPosition);
        this.setDeltaMovement(nextPosition.subtract(currentPosition));
        this.updateRotation();
        this.life++;
        this.entityData.set(DATA_LAUNCH_AGE, this.life);
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
        Vec3 origin = this.launchOrigin == null ? this.position() : this.launchOrigin;
        tag.putDouble("LaunchOriginX", origin.x);
        tag.putDouble("LaunchOriginY", origin.y);
        tag.putDouble("LaunchOriginZ", origin.z);
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
        this.launchOrigin = tag.contains("LaunchOriginX")
                ? new Vec3(tag.getDouble("LaunchOriginX"), tag.getDouble("LaunchOriginY"), tag.getDouble("LaunchOriginZ"))
                : this.position();
        this.configureAscentProfile();
        this.entityData.set(DATA_LAUNCH_AGE, this.life);
        if (tag.contains("VelocityX") && tag.contains("VelocityY") && tag.contains("VelocityZ")) {
            this.setDeltaMovement(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
        }
    }

    private void explode() {
        if (this.explosionDispatched) {
            return;
        }
        this.explosionDispatched = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            // This is the single authoritative detonation sound for every registered style. The client only
            // reconstructs its visual payload, so receivers do not play a second local blast.
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                    SoundSource.AMBIENT,
                    16.0F,
                    0.35F);
            PacketDistributor.sendToPlayersNear(
                    serverLevel,
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    EFFECT_RADIUS,
                    new GrandFireworkBurstPayload(
                            this.getX(), this.getY(), this.getZ(), this.explosionSeed, this.style().index()));
            this.burstDispatchCount++;
        }
        this.discard();
    }

    private void configureAscentProfile() {
        FireworkAscentTrajectory.Profile profile = FireworkAscentTrajectory.profile(this.style(), this.explosionSeed);
        this.entityData.set(DATA_LAUNCH_SEED, this.explosionSeed);
        this.entityData.set(DATA_TARGET_HEIGHT, profile.targetHeight());
        this.entityData.set(DATA_LANDING_OFFSET_X, profile.landingOffsetX());
        this.entityData.set(DATA_LANDING_OFFSET_Z, profile.landingOffsetZ());
        this.entityData.set(DATA_SWAY_PHASE, profile.swayPhase());
        this.entityData.set(DATA_SWAY_FREQUENCY, profile.swayFrequency());
    }

    /** Returns the synchronized, finite launch profile used by client-side visual reconstruction. */
    public FireworkAscentTrajectory.Profile launchProfile() {
        return new FireworkAscentTrajectory.Profile(
                this.entityData.get(DATA_TARGET_HEIGHT),
                this.entityData.get(DATA_LANDING_OFFSET_X),
                this.entityData.get(DATA_LANDING_OFFSET_Z),
                this.entityData.get(DATA_SWAY_PHASE),
                this.entityData.get(DATA_SWAY_FREQUENCY));
    }

    private Vec3 initialServerVelocity() {
        int flightTicks = Math.max(1, this.style().flightTicks());
        return FireworkAscentTrajectory.offset(this.launchProfile(), 1.0D).scale(1.0D / flightTicks);
    }

    private Vec3 arcPointForAge(int age, int flightTicks) {
        return this.arcPointForProgress((double) Math.min(flightTicks, Math.max(0, age)) / flightTicks);
    }

    private Vec3 arcPointForProgress(double progress) {
        if (this.launchOrigin == null) {
            this.launchOrigin = this.position();
        }
        return this.launchOrigin.add(FireworkAscentTrajectory.offset(this.launchProfile(), progress));
    }

    /** The physical client alone samples the complete bounded micro-arc between the authoritative endpoints. */
    private void advanceClientVisualPath() {
        int flightTicks = Math.max(1, this.style().flightTicks());
        int age = Math.min(flightTicks, Math.max(0, this.entityData.get(DATA_LAUNCH_AGE)));
        FireworkAscentTrajectory.Profile profile = this.launchProfile();
        Vec3 origin = this.launchOrigin;
        if (origin == null) {
            Vec3 linearProgress = FireworkAscentTrajectory.offset(profile, 1.0D)
                    .scale((double) age / flightTicks);
            origin = this.position().subtract(linearProgress);
            this.launchOrigin = origin;
        }
        int nextAge = Math.min(flightTicks, age + 1);
        Vec3 current = origin.add(FireworkAscentTrajectory.offset(profile, (double) age / flightTicks));
        Vec3 next = origin.add(FireworkAscentTrajectory.offset(profile, (double) nextAge / flightTicks));
        this.setPos(current);
        this.setDeltaMovement(next.subtract(current));
    }
}
