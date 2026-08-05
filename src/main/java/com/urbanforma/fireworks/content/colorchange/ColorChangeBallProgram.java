package com.urbanforma.fireworks.content.colorchange;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.NormalFireworkCatalog;
import java.util.Objects;

/**
 * Deterministic, one-shot color state for a ball made from an existing firework style.
 *
 * <p>The program keeps the initial palette unchanged until the switch tick, then returns the target palette
 * unchanged. It intentionally has no particle, item, registry, or scheduler ownership.</p>
 */
public final class ColorChangeBallProgram {
    public static final int TICKS_PER_SECOND = 20;
    public static final int MIN_SWITCH_DELAY_TICKS = 6;
    public static final int MAX_SWITCH_DELAY_TICKS = 14;
    public static final double MIN_SWITCH_DELAY_SECONDS = 0.3D;
    public static final double MAX_SWITCH_DELAY_SECONDS = 0.7D;

    private static final long LAYER_SEED_STEP = 0x9E3779B97F4A7C15L;

    private ColorChangeBallProgram() {
    }

    public enum Phase {
        INITIAL,
        TARGET
    }

    public enum Layer {
        PRIMARY,
        SECONDARY,
        ACCENT
    }

    /** The existing three-layer RGB contract, grouped without creating a new style or item. */
    public record Palette(
            FireworkStyle.Rgb primaryColor,
            FireworkStyle.Rgb secondaryColor,
            FireworkStyle.Rgb accentColor) {
        public Palette {
            Objects.requireNonNull(primaryColor, "primaryColor");
            Objects.requireNonNull(secondaryColor, "secondaryColor");
            Objects.requireNonNull(accentColor, "accentColor");
        }

        public FireworkStyle.Rgb colorFor(Layer layer) {
            return switch (Objects.requireNonNull(layer, "layer")) {
                case PRIMARY -> this.primaryColor;
                case SECONDARY -> this.secondaryColor;
                case ACCENT -> this.accentColor;
            };
        }
    }

    /** Runtime-only color-change data; it does not consume a registry or new-series item slot. */
    public record Profile(Palette initialPalette, Palette targetPalette, int switchDelayTicks) {
        public Profile {
            Objects.requireNonNull(initialPalette, "initialPalette");
            Objects.requireNonNull(targetPalette, "targetPalette");
            validateSwitchDelayTicks(switchDelayTicks);
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
    }

    /** A deterministic sample suitable for applying to one already-created particle. */
    public record Sample(Phase phase, Layer layer, FireworkStyle.Rgb color) {
        public Sample {
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

    /**
     * Selects one of the existing three layers from a stable seed and particle index, then applies the same
     * discrete color state as the layer-aware overload. The seed changes neither palette nor switch timing.
     */
    public static Sample sample(Profile profile, long seed, int particleIndex, int ageTicks) {
        return sample(profile, layerFor(seed, particleIndex), ageTicks);
    }

    /**
     * First integrated capability profile. It augments the existing red/orange sphere entry without adding a new
     * series item: warm initial layers transition discretely to blue/purple/magenta after seven ticks.
     */
    public static Profile profileFor(FireworkStyle style) {
        Objects.requireNonNull(style, "style");
        Profile integratedProfile = NormalFireworkCatalog.colorChangeProfileFor(style.id());
        if (integratedProfile != null) {
            return integratedProfile;
        }
        if (style != FireworkStyle.CINNABAR_AMBER_SPHERE) {
            return null;
        }
        return new Profile(
                new Palette(
                        style.primaryColor(),
                        style.secondaryColor(),
                        style.accentColor()),
                new Palette(
                        FireworkStyle.Rgb.fromHex("#2D6BFF"),
                        FireworkStyle.Rgb.fromHex("#7D42F5"),
                        FireworkStyle.Rgb.fromHex("#FF38D2")),
                10);
    }

    public static Layer layerFor(long seed, int particleIndex) {
        if (particleIndex < 0) {
            throw new IllegalArgumentException("particleIndex must be non-negative");
        }
        long mixed = mix64(seed + LAYER_SEED_STEP * (particleIndex + 1L));
        int layerIndex = (int) Math.floorMod(mixed, (long) Layer.values().length);
        return Layer.values()[layerIndex];
    }

    /** Converts an inclusive 0.3..0.7 second interval into an exact 6..14 tick discrete value. */
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
