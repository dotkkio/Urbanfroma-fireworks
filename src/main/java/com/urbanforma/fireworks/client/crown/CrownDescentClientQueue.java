package com.urbanforma.fireworks.client.crown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** Client-local bounded ownership for crown descent programs; it is not a shared particle quota. */
public final class CrownDescentClientQueue {
    public static final int MAX_ACTIVE_PROGRAMS = 2;
    public static final int MAX_OWNED_PARTICLES = MAX_ACTIVE_PROGRAMS
            * com.urbanforma.fireworks.content.crown.CrownDescentTrajectory.LOCAL_PEAK_OWNED_PARTICLES;

    private final List<CrownDescentClientProgram> active = new ArrayList<>(MAX_ACTIVE_PROGRAMS);

    /**
     * Returns false when this effect family's own two-program envelope is already occupied.
     * No request is forwarded to common logic, the server, or the network on rejection.
     */
    public boolean enqueue(CrownDescentClientProgram.Request request) {
        Objects.requireNonNull(request, "request");
        if (this.active.size() >= MAX_ACTIVE_PROGRAMS) {
            return false;
        }
        this.active.add(new CrownDescentClientProgram(request));
        return true;
    }

    public void tick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.level == null) {
            return;
        }
        Iterator<CrownDescentClientProgram> iterator = this.active.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick(minecraft)) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        for (CrownDescentClientProgram program : this.active) {
            program.retire();
        }
        this.active.clear();
    }

    public int activeProgramCount() {
        return this.active.size();
    }

    public int activeTrackedSparkCount() {
        int count = 0;
        for (CrownDescentClientProgram program : this.active) {
            count += program.trackedSparkCount();
        }
        return count;
    }

    public CrownDescentClientProgram activeVisual() {
        return this.active.isEmpty() ? null : this.active.getFirst();
    }
}
