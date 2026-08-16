package com.urbanforma.fireworks.content.midsize.radial;

import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, caller-driven geometry for the second medium radial batch.
 *
 * <p>The class creates no particle, packet, queue, item, registry, or launch trajectory. A client program samples a
 * single {@link Path} from the detonation seed. Every path owns a finite local emission plan and clamps its geometry
 * to the approved medium envelope.</p>
 */
public final class MidsizeRadialTrajectory {
    public static final String REFERENCE_EFFECT_ID = "giant_amber_radiant_firework";
    public static final int REFERENCE_TOTAL_PARTICLES = 12_288;
    public static final double REFERENCE_FULL_ENVELOPE = 260.0D;
    public static final int REFERENCE_ASCENT_TICKS = 138;
    public static final double SHARED_LAUNCH_SPEED = 1.45D;

    public static final double MAX_RADIUS = 65.0D;
    public static final double APPROVED_FULL_ENVELOPE = 130.0D;
    public static final int ASCENT_TICKS = 118;
    public static final double REFERENCE_DETONATION_HEIGHT = REFERENCE_ASCENT_TICKS * SHARED_LAUNCH_SPEED;
    public static final double DETONATION_HEIGHT = ASCENT_TICKS * SHARED_LAUNCH_SPEED;
    public static final double HEIGHT_RATIO = DETONATION_HEIGHT / REFERENCE_DETONATION_HEIGHT;

    /** Local per-instance ceiling, deliberately independent of the existing shared particle reference limit. */
    public static final int LOCAL_MAX_PARTICLES_PER_TICK = 480;
    private static final double MIN_PARTICLE_RATIO = 0.79D;
    private static final double MAX_PARTICLE_RATIO = 0.85D;
    private static final double INITIAL_RADIUS = 2.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double RADIUS_EPSILON = 1.0E-7D;
    private static final long BRANCH_SALT = 0x6A09E667F3BCC909L;
    private static final long AZIMUTH_SALT = 0xBB67AE8584CAA73BL;
    private static final long ELEVATION_SALT = 0x3C6EF372FE94F82BL;
    private static final long PHASE_SALT = 0xA54FF53A5F1D36F1L;
    private static final long LIFETIME_SALT = 0x510E527FADE682D1L;
    private static final long BRIGHTNESS_SALT = 0x9B05688C2B3E6C1FL;
    private static final long TWINKLE_SALT = 0x1F83D9ABFB41BD6BL;

    private MidsizeRadialTrajectory() {
    }

    public enum PathForm {
        STRAIGHT_LANCE,
        THICK_RADIANT,
        SEGMENTED_BEAM,
        STAGGERED_RING,
        INNER_OUTER_PHASE,
        SHORT_RAY_SHELL,
        DELAYED_CORE,
        PULSE_RADIAL,
        ROTARY_WHEEL,
        CROWN_LANCE,
        SPLIT_FAN,
        LATTICE_WEAVE,
        SPIRAL_HELIX,
        COMET_SWEEP,
        TWIN_HELIX,
        RING_BLOOM,
        PHASED_PINWHEEL,
        HOLLOW_CORE,
        FRACTURE_RAY,
        DIAMOND_STAR,
        CROSSWEAVE,
        WAVE_RADIANT,
        TRIPLE_PULSE,
        AURORA_ARC,
        ICE_WHEEL
    }

    /** One finite emission layer inside a single client-side detonation window. */
    public record Layer(
            String role,
            int branchCount,
            int segmentsPerBranch,
            int startTick,
            double maximumRadius,
            double verticalScale,
            double phaseOffset) {
        public Layer {
            if (role == null || role.isBlank() || branchCount <= 0 || segmentsPerBranch <= 1 || startTick < 0
                    || !Double.isFinite(maximumRadius) || maximumRadius <= INITIAL_RADIUS
                    || maximumRadius > MAX_RADIUS || !Double.isFinite(verticalScale) || verticalScale <= 0.0D
                    || !Double.isFinite(phaseOffset)) {
                throw new IllegalArgumentException("Medium radial layer must be finite and bounded");
            }
        }

        public int finalEmissionTick() {
            return startTick + segmentsPerBranch - 1;
        }

        public int particleCount() {
            return branchCount * segmentsPerBranch;
        }
    }

