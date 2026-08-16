package com.urbanforma.fireworks.client.release_next.medium_extension;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
/** Client program marker for bounded medium pulse entries. */
public final class MediumPulseClientProgram extends MediumExtensionClientProgram { public MediumPulseClientProgram(MediumExtensionDefinition d) { super(require(d)); } private static MediumExtensionDefinition require(MediumExtensionDefinition d) { if (d.category() != MediumExtensionDefinition.Category.PULSE) throw new IllegalArgumentException("Expected PULSE"); return d; } }
