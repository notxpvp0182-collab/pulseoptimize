#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
#  PulseOptimize — Termux Quick Setup
#  Run this inside Termux on your Android device.
# ============================================================
set -e

echo "=== PulseOptimize Termux Setup ==="
echo ""

# Confirm project root
if [ ! -f "build.gradle" ]; then
    echo "ERROR: Run from the PulseOptimize project root."
    exit 1
fi

# 1. Install required packages
echo "[1/4] Installing packages..."
pkg update -y 2>/dev/null | tail -2
pkg install -y openjdk-21 git curl 2>/dev/null | tail -3
echo "OK"

# 2. Verify javac
javac -version 2>&1 && echo "javac OK"

# 3. Download gradle-wrapper.jar
echo "[2/4] Downloading gradle-wrapper.jar..."
mkdir -p gradle/wrapper
curl -fsSL -o gradle/wrapper/gradle-wrapper.jar \
    "https://services.gradle.org/distributions/gradle-wrapper-8.8.jar"
echo "OK"

# 4. Make gradlew executable
echo "[3/4] Setting permissions..."
chmod +x gradlew
echo "OK"

# 5. Run build
echo "[4/4] Building PulseOptimize..."
echo "(This will download Minecraft mappings ~500 MB on first run — be patient)"
echo ""
./gradlew build --no-daemon 2>&1 | grep -E "BUILD|error:|warning:|FAILED|SUCCESS|Exception" || true

echo ""
if ls build/libs/pulseoptimize-*.jar 2>/dev/null; then
    echo "=== BUILD SUCCESSFUL ==="
    ls -lh build/libs/*.jar
    echo ""
    echo "JAR is ready. Copy it to your mods folder or push to GitHub."
else
    echo "=== BUILD FAILED — check output above ==="
fi
