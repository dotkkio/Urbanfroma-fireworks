package com.urbanforma.fireworks.client.batch_other;

import com.urbanforma.fireworks.content.batch_other.BatchOtherCatalog;
import com.urbanforma.fireworks.content.batch_other.BatchOtherFirework;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic client-side trajectories for the fifteen Other fireworks.
 *
 * <p>Each route owns a separate geometry strategy.  The shared effect may consume one
 * {@link Emission} at a time, but it never has to infer a shape from a colour or from a
 * legacy sphere hint.  A route emits every configured branch/segment exactly once, with
 * a fixed finite schedule and a bounded point envelope.</p>
 */
public final class BatchOtherClientPrograms {
    public static final int MAX_PER_TICK = 720;
    public static final int MAX_OWNED_PARTICLES = 15_000;
    public static final int MAX_LIFETIME_TICKS = 720;

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final long PHASE_SALT = 0xD1B54A32D192ED03L;
    private static final long LIFETIME_SALT = 0x94D049BB133111EBL;
    private static final Map<BatchOtherFirework.ProgramRoute, Spec> SPECS = createSpecs();
    private static final Map<BatchOtherFirework.ProgramRoute, Trajectory> TRAJECTORIES = createTrajectories();

    private BatchOtherClientPrograms() {
    }

    public static Program require(String programId) {
        for (BatchOtherFirework.ProgramRoute route : BatchOtherFirework.ProgramRoute.values()) {
            if (route.clientProgramId().equals(programId)) {
                Spec spec = SPECS.get(route);
                Trajectory trajectory = TRAJECTORIES.get(route);
                if (spec == null || trajectory == null) {
                    throw new IllegalArgumentException("Missing batch_other trajectory " + programId);
                }
                return new Program(programId, route, spec, trajectory);
            }
        }
        throw new IllegalArgumentException("Unknown batch_other client program " + programId);
    }

    public static List<Program> all() {
        List<Program> programs = new ArrayList<>(BatchOtherFirework.ProgramRoute.values().length);
        for (BatchOtherFirework.ProgramRoute route : BatchOtherFirework.ProgramRoute.values()) {
            programs.add(require(route.clientProgramId()));
        }
        return List.copyOf(programs);
    }

    /** Performs all finite source-level trajectory and budget checks for this isolated family. */
    public static void validateAll() {
        for (Program program : all()) {
            program.validate();
        }
    }

    /** A strategy is deliberately separate per route even when two routes share a transport category. */
    public interface Trajectory {
        String id();

        Point sample(long seed, int branch, int segment, Spec spec);

        default int scheduleTick(int branch, int segment, Spec spec) {
            return segment;
        }

        default int colorStage(int branch, int segment, Spec spec) {
            return Math.min(2, segment * 3 / spec.segmentCount());
        }

        default int lifetimeTicks(long seed, int branch, int segment, Spec spec) {
            long mixed = mix64(seed ^ LIFETIME_SALT ^ ((long) branch * 0x9E3779B97F4A7C15L)
                    ^ ((long) segment * 0xBF58476D1CE4E5B9L));
            return 48 + (int) Math.floorMod(mixed, 25L);
        }
    }

    /** Immutable handoff object consumed by the later shared scheduler. */
    public record Program(String id, BatchOtherFirework.ProgramRoute route, Spec spec, Trajectory trajectory) {
        public Program {
            if (id == null || route == null || spec == null || trajectory == null
                    || !id.equals(route.clientProgramId()) || !trajectory.id().equals(route.id())) {
                throw new IllegalArgumentException("Invalid batch_other client program");
            }
        }

        public int peakParticles() {
            return this.spec.peakParticles();
        }

        public int maxPerTick() {
            return this.spec.maxPerTick();
        }

        public int maximumScheduledParticles() {
            int maximum = 0;
            for (int tick = 0; tick < totalTicks(); tick++) {
                maximum = Math.max(maximum, emissionsAtTick(0L, this.spec.startDelay() + tick).size());
            }
            return maximum;
        }

        public int totalScheduledParticles() {
            int total = 0;
            for (int tick = 0; tick < totalTicks(); tick++) {
                total = Math.addExact(total, emissionsAtTick(0L, this.spec.startDelay() + tick).size());
            }
            return total;
        }

