# 视觉特效方块写法

本文记录 `Spectra Blocks` 中“微型黑洞”这种视觉特效方块的实现方式。核心思路是：方块本身只作为世界里的锚点，普通模型只用于物品栏和选择框，真正的动画效果由 `TileEntitySpecialRenderer` 在客户端实时绘制。

## 文件结构

当前微型黑洞涉及这些文件：

- `src/main/java/com/gingeryj/spectrablocks/block/BlockMicroSingularity.java`
- `src/main/java/com/gingeryj/spectrablocks/tile/TileMicroSingularity.java`
- `src/main/java/com/gingeryj/spectrablocks/creative/ModCreativeTabs.java`
- `src/main/java/com/gingeryj/spectrablocks/client/render/RenderMicroSingularity.java`
- `src/main/java/com/gingeryj/spectrablocks/client/render/RenderMicroWhiteHole.java`
- `src/main/java/com/gingeryj/spectrablocks/client/render/RenderMicroUniverse.java`
- `src/main/java/com/gingeryj/spectrablocks/client/render/RenderSingularityBase.java`
- `src/main/java/com/gingeryj/spectrablocks/client/render/RenderHelper.java`
- `src/main/java/com/gingeryj/spectrablocks/registry/ModContent.java`
- `src/main/java/com/gingeryj/spectrablocks/proxy/ClientProxy.java`
- `src/main/java/com/gingeryj/spectrablocks/proxy/CommonProxy.java`
- `src/main/resources/assets/spectrablocks/blockstates/micro_singularity.json`
- `src/main/resources/assets/spectrablocks/models/block/micro_singularity.json`
- `src/main/resources/assets/spectrablocks/models/item/micro_singularity.json`
- `src/main/resources/assets/spectrablocks/textures/blocks/micro_singularity.png`
- `src/main/resources/assets/spectrablocks/textures/blocks/micro_white_hole.png`
- `src/main/resources/assets/spectrablocks/textures/blocks/micro_universe.png`
- `src/main/resources/assets/spectrablocks/lang/zh_cn.lang`

## 1. 方块只做渲染锚点

视觉特效方块通常不需要普通实心方块外观。`BlockMicroSingularity` 的职责是：

- 设置注册名和翻译键。
- 设置发光、硬度、创造栏。
- 返回非完整方块、非不透明方块。
- 隐藏世界中的普通方块模型。
- 去掉碰撞箱。
- 提供一个小选择框。
- 创建对应的 TileEntity。

关键写法：

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
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
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
}
```

## 2. TileEntity 负责客户端粒子和渲染范围

`TileMicroSingularity` 不保存复杂数据，只做两件事：

- 客户端随机生成 `PORTAL` 粒子。
- 扩大 `getRenderBoundingBox()`，避免玩家稍微离开方块本体视锥后，外层光晕被裁掉。

关键写法：

```java
public class TileMicroSingularity extends TileEntity implements ITickable {

    private static final double RENDER_RADIUS = 3.25D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(4) != 0) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + (world.rand.nextDouble() - 0.5D) * 0.8D,
                centerY + (world.rand.nextDouble() - 0.5D) * 0.8D,
                centerZ + (world.rand.nextDouble() - 0.5D) * 0.8D,
                (world.rand.nextDouble() - 0.5D) * 0.2D,
                (world.rand.nextDouble() - 0.5D) * 0.2D,
                (world.rand.nextDouble() - 0.5D) * 0.2D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                pos.getX() + 0.5D - RENDER_RADIUS,
                pos.getY() + 0.5D - RENDER_RADIUS,
                pos.getZ() + 0.5D - RENDER_RADIUS,
                pos.getX() + 0.5D + RENDER_RADIUS,
                pos.getY() + 0.5D + RENDER_RADIUS,
                pos.getZ() + 0.5D + RENDER_RADIUS
        );
    }
}
```

## 3. 单独的创造栏

视觉特效方块统一放在 `Spectra Blocks` 自己的创造栏里。创造栏图标使用微型黑洞：

```java
public final class ModCreativeTabs {

