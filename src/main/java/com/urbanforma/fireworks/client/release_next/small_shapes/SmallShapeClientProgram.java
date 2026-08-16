package com.urbanforma.fireworks.client.release_next.small_shapes;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.small_shapes.SmallShapeCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

/** Caller-driven finite client program for the release-next small non-spherical shape catalog. */
public final class SmallShapeClientProgram {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long BRANCH_SALT = 0x74D2E3C196AB50F1L;
    private static final long PHASE_SALT = 0xCA7E91B35D2846F0L;

    private final SmallShapeCatalog.Definition definition;
    private final Request request;
    private int age;
    private int requestedParticles;
    private int createdParticles;

    public SmallShapeClientProgram(String effectId, Request request) {
        this.definition = SmallShapeCatalog.byId(effectId);
        if (this.definition == null) throw new IllegalArgumentException("Unknown small-shape effect " + effectId);
        this.request = request == null ? throwRequest() : request;
    }

    private static Request throwRequest() { throw new IllegalArgumentException("Small-shape request is required"); }

    /** Emits one bounded deterministic slice. Returns true only after its owned visual lifetime has expired. */
    public boolean tick(Minecraft minecraft) {
        ClientLevel level = minecraft == null ? null : minecraft.level;
        if (level == null) return false;
        if (age < definition.emissionTicks()) emitSlice(minecraft, age);
        age++;
        return age >= definition.emissionTicks() + definition.maxLifetimeTicks();
    }

    public int age() { return age; }
    public int requestedParticleCount() { return requestedParticles; }
    public int createdParticleCount() { return createdParticles; }
    public int plannedParticleCount() { return definition.totalParticles(); }
    public SmallShapeCatalog.Definition definition() { return definition; }

    private void emitSlice(Minecraft minecraft, int tick) {
        for (int branch = 0; branch < definition.branchCount(); branch++) {
            Point point = sample(request.seed(), branch, tick);
            requestedParticles++;
            Particle particle = FireworkParticleAppearance.createSpark(minecraft, request.x() + point.x, request.y() + point.y, request.z() + point.z, 0.0D, 0.0D, 0.0D);
            if (particle == null) continue;
            float[] color = colorFor(tick);
            particle.setParticleSpeed(0.0D, 0.0D, 0.0D);
            FireworkParticleAppearance.applyVividColor(particle, color[0], color[1], color[2], 1.06F, tick == 0 ? 0.18F : 0.06F);
            FireworkParticleAppearance.applyVisibilityScale(particle, tick == 0 ? 1.12F : 0.92F, tick == 0);
            particle.setLifetime(definition.minLifetimeTicks() + (int) (unit(request.seed() + branch * 31L + tick) * (definition.maxLifetimeTicks() - definition.minLifetimeTicks() + 1)));
            createdParticles++;
        }
        if (requestedParticles > definition.totalParticles() || createdParticles > SmallShapeCatalog.MAX_OWNED_PARTICLES) {
            throw new IllegalStateException("Small-shape program exceeded its instance budget");
        }
    }

    private Point sample(long seed, int branch, int tick) {
        double progress = (tick + 1.0D) / definition.emissionTicks();
        double angle = TWO_PI * branch / definition.branchCount() + unit(seed ^ PHASE_SALT) * TWO_PI;
        double reach = definition.maxRadius() * progress;
        double spread = 0.18D + 0.82D * unit(seed ^ (BRANCH_SALT + branch));
        Point raw = switch (definition.family()) {
            case SHORT_RAY -> new Point(Math.cos(angle) * reach, (spread - 0.5D) * reach, Math.sin(angle) * reach);
            case RING -> new Point(Math.cos(angle) * reach, Math.sin(angle * 2.0D + seed) * 0.30D, Math.sin(angle) * reach);
            case COMET -> new Point(Math.cos(angle) * reach, 1.2D * Math.sin(progress * Math.PI) - progress * progress * 3.2D, Math.sin(angle) * reach);
            case CROSS -> cross(branch, progress, reach);
            case SHORT_WILLOW -> new Point(Math.cos(angle) * reach * 0.74D, -progress * progress * 4.2D, Math.sin(angle) * reach * 0.74D);
            case MINE -> new Point(Math.cos(angle) * reach * 0.42D, progress * reach * 0.92D - progress * progress * 3.0D, Math.sin(angle) * reach * 0.42D);
            case PULSE -> new Point(Math.cos(angle) * reach * pulse(tick), (spread - 0.5D) * 1.1D, Math.sin(angle) * reach * pulse(tick));
            case SPLIT -> split(seed, branch, progress, reach, angle);
        };
        return raw.clamp(definition.maxRadius());
    }

    private Point cross(int branch, double progress, double reach) {
        int arm = branch % 4;
        double lateral = ((branch / 4) / (double) Math.max(1, definition.branchCount() / 4 - 1) - 0.5D) * 0.44D;
        return switch (arm) {
            case 0 -> new Point(reach, lateral, 0.0D);
            case 1 -> new Point(-reach, lateral, 0.0D);
            case 2 -> new Point(lateral, reach, 0.0D);
            default -> new Point(lateral, -reach, 0.0D);
        };
    }

    private Point split(long seed, int branch, double progress, double reach, double angle) {
        double fork = progress < 0.48D ? 0.0D : (progress - 0.48D) * 2.5D;
        double sign = (branch & 1) == 0 ? -1.0D : 1.0D;
        return new Point(Math.cos(angle) * reach + sign * fork, 0.55D * reach - progress * progress * 2.0D, Math.sin(angle) * reach + sign * fork);
    }

    private double pulse(int tick) { return tick % 2 == 0 ? 0.72D : 1.0D; }

    private float[] colorFor(int tick) {
        String hex = tick == 0 ? definition.palette().accent() : tick + 1 == definition.emissionTicks() ? definition.palette().secondary() : definition.palette().primary();
        return new float[] { Integer.parseInt(hex.substring(1, 3), 16) / 255.0F, Integer.parseInt(hex.substring(3, 5), 16) / 255.0F, Integer.parseInt(hex.substring(5, 7), 16) / 255.0F };
    }

    private static double unit(long value) { return (mix64(value) >>> 11) * 0x1.0p-53D; }
    private static long mix64(long value) { value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L; value = (value ^ value >>> 27) * 0x94D049BB133111EBL; return value ^ value >>> 31; }

    public record Request(double x, double y, double z, long seed) {
        public Request { if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Small-shape request position must be finite"); }
    }

    private record Point(double x, double y, double z) {
        private Point clamp(double max) { double length = Math.sqrt(x * x + y * y + z * z); return length <= max ? this : new Point(x * max / length, y * max / length, z * max / length); }
    }
}
