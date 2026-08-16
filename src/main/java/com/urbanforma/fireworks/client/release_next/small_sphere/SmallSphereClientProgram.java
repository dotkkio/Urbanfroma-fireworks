package com.urbanforma.fireworks.client.release_next.small_sphere;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.small_sphere.SmallSphereDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;

/** Caller-driven client-only sphere trajectory. It owns no listener, queue, payload, or server work. */
public final class SmallSphereClientProgram {
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private final SmallSphereDefinition definition;
    private final Request request;
    private int age;
    private int createdParticles;

    public SmallSphereClientProgram(SmallSphereDefinition definition, Request request) {
        this.definition = definition;
        this.request = request;
    }

    /** Emits at most one fixed branch ring per call and ends after eight calls. */
    public boolean tick(Minecraft minecraft) {
        if (minecraft == null || age >= definition.clientPlan().layers()) return false;
        int layer = age++;
        for (int branch = 0; branch < definition.clientPlan().branches(); branch++) emit(minecraft, branch, layer);
        return age < definition.clientPlan().layers();
    }

    public int createdParticles() { return createdParticles; }
    public int plannedParticles() { return definition.clientPlan().totalParticles(); }
    public boolean isComplete() { return age >= definition.clientPlan().layers(); }

    private void emit(Minecraft minecraft, int branch, int layer) {
        double latitude = 1.0D - 2.0D * (branch + 0.5D) / definition.clientPlan().branches();
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - latitude * latitude));
        double phase = branch * GOLDEN_ANGLE + unit(request.seed() ^ definition.variant().ordinal());
        double progress = (layer + 1.0D) / definition.clientPlan().layers();
        double radius = definition.clientPlan().maxRadius() * progress * variantScale(phase, progress);
        double x = request.x() + horizontal * Math.cos(phase) * radius;
        double y = request.y() + latitude * radius;
        double z = request.z() + horizontal * Math.sin(phase) * radius;
        Particle particle = FireworkParticleAppearance.createSpark(minecraft, x, y, z, 0.0D, 0.0D, 0.0D);
        if (particle != null) {
            float[] color = color(layer);
            FireworkParticleAppearance.applyVividColor(particle, color[0], color[1], color[2], 1.0F, layer == 0 ? 0.16F : 0.05F);
            FireworkParticleAppearance.applyVisibilityScale(particle, layer == 0 ? 1.05F : 0.84F, layer == 0);
            particle.setLifetime(18 + (int) (unit(request.seed() + branch * 31L + layer) * 10.0D));
            createdParticles++;
        }
    }

    private double variantScale(double phase, double progress) {
        return switch (definition.variant()) {
            case HOLLOW_LANTERN, EQUATORIAL_ORBIT, CRYSTAL_GRID -> 0.82D + 0.18D * Math.abs(Math.sin(phase));
            case PETAL_WEAVE, RIPPLE_SPHERE -> 0.86D + 0.14D * Math.sin(phase * 3.0D + progress * Math.PI);
            case SPIRAL_LATITUDE, AURORA_VEIL -> 0.88D + 0.12D * Math.sin(phase + progress * Math.PI * 3.0D);
            case OFFSET_HEMISPHERES -> 0.90D + 0.10D * Math.cos(phase * 2.0D);
            default -> 1.0D;
        };
    }

    private float[] color(int layer) {
        String hex = layer == 0 ? definition.palette().accent() : layer < 4 ? definition.palette().secondary() : definition.palette().primary();
        return new float[] { Integer.parseInt(hex.substring(1, 3), 16) / 255.0F, Integer.parseInt(hex.substring(3, 5), 16) / 255.0F, Integer.parseInt(hex.substring(5, 7), 16) / 255.0F };
    }

    private static double unit(long value) { return ((value ^ value >>> 33) & 0xFFFF) / 65535.0D; }

    public record Request(double x, double y, double z, long seed) {
        public Request { if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Request position must be finite"); }
    }
}