    public static final CreativeTabs SPECTRA_BLOCKS = new CreativeTabs(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModContent.MICRO_SINGULARITY);
        }
    };

    private ModCreativeTabs() {
    }
}
```

本地化键：

```properties
itemGroup.spectrablocks=Spectra Blocks
itemGroup.spectrablocks=光谱方块
```

## 4. 注册方块、物品和 TileEntity

`ModContent` 里统一注册内容：

```java
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModContent {

    public static final BlockMicroSingularity MICRO_SINGULARITY = new BlockMicroSingularity();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(MICRO_SINGULARITY);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(MICRO_SINGULARITY)
                .setRegistryName(MICRO_SINGULARITY.getRegistryName()));
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileMicroSingularity.class,
                new ResourceLocation(Reference.MOD_ID, "micro_singularity"));
    }
}
```

`CommonProxy` 在 `preInit` 注册 TileEntity：

```java
public class CommonProxy implements IProxy {
    @Override
    public void preInit() {
        ModContent.registerTileEntities();
    }
}
```

## 5. 客户端绑定 TESR 和物品模型

`ClientProxy` 负责两件事：

- 在 `init()` 绑定 `TileEntitySpecialRenderer`。
- 在 `ModelRegistryEvent` 注册物品栏模型。

```java
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroSingularity.class, new RenderMicroSingularity());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerBlockItemModel(ModContent.MICRO_SINGULARITY);
    }

    private static void registerBlockItemModel(Block block) {
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }
}
```

## 6. TESR 绘制动画效果

`RenderSingularityBase` 是这类球形奇点效果的主体。具体方块只需要提供配色，例如黑洞使用 `RenderMicroSingularity`，白洞使用 `RenderMicroWhiteHole`。

微型黑洞由三类图形组成：

- 中央近乎不透明的黑色球体。
- 内层紫色半透明光晕球。
- 外层暗紫色半透明光晕球。
- 内外两层旋转线框球。

关键参数：

```java
private static final double EVENT_HORIZON_RADIUS = 1.2D;
private static final double INNER_HALO_BASE = 1.8D;
private static final double OUTER_HALO_BASE = 2.8D;
private static final float BASE_ANIMATION_SPEED = 1.5F;
private static final float INNER_ANIMATION_SPEED = 0.7F;
private static final float OUTER_ANIMATION_SPEED = 0.35F;
```

动画时间：

```java
float ticks = te.getWorld().getTotalWorldTime() + partialTicks;
float coreTime = ticks * BASE_ANIMATION_SPEED;
float innerTime = ticks * INNER_ANIMATION_SPEED;
float outerTime = ticks * OUTER_ANIMATION_SPEED;
float innerExpand = wave(innerTime * 0.8F);
float innerBrightness = nestedWave(innerTime * 0.6F);
float innerGridEnergy = wave(innerTime * 1.25F);
float outerExpand = 0.5F + 0.5F * (float) Math.sin(outerTime * 0.45F);
float outerBrightness = 0.5F + 0.5F * (0.5F + 0.5F * (float) Math.sin(outerTime * 0.35F));
float outerGridEnergy = 0.5F + 0.5F * (float) Math.sin(outerTime * 0.7F);
```

核心、中层和外层最好拆开时间轴。核心保持稳定；中层使用 `INNER_ANIMATION_SPEED = 0.7F`，保留能量流动但不至于太急；外层使用 `OUTER_ANIMATION_SPEED = 0.35F`，边缘呼吸和旋转会更慢。

渲染状态：

```java
GlStateManager.depthMask(false);
GlStateManager.enableBlend();
GlStateManager.tryBlendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
);
GlStateManager.disableLighting();
GlStateManager.disableTexture2D();
GlStateManager.shadeModel(GL11.GL_SMOOTH);
GlStateManager.disableCull();
```

绘制主体：

```java
RenderHelper.drawSphere(EVENT_HORIZON_RADIUS, 0x000000, 0.99F,
        LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);

