package com.urbanforma.fireworks.registry;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.GiantTier;
import com.urbanforma.fireworks.content.IntegratedFireworkCatalog;
import com.urbanforma.fireworks.content.OtherExtraFireworkCatalog;
import com.urbanforma.fireworks.content.OtherFireworkCatalog;
import com.urbanforma.fireworks.content.MidsizeFireworkCatalog;
import com.urbanforma.fireworks.content.ReleaseNextFireworkCatalog;
import com.urbanforma.fireworks.content.midsize.MidsizeFireworkDefinition;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionCatalog;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
import com.urbanforma.fireworks.content.small.SmallFireworkCatalog;
import com.urbanforma.fireworks.content.small.SmallFireworkDefinition;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.Section;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.SectionPattern;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.SectionTheme;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.SectionTone;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

/** Typed section builders for the four independent Fireworks functional categories. */
public final class FireworkCreativeSections {
    private static final String TRANSLATION_PREFIX = "gui.urbanforma_fireworks.section.fireworks.";
    private static final SectionTheme THEME =
            new SectionTheme(SectionTone.DARK_PANEL, SectionPattern.HORIZONTAL_BARS);

    private FireworkCreativeSections() {
    }

    /** Large Fireworks only: legacy shape sections, the large-extra section, then Other. */
    public static List<Section> sections() {
        return List.of(
                section("sphere", style -> style.shape() == FireworkStyle.Shape.SPHERE && isLargeStyle(style)),
                section("double_sphere", style -> style.shape() == FireworkStyle.Shape.DOUBLE_SPHERE
                        && isLargeStyle(style)),
                section("crown_sphere", style -> style.shape() == FireworkStyle.Shape.CROWN_SPHERE
                        && isLargeStyle(style)),
                section("willow", style -> style.shape() == FireworkStyle.Shape.WILLOW_SPHERE
                        && isLargeStyle(style)),
                section("radiant", style -> style.shape() == FireworkStyle.Shape.RADIANT
                        && isLargeStyle(style)),
                section("radiant_willow", style -> style.shape() == FireworkStyle.Shape.RADIANT_WILLOW
                        && isLargeStyle(style)),
                section("hybrid", style -> style.shape() == FireworkStyle.Shape.HYBRID_SPHERE_RADIANT
                        && isLargeStyle(style)),
                section("saturn", style -> style.shape() == FireworkStyle.Shape.SATURN
                        && isLargeStyle(style)),
                section("large", style -> IntegratedFireworkCatalog.contains(style.id())
                        && IntegratedFireworkCatalog.require(style.id()).kind()
                                == IntegratedFireworkCatalog.Kind.LARGE_EXTRA
                        || isReleaseNext(style, ReleaseNextFireworkCatalog.Kind.LARGE)),
                section("other", otherStyles()));
    }

    /** The independent small category keeps its two own shape subsections. */
    public static List<Section> smallSections() {
        return List.of(
                section("small_sphere", style -> style.id().equals("small_layered_sphere_firework")
                        || isReleaseNext(style, ReleaseNextFireworkCatalog.Kind.SMALL_SPHERE)),
                section("small_shapes", style -> isReleaseNext(style, ReleaseNextFireworkCatalog.Kind.SMALL_SHAPE)),
                section("small_radiant", style -> style.id().equals("small_compact_radial_firework")));
    }

    /** Medium category: the old two trials and both 25-entry typed batches share the same taxonomy. */
    public static List<Section> midsizeSections() {
        return List.of(
                section("sphere", style -> isMidsizeStyle(style)
                        && (style.id().equals("midsize_dense_sphere_firework")
                        || IntegratedFireworkCatalog.contains(style.id())
                        && IntegratedFireworkCatalog.require(style.id()).kind()
                                == IntegratedFireworkCatalog.Kind.MEDIUM_SPHERE
                        || isReleaseNextMedium(style, MediumExtensionDefinition.Category.SPHERE))),
                section("radiant", style -> isMidsizeStyle(style)
                        && (style.id().equals("midsize_dense_radial_firework")
                        || IntegratedFireworkCatalog.contains(style.id())
                        && IntegratedFireworkCatalog.require(style.id()).kind()
                                == IntegratedFireworkCatalog.Kind.MEDIUM_RADIAL
                        || isReleaseNextMedium(style, MediumExtensionDefinition.Category.RADIAL))),
                section("ring_core", style -> isReleaseNextMedium(style, MediumExtensionDefinition.Category.RING_CORE)),
                section("short_willow", style -> isReleaseNextMedium(style, MediumExtensionDefinition.Category.SHORT_WILLOW)),
                section("pulse", style -> isReleaseNextMedium(style, MediumExtensionDefinition.Category.PULSE)),
                section("interleaved_shell", style -> isReleaseNextMedium(style, MediumExtensionDefinition.Category.INTERLEAVED_SHELL)));
    }

