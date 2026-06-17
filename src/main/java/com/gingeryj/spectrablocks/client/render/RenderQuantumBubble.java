package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileQuantumBubble;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderQuantumBubble extends TileEntitySpecialRenderer<TileQuantumBubble> {

    private static final double BUBBLE_RADIUS = 0.92D;
    private static final double INNER_RADIUS = 0.72D;
    private static final int SHELL_LAT_SEGMENTS = 22;
    private static final int SHELL_LON_SEGMENTS = 32;
    private static final int GRID_LAT_LINES = 7;
    private static final int GRID_LON_LINES = 12;
    private static final int GRID_SEGMENTS = 96;
    private static final int FLASH_POINT_COUNT = 48;
    private static final int ORBIT_ARC_COUNT = 5;
    private static final int ORBIT_SEGMENTS = 84;
    private static final float SHELL_ALPHA = 0.115F;
    private static final float GRID_ALPHA = 0.50F;
    private static final float POINT_ALPHA = 0.72F;
    private static final float ORBIT_ALPHA = 0.35F;
    private static final float GRID_ROTATION_SPEED = 0.38F;
    private static final int SHELL_COLOR = 0x65F7FF;
    private static final int GRID_COLOR = 0xB8FFF5;
    private static final int POINT_COLOR = 0xFFFFFF;
    private static final int SECONDARY_COLOR = 0x73A7FF;
    private static final int WARNING_COLOR = 0xFFD86E;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TileQuantumBubble te, double x, double y, double z,
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
        useAlphaBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        try {
            if (shader != null && shader.begin()) {
                drawShell(shader, ticks);
                drawJumpingGrid(shader, ticks);
                drawOrbitArcs(shader, ticks);
                drawFlashPoints(shader, ticks);
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("quantum bubble shader render failed: " + ex.getMessage());
        } finally {
            if (shader != null) {
                shader.end();
            }
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
            useAlphaBlend();
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private void drawShell(ShaderProgram shader, float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.050F);

        useAlphaBlend();
        setTechUniforms(shader, ticks, 1.0F, 0.0F, SHELL_COLOR, SECONDARY_COLOR, POINT_COLOR,
                SHELL_ALPHA * (0.75F + 0.25F * pulse), 0.86F, (float) BUBBLE_RADIUS);
        drawShaderSphere(BUBBLE_RADIUS + 0.025D * pulse, SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 1.0F, 1.0F, SECONDARY_COLOR, SHELL_COLOR, POINT_COLOR,
                SHELL_ALPHA * 0.42F, 0.92F, (float) INNER_RADIUS);
        drawShaderSphere(INNER_RADIUS + 0.020D * (1.0D - pulse), SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);
        useAlphaBlend();
    }

    private void drawJumpingGrid(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * GRID_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) Math.sin(ticks * 0.017F) * 8.0F, 0.0F, 0.0F, 1.0F);

        setTechUniforms(shader, ticks, 1.0F, 2.0F, GRID_COLOR, SECONDARY_COLOR, POINT_COLOR,
                GRID_ALPHA * 0.28F, 1.12F, (float) BUBBLE_RADIUS);
        GlStateManager.glLineWidth(2.6F);
        drawGrid(ticks, 0.055D);

        setTechUniforms(shader, ticks + 11.0F, 1.0F, 2.0F, GRID_COLOR, SECONDARY_COLOR, POINT_COLOR,
                GRID_ALPHA, 1.35F, (float) BUBBLE_RADIUS);
        GlStateManager.glLineWidth(1.2F);
        drawGrid(ticks + 11.0F, 0.035D);

        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawGrid(float ticks, double jitter) {
        for (int lat = 1; lat < GRID_LAT_LINES; lat++) {
            double theta = Math.PI * lat / GRID_LAT_LINES;
            double y = BUBBLE_RADIUS * Math.cos(theta);
            double radius = BUBBLE_RADIUS * Math.sin(theta);
            drawLatitude(radius, y, ticks, lat * 1.3D, jitter);
        }

        for (int lon = 0; lon < GRID_LON_LINES; lon++) {
            double phi = TWO_PI * lon / GRID_LON_LINES;
            drawLongitude(phi, ticks, lon * 0.91D, jitter);
        }
    }

    private void drawLatitude(double radius, double y, float ticks, double seed, double jitter) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < GRID_SEGMENTS; i++) {
            double progress = i / (double) GRID_SEGMENTS;
            double angle = TWO_PI * progress;
            double hop = Math.sin(angle * 8.0D + ticks * 0.080D + seed) * jitter;
            double localRadius = radius + hop;
            double localY = y + Math.cos(angle * 5.0D - ticks * 0.060D + seed) * jitter * 0.45D;
            addPosition(buffer, Math.cos(angle) * localRadius, localY, Math.sin(angle) * localRadius,
                    progress, (localY / BUBBLE_RADIUS + 1.0D) * 0.5D, 0.0D, 1.0D, 0.0D);
        }
        tessellator.draw();
    }

    private void drawLongitude(double phi, float ticks, double seed, double jitter) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= GRID_SEGMENTS; i++) {
            double progress = i / (double) GRID_SEGMENTS;
            double theta = Math.PI * progress;
            double pulse = Math.sin(theta * 9.0D - ticks * 0.090D + seed) * jitter;
            double radius = BUBBLE_RADIUS + pulse;
            double horizontal = Math.sin(theta) * radius;
            double y = Math.cos(theta) * radius;
            double wobblePhi = phi + Math.sin(theta * 4.0D + ticks * 0.045D + seed) * 0.018D;
            addPosition(buffer, Math.cos(wobblePhi) * horizontal, y, Math.sin(wobblePhi) * horizontal,
                    phi / TWO_PI, progress, 0.0D, 1.0D, 0.0D);
        }
        tessellator.draw();
    }

    private void drawOrbitArcs(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < ORBIT_ARC_COUNT; i++) {
            int color = i % 2 == 0 ? SECONDARY_COLOR : WARNING_COLOR;
            setTechUniforms(shader, ticks, 1.0F, 3.0F, color, GRID_COLOR, POINT_COLOR,
                    ORBIT_ALPHA * (i == 0 ? 1.0F : 0.72F), 1.22F, (float) BUBBLE_RADIUS);

            GlStateManager.pushMatrix();
            GlStateManager.rotate(36.0F + i * 28.0F + ticks * (0.22F + i * 0.04F), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(i * 61.0F - ticks * 0.31F, 0.0F, 1.0F, 0.0F);
            GlStateManager.glLineWidth(i == 0 ? 2.2F : 1.2F);
            drawOrbitArc(BUBBLE_RADIUS * (0.78D + i * 0.055D),
                    TWO_PI * (0.35D + (i % 3) * 0.08D), ticks, i * 2.4D);
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawOrbitArc(double radius, double sweep, float ticks, double seed) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double start = ticks * 0.018D + seed;
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            double progress = i / (double) ORBIT_SEGMENTS;
            double angle = start + progress * sweep;
            addPosition(buffer, Math.cos(angle) * radius,
                    Math.sin(angle * 3.0D + seed) * 0.026D,
                    Math.sin(angle) * radius,
                    progress, 0.5D, 0.0D, 1.0D, 0.0D);
        }
        tessellator.draw();
    }

    private void drawFlashPoints(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < FLASH_POINT_COUNT; i++) {
            double yaw = i * 2.399963229728653D + Math.sin(i * 1.7D) * 0.24D;
            double pitch = Math.asin(-0.92D + 1.84D * fract(i * 0.61803398875D));
            double pulse = Math.max(0.0D, Math.sin(ticks * (0.10D + (i % 5) * 0.018D) + i * 1.33D));
            double blink = pulse * pulse * pulse;
            if (blink <= 0.035D) {
                continue;
            }

            double radius = BUBBLE_RADIUS + 0.018D * Math.sin(ticks * 0.07D + i);
            double horizontal = Math.cos(pitch) * radius;
            double px = Math.cos(yaw) * horizontal;
            double py = Math.sin(pitch) * radius;
            double pz = Math.sin(yaw) * horizontal;
            double size = 0.012D + blink * 0.035D;
            int color = i % 9 == 0 ? WARNING_COLOR : (i % 4 == 0 ? SECONDARY_COLOR : POINT_COLOR);

            setTechUniforms(shader, ticks, 1.0F, 4.0F, color, GRID_COLOR, POINT_COLOR,
                    POINT_ALPHA * (float) blink, 1.45F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(px, py, pz);
            drawShaderSphere(size, 7, 7);
            GlStateManager.glLineWidth(1.0F);
            drawEnergySpark(size * 2.2D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawEnergySpark(double size) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addPosition(buffer, -size, 0.0D, 0.0D, 0.0D, 0.5D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, size, 0.0D, 0.0D, 1.0D, 0.5D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, 0.0D, -size, 0.0D, 0.5D, 0.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, 0.0D, size, 0.0D, 0.5D, 1.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, 0.0D, 0.0D, -size, 0.5D, 0.0D, 0.0D, 1.0D, 0.0D);
        addPosition(buffer, 0.0D, 0.0D, size, 0.5D, 1.0D, 0.0D, 1.0D, 0.0D);
        tessellator.draw();
    }

    private static void drawShaderSphere(double radius, int latSegs, int lonSegs) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = TWO_PI * lon / lonSegs;
                double phi1 = TWO_PI * (lon + 1) / lonSegs;
                addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addSphereVertex(buffer, radius, theta1, phi0, lon / (double) lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addSphereVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addSphereVertex(buffer, radius, theta0, phi1, (lon + 1.0D) / lonSegs, lat / (double) latSegs);
            }
        }
        tessellator.draw();
    }

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                        double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static void addPosition(BufferBuilder buffer, double x, double y, double z,
                                    double u, double v, double normalX, double normalY, double normalZ) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) normalX, (float) normalY, (float) normalZ)
                .endVertex();
    }

    private static void setTechUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                        int primary, int secondary, int tertiary,
                                        float alpha, float intensity, float scale) {
        shader.setUniform1f("uTime", ticks * 0.040F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uIntensity", intensity);
        shader.setUniform1f("uScale", scale);
        setColor(shader, "uPrimaryColor", primary);
        setColor(shader, "uSecondaryColor", secondary);
        setColor(shader, "uTertiaryColor", tertiary);
    }

    private static void setColor(ShaderProgram shader, String uniform, int color) {
        shader.setUniform3f(uniform,
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F);
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

    private static void useAlphaBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }
}
