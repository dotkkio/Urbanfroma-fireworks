package com.urbanforma.fireworks.client.giant.spiral;

import java.util.ArrayDeque;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Client-local FIFO for giant spiral requests.
 *
 * <p>One active visual plus a bounded request-only queue prevents concurrent instances from compounding. This is
 * not a shared particle limiter: every accepted request still emits its complete deterministic 9,024-particle plan.</p>
 */
public final class GiantSpiralClientQueue {
    public static final int MAX_PENDING_REQUESTS = 4;

    private final ArrayDeque<GiantSpiralClientProgram.Request> pending = new ArrayDeque<>();
    private GiantSpiralClientProgram active;

    /** Adds a request at the FIFO tail. False means no local request object was retained. */
    public boolean enqueue(GiantSpiralClientProgram.Request request) {
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
            GiantSpiralClientProgram.Request next = this.pending.pollFirst();
            if (next != null) {
                this.active = new GiantSpiralClientProgram(next);
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

    public GiantSpiralClientProgram activeVisual() {
        return this.active;
    }
}
