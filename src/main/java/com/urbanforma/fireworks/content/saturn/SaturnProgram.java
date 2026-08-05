package com.urbanforma.fireworks.content.saturn;

import com.urbanforma.fireworks.content.FireworkStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Complete data contract for one unregistered Saturn-ring effect. */
public record SaturnProgram(
        List<SaturnSphereLayer> spheres,
        SaturnRingConfiguration rings,
        SaturnPalette palette,
        SaturnParticleBudget budget) {
    public SaturnProgram {
        if (spheres == null || spheres.isEmpty()) {
            throw new IllegalArgumentException("A Saturn program requires at least one sphere layer");
        }
        spheres = List.copyOf(spheres);
        Set<String> ids = new HashSet<>();
        double previousRadius = Double.POSITIVE_INFINITY;
        for (SaturnSphereLayer sphere : spheres) {
            if (sphere == null) {
                throw new IllegalArgumentException("Sphere layer must not be null");
            }
            if (!ids.add(sphere.id())) {
                throw new IllegalArgumentException("Sphere layer ids must be non-null and unique");
            }
            if (sphere.radius() >= previousRadius) {
                throw new IllegalArgumentException("Sphere layers must be ordered outermost to innermost");
            }
            previousRadius = sphere.radius();
        }
        Objects.requireNonNull(rings, "rings");
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(budget, "budget");
        if (peakSamplesPerTick(spheres, rings) > budget.maxPerTick()) {
            throw new IllegalArgumentException("Configured Saturn layers exceed the particle budget per tick");
        }
        if (totalSampleCount(spheres, rings) > budget.maxOwnedParticles()) {
            throw new IllegalArgumentException("Configured Saturn samples exceed the owned particle budget");
        }
    }

    /** First integrated candidate: a full outer sphere, a retained inner core, and four inclined rings. */
    public static SaturnProgram prototype(FireworkStyle style) {
        Objects.requireNonNull(style, "style");
        SaturnSphereSampler fibonacci = (seed, sampleIndex, sampleCount) -> {
            double y = 1.0D - 2.0D * ((double) sampleIndex + 0.5D) / sampleCount;
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double angle = sampleIndex * Math.PI * (3.0D - Math.sqrt(5.0D))
                    + (seed & 0xFFFFL) * 0.0001D;
            return new net.minecraft.world.phys.Vec3(
                    horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
        };
        return new SaturnProgram(
                List.of(
                        new SaturnSphereLayer(
                                "outer",
                                52.5D,
                                1_920,
                                0,
                                SaturnColorBand.PRIMARY,
                                0,
                                12,
                                96,
                                fibonacci),
                        new SaturnSphereLayer(
                                "inner",
                                24.0D,
                                480,
                                1,
                                SaturnColorBand.SECONDARY,
                                12,
                                6,
                                80,
                                fibonacci)),
                SaturnRingConfiguration.concentric(
                        4,
                        58.0D,
                        8.0D,
                        4.0D,
                        18.0D,
                        2,
                        SaturnColorBand.ACCENT,
                        160,
                        0,
                        20,
                        108),
                new SaturnPalette(style.primaryColor(), style.secondaryColor(), style.accentColor()),
                new SaturnParticleBudget(480, 4_000));
    }

    public int totalSampleCount() {
        int total = 0;
        for (SaturnSphereLayer sphere : spheres) {
            total = Math.addExact(total, sphere.sampleCount());
        }
        for (SaturnRingSpec ring : rings.rings()) {
            total = Math.addExact(total, ring.sampleCount());
        }
        return total;
    }

    public int totalTicks() {
        int end = 0;
        for (SaturnSphereLayer sphere : spheres) {
            end = Math.max(end, Math.addExact(sphere.startTick(), sphere.emissionTicks()));
        }
        for (SaturnRingSpec ring : rings.rings()) {
            end = Math.max(end, Math.addExact(ring.startTick(), ring.emissionTicks()));
        }
        return end;
    }

    public int peakSamplesPerTick() {
        return peakSamplesPerTick(spheres, rings);
    }

    private static int peakSamplesPerTick(List<SaturnSphereLayer> spheres, SaturnRingConfiguration rings) {
        int peak = 0;
        int end = 0;
        for (SaturnSphereLayer sphere : spheres) {
            end = Math.max(end, Math.addExact(sphere.startTick(), sphere.emissionTicks()));
        }
        for (SaturnRingSpec ring : rings.rings()) {
            end = Math.max(end, Math.addExact(ring.startTick(), ring.emissionTicks()));
        }
        for (int tick = 0; tick < end; tick++) {
            int count = 0;
            for (SaturnSphereLayer sphere : spheres) {
                count = Math.addExact(count, samplesAtTick(
                        sphere.sampleCount(), sphere.startTick(), sphere.emissionTicks(), tick));
            }
            for (SaturnRingSpec ring : rings.rings()) {
                count = Math.addExact(count, samplesAtTick(
                        ring.sampleCount(), ring.startTick(), ring.emissionTicks(), tick));
            }
            peak = Math.max(peak, count);
        }
        return peak;
    }

    private static int totalSampleCount(List<SaturnSphereLayer> spheres, SaturnRingConfiguration rings) {
        int total = 0;
        for (SaturnSphereLayer sphere : spheres) {
            total = Math.addExact(total, sphere.sampleCount());
        }
        for (SaturnRingSpec ring : rings.rings()) {
            total = Math.addExact(total, ring.sampleCount());
        }
        return total;
    }

    public static int samplesAtTick(int sampleCount, int startTick, int emissionTicks, int tick) {
        int localTick = tick - startTick;
        if (localTick < 0 || localTick >= emissionTicks) {
            return 0;
        }
        int from = (int) ((long) sampleCount * localTick / emissionTicks);
        int to = (int) ((long) sampleCount * (localTick + 1) / emissionTicks);
        return to - from;
    }

    public record SaturnPalette(
            FireworkStyle.Rgb primary,
            FireworkStyle.Rgb secondary,
            FireworkStyle.Rgb accent) {
        public SaturnPalette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
            if (primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Saturn palette requires three distinct RGB colors");
            }
        }

        public FireworkStyle.Rgb color(SaturnColorBand band) {
            return switch (Objects.requireNonNull(band, "band")) {
                case PRIMARY -> primary;
                case SECONDARY -> secondary;
                case ACCENT -> accent;
            };
        }
    }

    public record SaturnParticleBudget(int maxPerTick, int maxOwnedParticles) {
        public SaturnParticleBudget {
            if (maxPerTick <= 0 || maxOwnedParticles < maxPerTick) {
                throw new IllegalArgumentException("Particle budget must be positive and internally ordered");
            }
        }
    }
}
