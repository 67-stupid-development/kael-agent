package dev.kael.sdk.manifest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val apiVersion: String,
    val kind: Kind,
    val metadata: Metadata,
    val compatibility: Compatibility = Compatibility(),
    val loader: Loader,
    val dependencies: Dependencies = Dependencies(),
    val permissions: Permissions = Permissions(),
    val lifecycle: Lifecycle = Lifecycle(),
)

@Serializable
enum class Kind {
    @SerialName("Extension") Extension,
    @SerialName("SkillPack") SkillPack,
}

@Serializable
data class Metadata(
    val id: String,
    val name: String,
    val version: String,
    val description: String = "",
    val license: String = "",
    val tags: List<String> = emptyList(),
)

@Serializable
data class Compatibility(
    val core: String = ">=0.0.0",
    val java: Int = 21,
)

@Serializable
enum class Loader {
    @SerialName("skill-only") SkillOnly,
    @SerialName("native-jvm") NativeJvm,
    @SerialName("graalvm") Graalvm,
    @SerialName("grpc") Grpc,
}

@Serializable
data class Dependencies(
    val extensions: Map<String, String> = emptyMap(),
)

@Serializable
data class Permissions(
    val agent: List<String> = emptyList(),
    val ipc: IpcPermissions = IpcPermissions(),
    val fs: List<FsPermission> = emptyList(),
    val network: List<NetworkPermission> = emptyList(),
)

@Serializable
data class IpcPermissions(
    val publish: List<String> = emptyList(),
    val subscribe: List<String> = emptyList(),
)

@Serializable
data class FsPermission(
    val path: String,
    val mode: String,
)

@Serializable
data class NetworkPermission(
    val host: String,
    val ports: List<Int> = emptyList(),
)

@Serializable
data class Lifecycle(
    val onLoad: OnLoadStrategy = OnLoadStrategy.Eager,
    val onUnload: OnUnloadStrategy = OnUnloadStrategy.Graceful,
    val hotReload: Boolean = false,
    val loadTimeoutMs: Long = 30_000,
    val unloadTimeoutMs: Long = 10_000,
)

@Serializable
enum class OnLoadStrategy {
    @SerialName("eager") Eager,
    @SerialName("lazy") Lazy,
}

@Serializable
enum class OnUnloadStrategy {
    @SerialName("graceful") Graceful,
    @SerialName("immediate") Immediate,
}
