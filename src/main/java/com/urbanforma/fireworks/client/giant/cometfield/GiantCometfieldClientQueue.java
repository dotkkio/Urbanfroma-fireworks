package com.urbanforma.fireworks.client.giant.cometfield;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** Bounded, client-local owner for interlaced comet-field visuals. */
public final class GiantCometfieldClientQueue {
    public static final int MAX_ACTIVE_VISUALS = 1;

    private final ArrayDeque<GiantCometfieldClientProgram> active = new ArrayDeque<>(MAX_ACTIVE_VISUALS);

    /** Rejects overlap instead of accumulating an unbounded backlog. */
    public boolean enqueue(GiantCometfieldClientProgram.Request request) {
        Objects.requireNonNull(request, "request");
        if (this.active.size() >= MAX_ACTIVE_VISUALS) {
            return false;
        }
        this.active.addLast(new GiantCometfieldClientProgram(request));
        return true;
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<GiantCometfieldClientProgram> iterator = this.active.iterator();
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

    public int activeVisualCount() {
        return this.active.size();
    }

    public GiantCometfieldClientProgram activeVisual() {
        return this.active.peekFirst();
    }
}
