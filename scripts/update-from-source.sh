#!/usr/bin/env bash
# Build Kael from the current source checkout and (re)install it.
#
# Usage:
#   ./scripts/update-from-source.sh             # build + install
#   ./scripts/update-from-source.sh --no-pull   # skip git pull
#   KAEL_INSTALL_DIR=... KAEL_BIN_DIR=... ./scripts/update-from-source.sh
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

pull=1
for arg in "$@"; do
    case "$arg" in
        --no-pull) pull=0 ;;
        -h|--help)
            sed -n '2,8p' "$0"
            exit 0
            ;;
        *) echo "unknown argument: $arg" >&2; exit 1 ;;
    esac
done

if [[ $pull -eq 1 && -d .git ]]; then
    echo "==> git pull"
    git pull --ff-only
fi

echo "==> ./gradlew bundle"
./gradlew :kael-runtime:bundle --quiet

bundle=$(ls -t kael-runtime/build/distributions/kael-bundle-*.tar.gz 2>/dev/null | head -1)
[[ -n "$bundle" && -f "$bundle" ]] || { echo "error: no bundle produced" >&2; exit 1; }

echo "==> install $bundle"
bash scripts/install.sh "$bundle"
