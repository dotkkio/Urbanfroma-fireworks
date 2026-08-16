package com.urbanforma.fireworks.client.release_next.medium_extension;
import com.urbanforma.fireworks.content.release_next.medium_extension.MediumExtensionDefinition;
/** Client program marker for bounded medium short-willow entries. */
public final class MediumShortWillowClientProgram extends MediumExtensionClientProgram { public MediumShortWillowClientProgram(MediumExtensionDefinition d) { super(require(d)); } private static MediumExtensionDefinition require(MediumExtensionDefinition d) { if (d.category() != MediumExtensionDefinition.Category.SHORT_WILLOW) throw new IllegalArgumentException("Expected SHORT_WILLOW"); return d; } }
