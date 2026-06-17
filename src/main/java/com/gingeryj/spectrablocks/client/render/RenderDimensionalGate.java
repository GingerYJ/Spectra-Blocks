package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileDimensionalGate;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderDimensionalGate extends TileEntitySpecialRenderer<TileDimensionalGate> {

    private static final float EFFECT_DIMENSIONAL_GATE = 3.0F;
    private static final double PORTAL_WIDTH = 2.10D;
    private static final double PORTAL_HEIGHT = 3.18D;
    private static final double RUNE_FIELD_RADIUS = 1.70D;
    private static final int PLANE_COLUMNS = 74;
    private static final int PLANE_ROWS = 112;
    private static final int SPHERE_LAT_SEGMENTS = 26;
    private static final int SPHERE_LON_SEGMENTS = 32;

    @Override
    public void render(TileDimensionalGate te, double x, double y, double z,
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
                drawGatePlane(shader, ticks, 0.0F, 0.0F, 1.0F);
                drawGatePlane(shader, ticks + 9.0F, 90.0F, 0.46F, 0.68F);
                drawRuneFields(shader, ticks);
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("dimensional gate render failed: " + ex.getMessage());
        } finally {
            shader.end();
            restoreState(blendWasEnabled, cullWasEnabled, alphaWasEnabled, textureWasEnabled,
                    lightingWasEnabled, depthMaskWasEnabled, previousCullFace);
            GlStateManager.popMatrix();
        }
    }

    private void drawGatePlane(ShaderProgram shader, float ticks, float yRotation, float seed, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(yRotation, 0.0F, 1.0F, 0.0F);
        setSpaceUniforms(shader, ticks, EFFECT_DIMENSIONAL_GATE, 0.0F, alpha, seed,
                0x071326, 0x223BFF, 0x5EF7FF, 0xFFFFFF);
        drawShaderPlane(PORTAL_WIDTH, PORTAL_HEIGHT, 0.0D, PLANE_COLUMNS, PLANE_ROWS);
        GlStateManager.popMatrix();
    }

    private void drawRuneFields(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.72F, 0.0F, 0.0F, 1.0F);
        setSpaceUniforms(shader, ticks, EFFECT_DIMENSIONAL_GATE, 1.0F, 0.76F, 0.24F,
                0x071326, 0xE8D7FF, 0x5EF7FF, 0xFFFFFF);
        drawShaderSphere(RUNE_FIELD_RADIUS, SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * 0.46F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        setSpaceUniforms(shader, ticks, EFFECT_DIMENSIONAL_GATE, 1.0F, 0.44F, 0.58F,
                0x071326, 0xA36BFF, 0x5EF7FF, 0xFFFFFF);
        drawShaderSphere(RUNE_FIELD_RADIUS * 1.12D, SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();
        useNormalBlend();
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
