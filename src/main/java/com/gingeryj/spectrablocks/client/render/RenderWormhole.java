package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileWormhole;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderWormhole extends TileEntitySpecialRenderer<TileWormhole> {

    private static final double CORE_RADIUS = 0.50D;
    private static final double INNER_SHELL_RADIUS = 0.80D;
    private static final double OUTER_HALO_RADIUS = 1.72D;
    private static final int SPHERE_LAT_SEGMENTS = 24;
    private static final int SPHERE_LON_SEGMENTS = 24;
    private static final int RING_SEGMENTS = 160;
    private static final int SPIRAL_ARMS = 5;
    private static final int SPIRAL_POINTS = 76;
    private static final int PARTICLE_COUNT = 76;
    private static final float CORE_ROTATION_SPEED = 1.30F;
    private static final float OUTER_ROTATION_SPEED = -0.52F;
    private static final float RING_ROTATION_SPEED = -0.82F;
    private static final float PARTICLE_ALPHA = 0.58F;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int CORE_COLOR = 0x010007;
    private static final int INNER_COLOR = 0x10114C;
    private static final int OUTER_COLOR = 0x295CFF;
    private static final int RING_COLOR = 0x7FEAFF;
    private static final int HOT_COLOR = 0xFFFFFF;

    @Override
    public void render(TileWormhole te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
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
            drawCore(ticks);
            drawSwirl(ticks);
            drawGlowingRings(ticks);
            drawInwardParticles(ticks);
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

    private void drawCore(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.052F);

        useNormalBlend();
        RenderHelper.drawSphere(CORE_RADIUS + 0.025D * pulse, CORE_COLOR, 0.96F,
                SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);

        useAdditiveBlend();
        RenderHelper.drawSphere(INNER_SHELL_RADIUS + 0.035D * pulse, INNER_COLOR, 0.22F + 0.08F * pulse,
                SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        RenderHelper.drawSphere(OUTER_HALO_RADIUS * 0.78D + 0.05D * pulse, OUTER_COLOR, 0.075F,
                SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
    }

    private void drawSwirl(float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * CORE_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(68.0F, 1.0F, 0.0F, 0.0F);

        GlStateManager.glLineWidth(3.0F);
        for (int arm = 0; arm < SPIRAL_ARMS; arm++) {
            drawSpiralArm(arm, ticks, OUTER_COLOR, 0.26F);
        }

        GlStateManager.glLineWidth(1.5F);
        for (int arm = 0; arm < SPIRAL_ARMS; arm++) {
            drawSpiralArm(arm, ticks + 8.0F, RING_COLOR, 0.42F);
        }
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawSpiralArm(int arm, float ticks, int color, float alpha) {
        float[] rgb = RenderHelper.unpackRGB(color);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double armOffset = arm * Math.PI * 2.0D / SPIRAL_ARMS;
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < SPIRAL_POINTS; i++) {
            double progress = (double) i / (SPIRAL_POINTS - 1);
            double radius = 0.26D + progress * 1.55D;
            double angle = armOffset + progress * Math.PI * 4.6D - ticks * 0.034D;
            double wave = Math.sin(progress * Math.PI * 6.0D + ticks * 0.060D + arm) * 0.055D;
            float pointAlpha = alpha * (float) Math.sin(progress * Math.PI);
            buffer.pos(Math.cos(angle) * radius,
                            wave,
                            Math.sin(angle) * radius)
                    .color(rgb[0], rgb[1], rgb[2], pointAlpha).endVertex();
        }
        tessellator.draw();
    }

    private void drawGlowingRings(float ticks) {
        useAdditiveBlend();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * OUTER_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        drawTiltedRing(0.92D, 62.0F, 0.0F, RING_COLOR, 0.40F, 3.0F);
        drawTiltedRing(1.28D, -54.0F, 34.0F, OUTER_COLOR, 0.30F, 2.0F);
        drawTiltedRing(1.68D, 76.0F, -28.0F, HOT_COLOR, 0.16F, 1.5F);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * RING_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        drawTiltedRing(1.48D, 18.0F, 90.0F, 0x6D4DFF, 0.20F, 2.0F);
        GlStateManager.popMatrix();
    }

    private void drawTiltedRing(double radius, float xTilt, float zTilt, int color, float alpha, float width) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(xTilt, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(zTilt, 0.0F, 0.0F, 1.0F);
        GlStateManager.glLineWidth(width + 2.0F);
        RenderHelper.drawCircle(radius, color, alpha * 0.35F, RING_SEGMENTS);
        GlStateManager.glLineWidth(width);
        RenderHelper.drawCircle(radius, color, alpha, RING_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawInwardParticles(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double progress = fract(ticks * (0.012D + (i % 4) * 0.0016D) + i * 0.113D);
            double radius = 1.82D - progress * 1.44D;
            double angle = i * GOLDEN_ANGLE + ticks * 0.070D + progress * Math.PI * 3.0D;
            double y = Math.sin(angle * 1.7D + i) * 0.31D * (1.0D - progress * 0.55D);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double sparkle = Math.max(0.0D, Math.sin(ticks * 0.18D + i * 1.73D));
            float fade = (float) Math.sin(progress * Math.PI);
            float alpha = PARTICLE_ALPHA * fade * (0.45F + 0.55F * (float) sparkle);
            double size = 0.020D + (1.0D - progress) * 0.030D + sparkle * 0.018D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, sparkle > 0.72D ? HOT_COLOR : RING_COLOR, alpha, 7, 7);
            GlStateManager.popMatrix();

            double tailRadius = radius + 0.13D;
            double tailAngle = angle - 0.16D;
            GlStateManager.glLineWidth(1.0F);
            RenderHelper.drawLine(x, y, z,
                    Math.cos(tailAngle) * tailRadius,
                    y * 1.06D,
                    Math.sin(tailAngle) * tailRadius,
                    OUTER_COLOR, alpha * 0.42F);
        }
        RenderHelper.resetLineWidth();
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
