# Pokémon GO LSPatch research scaffold

This is a **diagnostic starter project**, not a working quick-catch / skip-animation / enhanced-throw implementation.
It builds a legacy Xposed module intended to be loaded into a non-root LSPatch-patched Pokémon GO install. The module only logs process/lifecycle information and relevant mapped native libraries. A Frida IL2CPP discovery script is included to identify current method signatures before any version-specific hook is attempted.

## Why this is the right first step

Pokémon GO gameplay logic is largely Unity IL2CPP native code. An ordinary Xposed Java hook cannot directly intercept those native methods. The previous research notes for version 0.429.1 included native offsets, but those must **not** be reused for 0.431.0 or any newer build without validating the exact `libil2cpp.so` / `libmain.so` binary. This project deliberately avoids replacing `libmain.so` with an unvalidated wrapper.

## Build

Requires Android SDK and Gradle 8.9+ with JDK 17 or 21:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## LSPatch

Patch/install this module using the LSPatch manager/version you already use. Scope it to `com.nianticlabs.pokemongo`. Confirm the module is loaded by checking logcat for `PogoLSPatchResearch`.

## Runtime discovery

Use the included `pogo-il2cpp-inspector.ts` with a compatible Frida + `frida-il2cpp-bridge` setup against the exact game version under test. Record app version, class hits, method names, RVAs, and parameter types. Do not assume an RVA is stable between versions.

## Feature status

- Quick catch: not implemented; requires validated capture-flow hook and behavior testing.
- Skip animations: not implemented; first determine whether the relevant transition is Java-side or Unity-side.
- Enhanced throw: not implemented; requires verified throw-data type/layout and a tested native hook.

The module is intentionally observational until the current APK/native binaries are available and inspected. It does not change game state.
