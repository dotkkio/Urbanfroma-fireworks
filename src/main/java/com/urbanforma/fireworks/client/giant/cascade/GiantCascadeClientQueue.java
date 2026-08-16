package com.urbanforma.fireworks.client.giant.cascade;

import java.util.ArrayDeque;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Bounded client-local visual queue for the seventh giant.
 *
 * <p>One program owns the 768-particles-per-tick cascade budget. At most one later request can wait behind it;
 * further client requests are rejected without allocating particles, entities, or network traffic.</p>
 */
public final class GiantCascadeClientQueue {
    public static final int MAX_ACTIVE_PROGRAMS = 1;
    public static final int MAX_PENDING_REQUESTS = 1;
    public static final int MAX_TRACKED_REQUESTS = MAX_ACTIVE_PROGRAMS + MAX_PENDING_REQUESTS;

    private final ArrayDeque<GiantCascadeClientProgram.Request> pending = new ArrayDeque<>(MAX_PENDING_REQUESTS);
    private GiantCascadeClientProgram active;

    /**
     * Starts one visual immediately, buffers one later visual, and rejects further requests locally.
     *
     * @return whether this request is represented by the bounded client queue
     */
    public boolean enqueue(GiantCascadeClientProgram.Request request) {
        GiantCascadeClientProgram.Request checked = Objects.requireNonNull(request, "request");
        if (this.active == null) {
            this.active = new GiantCascadeClientProgram(checked);
            return true;
        }
        if (this.pending.size() >= MAX_PENDING_REQUESTS) {
            return false;
        }
        this.pending.addLast(checked);
        return true;
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        if (this.active != null && this.active.tick(minecraft)) {
            this.active = null;
        }
        if (this.active == null && !this.pending.isEmpty()) {
            this.active = new GiantCascadeClientProgram(this.pending.removeFirst());
        }
    }

    public void clear() {
        this.active = null;
        this.pending.clear();
    }

    public boolean hasActiveVisual() {
        return this.active != null;
    }

    public boolean hasPendingVisual() {
        return !this.pending.isEmpty();
    }

    public int queuedVisualCount() {
        return this.pending.size();
    }

    public int trackedVisualCount() {
        return (this.active == null ? 0 : 1) + this.pending.size();
    }

    public GiantCascadeClientProgram activeVisual() {
        return this.active;
    }
}
