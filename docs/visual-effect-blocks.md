# 视觉特效方块写法

本文档记录 Spectra Blocks 中视觉特效方块的当前实现方式。项目已经转为 shader-only 动态渲染：方块本体只是世界中的锚点，真正的视觉效果由客户端 TESR 和 shader 绘制。

## 基本结构

一个视觉特效方块通常包含这些文件：

- `block/BlockXxx.java`
- `tile/TileXxx.java` 或复用 `TileScalableEffect`
- `client/render/RenderXxx.java`
- `registry/ModContent.java`
- `proxy/ClientProxy.java`
- `assets/spectrablocks/blockstates/xxx.json`
- `assets/spectrablocks/models/block/xxx.json`
- `assets/spectrablocks/models/item/xxx.json`
- `assets/spectrablocks/textures/blocks/xxx.png`
- `assets/spectrablocks/recipes/xxx.json`
- `assets/spectrablocks/lang/en_us.lang`
- `assets/spectrablocks/lang/zh_cn.lang`

## 方块类

方块类负责注册名、创造栏、碰撞、TileEntity 和放置时 NBT 继承。

典型写法：

```java
public class BlockMicroSingularity extends Block {

    private static final AxisAlignedBB BOX = new AxisAlignedBB(
            0.25D, 0.25D, 0.25D,
            0.75D, 0.75D, 0.75D
    );

    public BlockMicroSingularity() {
        super(Material.IRON);
        setRegistryName(Reference.MOD_ID, "micro_singularity");
        setTranslationKey(Reference.MOD_ID + ".micro_singularity");
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setLightLevel(1.0F);
        setCreativeTab(ModCreativeTabs.SPECTRA_BLOCKS);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOX;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileMicroSingularity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        EffectBlockHelper.applyRenderScaleFromStack(world, pos, stack);
    }
}
```

关键点：

- `getRenderType` 返回 `INVISIBLE`，世界中不绘制普通方块模型。
- block/item model 仍然需要 PNG，占位纹理用于物品栏和模型系统。
- 碰撞箱通常为空，选择框可以保留小尺寸，便于右键工具配置。
- 放置时调用 `EffectBlockHelper.applyRenderScaleFromStack`，让物品 NBT 的 `RenderScale` 写入 TileEntity。

## TileEntity 与 RenderScale

特效方块只需要一个方块级 NBT：

```text
RenderScale
```

它控制整个渲染结果等比缩放。不要为单个视觉层添加额外 NBT，避免配置复杂化。

可复用 `TileScalableEffect`，或在专用 tile 中提供相同能力：

- 读取 `RenderScale`
- 写入 `RenderScale`
- 网络同步到客户端
- 提供 `renderScale(defaultValue)` 给 renderer 使用

## TESR 渲染器

渲染器负责：

- 移动到方块中心
- 应用 `RenderScale`
- 设置混合、深度写入、剔除、纹理状态
- 调用 shader
- 绘制球体、环带、ribbon、面片、点状小球等几何
- 恢复 OpenGL 状态

基础流程：

```java
@Override
public void render(TileXxx te, double x, double y, double z,
                   float partialTicks, int destroyStage, float alpha) {
    float ticks = te.getWorld().getTotalWorldTime() + partialTicks;

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

    double scale = te.renderScale(1.0D);
    GlStateManager.scale(scale, scale, scale);

    ShaderProgram shader = ShaderManager.getProgram("tech_effect");
    if (shader == null || !shader.begin()) {
        GlStateManager.popMatrix();
        return;
    }

    try {
        // set uniforms
        // draw geometry
    } finally {
        shader.end();
        GlStateManager.popMatrix();
    }
}
```

当前目标是不保留 fallback。shader 失败时可以不渲染，但不要回到旧贴图球体或粒子系统。

## Shader 与几何

常用 shader：

- `celestial_effect`：天体、星云、星系类。
- `space_effect`：黑洞、白洞、裂隙、虫洞、维度门类。
- `stellar_source`：恒星源。
- `micro_universe_body` / `micro_universe_shell`：微缩宇宙专用。
- `tech_effect`：科技、能源、矩阵、折射类。
- `natural_effect`：自然、风暴、极光、喷泉类。
- `arcane_effect`：奥术、水晶、符文、灵魂类。
- `basic`：纯色辅助面片。

可见线条不要直接依赖 `GL_LINES`、`GL_LINE_LOOP` 或 `GL_LINE_STRIP`，优先用三角 ribbon：

- 短线：`RenderHelper.drawTexturedLine`
- 彩色线：`RenderHelper.drawColorLine`
- 圆环：`RenderHelper.drawTexturedCircle` 或专用环带几何
- 复杂路径：按路径点生成 triangle strip

这样可以减少不同显卡、不同视角下的线宽不稳定和接缝问题。

## 注册流程

新增方块时需要：

1. 在 `ModContent` 中声明方块和物品。
2. 注册 TileEntity。
3. 在 `ClientProxy` 中绑定 TESR。
4. 添加 blockstate、block model、item model。
5. 添加 PNG 占位纹理。
6. 添加配方。
7. 添加英文和中文 lang。
8. 确认进入 Spectra Blocks 创造栏。

## PNG 资源规则

shader-only 不等于可以删除所有 PNG。

当前 `textures/blocks/*.png` 主要用于方块/物品模型占位：

- 物品栏显示
- 手持显示
- 方块模型资源完整性

只有确认 PNG 没有被任何模型、blockstate、物品或代码引用时才能删除。

## 验证

每次修改后至少运行：

```bat
gradlew.bat compileJava
```

推送前运行：

```bat
gradlew.bat build
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

当前已确认完成的 `RenderMicroUniverse.java` 是例外，不在批量清理中主动修改。
