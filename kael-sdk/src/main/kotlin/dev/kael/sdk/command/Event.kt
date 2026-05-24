package dev.kael.sdk.command

sealed interface Event<out O> {
    data class Progress(val message: String) : Event<Nothing>
    data class Partial<O>(val value: O) : Event<O>
    data class Complete<O>(val value: O) : Event<O>
    data class Failed(val error: Throwable) : Event<Nothing>
}
