package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public final class EffectDepthPrepass {

    private static final int SPHERE_LAT_SEGMENTS = 32;
    private static final int SPHERE_LON_SEGMENTS = 32;
    private static final int MICRO_STELLAR_SOURCE_SPHERE_SEGMENTS = 72;

    private static boolean renderedThisFrame;
    private static boolean renderingPrepass;

    private EffectDepthPrepass() {
    }

    static void render(TileScalableEffect current, double currentX, double currentY, double currentZ) {
        if (renderedThisFrame || renderingPrepass || current == null || current.getWorld() == null) {
            return;
        }

        renderedThisFrame = true;
        renderingPrepass = true;

        World world = current.getWorld();
        BlockPos currentPos = current.getPos();
        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean alphaWasEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMaskWasEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
        int previousFrontFace = GL11.glGetInteger(GL11.GL_FRONT_FACE);
        int previousDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        int previousShadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        ByteBuffer colorMask = BufferUtils.createByteBuffer(4);
        GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, colorMask);

        try {
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glColorMask(false, false, false, false);
            GL11.glFrontFace(GL11.GL_CCW);
            GlStateManager.enableCull();
            // Write the far side only, so each effect keeps its internal translucent layers.
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            GlStateManager.shadeModel(GL11.GL_FLAT);

            for (TileEntity tile : world.loadedTileEntityList) {
                if (!(tile instanceof TileScalableEffect) || tile.isInvalid()) {
                    continue;
                }

                TileScalableEffect effect = (TileScalableEffect) tile;
                if (!isInRenderRange(effect)) {
                    continue;
                }

                BlockPos pos = effect.getPos();
                double relativeX = currentX + (pos.getX() - currentPos.getX());
                double relativeY = currentY + (pos.getY() - currentPos.getY());
                double relativeZ = currentZ + (pos.getZ() - currentPos.getZ());
                double radius = depthRadius(effect) * effect.renderScale(defaultScale(effect));
                drawDepthShell(relativeX + 0.5D, relativeY + 0.5D, relativeZ + 0.5D, radius,
                        depthLatSegments(effect), depthLonSegments(effect));
            }
        } finally {
            GL11.glColorMask(colorMask.get(0) != 0, colorMask.get(1) != 0,
                    colorMask.get(2) != 0, colorMask.get(3) != 0);
            GL11.glDepthFunc(previousDepthFunc);
            GL11.glFrontFace(previousFrontFace);
            GlStateManager.cullFace(previousCullFace == GL11.GL_FRONT
                    ? GlStateManager.CullFace.FRONT
                    : GlStateManager.CullFace.BACK);
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.shadeModel(previousShadeModel);
            if (alphaWasEnabled) {
                GlStateManager.enableAlpha();
            } else {
                GlStateManager.disableAlpha();
            }
            if (textureWasEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
            if (lightingWasEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            if (depthWasEnabled) {
                GlStateManager.enableDepth();
            } else {
                GlStateManager.disableDepth();
            }
            GlStateManager.depthMask(depthMaskWasEnabled);
            if (blendWasEnabled) {
                GlStateManager.enableBlend();
            } else {
                GlStateManager.disableBlend();
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            renderingPrepass = false;
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        renderedThisFrame = false;
    }

    private static boolean isInRenderRange(TileScalableEffect effect) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.getRenderViewEntity() == null) {
            return true;
        }

        return effect.getDistanceSq(minecraft.getRenderViewEntity().posX,
                minecraft.getRenderViewEntity().posY,
                minecraft.getRenderViewEntity().posZ) <= effect.getMaxRenderDistanceSquared();
    }

    private static double defaultScale(TileScalableEffect effect) {
        switch (effect.getClass().getSimpleName()) {
            case "TileMicroSingularity":
                return ModConfig.microSingularityScale();
            case "TileMicroWhiteHole":
                return ModConfig.microWhiteHoleScale();
            case "TileMicroUniverse":
                return ModConfig.microUniverseScale();
            case "TileMicroStellarSource":
                return ModConfig.microStellarSourceScale();
            default:
                throw new IllegalArgumentException("Unsupported effect tile: " + effect.getClass().getName());
        }
    }

    private static double depthRadius(TileScalableEffect effect) {
        switch (effect.getClass().getSimpleName()) {
            case "TileMicroUniverse":
                return 5.55D;
            case "TileMicroStellarSource":
                return 5.595D;
            case "TileMicroSingularity":
            case "TileMicroWhiteHole":
                return 3.15D;
            default:
                throw new IllegalArgumentException("Unsupported effect tile: " + effect.getClass().getName());
        }
    }

    private static int depthLatSegments(TileScalableEffect effect) {
        if ("TileMicroStellarSource".equals(effect.getClass().getSimpleName())) {
            return MICRO_STELLAR_SOURCE_SPHERE_SEGMENTS;
        }
        return SPHERE_LAT_SEGMENTS;
    }

    private static int depthLonSegments(TileScalableEffect effect) {
        if ("TileMicroStellarSource".equals(effect.getClass().getSimpleName())) {
            return MICRO_STELLAR_SOURCE_SPHERE_SEGMENTS;
        }
        return SPHERE_LON_SEGMENTS;
    }

    private static void drawDepthShell(double x, double y, double z, double radius, int latSegments, int lonSegments) {
        if (radius <= 0.0D || latSegments < 3 || lonSegments < 3) {
            return;
        }

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
            for (int lat = 0; lat < latSegments; lat++) {
                double theta0 = Math.PI * lat / latSegments;
                double theta1 = Math.PI * (lat + 1) / latSegments;
                for (int lon = 0; lon < lonSegments; lon++) {
                    double phi0 = Math.PI * 2.0D * lon / lonSegments;
                    double phi1 = Math.PI * 2.0D * (lon + 1) / lonSegments;
                    addSphereVertex(buffer, radius, theta0, phi0);
                    addSphereVertex(buffer, radius, theta1, phi1);
                    addSphereVertex(buffer, radius, theta1, phi0);
                    addSphereVertex(buffer, radius, theta0, phi0);
                    addSphereVertex(buffer, radius, theta0, phi1);
                    addSphereVertex(buffer, radius, theta1, phi1);
                }
            }
            tessellator.draw();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi) {
        buffer.pos(Math.sin(theta) * Math.cos(phi) * radius,
                        Math.cos(theta) * radius,
                        Math.sin(theta) * Math.sin(phi) * radius)
                .endVertex();
    }
}
