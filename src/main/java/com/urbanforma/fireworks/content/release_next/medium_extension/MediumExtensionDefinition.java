package com.urbanforma.fireworks.content.release_next.medium_extension;

import java.util.Locale;
import java.util.Objects;

/** Isolated, append-only contract for the release-next medium extension. */
public record MediumExtensionDefinition(
        String id,
        Category category,
        String primary,
        String secondary,
        String accent,
        int totalParticles,
        int particlesPerTick,
        int emissionTicks,
        int minimumLifetime,
        int maximumLifetime,
        double maximumRadius,
        int cadenceBeats) {
    public MediumExtensionDefinition {
        if (id == null || !id.matches("medium_extension_[a-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid medium-extension id: " + id);
        }
        Objects.requireNonNull(category, "category");
        validateColor(primary); validateColor(secondary); validateColor(accent);
        if (totalParticles <= 0 || particlesPerTick <= 0 || emissionTicks <= 0
                || totalParticles != particlesPerTick * emissionTicks
                || particlesPerTick > 480 || minimumLifetime <= 0 || maximumLifetime < minimumLifetime
                || !Double.isFinite(maximumRadius) || maximumRadius <= 0.0D || maximumRadius > 65.0D
                || cadenceBeats < 2 || cadenceBeats > 12) {
            throw new IllegalArgumentException("Invalid bounded plan for " + id);
        }
    }

    private static void validateColor(String color) {
        if (color == null || !color.matches("#[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Colors must be six-digit hex values");
        }
    }

    public String translationKey() {
        return "item.urbanforma_fireworks." + id;
    }

    public String normalizedCategory() {
        return category.name().toLowerCase(Locale.ROOT);
    }

    public enum Category {
        SPHERE, RADIAL, RING_CORE, SHORT_WILLOW, PULSE, INTERLEAVED_SHELL
    }
}
