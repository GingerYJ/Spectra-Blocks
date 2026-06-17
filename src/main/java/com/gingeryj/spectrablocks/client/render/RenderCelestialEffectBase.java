package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

abstract class RenderCelestialEffectBase<T extends TileScalableEffect> extends TileEntitySpecialRenderer<T> {

    private static final double TWO_PI = Math.PI * 2.0D;

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

        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        useAlphaBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        try {
            renderCelestialEffect(te, ticks);
        } finally {
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            if (!blendWasEnabled) {
                GlStateManager.disableBlend();
            }
            useAlphaBlend();
            RenderHelper.resetLineWidth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    protected abstract void renderCelestialEffect(T te, float ticks);

    protected static void useAlphaBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    protected static void useAdditiveBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
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

    protected static void drawSphereAt(double x, double y, double z, double radius,
                                       int color, float alpha, int latSegs, int lonSegs) {
        if (alpha <= 0.01F || radius <= 0.0D) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        RenderHelper.drawSphere(radius, color, alpha, latSegs, lonSegs);
        GlStateManager.popMatrix();
    }

    protected static void drawFlatRing(double innerRadius, double outerRadius,
                                       int color, float alpha, int segments) {
        if (alpha <= 0.01F || innerRadius <= 0.0D || outerRadius <= innerRadius) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = TWO_PI * i / segments;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            buffer.pos(cos * outerRadius, 0.0D, sin * outerRadius)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
            buffer.pos(cos * innerRadius, 0.0D, sin * innerRadius)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    protected static void drawSpiralRibbon(double startRadius, double endRadius,
                                           double startAngle, double sweep,
                                           double width, int color, float alpha,
                                           int segments) {
        if (alpha <= 0.01F || width <= 0.0D) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double angle = startAngle + sweep * progress;
            double radius = lerp(startRadius, endRadius, progress);
            double halfWidth = width * (1.0D - progress * 0.55D);
            double y = Math.sin(startAngle * 1.7D + progress * Math.PI * 3.0D) * 0.025D;
            float edgeFade = (float) Math.sin(Math.PI * progress);
            float vertexAlpha = alpha * (0.18F + 0.82F * edgeFade);
            double inner = Math.max(0.0D, radius - halfWidth);
            double outer = radius + halfWidth;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            buffer.pos(cos * outer, y, sin * outer)
                    .color(rgb[0], rgb[1], rgb[2], vertexAlpha)
                    .endVertex();
            buffer.pos(cos * inner, -y, sin * inner)
                    .color(rgb[0], rgb[1], rgb[2], vertexAlpha)
                    .endVertex();
        }
        tessellator.draw();
    }

    protected static void drawSphericalArc(double radius, double startYaw, double sweepYaw,
                                           double basePitch, double pitchWave, double phase,
                                           int color, float alpha, int segments) {
        if (alpha <= 0.01F || radius <= 0.0D) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double yaw = startYaw + sweepYaw * progress;
            double pitch = basePitch + Math.sin(phase + progress * TWO_PI) * pitchWave;
            double horizontal = Math.cos(pitch) * radius;
            float fade = (float) Math.sin(Math.PI * progress);
            buffer.pos(Math.cos(yaw) * horizontal,
                            Math.sin(pitch) * radius,
                            Math.sin(yaw) * horizontal)
                    .color(rgb[0], rgb[1], rgb[2], alpha * (0.20F + 0.80F * fade))
                    .endVertex();
        }
        tessellator.draw();
    }

    protected static void drawLatitudeCircle(double sphereRadius, double y,
                                             int color, float alpha, int segments) {
        if (alpha <= 0.01F || Math.abs(y) >= sphereRadius) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        double radius = Math.sqrt(Math.max(0.0D, sphereRadius * sphereRadius - y * y));
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            double angle = TWO_PI * i / segments;
            buffer.pos(Math.cos(angle) * radius, y, Math.sin(angle) * radius)
                    .color(rgb[0], rgb[1], rgb[2], alpha)
                    .endVertex();
        }
        tessellator.draw();
    }
}
