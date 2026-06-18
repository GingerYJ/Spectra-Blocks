# Shader-Only Verification Checklist

This checklist is for the current `shader-experiment` branch after the project moved to shader-only visual effects.

## Required Behavior

- Visual effect blocks render through shader-driven TESR code.
- Legacy texture-sphere fallback is not used.
- Minecraft particle-system effects are not used.
- If a shader path fails, the effect may stop rendering or shaders may be disabled. It must not silently switch to a legacy texture fallback.
- All OpenGL state changes must be restored enough to avoid corrupting later world, item, GUI, or TESR rendering.

## Build Checks

Run:

```bat
gradlew.bat compileJava
gradlew.bat build
```

Both commands must pass before pushing.

## Old Path Scan

Run:

```powershell
Get-ChildItem -Path src\main\java\com\gingeryj\spectrablocks -Recurse -File |
  Select-String -Pattern 'bindTexture','getTextureManager','drawTexturedSphere','spawnParticle','new Particle','effectRenderer.addEffect' -SimpleMatch
```

Expected result:

- No real old render path.
- Mentions of `ArcaneShaderEffectRenderer` are not particle-system usage and should not be treated as a failure.

## Line Primitive Scan

Run:

```powershell
Get-ChildItem -Path src\main\java\com\gingeryj\spectrablocks\client\render -File |
  Select-String -Pattern 'GL11.GL_LINES','GL11.GL_LINE_LOOP','GL11.GL_LINE_STRIP' -SimpleMatch
```

Expected result:

- No non-frozen effect should rely on these primitives for visible lines.
- `RenderMicroUniverse.java` is currently an accepted exception because the user confirmed the effect is complete.

## Resource Scan

Run:

```powershell
$textureRoot = (Resolve-Path src\main\resources\assets\spectrablocks\textures).Path
$modelFiles = Get-ChildItem -Path src\main\resources\assets\spectrablocks\models -Recurse -File
$modelText = ''
foreach ($model in $modelFiles) {
  $modelText += (Get-Content $model.FullName -Raw) + "`n"
}
foreach ($file in Get-ChildItem -Path $textureRoot -Recurse -File) {
  $rel = $file.FullName.Substring($textureRoot.Length + 1).Replace('\','/')
  $id = $rel -replace '\.png$',''
  $resource = 'spectrablocks:' + $id
  [PSCustomObject]@{
    Texture = $rel
    UsedByModel = $modelText.Contains($resource)
    Size = $file.Length
  }
}
```

Expected result:

- Block and item placeholder PNG files may still be required by models.
- Do not delete a PNG unless `UsedByModel` is false and no Java/resource reference exists.

## Runtime Visual Checks

For each changed effect:

- Place the block in a clean test world.
- Observe from multiple angles.
- Move close and far away.
- Open inventory/GUI and return to world view.
- Confirm there are no obvious seam lines, missing layers, or GL state leaks.
- Confirm `RenderScale` still changes the whole effect size.

## Push Checklist

Before pushing:

- `build` passed.
- Worktree contains only intended files.
- The commit message describes the exact batch.
- The branch is pushed to `origin shader-experiment`.
