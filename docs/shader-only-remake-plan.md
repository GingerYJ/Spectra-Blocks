# Spectra Blocks Shader-Only Remake Plan

本文档记录剩余视觉特效方块的 shader-only 重制范围、拆分方式和回退策略。

## 已完成且冻结的效果

以下效果已经由用户确认完成，后续重制不主动修改：

- `micro_singularity` / 微型黑洞
- `micro_white_hole` / 微型白洞
- `micro_universe` / 微缩宇宙
- `micro_stellar_source` / 微缩恒星源

这些效果只在出现编译错误、公共 shader 管线接口变化或用户明确要求时调整。

## 总目标

- 剩余特效方块全部改为 shader 渲染。
- 删除旧 Tessellator fallback 逻辑，不再保留“shader 失败则回退”的路径。
- 保留每个方块现有的 `RenderScale` NBT 作为唯一方块级渲染控制。
- 保留配置文件中的默认渲染距离控制，默认 32 格。
- 每完成一组效果就编译、提交并推送，形成可回退节点。
- 清理无用 PNG 和旧渲染辅助代码前必须先确认没有引用。

## 公共渲染策略

1. 使用少量通用 shader 承载同类效果：
   - `celestial_effect`：星系、星云、坍缩恒星、宇宙背景辐射场。
   - `space_effect`：空间裂隙、虫洞、引力透镜、维度星门、时间裂隙。
   - `magic_effect`：虚空水晶、奥术星环、星环祭坛核心、灵魂涡流、虚空莲、梦境碎片。
   - `tech_effect`：等离子风暴、量子泡、虚数立方、光谱棱镜、水晶折射场、数据流矩阵、能源中心。
   - `natural_effect`：星尘喷泉、极光帷幕、深海光核、雷暴核心、熵云、日冕喷发、星轨沙漏。
2. Java 渲染器只负责：
   - 设置位置、缩放、旋转、混合状态和少量几何体。
   - 向 shader 传入 `uTime`、`uEffect`、`uLayer`、颜色、透明度、强度、随机种子等参数。
3. 几何体优先复用公共缓存：
   - 球体：按分段缓存单位球顶点。
   - 平面：用于裂隙、星门、光幕、数据流。
   - 环/带：用于轨道、符文环、吸积盘、日冕弧。
4. 旧的逐点 CPU 粒子、线条、球壳尽量收敛为 shader 内部噪声、闪烁、扫描线、环带和 billboard 点。

## Agent 分工

### Ptolemy：天体与宇宙组

负责文件：

- `RenderMiniatureGalaxy.java`
- `RenderNebulaCore.java`
- `RenderCollapsingStar.java`
- `RenderCosmicBackgroundRadiationField.java`
- `celestial_effect.vsh`
- `celestial_effect.fsh`

效果要求：

- `miniature_galaxy`：核心发光、旋涡星臂、星点闪烁、少量拖尾。
- `nebula_core`：中心能量核、多层云雾翻涌、局部云团变亮。
- `collapsing_star`：暗核心、吸积环、内吸粒子、偶发坍缩闪爆。
- `cosmic_background_radiation_field`：淡色场域、细密噪声、温差斑点和慢速漂移。

### Confucius：空间与维度组

负责文件：

- `RenderSpatialRift.java`
- `RenderWormhole.java`
- `RenderGravitationalLens.java`
- `RenderDimensionalGate.java`
- `RenderTemporalRift.java`
- `space_effect.vsh`
- `space_effect.fsh`

效果要求：

- `spatial_rift`：不规则裂口、异色虚空、边缘闪烁、波纹噪声。
- `wormhole`：深色球形漩涡、发光环、向心粒子、轻微呼吸。
- `gravitational_lens`：透明力场、中心暗核、焦散弧线、慢速聚焦闪光。
- `dimensional_gate`：竖直星门、流动星空、符文边环旋转。
- `temporal_rift`：钟表碎片感、透明波纹、残影、倒转光环。

### Linnaeus：魔法与奥术组

负责文件：

- `RenderVoidCrystal.java`
- `RenderArcaneStarRing.java`
- `RenderAstralAltarCore.java`
- `RenderSoulVortex.java`
- `RenderVoidLotus.java`
- `RenderDreamShards.java`
- `magic_effect.vsh`
- `magic_effect.fsh`

