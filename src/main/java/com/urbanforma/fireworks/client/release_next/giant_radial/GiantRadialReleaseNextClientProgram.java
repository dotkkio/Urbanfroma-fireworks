package com.urbanforma.fireworks.client.release_next.giant_radial;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.giant_radial.GiantRadialReleaseNext;
import com.urbanforma.fireworks.content.release_next.giant_radial.GiantRadialReleaseNext.Sample;
import com.urbanforma.fireworks.content.release_next.giant_radial.GiantRadialReleaseNext.Variant;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/** Client-only finite renderer. It has no server counterpart and never spawns child effects. */
public final class GiantRadialReleaseNextClientProgram {
    private static final Map<String, Variant> ROUTES_BY_STABLE_ID = indexRoutes();
    private final Request request;
    private int age;
    private int createdParticles;

    public GiantRadialReleaseNextClientProgram(Request request) { this.request = request; }

    /** Resolves one catalog stable ID to the existing typed client variant without a shared dispatcher or queue. */
    public static Request requestFor(String stableId, double x, double y, double z, long seed) {
        Variant variant = ROUTES_BY_STABLE_ID.get(stableId);
        if (variant == null) {
            throw new IllegalArgumentException("Unknown giant radial client route: " + stableId);
        }
        return new Request(variant, x, y, z, seed);
    }

    public static Variant routeFor(String stableId) {
        Variant variant = ROUTES_BY_STABLE_ID.get(stableId);
        if (variant == null) {
            throw new IllegalArgumentException("Unknown giant radial client route: " + stableId);
        }
        return variant;
    }

    public static void validateRoutes() {
        if (ROUTES_BY_STABLE_ID.size() != GiantRadialReleaseNext.items().size()) {
            throw new IllegalStateException("Giant radial client route coverage drifted");
        }
        for (GiantRadialReleaseNext.ItemMetadata item : GiantRadialReleaseNext.items()) {
            if (routeFor(item.stableId()) != item.variant()) {
                throw new IllegalStateException("Giant radial client route mismatch: " + item.stableId());
            }
        }
    }

    /** Emits exactly one bounded cross-section per client tick, then retires after the final maximum lifetime. */
    public boolean tick(Minecraft minecraft) {
        if (minecraft.level == null) return false;
        Variant variant = this.request.variant();
        if (this.age < variant.segments()) {
            for (int branch = 0; branch < variant.branches(); branch++) for (int strand = 0; strand < variant.strands(); strand++) {
                Sample sample = GiantRadialReleaseNext.sample(variant, this.request.seed(), branch, this.age, strand);
                Vec3 position = sample.position().add(this.request.x(), this.request.y(), this.request.z());
                Particle spark = FireworkParticleAppearance.createSpark(minecraft, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
                if (spark != null) {
                    FireworkParticleAppearance.applyVividColor(spark, sample.color().red(), sample.color().green(), sample.color().blue(), sample.brightness(), FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
                    spark.setParticleSpeed(0.0D, 0.0D, 0.0D);
                    spark.setLifetime(sample.lifetime());
                    this.createdParticles++;
                }
            }
        }
        this.age++;
        return this.age >= variant.totalVisualTicks();
    }

    public int age() { return this.age; }
    public int createdParticles() { return this.createdParticles; }
    public Request request() { return this.request; }

    public record Request(Variant variant, double x, double y, double z, long seed) {
        public Request { if (variant == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Finite client-only giant radial request required"); }
    }

    private static Map<String, Variant> indexRoutes() {
        Map<String, Variant> routes = new HashMap<>();
        for (GiantRadialReleaseNext.ItemMetadata item : GiantRadialReleaseNext.items()) {
            if (routes.put(item.stableId(), item.variant()) != null) {
                throw new IllegalStateException("Duplicate giant radial client route: " + item.stableId());
            }
        }
        return Map.copyOf(routes);
    }
}
