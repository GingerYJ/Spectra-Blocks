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
    private static final float INNER_EXPAND_BASE = 0.82F;
    private static final float INNER_EXPAND_RANGE = 0.36F;
    private static final float OUTER_EXPAND_BASE = 0.88F;
    private static final float OUTER_EXPAND_RANGE = 0.24F;

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;

        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;
        float coreTime = ticks * baseAnimationSpeed();
        float innerTime = ticks * innerAnimationSpeed();
        float outerTime = ticks * outerAnimationSpeed();

        float innerExpand = smoothWave(innerTime * innerExpandFrequency());
        float innerBrightness = nestedWave(innerTime * innerBrightnessFrequency());
        float innerGridEnergy = smoothWave(innerTime * innerGridFrequency());
        float outerExpand = smoothWave(outerTime * outerExpandFrequency());
        float outerBrightness = nestedWave(outerTime * outerBrightnessFrequency());
        float outerGridEnergy = smoothWave(outerTime * outerGridFrequency());

        double innerRadius = INNER_HALO_BASE * (innerExpandBase() + innerExpandRange() * innerExpand);
        double outerRadius = OUTER_HALO_BASE * (outerExpandBase() + outerExpandRange() * outerExpand);
        float innerAlpha = innerAlphaBase() + innerAlphaRange() * innerBrightness;
        float outerAlpha = outerAlphaBase() + outerAlphaRange() * outerBrightness;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(defaultRenderScale());
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean alphaWasEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean depthMaskWasEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        try {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-outerTime * outerRotationSpeed(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(12.0F, 0.5F, 0.0F, 1.0F);
            RenderHelper.drawSphere(outerRadius, outerHaloColor(), outerAlpha,
                    LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
            RenderHelper.drawWireframeSphere(outerRadius, outerGridColor(),
                    outerGridAlpha() * (0.5F + 0.5F * outerGridEnergy), GRID_LAT, GRID_LON);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.rotate(coreTime * innerRotationSpeed(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(18.0F, 1.0F, 0.0F, 0.3F);
            RenderHelper.drawSphere(innerRadius, innerHaloColor(), innerAlpha,
                    LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
            RenderHelper.drawWireframeSphere(innerRadius, innerGridColor(),
                    innerGridAlpha() * (0.5F + 0.5F * innerGridEnergy), GRID_LAT, GRID_LON);
            GlStateManager.popMatrix();

            RenderHelper.drawSphere(EVENT_HORIZON_RADIUS, coreColor(), coreAlpha(),
                    LATITUDE_SEGMENTS, LONGITUDE_SEGMENTS);
        } finally {
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.shadeModel(GL11.GL_FLAT);
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
            GlStateManager.depthMask(depthMaskWasEnabled);
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

    private static float smoothWave(float time) {
        float phase = wave(time);
        return phase * phase * (3.0F - 2.0F * phase);
    }

    private static float wave(float time) {
        return 0.5F + 0.5F * (float) Math.sin(time);
    }

    private static float nestedWave(float time) {
        return 0.5F + 0.5F * smoothWave(time);
    }

    protected abstract int coreColor();

    protected abstract float coreAlpha();

    protected abstract int innerHaloColor();

    protected abstract int innerGridColor();

    protected abstract int outerHaloColor();

    protected abstract int outerGridColor();

    protected abstract double defaultRenderScale();

    protected float baseAnimationSpeed() {
        return BASE_ANIMATION_SPEED;
    }

    protected float innerAnimationSpeed() {
        return INNER_ANIMATION_SPEED;
    }

    protected float outerAnimationSpeed() {
        return OUTER_ANIMATION_SPEED;
    }

    protected float innerExpandFrequency() {
        return 0.8F;
    }

    protected float innerBrightnessFrequency() {
        return 0.6F;
    }

    protected float innerGridFrequency() {
        return 1.25F;
    }

    protected float outerExpandFrequency() {
        return 0.45F;
    }

    protected float outerBrightnessFrequency() {
        return 0.35F;
    }

    protected float outerGridFrequency() {
        return 0.7F;
    }

    protected float innerRotationSpeed() {
        return 0.45F;
    }

    protected float outerRotationSpeed() {
        return 0.35F;
    }

    protected float innerExpandBase() {
        return INNER_EXPAND_BASE;
    }

    protected float innerExpandRange() {
        return INNER_EXPAND_RANGE;
    }

    protected float outerExpandBase() {
        return OUTER_EXPAND_BASE;
    }

    protected float outerExpandRange() {
        return OUTER_EXPAND_RANGE;
    }

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
