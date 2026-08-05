package com.urbanforma.fireworks.content.saturn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Deterministic geometry expansion for sphere layers and inclined annuli. */
public final class SaturnGeometry {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long RING_PHASE_SALT = 0x9E3779B97F4A7C15L;
    private static final long RING_RADIUS_SALT = 0xD6E8FEB86659FD93L;

    private static final Comparator<Sample> VISUAL_ORDER = Comparator
            .comparingInt(Sample::visualLayer)
            .thenComparingInt(sample -> sample.kind() == Kind.SPHERE ? 0 : 1)
            .thenComparingInt(Sample::sourceIndex)
            .thenComparingInt(Sample::sampleIndex);

    private final SaturnProgram program;

    public SaturnGeometry(SaturnProgram program) {
        this.program = program;
    }

    public SaturnProgram program() {
        return program;
    }

    /** Returns every sample exactly once in stable sphere-before-ring visual order. */
    public List<Sample> allSamples(long seed) {
        List<Sample> samples = new ArrayList<>(program.totalSampleCount());
        for (int sphereIndex = 0; sphereIndex < program.spheres().size(); sphereIndex++) {
            SaturnSphereLayer sphere = program.spheres().get(sphereIndex);
            for (int sampleIndex = 0; sampleIndex < sphere.sampleCount(); sampleIndex++) {
                samples.add(sphereSample(seed, sphere, sphereIndex, sampleIndex));
            }
        }
        for (SaturnRingSpec ring : program.rings().rings()) {
            for (int sampleIndex = 0; sampleIndex < ring.sampleCount(); sampleIndex++) {
                samples.add(ringSample(seed, ring, sampleIndex));
            }
        }
        samples.sort(VISUAL_ORDER);
        return List.copyOf(samples);
    }

