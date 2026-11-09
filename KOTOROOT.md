# KotoRoot 0.1.0

Personal offline edition of [Root My Galaxy](https://github.com/BuSung-dev/Root-My-Galaxy).

- Separate Android app ID: `dev.koto.kotoroot`.
- Bundled kit supports **SM-S931B / kernel 6.6.98** only. Compatibility is rechecked before execution, including manual selection.
- No catalog or payload network requests. Damaged extracted files are replaced from assets after SHA-256 verification.
- Bundled KernelSU Manager 3.2.5; app updates are checked only on request, against this fork.
- Dark mint interface, K monogram, existing history and settings retained.
- No automatic root at boot. Exploit/helper behavior is unchanged.

## Binary provenance

Payload repository: https://github.com/BuSung-dev/Root-My-Galaxy-Payloads/tree/6e3223e689688540060ddd97a0927e927bfed207

- `payload.so`: `artifacts/pa3q-S938NKSUACZF1/cve-2026-43499-app.so`
- `ksud`: `kernelsu/ksud-s25u-kdp`
- Helper: existing upstream `app/src/main/jniLibs/arm64-v8a/libcve43499root.so`, unchanged.
- Manager: https://github.com/tiann/KernelSU/releases/tag/v3.2.5
- Expected payload size and SHA-256 values are in `app/src/main/assets/offline/targets.json`.
- Payload license is included as `PAYLOAD-LICENSE.txt`. Upstream authors and Apache-2.0 license are retained.

Hashes pin these bytes; they do not guarantee exploit safety or make an unsupported build compatible.

## Build

Use JDK 21+, Android SDK 37, NDK and CMake 3.22.1:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

## Use

Open KotoRoot, leave Shizuku and Advanced mode off for the first standalone attempt, tap **Ready for temporary root**, then confirm **Install KernelSU**. Keep the screen open. The phone can freeze or restart during exploitation. After success open/install the bundled KernelSU Manager and grant root only to trusted apps. Root must be activated again after a reboot. Do not use Manager to flash or patch boot images as part of this temporary-root flow.

## Validation

The repository instrumentation test only prepares and verifies bundled files and repairs a deliberately corrupted cache. It does **not** run the exploit. Run only `PayloadRepositoryTest` on a matching phone; `ExploitUidTest` in upstream is a separate active exploit test.

Validated on the connected SM-S931B / S931BXXS9CZE1:

- Debug APK and instrumentation APK built successfully.
- 9 JVM tests passed.
- The offline preparation/cache-corruption test passed on hardware.
- KotoRoot and the bundled Manager installed successfully; the home screen reports Ready for temporary root.
- No exploit attempt was made during validation. UI display was confirmed by the phone owner; ADB screenshots were black.
