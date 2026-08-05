package com.urbanforma.fireworks.content.batch_other_extra;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fifteen new Other entries kept isolated until the shared integration owner accepts the handoff. */
public final class BatchOtherExtraCatalog {
    public static final String SERIES_ID = "other";
    public static final String BATCH_ID = "batch_other_extra";
    public static final int REQUIRED_ENTRY_COUNT = 15;
    public static final int EXISTING_COOL_COLOR_COUNT = 5;
    public static final int COOL_COLOR_DELTA = 1;
    public static final int COOL_COLOR_COUNT_AFTER_BATCH = EXISTING_COOL_COLOR_COUNT + COOL_COLOR_DELTA;
    public static final int COOL_COLOR_LIMIT = 20;
    public static final String INTEGRATION_STATUS = "ISOLATED_NOT_REGISTERED";
    public static final String CREATIVE_SECTION = "gui.urbanforma_fireworks.section.fireworks.other";
    public static final String PARTICLE_TYPE = BatchOtherExtraFirework.PARTICLE_TYPE;
    public static final String PARTICLE_ENGINE = BatchOtherExtraFirework.PARTICLE_ENGINE;
    public static final int ORDINARY_MAXIMUM_ENVELOPE = BatchOtherExtraFirework.ORDINARY_MAXIMUM_ENVELOPE;

    private static final List<String> RECIPE_PATTERN = List.of(" P ", "FGF", " P ");
    private static final java.util.Map<String, String> RECIPE_INGREDIENTS = java.util.Map.of(
            "P", "minecraft:paper",
            "F", "minecraft:firework_star",
            "G", "minecraft:gunpowder");

