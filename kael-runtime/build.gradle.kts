plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    distribution
}

application {
    mainClass.set("dev.kael.runtime.MainKt")
    applicationName = "kael"
}

dependencies {
    implementation(project(":kael-cli"))
    implementation(project(":kael-core"))
    runtimeOnly(libs.logback.classic)
}

val bundle by tasks.registering(Tar::class) {
    group = "distribution"
    description = "Self-contained bundle: distribution + install scripts."
    dependsOn(tasks.named("installDist"))

    compression = Compression.GZIP
    archiveBaseName.set("kael-bundle")
    archiveVersion.set(project.version.toString())
    archiveExtension.set("tar.gz")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    val bundleRoot = "kael-bundle-${project.version}"
    val distRoot = "$bundleRoot/kael-${project.version}"

    val scriptPermissions = Action<ConfigurableFilePermissions> {
        user {
            read = true; write = true; execute = true
        }
        group {
            read = true; execute = true
        }
        other {
            read = true; execute = true
        }
    }

    from(rootProject.file("scripts/install.sh")) {
        into(bundleRoot)
        filePermissions(scriptPermissions)
    }
    from(rootProject.file("scripts/BUNDLE-README.md")) {
        into(bundleRoot)
        rename { "README.md" }
    }
    from(layout.buildDirectory.dir("install/kael")) {
        into(distRoot)
        eachFile {
            if (path.endsWith("bin/kael")) {
                permissions(scriptPermissions)
            }
        }
    }
}

tasks.named("assembleDist") {
    dependsOn(bundle)
}
