package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderMicroUniverse extends TileEntitySpecialRenderer<TileMicroUniverse> {

    private static final double SHELL_RADIUS = 5.45D;
    private static final int SHELL_LAT_SEGMENTS = 36;
    private static final int SHELL_LON_SEGMENTS = 36;
    private static final int SHADER_SHELL_LAT_SEGMENTS = 48;
    private static final int SHADER_SHELL_LON_SEGMENTS = 48;
    private static final int ORBIT_SEGMENTS = 128;
    private static final double ORBIT_SPEED_SCALE = 0.28D;
    private static final int STAR_COUNT = 64;
    private static final RenderHelper.BillboardPoint[] STARS =
            RenderHelper.createBillboardPoints(STAR_COUNT);
    private static final ResourceLocation SUN_TEXTURE =
            new ResourceLocation(Reference.MOD_ID, "textures/effects/planets/sun.png");

    private static final Planet[] PLANETS = new Planet[]{
            new Planet(1.00D, 0.085D, 0.135D, 0xA7B5C8, 1.25F, 0.02D, 0xA8C4FF, "mercury", 24),
            new Planet(1.40D, 0.118D, 0.105D, 0xE8B36A, 2.35F, -0.03D, 0xFFD58A, "venus", 24),
            new Planet(1.86D, 0.145D, 0.082D, 0x4AA3FF, 3.30F, 0.04D, 0x74B8FF, "earth", 28),
            new Planet(2.34D, 0.122D, 0.066D, 0xD96642, 4.20F, -0.02D, 0xFF8064, "mars", 24),
            new Planet(3.02D, 0.275D, 0.043D, 0xD8B076, 5.60F, 0.03D, 0xFFE0A3, "jupiter", 34),
            new Planet(3.78D, 0.210D, 0.031D, 0x95B7D8, 0.75F, -0.04D, 0xB6D7FF, "saturn", 30),
            new Planet(4.52D, 0.165D, 0.023D, 0x75D3E8, 4.85F, 0.05D, 0x9DEFFF, "uranus", 28)
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
            drawUniverseShell(ticks);
            drawSolarSystem(ticks);
            drawMeteors(ticks);
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

    private void drawUniverseShell(float ticks) {
        if (drawShaderUniverseShell(ticks)) {
            return;
        }

        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.018F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.07F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(16.0F, 1.0F, 0.0F, 0.2F);
        RenderHelper.drawSphere(SHELL_RADIUS, 0x01020A, 0.50F + 0.08F * pulse,
                SHELL_LAT_SEGMENTS, SHELL_LON_SEGMENTS);
        RenderHelper.drawWireframeSphere(SHELL_RADIUS * 1.012D, 0x20305E, 0.08F + 0.06F * pulse, 10, 16);
        GlStateManager.popMatrix();

        drawStars(ticks);
    }

    private boolean drawShaderUniverseShell(float ticks) {
        if (!ModConfig.enableShaderEffects()) {
            return false;
        }

        ShaderProgram shader = ShaderManager.getProgram("micro_universe_shell");
        if (shader == null) {
            return false;
        }

        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.018F);
        boolean textureWasEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean matrixPushed = false;
        try {
            GlStateManager.pushMatrix();
            matrixPushed = true;
            GlStateManager.rotate(ticks * 0.07F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(16.0F, 1.0F, 0.0F, 0.2F);
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();

            if (!shader.begin()) {
                return false;
            }
            shader.setUniform1f("uTime", ticks);
            shader.setUniform1f("uPulse", pulse);
            shader.setUniform3f("uShellColor", 0.004F, 0.008F, 0.035F);
            shader.setUniform3f("uNebulaColor", 0.07F, 0.13F, 0.35F);
            shader.setUniform3f("uStarColor", 0.82F, 0.90F, 1.0F);
            drawShaderShellSphere(SHELL_RADIUS, SHADER_SHELL_LAT_SEGMENTS, SHADER_SHELL_LON_SEGMENTS);

            GlStateManager.glLineWidth(1.0F);
            RenderHelper.drawWireframeSphere(SHELL_RADIUS * 1.012D, 0x20305E, 0.06F + 0.05F * pulse, 10, 16);
            RenderHelper.resetLineWidth();
            return true;
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("micro universe shell render failed: " + ex.getMessage());
            return false;
        } finally {
            shader.end();
            if (matrixPushed) {
                GlStateManager.popMatrix();
            }
            if (cullWasEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            if (textureWasEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
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

    private void drawStars(float ticks) {
        for (int i = 0; i < STAR_COUNT; i++) {
            double yaw = i * 2.399963229728653D + ticks * 0.002D;
            double y = -0.92D + (i % 23) * 0.083D;
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double radius = SHELL_RADIUS * 0.88D;
            STARS[i].set(
                    Math.cos(yaw) * horizontal * radius,
                    y * radius,
                    Math.sin(yaw) * horizontal * radius,
                    0.050D + (i % 4) * 0.014D,
                    0xDDE7FF,
                    0.62F
            );
        }
        RenderHelper.drawBillboardGlowPoints(STARS, STAR_COUNT);
        RenderHelper.resetLineWidth();
    }

    private void drawSolarSystem(float ticks) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ticks * 0.014F, 0.0F, 1.0F, 0.0F);

        drawSun(ticks);

        for (Planet planet : PLANETS) {
            drawGlowingOrbit(planet);
            double angle = ticks * planet.speed + planet.phase;
            double planetX = Math.cos(angle) * planet.orbitRadius;
            double planetZ = Math.sin(angle) * planet.orbitRadius;
            GlStateManager.pushMatrix();
            GlStateManager.translate(planetX, planet.verticalOffset, planetZ);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(ticks * planet.selfRotationSpeed, 0.0F, 1.0F, 0.0F);
            GlStateManager.enableTexture2D();
            RenderHelper.drawTexturedSphere(planet.radius, planet.texture, 0.98F,
                    planet.textureSegments, planet.textureSegments);
            GlStateManager.disableTexture2D();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private void drawSun(float ticks) {
        float pulse = 0.5F + 0.5F * (float) Math.sin(ticks * 0.045F);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.enableTexture2D();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(ticks * 0.26F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(6.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.drawTexturedSphere(0.42D, SUN_TEXTURE, 1.0F, 48, 48);
        GlStateManager.popMatrix();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private void drawGlowingOrbit(Planet planet) {
        GlStateManager.glLineWidth(3.0F);
        RenderHelper.drawCircle(planet.orbitRadius, planet.orbitGlowColor, 0.060F, ORBIT_SEGMENTS);
        GlStateManager.glLineWidth(2.0F);
        RenderHelper.drawCircle(planet.orbitRadius, planet.orbitGlowColor, 0.110F, ORBIT_SEGMENTS);
        GlStateManager.glLineWidth(1.0F);
        RenderHelper.drawCircle(planet.orbitRadius, 0xE2ECFF, 0.185F, ORBIT_SEGMENTS);
        RenderHelper.resetLineWidth();
    }

    private void drawMeteors(float ticks) {
        int cycle = Math.floorMod((int) ticks, 260);
        if (cycle > 64) {
            return;
        }

        float progress = cycle / 64.0F;
        float fade = (float) Math.sin(Math.PI * progress);
        double startX = -SHELL_RADIUS * 0.78D;
        double startY = SHELL_RADIUS * 0.52D;
        double startZ = -SHELL_RADIUS * 0.34D;
        double endX = SHELL_RADIUS * 0.68D;
        double endY = -SHELL_RADIUS * 0.18D;
        double endZ = SHELL_RADIUS * 0.42D;
        double headX = lerp(startX, endX, progress);
        double headY = lerp(startY, endY, progress);
        double headZ = lerp(startZ, endZ, progress);
        double tailX = lerp(startX, endX, Math.max(0.0F, progress - 0.18F));
        double tailY = lerp(startY, endY, Math.max(0.0F, progress - 0.18F));
        double tailZ = lerp(startZ, endZ, Math.max(0.0F, progress - 0.18F));

        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(4.0F);
        RenderHelper.drawLine(headX, headY, headZ, tailX, tailY, tailZ, 0x9CCBFF, 0.12F * fade);
        GlStateManager.glLineWidth(2.0F);
        RenderHelper.drawLine(headX, headY, headZ, tailX, tailY, tailZ, 0xDDEBFF, 0.34F * fade);
        RenderHelper.resetLineWidth();
        GlStateManager.translate(headX, headY, headZ);
        RenderHelper.drawSphere(0.070D, 0xFFFFFF, 0.80F * fade, 8, 8);
        RenderHelper.drawSphere(0.130D, 0x75B8FF, 0.22F * fade, 8, 8);
        GlStateManager.popMatrix();
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private static final class Planet {
        private final double orbitRadius;
        private final double radius;
        private final double speed;
        private final int color;
        private final float phase;
        private final double verticalOffset;
        private final int orbitGlowColor;
        private final ResourceLocation texture;
        private final float selfRotationSpeed;
        private final int textureSegments;

        private Planet(double orbitRadius, double radius, double speed, int color,
                       float phase, double verticalOffset, int orbitGlowColor, String textureName,
                       int textureSegments) {
            this.orbitRadius = orbitRadius;
            this.radius = radius;
            this.speed = speed * ORBIT_SPEED_SCALE;
            this.color = color;
            this.phase = phase;
            this.verticalOffset = verticalOffset;
            this.orbitGlowColor = orbitGlowColor;
            this.texture = new ResourceLocation(Reference.MOD_ID, "textures/effects/planets/" + textureName + ".png");
            this.selfRotationSpeed = (float) (1.2D + speed * 10.0D);
            this.textureSegments = textureSegments;
        }
    }
}
