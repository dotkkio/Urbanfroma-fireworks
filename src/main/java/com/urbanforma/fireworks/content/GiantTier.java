package com.urbanforma.fireworks.content;

/** Independent giant-size queues. LARGE and EXTRA_LARGE intentionally never share an active-slot allowance. */
public enum GiantTier {
    NONE,
    LARGE,
    EXTRA_LARGE,
    SUPER_WILLOW,
    MULTI_RADIAL_II,
    THICK_RADIAL,
    CASCADE;

    public EffectCategory effectCategory() {
        return switch (this) {
            case NONE -> EffectCategory.STANDARD;
            case LARGE -> EffectCategory.GIANT_LARGE;
            case EXTRA_LARGE -> EffectCategory.GIANT_EXTRA_LARGE;
            case SUPER_WILLOW -> EffectCategory.GIANT_SUPER_WILLOW;
            case MULTI_RADIAL_II -> EffectCategory.GIANT_MULTI_RADIAL_II;
            case THICK_RADIAL -> EffectCategory.GIANT_THICK_RADIAL;
            case CASCADE -> EffectCategory.GIANT_CASCADE;
        };
    }
}