GlStateManager.pushMatrix();
GlStateManager.rotate(time * 0.8F, 0.0F, 1.0F, 0.0F);
GlStateManager.rotate(18.0F, 1.0F, 0.0F, 0.3F);
RenderHelper.drawSphere(innerRadius, 0x140029, innerAlpha,
        LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
RenderHelper.drawWireframeSphere(innerRadius, 0x7700DD,
        0.4F * (0.5F + 0.5F * gridEnergy), GRID_LAT, GRID_LON);
GlStateManager.popMatrix();

GlStateManager.pushMatrix();
GlStateManager.rotate(-time * 0.5F, 0.0F, 1.0F, 0.0F);
GlStateManager.rotate(12.0F, 0.5F, 0.0F, 1.0F);
RenderHelper.drawSphere(outerRadius, 0x05000D, outerAlpha,
        LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
RenderHelper.drawWireframeSphere(outerRadius, 0x440088,
        0.12F * (0.5F + 0.5F * gridEnergy), GRID_LAT, GRID_LON);
GlStateManager.popMatrix();
```

黑洞只需要提供黑紫色配色：

```java
public class RenderMicroSingularity extends RenderSingularityBase<TileMicroSingularity> {
    @Override
    protected int coreColor() {
        return 0x000000;
    }

    @Override
    protected int innerHaloColor() {
        return 0x140029;
    }

    @Override
    protected int innerGridColor() {
        return 0x7700DD;
    }
}
```

白洞复用同样动画，但提供亮白金色配色：

```java
public class RenderMicroWhiteHole extends RenderSingularityBase<TileMicroWhiteHole> {
    @Override
    protected int coreColor() {
        return 0xFFF9E6;
    }

    @Override
    protected int innerHaloColor() {
        return 0xFFE8A8;
    }

    @Override
    protected int innerGridColor() {
        return 0xFFFFFF;
    }
}
```

结束时必须恢复 GL 状态，否则可能污染其他方块、实体或 GUI 的渲染：

```java
GlStateManager.shadeModel(GL11.GL_FLAT);
GlStateManager.enableTexture2D();
GlStateManager.enableLighting();
GlStateManager.depthMask(true);
RenderHelper.resetLineWidth();
GlStateManager.popMatrix();
```

## 7. RenderHelper 的球体画法

`RenderHelper.drawSphere()` 使用 `Tessellator` 和 `BufferBuilder` 按经纬度切分球体，用 `GL_TRIANGLES` 画三角面。

`RenderHelper.drawWireframeSphere()` 使用：

- `GL_LINE_LOOP` 画纬线。
- `GL_LINE_STRIP` 画经线。

当前实现不使用贴图，只使用顶点颜色和透明度，因此需要在 TESR 中关闭 `Texture2D`。

## 8. 微缩宇宙写法

`RenderMicroUniverse` 使用同一套 `RenderHelper`，但组合方式不同：

- 最外层：深黑半透明球体，代表宇宙边界。当前半径为 `5.45D`，最大渲染直径约 10.9 个方块，留出少量余量避免超过 11 格。
- 外层网格：低透明度蓝色线框，增加空间感。
- 星点：在球壳内侧摆放多个小白蓝球。
- 中心太阳：金色球体，加一层低透明光晕。
- 行星：多个贴图小球按照不同半径、速度和相位绕太阳旋转。
- 轨道线：用 `RenderHelper.drawCircle()` 画 `GL_LINE_LOOP` 圆环，并用多层线宽叠加做发光效果。
- 流星：按周期偶尔出现，从宇宙壳一侧划过，使用发光线段和小球头组成。

行星运动整体速度由 `ORBIT_SPEED_SCALE` 控制：

```java
private static final double ORBIT_SPEED_SCALE = 0.28D;
```

`Planet` 构造参数里的速度会乘上这个缩放值。调慢/调快太阳系时，优先改这个常量。

TileEntity 的渲染包围盒也要跟着放大：

```java
private static final double RENDER_RADIUS = 5.75D;
```

轨道辅助函数：

```java
public static void drawCircle(double radius, int color, float alpha, int segments) {
    if (alpha <= 0.01F) {
        return;
    }

    float[] rgb = unpackRGB(color);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
    for (int i = 0; i < segments; i++) {
        double angle = 2.0D * Math.PI * i / segments;
        buffer.pos(radius * Math.cos(angle), 0.0D, radius * Math.sin(angle))
                .color(rgb[0], rgb[1], rgb[2], alpha)
                .endVertex();
    }
    tessellator.draw();
}
```

行星参数建议写成数组，这样后续调速度、颜色和轨道半径很方便：

```java
private static final Planet[] PLANETS = new Planet[]{
        new Planet(1.00D, 0.085D, 0.135D, 0xA7B5C8, 1.25F, 0.02D, 0xA8C4FF, "mercury"),
        new Planet(1.40D, 0.118D, 0.105D, 0xE8B36A, 2.35F, -0.03D, 0xFFD58A, "venus"),
        new Planet(1.86D, 0.145D, 0.082D, 0x4AA3FF, 3.30F, 0.04D, 0x74B8FF, "earth")
};
```

行星位置计算：

```java
double angle = ticks * planet.speed + planet.phase;
double planetX = Math.cos(angle) * planet.orbitRadius;
double planetZ = Math.sin(angle) * planet.orbitRadius;
GlStateManager.translate(planetX, planet.verticalOffset, planetZ);
GlStateManager.enableTexture2D();
RenderHelper.drawTexturedSphere(planet.radius, planet.texture, 0.98F, 18, 18);
GlStateManager.disableTexture2D();
```

行星贴图位于：

```text
assets/spectrablocks/textures/effects/planets/
```

当前包含：

```text
mercury.png
venus.png
earth.png
mars.png
jupiter.png
saturn.png
uranus.png
```

这些行星贴图使用 128x64 的经纬展开图。渲染时使用更高分段的贴图球体：

```java
RenderHelper.drawTexturedSphere(planet.radius, planet.texture, 0.98F, 32, 32);
```

如果后续需要更近距离展示，可以继续提高贴图分辨率；但 TESR 中球体分段也要同步提高，否则贴图细节会被低多边形球面压掉。

发光轨道通过三次绘制同一个圆实现：

```java
private void drawGlowingOrbit(Planet planet) {
    GlStateManager.glLineWidth(3.0F);
    RenderHelper.drawCircle(planet.orbitRadius, planet.orbitGlowColor, 0.060F, ORBIT_SEGMENTS);
    GlStateManager.glLineWidth(2.0F);
    RenderHelper.drawCircle(planet.orbitRadius, planet.orbitGlowColor, 0.110F, ORBIT_SEGMENTS);
    GlStateManager.glLineWidth(1.0F);
    RenderHelper.drawCircle(planet.orbitRadius, 0xE2ECFF, 0.185F, ORBIT_SEGMENTS);
    RenderHelper.resetLineWidth();
}
```

流星按固定周期偶发，避免每帧都出现导致画面过满：

```java
int cycle = Math.floorMod((int) ticks, 260);
if (cycle > 64) {
    return;
}

float progress = cycle / 64.0F;
float fade = (float) Math.sin(Math.PI * progress);
```

流星尾迹用两层不同线宽的 `RenderHelper.drawLine()` 实现，头部再叠两个小球。

## 9. 资源模型只做占位和物品栏显示

方块状态：

```json
{
  "variants": {
    "normal": {
      "model": "spectrablocks:micro_singularity"
    }
  }
}
```

方块模型：

```json
{
  "parent": "block/cube_all",
  "textures": {
    "all": "spectrablocks:blocks/micro_singularity"
  }
}
```

物品模型：

```json
{
  "parent": "spectrablocks:block/micro_singularity"
}
```

这里的静态 cube 模型不会承担主要视觉效果，只负责物品栏、方块选择和资源系统不缺模型。

注意：世界中的普通方块模型要通过 `getRenderType()` 返回 `EnumBlockRenderType.INVISIBLE` 隐藏掉。否则即使 PNG 贴图带透明通道，`cube_all` 仍可能在世界里显示成一块方形面片，和 TESR 动画叠在一起。

## 10. 本地化

中文显示名：

```properties
tile.spectrablocks.micro_singularity.name=微型黑洞
tile.spectrablocks.micro_white_hole.name=微型白洞
tile.spectrablocks.micro_universe.name=微缩宇宙
itemGroup.spectrablocks=光谱方块
```

英文显示名：

```properties
tile.spectrablocks.micro_singularity.name=Micro Singularity
tile.spectrablocks.micro_white_hole.name=Micro White Hole
tile.spectrablocks.micro_universe.name=Micro Universe
itemGroup.spectrablocks=Spectra Blocks
```

## 11. 新增另一个特效方块的步骤

1. 新建一个 `BlockXxxEffect`，按当前方块写法设置非完整、非不透明、无碰撞、带 TileEntity。
2. 新建一个 `TileXxxEffect`，按效果大小覆写 `getRenderBoundingBox()`。
3. 新建一个 `RenderXxxEffect extends TileEntitySpecialRenderer<TileXxxEffect>`。
4. 在 `ModContent` 注册方块、ItemBlock、TileEntity。
5. 在 `ClientProxy.init()` 绑定 TESR。
6. 在 `ClientProxy.registerModels()` 注册物品模型。
7. 添加 blockstate、block model、item model、贴图和 lang。
8. 将方块设置到 `ModCreativeTabs.SPECTRA_BLOCKS`。
9. 运行 `.\gradlew.bat compileJava processResources` 检查。

## 12. 常见坑

- 不要忘记扩大 `getRenderBoundingBox()`，否则大光效会被视锥裁剪。
- 世界中只显示 TESR 时，方块类要返回 `EnumBlockRenderType.INVISIBLE`，避免透明贴图 cube 被画出来。
- TESR 改过的 GL 状态必须恢复，尤其是 blend、texture、lighting、depth mask、cull、shade model。
- 中文 lang 文件保持 UTF-8 无 BOM。
- 方块注册名不要轻易改，改注册名会影响已有存档。
- 这个项目当前不需要 Mixin；视觉特效方块可以只靠 Forge 注册、TileEntity 和 TESR 完成。
