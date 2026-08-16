package com.urbanforma.fireworks.content.release_next.colorchange;

import java.util.List;
import java.util.Map;

/**
 * Isolated release-next data contract for color-changing effects. The common side only describes palettes;
 * particle ownership and ticking stay in the client package.
 */
public final class ReleaseNextColorChangeCatalog {
    public record Color(float red, float green, float blue) {
        public Color {
            if (!finite(red) || !finite(green) || !finite(blue)
                    || red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1) {
                throw new IllegalArgumentException("Color channels must be finite normalized RGB");
            }
        }
    }

    public record Recipe(String id, String legacyId, Color initial, Color transition, Color target,
            int switchTick, int lifetimeTicks) {
        public Recipe {
            if (id == null || id.isBlank() || legacyId == null || legacyId.isBlank()) {
                throw new IllegalArgumentException("Color-change IDs must not be blank");
            }
            if (switchTick < 6 || switchTick > 14 || lifetimeTicks <= switchTick + 1) {
                throw new IllegalArgumentException("Color-change timing is outside the bounded contract");
            }
        }
    }

    private static Color c(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return new Color((rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F);
    }

    private static Recipe r(String id, String legacy, String initial, String transition, String target,
            int switchTick) {
        return new Recipe(id, legacy, c(initial), c(transition), c(target), switchTick, 40);
    }

    /** Twelve reviewed recipes; each has an unmistakable intermediate and target phase. */
    public static final List<Recipe> RECIPES = List.of(
            r("release_next_cinnabar_azure", "cinnabar_amber_sphere", "#FF5A18", "#8B49E8", "#2D6BFF", 10),
            r("release_next_ember_violet", "batch05_ember_twilight_radiant", "#FF5A18", "#A34CE0", "#6A46D8", 10),
            r("release_next_sunset_orchid", "batch05_sunset_orchid_willow", "#F06A3D", "#B56DE6", "#9D55D8", 12),
            r("release_next_aurora_cyan", "batch05_aurora_pearl_hybrid", "#FFC441", "#52D6D1", "#2CBED0", 9),
            r("release_next_amber_ruby", "amber_radiant_firework", "#FF9B16", "#F04C6E", "#C51F52", 8),
            r("release_next_ruby_azure", "amber_radiant_willow_firework", "#EC1735", "#6F75E8", "#3157D7", 11),
            r("release_next_giant_amber_aurora", "giant_amber_radiant_firework", "#FFB52E", "#63D7C7", "#2AB5D0", 10),
            r("release_next_golden_violet", "giant_golden_white_radial_willow_firework", "#FFF0B4", "#B89AEF", "#7048D8", 12),
            r("release_next_led_scarlet_mint", "led_scarlet_sphere", "#FF244C", "#8BE08E", "#37C98B", 7),
            r("release_next_led_cobalt_rose", "led_cobalt_sphere", "#265DDB", "#C56ED2", "#F04D98", 8),
            r("release_next_polar_gold", "polar_silver_willow", "#44D7C3", "#F6D35A", "#FFB21A", 13),
            r("release_next_jade_magenta", "jade_pearl_willow", "#0AA874", "#D251B6", "#D51A91", 9));

    public static final Map<String, String> REPLACEMENT_MAP = RECIPES.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(Recipe::legacyId, Recipe::id));

    private ReleaseNextColorChangeCatalog() {
    }

    public static Recipe byId(String id) {
        return RECIPES.stream().filter(recipe -> recipe.id().equals(id)).findFirst().orElse(null);
    }

    private static boolean finite(float value) {
        return Float.isFinite(value);
    }
}
