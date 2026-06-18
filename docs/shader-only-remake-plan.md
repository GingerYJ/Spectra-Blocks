# Spectra Blocks Shader-Only Remake Plan

本文档记录 Spectra Blocks 视觉特效方块的 shader-only 重制计划、当前状态、agent 分工和清理规则。

## 当前目标

- 所有特效方块的动态效果都使用 shader 渲染。
- 不保留贴图球体 fallback，不保留“shader 失败后改用旧渲染”的路径。
- 不使用 Minecraft 粒子系统制作方块特效。
- 方块级 NBT 只保留 `RenderScale`，用于整体缩放。
- 配置文件保留渲染距离控制，默认 32 格。
- 每完成一批可验证改动就提交并推送到 `origin shader-experiment`，方便回退。

## 已确认完成并冻结

以下效果已经由用户确认效果完成。除非出现编译错误、公共 shader 接口变化，或用户明确要求，否则不主动重制：

- `micro_singularity` / 微型黑洞
- `micro_white_hole` / 微型白洞
- `micro_universe` / 微缩宇宙
- `micro_stellar_source` / 微缩恒星源

说明：`RenderMicroUniverse.java` 仍有少量 `GL_LINE_*` primitive，用于已确认完成的宇宙轨道/星线表现。本阶段按用户确认结果保留，不再主动修改。

## 当前审计结论

截至 `893a07a Convert shader effect lines to ribbons`：

- Java 旧路径扫描未发现 `bindTexture`、`getTextureManager`、`drawTexturedSphere`、`spawnParticle`、`new Particle`、`effectRenderer.addEffect`。
- 多数可见线条已经从 `GL_LINES / GL_LINE_LOOP / GL_LINE_STRIP` 改为三角 ribbon 或面片。
- `ArcaneShaderEffectRenderer` 是本项目内部 shader helper，不是 Minecraft 粒子系统。
- `ShaderManager.disableShaders(...)` 仍会在 shader 编译或渲染异常后禁用 shader。由于目标是不保留 fallback，这种失败表现为不渲染，而不是回退到贴图/粒子路径。

## Agent 分工

### Agent A：Java 渲染路径审计

负责范围：

- `src/main/java/com/gingeryj/spectrablocks/client/render`
- `src/main/java/com/gingeryj/spectrablocks/client/render/shader`
- 所有 `TileEntitySpecialRenderer` 和公共 shader helper

检查项：

- 是否仍存在贴图绑定、旧球体贴图绘制、MC 粒子系统调用。
- 是否还有非冻结方块直接使用 `GL_LINES / GL_LINE_LOOP / GL_LINE_STRIP`。
- `ShaderManager.disableShaders(...)` 的失败行为是否仍符合 shader-only 目标。

### Agent B：资源与 PNG 审计

负责范围：

- `src/main/resources/assets/spectrablocks/textures`
- `src/main/resources/assets/spectrablocks/models`
- `src/main/resources/assets/spectrablocks/blockstates`
- `src/main/resources/assets/spectrablocks/recipes`

检查项：

- `textures/blocks/*.png` 是否仍被 block/item model 引用。
- `textures/items/effect_configurator.png` 是否仍被工具物品模型引用。
- 是否存在旧 `textures/effects` 或不再被任何资源引用的 PNG。

删除规则：

- 只有确认没有任何模型、物品、方块状态或代码引用的 PNG 才能删除。
- 当前大多数 blocks PNG 是方块/物品占位模型所需资源，即使动态效果由 shader 渲染，也不能直接删除，否则物品栏/手持/方块模型会丢纹理。

### Agent C：文档与计划审计

负责范围：

- `README.md`
- `SHADER_EXPERIMENT_TASKS.md`
- `docs/*.md`
- `src/main/resources/mcmod.info`

检查项：

- 文档是否乱码。
- 文档是否仍提到已经删除的 fallback 方案。
- 是否准确记录冻结项、当前提交点和后续可回退策略。

## 剩余重制策略

### 1. 冻结项保护

不主动修改：

- 微型黑洞
- 微型白洞
- 微缩宇宙
- 微缩恒星源

如后续用户指出视觉问题，只针对指定方块做局部修改，并单独提交。

### 2. 非冻结项继续清理

后续优先检查这些风险：

- `glLineWidth(...)` 状态设置是否已经没有实际意义，可以逐步删除。
- 是否有 helper 名称、注释或文档仍描述 fallback。
- 是否有 shader 失败提示文字与“无 fallback”目标不一致。
- 是否有重复的 ribbon 几何 helper 可以收敛到 `RenderHelper`，但不为抽象而抽象，避免大范围重构。

### 3. 资源清理

当前不按文件名批量删除 PNG。正确流程是：

1. 先扫描所有模型和代码引用。
2. 确认资源没有被 block/item model 使用。
3. 删除资源。
4. 运行 `.\gradlew.bat build`。
5. 提交并推送。

### 4. 新增其他类型特效

剩余 shader-only 清理完成后，再新增非天体类效果。优先方向：

- 声音/共振类：声波共振器已添加，可继续扩展音叉、回声井、静默核心。
- 热/环境类：热扰动场已添加，可继续扩展寒霜折射、热浪柱、熔流核心。
- 炼金/仪式类：炼金转化环已添加，可继续扩展符文坩埚、光谱祭坛、元素置换阵。
- 机械/能源类：可继续做反应堆护罩、能量阀门、矩阵蓄能核心。

## 验证命令

每批代码修改至少运行：

```powershell
.\gradlew.bat compileJava
```

每批准备推送前运行：

```powershell
.\gradlew.bat build
```

旧路径扫描：

```powershell
Get-ChildItem -Path src\main\java\com\gingeryj\spectrablocks -Recurse -File |
  Select-String -Pattern 'bindTexture','getTextureManager','drawTexturedSphere','spawnParticle','new Particle','effectRenderer.addEffect' -SimpleMatch
```

line primitive 扫描：

```powershell
Get-ChildItem -Path src\main\java\com\gingeryj\spectrablocks\client\render -File |
  Select-String -Pattern 'GL11.GL_LINES','GL11.GL_LINE_LOOP','GL11.GL_LINE_STRIP' -SimpleMatch
```

## 提交策略

- 文档修复单独提交。
- 渲染器重制按视觉类型分批提交。
- 资源删除单独提交。
- 每次提交后推送到 `origin shader-experiment`。
- 用户确认某个效果完成后，将其加入冻结项。