    /**
     * Twenty-five non-palette path programs. Their layer timings, branch counts, shape form, and deformation inputs
     * are sampled by the shared geometry routine instead of using color-only variants.
     */
    public enum Path {
        SUN_LANCE(
                "sun_lance", PathForm.STRAIGHT_LANCE, 0.08D, 0.45D, 1.4D, 44, 74,
                new Layer("pin", 96, 10, 0, 20.0D, 0.92D, 0.00D),
                new Layer("spoke", 160, 36, 4, 54.0D, 0.98D, 0.13D),
                new Layer("crown", 192, 18, 18, 65.0D, 0.84D, 0.31D)),
        BROAD_BEAM(
                "broad_beam", PathForm.THICK_RADIANT, 0.16D, 3.10D, 1.0D, 46, 78,
                new Layer("ember", 80, 12, 0, 21.0D, 0.88D, 0.04D),
                new Layer("thick_body", 176, 34, 4, 56.0D, 0.96D, 0.21D),
                new Layer("pearl_edge", 176, 18, 16, 64.0D, 0.78D, 0.38D)),
        SEGMENT_BEAM(
                "segment_beam", PathForm.SEGMENTED_BEAM, 0.11D, 1.10D, 2.2D, 45, 76,
                new Layer("bead", 128, 12, 0, 18.0D, 0.94D, 0.00D),
                new Layer("segment_body", 144, 36, 8, 53.0D, 0.90D, 0.27D),
                new Layer("terminal", 192, 18, 22, 65.0D, 0.82D, 0.45D)),
        STAGGERED_RING(
                "staggered_ring", PathForm.STAGGERED_RING, 0.22D, 1.35D, 6.6D, 47, 81,
                new Layer("ring_core", 72, 14, 16, 20.0D, 0.84D, 0.06D),
                new Layer("ring_body", 208, 32, 0, 57.0D, 0.74D, 0.19D),
                new Layer("ring_outer", 144, 16, 12, 64.0D, 0.70D, 0.54D)),
        PHASE_SHELL(
                "phase_shell", PathForm.INNER_OUTER_PHASE, 0.48D, 1.75D, 2.8D, 46, 80,
                new Layer("inner_phase", 96, 12, 0, 19.0D, 0.96D, 0.00D),
                new Layer("middle_phase", 168, 34, 5, 54.0D, 0.90D, 0.47D),
                new Layer("outer_phase", 176, 18, 18, 65.0D, 0.82D, 0.94D)),
        SHORT_RAY(
                "short_ray", PathForm.SHORT_RAY_SHELL, 0.05D, 0.72D, 1.7D, 43, 72,
                new Layer("short_core", 120, 10, 0, 18.0D, 0.94D, 0.05D),
                new Layer("dense_shell", 184, 32, 4, 51.0D, 0.89D, 0.24D),
                new Layer("short_edge", 160, 18, 14, 61.0D, 0.80D, 0.39D)),
        DELAYED_CORE(
                "delayed_core", PathForm.DELAYED_CORE, 0.18D, 1.48D, 2.5D, 48, 86,
                new Layer("late_core", 64, 16, 24, 22.0D, 1.02D, 0.00D),
                new Layer("opening_body", 192, 32, 0, 55.0D, 0.88D, 0.20D),
                new Layer("open_edge", 160, 18, 12, 65.0D, 0.80D, 0.52D)),
        PULSE_RADIAL(
                "pulse_radial", PathForm.PULSE_RADIAL, 0.09D, 1.05D, 2.1D, 45, 77,
                new Layer("pulse_core", 128, 10, 0, 20.0D, 0.94D, 0.00D),
                new Layer("pulse_body", 152, 36, 4, 55.0D, 0.91D, 0.26D),
                new Layer("pulse_edge", 176, 18, 18, 64.0D, 0.82D, 0.49D)),
        ROTARY_WHEEL(
                "rotary_wheel", PathForm.ROTARY_WHEEL, 0.82D, 1.86D, 2.0D, 46, 82,
                new Layer("wheel_pin", 80, 12, 0, 19.0D, 0.95D, 0.00D),
                new Layer("wheel_spoke", 200, 32, 3, 56.0D, 0.86D, 0.17D),
                new Layer("wheel_tip", 144, 18, 16, 65.0D, 0.76D, 0.43D)),
        CROWN_LANCE(
                "crown_lance", PathForm.CROWN_LANCE, 0.14D, 1.22D, 8.4D, 48, 84,
                new Layer("crown_core", 104, 12, 0, 20.0D, 1.04D, 0.03D),
                new Layer("crown_body", 176, 32, 8, 54.0D, 0.92D, 0.29D),
                new Layer("crown_lance", 168, 18, 20, 65.0D, 0.72D, 0.61D)),
        SPLIT_FAN(
                "split_fan", PathForm.SPLIT_FAN, 0.28D, 1.52D, 5.1D, 44, 75,
                new Layer("fan_core", 88, 14, 0, 18.0D, 0.86D, 0.00D),
                new Layer("fan_body", 184, 32, 2, 55.0D, 0.58D, 0.20D),
                new Layer("fan_edge", 160, 18, 16, 64.0D, 0.52D, 0.58D)),
        LATTICE_WEAVE(
                "lattice_weave", PathForm.LATTICE_WEAVE, 0.34D, 3.25D, 3.4D, 46, 79,
                new Layer("lattice_core", 112, 10, 0, 19.0D, 0.94D, 0.00D),
                new Layer("lattice_body", 192, 30, 5, 56.0D, 0.88D, 0.25D),
                new Layer("lattice_crown", 160, 20, 12, 65.0D, 0.78D, 0.62D)),
        SPIRAL_HELIX(
                "spiral_helix", PathForm.SPIRAL_HELIX, 1.18D, 2.48D, 3.2D, 47, 83,
                new Layer("helix_core", 96, 14, 0, 20.0D, 0.96D, 0.00D),
                new Layer("helix_body", 176, 32, 4, 54.0D, 0.86D, 0.23D),
                new Layer("helix_tip", 160, 18, 15, 64.0D, 0.74D, 0.51D)),
        COMET_SWEEP(
                "comet_sweep", PathForm.COMET_SWEEP, 0.62D, 2.94D, 4.6D, 45, 80,
                new Layer("comet_core", 72, 16, 0, 18.0D, 0.94D, 0.02D),
                new Layer("comet_body", 208, 30, 5, 57.0D, 0.82D, 0.18D),
                new Layer("comet_tail", 144, 18, 18, 65.0D, 0.68D, 0.46D)),
        TWIN_HELIX(
                "twin_helix", PathForm.TWIN_HELIX, 1.54D, 3.02D, 2.7D, 47, 85,
                new Layer("twin_core", 128, 12, 0, 20.0D, 0.98D, 0.00D),
                new Layer("twin_body", 160, 34, 6, 54.0D, 0.88D, 0.30D),
                new Layer("twin_tip", 160, 18, 18, 64.0D, 0.76D, 0.66D)),
        RING_BLOOM(
                "ring_bloom", PathForm.RING_BLOOM, 0.26D, 1.58D, 7.2D, 46, 81,
                new Layer("bloom_core", 80, 14, 0, 19.0D, 0.86D, 0.00D),
                new Layer("bloom_ring", 192, 32, 5, 56.0D, 0.70D, 0.22D),
                new Layer("bloom_edge", 144, 18, 19, 65.0D, 0.64D, 0.57D)),
        PHASED_PINWHEEL(
                "phased_pinwheel", PathForm.PHASED_PINWHEEL, 1.86D, 2.36D, 3.7D, 48, 87,
                new Layer("pinwheel_core", 96, 12, 0, 20.0D, 0.94D, 0.00D),
                new Layer("pinwheel_body", 200, 30, 4, 55.0D, 0.82D, 0.19D),
                new Layer("pinwheel_tip", 160, 18, 16, 64.0D, 0.72D, 0.48D)),
        HOLLOW_CORE(
                "hollow_core", PathForm.HOLLOW_CORE, 0.38D, 1.14D, 2.0D, 44, 76,
                new Layer("hollow_inner", 112, 12, 8, 21.0D, 0.90D, 0.03D),
                new Layer("hollow_body", 168, 34, 0, 55.0D, 0.88D, 0.26D),
                new Layer("hollow_edge", 160, 18, 20, 65.0D, 0.78D, 0.59D)),
        FRACTURE_RAY(
                "fracture_ray", PathForm.FRACTURE_RAY, 0.44D, 3.54D, 3.9D, 49, 88,
                new Layer("fracture_core", 64, 18, 0, 19.0D, 0.96D, 0.00D),
                new Layer("fracture_body", 208, 30, 4, 56.0D, 0.84D, 0.25D),
                new Layer("fracture_tip", 176, 14, 20, 64.0D, 0.74D, 0.55D)),
        DIAMOND_STAR(
                "diamond_star", PathForm.DIAMOND_STAR, 0.12D, 1.42D, 2.9D, 46, 79,
                new Layer("diamond_core", 120, 12, 0, 20.0D, 0.96D, 0.00D),
                new Layer("diamond_body", 176, 32, 4, 55.0D, 0.88D, 0.27D),
                new Layer("diamond_tip", 160, 18, 16, 65.0D, 0.80D, 0.60D)),
        CROSSWEAVE(
                "crossweave", PathForm.CROSSWEAVE, 0.76D, 3.72D, 4.2D, 47, 83,
                new Layer("cross_core", 88, 12, 0, 19.0D, 0.94D, 0.00D),
                new Layer("cross_body", 192, 32, 4, 56.0D, 0.84D, 0.28D),
                new Layer("cross_tip", 160, 18, 17, 65.0D, 0.74D, 0.64D)),
        WAVE_RADIANT(
                "wave_radiant", PathForm.WAVE_RADIANT, 0.56D, 2.06D, 8.0D, 48, 86,
                new Layer("wave_core", 104, 12, 0, 20.0D, 0.96D, 0.02D),
                new Layer("wave_body", 184, 32, 6, 55.0D, 0.82D, 0.24D),
                new Layer("wave_tip", 160, 18, 18, 65.0D, 0.72D, 0.52D)),
        TRIPLE_PULSE(
                "triple_pulse", PathForm.TRIPLE_PULSE, 0.18D, 1.72D, 3.0D, 45, 78,
                new Layer("triple_core", 72, 14, 0, 18.0D, 0.96D, 0.00D),
                new Layer("triple_body", 200, 32, 4, 56.0D, 0.86D, 0.21D),
                new Layer("triple_edge", 160, 16, 18, 64.0D, 0.78D, 0.49D)),
        AURORA_ARC(
                "aurora_arc", PathForm.AURORA_ARC, 0.94D, 2.82D, 9.2D, 50, 92,
                new Layer("aurora_core", 96, 12, 0, 19.0D, 0.94D, 0.00D),
                new Layer("aurora_body", 184, 32, 5, 55.0D, 0.76D, 0.29D),
                new Layer("aurora_arc", 160, 18, 18, 65.0D, 0.66D, 0.68D)),
        ICE_WHEEL(
                "ice_wheel", PathForm.ICE_WHEEL, 2.18D, 2.64D, 4.8D, 49, 90,
                new Layer("ice_core", 112, 10, 0, 20.0D, 0.96D, 0.00D),
                new Layer("ice_body", 176, 34, 5, 55.0D, 0.84D, 0.25D),
                new Layer("ice_edge", 160, 18, 17, 65.0D, 0.72D, 0.61D));

