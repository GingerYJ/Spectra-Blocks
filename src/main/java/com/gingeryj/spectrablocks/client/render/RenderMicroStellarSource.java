package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderMicroStellarSource extends TileEntitySpecialRenderer<TileMicroStellarSource> {

    private static final double SHELL_RADIUS = 5.45D;
    private static final double OUTER_HALO_RADIUS = 5.88D;
    private static final int PARTICLE_COUNT = 180;
    private static final RenderHelper.BillboardPoint[] PARTICLES =
            RenderHelper.createBillboardPoints(PARTICLE_COUNT);
    private static final ResourceLocation STELLAR_TEXTURE =
            new ResourceLocation(Reference.MOD_ID, "textures/effects/planets/micro_stellar_source.png");

    @Override
    public void render(TileMicroStellarSource te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(ModConfig.microStellarSourceScale());
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
        GlStateManager.disableCull();

        try {
            drawCore(ticks);
            drawOuterRadiance(ticks);
            drawActiveParticles(ticks);
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
            RenderHelper.resetLineWidth();
            GlStateManager.popMatrix();
        }
    }

    private void drawOuterRadiance(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.028F);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        RenderHelper.drawSphere(OUTER_HALO_RADIUS + 0.08D * pulse, 0xDDFEFF, 0.180F + 0.060F * pulse, 36, 36);
        RenderHelper.drawWireframeSphere(OUTER_HALO_RADIUS + 0.02D * pulse, 0xFFFFFF, 0.130F + 0.050F * pulse, 12, 24);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawCore(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.05F);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderHelper.drawSphere(1.18D + 0.08D * pulse, 0x9DEEFF, 0.32F, 30, 30);
        RenderHelper.drawSphere(0.78D + 0.04D * pulse, 0xF3FFFF, 0.28F, 26, 26);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.34F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(9.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        RenderHelper.drawTexturedSphere(SHELL_RADIUS, STELLAR_TEXTURE, 1.0F, 56, 56);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    private void drawActiveParticles(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double baseYaw = i * 2.399963229728653D;
            double y = -0.96D + (i % 45) * (1.92D / 44.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double flutter = 0.5D + 0.5D * Math.sin(ticks * (0.090D + (i % 9) * 0.011D) + i * 1.731D);
            double surge = Math.max(0.0D, Math.sin(ticks * (0.052D + (i % 6) * 0.006D) + i * 0.91D));
            surge = surge * surge * surge;
            double wobbleA = Math.sin(ticks * 0.115D + i * 2.17D) * 0.070D;
            double wobbleB = Math.cos(ticks * 0.093D + i * 1.37D) * 0.055D;
            double radius = SHELL_RADIUS * (1.012D + flutter * 0.045D + surge * 0.120D) + wobbleB;
            double yaw = baseYaw + ticks * (0.015D + (i % 7) * 0.002D) + wobbleA;
            double particleX = Math.cos(yaw) * horizontal * radius;
            double particleY = y * radius + Math.sin(ticks * 0.120D + i) * (0.070D + surge * 0.120D);
            double particleZ = Math.sin(yaw) * horizontal * radius;
            double size = 0.018D + flutter * 0.030D + surge * 0.095D;
            float alpha = (float) (0.18D + flutter * 0.24D + surge * 0.45D);
            int color = surge > 0.62D ? 0xFFFFFF : (i % 3 == 0 ? 0xDDFEFF : 0x3DE5FF);

            PARTICLES[i].set(particleX, particleY, particleZ, size * 2.15D, color, alpha);
        }

        RenderHelper.drawBillboardGlowPoints(PARTICLES, PARTICLE_COUNT);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderHelper.resetLineWidth();
    }
}
