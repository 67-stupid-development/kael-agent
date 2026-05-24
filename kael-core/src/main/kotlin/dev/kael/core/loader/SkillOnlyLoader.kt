package dev.kael.core.loader

import dev.kael.core.skill.FrontmatterParser
import dev.kael.core.skill.LoadedSkill
import dev.kael.sdk.manifest.Manifest
import dev.kael.sdk.skill.SkillSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

class SkillOnlyLoader {

    fun discover(extensionRoot: Path, manifest: Manifest): List<LoadedSkill> {
        val skillsDir = extensionRoot.resolve("SKILLS")
        if (!Files.isDirectory(skillsDir)) return emptyList()

        return Files.list(skillsDir).use { stream ->
            stream.filter { Files.isRegularFile(it) && it.extension.equals("md", ignoreCase = true) }
                .map { loadOne(it, manifest.metadata.id) }
                .toList()
        }
    }

    private fun loadOne(file: Path, extensionId: String): LoadedSkill {
        val text = Files.readString(file)
        val parsed = FrontmatterParser.parse(text)

        val rawName = file.nameWithoutExtension
        val slug = normalizeSlug(rawName)
        val displayName = (parsed.data["name"] as? String) ?: humanize(rawName)
        val id = "$extensionId/$slug"

        return LoadedSkill(
            id = id,
            displayName = displayName,
            source = SkillSource(parsed.data, parsed.body),
            extensionId = extensionId,
            slug = slug,
        )
    }

    private fun normalizeSlug(name: String): String =
        name.lowercase()
            .replace('_', '-')
            .replace(Regex("[^a-z0-9-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')

    private fun humanize(name: String): String =
        name.replace('_', ' ')
            .replace('-', ' ')
            .split(' ')
            .filter { it.isNotEmpty() }
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
}
