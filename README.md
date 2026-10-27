<div align="center">

<h1>Mace Helper</h1>
<hr>

**Client-side Fabric mod that recolors the vanilla mace and shows its attack cooldown**

![Minecraft](https://img.shields.io/badge/MINECRAFT-1.21.11-62B47A?style=flat-square&logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/FABRIC_LOADER-0.17.3-DBD0B4?style=flat-square)
![Java](https://img.shields.io/badge/JAVA-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Release](https://img.shields.io/github/v/release/hhhhhffd/mace-helper?style=flat-square)
![License](https://img.shields.io/github/license/hhhhhffd/mace-helper?style=flat-square)

[Download](https://github.com/hhhhhffd/mace-helper/releases) · [Demo](https://youtu.be/tEFYyc4sGNY) · [Build](#build) · [License](#license)

</div>

> [!NOTE]
> Archived — no longer maintained.

[![Mace Helper demo on YouTube](https://img.youtube.com/vi/tEFYyc4sGNY/hqdefault.jpg)](https://youtu.be/tEFYyc4sGNY)

## What it is

Mace Helper tints the vanilla mace and switches its color after a successful hit, fading back over a configurable time. The same timer blocks your own mace attacks while it runs and fills the vanilla cooldown indicator in the hotbar slot.

## Features

- Two configurable colors in `#RRGGBB` format.
- After a hit the mace turns the second color and fades back to the first.
- One shared transition and cooldown timer per player.
- Local block on mace attacks while the timer runs.
- Vanilla cooldown indicator in the hotbar slot.
- Settings screen through the bundled Mod Menu; Fabric API and Mod Menu are included in the JAR.

The mod is fully client-side. On a server without the mod, blocking other players' attacks cannot be guaranteed.

## Configuration

`config/mace_helper.json`:

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | turns the mod on or off |
| `first_color` | `#00FF00` | base mace color |
| `second_color` | `#FF0000` | color right after a hit |
| `duration_seconds` | `3.0` | transition and attack-block duration |

## Install

Download the JAR from the [releases page](https://github.com/hhhhhffd/mace-helper/releases) (release `1.0.0`, asset `mace-helper.jar`) and put it into `mods/` for Minecraft 1.21.11 with Fabric Loader 0.17.3 or newer. Remove any older Mace Helper JAR first. The current source is version 1.0.1; to get it, build from source.

## Build

```bash
./gradlew build
```

The JAR is `build/libs/mace-helper-1.0.1.jar`. The `-sources.jar` file is for reading the source, not for installing.

## License

MIT, see [LICENSE](LICENSE).
