# Helios — task runner. Run `just` to see all recipes.
#
# Requires: just (https://github.com/casey/just). Install on macOS with `brew install just`.
# Most recipes shell out to the Gradle wrapper (./gradlew) and the Android platform-tools
# (adb), so run `just setup` once before anything else.

set shell := ["bash", "-uc"]

# Application identifiers (the debug build appends `.debug` to the applicationId).
app_id        := "com.helios.spaceweather"
app_id_debug  := app_id + ".debug"
main_activity := app_id + "/com.helios.spaceweather.MainActivity"
debug_apk     := "app/build/outputs/apk/debug/app-debug.apk"
release_apk   := "app/build/outputs/apk/release/app-release.apk"

# Show the list of available recipes (default when you just type `just`).
default:
    @just --list --unsorted

# One-time environment bootstrap (JDK, Android SDK check, local.properties, deps warm-up).
setup:
    ./setup.sh

# Auto-bootstrap on a fresh clone: if local.properties is missing (i.e. setup never ran),
# run setup.sh so `just build` / `just release` work straight after `git clone`.
_bootstrap:
    @[ -f local.properties ] || ./setup.sh

# Compile the debug APK, then copy the built APK file to the clipboard (macOS).
build: _assemble
    @osascript -e "set the clipboard to (POSIX file \"$(pwd)/{{debug_apk}}\")"
    @echo "Copied debug APK to clipboard."

# Assemble the debug APK without the clipboard copy (used by install / run / update).
_assemble: _bootstrap
    ./gradlew :app:assembleDebug

# Alias kept for discoverability.
build-debug: build

# Compile the minified, debug-signed release APK.
build-release: _bootstrap
    ./gradlew :app:assembleRelease

# Build the signed release APK and publish it as a GitHub release with the APK attached.
# Tag defaults to v<versionName> (read from app/build.gradle.kts); override e.g. `just release v1.2.0`.
# Requires the GitHub CLI (`gh auth login`). Re-running with an existing tag uploads/overwrites the asset.
release tag="": build-release
    #!/usr/bin/env bash
    set -euo pipefail
    version="$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' app/build.gradle.kts | head -1)"
    tag="{{tag}}"; [ -n "$tag" ] || tag="v${version}"
    asset="helios-space-weather-${tag}.apk"
    cp "{{release_apk}}" "$asset"
    if gh release view "$tag" >/dev/null 2>&1; then
        echo "Release $tag exists — uploading/replacing the APK asset."
        gh release upload "$tag" "$asset" --clobber
    else
        echo "Creating release $tag with $asset"
        gh release create "$tag" "$asset" --title "$tag" --generate-notes
    fi
    rm -f "$asset"
    echo "Done: $(gh release view "$tag" --json url -q .url)"

# Incremental rebuild — only what changed (no clipboard copy).
update: _assemble

# Full clean build from scratch.
rebuild: clean build

# Remove all build outputs.
clean:
    ./gradlew clean

# Static analysis (Android Lint) on the debug variant.
lint:
    ./gradlew :app:lintDebug

# Unit tests.
test:
    ./gradlew :app:testDebugUnitTest

# Lint + unit tests + assemble — the gate to run before pushing.
check: lint test build

# List connected devices/emulators.
devices:
    adb devices -l

# Install the debug APK onto the connected device (assembles first if needed).
install: _assemble
    adb install -r "{{debug_apk}}"

# Build, install, and launch the app on the connected device.
run: install
    adb shell am start -n "{{main_activity}}"

# Uninstall the debug build from the connected device.
uninstall:
    -adb uninstall "{{app_id_debug}}"

# Stream filtered logs for the app process.
logs:
    adb logcat --pid=$(adb shell pidof -s "{{app_id_debug}}")

# Print the absolute path of the built debug APK.
apk-path:
    @echo "$(pwd)/{{debug_apk}}"

# Copy the absolute debug-APK path (text) to the clipboard (macOS pbcopy).
copy-apk-path: _assemble
    @printf '%s' "$(pwd)/{{debug_apk}}" | pbcopy
    @echo "Copied APK path to clipboard."

# Copy the debug APK file to the clipboard — alias for `build`, which now does this.
copy-apk: build

# Regenerate the Gradle wrapper at a specific version, e.g. `just wrapper 8.11.1`.
wrapper version="8.11.1":
    gradle wrapper --gradle-version {{version}} --distribution-type bin

# Print the resolved dependency tree for the debug variant.
deps:
    ./gradlew :app:dependencies --configuration debugRuntimeClasspath