        private final String pathId;
        private final PathForm form;
        private final double rotationTurns;
        private final double sideAmplitude;
        private final double verticalAmplitude;
        private final int minimumLifetimeTicks;
        private final int maximumLifetimeTicks;
        private final List<Layer> layers;

        Path(
                String pathId,
                PathForm form,
                double rotationTurns,
                double sideAmplitude,
                double verticalAmplitude,
                int minimumLifetimeTicks,
                int maximumLifetimeTicks,
                Layer... layers) {
            if (pathId == null || pathId.isBlank() || form == null || !Double.isFinite(rotationTurns)
                    || !Double.isFinite(sideAmplitude) || sideAmplitude < 0.0D
                    || !Double.isFinite(verticalAmplitude) || minimumLifetimeTicks <= 0
                    || maximumLifetimeTicks < minimumLifetimeTicks || layers == null || layers.length < 3) {
                throw new IllegalArgumentException("Medium radial path configuration is invalid");
            }
            this.pathId = pathId;
            this.form = form;
            this.rotationTurns = rotationTurns;
            this.sideAmplitude = sideAmplitude;
            this.verticalAmplitude = verticalAmplitude;
            this.minimumLifetimeTicks = minimumLifetimeTicks;
            this.maximumLifetimeTicks = maximumLifetimeTicks;
            this.layers = List.of(layers);
            double particleRatio = (double) totalParticles() / REFERENCE_TOTAL_PARTICLES;
            if (particleRatio < MIN_PARTICLE_RATIO || particleRatio > MAX_PARTICLE_RATIO
                    || maxParticlesPerTick() > LOCAL_MAX_PARTICLES_PER_TICK
                    || maximumRadius() > MAX_RADIUS || maximumEmissionTick() < 0) {
                throw new IllegalArgumentException("Medium radial path must retain the approved local budget and envelope");
            }
        }

