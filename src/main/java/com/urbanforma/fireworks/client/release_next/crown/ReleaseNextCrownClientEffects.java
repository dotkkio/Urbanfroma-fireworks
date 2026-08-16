package com.urbanforma.fireworks.client.release_next.crown;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.release_next.crown.CrownReplacementManifest;
import com.urbanforma.fireworks.content.release_next.crown.CrownReplacementProgram;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Isolated client implementation for the release-next crown replacement.
 *
 * <p>The future shared scheduler owns routing and lifecycle hooks. Once routed, this type performs all geometry,
 * height lookup, particle creation, motion and retirement locally. It sends no packets and never runs on a server.</p>
 */
public final class ReleaseNextCrownClientEffects {
    private static final List<Program> ACTIVE = new ArrayList<>(CrownReplacementManifest.MAX_ACTIVE_PROGRAMS);

    private ReleaseNextCrownClientEffects() {
    }

    public static boolean supports(FireworkStyle style) {
        return CrownReplacementManifest.supports(style);
    }

    /** Called once by the shared client scheduler at the burst event; this is the only entry point that allocates. */
    public static boolean enqueue(double x, double y, double z, FireworkStyle style, long seed) {
        if (!CrownReplacementManifest.supports(style)
                || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || ACTIVE.size() >= CrownReplacementManifest.MAX_ACTIVE_PROGRAMS) {
            return false;
        }
        ACTIVE.add(new Program(x, y, z, new CrownReplacementProgram(seed, CrownReplacementManifest.paletteFrom(style))));
        return true;
    }

    /** Called from the existing client post-tick hook by the integrator. No server-side caller is valid. */
    public static void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        Iterator<Program> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick(minecraft)) {
                iterator.remove();
            }
        }
    }

    /** Called by the existing level-change/logout lifecycle hook. */
    public static void clear() {
        for (Program program : ACTIVE) {
            program.retire();
        }
        ACTIVE.clear();
    }

    public static int activeProgramCount() {
        return ACTIVE.size();
    }

    private static final class Program {
        private final double originX;
        private final double originY;
        private final double originZ;
        private final CrownReplacementProgram trajectory;
        private final List<TrackedParticle> particles = new ArrayList<>(CrownReplacementManifest.PARTICLES_PER_PROGRAM);
        private boolean exploded;
        private int age;

        private Program(double originX, double originY, double originZ, CrownReplacementProgram trajectory) {
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.trajectory = trajectory;
        }

        private boolean tick(Minecraft minecraft) {
            ClientLevel level = minecraft.level;
            if (level == null) {
                retire();
                return true;
            }
            if (!exploded) {
                explode(minecraft, level);
                exploded = true;
                // A partial bloom is intentional when a distant terrain column is unavailable; never fail the
                // complete program because one branch cannot satisfy its ground-clearance contract.
                if (particles.isEmpty()) {
                    return true;
                }
            }
            updateExistingParticles();
            age++;
            if (age >= CrownReplacementManifest.VISUAL_LIFETIME_TICKS) {
                retire();
                return true;
            }
            return false;
        }

        private void explode(Minecraft minecraft, ClientLevel level) {
            for (int branchIndex = 0; branchIndex < CrownReplacementManifest.BRANCH_COUNT; branchIndex++) {
                CrownReplacementProgram.Branch branch = trajectory.branch(branchIndex);
                CrownReplacementProgram.Terminal terminal = resolveSafeTerminal(level, branch);
                if (terminal == null) {
                    continue;
                }
                for (int particleIndex = 0; particleIndex < CrownReplacementManifest.PARTICLES_PER_BRANCH;
                        particleIndex++) {
                    CrownReplacementProgram.Sample sample = trajectory.sample(
                            originX, originY, originZ, branch, terminal, 0, particleIndex);
                    // Deliberately direct: this proves the explosion creates a real client particle, rather than
                    // merely enqueueing a geometry record or waiting for a server particle packet.
                    Particle particle = minecraft.particleEngine.createParticle(
                            ParticleTypes.FIREWORK,
                            sample.position().x,
                            sample.position().y,
                            sample.position().z,
                            sample.velocity().x,
                            sample.velocity().y,
                            sample.velocity().z);
                    if (particle == null) {
                        continue;
                    }
                    configure(particle, sample);
                    particles.add(new TrackedParticle(particle, branch, terminal, particleIndex));
                }
            }
        }

        private CrownReplacementProgram.Terminal resolveSafeTerminal(
                ClientLevel level, CrownReplacementProgram.Branch branch) {
            double terminalX = originX + Math.cos(branch.angle()) * branch.terminalRadius();
            double terminalZ = originZ + Math.sin(branch.angle()) * branch.terminalRadius();
            int blockX = (int) Math.floor(terminalX);
            int blockZ = (int) Math.floor(terminalZ);
            if (!level.hasChunk(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16))) {
                return null;
            }
            int terrain = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
            double terminalY = terrain + CrownReplacementManifest.GROUND_CLEARANCE_BLOCKS;
            if (terrain <= level.getMinBuildHeight() || terminalY >= level.getMaxBuildHeight()
                    || originY + branch.initialHeight() - terminalY < 18.0D) {
                return null;
            }
            return new CrownReplacementProgram.Terminal(terminalX, terminalY, terminalZ);
        }

        private void updateExistingParticles() {
            Iterator<TrackedParticle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                TrackedParticle tracked = iterator.next();
                Particle particle = tracked.particle();
                if (!particle.isAlive()) {
                    iterator.remove();
                    continue;
                }
                CrownReplacementProgram.Sample sample = trajectory.sample(
                        originX, originY, originZ, tracked.branch(), tracked.terminal(), age, tracked.particleIndex());
                particle.setPos(sample.position().x, sample.position().y, sample.position().z);
                particle.setParticleSpeed(sample.velocity().x, sample.velocity().y, sample.velocity().z);
                particle.setLifetime(particle.age + Math.max(2, CrownReplacementManifest.VISUAL_LIFETIME_TICKS - age));
            }
        }

        private static void configure(Particle particle, CrownReplacementProgram.Sample sample) {
            particle.setColor(sample.color().red(), sample.color().green(), sample.color().blue());
            if (particle instanceof SimpleAnimatedParticle animatedParticle) {
                // NeoForge 1.21.1 exposes this native fade-color API, while Particle#setAlpha is protected.
                // The FIREWORK provider creates SparkParticle, a SimpleAnimatedParticle, so its native fade is
                // deterministic and does not require reflection, a second particle type, or new allocations.
                animatedParticle.setFadeColor(0x000000);
            }
            particle.setParticleSpeed(sample.velocity().x, sample.velocity().y, sample.velocity().z);
            particle.setLifetime(CrownReplacementManifest.VISUAL_LIFETIME_TICKS + 2);
            particle.scale(sample.scale());
        }

        private void retire() {
            for (TrackedParticle tracked : particles) {
                if (tracked.particle().isAlive()) {
                    tracked.particle().remove();
                }
            }
            particles.clear();
        }
    }

    private record TrackedParticle(
            Particle particle,
            CrownReplacementProgram.Branch branch,
            CrownReplacementProgram.Terminal terminal,
            int particleIndex) {
    }
}
