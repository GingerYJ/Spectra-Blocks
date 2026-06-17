package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileGravitationalLens;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderGravitationalLens extends TileEntitySpecialRenderer<TileGravitationalLens> {

    private static final double SHELL_RADIUS = 1.30D;
    private static final double HALO_RADIUS = 1.62D;
    private static final int SHELL_LAT_SEGMENTS = 28;
    private static final int SHELL_LON_SEGMENTS = 28;
    private static final int CIRCLE_SEGMENTS = 160;
    private static final int ARC_SEGMENTS = 72;
    private static final int ARC_COUNT = 9;
    private static final int ORBIT_PARTICLE_COUNT = 24;
    private static final float SHELL_ALPHA = 0.105F;
    private static final float HALO_ALPHA = 0.34F;
    private static final float CAUSTIC_ALPHA = 0.36F;
    private static final float PARTICLE_ALPHA = 0.54F;
    private static final float FOCUS_SPEED = 0.024F;
    private static final float ORBIT_SPEED = 0.026F;
    private static final int SHELL_COLOR = 0xBDEEFF;
    private static final int HALO_COLOR = 0xFFFFFF;
    private static final int CAUSTIC_COLOR = 0x73D7FF;
    private static final int FOCUS_COLOR = 0xEAFBFF;

    @Override
    public void render(TileGravitationalLens te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        RenderQuality.update(centerX, centerY, centerZ);
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
            drawTransparentShell(ticks);
            drawHalos(ticks);
            drawCaustics(ticks);
            drawOrbitParticles(ticks);
            if (!RenderQuality.low()) {
                drawFocusFlash(ticks);
            }
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
            GlStateManager.popMatrix();
        }
    }

    private void drawTransparentShell(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * FOCUS_SPEED);

        useNormalBlend();
        RenderHelper.drawSphere(SHELL_RADIUS + pulse * 0.025D, SHELL_COLOR, SHELL_ALPHA + pulse * 0.035F,
                SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.035F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(16.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.drawWireframeSphere(SHELL_RADIUS * 1.012D, SHELL_COLOR, 0.085F + pulse * 0.040F, 7, 12);
        GlStateManager.popMatrix();
        RenderHelper.resetLineWidth();
    }

    private void drawHalos(float ticks) {
        useAdditiveBlend();
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.030F);

        GlStateManager.glLineWidth(3.0F);
        drawHaloCircle(HALO_RADIUS + pulse * 0.035D, 0.0F, 0.0F, HALO_COLOR, HALO_ALPHA * 0.34F);
        GlStateManager.glLineWidth(1.5F);
        drawHaloCircle(HALO_RADIUS, 90.0F, 0.0F, HALO_COLOR, HALO_ALPHA * 0.48F);
        drawHaloCircle(HALO_RADIUS * 0.82D, 62.0F, 32.0F, CAUSTIC_COLOR, HALO_ALPHA * 0.42F);
        if (!RenderQuality.low()) {
            drawHaloCircle(HALO_RADIUS * 1.08D, -54.0F, -24.0F, CAUSTIC_COLOR, HALO_ALPHA * 0.28F);
        }
        RenderHelper.resetLineWidth();
    }

    private void drawHaloCircle(double radius, float xTilt, float zTilt, int color, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(xTilt, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(zTilt, 0.0F, 0.0F, 1.0F);
        RenderHelper.drawCircle(radius, color, alpha, CIRCLE_SEGMENTS);
        GlStateManager.popMatrix();
    }

    private void drawCaustics(float ticks) {
        useAdditiveBlend();
        GlStateManager.glLineWidth(1.5F);
        int arcCount = RenderQuality.detailCount(ARC_COUNT, 3);
        for (int i = 0; i < arcCount; i++) {
            double radius = 0.38D + i * 0.125D;
            double start = ticks * 0.010D + i * 0.73D;
            double span = Math.PI * (0.72D + (i % 3) * 0.18D);
            float alpha = CAUSTIC_ALPHA * (0.38F + 0.62F
                    * (0.5F + 0.5F * (float) Math.sin(ticks * 0.047F + i)));

            GlStateManager.pushMatrix();
            GlStateManager.rotate(34.0F + i * 7.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(i * 29.0F - ticks * 0.018F, 0.0F, 1.0F, 0.0F);
            drawArc(radius, start, span, i % 2 == 0 ? CAUSTIC_COLOR : HALO_COLOR, alpha);
            if (!RenderQuality.low()) {
                drawArc(radius * 1.08D, start + Math.PI, span * 0.62D, CAUSTIC_COLOR, alpha * 0.45F);
            }
            GlStateManager.popMatrix();
        }
        RenderHelper.resetLineWidth();
    }

    private void drawArc(double radius, double startAngle, double span, int color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int segments = RenderQuality.scaleSegments(ARC_SEGMENTS, 10, ARC_SEGMENTS);
        for (int i = 0; i <= segments; i++) {
            double progress = (double) i / segments;
            double angle = startAngle + span * progress;
            double shimmer = Math.sin(progress * Math.PI * 3.0D + startAngle) * 0.025D;
            float fade = alpha * (float) Math.sin(progress * Math.PI);
            buffer.pos(Math.cos(angle) * (radius + shimmer),
                            Math.sin(angle) * (radius * 0.42D),
                            Math.sin(angle) * radius)
                    .color(rgb[0], rgb[1], rgb[2], fade).endVertex();
        }
        tessellator.draw();
    }

    private void drawOrbitParticles(float ticks) {
        useAdditiveBlend();
        int stride = RenderQuality.detailStride();
        for (int i = 0; i < ORBIT_PARTICLE_COUNT; i += stride) {
            int orbit = i % 3;
            double angle = ticks * (ORBIT_SPEED + orbit * 0.006F) + i * Math.PI * 2.0D / ORBIT_PARTICLE_COUNT;
            double longRadius = 1.48D + orbit * 0.11D;
            double shortRadius = 0.46D + orbit * 0.08D;
            double x = Math.cos(angle) * longRadius;
            double y = Math.sin(angle * 1.7D + orbit) * 0.16D;
            double z = Math.sin(angle) * shortRadius;
            float twinkle = 0.5F + 0.5F * (float) Math.sin(ticks * 0.14F + i * 1.41F);
            float alpha = PARTICLE_ALPHA * (0.35F + twinkle * 0.65F);
            double size = 0.018D + twinkle * 0.026D;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(orbit * 39.0F - 22.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(orbit * 54.0F + 18.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, i % 4 == 0 ? HALO_COLOR : CAUSTIC_COLOR, alpha, 7, 7);
            GlStateManager.popMatrix();
        }

        GlStateManager.glLineWidth(1.0F);
        int orbitCount = RenderQuality.low() ? 2 : 3;
        for (int orbit = 0; orbit < orbitCount; orbit++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(orbit * 39.0F - 22.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(orbit * 54.0F + 18.0F, 0.0F, 1.0F, 0.0F);
            drawEllipse(1.48D + orbit * 0.11D, 0.46D + orbit * 0.08D,
                    orbit == 1 ? HALO_COLOR : CAUSTIC_COLOR, 0.075F);
            GlStateManager.popMatrix();
        }
        RenderHelper.resetLineWidth();
    }

    private void drawEllipse(double longRadius, double shortRadius, int color, float alpha) {
        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        int segments = RenderQuality.scaleSegments(CIRCLE_SEGMENTS, 16, CIRCLE_SEGMENTS);
        for (int i = 0; i < segments; i++) {
            double angle = Math.PI * 2.0D * i / segments;
            buffer.pos(Math.cos(angle) * longRadius, 0.0D, Math.sin(angle) * shortRadius)
                    .color(rgb[0], rgb[1], rgb[2], alpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawFocusFlash(float ticks) {
        useAdditiveBlend();
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * FOCUS_SPEED);
        pulse = pulse * pulse * pulse;

        RenderHelper.drawSphere(0.110D + pulse * 0.070D, FOCUS_COLOR, 0.18F + pulse * 0.38F, 12, 12);
        GlStateManager.glLineWidth(2.0F);
        RenderHelper.drawLine(-0.38D - pulse * 0.12D, 0.0D, 0.0D,
                0.38D + pulse * 0.12D, 0.0D, 0.0D, HALO_COLOR, pulse * 0.44F);
        RenderHelper.drawLine(0.0D, -0.22D - pulse * 0.08D, 0.0D,
                0.0D, 0.22D + pulse * 0.08D, 0.0D, HALO_COLOR, pulse * 0.32F);
        RenderHelper.drawLine(0.0D, 0.0D, -0.38D - pulse * 0.12D,
                0.0D, 0.0D, 0.38D + pulse * 0.12D, CAUSTIC_COLOR, pulse * 0.36F);
        RenderHelper.resetLineWidth();
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
