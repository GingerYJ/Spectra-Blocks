package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
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
    private static final int BILLBOARD_SEGMENTS = 8;
    private static final double[] BILLBOARD_COS = new double[BILLBOARD_SEGMENTS];
    private static final double[] BILLBOARD_SIN = new double[BILLBOARD_SEGMENTS];
    private static final StellarPoint[] PARTICLES = createPoints(PARTICLE_COUNT + FLARE_COUNT);
    private static final StellarPoint[] PROMINENCE_POINTS = createPoints(PROMINENCE_COUNT * PROMINENCE_POINTS_PER_ARC);
    private static final int SHADER_SPHERE_LAT_SEGS = 56;
    private static final int SHADER_SPHERE_LON_SEGS = 56;

    static {
        for (int i = 0; i < BILLBOARD_SEGMENTS; i++) {
            double angle = Math.PI * 2.0D * i / BILLBOARD_SEGMENTS;
            BILLBOARD_COS[i] = Math.cos(angle);
            BILLBOARD_SIN[i] = Math.sin(angle);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileMicroStellarSource te) {
        return true;
    }

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

        SpectraRenderState.State renderState = SpectraRenderState.beginIsolated();
        try {
            drawCore(ticks);
            drawOuterRadiance(ticks);
            drawProminences(ticks);
            drawActiveParticles(ticks);
        } finally {
            renderState.close();
            GlStateManager.popMatrix();
        }
    }

    private void drawOuterRadiance(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.028F);
        ShaderProgram shader = ShaderManager.getProgram("natural_effect");
        if (shader == null) {
            return;
        }

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        RenderNaturalShaderHelper.drawNaturalSphere(shader, SHELL_RADIUS * 1.018D + 0.030D * pulse,
                RenderNaturalShaderHelper.MODE_SOLAR, 1.6F, 0xFFFFFF, 0xBFFFFF, 0xF7FFFF,
                0.085F + 0.035F * pulse, pulse, 0.62F, ticks * 0.040F, 211.0F, 30, 30);
        RenderNaturalShaderHelper.drawNaturalSphere(shader, OUTER_HALO_RADIUS + 0.120D * pulse,
                RenderNaturalShaderHelper.MODE_SOLAR, 2.2F, 0xDDFEFF, 0x62EFFF, 0xFFFFFF,
                0.145F + 0.055F * pulse, pulse, 0.78F, ticks * 0.036F, 227.0F, 34, 34);
        RenderNaturalShaderHelper.drawNaturalSphere(shader, OUTER_HALO_RADIUS + 0.38D + 0.160D * pulse,
                RenderNaturalShaderHelper.MODE_SOLAR, 2.8F, 0x62EFFF, 0x2DBDFF, 0xF7FFFF,
                0.055F + 0.035F * pulse, pulse, 0.54F, ticks * 0.032F, 241.0F, 28, 28);
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
            program.setUniform1f("uRimIntensity", 1.35F);
            program.setUniform1f("uPulseAmount", 0.75F + pulse * 0.35F);
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
                int color = step % 3 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0xFFF0A8 : 0xAFFFFF);

                PROMINENCE_POINTS[pointCount++].set(x, y, z, size, color, alpha);
            }
        }

        drawShaderBillboardGlowPoints(ShaderManager.getProgram("basic"), PROMINENCE_POINTS, pointCount);

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
            int color = surge > 0.70D ? 0xF5FFFF : (i % 5 == 0 ? 0xFFF4B8 : (i % 3 == 0 ? 0xBDFEFF : 0x39DFFF));

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
            int color = i % 3 == 0 ? 0xFFF1A8 : 0xF8FFFF;

            PARTICLES[pointCount++].set(particleX, particleY, particleZ, size, color, alpha);
        }

        drawShaderBillboardGlowPoints(ShaderManager.getProgram("basic"), PARTICLES, pointCount);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.glLineWidth(1.0F);
    }

    private static StellarPoint[] createPoints(int count) {
        StellarPoint[] points = new StellarPoint[count];
        for (int i = 0; i < count; i++) {
            points[i] = new StellarPoint();
        }
        return points;
    }

    private static void drawShaderBillboardGlowPoints(ShaderProgram shader, StellarPoint[] points, int count) {
        if (shader == null || count <= 0 || !shader.begin()) {
            return;
        }

        try {
            shader.setUniform1f("alpha", 1.0F);
            shader.setUniform4f("tint", 1.0F, 1.0F, 1.0F, 1.0F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            double yaw = Math.toRadians(renderManager.playerViewY);
            double pitch = Math.toRadians(renderManager.playerViewX);
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
                pitch = -pitch;
            }

            double rightX = Math.cos(yaw);
            double rightZ = -Math.sin(yaw);
            double upX = Math.sin(yaw) * Math.sin(pitch);
            double upY = Math.cos(pitch);
            double upZ = Math.cos(yaw) * Math.sin(pitch);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < count; i++) {
                StellarPoint point = points[i];
                if (point.alpha <= 0.01F || point.size <= 0.0D) {
                    continue;
                }
                addBillboardGlowPoint(buffer, point, rightX, rightZ, upX, upY, upZ);
            }
            tessellator.draw();
        } finally {
            shader.end();
        }
    }

    private static void addBillboardGlowPoint(BufferBuilder buffer, StellarPoint point,
                                              double rightX, double rightZ,
                                              double upX, double upY, double upZ) {
        float red = ((point.color >> 16) & 0xFF) / 255.0F;
        float green = ((point.color >> 8) & 0xFF) / 255.0F;
        float blue = (point.color & 0xFF) / 255.0F;
        double size = point.size;

        for (int i = 0; i < BILLBOARD_SEGMENTS; i++) {
            int next = (i + 1) % BILLBOARD_SEGMENTS;
            addGlowTriangle(buffer, point,
                    point.x + (rightX * BILLBOARD_COS[i] + upX * BILLBOARD_SIN[i]) * size,
                    point.y + upY * BILLBOARD_SIN[i] * size,
                    point.z + (rightZ * BILLBOARD_COS[i] + upZ * BILLBOARD_SIN[i]) * size,
                    point.x + (rightX * BILLBOARD_COS[next] + upX * BILLBOARD_SIN[next]) * size,
                    point.y + upY * BILLBOARD_SIN[next] * size,
                    point.z + (rightZ * BILLBOARD_COS[next] + upZ * BILLBOARD_SIN[next]) * size,
                    red, green, blue);
        }
    }

    private static void addGlowTriangle(BufferBuilder buffer, StellarPoint point,
                                        double ax, double ay, double az,
                                        double bx, double by, double bz,
                                        float red, float green, float blue) {
        buffer.pos(point.x, point.y, point.z).color(red, green, blue, point.alpha).endVertex();
        buffer.pos(ax, ay, az).color(red, green, blue, 0.0F).endVertex();
        buffer.pos(bx, by, bz).color(red, green, blue, 0.0F).endVertex();
    }

    private static final class StellarPoint {
        private double x;
        private double y;
        private double z;
        private double size;
        private int color;
        private float alpha;

        private void set(double x, double y, double z, double size, int color, float alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.color = color;
            this.alpha = alpha;
        }
    }
}
