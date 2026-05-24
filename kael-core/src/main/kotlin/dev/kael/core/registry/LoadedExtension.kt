package dev.kael.core.registry

import dev.kael.core.skill.LoadedSkill
import dev.kael.sdk.manifest.Manifest
import java.nio.file.Path

data class LoadedExtension(
    val manifest: Manifest,
    val root: Path,
    val skills: List<LoadedSkill>,
)
