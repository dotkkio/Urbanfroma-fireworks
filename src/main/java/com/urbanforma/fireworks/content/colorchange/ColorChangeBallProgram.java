package com.urbanforma.fireworks.content.colorchange;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.NormalFireworkCatalog;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable, fail-closed appearance data for the established color-shift fireworks.
 *
 * <p>This common-side class has no particle, scheduler, payload, or server ownership. The client adapter owns the
 * only particle mutation and only accepts the contracts declared here.</p>
 */
public final class ColorChangeBallProgram {
    public static final int TICKS_PER_SECOND = 20;
    public static final int MIN_SWITCH_DELAY_TICKS = 6;
    public static final int MAX_SWITCH_DELAY_TICKS = 14;
    public static final double MIN_SWITCH_DELAY_SECONDS = 0.3D;
    public static final double MAX_SWITCH_DELAY_SECONDS = 0.7D;

    /** One visible blend frame separates the initial and exact target RGB states. */
    public static final int VISUAL_TRANSITION_TICKS = 1;
    public static final float MIN_LAYER_COLOR_DISTANCE = 0.20F;
    public static final float MIN_LEAD_COLOR_DISTANCE = 0.45F;

    public static final String CINNABAR_AMBER_SPHERE_ID = "cinnabar_amber_sphere";
    public static final String EMBER_TWILIGHT_RADIANT_ID = "batch05_ember_twilight_radiant";
    public static final String SUNSET_ORCHID_WILLOW_ID = "batch05_sunset_orchid_willow";
    public static final String AURORA_PEARL_HYBRID_ID = "batch05_aurora_pearl_hybrid";

    private static final long LAYER_SEED_STEP = 0x9E3779B97F4A7C15L;
    private static final List<EffectContract> EXISTING_CONTRACTS = List.of(
            new EffectContract(CINNABAR_AMBER_SPHERE_ID, EffectPath.ORDINARY_SPHERE, 1_600, 12, 56),
            new EffectContract(EMBER_TWILIGHT_RADIANT_ID, EffectPath.RADIANT, 4_800, 30, 36),
            new EffectContract(SUNSET_ORCHID_WILLOW_ID, EffectPath.RADIANT_WILLOW, 4_800, 30, 36),
            new EffectContract(AURORA_PEARL_HYBRID_ID, EffectPath.HYBRID_SPHERE_RADIANT, 4_080, 12, 36));

    private ColorChangeBallProgram() {
    }

    /** Legacy two-state semantic retained for callers that only need the selected palette. */
    public enum Phase {
        INITIAL,
        TARGET
    }

    /** The client-visible sequence: existing initial RGB, one blended frame, then exact target RGB. */
    public enum AppearancePhase {
        INITIAL,
        TRANSITION,
        TARGET
    }

    public enum Layer {
        PRIMARY,
        SECONDARY,
        ACCENT
    }

    /** Existing effect paths that need explicit client-side particle registration. */
    public enum EffectPath {
        ORDINARY_SPHERE,
        RADIANT,
        RADIANT_WILLOW,
        HYBRID_SPHERE_RADIANT
    }

    /**
     * A finite local client budget. It is the exact maximum number of pre-existing particles that an effect may
     * register for recoloring; it is not a shared particle quota.
     */
    public record EffectContract(
            String stableId,
            EffectPath effectPath,
            int localParticleBudget,
            int emissionWindowTicks,
            int minimumParticleLifetimeTicks) {
        public EffectContract {
            if (stableId == null || stableId.isBlank()) {
                throw new IllegalArgumentException("Color-change contract id must not be blank");
            }
            Objects.requireNonNull(effectPath, "effectPath");
            if (localParticleBudget <= 0 || emissionWindowTicks <= 0) {
                throw new IllegalArgumentException("Color-change contract requires a positive local budget and window");
            }
            if (minimumParticleLifetimeTicks <= MAX_SWITCH_DELAY_TICKS + VISUAL_TRANSITION_TICKS) {
                throw new IllegalArgumentException("Color-change target must fit inside every declared particle lifetime");
            }
        }
    }

    /** A resolved contract is the only successful entry point for client particle tracking. */
    public record ResolvedContract(EffectContract contract, Profile profile) {
        public ResolvedContract {
            Objects.requireNonNull(contract, "contract");
            Objects.requireNonNull(profile, "profile");
            if (!profile.canReachTarget(contract.minimumParticleLifetimeTicks())) {
                throw new IllegalArgumentException("Color-change target does not fit the contract lifetime");
            }
        }
    }

