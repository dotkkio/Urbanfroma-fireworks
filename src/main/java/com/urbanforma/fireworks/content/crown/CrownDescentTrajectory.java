package com.urbanforma.fireworks.content.crown;

import com.urbanforma.fireworks.content.FireworkStyle;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.phys.Vec3;

/**
 * Deterministic, terrain-agnostic geometry for the crown descent client program.
 *
 * <p>This type deliberately contains no client world lookup, particle allocation, server state, or networking.
 * The client program resolves the endpoint terrain column immediately before it creates any spark.</p>
 */
public final class CrownDescentTrajectory {
    public static final Set<String> SUPPORTED_STYLE_IDS = Set.of(
            "coral_rose_crown_sphere",
            "glacier_teal_crown_sphere",
            "opal_rose_crown_sphere",
            "platinum_onyx_crown_sphere",
            "emerald_silver_crown_sphere");

    public static final int BRANCH_COUNT = 72;
    public static final int TRAIL_SEGMENTS_PER_BRANCH = 8;
    public static final int INITIAL_BLOOM_PARTICLE_COUNT = BRANCH_COUNT * TRAIL_SEGMENTS_PER_BRANCH;
    public static final int LOCAL_PEAK_OWNED_PARTICLES = INITIAL_BLOOM_PARTICLE_COUNT;
    public static final int VISUAL_LIFETIME_TICKS = 300;
    public static final int LAST_VISUAL_TICK = VISUAL_LIFETIME_TICKS - 1;
    public static final int RETIREMENT_START_TICK = 240;
    public static final int RETIREMENT_STAGGER_TICKS = 49;

    /** Every accepted endpoint stays exactly this far above the client-observed terrain height. */
    public static final double GROUND_CLEARANCE_BLOCKS = 30.0D;
    public static final double MIN_TERMINAL_DROP_BLOCKS = 24.0D;
    public static final double MAX_TERMINAL_DROP_BLOCKS = 192.0D;
    public static final double MAX_HORIZONTAL_ENVELOPE_BLOCKS = 88.0D;
    public static final double MAX_UPWARD_ENVELOPE_BLOCKS = 27.0D;
    public static final double MAX_DOWNWARD_ENVELOPE_BLOCKS = 200.0D;

    private static final int RING_COUNT = 3;
    private static final int SPOKES_PER_RING = BRANCH_COUNT / RING_COUNT;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double FAST_OPEN_END = 0.22D;
    private static final double RELAXED_FALL_END = 0.74D;
    private static final double TRAIL_DELAY_FRACTION = 0.48D;
    private static final double TRAIL_MIN_MOTION_FACTOR = 0.42D;
    private static final double MAX_CROWN_LIFT = 5.0D;
    private static final double ENVELOPE_EPSILON = 1.0E-7D;
    private static final long BRANCH_SALT = 0xC13FA9A902A6328FL;
    private static final long AZIMUTH_SALT = 0x91E10DA5C79E7B1DL;
    private static final long RADIUS_SALT = 0xD1B54A32D192ED03L;
    private static final long SWAY_SALT = 0x94D049BB133111EBL;
    private static final long RETIREMENT_SALT = 0xBF58476D1CE4E5B9L;
    private static final Palette PROOF_PALETTE = new Palette(
            new Rgb(1.0F, 0.4F, 0.2F),
            new Rgb(0.8F, 0.2F, 0.7F),
            new Rgb(1.0F, 0.95F, 0.7F));

    private final long seed;
    private final Palette palette;

    public CrownDescentTrajectory(long seed, Palette palette) {
        this.seed = seed;
        this.palette = Objects.requireNonNull(palette, "palette");
    }

    /**
     * Creates the typed crown program only for the five established CROWN_SPHERE styles.
     * Future CROWN_SPHERE entries stay on their old path until they receive an explicit trajectory contract.
     */
    public static CrownDescentTrajectory fromStyle(FireworkStyle style, long seed) {
        Objects.requireNonNull(style, "style");
        if (!supports(style)) {
            throw new IllegalArgumentException("Crown descent does not support style " + style.id());
        }
        return new CrownDescentTrajectory(seed, new Palette(
                copy(style.primaryColor()),
                copy(style.secondaryColor()),
                copy(style.accentColor())));
    }

