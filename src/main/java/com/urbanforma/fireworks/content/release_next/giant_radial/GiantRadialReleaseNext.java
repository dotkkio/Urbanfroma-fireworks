package com.urbanforma.fireworks.content.release_next.giant_radial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.Vec3;

/**
 * Common-side, deterministic contracts for the release-next giant radial set.
 *
 * <p>This package deliberately has no registry, network, entity, or tick hooks. A server only needs to transmit the
 * selected stable ID, detonation position, and seed; all samples and particle accounting are calculated client-side.
 * Each variant has a fixed radius and finite emission/lifetime budget.</p>
 */
public final class GiantRadialReleaseNext {
    public static final String INTEGRATION_STATUS = "ISOLATED_NOT_REGISTERED";
    public static final String CREATIVE_TARGET = "giant";
    public static final String VANILLA_ROCKET_ITEM_PARENT = "minecraft:item/firework_rocket";
    public static final String CLIENT_ROUTE =
            "com.urbanforma.fireworks.client.release_next.giant_radial.GiantRadialReleaseNextClientProgram";
    public static final double MAX_DECLARED_RADIUS = 130.0D;
    public static final int MAX_CLIENT_PARTICLES_PER_TICK = 384;
    public static final int MAX_PARTICLES_PER_VARIANT = 12_288;
    public static final int MIN_LIFETIME = 104;
    public static final int MAX_LIFETIME = 144;

    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double EPSILON = 1.0E-7D;

    private static final List<ItemMetadata> ITEMS = List.of(
            item(Variant.AURORA_TRIPLE_RADIANT, "Giant Aurora Triple Radiant Firework", "巨大极光三重放射烟花",
                    "MULTI_RADIAL_II", "GIANT_MULTI_RADIAL_II", "#45F0DB", "#9475FF", "#F0FAFF"),
            item(Variant.CINNABAR_THICK_RADIANT, "Giant Cinnabar Thick Radiant Firework", "巨大朱砂浓密放射烟花",
                    "THICK_RADIAL", "GIANT_THICK_RADIAL", "#FF291A", "#FF941A", "#FFE88F"),
            item(Variant.JADE_OUTER_SHELL_RADIANT, "Giant Jade Outer Shell Radiant Firework", "巨大翡翠外层壳放射烟花",
                    "LARGE", "GIANT_LARGE", "#14C275", "#B3FFB8", "#FFDB42"),
            item(Variant.PLATINUM_DUAL_SHELL_RADIANT, "Giant Platinum Dual Shell Radiant Firework", "巨大铂金双层壳放射烟花",
                    "MULTI_RADIAL_II", "GIANT_MULTI_RADIAL_II", "#B8DBFF", "#FAFAFA", "#5C9EFF"),
            item(Variant.EMBER_CHRYSANTHEMUM_RADIAL, "Giant Ember Chrysanthemum Radial Firework", "巨大余烬菊花放射烟花",
                    "CHRYSANTHEMUM_MULTI_SHELL", "GIANT_LARGE", "#FF570D", "#FFB82E", "#FFF5B8"),
            item(Variant.SAPPHIRE_CROWN_SHELL_RADIANT, "Giant Sapphire Crown Shell Radiant Firework", "巨大蓝宝石冠冕壳放射烟花",
                    "LARGE", "GIANT_LARGE", "#1F6BFF", "#6BD6FF", "#E0F5FF"));
    private static final Map<String, ItemMetadata> ITEMS_BY_STABLE_ID = indexItems();

    private GiantRadialReleaseNext() {
    }

