package com.urbanforma.fireworks.content.release_next.giant_willow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Isolated registration handoff for the five release-next giant willow items.
 *
 * <p>This is metadata only. Shared item registration, language entries, creative-tab assembly, recipe emission,
 * network payload routing, and client scheduling remain with their respective integration owners.</p>
 */
public final class GiantWillowReleaseNextCatalog {
    public static final String BATCH_ID = "release_next:giant_willow";
    public static final String INTEGRATION_STATUS = "ISOLATED_NOT_REGISTERED";
    public static final String CREATIVE_SECTION = "giant";
    public static final int REQUIRED_ENTRY_COUNT = 5;
    public static final String CLIENT_PROGRAM_CLASS =
            "com.urbanforma.fireworks.client.release_next.giant_willow.GiantWillowReleaseNextClientProgram";

    private static final List<Entry> VALUES = List.of(
            entry(1, "giant_imperial_canopy_willow", "\u5de8\u578b\u5e1d\u738b\u91d1\u51a0\u5782\u67f3\u70df\u82b1", "Giant Imperial Canopy Willow Firework",
                    GiantWillowReleaseNextTrajectory.Profile.IMPERIAL_CANOPY, "#FFA829", "#FFD36A", "#FFF0C2",
                    SuggestedTier.SUPER_WILLOW, SuggestedCategory.GIANT_SUPER_WILLOW, "giant_superwillow_firework"),
            entry(2, "giant_silver_waterfall_willow", "\u5de8\u578b\u94f6\u7011\u5782\u67f3\u70df\u82b1", "Giant Silver Waterfall Willow Firework",
                    GiantWillowReleaseNextTrajectory.Profile.SILVER_WATERFALL, "#96B9D0", "#D1EFFF", "#FFFFFF",
                    SuggestedTier.EXTRA_LARGE, SuggestedCategory.GIANT_EXTRA_LARGE, "polar_silver_willow"),
            entry(3, "giant_emerald_braided_willow", "\u5de8\u578b\u7fe1\u7fe0\u7f16\u8faf\u5782\u67f3\u70df\u82b1", "Giant Emerald Braided Willow Firework",
                    GiantWillowReleaseNextTrajectory.Profile.EMERALD_BRAID, "#0BCF6E", "#33FF94", "#DFFFF0",
                    SuggestedTier.SUPER_WILLOW, SuggestedCategory.GIANT_SUPER_WILLOW, "jade_pearl_willow"),
            entry(4, "giant_amber_fountain_willow", "\u5de8\u578b\u7425\u73c0\u55b7\u6cc9\u5782\u67f3\u70df\u82b1", "Giant Amber Fountain Willow Firework",
                    GiantWillowReleaseNextTrajectory.Profile.AMBER_FOUNTAIN, "#FF6B14", "#FFB12A", "#FFF0B2",
                    SuggestedTier.EXTRA_LARGE, SuggestedCategory.GIANT_EXTRA_LARGE, "giant_amber_radiant_firework"),
            entry(5, "giant_violet_rain_curtain_willow", "\u5de8\u578b\u7d2b\u7f57\u5170\u96e8\u5e55\u5782\u67f3\u70df\u82b1", "Giant Violet Rain Curtain Willow Firework",
                    GiantWillowReleaseNextTrajectory.Profile.VIOLET_RAIN_CURTAIN, "#6D34D4", "#B057FF", "#F3DDFF",
                    SuggestedTier.SUPER_WILLOW, SuggestedCategory.GIANT_SUPER_WILLOW, "violet_champagne_radiant"));
    private static final Map<String, Entry> BY_STABLE_ID = indexByStableId();

    static {
        validate();
    }

    private GiantWillowReleaseNextCatalog() {
    }

    public static List<Entry> values() {
        return VALUES;
    }

