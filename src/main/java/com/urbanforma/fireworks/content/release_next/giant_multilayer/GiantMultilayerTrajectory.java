package com.urbanforma.fireworks.content.release_next.giant_multilayer;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, common-side geometry for release-next giant multilayer shells.
 *
 * <p>This class describes one explosion at one origin. It has no timers, particle allocation, network access,
 * or server simulation. The client program consumes the fixed samples and is responsible for the finite visual.
 */
public final class GiantMultilayerTrajectory {
    public static final double MAX_RADIUS = 130.0D;
    public static final int MAX_CLIENT_PARTICLES_PER_TICK = 720;
    public static final int MIN_LIFETIME = 92;
    public static final int MAX_LIFETIME = 144;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double EPSILON = 1.0E-7D;

    private GiantMultilayerTrajectory() {
    }

    public enum Profile {
        AURORA_CROWN("aurora_crown", 4, 10_880),
        EMBER_CASCADE("ember_cascade", 4, 10_624),
        JADE_LOTUS("jade_lotus", 4, 10_880),
        PEARL_CATHEDRAL("pearl_cathedral", 5, 12_992),
        COPPER_HELIX("copper_helix", 4, 10_880),
        VIOLET_LANTERN("violet_lantern", 4, 10_048),
        SOLAR_FAN("solar_fan", 4, 10_624),
        AZURE_COMPASS("azure_compass", 4, 10_624),
        CRIMSON_PETAL("crimson_petal", 5, 12_608),
        GOLDEN_PAGODA("golden_pagoda", 5, 13_664),
        MULTIRADIAL_REPLACEMENT("multiradial_replacement", 5, 12_992),
        MULTIRADIAL2_REPLACEMENT("multiradial2_replacement", 5, 13_664);

        private final String id;
        private final int layerCount;
        private final int declaredParticles;

        Profile(String id, int layerCount, int declaredParticles) {
            this.id = id;
            this.layerCount = layerCount;
            this.declaredParticles = declaredParticles;
        }

        public String id() { return this.id; }
        public int layerCount() { return this.layerCount; }
        public int declaredParticles() { return this.declaredParticles; }
    }

    public enum ColorBand {
        PEARL(1.0F, 0.96F, 0.78F, 1.52F),
        GOLD(1.0F, 0.69F, 0.13F, 1.32F),
        AMBER(1.0F, 0.35F, 0.07F, 1.24F),
        JADE(0.24F, 1.0F, 0.63F, 1.30F),
        AZURE(0.35F, 0.73F, 1.0F, 1.28F),
        VIOLET(0.76F, 0.42F, 1.0F, 1.30F),
        CRIMSON(1.0F, 0.19F, 0.22F, 1.27F);

        private final float red;
        private final float green;
        private final float blue;
        private final float scale;

        ColorBand(float red, float green, float blue, float scale) {
            this.red = red; this.green = green; this.blue = blue; this.scale = scale;
        }
        public float red() { return this.red; }
        public float green() { return this.green; }
        public float blue() { return this.blue; }
        public float scale() { return this.scale; }
    }

    /** Each layer emits one complete branch ring on every tick in its finite segment interval. */
    public record Layer(int branches, int segments, int startTick, double radius, double verticalScale,
                        double twistTurns, double lift, ColorBand color) {
        public Layer {
            if (branches <= 0 || branches > MAX_CLIENT_PARTICLES_PER_TICK || segments <= 0 || startTick < 0
                    || radius <= 0.0D || radius > MAX_RADIUS || verticalScale <= 0.0D || color == null) {
                throw new IllegalArgumentException("Invalid bounded giant multilayer layer");
            }
        }
        public int endTickInclusive() { return this.startTick + this.segments - 1; }
        public int particleCount() { return this.branches * this.segments; }
    }

