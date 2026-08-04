package com.urbanforma.fireworks.content;

import net.minecraft.world.phys.Vec3;

/**
 * Common-side deterministic deformation for the continuous radiant-willow firework.
 *
 * <p>All 4,800 sparks are created by the original radiant shell. Its three short-lived center rings naturally
 * disappear, while the remaining 4,320 sparks are repositioned along their own branches for the whole willow
 * extension. There is deliberately no continuation-node program and no clear-delay between the two appearances.</p>
 */
public final class RadiantWillowTrajectory {
    public static final int BRANCH_COUNT = RadiantTrajectory.BRANCH_COUNT;
    public static final int RADIANT_SEGMENTS_PER_BRANCH = RadiantTrajectory.SEGMENTS_PER_BRANCH;
    public static final int SHORT_LIVED_RADIANT_SEGMENTS = RadiantTrajectory.CORE_SEGMENT_COUNT;
    public static final int MANAGED_FIRST_RADIANT_SEGMENT = SHORT_LIVED_RADIANT_SEGMENTS;
    public static final int MANAGED_SEGMENTS_PER_BRANCH =
            RADIANT_SEGMENTS_PER_BRANCH - MANAGED_FIRST_RADIANT_SEGMENT;
    public static final int RADIANT_NODE_COUNT = BRANCH_COUNT * RADIANT_SEGMENTS_PER_BRANCH;
    public static final int MANAGED_NODE_COUNT = BRANCH_COUNT * MANAGED_SEGMENTS_PER_BRANCH;
    /** The willow only reuses radiant sparks; it may not allocate a second particle set. */
    public static final int NEW_EXTENSION_NODE_COUNT = 0;
    public static final int TOTAL_NODE_COUNT = RADIANT_NODE_COUNT;
    public static final int BRANCHES_PER_TICK = BRANCH_COUNT;
    public static final int MIN_EXTENSION_TICKS = 100;
    public static final int MAX_EXTENSION_TICKS = 140;
    public static final int MAX_COMPLETE_RINGS_PER_TICK = 4;
    public static final int MAX_PARTICLES_PER_TICK = BRANCH_COUNT;
    public static final int MAX_GLOBAL_PARTICLES_PER_TICK = MAX_COMPLETE_RINGS_PER_TICK * BRANCH_COUNT;
    public static final int MAX_TERMINAL_RETIREMENTS_PER_BRANCH = 5;
    public static final double TERMINAL_RETIREMENT_START_PROGRESS = 0.35D;
    public static final int TERMINAL_RETIREMENT_INTERVAL_MIN_TICKS = 12;
    public static final int TERMINAL_RETIREMENT_INTERVAL_MAX_TICKS = 17;
    /** A terminal spark remains visible for this many ticks while vanilla flicker is enabled. */
    public static final int RETIREMENT_FLICKER_MIN_TICKS = 5;
    public static final int RETIREMENT_FLICKER_MAX_TICKS = 9;
    /** Final-drain windows start at a seeded offset so the retained front does not vanish in one frame. */
    public static final int FINAL_DRAIN_FLICKER_MAX_DELAY_TICKS = 6;
    public static final int NEVER_RETIRES = -1;
    public static final double MIN_TANGENT_SPEED = 0.018D;
    public static final double MAX_TANGENT_SPEED = 0.030D;
    public static final double APPROVED_FULL_ENVELOPE = 220.0D;
    public static final double CONSERVATIVE_MOTION_MARGIN = 4.0D;

    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double TANGENT_DIFFERENCE = 1.0D / 240.0D;
    /* Vec3.normalize() returns ZERO below this magnitude; preserve the seeded branch direction instead. */
    private static final double MIN_NORMALIZABLE_LENGTH_SQR = 1.0E-8D;
    private static final long DURATION_SALT = 0xA54FF53A5F1D36F1L;
    private static final long RADIAL_SALT = 0x510E527FADE682D1L;
    private static final long DROP_SALT = 0x9B05688C2B3E6C1FL;
    private static final long BEND_SALT = 0x1F83D9ABFB41BD6BL;
    private static final long SWAY_SALT = 0x5BE0CD19137E2179L;
    private static final long SWAY_PHASE_SALT = 0xCBBB9D5DC1059ED8L;
    private static final long SWAY_FREQUENCY_SALT = 0x629A292A367CD507L;
    private static final long SWAY_SECONDARY_PHASE_SALT = 0x152FECD8F70E5939L;
    private static final long TANGENT_SALT = 0x9159015A3070DD17L;
    private static final long RETIREMENT_OFFSET_SALT = 0x67332667FFC00B31L;
    private static final long RETIREMENT_INTERVAL_SALT = 0xBB67AE8584CAA73BL;
    private static final long FLICKER_PARTICLE_SALT = 0x3C6EF372FE94F82BL;
    private static final long TERMINAL_FLICKER_DURATION_SALT = 0xD1B54A32D192ED03L;
    private static final long TERMINAL_FLICKER_PHASE_SALT = 0x94D049BB133111EBL;
    private static final long FINAL_FLICKER_DELAY_SALT = 0x369DEA0F31A53F85L;
    private static final long FINAL_FLICKER_DURATION_SALT = 0xDB4F0B9175AE2165L;
    private static final long FINAL_FLICKER_PHASE_SALT = 0xBBE0563303A4615FL;

