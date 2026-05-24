package dev.kael.core.registry

import dev.kael.core.skill.LoadedSkill

class SkillRegistry {
    private val byId = linkedMapOf<String, LoadedSkill>()
    private val bySlug = mutableMapOf<String, MutableList<LoadedSkill>>()

    fun register(skill: LoadedSkill) {
        check(byId.put(skill.id, skill) == null) {
            "skill ${skill.id} already registered"
        }
        bySlug.getOrPut(skill.slug) { mutableListOf() }.add(skill)
    }

    fun unregisterExtension(extensionId: String) {
        val removed = byId.values.filter { it.extensionId == extensionId }
        removed.forEach { skill ->
            byId.remove(skill.id)
            bySlug[skill.slug]?.removeIf { it.extensionId == extensionId }
            if (bySlug[skill.slug]?.isEmpty() == true) bySlug.remove(skill.slug)
        }
    }

    fun list(): List<LoadedSkill> = byId.values.toList()

    fun byId(id: String): LoadedSkill? = byId[id]

    fun resolve(reference: String): LoadedSkill? {
        if ('/' in reference) return byId[reference]
        val matches = bySlug[reference].orEmpty()
        return when (matches.size) {
            0 -> null
            1 -> matches.single()
            else -> throw IllegalArgumentException(
                "skill reference '$reference' is ambiguous — matches: ${matches.joinToString { it.id }}"
            )
        }
    }
}
