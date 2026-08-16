package com.urbanforma.fireworks.client.colorchange;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.colorchange.ColorChangeBallProgram;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.particle.Particle;

/**
 * Client-only appearance bridge for color-shift fireworks.
 *
 * <p>It never creates particles. A caller registers an already-created {@link Particle}, and this adapter later
 * writes a blended RGB once and the exact target RGB once to that same live particle.</p>
 */
public final class ColorChangeBallParticleAdapter {
    private static final List<AppearanceSession> ACTIVE_SESSIONS = new ArrayList<>();

    private ColorChangeBallParticleAdapter() {
    }

    public static ColorChangeBallProgram.Sample apply(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            ColorChangeBallProgram.Layer layer,
            int ageTicks) {
        return apply(particle, profile, layer, ageTicks, false);
    }

    /**
     * Compatibility mutation for the existing ordinary-sphere tracker. The color is applied directly so the final
     * target channels are not hidden by an unrelated brightness or white-lift transform.
     */
    public static ColorChangeBallProgram.Sample apply(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            ColorChangeBallProgram.Layer layer,
            int ageTicks,
            boolean coreHighlight) {
        ColorChangeBallProgram.AppearanceSample appearance =
                applyAppearance(particle, profile, layer, ageTicks, coreHighlight);
        return new ColorChangeBallProgram.Sample(profile.phaseAt(ageTicks), layer, appearance.color());
    }

    public static ColorChangeBallProgram.Sample apply(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            long seed,
            int particleIndex,
            int ageTicks) {
        ColorChangeBallProgram.Layer layer = ColorChangeBallProgram.layerFor(seed, particleIndex);
        return apply(particle, profile, layer, ageTicks);
    }

    /** Applies one explicit appearance sample to one existing particle. */
    public static ColorChangeBallProgram.AppearanceSample applyAppearance(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            ColorChangeBallProgram.Layer layer,
            int particleAgeTicks,
            boolean coreHighlight) {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(profile, "profile");
        ColorChangeBallProgram.AppearanceSample appearance =
                ColorChangeBallProgram.appearanceSample(profile, layer, particleAgeTicks);
        applyColor(particle, appearance.color());
        return appearance;
    }

    /** Writes exact normalized RGB channels to an already-created client particle. */
    public static void applyColor(Particle particle, FireworkStyle.Rgb color) {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(color, "color");
        particle.setColor(color.red(), color.green(), color.blue());
    }

    /**
     * Opens a finite local session for a known contract. Unknown or changed future IDs return empty and cannot
     * accidentally gain a color-change effect from a catalog entry alone.
     */
    public static Optional<AppearanceSession> beginSession(FireworkStyle style) {
        Optional<ColorChangeBallProgram.ResolvedContract> resolved = ColorChangeBallProgram.contractFor(style);
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        AppearanceSession session = new AppearanceSession(resolved.get());
        ACTIVE_SESSIONS.add(session);
        return Optional.of(session);
    }

    /** Called once from the physical-client tick after the base effects have advanced. */
    public static void tickAllSessions() {
        Iterator<AppearanceSession> iterator = ACTIVE_SESSIONS.iterator();
        while (iterator.hasNext()) {
            AppearanceSession session = iterator.next();
            session.tick();
            if (session.finished()) {
                iterator.remove();
            }
        }
    }

    /** Clears client references on logout or level replacement; no server state exists to synchronize. */
    public static void clearSessions() {
        ACTIVE_SESSIONS.clear();
    }

    /** A finite session owned by one burst and bounded by that burst's known base-program allocation. */
    public static final class AppearanceSession {
        private final ColorChangeBallProgram.ResolvedContract resolvedContract;
        private final List<TrackedParticle> trackedParticles = new ArrayList<>();
        private int ageTicks;
        private int admittedParticleCount;
        private boolean sealed;
        private boolean expired;

        private AppearanceSession(ColorChangeBallProgram.ResolvedContract resolvedContract) {
            this.resolvedContract = Objects.requireNonNull(resolvedContract, "resolvedContract");
        }

