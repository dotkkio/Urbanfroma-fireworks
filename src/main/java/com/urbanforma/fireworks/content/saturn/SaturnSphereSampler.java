package com.urbanforma.fireworks.content.saturn;

import net.minecraft.world.phys.Vec3;

/** Supplies a deterministic direction for one point on a spherical base. */
@FunctionalInterface
public interface SaturnSphereSampler {
    /**
     * Returns a direction for the requested sample. The geometry layer normalizes it before applying its radius,
     * so samplers may return any non-zero finite vector.
     */
    Vec3 sample(long seed, int sampleIndex, int sampleCount);
}
