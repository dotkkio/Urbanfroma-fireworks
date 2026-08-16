package com.urbanforma.fireworks.client.crown;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.crown.CrownDescentTrajectory;
import com.urbanforma.fireworks.content.crown.CrownDescentTrajectory.Branch;
import com.urbanforma.fireworks.content.crown.CrownDescentTrajectory.Column;
import com.urbanforma.fireworks.content.crown.CrownDescentTrajectory.Sample;
import com.urbanforma.fireworks.content.crown.CrownDescentTrajectory.Terminal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Physical-client-only crown descent renderer.
 *
 * <p>Before allocating any spark, this program resolves every terminal column against the current client level.
 * A missing chunk, invalid height, or unsafe drop retires the full request without a fallback trajectory.</p>
 */
public final class CrownDescentClientProgram {
    private static final int RETIREMENT_TWINKLE_LEAD_TICKS = 12;

    private final Request request;
    private final Branch[] branches = new Branch[CrownDescentTrajectory.BRANCH_COUNT];
    private final Terminal[] terminals = new Terminal[CrownDescentTrajectory.BRANCH_COUNT];
    private final List<TrackedSpark> trackedSparks = new ArrayList<>(
            CrownDescentTrajectory.INITIAL_BLOOM_PARTICLE_COUNT);
    private GroundResolution groundResolution = GroundResolution.PENDING;
    private ProgramState state = ProgramState.PENDING;
    private int age;
    private int createdParticles;

    public CrownDescentClientProgram(Request request) {
        this.request = Objects.requireNonNull(request, "request");
        for (int index = 0; index < this.branches.length; index++) {
            this.branches[index] = request.trajectory().branch(index);
        }
    }

    /** Returns true only after retirement or an intentional client-side fail-closed decision. */
    public boolean tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (this.state == ProgramState.FAILED_CLOSED || this.state == ProgramState.COMPLETE) {
            return true;
        }

        ClientLevel level = minecraft.level;
        if (level == null) {
            return false;
        }
        if (this.state == ProgramState.PENDING) {
            if (!resolveTerminals(level)) {
                this.state = ProgramState.FAILED_CLOSED;
                return true;
            }
            boolean bloomEmitted;
            try {
                bloomEmitted = emitCompleteBloom(minecraft);
            } catch (RuntimeException ignored) {
                bloomEmitted = false;
            }
            if (!bloomEmitted) {
                discardTrackedSparks();
                this.state = ProgramState.FAILED_CLOSED;
                return true;
            }
            this.state = ProgramState.ACTIVE;
        }