        public String pathId() {
            return pathId;
        }

        public PathForm form() {
            return form;
        }

        public double rotationTurns() {
            return rotationTurns;
        }

        public double sideAmplitude() {
            return sideAmplitude;
        }

        public double verticalAmplitude() {
            return verticalAmplitude;
        }

        public int minimumLifetimeTicks() {
            return minimumLifetimeTicks;
        }

        public int maximumLifetimeTicks() {
            return maximumLifetimeTicks;
        }

        public List<Layer> layers() {
            return layers;
        }

        public Layer layer(int index) {
            if (index < 0 || index >= layers.size()) {
                throw new IllegalArgumentException("Medium radial layer index is outside the configured path");
            }
            return layers.get(index);
        }

        public int totalParticles() {
            int total = 0;
            for (Layer layer : layers) {
                total += layer.particleCount();
            }
            return total;
        }

        public int maximumEmissionTick() {
            int maximum = 0;
            for (Layer layer : layers) {
                maximum = Math.max(maximum, layer.finalEmissionTick());
            }
            return maximum;
        }

        public int maxParticlesPerTick() {
            int maximum = 0;
            for (int tick = 0; tick <= maximumEmissionTick(); tick++) {
                maximum = Math.max(maximum, particlesCreatedThisTick(this, tick));
            }
            return maximum;
        }

        public int totalVisualTicks() {
            return maximumEmissionTick() + maximumLifetimeTicks + 1;
        }

