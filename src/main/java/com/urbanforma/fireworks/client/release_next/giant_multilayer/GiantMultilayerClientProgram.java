package com.urbanforma.fireworks.client.release_next.giant_multilayer;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.giant_multilayer.GiantMultilayerTrajectory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/** Bounded client-only executor. It creates no particle after its profile's final emission tick. */
public final class GiantMultilayerClientProgram {
    private final Request request;
    private int age;
    private int createdParticles;

    public GiantMultilayerClientProgram(Request request) {
        this.request = request;
        GiantMultilayerTrajectory.validateProfile(request.profile());
    }

    public boolean tick(Minecraft minecraft) {
        if (minecraft.level == null) return false;
        for (int layerIndex = 0; layerIndex < GiantMultilayerTrajectory.layers(this.request.profile()).size(); layerIndex++) {
            GiantMultilayerTrajectory.Layer layer = GiantMultilayerTrajectory.layers(this.request.profile()).get(layerIndex);
            int segment = this.age - layer.startTick();
            if (segment >= 0 && segment < layer.segments()) {
                for (int branch = 0; branch < layer.branches(); branch++) emit(minecraft, layerIndex, branch, segment);
            }
        }
        this.age++;
        return this.age >= GiantMultilayerTrajectory.totalVisualTicks(this.request.profile());
    }

    private void emit(Minecraft minecraft, int layerIndex, int branch, int segment) {
        GiantMultilayerTrajectory.BranchSample sample = GiantMultilayerTrajectory.sample(this.request.seed(), this.request.profile(), layerIndex, branch, segment);
        Vec3 point = sample.position().add(this.request.x(), this.request.y(), this.request.z());
        Particle particle = FireworkParticleAppearance.createSpark(minecraft, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
        if (particle == null) return;
        if (sample.core()) {
            FireworkParticleAppearance.applyCoreColor(particle, sample.layer().color().red(), sample.layer().color().green(), sample.layer().color().blue());
            FireworkParticleAppearance.applyVisibilityScale(particle, FireworkParticleAppearance.CORE_BASE_SCALE, true);
        } else {
            FireworkParticleAppearance.applyVividColor(particle, sample.layer().color().red(), sample.layer().color().green(), sample.layer().color().blue(), sample.brightness(), FireworkParticleAppearance.OUTER_COLOR_WHITE_LIFT);
            FireworkParticleAppearance.applyVisibilityScale(particle, sample.layer().color().scale());
        }
        particle.setParticleSpeed(0.0D, 0.0D, 0.0D);
        particle.setLifetime(sample.lifetime());
        if (particle instanceof FireworkParticles.SparkParticle spark && sample.lifetime() < 108) spark.setTwinkle(true);
        this.createdParticles++;
    }

    public int age() { return this.age; }
    public int createdParticles() { return this.createdParticles; }
    public Request request() { return this.request; }
    public record Request(double x, double y, double z, long seed, GiantMultilayerTrajectory.Profile profile) {
        public Request {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || profile == null) throw new IllegalArgumentException("Invalid giant multilayer request");
        }
    }
}
