package com.urbanforma.fireworks.client.giant.multiradial2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** Client-local concurrent visual collection for the fifth giant. */
public final class GiantMultiRadial2ClientQueue {
    private final List<GiantMultiRadial2ClientProgram> active = new ArrayList<>();

    public void enqueue(GiantMultiRadial2ClientProgram.Request request) {
        this.active.add(new GiantMultiRadial2ClientProgram(Objects.requireNonNull(request, "request")));
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<GiantMultiRadial2ClientProgram> iterator = this.active.iterator();
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

    public GiantMultiRadial2ClientProgram activeVisual() {
        return this.active.isEmpty() ? null : this.active.getFirst();
    }
}
