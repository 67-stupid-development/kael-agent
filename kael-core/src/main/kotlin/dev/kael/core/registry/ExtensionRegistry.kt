package dev.kael.core.registry

import dev.kael.core.loader.SkillOnlyLoader
import dev.kael.core.manifest.ManifestParser
import dev.kael.sdk.manifest.Loader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.notExists

class ExtensionRegistry(
    private val skills: SkillRegistry,
    private val commands: CommandRegistry,
    private val statePath: Path,
) {
    private val loaded = linkedMapOf<String, LoadedExtension>()
    private val skillOnlyLoader = SkillOnlyLoader()

    fun bootstrap() {
        if (statePath.notExists()) return
        val paths = Files.readAllLines(statePath)
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }

        paths.forEach { raw ->
            val path = Path.of(raw)
            if (!path.exists()) {
                System.err.println("warning: extension at $raw no longer exists, skipping")
                return@forEach
            }
            runCatching { loadFromPath(path, persist = false) }
                .onFailure { System.err.println("warning: failed to load $raw — ${it.message}") }
        }
    }

    fun load(path: Path): LoadedExtension = loadFromPath(path, persist = true)

    private fun loadFromPath(path: Path, persist: Boolean): LoadedExtension {
        val root = path.absolute().normalize()
        val manifestPath = root.resolve("manifest.yml")
        val manifest = ManifestParser.parse(manifestPath)

        if (manifest.loader != Loader.SkillOnly) {
            throw IllegalStateException(
                "loader '${manifest.loader}' is not supported in v0.1 — only 'skill-only' for now"
            )
        }

        loaded[manifest.metadata.id]?.let {
            throw IllegalStateException("extension '${manifest.metadata.id}' is already loaded from ${it.root}")
        }

        val discovered = skillOnlyLoader.discover(root, manifest)
        val ext = LoadedExtension(manifest, root, discovered)
        loaded[manifest.metadata.id] = ext
        discovered.forEach(skills::register)

        if (persist) writeState()
        return ext
    }

    fun unload(id: String) {
        loaded.remove(id) ?: throw IllegalStateException("extension '$id' is not loaded")
        skills.unregisterExtension(id)
        writeState()
    }

    fun list(): List<LoadedExtension> = loaded.values.toList()

    fun byId(id: String): LoadedExtension? = loaded[id]

    @Suppress("unused")
    val commandRegistry: CommandRegistry = commands

    private fun writeState() {
        Files.createDirectories(statePath.parent)
        val lines = loaded.values.map { it.root.toString() }
        Files.writeString(statePath, lines.joinToString("\n", postfix = "\n"))
    }
}