    private RadiantWillowTrajectory() {
    }

    /** Per-branch settings derived solely from the burst seed. */
    public record Branch(
            int index,
            long seed,
            RadiantTrajectory.Branch radiantBranch,
            double radialMultiplier,
            double dropMultiplier,
            double bendStart,
            double swayAmplitude,
            double swayPhase,
            double swayFrequency,
            double secondarySwayPhase,
            double tangentSpeed) {
    }

    /** A deterministic, exclusive tick window in which an existing spark visibly flickers before removal. */
    public record RetirementFlicker(int startTick, int endTick, int cadencePhase) {
        public RetirementFlicker {
            boolean disabled = startTick == NEVER_RETIRES && endTick == NEVER_RETIRES;
            if ((!disabled && (startTick < 0 || endTick <= startTick))
                    || (disabled && cadencePhase != 0)
                    || (cadencePhase != 0 && cadencePhase != 1)) {
                throw new IllegalArgumentException("Invalid radiant willow retirement flicker window");
            }
        }

        public boolean enabled() {
            return this.startTick != NEVER_RETIRES;
        }

        public boolean activeAt(int effectAgeTicks) {
            return this.enabled()
                    && effectAgeTicks >= this.startTick
                    && effectAgeTicks < this.endTick;
        }
    }

    private static final RetirementFlicker NO_RETIREMENT_FLICKER =
            new RetirementFlicker(NEVER_RETIRES, NEVER_RETIRES, 0);

