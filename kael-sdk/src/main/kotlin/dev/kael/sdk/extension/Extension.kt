package dev.kael.sdk.extension

import dev.kael.sdk.command.Command
import dev.kael.sdk.manifest.Manifest
import dev.kael.sdk.skill.Skill
import org.slf4j.Logger

interface Extension {
    val manifest: Manifest
    suspend fun onLoad(ctx: ExtensionContext) {}
    suspend fun onUnload() {}
}

interface ExtensionContext {
    val logger: Logger
    fun registerCommand(command: Command<*, *>)
    fun registerSkill(skill: Skill)
}
