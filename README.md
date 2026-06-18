# Spectra Blocks

Spectra Blocks is a Cleanroom/Forge 1.12.2 mod by GingerYJ. It adds shader-driven visual effect blocks for builders, map makers, showcase scenes, rituals, energy cores, and decorative machines.

## Current Status

This branch is the shader-only development branch.

- Visual effects are rendered by shader-based TESR code.
- Legacy texture-sphere fallback rendering is not used.
- Minecraft particle-system effects are not used for effect blocks.
- `RenderScale` is the only block NBT used for render scaling.
- Every verified batch is pushed to `origin shader-experiment` for rollback.

## Included Effects

The mod currently contains a large visual-effect block set, including:

- Micro Singularity / 微型黑洞
- Micro White Hole / 微型白洞
- Micro Universe / 微缩宇宙
- Micro Stellar Source / 微缩恒星源
- Energy Nexus, Ether Reactor Core, Neon Circuit Core
- Miniature Galaxy, Nebula Core, Wormhole, Spatial Rift
- Void Crystal, Arcane Star Ring, Astral Altar Core
- Plasma Storm, Quantum Bubble, Data Stream Matrix
- Aurora Veil, Stardust Fountain, Storm Core
- Soundwave Resonator, Thermal Distortion Field, Alchemy Transmutation Ring

The four micro effects above are currently user-approved and treated as frozen unless explicitly requested otherwise.

## Configuration

After first launch, edit:

```text
config/spectrablocks.cfg
```

Important options:

```properties
visualTileEntityRenderDistance=32
microSingularityScale=1.0
microWhiteHoleScale=1.0
microUniverseScale=1.0
microStellarSourceScale=1.0
```

Notes:

- Visual effect rendering is shader-only and has no enable/disable config switch.
- `visualTileEntityRenderDistance=32` renders visual TileEntities within 32 blocks.
- Render scale values are clamped from `0.01` to `50.0`.
- Block NBT `RenderScale` can override individual placed block scale.

## Documentation

- [Shader-only remake plan](docs/shader-only-remake-plan.md)
- [Shader-only verification checklist](docs/shader-experiment-verification.md)
- [Visual effect block implementation notes](docs/visual-effect-blocks.md)

## Development

Compile:

```bat
gradlew.bat compileJava
```

Build:

```bat
gradlew.bat build
```

Run a client from Gradle:

```bat
gradlew.bat runClient
```
