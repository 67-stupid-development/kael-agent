#!/usr/bin/env bash
# Kael installer / updater.
#
# Usage:
#   ./install.sh                    # install from the bundle this script lives in
#   ./install.sh path/to/bundle.tar.gz   # install from a downloaded bundle
#   KAEL_INSTALL_DIR=/opt/kael KAEL_BIN_DIR=/usr/local/bin sudo ./install.sh
#   ./install.sh --uninstall        # remove the current install
#   ./install.sh --rollback         # restore the previous version
set -euo pipefail

KAEL_INSTALL_DIR="${KAEL_INSTALL_DIR:-$HOME/.local/share/kael}"
KAEL_BIN_DIR="${KAEL_BIN_DIR:-$HOME/.local/bin}"

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

log()  { printf '  %s\n' "$*"; }
warn() { printf '  warning: %s\n' "$*" >&2; }
die()  { printf '  error: %s\n' "$*" >&2; exit 1; }

require_cmd() {
    command -v "$1" >/dev/null 2>&1 || die "'$1' not found in PATH"
}

uninstall() {
    if [[ -L "$KAEL_BIN_DIR/kael" ]]; then
        rm "$KAEL_BIN_DIR/kael"
        log "removed symlink $KAEL_BIN_DIR/kael"
    fi
    if [[ -d "$KAEL_INSTALL_DIR" ]]; then
        rm -rf "$KAEL_INSTALL_DIR"
        log "removed $KAEL_INSTALL_DIR"
    fi
    log "uninstalled."
}

rollback() {
    [[ -L "$KAEL_INSTALL_DIR/previous" ]] || die "no previous version to roll back to"
    local cur prev
    cur=$(readlink "$KAEL_INSTALL_DIR/current" 2>/dev/null || true)
    prev=$(readlink "$KAEL_INSTALL_DIR/previous")
    ln -sfn "$prev" "$KAEL_INSTALL_DIR/current"
    rm "$KAEL_INSTALL_DIR/previous"
    log "rolled back: $(basename "$cur") -> $(basename "$prev")"
}

case "${1:-}" in
    --uninstall) uninstall; exit 0 ;;
    --rollback)  rollback;  exit 0 ;;
esac

require_cmd tar
require_cmd java

# Resolve the dist directory we'll install.
work_root=""
cleanup() { [[ -n "$work_root" && -d "$work_root" ]] && rm -rf "$work_root"; }
trap cleanup EXIT

if [[ $# -ge 1 ]]; then
    [[ -f "$1" ]] || die "bundle file not found: $1"
    work_root="$(mktemp -d)"
    log "extracting $1"
    tar -xzf "$1" -C "$work_root"
    search_root="$work_root"
else
    search_root="$script_dir"
fi

dist_dir=""
while IFS= read -r -d '' candidate; do
    if [[ -x "$candidate/bin/kael" ]]; then
        dist_dir="$candidate"
        break
    fi
done < <(find "$search_root" -maxdepth 3 -type d -name 'kael-*' -print0 2>/dev/null)

[[ -n "$dist_dir" ]] || die "no kael distribution found under $search_root"

version="$(basename "$dist_dir" | sed 's/^kael-//')"
[[ -n "$version" ]] || die "could not infer version from $(basename "$dist_dir")"

log "installing kael $version"
log "  install dir: $KAEL_INSTALL_DIR"
log "  bin dir:     $KAEL_BIN_DIR"

mkdir -p "$KAEL_INSTALL_DIR" "$KAEL_BIN_DIR"

# Detect previous install (for upgrade messaging + rollback support).
previous_version=""
if [[ -L "$KAEL_INSTALL_DIR/current" ]]; then
    prev_target=$(readlink "$KAEL_INSTALL_DIR/current")
    previous_version=$(basename "$prev_target" | sed 's/^kael-//')
fi

target="$KAEL_INSTALL_DIR/kael-$version"
if [[ -d "$target" ]]; then
    log "removing existing $target"
    rm -rf "$target"
fi

cp -R "$dist_dir" "$target"
chmod +x "$target/bin/kael"

# Switch the 'current' pointer atomically; keep the prior pointer for rollback.
if [[ -L "$KAEL_INSTALL_DIR/current" ]]; then
    cur_target=$(readlink "$KAEL_INSTALL_DIR/current")
    if [[ "$cur_target" != "$target" ]]; then
        ln -sfn "$cur_target" "$KAEL_INSTALL_DIR/previous"
    fi
fi
ln -sfn "$target" "$KAEL_INSTALL_DIR/current"
ln -sfn "$KAEL_INSTALL_DIR/current/bin/kael" "$KAEL_BIN_DIR/kael"

if [[ -n "$previous_version" && "$previous_version" != "$version" ]]; then
    log "updated: $previous_version -> $version"
elif [[ -n "$previous_version" ]]; then
    log "reinstalled: $version"
else
    log "installed: $version"
fi

if ! printf %s ":$PATH:" | grep -q ":$KAEL_BIN_DIR:"; then
    warn "$KAEL_BIN_DIR is not on your PATH."
    warn "  add to your shell profile:  export PATH=\"$KAEL_BIN_DIR:\$PATH\""
fi

log "smoke-testing 'kael --help'..."
if "$KAEL_BIN_DIR/kael" --help >/dev/null 2>&1; then
    log "ok."
else
    warn "smoke test failed — try running '$KAEL_BIN_DIR/kael --help' manually"
    exit 1
fi