        public double maximumRadius() {
            double maximum = 0.0D;
            for (Layer layer : layers) {
                maximum = Math.max(maximum, layer.maximumRadius());
            }
            return maximum;
        }

        public boolean hasFiniteLocalPlan() {
            return totalParticles() > 0 && maxParticlesPerTick() > 0
                    && maxParticlesPerTick() <= LOCAL_MAX_PARTICLES_PER_TICK
                    && totalVisualTicks() > maximumEmissionTick();
        }
    }

    public enum ColorBand {
        CORE(1.26F),
        BODY(1.16F),
        EDGE(1.31F);

        private final float scale;

        ColorBand(float scale) {
            this.scale = scale;
        }

        public float scale() {
            return scale;
        }
    }

    public record Branch(
            Path path,
            int layerIndex,
            Layer layer,
            int index,
            long seed,
            Vec3 direction,
            double azimuth,
            double phase) {
        public Branch {
            if (path == null || layer == null || layerIndex < 0 || layerIndex >= path.layers().size()
                    || path.layer(layerIndex) != layer || index < 0 || index >= layer.branchCount()
                    || direction == null || direction.lengthSqr() < RADIUS_EPSILON
                    || !Double.isFinite(azimuth) || !Double.isFinite(phase)) {
                throw new IllegalArgumentException("Medium radial branch is invalid");
            }
        }
    }

    public record BranchSample(
            Branch branch,
            int segmentIndex,
            int emissionTick,
            double progress,
            Vec3 position,
            ColorBand colorBand,
            float brightness,
            int lifetime,
            boolean twinkles) {
        public BranchSample {
            if (branch == null || segmentIndex < 0 || segmentIndex >= branch.layer().segmentsPerBranch()
                    || emissionTick != branch.layer().startTick() + segmentIndex || !Double.isFinite(progress)
                    || progress < 0.0D || progress > 1.0D || position == null || colorBand == null
                    || position.lengthSqr() > branch.path().maximumRadius() * branch.path().maximumRadius()
                            + RADIUS_EPSILON
                    || !Float.isFinite(brightness) || brightness < 1.0F
                    || lifetime < branch.path().minimumLifetimeTicks()
                    || lifetime > branch.path().maximumLifetimeTicks()) {
                throw new IllegalArgumentException("Medium radial sample is outside its declared contract");
            }
        }
    }

