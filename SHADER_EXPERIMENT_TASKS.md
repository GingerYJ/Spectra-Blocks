# Spectra Blocks Shader-Only Tasks

This workspace is the shader-only development copy for Spectra Blocks.

- Source template: `CleanroomModTemplate-mixin`
- Active workspace: `Spectra-Blocks-shader-experiment`
- Branch: `shader-experiment`
- Remote: `GingerYJ/Spectra-Blocks`
- Backup tag before the shader experiment: `backup/pre-shader-experiment-20260617`

## Current Direction

The experiment branch has moved from “optional shader prototype with fallback” to “shader-only visual effects”.

Current rules:

- Visual effects are rendered through shader-driven TESR paths.
- Legacy texture-sphere fallback paths are not kept.
- Minecraft particle-system effects are not used for these blocks.
- `RenderScale` remains the only block NBT used for render scaling.
- Per-effect config scale and render distance config are kept.
- Each verified batch is committed and pushed to `origin shader-experiment`.

## Frozen Effects

These effects are considered visually accepted and should not be changed unless the user explicitly asks:

- `micro_singularity` / Micro Singularity / 微型黑洞
- `micro_white_hole` / Micro White Hole / 微型白洞
- `micro_universe` / Micro Universe / 微缩宇宙
- `micro_stellar_source` / Micro Stellar Source / 微缩恒星源

## Completed Checkpoints

- `90ff9fe Convert matrix and refraction lines to shader ribbons`
- `1da78b1 Add resonance thermal and alchemy effects`
- `893a07a Convert shader effect lines to ribbons`

## Remaining Work

### 1. Java Render Audit

Check for accidental old paths:

- `bindTexture`
- `getTextureManager`
- `drawTexturedSphere`
- `spawnParticle`
- `new Particle`
- `effectRenderer.addEffect`

Current expected result: no real old rendering path should be present.

### 2. Line Primitive Cleanup

Most active effects have been moved from OpenGL line primitives to triangle ribbons.

Known exception:

- `RenderMicroUniverse.java` still uses several `GL_LINE_*` calls. This file is frozen because the user confirmed the micro-universe effect is complete.

Do not modify frozen effects during broad cleanup.

### 3. Resource Cleanup

PNG files under `textures/blocks` are currently used by block/item models as inventory and block placeholder textures. They are not shader fallback textures.

Only delete PNG files after proving they are not referenced by:

- block models
- item models
- blockstates
- Java code

### 4. Documentation Cleanup

Documents must no longer claim that shader fallback is required. The current intended failure behavior is:

- Shader render succeeds: effect renders.
- Shader render fails: no fallback path is used.
- The error is logged or shaders are disabled by `ShaderManager`.

## Validation Commands

Compile:

```bat
gradlew.bat compileJava
```

Full build:

```bat
gradlew.bat build
```

Old path scan:

```powershell
Get-ChildItem -Path src\main\java\com\gingeryj\spectrablocks -Recurse -File |
  Select-String -Pattern 'bindTexture','getTextureManager','drawTexturedSphere','spawnParticle','new Particle','effectRenderer.addEffect' -SimpleMatch
```

Line primitive scan:

```powershell
Get-ChildItem -Path src\main\java\com\gingeryj\spectrablocks\client\render -File |
  Select-String -Pattern 'GL11.GL_LINES','GL11.GL_LINE_LOOP','GL11.GL_LINE_STRIP' -SimpleMatch
```

## Commit Policy

- Keep documentation-only changes separate from render changes when practical.
- Keep resource deletion separate from render changes.
- Run `build` before pushing.
- Push every verified batch to `origin shader-experiment`.
