package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileImaginaryCube;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderImaginaryCube extends TileEntitySpecialRenderer<TileImaginaryCube> {

    private static final double BASE_HALF_SIZE = 0.60D;
    private static final double OUTER_HALF_SIZE = 0.82D;
    private static final double INNER_HALF_SIZE = 0.42D;
    private static final double OFFSET_STRENGTH = 0.105D;
    private static final int CUBE_LAYER_COUNT = 4;
    private static final int CORNER_SPARK_COUNT = 16;
    private static final int CROSS_LINE_COUNT = 12;
    private static final float FRAME_ALPHA = 0.68F;
    private static final float GHOST_ALPHA = 0.24F;
    private static final float FACE_ALPHA = 0.060F;
    private static final float SPARK_ALPHA = 0.70F;
    private static final float ROTATION_SPEED = 0.36F;
    private static final int PRIMARY_COLOR = 0x7AFDFF;
    private static final int SECONDARY_COLOR = 0xFF75E6;
    private static final int GHOST_COLOR = 0xA58CFF;
    private static final int FACE_COLOR = 0x1A274B;
    private static final int HOT_COLOR = 0xFFFFFF;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TileImaginaryCube te, double x, double y, double z,
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
                drawTransparentFaces(shader, ticks);
                drawShiftedFrames(shader, ticks);
                drawCrossLines(shader, ticks);
                drawCornerSparks(shader, ticks);
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("imaginary cube shader render failed: " + ex.getMessage());
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
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private void drawTransparentFaces(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        setTechUniforms(shader, ticks, 2.0F, 0.0F, FACE_COLOR, PRIMARY_COLOR, SECONDARY_COLOR,
                FACE_ALPHA * (0.72F + 0.28F * (float) Math.sin(ticks * 0.065F)),
                0.90F, (float) INNER_HALF_SIZE);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * ROTATION_SPEED * 0.45F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(18.0F + (float) Math.sin(ticks * 0.020F) * 4.0F, 1.0F, 0.0F, 0.0F);
        drawCubeFaces(INNER_HALF_SIZE);
        GlStateManager.popMatrix();
    }

    private void drawShiftedFrames(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CUBE_LAYER_COUNT; i++) {
            double layer = i / (double) (CUBE_LAYER_COUNT - 1);
            double size = BASE_HALF_SIZE + (layer - 0.5D) * 0.22D;
            float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.115F + i * 1.8F);
            double offsetX = Math.sin(ticks * 0.034D + i * 2.1D) * OFFSET_STRENGTH;
            double offsetY = Math.cos(ticks * 0.030D + i * 1.7D) * OFFSET_STRENGTH * 0.72D;
            double offsetZ = Math.sin(ticks * 0.028D + i * 2.8D) * OFFSET_STRENGTH;
            int color = i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR;
            float alpha = (i == 1 ? FRAME_ALPHA : GHOST_ALPHA) * (0.62F + 0.38F * pulse);

            setTechUniforms(shader, ticks, 2.0F, 1.0F + i * 0.20F, color, SECONDARY_COLOR, HOT_COLOR,
                    alpha, 1.15F + pulse * 0.25F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(offsetX, offsetY, offsetZ);
            GlStateManager.rotate(ticks * (ROTATION_SPEED + i * 0.11F) + i * 21.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(12.0F + i * 17.0F + (float) Math.sin(ticks * 0.018F + i) * 6.0F,
                    1.0F, 0.0F, 0.0F);
            drawCubeEdges(size, i == 1 ? 0.034D : 0.018D);
            GlStateManager.popMatrix();
        }

        setTechUniforms(shader, ticks, 2.0F, 2.0F, GHOST_COLOR, PRIMARY_COLOR, SECONDARY_COLOR,
                GHOST_ALPHA * 0.65F, 1.00F, (float) OUTER_HALF_SIZE);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * ROTATION_SPEED * 0.72F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 0.0F, 1.0F);
        drawCubeEdges(OUTER_HALF_SIZE, 0.014D);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawCrossLines(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        setTechUniforms(shader, ticks, 2.0F, 2.0F, PRIMARY_COLOR, SECONDARY_COLOR, HOT_COLOR,
                GHOST_ALPHA, 1.18F, (float) OUTER_HALF_SIZE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < CROSS_LINE_COUNT; i++) {
            double phase = TWO_PI * i / CROSS_LINE_COUNT;
            double pulse = Math.max(0.0D, Math.sin(ticks * 0.075D + i * 0.83D));
            if (pulse <= 0.08D) {
                continue;
            }
            double a = OUTER_HALF_SIZE * (0.56D + 0.24D * Math.sin(ticks * 0.021D + i));
            double b = OUTER_HALF_SIZE * (0.56D + 0.24D * Math.cos(ticks * 0.026D + i));

            RenderHelper.addTexturedLine(buffer,
                    Math.cos(phase) * a, Math.sin(phase * 1.7D) * b, -OUTER_HALF_SIZE,
                    -Math.sin(phase) * b, Math.cos(phase * 1.3D) * a, OUTER_HALF_SIZE,
                    0.012D);
        }
        tessellator.draw();
        useAlphaBlend();
    }

    private void drawCornerSparks(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < CORNER_SPARK_COUNT; i++) {
            double signX = (i & 1) == 0 ? -1.0D : 1.0D;
            double signY = (i & 2) == 0 ? -1.0D : 1.0D;
            double signZ = (i & 4) == 0 ? -1.0D : 1.0D;
            double ghost = (i & 8) == 0 ? 0.0D : 0.09D;
            double size = BASE_HALF_SIZE + ghost;
            double pulse = Math.max(0.0D, Math.sin(ticks * 0.125D + i * 1.17D));
            double blink = pulse * pulse;
            if (blink <= 0.03D) {
                continue;
            }

            double px = signX * size + Math.sin(ticks * 0.040D + i) * 0.035D;
            double py = signY * size + Math.cos(ticks * 0.034D + i) * 0.035D;
            double pz = signZ * size + Math.sin(ticks * 0.030D + i * 2.0D) * 0.035D;
            double sparkSize = 0.018D + blink * 0.035D;

            setTechUniforms(shader, ticks, 2.0F, 3.0F, i % 3 == 0 ? HOT_COLOR : PRIMARY_COLOR,
                    i % 2 == 0 ? PRIMARY_COLOR : SECONDARY_COLOR, HOT_COLOR,
                    SPARK_ALPHA * (float) blink, 1.45F, (float) sparkSize);
            GlStateManager.pushMatrix();
            GlStateManager.translate(px, py, pz);
            drawShaderSphere(sparkSize, 7, 7);
            drawSpark(sparkSize * 2.4D, 0.008D + blink * 0.006D);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawCubeFaces(double halfSize) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addFace(buffer, -halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, 0.0D, 0.0D, -1.0D);
        addFace(buffer, halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, 0.0D, 0.0D, 1.0D);
        addFace(buffer, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, 0.0D, 1.0D, 0.0D);
        addFace(buffer, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, 0.0D, -1.0D, 0.0D);
        addFace(buffer, halfSize, -halfSize, -halfSize, halfSize, halfSize, halfSize, 1.0D, 0.0D, 0.0D);
        addFace(buffer, -halfSize, -halfSize, halfSize, -halfSize, halfSize, -halfSize, -1.0D, 0.0D, 0.0D);
        tessellator.draw();
    }

    private static void addFace(BufferBuilder buffer, double x0, double y0, double z0,
                                double x1, double y1, double z1,
                                double normalX, double normalY, double normalZ) {
        buffer.pos(x0, y0, z0).tex(0.0D, 0.0D).normal((float) normalX, (float) normalY, (float) normalZ).endVertex();
        buffer.pos(x1, y0, z0).tex(1.0D, 0.0D).normal((float) normalX, (float) normalY, (float) normalZ).endVertex();
        buffer.pos(x1, y1, z1).tex(1.0D, 1.0D).normal((float) normalX, (float) normalY, (float) normalZ).endVertex();
        buffer.pos(x0, y1, z1).tex(0.0D, 1.0D).normal((float) normalX, (float) normalY, (float) normalZ).endVertex();
    }

    private static void drawCubeEdges(double halfSize, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                addLine(buffer, x * halfSize, y * halfSize, -halfSize,
                        x * halfSize, y * halfSize, halfSize, width);
            }
        }
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                addLine(buffer, x * halfSize, -halfSize, z * halfSize,
                        x * halfSize, halfSize, z * halfSize, width);
            }
        }
        for (int y = -1; y <= 1; y += 2) {
            for (int z = -1; z <= 1; z += 2) {
                addLine(buffer, -halfSize, y * halfSize, z * halfSize,
                        halfSize, y * halfSize, z * halfSize, width);
            }
        }
        tessellator.draw();
    }

    private static void drawSpark(double size, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addLine(buffer, -size, 0.0D, 0.0D, size, 0.0D, 0.0D, width);
        addLine(buffer, 0.0D, -size, 0.0D, 0.0D, size, 0.0D, width);
        addLine(buffer, 0.0D, 0.0D, -size, 0.0D, 0.0D, size, width);
        tessellator.draw();
    }

    private static void addLine(BufferBuilder buffer, double x0, double y0, double z0,
                                double x1, double y1, double z1, double width) {
        RenderHelper.addTexturedLine(buffer, x0, y0, z0, x1, y1, z1, width);
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

    private static void addPosition(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal(0.0F, 1.0F, 0.0F)
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
