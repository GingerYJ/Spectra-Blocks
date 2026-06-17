package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileVoidCrystal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderVoidCrystal extends TileEntitySpecialRenderer<TileVoidCrystal> {

    private static final double CRYSTAL_RADIUS = 0.48D;
    private static final double CRYSTAL_HEIGHT = 1.82D;
    private static final double INNER_HALO_RADIUS = 1.18D;
    private static final double OUTER_HALO_RADIUS = 2.15D;
    private static final double RUNE_RING_RADIUS = 1.38D;
    private static final int CRYSTAL_FACETS = 7;
    private static final int RUNE_SEGMENTS = 112;
    private static final int RUNE_MARKS = 18;
    private static final int INWARD_PARTICLE_COUNT = 42;
    private static final int ARC_COUNT = 5;
    private static final float CRYSTAL_ROTATION_SPEED = 0.34F;
    private static final float PARTICLE_PULL_SPEED = 0.018F;
    private static final float RUNE_ROTATION_SPEED = 0.90F;
    private static final float ARC_ROTATION_SPEED = 0.042F;

    @Override
    public void render(TileVoidCrystal te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        if (!RenderQuality.shouldRender(centerX, centerY, centerZ)) {
            return;
        }
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
            drawHalo(ticks);
            drawCrystal(ticks);
            drawRuneRing(ticks);
            drawInwardParticles(ticks);
            if (!RenderQuality.low()) {
                drawShortArcs(ticks);
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

    private void drawHalo(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.045F);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderHelper.drawSphere(INNER_HALO_RADIUS + pulse * 0.08D, 0x4B168B, 0.115F + pulse * 0.045F, 20, 20);
        RenderHelper.drawSphere(OUTER_HALO_RADIUS + pulse * 0.16D, 0x1A052C, 0.080F + pulse * 0.035F, 24, 24);
        GlStateManager.glLineWidth(1.4F);
        RenderHelper.drawWireframeSphere(OUTER_HALO_RADIUS * 0.86D, 0x884CFF, 0.085F + pulse * 0.040F, 7, 12);
        RenderHelper.resetLineWidth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawCrystal(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.060F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * CRYSTAL_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(7.5F, 0.35F, 0.0F, 1.0F);
        RenderEnergyEffectHelper.drawFacetedCrystal(CRYSTAL_RADIUS, CRYSTAL_HEIGHT,
                0x150020, 0x3F0F68, 0.58F + pulse * 0.08F, CRYSTAL_FACETS);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.glLineWidth(2.2F);
        RenderEnergyEffectHelper.drawCrystalEdges(CRYSTAL_RADIUS * 1.015D, CRYSTAL_HEIGHT * 1.010D,
                0xB67CFF, 0.34F + pulse * 0.12F, CRYSTAL_FACETS);
        RenderHelper.resetLineWidth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.popMatrix();
    }

    private void drawRuneRing(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.035F);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(58.0F, 1.0F, 0.0F, 0.20F);
        GlStateManager.rotate(ticks * RUNE_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        RenderEnergyEffectHelper.drawFlatBand(RUNE_RING_RADIUS, 0.035D, 0x3E155F, 0.18F, RUNE_SEGMENTS);
        GlStateManager.glLineWidth(2.4F);
        RenderHelper.drawCircle(RUNE_RING_RADIUS, 0xB287FF, 0.30F + pulse * 0.12F, RUNE_SEGMENTS);
        GlStateManager.glLineWidth(1.2F);
        RenderEnergyEffectHelper.drawRuneMarks(RUNE_RING_RADIUS, 0.22D, RUNE_MARKS,
                0xD7B7FF, 0.36F + pulse * 0.16F, ticks * 0.015D);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawInwardParticles(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int stride = RenderQuality.detailStride();
        for (int i = 0; i < INWARD_PARTICLE_COUNT; i += stride) {
            double progress = (ticks * PARTICLE_PULL_SPEED + i * 0.061D) % 1.0D;
            double radius = 2.35D * (1.0D - progress) + 0.20D;
            double angle = i * 2.399963229728653D + ticks * (0.012D + (i % 5) * 0.002D);
            double height = Math.sin(i * 1.618D + ticks * 0.027D) * (0.70D * (1.0D - progress));
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double size = 0.024D + (1.0D - progress) * 0.020D;
            float fade = (float) Math.sin(Math.PI * progress);
            int color = i % 3 == 0 ? 0xE8CCFF : 0x8D4CFF;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, height, z);
            RenderHelper.drawSphere(size, color, 0.28F + fade * 0.45F, 6, 6);
            GlStateManager.popMatrix();
        }

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawShortArcs(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int arcCount = RenderQuality.detailCount(ARC_COUNT, 2);
        for (int i = 0; i < arcCount; i++) {
            double phase = ticks * ARC_ROTATION_SPEED + i * 1.2566370614359172D;
            double arcPhase = (ticks * 0.013D + i * 0.21D) % 1.0D;
            float arcAlpha = (float) Math.sin(Math.PI * arcPhase) * 0.46F;
            GlStateManager.glLineWidth(2.0F + (i % 2));
            RenderEnergyEffectHelper.drawJaggedArc(0.92D + (i % 3) * 0.20D,
                    phase, 0.46D + (i % 2) * 0.18D,
                    -0.42D + (i % 4) * 0.26D,
                    0.18D + (i % 2) * 0.08D,
                    0.055D, 7, i % 2 == 0 ? 0xF0D7FF : 0x9B5BFF,
                    arcAlpha, ticks, i * 23 + 5);
        }

        RenderHelper.resetLineWidth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }
}