    public static boolean supports(FireworkStyle style) {
        return style != null
                && style.shape() == FireworkStyle.Shape.CROWN_SPHERE
                && SUPPORTED_STYLE_IDS.contains(style.id());
    }

    public long seed() {
        return this.seed;
    }

    public Palette palette() {
        return this.palette;
    }

    public Branch branch(int branchIndex) {
        return branch(this.seed, branchIndex);
    }

    public static Branch branch(long seed, int branchIndex) {
        validateBranchIndex(branchIndex);
        int ring = branchIndex % RING_COUNT;
        int spoke = branchIndex / RING_COUNT;
        long branchSeed = mix64(seed ^ BRANCH_SALT ^ ((long) branchIndex * 0x9E3779B97F4A7C15L));
        double rotation = randomUnit(seed ^ AZIMUTH_SALT) * TWO_PI;
        double jitter = centered(branchSeed ^ AZIMUTH_SALT) * 0.055D;
        double azimuth = rotation + spoke * TWO_PI / SPOKES_PER_RING + jitter;
        double startRadius = switch (ring) {
            case 0 -> 16.0D;
            case 1 -> 28.0D;
            case 2 -> 38.0D;
            default -> throw new IllegalStateException("Unexpected crown ring " + ring);
        };
        double startHeight = switch (ring) {
            case 0 -> 21.0D;
            case 1 -> 13.0D;
            case 2 -> 6.0D;
            default -> throw new IllegalStateException("Unexpected crown ring " + ring);
        };
        double terminalRadius = 68.0D + randomUnit(branchSeed ^ RADIUS_SALT) * 14.0D;
        double sway = 1.2D + randomUnit(branchSeed ^ SWAY_SALT) * 1.6D;
        double swayPhase = randomUnit(branchSeed ^ (SWAY_SALT << 1));
        return new Branch(branchIndex, branchSeed, ring, azimuth, startRadius, startHeight,
                terminalRadius, sway, swayPhase);
    }

    /** Returns the loaded client column required for a branch's terminal endpoint. */
    public Column terminalColumn(double originX, double originZ, Branch branch) {
        validateFinite(originX, "originX");
        validateFinite(originZ, "originZ");
        Objects.requireNonNull(branch, "branch");
        return new Column(
                floorToInt(originX + Math.cos(branch.azimuth()) * branch.terminalRadius()),
                floorToInt(originZ + Math.sin(branch.azimuth()) * branch.terminalRadius()));
    }

    /**
     * Converts a client-only terrain-height result to an exact endpoint. The height is never queried here.
     */
    public Terminal terminal(
            double originX, double originZ, Branch branch, int terrainHeight) {
        Column column = terminalColumn(originX, originZ, branch);
        return new Terminal(
                column,
                originX + Math.cos(branch.azimuth()) * branch.terminalRadius(),
                terrainHeight,
                originZ + Math.sin(branch.azimuth()) * branch.terminalRadius());
    }

    /** Rejects endpoints that cannot represent a visibly descending, bounded crown tail. */
    public boolean acceptsTerminal(double originY, Branch branch, Terminal terminal) {
        validateFinite(originY, "originY");
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(terminal, "terminal");
        double drop = originY + branch.startHeight() - terminal.targetY();
        return Double.isFinite(drop)
                && drop >= MIN_TERMINAL_DROP_BLOCKS
                && drop <= MAX_TERMINAL_DROP_BLOCKS;
    }

