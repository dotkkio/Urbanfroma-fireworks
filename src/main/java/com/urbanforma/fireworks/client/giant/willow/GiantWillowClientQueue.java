package com.urbanforma.fireworks.client.giant.willow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** Client-local concurrent visual collection for the second giant. */
public final class GiantWillowClientQueue {
    private final List<GiantWillowClientProgram> active = new ArrayList<>();

    public void enqueue(GiantWillowClientProgram.Request request) {
        this.active.add(new GiantWillowClientProgram(Objects.requireNonNull(request, "request")));
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<GiantWillowClientProgram> iterator = this.active.iterator();
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

    public GiantWillowClientProgram activeVisual() {
        return this.active.isEmpty() ? null : this.active.getFirst();
    }
}
