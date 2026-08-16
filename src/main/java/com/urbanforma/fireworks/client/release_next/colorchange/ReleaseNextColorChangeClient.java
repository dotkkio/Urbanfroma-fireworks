package com.urbanforma.fireworks.client.release_next.colorchange;

import com.urbanforma.fireworks.content.release_next.colorchange.ReleaseNextColorChangeCatalog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.particle.Particle;

/** Client-only finite recoloring sessions for particles that have already been emitted. */
public final class ReleaseNextColorChangeClient {
    private static final int MAX_TRACKED_PARTICLES = 4_800;
    private static final int TRANSITION_TICKS = 1;
    private final ReleaseNextColorChangeCatalog.Recipe recipe;
    private final List<Tracked> particles = new ArrayList<>();
    private int age;
    private boolean sealed;

    private ReleaseNextColorChangeClient(ReleaseNextColorChangeCatalog.Recipe recipe) {
        this.recipe = recipe;
    }

    public static ReleaseNextColorChangeClient begin(String id) {
        ReleaseNextColorChangeCatalog.Recipe recipe = ReleaseNextColorChangeCatalog.byId(id);
        return recipe == null ? null : new ReleaseNextColorChangeClient(recipe);
    }

    public boolean track(Particle particle) {
        if (particle == null || sealed || particles.size() >= MAX_TRACKED_PARTICLES) return false;
        particles.add(new Tracked(particle, age));
        return true;
    }

    public void seal() { sealed = true; }

    public boolean tick() {
        age++;
        Iterator<Tracked> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Tracked tracked = iterator.next();
            if (!tracked.particle.isAlive()) { iterator.remove(); continue; }
            int particleAge = age - tracked.createdAt;
            if (particleAge < recipe.switchTick()) {
                set(tracked.particle, recipe.initial());
            } else if (particleAge == recipe.switchTick()) {
                set(tracked.particle, recipe.transition());
            } else if (particleAge == recipe.switchTick() + TRANSITION_TICKS) {
                set(tracked.particle, recipe.target());
            }
            if (particleAge >= recipe.lifetimeTicks()) iterator.remove();
        }
        return sealed && particles.isEmpty() || age > recipe.lifetimeTicks() + recipe.switchTick();
    }

    public int trackedCount() { return particles.size(); }
    public int age() { return age; }
    public ReleaseNextColorChangeCatalog.Recipe recipe() { return recipe; }

    private static void set(Particle particle, ReleaseNextColorChangeCatalog.Color color) {
        particle.setColor(color.red(), color.green(), color.blue());
    }

    private record Tracked(Particle particle, int createdAt) { }
}