    /**
     * Samples one retained spark. Every segment starts in the complete crown bloom and reaches the same safe
     * terrain-relative endpoint by the final tick, with a segment-specific delay stretching the visible tail.
     */
    public Sample sample(
            double originX,
            double originY,
            double originZ,
            Branch branch,
            Terminal terminal,
            int age,
            int trailSegment) {
        validateFinite(originX, "originX");
        validateFinite(originY, "originY");
        validateFinite(originZ, "originZ");
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(terminal, "terminal");
        validateAge(age);
        validateTrailSegment(trailSegment);
        if (!acceptsTerminal(originY, branch, terminal)) {
            throw new IllegalArgumentException("Crown terminal is outside the safe descent envelope");
        }

        double progress = segmentProgress(age, trailSegment);
        double radial = branch.startRadius()
                + (branch.terminalRadius() - branch.startRadius()) * radialProgress(progress);
        double sway = branch.sway()
                * Math.sin(TWO_PI * (progress + branch.swayPhase()))
                * Math.sin(Math.PI * progress);
        double cos = Math.cos(branch.azimuth());
        double sin = Math.sin(branch.azimuth());
        double x = originX + cos * radial - sin * sway;
        double z = originZ + sin * radial + cos * sway;
        double startY = originY + branch.startHeight();
        double vertical = verticalProgress(progress);
        double lift = MAX_CROWN_LIFT * (0.72D + branch.ring() * 0.12D)
                * Math.sin(Math.PI * progress) * (1.0D - progress * 0.20D);
        double y = startY + (terminal.targetY() - startY) * vertical + lift;

        return new Sample(
                branch,
                terminal,
                age,
                trailSegment,
                progress,
                new Vec3(x, y, z),
                this.palette.colorForRing(branch.ring()),
                1.02F + trailSegment * 0.025F,
                1.08F - trailSegment * 0.035F,
                retirementTick(branch, trailSegment));
    }

    public static int retirementTick(Branch branch, int trailSegment) {
        Objects.requireNonNull(branch, "branch");
        validateTrailSegment(trailSegment);
        if (trailSegment == 0) {
            return VISUAL_LIFETIME_TICKS;
        }
        long value = mix64(branch.branchSeed() ^ RETIREMENT_SALT
                ^ ((long) trailSegment * 0xD1342543DE82EF95L));
        return RETIREMENT_START_TICK + (int) Math.floorMod(value, (long) RETIREMENT_STAGGER_TICKS);
    }

