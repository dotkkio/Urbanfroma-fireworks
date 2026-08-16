package com.urbanforma.fireworks.client.giant.palm;

import java.util.ArrayDeque;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Client-local FIFO for giant palm requests.
 *
 * <p>Only one palm visual is active at a time. The small request queue bounds local retained request objects;
 * it is not a global particle quota and does not alter the program's fixed per-instance population.</p>
 */
public final class GiantPalmClientQueue {
    public static final int MAX_PENDING_REQUESTS = 4;

    private final ArrayDeque<GiantPalmClientProgram.Request> pending = new ArrayDeque<>();
    private GiantPalmClientProgram active;

    /** Adds a request at the tail. False means this client-local FIFO is full and nothing was allocated. */
    public boolean enqueue(GiantPalmClientProgram.Request request) {
        Objects.requireNonNull(request, "request");
        if (this.pending.size() >= MAX_PENDING_REQUESTS) {
            return false;
        }
        this.pending.addLast(request);
        return true;
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        if (this.active == null) {
            GiantPalmClientProgram.Request next = this.pending.pollFirst();
            if (next != null) {
                this.active = new GiantPalmClientProgram(next);
            }
        }
        if (this.active != null && this.active.tick(minecraft)) {
            this.active = null;
        }
    }

    public void clear() {
        this.pending.clear();
        this.active = null;
    }

    public boolean hasActiveVisual() {
        return this.active != null;
    }

    public boolean hasPendingVisual() {
        return !this.pending.isEmpty();
    }

    public int pendingVisualCount() {
        return this.pending.size();
    }

    public GiantPalmClientProgram activeVisual() {
        return this.active;
    }
}
