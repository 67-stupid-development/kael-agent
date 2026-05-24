package dev.kael.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import dev.kael.cli.command.ExtCommand
import dev.kael.cli.command.SkillCommand
import dev.kael.core.registry.CommandRegistry
import dev.kael.core.registry.ExtensionRegistry
import dev.kael.core.registry.SkillRegistry
import java.nio.file.Path

class KaelCli : CliktCommand(name = "kael") {
    override fun help(context: com.github.ajalt.clikt.core.Context) =
        "Kael — Kotlin Agent Extension Loader"

    override fun run() {}

    companion object {
        fun build(stateDir: Path): KaelCli {
            val skills = SkillRegistry()
            val commands = CommandRegistry()
            val extensions = ExtensionRegistry(skills, commands, stateDir.resolve("extensions.txt"))
            extensions.bootstrap()

            return KaelCli().subcommands(
                ExtCommand(extensions),
                SkillCommand(skills),
            )
        }
    }
}