    public record BranchSample(Layer layer, int branchIndex, int segmentIndex, Vec3 position, int lifetime,
                               float brightness, boolean core) {
        public BranchSample {
            if (layer == null || branchIndex < 0 || branchIndex >= layer.branches() || segmentIndex < 0
                    || segmentIndex >= layer.segments() || position == null
                    || position.lengthSqr() > MAX_RADIUS * MAX_RADIUS + EPSILON || lifetime < MIN_LIFETIME
                    || lifetime > MAX_LIFETIME || !Float.isFinite(brightness)) {
                throw new IllegalArgumentException("Invalid giant multilayer sample");
            }
        }
    }

    public static List<Layer> layers(Profile profile) {
        return switch (profile) {
            case AURORA_CROWN -> List.of(l(192, 18, 0, 38, .92, .4, 0, ColorBand.PEARL), l(160, 20, 4, 76, .74, 1.2, 3, ColorBand.AZURE), l(128, 24, 10, 114, .55, 2.0, 6, ColorBand.JADE), l(64, 18, 18, 130, .35, 3.1, 10, ColorBand.PEARL));
            case EMBER_CASCADE -> List.of(l(192, 16, 0, 34, .90, .2, 0, ColorBand.PEARL), l(160, 20, 3, 70, .76, .9, -2, ColorBand.GOLD), l(128, 24, 9, 110, .58, 1.8, -8, ColorBand.AMBER), l(64, 20, 16, 130, .42, 2.7, -16, ColorBand.CRIMSON));
            case JADE_LOTUS -> List.of(l(192, 16, 0, 32, .98, .1, 0, ColorBand.PEARL), l(160, 20, 4, 72, .84, .7, 5, ColorBand.JADE), l(128, 24, 10, 112, .66, 1.4, 12, ColorBand.GOLD), l(64, 24, 16, 130, .40, 2.5, 18, ColorBand.JADE));
            case PEARL_CATHEDRAL, MULTIRADIAL_REPLACEMENT -> List.of(l(192, 16, 0, 30, .96, .2, 0, ColorBand.PEARL), l(176, 18, 3, 62, .82, .8, 3, ColorBand.GOLD), l(144, 22, 8, 96, .66, 1.5, 7, ColorBand.PEARL), l(96, 24, 14, 124, .48, 2.3, 12, ColorBand.GOLD), l(64, 20, 20, 130, .30, 3.2, 18, ColorBand.AZURE));
            case COPPER_HELIX -> List.of(l(192, 16, 0, 34, .94, .4, 0, ColorBand.PEARL), l(160, 20, 3, 74, .72, 1.8, 2, ColorBand.GOLD), l(128, 24, 8, 112, .54, 3.5, 5, ColorBand.AMBER), l(64, 24, 15, 130, .36, 5.0, 8, ColorBand.PEARL));
            case VIOLET_LANTERN -> List.of(l(192, 14, 0, 32, .94, .2, 0, ColorBand.PEARL), l(160, 18, 4, 70, .80, .8, 5, ColorBand.VIOLET), l(128, 24, 10, 108, .62, 1.7, 10, ColorBand.AZURE), l(64, 22, 18, 130, .38, 2.7, 16, ColorBand.VIOLET));
            case SOLAR_FAN -> List.of(l(192, 16, 0, 34, .90, .2, 0, ColorBand.PEARL), l(160, 20, 3, 72, .68, .9, 6, ColorBand.GOLD), l(128, 24, 8, 112, .48, 1.8, 16, ColorBand.AMBER), l(64, 20, 16, 130, .30, 2.6, 24, ColorBand.PEARL));
            case AZURE_COMPASS -> List.of(l(192, 16, 0, 34, .96, .1, 0, ColorBand.PEARL), l(160, 20, 4, 74, .82, .7, 0, ColorBand.AZURE), l(128, 24, 10, 112, .60, 1.4, 3, ColorBand.JADE), l(64, 20, 18, 130, .34, 2.2, 7, ColorBand.PEARL));
            case CRIMSON_PETAL -> List.of(l(192, 14, 0, 30, .94, .2, 0, ColorBand.PEARL), l(176, 18, 3, 64, .76, .9, 2, ColorBand.CRIMSON), l(144, 22, 8, 98, .60, 1.7, 8, ColorBand.GOLD), l(96, 24, 14, 124, .44, 2.6, 14, ColorBand.CRIMSON), l(64, 20, 20, 130, .28, 3.5, 20, ColorBand.PEARL));
            case GOLDEN_PAGODA, MULTIRADIAL2_REPLACEMENT -> List.of(l(192, 16, 0, 30, .96, .2, 0, ColorBand.PEARL), l(176, 18, 3, 60, .84, .7, 4, ColorBand.GOLD), l(160, 20, 7, 92, .68, 1.4, 9, ColorBand.PEARL), l(112, 24, 13, 120, .48, 2.2, 16, ColorBand.GOLD), l(64, 24, 20, 130, .30, 3.1, 24, ColorBand.AMBER));
        };
    }

