package com.urbanforma.fireworks.client.release_next.large_extension;

import com.urbanforma.fireworks.client.FireworkParticleAppearance;
import com.urbanforma.fireworks.content.release_next.large_extension.LargeExtensionCatalog;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;

/** Physical-client-only deterministic programs for {@link LargeExtensionCatalog}; no queue, listener, packet, or server logic. */
public final class LargeExtensionClientPrograms {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double EPSILON = 1.0E-9D;
    private LargeExtensionClientPrograms() { }
    public static Program start(Request request) { return new Program(request, LargeExtensionCatalog.require(request.id())); }
    public static List<Emission> emissionsAtTick(Request request, int tick) { return emissionsAtTick(LargeExtensionCatalog.require(request.id()), request, tick); }
    public static void validateAll() {
        LargeExtensionCatalog.validate();
        for (LargeExtensionCatalog.Entry entry : LargeExtensionCatalog.values()) {
            Request request = new Request(entry.id(), 0.0D, 0.0D, 0.0D, 0x5EEDL + entry.order()); int total = 0;
            for (int tick = 0; tick < entry.budget().emissionTicks(); tick++) {
                List<Emission> emissions = emissionsAtTick(request, tick);
                if (emissions.size() != entry.budget().perTick()) throw new IllegalStateException("Per-tick budget drifted: " + entry.id());
                for (Emission emission : emissions) if (emission.distanceFrom(request) > entry.radiusBlocks() + EPSILON) throw new IllegalStateException("Envelope escape: " + entry.id());
                total += emissions.size();
            }
            if (total != entry.budget().plannedParticles()) throw new IllegalStateException("Total budget drifted: " + entry.id());
        }
    }
    private static List<Emission> emissionsAtTick(LargeExtensionCatalog.Entry entry, Request request, int tick) {
        if (tick < 0 || tick >= entry.budget().emissionTicks()) return List.of();
        List<Emission> values = new ArrayList<>(entry.budget().perTick());
        for (int index = 0; index < entry.budget().perTick(); index++) {
            Offset offset = clampToEnvelope(offset(entry, request.seed(), tick, index), entry.radiusBlocks());
            int layer = Math.floorMod(index + tick * 3, 3);
            values.add(new Emission(request.x() + offset.x, request.y() + offset.y, request.z() + offset.z, layer,
                    1.04F + layer * 0.07F, lifetime(entry, request.seed(), tick, index), index % 31 == 0, (index + tick) % 7 == 0));
        }
        return List.copyOf(values);
    }
    private static Offset offset(LargeExtensionCatalog.Entry e, long seed, int tick, int index) {
        double p = (tick + 1.0D) / e.budget().emissionTicks(), r = e.radiusBlocks(), a = TWO_PI * (index + unit(seed)) / e.budget().perTick();
        double q = TWO_PI * (tick + index * 0.17D + unit(seed ^ index)) / e.budget().emissionTicks();
        return switch (e.form()) {
            case FAN_PALM -> new Offset(Math.cos(a) * r * p, Math.sin(a) * r * .42D * p + r * .12D, Math.sin(a) * r * .36D * p);
            case CROWN_FOUNTAIN -> new Offset(Math.cos(a) * r * (.28D + .58D * p), r * (.72D - p * .92D) + Math.sin(a * 4) * r * .08D, Math.sin(a) * r * (.28D + .58D * p));
            case SCALLOP_SHELL -> new Offset(Math.cos(a) * r * p, Math.abs(Math.sin(a * 3)) * r * .58D * p - r * .18D, Math.sin(a) * r * p);
            case PETAL_LACE -> new Offset(Math.cos(a) * r * (.45D + .43D * Math.cos(a * 5)) * p, Math.sin(a * 5) * r * .34D * p, Math.sin(a) * r * (.45D + .43D * Math.cos(a * 5)) * p);
            case HEART_BLOOM -> new Offset(Math.sin(a) * r * .78D * p, (Math.cos(a) * r * .38D - Math.cos(2 * a) * r * .19D - Math.cos(3 * a) * r * .08D) * p, Math.sin(a * .5D) * r * .16D * p);
            case SPIDERWEB -> new Offset(Math.cos(a) * r * p, Math.sin(a * 4) * r * .28D * p, Math.sin(a) * r * p);
            case COMET_WHEEL -> new Offset(Math.cos(q) * r * (.25D + .68D * p), Math.sin(q * 2) * r * .22D, Math.sin(q) * r * (.25D + .68D * p));
            case DOUBLE_HELIX -> new Offset(Math.cos(q + (index & 1) * Math.PI) * r * .54D, (p - .5D) * r * 1.28D, Math.sin(q + (index & 1) * Math.PI) * r * .54D);
            case SNOWFLAKE -> new Offset(Math.cos(a) * r * p, Math.sin(a * 3) * r * .31D * p, Math.sin(a) * r * p);
            case ECLIPSE -> new Offset(Math.cos(a) * r * (.54D + .22D * Math.sin(q)), Math.sin(a) * r * .32D, Math.sin(q) * r * .17D);
            case CHRYSANTHEMUM -> new Offset(Math.cos(a) * r * p, Math.sin(a) * r * p - p * p * r * .35D, Math.sin(a + q) * r * .68D * p);
            case DIAMOND -> new Offset(Math.cos(a) * r * p, (Math.abs(Math.sin(a)) * r - r * .36D) * p, Math.sin(a) * r * p);
            case LANTERN -> new Offset(Math.cos(a) * r * (.30D + .42D * Math.sin(Math.PI * p)), (p - .5D) * r * .95D, Math.sin(a) * r * (.30D + .42D * Math.sin(Math.PI * p)));
            case REEF_BRANCH -> new Offset(Math.cos(a) * r * p, Math.sin(a * 2 + q) * r * .36D * p, Math.sin(a) * r * p);
            case GARDEN_MAZE -> new Offset(Math.cos(a) * r * (.20D + .64D * p), Math.sin(a * 6) * r * .20D, Math.sin(a * 2) * r * (.20D + .64D * p));
            case ORBIT_LATTICE -> new Offset(Math.cos(q) * r * .64D, Math.sin(q * 3) * r * .27D, Math.sin(q) * r * .64D);
            case POLAR_CROSS -> new Offset((index % 2 == 0 ? 1 : 0) * Math.cos(a) * r * p, (index % 2 == 1 ? 1 : 0) * Math.sin(a) * r * p, Math.sin(a * 2) * r * .19D);
            case TIDE_ARC -> new Offset(Math.cos(a) * r * p, Math.sin(a) * r * .23D + Math.sin(q) * r * .24D, Math.sin(a) * r * .62D * p);
            case STARBURST -> new Offset(Math.cos(a) * r * p, Math.sin(a * 5) * r * .15D + (index % 5 - 2) * r * .10D, Math.sin(a) * r * p);
            case GILDED_GATE -> new Offset(Math.signum(Math.cos(a)) * r * (.25D + .52D * p), Math.sin(a) * r * .68D * p, Math.sin(a * 2) * r * .22D);
        };
    }
    static Offset clampToEnvelope(Offset value, double radius) { double l = value.length(); return l <= radius || l < EPSILON ? value : value.scale(radius / l); }
    private static int lifetime(LargeExtensionCatalog.Entry e, long seed, int tick, int index) { int span = e.budget().maxLifetimeTicks() - e.budget().minLifetimeTicks() + 1; return e.budget().minLifetimeTicks() + (int) (unit(seed ^ ((long) tick << 32) ^ index) * span); }
    private static double unit(long value) { long z = value + 0x9E3779B97F4A7C15L; z = (z ^ z >>> 30) * 0xBF58476D1CE4E5B9L; z = (z ^ z >>> 27) * 0x94D049BB133111EBL; return ((z ^ z >>> 31) >>> 11) * 0x1.0p-53D; }
    public record Request(String id, double x, double y, double z, long seed) { public Request { if (id == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Invalid client request"); LargeExtensionCatalog.require(id); } }
    public record Emission(double x, double y, double z, int layer, float scale, int lifetimeTicks, boolean core, boolean twinkle) { public Emission { if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || layer < 0 || layer > 2 || scale <= 0 || lifetimeTicks < 1) throw new IllegalArgumentException("Invalid emission"); } double distanceFrom(Request r) { double dx=x-r.x,dy=y-r.y,dz=z-r.z; return Math.sqrt(dx*dx+dy*dy+dz*dz); } }
    public static final class Program {
        private final Request request; private final LargeExtensionCatalog.Entry entry; private int age; private int scheduled;
        private Program(Request request, LargeExtensionCatalog.Entry entry) { this.request=request; this.entry=entry; }
        public boolean tick(Minecraft minecraft) { if (minecraft == null || minecraft.level == null) return false; if (age < entry.budget().emissionTicks()) for (Emission emission : emissionsAtTick(entry, request, age)) create(minecraft, emission); age++; return age >= entry.budget().totalVisualTicks(); }
        public int scheduledParticles() { return scheduled; }
        private void create(Minecraft minecraft, Emission e) { if (++scheduled > entry.budget().plannedParticles()) throw new IllegalStateException("Particle budget exceeded"); Particle particle=FireworkParticleAppearance.createSpark(minecraft,e.x,e.y,e.z,0,0,0); if (particle == null) return; String color = e.layer==0 ? entry.palette().primary() : e.layer==1 ? entry.palette().secondary() : entry.palette().accent(); int rgb=Integer.parseInt(color.substring(1),16); particle.setParticleSpeed(0,0,0); FireworkParticleAppearance.applyVisibilityScale(particle,e.scale,e.core); FireworkParticleAppearance.applyVividColor(particle,((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F,1.03F,e.core ? .18F : .06F); particle.setLifetime(e.lifetimeTicks); if(e.twinkle && particle instanceof FireworkParticles.SparkParticle spark) spark.setTwinkle(true); }
    }
    record Offset(double x, double y, double z) { Offset scale(double v){return new Offset(x*v,y*v,z*v);} double length(){return Math.sqrt(x*x+y*y+z*z);} }
}
