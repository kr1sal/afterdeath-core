# Pinned reference versions (NeoForge 1.21.1)

Last verified against upstream: **2026-08-18**.

Re-fetch the Maven metadata (see SKILL.md) if the pin is stale — 21.1.x is closed but Parchment could still tick, and the "verified" date is the only signal that this file is current.

## What the project should have

| Field (`gradle.properties`) | Value | Notes |
|---|---|---|
| `minecraft_version` | `1.21.1` | Fixed for this project. |
| `minecraft_version_range` | `[1.21.1,1.22)` | Standard MC range for 1.21.1 mods. |
| `neo_version` | `21.1.248` | Final 21.1.x release; the series is closed — 21.1.248 is the terminal patch. Successor line is `21.11.x` for MC 1.21.11 and `26.x` for MC 26.x. |
| `neo_version_range` | `[21.1.0,)` | Open-ended; fine for 1.21.1 mods. |
| `loader_version_range` | `[4,)` | FML v4, current for 21.1.x. |
| `neogradle.subsystems.parchment.minecraftVersion` | `1.21.1` | |
| `neogradle.subsystems.parchment.mappingsVersion` | `2024.11.17` | Latest Parchment release for 1.21.1. |
| JDK (`org.gradle.java.home`) | JDK **21** | 1.21.x requires Java 21. Newer NeoForge (26.x) has moved to JDK 25 — do **not** apply that here. |

## Cross-check commands

```bash
# NeoForge — confirm 21.1.x still ends at 21.1.248
curl -s https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml \
  | grep -oE '<version>21\.1\.[0-9]+</version>' | tail

# Parchment — confirm latest 1.21.1 mapping release
curl -sL https://maven.parchmentmc.org/org/parchmentmc/data/parchment-1.21.1/maven-metadata.xml \
  | grep -oE '<(release|latest)>[^<]+' | head
```

If either lookup shows a value newer than what's pinned above, update this file **and** `gradle.properties`.

## Known upstream notes worth surfacing

- **NeoForge 26.1 released 2026-03-24** — new project template, JDK 25, 4-component version scheme. Not applicable to this 1.21.1 project; call it out only if the user is considering a jump.
- **Network vulnerability advisory published 2026-05-11** (see `https://neoforged.net/news/`) — verify whether the fix was backported to 21.1.x before advising anyone to stay on 21.1.248 in a shared/server context.
