package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.GlStateManager;

public final class RenderHelper {

    private RenderHelper() {
    }

    public static float[] unpackRGB(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }

    public static void resetLineWidth() {
        GlStateManager.glLineWidth(1.0F);
    }
}