        public int totalTicks() {
            int last = 0;
            for (int segment = 0; segment < this.spec.segmentCount(); segment++) {
                for (int branch = 0; branch < this.spec.branchCount(); branch++) {
                    last = Math.max(last, this.trajectory.scheduleTick(branch, segment, this.spec));
                }
            }
            return last + 1;
        }

        public int ownedParticleBudget() {
            return this.spec.peakParticles();
        }

        public int colorStage(int branch, int segment) {
            checkIndex(branch, segment);
            return this.trajectory.colorStage(branch, segment, this.spec);
        }

        public int lifetimeTicks(long seed, int branch, int segment) {
            checkIndex(branch, segment);
            int lifetime = this.trajectory.lifetimeTicks(seed, branch, segment, this.spec);
            if (lifetime <= 0 || lifetime > MAX_LIFETIME_TICKS) {
                throw new IllegalStateException("batch_other lifetime escaped its fixed bound");
            }
            return lifetime;
        }

        public Lifecycle lifecycle() {
            int minimum = Integer.MAX_VALUE;
            int maximum = 0;
            for (int segment = 0; segment < this.spec.segmentCount(); segment++) {
                for (int branch = 0; branch < this.spec.branchCount(); branch++) {
                    int lifetime = lifetimeTicks(0L, branch, segment);
                    minimum = Math.min(minimum, lifetime);
                    maximum = Math.max(maximum, lifetime);
                }
            }
            return new Lifecycle(this.spec.startDelay(), totalTicks(), minimum, maximum,
                    this.spec.peakParticles());
        }

        /** Returns only the bounded work for one client tick; no particle is created here. */
        public List<Emission> emissionsAtTick(long burstSeed, int tick) {
            int localTick = tick - this.spec.startDelay();
            if (localTick < 0 || localTick >= totalTicks()) {
                return List.of();
            }
            List<Emission> emissions = new ArrayList<>(Math.min(this.spec.maxPerTick(), MAX_PER_TICK));
            for (int segment = 0; segment < this.spec.segmentCount(); segment++) {
                for (int branch = 0; branch < this.spec.branchCount(); branch++) {
                    if (this.trajectory.scheduleTick(branch, segment, this.spec) == localTick) {
                        if (emissions.size() >= this.spec.maxPerTick()) {
                            throw new IllegalStateException("batch_other route attempted to exceed its fixed tick budget: "
                                    + this.id);
                        }
                        emissions.add(new Emission(branch, segment,
                                this.trajectory.sample(burstSeed, branch, segment, this.spec)));
                    }
                }
            }
            if (emissions.size() > MAX_PER_TICK || emissions.size() > this.spec.maxPerTick()) {
                throw new IllegalStateException("batch_other route exceeded its finite tick budget: " + this.id);
            }
            return List.copyOf(emissions);
        }

        public List<Emission> allEmissions(long burstSeed) {
            List<Emission> emissions = new ArrayList<>(this.spec.peakParticles());
            for (int tick = 0; tick < totalTicks(); tick++) {
                List<Emission> frame = emissionsAtTick(burstSeed, this.spec.startDelay() + tick);
                if (emissions.size() + frame.size() > this.spec.peakParticles()) {
                    throw new IllegalStateException("batch_other route attempted to exceed its fixed ownership budget: "
                            + this.id);
                }
                emissions.addAll(frame);
            }
            if (emissions.size() != this.spec.peakParticles()) {
                throw new IllegalStateException("batch_other route did not emit its fixed node count: " + this.id);
            }
            return List.copyOf(emissions);
        }

        public Point sample(long burstSeed, int branch, int segment) {
            checkIndex(branch, segment);
            return this.trajectory.sample(burstSeed, branch, segment, this.spec);
        }

        public boolean fitsWithin(int envelope) {
            return this.spec.maxEnvelope() <= envelope && envelope <= BatchOtherCatalog.ORDINARY_MAXIMUM_ENVELOPE;
        }

        public void validate() {
            if (this.spec.peakParticles() != this.spec.branchCount() * this.spec.segmentCount()
                    || this.spec.peakParticles() > MAX_OWNED_PARTICLES
                    || this.spec.maxPerTick() > MAX_PER_TICK
                    || maximumScheduledParticles() > this.spec.maxPerTick()
                    || totalScheduledParticles() != this.spec.peakParticles()
                    || lifecycle().maximumLifetime() > MAX_LIFETIME_TICKS) {
                throw new IllegalStateException("batch_other trajectory budget drifted: " + this.id);
            }
            validateGeometry(0L);
            validateGeometry(0x5EED5EED1234ABCDL);
        }

