# libseanime Binary

This directory should contain the prebuilt `libseanime.so` binaries compiled from the Seanime Go backend.

## Required Structure

```
jniLibs/
  arm64-v8a/
    libseanime.so
  armeabi-v7a/
    libseanime.so
  x86_64/
    libseanime.so
```

## How to Obtain

Build the binaries from the official Seanime repository following the Android build instructions:

1. Clone https://github.com/5rahim/seanime
2. Follow the DEVELOPMENT.md guide for patching main.go and dependencies
3. Cross-compile for Android ARM64, ARMv7, and x86_64
4. Place the resulting `libseanime.so` files in the correct ABI directories above

## Integration with Seanime-Android

You can also extract the binaries from a compiled Seanime-Android APK:

1. Download the APK from https://github.com/Seanime-contributions/Seanime-Android/releases
2. Extract using `unzip app-release.apk`
3. Find `.so` files in `lib/{abi}/libseanime.so`
4. Copy to the appropriate jniLibs directories
