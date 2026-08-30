# NeoForge docs URL index

Fetch these directly with `WebFetch`. The docs site is versioned but its `/docs/` prefix silently redirects to the newest supported branch — always double-check the page's own version banner shows **1.21.x** before quoting it. Where an older, MC-1.21.1-relevant primer exists under `/primer/`, prefer that.

## Getting started

- `https://docs.neoforged.net/docs/gettingstarted/` — overview + JDK/gradle steps
- `https://docs.neoforged.net/docs/gettingstarted/modfiles` — `neoforge.mods.toml` reference
- `https://docs.neoforged.net/docs/gettingstarted/structuring` — package/class layout
- `https://docs.neoforged.net/docs/gettingstarted/versioning` — versioning scheme + MVR ranges

## Concepts

- `https://docs.neoforged.net/docs/concepts/sides` — client vs. server, `Dist`, `@OnlyIn`
- `https://docs.neoforged.net/docs/concepts/events` — event bus, `@SubscribeEvent`, mod bus vs. game bus
- `https://docs.neoforged.net/docs/concepts/registries` — `DeferredRegister`, `RegisterEvent`, custom registries
- `https://docs.neoforged.net/docs/concepts/lifecycle` — mod loading phases

## Blocks / items / entities

- `https://docs.neoforged.net/docs/blocks/` — index for block system
- `https://docs.neoforged.net/docs/items/` — index for items, tabs, tooltips
- `https://docs.neoforged.net/docs/blockentities/` — block entities & renderers
- `https://docs.neoforged.net/docs/entities/` — entities & attributes

## Data / resources

- `https://docs.neoforged.net/docs/resources/` — resource-pack format for 1.21.x (pack_format 34)
- `https://docs.neoforged.net/docs/resources/client/models/` — models & datagen
- `https://docs.neoforged.net/docs/resources/server/` — recipes, loot, tags, advancements
- `https://docs.neoforged.net/docs/datagen/` — `GatherDataEvent`, generators
- `https://docs.neoforged.net/docs/datagen/tags` — tag data-gen
- `https://docs.neoforged.net/docs/datagen/recipes` — recipe data-gen

## Networking / capabilities / IPC

- `https://docs.neoforged.net/docs/networking/` — network channel + custom payloads (1.21 rework)
- `https://docs.neoforged.net/docs/networking/payload` — `CustomPacketPayload` API
- `https://docs.neoforged.net/docs/datastorage/capabilities` — capabilities framework (1.20.5+ rework)
- `https://docs.neoforged.net/docs/datastorage/attachments` — data attachments

## Rendering / GUI

- `https://docs.neoforged.net/docs/gui/` — screens, menus, widgets
- `https://docs.neoforged.net/docs/gui/menus` — `AbstractContainerMenu`
- `https://docs.neoforged.net/docs/rendering/` — client rendering hooks

## Misc

- `https://docs.neoforged.net/docs/misc/config` — `ModConfigSpec` (mirrors the project's `Config.java`)
- `https://docs.neoforged.net/docs/misc/keymappings` — key bindings
- `https://docs.neoforged.net/docs/misc/updatechecker/` — update JSON schema (the `updateJSONURL` in `neoforge.mods.toml`)

## Primers (per-MC-version migration notes)

- `https://docs.neoforged.net/primer/docs/` — index
- `https://docs.neoforged.net/primer/1.21/` — 1.20.6 → 1.21 changes (relevant baseline for this project)
- `https://docs.neoforged.net/primer/1.20.5/` — the big data-components / capabilities rework

## Tooling / gradle

- `https://neoforged.net/neogradle/` — NeoGradle 7 index
- `https://docs.neoforged.net/toolchain/docs/` — toolchain features
- `https://parchmentmc.org/docs/getting-started` — Parchment integration

## Live signals

- `https://neoforged.net/news/` — advisories & release announcements
- `https://neoforged.net/changelog/` — recent version-by-version changelog
- `https://projects.neoforged.net/neoforged/neoforge` — project status per MC version
