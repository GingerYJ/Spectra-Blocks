package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileTemporalRift;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderTemporalRift extends TileEntitySpecialRenderer<TileTemporalRift> {

    private static final float EFFECT_TEMPORAL_RIFT = 4.0F;
    private static final double RIFT_HALF_WIDTH = 0.82D;
    private static final double RIFT_HALF_HEIGHT = 1.30D;
    private static final double CLOCK_FIELD_RADIUS = 1.08D;
    private static final double FRAGMENT_FIELD_RADIUS = 1.36D;
    private static final int PLANE_COLUMNS = 60;
    private static final int PLANE_ROWS = 96;
    private static final int SPHERE_LAT_SEGMENTS = 28;
    private static final int SPHERE_LON_SEGMENTS = 28;

    @Override
    public void render(TileTemporalRift te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) {
            return;
        }

        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;
        ShaderProgram shader = ShaderManager.getProgram("space_effect");
        if (shader == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        double renderScale = te.renderScale(1.0D);
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean alphaWasEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean depthMaskWasEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);

        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        useNormalBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();

        try {
            if (shader.begin()) {
                drawAfterimages(shader, ticks);
                drawRiftPlane(shader, ticks, 0.0F, 1.0F);
                drawClockFields(shader, ticks);
                drawFragmentField(shader, ticks);
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("temporal rift render failed: " + ex.getMessage());
        } finally {
            shader.end();
            restoreState(blendWasEnabled, cullWasEnabled, alphaWasEnabled, textureWasEnabled,
                    lightingWasEnabled, depthMaskWasEnabled, previousCullFace);
            GlStateManager.popMatrix();
        }
    }

    private void drawAfterimages(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < 3; i++) {
            float pulse = sharpPulse(ticks * 0.070F - i * 0.76F);
            if (pulse <= 0.02F) {
                continue;
            }

            GlStateManager.pushMatrix();
            GlStateManager.rotate(28.0F * (i + 1) + (float) Math.sin(ticks * 0.012F + i) * 2.0F,
                    0.0F, 1.0F, 0.0F);
            GlStateManager.translate((i - 1) * 0.055D, 0.0D, -0.045D * (i + 1));
            setSpaceUniforms(shader, ticks - 9.0F - i * 4.0F, EFFECT_TEMPORAL_RIFT, 1.0F,
                    0.25F * pulse, i * 0.22F, 0x060416, 0x9B6CFF, 0x77F2FF, 0xFFD37D);
            drawShaderPlane(RIFT_HALF_WIDTH * 2.0D, RIFT_HALF_HEIGHT * 2.0D, 0.0D, PLANE_COLUMNS, PLANE_ROWS);
            GlStateManager.popMatrix();
        }
        useNormalBlend();
    }

    private void drawRiftPlane(ShaderProgram shader, float ticks, float seed, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) Math.sin(ticks * 0.018F) * 3.0F, 0.0F, 1.0F, 0.0F);
        setSpaceUniforms(shader, ticks, EFFECT_TEMPORAL_RIFT, 0.0F, alpha, seed,
                0x060416, 0x77F2FF, 0xFFD37D, 0xB7FBFF);
        drawShaderPlane(RIFT_HALF_WIDTH * 2.0D, RIFT_HALF_HEIGHT * 2.0D, 0.0D, PLANE_COLUMNS, PLANE_ROWS);
        GlStateManager.popMatrix();
    }

    private void drawClockFields(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * -0.28F, 0.0F, 0.0F, 1.0F);
        setSpaceUniforms(shader, ticks, EFFECT_TEMPORAL_RIFT, 2.0F, 0.56F, 0.30F,
                0x060416, 0xFFE6A3, 0x77F2FF, 0xFFD37D);
        drawShaderSphere(CLOCK_FIELD_RADIUS, SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F + ticks * -0.20F, 0.0F, 1.0F, 0.0F);
        setSpaceUniforms(shader, ticks, EFFECT_TEMPORAL_RIFT, 2.0F, 0.32F, 0.61F,
                0x060416, 0x77F2FF, 0x9B6CFF, 0xB7FBFF);
        drawShaderSphere(CLOCK_FIELD_RADIUS * 0.76D, SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();
        useNormalBlend();
    }

    private void drawFragmentField(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * -0.72F * 0.018F * 57.29578F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(14.0F, 1.0F, 0.0F, 0.0F);
        setSpaceUniforms(shader, ticks, EFFECT_TEMPORAL_RIFT, 3.0F, 0.70F, 0.49F,
                0x060416, 0x77F2FF, 0xFFD37D, 0xFFFFFF);
        drawShaderSphere(FRAGMENT_FIELD_RADIUS, SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();
        useNormalBlend();
    }

    private static float sharpPulse(float value) {
        float pulse = Math.max(0.0F, (float) Math.sin(value));
        return pulse * pulse * pulse;
    }

    private static void setSpaceUniforms(ShaderProgram shader, float ticks, float effect, float layer,
                                         float alpha, float seed, int primaryColor, int secondaryColor,
                                         int accentColor, int highlightColor) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.052F + seed * 5.0F);
        shader.setUniform1f("uTime", ticks * 0.035F);
        shader.setUniform1f("uEffect", effect);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uSeed", seed);
        shader.setUniform1f("uPulse", pulse);
        setUniformColor(shader, "uPrimaryColor", primaryColor);
        setUniformColor(shader, "uSecondaryColor", secondaryColor);
        setUniformColor(shader, "uAccentColor", accentColor);
        setUniformColor(shader, "uHighlightColor", highlightColor);
    }

    private static void setUniformColor(ShaderProgram shader, String name, int color) {
        shader.setUniform3f(name,
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F);
    }

    private static void drawShaderPlane(double width, double height, double z, int columns, int rows) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int row = 0; row < rows; row++) {
            double v0 = row / (double) rows;
            double v1 = (row + 1.0D) / rows;
            double y0 = (v0 - 0.5D) * height;
            double y1 = (v1 - 0.5D) * height;
            for (int column = 0; column < columns; column++) {
                double u0 = column / (double) columns;
                double u1 = (column + 1.0D) / columns;
                double x0 = (u0 - 0.5D) * width;
                double x1 = (u1 - 0.5D) * width;
                addPlaneVertex(buffer, x0, y0, z, u0, v0);
                addPlaneVertex(buffer, x0, y1, z, u0, v1);
                addPlaneVertex(buffer, x1, y1, z, u1, v1);
                addPlaneVertex(buffer, x0, y0, z, u0, v0);
                addPlaneVertex(buffer, x1, y1, z, u1, v1);
                addPlaneVertex(buffer, x1, y0, z, u1, v0);
            }
        }
        tessellator.draw();
    }

    private static void addPlaneVertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
        buffer.pos(x, y, z).tex(u, v).normal(0.0F, 0.0F, 1.0F).endVertex();
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

    private static void restoreState(boolean blendWasEnabled, boolean cullWasEnabled, boolean alphaWasEnabled,
                                     boolean textureWasEnabled, boolean lightingWasEnabled,
                                     boolean depthMaskWasEnabled, int previousCullFace) {
        if (cullWasEnabled) {
            GlStateManager.enableCull();
        } else {
            GlStateManager.disableCull();
        }
        GlStateManager.cullFace(previousCullFace == GL11.GL_FRONT
                ? GlStateManager.CullFace.FRONT
                : GlStateManager.CullFace.BACK);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        if (alphaWasEnabled) {
            GlStateManager.enableAlpha();
        } else {
            GlStateManager.disableAlpha();
        }
        if (textureWasEnabled) {
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.disableTexture2D();
        }
        if (lightingWasEnabled) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }
        GlStateManager.depthMask(depthMaskWasEnabled);
        if (!blendWasEnabled) {
            GlStateManager.disableBlend();
        }
        useNormalBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
