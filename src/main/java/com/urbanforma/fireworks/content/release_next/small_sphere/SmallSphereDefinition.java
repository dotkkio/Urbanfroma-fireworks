package com.urbanforma.fireworks.content.release_next.small_sphere;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** Unregistered common-side contract for one release-next small sphere firework. */
public record SmallSphereDefinition(
        String id,
        String zhName,
        String enName,
        Palette palette,
        ShapeVariant variant,
        Set<StructuralFeature> structuralFeatures,
        ClientPlan clientPlan) {
    private static final Pattern ID = Pattern.compile("release_next_small_[a-z_]+_sphere_firework");

    public SmallSphereDefinition {
        if (id == null || !ID.matcher(id).matches() || zhName == null || zhName.isBlank()
                || enName == null || enName.isBlank()) {
            throw new IllegalArgumentException("Small sphere identity is invalid");
        }
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(variant, "variant");
        structuralFeatures = Set.copyOf(Objects.requireNonNull(structuralFeatures, "structuralFeatures"));
        Objects.requireNonNull(clientPlan, "clientPlan");
        if (structuralFeatures.size() < 2) {
            throw new IllegalArgumentException("Every small sphere needs at least two structural differences");
        }
    }

    public record Palette(String primary, String secondary, String accent) {
        private static final Pattern COLOR = Pattern.compile("#[0-9A-Fa-f]{6}");

        public Palette {
            if (primary == null || secondary == null || accent == null || !COLOR.matcher(primary).matches()
                    || !COLOR.matcher(secondary).matches() || !COLOR.matcher(accent).matches()
                    || primary.equalsIgnoreCase(secondary) || primary.equalsIgnoreCase(accent)
                    || secondary.equalsIgnoreCase(accent)) {
                throw new IllegalArgumentException("Small sphere palette needs three distinct RGB colors");
            }
        }
    }

    public record ClientPlan(int branches, int layers, int particlesPerTick, int maxLifetimeTicks, double maxRadius) {
        public ClientPlan {
            if (branches != 48 || layers != 8 || particlesPerTick != 48 || maxLifetimeTicks < 18
                    || maxLifetimeTicks > 32 || !Double.isFinite(maxRadius) || maxRadius <= 0.0D || maxRadius > 6.4D) {
                throw new IllegalArgumentException("Small sphere plan must remain the fixed low-altitude client budget");
            }
        }

        public int totalParticles() {
            return branches * layers;
        }
    }

    public enum ShapeVariant {
        PEARL_KERNEL, HOLLOW_LANTERN, TWIN_SHELL, PETAL_WEAVE, EQUATORIAL_ORBIT,
        SPIRAL_LATITUDE, STAR_TIP, CROWNED_CORE, DELAYED_OUTER_RING, LACE_GLOBE,
        COMET_SHELL, CRYSTAL_GRID, RIPPLE_SPHERE, OFFSET_HEMISPHERES, AURORA_VEIL
    }

    public enum StructuralFeature {
        INNER_CORE, HOLLOW_CENTER, DOUBLE_SHELL, PETAL_OFFSET, EQUATOR_RING, SPIRAL_LATITUDES,
        TERMINAL_STARS, SPARSE_CROWN, DELAYED_RING, LACE_GAPS, SHORT_COMET_TRAILS,
        CRYSTAL_GRID, RIPPLE_BANDS, OFFSET_HEMISPHERES, VEIL_ARCS
    }

    public static List<StructuralFeature> orderedFeatures(SmallSphereDefinition definition) {
        return definition.structuralFeatures().stream().sorted().toList();
    }
}
