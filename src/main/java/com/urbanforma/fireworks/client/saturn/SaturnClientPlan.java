package com.urbanforma.fireworks.client.saturn;

import com.urbanforma.fireworks.content.saturn.SaturnGeometry;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic client-side view of a Saturn program. It creates no particles and owns no event listener; callers
 * can hand one tick of this plan to the shared scheduler while preserving sphere-before-ring visual hierarchy.
 */
public final class SaturnClientPlan {
    private final SaturnProgram program;
    private final SaturnGeometry geometry;

    public SaturnClientPlan(SaturnProgram program) {
        this.program = program;
        this.geometry = new SaturnGeometry(program);
    }

    public SaturnProgram program() {
        return program;
    }

    public List<SaturnEmission> emissionsAtTick(long seed, int tick) {
        List<SaturnGeometry.Sample> samples = geometry.samplesAtTick(seed, tick);
        if (samples.size() > program.budget().maxPerTick()) {
            throw new IllegalStateException("Saturn client plan exceeded its per-tick particle budget");
        }
        List<SaturnEmission> emissions = new ArrayList<>(samples.size());
        for (SaturnGeometry.Sample sample : samples) {
            emissions.add(new SaturnEmission(
                    tick,
                    sample.kind(),
                    sample.sourceId(),
                    sample.sampleIndex(),
                    sample.visualLayer(),
                    sample.position(),
                    sample.normal(),
                    program.palette().color(sample.colorBand()),
                    sample.lifetimeTicks()));
        }
        return List.copyOf(emissions);
    }

    public List<SaturnEmission> allEmissions(long seed) {
        List<SaturnEmission> emissions = new ArrayList<>(program.totalSampleCount());
        for (int tick = 0; tick < program.totalTicks(); tick++) {
            emissions.addAll(emissionsAtTick(seed, tick));
        }
        if (emissions.size() != program.totalSampleCount()) {
            throw new IllegalStateException("Saturn schedule did not emit every configured sample exactly once");
        }
        return List.copyOf(emissions);
    }

    public BudgetProof budgetProof(long seed) {
        int observedTotal = 0;
        int observedPeak = 0;
        for (int tick = 0; tick < program.totalTicks(); tick++) {
            int count = emissionsAtTick(seed, tick).size();
            observedTotal = Math.addExact(observedTotal, count);
            observedPeak = Math.max(observedPeak, count);
        }
        return new BudgetProof(
                seed,
                observedTotal,
                observedPeak,
                program.budget().maxPerTick(),
                program.budget().maxOwnedParticles(),
                observedPeak <= program.budget().maxPerTick()
                        && observedTotal <= program.budget().maxOwnedParticles());
    }

    public record BudgetProof(
            long seed,
            int totalEmissions,
            int peakPerTick,
            int maxPerTick,
            int maxOwnedParticles,
            boolean withinBudget) {
    }
}