    public static BranchSample sample(long seed, Profile profile, int layerIndex, int branchIndex, int segmentIndex) {
        Layer layer = layers(profile).get(layerIndex);
        double progress = layer.segments() == 1 ? 1.0D : (double) segmentIndex / (layer.segments() - 1);
        long mixed = mix(seed + 0x9E3779B97F4A7C15L * (layerIndex + 1L) + 0xD1B54A32D192ED03L * branchIndex);
        double latitude = Math.asin(clamp(1.0D - 2.0D * ((branchIndex + .5D) / layer.branches()), -.96D, .96D));
        double angle = branchIndex * GOLDEN_ANGLE + unit(mixed) * .16D + layer.twistTurns() * Math.PI * 2.0D * progress;
        double radial = 2.0D + (layer.radius() - 2.0D) * smooth(progress);
        double y = Math.sin(latitude) * radial * layer.verticalScale() + layer.lift() * progress * progress;
        double horizontal = Math.sqrt(Math.max(0.0D, radial * radial - y * y));
        Vec3 point = new Vec3(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal);
        int lifetime = MIN_LIFETIME + (int) Math.floor(unit(mixed ^ (segmentIndex * 0x9E3779B9L)) * (MAX_LIFETIME - MIN_LIFETIME + 1));
        return new BranchSample(layer, branchIndex, segmentIndex, point, lifetime,
                1.02F + (float) unit(mixed ^ 0xA4093822299F31D0L) * .22F, layerIndex == 0 || segmentIndex < 3);
    }

    public static int totalParticles(Profile profile) { return layers(profile).stream().mapToInt(Layer::particleCount).sum(); }
    public static int peakParticlesPerTick(Profile profile) {
        return layers(profile).stream().flatMapToInt(l -> java.util.stream.IntStream.rangeClosed(l.startTick(), l.endTickInclusive()))
                .distinct().map(t -> particlesAtTick(profile, t)).max().orElse(0);
    }
    public static int particlesAtTick(Profile profile, int tick) {
        return layers(profile).stream().filter(l -> tick >= l.startTick() && tick <= l.endTickInclusive()).mapToInt(Layer::branches).sum();
    }
    public static int totalVisualTicks(Profile profile) { return layers(profile).stream().mapToInt(Layer::endTickInclusive).max().orElse(0) + MAX_LIFETIME + 1; }
    public static void validateProfile(Profile profile) {
        if (profile == null || layers(profile).size() != profile.layerCount() || totalParticles(profile) != profile.declaredParticles()
                || peakParticlesPerTick(profile) > MAX_CLIENT_PARTICLES_PER_TICK || layers(profile).stream().anyMatch(l -> l.radius() > MAX_RADIUS)) {
            throw new IllegalStateException("Invalid multilayer profile: " + profile);
        }
    }
    public static void validateAllProfiles() { Arrays.stream(Profile.values()).forEach(GiantMultilayerTrajectory::validateProfile); }
    private static Layer l(int b, int s, int t, double r, double v, double w, double h, ColorBand c) { return new Layer(b, s, t, r, v, w, h, c); }
    private static double smooth(double value) { return value * value * (3.0D - 2.0D * value); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    private static double unit(long value) { return (mix(value) >>> 11) * 0x1.0p-53; }
    private static long mix(long value) { value ^= value >>> 33; value *= 0xff51afd7ed558ccdl; value ^= value >>> 33; value *= 0xc4ceb9fe1a85ec53l; return value ^ value >>> 33; }
}
