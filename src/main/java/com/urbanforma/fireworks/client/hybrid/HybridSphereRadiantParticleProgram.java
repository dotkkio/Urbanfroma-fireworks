package com.urbanforma.fireworks.client.hybrid;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.client.colorchange.ColorChangeBallParticleAdapter;
import com.urbanforma.fireworks.content.colorchange.ColorChangeBallProgram;
import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.RadiantTrajectory;
import com.urbanforma.fireworks.content.hybrid.HybridSphereRadiantTrajectory;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only prototype program. A caller owns its tick scheduling; this class deliberately does not register
 * an event listener, enqueue a payload, or touch the shared Fireworks scheduler.
 *
 * <p>All allocations use the single vanilla {@code FIREWORK} particle. The first twelve ticks overlap the
 * 1,920-particle outer shell, the 240-particle core, and one complete 160-branch radial ring. The core is
 * present from tick zero, preserving the accepted 2,160-node sphere contract while making the center readable.</p>
 */
public final class HybridSphereRadiantParticleProgram {
    private final Minecraft minecraft;
    private final double x;
    private final double y;
    private final double z;
    private final long payloadSeed;
    private final FireworkStyle.RadiantProfile radialProfile;
    private final FireworkStyle.Rgb primaryColor;
    private final FireworkStyle.Rgb secondaryColor;
    private final FireworkStyle.Rgb accentColor;
    private final ColorChangeBallParticleAdapter.AppearanceSession colorChangeSession;
    private final RadiantTrajectory.Branch[] radialBranches;
    private int ageTicks;

    public HybridSphereRadiantParticleProgram(
            Minecraft minecraft,
            double x,
            double y,
            double z,
            long payloadSeed,
            FireworkStyle.RadiantProfile radialProfile,
            FireworkStyle.Rgb primaryColor,
            FireworkStyle.Rgb secondaryColor,
            FireworkStyle.Rgb accentColor) {
        this(minecraft, x, y, z, payloadSeed, radialProfile,
                primaryColor, secondaryColor, accentColor, null);
    }

    public HybridSphereRadiantParticleProgram(
            Minecraft minecraft,
            double x,
            double y,
            double z,
            long payloadSeed,
            FireworkStyle.RadiantProfile radialProfile,
            FireworkStyle.Rgb primaryColor,
            FireworkStyle.Rgb secondaryColor,
            FireworkStyle.Rgb accentColor,
            ColorChangeBallParticleAdapter.AppearanceSession colorChangeSession) {
        this.minecraft = Objects.requireNonNull(minecraft, "minecraft");
        this.x = x;
        this.y = y;
        this.z = z;
        this.payloadSeed = payloadSeed;
        this.radialProfile = Objects.requireNonNull(radialProfile, "radialProfile");
        this.primaryColor = Objects.requireNonNull(primaryColor, "primaryColor");
        this.secondaryColor = Objects.requireNonNull(secondaryColor, "secondaryColor");
        this.accentColor = Objects.requireNonNull(accentColor, "accentColor");
        this.colorChangeSession = colorChangeSession;
        this.radialBranches = new RadiantTrajectory.Branch[HybridSphereRadiantTrajectory.RADIAL_BRANCH_COUNT];
        for (int branchIndex = 0; branchIndex < this.radialBranches.length; branchIndex++) {
            this.radialBranches[branchIndex] = HybridSphereRadiantTrajectory.radialBranch(
                    this.radialProfile, this.payloadSeed, branchIndex);
        }
    }

    /** Emits one deterministic frame and returns the number of requested FIREWORK particle nodes. */
    public int emitTick() {
        if (complete()) {
            return 0;
        }
        HybridSphereRadiantTrajectory.EmissionFrame frame =
                HybridSphereRadiantTrajectory.emissionFrame(this.ageTicks);
        int emitted = 0;
        for (int index = frame.sphereOuterStart();
                index < frame.sphereOuterStart() + frame.sphereOuterCount();
                index++) {
            emitSphereNode(HybridSphereRadiantTrajectory.sphereNode(
                    this.payloadSeed,
                    HybridSphereRadiantTrajectory.SphereLayer.OUTER,
                    index));
            emitted++;
        }
        for (int index = frame.sphereCoreStart();
                index < frame.sphereCoreStart() + frame.sphereCoreCount();
                index++) {
            emitSphereNode(HybridSphereRadiantTrajectory.sphereNode(
                    this.payloadSeed,
                    HybridSphereRadiantTrajectory.SphereLayer.CORE,
                    index));
            emitted++;
        }
        if (frame.radialRingIndex() >= 0) {
            for (int branchIndex = 0;
                    branchIndex < HybridSphereRadiantTrajectory.RADIAL_BRANCH_COUNT;
                    branchIndex++) {
                HybridSphereRadiantTrajectory.RadialNode node =
                        HybridSphereRadiantTrajectory.radialNode(
                                this.radialProfile,
                                this.radialBranches[branchIndex],
                                frame.radialRingIndex());
                emitRadialNode(node);
                emitted++;
            }
        }
        this.ageTicks++;
        if (this.ageTicks >= HybridSphereRadiantTrajectory.TOTAL_EMISSION_TICKS
                && this.colorChangeSession != null) {
            this.colorChangeSession.seal();
        }
        return emitted;
    }

