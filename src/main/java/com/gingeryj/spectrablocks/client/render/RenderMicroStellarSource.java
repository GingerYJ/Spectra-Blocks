package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderMicroStellarSource extends TileEntitySpecialRenderer<TileMicroStellarSource> {

    private static final double SHELL_RADIUS = 5.45D;
    private static final double OUTER_HALO_RADIUS = 5.88D;
    private static final int PARTICLE_COUNT = 220;
    private static final int FLARE_COUNT = 48;
    private static final int PROMINENCE_COUNT = 12;
    private static final int PROMINENCE_POINTS_PER_ARC = 10;
    private static final RenderHelper.BillboardPoint[] PARTICLES =
            RenderHelper.createBillboardPoints(PARTICLE_COUNT + FLARE_COUNT);
    private static final RenderHelper.BillboardPoint[] PROMINENCE_POINTS =
            RenderHelper.createBillboardPoints(PROMINENCE_COUNT * PROMINENCE_POINTS_PER_ARC);
    private static final int SHADER_SPHERE_LAT_SEGS = 56;
    private static final int SHADER_SPHERE_LON_SEGS = 56;

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
            drawProminences(ticks);
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
        RenderHelper.drawSphere(SHELL_RADIUS * 1.012D + 0.020D * pulse, 0xDFFFFF, 0.060F + 0.030F * pulse, 28, 28);
        RenderHelper.drawSphere(OUTER_HALO_RADIUS - 0.26D + 0.070D * pulse, 0x58F6FF, 0.070F + 0.040F * pulse, 30, 30);
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
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.34F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(9.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        drawShaderShell(ticks, pulse);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    private void drawShaderShell(float ticks, float pulse) {
        ShaderProgram program = ShaderManager.getProgram("stellar_source");
        if (program == null || !program.begin()) {
            return;
        }

        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        try {
            GlStateManager.disableTexture2D();
            program.setUniform1f("uTime", ticks * 0.045F);
            program.setUniform3f("uBaseColor", 0.18F, 0.82F, 1.0F);
            program.setUniform1f("uRimIntensity", 0.92F);
            program.setUniform1f("uPulseAmount", 0.92F + pulse * 0.12F);
            program.setUniform1f("uNoiseSpeed", 0.62F);
            drawShaderSphere(SHELL_RADIUS + 0.035D * pulse, SHADER_SPHERE_LAT_SEGS, SHADER_SPHERE_LON_SEGS);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("stellar_source render failed: " + ex.getMessage());
        } finally {
            program.end();
            if (textureWasEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
        }
    }

    private static void drawShaderSphere(double radius, int latSegs, int lonSegs) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = 2.0D * Math.PI * lon / lonSegs;
                double phi1 = 2.0D * Math.PI * (lon + 1) / lonSegs;
                addShaderSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addShaderSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addShaderSphereVertex(buffer, radius, theta1, phi0, lon / (double) lonSegs, (lat + 1.0D) / latSegs);
                addShaderSphereVertex(buffer, radius, theta0, phi1, (lon + 1.0D) / lonSegs, lat / (double) latSegs);
                addShaderSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addShaderSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
            }
        }
        tessellator.draw();
    }

    private static void addShaderSphereVertex(BufferBuilder buffer, double radius, double theta, double phi, double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private void drawProminences(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int pointCount = 0;
        for (int i = 0; i < PROMINENCE_COUNT; i++) {
            double phase = ticks * (0.018D + (i % 4) * 0.003D) + i * 1.771D;
            double surge = Math.max(0.0D, Math.sin(phase * 1.7D + Math.sin(phase * 0.63D) * 0.9D));
            surge = surge * surge;
            if (surge < 0.12D) {
                continue;
            }

            double baseYaw = i * 2.399963229728653D + ticks * (0.009D + (i % 3) * 0.002D);
            double basePitch = -0.72D + (i % 7) * 0.24D + Math.sin(phase) * 0.08D;
            double arcLift = 0.24D + surge * (0.55D + (i % 3) * 0.12D);
            double sideSweep = (0.18D + surge * 0.20D) * (i % 2 == 0 ? 1.0D : -1.0D);

            for (int step = 0; step < PROMINENCE_POINTS_PER_ARC; step++) {
                double progress = step / (double) (PROMINENCE_POINTS_PER_ARC - 1);
                double crest = Math.sin(Math.PI * progress);
                double yaw = baseYaw + sideSweep * (progress - 0.5D)
                        + Math.sin(phase + progress * 5.3D) * 0.025D;
                double pitch = basePitch + crest * arcLift;
                double horizontal = Math.cos(pitch);
                double radius = SHELL_RADIUS * (1.012D + crest * (0.075D + surge * 0.070D));
                double x = Math.cos(yaw) * horizontal * radius;
                double y = Math.sin(pitch) * radius;
                double z = Math.sin(yaw) * horizontal * radius;
                double size = 0.045D + crest * (0.070D + surge * 0.060D);
                float alpha = (float) ((0.10D + crest * 0.34D + surge * 0.20D) * surge);
                int color = step % 3 == 0 ? 0xEFFFFF : (i % 2 == 0 ? 0x8EFFFF : 0x37DFFF);

                PROMINENCE_POINTS[pointCount++].set(x, y, z, size, color, alpha);
            }
        }

        RenderHelper.drawBillboardGlowPoints(PROMINENCE_POINTS, pointCount);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawActiveParticles(float ticks) {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int pointCount = 0;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double baseYaw = i * 2.399963229728653D;
            double y = -0.96D + (i % 45) * (1.92D / 44.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double flutter = 0.5D + 0.5D * Math.sin(ticks * (0.090D + (i % 9) * 0.011D) + i * 1.731D);
            double surge = Math.max(0.0D, Math.sin(ticks * (0.052D + (i % 6) * 0.006D) + i * 0.91D));
            surge = surge * surge * surge;
            double wobbleA = Math.sin(ticks * 0.115D + i * 2.17D) * 0.070D;
            double wobbleB = Math.cos(ticks * 0.093D + i * 1.37D) * 0.055D;
            double radius = SHELL_RADIUS * (1.004D + flutter * 0.026D + surge * 0.072D) + wobbleB;
            double yaw = baseYaw + ticks * (0.015D + (i % 7) * 0.002D) + wobbleA;
            double particleX = Math.cos(yaw) * horizontal * radius;
            double particleY = y * radius + Math.sin(ticks * 0.120D + i) * (0.070D + surge * 0.120D);
            double particleZ = Math.sin(yaw) * horizontal * radius;
            double size = 0.026D + flutter * 0.036D + surge * 0.060D;
            float alpha = (float) (0.14D + flutter * 0.20D + surge * 0.32D);
            int color = surge > 0.70D ? 0xF4FFFF : (i % 5 == 0 ? 0x9CFFFF : (i % 3 == 0 ? 0x5BEAFF : 0x18CFFF));

            PARTICLES[pointCount++].set(particleX, particleY, particleZ, size, color, alpha);
        }

        for (int i = 0; i < FLARE_COUNT; i++) {
            double pulse = Math.max(0.0D, Math.sin(ticks * (0.105D + (i % 4) * 0.015D) + i * 2.41D));
            pulse = pulse * pulse * pulse * pulse;
            if (pulse <= 0.018D) {
                continue;
            }

            double baseYaw = i * 2.399963229728653D + ticks * (0.030D + (i % 5) * 0.004D);
            double y = -0.88D + (i % 12) * (1.76D / 11.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double radius = SHELL_RADIUS * (1.045D + pulse * 0.090D);
            double particleX = Math.cos(baseYaw) * horizontal * radius;
            double particleY = y * radius + Math.sin(ticks * 0.160D + i * 0.7D) * 0.13D;
            double particleZ = Math.sin(baseYaw) * horizontal * radius;
            double size = 0.060D + pulse * 0.080D;
            float alpha = (float) (0.22D + pulse * 0.46D);
            int color = i % 3 == 0 ? 0xDFFFFF : 0x63EFFF;

            PARTICLES[pointCount++].set(particleX, particleY, particleZ, size, color, alpha);
        }

        RenderHelper.drawBillboardGlowPoints(PARTICLES, pointCount);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderHelper.resetLineWidth();
    }
}
