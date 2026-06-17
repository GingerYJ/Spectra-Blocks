package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TilePlasmaStorm;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderPlasmaStorm extends TileEntitySpecialRenderer<TilePlasmaStorm> {

    private static final double CORE_RADIUS = 0.62D;
    private static final double INNER_GLOW_RADIUS = 1.22D;
    private static final double STORM_RADIUS = 3.35D;
    private static final int STORM_SEGMENTS = 160;
    private static final int STORM_PARTICLE_COUNT = 116;
    private static final int ARC_COUNT = 9;
    private static final float CORE_PULSE_SPEED = 0.085F;
    private static final float BAND_ROTATION_SPEED = 1.35F;
    private static final float PARTICLE_SPEED = 0.048F;
    private static final float ARC_CYCLE_SPEED = 0.023F;

    @Override
    public void render(TilePlasmaStorm te, double x, double y, double z,
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
            drawStormShell(ticks);
            drawStormBands(ticks);
            drawFastParticles(ticks);
            if (!RenderQuality.low()) {
                drawLightning(ticks);
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
        RenderHelper.drawSphere(INNER_GLOW_RADIUS + pulse * 0.20D, 0x23E8FF, 0.20F + pulse * 0.08F, 24, 24);
        RenderHelper.drawSphere(CORE_RADIUS + pulse * 0.05D, 0xFFFFFF, 0.58F + pulse * 0.18F, 22, 22);
        RenderHelper.drawSphere(CORE_RADIUS * 0.72D, 0x78F6FF, 0.58F, 20, 20);
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawStormShell(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.040F);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderHelper.drawSphere(STORM_RADIUS + pulse * 0.16D, 0x0CB9FF, 0.065F + pulse * 0.030F, 30, 30);
        GlStateManager.glLineWidth(1.6F);
        RenderHelper.drawWireframeSphere(STORM_RADIUS * 0.98D, 0x77F3FF, 0.105F + pulse * 0.045F, 9, 16);
        RenderHelper.resetLineWidth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawStormBands(float ticks) {
        drawStormBand(ticks, 34.0F, 0.0F, 1.0F, 0.0F, 0x19CFFF, 0.62F);
        drawStormBand(ticks, -52.0F, 1.0F, 0.15F, 0.25F, 0xA85CFF, -0.48F);
        if (!RenderQuality.low()) {
            drawStormBand(ticks, 72.0F, 0.35F, 1.0F, 0.0F, 0xF8FEFF, 0.32F);
            drawStormBand(ticks, -18.0F, 1.0F, 0.0F, 0.45F, 0x00F0B8, -0.86F);
        }
    }

    private void drawStormBand(float ticks, float tilt, float axisX, float axisY, float axisZ,
                               int color, float speedScale) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.061F + tilt * 0.04F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(tilt, axisX, axisY, axisZ);
        GlStateManager.rotate(ticks * BAND_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);
        RenderEnergyEffectHelper.drawFlatBand(STORM_RADIUS * (0.68D + pulse * 0.06D),
                0.055D + pulse * 0.018D, color, 0.105F + pulse * 0.055F, STORM_SEGMENTS);
        GlStateManager.glLineWidth(2.4F);
        RenderHelper.drawCircle(STORM_RADIUS * (0.68D + pulse * 0.06D),
                color, 0.22F + pulse * 0.12F, STORM_SEGMENTS);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawFastParticles(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int stride = RenderQuality.detailStride();
        for (int i = 0; i < STORM_PARTICLE_COUNT; i += stride) {
            double baseAngle = i * 2.399963229728653D;
            double angle = baseAngle + ticks * (PARTICLE_SPEED + (i % 7) * 0.004D);
            double normalizedY = -0.92D + (i % 47) * (1.84D / 46.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - normalizedY * normalizedY));
            double surge = 0.5D + 0.5D * Math.sin(ticks * 0.105D + i * 0.73D);
            double radius = STORM_RADIUS * (0.72D + surge * 0.20D);
            double x = Math.cos(angle) * horizontal * radius;
            double y = normalizedY * radius * 0.64D + Math.sin(ticks * 0.14D + i) * 0.10D;
            double z = Math.sin(angle) * horizontal * radius;
            double size = 0.018D + surge * 0.040D;
            float particleAlpha = 0.20F + (float) surge * 0.52F;
            int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x43F1FF : 0xBD6DFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            RenderHelper.drawSphere(size, color, particleAlpha, 6, 6);
            GlStateManager.popMatrix();
        }

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawLightning(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int arcCount = RenderQuality.detailCount(ARC_COUNT, 3);
        for (int i = 0; i < arcCount; i++) {
            double flashPhase = (ticks * ARC_CYCLE_SPEED + i * 0.137D) % 1.0D;
            float flash = (float) Math.max(0.0D, Math.sin(Math.PI * flashPhase));
            double angle = i * 0.6981317007977318D + ticks * (0.020D + (i % 4) * 0.006D);
            double length = 0.62D + (i % 3) * 0.22D;
            double y = -1.15D + (i % 5) * 0.55D;

            GlStateManager.glLineWidth(3.0F);
            RenderEnergyEffectHelper.drawJaggedArc(STORM_RADIUS * (0.70D + (i % 4) * 0.045D),
                    angle, length, y, 0.36D + (i % 2) * 0.20D,
                    0.115D, 8, 0xC7FBFF, flash * 0.26F, ticks, 97 + i * 31);
            GlStateManager.glLineWidth(1.4F);
            RenderEnergyEffectHelper.drawJaggedArc(STORM_RADIUS * (0.70D + (i % 4) * 0.045D),
                    angle, length, y, 0.36D + (i % 2) * 0.20D,
                    0.090D, 8, i % 2 == 0 ? 0xFFFFFF : 0xC678FF,
                    flash * 0.55F, ticks, 197 + i * 31);
        }

        RenderHelper.resetLineWidth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }
}
