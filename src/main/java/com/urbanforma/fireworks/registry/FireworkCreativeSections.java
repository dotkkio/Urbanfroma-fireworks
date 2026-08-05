package com.urbanforma.fireworks.registry;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.OtherFireworkCatalog;
import com.urbanforma.fireworks.content.OtherExtraFireworkCatalog;
import com.urbanforma.fireworks.content.MidsizeFireworkCatalog;
import com.urbanforma.fireworks.content.midsize.MidsizeFireworkDefinition;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.Section;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.SectionPattern;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.SectionTheme;
import com.urbanforma.neo.client.UrbanformaCreativeCategory.SectionTone;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Shape-only sections displayed inside the Urbanforma:Fireworks functional category. */
public final class FireworkCreativeSections {
    private static final String TRANSLATION_PREFIX = "gui.urbanforma_fireworks.section.fireworks.";
    private static final SectionTheme THEME =
            new SectionTheme(SectionTone.DARK_PANEL, SectionPattern.HORIZONTAL_BARS);

    private FireworkCreativeSections() {
    }

    public static List<Section> sections() {
        return List.of(
                section("sphere", FireworkStyle.Shape.SPHERE),
                section("double_sphere", FireworkStyle.Shape.DOUBLE_SPHERE),
                section("crown_sphere", FireworkStyle.Shape.CROWN_SPHERE),
                section("willow", FireworkStyle.Shape.WILLOW_SPHERE),
                section("radiant", FireworkStyle.Shape.RADIANT),
                section("radiant_willow", FireworkStyle.Shape.RADIANT_WILLOW),
                section("giant_radiant", FireworkStyle.Shape.GIANT_RADIANT),
                section("hybrid", FireworkStyle.Shape.HYBRID_SPHERE_RADIANT),
                section("saturn", FireworkStyle.Shape.SATURN),
                section("other", otherStyles()));
    }

    /** Keeps midsize trial entries in their own top-level category using the shared shape taxonomy. */
    public static List<Section> midsizeSections() {
        return List.of(
                midsizeSection(MidsizeFireworkDefinition.EffectType.DENSE_SPHERE),
                midsizeSection(MidsizeFireworkDefinition.EffectType.DENSE_RADIAL));
    }

    private static Section midsizeSection(MidsizeFireworkDefinition.EffectType effectType) {
        List<FireworkStyle> styles = MidsizeFireworkCatalog.entries().stream()
                .filter(entry -> entry.source().effectType() == effectType)
                .map(MidsizeFireworkCatalog.Entry::style)
                .toList();
        return section(effectType.creativeSection(), styles);
    }

    private static Section section(String key, FireworkStyle.Shape shape) {
        List<FireworkStyle> styles = FireworkStyle.values().stream()
                .filter(style -> style.shape() == shape && !isOther(style) && !isMidsize(style))
                .toList();
        return section(key, styles);
    }

    private static boolean isOther(FireworkStyle style) {
        return style.index() >= OtherFireworkCatalog.FIRST_STYLE_INDEX
                && style.index() < OtherExtraFireworkCatalog.FIRST_STYLE_INDEX
                        + OtherExtraFireworkCatalog.OTHER_EXTRA_STYLE_COUNT;
    }

    private static boolean isMidsize(FireworkStyle style) {
        return style.index() >= MidsizeFireworkCatalog.FIRST_STYLE_INDEX
                && style.index() < MidsizeFireworkCatalog.FIRST_STYLE_INDEX + MidsizeFireworkCatalog.MIDSIZE_STYLE_COUNT;
    }

    private static List<FireworkStyle> otherStyles() {
        return java.util.stream.Stream.concat(
                        OtherFireworkCatalog.entries().stream().map(OtherFireworkCatalog.Entry::style),
                        OtherExtraFireworkCatalog.entries().stream().map(OtherExtraFireworkCatalog.Entry::style))
                .toList();
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