    /** The approved three-layer RGB contract, grouped without creating a new style or item. */
    public record Palette(
            FireworkStyle.Rgb primaryColor,
            FireworkStyle.Rgb secondaryColor,
            FireworkStyle.Rgb accentColor) {
        public Palette {
            Objects.requireNonNull(primaryColor, "primaryColor");
            Objects.requireNonNull(secondaryColor, "secondaryColor");
            Objects.requireNonNull(accentColor, "accentColor");
            validateFiniteColor(primaryColor, "primaryColor");
            validateFiniteColor(secondaryColor, "secondaryColor");
            validateFiniteColor(accentColor, "accentColor");
        }

        public FireworkStyle.Rgb colorFor(Layer layer) {
            return switch (Objects.requireNonNull(layer, "layer")) {
                case PRIMARY -> this.primaryColor;
                case SECONDARY -> this.secondaryColor;
                case ACCENT -> this.accentColor;
            };
        }

        private boolean matchesStyle(FireworkStyle style) {
            return this.primaryColor.equals(style.primaryColor())
                    && this.secondaryColor.equals(style.secondaryColor())
                    && this.accentColor.equals(style.accentColor());
        }
    }

    /** Runtime-only color data. A profile is rejected unless each layer has a real visible target shift. */
    public record Profile(Palette initialPalette, Palette targetPalette, int switchDelayTicks) {
        public Profile {
            Objects.requireNonNull(initialPalette, "initialPalette");
            Objects.requireNonNull(targetPalette, "targetPalette");
            validateSwitchDelayTicks(switchDelayTicks);
            validateVisibleShift(initialPalette, targetPalette);
        }

        public static Profile fromSeconds(
                Palette initialPalette, Palette targetPalette, double switchDelaySeconds) {
            return new Profile(
                    initialPalette,
                    targetPalette,
                    switchDelayTicksFromSeconds(switchDelaySeconds));
        }

        public Phase phaseAt(int ageTicks) {
            validateAgeTicks(ageTicks);
            return ageTicks < this.switchDelayTicks ? Phase.INITIAL : Phase.TARGET;
        }

        public Palette paletteAt(int ageTicks) {
            return this.phaseAt(ageTicks) == Phase.INITIAL
                    ? this.initialPalette
                    : this.targetPalette;
        }

        public FireworkStyle.Rgb colorAt(Layer layer, int ageTicks) {
            return this.paletteAt(ageTicks).colorFor(layer);
        }

        public boolean hasSwitchedAt(int ageTicks) {
            return this.phaseAt(ageTicks) == Phase.TARGET;
        }

        /** The first tick where the client writes the exact target RGB after the blend frame. */
        public int targetAppearanceTick() {
            return this.switchDelayTicks + VISUAL_TRANSITION_TICKS;
        }

        public boolean canReachTarget(int declaredLifetimeTicks) {
            return declaredLifetimeTicks > this.targetAppearanceTick();
        }

        public AppearanceSample appearanceAt(Layer layer, int particleAgeTicks) {
            validateAgeTicks(particleAgeTicks);
            Objects.requireNonNull(layer, "layer");
            if (particleAgeTicks < this.switchDelayTicks) {
                return new AppearanceSample(
                        AppearancePhase.INITIAL, layer, this.initialPalette.colorFor(layer));
            }
            if (particleAgeTicks < this.targetAppearanceTick()) {
                return new AppearanceSample(
                        AppearancePhase.TRANSITION,
                        layer,
                        mix(this.initialPalette.colorFor(layer), this.targetPalette.colorFor(layer), 0.5F));
            }
            return new AppearanceSample(AppearancePhase.TARGET, layer, this.targetPalette.colorFor(layer));
        }

        private boolean matchesStyle(FireworkStyle style) {
            return this.initialPalette.matchesStyle(style);
        }
    }

    /** A deterministic palette sample for callers that retain the legacy two-state query. */
    public record Sample(Phase phase, Layer layer, FireworkStyle.Rgb color) {
        public Sample {
            Objects.requireNonNull(phase, "phase");
            Objects.requireNonNull(layer, "layer");
            Objects.requireNonNull(color, "color");
        }
    }

    /** A client-visible RGB state. The adapter writes this exact color to the existing particle. */
    public record AppearanceSample(AppearancePhase phase, Layer layer, FireworkStyle.Rgb color) {
        public AppearanceSample {
            Objects.requireNonNull(phase, "phase");
            Objects.requireNonNull(layer, "layer");
            Objects.requireNonNull(color, "color");
        }
    }

