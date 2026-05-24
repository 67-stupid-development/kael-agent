package dev.kael.sdk.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface Command<I : Any, O : Any> {
    val id: String
    val inputType: KClass<I>
    val outputType: KClass<O>
    fun invoke(input: I, ctx: CallContext): Flow<Event<O>>
}

interface CallContext {
    val callId: String
    val sessionId: String?
    val scope: CoroutineScope
}
