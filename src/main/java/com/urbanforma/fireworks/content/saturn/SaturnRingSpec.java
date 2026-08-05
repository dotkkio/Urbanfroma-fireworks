package com.urbanforma.fireworks.content.saturn;

import java.util.Objects;

/** Immutable geometry and emission settings for one annular layer. */
public record SaturnRingSpec(
        int index,
        double radius,
        double width,
        double tiltDegrees,
        int visualLayer,
        SaturnColorBand colorBand,
        int sampleCount,
        int startTick,
        int emissionTicks,
        int lifetimeTicks) {
    public SaturnRingSpec {
        if (index < 0) {
            throw new IllegalArgumentException("Ring index must not be negative");
        }
        if (!Double.isFinite(radius) || radius <= 0.0D
                || !Double.isFinite(width) || width <= 0.0D || width >= radius * 2.0D) {
            throw new IllegalArgumentException("Ring radius and width must describe a finite annulus");
        }
        if (!Double.isFinite(tiltDegrees) || Math.abs(tiltDegrees) > 180.0D) {
            throw new IllegalArgumentException("Ring tilt must be between -180 and 180 degrees");
        }
        if (visualLayer < 0 || sampleCount < 3 || startTick < 0 || emissionTicks <= 0 || lifetimeTicks <= 0) {
            throw new IllegalArgumentException("Ring counts and timing must be positive");
        }
        Objects.requireNonNull(colorBand, "colorBand");
    }

    public double innerRadius() {
        return radius - width * 0.5D;
    }

    public double outerRadius() {
        return radius + width * 0.5D;
    }
}
