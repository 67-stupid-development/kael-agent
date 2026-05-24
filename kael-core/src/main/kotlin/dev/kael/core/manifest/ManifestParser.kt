package dev.kael.core.manifest

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import dev.kael.sdk.manifest.Manifest
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.toConstraint
import kotlinx.serialization.SerializationException
import java.nio.file.Files
import java.nio.file.Path

class ManifestParseException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

object ManifestParser {

    private val yaml = Yaml.default

    fun parse(path: Path): Manifest {
        if (!Files.isRegularFile(path)) {
            throw ManifestParseException("manifest file not found: $path")
        }
        val text = Files.readString(path)
        return parseString(text, path.toString())
    }

    fun parseString(text: String, source: String = "<string>"): Manifest {
        val manifest = try {
            yaml.decodeFromString(Manifest.serializer(), text)
        } catch (e: YamlException) {
            throw ManifestParseException("invalid YAML in $source: ${e.message}", e)
        } catch (e: SerializationException) {
            throw ManifestParseException("invalid manifest in $source: ${e.message}", e)
        }
        validate(manifest, source)
        return manifest
    }

    private fun validate(manifest: Manifest, source: String) {
        val errors = mutableListOf<String>()

        if (manifest.apiVersion.isBlank()) {
            errors += "apiVersion must be set"
        }
        if (!ID_REGEX.matches(manifest.metadata.id)) {
            errors += "metadata.id '${manifest.metadata.id}' must be reverse-DNS (e.g. com.example.foo)"
        }
        if (manifest.metadata.name.isBlank()) {
            errors += "metadata.name must be set"
        }
        runCatching { Version.parse(manifest.metadata.version) }.onFailure {
            errors += "metadata.version '${manifest.metadata.version}' is not valid SemVer"
        }
        runCatching { manifest.compatibility.core.toConstraint() }.onFailure {
            errors += "compatibility.core '${manifest.compatibility.core}' is not a valid SemVer range"
        }
        if (manifest.compatibility.java < 21) {
            errors += "compatibility.java must be >= 21 (got ${manifest.compatibility.java})"
        }
        manifest.dependencies.extensions.forEach { (depId, range) ->
            runCatching { range.toConstraint() }.onFailure {
                errors += "dependencies.extensions['$depId'] = '$range' is not a valid SemVer range"
            }
        }

        if (errors.isNotEmpty()) {
            throw ManifestParseException(
                "manifest validation failed in $source:\n" + errors.joinToString("\n") { "  - $it" }
            )
        }
    }

    private val ID_REGEX = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$")
}
