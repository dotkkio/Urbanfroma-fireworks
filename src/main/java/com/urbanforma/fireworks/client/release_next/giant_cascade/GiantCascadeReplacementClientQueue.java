package com.urbanforma.fireworks.client.release_next.giant_cascade;

import java.util.ArrayDeque;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** One active and one pending client-local replacement visual; excess requests are rejected without allocation. */
public final class GiantCascadeReplacementClientQueue {
    public static final int MAX_ACTIVE_PROGRAMS = 1;
    public static final int MAX_PENDING_REQUESTS = 1;
    private final ArrayDeque<GiantCascadeReplacementClientProgram.Request> pending = new ArrayDeque<>(MAX_PENDING_REQUESTS);
    private GiantCascadeReplacementClientProgram active;

    public boolean enqueue(GiantCascadeReplacementClientProgram.Request request) {
        if (active == null) { active = new GiantCascadeReplacementClientProgram(Objects.requireNonNull(request, "request")); return true; }
        if (pending.size() == MAX_PENDING_REQUESTS) return false;
        pending.addLast(Objects.requireNonNull(request, "request"));
        return true;
    }

    public void tick(Minecraft minecraft) {
        if (minecraft.level == null) return;
        if (active != null && active.tick(minecraft)) active = null;
        if (active == null && !pending.isEmpty()) active = new GiantCascadeReplacementClientProgram(pending.removeFirst());
    }

    public void clear() { active = null; pending.clear(); }
    public int trackedVisualCount() { return (active == null ? 0 : 1) + pending.size(); }
    public GiantCascadeReplacementClientProgram activeVisual() { return active; }
}