    public static Sample sample(Profile profile, Layer layer, int ageTicks) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(layer, "layer");
        return new Sample(profile.phaseAt(ageTicks), layer, profile.colorAt(layer, ageTicks));
    }

    public static AppearanceSample appearanceSample(Profile profile, Layer layer, int particleAgeTicks) {
        Objects.requireNonNull(profile, "profile");
        return profile.appearanceAt(layer, particleAgeTicks);
    }

    /** Selects an existing layer from a stable seed without changing palette or switch timing. */
    public static Sample sample(Profile profile, long seed, int particleIndex, int ageTicks) {
        return sample(profile, layerFor(seed, particleIndex), ageTicks);
    }

    /** Returns all supported current IDs in integration order; unknown and future IDs remain disabled. */
    public static List<String> supportedStyleIds() {
        return EXISTING_CONTRACTS.stream().map(EffectContract::stableId).toList();
    }

    /** Returns the finite client contract for a currently supported style, or empty for every unknown entry. */
    public static Optional<ResolvedContract> contractFor(FireworkStyle style) {
        Objects.requireNonNull(style, "style");
        EffectContract contract = findContract(style.id());
        if (contract == null) {
            return Optional.empty();
        }

        Profile profile = profileForKnownContract(style, contract);
        if (profile == null
                || !profile.matchesStyle(style)
                || !profile.canReachTarget(contract.minimumParticleLifetimeTicks())) {
            return Optional.empty();
        }
        return Optional.of(new ResolvedContract(contract, profile));
    }

    /**
     * Compatibility lookup for the existing ordinary burst code. New catalog entries are deliberately not inferred
     * from a non-null profile: they must first be added to {@link #EXISTING_CONTRACTS} with a reviewed budget.
     */
    public static Profile profileFor(FireworkStyle style) {
        return contractFor(style).map(ResolvedContract::profile).orElse(null);
    }

    public static Layer layerFor(long seed, int particleIndex) {
        if (particleIndex < 0) {
            throw new IllegalArgumentException("particleIndex must be non-negative");
        }
        long mixed = mix64(seed + LAYER_SEED_STEP * (particleIndex + 1L));
        int layerIndex = (int) Math.floorMod(mixed, (long) Layer.values().length);
        return Layer.values()[layerIndex];
    }

    /** Converts an inclusive 0.3..0.7 second interval into an exact 6..14 tick value. */
    public static int switchDelayTicksFromSeconds(double switchDelaySeconds) {
        if (!Double.isFinite(switchDelaySeconds)
                || switchDelaySeconds < MIN_SWITCH_DELAY_SECONDS
                || switchDelaySeconds > MAX_SWITCH_DELAY_SECONDS) {
            throw new IllegalArgumentException("Switch delay must be between 0.3 and 0.7 seconds");
        }
        int ticks = (int) Math.round(switchDelaySeconds * TICKS_PER_SECOND);
        validateSwitchDelayTicks(ticks);
        return ticks;
    }

    public static double switchDelaySecondsFromTicks(int switchDelayTicks) {
        validateSwitchDelayTicks(switchDelayTicks);
        return (double) switchDelayTicks / TICKS_PER_SECOND;
    }

    public static void validateSwitchDelayTicks(int switchDelayTicks) {
        if (switchDelayTicks < MIN_SWITCH_DELAY_TICKS
                || switchDelayTicks > MAX_SWITCH_DELAY_TICKS) {
            throw new IllegalArgumentException("Switch delay must be between 6 and 14 ticks");
        }
    }

    private static EffectContract findContract(String styleId) {
        for (EffectContract contract : EXISTING_CONTRACTS) {
            if (contract.stableId().equals(styleId)) {
                return contract;
            }
        }
        return null;
    }

    private static Profile profileForKnownContract(FireworkStyle style, EffectContract contract) {
        if (contract.stableId().equals(CINNABAR_AMBER_SPHERE_ID)) {
            return new Profile(
                    new Palette(style.primaryColor(), style.secondaryColor(), style.accentColor()),
                    new Palette(
                            FireworkStyle.Rgb.fromHex("#2D6BFF"),
                            FireworkStyle.Rgb.fromHex("#7D42F5"),
                            FireworkStyle.Rgb.fromHex("#FF38D2")),
                    10);
        }
        return NormalFireworkCatalog.colorChangeProfileFor(style.id());
    }

    private static void validateVisibleShift(Palette initialPalette, Palette targetPalette) {
        float primaryDistance = rgbDistance(initialPalette.primaryColor(), targetPalette.primaryColor());
        float secondaryDistance = rgbDistance(initialPalette.secondaryColor(), targetPalette.secondaryColor());
        float accentDistance = rgbDistance(initialPalette.accentColor(), targetPalette.accentColor());
        if (primaryDistance < MIN_LEAD_COLOR_DISTANCE
                || secondaryDistance < MIN_LEAD_COLOR_DISTANCE
                || accentDistance < MIN_LAYER_COLOR_DISTANCE) {
            throw new IllegalArgumentException("Color-change profile must have a visibly distinct target RGB palette");
        }
    }

    private static float rgbDistance(FireworkStyle.Rgb first, FireworkStyle.Rgb second) {
        float red = first.red() - second.red();
        float green = first.green() - second.green();
        float blue = first.blue() - second.blue();
        return (float) Math.sqrt(red * red + green * green + blue * blue);
    }

    private static FireworkStyle.Rgb mix(FireworkStyle.Rgb initial, FireworkStyle.Rgb target, float progress) {
        return new FireworkStyle.Rgb(
                initial.red() + (target.red() - initial.red()) * progress,
                initial.green() + (target.green() - initial.green()) * progress,
                initial.blue() + (target.blue() - initial.blue()) * progress);
    }

    private static void validateFiniteColor(FireworkStyle.Rgb color, String name) {
        if (!Float.isFinite(color.red()) || !Float.isFinite(color.green()) || !Float.isFinite(color.blue())) {
            throw new IllegalArgumentException(name + " channels must be finite");
        }
    }

    private static void validateAgeTicks(int ageTicks) {
        if (ageTicks < 0) {
            throw new IllegalArgumentException("ageTicks must be non-negative");
        }
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
