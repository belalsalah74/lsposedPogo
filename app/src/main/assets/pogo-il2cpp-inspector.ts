import "frida-il2cpp-bridge";

// Discovery only. This script does not alter game state or automate catches/throws.
const targetClasses = [
  "Niantic.Holoholo.Encounter.EncounterInteractionState",
  "Niantic.Holoholo.Encounter.Pokeball",
  "Niantic.Holoholo.Encounter.PokemonEncounter",
];
const targetMethods = new Set(["AttemptCapture", "PokemonCaptured", "EnterCaptureState"]);

function safe<T>(fn: () => T, fallback: T): T {
  try { return fn(); } catch (_) { return fallback; }
}

Il2Cpp.perform(() => {
  console.log(`[POGO] app=${safe(() => Il2Cpp.application.identifier, "?")} version=${safe(() => Il2Cpp.application.version, "?")}`);
  console.log(`[POGO] Unity=${safe(() => Il2Cpp.unityVersion, "?")}`);
  const image = Il2Cpp.domain.assembly("Assembly-CSharp").image;
  for (const className of targetClasses) {
    let klass: any;
    try { klass = image.class(className); }
    catch (_) { console.log(`[MISS] ${className}`); continue; }
    console.log(`\n[CLASS] ${className}`);
    for (const method of klass.methods) {
      if (!targetMethods.has(method.name)) continue;
      console.log(`[METHOD] ${method.name} RVA=${safe(() => "0x" + method.relativeVirtualAddress.toString(16), "?")}`);
      for (const [i, p] of method.parameters.entries()) {
        console.log(`  arg${i}: ${safe(() => p.name, "?")} : ${safe(() => p.type.name, "?")}`);
      }
    }
  }
  console.log("[POGO] discovery complete; no hooks installed");
});
