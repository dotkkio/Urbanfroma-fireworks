package com.urbanforma.fireworks.client.release_next.giant_willow;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.giant_willow.GiantWillowReleaseNextCatalog;
import com.urbanforma.fireworks.content.release_next.giant_willow.GiantWillowReleaseNextTrajectory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;

/** Client-only bounded executor for one release-next giant willow payload. */
public final class GiantWillowReleaseNextClientProgram {
    private final Request request;
    private int age;
    private int createdParticles;

    public GiantWillowReleaseNextClientProgram(Request request) {
        this.request = request;
        GiantWillowReleaseNextTrajectory.validateProfile(request.profile());
    }

    /**
     * Typed handoff route for an isolated catalog entry. The coordinator supplies only the normal burst origin and
     * seed; this program remains client-only and has no scheduler, packet, or server-side particle behavior.
     */
    public static GiantWillowReleaseNextClientProgram forEntry(GiantWillowReleaseNextCatalog.Entry entry,
            double x, double y, double z, long seed) {
        if (entry == null || entry.clientRoute() != GiantWillowReleaseNextCatalog.ClientRoute.GIANT_WILLOW_RELEASE_NEXT) {
            throw new IllegalArgumentException("Unsupported release-next giant willow client route");
        }
        return new GiantWillowReleaseNextClientProgram(new Request(x, y, z, seed, entry.profile()));
    }

    public boolean tick(Minecraft minecraft) {
        if (minecraft.level == null) return false;
        if (GiantWillowReleaseNextTrajectory.emitsAt(request.profile(), age)) {
            int segment = GiantWillowReleaseNextTrajectory.segmentAt(request.profile(), age);
            for (int branch = 0; branch < request.profile().branches(); branch++) emit(minecraft, branch, segment);
        }
        age++;
        return age >= GiantWillowReleaseNextTrajectory.totalVisualTicks(request.profile());
    }

    public int age() { return age; }
    public int createdParticles() { return createdParticles; }
    public int expectedParticles() { return GiantWillowReleaseNextTrajectory.totalParticles(request.profile()); }

    private void emit(Minecraft minecraft, int branch, int segment) {
        GiantWillowReleaseNextTrajectory.Sample sample = GiantWillowReleaseNextTrajectory.sample(request.seed(), request.profile(), branch, segment);
        Particle particle = FireworkParticleAppearance.createSpark(minecraft, request.x() + sample.position().x,
                request.y() + sample.position().y, request.z() + sample.position().z, 0.0D, 0.0D, 0.0D);
        if (particle == null) return;
        if (sample.core()) {
            FireworkParticleAppearance.applyCoreColor(particle, sample.color().red(), sample.color().green(), sample.color().blue());
            FireworkParticleAppearance.applyVisibilityScale(particle, FireworkParticleAppearance.CORE_BASE_SCALE, true);
        } else {
            FireworkParticleAppearance.applyVividColor(particle, sample.color().red(), sample.color().green(), sample.color().blue(),
                    1.08F, FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
            FireworkParticleAppearance.applyVisibilityScale(particle, sample.color().scale());
        }
        particle.setParticleSpeed(0.0D, 0.0D, 0.0D);
        particle.setLifetime(sample.lifetime());
        if (particle instanceof FireworkParticles.SparkParticle spark && sample.lifetime() < 104) spark.setTwinkle(true);
        createdParticles++;
    }

    public record Request(double x, double y, double z, long seed, GiantWillowReleaseNextTrajectory.Profile profile) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || profile == null) {
                throw new IllegalArgumentException("Invalid giant willow request");
            }
        }
    }
}
