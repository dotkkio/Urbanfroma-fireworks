package com.urbanforma.fireworks.content.batch05;

import com.urbanforma.fireworks.content.FireworkStyle;
import com.urbanforma.fireworks.content.saturn.SaturnColorBand;
import com.urbanforma.fireworks.content.saturn.SaturnGeometry;
import com.urbanforma.fireworks.content.saturn.SaturnProgram;
import com.urbanforma.fireworks.content.saturn.SaturnRingConfiguration;
import com.urbanforma.fireworks.content.saturn.SaturnSphereLayer;
import com.urbanforma.fireworks.content.saturn.SaturnSphereSampler;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/** Effect-profile factories for batch05. They create no particle, item, registry, or scheduler ownership. */
public final class Batch05EffectProfiles {
    private static final SaturnSphereSampler FIBONACCI_SAMPLER = (seed, index, count) -> {
        double y = 1.0D - 2.0D * ((double) index + 0.5D) / count;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double angle = index * Math.PI * (3.0D - Math.sqrt(5.0D)) + (seed & 0xFFFFL) * 0.0001D;
        return new Vec3(horizontal * Math.cos(angle), y, horizontal * Math.sin(angle));
    };

    private Batch05EffectProfiles() {
    }

    public static FireworkStyle.RadiantProfile radiantProfile() {
        return new FireworkStyle.RadiantProfile(3.5D, 48.0D, 0.94D, 0.38D, 0.46D, 9.0D);
    }

    public static FireworkStyle.RadiantWillowProfile radiantWillowProfile() {
        return new FireworkStyle.RadiantWillowProfile(
                radiantProfile(), 100, 140, 18.0D, 0.28D, 0.42D, 66.0D, 7.5D);
    }

    /**
     * Builds four deliberately different but bounded Saturn layouts. The integration owner supplies the existing
     * client plan and scheduler; this method only produces the common immutable geometry program.
     */
    public static SaturnProgram saturnProgram(Batch05FireworkDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definition.effectType() != Batch05FireworkDefinition.EffectType.SATURN) {
            throw new IllegalArgumentException("Only batch05 Saturn entries have Saturn programs");
        }
        SaturnProgram program = switch (definition.id()) {
            case "batch05_copper_crown_saturn" -> saturn(
                    definition, 48.0D, 1_440, 20.0D, 320, 3, 48.0D, 8.0D, 4.0D, 20.0D);
            case "batch05_rose_garnet_saturn" -> saturn(
                    definition, 50.0D, 1_500, 22.0D, 360, 2, 56.0D, 12.0D, 4.0D, -28.0D);
            case "batch05_violet_opal_saturn" -> saturn(
                    definition, 46.0D, 1_380, 18.0D, 300, 4, 40.0D, 7.0D, 3.0D, 34.0D);
            case "batch05_aqua_platinum_saturn" -> saturn(
                    definition, 52.0D, 1_560, 24.0D, 420, 1, 64.0D, 0.0D, 4.0D, 12.0D);
            default -> throw new IllegalArgumentException("Unknown batch05 Saturn id " + definition.id());
        };
        SaturnGeometry.Bounds bounds = new SaturnGeometry(program).conservativeBounds();
        if (!bounds.fitsWithin(definition.expectedBoundary().fullEnvelopeBlocks())) {
            throw new IllegalStateException("batch05 Saturn program exceeds its declared boundary");
        }
        return program;
    }

    private static SaturnProgram saturn(
            Batch05FireworkDefinition definition,
            double outerRadius,
            int outerSamples,
            double innerRadius,
            int innerSamples,
            int ringCount,
            double firstRingRadius,
            double spacing,
            double width,
            double tiltDegrees) {
        return new SaturnProgram(
                List.of(
                        new SaturnSphereLayer(
                                "outer", outerRadius, outerSamples, 0, SaturnColorBand.PRIMARY,
                                0, 12, 96, FIBONACCI_SAMPLER),
                        new SaturnSphereLayer(
                                "inner", innerRadius, innerSamples, 1, SaturnColorBand.SECONDARY,
                                12, 8, 80, FIBONACCI_SAMPLER)),
                SaturnRingConfiguration.concentric(
                        ringCount, firstRingRadius, spacing, width, tiltDegrees, 2,
                        SaturnColorBand.ACCENT, 160, 0, 20, 108),
                new SaturnProgram.SaturnPalette(
                        definition.palette().primary(),
                        definition.palette().secondary(),
                        definition.palette().accent()),
                new SaturnProgram.SaturnParticleBudget(480, 4_000));
    }
}
