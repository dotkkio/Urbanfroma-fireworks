package com.urbanforma.fireworks.client.release_next.medium_extension;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
/** Client program marker for bounded medium ring-core entries. */
public final class MediumRingCoreClientProgram extends MediumExtensionClientProgram { public MediumRingCoreClientProgram(MediumExtensionDefinition d) { super(require(d)); } private static MediumExtensionDefinition require(MediumExtensionDefinition d) { if (d.category() != MediumExtensionDefinition.Category.RING_CORE) throw new IllegalArgumentException("Expected RING_CORE"); return d; } }
