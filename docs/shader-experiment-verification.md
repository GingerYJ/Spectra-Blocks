# Shader 实验配置、兼容性与验证清单草案

本文用于 `shader-experiment` 分支的 shader 实验收口检查。目标是让 shader 路径可以被安全试用，同时保证默认启动、外部光影环境和异常驱动环境都能回到现有 Tessellator 渲染路径。

## 配置原则

建议新增配置项：

```properties
enableShaderEffects=false
```

`enableShaderEffects` 在实验分支中应默认 `false`，原因如下：

- 当前 shader 路径是实验实现，不应在玩家首次启动或整合包默认配置中改变既有视觉表现。
- Minecraft 1.12.2 的 OpenGL 状态较脆弱，自定义 shader 如果遗漏状态恢复，可能影响后续 TESR、物品、GUI 或世界渲染。
- OptiFine、外部 shader pack、显卡驱动和 Java/LWJGL 环境差异较大，默认开启会扩大兼容性风险。
- 现有 Tessellator 路径已经是稳定 fallback，默认关闭能保证实验失败时用户体验与稳定分支一致。
- 实验效果需要逐项对比 FPS、画面一致性和日志表现，适合由开发者或测试者显式 opt-in。

建议 `ModConfig.java` 注释文案保持中英双语：

```java
"是否启用实验性 shader 特效。默认关闭；关闭或 shader 不可用时会使用现有 Tessellator 渲染路径。 / Enables experimental shader effects. Disabled by default; when disabled or unavailable, the existing Tessellator renderer is used."
```

## OptiFine 与外部光影包风险

已知或需要重点验证的风险：

- OptiFine shader pack 通常会接管世界渲染管线，自定义 `glUseProgram` 可能与其 program 绑定、uniform 状态或 framebuffer 假设冲突。
- 外部 shader pack 可能改变深度、透明混合、光照贴图、纹理单元和 framebuffer 状态，导致实验 shader 颜色异常、透明层排序错误或完全不可见。
- 部分 shader pack 会使用多渲染阶段和自定义后处理，自定义 TESR shader 如果没有完整恢复 `activeProgram`、纹理绑定、blend、depth mask、cull、line width 等状态，会污染后续画面。
- OptiFine 动态光照、Fast Render、Antialiasing、Render Regions 等选项可能改变 GL 能力或状态缓存，不应假设 vanilla/Forge 渲染状态。
- 老显卡或旧驱动可能编译失败 GLSL 语法、precision、内置变量或扩展能力；失败必须只记录日志并进入 fallback。

兼容性建议：

- 首轮实验说明中标注“不保证兼容 OptiFine shader pack”，并要求用户先在无 OptiFine/无外部光影环境验证。
- 检测到 shader 编译、链接或运行期绑定失败时，将当前效果实例标记为 shader unavailable，避免每帧重复刷日志。
- 日志中输出 shader 资源名、编译/链接阶段和简短错误，不输出过长重复堆栈。
- shader 渲染结束后必须恢复进入前的 GL program 与关键渲染状态；异常路径也要恢复。

## Fallback 流程

推荐流程：

1. 读取配置：`enableShaderEffects=false` 时直接使用现有 Tessellator 路径。
2. 配置开启后，初始化 shader 管线并加载对应 `.vsh` / `.fsh` 资源。
3. 若资源缺失、编译失败、链接失败、uniform 初始化失败或 OpenGL 能力不足，记录一次日志并设置 capability flag 为不可用。
4. renderer 每次渲染前同时检查配置开关和 capability flag。
5. 检查失败时调用原有 renderer 路径，视觉效果应与 shader 实验前一致。
6. shader 渲染中如捕获可恢复异常，立即恢复 GL 状态，禁用该 shader path，并从下一帧开始 fallback。
7. fallback 不改变 block ID、item ID、NBT、语言键、配方或 TileEntity 数据。

建议伪代码：

```java
if (!ModConfig.enableShaderEffects() || !ShaderEffects.isUsable(effectId)) {
    renderLegacyTessellatorPath(...);
    return;
}

if (!ShaderEffects.render(effectId, ...)) {
    renderLegacyTessellatorPath(...);
}
```

## 测试步骤

基础构建：

- 运行 `gradlew.bat compileJava`。
- 运行 `gradlew.bat build`。
- 确认默认配置生成后 `enableShaderEffects=false`。

默认关闭验证：

- 使用全新配置启动客户端。
- 放置 Micro Stellar Source 与 Micro Universe。
- 确认画面与实验前 Tessellator 路径一致。
- 检查日志中没有 shader 编译、链接或运行期错误。

