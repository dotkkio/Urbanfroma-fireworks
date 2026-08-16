package com.urbanforma.fireworks.content.large_extra;

import com.urbanforma.fireworks.content.EffectCategory;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Common-side metadata for one isolated Large Fireworks candidate.
 *
 * <p>This record intentionally contains no trajectory math, particle creation, scheduler, listener, network
 * mutation, or server callback. The physical-client program reconstructs the declared visual structure from the
 * compact burst seed after the integration owner routes the stable id.</p>
 */
public record LargeExtraFireworkDefinition(
        int stableOrder,
        String id,
        String zhName,
        String enName,
        EffectPath effectPath,
        Palette palette,
        RecipeContract recipe,
        CreativeTarget creativeTarget,
        ReuseContract reuseContract,
        ParticleBudget particleBudget,
        Envelope envelope,
        VisualDifference visualDifference) {
    public static final String BATCH_ID = "large_extra";
    public static final String MOD_ID = "urbanforma_fireworks";
    public static final String MODEL_PARENT = "minecraft:item/firework_rocket";
    public static final String HD_SPARK = "urbanforma_fireworks:hd_firework_spark";
    public static final String VANILLA_FIREWORK_PARTICLE = "minecraft:firework";
    public static final String RECIPE_TEMPLATE = "normal_firework_rocket_3x3";
    public static final String LARGE_CREATIVE_SECTION = "large";
    public static final String LARGE_CREATIVE_TRANSLATION_KEY = "gui.urbanforma_fireworks.section.fireworks.large";
    public static final String CLIENT_PROGRAM_OWNER =
            "com.urbanforma.fireworks.client.large_extra.LargeExtraClientPrograms";
    public static final int CURRENT_LARGE_FLIGHT_TICKS = 100;
    public static final double CURRENT_LARGE_BURST_HEIGHT_BLOCKS = 100.0D;
    public static final int LARGE_MAXIMUM_FULL_ENVELOPE = 120;
    public static final int MAX_LOCAL_PARTICLES_PER_TICK = 160;
    public static final int MAX_LOCAL_LIVE_PARTICLES = 3_000;
    public static final int MAX_LOCAL_VISUAL_TICKS = 120;
    private static final Pattern STABLE_ID = Pattern.compile("large_extra_[a-z0-9_]+");

    public LargeExtraFireworkDefinition {
        if (stableOrder <= 0 || id == null || !STABLE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Large Extra entries require a positive order and stable large_extra id");
        }
        requireText(zhName, "zhName");
        requireText(enName, "enName");
        Objects.requireNonNull(effectPath, "effectPath");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(creativeTarget, "creativeTarget");
        Objects.requireNonNull(reuseContract, "reuseContract");
        Objects.requireNonNull(particleBudget, "particleBudget");
        Objects.requireNonNull(envelope, "envelope");
        Objects.requireNonNull(visualDifference, "visualDifference");
        if (!recipe.resultId().equals(MOD_ID + ":" + id)
                || !creativeTarget.sectionKey().equals(LARGE_CREATIVE_SECTION)
                || !creativeTarget.translationKey().equals(LARGE_CREATIVE_TRANSLATION_KEY)
                || !reuseContract.clientProgramId().equals(effectPath.clientProgramId())
                || reuseContract.effectCategory() != EffectCategory.STANDARD
                || envelope.fullEnvelopeBlocks() > LARGE_MAXIMUM_FULL_ENVELOPE) {
            throw new IllegalArgumentException("Large Extra shared-contract fields drifted for " + id);
        }
    }

    /** Distinct client-only structures; the enum never maps to a shared global EffectCategory. */
    public enum EffectPath {
        GLOBE_SHELL("globe_shell"),
        TRIPLE_TIER_RADIANCE("triple_tier_radiance"),
        DUAL_BREAK("dual_break"),
        WORLD_GRID("world_grid"),
        STOUT_COMET("stout_comet"),
        APERTURE_HEX_REVEAL("aperture_hex_reveal"),
        ORBITAL_NUCLEUS("orbital_nucleus"),
        INTERWOVEN_RADIANCE("interwoven_radiance"),
        POLAR_LANTERN("polar_lantern"),
        ECLIPSE_ARC_SPLIT("eclipse_arc_split");

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

    /** Six non-palette axes that make a structural difference reviewable before shared wiring. */
    public enum VisualAxis {
        GEOMETRY,
        CORE,
        LAYERING,
        CADENCE,
        MOTION,
        TERMINAL
    }

    public record Rgb(String hex) {
        public Rgb {
            if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) {
                throw new IllegalArgumentException("RGB values must use #RRGGBB notation");
            }
        }

        public float red() {
            return channel(0);
        }

        public float green() {
            return channel(2);
        }

        public float blue() {
            return channel(4);
        }

        private float channel(int offset) {
            return Integer.parseInt(this.hex.substring(1 + offset, 3 + offset), 16) / 255.0F;
        }
    }

    public record Palette(Rgb primary, Rgb secondary, Rgb accent, boolean countsTowardCoolColorLedger) {
        public Palette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Large Extra palette layers must be distinct");
            }
        }

        public String signature() {
            return this.primary.hex() + "/" + this.secondary.hex() + "/" + this.accent.hex();
        }
    }

    /** The existing ordinary 3x3 rocket recipe remains a data contract; no recipe is emitted by this batch. */
    public record RecipeContract(List<String> pattern, java.util.Map<String, String> key, String resultId, int count) {
        public RecipeContract {
            pattern = List.copyOf(pattern);
            key = java.util.Map.copyOf(key);
            if (!pattern.equals(List.of(" P ", "FGF", " P "))
                    || !key.equals(java.util.Map.of(
                            "P", "minecraft:paper",
                            "F", "minecraft:firework_star",
                            "G", "minecraft:gunpowder"))
                    || resultId == null
                    || resultId.isBlank()
                    || count != 1) {
                throw new IllegalArgumentException("Large Extra entries must retain the ordinary 3x3 rocket recipe");
            }
        }
    }

    /** The integration owner appends these entries to Large Fireworks, never to midsize, other, or giant sections. */
    public record CreativeTarget(String sectionKey, String translationKey, int orderHint) {
        public CreativeTarget {
            if (!LARGE_CREATIVE_SECTION.equals(sectionKey)
                    || !LARGE_CREATIVE_TRANSLATION_KEY.equals(translationKey)
                    || orderHint <= 0) {
                throw new IllegalArgumentException("Large Extra creative target must be the Large Fireworks section");
            }
        }
    }

    /** Reuse-only bridge metadata. The shared dispatcher remains owned by the integration thread. */
    public record ReuseContract(
            String itemModelParent,
            String hdSparkParticleType,
            String vanillaFallbackParticleType,
            String recipeTemplate,
            String clientProgramId,
            EffectCategory effectCategory,
            boolean serverParticleLoop,
            boolean serverTrajectoryComputation,
            boolean addsNetworkPayload,
            boolean createsGlobalScheduler) {
        public ReuseContract {
            if (!MODEL_PARENT.equals(itemModelParent)
                    || !HD_SPARK.equals(hdSparkParticleType)
                    || !VANILLA_FIREWORK_PARTICLE.equals(vanillaFallbackParticleType)
                    || !RECIPE_TEMPLATE.equals(recipeTemplate)
                    || clientProgramId == null
                    || clientProgramId.isBlank()
                    || effectCategory != EffectCategory.STANDARD
                    || serverParticleLoop
                    || serverTrajectoryComputation
                    || addsNetworkPayload
                    || createsGlobalScheduler) {
                throw new IllegalArgumentException("Large Extra must stay client-only and reuse the approved contracts");
            }
        }
    }

    /** A per-effect finite cap, deliberately independent from any shared global particle limiter. */
    public record ParticleBudget(
            int emissionTicks,
            int particlesPerTick,
            int maxLiveParticles,
            int minLifetimeTicks,
            int maxLifetimeTicks) {
        public ParticleBudget {
            long planned = (long) emissionTicks * particlesPerTick;
            if (emissionTicks <= 0
                    || particlesPerTick <= 0
                    || particlesPerTick > MAX_LOCAL_PARTICLES_PER_TICK
                    || planned > MAX_LOCAL_LIVE_PARTICLES
                    || maxLiveParticles != planned
                    || minLifetimeTicks <= 0
                    || maxLifetimeTicks < minLifetimeTicks
                    || emissionTicks + maxLifetimeTicks > MAX_LOCAL_VISUAL_TICKS) {
                throw new IllegalArgumentException("Large Extra local particle budget is not finite and self-contained");
            }
        }

        public int plannedParticles() {
            return Math.toIntExact((long) this.emissionTicks * this.particlesPerTick);
        }

        public int totalVisualTicks() {
            return this.emissionTicks + this.maxLifetimeTicks;
        }
    }

    /** The client program clamps every sampled offset to the declared radius before a particle is created. */
    public record Envelope(
            int flightTicks,
            double burstCenterHeightBlocks,
            int nominalDiameterBlocks,
            int fullEnvelopeBlocks,
            double maximumRadiusBlocks,
            String proofContract) {
        public Envelope {
            if (flightTicks != CURRENT_LARGE_FLIGHT_TICKS
                    || Math.abs(burstCenterHeightBlocks - CURRENT_LARGE_BURST_HEIGHT_BLOCKS) > 1.0E-9D
                    || nominalDiameterBlocks <= 0
                    || fullEnvelopeBlocks < nominalDiameterBlocks
                    || fullEnvelopeBlocks > LARGE_MAXIMUM_FULL_ENVELOPE
                    || Math.abs(maximumRadiusBlocks * 2.0D - fullEnvelopeBlocks) > 1.0E-9D
                    || proofContract == null
                    || !proofContract.contains("clampToEnvelope")) {
                throw new IllegalArgumentException("Large Extra envelope must use the current large scale and clamp proof");
            }
        }
    }

    public record VisualDifference(
            String geometry,
            String core,
            String layering,
            String cadence,
            String motion,
            String terminal,
            String structureSignature,
            String description) {
        public VisualDifference {
            for (String value : axisValues(geometry, core, layering, cadence, motion, terminal)) {
                requireText(value, "visual axis");
            }
            requireText(description, "description");
            String computed = String.join(" | ", axisValues(geometry, core, layering, cadence, motion, terminal));
            if (!computed.equals(structureSignature)) {
                throw new IllegalArgumentException("Large Extra structure signature must encode all six visual axes");
            }
        }

        public List<String> axisValues() {
            return axisValues(this.geometry, this.core, this.layering, this.cadence, this.motion, this.terminal);
        }

        public String axisValue(VisualAxis axis) {
            return switch (axis) {
                case GEOMETRY -> this.geometry;
                case CORE -> this.core;
                case LAYERING -> this.layering;
                case CADENCE -> this.cadence;
                case MOTION -> this.motion;
                case TERMINAL -> this.terminal;
            };
        }

        public int differingAxisCount(VisualDifference other) {
            Objects.requireNonNull(other, "other");
            int different = 0;
            for (VisualAxis axis : VisualAxis.values()) {
                if (!axisValue(axis).equals(other.axisValue(axis))) {
                    different++;
                }
            }
            return different;
        }

        private static List<String> axisValues(
                String geometry, String core, String layering, String cadence, String motion, String terminal) {
            return List.of(geometry, core, layering, cadence, motion, terminal);
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
