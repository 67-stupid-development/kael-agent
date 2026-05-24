package dev.kael.sdk.skill

interface Skill {
    val id: String
    val displayName: String
    val source: SkillSource
}

data class SkillSource(
    val frontmatter: Map<String, Any?>,
    val body: String,
)