显式开启验证：

- 将 `config/spectrablocks.cfg` 中 `enableShaderEffects=true`。
- 启动客户端并进入测试世界。
- 观察 Micro Stellar Source 的 shader 原型是否正常显示，脉冲、边缘光和时间变化是否连续。
- 观察 Micro Universe 外壳或星场 shader 原型是否正常显示，原有行星和轨道逻辑不应改变。
- 远离并靠近方块，确认渲染距离裁剪仍生效。
- 反复切换维度、打开 GUI、切换第三人称、手持物品观察，确认没有 GL 状态污染。

破损 shader 验证：

- 临时制造一个 `.fsh` 或 `.vsh` 语法错误。
- 启动客户端或重新加载资源。
- 确认客户端不崩溃。
- 确认对应效果回到 Tessellator fallback。
- 确认日志只记录可读的编译/链接失败信息，不每帧刷屏。
- 恢复 shader 文件后重新运行构建。

兼容性矩阵：

- 无 OptiFine、无外部光影包：必须通过。
- OptiFine 已安装但 shader pack 关闭：记录表现，若异常则默认建议关闭实验 shader。
- OptiFine shader pack 开启：仅作为风险记录，不作为首轮发布阻塞项；若冲突，应 fallback 或提示用户关闭 `enableShaderEffects`。
- 不同显卡供应商或驱动版本：至少记录 GPU、驱动、Java 版本和是否 fallback。

性能观察：

- 单个 Micro Stellar Source 放置后记录 FPS 与日志。
- 单个 Micro Universe 放置后记录 FPS 与日志。
- 多个效果方块同屏时观察是否出现明显卡顿或 shader 重复初始化。
- shader path 不应比现有优化后的稳定路径出现明显回退；若回退，应继续默认关闭。

## 回退步骤

用户侧回退：

1. 关闭游戏。
2. 打开 `config/spectrablocks.cfg`。
3. 设置 `enableShaderEffects=false`。
4. 重新启动客户端。
5. 若仍有问题，移除实验构建并切回稳定构建。

开发侧回退：

1. 确认当前工作区没有其他 agent 的未提交改动需要保留。
2. 在 `shader-experiment` 分支上只回退 shader 实验相关提交，不回退无关改动。
3. 优先回退最近的小提交，例如 `Document shader experiment configuration`、shader 原型提交或 shader infrastructure 提交。
4. 如需恢复到实验前状态，以 `backup/pre-shader-experiment-20260617` tag 或 base commit `3d2ba7b` 作为参照。
5. 回退后再次运行 `gradlew.bat compileJava` 和 `gradlew.bat build`。

## GitHub tag / branch 策略

- 稳定源码目录 `CleanroomModTemplate-mixin` 保持不改，shader 实验只在 `Spectra-Blocks-shader-experiment` 工作区进行。
- `shader-experiment` 分支用于所有 shader 实验提交，不直接合入稳定分支。
- 已有备份 tag：`backup/pre-shader-experiment-20260617`，用于标记 shader 实验前状态。
- 建议在阶段性可运行时打轻量或带注释 tag，例如：
  - `shader-experiment/infrastructure-ready`
  - `shader-experiment/stellar-prototype`
  - `shader-experiment/universe-shell-prototype`
  - `shader-experiment/docs-verification`
- 若准备向稳定分支合并，应先创建候选 tag，例如 `shader-experiment/merge-candidate-YYYYMMDD`，并在 PR 或合并说明中列出验证矩阵。
- 不建议把实验 tag 当作发布 tag；正式发布 tag 应只指向已通过默认关闭、fallback 和兼容性验证的提交。

## 合入前检查清单

- [ ] `enableShaderEffects` 默认值为 `false`。
- [ ] 配置注释为中英双语，并明确 fallback 行为。
- [ ] 配置关闭时不初始化或不使用 shader 渲染路径。
- [ ] shader 编译/链接失败不崩溃。
- [ ] shader 资源缺失不崩溃。
- [ ] renderer 可无条件回到现有 Tessellator 路径。
- [ ] shader path 完整恢复 OpenGL program、纹理、blend、depth mask、cull、line width 等状态。
- [ ] Micro Stellar Source fallback 视觉可接受。
- [ ] Micro Universe fallback 视觉可接受。
- [ ] OptiFine / 外部 shader pack 风险已写入说明。
- [ ] `gradlew.bat compileJava` 通过。
- [ ] `gradlew.bat build` 通过。
- [ ] 回退说明包含配置回退、提交回退和 tag 参照。
