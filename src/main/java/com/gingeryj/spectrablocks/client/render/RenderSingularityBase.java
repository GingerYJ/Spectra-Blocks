package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import org.lwjgl.opengl.GL11;

public abstract class RenderSingularityBase<T extends TileScalableEffect> extends TileEntitySpecialRenderer<T> {

    private static final double EVENT_HORIZON_RADIUS = 1.2D;
    private static final double INNER_HALO_BASE = 1.8D;
    private static final double OUTER_HALO_BASE = 2.8D;

    private static final int LATITUDE_SEGMENTS = 16;
    private static final int LONGITUDE_SEGMENTS = 16;
    private static final int GRID_LAT = 6;
    private static final int GRID_LON = 8;

    private static final float BASE_ANIMATION_SPEED = 1.5F;
    private static final float INNER_ANIMATION_SPEED = 0.7F;
    private static final float OUTER_ANIMATION_SPEED = 0.35F;

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;

        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;
        float coreTime = ticks * BASE_ANIMATION_SPEED;
        float innerTime = ticks * INNER_ANIMATION_SPEED;
        float outerTime = ticks * OUTER_ANIMATION_SPEED;

        float innerExpand = wave(innerTime * 0.8F);
        float innerBrightness = nestedWave(innerTime * 0.6F);
        float innerGridEnergy = wave(innerTime * 1.25F);
        float outerExpand = wave(outerTime * 0.45F);
        float outerBrightness = nestedWave(outerTime * 0.35F);
        float outerGridEnergy = wave(outerTime * 0.7F);

        double innerRadius = INNER_HALO_BASE * (0.82D + 0.36D * innerExpand);
        double outerRadius = OUTER_HALO_BASE * (0.88D + 0.24D * outerExpand);
        float innerAlpha = innerAlphaBase() + innerAlphaRange() * innerBrightness;
        float outerAlpha = outerAlphaBase() + outerAlphaRange() * outerBrightness;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(defaultRenderScale());
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        try {
            RenderHelper.drawSphere(EVENT_HORIZON_RADIUS, coreColor(), coreAlpha(),
                    LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(coreTime * 0.45F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(18.0F, 1.0F, 0.0F, 0.3F);
            RenderHelper.drawSphere(innerRadius, innerHaloColor(), innerAlpha,
                    LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
            RenderHelper.drawWireframeSphere(innerRadius, innerGridColor(),
                    innerGridAlpha() * (0.5F + 0.5F * innerGridEnergy), GRID_LAT, GRID_LON);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.rotate(-outerTime * 0.35F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(12.0F, 0.5F, 0.0F, 1.0F);
            RenderHelper.drawSphere(outerRadius, outerHaloColor(), outerAlpha,
                    LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
            RenderHelper.drawWireframeSphere(outerRadius, outerGridColor(),
                    outerGridAlpha() * (0.5F + 0.5F * outerGridEnergy), GRID_LAT, GRID_LON);
            GlStateManager.popMatrix();
        } finally {
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            if (!blendWasEnabled) {
                GlStateManager.disableBlend();
            }
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
    }

    private static float wave(float time) {
        return 0.5F + 0.5F * (float) Math.sin(time);
    }

    private static float nestedWave(float time) {
        return 0.5F + 0.5F * wave(time);
    }

    protected abstract int coreColor();

    protected abstract float coreAlpha();

    protected abstract int innerHaloColor();

    protected abstract int innerGridColor();

    protected abstract int outerHaloColor();

    protected abstract int outerGridColor();

    protected abstract double defaultRenderScale();

    protected float innerAlphaBase() {
        return 0.15F;
    }

    protected float innerAlphaRange() {
        return 0.35F;
    }

    protected float outerAlphaBase() {
        return 0.08F;
    }

    protected float outerAlphaRange() {
        return 0.18F;
    }

    protected float innerGridAlpha() {
        return 0.4F;
    }

    protected float outerGridAlpha() {
        return 0.12F;
    }
}
