package dev.kael.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import dev.kael.core.manifest.ManifestParseException
import dev.kael.core.registry.ExtensionRegistry
import java.nio.file.Path

class ExtCommand(registry: ExtensionRegistry) : CliktCommand(name = "ext") {
    override fun help(context: Context) = "Manage extensions"
    override fun run() {}

    init {
        subcommands(
            Load(registry),
            Unload(registry),
            ListExts(registry),
            Info(registry),
        )
    }

    private class Load(private val registry: ExtensionRegistry) : CliktCommand(name = "load") {
        override fun help(context: Context) = "Load an extension from a directory"
        private val path by argument("path").convert { Path.of(it) }

        override fun run() {
            val ext = try {
                registry.load(path)
            } catch (e: ManifestParseException) {
                throw CliktError(e.message ?: "manifest parse failed", statusCode = 1)
            } catch (e: IllegalStateException) {
                throw CliktError(e.message ?: "load failed", statusCode = 1)
            }
            echo("loaded ${ext.manifest.metadata.id} (${ext.skills.size} skill(s))")
        }
    }

    private class Unload(private val registry: ExtensionRegistry) : CliktCommand(name = "unload") {
        override fun help(context: Context) = "Unload an extension by id"
        private val id by argument("id")

        override fun run() {
            try {
                registry.unload(id)
            } catch (e: IllegalStateException) {
                throw CliktError(e.message ?: "unload failed", statusCode = 1)
            }
            echo("unloaded $id")
        }
    }

    private class ListExts(private val registry: ExtensionRegistry) : CliktCommand(name = "list") {
        override fun help(context: Context) = "List loaded extensions"

        override fun run() {
            val all = registry.list()
            if (all.isEmpty()) {
                echo("no extensions loaded")
                return
            }
            all.forEach { ext ->
                echo("${ext.manifest.metadata.id}  ${ext.manifest.metadata.version}  ${ext.manifest.loader}  (${ext.skills.size} skill(s))")
            }
        }
    }

    private class Info(private val registry: ExtensionRegistry) : CliktCommand(name = "info") {
        override fun help(context: Context) = "Show details about a loaded extension"
        private val id by argument("id")

        override fun run() {
            val ext = registry.byId(id)
                ?: throw CliktError("extension '$id' is not loaded", statusCode = 1)
            val m = ext.manifest
            echo("id:          ${m.metadata.id}")
            echo("name:        ${m.metadata.name}")
            echo("version:     ${m.metadata.version}")
            echo("description: ${m.metadata.description}")
            echo("license:     ${m.metadata.license}")
            echo("loader:      ${m.loader}")
            echo("kind:        ${m.kind}")
            echo("root:        ${ext.root}")
            echo("skills:")
            if (ext.skills.isEmpty()) echo("  (none)") else ext.skills.forEach { echo("  - ${it.id} — ${it.displayName}") }
        }
    }
}

