package com.urbanforma.fireworks.content.release_next.crown;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/** Pure deterministic trajectory samples for a client-owned crown descent. */
public final class CrownReplacementProgram {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double MAX_RADIUS = 68.0D;
    private static final double MAX_DROP = 176.0D;
    private static final long BRANCH_SALT = 0x9E3779B97F4A7C15L;

    private final long seed;
    private final Palette palette;

    public CrownReplacementProgram(long seed, Palette palette) {
        this.seed = seed;
        this.palette = Objects.requireNonNull(palette, "palette");
    }

    public Branch branch(int index) {
        if (index < 0 || index >= CrownReplacementManifest.BRANCH_COUNT) {
            throw new IllegalArgumentException("Crown branch index is outside the configured range");
        }
        long branchSeed = mix(seed ^ BRANCH_SALT ^ ((long) index * 0xD1342543DE82EF95L));
        double angle = TWO_PI * (index / (double) CrownReplacementManifest.BRANCH_COUNT)
                + centered(branchSeed) * 0.085D;
        double outer = 0.42D + 0.58D * ((index % 8) / 7.0D);
        return new Branch(index, branchSeed, angle, 18.0D + outer * 22.0D,
                9.0D + outer * 14.0D, 46.0D + unit(branchSeed ^ 0xA24BAED4963EE407L) * 22.0D,
                0.75D + unit(branchSeed ^ 0x3C79AC492BA7B653L) * 1.35D);
    }

    public Sample sample(double originX, double originY, double originZ, Branch branch, Terminal terminal,
            int age, int particleIndex) {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(terminal, "terminal");
        if (age < 0 || age >= CrownReplacementManifest.VISUAL_LIFETIME_TICKS
                || particleIndex < 0 || particleIndex >= CrownReplacementManifest.PARTICLES_PER_BRANCH) {
            throw new IllegalArgumentException("Crown sample is outside its finite contract");
        }
        double delay = particleIndex * 0.027D;
        double progress = clamp((age / 299.0D - delay) / (1.0D - delay), 0.0D, 1.0D);
        double open = 1.0D - Math.pow(1.0D - progress, 1.6D);
        double fall = progress * progress * (3.0D - 2.0D * progress);
        double radius = branch.initialRadius() + (branch.terminalRadius() - branch.initialRadius()) * open;
        double sway = branch.sway() * Math.sin(TWO_PI * (progress * 1.25D + unit(branch.seed())))
                * Math.sin(Math.PI * progress);
        double cos = Math.cos(branch.angle());
        double sin = Math.sin(branch.angle());
        double x = originX + cos * radius - sin * sway;
        double z = originZ + sin * radius + cos * sway;
        double startY = originY + branch.initialHeight();
        double y = startY + (terminal.y() - startY) * fall + 4.0D * Math.sin(Math.PI * progress);

        // Tangent is derived from a one-tick forward sample, so existing sparks carry a visible descent velocity.
        int nextAge = Math.min(299, age + 1);
        Vec3 next = positionAt(originX, originY, originZ, branch, terminal, nextAge, particleIndex);
        Vec3 velocity = next.subtract(x, y, z).scale(0.72D);
        float alpha = (float) clamp(1.0D - Math.pow(progress, 2.8D), 0.0D, 1.0D);
        float scale = (float) (1.12D - progress * 0.42D + particleIndex * 0.018D);
        return new Sample(new Vec3(x, y, z), velocity, palette.colorFor(branch.index()), alpha, scale);
    }

    private Vec3 positionAt(double x, double y, double z, Branch branch, Terminal terminal, int age, int particle) {
        double delay = particle * 0.027D;
        double progress = clamp((age / 299.0D - delay) / (1.0D - delay), 0.0D, 1.0D);
        double open = 1.0D - Math.pow(1.0D - progress, 1.6D);
        double fall = progress * progress * (3.0D - 2.0D * progress);
        double radius = branch.initialRadius() + (branch.terminalRadius() - branch.initialRadius()) * open;
        double sway = branch.sway() * Math.sin(TWO_PI * (progress * 1.25D + unit(branch.seed())))
                * Math.sin(Math.PI * progress);
        double cos = Math.cos(branch.angle());
        double sin = Math.sin(branch.angle());
        return new Vec3(x + cos * radius - sin * sway,
                y + branch.initialHeight() + (terminal.y() - (y + branch.initialHeight())) * fall
                        + 4.0D * Math.sin(Math.PI * progress),
                z + sin * radius + cos * sway);
    }

    public record Color(float red, float green, float blue) {
        public Color {
            if (!Float.isFinite(red) || !Float.isFinite(green) || !Float.isFinite(blue)) {
                throw new IllegalArgumentException("Crown color must be finite");
            }
        }
    }

    public record Palette(Color primary, Color secondary, Color accent) {
        public Palette {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
            Objects.requireNonNull(accent, "accent");
        }

        private Color colorFor(int branchIndex) {
            return switch (branchIndex % 3) {
                case 0 -> primary;
                case 1 -> secondary;
                default -> accent;
            };
        }
    }

    public record Branch(int index, long seed, double angle, double initialRadius, double initialHeight,
                         double terminalRadius, double sway) {
    }

    public record Terminal(double x, double y, double z) {
        public Terminal {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("Crown terminal must be finite");
            }
        }
    }

    public record Sample(Vec3 position, Vec3 velocity, Color color, float alpha, float scale) {
    }

    public static boolean staticContractHolds() {
        CrownReplacementProgram program = new CrownReplacementProgram(17L,
                new Palette(new Color(1.0F, 0.2F, 0.1F), new Color(0.3F, 0.9F, 0.8F), new Color(1.0F, 1.0F, 0.8F)));
        for (int branch = 0; branch < CrownReplacementManifest.BRANCH_COUNT; branch++) {
            Branch definition = program.branch(branch);
            Terminal terminal = new Terminal(Math.cos(definition.angle()) * definition.terminalRadius(),
                    -120.0D, Math.sin(definition.angle()) * definition.terminalRadius());
            for (int particle = 0; particle < CrownReplacementManifest.PARTICLES_PER_BRANCH; particle++) {
                Sample first = program.sample(0.0D, 0.0D, 0.0D, definition, terminal, 0, particle);
                Sample last = program.sample(0.0D, 0.0D, 0.0D, definition, terminal, 299, particle);
                if (!Double.isFinite(first.position().x) || !Double.isFinite(last.position().y)
                        || last.position().y < -MAX_DROP - 8.0D || last.alpha() > 0.01F) {
                    return false;
                }
            }
        }
        return CrownReplacementManifest.PARTICLES_PER_PROGRAM == 384
                && CrownReplacementManifest.MAX_OWNED_PARTICLES == 384;
    }

    private static double unit(long value) {
        return (mix(value) >>> 11) * 0x1.0p-53D;
    }

    private static double centered(long value) {
        return unit(value) - 0.5D;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
