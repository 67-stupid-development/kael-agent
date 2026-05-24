package dev.kael.core.skill

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar

internal data class Frontmatter(
    val data: Map<String, Any?>,
    val body: String,
)

internal object FrontmatterParser {

    private val OPENING = Regex("^---\\s*\\r?\\n")
    private val CLOSING = Regex("\\r?\\n---\\s*\\r?\\n")

    fun parse(text: String): Frontmatter {
        val opening = OPENING.find(text) ?: return Frontmatter(emptyMap(), text)
        val afterOpen = text.substring(opening.range.last + 1)
        val closing = CLOSING.find(afterOpen) ?: return Frontmatter(emptyMap(), text)

        val yamlText = afterOpen.substring(0, closing.range.first)
        val body = afterOpen.substring(closing.range.last + 1)

        if (yamlText.isBlank()) return Frontmatter(emptyMap(), body)

        val node = Yaml.default.parseToYamlNode(yamlText)
        val data = (node as? YamlMap)?.let(::convertMap) ?: emptyMap()
        return Frontmatter(data, body)
    }

    private fun convertMap(map: YamlMap): Map<String, Any?> =
        map.entries.entries.associate { (key, value) ->
            key.content to convert(value)
        }

    private fun convert(node: YamlNode): Any? = when (node) {
        is YamlScalar -> coerceScalar(node.content)
        is YamlList -> node.items.map(::convert)
        is YamlMap -> convertMap(node)
        is YamlNull -> null
        else -> node.contentToString()
    }

    private fun coerceScalar(raw: String): Any? {
        if (raw.equals("null", ignoreCase = true) || raw == "~") return null
        if (raw.equals("true", ignoreCase = true)) return true
        if (raw.equals("false", ignoreCase = true)) return false
        raw.toLongOrNull()?.let { return it }
        raw.toDoubleOrNull()?.let { return it }
        return raw
    }
}
