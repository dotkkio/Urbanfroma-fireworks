package com.urbanforma.fireworks.registry;

import com.urbanforma.fireworks.content.FireworkStyle;
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
                section("radiant_willow", FireworkStyle.Shape.RADIANT_WILLOW));
    }

    private static Section section(String key, FireworkStyle.Shape shape) {
        List<FireworkStyle> styles = FireworkStyle.values().stream()
                .filter(style -> style.shape() == shape)
                .toList();
        return section(key, styles);
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