    /** Returns the samples due on one tick without invoking any client scheduler. */
    public List<Sample> samplesAtTick(long seed, int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick must not be negative");
        }
        List<Sample> samples = new ArrayList<>(program.peakSamplesPerTick());
        for (int sphereIndex = 0; sphereIndex < program.spheres().size(); sphereIndex++) {
            SaturnSphereLayer sphere = program.spheres().get(sphereIndex);
            int from = sampleFrom(sphere.sampleCount(), sphere.startTick(), sphere.emissionTicks(), tick);
            int to = sampleTo(sphere.sampleCount(), sphere.startTick(), sphere.emissionTicks(), tick);
            for (int sampleIndex = from; sampleIndex < to; sampleIndex++) {
                samples.add(sphereSample(seed, sphere, sphereIndex, sampleIndex));
            }
        }
        for (SaturnRingSpec ring : program.rings().rings()) {
            int from = sampleFrom(ring.sampleCount(), ring.startTick(), ring.emissionTicks(), tick);
            int to = sampleTo(ring.sampleCount(), ring.startTick(), ring.emissionTicks(), tick);
            for (int sampleIndex = from; sampleIndex < to; sampleIndex++) {
                samples.add(ringSample(seed, ring, sampleIndex));
            }
        }
        samples.sort(VISUAL_ORDER);
        return List.copyOf(samples);
    }

    public Bounds bounds(long seed) {
        List<Sample> samples = allSamples(seed);
        if (samples.isEmpty()) {
            return new Bounds(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (Sample sample : samples) {
            Vec3 position = sample.position();
            minX = Math.min(minX, position.x);
            minY = Math.min(minY, position.y);
            minZ = Math.min(minZ, position.z);
            maxX = Math.max(maxX, position.x);
            maxY = Math.max(maxY, position.y);
            maxZ = Math.max(maxZ, position.z);
        }
        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** Seed-independent AABB proof; an inclined ring never exceeds its outer radius on any axis. */
    public Bounds conservativeBounds() {
        double extent = 0.0D;
        for (SaturnSphereLayer sphere : program.spheres()) {
            extent = Math.max(extent, sphere.radius());
        }
        for (SaturnRingSpec ring : program.rings().rings()) {
            extent = Math.max(extent, ring.outerRadius());
        }
        return new Bounds(-extent, -extent, -extent, extent, extent, extent);
    }

    public static int sampleFrom(int sampleCount, int startTick, int emissionTicks, int tick) {
        if (tick < startTick || tick >= (long) startTick + emissionTicks) {
            return 0;
        }
        return (int) ((long) sampleCount * (tick - startTick) / emissionTicks);
    }

    public static int sampleTo(int sampleCount, int startTick, int emissionTicks, int tick) {
        if (tick < startTick || tick >= (long) startTick + emissionTicks) {
            return 0;
        }
        return (int) ((long) sampleCount * (tick - startTick + 1L) / emissionTicks);
    }

    private static Sample sphereSample(
            long seed, SaturnSphereLayer sphere, int sphereIndex, int sampleIndex) {
        Vec3 position = sphere.position(seed, sampleIndex);
        return new Sample(
                Kind.SPHERE,
                sphere.id(),
                sphereIndex,
                sampleIndex,
                sphere.visualLayer(),
                sphere.colorBand(),
                position,
                position.normalize(),
                1.0D,
                sphere.lifetimeTicks());
    }

    private static Sample ringSample(long seed, SaturnRingSpec ring, int sampleIndex) {
        RingPoint point = ringPoint(seed, ring, sampleIndex);
        return new Sample(
                Kind.RING,
                "ring_" + ring.index(),
                ring.index(),
                sampleIndex,
                ring.visualLayer(),
                ring.colorBand(),
                point.position(),
                point.normal(),
                point.radialProgress(),
                ring.lifetimeTicks());
    }

    private static RingPoint ringPoint(long seed, SaturnRingSpec ring, int sampleIndex) {
        long ringSeed = mix64(seed ^ RING_PHASE_SALT ^ ((long) ring.index() * RING_RADIUS_SALT));
        double phase = randomUnit(ringSeed) * TWO_PI;
        long sampleSeed = mix64(ringSeed ^ ((long) sampleIndex * RING_RADIUS_SALT));
        double radial = ring.innerRadius() + ring.width() * randomUnit(sampleSeed);
        double angle = phase + TWO_PI * sampleIndex / ring.sampleCount();
        double localX = Math.cos(angle) * radial;
        double localZ = Math.sin(angle) * radial;
        double tilt = Math.toRadians(ring.tiltDegrees());
        double cosine = Math.cos(tilt);
        double sine = Math.sin(tilt);
        Vec3 position = new Vec3(localX, -localZ * sine, localZ * cosine);
        Vec3 normal = new Vec3(0.0D, cosine, sine);
        return new RingPoint(position, normal, (radial - ring.innerRadius()) / ring.width());
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private record RingPoint(Vec3 position, Vec3 normal, double radialProgress) {
    }

    public enum Kind {
        SPHERE,
        RING
    }

    public record Sample(
            Kind kind,
            String sourceId,
            int sourceIndex,
            int sampleIndex,
            int visualLayer,
            SaturnColorBand colorBand,
            Vec3 position,
            Vec3 normal,
            double radialProgress,
            int lifetimeTicks) {
        public Sample {
            if (kind == null || sourceId == null || sourceId.isBlank() || sourceIndex < 0 || sampleIndex < 0
                    || visualLayer < 0 || colorBand == null || position == null || normal == null
                    || !Double.isFinite(radialProgress) || radialProgress < 0.0D || radialProgress > 1.0D
                    || lifetimeTicks <= 0) {
                throw new IllegalArgumentException("Invalid Saturn geometry sample");
            }
        }
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!finite(minX) || !finite(minY) || !finite(minZ) || !finite(maxX) || !finite(maxY) || !finite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Saturn bounds must be finite and ordered");
            }
        }

        public double spanX() {
            return maxX - minX;
        }

        public double spanY() {
            return maxY - minY;
        }

        public double spanZ() {
            return maxZ - minZ;
        }

        public double maxSpan() {
            return Math.max(spanX(), Math.max(spanY(), spanZ()));
        }

        public boolean fitsWithin(double diameter) {
            return Double.isFinite(diameter) && diameter > 0.0D && maxSpan() <= diameter + 1.0E-9D;
        }

        private static boolean finite(double value) {
            return Double.isFinite(value);
        }
    }
}