    private static final List<BatchOtherExtraFirework> VALUES = List.of(
            entry(1, "other_extra_axis_weave", "轴织脉冲", "Axis-Weave Pulse Firework", BatchOtherExtraFirework.Family.WARM,
                    palette("#C9472E", "#F29B38", "#FFE2AA", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.AXIS_WEAVE, 48, 28, 3, 0, 2, 0, 1,
                            4.0D, 34.0D, 6.0D, 6.0D, 5.0D, 1.4D, 26, 8, 3, 3,
                            BatchOtherExtraFirework.TrailPhysics.AXIAL_SWEEP),
                    68, 110, 56, 32, 72, 54, 520, 300, 180, 0.30F, 0.52F),
            entry(2, "other_extra_triad_step", "三向阶跃", "Triad-Step Firework", BatchOtherExtraFirework.Family.METALLIC,
                    palette("#C75B2A", "#F4C95D", "#FFF0C2", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.TRIAD_STEP, 54, 30, 3, 2, 3, 1, 1,
                            5.0D, 34.0D, 8.0D, 7.0D, 6.0D, 2.1D, 24, 9, 4, 2,
                            BatchOtherExtraFirework.TrailPhysics.STEPPED_FLICKER),
                    68, 118, 60, 34, 76, 56, 500, 320, 180, 0.32F, 0.55F),
            entry(3, "other_extra_tetra_twist", "四面扭束", "Tetra-Twist Spine Firework", BatchOtherExtraFirework.Family.JEWEL,
                    palette("#A52A2A", "#E09F3E", "#FFF3C4", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.TETRA_TWIST, 48, 32, 4, 1, 2, 0, 0,
                            5.0D, 38.0D, 6.0D, 6.0D, 5.0D, 3.8D, 28, 10, 5, 3,
                            BatchOtherExtraFirework.TrailPhysics.TETRA_DRAG),
                    76, 120, 64, 36, 82, 58, 540, 280, 180, 0.34F, 0.58F),
            entry(4, "other_extra_polar_staircase", "极向梯列", "Polar Staircase Firework", BatchOtherExtraFirework.Family.WARM,
                    palette("#D46335", "#F2B134", "#FFE8B6", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.POLAR_STAIRCASE, 56, 34, 2, 3, 4, 1, 1,
                            5.0D, 33.0D, 8.0D, 7.0D, 5.0D, 2.4D, 30, 9, 6, 2,
                            BatchOtherExtraFirework.TrailPhysics.POLAR_DRIP),
                    66, 118, 62, 38, 84, 60, 480, 340, 180, 0.30F, 0.56F),
            entry(5, "other_extra_prism_return", "棱柱回折", "Prism Return Firework", BatchOtherExtraFirework.Family.METALLIC,
                    palette("#9E2A2B", "#D77A30", "#FFD6A5", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.PRISM_RETURN, 60, 30, 3, 4, 3, 0, 2,
                            4.0D, 32.0D, 7.0D, 8.0D, 6.0D, 1.2D, 26, 8, 4, 3,
                            BatchOtherExtraFirework.TrailPhysics.RETURN_SNAP),
                    64, 118, 58, 34, 78, 56, 510, 310, 180, 0.31F, 0.54F),
            entry(6, "other_extra_counter_twist_braid", "逆旋编带", "Counter-Twist Braid Firework", BatchOtherExtraFirework.Family.JEWEL,
                    palette("#B23A48", "#E85D75", "#FFE5D9", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.COUNTER_TWIST_BRAID, 64, 32, 2, 2, 2, 1, 1,
                            4.0D, 35.0D, 5.0D, 7.0D, 8.0D, 5.2D, 28, 10, 6, 3,
                            BatchOtherExtraFirework.TrailPhysics.COUNTER_TWIST),
                    70, 120, 66, 38, 86, 62, 500, 300, 200, 0.33F, 0.58F),
            entry(7, "other_extra_octant_gap", "八向断续", "Octant-Gap Firework", BatchOtherExtraFirework.Family.COOL,
                    palette("#2A9D8F", "#8FD694", "#F1FAEE", true),
                    trajectory(BatchOtherExtraFirework.EffectPath.OCTANT_GAP, 64, 36, 4, 1, 4, 0, 1,
                            5.0D, 34.0D, 6.0D, 6.0D, 5.0D, 0.8D, 24, 8, 3, 2,
                            BatchOtherExtraFirework.TrailPhysics.OCTANT_BREAK),
                    68, 114, 60, 32, 70, 50, 560, 280, 160, 0.28F, 0.50F),
            entry(8, "other_extra_canted_columns", "斜柱层叠", "Canted Column Firework", BatchOtherExtraFirework.Family.METALLIC,
                    palette("#8F3B2B", "#D97D3F", "#FFE1B5", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.CANTED_COLUMNS, 60, 30, 3, 5, 3, 2, 1,
                            4.0D, 31.0D, 9.0D, 7.0D, 6.0D, 1.6D, 30, 9, 7, 2,
                            BatchOtherExtraFirework.TrailPhysics.COLUMN_SHEAR),
                    62, 118, 64, 40, 88, 64, 470, 340, 190, 0.32F, 0.56F),
            entry(9, "other_extra_axial_tunnel", "轴隧回响", "Axial Tunnel Pulse Firework", BatchOtherExtraFirework.Family.WARM,
                    palette("#B5651D", "#E09F3E", "#FFF1C1", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.AXIAL_TUNNEL, 72, 34, 3, 3, 2, 0, 0,
                            4.0D, 30.0D, 5.0D, 7.0D, 8.0D, 2.8D, 24, 9, 5, 3,
                            BatchOtherExtraFirework.TrailPhysics.TUNNEL_ECHO),
                    60, 114, 58, 34, 76, 54, 530, 300, 170, 0.30F, 0.53F),
            entry(10, "other_extra_diagonal_ladder", "对角阶梯", "Diagonal Ladder Firework", BatchOtherExtraFirework.Family.METALLIC,
                    palette("#C44900", "#F48C06", "#FFE8A3", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.DIAGONAL_LADDER, 64, 32, 2, 2, 3, 1, 1,
                            5.0D, 32.0D, 8.0D, 8.0D, 6.0D, 1.0D, 26, 8, 4, 2,
                            BatchOtherExtraFirework.TrailPhysics.LADDER_RUNG),
                    64, 120, 60, 36, 80, 58, 500, 320, 180, 0.29F, 0.55F),
            entry(11, "other_extra_triple_torsion", "三重扭轨", "Triple Torsion Track Firework", BatchOtherExtraFirework.Family.JEWEL,
                    palette("#9C2F46", "#DB6B30", "#FFF0D0", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.TRIPLE_TORSION, 72, 34, 3, 4, 4, 0, 1,
                            5.0D, 33.0D, 7.0D, 7.0D, 8.0D, 6.0D, 28, 10, 6, 3,
                            BatchOtherExtraFirework.TrailPhysics.TORSION_DRIFT),
                    66, 120, 64, 40, 90, 64, 520, 300, 180, 0.34F, 0.60F),
            entry(12, "other_extra_split_merge", "分合双轨", "Split-Merge Track Firework", BatchOtherExtraFirework.Family.WARM,
                    palette("#A23E48", "#E08E45", "#FFE2B8", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.SPLIT_MERGE, 60, 32, 2, 6, 2, 0, 1,
                            4.0D, 32.0D, 6.0D, 9.0D, 7.0D, 1.8D, 26, 9, 5, 3,
                            BatchOtherExtraFirework.TrailPhysics.SPLIT_REJOIN),
                    64, 120, 58, 36, 78, 56, 500, 320, 180, 0.31F, 0.56F),
            entry(13, "other_extra_cubic_offset", "立方错棱", "Cubic Offset Edge Firework", BatchOtherExtraFirework.Family.METALLIC,
                    palette("#7F5539", "#C98B4A", "#FFE6B0", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.CUBIC_OFFSET, 72, 30, 3, 2, 5, 1, 1,
                            5.0D, 33.0D, 6.0D, 8.0D, 7.0D, 0.0D, 24, 8, 3, 2,
                            BatchOtherExtraFirework.TrailPhysics.EDGE_GLINT),
                    66, 120, 60, 32, 72, 50, 580, 260, 160, 0.28F, 0.50F),
            entry(14, "other_extra_vertical_fracture", "垂直裂束", "Vertical Fracture Firework", BatchOtherExtraFirework.Family.JEWEL,
                    palette("#C44536", "#E89B4A", "#FFF0B3", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.VERTICAL_FRACTURE, 64, 36, 4, 3, 3, 2, 2,
                            4.0D, 32.0D, 7.0D, 8.0D, 7.0D, 2.2D, 30, 10, 4, 3,
                            BatchOtherExtraFirework.TrailPhysics.FRACTURE_DELAY),
                    64, 120, 62, 40, 88, 62, 490, 330, 180, 0.33F, 0.59F),
            entry(15, "other_extra_phased_spindle", "纺锤相列", "Phased Spindle Track Firework", BatchOtherExtraFirework.Family.WARM,
                    palette("#A63D40", "#E6A23C", "#FFF2C9", false),
                    trajectory(BatchOtherExtraFirework.EffectPath.PHASED_SPINDLE, 72, 34, 3, 5, 2, 0, 1,
                            5.0D, 33.0D, 7.0D, 8.0D, 7.0D, 4.6D, 28, 11, 6, 3,
                            BatchOtherExtraFirework.TrailPhysics.SPINDLE_TAPER),
                    66, 120, 66, 42, 92, 66, 520, 300, 180, 0.35F, 0.62F));

