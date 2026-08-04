package com.urbanforma.fireworks.client;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.RadiantTrajectory;
import com.urbanforma.fireworks.content.RadiantWillowTrajectory;
import com.urbanforma.fireworks.content.WillowTrajectory;
import com.urbanforma.fireworks.entity.GrandFireworkRocketEntity;
import com.urbanforma.fireworks.network.payload.GrandFireworkBurstPayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
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

    private static final int MAX_CLIENT_PARTICLES_PER_TICK = 720;
    private static final int MAX_BURST_PARTICLES_PER_TICK = 216;
    private static final int BURST_EMISSION_QUANTUM = 18;
    private static final int MAX_COMPLETE_BRANCH_RINGS_PER_TICK = 4;
    /**
     * Stay below Minecraft's 16,384 translucent-particle queue while accommodating three complete
     * 4,800-spark radiant shells. This is a reservation budget, not an active-burst cap: pending rings
     * remain in the normal round-robin scheduler until their sparks leave the effect.
     */
    private static final int MAX_OWNED_FIREWORK_SPARKS = 15_000;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final List<ActiveBurst> ACTIVE_BURSTS = new ArrayList<>();
    private static final TreeMap<Long, Integer> LIVE_SPARK_EXPIRY_COUNTS = new TreeMap<>();

    private static int burstRoundRobinCursor;
    private static int branchProgramRoundRobinCursor;
    private static int trailRoundRobinCursor;
    private static int liveOwnedFireworkSparks;
    private static long clientEffectTick;
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
        advanceSparkReservations();
        int remainingBudget = emitBurstQueue(minecraft, MAX_CLIENT_PARTICLES_PER_TICK);
        emitRocketTrails(minecraft, level, remainingBudget);
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearActiveEffects();
    }

    private static void clearActiveEffects() {
        ACTIVE_BURSTS.clear();
        burstRoundRobinCursor = 0;
        branchProgramRoundRobinCursor = 0;
        trailRoundRobinCursor = 0;
        liveOwnedFireworkSparks = 0;
        clientEffectTick = 0L;
        LIVE_SPARK_EXPIRY_COUNTS.clear();
        observedLevel = null;
    }

    private static void advanceSparkReservations() {
        clientEffectTick++;
        while (!LIVE_SPARK_EXPIRY_COUNTS.isEmpty()
                && LIVE_SPARK_EXPIRY_COUNTS.firstKey() <= clientEffectTick) {
            liveOwnedFireworkSparks -= LIVE_SPARK_EXPIRY_COUNTS.pollFirstEntry().getValue();
        }
        if (liveOwnedFireworkSparks < 0) {
            throw new IllegalStateException("Firework spark reservation accounting underflowed");
        }
    }

    private static boolean canReserveFireworkSparks(int count) {
        return count > 0 && liveOwnedFireworkSparks + count <= MAX_OWNED_FIREWORK_SPARKS;
    }

    private static boolean reserveFireworkSparks(int count, int lifetime) {
        if (!canReserveFireworkSparks(count) || lifetime <= 0) {
            return false;
        }
        long expiryTick = reservationExpiryTick(lifetime);
        LIVE_SPARK_EXPIRY_COUNTS.merge(expiryTick, count, Integer::sum);
        liveOwnedFireworkSparks += count;
        return true;
    }

    private static long reservationExpiryTick(int lifetime) {
        return clientEffectTick + lifetime + 1L;
    }

    /** Releases a reservation when a directly controlled spark actually leaves the client effect. */
    private static boolean releaseFireworkSparks(long expiryTick, int count) {
        if (count <= 0) {
            return false;
        }
        Integer scheduled = LIVE_SPARK_EXPIRY_COUNTS.get(expiryTick);
        if (scheduled == null || scheduled < count) {
            return false;
        }
        if (scheduled == count) {
            LIVE_SPARK_EXPIRY_COUNTS.remove(expiryTick);
        } else {
            LIVE_SPARK_EXPIRY_COUNTS.put(expiryTick, scheduled - count);
        }
        liveOwnedFireworkSparks -= count;
        if (liveOwnedFireworkSparks < 0) {
            throw new IllegalStateException("Firework spark reservation accounting underflowed");
        }
        return true;
    }

    private static void observeLevel(ClientLevel level) {
        if (observedLevel != null && observedLevel != level) {
            clearActiveEffects();
        }
        observedLevel = level;
    }

    private static void startBurst(ClientLevel level, GrandFireworkBurstPayload payload) {
        level.playLocalSound(
                payload.x(),
                payload.y(),
                payload.z(),
                SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.AMBIENT,
                16.0F,
                0.35F,
                false);
        FireworkStyle style = payload.style();
        ACTIVE_BURSTS.add(new ActiveBurst(payload.x(), payload.y(), payload.z(), payload.seed(), style));
    }

    /**
     * Complete shell programs are queued whole. The global budget delays excess work instead of
     * sampling stars away, while the rotating cursor prevents one active shell from monopolizing it.
     */
    private static int emitBurstQueue(Minecraft minecraft, int particleBudget) {
        if (particleBudget <= 0 || ACTIVE_BURSTS.isEmpty()) {
            return particleBudget;
        }

        for (ActiveBurst burst : ACTIVE_BURSTS) {
            burst.beginTick();
        }

        particleBudget = emitCompleteBranchProgramRings(minecraft, particleBudget);

        int burstCount = ACTIVE_BURSTS.size();
        int startIndex = Math.floorMod(burstRoundRobinCursor, burstCount);
        boolean emittedOnPass;
        do {
            emittedOnPass = false;
            for (int offset = 0; offset < burstCount && particleBudget > 0; offset++) {
                ActiveBurst burst = ACTIVE_BURSTS.get((startIndex + offset) % burstCount);
                if (burst.isBranchProgramBurst()) {
                    continue;
                }
                int emitted = burst.emit(minecraft, Math.min(BURST_EMISSION_QUANTUM, particleBudget));
                if (emitted > 0) {
                    particleBudget -= emitted;
                    emittedOnPass = true;
                }
            }
        } while (particleBudget > 0 && emittedOnPass);

        Iterator<ActiveBurst> iterator = ACTIVE_BURSTS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().complete()) {
                iterator.remove();
            }
        }

        if (ACTIVE_BURSTS.isEmpty()) {
            burstRoundRobinCursor = 0;
            branchProgramRoundRobinCursor = 0;
        } else {
            burstRoundRobinCursor = Math.floorMod(startIndex + 1, ACTIVE_BURSTS.size());
            branchProgramRoundRobinCursor = Math.floorMod(branchProgramRoundRobinCursor, ACTIVE_BURSTS.size());
        }
        return particleBudget;
    }

    /**
     * Emits complete branch rings before ordinary shell scheduling. A ring is never split: all
     * branches for one segment appear in the same client tick, while the rotating cursor shares
     * the four-ring global allowance fairly between willow and radiant programs.
     */
    private static int emitCompleteBranchProgramRings(Minecraft minecraft, int particleBudget) {
        if (particleBudget <= 0 || ACTIVE_BURSTS.isEmpty()) {
            return particleBudget;
        }

        int burstCount = ACTIVE_BURSTS.size();
        int startIndex = Math.floorMod(branchProgramRoundRobinCursor, burstCount);
        int lastSelectedIndex = -1;
        int emittedRings = 0;
        for (int offset = 0; offset < burstCount
                && emittedRings < MAX_COMPLETE_BRANCH_RINGS_PER_TICK; offset++) {
            int index = (startIndex + offset) % burstCount;
            ActiveBurst burst = ACTIVE_BURSTS.get(index);
            if (!burst.hasReadyBranchRing()) {
                continue;
            }

            int ringSize = burst.branchRingSize();
            if (ringSize > MAX_BURST_PARTICLES_PER_TICK) {
                throw new IllegalStateException("A branch ring exceeds the single-burst particle budget");
            }
            if (particleBudget < ringSize || !burst.canEmitWholeBranchRing()) {
                continue;
            }

            int emitted = burst.emitWholeBranchRing(minecraft);
            if (emitted != ringSize) {
                throw new IllegalStateException("A branch segment must emit as one complete ring");
            }
            particleBudget -= emitted;
            emittedRings++;
            lastSelectedIndex = index;
        }

        branchProgramRoundRobinCursor = lastSelectedIndex >= 0
                ? Math.floorMod(lastSelectedIndex + 1, burstCount)
                : Math.floorMod(startIndex + 1, burstCount);
        return particleBudget;
    }

    /** Tail sparks only use budget that remains after complete shell programs. */
    private static int emitRocketTrails(Minecraft minecraft, ClientLevel level, int particleBudget) {
        if (particleBudget <= 0) {
            return particleBudget;
        }

        AABB visibleArea = minecraft.player.getBoundingBox().inflate(256.0D);
        List<GrandFireworkRocketEntity> rockets = level.getEntitiesOfClass(
                GrandFireworkRocketEntity.class, visibleArea);
        if (rockets.isEmpty()) {
            trailRoundRobinCursor = 0;
            return particleBudget;
        }

        int rocketCount = rockets.size();
        int startIndex = Math.floorMod(trailRoundRobinCursor, rocketCount);
        int[] emittedByRocket = new int[rocketCount];
        boolean emittedOnPass;
        do {
            emittedOnPass = false;
            for (int offset = 0; offset < rocketCount && particleBudget > 0; offset++) {
                int rocketIndex = (startIndex + offset) % rocketCount;
                GrandFireworkRocketEntity rocket = rockets.get(rocketIndex);
                FireworkStyle style = rocket.style();
                int trailStars = isGoldenDemonstration(style)
                        ? GOLDEN_TRAIL_STARS_PER_TICK
                        : style.trailStarsPerTick();
                if (emittedByRocket[rocketIndex] >= trailStars) {
                    continue;
                }

                if (emitTrailSpark(minecraft, level, rocket, style)) {
                    emittedByRocket[rocketIndex]++;
                    particleBudget--;
                    emittedOnPass = true;
                } else {
                    // Trail frames are time-bound, so do not retry an unavailable frame in this client tick.
                    emittedByRocket[rocketIndex] = trailStars;
                }
            }
        } while (particleBudget > 0 && emittedOnPass);

        trailRoundRobinCursor = Math.floorMod(startIndex + 1, rocketCount);
        return particleBudget;
    }

    private static boolean emitTrailSpark(
            Minecraft minecraft,
            ClientLevel level,
            GrandFireworkRocketEntity rocket,
            FireworkStyle style) {
        int lifetime = isGoldenDemonstration(style) ? GOLDEN_TRAIL_LIFETIME : style.trailLifetime();
        if (!reserveFireworkSparks(1, lifetime)) {
            return false;
        }
        Vec3 motion = rocket.getDeltaMovement();
        double spreadX = (level.random.nextDouble() - 0.5D) * 0.16D;
        double spreadZ = (level.random.nextDouble() - 0.5D) * 0.16D;
        Particle spark = minecraft.particleEngine.createParticle(
                ParticleTypes.FIREWORK,
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
        spark.scale(1.05F);
        spark.setLifetime(lifetime);
        return true;
    }

    private static boolean isGoldenDemonstration(FireworkStyle style) {
        return style.index() == FireworkStyle.GRAND_GOLDEN_SPHERE.index();
    }

    private static boolean isWillowStyle(FireworkStyle style) {
        return style.shape() == FireworkStyle.Shape.WILLOW_SPHERE;
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

    private static final class ActiveBurst {
        private final double x;
        private final double y;
        private final double z;
        private final FireworkStyle style;
        private final Random random;
        private final double phase;
        private final double secondaryPhase;
        private final double accentPhase;
        private final BranchProgram branchProgram;
        private final int[] emittedByStage = new int[BurstStage.values().length];
        private int goldenEmitted;
        private int emittedThisTick;
        private int elapsedTicks;
        private int stageRoundRobinCursor;

        private ActiveBurst(double x, double y, double z, long seed, FireworkStyle style) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.style = style;
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
            this.emittedThisTick = 0;
            if (!isGoldenDemonstration(this.style)) {
                this.elapsedTicks++;
            }
            if (this.branchProgram != null) {
                this.branchProgram.beginTick();
            }
        }

        private boolean isBranchProgramBurst() {
            return this.branchProgram != null;
        }

        private boolean hasReadyBranchRing() {
            return this.branchProgram != null && this.branchProgram.hasWholeSegmentReady();
        }

        private int branchRingSize() {
            if (this.branchProgram == null) {
                throw new IllegalStateException("Only branch programs have complete branch rings");
            }
            return this.branchProgram.ringSize();
        }

        private int emitWholeBranchRing(Minecraft minecraft) {
            int ringSize = this.branchRingSize();
            int allowance = Math.min(requestedParticleAllowance(ringSize), ringSize);
            if (!hasReadyBranchRing() || allowance < ringSize) {
                return 0;
            }
            if (!reserveFireworkSparks(ringSize, this.branchProgram.maximumParticleLifetime())) {
                return 0;
            }

            int emitted = this.branchProgram.emitWholeSegment(minecraft);
            this.emittedThisTick += emitted;
            return emitted;
        }

        private boolean canEmitWholeBranchRing() {
            return this.branchProgram != null
                    && this.hasReadyBranchRing()
                    && canReserveFireworkSparks(this.branchRingSize());
        }

        private int requestedParticleAllowance(int requested) {
            return Math.min(requested, MAX_BURST_PARTICLES_PER_TICK - this.emittedThisTick);
        }

        private int emit(Minecraft minecraft, int requested) {
            if (this.branchProgram != null) {
                if (requested < this.branchProgram.ringSize()) {
                    return 0;
                }
                return this.emitWholeBranchRing(minecraft);
            }

            int styleLimit = isGoldenDemonstration(this.style)
                    ? GOLDEN_STARS_PER_TICK
                    : this.style.starsPerTick();
            int allowance = Math.min(
                    Math.min(requested, MAX_BURST_PARTICLES_PER_TICK - this.emittedThisTick),
                    Math.max(0, styleLimit - this.emittedThisTick));
            int emittedNow = 0;
            while (emittedNow < allowance && !this.complete()) {
                if (isGoldenDemonstration(this.style)) {
                    if (!reserveFireworkSparks(1, this.maximumGoldenLifetime())) {
                        break;
                    }
                    this.emitGoldenSpark(minecraft);
                    this.goldenEmitted++;
                } else {
                    BurstStage stage = this.nextReadyStage();
                    if (stage == null) {
                        break;
                    }
                    if (!reserveFireworkSparks(1, this.maximumStyledLifetime(stage))) {
                        break;
                    }
                    this.emitStyledSpark(minecraft, stage);
                    this.emittedByStage[stage.ordinal()]++;
                }
                this.emittedThisTick++;
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
                case WILLOW_SPHERE, RADIANT, RADIANT_WILLOW -> throw new IllegalStateException(
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
            Particle spark = minecraft.particleEngine.createParticle(
                    ParticleTypes.FIREWORK,
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
            spark.scale(outer ? 1.48F : 1.24F);
            setGoldenBurstColor(spark, outer, this.random.nextFloat());
            spark.setLifetime(lifetime);
            enableTwinkle(spark);
        }

        private void emitStyledSpark(Minecraft minecraft, BurstStage stage) {
            int index = this.emittedByStage[stage.ordinal()];
            int total = this.stageCount(stage);
            Vec3 direction = this.styledDirection(stage, index, total);
            double speed = this.styledSpeed(stage);
            Particle spark = minecraft.particleEngine.createParticle(
                    ParticleTypes.FIREWORK,
                    this.x,
                    this.y,
                    this.z,
                    direction.x * speed,
                    direction.y * speed,
                    direction.z * speed);
            if (spark == null) {
                return;
            }

            spark.scale(stage == BurstStage.MAIN ? 1.48F : stage == BurstStage.SECONDARY ? 1.34F : 1.18F);
            spark.setLifetime(this.styledLifetime(stage));
            setVividColor(spark, this.stageColor(stage), stage, this.random.nextFloat());
            enableRandomTwinkle(spark);
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
                case WILLOW_SPHERE, RADIANT, RADIANT_WILLOW -> throw new IllegalStateException(
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
                case WILLOW_SPHERE, RADIANT, RADIANT_WILLOW -> throw new IllegalStateException(
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

        private int maximumGoldenLifetime() {
            return this.goldenEmitted < GOLDEN_OUTER_STARS
                    ? GOLDEN_OUTER_MIN_LIFETIME + GOLDEN_OUTER_LIFETIME_VARIATION - 1
                    : GOLDEN_INNER_MIN_LIFETIME + GOLDEN_INNER_LIFETIME_VARIATION - 1;
        }

        private int maximumStyledLifetime(BurstStage stage) {
            int baseLifetime = switch (stage) {
                case MAIN -> this.style.outerLifetime();
                case SECONDARY -> this.style.innerLifetime();
                case ACCENT -> this.style.accentLifetime();
            };
            int jitter = Math.max(4, Math.min(16, baseLifetime / 10));
            return baseLifetime + jitter - 1;
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

        int maximumParticleLifetime();

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
        public int maximumParticleLifetime() {
            return this.currentSegment < WillowTrajectory.SHORT_LIVED_SEGMENT_COUNT
                    ? WillowTrajectory.SHORT_LIFETIME_MAX
                    : this.profile.maxLifetime();
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
            Particle spark = minecraft.particleEngine.createParticle(
                    ParticleTypes.FIREWORK,
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
            spark.scale(switch (colorStage) {
                case MAIN -> 1.04F;
                case SECONDARY -> 1.12F;
                case ACCENT -> 1.18F;
            });
            spark.setLifetime(sample.lifetime());
            setVividColor(spark, this.colorFor(sample.colorBand()), colorStage, sample.colorTone());
            if (sample.twinkles()) {
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
        public int maximumParticleLifetime() {
            return this.currentSegment < RadiantTrajectory.CORE_SEGMENT_COUNT
                    ? RadiantTrajectory.CORE_LIFETIME_MAX
                    : RadiantTrajectory.STAR_LIFETIME_MAX;
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
            Particle spark = minecraft.particleEngine.createParticle(
                    ParticleTypes.FIREWORK,
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
            spark.scale(switch (colorStage) {
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
        private static final int MANAGED_RESERVATION_LIFETIME = Integer.MAX_VALUE;

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
        public int maximumParticleLifetime() {
            if (this.phase != Phase.RADIANT || this.currentSegment < 0) {
                throw new IllegalStateException("Only a ready radiant ring may reserve particles");
            }
            return RadiantWillowTrajectory.isManagedRadiantSegment(this.currentSegment)
                    ? MANAGED_RESERVATION_LIFETIME
                    : RadiantTrajectory.CORE_LIFETIME_MAX;
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
            Particle spark = minecraft.particleEngine.createParticle(
                    ParticleTypes.FIREWORK,
                    position.x,
                    position.y,
                    position.z,
                    tangent.x,
                    tangent.y,
                    tangent.z);
            if (spark == null) {
                if (managed) {
                    releaseFireworkSparks(reservationExpiryTick(MANAGED_RESERVATION_LIFETIME), 1);
                }
                return;
            }

            spark.setParticleSpeed(tangent.x, tangent.y, tangent.z);
            spark.scale(switch (colorStage) {
                case MAIN -> 1.08F;
                case SECONDARY -> 1.14F;
                case ACCENT -> 1.20F;
            });
            spark.setLifetime(managed ? MANAGED_RESERVATION_LIFETIME : sample.lifetime());
            setVividColor(spark, this.colorFor(sample.colorBand()), colorStage, sample.colorTone());
            if (sample.twinkles()) {
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
                        managed.releaseReservation();
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
                    this.willowBranches[branchIndex],
                    reservationExpiryTick(MANAGED_RESERVATION_LIFETIME));
        }

        private void releaseRemovedManagedSparks() {
            for (ManagedRadiantSpark[] branchSparks : this.managedSparks) {
                for (ManagedRadiantSpark managed : branchSparks) {
                    if (managed != null && !managed.released() && !managed.spark().isAlive()) {
                        managed.releaseReservation();
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
                        managed.releaseReservation();
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
            private final long reservationExpiryTick;
            private boolean released;

            private ManagedRadiantSpark(
                    Particle spark,
                    int radiantSegment,
                    RadiantWillowTrajectory.Branch branch,
                    long reservationExpiryTick) {
                this.spark = spark;
                this.radiantSegment = radiantSegment;
                this.branch = branch;
                this.reservationExpiryTick = reservationExpiryTick;
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
                this.releaseReservation();
            }

            private void releaseReservation() {
                if (!this.released) {
                    releaseFireworkSparks(this.reservationExpiryTick, 1);
                    this.released = true;
                }
            }
        }
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
        float brilliance = switch (stage) {
            case MAIN -> 0.88F + tone * 0.22F;
            case SECONDARY -> 0.93F + tone * 0.20F;
            case ACCENT -> 0.98F + tone * 0.18F;
        };
        float highlight = switch (stage) {
            case MAIN -> 0.012F + tone * 0.024F;
            case SECONDARY -> 0.020F + tone * 0.035F;
            case ACCENT -> 0.055F + tone * 0.075F;
        };
        particle.setColor(
                vividChannel(color.red(), brilliance, highlight),
                vividChannel(color.green(), brilliance, highlight),
                vividChannel(color.blue(), brilliance, highlight));
    }

    private static float vividChannel(float channel, float brilliance, float highlight) {
        return Math.max(0.0F, Math.min(1.0F, channel * brilliance + (1.0F - channel) * highlight));
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
