package com.urbanforma.fireworks.content.hybrid;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.RadiantTrajectory;
import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * Deterministic geometry for the unregistered sphere-plus-radiant prototype.
 *
 * <p>The sphere is deliberately smaller than the radiant layer: the outer sphere reaches about 30 blocks,
 * while the existing radiant branches reach 48 blocks before their conservative motion allowance. The
 * radial layer reuses the existing 160 Fibonacci branches and samples twelve points on each branch, so every
 * branch begins inside the sphere and then crosses its outer surface into the larger radiant envelope. The 240-node
 * core starts with the outer shell so the visual center is present during the first emission window.</p>
 */
public final class HybridSphereRadiantTrajectory {
    public static final int SPHERE_OUTER_COUNT = 1_920;
    public static final int SPHERE_CORE_COUNT = 240;
    public static final int SPHERE_TOTAL_COUNT = SPHERE_OUTER_COUNT + SPHERE_CORE_COUNT;
    public static final int SPHERE_OUTER_PER_TICK = 180;
    public static final int SPHERE_CORE_PER_TICK = 120;
    public static final int SPHERE_OUTER_EMISSION_TICKS =
            ceilDiv(SPHERE_OUTER_COUNT, SPHERE_OUTER_PER_TICK);
    public static final int SPHERE_CORE_EMISSION_TICKS =
            ceilDiv(SPHERE_CORE_COUNT, SPHERE_CORE_PER_TICK);
    public static final int SPHERE_CORE_START_TICK = 0;

    /** Twelve evenly spaced radiant samples keep the full 160-branch silhouette readable. */
    private static final int[] RADIAL_SEGMENTS = {3, 5, 7, 9, 12, 15, 18, 21, 23, 25, 27, 29};
    public static final int RADIAL_BRANCH_COUNT = RadiantTrajectory.BRANCH_COUNT;
    public static final int RADIAL_NODES_PER_RING = RADIAL_BRANCH_COUNT;
    public static final int RADIAL_RING_COUNT = RADIAL_SEGMENTS.length;
    public static final int RADIAL_NODE_COUNT = RADIAL_NODES_PER_RING * RADIAL_RING_COUNT;

    /** The radial rings start with the first tick and share the same budget window as the sphere shell. */
    public static final int RADIAL_START_TICK = 0;
    public static final int TOTAL_EMISSION_TICKS = Math.max(
            Math.max(SPHERE_OUTER_EMISSION_TICKS,
                    SPHERE_CORE_START_TICK + SPHERE_CORE_EMISSION_TICKS),
            RADIAL_START_TICK + RADIAL_RING_COUNT);

    public static final int MAX_EMISSION_PER_TICK =
            SPHERE_OUTER_PER_TICK + SPHERE_CORE_PER_TICK + RADIAL_NODES_PER_RING;
    public static final int MAX_LIVE_PARTICLES = SPHERE_TOTAL_COUNT + RADIAL_NODE_COUNT;

    /** Nominal travel radii after the vanilla SparkParticle friction is applied. */
    public static final double SPHERE_OUTER_RADIUS = 30.0D;
    public static final double SPHERE_CORE_RADIUS = 13.0D;
    public static final double SPHERE_DIAMETER = SPHERE_OUTER_RADIUS * 2.0D;
    public static final double RADIAL_OUTER_RADIUS = RadiantTrajectory.MAX_RADIUS;
    /** The radial conservative envelope also covers sway and tangent motion. */
    public static final double RADIAL_OUTER_ENVELOPE_RADIUS = 52.65D;
    /** The union remains bounded by the larger radial layer, not by the sphere. */
    public static final double APPROVED_FULL_ENVELOPE = 112.0D;
    /** 0.91 vanilla spark friction maps these speeds to the two smaller spherical layers. */
    public static final double OUTER_SPEED = 2.70D;
    public static final double CORE_SPEED = 1.17D;
    public static final int OUTER_LIFETIME_MIN = 82;
    public static final int OUTER_LIFETIME_MAX = 102;
    public static final int CORE_LIFETIME_MIN = 64;
    public static final int CORE_LIFETIME_MAX = 80;
    public static final int MAX_PARTICLE_LIFETIME = Math.max(OUTER_LIFETIME_MAX, RadiantTrajectory.STAR_LIFETIME_MAX);

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double SPHERE_PHASE_JITTER = 0.028D;
    private static final long SPHERE_PHASE_SALT = 0xD6E8FEB86659FD93L;
    private static final long SPHERE_JITTER_SALT = 0x13198A2E03707344L;
    private static final long SPHERE_LIFETIME_SALT = 0x243F6A8885A308D3L;
    private static final long SPHERE_TONE_SALT = 0x3BD39E10CB0EF593L;
    private static final long SPHERE_TWINKLE_SALT = 0xC0AC29B7C97C50DDL;
    private static final long CORE_LAYER_SALT = 0xB7E151628AED2A6BL;

