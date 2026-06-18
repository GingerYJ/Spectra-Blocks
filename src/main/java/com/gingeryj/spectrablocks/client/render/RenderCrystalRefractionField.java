package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileCrystalRefractionField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderCrystalRefractionField extends RenderCelestialEffectBase<TileCrystalRefractionField> {

    private static final int PLANE_COUNT = 9;
    private static final int EDGE_COUNT = 7;
    private static final double INNER_RADIUS = 0.86D;
    private static final double OUTER_RADIUS = 3.42D;
    private static final double PLANE_HEIGHT = 1.86D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final float PLANE_ROTATION_SPEED = 0.090F;
    private static final int[] PLANE_COLORS = new int[]{
            0xD8FFFF, 0x9EF5FF, 0xC2B9FF, 0xFFE2F8
    };

    @Override
    protected void renderCelestialEffect(TileCrystalRefractionField te, float ticks) {
        ShaderProgram shader = ShaderManager.getProgram("tech_effect");
        if (shader == null || !shader.begin()) {
            return;
        }

        try {
            drawCore(shader, ticks);
            drawPlanes(shader, ticks);
            drawFacetEdges(shader, ticks);
            drawCaustics(shader, ticks);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("crystal refraction field shader render failed: " + ex.getMessage());
        } finally {
            shader.end();
        }
    }

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = wave(ticks * 0.044D);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 4.0F, 0.0F, 0xFFFFFF, 0xBDFBFF, 0xFFE2F8,
                0.24F, 1.20F, 0.54F);
        drawShaderSphere(0.54D + pulse * 0.04D, 20, 20);
        setTechUniforms(shader, ticks, 4.0F, 1.0F, 0xBDFBFF, 0xC2B9FF, 0xFFFFFF,
                0.13F, 0.95F, 0.96F);
        drawShaderSphere(0.96D + pulse * 0.07D, 20, 20);
        useAlphaBlend();
    }

    private void drawPlanes(ShaderProgram shader, float ticks) {
        useAlphaBlend();
        for (int i = 0; i < PLANE_COUNT; i++) {
            double angle = TWO_PI * i / PLANE_COUNT + ticks * PLANE_ROTATION_SPEED * (i % 2 == 0 ? 1.0D : -0.65D);
            double radius = INNER_RADIUS + (i % 3) * 0.24D;
            double farRadius = OUTER_RADIUS - (i % 4) * 0.18D;
            double yShift = Math.sin(ticks * 0.020D + i) * 0.18D;
            double tilt = -0.62D + (i % 5) * 0.31D;
            int color = PLANE_COLORS[i % PLANE_COLORS.length];
            float alpha = 0.090F + 0.052F * wave(ticks * 0.036D + i * 0.61D);

            setTechUniforms(shader, ticks, 4.0F, 2.0F + i * 0.08F, color,
                    PLANE_COLORS[(i + 1) % PLANE_COLORS.length], 0xFFFFFF,
                    alpha, 1.14F, (float) farRadius);
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) Math.toDegrees(angle), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) Math.toDegrees(tilt), 0.0F, 0.0F, 1.0F);
            drawFacetPlane(radius, farRadius, PLANE_HEIGHT, yShift, 0.34D + (i % 3) * 0.08D);
            GlStateManager.popMatrix();
        }
    }

    private void drawFacetEdges(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < EDGE_COUNT; i++) {
            double angle = TWO_PI * i / EDGE_COUNT - ticks * 0.012D;
            double y0 = -1.30D + (i % 3) * 0.38D;
            double y1 = 1.30D - (i % 2) * 0.34D;
            double radius = 2.04D + (i % 4) * 0.29D;
            int color = PLANE_COLORS[(i + 1) % PLANE_COLORS.length];
            float alpha = 0.18F + 0.08F * wave(ticks * 0.050D + i);

            setTechUniforms(shader, ticks, 4.0F, 3.0F, color, 0xFFFFFF, 0xD8FFFF,
                    alpha, 1.35F, (float) radius);
            drawShaderLine(Math.cos(angle) * radius, y0, Math.sin(angle) * radius,
                    Math.cos(angle + 0.42D) * (radius * 0.66D), y1,
                    Math.sin(angle + 0.42D) * (radius * 0.66D), 0.052D);

            setTechUniforms(shader, ticks, 4.0F, 3.0F, 0xFFFFFF, color, 0xD8FFFF,
                    alpha * 0.52F, 1.45F, (float) radius);
            drawShaderLine(Math.cos(angle) * radius, y0, Math.sin(angle) * radius,
                    Math.cos(angle + 0.42D) * (radius * 0.66D), y1,
                    Math.sin(angle + 0.42D) * (radius * 0.66D), 0.020D);
        }
        useAlphaBlend();
    }

    private void drawCaustics(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.032F, 0.0F, 1.0F, 0.0F);
        for (int i = 0; i < 4; i++) {
            double radius = 1.25D + i * 0.46D;
            float alpha = 0.075F + 0.035F * wave(ticks * 0.042D + i);
            setTechUniforms(shader, ticks, 4.0F, 4.0F + i * 0.1F,
                    PLANE_COLORS[i % PLANE_COLORS.length], 0xFFFFFF, 0x9EF5FF,
                    alpha, 1.12F, (float) radius);
            drawShaderCircle(radius, 80, 0.020D + i * 0.005D);
        }
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private static void drawFacetPlane(double innerRadius, double outerRadius, double height,
                                       double yShift, double halfWidth) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addPosition(buffer, innerRadius, yShift - height * 0.30D, -halfWidth, 0.0D, 0.0D);
        addPosition(buffer, outerRadius, yShift + height * 0.22D, 0.0D, 1.0D, 0.5D);
        addPosition(buffer, innerRadius, yShift + height * 0.48D, halfWidth, 0.0D, 1.0D);
        addPosition(buffer, -innerRadius * 0.26D, yShift + height * 0.18D, halfWidth * 0.52D, 0.35D, 0.85D);
        addPosition(buffer, innerRadius, yShift - height * 0.30D, -halfWidth, 0.0D, 0.0D);
        addPosition(buffer, innerRadius, yShift + height * 0.48D, halfWidth, 0.0D, 1.0D);
        tessellator.draw();
    }

    private static void drawShaderLine(double x0, double y0, double z0,
                                       double x1, double y1, double z1, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addLineRibbon(buffer, x0, y0, z0, x1, y1, z1, width);
        tessellator.draw();
    }

    private static void drawShaderCircle(double radius, int segments, double width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        double innerRadius = Math.max(0.001D, radius - width * 0.5D);
        double outerRadius = radius + width * 0.5D;
        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1.0D) / segments;
            double angle0 = TWO_PI * u0;
            double angle1 = TWO_PI * u1;
            addRingQuad(buffer, innerRadius, outerRadius, angle0, angle1, u0, u1);
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

    private static void addRingQuad(BufferBuilder buffer, double innerRadius, double outerRadius,
                                    double angle0, double angle1, double u0, double u1) {
        double c0 = Math.cos(angle0);
        double s0 = Math.sin(angle0);
        double c1 = Math.cos(angle1);
        double s1 = Math.sin(angle1);
        addPosition(buffer, c0 * innerRadius, 0.0D, s0 * innerRadius, u0, 0.0D);
        addPosition(buffer, c0 * outerRadius, 0.0D, s0 * outerRadius, u0, 1.0D);
        addPosition(buffer, c1 * outerRadius, 0.0D, s1 * outerRadius, u1, 1.0D);
        addPosition(buffer, c0 * innerRadius, 0.0D, s0 * innerRadius, u0, 0.0D);
        addPosition(buffer, c1 * outerRadius, 0.0D, s1 * outerRadius, u1, 1.0D);
        addPosition(buffer, c1 * innerRadius, 0.0D, s1 * innerRadius, u1, 0.0D);
    }

    private static void addLineRibbon(BufferBuilder buffer, double x0, double y0, double z0,
                                      double x1, double y1, double z1, double width) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.0001D) {
            return;
        }

        dx /= length;
        dy /= length;
        dz /= length;
        double refX = Math.abs(dy) > 0.88D ? 1.0D : 0.0D;
        double refY = Math.abs(dy) > 0.88D ? 0.0D : 1.0D;
        double refZ = 0.0D;
        double px = dy * refZ - dz * refY;
        double py = dz * refX - dx * refZ;
        double pz = dx * refY - dy * refX;
        double pLength = Math.sqrt(px * px + py * py + pz * pz);
        if (pLength < 0.0001D) {
            px = 1.0D;
            py = 0.0D;
            pz = 0.0D;
            pLength = 1.0D;
        }

        double half = width * 0.5D / pLength;
        px *= half;
        py *= half;
        pz *= half;
        addRibbonVertex(buffer, x0 + px, y0 + py, z0 + pz, 0.0D, 1.0D, px, py, pz);
        addRibbonVertex(buffer, x0 - px, y0 - py, z0 - pz, 0.0D, 0.0D, -px, -py, -pz);
        addRibbonVertex(buffer, x1 - px, y1 - py, z1 - pz, 1.0D, 0.0D, -px, -py, -pz);
        addRibbonVertex(buffer, x0 + px, y0 + py, z0 + pz, 0.0D, 1.0D, px, py, pz);
        addRibbonVertex(buffer, x1 - px, y1 - py, z1 - pz, 1.0D, 0.0D, -px, -py, -pz);
        addRibbonVertex(buffer, x1 + px, y1 + py, z1 + pz, 1.0D, 1.0D, px, py, pz);
    }

    private static void addRibbonVertex(BufferBuilder buffer, double x, double y, double z,
                                        double u, double v, double normalX, double normalY, double normalZ) {
        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (normalLength < 0.0001D) {
            normalX = 0.0D;
            normalY = 1.0D;
            normalZ = 0.0D;
            normalLength = 1.0D;
        }
        buffer.pos(x, y, z)
                .tex(u, v)
                .normal((float) (normalX / normalLength), (float) (normalY / normalLength), (float) (normalZ / normalLength))
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
}
