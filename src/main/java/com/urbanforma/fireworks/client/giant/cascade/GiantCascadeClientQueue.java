package com.urbanforma.fireworks.client.giant.cascade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Client-local concurrent visual collection for the seventh giant.
 */
public final class GiantCascadeClientQueue {
    private final List<GiantCascadeClientProgram> active = new ArrayList<>();

    public boolean enqueue(GiantCascadeClientProgram.Request request) {
        this.active.add(new GiantCascadeClientProgram(Objects.requireNonNull(request, "request")));
        return true;
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<GiantCascadeClientProgram> iterator = this.active.iterator();
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

    public boolean hasPendingVisual() {
        return false;
    }

    public GiantCascadeClientProgram activeVisual() {
        return this.active.isEmpty() ? null : this.active.getFirst();
    }
}
