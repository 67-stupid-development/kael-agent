package dev.kael.core.skill

import dev.kael.sdk.skill.Skill
import dev.kael.sdk.skill.SkillSource

data class LoadedSkill(
    override val id: String,
    override val displayName: String,
    override val source: SkillSource,
    val extensionId: String,
    val slug: String,
) : Skill