        public ColorChangeBallProgram.EffectContract contract() {
            return this.resolvedContract.contract();
        }

        public ColorChangeBallProgram.Profile profile() {
            return this.resolvedContract.profile();
        }

        public int ageTicks() {
            return this.ageTicks;
        }

        public int admittedParticleCount() {
            return this.admittedParticleCount;
        }

        public int trackedParticleCount() {
            return this.trackedParticles.size();
        }

        /**
         * Registers one existing particle and its actual declared lifetime. The session does not recolor it yet;
         * its existing base-effect initial appearance remains intact until the explicit transition tick.
         */
        public TrackResult trackExisting(
                Particle particle,
                ColorChangeBallProgram.Layer layer,
                int declaredLifetimeTicks) {
            Objects.requireNonNull(particle, "particle");
            Objects.requireNonNull(layer, "layer");
            if (this.sealed || this.expired) {
                return TrackResult.SESSION_CLOSED;
            }
            if (this.admittedParticleCount >= this.contract().localParticleBudget()) {
                return TrackResult.LOCAL_BUDGET_EXHAUSTED;
            }
            if (!this.profile().canReachTarget(declaredLifetimeTicks)) {
                return TrackResult.TARGET_OUTSIDE_LIFETIME;
            }
            this.trackedParticles.add(new TrackedParticle(particle, layer, this.ageTicks));
            this.admittedParticleCount++;
            return TrackResult.TRACKED;
        }

        /** Compatibility overload for emitters that already distinguish core particles. */
        public TrackResult trackExisting(
                Particle particle,
                ColorChangeBallProgram.Layer layer,
                int declaredLifetimeTicks,
                boolean coreHighlight) {
            return trackExisting(particle, layer, declaredLifetimeTicks);
        }

        /** Marks that the base effect has emitted its final pre-existing particle. */
        public void seal() {
            this.sealed = true;
        }

        public boolean finished() {
            return this.expired || this.sealed && this.trackedParticles.isEmpty();
        }

        private void tick() {
            if (this.finished()) {
                return;
            }
            this.ageTicks++;
            Iterator<TrackedParticle> iterator = this.trackedParticles.iterator();
            while (iterator.hasNext()) {
                TrackedParticle tracked = iterator.next();
                if (!tracked.particle.isAlive()) {
                    iterator.remove();
                    continue;
                }

                int particleAgeTicks = this.ageTicks - tracked.createdAtSessionTick;
                ColorChangeBallProgram.AppearanceSample appearance =
                        this.profile().appearanceAt(tracked.layer, particleAgeTicks);
                if (appearance.phase() != tracked.lastAppliedPhase) {
                    applyColor(tracked.particle, appearance.color());
                    tracked.lastAppliedPhase = appearance.phase();
                }
                if (appearance.phase() == ColorChangeBallProgram.AppearancePhase.TARGET) {
                    iterator.remove();
                }
            }

            int lastRequiredTick = this.contract().emissionWindowTicks() + this.profile().targetAppearanceTick();
            if (this.ageTicks > lastRequiredTick) {
                this.trackedParticles.clear();
                this.expired = true;
            }
        }
    }

    public enum TrackResult {
        TRACKED,
        SESSION_CLOSED,
        LOCAL_BUDGET_EXHAUSTED,
        TARGET_OUTSIDE_LIFETIME
    }

    private static final class TrackedParticle {
        private final Particle particle;
        private final ColorChangeBallProgram.Layer layer;
        private final int createdAtSessionTick;
        private ColorChangeBallProgram.AppearancePhase lastAppliedPhase =
                ColorChangeBallProgram.AppearancePhase.INITIAL;

        private TrackedParticle(
                Particle particle, ColorChangeBallProgram.Layer layer, int createdAtSessionTick) {
            this.particle = particle;
            this.layer = layer;
            this.createdAtSessionTick = createdAtSessionTick;
        }
    }
}
