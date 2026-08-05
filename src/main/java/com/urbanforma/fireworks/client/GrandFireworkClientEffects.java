package com.urbanforma.fireworks.client;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.MidsizeFireworkCatalog;
import com.urbanforma.fireworks.content.NormalFireworkCatalog;
import com.urbanforma.fireworks.content.OtherExtraFireworkCatalog;
import com.urbanforma.fireworks.content.OtherFireworkCatalog;
import com.urbanforma.fireworks.content.RadiantTrajectory;
import com.urbanforma.fireworks.content.RadiantWillowTrajectory;
import com.urbanforma.fireworks.content.WillowTrajectory;
import com.urbanforma.fireworks.content.colorchange.ColorChangeBallProgram;
import com.urbanforma.fireworks.content.giant.GiantRadiantTrajectory;
import com.urbanforma.fireworks.content.hybrid.HybridSphereRadiantTrajectory;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import com.urbanforma.fireworks.content.batch_other.BatchOtherCatalog;
import com.urbanforma.fireworks.content.batch_other.BatchOtherFirework;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraCatalog;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraFirework;
import com.urbanforma.fireworks.content.batch_other_extra.BatchOtherExtraTrajectory;
import com.urbanforma.fireworks.content.midsize.MidsizeFireworkDefinition;
import com.urbanforma.fireworks.client.colorchange.ColorChangeBallParticleAdapter;
import com.urbanforma.fireworks.client.giant.GiantRadiantClientQueue;
import com.urbanforma.fireworks.client.giant.GiantRadiantClientProgram;
import com.urbanforma.fireworks.client.giant.willow.GiantWillowClientProgram;
import com.urbanforma.fireworks.client.giant.willow.GiantWillowClientQueue;
import com.urbanforma.fireworks.client.giant.superwillow.SuperWillowClientProgram;
import com.urbanforma.fireworks.client.giant.superwillow.SuperWillowClientQueue;
import com.urbanforma.fireworks.client.giant.multiradial2.GiantMultiRadial2ClientProgram;
import com.urbanforma.fireworks.client.giant.multiradial2.GiantMultiRadial2ClientQueue;
import com.urbanforma.fireworks.client.giant.thickradial.GiantThickRadialClientProgram;
import com.urbanforma.fireworks.client.giant.thickradial.GiantThickRadialClientQueue;
import com.urbanforma.fireworks.client.giant.cascade.GiantCascadeClientProgram;
import com.urbanforma.fireworks.client.giant.cascade.GiantCascadeClientQueue;
import com.urbanforma.fireworks.client.hybrid.HybridSphereRadiantParticleProgram;
import com.urbanforma.fireworks.client.batch_other.BatchOtherClientPrograms;
import com.urbanforma.fireworks.client.batch_other_extra.BatchOtherExtraClientPrograms;
import com.urbanforma.fireworks.client.midsize.MidsizeDenseRadialClientProgram;
import com.urbanforma.fireworks.client.midsize.MidsizeDenseSphereClientProgram;
import com.urbanforma.fireworks.client.saturn.SaturnClientPlan;
import com.urbanforma.fireworks.client.saturn.SaturnEmission;
import com.urbanforma.fireworks.entity.GrandFireworkRocketEntity;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client-only visual scheduler. The server sends one small burst description;
 * this class reconstructs every full shell locally using only vanilla spark particles.
 */
public final class GrandFireworkClientEffects {
    // The golden demonstration burst remains behaviorally identical to the accepted version.
    private static final int GOLDEN_OUTER_STARS = 1_920;
    private static final int GOLDEN_INNER_STARS = 240;
    private static final int GOLDEN_STARS_PER_TICK = 180;
    private static final int GOLDEN_OUTER_MIN_LIFETIME = 82;
    private static final int GOLDEN_OUTER_LIFETIME_VARIATION = 21;
    private static final int GOLDEN_INNER_MIN_LIFETIME = 64;
    private static final int GOLDEN_INNER_LIFETIME_VARIATION = 17;
    private static final double GOLDEN_DIAMETER = 105.0D;
    private static final double GOLDEN_OUTER_SPEED = 4.9D;
    private static final double GOLDEN_INNER_SPEED = 2.2D;

    private static final int GOLDEN_TRAIL_STARS_PER_TICK = 18;
    private static final int GOLDEN_TRAIL_LIFETIME = 26;
    private static final float CHAMPAGNE_RED = 1.0F;
    private static final float CHAMPAGNE_GREEN = 241.0F / 255.0F;
    private static final float CHAMPAGNE_BLUE = 176.0F / 255.0F;
    private static final float PEARL_RED = 1.0F;
    private static final float PEARL_GREEN = 250.0F / 255.0F;
    private static final float PEARL_BLUE = 214.0F / 255.0F;
    private static final float GOLD_RED = 1.0F;
    private static final float GOLD_GREEN = 192.0F / 255.0F;
    private static final float GOLD_BLUE = 32.0F / 255.0F;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final List<ActiveBurst> ACTIVE_BURSTS = new ArrayList<>();
    private static final List<PrototypeBurst> ACTIVE_PROTOTYPES = new ArrayList<>();
    private static final List<SpecialBurst> ACTIVE_SPECIAL_BURSTS = new ArrayList<>();
    private static final GiantRadiantClientQueue GIANT_LARGE_QUEUE = new GiantRadiantClientQueue();
    private static final GiantWillowClientQueue GIANT_EXTRA_LARGE_QUEUE = new GiantWillowClientQueue();
    private static final SuperWillowClientQueue GIANT_SUPER_WILLOW_QUEUE = new SuperWillowClientQueue();
    private static final GiantMultiRadial2ClientQueue GIANT_MULTI_RADIAL_II_QUEUE = new GiantMultiRadial2ClientQueue();
    private static final GiantThickRadialClientQueue GIANT_THICK_RADIAL_QUEUE = new GiantThickRadialClientQueue();
    private static final GiantCascadeClientQueue GIANT_CASCADE_QUEUE = new GiantCascadeClientQueue();
    private static ClientLevel observedLevel;

    private GrandFireworkClientEffects() {
    }

    public static void enqueue(GrandFireworkBurstPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        observeLevel(level);
        startBurst(level, payload);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            clearActiveEffects();
            return;
        }