    /** One existing radiant spark under client-side control during the extension. */
    public record ManagedParticleSample(
            Branch branch,
            int radiantSegmentIndex,
            int managedSegmentIndex,
            int extensionAgeTicks,
            int extensionDurationTicks,
            double extensionProgress,
            Vec3 position,
            Vec3 tangent,
            Vec3 velocity,
            RadiantTrajectory.ColorBand colorBand,
            RetirementFlicker terminalFlicker,
            int terminalRetirementTick,
            boolean retired) {
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                    || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Radiant willow bounds must be finite and ordered");
            }
        }

        public double spanX() {
            return this.maxX - this.minX;
        }

        public double spanY() {
            return this.maxY - this.minY;
        }

        public double spanZ() {
            return this.maxZ - this.minZ;
        }

        public double maxSpan() {
            return Math.max(this.spanX(), Math.max(this.spanY(), this.spanZ()));
        }

        public boolean fitsWithin(double envelope) {
            return envelope > 0.0D && this.maxSpan() <= envelope + 1.0E-9D;
        }
    }

    /** Builds the same deterministic branch for every client that receives a given burst seed. */
    public static Branch branch(FireworkStyle.RadiantWillowProfile profile, long payloadSeed, int branchIndex) {
        validateProfile(profile);
        validateBranchIndex(branchIndex);

        long seed = mix64(payloadSeed ^ ((long) branchIndex * 0x9E3779B97F4A7C15L) ^ 0x68E31DA4C9B2F705L);
        RadiantTrajectory.Branch radiantBranch = RadiantTrajectory.branch(
                profile.radiantProfile(), payloadSeed, branchIndex);
        return new Branch(
                branchIndex,
                seed,
                radiantBranch,
                between(seed, RADIAL_SALT, 0.90D, 1.00D),
                between(seed, DROP_SALT, 0.92D, 1.06D),
                between(seed, BEND_SALT, profile.bendStartMin(), profile.bendStartMax()),
                between(seed, SWAY_SALT, 2.25D, profile.maximumLateralSway()),
                randomUnit(seed ^ SWAY_PHASE_SALT) * TWO_PI,
                between(seed, SWAY_FREQUENCY_SALT, 1.20D, 2.65D),
                randomUnit(seed ^ SWAY_SECONDARY_PHASE_SALT) * TWO_PI,
                between(seed, TANGENT_SALT, MIN_TANGENT_SPEED, MAX_TANGENT_SPEED));
    }

    public static boolean isManagedRadiantSegment(int radiantSegmentIndex) {
        return radiantSegmentIndex >= MANAGED_FIRST_RADIANT_SEGMENT
                && radiantSegmentIndex < RADIANT_SEGMENTS_PER_BRANCH;
    }

    public static int managedSegmentIndex(int radiantSegmentIndex) {
        validateManagedRadiantSegment(radiantSegmentIndex);
        return radiantSegmentIndex - MANAGED_FIRST_RADIANT_SEGMENT;
    }

    /** The retained spark keeps its original radiant color band throughout the willow deformation. */
    public static RadiantTrajectory.ColorBand colorBand(int radiantSegmentIndex) {
        validateManagedRadiantSegment(radiantSegmentIndex);
        return RadiantTrajectory.colorBand(radiantSegmentIndex);
    }

    /** Returns the exact radiant-shell location at which this existing spark starts its willow deformation. */
    public static Vec3 radiantPosition(
            FireworkStyle.RadiantWillowProfile profile, Branch branch, int radiantSegmentIndex) {
        validateProfile(profile);
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);
        return RadiantTrajectory.position(
                profile.radiantProfile(), branch.radiantBranch(), radiantProgress(radiantSegmentIndex));
    }

    /** The branch endpoint is the final ring of the first-phase radiant shell. */
    public static Vec3 radiantEndpoint(FireworkStyle.RadiantWillowProfile profile, Branch branch) {
        return radiantPosition(profile, branch, RADIANT_SEGMENTS_PER_BRANCH - 1);
    }

    /**
     * Returns a controlled existing particle position. At extension progress zero this is exactly its radiant
     * position, including the outer endpoint, so no visually separate second burst is introduced.
     */
    public static Vec3 position(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            double extensionProgress) {
        validateProfile(profile);
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);

        Vec3 radiantPosition = radiantPosition(profile, branch, radiantSegmentIndex);
        Vec3 finalPosition = finalCurvePosition(
                profile, branch, managedProgress(radiantSegmentIndex));
        return radiantPosition.add(finalPosition.subtract(radiantPosition).scale(smoothStep(extensionProgress)));
    }

    /** Convenience overload for the outermost existing spark of a branch. */
    public static Vec3 position(
            FireworkStyle.RadiantWillowProfile profile, Branch branch, double extensionProgress) {
        return position(profile, branch, RADIANT_SEGMENTS_PER_BRANCH - 1, extensionProgress);
    }

    /** Returns the final long, curved willow location for one managed radiant spark. */
    public static Vec3 finalCurvePosition(
            FireworkStyle.RadiantWillowProfile profile, Branch branch, double branchProgress) {
        validateProfile(profile);
        validateBranch(branch);

        double progress = clamp(branchProgress, 0.0D, 1.0D);
        Vec3 root = radiantPosition(profile, branch, MANAGED_FIRST_RADIANT_SEGMENT);
        double startRadius = radiantRadius(profile, branch, MANAGED_FIRST_RADIANT_SEGMENT);
        double terminalRadius = branch.radiantBranch().radialReach()
                + profile.additionalRadialExtension() * branch.radialMultiplier();
        double radialDistance = Math.max(0.0D, terminalRadius - startRadius) * progress;
        Vec3 direction = branch.radiantBranch().direction();
        Vec3 radialOffset = new Vec3(
                direction.x * radialDistance,
                direction.y * profile.radiantProfile().verticalScale() * radialDistance,
                direction.z * radialDistance);

        double bendProgress = progress <= branch.bendStart()
                ? 0.0D
                : (progress - branch.bendStart()) / (1.0D - branch.bendStart());
        double drop = profile.terminalDrop() * branch.dropMultiplier()
                * Math.pow(smoothStep(bendProgress), 1.35D);
        double swayEnvelope = branch.swayAmplitude() * Math.sin(Math.PI * progress);
        double sway = swayEnvelope * (
                0.58D * Math.sin(branch.swayPhase() + TWO_PI * branch.swayFrequency() * progress)
                        + 0.42D * Math.sin(branch.secondarySwayPhase()
                                + TWO_PI * (branch.swayFrequency() + 0.63D) * progress));
        return root
                .add(radialOffset)
                .add(branch.radiantBranch().sideDirection().scale(sway))
                .add(0.0D, -drop, 0.0D);
    }

    /** Low non-zero tangent for client particle interpolation and seeded branch liveliness. */
    public static Vec3 tangent(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            double extensionProgress) {
        validateProfile(profile);
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);

        double progress = clamp(extensionProgress, 0.0D, 1.0D);
        double before = Math.max(0.0D, progress - TANGENT_DIFFERENCE);
        double after = Math.min(1.0D, progress + TANGENT_DIFFERENCE);
        Vec3 difference = position(profile, branch, radiantSegmentIndex, after)
                .subtract(position(profile, branch, radiantSegmentIndex, before));
        if (difference.lengthSqr() < MIN_NORMALIZABLE_LENGTH_SQR) {
            difference = branch.radiantBranch().direction();
        }
        return difference.normalize().scale(branch.tangentSpeed());
    }

    /** Convenience overload for the outermost existing spark of a branch. */
    public static Vec3 tangent(
            FireworkStyle.RadiantWillowProfile profile, Branch branch, double extensionProgress) {
        return tangent(profile, branch, RADIANT_SEGMENTS_PER_BRANCH - 1, extensionProgress);
    }

    /**
     * Returns the one-tick positional delta for a client that directly repositions retained particles.
     * This is intentionally distinct from {@link #tangent(FireworkStyle.RadiantWillowProfile, Branch, int, double)}.
     */
    public static Vec3 velocity(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            double extensionProgress,
            int extensionDurationTicks) {
        validateDuration(profile, extensionDurationTicks);
        double progress = clamp(extensionProgress, 0.0D, 1.0D);
        double nextProgress = Math.min(1.0D, progress + 1.0D / extensionDurationTicks);
        return position(profile, branch, radiantSegmentIndex, nextProgress)
                .subtract(position(profile, branch, radiantSegmentIndex, progress));
    }

    public static double extensionProgress(int extensionAgeTicks, int extensionDurationTicks) {
        if (extensionDurationTicks < MIN_EXTENSION_TICKS || extensionDurationTicks > MAX_EXTENSION_TICKS) {
            throw new IllegalArgumentException("Radiant willow duration is outside the allowed range");
        }
        return clamp((double) extensionAgeTicks / extensionDurationTicks, 0.0D, 1.0D);
    }

    public static ManagedParticleSample managedParticle(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            int extensionAgeTicks,
            int extensionDurationTicks) {
        validateProfile(profile);
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);
        validateDuration(profile, extensionDurationTicks);

        double progress = extensionProgress(extensionAgeTicks, extensionDurationTicks);
        int retirementTick = terminalRetirementTick(profile, branch, radiantSegmentIndex, extensionDurationTicks);
        RetirementFlicker terminalFlicker = terminalRetirementFlicker(
                profile, branch, radiantSegmentIndex, extensionDurationTicks);
        return new ManagedParticleSample(
                branch,
                radiantSegmentIndex,
                managedSegmentIndex(radiantSegmentIndex),
                extensionAgeTicks,
                extensionDurationTicks,
                progress,
                position(profile, branch, radiantSegmentIndex, progress),
                tangent(profile, branch, radiantSegmentIndex, progress),
                velocity(profile, branch, radiantSegmentIndex, progress, extensionDurationTicks),
                colorBand(radiantSegmentIndex),
                terminalFlicker,
                retirementTick,
                retirementTick != NEVER_RETIRES && extensionAgeTicks >= retirementTick);
    }

    /** The extension begins on the tick that the last radiant ring was actually emitted. */
    public static int extensionDurationTicks(FireworkStyle.RadiantWillowProfile profile, long payloadSeed) {
        validateProfile(profile);
        return randomInt(payloadSeed, DURATION_SALT, profile.minExtensionTicks(), profile.maxExtensionTicks());
    }

    /**
     * A small seeded phase offset avoids every branch thinning on the same frame while retaining the specified
     * 35-percent earliest start.
     */
    public static int terminalRetirementStartTick(
            FireworkStyle.RadiantWillowProfile profile, Branch branch, int extensionDurationTicks) {
        validateProfile(profile);
        validateBranch(branch);
        validateDuration(profile, extensionDurationTicks);
        int firstEligibleTick = (int) Math.ceil(extensionDurationTicks * TERMINAL_RETIREMENT_START_PROGRESS);
        return firstEligibleTick + randomInt(branch.seed(), RETIREMENT_OFFSET_SALT, 0, 3);
    }

    /** Returns the deterministic 12-17 tick interval after one terminal retirement. */
    public static int terminalRetirementIntervalTicks(Branch branch, int retirementOrdinal) {
        validateBranch(branch);
        if (retirementOrdinal < 0 || retirementOrdinal >= MAX_TERMINAL_RETIREMENTS_PER_BRANCH - 1) {
            throw new IllegalArgumentException("Radiant willow retirement ordinal is outside the configured range");
        }
        return randomInt(
                branch.seed(),
                RETIREMENT_INTERVAL_SALT + retirementOrdinal,
                TERMINAL_RETIREMENT_INTERVAL_MIN_TICKS,
                TERMINAL_RETIREMENT_INTERVAL_MAX_TICKS);
    }

    /**
     * Returns the extension-relative retirement tick for one of at most five outer-adjacent nodes. The true
     * outermost node is never reclaimed before the extension ends, keeping each branch's growing frontier intact.
     */
    public static int terminalRetirementTick(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            int extensionDurationTicks) {
        validateProfile(profile);
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);
        validateDuration(profile, extensionDurationTicks);

        int retirementOrdinal = (RADIANT_SEGMENTS_PER_BRANCH - 2) - radiantSegmentIndex;
        if (retirementOrdinal < 0 || retirementOrdinal >= MAX_TERMINAL_RETIREMENTS_PER_BRANCH) {
            return NEVER_RETIRES;
        }

        int retirementTick = terminalRetirementStartTick(profile, branch, extensionDurationTicks);
        for (int ordinal = 0; ordinal < retirementOrdinal; ordinal++) {
            retirementTick += terminalRetirementIntervalTicks(branch, ordinal);
        }
        return retirementTick <= extensionDurationTicks ? retirementTick : NEVER_RETIRES;
    }

    /** Returns the seeded flicker window immediately before an early terminal node is reclaimed. */
    public static RetirementFlicker terminalRetirementFlicker(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            int extensionDurationTicks) {
        validateProfile(profile);
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);
        validateDuration(profile, extensionDurationTicks);

        int retirementTick = terminalRetirementTick(profile, branch, radiantSegmentIndex, extensionDurationTicks);
        if (retirementTick == NEVER_RETIRES) {
            return NO_RETIREMENT_FLICKER;
        }
        long particleSeed = particleSeed(branch, radiantSegmentIndex);
        int duration = randomInt(
                particleSeed,
                TERMINAL_FLICKER_DURATION_SALT,
                RETIREMENT_FLICKER_MIN_TICKS,
                RETIREMENT_FLICKER_MAX_TICKS);
        int phase = randomInt(particleSeed, TERMINAL_FLICKER_PHASE_SALT, 0, 1);
        return new RetirementFlicker(Math.max(0, retirementTick - duration), retirementTick, phase);
    }

    /** Returns the seeded flicker window used when all remaining sparks leave at the end of the extension. */
    public static RetirementFlicker finalRetirementFlicker(Branch branch, int radiantSegmentIndex) {
        validateBranch(branch);
        validateManagedRadiantSegment(radiantSegmentIndex);

        long particleSeed = particleSeed(branch, radiantSegmentIndex);
        int start = randomInt(particleSeed, FINAL_FLICKER_DELAY_SALT, 0, FINAL_DRAIN_FLICKER_MAX_DELAY_TICKS);
        int duration = randomInt(
                particleSeed,
                FINAL_FLICKER_DURATION_SALT,
                RETIREMENT_FLICKER_MIN_TICKS,
                RETIREMENT_FLICKER_MAX_TICKS);
        int phase = randomInt(particleSeed, FINAL_FLICKER_PHASE_SALT, 0, 1);
        return new RetirementFlicker(start, start + duration, phase);
    }

    public static boolean isTerminalNodeRetired(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            int extensionAgeTicks,
            int extensionDurationTicks) {
        int retirementTick = terminalRetirementTick(profile, branch, radiantSegmentIndex, extensionDurationTicks);
        return retirementTick != NEVER_RETIRES && extensionAgeTicks >= retirementTick;
    }

    /** Number of retained sparks that may be reclaimed after the current extension age. */
    public static int terminalRetirementCount(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int extensionAgeTicks,
            int extensionDurationTicks) {
        validateProfile(profile);
        validateBranch(branch);
        validateDuration(profile, extensionDurationTicks);

        int retired = 0;
        for (int radiantSegment = RADIANT_SEGMENTS_PER_BRANCH - 2;
                radiantSegment >= RADIANT_SEGMENTS_PER_BRANCH - 1 - MAX_TERMINAL_RETIREMENTS_PER_BRANCH;
                radiantSegment--) {
            if (isTerminalNodeRetired(profile, branch, radiantSegment, extensionAgeTicks, extensionDurationTicks)) {
                retired++;
            }
        }
        return retired;
    }

    /** Extension advances all 27 retained branch positions while no more than five are ever reclaimed. */
    public static double extensionToTerminalRetirementSpeedRatio(int extensionDurationTicks) {
        if (extensionDurationTicks < MIN_EXTENSION_TICKS || extensionDurationTicks > MAX_EXTENSION_TICKS) {
            throw new IllegalArgumentException("Radiant willow duration is outside the allowed range");
        }
        int activeExtensionTicks = extensionDurationTicks
                - (int) Math.ceil(extensionDurationTicks * TERMINAL_RETIREMENT_START_PROGRESS);
        return ((double) MANAGED_SEGMENTS_PER_BRANCH / extensionDurationTicks)
                / ((double) MAX_TERMINAL_RETIREMENTS_PER_BRANCH / activeExtensionTicks);
    }

    public static boolean extensionOutrunsTerminalRetirement(int extensionDurationTicks) {
        return extensionToTerminalRetirementSpeedRatio(extensionDurationTicks) >= 3.0D;
    }

    /** Remaining lifetime from the instant the extension begins; client code adds the particle's current age. */
    public static int remainingLifetimeTicks(
            FireworkStyle.RadiantWillowProfile profile,
            Branch branch,
            int radiantSegmentIndex,
            int extensionDurationTicks) {
        int retirementTick = terminalRetirementTick(profile, branch, radiantSegmentIndex, extensionDurationTicks);
        return retirementTick == NEVER_RETIRES ? extensionDurationTicks + 1 : retirementTick + 1;
    }

    /** Bounds all actual initial radiant nodes and all retained-particle final curve nodes. */
    public static Bounds conservativeBounds(
            FireworkStyle.RadiantWillowProfile profile, long payloadSeed, int extensionDurationTicks) {
        validateProfile(profile);
        validateDuration(profile, extensionDurationTicks);
        BoundsAccumulator accumulator = new BoundsAccumulator();
        for (int branchIndex = 0; branchIndex < BRANCH_COUNT; branchIndex++) {
            Branch branch = branch(profile, payloadSeed, branchIndex);
            for (int radiantSegment = 0; radiantSegment < RADIANT_SEGMENTS_PER_BRANCH; radiantSegment++) {
                accumulator.include(RadiantTrajectory.position(
                        profile.radiantProfile(),
                        branch.radiantBranch(),
                        radiantProgress(radiantSegment)));
            }
            for (int radiantSegment = MANAGED_FIRST_RADIANT_SEGMENT;
                    radiantSegment < RADIANT_SEGMENTS_PER_BRANCH;
                    radiantSegment++) {
                accumulator.include(finalCurvePosition(
                        profile, branch, managedProgress(radiantSegment)));
            }
        }
        return accumulator.build(CONSERVATIVE_MOTION_MARGIN);
    }

    public static boolean fitsEnvelope(
            FireworkStyle.RadiantWillowProfile profile, long payloadSeed, int extensionDurationTicks) {
        return conservativeBounds(profile, payloadSeed, extensionDurationTicks)
                .fitsWithin(APPROVED_FULL_ENVELOPE);
    }

    private static void validateProfile(FireworkStyle.RadiantWillowProfile profile) {
        if (profile == null
                || profile.branchCount() != BRANCH_COUNT
                || profile.radiantSegmentsPerBranch() != RADIANT_SEGMENTS_PER_BRANCH
                || profile.managedFirstRadiantSegment() != MANAGED_FIRST_RADIANT_SEGMENT
                || profile.managedSegmentsPerBranch() != MANAGED_SEGMENTS_PER_BRANCH
                || profile.minExtensionTicks() != MIN_EXTENSION_TICKS
                || profile.maxExtensionTicks() != MAX_EXTENSION_TICKS
                || profile.additionalRadialExtension() != 18.0D
                || profile.bendStartMin() != 0.28D
                || profile.bendStartMax() != 0.42D
                || profile.terminalDrop() != 66.0D
                || profile.maximumLateralSway() != 7.5D) {
            throw new IllegalArgumentException("RadiantWillowTrajectory requires the fixed v0.2.9 profile");
        }
    }

    private static void validateBranch(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Radiant willow branch may not be null");
        }
    }

    private static void validateBranchIndex(int branchIndex) {
        if (branchIndex < 0 || branchIndex >= BRANCH_COUNT) {
            throw new IllegalArgumentException("Radiant willow branch index is outside the configured count");
        }
    }

    private static void validateManagedRadiantSegment(int radiantSegmentIndex) {
        if (!isManagedRadiantSegment(radiantSegmentIndex)) {
            throw new IllegalArgumentException("Radiant willow controls only non-core radiant segments");
        }
    }

    private static void validateDuration(FireworkStyle.RadiantWillowProfile profile, int extensionDurationTicks) {
        if (extensionDurationTicks < profile.minExtensionTicks()
                || extensionDurationTicks > profile.maxExtensionTicks()) {
            throw new IllegalArgumentException("Radiant willow duration is outside the configured range");
        }
    }

    private static double radiantProgress(int radiantSegmentIndex) {
        if (radiantSegmentIndex < 0 || radiantSegmentIndex >= RADIANT_SEGMENTS_PER_BRANCH) {
            throw new IllegalArgumentException("Radiant segment index is outside the configured count");
        }
        return (double) radiantSegmentIndex / (RADIANT_SEGMENTS_PER_BRANCH - 1);
    }

    private static double managedProgress(int radiantSegmentIndex) {
        return (double) managedSegmentIndex(radiantSegmentIndex) / (MANAGED_SEGMENTS_PER_BRANCH - 1);
    }

    private static double radiantRadius(
            FireworkStyle.RadiantWillowProfile profile, Branch branch, int radiantSegmentIndex) {
        double progress = radiantProgress(radiantSegmentIndex);
        return profile.radiantProfile().initialRadius()
                + (branch.radiantBranch().radialReach() - profile.radiantProfile().initialRadius()) * progress;
    }

    private static final class BoundsAccumulator {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        private void include(Vec3 value) {
            this.minX = Math.min(this.minX, value.x);
            this.minY = Math.min(this.minY, value.y);
            this.minZ = Math.min(this.minZ, value.z);
            this.maxX = Math.max(this.maxX, value.x);
            this.maxY = Math.max(this.maxY, value.y);
            this.maxZ = Math.max(this.maxZ, value.z);
        }

        private Bounds build(double margin) {
            return new Bounds(
                    this.minX - margin,
                    this.minY - margin,
                    this.minZ - margin,
                    this.maxX + margin,
                    this.maxY + margin,
                    this.maxZ + margin);
        }
    }

    private static double smoothStep(double value) {
        double bounded = clamp(value, 0.0D, 1.0D);
        return bounded * bounded * (3.0D - 2.0D * bounded);
    }

    private static double between(long seed, long salt, double min, double max) {
        return min + randomUnit(seed ^ salt) * (max - min);
    }

    private static int randomInt(long seed, long salt, int min, int max) {
        return min + (int) Math.floor(randomUnit(seed ^ salt) * (max - min + 1));
    }

    private static long particleSeed(Branch branch, int radiantSegmentIndex) {
        return mix64(branch.seed()
                ^ ((long) (radiantSegmentIndex + 1) * 0xD6E8FEB86659FD93L)
                ^ FLICKER_PARTICLE_SALT);
    }

    private static double randomUnit(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53D;
    }

    private static long mix64(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ mixed >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
