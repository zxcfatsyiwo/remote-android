#!/bin/bash
PROJECT_DIR=$(pwd)
BUILD_DIR="$PROJECT_DIR/build_manual"
mkdir -p "$BUILD_DIR"

AAPT=/data/data/com.termux/files/usr/bin/aapt
DX=/data/data/com.termux/files/usr/bin/dx
APKSIGNER=/data/data/com.termux/files/usr/bin/apksigner
ANDROID_JAR=/data/data/com.termux/files/home/android-sdk/android.jar
KEYSTORE=~/.debug.keystore
KEYSTORE_PASS=android
KEY_ALIAS=debug

echo "=== Step 1: Compile resources ==="
$AAPT package -f -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I "$ANDROID_JAR" \
  -F "$BUILD_DIR/resources.apk"

echo "=== Step 2: Compile Kotlin/Java code ==="
find app/src/main/java -name "*.kt" -o -name "*.java" > "$BUILD_DIR/sources.txt"
mkdir -p "$BUILD_DIR/classes"
kotlinc -cp "$ANDROID_JAR" -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt"

echo "=== Step 3: Create DEX ==="
$DX --dex --output="$BUILD_DIR/classes.dex" "$BUILD_DIR/classes"

echo "=== Step 4: Build APK ==="
$AAPT package -f -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I "$ANDROID_JAR" \
  -F "$BUILD_DIR/app-unaligned.apk" "$BUILD_DIR/resources.apk"
$AAPT add "$BUILD_DIR/app-unaligned.apk" "$BUILD_DIR/classes.dex"

echo "=== Step 5: Sign APK ==="
$APKSIGNER sign --ks "$KEYSTORE" --ks-pass pass:"$KEYSTORE_PASS" --key-pass pass:"$KEYSTORE_PASS" "$BUILD_DIR/app-unaligned.apk"
mv "$BUILD_DIR/app-unaligned.apk" "$BUILD_DIR/app-debug.apk"

echo "=== BUILD SUCCESSFUL ==="
echo "APK: $BUILD_DIR/app-debug.apk"
cp "$BUILD_DIR/app-debug.apk" /storage/emulated/0/Download/
echo "Copied to /storage/emulated/0/Download/app-debug.apk"
