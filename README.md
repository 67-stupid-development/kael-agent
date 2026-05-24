# Kael

**Kotlin Agent Extension Loader.** A manifest-driven extension framework for
building agents on the JVM.

> Status: **v0.1 — M1 complete.** Skill-only extensions load and list through
> the CLI. The Command bus, `native-jvm` loader, sessions, memory, and
> `agent.run` are next milestones.

## What it is

Kael loads agent capabilities — *extensions* — at runtime from a versioned
manifest. Today only one extension flavor is wired up:

- **`skill-only`** — a folder of Anthropic-style Markdown skills (frontmatter
  + body) plus a `manifest.yml`. No code, no JAR.

Planned flavors (post-v0.1): `native-jvm` (SPI via `ServiceLoader` + isolated
`URLClassLoader`), `graalvm` (per-`Context` host bindings), `grpc` (sidecar
processes for other languages).

## Layout

```
kael-sdk/        stable interfaces extensions compile against
kael-core/       manifest parser, loaders, registries
kael-cli/        Clikt command tree (ext, skill)
kael-runtime/    main() — wires core + cli; produces the dist + bundle
examples/        hello-skill — smoke test for the skill-only loader
scripts/         install.sh, update-from-source.sh, BUNDLE-README.md
```

## Requirements

- JDK 21+ on `PATH` to run. Builds with Java 25 (compiles to bytecode 21).
- Linux or macOS for the installer. Windows works under WSL — Git Bash
  doesn't create real symlinks by default.

## Build

```sh
./gradlew :kael-runtime:installDist        # exploded dist
./gradlew :kael-runtime:bundle              # kael-bundle-<version>.tar.gz
```

The bundle lands in `kael-runtime/build/distributions/` and contains the
distribution, `install.sh`, and a README.

## Install

From a bundle tarball:

```sh
tar -xzf kael-bundle-0.1.0-SNAPSHOT.tar.gz
cd kael-bundle-0.1.0-SNAPSHOT
./install.sh
```

From a source checkout (build + install in one step):

```sh
./scripts/update-from-source.sh             # git pull + bundle + install
./scripts/update-from-source.sh --no-pull   # skip the pull
```

The installer is idempotent. Versions live in `~/.local/share/kael/kael-<v>/`
with a `current` symlink; the prior version is kept as `previous` for
rollback.

```sh
./install.sh --rollback     # swap current ↔ previous
./install.sh --uninstall    # remove everything
```

Override defaults with `KAEL_INSTALL_DIR` and `KAEL_BIN_DIR`. See
[scripts/BUNDLE-README.md](scripts/BUNDLE-README.md) for details.

## CLI

```sh
kael ext load <path>        # load an extension from a directory
kael ext list               # list loaded extensions
kael ext info <id>          # show manifest details
kael ext unload <id>

kael skill list             # all skills across loaded extensions
kael skill info <ref>       # full id or unambiguous slug
```

State (the set of loaded extensions) persists in `$KAEL_HOME/extensions.txt`,
defaulting to `~/.kael/`.

## Example

```sh
kael ext load examples/hello-skill
kael skill list
# dev.kael.examples.hello/greet         —  Greet
# dev.kael.examples.hello/code-review   —  Code Review

kael skill info greet
```

The manifest is at [examples/hello-skill/manifest.yml](examples/hello-skill/manifest.yml).

## Roadmap

- **M2** — `Command<I, O>` bus + `native-jvm` SPI loader. Built-in
  `extension.*` / `skill.*` commands so the CLI becomes a thin wrapper over
  the bus.
- **M3** — Sessions, file-backed memory, `agent.run` streaming via
  `Flow<Event<O>>`.
- **M4+** — `graalvm` loader, `grpc` sidecar loader, NATS event bus, Mixin
  opt-in, hot reload, permissions enforcement, extension signing, OTel.

## License

TBD.
