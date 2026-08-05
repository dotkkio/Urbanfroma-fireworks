package com.urbanforma.fireworks.content;

import com.urbanforma.fireworks.content.giant.cascade.GiantCascadeTrajectory;
import com.urbanforma.fireworks.content.giant.multiradial2.GiantMultiRadial2Trajectory;
import com.urbanforma.fireworks.content.giant.superwillow.SuperWillowTrajectory;
import com.urbanforma.fireworks.content.giant.thickradial.GiantThickRadialTrajectory;
import java.util.List;

/** Append-only shared registration adapter for the completed independent giant trajectories. */
public final class GiantFireworkCatalog {
    public static final int FIRST_STYLE_INDEX = NormalFireworkCatalog.FIRST_STYLE_INDEX
            + NormalFireworkCatalog.NEW_ORDINARY_STYLE_COUNT;
    public static final int INTEGRATED_GIANT_COUNT = 4;
    private static final FireworkStyle.LayerShares GIANT_SHARES = new FireworkStyle.LayerShares(334, 333, 333);

    private GiantFireworkCatalog() {
    }

    public static List<FireworkStyle> stylesFrom(int firstStyleIndex) {
        if (firstStyleIndex != FIRST_STYLE_INDEX) {
            throw new IllegalArgumentException("Integrated giant styles must follow the normal-100 catalog");
        }
        List<FireworkStyle> styles = List.of(
                giant(
                        firstStyleIndex,
                        "giant_superwillow_firework",
                        "巨型金白超级垂柳烟花",
                        "Giant Golden White Super Willow Firework",
                        FireworkStyle.Family.METALLIC,
                        "#FFF2C7", "#FFD15A", "#FFFDF0",
                        SuperWillowTrajectory.TOTAL_PARTICLES,
                        GiantTier.SUPER_WILLOW),
                giant(
                        firstStyleIndex + 1,
                        "giant_multiradial2_firework",
                        "巨型金白多放射二型烟花",
                        "Giant Golden White Multi-Radial II Firework",
                        FireworkStyle.Family.METALLIC,
                        "#FFE7A3", "#FFB84D", "#FFF8DB",
                        GiantMultiRadial2Trajectory.TOTAL_PARTICLES,
                        GiantTier.MULTI_RADIAL_II),
                giant(
                        firstStyleIndex + 2,
                        "giant_thickradial_firework",
                        "巨型琥珀粗放射烟花",
                        "Giant Amber Thick Radial Firework",
                        FireworkStyle.Family.WARM,
                        "#FF6B19", "#FFA424", "#FFE1A6",
                        GiantThickRadialTrajectory.TOTAL_PARTICLES,
                        GiantTier.THICK_RADIAL),
                giant(
                        firstStyleIndex + 3,
                        "giant_cascade_firework",
                        "巨型绯金层叠烟花",
                        "Giant Scarlet Gold Cascade Firework",
                        FireworkStyle.Family.WARM,
                        "#E92B35", "#FF9E1A", "#FFF0A8",
                        GiantCascadeTrajectory.TOTAL_PARTICLES,
                        GiantTier.CASCADE));
        if (SuperWillowTrajectory.MAX_RADIUS > 130.0D
                || GiantMultiRadial2Trajectory.MAX_RADIUS > 130.0D
                || GiantThickRadialTrajectory.MAX_RADIUS > 130.0D
                || GiantCascadeTrajectory.MAX_RADIUS > 130.0D) {
            throw new IllegalStateException("Integrated giant contract exceeds the approved radius or particle ceiling");
        }
        return styles;
    }

    private static FireworkStyle giant(
            int index,
            String id,
            String zhName,
            String enName,
            FireworkStyle.Family family,
            String primary,
            String secondary,
            String accent,
            int totalParticles,
            GiantTier tier) {
        return new FireworkStyle(
                index,
                id,
                zhName,
                enName,
                family,
                FireworkStyle.Shape.GIANT_RADIANT,
                FireworkStyle.Rgb.fromHex(primary),
                FireworkStyle.Rgb.fromHex(secondary),
                FireworkStyle.Rgb.fromHex(accent),
                138,
                260,
                260,
                0,
                totalParticles,
                GIANT_SHARES,
                256,
                FireworkStyle.TrailTier.GRAND,
                116,
                104,
                96,
                0.35F,
                0.60F,
                null,
                null,
                null,
                tier);
    }
}