    private HybridSphereRadiantTrajectory() {
    }

    public enum SphereLayer {
        OUTER,
        CORE
    }

    /** One deterministic spherical spark; the client adds the burst origin when creating the particle. */
    public record SphereNode(
            SphereLayer layer,
            int index,
            Vec3 direction,
            double speed,
            int lifetime,
            RadiantTrajectory.ColorBand colorBand,
            float colorTone,
            float twinklePhase) {
    }

    /** One selected node from an existing deterministic radiant branch. */
    public record RadialNode(
            int branchIndex,
            int ringIndex,
            int radiantSegmentIndex,
            RadiantTrajectory.BranchSample sample) {
        public RadialNode {
            Objects.requireNonNull(sample, "sample");
        }
    }

    /** Exact work assigned to one client tick; counts include every requested particle allocation. */
    public record EmissionFrame(
            int tick,
            int sphereOuterStart,
            int sphereOuterCount,
            int sphereCoreStart,
            int sphereCoreCount,
            int radialRingIndex,
            int radialCount,
            int totalCount) {
        public EmissionFrame {
            if (tick < 0 || sphereOuterStart < 0 || sphereOuterCount < 0
                    || sphereCoreStart < 0 || sphereCoreCount < 0 || radialRingIndex < -1
                    || radialCount < 0 || totalCount < 0
                    || sphereOuterCount + sphereCoreCount + radialCount != totalCount) {
                throw new IllegalArgumentException("Invalid hybrid emission frame");
            }
        }
    }

