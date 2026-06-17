package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderMicroUniverse extends TileEntitySpecialRenderer<TileMicroUniverse> {

    private static final double SHELL_RADIUS = 5.45D;
    private static final int SHADER_SHELL_LAT_SEGMENTS = 48;
    private static final int SHADER_SHELL_LON_SEGMENTS = 48;
    private static final int SHADER_BODY_LAT_SEGMENTS = 28;
    private static final int SHADER_BODY_LON_SEGMENTS = 28;
    private static final int ORBIT_SEGMENTS = 96;
    private static final double ORBIT_SPEED_SCALE = 0.18D;
    private static final float METEOR_CYCLE_TICKS = 340.0F;
    private static final float METEOR_ACTIVE_TICKS = 86.0F;

    private static final Planet[] PLANETS = new Planet[]{
            new Planet(1.00D, 0.085D, 0.135D, 0xA7B5C8, 0x6D7A8B, 1.25F, 0.02D, 0xA8C4FF, 0.0F),
            new Planet(1.40D, 0.118D, 0.105D, 0xE8B36A, 0xFFF0A8, 2.35F, -0.03D, 0xFFD58A, 1.0F),
            new Planet(1.86D, 0.145D, 0.082D, 0x4AA3FF, 0x5AD470, 3.30F, 0.04D, 0x74B8FF, 2.0F),
            new Planet(2.34D, 0.122D, 0.066D, 0xD96642, 0x8B392E, 4.20F, -0.02D, 0xFF8064, 3.0F),
            new Planet(3.02D, 0.275D, 0.043D, 0xD8B076, 0xFFF0C2, 5.60F, 0.03D, 0xFFE0A3, 4.0F),
            new Planet(3.78D, 0.210D, 0.031D, 0x95B7D8, 0xE8D59A, 0.75F, -0.04D, 0xB6D7FF, 5.0F),
            new Planet(4.52D, 0.165D, 0.023D, 0x75D3E8, 0xC7F5FF, 4.85F, 0.05D, 0x9DEFFF, 6.0F),
            new Planet(5.05D, 0.150D, 0.018D, 0x466DFF, 0xB8D4FF, 2.10F, -0.03D, 0x89AFFF, 7.0F)
    };

    @Override
    public void render(TileMicroUniverse te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;
        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(ModConfig.microUniverseScale());
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
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        try {
            ShaderProgram shellShader = ShaderManager.getProgram("micro_universe_shell");
            ShaderProgram bodyShader = ShaderManager.getProgram("micro_universe_body");
            ShaderProgram colorShader = ShaderManager.getProgram("basic");
            drawUniverseShell(ticks, shellShader, colorShader);
            drawSolarSystem(ticks, bodyShader, colorShader);
            drawMeteors(ticks, bodyShader, colorShader);
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

    private void drawUniverseShell(float ticks, ShaderProgram shellShader, ShaderProgram colorShader) {
        if (shellShader == null || !shellShader.begin()) {
            return;
        }

        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.018F);
        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
        boolean matrixPushed = false;
        try {
            GlStateManager.pushMatrix();
            matrixPushed = true;
            GlStateManager.rotate(ticks * 0.07F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(16.0F, 1.0F, 0.0F, 0.2F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableCull();
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);

            shellShader.setUniform1f("uTime", ticks);
            shellShader.setUniform1f("uPulse", pulse);
            shellShader.setUniform3f("uShellColor", 0.000F, 0.001F, 0.006F);
            shellShader.setUniform3f("uNebulaColor", 0.018F, 0.040F, 0.125F);
            shellShader.setUniform3f("uStarColor", 0.92F, 0.96F, 1.0F);
            drawShaderShellSphere(SHELL_RADIUS, SHADER_SHELL_LAT_SEGMENTS, SHADER_SHELL_LON_SEGMENTS);

            shellShader.end();
            restoreCullState(cullWasEnabled, previousCullFace);
            GlStateManager.glLineWidth(1.0F);
            drawShaderWireframeSphere(colorShader, SHELL_RADIUS * 1.012D, 0x20305E, 0.045F + 0.035F * pulse, 9, 14);
            RenderHelper.resetLineWidth();
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("micro universe shell render failed: " + ex.getMessage());
        } finally {
            shellShader.end();
            if (matrixPushed) {
                GlStateManager.popMatrix();
            }
            restoreCullState(cullWasEnabled, previousCullFace);
            if (textureWasEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
        }
    }

    private static void restoreCullState(boolean cullWasEnabled, int previousCullFace) {
        GlStateManager.cullFace(previousCullFace == GL11.GL_FRONT
                ? GlStateManager.CullFace.FRONT
                : GlStateManager.CullFace.BACK);
        if (cullWasEnabled) {
            GlStateManager.enableCull();
        } else {
            GlStateManager.disableCull();
        }
    }

    private static void drawShaderShellSphere(double radius, int latSegs, int lonSegs) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int lat = 0; lat < latSegs; lat++) {
            double theta0 = Math.PI * lat / latSegs;
            double theta1 = Math.PI * (lat + 1) / latSegs;
            for (int lon = 0; lon < lonSegs; lon++) {
                double phi0 = 2.0D * Math.PI * lon / lonSegs;
                double phi1 = 2.0D * Math.PI * (lon + 1) / lonSegs;
                addShaderShellVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addShaderShellVertex(buffer, radius, theta1, phi0, lon / (double) lonSegs, (lat + 1.0D) / latSegs);
                addShaderShellVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addShaderShellVertex(buffer, radius, theta0, phi0, lon / (double) lonSegs, lat / (double) latSegs);
                addShaderShellVertex(buffer, radius, theta1, phi1, (lon + 1.0D) / lonSegs, (lat + 1.0D) / latSegs);
                addShaderShellVertex(buffer, radius, theta0, phi1, (lon + 1.0D) / lonSegs, lat / (double) latSegs);
            }
        }
        tessellator.draw();
    }

    private static void addShaderShellVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                             double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private void drawSolarSystem(float ticks, ShaderProgram bodyShader, ShaderProgram colorShader) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-4.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(ticks * 0.014F, 0.0F, 1.0F, 0.0F);

        drawSun(ticks, bodyShader);

        for (Planet planet : PLANETS) {
            drawGlowingOrbit(colorShader, planet, ticks);
            double angle = ticks * planet.speed + planet.phase;
            double planetX = Math.cos(angle) * planet.orbitRadius;
            double planetZ = Math.sin(angle) * planet.orbitRadius;
            GlStateManager.pushMatrix();
            GlStateManager.translate(planetX, planet.verticalOffset, planetZ);
            GlStateManager.rotate(ticks * planet.selfRotationSpeed, 0.0F, 1.0F, 0.0F);
            drawPlanet(bodyShader, planet, ticks);
            if (planet.style == 5.0F) {
                drawSaturnRing(colorShader, planet);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private void drawSun(float ticks, ShaderProgram bodyShader) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.045F);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.26F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(6.0F, 0.0F, 0.0F, 1.0F);
        drawShaderBody(bodyShader, 0.42D, 0.0F, 0.0F, 0xFFE071, 0xFFFFFF, 1.0F, 1.08F + pulse * 0.12F,
                ticks, 42);
        GlStateManager.popMatrix();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawPlanet(ShaderProgram bodyShader, Planet planet, float ticks) {
        drawShaderBody(bodyShader, planet.radius, 1.0F, planet.style, planet.color, planet.accentColor, 1.0F, 1.18F,
                ticks, SHADER_BODY_LAT_SEGMENTS);
    }

    private void drawShaderBody(ShaderProgram bodyShader, double radius, float bodyType, float style,
                                int baseColor, int accentColor, float alpha, float brightness, float ticks,
                                int segments) {
        if (bodyShader == null || !bodyShader.begin()) {
            return;
        }

        float[] base = RenderHelper.unpackRGB(baseColor);
        float[] accent = RenderHelper.unpackRGB(accentColor);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
        try {
            GlStateManager.enableCull();
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            bodyShader.setUniform1f("uTime", ticks * 0.035F);
            bodyShader.setUniform1f("uBodyType", bodyType);
            bodyShader.setUniform1f("uStyle", style);
            bodyShader.setUniform1f("uAlpha", alpha);
            bodyShader.setUniform1f("uBrightness", brightness);
            bodyShader.setUniform3f("uBaseColor", base[0], base[1], base[2]);
            bodyShader.setUniform3f("uAccentColor", accent[0], accent[1], accent[2]);
            drawShaderShellSphere(radius, segments, segments);
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("micro universe body render failed: " + ex.getMessage());
        } finally {
            bodyShader.end();
            restoreCullState(cullWasEnabled, previousCullFace);
        }
    }

    private void drawGlowingOrbit(ShaderProgram colorShader, Planet planet, float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.018F + planet.phase);
        GlStateManager.glLineWidth(2.2F);
        drawShaderCircle(colorShader, planet.orbitRadius, planet.orbitGlowColor, 0.120F + pulse * 0.035F, ORBIT_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        drawShaderCircle(colorShader, planet.orbitRadius, 0xF0F6FF, 0.205F + pulse * 0.045F, ORBIT_SEGMENTS);
        RenderHelper.resetLineWidth();
    }

    private void drawSaturnRing(ShaderProgram colorShader, Planet planet) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(22.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.glLineWidth(2.0F);
        drawShaderCircle(colorShader, planet.radius * 1.75D, 0xE8D59A, 0.28F, 72);
        GlStateManager.glLineWidth(1.0F);
        drawShaderCircle(colorShader, planet.radius * 2.20D, 0xFFFFFF, 0.16F, 72);
        RenderHelper.resetLineWidth();
        GlStateManager.popMatrix();
    }

    private void drawMeteors(float ticks, ShaderProgram bodyShader, ShaderProgram colorShader) {
        float cycle = ticks % METEOR_CYCLE_TICKS;
        if (cycle < 0.0F) {
            cycle += METEOR_CYCLE_TICKS;
        }
        if (cycle > METEOR_ACTIVE_TICKS) {
            return;
        }

        float progress = cycle / METEOR_ACTIVE_TICKS;
        float easedProgress = smoothstep(progress);
        float tailProgress = Math.max(0.0F, progress - 0.16F);
        float easedTailProgress = smoothstep(tailProgress);
        float fade = (float) Math.sin(Math.PI * progress);
        double startX = -SHELL_RADIUS * 0.78D;
        double startY = SHELL_RADIUS * 0.52D;
        double startZ = -SHELL_RADIUS * 0.34D;
        double endX = SHELL_RADIUS * 0.68D;
        double endY = -SHELL_RADIUS * 0.18D;
        double endZ = SHELL_RADIUS * 0.42D;
        double headArc = Math.sin(Math.PI * progress);
        double tailArc = Math.sin(Math.PI * tailProgress);
        double headX = lerp(startX, endX, easedProgress);
        double headY = lerp(startY, endY, easedProgress) + headArc * 0.28D;
        double headZ = lerp(startZ, endZ, easedProgress) - headArc * 0.18D;
        double tailX = lerp(startX, endX, easedTailProgress);
        double tailY = lerp(startY, endY, easedTailProgress) + tailArc * 0.28D;
        double tailZ = lerp(startZ, endZ, easedTailProgress) - tailArc * 0.18D;

        GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.glLineWidth(4.0F);
        drawShaderLine(colorShader, headX, headY, headZ, tailX, tailY, tailZ, 0x9CCBFF, 0.085F * fade);
        GlStateManager.glLineWidth(2.0F);
        drawShaderLine(colorShader, headX, headY, headZ, tailX, tailY, tailZ, 0xDDEBFF, 0.28F * fade);
        RenderHelper.resetLineWidth();
        GlStateManager.translate(headX, headY, headZ);
        drawShaderBody(bodyShader, 0.064D, 2.0F, 0.0F, 0xFFFFFF, 0x75B8FF, 0.72F * fade, 1.0F, ticks, 10);
        drawShaderBody(bodyShader, 0.120D, 2.0F, 1.0F, 0x75B8FF, 0xFFFFFF, 0.18F * fade, 0.72F, ticks, 10);
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.popMatrix();
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private static float smoothstep(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private static void drawShaderWireframeSphere(ShaderProgram colorShader, double radius, int color,
                                                  float alpha, int gridLat, int gridLon) {
        if (colorShader == null || !colorShader.begin()) {
            return;
        }

        try {
            setColorShaderUniforms(colorShader, alpha);
            float[] rgb = RenderHelper.unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            for (int lat = 1; lat < gridLat; lat++) {
                double theta = Math.PI * lat / gridLat;
                double y = radius * Math.cos(theta);
                double horizontalRadius = radius * Math.sin(theta);
                buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
                for (int lon = 0; lon <= gridLon; lon++) {
                    double phi = 2.0D * Math.PI * lon / gridLon;
                    buffer.pos(horizontalRadius * Math.cos(phi), y, horizontalRadius * Math.sin(phi))
                            .color(rgb[0], rgb[1], rgb[2], 1.0F)
                            .endVertex();
                }
                tessellator.draw();
            }

            for (int lon = 0; lon < gridLon; lon++) {
                double phi = 2.0D * Math.PI * lon / gridLon;
                buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                for (int lat = 0; lat <= gridLat; lat++) {
                    double theta = Math.PI * lat / gridLat;
                    buffer.pos(radius * Math.sin(theta) * Math.cos(phi),
                                    radius * Math.cos(theta),
                                    radius * Math.sin(theta) * Math.sin(phi))
                            .color(rgb[0], rgb[1], rgb[2], 1.0F)
                            .endVertex();
                }
                tessellator.draw();
            }
        } finally {
            colorShader.end();
        }
    }

    private static void drawShaderCircle(ShaderProgram colorShader, double radius, int color, float alpha, int segments) {
        if (colorShader == null || !colorShader.begin()) {
            return;
        }

        try {
            setColorShaderUniforms(colorShader, alpha);
            float[] rgb = RenderHelper.unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < segments; i++) {
                double angle = 2.0D * Math.PI * i / segments;
                buffer.pos(radius * Math.cos(angle), 0.0D, radius * Math.sin(angle))
                        .color(rgb[0], rgb[1], rgb[2], 1.0F)
                        .endVertex();
            }
            tessellator.draw();
        } finally {
            colorShader.end();
        }
    }

    private static void drawShaderLine(ShaderProgram colorShader, double x1, double y1, double z1,
                                       double x2, double y2, double z2, int color, float alpha) {
        if (colorShader == null || !colorShader.begin()) {
            return;
        }

        try {
            setColorShaderUniforms(colorShader, alpha);
            float[] rgb = RenderHelper.unpackRGB(color);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x1, y1, z1).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
            buffer.pos(x2, y2, z2).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
            tessellator.draw();
        } finally {
            colorShader.end();
        }
    }

    private static void setColorShaderUniforms(ShaderProgram colorShader, float alpha) {
        colorShader.setUniform1f("alpha", alpha);
        colorShader.setUniform4f("tint", 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static final class Planet {
        private final double orbitRadius;
        private final double radius;
        private final double speed;
        private final int color;
        private final int accentColor;
        private final float phase;
        private final double verticalOffset;
        private final int orbitGlowColor;
        private final float selfRotationSpeed;
        private final float style;

        private Planet(double orbitRadius, double radius, double speed, int color, int accentColor,
                       float phase, double verticalOffset, int orbitGlowColor, float style) {
            this.orbitRadius = orbitRadius;
            this.radius = radius;
            this.speed = speed * ORBIT_SPEED_SCALE;
            this.color = color;
            this.accentColor = accentColor;
            this.phase = phase;
            this.verticalOffset = verticalOffset;
            this.orbitGlowColor = orbitGlowColor;
            this.selfRotationSpeed = (float) (1.2D + speed * 10.0D);
            this.style = style;
        }
    }
}
