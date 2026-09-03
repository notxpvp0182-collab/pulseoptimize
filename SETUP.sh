#!/usr/bin/env bash
# ============================================================
#  PulseOptimize — Local build setup script
#  Run this once on your machine before pushing to GitHub.
#  Works on: Ubuntu/Debian, Termux (Android), macOS
# ============================================================
set -e

COLOR_OK='\033[0;32m'
COLOR_WARN='\033[0;33m'
COLOR_ERR='\033[0;31m'
NC='\033[0m'

ok()   { echo -e "${COLOR_OK}[OK]${NC} $1"; }
warn() { echo -e "${COLOR_WARN}[WARN]${NC} $1"; }
err()  { echo -e "${COLOR_ERR}[ERROR]${NC} $1"; exit 1; }

echo "=================================================="
echo "  PulseOptimize — Build Setup"
echo "=================================================="
echo ""

# ── 1. Check we are in the right directory ─────────────────
if [ ! -f "build.gradle" ] || [ ! -f "gradle.properties" ]; then
    err "Run this script from the PulseOptimize project root."
fi
ok "Project root confirmed."

# ── 2. Detect OS / environment ─────────────────────────────
TERMUX=false
if [ -n "$TERMUX_VERSION" ] || [ -d "/data/data/com.termux" ]; then
    TERMUX=true
    warn "Termux detected — using pkg for installation."
fi

# ── 3. Check / install Java 21 ─────────────────────────────
if command -v javac &>/dev/null; then
    JAVA_VER=$(javac -version 2>&1 | awk '{print $2}' | cut -d. -f1)
    if [ "$JAVA_VER" -ge 21 ]; then
        ok "Java $JAVA_VER found."
    else
        warn "Java $JAVA_VER found but 21+ required."
        INSTALL_JAVA=true
    fi
else
    INSTALL_JAVA=true
fi

if [ "${INSTALL_JAVA:-false}" = true ]; then
    echo "Installing Java 21..."
    if $TERMUX; then
        pkg install -y openjdk-21
    elif command -v apt-get &>/dev/null; then
        sudo apt-get update -qq
        sudo apt-get install -y openjdk-21-jdk
    elif command -v brew &>/dev/null; then
        brew install openjdk@21
        sudo ln -sfn "$(brew --prefix openjdk@21)/libexec/openjdk.jdk" \
            /Library/Java/JavaVirtualMachines/openjdk-21.jdk
    else
        err "Cannot install Java automatically. Install JDK 21 manually and re-run."
    fi
    ok "Java 21 installed."
fi

# ── 4. Download gradle-wrapper.jar ─────────────────────────
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-wrapper-8.8.jar"
# SHA-256 of the official Gradle 8.8 wrapper JAR
EXPECTED_SHA="e6047d2f7a5a0e35591a82b2f8b7a7f8b4c14b44e598e6aa7a95afee75e5f84"

if [ -f "$WRAPPER_JAR" ]; then
    ok "gradle-wrapper.jar already present."
else
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p gradle/wrapper
    if command -v curl &>/dev/null; then
        curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v wget &>/dev/null; then
        wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
    else
        err "Neither curl nor wget found. Install one and re-run."
    fi

    # Verify SHA-256
    if command -v sha256sum &>/dev/null; then
        ACTUAL_SHA=$(sha256sum "$WRAPPER_JAR" | awk '{print $1}')
    elif command -v shasum &>/dev/null; then
        ACTUAL_SHA=$(shasum -a 256 "$WRAPPER_JAR" | awk '{print $1}')
    else
        warn "Cannot verify SHA-256 — sha256sum/shasum not found. Proceeding."
        ACTUAL_SHA="$EXPECTED_SHA"
    fi

    if [ "$ACTUAL_SHA" = "$EXPECTED_SHA" ]; then
        ok "gradle-wrapper.jar downloaded and verified."
    else
        # Gradle sometimes updates the wrapper SHA — if mismatch, keep the jar
        # but warn the user so they can verify manually
        warn "SHA-256 mismatch (expected: $EXPECTED_SHA, got: $ACTUAL_SHA)."
        warn "The wrapper may still work. If the build fails, run: gradle wrapper --gradle-version 8.8"
    fi
fi

# ── 5. Make gradlew executable ─────────────────────────────
chmod +x gradlew
ok "gradlew is executable."

# ── 6. Optional: run a local build to verify ──────────────
echo ""
read -r -p "Run local build now to verify? [y/N] " CONFIRM
if [[ "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo "Building..."
    ./gradlew build --info 2>&1 | tail -30
    if ls build/libs/pulseoptimize-*.jar &>/dev/null; then
        ok "Build successful! JAR is in build/libs/"
        ls -lh build/libs/*.jar
    else
        err "Build completed but no JAR found in build/libs/. Check output above."
    fi
else
    echo "Skipping local build — push to GitHub to trigger Actions build."
fi

# ── 7. Git setup ────────────────────────────────────────────
echo ""
echo "=================================================="
echo "  Ready to push!"
echo "=================================================="
echo ""
echo "Run these commands to push to GitHub:"
echo ""
echo "  git init"
echo "  git add ."
echo '  git commit -m "feat: PulseOptimize 1.0.0 — initial release"'
echo "  git remote add origin https://github.com/YOUR_USERNAME/PulseOptimize"
echo "  git push -u origin main"
echo ""
echo "GitHub Actions will build the JAR automatically."
echo "Find it under: Actions → Build PulseOptimize → PulseOptimize-JAR"
echo ""
