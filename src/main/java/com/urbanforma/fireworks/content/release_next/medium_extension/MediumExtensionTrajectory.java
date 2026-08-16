package com.urbanforma.fireworks.content.release_next.medium_extension;

import net.minecraft.world.phys.Vec3;

/** Deterministic, client-consumed geometry only. It owns no server tick or global scheduler state. */
public final class MediumExtensionTrajectory {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

    private MediumExtensionTrajectory() {}

    public static Sample sample(MediumExtensionDefinition definition, long seed, int emissionTick, int ordinal) {
        if (definition == null || emissionTick < 0 || emissionTick >= definition.emissionTicks()
                || ordinal < 0 || ordinal >= definition.particlesPerTick()) {
            throw new IllegalArgumentException("Medium extension sample is outside its finite plan");
        }
        int index = emissionTick * definition.particlesPerTick() + ordinal;
        double progress = (index + 0.5D) / definition.totalParticles();
        double azimuth = GOLDEN_ANGLE * index + unit(seed ^ index) * 0.25D;
        double polar = Math.acos(1.0D - 2.0D * progress);
        Vec3 direction = new Vec3(Math.sin(polar) * Math.cos(azimuth), Math.cos(polar), Math.sin(polar) * Math.sin(azimuth));
        double radius = definition.maximumRadius() * radialProgress(definition.category(), progress, emissionTick, definition);
        Vec3 position = shape(definition, direction, azimuth, progress, emissionTick, radius);
        position = clampRadius(position, definition.maximumRadius());
        int lifetime = definition.minimumLifetime() + (int) Math.floor(unit(seed + 0x9E3779B97F4A7C15L + index)
                * (definition.maximumLifetime() - definition.minimumLifetime() + 1));
        return new Sample(emissionTick, ordinal, position, lifetime, colorBand(progress, emissionTick, definition));
    }

    public static int particlesCreatedThisTick(MediumExtensionDefinition definition, int tick) {
        return definition != null && tick >= 0 && tick < definition.emissionTicks() ? definition.particlesPerTick() : 0;
    }

    public static boolean staticContractHolds(MediumExtensionDefinition definition, long seed) {
        if (definition == null || definition.particlesPerTick() > MediumExtensionCatalog.MAX_PARTICLES_PER_TICK) return false;
        for (int tick = 0; tick < definition.emissionTicks(); tick++) {
            for (int ordinal = 0; ordinal < definition.particlesPerTick(); ordinal++) {
                Sample first = sample(definition, seed, tick, ordinal);
                Sample second = sample(definition, seed, tick, ordinal);
                if (!first.equals(second) || first.position().lengthSqr() > definition.maximumRadius() * definition.maximumRadius() + 1.0E-7D
                        || first.lifetime() < definition.minimumLifetime() || first.lifetime() > definition.maximumLifetime()) return false;
            }
        }
        return true;
    }

    private static Vec3 shape(MediumExtensionDefinition d, Vec3 direction, double azimuth, double p, int tick, double radius) {
        return switch (d.category()) {
            case SPHERE -> direction.scale(radius * (0.84D + 0.16D * Math.sin(Math.PI * p)));
            case RADIAL -> direction.scale(radius).add(0.0D, Math.sin(TWO_PI * p + azimuth) * 2.8D, 0.0D);
            case RING_CORE -> new Vec3(Math.cos(azimuth) * radius, direction.y * radius * 0.34D, Math.sin(azimuth) * radius);
            case SHORT_WILLOW -> direction.scale(radius).add(0.0D, -radius * 0.48D * p * p, 0.0D);
            case PULSE -> direction.scale(radius * (0.72D + 0.28D * Math.sin(Math.PI * (tick % d.cadenceBeats()) / d.cadenceBeats())));
            case INTERLEAVED_SHELL -> direction.scale(radius).add(
                    Math.cos(azimuth * 3.0D) * 2.0D * Math.sin(Math.PI * p),
                    Math.sin(azimuth * 2.0D) * 1.8D * Math.sin(Math.PI * p),
                    Math.sin(azimuth * 3.0D) * 2.0D * Math.sin(Math.PI * p));
        };
    }

    private static double radialProgress(MediumExtensionDefinition.Category category, double p, int tick, MediumExtensionDefinition d) {
        double eased = p * p * (3.0D - 2.0D * p);
        return switch (category) {
            case RING_CORE -> 0.22D + eased * 0.78D;
            case PULSE -> Math.min(1.0D, eased * (0.88D + 0.12D * Math.sin(TWO_PI * tick / d.cadenceBeats())));
            case SHORT_WILLOW -> 0.15D + eased * 0.85D;
            default -> eased;
        };
    }
    private static int colorBand(double progress, int tick, MediumExtensionDefinition d) { return progress < 0.28D ? 0 : ((tick + 1) % d.cadenceBeats() == 0 || progress > 0.78D ? 2 : 1); }
    private static Vec3 clampRadius(Vec3 position, double maximum) { double length = position.length(); return length <= maximum ? position : position.scale(maximum / length); }
    private static double unit(long v) { long x = v + 0x9E3779B97F4A7C15L; x = (x ^ x >>> 30) * 0xBF58476D1CE4E5B9L; x = (x ^ x >>> 27) * 0x94D049BB133111EBL; return ((x ^ x >>> 31) >>> 11) * 0x1.0p-53D; }

    public record Sample(int emissionTick, int ordinal, Vec3 position, int lifetime, int colorBand) {}
}
