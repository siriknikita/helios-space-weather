#!/usr/bin/env bash
#
# Helios — development environment bootstrap.
#
# Idempotent. Verifies the toolchain (JDK 17, Android SDK, adb), writes a local.properties
# pointing at the Android SDK, then primes the Gradle dependency cache. Safe to re-run.
#
# Usage: ./setup.sh   (or `just setup`)

set -euo pipefail

# ---- pretty output helpers --------------------------------------------------
bold() { printf '\033[1m%s\033[0m\n' "$1"; }
ok()   { printf '\033[32m✓\033[0m %s\n' "$1"; }
warn() { printf '\033[33m!\033[0m %s\n' "$1"; }
err()  { printf '\033[31m✗\033[0m %s\n' "$1" >&2; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

bold "Helios setup — repo: $REPO_ROOT"

# ---- 1. JDK 17 --------------------------------------------------------------
bold "Checking JDK..."
if ! command -v java >/dev/null 2>&1; then
    if command -v brew >/dev/null 2>&1; then
        warn "Java not found — installing openjdk@17 via Homebrew..."
        brew install openjdk@17 || true
        # Make the brew JDK discoverable to the system java wrapper.
        sudo ln -sfn "$(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk" \
            /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || true
    fi
fi
if ! command -v java >/dev/null 2>&1; then
    err "Java not found. Install a JDK 17 (e.g. 'brew install openjdk@17') and re-run."
    exit 1
fi
JAVA_MAJOR="$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)"
if [ "${JAVA_MAJOR:-0}" -lt 17 ]; then
    err "JDK 17+ required, found major version ${JAVA_MAJOR:-unknown}."
    err "On macOS: brew install openjdk@17"
    exit 1
fi
ok "JDK ${JAVA_MAJOR} detected."

# ---- 2. Android SDK ---------------------------------------------------------
bold "Locating Android SDK..."
SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK_DIR" ]; then
    # Common default install locations.
    for candidate in "$HOME/Library/Android/sdk" "$HOME/Android/Sdk"; do
        if [ -d "$candidate" ]; then SDK_DIR="$candidate"; break; fi
    done
fi

if [ -z "$SDK_DIR" ] || [ ! -d "$SDK_DIR" ]; then
    warn "Android SDK not found."
    warn "Install Android Studio (https://developer.android.com/studio) or the command-line"
    warn "tools, then set ANDROID_HOME, e.g.:"
    warn "  export ANDROID_HOME=\"\$HOME/Library/Android/sdk\""
    warn "Re-run ./setup.sh afterwards to write local.properties."
else
    ok "Android SDK at: $SDK_DIR"
    # local.properties must use an escaped path on all platforms.
    printf 'sdk.dir=%s\n' "$SDK_DIR" > local.properties
    ok "Wrote local.properties (sdk.dir)."

    # Accept SDK licenses non-interactively so a fresh clone can build unattended.
    SDKMANAGER=""
    for sm in "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" "$SDK_DIR/tools/bin/sdkmanager"; do
        if [ -x "$sm" ]; then SDKMANAGER="$sm"; break; fi
    done
    if [ -n "$SDKMANAGER" ]; then
        if yes | "$SDKMANAGER" --licenses >/dev/null 2>&1; then
            ok "Android SDK licenses accepted."
        else
            warn "Could not auto-accept SDK licenses; open Android Studio's SDK Manager once."
        fi
    fi

    # ---- 3. adb (platform-tools) -------------------------------------------
    if command -v adb >/dev/null 2>&1; then
        ok "adb on PATH: $(adb version | head -1)"
    elif [ -x "$SDK_DIR/platform-tools/adb" ]; then
        warn "adb found in the SDK but not on PATH. Add this to your shell profile:"
        warn "  export PATH=\"\$PATH:$SDK_DIR/platform-tools\""
    else
        warn "adb not found. Install it via Android Studio's SDK Manager (Platform-Tools)."
    fi
fi

# ---- 4. just (optional task runner) ----------------------------------------
bold "Checking optional tooling..."
if command -v just >/dev/null 2>&1; then
    ok "just $(just --version)"
elif command -v brew >/dev/null 2>&1; then
    warn "'just' not installed — installing via Homebrew..."
    brew install just && ok "just $(just --version)" || warn "Could not install 'just' automatically."
else
    warn "'just' not installed (optional). On macOS: brew install just"
fi

# ---- 5. Gradle wrapper sanity + dependency warm-up -------------------------
bold "Priming Gradle (this downloads dependencies on first run)..."
if [ ! -x ./gradlew ]; then
    chmod +x ./gradlew || true
fi
if [ -f local.properties ]; then
    if ./gradlew --quiet :app:help >/dev/null 2>&1; then
        ok "Gradle configured successfully; dependencies resolved."
    else
        warn "Gradle could not fully configure yet — usually a missing SDK component."
        warn "Open the project in Android Studio once to accept SDK licenses, then re-run."
    fi
else
    warn "Skipping Gradle warm-up until the Android SDK is configured."
fi

bold "Done."
echo "Next: 'just build' to assemble the debug APK, or 'just run' to install + launch."