效果要求：

- `void_crystal`：黑紫水晶、吞光核心、符文环、短电弧。
- `arcane_star_ring`：星核、淡金光晕、多层符文环、轨道星点。
- `astral_altar_core`：水平法阵、中心星火、层级旋转。
- `soul_vortex`：青绿色灵魂流绕中心上升。
- `void_lotus`：暗紫光片花瓣缓慢开合。
- `dream_shards`：彩色碎片漂浮，偶发短暂圆环。

### Popper：科技、能量与折射组

负责文件：

- `RenderPlasmaStorm.java`
- `RenderQuantumBubble.java`
- `RenderImaginaryCube.java`
- `RenderSpectralPrism.java`
- `RenderCrystalRefractionField.java`
- `RenderDataStreamMatrix.java`
- `RenderEnergyNexus.java`
- `tech_effect.vsh`
- `tech_effect.fsh`

效果要求：

- `plasma_storm`：蓝青等离子旋涡、短促电弧、高速闪烁。
- `quantum_bubble`：透明护罩、跳动网格、随机光点。
- `imaginary_cube`：透明立方框架错位、重叠、闪烁。
- `spectral_prism`：中心棱晶、彩色光束、折射纹。
- `crystal_refraction_field`：多层透明棱面、空气折射感。
- `data_stream_matrix`：竖向光符号下落、扫描线。
- `energy_nexus`：适合作为能源中心，中心球呼吸、能量流向核心、多层能量约束结构。

### Copernicus：自然、装饰与流体组

负责文件：

- `RenderStardustFountain.java`
- `RenderAuroraVeil.java`
- `RenderAbyssalCore.java`
- `RenderStormCore.java`
- `RenderEntropyCloud.java`
- `RenderSolarCoronaBurst.java`
- `RenderStellarHourglass.java`
- `natural_effect.vsh`
- `natural_effect.fsh`

效果要求：

- `stardust_fountain`：星点向上喷出、顶部散开、缓慢回落。
- `aurora_veil`：竖向彩色光幕、多层波浪面。
- `abyssal_core`：蓝绿色水波光球、深海浮游光点。
- `storm_core`：自然雷暴云团、旋转风眼、电弧闪烁。
- `entropy_cloud`：灰白噪声云不断分解重组，偶发黑色裂纹。
- `solar_corona_burst`：小型太阳核心、外侧日珥喷发弧线。
- `stellar_hourglass`：上下星云团、中间星尘流动。

## 清理计划

第一阶段不删除资源，只完成 shader-only 重制和编译通过。

第二阶段执行引用检查：

- 搜索所有 `textures/effects` 引用。
- 搜索所有 `textures/blocks/*.png` 是否仅用于方块/物品模型。
- 搜索 `RenderHelper`、`RenderEnergyEffectHelper`、`RenderCelestialEffectBase` 是否仍被剩余渲染器使用。

第三阶段再删除确认无用的文件：

- 已不再被 shader 或模型引用的效果 PNG。
- 旧 fallback 分支代码。
- 不再使用的 Tessellator 辅助方法。

## 提交与推送顺序

1. 提交本计划文档：`Plan shader-only remake for remaining effects`。
2. 天体组完成后提交：`Remake celestial effects with shaders`。
3. 空间组完成后提交：`Remake space effects with shaders`。
4. 魔法组完成后提交：`Remake magic effects with shaders`。
5. 科技组完成后提交：`Remake tech effects with shaders`。
6. 自然组完成后提交：`Remake natural effects with shaders`。
7. 公共缓存和清理完成后提交：`Clean shader-only rendering leftovers`。

每个代码提交前至少运行：

- `.\gradlew.bat compileJava`

每个阶段完成后推送到 `origin shader-experiment`。

## 当前已知风险

- 当前配置说明仍提到实验性 shader 和 fallback，需要在 shader-only 清理阶段更新。
- `ShaderManager.disableShaders` 仍会全局禁用 shader，需要在移除 fallback 时重新评估失败处理文字与行为。
- 共享 shader 参数名需要统一，避免不同 agent 增加互不兼容的 uniform。
- 透明效果需要继续关注视角下的接缝、背面剔除和深度写入状态。
- 旧 PNG 是否删除不能只按文件名判断，必须先查模型、物品和语言文件引用。
