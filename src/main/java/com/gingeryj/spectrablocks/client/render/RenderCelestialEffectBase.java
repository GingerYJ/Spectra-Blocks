package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

abstract class RenderCelestialEffectBase<T extends TileScalableEffect> extends TileEntitySpecialRenderer<T> {

    @Override
    public final void render(T te, double x, double y, double z,
                             float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) {
            return;
        }

        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(1.0D);
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        SpectraRenderState.State renderState = SpectraRenderState.beginIsolated();
        try {
            renderCelestialEffect(te, ticks);
        } finally {
            renderState.close();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(T te) {
        return true;
    }

    protected abstract void renderCelestialEffect(T te, float ticks);

    protected static void useAlphaBlend() {
        SpectraRenderState.useAlphaBlend();
    }

    protected static void useAdditiveBlend() {
        SpectraRenderState.useAdditiveBlend();
    }

    protected static float wave(double time) {
        return 0.5F + 0.5F * (float) Math.sin(time);
    }

    protected static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    protected static double fract(double value) {
        return value - Math.floor(value);
    }
}
