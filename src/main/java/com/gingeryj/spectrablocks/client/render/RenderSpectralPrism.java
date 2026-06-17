package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileSpectralPrism;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderSpectralPrism extends RenderCelestialEffectBase<TileSpectralPrism> {

    private static final double PRISM_RADIUS = 0.58D;
    private static final double PRISM_HEIGHT = 1.42D;
    private static final double INNER_GLOW_RADIUS = 0.72D;
    private static final int PRISM_FACETS = 6;
    private static final int BEAM_COUNT = 12;
    private static final double BEAM_INNER_RADIUS = 0.34D;
    private static final double BEAM_OUTER_RADIUS = 4.42D;
    private static final double BEAM_WIDTH = 0.13D;
    private static final int DUST_COUNT = 96;
    private static final double DUST_RADIUS = 3.85D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final float PRISM_ROTATION_SPEED = 0.62F;
    private static final float BEAM_ROTATION_SPEED = 0.055F;

    private static final int[] SPECTRUM_COLORS = new int[]{
            0xFF5A78, 0xFF9B45, 0xFFE86B, 0x8AFF7C, 0x59F3FF, 0x7A82FF, 0xD27CFF
    };

    @Override
    protected void renderCelestialEffect(TileSpectralPrism te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawPrism(shader, ticks);
            drawBeams(shader, ticks);
            drawDust(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("spectral prism shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawPrism(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.050D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 3.0F, 0.0F, 0xF8FEFF, 0x59F3FF, 0xD27CFF,
                0.20F, 1.10F, (float) INNER_GLOW_RADIUS);
        drawShaderSphere(INNER_GLOW_RADIUS + pulse * 0.06D, 20, 20);

        useAlphaBlend();
        setTechUniforms(shader, ticks, 3.0F, 1.0F, 0xC8FFFF, 0xFFB8F4, 0xFFFFFF,
                0.32F + pulse * 0.06F, 1.20F, (float) PRISM_RADIUS);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * PRISM_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        drawFacetedCrystal(PRISM_RADIUS, PRISM_HEIGHT, PRISM_FACETS);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 3.0F, 2.0F, 0xFFFFFF, 0x82F6FF, 0xFFB8F4,
                0.52F, 1.35F, (float) PRISM_RADIUS);
        GlStateManager.glLineWidth(2.0F);
        drawCrystalEdges(PRISM_RADIUS * 1.01D, PRISM_HEIGHT * 1.01D, PRISM_FACETS);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-ticks * 0.22F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        setTechUniforms(shader, ticks, 3.0F, 3.0F, 0xFFFFFF, 0x82F6FF, 0xD27CFF,
                0.18F, 1.10F, 1.18F);
        GlStateManager.glLineWidth(1.5F);
        drawShaderCircle(1.18D + pulse * 0.04D, 72);
        setTechUniforms(shader, ticks, 3.0F, 3.0F, 0x82F6FF, 0xFFFFFF, 0xFFB8F4,
                0.11F, 1.00F, 1.46F);
        drawShaderCircle(1.46D, 72);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawBeams(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * BEAM_ROTATION_SPEED, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < BEAM_COUNT; i++) {
            double angle = TWO_PI * i / BEAM_COUNT;
            double tilt = Math.sin(ticks * 0.018D + i * 0.74D) * 0.28D;
            int color = SPECTRUM_COLORS[i % SPECTRUM_COLORS.length];
            float alpha = 0.12F + 0.06F * wave(ticks * 0.040D + i);

            setTechUniforms(shader, ticks, 3.0F, 4.0F, color, 0xFFFFFF, SPECTRUM_COLORS[(i + 3) % SPECTRUM_COLORS.length],
                    alpha, 1.30F, (float) BEAM_OUTER_RADIUS);
            drawBeam(angle, tilt, BEAM_INNER_RADIUS, BEAM_OUTER_RADIUS, BEAM_WIDTH);
            if ((i & 1) == 0) {
                setTechUniforms(shader, ticks, 3.0F, 4.0F, 0xFFFFFF, color, 0x82F6FF,
                        alpha * 0.42F, 1.18F, (float) BEAM_OUTER_RADIUS);
                drawBeam(angle + 0.055D, -tilt * 0.72D, 0.48D, BEAM_OUTER_RADIUS * 0.82D, BEAM_WIDTH * 0.56D);
            }
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawDust(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < DUST_COUNT; i++) {
            double band = (i + 0.5D) / DUST_COUNT;
            double angle = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 4) * 0.0005D);
            double radius = 1.20D + Math.pow(band, 0.62D) * DUST_RADIUS;
            double y = Math.sin(i * 0.83D + ticks * 0.024D) * 0.78D;
            double px = Math.cos(angle) * radius;
            double pz = Math.sin(angle) * radius;
            int color = SPECTRUM_COLORS[(i + (int) (ticks * 0.025F)) % SPECTRUM_COLORS.length];
            float alpha = 0.16F + 0.16F * wave(ticks * 0.055D + i * 0.51D);
            double size = 0.018D + (i % 4) * 0.005D;

            setTechUniforms(shader, ticks, 3.0F, 5.0F, color, 0xFFFFFF, 0x59F3FF,
                    alpha, 1.28F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(px, y, pz);
            drawShaderSphere(size, 7, 7);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private static void drawBeam(double angle, double tilt, double innerRadius, double outerRadius, double halfWidth) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double sideX = -sin * halfWidth;
        double sideZ = cos * halfWidth;
        double innerY = tilt * innerRadius;
        double outerY = tilt * outerRadius;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addPosition(buffer, cos * innerRadius - sideX, innerY, sin * innerRadius - sideZ, 0.0D, 0.0D);
        addPosition(buffer, cos * innerRadius + sideX, innerY, sin * innerRadius + sideZ, 0.0D, 1.0D);
        addPosition(buffer, cos * outerRadius - sideX, outerY, sin * outerRadius - sideZ, 1.0D, 0.0D);
        addPosition(buffer, cos * outerRadius + sideX, outerY, sin * outerRadius + sideZ, 1.0D, 1.0D);
        tessellator.draw();
    }

    private static void drawFacetedCrystal(double radius, double height, int facets) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double topY = height * 0.5D;
        double bottomY = -height * 0.5D;
        for (int i = 0; i < facets; i++) {
            double angle0 = TWO_PI * i / facets;
            double angle1 = TWO_PI * (i + 1) / facets;
            double x0 = Math.cos(angle0) * radius;
            double z0 = Math.sin(angle0) * radius;
            double x1 = Math.cos(angle1) * radius;
            double z1 = Math.sin(angle1) * radius;

            addPosition(buffer, 0.0D, topY, 0.0D, 0.5D, 0.0D);
            addPosition(buffer, x0, 0.0D, z0, i / (double) facets, 0.5D);
            addPosition(buffer, x1, 0.0D, z1, (i + 1.0D) / facets, 0.5D);
            addPosition(buffer, 0.0D, bottomY, 0.0D, 0.5D, 1.0D);
            addPosition(buffer, x1, 0.0D, z1, (i + 1.0D) / facets, 0.5D);
            addPosition(buffer, x0, 0.0D, z0, i / (double) facets, 0.5D);
        }
        tessellator.draw();
    }

    private static void drawCrystalEdges(double radius, double height, int facets) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double topY = height * 0.5D;
        double bottomY = -height * 0.5D;
        for (int i = 0; i < facets; i++) {
            double angle0 = TWO_PI * i / facets;
            double angle1 = TWO_PI * (i + 1) / facets;
            double x0 = Math.cos(angle0) * radius;
            double z0 = Math.sin(angle0) * radius;
            double x1 = Math.cos(angle1) * radius;
            double z1 = Math.sin(angle1) * radius;
            addLine(buffer, 0.0D, topY, 0.0D, x0, 0.0D, z0);
            addLine(buffer, 0.0D, bottomY, 0.0D, x0, 0.0D, z0);
            addLine(buffer, x0, 0.0D, z0, x1, 0.0D, z1);
        }
        tessellator.draw();
    }

    private static void drawShaderCircle(double radius, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            addPosition(buffer, Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius, progress, 0.5D);
        }
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

    private static void addSphereVertex(BufferBuilder buffer, double radius, double theta, double phi, double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static void addLine(BufferBuilder buffer, double x0, double y0, double z0,
                                double x1, double y1, double z1) {
        addPosition(buffer, x0, y0, z0, 0.0D, 0.0D);
        addPosition(buffer, x1, y1, z1, 1.0D, 1.0D);
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
}
