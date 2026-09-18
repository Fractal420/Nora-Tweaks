# Nora Tweaks

QoL modules, commands, and tweaks for [Meteor Client](https://meteorclient.com).

## Supported versions

| Version | Status | Java | Notes |
|---------|--------|------|-------|
| **26.2** | Full | 25 | Default / active |
| **26.1.2** | Full | 25 | |
| **1.21.11** | Partial | 21 | Build script ready; needs more API ports (not in `buildAll`) |

Multi-version uses [Stonecutter](https://stonecutter.kikugie.dev/). Shared sources in `src/`.

## Build

```bash
./gradlew build              # active (26.2)
./gradlew :26.2:build
./gradlew :26.1.2:build
./gradlew buildAll           # 26.1.2 + 26.2

# 1.21.11 (experimental — will fail until more //? gates are added)
./gradlew :1.21.11:build
```

JARs: `versions/<mc>/build/libs/`

### 1.21.11 status

The sources target 26.x Meteor/MC APIs. Remaining 1.21.11 gaps include:

- Command API (`ClientSuggestionProvider` vs `SharedSuggestionProvider`)
- `KeyInputEvent`, `ContainerInput`, `FarmlandBlock`, attack packets
- Baritone mixin intermediate names under official mappings

Cubiomes is 26.x only (JVM 23+).

## License

CC0 1.0 Universal
