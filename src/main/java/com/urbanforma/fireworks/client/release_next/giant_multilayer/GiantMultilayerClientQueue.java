package com.urbanforma.fireworks.client.release_next.giant_multilayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** Client-local collection; each request stays independent and retires after its finite profile duration. */
public final class GiantMultilayerClientQueue {
    private final List<GiantMultilayerClientProgram> active = new ArrayList<>();
    public void enqueue(GiantMultilayerClientProgram.Request request) { this.active.add(new GiantMultilayerClientProgram(Objects.requireNonNull(request, "request"))); }
    public void tick(Minecraft minecraft) {
        if (minecraft.level == null) return;
        Iterator<GiantMultilayerClientProgram> iterator = this.active.iterator();
        while (iterator.hasNext()) if (iterator.next().tick(minecraft)) iterator.remove();
    }
    public void clear() { this.active.clear(); }
    public boolean hasActiveVisual() { return !this.active.isEmpty(); }
    public int activeVisualCount() { return this.active.size(); }
}
