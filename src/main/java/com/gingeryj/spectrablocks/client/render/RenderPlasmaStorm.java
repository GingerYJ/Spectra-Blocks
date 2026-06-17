package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TilePlasmaStorm;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderPlasmaStorm extends TileEntitySpecialRenderer<TilePlasmaStorm> {

    private static final double CORE_RADIUS = 0.62D;
    private static final double INNER_GLOW_RADIUS = 1.22D;
    private static final double STORM_RADIUS = 3.35D;
    private static final int SPHERE_SEGMENTS = 28;
    private static final int STORM_SEGMENTS = 160;
    private static final int STORM_PARTICLE_COUNT = 116;
    private static final int ARC_COUNT = 9;
    private static final float CORE_PULSE_SPEED = 0.085F;
    private static final float BAND_ROTATION_SPEED = 1.35F;
    private static final float PARTICLE_SPEED = 0.048F;
    private static final float ARC_CYCLE_SPEED = 0.023F;
    private static final double TWO_PI = Math.PI * 2.0D;

    @Override
    public void render(TilePlasmaStorm te, double x, double y, double z,
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
                drawCore(shader, ticks);
                drawStormShell(shader, ticks);
                drawStormBands(shader, ticks);
                drawFastParticles(shader, ticks);
                drawLightning(shader, ticks);
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("plasma storm shader render failed: " + ex.getMessage());
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

    private void drawCore(ShaderProgram shader, float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * CORE_PULSE_SPEED);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 0.0F, 0.0F, 0x83F7FF, 0xC56EFF, 0xFFFFFF,
                0.30F + pulse * 0.10F, 1.15F, (float) INNER_GLOW_RADIUS);
        drawShaderSphere(INNER_GLOW_RADIUS + pulse * 0.20D, 24, 24);

        setTechUniforms(shader, ticks, 0.0F, 0.0F, 0xFFFFFF, 0x78F6FF, 0x23E8FF,
                0.58F + pulse * 0.18F, 1.45F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS + pulse * 0.05D, 24, 24);

        setTechUniforms(shader, ticks, 0.0F, 0.0F, 0x78F6FF, 0xFFFFFF, 0x23E8FF,
                0.54F, 1.30F, (float) CORE_RADIUS);
        drawShaderSphere(CORE_RADIUS * 0.72D, 20, 20);
        useAlphaBlend();
    }

    private void drawStormShell(ShaderProgram shader, float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.040F);

        useAdditiveBlend();
        setTechUniforms(shader, ticks, 0.0F, 1.0F, 0x0CB9FF, 0x7A35FF, 0xF8FEFF,
                0.070F + pulse * 0.030F, 0.84F, (float) STORM_RADIUS);
        drawShaderSphere(STORM_RADIUS + pulse * 0.16D, 32, 32);

        setTechUniforms(shader, ticks, 0.0F, 2.0F, 0x77F3FF, 0xBD6DFF, 0xFFFFFF,
                0.11F + pulse * 0.050F, 1.10F, (float) STORM_RADIUS);
        GlStateManager.glLineWidth(1.6F);
        drawShaderWireSphere(STORM_RADIUS * 0.98D, 9, 16);
        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
    }

    private void drawStormBands(ShaderProgram shader, float ticks) {
        drawStormBand(shader, ticks, 34.0F, 0.0F, 1.0F, 0.0F, 0x19CFFF, 0.62F);
        drawStormBand(shader, ticks, -52.0F, 1.0F, 0.15F, 0.25F, 0xA85CFF, -0.48F);
        drawStormBand(shader, ticks, 72.0F, 0.35F, 1.0F, 0.0F, 0xF8FEFF, 0.32F);
        drawStormBand(shader, ticks, -18.0F, 1.0F, 0.0F, 0.45F, 0x00F0B8, -0.86F);
    }

    private void drawStormBand(ShaderProgram shader, float ticks, float tilt, float axisX, float axisY, float axisZ,
                               int color, float speedScale) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.061F + tilt * 0.04F);
        double radius = STORM_RADIUS * (0.68D + pulse * 0.06D);

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(tilt, axisX, axisY, axisZ);
        GlStateManager.rotate(ticks * BAND_ROTATION_SPEED * speedScale, 0.0F, 1.0F, 0.0F);

        setTechUniforms(shader, ticks, 0.0F, 2.0F, color, 0xFFFFFF, 0xBD6DFF,
                0.14F + pulse * 0.08F, 1.18F, (float) radius);
        drawShaderFlatBand(radius, 0.055D + pulse * 0.018D, STORM_SEGMENTS);

        setTechUniforms(shader, ticks, 0.0F, 2.0F, color, 0xFFFFFF, 0x43F1FF,
                0.22F + pulse * 0.12F, 1.25F, (float) radius);
        GlStateManager.glLineWidth(2.4F);
        drawShaderCircle(radius, STORM_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawFastParticles(ShaderProgram shader, float ticks) {
        useAdditiveBlend();

        for (int i = 0; i < STORM_PARTICLE_COUNT; i++) {
            double baseAngle = i * 2.399963229728653D;
            double angle = baseAngle + ticks * (PARTICLE_SPEED + (i % 7) * 0.004D);
            double normalizedY = -0.92D + (i % 47) * (1.84D / 46.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - normalizedY * normalizedY));
            double surge = 0.5D + 0.5D * Math.sin(ticks * 0.105D + i * 0.73D);
            double radius = STORM_RADIUS * (0.72D + surge * 0.20D);
            double px = Math.cos(angle) * horizontal * radius;
            double py = normalizedY * radius * 0.64D + Math.sin(ticks * 0.14D + i) * 0.10D;
            double pz = Math.sin(angle) * horizontal * radius;
            double size = 0.018D + surge * 0.040D;
            float particleAlpha = 0.20F + (float) surge * 0.52F;
            int color = i % 5 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x43F1FF : 0xBD6DFF);

            setTechUniforms(shader, ticks, 0.0F, 3.0F, color, 0xFFFFFF, 0x23E8FF,
                    particleAlpha, 1.35F, (float) size);
            GlStateManager.pushMatrix();
            GlStateManager.translate(px, py, pz);
            drawShaderSphere(size, 8, 8);
            GlStateManager.popMatrix();
        }

        useAlphaBlend();
    }

    private void drawLightning(ShaderProgram shader, float ticks) {
        useAdditiveBlend();

        for (int i = 0; i < ARC_COUNT; i++) {
            double flashPhase = (ticks * ARC_CYCLE_SPEED + i * 0.137D) % 1.0D;
            float flash = (float) Math.max(0.0D, Math.sin(Math.PI * flashPhase));
            double angle = i * 0.6981317007977318D + ticks * (0.020D + (i % 4) * 0.006D);
            double length = 0.62D + (i % 3) * 0.22D;
            double y = -1.15D + (i % 5) * 0.55D;
            double radius = STORM_RADIUS * (0.70D + (i % 4) * 0.045D);

            setTechUniforms(shader, ticks, 0.0F, 4.0F, 0xC7FBFF, 0xFFFFFF, 0xC678FF,
                    flash * 0.26F, 1.42F, (float) radius);
            GlStateManager.glLineWidth(3.0F);
            drawJaggedArc(radius, angle, length, y, 0.36D + (i % 2) * 0.20D,
                    0.115D, 8, ticks, 97 + i * 31);

            setTechUniforms(shader, ticks, 0.0F, 4.0F, i % 2 == 0 ? 0xFFFFFF : 0xC678FF,
                    0xC7FBFF, 0xFFFFFF, flash * 0.55F, 1.55F, (float) radius);
            GlStateManager.glLineWidth(1.4F);
            drawJaggedArc(radius, angle, length, y, 0.36D + (i % 2) * 0.20D,
                    0.090D, 8, ticks, 197 + i * 31);
        }

        GlStateManager.glLineWidth(1.0F);
        useAlphaBlend();
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

    private static void drawShaderWireSphere(double radius, int gridLat, int gridLon) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int lat = 1; lat < gridLat; lat++) {
            double theta = Math.PI * lat / gridLat;
            double y = radius * Math.cos(theta);
            double horizontalRadius = radius * Math.sin(theta);
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int lon = 0; lon < gridLon; lon++) {
                double phi = TWO_PI * lon / gridLon;
                addPosition(buffer, horizontalRadius * Math.cos(phi), y, horizontalRadius * Math.sin(phi),
                        lon / (double) gridLon, lat / (double) gridLat, 0.0D, 1.0D, 0.0D);
            }
            tessellator.draw();
        }

        for (int lon = 0; lon < gridLon; lon++) {
            double phi = TWO_PI * lon / gridLon;
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int lat = 0; lat <= gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                addPosition(buffer, radius * Math.sin(theta) * Math.cos(phi),
                        radius * Math.cos(theta),
                        radius * Math.sin(theta) * Math.sin(phi),
                        lon / (double) gridLon, lat / (double) gridLat, 0.0D, 1.0D, 0.0D);
            }
            tessellator.draw();
        }
    }

    private static void drawShaderFlatBand(double radius, double halfWidth, int segments) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double angle = TWO_PI * progress;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addPosition(buffer, cos * (radius + halfWidth), 0.0D, sin * (radius + halfWidth),
                    progress, 1.0D, 0.0D, 1.0D, 0.0D);
            addPosition(buffer, cos * (radius - halfWidth), 0.0D, sin * (radius - halfWidth),
                    progress, 0.0D, 0.0D, 1.0D, 0.0D);
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
            addPosition(buffer, Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius,
                    progress, 0.5D, 0.0D, 1.0D, 0.0D);
        }
        tessellator.draw();
    }

    private static void drawJaggedArc(double radius, double startAngle, double length, double y,
                                      double height, double jitter, int segments, float ticks, int seed) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i <= segments; i++) {
            double progress = i / (double) segments;
            double localJitter = Math.sin(seed * 0.37D + i * 4.91D + ticks * 0.41D) * jitter;
            double angle = startAngle + length * (progress - 0.5D) + localJitter;
            double localRadius = radius + Math.sin(seed + i * 1.7D) * jitter * 2.6D;
            double localY = y + Math.sin(progress * Math.PI) * height
                    + Math.cos(seed * 0.23D + i * 2.13D + ticks * 0.22D) * jitter;
            addPosition(buffer, Math.cos(angle) * localRadius, localY, Math.sin(angle) * localRadius,
                    progress, 0.5D, 0.0D, 1.0D, 0.0D);
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