    static {
        validateCatalog();
    }

    private BatchOtherExtraCatalog() {
    }

    public static List<BatchOtherExtraFirework> values() {
        return VALUES;
    }

    public static BatchOtherExtraFirework byId(String id) {
        for (BatchOtherExtraFirework value : VALUES) {
            if (value.id().equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown Extra Other firework " + id);
    }

    private static BatchOtherExtraFirework entry(
            int stableOrder,
            String id,
            String zhName,
            String enName,
            BatchOtherExtraFirework.Family family,
            BatchOtherExtraFirework.Palette palette,
            BatchOtherExtraFirework.TrajectoryContract trajectory,
            int nominalDiameter,
            int fullEnvelope,
            int flightTicks,
            int coreLifetime,
            int trailLifetime,
            int accentLifetime,
            int primaryLayerPermille,
            int secondaryLayerPermille,
            int accentLayerPermille,
            float twinkleMin,
            float twinkleMax) {
        int totalParticles = trajectory.plannedParticles();
        int maxPerTick = trajectory.maximumParticlesPerTick();
        return new BatchOtherExtraFirework(
                id,
                zhName,
                enName,
                trajectory.route(),
                trajectory.route().clientProgramId(),
                family,
                palette,
                new BatchOtherExtraFirework.StyleParameters(
                        flightTicks,
                        nominalDiameter,
                        fullEnvelope,
                        trajectory.startDelayTicks(),
                        totalParticles,
                        maxPerTick,
                        totalParticles,
                        primaryLayerPermille,
                        secondaryLayerPermille,
                        accentLayerPermille,
                        coreLifetime,
                        trailLifetime,
                        accentLifetime,
                        twinkleMin,
                        twinkleMax),
                BatchOtherExtraVisualSignatures.forId(id, palette.signature()),
                new BatchOtherExtraFirework.RecipeContract(
                        BatchOtherExtraFirework.RECIPE_TEMPLATE,
                        RECIPE_PATTERN,
                        RECIPE_INGREDIENTS,
                        "urbanforma_fireworks:" + id,
                        1),
                new BatchOtherExtraFirework.CreativeContract(CREATIVE_SECTION, "other_extra", stableOrder),
                new BatchOtherExtraFirework.ModelContract(
                        BatchOtherExtraFirework.MODEL_PARENT,
                        BatchOtherExtraFirework.MODEL_PATH_TEMPLATE,
                        "vanilla_firework_rocket"),
                new BatchOtherExtraFirework.ParticleContract(
                        PARTICLE_TYPE,
                        PARTICLE_ENGINE,
                        "STANDARD",
                        totalParticles,
                        maxPerTick),
                new BatchOtherExtraFirework.ExpectedBoundary(
                        nominalDiameter,
                        fullEnvelope,
                        ORDINARY_MAXIMUM_ENVELOPE,
                        "BatchOtherExtraClientPrograms." + trajectory.route().id() + ".fitsWithin(" + fullEnvelope + ")"),
                trajectory);
    }

    private static BatchOtherExtraFirework.Palette palette(
            String primary,
            String secondary,
            String accent,
            boolean cool) {
        return new BatchOtherExtraFirework.Palette(primary, secondary, accent, cool);
    }

    private static BatchOtherExtraFirework.TrajectoryContract trajectory(
            BatchOtherExtraFirework.EffectPath route,
            int branches,
            int segments,
            int components,
            int startDelay,
            int pulseStride,
            int phaseOffset,
            int componentPhaseStride,
            double innerRadius,
            double outerRadius,
            double verticalAmplitude,
            double lateralAmplitude,
            double depthAmplitude,
            double twist,
            int baseLifetime,
            int lifetimeSpread,
            int baseTrailLength,
            int trailSpread,
            BatchOtherExtraFirework.TrailPhysics trailPhysics) {
        return new BatchOtherExtraFirework.TrajectoryContract(
                route,
                branches,
                segments,
                components,
                startDelay,
                pulseStride,
                phaseOffset,
                componentPhaseStride,
                innerRadius,
                outerRadius,
                verticalAmplitude,
                lateralAmplitude,
                depthAmplitude,
                twist,
                baseLifetime,
                lifetimeSpread,
                baseTrailLength,
                trailSpread,
                trailPhysics);
    }

    private static void validateCatalog() {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT || COOL_COLOR_COUNT_AFTER_BATCH > COOL_COLOR_LIMIT) {
            throw new IllegalStateException("Extra Other count or cool-color budget is invalid");
        }
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<String> structures = new HashSet<>();
        Set<String> palettes = new HashSet<>();
        Set<BatchOtherExtraFirework.EffectPath> routes = new HashSet<>();
        int coolCount = 0;
        for (int index = 0; index < VALUES.size(); index++) {
            BatchOtherExtraFirework value = VALUES.get(index);
            if (!ids.add(value.id()) || !names.add(value.zhName()) || !names.add(value.enName())
                    || !structures.add(value.visualDifference().structureSignature())
                    || !palettes.add(value.palette().signature())
                    || !routes.add(value.effectPath())
                    || value.creative().stableOrder() != index + 1
                    || !value.clientProgram().equals(value.effectPath().clientProgramId())
                    || value.visualDifference().structuralAxes().size() != 6
                    || !value.visualDifference().structureSignature().equals(
                            value.visualDifference().computedStructureSignature())
                    || value.trajectory().route() != value.effectPath()
                    || value.trajectory().lifecycleTicks() > 240
                    || BatchOtherExtraTrajectory.conservativeEnvelopeBlocks(value.trajectory()) > value.style().fullEnvelopeBlocks()
                    || value.style().fullEnvelopeBlocks() > ORDINARY_MAXIMUM_ENVELOPE) {
                throw new IllegalStateException("Invalid Extra Other entry contract " + value.id());
            }
            if (value.palette().consumesCoolColorQuota()) {
                coolCount++;
            }
        }
        if (coolCount != COOL_COLOR_DELTA || COOL_COLOR_COUNT_AFTER_BATCH != EXISTING_COOL_COLOR_COUNT + coolCount
                || routes.size() != REQUIRED_ENTRY_COUNT || structures.size() != REQUIRED_ENTRY_COUNT
                || palettes.size() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Extra Other route, signature, or cool-color budget drifted");
        }
    }
}
