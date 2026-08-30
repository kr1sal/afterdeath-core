# Project-sweep checklist

Walk these in order for `/neoforge-docs` (no args) or the Phase C "check everything" pass. Each item names the file to read, what the docs require, and how to report the finding.

## 1. Toolchain pins (`gradle.properties`)

Compare against `references/versions.md` and the two Maven metadata URLs there. Report:
- `minecraft_version` == `1.21.1`
- `neo_version` == latest 21.1.x on Maven (currently `21.1.248`, series closed)
- `neogradle.subsystems.parchment.mappingsVersion` == latest 1.21.1 release
- `org.gradle.java.home` points at a JDK **21** install

Docs: `https://docs.neoforged.net/docs/gettingstarted/` + `https://docs.neoforged.net/docs/gettingstarted/versioning`.

## 2. `neoforge.mods.toml`

File: `src/main/resources/META-INF/neoforge.mods.toml`. Cross-reference against `https://docs.neoforged.net/docs/gettingstarted/modfiles`.

Verify:
- `modLoader = "javafml"` (or documented alternative)
- `loaderVersion` uses `${loader_version_range}` and matches `gradle.properties`
- `license`, `[[mods]]`, `modId`, `version`, `displayName`, `description` present
- `modId` matches `[a-z][a-z0-9_]{1,63}` and equals `mod_id` in `gradle.properties`
- Every `[[dependencies.<modid>]]` block has `modId`, `type`, `versionRange`
- `neoforge` dependency has `type = "required"` and `versionRange = "${neo_version_range}"`
- `minecraft` dependency has `versionRange = "${minecraft_version_range}"`

Flag any hard-coded version literal that should be `${…}` substitution.

## 3. Main mod class

File: `src/main/java/<group>/<modid>/*.java`. Docs: `https://docs.neoforged.net/docs/gettingstarted/modfiles` + `https://docs.neoforged.net/docs/concepts/events`.

Verify:
- `@Mod("<modid>")` annotation, id matches `neoforge.mods.toml` and `gradle.properties`
- Constructor takes some subset of `IEventBus modBus`, `ModContainer container`, `FMLModContainer`, `Dist` (any order)
- Uses the **mod event bus** (`modBus.addListener(...)`) for lifecycle/registration events, and `NeoForge.EVENT_BUS` only for gameplay events

## 4. Registration pattern

Grep for `DeferredRegister` usages. Docs: `https://docs.neoforged.net/docs/concepts/registries`.

Verify:
- `DeferredRegister.create(...)` uses a `BuiltInRegistries` handle or a custom-registry key that exists in 1.21.1
- `.register(modBus)` is called from the mod constructor (not from a static initializer)

## 5. Config

File: `src/main/java/<group>/<modid>/Config.java`. Docs: `https://docs.neoforged.net/docs/misc/config`.

Verify:
- Uses `ModConfigSpec.Builder` (NOT `ForgeConfigSpec` — that's the old Forge name)
- `@EventBusSubscriber(modid = "…", bus = EventBusSubscriber.Bus.MOD)` on the config-loading class
- Listens to `ModConfigEvent.Loading` / `.Reloading` on the mod bus

## 6. Resource pack format

If `src/main/resources/pack.mcmeta` exists, confirm `pack.pack_format == 34` for MC 1.21 / 1.21.1. Docs: `https://docs.neoforged.net/docs/resources/`.

## 7. Data generation (only if `GatherDataEvent` referenced)

Docs: `https://docs.neoforged.net/docs/datagen/`. Verify the listener is on the mod bus, and generators are registered inside `event.includeClient()` / `event.includeServer()` as appropriate.

## 8. Networking (only if `CustomPacketPayload` or `PayloadRegistrar` referenced)

Docs: `https://docs.neoforged.net/docs/networking/payload`. Verify:
- `RegisterPayloadHandlersEvent` is subscribed on the mod bus
- Payloads implement `CustomPacketPayload` with a `Type<T>` and `StreamCodec` — the pre-1.20.5 channel-based API is gone

## Reporting format

Emit one bulleted line per item:

```
- [OK] mods.toml modLoader = javafml
- [ISSUE] gradle.properties:21 — neo_version=21.1.240, latest is 21.1.248 (Maven metadata)
- [SKIP] no CustomPacketPayload usage found
```

End with a one-line summary: `N OK · M issues · K skipped`.
