package com.urbanforma.fireworks.client.giant.superwillow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** Client-local concurrent visual collection for the third giant. */
public final class SuperWillowClientQueue {
    private final List<SuperWillowClientProgram> active = new ArrayList<>();

    public void enqueue(SuperWillowClientProgram.Request request) {
        this.active.add(new SuperWillowClientProgram(Objects.requireNonNull(request, "request")));
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<SuperWillowClientProgram> iterator = this.active.iterator();
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

    public SuperWillowClientProgram activeVisual() {
        return this.active.isEmpty() ? null : this.active.getFirst();
    }
}
