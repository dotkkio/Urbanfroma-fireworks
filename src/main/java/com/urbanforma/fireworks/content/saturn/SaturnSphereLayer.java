package com.urbanforma.fireworks.content.saturn;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/** One spherical shell or inner sphere in a Saturn-style effect. */
public record SaturnSphereLayer(
        String id,
        double radius,
        int sampleCount,
        int visualLayer,
        SaturnColorBand colorBand,
        int startTick,
        int emissionTicks,
        int lifetimeTicks,
        SaturnSphereSampler sampler) {
    public SaturnSphereLayer {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Sphere layer id must not be blank");
        }
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            throw new IllegalArgumentException("Sphere layer radius must be finite and positive");
        }
        if (sampleCount <= 0 || visualLayer < 0 || startTick < 0 || emissionTicks <= 0 || lifetimeTicks <= 0) {
            throw new IllegalArgumentException("Sphere layer counts and timing must be positive");
        }
        Objects.requireNonNull(colorBand, "colorBand");
        Objects.requireNonNull(sampler, "sampler");
    }

    public Vec3 position(long seed, int sampleIndex) {
        if (sampleIndex < 0 || sampleIndex >= sampleCount) {
            throw new IllegalArgumentException("Sphere sample index is outside the configured sample count");
        }
        Vec3 direction = sampler.sample(seed, sampleIndex, sampleCount);
        if (direction == null || !finite(direction) || direction.lengthSqr() < 1.0E-12D) {
            throw new IllegalArgumentException("Sphere sampler returned an invalid direction for " + id);
        }
        return direction.normalize().scale(radius);
    }

    private static boolean finite(Vec3 value) {
        return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
    }
}
