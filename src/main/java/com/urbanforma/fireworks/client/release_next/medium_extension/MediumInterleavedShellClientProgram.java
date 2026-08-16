package com.urbanforma.fireworks.client.release_next.medium_extension;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
/** Client program marker for bounded medium interleaved-shell entries. */
public final class MediumInterleavedShellClientProgram extends MediumExtensionClientProgram { public MediumInterleavedShellClientProgram(MediumExtensionDefinition d) { super(require(d)); } private static MediumExtensionDefinition require(MediumExtensionDefinition d) { if (d.category() != MediumExtensionDefinition.Category.INTERLEAVED_SHELL) throw new IllegalArgumentException("Expected INTERLEAVED_SHELL"); return d; } }
