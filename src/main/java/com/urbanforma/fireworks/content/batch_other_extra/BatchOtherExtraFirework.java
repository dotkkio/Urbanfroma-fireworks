package com.urbanforma.fireworks.content.batch_other_extra;

import com.urbanforma.fireworks.content.FireworkStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable handoff contract for one of the fifteen additional Other effects.
 *
 * <p>This type is common-side and deliberately contains no registry, event, network, server particle, or client
 * scheduler reference. The integration owner can map every field explicitly after reviewing this isolated catalog.</p>
 */
public record BatchOtherExtraFirework(
        String id,
        String zhName,
        String enName,
        EffectPath effectPath,
        String clientProgram,
        Family family,
        Palette palette,
        StyleParameters style,
        VisualDifference visualDifference,
        RecipeContract recipe,
        CreativeContract creative,
        ModelContract model,
        ParticleContract particle,
        ExpectedBoundary expectedBoundary,
        TrajectoryContract trajectory) {

    public static final String SERIES_ID = "other";
    public static final String BATCH_ID = "batch_other_extra";
    public static final String PARTICLE_TYPE = "urbanforma_fireworks:hd_firework_spark";
    public static final String PARTICLE_ENGINE = "GrandFireworkClientEffects.ActiveBurst";
    public static final String MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final String MODEL_PATH_TEMPLATE = "assets/urbanforma_fireworks/models/item/{id}.json";
    public static final String RECIPE_TEMPLATE = "normal_firework_rocket_3x3";
    public static final int ORDINARY_MAXIMUM_ENVELOPE = 120;
    public static final int MAX_PER_TICK = 720;
    public static final int MAX_OWNED_PARTICLES = 15_000;

    /** Every route is abstract geometry, not a pictorial silhouette or a color-only variant. */
    public enum EffectPath {
        AXIS_WEAVE("axis_weave"),
        TRIAD_STEP("triad_step"),
        TETRA_TWIST("tetra_twist"),
        POLAR_STAIRCASE("polar_staircase"),
        PRISM_RETURN("prism_return"),
        COUNTER_TWIST_BRAID("counter_twist_braid"),
        OCTANT_GAP("octant_gap"),
        CANTED_COLUMNS("canted_columns"),
        AXIAL_TUNNEL("axial_tunnel"),
        DIAGONAL_LADDER("diagonal_ladder"),
        TRIPLE_TORSION("triple_torsion"),
        SPLIT_MERGE("split_merge"),
        CUBIC_OFFSET("cubic_offset"),
        VERTICAL_FRACTURE("vertical_fracture"),
        PHASED_SPINDLE("phased_spindle");

        private final String id;

        EffectPath(String id) {
            this.id = id;
        }

        public String id() {
            return this.id;
        }

        public String clientProgramId() {
            return BATCH_ID + ":" + this.id;
        }
    }

    public enum Family {
        WARM,
        JEWEL,
        METALLIC,
        COOL
    }

    /** Named trail physics are part of the handoff so the integrator cannot silently collapse all tails. */
    public enum TrailPhysics {
        AXIAL_SWEEP,
        STEPPED_FLICKER,
        TETRA_DRAG,
        POLAR_DRIP,
        RETURN_SNAP,
        COUNTER_TWIST,
        OCTANT_BREAK,
        COLUMN_SHEAR,
        TUNNEL_ECHO,
        LADDER_RUNG,
        TORSION_DRIFT,
        SPLIT_REJOIN,
        EDGE_GLINT,
        FRACTURE_DELAY,
        SPINDLE_TAPER
    }

    public BatchOtherExtraFirework {
        if (id == null || !id.matches("other_extra_[a-z0-9_]+")) {
            throw new IllegalArgumentException("Extra Other ids must use the other_extra_ prefix");
        }
        if (zhName == null || zhName.isBlank() || enName == null || enName.isBlank()) {
            throw new IllegalArgumentException("Extra Other entries require bilingual names");
        }
        Objects.requireNonNull(effectPath, "effectPath");
        if (clientProgram == null || !clientProgram.equals(effectPath.clientProgramId())) {
            throw new IllegalArgumentException("Client program must be derived from the typed effect path");
        }
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(style, "style");
        Objects.requireNonNull(visualDifference, "visualDifference");
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(creative, "creative");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(expectedBoundary, "expectedBoundary");
        Objects.requireNonNull(trajectory, "trajectory");
        if (trajectory.route() != effectPath
                || visualDifference.paletteSignature() == null
                || !visualDifference.paletteSignature().equals(palette.signature())
                || !visualDifference.structureSignature().equals(visualDifference.computedStructureSignature())) {
            throw new IllegalArgumentException("Extra Other visual and trajectory contracts drifted");
        }
        if (style.totalParticleCount() != trajectory.plannedParticles()
                || style.maxParticlesPerTick() != trajectory.maximumParticlesPerTick()
                || style.ownedParticles() < style.totalParticleCount()
                || style.ownedParticles() > MAX_OWNED_PARTICLES) {
            throw new IllegalArgumentException("Extra Other style and trajectory budgets disagree");
        }
        if (particle.peakPerBurst() != style.totalParticleCount()
                || particle.maxPerTick() != style.maxParticlesPerTick()
                || expectedBoundary.fullEnvelopeBlocks() != style.fullEnvelopeBlocks()
                || expectedBoundary.fullEnvelopeBlocks() > ORDINARY_MAXIMUM_ENVELOPE) {
            throw new IllegalArgumentException("Extra Other particle and envelope contracts disagree");
        }
    }

    public record Palette(
            String primaryHex,
            String secondaryHex,
            String accentHex,
            boolean consumesCoolColorQuota) {
        public Palette {
            FireworkStyle.Rgb primary = FireworkStyle.Rgb.fromHex(primaryHex);
            FireworkStyle.Rgb secondary = FireworkStyle.Rgb.fromHex(secondaryHex);
            FireworkStyle.Rgb accent = FireworkStyle.Rgb.fromHex(accentHex);
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Extra Other palette layers must be distinct");
            }
        }

        public FireworkStyle.Rgb primary() {
            return FireworkStyle.Rgb.fromHex(this.primaryHex);
        }

        public FireworkStyle.Rgb secondary() {
            return FireworkStyle.Rgb.fromHex(this.secondaryHex);
        }

        public FireworkStyle.Rgb accent() {
            return FireworkStyle.Rgb.fromHex(this.accentHex);
        }

        public String signature() {
            return String.join(
                    "/",
                    this.primaryHex.toUpperCase(Locale.ROOT),
                    this.secondaryHex.toUpperCase(Locale.ROOT),
                    this.accentHex.toUpperCase(Locale.ROOT));
        }
    }

    public record StyleParameters(
            int flightTicks,
            int nominalDiameterBlocks,
            int fullEnvelopeBlocks,
            int phaseDelayTicks,
            int totalParticleCount,
            int maxParticlesPerTick,
            int ownedParticles,
            int primaryLayerPermille,
            int secondaryLayerPermille,
            int accentLayerPermille,
            int coreLifetimeTicks,
            int trailLifetimeTicks,
            int accentLifetimeTicks,
            float twinkleChanceMin,
            float twinkleChanceMax) {
        public StyleParameters {
            if (flightTicks < 40 || flightTicks > 100
                    || nominalDiameterBlocks <= 0 || fullEnvelopeBlocks < nominalDiameterBlocks
                    || fullEnvelopeBlocks > ORDINARY_MAXIMUM_ENVELOPE
                    || phaseDelayTicks < 0 || phaseDelayTicks > 40
                    || totalParticleCount <= 0 || totalParticleCount > MAX_OWNED_PARTICLES
                    || maxParticlesPerTick <= 0 || maxParticlesPerTick > MAX_PER_TICK
                    || ownedParticles < totalParticleCount || ownedParticles > MAX_OWNED_PARTICLES
                    || primaryLayerPermille < 0 || secondaryLayerPermille < 0 || accentLayerPermille < 0
                    || primaryLayerPermille + secondaryLayerPermille + accentLayerPermille != 1_000
                    || coreLifetimeTicks <= 0 || trailLifetimeTicks <= 0 || accentLifetimeTicks <= 0
                    || coreLifetimeTicks > 100 || trailLifetimeTicks > 140 || accentLifetimeTicks > 140
                    || twinkleChanceMin < 0.0F || twinkleChanceMax > 1.0F
                    || twinkleChanceMin > twinkleChanceMax) {
                throw new IllegalArgumentException("Invalid Extra Other style parameters");
            }
        }
    }

    public record VisualDifference(
            String shape,
            String core,
            String trail,
            String layering,
            String cadence,
            String density,
            List<String> structuralAxes,
            String structureSignature,
            String paletteSignature,
            String description) {
        public VisualDifference {
            if (shape == null || shape.isBlank() || core == null || core.isBlank()
                    || trail == null || trail.isBlank() || layering == null || layering.isBlank()
                    || cadence == null || cadence.isBlank() || density == null || density.isBlank()
                    || structuralAxes == null || structuralAxes.size() != 6
                    || structureSignature == null || structureSignature.isBlank()
                    || paletteSignature == null || paletteSignature.isBlank()
                    || description == null || description.isBlank()) {
                throw new IllegalArgumentException("Every Extra Other entry needs six visual axes");
            }
            structuralAxes = List.copyOf(structuralAxes);
            if (!structuralAxes.equals(List.of("SHAPE", "CORE", "TRAIL", "LAYERING", "CADENCE", "DENSITY"))) {
                throw new IllegalArgumentException("Extra Other structural axes must be complete and ordered");
            }
        }

        public String computedStructureSignature() {
            return String.join("|", this.shape, this.core, this.trail, this.layering, this.cadence, this.density);
        }
    }

    public record RecipeContract(
            String template,
            List<String> pattern,
            Map<String, String> ingredients,
            String result,
            int count) {
        public RecipeContract {
            Objects.requireNonNull(template, "template");
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(ingredients, "ingredients");
            Objects.requireNonNull(result, "result");
            if (!template.equals(RECIPE_TEMPLATE)
                    || !pattern.equals(List.of(" P ", "FGF", " P "))
                    || !ingredients.equals(Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || !result.startsWith("urbanforma_fireworks:other_extra_") || count != 1) {
                throw new IllegalArgumentException("Extra Other recipes must reuse the normal rocket template");
            }
        }
    }

    public record CreativeContract(String sectionKey, String orderGroup, int stableOrder) {
        public CreativeContract {
            if (!"gui.urbanforma_fireworks.section.fireworks.other".equals(sectionKey)
                    || !"other_extra".equals(orderGroup) || stableOrder < 1 || stableOrder > 15) {
                throw new IllegalArgumentException("Extra Other creative entries must append to the Other section");
            }
        }
    }

    public record ModelContract(String parent, String pathTemplate, String reuseContract) {
        public ModelContract {
            if (!MODEL_PARENT.equals(parent)
                    || !MODEL_PATH_TEMPLATE.equals(pathTemplate)
                    || !"vanilla_firework_rocket".equals(reuseContract)) {
                throw new IllegalArgumentException("Extra Other models must reuse the vanilla rocket contract");
            }
        }
    }

    public record ParticleContract(
            String particleType,
            String engine,
            String category,
            int peakPerBurst,
            int maxPerTick) {
        public ParticleContract {
            if (!PARTICLE_TYPE.equals(particleType) || !PARTICLE_ENGINE.equals(engine)
                    || !"STANDARD".equals(category) || peakPerBurst <= 0
                    || peakPerBurst > MAX_OWNED_PARTICLES || maxPerTick <= 0 || maxPerTick > MAX_PER_TICK) {
                throw new IllegalArgumentException("Invalid Extra Other particle contract");
            }
        }
    }

    public record ExpectedBoundary(
            int nominalDiameterBlocks,
            int fullEnvelopeBlocks,
            int ordinaryMaximumBlocks,
            String boundaryEvidence) {
        public ExpectedBoundary {
            if (nominalDiameterBlocks <= 0 || fullEnvelopeBlocks < nominalDiameterBlocks
                    || fullEnvelopeBlocks > ordinaryMaximumBlocks
                    || ordinaryMaximumBlocks != ORDINARY_MAXIMUM_ENVELOPE
                    || boundaryEvidence == null || boundaryEvidence.isBlank()) {
                throw new IllegalArgumentException("Invalid Extra Other envelope contract");
            }
        }
    }

    /** Fixed trajectory parameters shared by the common contract and the client sampler. */
    public record TrajectoryContract(
            EffectPath route,
            int branchCount,
            int segmentCount,
            int componentCount,
            int startDelayTicks,
            int pulseStride,
            int phaseOffset,
            int componentPhaseStride,
            double innerRadius,
            double outerRadius,
            double verticalAmplitude,
            double lateralAmplitude,
            double depthAmplitude,
            double twist,
            int baseLifetimeTicks,
            int lifetimeSpreadTicks,
            int baseTrailLength,
            int trailSpread,
            TrailPhysics trailPhysics) {
        public TrajectoryContract {
            Objects.requireNonNull(route, "route");
            Objects.requireNonNull(trailPhysics, "trailPhysics");
            if (branchCount <= 0 || branchCount > 96 || segmentCount <= 0 || segmentCount > 40
                    || componentCount <= 0 || componentCount > 4 || startDelayTicks < 0 || startDelayTicks > 40
                    || pulseStride <= 0 || pulseStride > 8 || phaseOffset < 0 || componentPhaseStride < 0
                    || !finitePositive(innerRadius) || !finitePositive(outerRadius) || outerRadius <= innerRadius
                    || !finiteNonNegative(verticalAmplitude) || !finiteNonNegative(lateralAmplitude)
                    || !finiteNonNegative(depthAmplitude) || !Double.isFinite(twist)
                    || baseLifetimeTicks <= 0 || lifetimeSpreadTicks <= 0 || baseLifetimeTicks + lifetimeSpreadTicks > 140
                    || baseTrailLength < 0 || trailSpread < 0 || baseTrailLength + trailSpread > 32) {
                throw new IllegalArgumentException("Invalid bounded Extra Other trajectory contract");
            }
        }

        private static boolean finitePositive(double value) {
            return Double.isFinite(value) && value > 0.0D;
        }

        private static boolean finiteNonNegative(double value) {
            return Double.isFinite(value) && value >= 0.0D;
        }

        public int plannedParticles() {
            return BatchOtherExtraTrajectory.plannedParticles(this);
        }

        public int maximumParticlesPerTick() {
            return BatchOtherExtraTrajectory.maximumParticlesPerTick(this);
        }

        public int lifecycleTicks() {
            return this.startDelayTicks + this.segmentCount + this.baseLifetimeTicks + this.lifetimeSpreadTicks;
        }
    }
}
