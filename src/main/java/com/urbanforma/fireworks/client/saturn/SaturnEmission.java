package com.urbanforma.fireworks.client.saturn;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.saturn.SaturnGeometry;
import net.minecraft.world.phys.Vec3;

/** Client-neutral emission data; the shared client scheduler may consume this record later. */
public record SaturnEmission(
        int tick,
        SaturnGeometry.Kind kind,
        String sourceId,
        int sampleIndex,
        int visualLayer,
        Vec3 position,
        Vec3 normal,
        FireworkStyle.Rgb color,
        int lifetimeTicks) {
    public SaturnEmission {
        if (tick < 0 || kind == null || sourceId == null || sourceId.isBlank() || sampleIndex < 0
                || visualLayer < 0 || position == null || normal == null || color == null || lifetimeTicks <= 0) {
            throw new IllegalArgumentException("Invalid Saturn client emission");
        }
    }
}
