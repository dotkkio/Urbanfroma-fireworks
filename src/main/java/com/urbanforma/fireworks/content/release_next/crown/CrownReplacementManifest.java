package com.urbanforma.fireworks.content.release_next.crown;

import com.urbanforma.fireworks.content.FireworkStyle;
import java.util.List;
import java.util.Objects;

/**
 * Typed, append-only replacement contract for the five original crown shells.
 *
 * <p>The shared scheduler must route supported payloads here before its legacy crown branch. This class has no
 * client classes, particle allocation, server tick logic, or network side effects.</p>
 */
public final class CrownReplacementManifest {
    public static final int VISUAL_LIFETIME_TICKS = 300;
    public static final int BRANCH_COUNT = 48;
    public static final int PARTICLES_PER_BRANCH = 8;
    public static final int PARTICLES_PER_PROGRAM = BRANCH_COUNT * PARTICLES_PER_BRANCH;
    public static final int MAX_ACTIVE_PROGRAMS = 1;
    public static final int MAX_OWNED_PARTICLES = PARTICLES_PER_PROGRAM * MAX_ACTIVE_PROGRAMS;
    public static final double GROUND_CLEARANCE_BLOCKS = 24.0D;

    private static final List<String> REPLACED_IDS = List.of(
            "coral_rose_crown_sphere",
            "glacier_teal_crown_sphere",
            "opal_rose_crown_sphere",
            "platinum_onyx_crown_sphere",
            "emerald_silver_crown_sphere");

    private CrownReplacementManifest() {
    }

    public static List<String> replacedIds() {
        return REPLACED_IDS;
    }

    public static boolean supports(FireworkStyle style) {
        return style != null
                && style.shape() == FireworkStyle.Shape.CROWN_SPHERE
                && REPLACED_IDS.contains(style.id());
    }

    public static CrownReplacementProgram.Palette paletteFrom(FireworkStyle style) {
        Objects.requireNonNull(style, "style");
        if (!supports(style)) {
            throw new IllegalArgumentException("No release-next crown replacement for " + style.id());
        }
        return new CrownReplacementProgram.Palette(
                color(style.primaryColor()), color(style.secondaryColor()), color(style.accentColor()));
    }

    private static CrownReplacementProgram.Color color(FireworkStyle.Rgb source) {
        return new CrownReplacementProgram.Color(source.red(), source.green(), source.blue());
    }
}
