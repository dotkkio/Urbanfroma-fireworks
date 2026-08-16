package com.urbanforma.fireworks.client.release_next.medium_extension;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
/** Client program marker for bounded medium radial entries. */
public final class MediumRadialClientProgram extends MediumExtensionClientProgram { public MediumRadialClientProgram(MediumExtensionDefinition d) { super(require(d)); } private static MediumExtensionDefinition require(MediumExtensionDefinition d) { if (d.category() != MediumExtensionDefinition.Category.RADIAL) throw new IllegalArgumentException("Expected RADIAL"); return d; } }