    /** Six visually distinct, stable integration IDs. Their order is not a registration index. */
    public enum Variant {
        AURORA_TRIPLE_RADIANT("giant_aurora_triple_radiant", 128, 48, 1, 128.0D, 1.00D, 0.08D,
                new Rgb(0.27F, 0.94F, 0.86F), new Rgb(0.58F, 0.46F, 1.00F), new Rgb(0.94F, 0.98F, 1.00F)),
        CINNABAR_THICK_RADIANT("giant_cinnabar_thick_radiant", 64, 56, 3, 124.0D, 0.82D, 0.18D,
                new Rgb(1.00F, 0.16F, 0.10F), new Rgb(1.00F, 0.58F, 0.10F), new Rgb(1.00F, 0.91F, 0.56F)),
        JADE_OUTER_SHELL_RADIANT("giant_jade_outer_shell_radiant", 144, 44, 1, 130.0D, 0.94D, 0.12D,
                new Rgb(0.08F, 0.76F, 0.46F), new Rgb(0.70F, 1.00F, 0.72F), new Rgb(1.00F, 0.86F, 0.26F)),
        PLATINUM_DUAL_SHELL_RADIANT("giant_platinum_dual_shell_radiant", 96, 52, 2, 126.0D, 1.00D, 0.04D,
                new Rgb(0.72F, 0.86F, 1.00F), new Rgb(0.98F, 0.98F, 1.00F), new Rgb(0.36F, 0.62F, 1.00F)),
        EMBER_CHRYSANTHEMUM_RADIAL("giant_ember_chrysanthemum_radial", 112, 48, 1, 122.0D, 0.72D, 0.26D,
                new Rgb(1.00F, 0.34F, 0.05F), new Rgb(1.00F, 0.72F, 0.18F), new Rgb(1.00F, 0.96F, 0.72F)),
        SAPPHIRE_CROWN_SHELL_RADIANT("giant_sapphire_crown_shell_radiant", 80, 56, 2, 128.0D, 0.90D, 0.10D,
                new Rgb(0.12F, 0.42F, 1.00F), new Rgb(0.42F, 0.84F, 1.00F), new Rgb(0.88F, 0.96F, 1.00F));

        private final String stableId;
        private final int branches;
        private final int segments;
        private final int strands;
        private final double radius;
        private final double verticalScale;
        private final double terminalDroop;
        private final Rgb primary;
        private final Rgb secondary;
        private final Rgb accent;

        Variant(String stableId, int branches, int segments, int strands, double radius, double verticalScale,
                double terminalDroop, Rgb primary, Rgb secondary, Rgb accent) {
            this.stableId = stableId;
            this.branches = branches;
            this.segments = segments;
            this.strands = strands;
            this.radius = radius;
            this.verticalScale = verticalScale;
            this.terminalDroop = terminalDroop;
            this.primary = primary;
            this.secondary = secondary;
            this.accent = accent;
        }

        public String stableId() { return this.stableId; }
        public int branches() { return this.branches; }
        public int segments() { return this.segments; }
        public int strands() { return this.strands; }
        public double radius() { return this.radius; }
        public int particlesPerTick() { return this.branches * this.strands; }
        public int totalParticles() { return this.particlesPerTick() * this.segments; }
        public int totalVisualTicks() { return this.segments + MAX_LIFETIME; }

        private Rgb color(int segment, int strand) {
            return segment < this.segments / 7 ? this.accent
                    : (segment + strand * 3) % 5 == 0 ? this.primary : this.secondary;
        }
    }

    public record Rgb(float red, float green, float blue) {
        public Rgb {
            if (!validChannel(red) || !validChannel(green) || !validChannel(blue)) {
                throw new IllegalArgumentException("RGB channels must be finite values from zero through one");
            }
        }
    }

    /**
     * Private handoff metadata for one future item registration. Suggested enum names are plain strings so this
     * isolated package neither imports nor modifies the shared GiantTier or EffectCategory types.
     */
    public record ItemMetadata(Variant variant, String englishName, String chineseName, String creativeTarget,
            String modelParent, RecipeTemplate recipe, String typedClientRoute, Palette palette,
            int emissionTicks, int peakParticlesPerTick, int totalParticles, double radiusBlocks,
            int minLifetimeTicks, int maxLifetimeTicks, int totalVisualTicks,
            String suggestedGiantTier, String suggestedEffectCategory) {
        public ItemMetadata {
            if (variant == null || blank(englishName) || blank(chineseName) || !CREATIVE_TARGET.equals(creativeTarget)
                    || !VANILLA_ROCKET_ITEM_PARENT.equals(modelParent) || recipe == null || !CLIENT_ROUTE.equals(typedClientRoute)
                    || palette == null || emissionTicks != variant.segments() || peakParticlesPerTick != variant.particlesPerTick()
                    || totalParticles != variant.totalParticles() || radiusBlocks != variant.radius()
                    || minLifetimeTicks != MIN_LIFETIME || maxLifetimeTicks != MAX_LIFETIME
                    || totalVisualTicks != variant.totalVisualTicks() || blank(suggestedGiantTier)
                    || blank(suggestedEffectCategory) || !recipe.outputId().equals(variant.stableId())) {
                throw new IllegalArgumentException("Invalid giant radial item metadata");
            }
        }

        public String stableId() {
            return variant.stableId();
        }
    }

