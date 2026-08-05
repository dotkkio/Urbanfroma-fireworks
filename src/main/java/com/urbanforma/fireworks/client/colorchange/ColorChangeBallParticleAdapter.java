package com.urbanforma.fireworks.client.colorchange;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.colorchange.ColorChangeBallProgram;
import java.util.Objects;
import net.minecraft.client.particle.Particle;

/**
 * Applies a color-change sample to a particle that the shared client effect already created.
 *
 * <p>This adapter only mutates the existing particle color. Particle creation, scheduling, and ownership stay
 * with the shared client effect, and the effect continues to use the vanilla FIREWORK particle type.</p>
 */
public final class ColorChangeBallParticleAdapter {
    private ColorChangeBallParticleAdapter() {
    }

    public static ColorChangeBallProgram.Sample apply(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            ColorChangeBallProgram.Layer layer,
            int ageTicks) {
        return apply(particle, profile, layer, ageTicks, false);
    }

    public static ColorChangeBallProgram.Sample apply(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            ColorChangeBallProgram.Layer layer,
            int ageTicks,
            boolean coreHighlight) {
        Objects.requireNonNull(particle, "particle");
        ColorChangeBallProgram.Sample sample =
                ColorChangeBallProgram.sample(profile, layer, ageTicks);
        applyColor(particle, sample.color(), coreHighlight);
        return sample;
    }

    public static ColorChangeBallProgram.Sample apply(
            Particle particle,
            ColorChangeBallProgram.Profile profile,
            long seed,
            int particleIndex,
            int ageTicks) {
        Objects.requireNonNull(particle, "particle");
        ColorChangeBallProgram.Sample sample =
                ColorChangeBallProgram.sample(profile, seed, particleIndex, ageTicks);
        applyColor(particle, sample.color());
        return sample;
    }

    public static void applyColor(Particle particle, FireworkStyle.Rgb color) {
        applyColor(particle, color, false);
    }

    public static void applyColor(
            Particle particle, FireworkStyle.Rgb color, boolean coreHighlight) {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(color, "color");
        if (coreHighlight) {
            FireworkParticleAppearance.applyCoreColor(
                    particle, color.red(), color.green(), color.blue());
        } else {
            FireworkParticleAppearance.applyVividColor(
                    particle,
                    color.red(),
                    color.green(),
                    color.blue(),
                    1.04F,
                    FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
        }
    }
}