        updateTrackedSparks();
        this.age++;
        if (this.age >= CrownDescentTrajectory.VISUAL_LIFETIME_TICKS) {
            discardTrackedSparks();
            this.state = ProgramState.COMPLETE;
            return true;
        }
        return false;
    }

    public Request request() {
        return this.request;
    }

    public int age() {
        return this.age;
    }

    public int createdParticleCount() {
        return this.createdParticles;
    }

    public int trackedSparkCount() {
        return this.trackedSparks.size();
    }

    public int localParticlePeak() {
        return CrownDescentTrajectory.LOCAL_PEAK_OWNED_PARTICLES;
    }

    public GroundResolution groundResolution() {
        return this.groundResolution;
    }

    public boolean failedClosed() {
        return this.state == ProgramState.FAILED_CLOSED;
    }

    public boolean hasActiveVisual() {
        return this.state == ProgramState.ACTIVE && !this.trackedSparks.isEmpty();
    }

    /** Removes owned particles immediately when a level unloads or the bounded queue is cleared. */
    public void retire() {
        discardTrackedSparks();
        this.state = ProgramState.COMPLETE;
    }

    private boolean resolveTerminals(ClientLevel level) {
        try {
            for (int index = 0; index < this.branches.length; index++) {
                Branch branch = this.branches[index];
                Column column = this.request.trajectory().terminalColumn(this.request.x(), this.request.z(), branch);
                int chunkX = Math.floorDiv(column.blockX(), 16);
                int chunkZ = Math.floorDiv(column.blockZ(), 16);
                if (!level.hasChunk(chunkX, chunkZ)) {
                    this.groundResolution = GroundResolution.FAILED_CLOSED;
                    return false;
                }

                int terrainHeight = level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.blockX(), column.blockZ());
                double targetY = terrainHeight + CrownDescentTrajectory.GROUND_CLEARANCE_BLOCKS;
                if (terrainHeight <= level.getMinBuildHeight()
                        || terrainHeight >= level.getMaxBuildHeight()
                        || targetY >= level.getMaxBuildHeight()) {
                    this.groundResolution = GroundResolution.FAILED_CLOSED;
                    return false;
                }

                Terminal terminal = this.request.trajectory().terminal(
                        this.request.x(), this.request.z(), branch, terrainHeight);
                if (!this.request.trajectory().acceptsTerminal(this.request.y(), branch, terminal)) {
                    this.groundResolution = GroundResolution.FAILED_CLOSED;
                    return false;
                }
                this.terminals[index] = terminal;
            }
        } catch (RuntimeException ignored) {
            this.groundResolution = GroundResolution.FAILED_CLOSED;
            return false;
        }

        this.groundResolution = GroundResolution.RESOLVED;
        return true;
    }

    /** Allocates the full 72 by 8 crown on the first client tick, never progressively over network or server ticks. */
    private boolean emitCompleteBloom(Minecraft minecraft) {
        List<TrackedSpark> staged = new ArrayList<>(CrownDescentTrajectory.INITIAL_BLOOM_PARTICLE_COUNT);
        for (int branchIndex = 0; branchIndex < this.branches.length; branchIndex++) {
            Branch branch = this.branches[branchIndex];
            Terminal terminal = this.terminals[branchIndex];
            if (terminal == null) {
                discard(staged);
                return false;
            }
            for (int segment = 0; segment < CrownDescentTrajectory.TRAIL_SEGMENTS_PER_BRANCH; segment++) {
                Sample sample = this.request.trajectory().sample(
                        this.request.x(), this.request.y(), this.request.z(), branch, terminal, this.age, segment);
                Particle spark = FireworkParticleAppearance.createSpark(
                        minecraft, sample.position().x, sample.position().y, sample.position().z, 0.0D, 0.0D, 0.0D);
                if (spark == null) {
                    discard(staged);
                    return false;
                }
                configureSpark(spark, sample);
                staged.add(new TrackedSpark(spark, branch, terminal, segment, sample.retirementTick()));
            }
        }
        this.trackedSparks.addAll(staged);
        this.createdParticles += staged.size();
        return staged.size() == CrownDescentTrajectory.INITIAL_BLOOM_PARTICLE_COUNT;
    }

    private static void configureSpark(Particle spark, Sample sample) {
        CrownDescentTrajectory.Rgb color = sample.color();
        FireworkParticleAppearance.applyVividColor(
                spark, color.red(), color.green(), color.blue(), sample.brightness(),
                FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
        FireworkParticleAppearance.applyVisibilityScale(spark, sample.scale());
        spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
        spark.setLifetime(CrownDescentTrajectory.VISUAL_LIFETIME_TICKS + 2);
    }

    /** Moves only the locally-owned initial sparks; no later tick creates another particle allocation. */
    private void updateTrackedSparks() {
        Iterator<TrackedSpark> iterator = this.trackedSparks.iterator();
        while (iterator.hasNext()) {
            TrackedSpark tracked = iterator.next();
            Particle spark = tracked.spark();
            if (!spark.isAlive() || this.age >= tracked.retirementTick()) {
                if (spark.isAlive()) {
                    spark.remove();
                }
                iterator.remove();
                continue;
            }

            Sample sample = this.request.trajectory().sample(
                    this.request.x(), this.request.y(), this.request.z(), tracked.branch(), tracked.terminal(),
                    this.age, tracked.trailSegment());
            spark.setPos(sample.position().x, sample.position().y, sample.position().z);
            spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
            int remaining = Math.max(1, tracked.retirementTick() - this.age + 1);
            spark.setLifetime(spark.age + remaining);
            if (this.age >= tracked.retirementTick() - RETIREMENT_TWINKLE_LEAD_TICKS
                    && spark instanceof FireworkParticles.SparkParticle sparkParticle) {
                sparkParticle.setTwinkle(true);
            }
        }
    }

    private void discardTrackedSparks() {
        discard(this.trackedSparks);
        this.trackedSparks.clear();
    }

    private static void discard(List<TrackedSpark> sparks) {
        for (TrackedSpark tracked : sparks) {
            if (tracked.spark().isAlive()) {
                tracked.spark().remove();
            }
        }
    }

    private record TrackedSpark(
            Particle spark, Branch branch, Terminal terminal, int trailSegment, int retirementTick) {
        private TrackedSpark {
            Objects.requireNonNull(spark, "spark");
            Objects.requireNonNull(branch, "branch");
            Objects.requireNonNull(terminal, "terminal");
        }
    }

    private enum ProgramState {
        PENDING,
        ACTIVE,
        FAILED_CLOSED,
        COMPLETE
    }

    public enum GroundResolution {
        PENDING,
        RESOLVED,
        FAILED_CLOSED
    }

    /** Typed handoff from the shared payload/style scheduler into this client-only program. */
    public record Request(double x, double y, double z, CrownDescentTrajectory trajectory) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Crown descent request position must be finite");
            }
            Objects.requireNonNull(trajectory, "trajectory");
        }

        public static Request fromStyle(double x, double y, double z, FireworkStyle style, long seed) {
            return new Request(x, y, z, CrownDescentTrajectory.fromStyle(style, seed));
        }
    }
}
