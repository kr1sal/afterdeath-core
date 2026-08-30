---
name: neoforge-docs
description: Look up authoritative info from the NeoForge 1.21.1 documentation and cross-check the current project against it. Use whenever the user asks about a NeoForge API, feature, config field, event, registry, data-gen setup, gradle/parchment version, mods.toml key, or asks to "check the docs" for this mod project. Fetches live pages from docs.neoforged.net (pinned to the 1.21.1 branch), the versioning primer, and the NeoForged Maven, then reports what the docs say vs. what the project actually has.
---

# NeoForge 1.21.1 Docs Checker

Answer NeoForge questions from **live documentation**, not memory. The 21.1.x line is closed at `21.1.248` (final for MC 1.21.1) and the docs site keeps a per-version branch, so guesses drift fast. Always fetch before answering.

## When to invoke

- The user asks anything about a NeoForge API, event, registry, capability, data-gen entry point, network packet, config, `neoforge.mods.toml` field, gradle config, or JDK/Parchment pin.
- The user says "check the docs", "актуализируй", "verify", "is this still correct", or references the NeoForge changelog / a version bump.
- Before advising a code change that touches NeoForge classes — confirm the API is still the shape the docs describe on the **1.21.1** branch.

Skip only when the question is pure Java / Minecraft-vanilla / unrelated Gradle.

## Sources of truth (fetch, don't recall)

Use `WebFetch` on these. Prefer the version-pinned URL when it exists — the docs site auto-redirects `/docs/...` to the latest supported branch, which is **not** always 1.21.1.

| Topic | URL |
|---|---|
| Doc index & category map | `references/doc-index.md` (this skill) |
| Getting started, gradle setup | `https://docs.neoforged.net/docs/gettingstarted/` |
| `neoforge.mods.toml` reference | `https://docs.neoforged.net/docs/gettingstarted/modfiles` |
| Structuring your mod | `https://docs.neoforged.net/docs/gettingstarted/structuring` |
| Versioning scheme | `https://docs.neoforged.net/docs/gettingstarted/versioning` |
| Concepts (events, sides, registries…) | `https://docs.neoforged.net/docs/concepts/sides` etc. |
| Primers (per-version migration notes) | `https://docs.neoforged.net/primer/docs/` |
| Latest news / security advisories | `https://neoforged.net/news/` |
| NeoForge Maven — 21.1.x list | `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml` |
| Parchment Maven — 1.21.1 mappings | `https://ldtteam.jfrog.io/artifactory/parchmentmc-public/org/parchmentmc/data/parchment-1.21.1/maven-metadata.xml` |
| Project Listing (all versions/status) | `https://projects.neoforged.net/neoforged/neoforge` |

If a page 404s, fall through to `https://docs.neoforged.net/` and re-navigate — the site has moved pages across the 21.x → 26.x transition.

Broader/older lookups: use `WebSearch` scoped with `site:docs.neoforged.net` or `site:neoforged.net`.

## Workflow

1. **Detect what the user is asking.** Route to one of:
   - *Version / dependency check* → Phase A
   - *API / concept lookup* → Phase B
   - *Full project sweep* ("check everything") → Phase A + B + C

2. **Phase A — Version & toolchain check.**
   Read `gradle.properties` and compare against:
   - Latest 21.1.x on NeoForged Maven (recorded in `references/versions.md`).
   - Latest Parchment release for 1.21.1.
   - Required JDK (21 for MC 1.21.1).
   Report: current value → latest → verdict (`up-to-date` / `behind by N` / `newer than recorded, refresh this skill`).

3. **Phase B — API/concept lookup.**
   Fetch the specific docs page (see `references/doc-index.md`). Quote the exact field name / method signature / event class from the page. If the project has code touching that API, open the file and check it matches — surface the file + line where it diverges.

4. **Phase C — Project sweep.**
   Walk the checklist in `references/checklist.md`. Report each item as `OK` / `ISSUE: …` with a file:line and the doc URL the check comes from.

5. **Output shape.** Short table or bulleted list. For each finding include:
   - What the docs say (quoted or paraphrased with URL).
   - What the project has (file:line).
   - Verdict + suggested change if any.

Do **not** invent method names, event classes, or `mods.toml` keys — if the fetched page doesn't confirm it, say so and stop.

## Guardrails

- Anchor every claim to a URL you fetched *this turn*. Cached knowledge about NeoForge from before the pinned date in `references/versions.md` is not authoritative.
- If the user asks about a version other than 1.21.1 (e.g. 1.21.4, 26.1), tell them this skill is 1.21.1-scoped and the doc site's `/docs/` prefix will silently redirect — pin the URL to the correct primer.
- Never touch project files as part of a "check". This skill reads. If a fix is needed, describe it and let the user (or a follow-up task) apply it.
- If a fetch fails, say so and name the URL — don't paper over it with recalled content.

## Arguments (optional)

The skill accepts a free-form topic:

- `/neoforge-docs` — run Phase A + C (versions + project sweep).
- `/neoforge-docs registration` — Phase B focused on registries / DeferredRegister.
- `/neoforge-docs events` — Phase B focused on the event bus / `@SubscribeEvent`.
- `/neoforge-docs datagen` — Phase B focused on `GatherDataEvent` and generators.
- `/neoforge-docs versions` — Phase A only.
- `/neoforge-docs <anything>` — treat as a docs search term.
