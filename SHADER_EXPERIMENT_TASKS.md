# Spectra Blocks Shader Experiment Tasks

This workspace is a separate shader experiment copy based on:

- Source directory: `CleanroomModTemplate-mixin`
- Experiment directory: `Spectra-Blocks-shader-experiment`
- Branch: `shader-experiment`
- Base commit: `3d2ba7b Cache sphere meshes and remove universe halos`
- GitHub backup tag: `backup/pre-shader-experiment-20260617`

## Goal

Explore shader-based rendering for selected Spectra Blocks visual effects while keeping the existing Tessellator renderers as safe fallbacks.

The first implementation target should be an experimental shader path, not a hard replacement for all existing renderers.

## Non-Negotiable Requirements

- Keep existing non-shader rendering as fallback.
- Shader compile/link failure must not crash the client.
- Add a config switch to enable or disable shader effects.
- Keep OpenGL state restoration strict after shader rendering.
- Do not change block IDs, item IDs, recipes, language keys, or NBT format.
- Do not modify the stable source directory while working in this experiment copy.
- Prefer small, independently revertible commits.

## Task Groups

### 1. Shader Infrastructure

Owner scope:

- `src/main/java/com/gingeryj/spectrablocks/client/render/shader/`
- `src/main/resources/assets/spectrablocks/shaders/`
- minimal config additions in `ModConfig.java`

Deliverables:

- A small shader program wrapper for Minecraft 1.12 / LWJGL OpenGL.
- Resource loading for `.vsh` and `.fsh` files from the mod asset domain.
- Uniform helpers for `float`, `vec2`, `vec3`, `vec4`, and matrices if needed.
- Safe compile/link log output.
- A capability flag so renderers can ask whether shader effects are usable.
- A config option such as `enableShaderEffects`, defaulting to `false` for the experiment branch.

Validation:

- `compileJava` passes.
- Shader disabled path changes no visuals.
- Broken shader source falls back without crashing.

### 2. Micro Stellar Source Shader Prototype

Owner scope:

- `RenderMicroStellarSource.java`
- new shader files for the stellar source
- helper calls into shader infrastructure

Rendering target:

- Keep the current billboard particles as-is or lightly reused.
- Replace or augment the large stellar texture sphere with a shader-driven plasma surface.
- Use uniforms for time, base color, rim intensity, pulse amount, and noise speed.
- If shader support is disabled or unavailable, call the existing renderer path.

Validation:

- `compileJava` passes.
- FPS should stay close to or better than the current optimized stellar source.
- Visual should still read as an active stellar energy source.

### 3. Micro Universe Shader Prototype

Owner scope:

- `RenderMicroUniverse.java`
- new shader files for the universe shell or starfield
- helper calls into shader infrastructure

Rendering target:

- Do not rewrite the solar-system logic first.
- Start with the outer universe shell / starfield, because that is a contained layer.
- Preserve the current planet and orbit rendering for the first prototype.
- Shader path should be optional and fallback to the current shell.

Validation:

- `compileJava` passes.
- FPS should improve or at least not regress with one micro universe placed.
- Visual should still read as black outer space containing a miniature solar system.

### 4. Compatibility, Config, and Verification

Owner scope:

- `ModConfig.java`
- docs
- final integration checks
- optional small render helper changes

Deliverables:

- Config comments in Chinese and English.
- Clear docs explaining shader mode, fallback mode, and known compatibility risks.
- Build verification.
- A short rollback note listing commits and tags.

Validation:

- `compileJava` and `build` pass.
- Default config should be conservative.
- Worktree is clean after integration.

## Suggested Commit Plan

1. `Add shader experiment infrastructure`
2. `Add stellar source shader prototype`
3. `Add micro universe shell shader prototype`
4. `Document shader experiment configuration`

Each commit should be pushed to the `shader-experiment` branch.

## Known Risks

- OptiFine or external shader packs may conflict with custom OpenGL shader usage.
- OpenGL state leaks can corrupt later TESR, item, GUI, or world rendering.
- Minecraft 1.12 render state is fragile; always restore texture, blend, depth mask, culling, active program, and line width.
- Shader support differs by GPU and driver, so fallback is required.
