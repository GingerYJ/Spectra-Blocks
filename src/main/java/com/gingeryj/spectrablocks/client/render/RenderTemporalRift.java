package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileTemporalRift;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderTemporalRift extends TileEntitySpecialRenderer<TileTemporalRift> {

    private static final double HALF_HEIGHT = 1.18D;
    private static final double BASE_HALF_WIDTH = 0.040D;
    private static final double WIDTH_VARIANCE = 0.125D;
    private static final double RIFT_DEPTH = 0.010D;
    private static final double JITTER_STRENGTH = 0.055D;
    private static final int CRACK_SEGMENTS = 22;
    private static final int RIPPLE_SEGMENTS = 112;
    private static final int RIPPLE_COUNT = 4;
    private static final int CLOCK_FRAGMENT_COUNT = 18;
    private static final int AFTERIMAGE_COUNT = 3;
    private static final int HALO_SEGMENTS = 128;
    private static final float BACKWARD_HALO_SPEED = -0.28F;
    private static final float FRAGMENT_SPEED = -0.72F;
    private static final float RIFT_ALPHA = 0.68F;
    private static final float EDGE_ALPHA = 0.58F;
    private static final float RIPPLE_ALPHA = 0.20F;
    private static final float FRAGMENT_ALPHA = 0.56F;
    private static final float AFTERIMAGE_ALPHA = 0.18F;
    private static final int VOID_COLOR = 0x060416;
    private static final int EDGE_COLOR = 0x77F2FF;
    private static final int WARM_EDGE_COLOR = 0xFFD37D;
    private static final int RIPPLE_COLOR = 0xB7FBFF;
    private static final int HALO_COLOR = 0xFFE6A3;
    private static final int AFTERIMAGE_COLOR = 0x9B6CFF;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TileTemporalRift te, double x, double y, double z,
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
        useNormalBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        try {
            drawAfterimages(ticks);
            drawRiftPlane(ticks);
            drawBackwardHalos(ticks);
            drawClockFragments(ticks);
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
            useNormalBlend();
            RenderHelper.resetLineWidth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private void drawRiftPlane(float ticks) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) Math.sin(ticks * 0.018F) * 3.0F, 0.0F, 1.0F, 0.0F);
        useNormalBlend();
        drawCrackBody(ticks, 0.0D, RIFT_ALPHA);

        useAdditiveBlend();
        drawRipples(ticks, 0.0D, 1.0F);
        drawCrackEdges(ticks, 0.0D, 1.0F);
        GlStateManager.popMatrix();
    }

    private void drawAfterimages(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) {
            float ghostPulse = sharpPulse(ticks * 0.070F - i * 0.76F);
            if (ghostPulse <= 0.02F) {
                continue;
            }

            GlStateManager.pushMatrix();
            GlStateManager.rotate(28.0F * (i + 1) + (float) Math.sin(ticks * 0.012F + i) * 2.0F,
                    0.0F, 1.0F, 0.0F);
            GlStateManager.translate((i - 1) * 0.055D, 0.0D, -0.045D * (i + 1));
            drawCrackBody(ticks - 9.0F - i * 4.0F, i * 2.7D, AFTERIMAGE_ALPHA * ghostPulse);
            drawRipples(ticks - i * 5.0F, i * 3.1D, ghostPulse * 0.55F);
            GlStateManager.popMatrix();
        }
    }

    private void drawCrackBody(float ticks, double seed, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(VOID_COLOR);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= CRACK_SEGMENTS; i++) {
            double progress = (double) i / CRACK_SEGMENTS;
            double y = -HALF_HEIGHT + progress * HALF_HEIGHT * 2.0D;
            double center = crackCenter(progress, ticks, seed);
            double halfWidth = crackHalfWidth(progress, ticks, seed);
            float fade = 0.50F + 0.50F * (float) Math.sin(progress * Math.PI);
            buffer.pos(center - halfWidth, y, -RIFT_DEPTH)
                    .color(rgb[0], rgb[1], rgb[2], alpha * fade).endVertex();
            buffer.pos(center + halfWidth, y, RIFT_DEPTH)
                    .color(rgb[0], rgb[1], rgb[2], alpha * fade).endVertex();
        }
        tessellator.draw();
    }

    private void drawCrackEdges(float ticks, double seed, float alphaScale) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.085F + seed);
        GlStateManager.glLineWidth(4.0F);
        drawCrackEdge(ticks, seed, -1.0D, AFTERIMAGE_COLOR, EDGE_ALPHA * alphaScale * (0.35F + 0.40F * pulse));
        drawCrackEdge(ticks, seed, 1.0D, AFTERIMAGE_COLOR, EDGE_ALPHA * alphaScale * (0.35F + 0.40F * pulse));

        GlStateManager.glLineWidth(1.6F);
        drawCrackEdge(ticks + 2.0F, seed, -1.0D, EDGE_COLOR, EDGE_ALPHA * alphaScale);
        drawCrackEdge(ticks + 2.0F, seed, 1.0D, WARM_EDGE_COLOR, EDGE_ALPHA * alphaScale * 0.76F);
        RenderHelper.resetLineWidth();
    }

    private void drawCrackEdge(float ticks, double seed, double side, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= CRACK_SEGMENTS; i++) {
            double progress = (double) i / CRACK_SEGMENTS;
            double y = -HALF_HEIGHT + progress * HALF_HEIGHT * 2.0D;
            double center = crackCenter(progress, ticks, seed);
            double halfWidth = crackHalfWidth(progress, ticks, seed);
            double tick = Math.sin(progress * Math.PI * 31.0D - ticks * 0.048D + seed) * 0.018D;
            buffer.pos(center + side * (halfWidth + tick), y, side * 0.015D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawRipples(float ticks, double seed, float alphaScale) {
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            double age = fract(ticks * 0.0075D + i * 0.223D + seed * 0.031D);
            float fade = (float) Math.sin(age * Math.PI);
            double width = 0.24D + age * 0.95D;
            double height = 0.38D + age * 1.15D;
            drawWavyEllipse(width, height, ticks, seed + i * 4.3D,
                    i == 0 ? HALO_COLOR : RIPPLE_COLOR, RIPPLE_ALPHA * fade * alphaScale);
        }
    }

    private void drawWavyEllipse(double width, double height, float ticks, double seed, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < RIPPLE_SEGMENTS; i++) {
            double angle = TWO_PI * i / RIPPLE_SEGMENTS;
            double wobble = 1.0D
                    + Math.sin(angle * 5.0D - ticks * 0.040D + seed) * 0.055D
                    + Math.cos(angle * 8.0D + ticks * 0.025D + seed) * 0.035D;
            buffer.pos(Math.cos(angle) * width * wobble,
                            Math.sin(angle) * height * wobble,
                            Math.sin(angle * 3.0D + ticks * 0.030D + seed) * 0.018D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawBackwardHalos(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * BACKWARD_HALO_SPEED, 0.0F, 0.0F, 1.0F);
        drawHaloRing(0.84D, 0.028D, HALO_COLOR, 0.25F, ticks, 0.0D);
        drawTickRing(0.96D, HALO_COLOR, 0.42F, 24, ticks * -0.015D);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F + ticks * BACKWARD_HALO_SPEED * 0.72F, 0.0F, 1.0F, 0.0F);
        drawHaloRing(0.64D, 0.020D, EDGE_COLOR, 0.18F, ticks, 2.0D);
        drawTickRing(0.72D, EDGE_COLOR, 0.24F, 12, ticks * -0.011D);
        GlStateManager.popMatrix();
    }

    private void drawHaloRing(double radius, double halfWidth, int color, float alpha, float ticks, double seed) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= HALO_SEGMENTS; i++) {
            double angle = TWO_PI * i / HALO_SEGMENTS;
            double pulse = 1.0D + Math.sin(angle * 6.0D - ticks * 0.04D + seed) * 0.025D;
            buffer.pos(Math.cos(angle) * (radius + halfWidth) * pulse,
                            Math.sin(angle) * (radius + halfWidth) * pulse,
                            0.0D)
                    .color(rgb[0], rgb[1], rgb[2], alpha * 0.36F).endVertex();
            buffer.pos(Math.cos(angle) * (radius - halfWidth) * pulse,
                            Math.sin(angle) * (radius - halfWidth) * pulse,
                            0.0D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawTickRing(double radius, int color, float alpha, int count, double phase) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < count; i++) {
            double angle = phase + TWO_PI * i / count;
            double length = i % 3 == 0 ? 0.105D : 0.055D;
            double inner = radius - length;
            double outer = radius + length * 0.25D;
            buffer.pos(Math.cos(angle) * inner, Math.sin(angle) * inner, 0.0D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
            buffer.pos(Math.cos(angle) * outer, Math.sin(angle) * outer, 0.0D)
                    .color(rgb[0], rgb[1], rgb[2], alpha * 0.70F).endVertex();
        }
        tessellator.draw();
    }

    private void drawClockFragments(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CLOCK_FRAGMENT_COUNT; i++) {
            double orbit = 0.46D + (i % 5) * 0.105D;
            double angle = i * 2.399963229728653D + ticks * FRAGMENT_SPEED * 0.018D;
            double y = -0.70D + (i % 9) * 0.175D + Math.sin(ticks * 0.045D + i) * 0.035D;
            double x = Math.cos(angle) * orbit;
            double z = Math.sin(angle) * orbit * 0.34D;
            float pulse = 0.62F + 0.38F * (float) Math.sin(ticks * 0.11F + i * 1.7D);
            double size = 0.050D + (i % 3) * 0.012D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate((float) (angle * 57.29577951308232D) + ticks * -1.6F, 0.0F, 0.0F, 1.0F);
            drawFragment(size, i % 4 == 0 ? WARM_EDGE_COLOR : EDGE_COLOR, FRAGMENT_ALPHA * pulse);
            GlStateManager.popMatrix();
        }
    }

    private void drawFragment(double size, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-size, -size * 0.45D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(size * 0.70D, -size * 0.24D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        buffer.pos(size, size * 0.62D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha * 0.75F).endVertex();
        buffer.pos(-size * 0.52D, size * 0.36D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha * 0.82F).endVertex();
        buffer.pos(-size, -size * 0.45D, 0.0D).color(rgb[0], rgb[1], rgb[2], alpha * 0.65F).endVertex();
        tessellator.draw();
    }

    private static double crackCenter(double progress, float ticks, double seed) {
        return Math.sin(progress * Math.PI * 4.0D - ticks * 0.034D + seed) * JITTER_STRENGTH
                + Math.cos(progress * Math.PI * 11.0D + seed * 0.7D) * 0.026D;
    }

    private static double crackHalfWidth(double progress, float ticks, double seed) {
        double taper = Math.sin(progress * Math.PI);
        double pulse = 0.5D + 0.5D * Math.sin(progress * Math.PI * 7.0D + ticks * 0.050D + seed);
        double chip = 0.5D + 0.5D * Math.cos(progress * Math.PI * 19.0D - seed);
        return (BASE_HALF_WIDTH + WIDTH_VARIANCE * (0.60D * pulse + 0.40D * chip))
                * (0.12D + 0.88D * taper);
    }

    private static float sharpPulse(float value) {
        float pulse = Math.max(0.0F, (float) Math.sin(value));
        return pulse * pulse * pulse;
    }

    private static double fract(double value) {
        return value - Math.floor(value);
    }

    private static void useAdditiveBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private static void useNormalBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }
}
