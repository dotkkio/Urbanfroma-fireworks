package com.urbanforma.fireworks.content.saturn;

import java.util.ArrayList;
import java.util.List;

/** Ordered ring collection; an empty collection is valid for a sphere-only candidate. */
public record SaturnRingConfiguration(List<SaturnRingSpec> rings) {
    public SaturnRingConfiguration {
        if (rings == null) {
            throw new IllegalArgumentException("Ring collection must not be null");
        }
        rings = List.copyOf(rings);
        for (int position = 0; position < rings.size(); position++) {
            SaturnRingSpec ring = rings.get(position);
            if (ring == null || ring.index() != position) {
                throw new IllegalArgumentException("Ring indices must be contiguous and match collection order");
            }
        }
    }

    public static SaturnRingConfiguration concentric(
            int ringCount,
            double firstRadius,
            double spacing,
            double width,
            double tiltDegrees,
            int visualLayer,
            SaturnColorBand colorBand,
            int samplesPerRing,
            int startTick,
            int emissionTicks,
            int lifetimeTicks) {
        if (ringCount < 0 || (ringCount > 1 && spacing <= width)) {
            throw new IllegalArgumentException("Ring count and spacing must describe separated rings");
        }
        if (!Double.isFinite(firstRadius) || firstRadius <= 0.0D
                || !Double.isFinite(spacing) || spacing < 0.0D) {
            throw new IllegalArgumentException("Ring radius and spacing must be finite and non-negative");
        }

        List<SaturnRingSpec> result = new ArrayList<>(ringCount);
        for (int index = 0; index < ringCount; index++) {
            result.add(new SaturnRingSpec(
                    index,
                    firstRadius + index * spacing,
                    width,
                    tiltDegrees,
                    visualLayer,
                    colorBand,
                    samplesPerRing,
                    startTick,
                    emissionTicks,
                    lifetimeTicks));
        }
        return new SaturnRingConfiguration(result);
    }

    public int ringCount() {
        return rings.size();
    }
}
