# Spectra Blocks

Spectra Blocks is a Cleanroom/Forge 1.12.2 mod by GingerYJ. It adds animated visual effect blocks for builders, map makers, and showcase scenes.

## Features

- Micro Singularity
- Micro White Hole
- Micro Universe
- Micro Stellar Source
- Dedicated Spectra Blocks creative tab
- Per-effect render scale configuration

## Configuration

After first launch, edit:

```text
config/spectrablocks.cfg
```

Each visual effect block has its own render scale:

```properties
microSingularityScale=1.0
microWhiteHoleScale=1.0
microUniverseScale=1.0
microStellarSourceScale=1.0
```

Use `1.0` for the default size, `0.5` for half size, and `2.0` for double size.

## Development

Build with:

```bat
gradlew.bat build
```

Run a client from Gradle with:

```bat
gradlew.bat runClient
```
