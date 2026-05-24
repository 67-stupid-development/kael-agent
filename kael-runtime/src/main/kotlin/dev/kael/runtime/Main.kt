package dev.kael.runtime

import com.github.ajalt.clikt.core.main
import dev.kael.cli.KaelCli
import java.nio.file.Path

fun main(args: Array<String>) {
    val home = System.getProperty("user.home")
    val stateDir = System.getenv("KAEL_HOME")
        ?.let(Path::of)
        ?: Path.of(home, ".kael")

    KaelCli.build(stateDir).main(args)
}
