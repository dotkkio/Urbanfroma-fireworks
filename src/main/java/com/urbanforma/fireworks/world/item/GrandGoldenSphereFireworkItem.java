package com.urbanforma.fireworks.world.item;

import com.urbanforma.fireworks.content.FireworkStyle;
import net.minecraft.world.item.Item;

/**
 * Compatibility type retained for the original public item field. Its behavior is wholly inherited from the
 * style-driven launcher used by every other series item.
 */
@Deprecated(forRemoval = false)
public final class GrandGoldenSphereFireworkItem extends FireworkRocketItem {
    public GrandGoldenSphereFireworkItem(Item.Properties properties) {
        super(FireworkStyle.GRAND_GOLDEN_SPHERE, properties);
    }
}