    public record Bounds(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            double maxDistance) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || !Double.isFinite(maxDistance) || minX > maxX || minY > maxY || minZ > maxZ
                    || maxDistance < 0.0D) {
                throw new IllegalArgumentException("Medium radial bounds must be finite and ordered");
            }
        }

        public double maxSpan() {
            return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        }

        public boolean fitsEnvelope(double envelope) {
            return envelope > 0.0D && maxSpan() <= envelope + RADIUS_EPSILON;
        }
    }

    public record ParticlePlan(
            int tick,
            int createdThisTick,
            int cumulativeCreated,
            int activeUpperBound,
            int remainingToEmit) {
        public ParticlePlan {
            if (tick < 0 || createdThisTick < 0 || cumulativeCreated < 0 || activeUpperBound < 0
                    || remainingToEmit < 0) {
                throw new IllegalArgumentException("Medium radial particle accounting cannot be negative");
            }
        }
    }

    public static Branch branch(long detonationSeed, Path path, int layerIndex, int branchIndex) {
        if (path == null) {
            throw new IllegalArgumentException("Medium radial path may not be null");
        }
        Layer layer = path.layer(layerIndex);
        if (branchIndex < 0 || branchIndex >= layer.branchCount()) {
            throw new IllegalArgumentException("Medium radial branch index is outside the configured layer");
        }
        long branchSeed = mix64(detonationSeed ^ BRANCH_SALT
                ^ ((long) path.ordinal() * 0x9E3779B97F4A7C15L)
                ^ ((long) layerIndex * 0xD1342543DE82EF95L)
                ^ ((long) branchIndex * 0x94D049BB133111EBL));
        double sphereY = 1.0D - 2.0D * ((double) branchIndex + 0.5D) / layer.branchCount();
        double azimuth = branchIndex * GOLDEN_ANGLE
                + randomUnit(detonationSeed ^ AZIMUTH_SALT ^ path.ordinal()) * TWO_PI
                + centered(branchSeed, AZIMUTH_SALT) * 0.055D;
        double elevation = Math.asin(clamp(sphereY, -1.0D, 1.0D))
                + centered(branchSeed, ELEVATION_SALT) * 0.035D;
        double horizontal = Math.cos(elevation);
        Vec3 direction = new Vec3(
                horizontal * Math.cos(azimuth), Math.sin(elevation), horizontal * Math.sin(azimuth)).normalize();
        return new Branch(
                path,
                layerIndex,
                layer,
                branchIndex,
                branchSeed,
                direction,
                azimuth,
                randomUnit(branchSeed ^ PHASE_SALT) * TWO_PI);
    }

    public static BranchSample sample(long detonationSeed, Path path, int layerIndex, int branchIndex, int segmentIndex) {
        return sample(branch(detonationSeed, path, layerIndex, branchIndex), segmentIndex);
    }

    public static BranchSample sample(Branch branch, int segmentIndex) {
        if (branch == null || segmentIndex < 0 || segmentIndex >= branch.layer().segmentsPerBranch()) {
            throw new IllegalArgumentException("Medium radial segment index is outside the configured layer");
        }
        double progress = (double) segmentIndex / (branch.layer().segmentsPerBranch() - 1);
        ColorBand band = colorBand(branch, progress);
        return new BranchSample(
                branch,
                segmentIndex,
                branch.layer().startTick() + segmentIndex,
                progress,
                positionFor(branch, progress),
                band,
                brightness(branch, segmentIndex, band),
                lifetime(branch, segmentIndex, band),
                twinkles(branch, segmentIndex, band));
    }

    public static int particlesCreatedThisTick(Path path, int tick) {
        if (path == null || tick < 0 || tick > path.maximumEmissionTick()) {
            return 0;
        }
        int total = 0;
        for (Layer layer : path.layers()) {
            if (tick >= layer.startTick() && tick <= layer.finalEmissionTick()) {
                total += layer.branchCount();
            }
        }
        return total;
    }

    public static int particlesCreatedThroughTick(Path path, int tick) {
        if (path == null) {
            throw new IllegalArgumentException("Medium radial path may not be null");
        }
        if (tick < 0) {
            return 0;
        }
        int total = 0;
        for (Layer layer : path.layers()) {
            int emittedSegments = Math.max(0, Math.min(layer.segmentsPerBranch(), tick - layer.startTick() + 1));
            total += emittedSegments * layer.branchCount();
        }
        return total;
    }

    public static int activeParticleUpperBoundAtTick(Path path, int tick) {
        if (path == null) {
            throw new IllegalArgumentException("Medium radial path may not be null");
        }
        if (tick < 0) {
            return 0;
        }
        int total = 0;
        int firstLiveEmission = tick - path.maximumLifetimeTicks();
        for (Layer layer : path.layers()) {
            int first = Math.max(layer.startTick(), firstLiveEmission);
            int last = Math.min(tick, layer.finalEmissionTick());
            if (first <= last) {
                total += (last - first + 1) * layer.branchCount();
            }
        }
        return total;
    }

    public static ParticlePlan particlePlanAtTick(Path path, int tick) {
        if (path == null || tick < 0) {
            throw new IllegalArgumentException("Medium radial particle-plan request is invalid");
        }
        int cumulative = particlesCreatedThroughTick(path, tick);
        return new ParticlePlan(
                tick,
                particlesCreatedThisTick(path, tick),
                cumulative,
                activeParticleUpperBoundAtTick(path, tick),
                path.totalParticles() - cumulative);
    }

    public static Bounds conservativeBounds(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Medium radial path may not be null");
        }
        double radius = path.maximumRadius();
        return new Bounds(-radius, -radius, -radius, radius, radius, radius, radius);
    }

    public static boolean fitsEnvelope(Path path) {
        return path != null && conservativeBounds(path).fitsEnvelope(APPROVED_FULL_ENVELOPE);
    }

    /** Static proof utility for integration tests: deterministic samples, local cap, finite lifetime, and envelope. */
    public static boolean staticContractHolds(Path path, long detonationSeed) {
        if (path == null || !path.hasFiniteLocalPlan() || !fitsEnvelope(path)
                || path.totalParticles() < Math.ceil(REFERENCE_TOTAL_PARTICLES * MIN_PARTICLE_RATIO)
                || path.totalParticles() > Math.floor(REFERENCE_TOTAL_PARTICLES * MAX_PARTICLE_RATIO)
                || particlePlanAtTick(path, path.maximumEmissionTick()).cumulativeCreated() != path.totalParticles()
                || particlePlanAtTick(path, path.maximumEmissionTick()).activeUpperBound() > path.totalParticles()) {
            return false;
        }
        for (int layerIndex = 0; layerIndex < path.layers().size(); layerIndex++) {
            Layer layer = path.layer(layerIndex);
            for (int branchIndex = 0; branchIndex < layer.branchCount(); branchIndex++) {
                Branch first = branch(detonationSeed, path, layerIndex, branchIndex);
                Branch second = branch(detonationSeed, path, layerIndex, branchIndex);
                if (!first.equals(second)) {
                    return false;
                }
                for (int segmentIndex = 0; segmentIndex < layer.segmentsPerBranch(); segmentIndex++) {
                    BranchSample sample = sample(first, segmentIndex);
                    if (sample.emissionTick() > path.maximumEmissionTick()
                            || sample.position().lengthSqr() > path.maximumRadius() * path.maximumRadius()
                                    + RADIUS_EPSILON
                            || sample.lifetime() < path.minimumLifetimeTicks()
                            || sample.lifetime() > path.maximumLifetimeTicks()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static ColorBand colorBand(Branch branch, double progress) {
        if (branch.layerIndex() == 0 && progress < 0.72D) {
            return ColorBand.CORE;
        }
        if (branch.layerIndex() == branch.path().layers().size() - 1 || progress >= 0.78D) {
            return ColorBand.EDGE;
        }
        return ColorBand.BODY;
    }

    private static Vec3 positionFor(Branch branch, double progress) {
        Path path = branch.path();
        Layer layer = branch.layer();
        double radialProgress = radialProgress(path.form(), branch, progress);
        double radius = INITIAL_RADIUS + (layer.maximumRadius() - INITIAL_RADIUS) * radialProgress;
        double azimuth = branch.azimuth() + layer.phaseOffset() + rotationFor(path, branch, progress);
        double horizontalScale = Math.sqrt(branch.direction().x * branch.direction().x
                + branch.direction().z * branch.direction().z);
        double horizontal = radius * horizontalScale;
        double vertical = branch.direction().y * radius * layer.verticalScale();
        double lateral = path.sideAmplitude() * Math.sin(Math.PI * progress)
                * Math.sin(branch.phase() + TWO_PI * progress + layer.phaseOffset());

        switch (path.form()) {
            case STAGGERED_RING -> vertical += path.verticalAmplitude() * Math.sin(TWO_PI * progress + layer.phaseOffset());
            case INNER_OUTER_PHASE -> vertical += path.verticalAmplitude() * Math.sin(Math.PI * progress + branch.layerIndex());
            case CROWN_LANCE -> vertical += path.verticalAmplitude() * Math.sin(Math.PI * progress);
            case SPLIT_FAN -> vertical = branch.direction().y * radius * 0.48D
                    + path.verticalAmplitude() * Math.sin(Math.PI * progress) * (branch.index() & 1) * 0.5D;
            case LATTICE_WEAVE -> lateral += path.sideAmplitude() * 0.65D
                    * Math.sin(TWO_PI * 2.0D * progress + branch.phase());
            case SPIRAL_HELIX -> vertical += path.verticalAmplitude() * 0.42D
                    * Math.sin(TWO_PI * progress + branch.phase());
            case COMET_SWEEP -> {
                vertical += path.verticalAmplitude() * 0.34D * progress * progress;
                lateral += path.sideAmplitude() * 0.45D * smoothStep(progress);
            }
            case TWIN_HELIX -> lateral += path.sideAmplitude() * Math.sin(TWO_PI * 2.0D * progress
                    + ((branch.index() & 1) == 0 ? 0.0D : Math.PI));
            case RING_BLOOM -> vertical += path.verticalAmplitude() * Math.sin(TWO_PI * 1.5D * progress
                    + layer.phaseOffset()) * Math.sin(Math.PI * progress);
            case PHASED_PINWHEEL -> vertical += path.verticalAmplitude() * 0.36D
                    * Math.cos(TWO_PI * progress + branch.phase());
            case FRACTURE_RAY -> lateral += ((branch.index() & 1) == 0 ? 1.0D : -1.0D)
                    * path.sideAmplitude() * 0.70D * Math.sin(Math.PI * progress);
            case DIAMOND_STAR -> vertical += path.verticalAmplitude() * 0.28D
                    * Math.sin(4.0D * azimuth) * Math.sin(Math.PI * progress);
            case CROSSWEAVE -> {
                lateral += path.sideAmplitude() * 0.75D * Math.sin(TWO_PI * 3.0D * progress + branch.phase());
                vertical += path.verticalAmplitude() * 0.32D * Math.cos(TWO_PI * 2.0D * progress + layer.phaseOffset());
            }
            case WAVE_RADIANT -> vertical += path.verticalAmplitude() * Math.sin(TWO_PI * 2.0D * progress + branch.phase());
            case TRIPLE_PULSE -> vertical += path.verticalAmplitude() * 0.24D
                    * Math.sin(TWO_PI * 3.0D * progress + layer.phaseOffset());
            case AURORA_ARC -> vertical += path.verticalAmplitude() * Math.sin(Math.PI * progress)
                    * Math.sin(TWO_PI * progress + branch.phase());
            case ICE_WHEEL -> vertical += path.verticalAmplitude() * 0.30D
                    * Math.cos(TWO_PI * 2.5D * progress + branch.phase());
            default -> {
                // The remaining forms are fully expressed by their radial and rotation transforms below.
            }
        }

        Vec3 raw = new Vec3(horizontal * Math.cos(azimuth), vertical, horizontal * Math.sin(azimuth));
        Vec3 side = new Vec3(-Math.sin(azimuth), 0.0D, Math.cos(azimuth));
        raw = raw.add(side.scale(lateral));
        return radiusSafe(raw, path.maximumRadius());
    }

    private static double radialProgress(PathForm form, Branch branch, double progress) {
        double eased = smoothStep(progress);
        return switch (form) {
            case SEGMENTED_BEAM -> smoothStep(Math.floor(progress * 7.0D) / 6.0D);
            case SHORT_RAY_SHELL -> clamp(eased * 0.93D, 0.0D, 1.0D);
            case HOLLOW_CORE -> 0.18D + eased * 0.82D;
            case FRACTURE_RAY -> clamp(eased * ((branch.index() & 1) == 0 ? 1.0D : 0.82D), 0.0D, 1.0D);
            case DIAMOND_STAR -> clamp(eased * (0.84D + 0.16D * Math.abs(Math.sin(2.0D * branch.azimuth()))), 0.0D, 1.0D);
            case PULSE_RADIAL -> clamp(eased * (0.91D + 0.09D * Math.sin(TWO_PI * 1.5D * progress)), 0.0D, 1.0D);
            case TRIPLE_PULSE -> clamp(eased * (0.89D + 0.11D * Math.sin(TWO_PI * 3.0D * progress)), 0.0D, 1.0D);
            case RING_BLOOM -> clamp(eased * (0.90D + 0.10D * Math.sin(TWO_PI * progress + branch.layer().phaseOffset())), 0.0D, 1.0D);
            default -> eased;
        };
    }

    private static double rotationFor(Path path, Branch branch, double progress) {
        double rotation = TWO_PI * path.rotationTurns() * progress;
        return switch (path.form()) {
            case ROTARY_WHEEL -> rotation + TWO_PI * 0.55D * progress;
            case SPIRAL_HELIX -> rotation + TWO_PI * 0.32D * Math.sin(Math.PI * progress);
            case COMET_SWEEP -> rotation + 0.72D * Math.sin(Math.PI * progress);
            case TWIN_HELIX -> rotation + TWO_PI * (branch.index() & 1) * 0.18D * progress;
            case PHASED_PINWHEEL -> rotation + TWO_PI * 0.22D * Math.floorMod(branch.index(), 5) / 5.0D;
            case ICE_WHEEL -> rotation + TWO_PI * 0.44D * Math.sin(Math.PI * progress);
            case INNER_OUTER_PHASE -> rotation + branch.layerIndex() * 0.37D;
            case CROSSWEAVE -> rotation + 0.18D * Math.sin(TWO_PI * 2.0D * progress + branch.phase());
            default -> rotation;
        };
    }

    private static int lifetime(Branch branch, int segmentIndex, ColorBand band) {
        int lower = branch.path().minimumLifetimeTicks() + switch (band) {
            case CORE -> 0;
            case BODY -> 3;
            case EDGE -> 6;
        };
        int upper = branch.path().maximumLifetimeTicks() - switch (band) {
            case CORE -> 8;
            case BODY -> 4;
            case EDGE -> 0;
        };
        return randomInt(branch.seed(), LIFETIME_SALT + segmentIndex, lower, upper);
    }

    private static float brightness(Branch branch, int segmentIndex, ColorBand band) {
        double base = switch (band) {
            case CORE -> 1.13D;
            case BODY -> 1.05D;
            case EDGE -> 1.17D;
        };
        return (float) (base + randomUnit(branch.seed() ^ (BRIGHTNESS_SALT + segmentIndex)) * 0.13D);
    }

    private static boolean twinkles(Branch branch, int segmentIndex, ColorBand band) {
        if (band == ColorBand.CORE) {
            return false;
        }
        double chance = band == ColorBand.EDGE ? 0.58D : 0.24D;
        if (branch.path().form() == PathForm.SEGMENTED_BEAM || branch.path().form() == PathForm.FRACTURE_RAY) {
            chance += 0.08D;
        }
        return randomUnit(branch.seed() ^ (TWINKLE_SALT + segmentIndex)) < chance;
    }

    private static Vec3 radiusSafe(Vec3 value, double maximumRadius) {
        double lengthSqr = value.lengthSqr();
        double maximumSqr = maximumRadius * maximumRadius;
        return lengthSqr <= maximumSqr ? value : value.scale(maximumRadius / Math.sqrt(lengthSqr));
    }

    private static int randomInt(long seed, long salt, int minimum, int maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Medium radial lifetime range is inverted");
        }
        return minimum + (int) Math.floor(randomUnit(seed ^ salt) * (maximum - minimum + 1));
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long seed, long salt) {
        return randomUnit(seed ^ salt) - 0.5D;
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
