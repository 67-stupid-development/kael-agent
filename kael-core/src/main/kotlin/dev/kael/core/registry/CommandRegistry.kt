package dev.kael.core.registry

import dev.kael.sdk.command.Command

class CommandRegistry {
    private val byId = linkedMapOf<String, Command<*, *>>()

    fun register(command: Command<*, *>) {
        check(byId.put(command.id, command) == null) {
            "command ${command.id} already registered"
        }
    }

    fun unregister(id: String) {
        byId.remove(id)
    }

    fun list(): List<Command<*, *>> = byId.values.toList()

    fun byId(id: String): Command<*, *>? = byId[id]
}
