package dev.kael.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import dev.kael.core.registry.SkillRegistry

class SkillCommand(registry: SkillRegistry) : CliktCommand(name = "skill") {
    override fun help(context: Context) = "Inspect skills exposed by loaded extensions"
    override fun run() {}

    init {
        subcommands(
            ListSkills(registry),
            Info(registry),
        )
    }

    private class ListSkills(private val registry: SkillRegistry) : CliktCommand(name = "list") {
        override fun help(context: Context) = "List all skills"

        override fun run() {
            val all = registry.list()
            if (all.isEmpty()) {
                echo("no skills available")
                return
            }
            all.forEach { echo("${it.id}  —  ${it.displayName}") }
        }
    }

    private class Info(private val registry: SkillRegistry) : CliktCommand(name = "info") {
        override fun help(context: Context) = "Show details for a skill (full id or slug)"
        private val ref by argument("ref")

        override fun run() {
            val skill = try {
                registry.resolve(ref)
            } catch (e: IllegalArgumentException) {
                throw CliktError(e.message ?: "skill ref error", statusCode = 1)
            } ?: throw CliktError("skill '$ref' not found", statusCode = 1)
            echo("id:           ${skill.id}")
            echo("display name: ${skill.displayName}")
            echo("extension:    ${skill.extensionId}")
            echo("slug:         ${skill.slug}")
            if (skill.source.frontmatter.isNotEmpty()) {
                echo("frontmatter:")
                skill.source.frontmatter.forEach { (k, v) -> echo("  $k: $v") }
            }
            echo("---")
            echo(skill.source.body.trimEnd())
        }
    }
}