    public static Entry require(String stableId) {
        Entry entry = BY_STABLE_ID.get(stableId);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown release-next giant willow id: " + stableId);
        }
        return entry;
    }

    public static void validate() {
        if (VALUES.size() != REQUIRED_ENTRY_COUNT) {
            throw new IllegalStateException("Giant willow entry count drifted");
        }
        Map<GiantWillowReleaseNextTrajectory.Profile, Entry> profiles = new HashMap<>();
        for (Entry entry : VALUES) {
            if (profiles.put(entry.profile(), entry) != null || entry.budget().peakParticlesPerTick()
                    > GiantWillowReleaseNextTrajectory.MAX_CLIENT_PARTICLES_PER_TICK
                    || entry.envelope().horizontalRadius() > GiantWillowReleaseNextTrajectory.MAX_HORIZONTAL_RADIUS
                    || entry.envelope().maximumDistance() != GiantWillowReleaseNextTrajectory.MAX_ENVELOPE_DISTANCE) {
                throw new IllegalStateException("Giant willow metadata contract drifted: " + entry.stableId());
            }
        }
        if (profiles.size() != GiantWillowReleaseNextTrajectory.Profile.values().length) {
            throw new IllegalStateException("A giant willow trajectory profile has no item metadata");
        }
        GiantWillowReleaseNextTrajectory.validateAllProfiles();
    }

    private static Entry entry(int order, String stableId, String zhName, String enName,
            GiantWillowReleaseNextTrajectory.Profile profile, String primary, String secondary, String accent,
            SuggestedTier tier, SuggestedCategory category, String texture) {
        return new Entry(order, stableId, zhName, enName, profile, new Palette(primary, secondary, accent), tier, category,
                ClientRoute.GIANT_WILLOW_RELEASE_NEXT, new ItemPlan(
                        "release_next/giant_willow/models/item/" + stableId + ".json",
                        "models/item/" + stableId + ".json", texture, ShapedRecipe.giant(stableId)));
    }

    private static Map<String, Entry> indexByStableId() {
        Map<String, Entry> entries = new HashMap<>();
        for (Entry entry : VALUES) {
            if (entries.put(entry.stableId(), entry) != null) {
                throw new IllegalStateException("Duplicate release-next giant willow id: " + entry.stableId());
            }
        }
        return Map.copyOf(entries);
    }

    /** Suggested names match the existing shared enums but intentionally do not mutate or import them. */
    public enum SuggestedTier {
        EXTRA_LARGE,
        SUPER_WILLOW
    }

    /** Suggested values match the existing shared effect taxonomy without taking ownership of its enum. */
    public enum SuggestedCategory {
        GIANT_EXTRA_LARGE,
        GIANT_SUPER_WILLOW
    }

    /** The coordinator routes this enum and profile rather than inferring a client visual from an item-id prefix. */
    public enum ClientRoute {
        GIANT_WILLOW_RELEASE_NEXT
    }

    public record Entry(int order, String stableId, String zhName, String enName,
            GiantWillowReleaseNextTrajectory.Profile profile, Palette palette, SuggestedTier suggestedTier,
            SuggestedCategory suggestedCategory, ClientRoute clientRoute, ItemPlan itemPlan) {
        public Entry {
            if (order < 1 || stableId == null || !stableId.matches("giant_[a-z0-9_]+_willow") || zhName == null
                    || zhName.isBlank() || enName == null || enName.isBlank() || profile == null || palette == null
                    || suggestedTier == null || suggestedCategory == null || clientRoute == null || itemPlan == null
                    || !stableId.equals(profile.stableId())) {
                throw new IllegalArgumentException("Invalid release-next giant willow entry");
            }
            if (suggestedTier == SuggestedTier.EXTRA_LARGE && suggestedCategory != SuggestedCategory.GIANT_EXTRA_LARGE
                    || suggestedTier == SuggestedTier.SUPER_WILLOW && suggestedCategory != SuggestedCategory.GIANT_SUPER_WILLOW) {
                throw new IllegalArgumentException("Giant willow suggested tier/category mismatch: " + stableId);
            }
        }

        public String itemId() {
            return "urbanforma_fireworks:" + stableId;
        }

        public String translationKey() {
            return "item.urbanforma_fireworks." + stableId;
        }

        public Budget budget() {
            return new Budget(profile.branches(), profile.segments(), profile.startTick(),
                    GiantWillowReleaseNextTrajectory.lastEmissionTick(profile), profile.declaredParticles(),
                    GiantWillowReleaseNextTrajectory.particlesAtTick(profile, profile.startTick()),
                    GiantWillowReleaseNextTrajectory.MIN_LIFETIME, GiantWillowReleaseNextTrajectory.MAX_LIFETIME,
                    GiantWillowReleaseNextTrajectory.totalVisualTicks(profile));
        }

        public Envelope envelope() {
            return new Envelope(profile.radius(), profile.drop(), profile.lift(),
                    GiantWillowReleaseNextTrajectory.MAX_ENVELOPE_DISTANCE);
        }
    }

    public record Palette(String primary, String secondary, String accent) {
        public Palette {
            if (!hex(primary) || !hex(secondary) || !hex(accent) || primary.equals(secondary) || primary.equals(accent)
                    || secondary.equals(accent)) {
                throw new IllegalArgumentException("Invalid giant willow palette");
            }
        }
    }

    public record Budget(int branches, int segmentsPerBranch, int firstEmissionTick, int lastEmissionTick,
            int totalParticles, int peakParticlesPerTick, int minimumLifetimeTicks, int maximumLifetimeTicks,
            int totalVisualTicks) {
        public Budget {
            if (branches <= 0 || segmentsPerBranch <= 0 || firstEmissionTick < 0 || lastEmissionTick < firstEmissionTick
                    || totalParticles != branches * segmentsPerBranch || peakParticlesPerTick != branches
                    || minimumLifetimeTicks <= 0 || maximumLifetimeTicks < minimumLifetimeTicks
                    || totalVisualTicks != lastEmissionTick + maximumLifetimeTicks + 1) {
                throw new IllegalArgumentException("Invalid giant willow finite budget");
            }
        }
    }

    public record Envelope(double horizontalRadius, double verticalDrop, double initialLift, double maximumDistance) {
        public Envelope {
            if (!Double.isFinite(horizontalRadius) || !Double.isFinite(verticalDrop) || !Double.isFinite(initialLift)
                    || !Double.isFinite(maximumDistance) || horizontalRadius <= 0.0D || verticalDrop <= 0.0D
                    || maximumDistance < horizontalRadius || maximumDistance < verticalDrop) {
                throw new IllegalArgumentException("Invalid giant willow envelope");
            }
        }
    }

    public record ItemPlan(String modelDescriptorPath, String integrationTargetModelPath, String textureId,
            ShapedRecipe recipe) {
        public ItemPlan {
            if (modelDescriptorPath == null || !modelDescriptorPath.matches("release_next/giant_willow/models/item/giant_[a-z0-9_]+_willow\\.json")
                    || integrationTargetModelPath == null || !integrationTargetModelPath.matches("models/item/giant_[a-z0-9_]+_willow\\.json")
                    || textureId == null || !textureId.matches("[a-z0-9_]+") || recipe == null) {
                throw new IllegalArgumentException("Invalid giant willow item plan");
            }
        }
    }

    public record ShapedRecipe(List<String> pattern, Map<String, String> key, String resultId, int resultCount) {
        public ShapedRecipe {
            pattern = List.copyOf(Objects.requireNonNull(pattern, "pattern"));
            key = Map.copyOf(Objects.requireNonNull(key, "key"));
            if (pattern.size() != 3 || pattern.stream().anyMatch(row -> row == null || row.length() != 3)
                    || !pattern.equals(List.of(" P ", "FGF", " P ")) || !key.equals(Map.of(
                    "P", "minecraft:paper", "F", "minecraft:firework_star", "G", "minecraft:gunpowder"))
                    || resultId == null || !resultId.matches("urbanforma_fireworks:giant_[a-z0-9_]+_willow") || resultCount != 1) {
                throw new IllegalArgumentException("Invalid giant willow 3x3 recipe plan");
            }
        }

        private static ShapedRecipe giant(String stableId) {
            return new ShapedRecipe(List.of(" P ", "FGF", " P "), Map.of(
                    "P", "minecraft:paper", "F", "minecraft:firework_star", "G", "minecraft:gunpowder"),
                    "urbanforma_fireworks:" + stableId, 1);
        }
    }

    private static boolean hex(String value) {
        return value != null && value.matches("#[0-9A-Fa-f]{6}");
    }
}