    /** Conservative local bounds for the union of the accepted sphere envelope and radial geometry. */
    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Hybrid bounds must be finite and ordered");
            }
        }

        public double spanX() {
            return this.maxX - this.minX;
        }

        public double spanY() {
            return this.maxY - this.minY;
        }

        public double spanZ() {
            return this.maxZ - this.minZ;
        }

        public double maxSpan() {
            return Math.max(this.spanX(), Math.max(this.spanY(), this.spanZ()));
        }

        public boolean fitsWithin(double envelope) {
            return envelope > 0.0D && this.maxSpan() <= envelope + 1.0E-9D;
        }
    }

    /** Returns one seeded spherical node while preserving the existing outer/core count split. */
    public static SphereNode sphereNode(long payloadSeed, SphereLayer layer, int index) {
        Objects.requireNonNull(layer, "layer");
        int count = layer == SphereLayer.OUTER ? SPHERE_OUTER_COUNT : SPHERE_CORE_COUNT;
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Sphere node index is outside its layer");
        }

        long layerSalt = layer == SphereLayer.OUTER ? 0L : CORE_LAYER_SALT;
        long elementSeed = mix64(payloadSeed ^ layerSalt ^ ((long) index * 0x9E3779B97F4A7C15L));
        double phase = unit(mix64(payloadSeed ^ SPHERE_PHASE_SALT)) * TWO_PI;
        double y = 1.0D - 2.0D * ((double) index + 0.5D) / count;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double angle = index * GOLDEN_ANGLE + phase
                + centered(elementSeed, SPHERE_JITTER_SALT) * SPHERE_PHASE_JITTER;
        Vec3 direction = new Vec3(horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
        boolean outer = layer == SphereLayer.OUTER;
        int lifetime = outer
                ? randomInt(elementSeed ^ SPHERE_LIFETIME_SALT, OUTER_LIFETIME_MIN, OUTER_LIFETIME_MAX)
                : randomInt(elementSeed ^ SPHERE_LIFETIME_SALT, CORE_LIFETIME_MIN, CORE_LIFETIME_MAX);
        float tone = (float) unit(elementSeed ^ SPHERE_TONE_SALT);
        return new SphereNode(
                layer,
                index,
                direction,
                outer ? OUTER_SPEED : CORE_SPEED,
                lifetime,
                sphereColorBand(layer, index),
                tone,
                (float) unit(elementSeed ^ SPHERE_TWINKLE_SALT));
    }

    /** Reuses the shared deterministic radiant branch generator without adding a second branch implementation. */
    public static RadiantTrajectory.Branch radialBranch(
            FireworkStyle.RadiantProfile profile, long payloadSeed, int branchIndex) {
        return RadiantTrajectory.branch(profile, payloadSeed, branchIndex);
    }

    /** Returns one of the twelve selected samples on a complete 160-branch radiant ring. */
    public static RadialNode radialNode(
            FireworkStyle.RadiantProfile profile, long payloadSeed, int branchIndex, int ringIndex) {
        if (ringIndex < 0 || ringIndex >= RADIAL_RING_COUNT) {
            throw new IllegalArgumentException("Hybrid radial ring index is outside the selected combination");
        }
        if (branchIndex < 0 || branchIndex >= RADIAL_BRANCH_COUNT) {
            throw new IllegalArgumentException("Hybrid radial branch index is outside the branch count");
        }
        RadiantTrajectory.Branch branch = radialBranch(profile, payloadSeed, branchIndex);
        return radialNode(profile, branch, ringIndex);
    }

    /** Samples a cached deterministic branch without rebuilding its seeded parameters. */
    public static RadialNode radialNode(
            FireworkStyle.RadiantProfile profile, RadiantTrajectory.Branch branch, int ringIndex) {
        Objects.requireNonNull(branch, "branch");
        if (ringIndex < 0 || ringIndex >= RADIAL_RING_COUNT) {
            throw new IllegalArgumentException("Hybrid radial ring index is outside the selected combination");
        }
        if (branch.index() < 0 || branch.index() >= RADIAL_BRANCH_COUNT) {
            throw new IllegalArgumentException("Hybrid radial branch index is outside the branch count");
        }
        int segment = RADIAL_SEGMENTS[ringIndex];
        return new RadialNode(
                branch.index(),
                ringIndex,
                segment,
                RadiantTrajectory.sample(profile, branch, segment));
    }

    public static int radialSegment(int ringIndex) {
        if (ringIndex < 0 || ringIndex >= RADIAL_RING_COUNT) {
            throw new IllegalArgumentException("Hybrid radial ring index is outside the selected combination");
        }
        return RADIAL_SEGMENTS[ringIndex];
    }

    /** Returns true when the selected deterministic branch has samples on both sides of the sphere surface. */
    public static boolean radialPathCrossesSphereRadius(
            FireworkStyle.RadiantProfile profile, long payloadSeed, int branchIndex) {
        boolean hasInnerSample = false;
        boolean hasOuterSample = false;
        for (int ringIndex = 0; ringIndex < RADIAL_RING_COUNT; ringIndex++) {
            double distance = radialNode(profile, payloadSeed, branchIndex, ringIndex)
                    .sample().position().length();
            hasInnerSample |= distance <= SPHERE_OUTER_RADIUS;
            hasOuterSample |= distance >= SPHERE_OUTER_RADIUS;
        }
        return hasInnerSample && hasOuterSample;
    }

    /** Static proof that all 160 deterministic branches visibly pass through the smaller sphere. */
    public static boolean allRadialPathsCrossSphereRadius(
            FireworkStyle.RadiantProfile profile, long payloadSeed) {
        for (int branchIndex = 0; branchIndex < RADIAL_BRANCH_COUNT; branchIndex++) {
            if (!radialPathCrossesSphereRadius(profile, payloadSeed, branchIndex)) {
                return false;
            }
        }
        return true;
    }

    /** Describes the overlap schedule: sphere outer/core work starts together, radial rings start immediately. */
    public static EmissionFrame emissionFrame(int tick) {
        if (tick < 0 || tick >= TOTAL_EMISSION_TICKS) {
            throw new IllegalArgumentException("Hybrid emission tick is outside the prototype lifetime");
        }
        int outerStart = Math.min(SPHERE_OUTER_COUNT, tick * SPHERE_OUTER_PER_TICK);
        int outerCount = Math.min(SPHERE_OUTER_PER_TICK, SPHERE_OUTER_COUNT - outerStart);
        int coreTick = tick - SPHERE_CORE_START_TICK;
        int coreStart = coreTick < 0
                ? 0
                : Math.min(SPHERE_CORE_COUNT, coreTick * SPHERE_CORE_PER_TICK);
        int coreCount = coreTick < 0
                ? 0
                : Math.min(SPHERE_CORE_PER_TICK, SPHERE_CORE_COUNT - coreStart);
        int radialRing = tick - RADIAL_START_TICK;
        int radialCount = radialRing >= 0 && radialRing < RADIAL_RING_COUNT
                ? RADIAL_NODES_PER_RING
                : 0;
        if (radialCount == 0) {
            radialRing = -1;
        }
        return new EmissionFrame(
                tick,
                outerStart,
                outerCount,
                coreStart,
                coreCount,
                radialRing,
                radialCount,
                outerCount + coreCount + radialCount);
    }

    public static boolean isCompleteAt(int tick) {
        return tick >= TOTAL_EMISSION_TICKS;
    }

    public static int maxEmissionPerTick() {
        int maximum = 0;
        for (int tick = 0; tick < TOTAL_EMISSION_TICKS; tick++) {
            maximum = Math.max(maximum, emissionFrame(tick).totalCount());
        }
        return maximum;
    }

    /** Unions the smaller spherical envelope with the larger radiant proof bound. */
    public static Bounds conservativeBounds(FireworkStyle.RadiantProfile profile, long payloadSeed) {
        RadiantTrajectory.Bounds radial = RadiantTrajectory.conservativeBounds(profile, payloadSeed);
        double sphereRadius = SPHERE_DIAMETER * 0.5D;
        return new Bounds(
                Math.min(-sphereRadius, radial.minX()),
                Math.min(-sphereRadius, radial.minY()),
                Math.min(-sphereRadius, radial.minZ()),
                Math.max(sphereRadius, radial.maxX()),
                Math.max(sphereRadius, radial.maxY()),
                Math.max(sphereRadius, radial.maxZ()));
    }

    public static boolean fitsEnvelope(FireworkStyle.RadiantProfile profile, long payloadSeed) {
        return conservativeBounds(profile, payloadSeed).fitsWithin(APPROVED_FULL_ENVELOPE);
    }

    private static RadiantTrajectory.ColorBand sphereColorBand(SphereLayer layer, int index) {
        int paletteSlot = index % 1_000;
        if (layer == SphereLayer.CORE) {
            return paletteSlot < 650
                    ? RadiantTrajectory.ColorBand.SECONDARY
                    : RadiantTrajectory.ColorBand.ACCENT;
        }
        if (paletteSlot < 640) {
            return RadiantTrajectory.ColorBand.PRIMARY;
        }
        return paletteSlot < 900
                ? RadiantTrajectory.ColorBand.SECONDARY
                : RadiantTrajectory.ColorBand.ACCENT;
    }

    private static int ceilDiv(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static int randomInt(long seed, int minInclusive, int maxInclusive) {
        return minInclusive + (int) Math.floor(unit(seed) * (maxInclusive - minInclusive + 1));
    }

    private static double centered(long seed, long salt) {
        return unit(mix64(seed ^ salt)) - 0.5D;
    }

    private static double unit(long seed) {
        return (mix64(seed) >>> 11) * 0x1.0p-53D;
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
