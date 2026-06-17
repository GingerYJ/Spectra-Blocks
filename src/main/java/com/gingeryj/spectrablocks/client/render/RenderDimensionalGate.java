package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileDimensionalGate;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderDimensionalGate extends TileEntitySpecialRenderer<TileDimensionalGate> {

    private static final double PORTAL_HALF_WIDTH = 0.78D;
    private static final double PORTAL_HALF_HEIGHT = 1.35D;
    private static final double CORE_DEPTH = 0.018D;
    private static final double OUTER_RING_WIDTH = 0.080D;
    private static final int ELLIPSE_SEGMENTS = 144;
    private static final int CORE_RINGS = 8;
    private static final int STAR_COUNT = 72;
    private static final int RUNE_COUNT = 28;
    private static final float CORE_ALPHA = 0.30F;
    private static final float RING_ALPHA = 0.58F;
    private static final float RUNE_ALPHA = 0.66F;
    private static final float STAR_ALPHA = 0.76F;
    private static final float RING_ROTATION_SPEED = 0.72F;
    private static final float STAR_FLOW_SPEED = 0.020F;
    private static final int CORE_INNER_COLOR = 0x071326;
    private static final int CORE_OUTER_COLOR = 0x223BFF;
    private static final int EDGE_COLOR = 0x5EF7FF;
    private static final int RUNE_COLOR = 0xE8D7FF;
    private static final int STAR_COLOR = 0xFFFFFF;
    private static final int VIOLET_COLOR = 0xA36BFF;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TileDimensionalGate te, double x, double y, double z,
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
            drawGatePlane(ticks, 0.0F);
            drawGatePlane(ticks + 9.0F, 90.0F);
            drawEdgeRings(ticks);
            drawRuneRing(ticks);
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

    private void drawGatePlane(float ticks, float yRotation) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(yRotation, 0.0F, 1.0F, 0.0F);
        useNormalBlend();
        drawPortalCore(ticks, yRotation * 0.03D);
        useAdditiveBlend();
        drawStarfield(ticks, yRotation * 0.11D);
        GlStateManager.popMatrix();
    }

    private void drawPortalCore(float ticks, double seed) {
        for (int ring = CORE_RINGS; ring >= 1; ring--) {
            double progress = (double) ring / CORE_RINGS;
            double width = PORTAL_HALF_WIDTH * progress;
            double height = PORTAL_HALF_HEIGHT * progress;
            int color = ring < 4 ? CORE_INNER_COLOR : CORE_OUTER_COLOR;
            float alpha = CORE_ALPHA * (0.20F + 0.80F * (float) progress);
            drawFilledEllipse(width, height, CORE_DEPTH * (ring % 2 == 0 ? 1.0D : -1.0D),
                    color, alpha, ticks, seed + ring * 0.63D);
        }
    }

    private void drawFilledEllipse(double width, double height, double depth, int color,
                                   float alpha, float ticks, double seed) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, depth)
                .color(rgb[0], rgb[1], rgb[2], alpha * 0.92F).endVertex();
        for (int i = 0; i <= ELLIPSE_SEGMENTS; i++) {
            double angle = TWO_PI * i / ELLIPSE_SEGMENTS;
            double swirl = 1.0D
                    + Math.sin(angle * 4.0D + ticks * 0.036D + seed) * 0.040D
                    + Math.cos(angle * 7.0D - ticks * 0.025D + seed) * 0.020D;
            float edgeAlpha = alpha * (0.30F + 0.30F * (float) Math.sin(angle * 2.0D + ticks * 0.03D));
            buffer.pos(Math.cos(angle) * width * swirl,
                            Math.sin(angle) * height * swirl,
                            depth)
                    .color(rgb[0], rgb[1], rgb[2], edgeAlpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawStarfield(float ticks, double seed) {
        for (int i = 0; i < STAR_COUNT; i++) {
            double age = fract(ticks * STAR_FLOW_SPEED * (0.70D + (i % 5) * 0.08D) + i * 0.131D + seed);
            double radius = Math.sqrt(age);
            double angle = i * 2.399963229728653D + ticks * 0.012D + age * 1.6D;
            double x = Math.cos(angle) * PORTAL_HALF_WIDTH * radius * 0.92D;
            double y = Math.sin(angle) * PORTAL_HALF_HEIGHT * radius * 0.92D;
            double z = Math.sin(ticks * 0.030D + i) * 0.035D;
            float fade = (float) Math.sin(age * Math.PI);
            float blink = 0.45F + 0.55F * (float) Math.max(0.0D, Math.sin(ticks * 0.17D + i * 1.9D));
            double size = 0.010D + (i % 4) * 0.004D + blink * 0.010D;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, i % 7 == 0 ? VIOLET_COLOR : STAR_COLOR,
                    STAR_ALPHA * fade * blink, 5, 5);
            GlStateManager.popMatrix();
        }
    }

    private void drawEdgeRings(float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(5.0F);
        drawEllipseLine(PORTAL_HALF_WIDTH + OUTER_RING_WIDTH, PORTAL_HALF_HEIGHT + OUTER_RING_WIDTH,
                EDGE_COLOR, RING_ALPHA * 0.45F, ticks, 0.0D);
        GlStateManager.glLineWidth(2.0F);
        drawEllipseLine(PORTAL_HALF_WIDTH, PORTAL_HALF_HEIGHT,
                EDGE_COLOR, RING_ALPHA, ticks, 1.7D);
        drawEllipseLine(PORTAL_HALF_WIDTH * 0.83D, PORTAL_HALF_HEIGHT * 0.86D,
                VIOLET_COLOR, RING_ALPHA * 0.36F, -ticks * 0.6F, 2.9D);
        RenderHelper.resetLineWidth();
    }

    private void drawEllipseLine(double width, double height, int color, float alpha, float ticks, double seed) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < ELLIPSE_SEGMENTS; i++) {
            double angle = TWO_PI * i / ELLIPSE_SEGMENTS;
            double wave = 1.0D
                    + Math.sin(angle * 6.0D + ticks * 0.032D + seed) * 0.026D
                    + Math.cos(angle * 11.0D - ticks * 0.018D + seed) * 0.014D;
            buffer.pos(Math.cos(angle) * width * wave,
                            Math.sin(angle) * height * wave,
                            0.028D)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawRuneRing(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED, 0.0F, 0.0F, 1.0F);
        GlStateManager.glLineWidth(2.0F);
        drawRunes(PORTAL_HALF_WIDTH + 0.18D, PORTAL_HALF_HEIGHT + 0.18D,
                RUNE_COLOR, RUNE_ALPHA, RUNE_COUNT, ticks);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * RING_ROTATION_SPEED * 0.62F, 0.0F, 0.0F, 1.0F);
        GlStateManager.glLineWidth(1.0F);
        drawRunes(PORTAL_HALF_WIDTH + 0.31D, PORTAL_HALF_HEIGHT + 0.31D,
                EDGE_COLOR, RUNE_ALPHA * 0.48F, RUNE_COUNT / 2, ticks + 17.0F);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawRunes(double width, double height, int color, float alpha, int count, float ticks) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < count; i++) {
            double angle = TWO_PI * i / count;
            double pulse = 0.70D + 0.30D * Math.sin(ticks * 0.080D + i);
            double x = Math.cos(angle) * width;
            double y = Math.sin(angle) * height;
            double tx = -Math.sin(angle);
            double ty = Math.cos(angle);
            double nx = Math.cos(angle);
            double ny = Math.sin(angle);
            double half = (0.030D + (i % 4) * 0.008D) * pulse;
            double lift = i % 3 == 0 ? 0.068D : 0.042D;
            float runeAlpha = alpha * (0.65F + 0.35F * (float) pulse);

            buffer.pos(x - tx * half, y - ty * half, 0.040D)
                    .color(rgb[0], rgb[1], rgb[2], runeAlpha).endVertex();
            buffer.pos(x + tx * half, y + ty * half, 0.040D)
                    .color(rgb[0], rgb[1], rgb[2], runeAlpha).endVertex();

            if ((i & 1) == 0) {
                buffer.pos(x, y, 0.040D)
                        .color(rgb[0], rgb[1], rgb[2], runeAlpha * 0.75F).endVertex();
                buffer.pos(x + nx * lift, y + ny * lift, 0.040D)
                        .color(rgb[0], rgb[1], rgb[2], runeAlpha * 0.55F).endVertex();
            }
        }
        tessellator.draw();
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