    public boolean complete() {
        return HybridSphereRadiantTrajectory.isCompleteAt(this.ageTicks);
    }

    public int ageTicks() {
        return this.ageTicks;
    }

    private void emitSphereNode(HybridSphereRadiantTrajectory.SphereNode node) {
        Vec3 direction = node.direction();
        Particle spark = FireworkParticleAppearance.createSpark(
                this.minecraft,
                this.x,
                this.y,
                this.z,
                direction.x * node.speed(),
                direction.y * node.speed(),
                direction.z * node.speed());
        if (spark == null) {
            return;
        }

        boolean coreHighlight = node.layer() == HybridSphereRadiantTrajectory.SphereLayer.CORE;
        FireworkParticleAppearance.applyVisibilityScale(spark, 1.48F, coreHighlight);
        spark.setLifetime(node.lifetime());
        setVividColor(
                spark,
                colorFor(node.colorBand()),
                node.colorBand(),
                node.colorTone(),
                coreHighlight);
        if (!coreHighlight) {
            enableTwinkle(spark);
        }
        trackExisting(spark, node.colorBand(), node.lifetime(), coreHighlight);
    }

    private void emitRadialNode(HybridSphereRadiantTrajectory.RadialNode node) {
        RadiantTrajectory.BranchSample sample = node.sample();
        Vec3 position = sample.position().add(this.x, this.y, this.z);
        Vec3 tangent = sample.tangent();
        Particle spark = FireworkParticleAppearance.createSpark(
                this.minecraft,
                position.x,
                position.y,
                position.z,
                tangent.x,
                tangent.y,
                tangent.z);
        if (spark == null) {
            return;
        }

        spark.setParticleSpeed(tangent.x, tangent.y, tangent.z);
        FireworkParticleAppearance.applyVisibilityScale(spark, 1.12F);
        spark.setLifetime(sample.lifetime());
        setVividColor(spark, colorFor(sample.colorBand()), sample.colorBand(), sample.colorTone());
        if (sample.twinkles()) {
            enableTwinkle(spark);
        }
        trackExisting(spark, sample.colorBand(), sample.lifetime(), false);
    }

    private void trackExisting(
            Particle spark,
            RadiantTrajectory.ColorBand colorBand,
            int lifetime,
            boolean coreHighlight) {
        if (this.colorChangeSession == null) {
            return;
        }
        ColorChangeBallProgram.Layer layer = switch (colorBand) {
            case PRIMARY -> ColorChangeBallProgram.Layer.PRIMARY;
            case SECONDARY -> ColorChangeBallProgram.Layer.SECONDARY;
            case ACCENT -> ColorChangeBallProgram.Layer.ACCENT;
        };
        this.colorChangeSession.trackExisting(spark, layer, lifetime, coreHighlight);
    }

    private FireworkStyle.Rgb colorFor(RadiantTrajectory.ColorBand band) {
        return switch (band) {
            case PRIMARY -> this.primaryColor;
            case SECONDARY -> this.secondaryColor;
            case ACCENT -> this.accentColor;
        };
    }

    private static void setVividColor(
            Particle particle, FireworkStyle.Rgb color, RadiantTrajectory.ColorBand band, float tone) {
        setVividColor(particle, color, band, tone, false);
    }

    private static void setVividColor(
            Particle particle,
            FireworkStyle.Rgb color,
            RadiantTrajectory.ColorBand band,
            float tone,
            boolean coreHighlight) {
        if (coreHighlight) {
            FireworkParticleAppearance.applyCoreColor(
                    particle, color.red(), color.green(), color.blue());
            return;
        }
        float brilliance = switch (band) {
            case PRIMARY -> 1.0F + tone * 0.22F;
            case SECONDARY -> 1.0F + tone * 0.20F;
            case ACCENT -> 1.0F + tone * 0.18F;
        };
        float highlight = switch (band) {
            case PRIMARY -> 0.050F + tone * 0.050F;
            case SECONDARY -> 0.065F + tone * 0.060F;
            case ACCENT -> 0.100F + tone * 0.080F;
        };
        FireworkParticleAppearance.applyVividColor(
                particle, color.red(), color.green(), color.blue(), brilliance, highlight);
    }

    private static void enableTwinkle(Particle particle) {
        if (particle instanceof FireworkParticles.SparkParticle spark) {
            spark.setTwinkle(true);
        }
    }
}
