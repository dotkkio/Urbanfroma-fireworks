package com.urbanforma.fireworks.client.giant.willow;

import com.urbanforma.fireworks.content.giant.willow.GiantWillowTrajectory;
import java.util.ArrayDeque;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/**
 * Private client queue for the stable EXTRA_LARGE willow effect.
 *
 * <p>Only one local program can emit at a time. Two later requests are retained in FIFO order; further requests
 * are explicitly dropped instead of multiplying the giant's per-tick and alive-particle envelope.</p>
 */
public final class GiantWillowClientQueue {
    private final ArrayDeque<GiantWillowClientProgram.Request> pending = new ArrayDeque<>();
    private GiantWillowClientProgram active;
    private int droppedRequests;

    public void enqueue(GiantWillowClientProgram.Request request) {
        GiantWillowClientProgram.Request checked = Objects.requireNonNull(request, "request");
        if (this.active == null) {
            this.active = new GiantWillowClientProgram(checked);
        } else if (this.pending.size() < GiantWillowTrajectory.MAX_CLIENT_PENDING_REQUESTS) {
            this.pending.addLast(checked);
        } else {
            this.droppedRequests++;
        }
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        if (this.active != null && this.active.tick(minecraft)) {
            this.active = null;
        }
        this.promoteNext();
    }

    public void clear() {
        this.pending.clear();
        this.active = null;
        this.droppedRequests = 0;
    }

    public boolean hasActiveVisual() {
        return this.active != null;
    }

    public int activeVisualCount() {
        return this.active == null ? 0 : GiantWillowTrajectory.MAX_CLIENT_ACTIVE_PROGRAMS;
    }

    public int queuedVisualCount() {
        return this.pending.size();
    }

    public int droppedRequestCount() {
        return this.droppedRequests;
    }

    public GiantWillowClientProgram activeVisual() {
        return this.active;
    }

    private void promoteNext() {
        if (this.active == null && !this.pending.isEmpty()) {
            this.active = new GiantWillowClientProgram(this.pending.removeFirst());
        }
    }
}