        observeLevel(level);
        GIANT_LARGE_QUEUE.tick(minecraft);
        GIANT_EXTRA_LARGE_QUEUE.tick(minecraft);
        GIANT_SUPER_WILLOW_QUEUE.tick(minecraft);
        GIANT_MULTI_RADIAL_II_QUEUE.tick(minecraft);
        GIANT_THICK_RADIAL_QUEUE.tick(minecraft);
        GIANT_CASCADE_QUEUE.tick(minecraft);
        emitSpecialBurstQueue(minecraft);
        emitPrototypeQueue(minecraft);
        emitBurstQueue(minecraft);
        emitRocketTrails(minecraft, level);
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearActiveEffects();
    }

    private static void clearActiveEffects() {
        ACTIVE_BURSTS.clear();
        ACTIVE_PROTOTYPES.clear();
        ACTIVE_SPECIAL_BURSTS.clear();
        GIANT_LARGE_QUEUE.clear();
        GIANT_EXTRA_LARGE_QUEUE.clear();
        GIANT_SUPER_WILLOW_QUEUE.clear();
        GIANT_MULTI_RADIAL_II_QUEUE.clear();
        GIANT_THICK_RADIAL_QUEUE.clear();
        GIANT_CASCADE_QUEUE.clear();
        observedLevel = null;
    }

    private static void observeLevel(ClientLevel level) {
        if (observedLevel != null && observedLevel != level) {
            clearActiveEffects();
        }
        observedLevel = level;
    }

    private static void startBurst(ClientLevel level, GrandFireworkBurstPayload payload) {
        FireworkStyle style = payload.style();
        if (style.index() >= MidsizeFireworkCatalog.FIRST_STYLE_INDEX) {
            startMidsizeBurst(level, payload, style);
            return;
        }
        if (style.index() >= OtherExtraFireworkCatalog.FIRST_STYLE_INDEX) {
            startOtherExtraBurst(level, payload, style);
            return;
        }
        if (style.index() >= OtherFireworkCatalog.FIRST_STYLE_INDEX) {
            startOtherBurst(level, payload, style);
            return;
        }
        if (style.giantTier() != com.urbanforma.fireworks.content.GiantTier.NONE) {
            switch (style.giantTier()) {
                case LARGE -> GIANT_LARGE_QUEUE.enqueue(
                        new GiantRadiantClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
                case EXTRA_LARGE -> GIANT_EXTRA_LARGE_QUEUE.enqueue(
                        new GiantWillowClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
                case SUPER_WILLOW -> GIANT_SUPER_WILLOW_QUEUE.enqueue(
                        new SuperWillowClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
                case MULTI_RADIAL_II -> GIANT_MULTI_RADIAL_II_QUEUE.enqueue(
                        new GiantMultiRadial2ClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
                case THICK_RADIAL -> GIANT_THICK_RADIAL_QUEUE.enqueue(
                        new GiantThickRadialClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
                case CASCADE -> GIANT_CASCADE_QUEUE.enqueue(
                        new GiantCascadeClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
                case NONE -> throw new IllegalStateException("A giant style requires an independent giant tier");
            }
            return;
        }

        if (style.shape() == FireworkStyle.Shape.HYBRID_SPHERE_RADIANT) {
            playBurstSound(level, payload);
            ACTIVE_PROTOTYPES.add(new HybridPrototypeBurst(payload, style));
            return;
        }
        if (style.shape() == FireworkStyle.Shape.SATURN) {
            playBurstSound(level, payload);
            ACTIVE_PROTOTYPES.add(new SaturnPrototypeBurst(payload, style));
            return;
        }
        startOrdinaryBurst(level, payload, style);
    }

    private static void startMidsizeBurst(ClientLevel level, GrandFireworkBurstPayload payload, FireworkStyle style) {
        MidsizeFireworkDefinition definition = com.urbanforma.fireworks.content.midsize.MidsizeFireworkCatalog.byId(style.id());
        if (!definition.effectPath().clientProgramClass().equals(definition.effectType().clientProgramClass())) {
            throw new IllegalStateException("Midsize typed client mapping drifted for " + style.id());
        }
        playBurstSound(level, payload);
        if (definition.effectType() == MidsizeFireworkDefinition.EffectType.DENSE_SPHERE) {
            ACTIVE_SPECIAL_BURSTS.add(new MidsizeSphereBurst(payload));
        } else {
            ACTIVE_SPECIAL_BURSTS.add(new MidsizeRadialBurst(payload));
        }
    }

    private static void startOtherBurst(ClientLevel level, GrandFireworkBurstPayload payload, FireworkStyle style) {
        BatchOtherFirework definition = BatchOtherCatalog.byId(style.id());
        BatchOtherClientPrograms.Program program = BatchOtherClientPrograms.require(definition.clientProgram());
        if (program.route().clientProgramId() == null
                || !program.route().clientProgramId().equals(definition.clientProgram())) {
            throw new IllegalStateException("batch_other typed client mapping drifted for " + style.id());
        }
        playBurstSound(level, payload);
        ACTIVE_SPECIAL_BURSTS.add(new OtherBurst(payload, style, program));
    }

    private static void startOtherExtraBurst(ClientLevel level, GrandFireworkBurstPayload payload, FireworkStyle style) {
        BatchOtherExtraFirework definition = BatchOtherExtraCatalog.byId(style.id());
        if (!definition.clientProgram().equals(definition.effectPath().clientProgramId())) {
            throw new IllegalStateException("batch_other_extra typed client mapping drifted for " + style.id());
        }
        playBurstSound(level, payload);
        ACTIVE_SPECIAL_BURSTS.add(new OtherExtraBurst(
                payload, style, BatchOtherExtraClientPrograms.forEntry(definition.id())));
    }

    private static void startOrdinaryBurst(
            ClientLevel level, GrandFireworkBurstPayload payload, FireworkStyle style) {
        playBurstSound(level, payload);
        ACTIVE_BURSTS.add(new ActiveBurst(payload.x(), payload.y(), payload.z(), payload.seed(), style));
    }

    private static void playBurstSound(ClientLevel level, GrandFireworkBurstPayload payload) {
        level.playLocalSound(
                payload.x(), payload.y(), payload.z(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.AMBIENT, 16.0F, 0.35F, false);
    }

    /** Every received compact event advances its own finite, style-defined visual frame without a shared quota. */
    private static void emitBurstQueue(Minecraft minecraft) {
        for (ActiveBurst burst : ACTIVE_BURSTS) {
            burst.beginTick();
        }
        for (ActiveBurst burst : ACTIVE_BURSTS) {
            if (burst.isBranchProgramBurst()) {
                while (burst.hasReadyBranchRing()) {
                    burst.emitWholeBranchRing(minecraft);
                }
            } else {
                burst.emit(minecraft);
            }
        }

        Iterator<ActiveBurst> iterator = ACTIVE_BURSTS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().complete()) {
                iterator.remove();
            }
        }
    }

    private static void emitPrototypeQueue(Minecraft minecraft) {
        Iterator<PrototypeBurst> iterator = ACTIVE_PROTOTYPES.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick(minecraft)) {
                iterator.remove();
            }
        }
    }

    private static void emitSpecialBurstQueue(Minecraft minecraft) {
        Iterator<SpecialBurst> iterator = ACTIVE_SPECIAL_BURSTS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick(minecraft)) {
                iterator.remove();
            }
        }
    }

    /** Trail density is defined by each style and is evaluated only on the physical client. */
    private static void emitRocketTrails(Minecraft minecraft, ClientLevel level) {
        AABB visibleArea = minecraft.player.getBoundingBox().inflate(256.0D);
        List<GrandFireworkRocketEntity> rockets = level.getEntitiesOfClass(
                GrandFireworkRocketEntity.class, visibleArea);
        if (rockets.isEmpty()) {
            return;
        }
        for (GrandFireworkRocketEntity rocket : rockets) {
            FireworkStyle style = rocket.style();
            int trailStars = isGoldenDemonstration(style)
                    ? GOLDEN_TRAIL_STARS_PER_TICK
                    : style.trailStarsPerTick();
            for (int sparkIndex = 0; sparkIndex < trailStars; sparkIndex++) {
                emitTrailSpark(minecraft, level, rocket, style);
            }
        }
    }

    private static boolean emitTrailSpark(
            Minecraft minecraft,
            ClientLevel level,
            GrandFireworkRocketEntity rocket,
            FireworkStyle style) {
        int lifetime = isGoldenDemonstration(style) ? GOLDEN_TRAIL_LIFETIME : style.trailLifetime();
        Vec3 motion = rocket.getDeltaMovement();
        double spreadX = (level.random.nextDouble() - 0.5D) * 0.16D;
        double spreadZ = (level.random.nextDouble() - 0.5D) * 0.16D;
        Particle spark = FireworkParticleAppearance.createSpark(
                minecraft,
                rocket.getX(),
                rocket.getY(),
                rocket.getZ(),
                spreadX,
                -motion.y * 0.20D,
                spreadZ);
        if (spark == null) {
            return false;
        }

        setGoldWhiteTailColor(spark, level.random.nextFloat());
        FireworkParticleAppearance.applyVisibilityScale(spark, 1.05F);
        spark.setLifetime(lifetime);
        return true;
    }

    private static boolean isGoldenDemonstration(FireworkStyle style) {
        return style.index() == FireworkStyle.GRAND_GOLDEN_SPHERE.index();
    }

    private static boolean isWillowStyle(FireworkStyle style) {
        return style.shape() == FireworkStyle.Shape.WILLOW_SPHERE;
    }

    private static boolean isCoreSphereLayer(FireworkStyle style, BurstStage stage) {
        return (style.shape() == FireworkStyle.Shape.SPHERE
                || style.shape() == FireworkStyle.Shape.DOUBLE_SPHERE)
                && stage != BurstStage.MAIN;
    }

    private static boolean isRadiantStyle(FireworkStyle style) {
        return style.shape() == FireworkStyle.Shape.RADIANT;
    }

    private static boolean isRadiantWillowStyle(FireworkStyle style) {
        return style.shape() == FireworkStyle.Shape.RADIANT_WILLOW;
    }

    private enum BurstStage {
        MAIN,
        SECONDARY,
        ACCENT
    }

    private interface PrototypeBurst {
        boolean tick(Minecraft minecraft);
    }

    private interface SpecialBurst {
        boolean tick(Minecraft minecraft);
    }

    private static final class MidsizeSphereBurst implements SpecialBurst {
        private final MidsizeDenseSphereClientProgram program;

        private MidsizeSphereBurst(GrandFireworkBurstPayload payload) {
            this.program = new MidsizeDenseSphereClientProgram(
                    new MidsizeDenseSphereClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
        }

        @Override
        public boolean tick(Minecraft minecraft) {
            return program.tick(minecraft);
        }
    }

    private static final class MidsizeRadialBurst implements SpecialBurst {
        private final MidsizeDenseRadialClientProgram program;

        private MidsizeRadialBurst(GrandFireworkBurstPayload payload) {
            this.program = new MidsizeDenseRadialClientProgram(
                    new MidsizeDenseRadialClientProgram.Request(payload.x(), payload.y(), payload.z(), payload.seed()));
        }

        @Override
        public boolean tick(Minecraft minecraft) {
            return program.tick(minecraft);
        }
    }

    private static final class OtherBurst implements SpecialBurst {
        private final double x;
        private final double y;
        private final double z;
        private final long seed;
        private final FireworkStyle style;
        private final BatchOtherClientPrograms.Program program;
        private int age;

        private OtherBurst(GrandFireworkBurstPayload payload, FireworkStyle style,
                BatchOtherClientPrograms.Program program) {
            this.x = payload.x();
            this.y = payload.y();
            this.z = payload.z();
            this.seed = payload.seed();
            this.style = style;
            this.program = program;
        }

        @Override
        public boolean tick(Minecraft minecraft) {
            for (BatchOtherClientPrograms.Emission emission : program.emissionsAtTick(seed, age)) {
                BatchOtherClientPrograms.Point sample = emission.point();
                Particle spark = FireworkParticleAppearance.createSpark(
                        minecraft, sample.x() + x, sample.y() + y, sample.z() + z, 0.0D, 0.0D, 0.0D);
                if (spark == null) {
                    continue;
                }
                applyStageColor(spark, style, program.colorStage(emission.branch(), emission.segment()));
                spark.setLifetime(program.lifetimeTicks(seed, emission.branch(), emission.segment()));
                FireworkParticleAppearance.applyVisibilityScale(spark, 1.12F);
            }
            age++;
            return age >= program.spec().startDelay() + program.totalTicks();
        }
    }

    private static final class OtherExtraBurst implements SpecialBurst {
        private final double x;
        private final double y;
        private final double z;
        private final long seed;
        private final FireworkStyle style;
        private final BatchOtherExtraClientPrograms.Program program;
        private int age;

        private OtherExtraBurst(GrandFireworkBurstPayload payload, FireworkStyle style,
                BatchOtherExtraClientPrograms.Program program) {
            this.x = payload.x();
            this.y = payload.y();
            this.z = payload.z();
            this.seed = payload.seed();
            this.style = style;
            this.program = program;
        }

        @Override
        public boolean tick(Minecraft minecraft) {
            for (BatchOtherExtraTrajectory.Sample sample : program.emissionsAtTick(seed, age)) {
                Particle spark = FireworkParticleAppearance.createSpark(
                        minecraft,
                        sample.point().x() + x,
                        sample.point().y() + y,
                        sample.point().z() + z,
                        0.0D,
                        0.0D,
                        0.0D);
                if (spark == null) {
                    continue;
                }
                applyStageColor(spark, style, sample.colorStage());
                spark.setLifetime(sample.lifetimeTicks());
                FireworkParticleAppearance.applyVisibilityScale(
                        spark, sample.layer() == BatchOtherExtraTrajectory.Layer.CORE ? 1.28F : 1.08F);
            }
            age++;
            return age >= program.trajectory().startDelayTicks() + program.trajectory().segmentCount();
        }
    }

    private static void applyStageColor(Particle spark, FireworkStyle style, int stage) {
        FireworkStyle.Rgb color = switch (stage) {
            case 0 -> style.primaryColor();
            case 1 -> style.secondaryColor();
            default -> style.accentColor();
        };
        FireworkParticleAppearance.applyVividColor(
                spark, color.red(), color.green(), color.blue(), 1.04F, FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
    }

    private static final class HybridPrototypeBurst implements PrototypeBurst {
        private final HybridSphereRadiantParticleProgram program;

        private HybridPrototypeBurst(GrandFireworkBurstPayload payload, FireworkStyle style) {
            this.program = new HybridSphereRadiantParticleProgram(
                    Minecraft.getInstance(),
                    payload.x(),
                    payload.y(),
                    payload.z(),
                    payload.seed(),
                    style.radiantProfile(),
                    style.primaryColor(),
                    style.secondaryColor(),
                    style.accentColor());
        }

        @Override
        public boolean tick(Minecraft minecraft) {
            this.program.emitTick();
            return this.program.complete();
        }
    }

    private static final class SaturnPrototypeBurst implements PrototypeBurst {
        private final SaturnClientPlan plan;
        private final SaturnProgram program;
        private final double x;
        private final double y;
        private final double z;
        private final long seed;
        private int age;

        private SaturnPrototypeBurst(GrandFireworkBurstPayload payload, FireworkStyle style) {
            SaturnProgram integratedProgram = NormalFireworkCatalog.saturnProgramFor(style.id());
            this.program = integratedProgram == null ? SaturnProgram.prototype(style) : integratedProgram;
            this.plan = new SaturnClientPlan(this.program);
            this.x = payload.x();
            this.y = payload.y();
            this.z = payload.z();
            this.seed = payload.seed();
        }

        @Override
        public boolean tick(Minecraft minecraft) {
            if (this.age >= this.program.totalTicks()) {
                return true;
            }
            List<SaturnEmission> emissions = this.plan.emissionsAtTick(this.seed, this.age);
            for (SaturnEmission emission : emissions) {
                Vec3 position = emission.position().add(this.x, this.y, this.z);
                Vec3 velocity = emission.normal().scale(0.035D);
                Particle spark = FireworkParticleAppearance.createSpark(
                        minecraft,
                        position.x,
                        position.y,
                        position.z,
                        velocity.x,
                        velocity.y,
                        velocity.z);
                if (spark == null) {
                    continue;
                }
                boolean coreHighlight = emission.kind()
                        == com.urbanforma.fireworks.content.saturn.SaturnGeometry.Kind.SPHERE;
                if (coreHighlight) {
                    FireworkParticleAppearance.applyCoreColor(
                            spark,
                            emission.color().red(),
                            emission.color().green(),
                            emission.color().blue());
                } else {
                    FireworkParticleAppearance.applyVividColor(
                            spark,
                            emission.color().red(),
                            emission.color().green(),
                            emission.color().blue(),
                            1.04F,
                            FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
                }
                FireworkParticleAppearance.applyVisibilityScale(
                        spark,
                        coreHighlight ? 1.28F : 1.10F,
                        coreHighlight);
                spark.setLifetime(emission.lifetimeTicks());
                if (spark instanceof FireworkParticles.SparkParticle fireworkSpark) {
                    fireworkSpark.setTwinkle(true);
                }
            }
            this.age++;
            return this.age >= this.program.totalTicks();
        }
    }

    private static final class ActiveBurst {
        private final double x;
        private final double y;
        private final double z;
        private final FireworkStyle style;
        private final ColorChangeBallProgram.Profile colorChangeProfile;
        private final List<ColorChangedParticle> colorChangedParticles = new ArrayList<>();
        private final Random random;
        private final double phase;
        private final double secondaryPhase;
        private final double accentPhase;
        private final BranchProgram branchProgram;
        private final int[] emittedByStage = new int[BurstStage.values().length];
        private int goldenEmitted;
        private int elapsedTicks;
        private int stageRoundRobinCursor;

        private ActiveBurst(double x, double y, double z, long seed, FireworkStyle style) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.style = style;
            this.colorChangeProfile = ColorChangeBallProgram.profileFor(style);
            this.random = new Random(seed);
            this.phase = this.random.nextDouble() * Math.PI * 2.0D;
            if (isGoldenDemonstration(style)) {
                this.secondaryPhase = 0.0D;
                this.accentPhase = 0.0D;
                this.branchProgram = null;
            } else if (isWillowStyle(style)) {
                this.secondaryPhase = 0.0D;
                this.accentPhase = 0.0D;
                this.branchProgram = new WillowBranchProgram(x, y, z, seed, style);
            } else if (isRadiantStyle(style)) {
                this.secondaryPhase = 0.0D;
                this.accentPhase = 0.0D;
                this.branchProgram = new RadiantBranchProgram(x, y, z, seed, style);
            } else if (isRadiantWillowStyle(style)) {
                this.secondaryPhase = 0.0D;
                this.accentPhase = 0.0D;
                this.branchProgram = new RadiantWillowBranchProgram(x, y, z, seed, style);
            } else {
                this.secondaryPhase = (this.random.nextDouble() - 0.5D) * 0.42D;
                this.accentPhase = (this.random.nextDouble() - 0.5D) * 0.74D;
                this.branchProgram = null;
            }
        }

        private void beginTick() {
            if (!isGoldenDemonstration(this.style)) {
                this.elapsedTicks++;
            }
            if (this.branchProgram != null) {
                this.branchProgram.beginTick();
            }
            this.updateColorChangedParticles();
        }

        private boolean isBranchProgramBurst() {
            return this.branchProgram != null;
        }

        private boolean hasReadyBranchRing() {
            return this.branchProgram != null && this.branchProgram.hasWholeSegmentReady();
        }

        private int emitWholeBranchRing(Minecraft minecraft) {
            if (!hasReadyBranchRing()) {
                return 0;
            }
            int emitted = this.branchProgram.emitWholeSegment(minecraft);
            return emitted;
        }

        private int emit(Minecraft minecraft) {
            if (this.branchProgram != null) {
                return this.emitWholeBranchRing(minecraft);
            }

            int frameParticleCount = isGoldenDemonstration(this.style)
                    ? GOLDEN_STARS_PER_TICK
                    : this.style.starsPerTick();
            int emittedNow = 0;
            while (emittedNow < frameParticleCount && !this.complete()) {
                if (isGoldenDemonstration(this.style)) {
                    this.emitGoldenSpark(minecraft);
                    this.goldenEmitted++;
                } else {
                    BurstStage stage = this.nextReadyStage();
                    if (stage == null) {
                        break;
                    }
                    this.emitStyledSpark(minecraft, stage);
                    this.emittedByStage[stage.ordinal()]++;
                }
                emittedNow++;
            }
            return emittedNow;
        }

        private BurstStage nextReadyStage() {
            BurstStage selected = null;
            double lowestProgress = Double.MAX_VALUE;
            BurstStage[] stages = BurstStage.values();
            for (int offset = 0; offset < stages.length; offset++) {
                BurstStage candidate = stages[(this.stageRoundRobinCursor + offset) % stages.length];
                int count = this.stageCount(candidate);
                if (count <= 0 || this.emittedByStage[candidate.ordinal()] >= count || !this.stageReady(candidate)) {
                    continue;
                }

                double progress = (double) this.emittedByStage[candidate.ordinal()] / count;
                if (progress < lowestProgress) {
                    lowestProgress = progress;
                    selected = candidate;
                }
            }

            if (selected != null) {
                this.stageRoundRobinCursor = (selected.ordinal() + 1) % stages.length;
            }
            return selected;
        }

        private boolean stageReady(BurstStage stage) {
            if (stage == BurstStage.MAIN) {
                return true;
            }
            return switch (this.style.shape()) {
                case CROWN_SPHERE -> this.elapsedTicks >= this.style.phaseDelayTicks();
                case SPHERE, DOUBLE_SPHERE -> true;
                case WILLOW_SPHERE, RADIANT, RADIANT_WILLOW, GIANT_RADIANT,
                        HYBRID_SPHERE_RADIANT, SATURN, OTHER -> throw new IllegalStateException(
                        "Branch styles use a dedicated branch program");
            };
        }

        private int stageCount(BurstStage stage) {
            return switch (stage) {
                case MAIN -> this.style.mainStarCount();
                case SECONDARY -> this.style.secondaryStarCount();
                case ACCENT -> this.style.accentStarCount();
            };
        }

        private void emitGoldenSpark(Minecraft minecraft) {
            boolean outer = this.goldenEmitted < GOLDEN_OUTER_STARS;
            int index = outer ? this.goldenEmitted : this.goldenEmitted - GOLDEN_OUTER_STARS;
            int total = outer ? GOLDEN_OUTER_STARS : GOLDEN_INNER_STARS;
            Vec3 direction = fibonacciDirection(index, total, this.phase, this.random);
            double speed = outer
                    ? GOLDEN_OUTER_SPEED * (0.96D + this.random.nextDouble() * 0.08D)
                    : GOLDEN_INNER_SPEED * (0.94D + this.random.nextDouble() * 0.12D);
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    this.x,
                    this.y,
                    this.z,
                    direction.x * speed,
                    direction.y * speed,
                    direction.z * speed);
            if (spark == null) {
                return;
            }

            int lifetime = randomizedGoldenLifetime(outer, this.random);
            FireworkParticleAppearance.applyVisibilityScale(spark, outer ? 1.48F : 1.24F);
            setGoldenBurstColor(spark, outer, this.random.nextFloat());
            spark.setLifetime(lifetime);
            enableTwinkle(spark);
        }

        private void emitStyledSpark(Minecraft minecraft, BurstStage stage) {
            int index = this.emittedByStage[stage.ordinal()];
            int total = this.stageCount(stage);
            Vec3 direction = this.styledDirection(stage, index, total);
            double speed = this.styledSpeed(stage);
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    this.x,
                    this.y,
                    this.z,
                    direction.x * speed,
                    direction.y * speed,
                    direction.z * speed);
            if (spark == null) {
                return;
            }

            boolean coreHighlight = isCoreSphereLayer(this.style, stage);
            float baseScale = coreHighlight
                    ? 1.48F
                    : stage == BurstStage.MAIN ? 1.48F : stage == BurstStage.SECONDARY ? 1.34F : 1.18F;
            FireworkParticleAppearance.applyVisibilityScale(spark, baseScale, coreHighlight);
            spark.setLifetime(this.styledLifetime(stage));
            setVividColor(
                    spark, this.stageColor(stage), stage, this.random.nextFloat(), coreHighlight);
            if (!coreHighlight) {
                enableRandomTwinkle(spark);
            }
            if (this.colorChangeProfile != null) {
                this.colorChangedParticles.add(new ColorChangedParticle(
                        spark,
                        switch (stage) {
                            case MAIN -> ColorChangeBallProgram.Layer.PRIMARY;
                            case SECONDARY -> ColorChangeBallProgram.Layer.SECONDARY;
                            case ACCENT -> ColorChangeBallProgram.Layer.ACCENT;
                        },
                        coreHighlight));
            }
        }

        private void updateColorChangedParticles() {
            if (this.colorChangeProfile == null || this.colorChangedParticles.isEmpty()) {
                return;
            }
            Iterator<ColorChangedParticle> iterator = this.colorChangedParticles.iterator();
            while (iterator.hasNext()) {
                ColorChangedParticle tracked = iterator.next();
                if (!tracked.particle().isAlive()) {
                    iterator.remove();
                    continue;
                }
                ColorChangeBallParticleAdapter.apply(
                        tracked.particle(),
                        this.colorChangeProfile,
                        tracked.layer(),
                        this.elapsedTicks,
                        tracked.coreHighlight());
            }
        }

        private Vec3 styledDirection(BurstStage stage, int index, int total) {
            Vec3 direction = fibonacciDirection(index, total, this.stagePhase(stage), this.random);
            return switch (this.style.shape()) {
                case SPHERE, DOUBLE_SPHERE -> direction;
                case CROWN_SPHERE -> switch (stage) {
                    case MAIN -> direction;
                    case SECONDARY -> raisedCrownDirection(direction);
                    case ACCENT -> crownRimDirection(direction);
                };
                case WILLOW_SPHERE, RADIANT, RADIANT_WILLOW, GIANT_RADIANT,
                        HYBRID_SPHERE_RADIANT, SATURN, OTHER -> throw new IllegalStateException(
                        "Branch styles use a dedicated branch program");
            };
        }

        private double stagePhase(BurstStage stage) {
            return switch (stage) {
                case MAIN -> this.phase;
                case SECONDARY -> this.phase + this.secondaryPhase;
                case ACCENT -> this.phase + this.accentPhase;
            };
        }

        private double styledSpeed(BurstStage stage) {
            double diameterScale = Math.min(120.0D, this.style.diameter()) / GOLDEN_DIAMETER;
            double requestedSpeed = GOLDEN_OUTER_SPEED * diameterScale
                    * (0.92D + this.random.nextDouble() * 0.08D);
            double envelopeSpeedLimit = GOLDEN_OUTER_SPEED
                    * Math.min(120.0D, this.style.fullEnvelope()) / GOLDEN_DIAMETER;
            double shellSpeed = Math.min(requestedSpeed, envelopeSpeedLimit);
            return switch (this.style.shape()) {
                case SPHERE -> switch (stage) {
                    case MAIN -> shellSpeed;
                    case SECONDARY -> shellSpeed * 0.61D;
                    case ACCENT -> shellSpeed * 0.33D;
                };
                case DOUBLE_SPHERE -> switch (stage) {
                    case MAIN -> shellSpeed * 0.96D;
                    case SECONDARY -> shellSpeed * 0.67D;
                    case ACCENT -> shellSpeed * 0.36D;
                };
                case CROWN_SPHERE -> switch (stage) {
                    case MAIN -> shellSpeed * 0.78D;
                    case SECONDARY -> shellSpeed * 0.96D;
                    case ACCENT -> shellSpeed * 0.90D;
                };
                case WILLOW_SPHERE, RADIANT, RADIANT_WILLOW, GIANT_RADIANT,
                        HYBRID_SPHERE_RADIANT, SATURN, OTHER -> throw new IllegalStateException(
                        "Branch styles use a dedicated branch program");
            };
        }

        private int styledLifetime(BurstStage stage) {
            int baseLifetime = switch (stage) {
                case MAIN -> this.style.outerLifetime();
                case SECONDARY -> this.style.innerLifetime();
                case ACCENT -> this.style.accentLifetime();
            };
            int jitter = Math.max(4, Math.min(16, baseLifetime / 10));
            return baseLifetime + this.random.nextInt(jitter);
        }

        private FireworkStyle.Rgb stageColor(BurstStage stage) {
            return switch (stage) {
                case MAIN -> this.style.primaryColor();
                case SECONDARY -> this.style.secondaryColor();
                case ACCENT -> this.style.accentColor();
            };
        }

        private void enableRandomTwinkle(Particle particle) {
            float chanceMin = Math.max(0.35F, Math.min(0.60F, this.style.twinkleChanceMin()));
            float chanceMax = Math.max(chanceMin, Math.min(0.60F, this.style.twinkleChanceMax()));
            float chance = chanceMin + this.random.nextFloat() * (chanceMax - chanceMin);
            if (this.random.nextFloat() < chance) {
                enableTwinkle(particle);
            }
        }

        private boolean complete() {
            if (this.branchProgram != null) {
                return this.branchProgram.complete();
            }
            if (isGoldenDemonstration(this.style)) {
                return this.goldenEmitted >= GOLDEN_OUTER_STARS + GOLDEN_INNER_STARS;
            }
            return this.emittedByStage[BurstStage.MAIN.ordinal()] >= this.stageCount(BurstStage.MAIN)
                    && this.emittedByStage[BurstStage.SECONDARY.ordinal()] >= this.stageCount(BurstStage.SECONDARY)
                    && this.emittedByStage[BurstStage.ACCENT.ordinal()] >= this.stageCount(BurstStage.ACCENT);
        }
    }

    /** A client-side branch program that emits one complete segment ring at a time. */
    private interface BranchProgram {
        void beginTick();

        boolean hasWholeSegmentReady();

        int ringSize();

        int emitWholeSegment(Minecraft minecraft);

        boolean complete();
    }

    /**
     * A deterministic willow is a set of growing curves, not a slowed spherical shell. Each
     * segment emits the matching node from every branch, so the visible result reads as 160 arcs.
     */
    private static final class WillowBranchProgram implements BranchProgram {
        private final double x;
        private final double y;
        private final double z;
        private final FireworkStyle style;
        private final FireworkStyle.WillowProfile profile;
        private final WillowTrajectory.Branch[] branches;
        private int currentSegment = -1;
        private int nextBranch;
        private int segmentDelayTicks;

        private WillowBranchProgram(double x, double y, double z, long seed, FireworkStyle style) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.style = style;
            this.profile = style.willowProfile();
            if (this.profile == null) {
                throw new IllegalArgumentException("Willow style is missing its WillowProfile");
            }
            this.branches = new WillowTrajectory.Branch[this.profile.branchCount()];
            for (int index = 0; index < this.branches.length; index++) {
                this.branches[index] = WillowTrajectory.branch(this.profile, seed, index);
            }
        }

        @Override
        public void beginTick() {
            if (this.complete()) {
                return;
            }
            if (this.currentSegment < 0) {
                this.startSegment(0);
            } else if (this.segmentComplete()) {
                if (this.segmentDelayTicks > 0) {
                    this.segmentDelayTicks--;
                    return;
                }
                this.startSegment(this.currentSegment + 1);
            }
        }

        @Override
        public boolean hasWholeSegmentReady() {
            return this.currentSegment >= 0
                    && !this.complete()
                    && !this.segmentComplete()
                    && this.nextBranch == 0;
        }

        @Override
        public int ringSize() {
            return this.profile.branchCount();
        }

        @Override
        public int emitWholeSegment(Minecraft minecraft) {
            if (!this.hasWholeSegmentReady()) {
                return 0;
            }

            int expected = this.profile.branchCount();
            int emitted = this.emit(minecraft, expected);
            if (emitted == expected) {
                this.segmentDelayTicks = Math.max(0, WillowTrajectory.SEGMENT_INTERVAL_TICKS - 1);
            }
            return emitted;
        }

        private int emit(Minecraft minecraft, int allowance) {
            if (allowance <= 0 || this.currentSegment < 0 || this.complete()) {
                return 0;
            }

            int emitted = 0;
            while (emitted < allowance && !this.segmentComplete()) {
                this.emitBranchNode(minecraft, this.branches[this.nextBranch]);
                this.nextBranch++;
                emitted++;
            }
            return emitted;
        }

        @Override
        public boolean complete() {
            int lastSegment = this.profile.segmentsPerBranch() - 1;
            return this.currentSegment >= lastSegment && this.segmentComplete();
        }

        private boolean segmentComplete() {
            return this.currentSegment >= this.profile.segmentsPerBranch()
                    || this.currentSegment >= 0
                    && this.nextBranch >= this.profile.branchCount();
        }

        private void startSegment(int segment) {
            this.currentSegment = segment;
            this.nextBranch = 0;
        }

        private void emitBranchNode(Minecraft minecraft, WillowTrajectory.Branch branch) {
            WillowTrajectory.BranchSample sample = WillowTrajectory.sample(this.profile, branch, this.currentSegment);
            Vec3 position = sample.position().add(this.x, this.y, this.z);
            Vec3 tangent = sample.tangent();
            BurstStage colorStage = this.colorStage(sample.colorBand());
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    position.x,
                    position.y,
                    position.z,
                    tangent.x,
                    tangent.y,
                    tangent.z);
            if (spark == null) {
                return;
            }

            spark.setParticleSpeed(tangent.x, tangent.y, tangent.z);
            boolean coreHighlight = RadiantTrajectory.isCoreSegment(sample.segmentIndex());
            float baseScale = coreHighlight ? 1.48F : switch (colorStage) {
                case MAIN -> 1.04F;
                case SECONDARY -> 1.12F;
                case ACCENT -> 1.18F;
            };
            FireworkParticleAppearance.applyVisibilityScale(spark, baseScale, coreHighlight);
            spark.setLifetime(sample.lifetime());
            setVividColor(
                    spark, this.colorFor(sample.colorBand()), colorStage, sample.colorTone(), coreHighlight);
            if (!coreHighlight && sample.twinkles()) {
                enableTwinkle(spark);
            }
        }

        private BurstStage colorStage(WillowTrajectory.ColorBand colorBand) {
            return switch (colorBand) {
                case PRIMARY -> BurstStage.MAIN;
                case SECONDARY -> BurstStage.SECONDARY;
                case ACCENT -> BurstStage.ACCENT;
            };
        }

        private FireworkStyle.Rgb colorFor(WillowTrajectory.ColorBand colorBand) {
            return switch (colorBand) {
                case PRIMARY -> this.style.primaryColor();
                case SECONDARY -> this.style.secondaryColor();
                case ACCENT -> this.style.accentColor();
            };
        }
    }

    /**
     * A short-lived radiant burst. Its full Fibonacci-distributed ring is emitted every tick so
     * the explosion reads as a single outward break before its tips take on the slight fall from
     * {@link RadiantTrajectory}.
     */
    private static final class RadiantBranchProgram implements BranchProgram {
        private final double x;
        private final double y;
        private final double z;
        private final FireworkStyle style;
        private final FireworkStyle.RadiantProfile profile;
        private final RadiantTrajectory.Branch[] branches;
        private int currentSegment = -1;
        private int nextBranch;

        private RadiantBranchProgram(double x, double y, double z, long seed, FireworkStyle style) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.style = style;
            this.profile = style.radiantProfile();
            if (this.profile == null) {
                throw new IllegalArgumentException("Radiant style is missing its RadiantProfile");
            }
            if (this.profile.branchCount() != RadiantTrajectory.BRANCH_COUNT
                    || this.profile.segmentsPerBranch() != RadiantTrajectory.SEGMENTS_PER_BRANCH) {
                throw new IllegalArgumentException("RadiantProfile does not match the fixed radiant trajectory");
            }
            this.branches = new RadiantTrajectory.Branch[this.profile.branchCount()];
            for (int index = 0; index < this.branches.length; index++) {
                this.branches[index] = RadiantTrajectory.branch(this.profile, seed, index);
            }
        }

        @Override
        public void beginTick() {
            if (this.complete()) {
                return;
            }
            if (this.currentSegment < 0 || this.segmentComplete()) {
                this.startSegment(this.currentSegment + 1);
            }
        }

        @Override
        public boolean hasWholeSegmentReady() {
            return this.currentSegment >= 0
                    && !this.complete()
                    && !this.segmentComplete()
                    && this.nextBranch == 0;
        }

        @Override
        public int ringSize() {
            return this.profile.branchCount();
        }

        @Override
        public int emitWholeSegment(Minecraft minecraft) {
            if (!this.hasWholeSegmentReady()) {
                return 0;
            }

            int expected = this.ringSize();
            int emitted = 0;
            while (emitted < expected && !this.segmentComplete()) {
                this.emitBranchNode(minecraft, this.branches[this.nextBranch]);
                this.nextBranch++;
                emitted++;
            }
            return emitted;
        }

        @Override
        public boolean complete() {
            int lastSegment = this.profile.segmentsPerBranch() - 1;
            return this.currentSegment >= lastSegment && this.segmentComplete();
        }

        private boolean segmentComplete() {
            return this.currentSegment >= this.profile.segmentsPerBranch()
                    || this.currentSegment >= 0
                    && this.nextBranch >= this.ringSize();
        }

        private void startSegment(int segment) {
            this.currentSegment = segment;
            this.nextBranch = 0;
        }

        private void emitBranchNode(Minecraft minecraft, RadiantTrajectory.Branch branch) {
            RadiantTrajectory.BranchSample sample =
                    RadiantTrajectory.sample(this.profile, branch, this.currentSegment);
            Vec3 position = sample.position().add(this.x, this.y, this.z);
            Vec3 tangent = sample.tangent();
            BurstStage colorStage = this.colorStage(sample.colorBand());
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    position.x,
                    position.y,
                    position.z,
                    tangent.x,
                    tangent.y,
                    tangent.z);
            if (spark == null) {
                return;
            }

            spark.setParticleSpeed(tangent.x, tangent.y, tangent.z);
            FireworkParticleAppearance.applyVisibilityScale(spark, switch (colorStage) {
                case MAIN -> 1.08F;
                case SECONDARY -> 1.14F;
                case ACCENT -> 1.20F;
            });
            spark.setLifetime(sample.lifetime());
            setVividColor(spark, this.colorFor(sample.colorBand()), colorStage, sample.colorTone());
            if (sample.twinkles()) {
                enableTwinkle(spark, sample.twinklePhase());
            }
        }

        private BurstStage colorStage(RadiantTrajectory.ColorBand colorBand) {
            return switch (colorBand) {
                case PRIMARY -> BurstStage.MAIN;
                case SECONDARY -> BurstStage.SECONDARY;
                case ACCENT -> BurstStage.ACCENT;
            };
        }

        private FireworkStyle.Rgb colorFor(RadiantTrajectory.ColorBand colorBand) {
            return switch (colorBand) {
                case PRIMARY -> this.style.primaryColor();
                case SECONDARY -> this.style.secondaryColor();
                case ACCENT -> this.style.accentColor();
            };
        }
    }

    /**
     * The 4,800-particle radiant shell is the only allocation path for this effect. Once its last ring has
     * actually emitted, the 4,320 non-core sparks are directly repositioned into their long willow curves.
     */
    private static final class RadiantWillowBranchProgram implements BranchProgram {
        private static final int MANAGED_PARTICLE_LIFETIME = Integer.MAX_VALUE;

        private enum Phase {
            RADIANT,
            RETAINED_SPARKS,
            DRAINING,
            COMPLETE
        }

        private final double x;
        private final double y;
        private final double z;
        private final FireworkStyle style;
        private final FireworkStyle.RadiantWillowProfile profile;
        private final int extensionDurationTicks;
        private final RadiantTrajectory.Branch[] radiantBranches;
        private final RadiantWillowTrajectory.Branch[] willowBranches;
        private final ManagedRadiantSpark[][] managedSparks;
        private Phase phase = Phase.RADIANT;
        private int currentSegment = -1;
        private int nextBranch;
        private int extensionAgeTicks;
        private int drainAgeTicks;

        private RadiantWillowBranchProgram(double x, double y, double z, long seed, FireworkStyle style) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.style = style;
            this.profile = style.radiantWillowProfile();
            if (this.profile == null
                    || this.profile.branchCount() != RadiantWillowTrajectory.BRANCH_COUNT
                    || this.profile.radiantSegmentsPerBranch()
                    != RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH
                    || this.profile.managedFirstRadiantSegment()
                    != RadiantWillowTrajectory.MANAGED_FIRST_RADIANT_SEGMENT
                    || this.profile.managedSegmentsPerBranch()
                    != RadiantWillowTrajectory.MANAGED_SEGMENTS_PER_BRANCH) {
                throw new IllegalArgumentException("Radiant willow style is missing its fixed profile");
            }
            this.extensionDurationTicks = RadiantWillowTrajectory.extensionDurationTicks(this.profile, seed);
            this.radiantBranches = new RadiantTrajectory.Branch[this.profile.branchCount()];
            this.willowBranches = new RadiantWillowTrajectory.Branch[this.profile.branchCount()];
            this.managedSparks = new ManagedRadiantSpark[
                    RadiantWillowTrajectory.MANAGED_SEGMENTS_PER_BRANCH][this.profile.branchCount()];
            for (int index = 0; index < this.radiantBranches.length; index++) {
                this.radiantBranches[index] = RadiantTrajectory.branch(this.profile.radiantProfile(), seed, index);
                this.willowBranches[index] = RadiantWillowTrajectory.branch(this.profile, seed, index);
            }
        }

        @Override
        public void beginTick() {
            switch (this.phase) {
                case RADIANT -> {
                    this.releaseRemovedManagedSparks();
                    if (this.currentSegment < 0) {
                        this.startSegment(0);
                    } else if (this.segmentComplete()) {
                        this.startSegment(this.currentSegment + 1);
                    }
                }
                case RETAINED_SPARKS -> {
                    this.extensionAgeTicks++;
                    this.updateManagedSparks();
                    if (this.extensionAgeTicks >= this.extensionDurationTicks) {
                        this.phase = Phase.DRAINING;
                        this.drainAgeTicks = 0;
                        if (!this.updateDrainingSparks()) {
                            this.phase = Phase.COMPLETE;
                        }
                    }
                }
                case DRAINING -> {
                    this.drainAgeTicks++;
                    if (!this.updateDrainingSparks()) {
                        this.phase = Phase.COMPLETE;
                    }
                }
                case COMPLETE -> {
                    // The retained particles have already been released from the client reservation pool.
                }
            }
        }

        @Override
        public boolean hasWholeSegmentReady() {
            return this.phase == Phase.RADIANT
                    && this.currentSegment >= 0
                    && !this.segmentComplete()
                    && this.nextBranch == 0;
        }

        @Override
        public int ringSize() {
            return this.profile.branchCount();
        }

        @Override
        public int emitWholeSegment(Minecraft minecraft) {
            if (!this.hasWholeSegmentReady()) {
                return 0;
            }

            int emittedSegment = this.currentSegment;
            int expected = this.ringSize();
            int emitted = 0;
            while (emitted < expected && !this.segmentComplete()) {
                this.emitRadiantNode(minecraft, this.radiantBranches[this.nextBranch]);
                this.nextBranch++;
                emitted++;
            }

            if (emitted == expected
                    && emittedSegment == RadiantWillowTrajectory.RADIANT_SEGMENTS_PER_BRANCH - 1) {
                this.phase = Phase.RETAINED_SPARKS;
                this.extensionAgeTicks = 0;
                this.updateManagedSparks();
            }
            return emitted;
        }

        @Override
        public boolean complete() {
            return this.phase == Phase.COMPLETE;
        }

        private boolean segmentComplete() {
            return this.currentSegment >= 0 && this.nextBranch >= this.ringSize();
        }

        private void startSegment(int segment) {
            this.currentSegment = segment;
            this.nextBranch = 0;
        }

        private void emitRadiantNode(Minecraft minecraft, RadiantTrajectory.Branch branch) {
            RadiantTrajectory.BranchSample sample =
                    RadiantTrajectory.sample(this.profile.radiantProfile(), branch, this.currentSegment);
            Vec3 position = sample.position().add(this.x, this.y, this.z);
            Vec3 tangent = sample.tangent();
            BurstStage colorStage = this.colorStage(sample.colorBand());
            boolean managed = RadiantWillowTrajectory.isManagedRadiantSegment(this.currentSegment);
            Particle spark = FireworkParticleAppearance.createSpark(
                    minecraft,
                    position.x,
                    position.y,
                    position.z,
                    tangent.x,
                    tangent.y,
                    tangent.z);
            if (spark == null) {
                return;
            }

            spark.setParticleSpeed(tangent.x, tangent.y, tangent.z);
            boolean coreHighlight = RadiantTrajectory.isCoreSegment(sample.segmentIndex());
            float baseScale = coreHighlight ? 1.48F : switch (colorStage) {
                case MAIN -> 1.08F;
                case SECONDARY -> 1.14F;
                case ACCENT -> 1.20F;
            };
            FireworkParticleAppearance.applyVisibilityScale(spark, baseScale, coreHighlight);
            spark.setLifetime(managed ? MANAGED_PARTICLE_LIFETIME : sample.lifetime());
            setVividColor(
                    spark, this.colorFor(sample.colorBand()), colorStage, sample.colorTone(), coreHighlight);
            if (!coreHighlight && sample.twinkles()) {
                enableTwinkle(spark, sample.twinklePhase());
            }
            if (managed) {
                this.trackManagedSpark(spark, branch.index());
            }
        }

        /** Updates retained existing particles only; this continuation must never allocate a particle. */
        private void updateManagedSparks() {
            for (ManagedRadiantSpark[] branchSparks : this.managedSparks) {
                for (ManagedRadiantSpark managed : branchSparks) {
                    if (managed == null || managed.released()) {
                        continue;
                    }
                    Particle spark = managed.spark();
                    if (!spark.isAlive()) {
                        managed.markReleased();
                        continue;
                    }

                    RadiantWillowTrajectory.ManagedParticleSample sample = RadiantWillowTrajectory.managedParticle(
                            this.profile,
                            managed.branch(),
                            managed.radiantSegment(),
                            this.extensionAgeTicks,
                            this.extensionDurationTicks);
                    if (sample.retired()) {
                        managed.retire();
                        continue;
                    }

                    Vec3 position = sample.position().add(this.x, this.y, this.z);
                    Vec3 velocity = sample.velocity();
                    spark.setPos(position.x, position.y, position.z);
                    spark.setParticleSpeed(velocity.x, velocity.y, velocity.z);
                    int remainingLifetime = Math.max(
                            1,
                            RadiantWillowTrajectory.remainingLifetimeTicks(
                                            this.profile,
                                            managed.branch(),
                                            managed.radiantSegment(),
                                            this.extensionDurationTicks)
                                    - this.extensionAgeTicks);
                    if (sample.terminalFlicker().activeAt(this.extensionAgeTicks)) {
                        applyRetirementFlicker(spark, sample.terminalFlicker(), this.extensionAgeTicks);
                    } else {
                        spark.setLifetime(spark.age + remainingLifetime);
                    }
                }
            }
        }

        private void trackManagedSpark(Particle spark, int branchIndex) {
            int managedSegment = RadiantWillowTrajectory.managedSegmentIndex(this.currentSegment);
            ManagedRadiantSpark previous = this.managedSparks[managedSegment][branchIndex];
            if (previous != null) {
                previous.retire();
            }
            this.managedSparks[managedSegment][branchIndex] = new ManagedRadiantSpark(
                    spark,
                    this.currentSegment,
                    this.willowBranches[branchIndex]);
        }

        private void releaseRemovedManagedSparks() {
            for (ManagedRadiantSpark[] branchSparks : this.managedSparks) {
                for (ManagedRadiantSpark managed : branchSparks) {
                    if (managed != null && !managed.released() && !managed.spark().isAlive()) {
                        managed.markReleased();
                    }
                }
            }
        }

        /** Lets each remaining existing spark finish its own seeded flicker window before removal. */
        private boolean updateDrainingSparks() {
            boolean hasRemainingSparks = false;
            for (ManagedRadiantSpark[] branchSparks : this.managedSparks) {
                for (ManagedRadiantSpark managed : branchSparks) {
                    if (managed == null || managed.released()) {
                        continue;
                    }
                    Particle spark = managed.spark();
                    if (!spark.isAlive()) {
                        managed.markReleased();
                        continue;
                    }

                    RadiantWillowTrajectory.RetirementFlicker flicker =
                            RadiantWillowTrajectory.finalRetirementFlicker(
                                    managed.branch(), managed.radiantSegment());
                    if (this.drainAgeTicks >= flicker.endTick()) {
                        managed.retire();
                        continue;
                    }

                    RadiantWillowTrajectory.ManagedParticleSample finalSample =
                            RadiantWillowTrajectory.managedParticle(
                                    this.profile,
                                    managed.branch(),
                                    managed.radiantSegment(),
                                    this.extensionDurationTicks,
                                    this.extensionDurationTicks);
                    Vec3 position = finalSample.position().add(this.x, this.y, this.z);
                    spark.setPos(position.x, position.y, position.z);
                    spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
                    int remainingLifetime = Math.max(1, flicker.endTick() - this.drainAgeTicks + 1);
                    if (flicker.activeAt(this.drainAgeTicks)) {
                        applyRetirementFlicker(spark, flicker, this.drainAgeTicks);
                    } else {
                        spark.setLifetime(spark.age + remainingLifetime);
                    }
                    hasRemainingSparks = true;
                }
            }
            return hasRemainingSparks;
        }

        private BurstStage colorStage(RadiantTrajectory.ColorBand colorBand) {
            return switch (colorBand) {
                case PRIMARY -> BurstStage.MAIN;
                case SECONDARY -> BurstStage.SECONDARY;
                case ACCENT -> BurstStage.ACCENT;
            };
        }

        private FireworkStyle.Rgb colorFor(RadiantTrajectory.ColorBand colorBand) {
            return switch (colorBand) {
                case PRIMARY -> this.style.primaryColor();
                case SECONDARY -> this.style.secondaryColor();
                case ACCENT -> this.style.accentColor();
            };
        }

        private static final class ManagedRadiantSpark {
            private final Particle spark;
            private final int radiantSegment;
            private final RadiantWillowTrajectory.Branch branch;
            private boolean released;

            private ManagedRadiantSpark(
                    Particle spark,
                    int radiantSegment,
                    RadiantWillowTrajectory.Branch branch) {
                this.spark = spark;
                this.radiantSegment = radiantSegment;
                this.branch = branch;
            }

            private Particle spark() {
                return this.spark;
            }

            private int radiantSegment() {
                return this.radiantSegment;
            }

            private RadiantWillowTrajectory.Branch branch() {
                return this.branch;
            }

            private boolean released() {
                return this.released;
            }

            private void retire() {
                if (this.spark.isAlive()) {
                    this.spark.remove();
                }
                this.markReleased();
            }

            private void markReleased() {
                if (!this.released) {
                    this.released = true;
                }
            }
        }
    }

    private record ColorChangedParticle(
            Particle particle, ColorChangeBallProgram.Layer layer, boolean coreHighlight) {
    }

    private static Vec3 fibonacciDirection(int index, int count, double phase, Random random) {
        double y = 1.0D - 2.0D * ((double) index + 0.5D) / count;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double angle = index * GOLDEN_ANGLE + phase + (random.nextDouble() - 0.5D) * 0.028D;
        return new Vec3(horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
    }

    /** A broad upper dome that visibly rises above the compact main sphere. */
    private static Vec3 raisedCrownDirection(Vec3 sphereDirection) {
        double progress = (sphereDirection.y + 1.0D) * 0.5D;
        double y = 0.42D + progress * 0.55D;
        return directionAtHeight(sphereDirection, y);
    }

    /** A thick, bright lower edge of the crown rather than a flat geometric ring. */
    private static Vec3 crownRimDirection(Vec3 sphereDirection) {
        double progress = (sphereDirection.y + 1.0D) * 0.5D;
        double y = 0.18D + progress * 0.25D;
        return directionAtHeight(sphereDirection, y);
    }

    private static Vec3 directionAtHeight(Vec3 sourceDirection, double y) {
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double angle = Math.atan2(sourceDirection.z, sourceDirection.x);
        return new Vec3(horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
    }

    private static int randomizedGoldenLifetime(boolean outer, Random random) {
        return outer
                ? GOLDEN_OUTER_MIN_LIFETIME + random.nextInt(GOLDEN_OUTER_LIFETIME_VARIATION)
                : GOLDEN_INNER_MIN_LIFETIME + random.nextInt(GOLDEN_INNER_LIFETIME_VARIATION);
    }

    private static void setGoldWhiteTailColor(Particle particle, float tone) {
        if (tone < 0.45F) {
            particle.setColor(CHAMPAGNE_RED, CHAMPAGNE_GREEN, CHAMPAGNE_BLUE);
        } else if (tone < 0.80F) {
            particle.setColor(PEARL_RED, PEARL_GREEN, PEARL_BLUE);
        } else {
            particle.setColor(GOLD_RED, GOLD_GREEN, GOLD_BLUE);
        }
    }

    private static void setGoldenBurstColor(Particle particle, boolean outer, float tone) {
        if (outer) {
            if (tone < 0.18F) {
                particle.setColor(1.0F, 0.96F, 0.64F);
            } else if (tone < 0.76F) {
                particle.setColor(1.0F, 0.80F, 0.10F);
            } else {
                particle.setColor(1.0F, 0.60F, 0.02F);
            }
        } else if (tone < 0.48F) {
            particle.setColor(1.0F, 0.98F, 0.78F);
        } else {
            particle.setColor(1.0F, 0.88F, 0.30F);
        }
    }

    /** Keeps the jewel-tone layers saturated; only sparse accent stars are deliberately pearl-lifted. */
    private static void setVividColor(Particle particle, FireworkStyle.Rgb color, BurstStage stage, float tone) {
        setVividColor(particle, color, stage, tone, false);
    }

    private static void setVividColor(
            Particle particle,
            FireworkStyle.Rgb color,
            BurstStage stage,
            float tone,
            boolean coreHighlight) {
        if (coreHighlight) {
            FireworkParticleAppearance.applyCoreColor(
                    particle, color.red(), color.green(), color.blue());
            return;
        }
        float brilliance = switch (stage) {
            case MAIN -> 1.0F + tone * 0.22F;
            case SECONDARY -> 1.0F + tone * 0.20F;
            case ACCENT -> 1.0F + tone * 0.18F;
        };
        float highlight = switch (stage) {
            case MAIN -> 0.050F + tone * 0.050F;
            case SECONDARY -> 0.065F + tone * 0.060F;
            case ACCENT -> 0.100F + tone * 0.080F;
        };
        FireworkParticleAppearance.applyVividColor(
                particle, color.red(), color.green(), color.blue(), brilliance, highlight);
    }

    /** Enables vanilla spark blinking only inside the particle's seeded retirement window. */
    private static void applyRetirementFlicker(
            Particle particle, RadiantWillowTrajectory.RetirementFlicker flicker, int effectAgeTicks) {
        if (!flicker.activeAt(effectAgeTicks)) {
            return;
        }
        enableTwinkle(particle);
        int remainingTicks = Math.max(1, flicker.endTick() - effectAgeTicks);
        // SparkParticle renders by ((age + lifetime) / 3) parity; three ticks invert that stable cadence phase.
        particle.setLifetime(particle.age + remainingTicks + flicker.cadencePhase() * 3);
    }

    private static void enableTwinkle(Particle particle) {
        if (particle instanceof FireworkParticles.SparkParticle spark) {
            spark.setTwinkle(true);
        }
    }

    /**
     * Vanilla firework sparks blink on a short cadence. Seeded branches select either cadence phase without
     * reducing their configured lifetime, so one complete branch ring does not blink in lockstep.
     */
    private static void enableTwinkle(Particle particle, float phase) {
        if (particle instanceof FireworkParticles.SparkParticle spark) {
            spark.setTwinkle(true);
            spark.age = phase < 0.5F ? 0 : 1;
        }
    }
}
