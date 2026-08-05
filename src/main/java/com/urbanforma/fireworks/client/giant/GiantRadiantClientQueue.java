package com.urbanforma.fireworks.client.giant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Instance-owned client visual collection for giant radiant events.
 */
public final class GiantRadiantClientQueue {
    private final List<GiantRadiantClientProgram> active = new ArrayList<>();

    public void enqueue(GiantRadiantClientProgram.Request request) {
        this.active.add(new GiantRadiantClientProgram(Objects.requireNonNull(request, "request")));
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<GiantRadiantClientProgram> iterator = this.active.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick(minecraft)) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        this.active.clear();
    }

    public boolean hasActiveVisual() {
        return !this.active.isEmpty();
    }

    public int queuedVisualCount() {
        return 0;
    }

    public GiantRadiantClientProgram activeVisual() {
        return this.active.isEmpty() ? null : this.active.getFirst();
    }
}
