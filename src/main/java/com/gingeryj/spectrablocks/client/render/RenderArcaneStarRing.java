package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileArcaneStarRing;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderArcaneStarRing extends TileEntitySpecialRenderer<TileArcaneStarRing> {

    private static final double CORE_RADIUS = 0.46D;
    private static final double HALO_RADIUS = 1.34D;
    private static final double INNER_RING_RADIUS = 1.32D;
    private static final double MIDDLE_RING_RADIUS = 2.02D;
    private static final double OUTER_RING_RADIUS = 2.72D;
    private static final int RING_SEGMENTS = 144;
    private static final int STAR_COUNT = 54;
    private static final int STAR_LINK_COUNT = 12;
    private static final float CORE_PULSE_SPEED = 0.052F;
    private static final float RING_ROTATION_SPEED = 0.52F;
    private static final float STAR_ORBIT_SPEED = 0.018F;

    @Override
    public void render(TileArcaneStarRing te, double x, double y, double z,
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
            drawCore(ticks);
            drawRuneRings(ticks);
            drawOrbitStars(ticks);
            drawStarLinks(ticks);
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
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
    }

    private void drawCore(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * CORE_PULSE_SPEED);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderHelper.drawSphere(HALO_RADIUS + pulse * 0.12D, 0xFFE7A3, 0.110F + pulse * 0.050F, 24, 24);
        RenderHelper.drawSphere(CORE_RADIUS + pulse * 0.045D, 0xFFF5CC, 0.75F, 22, 22);
        RenderHelper.drawSphere(CORE_RADIUS * 0.55D, 0xFFFFFF, 0.56F + pulse * 0.18F, 18, 18);

        GlStateManager.glLineWidth(2.2F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        RenderEnergyEffectHelper.drawStarRays(0.34D, 1.05D + pulse * 0.12D, 8,
                0xFFF2C1, 0.45F + pulse * 0.20F, ticks * 0.008D);
        GlStateManager.popMatrix();
        RenderHelper.resetLineWidth();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawRuneRings(float ticks) {
        drawRuneRing(ticks, INNER_RING_RADIUS, 42.0F, 0.0F, 1.0F, 0.16F,
                0xEED28C, 0xFFF2C1, 14, 0.82F);
        drawRuneRing(ticks, MIDDLE_RING_RADIUS, -27.0F, 1.0F, 0.0F, -0.11F,
                0xBFA2FF, 0xF2E8FF, 20, -0.48F);
        drawRuneRing(ticks, OUTER_RING_RADIUS, 68.0F, 0.35F, 1.0F, 0.07F,
                0xFFD176, 0xFFF7D5, 28, 0.28F);
    }

    private void drawRuneRing(float ticks, double radius, float tilt, float axisX, float axisZ,
                              float offsetY, int bandColor, int lineColor, int marks, float speedScale) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.037F + radius);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, offsetY, 0.0D);
        GlStateManager.rotate(tilt, axisX, 0.0F, axisZ);
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        RenderEnergyEffectHelper.drawFlatBand(radius, 0.032D, bandColor, 0.130F + pulse * 0.055F, RING_SEGMENTS);
        GlStateManager.glLineWidth(2.0F);
        RenderHelper.drawCircle(radius, lineColor, 0.27F + pulse * 0.12F, RING_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        RenderEnergyEffectHelper.drawRuneMarks(radius, 0.20D, marks,
                lineColor, 0.32F + pulse * 0.16F, ticks * 0.010D * speedScale);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawOrbitStars(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        for (int i = 0; i < STAR_COUNT; i++) {
            double angle = i * 2.399963229728653D + ticks * (STAR_ORBIT_SPEED + (i % 5) * 0.0018D);
            double wave = Math.sin(ticks * 0.035D + i * 0.77D);
            double radius = 1.55D + (i % 11) * 0.135D + wave * 0.10D;
            double height = Math.sin(angle * 1.7D + i) * 0.52D;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double size = 0.028D + (i % 4) * 0.006D;
            float starAlpha = 0.32F + (float) (0.5D + 0.5D * wave) * 0.42F;
            int color = i % 6 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFE6A3 : 0xD9C7FF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, height, z);
            RenderHelper.drawSphere(size, color, starAlpha, 7, 7);
            if (i % 9 == 0) {
                GlStateManager.glLineWidth(1.0F);
                RenderEnergyEffectHelper.drawSpark(size * 2.6D, color, starAlpha * 0.55F);
            }
            GlStateManager.popMatrix();
        }

        RenderHelper.resetLineWidth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawStarLinks(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        GlStateManager.glLineWidth(1.6F);
        for (int i = 0; i < STAR_LINK_COUNT; i++) {
            double angle = i * 0.947D + ticks * 0.014D;
            double angleB = angle + 0.24D + (i % 3) * 0.07D;
            double radiusA = 1.18D + (i % 4) * 0.42D;
            double radiusB = radiusA + 0.34D;
            double yA = Math.sin(ticks * 0.030D + i) * 0.34D;
            double yB = yA + Math.cos(ticks * 0.025D + i * 1.4D) * 0.18D;
            float linkFade = 0.5F + 0.5F * (float) Math.sin(ticks * 0.075F + i * 0.9F);

            RenderHelper.drawLine(Math.cos(angle) * radiusA, yA, Math.sin(angle) * radiusA,
                    Math.cos(angleB) * radiusB, yB, Math.sin(angleB) * radiusB,
                    i % 2 == 0 ? 0xFFF6CF : 0xDCCBFF, 0.16F + linkFade * 0.20F);
        }
        RenderHelper.resetLineWidth();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }
}
