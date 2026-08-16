package com.urbanforma.fireworks.client.release_next.medium_extension;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
/** Client program marker for bounded medium sphere entries. */
public final class MediumSphereClientProgram extends MediumExtensionClientProgram { public MediumSphereClientProgram(MediumExtensionDefinition d) { super(require(d, MediumExtensionDefinition.Category.SPHERE)); } private static MediumExtensionDefinition require(MediumExtensionDefinition d, MediumExtensionDefinition.Category c) { if (d.category() != c) throw new IllegalArgumentException("Expected " + c); return d; } }