        private void validateGeometry(long seed) {
            double maximum = 0.0D;
            for (int segment = 0; segment < this.spec.segmentCount(); segment++) {
                for (int branch = 0; branch < this.spec.branchCount(); branch++) {
                    Point point = sample(seed, branch, segment);
                    maximum = Math.max(maximum, Math.max(Math.abs(point.x()),
                            Math.max(Math.abs(point.y()), Math.abs(point.z()))));
                }
            }
            if (maximum * 2.0D > this.spec.maxEnvelope() + 1.0D) {
                throw new IllegalStateException("batch_other trajectory exceeded its declared envelope: " + this.id);
            }
        }

        private void checkIndex(int branch, int segment) {
            if (branch < 0 || branch >= this.spec.branchCount()
                    || segment < 0 || segment >= this.spec.segmentCount()) {
                throw new IllegalArgumentException("batch_other sample index out of bounds");
            }
        }
    }

    public record Emission(int branch, int segment, Point point) {
        public Emission {
            if (branch < 0 || segment < 0 || point == null) {
                throw new IllegalArgumentException("Invalid batch_other emission");
            }
        }
    }

    public record Point(double x, double y, double z) {
        public Point {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("batch_other trajectory point must be finite");
            }
        }
    }

    public record Lifecycle(int startDelay, int emissionTicks, int minimumLifetime,
                            int maximumLifetime, int ownedParticles) {
        public Lifecycle {
            if (startDelay < 0 || emissionTicks <= 0 || minimumLifetime <= 0
                    || maximumLifetime < minimumLifetime || maximumLifetime > MAX_LIFETIME_TICKS
                    || ownedParticles <= 0 || ownedParticles > MAX_OWNED_PARTICLES) {
                throw new IllegalArgumentException("Invalid batch_other lifecycle contract");
            }
        }
    }

    /** The public shape names remain route-compatible, but each enum constant owns its own sampler. */
    private enum Geometry implements Trajectory {
        RADIAL_STRAIGHT("radial_straight", 54, 72) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double progress = progress(spec, segment);
                double radius = lerp(spec.innerRadius(), spec.outerRadius(), progress);
                double angle = branchAngle(seed, branch, spec.branchCount());
                return radial(angle, radius, spec.verticalBias() * radius);
            }
        },
        SPARSE_LONG_RAYS("sparse_long_rays", 56, 86) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double progress = progress(spec, segment);
                double radius = lerp(spec.innerRadius(), spec.outerRadius(), progress);
                double angle = branchAngle(seed, branch, spec.branchCount()) + (branch & 1) * 0.035D * progress;
                return radial(angle, radius, spec.verticalBias() * radius + Math.sin(angle * 2.0D) * 0.45D);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment * spec.pulseStride()
                        + Math.floorMod(branch + spec.phaseOffset(), spec.pulseStride());
            }
        },
        OFFSET_DOUBLE_RING("offset_double_ring", 58, 78) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                boolean outer = segment >= spec.segmentCount() / 2;
                int ringSegment = outer ? segment - spec.segmentCount() / 2 : segment;
                double progress = (ringSegment + 0.5D) / (spec.segmentCount() / 2.0D);
                double radius = (outer ? spec.outerRadius() : spec.outerRadius() * 0.58D) + progress * 1.8D;
                double angle = branchAngle(seed, branch, spec.branchCount())
                        + (outer ? -spec.angularDrift() : spec.angularDrift()) * progress;
                return tiltedRing(angle, radius, outer ? -0.28D : 0.24D,
                        outer ? 2.4D : -1.4D);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment + (segment >= spec.segmentCount() / 2 ? 4 : 0);
            }
        },
        THREE_CONCENTRIC_RINGS("three_concentric_rings", 62, 84) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double angle = branchAngle(seed, branch, spec.branchCount());
                if (segment < 2) {
                    return radial(angle, 1.25D + segment * 0.65D, 0.0D);
                }
                int ring = Math.min(2, (segment - 2) / 9);
                double local = ((segment - 2) % 9 + 0.5D) / 9.0D;
                double radius = switch (ring) {
                    case 0 -> 14.0D + local * 1.8D;
                    case 1 -> 25.0D + local * 2.0D;
                    default -> 38.0D + local * 2.2D;
                };
                return tiltedRing(angle, radius, 0.05D + ring * 0.04D, 0.0D);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment + (segment < 2 ? 0 : (segment - 2) / 9 * 2);
            }
        },
        RING_CORE_HYBRID("ring_core_hybrid", 60, 82) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double angle = branchAngle(seed, branch, spec.branchCount());
                if (segment < 6) {
                    double radius = lerp(1.4D, 16.0D, (segment + 1.0D) / 6.0D);
                    return radial(angle, radius, 0.08D * radius);
                }
                double ringProgress = (segment - 6.0D) / (spec.segmentCount() - 6.0D);
                return tiltedRing(angle + 0.18D * ringProgress,
                        29.0D + ringProgress * 9.0D, 0.18D, 0.0D);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment < 6 ? segment : segment + 3;
            }
        },
        DROOPING_TAILS("drooping_tails", 68, 92) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double progress = progress(spec, segment);
                double radius = lerp(spec.innerRadius(), spec.outerRadius(), progress);
                double dropProgress = Math.max(0.0D, (progress - 0.42D) / 0.58D);
                double angle = branchAngle(seed, branch, spec.branchCount());
                double sway = Math.sin(progress * Math.PI) * spec.sway();
                return new Point(Math.cos(angle) * radius + Math.cos(angle + Math.PI / 2.0D) * sway,
                        spec.verticalBias() * radius - Math.pow(dropProgress, 1.35D) * spec.dropAmount(),
                        Math.sin(angle) * radius + Math.sin(angle + Math.PI / 2.0D) * sway);
            }
        },
        LAYERED_WILLOW("layered_willow", 70, 96) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                int tier = Math.min(2, segment / 10);
                double local = (segment % 10 + 0.5D) / 10.0D;
                double tierRadius = switch (tier) {
                    case 0 -> 34.0D;
                    case 1 -> 28.0D;
                    default -> 22.0D;
                };
                double angle = branchAngle(seed, branch, spec.branchCount()) + (tier - 1) * 0.17D * local;
                double drop = switch (tier) {
                    case 0 -> 6.0D;
                    case 1 -> 18.0D;
                    default -> 30.0D;
                };
                double radius = tierRadius * (0.55D + 0.45D * local);
                double y = 15.0D - tier * 10.0D - Math.pow(local, 1.25D) * drop;
                return new Point(Math.cos(angle) * radius + Math.sin(local * Math.PI) * spec.sway(),
                        y, Math.sin(angle) * radius);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment + (segment / 10) * 2;
            }
        },
        HELICAL_RADIATION("helical_radiation", 60, 80) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double progress = progress(spec, segment);
                double radius = lerp(spec.innerRadius(), spec.outerRadius(), progress);
                double angle = branchAngle(seed, branch, spec.branchCount())
                        + progress * (TWO_PI * 1.65D + spec.angularDrift());
                return new Point(Math.cos(angle) * radius,
                        spec.verticalBias() * radius + Math.sin(progress * TWO_PI + angle) * 4.5D,
                        Math.sin(angle) * radius);
            }
        },
        ALTERNATING_PULSES("alternating_pulses", 56, 76) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double progress = progress(spec, segment);
                boolean outerPulse = ((branch + segment + spec.phaseOffset()) & 1) == 0;
                double radius = lerp(2.0D, outerPulse ? spec.outerRadius() : spec.outerRadius() * 0.57D, progress);
                double angle = branchAngle(seed, branch, spec.branchCount()) + (outerPulse ? 0.0D : 0.12D);
                return radial(angle, radius, (outerPulse ? 0.16D : -0.12D) * radius);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment * spec.pulseStride()
                        + Math.floorMod(branch + spec.phaseOffset(), spec.pulseStride());
            }
        },
        THICK_MULTILAYER_RAYS("thick_multilayer_rays", 60, 84) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                int layer = Math.min(2, segment / 7);
                double local = (segment % 7 + 1.0D) / 7.0D;
                double base = switch (layer) {
                    case 0 -> 21.0D;
                    case 1 -> 29.0D;
                    default -> 37.0D;
                };
                double radius = base * (0.84D + 0.16D * local);
                double angle = branchAngle(seed, branch, spec.branchCount()) + layer * 0.035D;
                return radial(angle, radius, (layer - 1) * 2.8D + spec.verticalBias() * radius * 0.25D);
            }
        },
        DELAYED_CORE_SHELL("delayed_core_shell", 64, 90) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double angle = branchAngle(seed, branch, spec.branchCount());
                if (segment < spec.coreHoldSegments()) {
                    return radial(angle, lerp(1.2D, 9.5D,
                            (segment + 1.0D) / spec.coreHoldSegments()), 0.0D);
                }
                double shellProgress = (segment - spec.coreHoldSegments())
                        / (spec.segmentCount() - (double) spec.coreHoldSegments());
                return radial(angle, lerp(33.0D, spec.outerRadius(), shellProgress),
                        spec.verticalBias() * 0.35D * spec.outerRadius());
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment < spec.coreHoldSegments() ? segment : segment + 4;
            }
        },
        ORBITAL_SATURN("orbital_saturn", 60, 82) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double angle = branchAngle(seed, branch, spec.branchCount());
                if (segment < 3) {
                    return radial(angle, 2.0D + segment * 2.0D, 0.0D);
                }
                double progress = (segment - 3.0D) / (spec.segmentCount() - 3.0D);
                double orbitAngle = angle + progress * (TWO_PI * 1.25D + spec.angularDrift());
                double radius = 31.0D + progress * 6.0D;
                double x = Math.cos(orbitAngle) * radius;
                double z = Math.sin(orbitAngle) * radius;
                double y = Math.sin(orbitAngle) * radius * 0.34D + 2.0D;
                return new Point(x, y, z * 0.92D);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment < 3 ? segment : segment + 2;
            }
        },
        TWIN_CROSS_ORBITS("twin_cross_orbits", 64, 88) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                double progress = progress(spec, segment);
                double radius = lerp(8.0D, spec.outerRadius(), progress);
                double angle = branchAngle(seed, branch, spec.branchCount())
                        + progress * (TWO_PI * 0.85D + spec.angularDrift());
                if ((branch & 1) == 0) {
                    return new Point(Math.cos(angle) * radius, Math.sin(angle) * radius * 0.36D,
                            Math.sin(angle) * radius);
                }
                return new Point(Math.sin(angle) * radius * 0.36D, Math.cos(angle) * radius,
                        Math.sin(angle) * radius);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment * spec.pulseStride() + Math.floorMod(branch, spec.pulseStride());
            }
        },
        SEGMENTED_RAYS("segmented_rays", 56, 78) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                int block = segment / 4;
                int piece = segment % 4;
                double radius = 4.0D + block * 8.0D + piece * 1.20D;
                double angle = branchAngle(seed, branch, spec.branchCount());
                return radial(angle, radius, spec.verticalBias() * radius);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment + segment / 2;
            }
        },
        COLOR_SHIFT_BEADS("color_shift_beads", 64, 90) {
            @Override
            public Point sample(long seed, int branch, int segment, Spec spec) {
                int bead = segment % 4;
                int chain = segment / 4;
                double radius = 5.0D + chain * 8.0D + bead * 1.15D;
                double angle = branchAngle(seed, branch, spec.branchCount()) + bead * 0.18D;
                double y = Math.sin((chain + 1.0D) * 0.9D + angle) * 1.6D;
                return radial(angle, radius, y);
            }

            @Override
            public int scheduleTick(int branch, int segment, Spec spec) {
                return segment * spec.pulseStride() + Math.floorMod(branch, spec.pulseStride());
            }

            @Override
            public int colorStage(int branch, int segment, Spec spec) {
                return segment % 3;
            }
        };

        private final String id;
        private final int lifetimeMinimum;
        private final int lifetimeMaximum;

        Geometry(String id, int lifetimeMinimum, int lifetimeMaximum) {
            this.id = id;
            this.lifetimeMinimum = lifetimeMinimum;
            this.lifetimeMaximum = lifetimeMaximum;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public int lifetimeTicks(long seed, int branch, int segment, Spec spec) {
            long mixed = mix64(seed ^ LIFETIME_SALT ^ ((long) branch * 0x9E3779B97F4A7C15L)
                    ^ ((long) segment * 0xBF58476D1CE4E5B9L));
            int spread = this.lifetimeMaximum - this.lifetimeMinimum + 1;
            return this.lifetimeMinimum + (int) Math.floorMod(mixed, (long) spread);
        }

        private static Trajectory forRoute(BatchOtherFirework.ProgramRoute route) {
            return switch (route) {
                case RADIAL_STRAIGHT -> RADIAL_STRAIGHT;
                case SPARSE_LONG_RAYS -> SPARSE_LONG_RAYS;
                case OFFSET_DOUBLE_RING -> OFFSET_DOUBLE_RING;
                case THREE_CONCENTRIC_RINGS -> THREE_CONCENTRIC_RINGS;
                case RING_CORE_HYBRID -> RING_CORE_HYBRID;
                case DROOPING_TAILS -> DROOPING_TAILS;
                case LAYERED_WILLOW -> LAYERED_WILLOW;
                case HELICAL_RADIATION -> HELICAL_RADIATION;
                case ALTERNATING_PULSES -> ALTERNATING_PULSES;
                case THICK_MULTILAYER_RAYS -> THICK_MULTILAYER_RAYS;
                case DELAYED_CORE_SHELL -> DELAYED_CORE_SHELL;
                case ORBITAL_SATURN -> ORBITAL_SATURN;
                case TWIN_CROSS_ORBITS -> TWIN_CROSS_ORBITS;
                case SEGMENTED_RAYS -> SEGMENTED_RAYS;
                case COLOR_SHIFT_BEADS -> COLOR_SHIFT_BEADS;
            };
        }
    }

    /** Fixed route parameters.  The product of branches and segments is the exact owned node count. */
    public record Spec(
            int branchCount,
            int segmentCount,
            int maxPerTick,
            int peakParticles,
            int maxEnvelope,
            int startDelay,
            int pulseStride,
            int phaseOffset,
            int coreHoldSegments,
            double innerRadius,
            double outerRadius,
            double verticalBias,
            double angularDrift,
            double dropAmount,
            double sway) {
        public Spec {
            if (branchCount <= 0 || branchCount > 720 || segmentCount <= 0 || segmentCount > 120
                    || maxPerTick <= 0 || maxPerTick > MAX_PER_TICK || peakParticles <= 0
                    || peakParticles > MAX_OWNED_PARTICLES || maxEnvelope <= 0
                    || maxEnvelope > BatchOtherCatalog.ORDINARY_MAXIMUM_ENVELOPE || startDelay < 0
                    || pulseStride <= 0 || phaseOffset < 0 || coreHoldSegments < 0
                    || !finitePositive(innerRadius) || !finitePositive(outerRadius) || outerRadius <= innerRadius
                    || !Double.isFinite(verticalBias) || Math.abs(verticalBias) > 1.0D
                    || !Double.isFinite(angularDrift) || !Double.isFinite(dropAmount) || dropAmount < 0.0D
                    || !Double.isFinite(sway) || sway < 0.0D
                    || peakParticles != branchCount * segmentCount) {
                throw new IllegalArgumentException("Invalid bounded batch_other client program spec");
            }
        }

        private static boolean finitePositive(double value) {
            return Double.isFinite(value) && value > 0.0D;
        }
    }

    private static Map<BatchOtherFirework.ProgramRoute, Spec> createSpecs() {
        EnumMap<BatchOtherFirework.ProgramRoute, Spec> specs =
                new EnumMap<>(BatchOtherFirework.ProgramRoute.class);
        add(specs, BatchOtherFirework.ProgramRoute.RADIAL_STRAIGHT,
                70, 20, 126, 1_400, 86, 0, 1, 0, 0, 1.0D, 34.0D, 0.12D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.SPARSE_LONG_RAYS,
                50, 30, 108, 1_500, 98, 1, 3, 1, 0, 2.0D, 46.0D, 0.08D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.OFFSET_DOUBLE_RING,
                85, 20, 144, 1_700, 96, 5, 2, 1, 0, 8.0D, 40.0D, 0.04D, 0.75D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.THREE_CONCENTRIC_RINGS,
                70, 30, 162, 2_100, 108, 3, 2, 0, 0, 8.0D, 42.0D, 0.02D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.RING_CORE_HYBRID,
                95, 20, 144, 1_900, 102, 4, 2, 0, 0, 2.0D, 40.0D, 0.10D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.DROOPING_TAILS,
                110, 20, 180, 2_200, 112, 2, 1, 0, 0, 2.0D, 40.0D, 0.20D, 0.0D, 28.0D, 4.0D);
        add(specs, BatchOtherFirework.ProgramRoute.LAYERED_WILLOW,
                70, 30, 162, 2_100, 116, 7, 2, 1, 0, 3.0D, 38.0D, 0.28D, 0.0D, 30.0D, 2.0D);
        add(specs, BatchOtherFirework.ProgramRoute.HELICAL_RADIATION,
                90, 20, 144, 1_800, 96, 3, 2, 0, 0, 2.0D, 40.0D, 0.08D, 5.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.ALTERNATING_PULSES,
                80, 20, 126, 1_600, 90, 6, 2, 0, 0, 2.0D, 40.0D, 0.12D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.THICK_MULTILAYER_RAYS,
                110, 20, 216, 2_200, 104, 1, 1, 0, 0, 2.0D, 39.0D, 0.16D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.DELAYED_CORE_SHELL,
                115, 20, 180, 2_300, 118, 12, 2, 0, 8, 2.0D, 44.0D, 0.05D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.ORBITAL_SATURN,
                95, 20, 162, 1_900, 100, 4, 2, 1, 0, 2.0D, 38.0D, 0.05D, 0.45D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.TWIN_CROSS_ORBITS,
                105, 20, 180, 2_100, 110, 8, 2, 0, 0, 2.0D, 42.0D, 0.14D, 0.50D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.SEGMENTED_RAYS,
                75, 20, 126, 1_500, 88, 2, 2, 1, 0, 3.0D, 44.0D, 0.10D, 0.0D, 0.0D, 0.0D);
        add(specs, BatchOtherFirework.ProgramRoute.COLOR_SHIFT_BEADS,
                110, 20, 180, 2_200, 114, 10, 2, 0, 0, 3.0D, 42.0D, 0.08D, 0.0D, 0.0D, 0.0D);
        return Map.copyOf(specs);
    }

    private static Map<BatchOtherFirework.ProgramRoute, Trajectory> createTrajectories() {
        EnumMap<BatchOtherFirework.ProgramRoute, Trajectory> trajectories =
                new EnumMap<>(BatchOtherFirework.ProgramRoute.class);
        for (BatchOtherFirework.ProgramRoute route : BatchOtherFirework.ProgramRoute.values()) {
            Trajectory previous = trajectories.put(route, Geometry.forRoute(route));
            if (previous != null) {
                throw new IllegalStateException("Duplicate batch_other trajectory " + route);
            }
        }
        return Map.copyOf(trajectories);
    }

    private static void add(
            Map<BatchOtherFirework.ProgramRoute, Spec> specs,
            BatchOtherFirework.ProgramRoute route,
            int branches,
            int segments,
            int maxPerTick,
            int peakParticles,
            int envelope,
            int startDelay,
            int pulseStride,
            int phaseOffset,
            int coreHoldSegments,
            double innerRadius,
            double outerRadius,
            double verticalBias,
            double angularDrift,
            double dropAmount,
            double sway) {
        Spec previous = specs.put(route, new Spec(
                branches, segments, maxPerTick, peakParticles, envelope, startDelay, pulseStride, phaseOffset,
                coreHoldSegments, innerRadius, outerRadius, verticalBias, angularDrift, dropAmount, sway));
        if (previous != null) {
            throw new IllegalStateException("Duplicate batch_other client program " + route);
        }
    }

    private static double progress(Spec spec, int segment) {
        return (segment + 0.5D) / spec.segmentCount();
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private static double branchAngle(long seed, int branch, int branchCount) {
        return TWO_PI * branch / branchCount + phase(seed, branch);
    }

    private static Point radial(double angle, double radius, double y) {
        return new Point(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private static Point tiltedRing(double angle, double radius, double tilt, double yOffset) {
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        return new Point(x, Math.sin(angle) * radius * tilt + yOffset, z * Math.cos(tilt));
    }

    private static double phase(long seed, int branch) {
        long mixed = mix64(seed ^ PHASE_SALT ^ ((long) branch * 0x9E3779B97F4A7C15L));
        return ((mixed & 0xFFFFL) / 65_536.0D) * TWO_PI * 0.08D;
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