    public int activeParticleUpperBoundAtTick(int tick) {
        if (tick < 0) {
            return 0;
        }
        if (tick >= VISUAL_LIFETIME_TICKS) {
            return 0;
        }
        int active = 0;
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(branchIndex);
            for (int segment = 0; segment < TRAIL_SEGMENTS_PER_BRANCH; segment++) {
                if (tick < retirementTick(branch, segment)) {
                    active++;
                }
            }
        }
        return active;
    }

    public static int particlesCreatedThisTick(int tick) {
        return tick == 0 ? INITIAL_BLOOM_PARTICLE_COUNT : 0;
    }

    public static Bounds declaredEnvelope() {
        return new Bounds(
                -MAX_HORIZONTAL_ENVELOPE_BLOCKS,
                -MAX_DOWNWARD_ENVELOPE_BLOCKS,
                -MAX_HORIZONTAL_ENVELOPE_BLOCKS,
                MAX_HORIZONTAL_ENVELOPE_BLOCKS,
                MAX_UPWARD_ENVELOPE_BLOCKS,
                MAX_HORIZONTAL_ENVELOPE_BLOCKS);
    }

    /** Exhaustive fixed-lattice proof suitable for a later isolated GameTest without loading any client class. */
    public static boolean staticContractHolds(long seed) {
        if (SUPPORTED_STYLE_IDS.size() != 5
                || BRANCH_COUNT != 72
                || TRAIL_SEGMENTS_PER_BRANCH != 8
                || INITIAL_BLOOM_PARTICLE_COUNT != 576
                || LOCAL_PEAK_OWNED_PARTICLES != INITIAL_BLOOM_PARTICLE_COUNT
                || VISUAL_LIFETIME_TICKS != 300
                || GROUND_CLEARANCE_BLOCKS != 30.0D) {
            return false;
        }

        CrownDescentTrajectory trajectory = new CrownDescentTrajectory(seed, PROOF_PALETTE);
        Bounds bounds = declaredEnvelope();
        int maximumActive = 0;
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = trajectory.branch(branchIndex);
            int terrainHeight = (int) (branch.startHeight() - MAX_TERMINAL_DROP_BLOCKS - GROUND_CLEARANCE_BLOCKS);
            Terminal terminal = trajectory.terminal(0.0D, 0.0D, branch, terrainHeight);
            if (!trajectory.acceptsTerminal(0.0D, branch, terminal)) {
                return false;
            }
            for (int segment = 0; segment < TRAIL_SEGMENTS_PER_BRANCH; segment++) {
                int retireAt = retirementTick(branch, segment);
                if (retireAt < RETIREMENT_START_TICK || retireAt > VISUAL_LIFETIME_TICKS) {
                    return false;
                }
                for (int tick = 0; tick < VISUAL_LIFETIME_TICKS; tick++) {
                    Sample sample = trajectory.sample(0.0D, 0.0D, 0.0D, branch, terminal, tick, segment);
                    if (!bounds.contains(sample.position())) {
                        return false;
                    }
                    if (tick == LAST_VISUAL_TICK && !sample.endsAtTerminal()) {
                        return false;
                    }
                }
            }
        }
        for (int tick = 0; tick < VISUAL_LIFETIME_TICKS; tick++) {
            maximumActive = Math.max(maximumActive, trajectory.activeParticleUpperBoundAtTick(tick));
        }
        return maximumActive == LOCAL_PEAK_OWNED_PARTICLES
                && trajectory.activeParticleUpperBoundAtTick(LAST_VISUAL_TICK) == BRANCH_COUNT;
    }

    private static Rgb copy(FireworkStyle.Rgb color) {
        Objects.requireNonNull(color, "color");
        return new Rgb(color.red(), color.green(), color.blue());
    }

    private static double segmentProgress(int age, int trailSegment) {
        double timeline = age / (double) LAST_VISUAL_TICK;
        double trailFraction = trailSegment / (TRAIL_SEGMENTS_PER_BRANCH - 1.0D);
        double delay = trailFraction * TRAIL_DELAY_FRACTION;
        double delayed = clamp((timeline - delay) / (1.0D - delay), 0.0D, 1.0D);
        // Every retained spark starts moving immediately; later segments still preserve a long delayed history.
        double initialGlide = timeline * (1.0D - (1.0D - TRAIL_MIN_MOTION_FACTOR) * trailFraction);
        return initialGlide + (1.0D - initialGlide) * delayed;
    }

    private static double radialProgress(double progress) {
        if (progress <= FAST_OPEN_END) {
            return 0.58D * fastOut(progress / FAST_OPEN_END);
        }
        if (progress <= RELAXED_FALL_END) {
            return 0.58D + 0.20D * smoothStep((progress - FAST_OPEN_END)
                    / (RELAXED_FALL_END - FAST_OPEN_END));
        }
        return 0.78D + 0.22D * easeIn((progress - RELAXED_FALL_END)
                / (1.0D - RELAXED_FALL_END));
    }

    private static double verticalProgress(double progress) {
        if (progress <= FAST_OPEN_END) {
            return 0.18D * fastOut(progress / FAST_OPEN_END);
        }
        if (progress <= RELAXED_FALL_END) {
            return 0.18D + 0.30D * smoothStep((progress - FAST_OPEN_END)
                    / (RELAXED_FALL_END - FAST_OPEN_END));
        }
        return 0.48D + 0.52D * easeIn((progress - RELAXED_FALL_END)
                / (1.0D - RELAXED_FALL_END));
    }

    private static double fastOut(double value) {
        return 1.0D - Math.pow(1.0D - clamp(value, 0.0D, 1.0D), 1.55D);
    }

    private static double easeIn(double value) {
        return Math.pow(clamp(value, 0.0D, 1.0D), 1.8D);
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long value) {
        return randomUnit(value) - 0.5D;
    }

    private static int floorToInt(double value) {
        validateFinite(value, "column coordinate");
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Crown terminal column is outside integer coordinates");
        }
        return (int) Math.floor(value);
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Crown branch index is outside the configured count");
        }
    }

    private static void validateTrailSegment(int trailSegment) {
        if (trailSegment < 0 || trailSegment >= TRAIL_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Crown trail segment is outside the configured count");
        }
    }

    private static void validateAge(int age) {
        if (age < 0 || age >= VISUAL_LIFETIME_TICKS) {
            throw new IllegalArgumentException("Crown age is outside the 300-tick visual lifetime");
        }
    }

    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Rgb(float red, float green, float blue) {
        public Rgb {
            if (!Float.isFinite(red) || !Float.isFinite(green) || !Float.isFinite(blue)
                    || red < 0.0F || red > 1.0F
                    || green < 0.0F || green > 1.0F
                    || blue < 0.0F || blue > 1.0F) {
                throw new IllegalArgumentException("Crown RGB channels must be finite values between zero and one");
            }
        }
    }

    public record Palette(Rgb primary, Rgb secondary, Rgb accent) {
        public Palette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
        }

        public Rgb colorForRing(int ring) {
            return switch (ring) {
                case 0 -> this.accent;
                case 1 -> this.secondary;
                case 2 -> this.primary;
                default -> throw new IllegalArgumentException("Crown ring is outside the palette");
            };
        }
    }

    public record Branch(
            int index,
            long branchSeed,
            int ring,
            double azimuth,
            double startRadius,
            double startHeight,
            double terminalRadius,
            double sway,
            double swayPhase) {
        public Branch {
            validateBranchIndex(index);
            if (ring < 0 || ring >= RING_COUNT
                    || !Double.isFinite(azimuth)
                    || !Double.isFinite(startRadius) || startRadius <= 0.0D
                    || !Double.isFinite(startHeight) || startHeight < 0.0D
                    || !Double.isFinite(terminalRadius) || terminalRadius < startRadius
                    || terminalRadius > 82.0D
                    || !Double.isFinite(sway) || sway < 0.0D || sway > 2.8D
                    || !Double.isFinite(swayPhase) || swayPhase < 0.0D || swayPhase > 1.0D) {
                throw new IllegalArgumentException("Invalid crown descent branch");
            }
        }
    }

    public record Column(int blockX, int blockZ) {
    }

    /** Terrain height is a client observation; targetY remains exactly 30 blocks above that observation. */
    public record Terminal(Column column, double x, int terrainHeight, double z) {
        public Terminal {
            Objects.requireNonNull(column, "column");
            if (!Double.isFinite(x) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Crown terminal coordinates must be finite");
            }
        }

        public double targetY() {
            return this.terrainHeight + GROUND_CLEARANCE_BLOCKS;
        }
    }

    public record Sample(
            Branch branch,
            Terminal terminal,
            int age,
            int trailSegment,
            double progress,
            Vec3 position,
            Rgb color,
            float brightness,
            float scale,
            int retirementTick) {
        public Sample {
            Objects.requireNonNull(branch, "branch");
            Objects.requireNonNull(terminal, "terminal");
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(color, "color");
            validateAge(age);
            validateTrailSegment(trailSegment);
            if (!Double.isFinite(progress) || progress < 0.0D || progress > 1.0D
                    || !Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)
                    || !Float.isFinite(brightness) || brightness <= 0.0F
                    || !Float.isFinite(scale) || scale <= 0.0F
                    || retirementTick < RETIREMENT_START_TICK || retirementTick > VISUAL_LIFETIME_TICKS) {
                throw new IllegalArgumentException("Invalid crown descent sample");
            }
        }

        public boolean endsAtTerminal() {
            return this.age == LAST_VISUAL_TICK
                    && Math.abs(this.position.x - this.terminal.x()) <= ENVELOPE_EPSILON
                    && Math.abs(this.position.y - this.terminal.targetY()) <= ENVELOPE_EPSILON
                    && Math.abs(this.position.z - this.terminal.z()) <= ENVELOPE_EPSILON;
        }
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Crown bounds must be finite and ordered");
            }
        }

        public boolean contains(Vec3 position) {
            return position != null
                    && position.x >= this.minX - ENVELOPE_EPSILON
                    && position.x <= this.maxX + ENVELOPE_EPSILON
                    && position.y >= this.minY - ENVELOPE_EPSILON
                    && position.y <= this.maxY + ENVELOPE_EPSILON
                    && position.z >= this.minZ - ENVELOPE_EPSILON
                    && position.z <= this.maxZ + ENVELOPE_EPSILON;
        }
    }
}