    /** Recipe data only; the integrator owns the actual recipe JSON registration. */
    public record RecipeTemplate(String outputId, int outputCount, List<String> pattern, Map<String, String> ingredients) {
        public RecipeTemplate {
            if (blank(outputId) || outputCount != 1 || !List.of(" P ", "FGF", " P ").equals(pattern)
                    || !Map.of("P", "minecraft:paper", "F", "minecraft:firework_star", "G", "minecraft:gunpowder").equals(ingredients)) {
                throw new IllegalArgumentException("Giant radial recipe template drifted");
            }
            pattern = List.copyOf(pattern);
            ingredients = Map.copyOf(ingredients);
        }
    }

    /** Hex palette retained alongside normalized runtime RGB so integration need not infer display colors. */
    public record Palette(String primary, String secondary, String accent) {
        public Palette {
            if (!hex(primary) || !hex(secondary) || !hex(accent)
                    || primary.equals(secondary) || primary.equals(accent) || secondary.equals(accent)) {
                throw new IllegalArgumentException("Invalid giant radial palette");
            }
        }
    }

    public record ParticlePlan(int tick, int createdThisTick, int cumulativeCreated, int activeUpperBound) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0) {
                throw new IllegalArgumentException("Particle accounting values must be non-negative");
            }
        }
    }

    public record Sample(Vec3 position, Rgb color, int lifetime, float brightness) {
        public Sample {
            if (position == null || color == null || !finite(position) || position.lengthSqr() > MAX_DECLARED_RADIUS * MAX_DECLARED_RADIUS + EPSILON
                    || lifetime < MIN_LIFETIME || lifetime > MAX_LIFETIME || !Float.isFinite(brightness)) {
                throw new IllegalArgumentException("Giant radial sample escaped its declared contract");
            }
        }
    }

    static {
        for (Variant variant : Variant.values()) {
            if (variant.radius > MAX_DECLARED_RADIUS || variant.particlesPerTick() > MAX_CLIENT_PARTICLES_PER_TICK
                    || variant.totalParticles() > MAX_PARTICLES_PER_VARIANT || variant.segments < 2) {
                throw new IllegalStateException("Invalid giant radial release-next budget: " + variant.stableId);
            }
        }
        if (ITEMS.size() != Variant.values().length || ITEMS_BY_STABLE_ID.size() != Variant.values().length) {
            throw new IllegalStateException("Giant radial item metadata coverage drifted");
        }
    }

    public static List<ItemMetadata> items() {
        return ITEMS;
    }

    public static ItemMetadata requireItem(String stableId) {
        ItemMetadata item = ITEMS_BY_STABLE_ID.get(stableId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown giant radial stable ID: " + stableId);
        }
        return item;
    }

    /** Produces one immutable local sample. It is deterministic for an identical variant, seed, branch, segment and strand. */
    public static Sample sample(Variant variant, long seed, int branch, int segment, int strand) {
        validateIndex(variant, branch, segment, strand);
        long mixed = mix64(seed ^ ((long) variant.ordinal() * 0x9E3779B97F4A7C15L) ^ ((long) branch * 0xD6E8FEB86659FD93L));
        double fraction = ((double) branch + 0.5D) / variant.branches;
        double elevation = Math.asin(1.0D - 2.0D * fraction) * variant.verticalScale;
        double azimuth = branch * GOLDEN_ANGLE + unit(mixed ^ 0xA4093822299F31D0L) * TWO_PI;
        Vec3 direction = new Vec3(Math.cos(elevation) * Math.cos(azimuth), Math.sin(elevation), Math.cos(elevation) * Math.sin(azimuth));
        Vec3 side = new Vec3(-direction.z, 0.0D, direction.x).normalize();
        double progress = (double) segment / (variant.segments - 1);
        double radius = variant.radius * smoothStep(progress);
        double strandOffset = (strand - (variant.strands - 1) * 0.5D) * (0.35D + 1.70D * progress);
        Vec3 drooped = new Vec3(direction.x, direction.y - variant.terminalDroop * smoothStep((progress - 0.56D) / 0.44D), direction.z).normalize();
        Vec3 position = drooped.scale(radius).add(side.scale(strandOffset));
        if (position.lengthSqr() > variant.radius * variant.radius) {
            position = position.normalize().scale(variant.radius);
        }
        int lifetime = MIN_LIFETIME + (int) Math.floor(unit(mixed ^ ((long) segment << 19) ^ strand) * (MAX_LIFETIME - MIN_LIFETIME + 1));
        float brightness = 1.04F + (float) unit(mixed ^ ((long) segment << 32) ^ (strand * 31L)) * 0.24F;
        return new Sample(position, variant.color(segment, strand), lifetime, brightness);
    }

    public static ParticlePlan particlePlanAtTick(Variant variant, int tick) {
        if (variant == null || tick < 0) throw new IllegalArgumentException("Variant and tick are required");
        int perTick = tick < variant.segments ? variant.particlesPerTick() : 0;
        int cumulative = Math.min(tick + 1, variant.segments) * variant.particlesPerTick();
        int firstLiveTick = Math.max(0, tick - MAX_LIFETIME);
        int lastLiveTick = Math.min(tick, variant.segments - 1);
        int active = firstLiveTick > lastLiveTick ? 0 : (lastLiveTick - firstLiveTick + 1) * variant.particlesPerTick();
        return new ParticlePlan(tick, perTick, cumulative, active);
    }

    public static boolean fitsEnvelope(Variant variant, long seed) {
        for (int branch = 0; branch < variant.branches; branch++) for (int segment = 0; segment < variant.segments; segment++)
            for (int strand = 0; strand < variant.strands; strand++) if (sample(variant, seed, branch, segment, strand).position().lengthSqr() > variant.radius * variant.radius + EPSILON) return false;
        return true;
    }

    private static ItemMetadata item(Variant variant, String englishName, String chineseName,
            String suggestedGiantTier, String suggestedEffectCategory, String primary, String secondary, String accent) {
        return new ItemMetadata(variant, englishName, chineseName, CREATIVE_TARGET, VANILLA_ROCKET_ITEM_PARENT,
                new RecipeTemplate(variant.stableId(), 1, List.of(" P ", "FGF", " P "),
                        Map.of("P", "minecraft:paper", "F", "minecraft:firework_star", "G", "minecraft:gunpowder")),
                CLIENT_ROUTE, new Palette(primary, secondary, accent), variant.segments(), variant.particlesPerTick(),
                variant.totalParticles(), variant.radius(), MIN_LIFETIME, MAX_LIFETIME, variant.totalVisualTicks(),
                suggestedGiantTier, suggestedEffectCategory);
    }

    private static Map<String, ItemMetadata> indexItems() {
        Map<String, ItemMetadata> values = new HashMap<>();
        for (ItemMetadata item : ITEMS) {
            if (values.put(item.stableId(), item) != null) {
                throw new IllegalStateException("Duplicate giant radial stable ID: " + item.stableId());
            }
        }
        return Map.copyOf(values);
    }

    private static void validateIndex(Variant variant, int branch, int segment, int strand) {
        if (variant == null || branch < 0 || branch >= variant.branches || segment < 0 || segment >= variant.segments || strand < 0 || strand >= variant.strands) throw new IllegalArgumentException("Giant radial sample index is outside its fixed plan");
    }
    private static boolean validChannel(float value) { return Float.isFinite(value) && value >= 0.0F && value <= 1.0F; }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static boolean hex(String value) { return value != null && value.matches("#[0-9A-Fa-f]{6}"); }
    private static boolean finite(Vec3 value) { return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z); }
    private static double smoothStep(double value) { double bounded = Math.max(0.0D, Math.min(1.0D, value)); return bounded * bounded * (3.0D - 2.0D * bounded); }
    private static double unit(long value) { return (mix64(value) >>> 11) * 0x1.0p-53D; }
    private static long mix64(long value) { long mixed = value + 0x9E3779B97F4A7C15L; mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L; mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL; return mixed ^ mixed >>> 31; }
}