    /** Giant category: every giant style is assigned to one explicit tier subsection. */
    public static List<Section> giantSections() {
        return List.of(
                giantSection("giant_radial", GiantTier.LARGE, GiantTier.THICK_RADIAL),
                giantSection("giant_willow", GiantTier.EXTRA_LARGE, GiantTier.SUPER_WILLOW),
                giantSection("giant_multilayer", GiantTier.MULTI_RADIAL_II),
                giantSection(
                        "giant_other",
                        GiantTier.CASCADE,
                        GiantTier.PALM,
                        GiantTier.SPIRAL,
                        GiantTier.CHRYSANTHEMUM_MULTI_SHELL,
                        GiantTier.INTERLACED_COMET_FIELD));
    }

    private static Section giantSection(String key, GiantTier... tiers) {
        return section(key, style -> {
            if (style.giantTier() == GiantTier.NONE) {
                return false;
            }
            for (GiantTier tier : tiers) {
                if (style.giantTier() == tier) {
                    return true;
                }
            }
            return false;
        });
    }

    private static boolean isLargeStyle(FireworkStyle style) {
        return style.giantTier() == GiantTier.NONE
                && !isMidsizeStyle(style)
                && !IntegratedFireworkCatalog.contains(style.id())
                && !ReleaseNextFireworkCatalog.contains(style.id());
    }

    private static boolean isMidsizeStyle(FireworkStyle style) {
        if (style.index() >= MidsizeFireworkCatalog.FIRST_STYLE_INDEX
                && style.index() < MidsizeFireworkCatalog.FIRST_STYLE_INDEX
                        + MidsizeFireworkCatalog.MIDSIZE_STYLE_COUNT) {
            return true;
        }
        return IntegratedFireworkCatalog.contains(style.id())
                && (IntegratedFireworkCatalog.require(style.id()).kind()
                        == IntegratedFireworkCatalog.Kind.MEDIUM_SPHERE
                        || IntegratedFireworkCatalog.require(style.id()).kind()
                        == IntegratedFireworkCatalog.Kind.MEDIUM_RADIAL)
                || isReleaseNext(style, ReleaseNextFireworkCatalog.Kind.MEDIUM);
    }

    private static boolean isReleaseNext(FireworkStyle style, ReleaseNextFireworkCatalog.Kind kind) {
        return ReleaseNextFireworkCatalog.contains(style.id())
                && ReleaseNextFireworkCatalog.require(style.id()).kind() == kind;
    }

    private static boolean isReleaseNextMedium(FireworkStyle style, MediumExtensionDefinition.Category category) {
        return isReleaseNext(style, ReleaseNextFireworkCatalog.Kind.MEDIUM)
                && MediumExtensionCatalog.byId(style.id()).category() == category;
    }

    private static List<FireworkStyle> otherStyles() {
        return java.util.stream.Stream.concat(
                        OtherFireworkCatalog.entries().stream().map(OtherFireworkCatalog.Entry::style),
                        OtherExtraFireworkCatalog.entries().stream().map(OtherExtraFireworkCatalog.Entry::style))
                .toList();
    }

    private static Section section(String key, Predicate<FireworkStyle> predicate) {
        return section(key, FireworkStyle.values().stream().filter(predicate).toList());
    }

    private static Section section(String key, List<FireworkStyle> styles) {
        List<ItemStack> stacks = styles.stream()
                .map(FireworksItems::itemFor)
                .map(item -> item.getDefaultInstance())
                .toList();
        List<String> paths = styles.stream().map(FireworkStyle::id).toList();
        return new Section(TRANSLATION_PREFIX + key, stacks, paths, THEME);
    }
}
