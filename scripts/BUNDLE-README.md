# Kael Bundle

This archive contains a self-contained build of Kael (the Kotlin Agent
Extension Loader) plus an installer script.

## Install / update

```sh
./install.sh
```

The script is idempotent: run it again to upgrade in place. The previous
version is kept as a `previous` symlink so you can roll back.

### Custom paths

```sh
KAEL_INSTALL_DIR=/opt/kael KAEL_BIN_DIR=/usr/local/bin sudo ./install.sh
```

Defaults are `~/.local/share/kael` and `~/.local/bin`.

### Rollback / uninstall

```sh
./install.sh --rollback
./install.sh --uninstall
```

## Requirements

- Java 21 or newer must be on `PATH`. The `kael` launcher honours
  `JAVA_HOME` if set.
- A filesystem that supports symbolic links (any standard Linux or
  macOS setup). On Windows use WSL — Git Bash will not create real
  symlinks under default settings.
