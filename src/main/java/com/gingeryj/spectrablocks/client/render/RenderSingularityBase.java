package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderManager;
import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import org.lwjgl.opengl.GL11;

public abstract class RenderSingularityBase<T extends TileScalableEffect> extends TileEntitySpecialRenderer<T> {

    private static final double EVENT_HORIZON_RADIUS = 1.2D;
    private static final double INNER_HALO_BASE = 1.8D;
    private static final double OUTER_HALO_BASE = 2.8D;

    private static final int SPHERE_LAT_SEGMENTS = 28;
    private static final int SPHERE_LON_SEGMENTS = 28;

    private static final float BASE_ANIMATION_SPEED = 1.5F;
    private static final float INNER_ANIMATION_SPEED = 0.7F;
    private static final float OUTER_ANIMATION_SPEED = 0.35F;
    private static final float INNER_EXPAND_BASE = 0.82F;
    private static final float INNER_EXPAND_RANGE = 0.36F;
    private static final float OUTER_EXPAND_BASE = 0.88F;
    private static final float OUTER_EXPAND_RANGE = 0.24F;

    @Override
    public boolean isGlobalRenderer(T te) {
        return true;
    }

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        double centerX = x + 0.5D;
        double centerY = y + 0.5D;
        double centerZ = z + 0.5D;

        float ticks = te.getWorld().getTotalWorldTime() + partialTicks;
        float coreTime = ticks * baseAnimationSpeed();
        float innerTime = ticks * innerAnimationSpeed();
        float outerTime = ticks * outerAnimationSpeed();

        float innerExpand = smoothWave(innerTime * innerExpandFrequency());
        float innerBrightness = nestedWave(innerTime * innerBrightnessFrequency());
        float innerGridEnergy = smoothWave(innerTime * innerGridFrequency());
        float outerExpand = smoothWave(outerTime * outerExpandFrequency());
        float outerBrightness = nestedWave(outerTime * outerBrightnessFrequency());
        float outerGridEnergy = smoothWave(outerTime * outerGridFrequency());

        double innerRadius = INNER_HALO_BASE * (innerExpandBase() + innerExpandRange() * innerExpand);
        double outerRadius = OUTER_HALO_BASE * (outerExpandBase() + outerExpandRange() * outerExpand);
        float innerAlpha = innerAlphaBase() + innerAlphaRange() * innerBrightness;
        float outerAlpha = outerAlphaBase() + outerAlphaRange() * outerBrightness;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, centerZ);
        double renderScale = te.renderScale(defaultRenderScale());
        GlStateManager.scale(renderScale, renderScale, renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        SpectraRenderState.State renderState = SpectraRenderState.beginIsolated();
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);

        ShaderProgram shader = ShaderManager.getProgram("singularity");
        try {
            if (shader != null && shader.begin()) {
                drawShaderLayer(shader, outerRadius, 2.0F, outerHaloColor(), outerGridColor(),
                        outerAlpha, outerGridAlpha(), outerBrightness, outerGridEnergy,
                        ticks, -outerTime * outerRotationSpeed(), 12.0F, 0.5F, 0.0F, 1.0F);
                drawShaderLayer(shader, innerRadius, 1.0F, innerHaloColor(), innerGridColor(),
                        innerAlpha, innerGridAlpha(), innerBrightness, innerGridEnergy,
                        ticks, coreTime * innerRotationSpeed(), 18.0F, 1.0F, 0.0F, 0.3F);
                drawShaderLayer(shader, EVENT_HORIZON_RADIUS, 0.0F, coreColor(), innerGridColor(),
                        coreAlpha(), 0.0F, innerBrightness, 0.0F,
                        ticks, coreTime * 0.16F, 0.0F, 0.0F, 1.0F, 0.0F);
            }
        } catch (RuntimeException ex) {
            ShaderManager.disableShaders("singularity render failed: " + ex.getMessage());
        } finally {
            if (shader != null) {
                shader.end();
            }
            renderState.close();
            GlStateManager.popMatrix();
        }
    }

    private void drawShaderLayer(ShaderProgram shader, double radius, float layer, int primaryColor, int gridColor,
                                 float alpha, float gridAlpha, float pulse, float gridPulse, float ticks,
                                 float mainRotation, float tilt, float tiltX, float tiltY, float tiltZ) {
        float[] primary = RenderHelper.unpackRGB(primaryColor);
        float[] grid = RenderHelper.unpackRGB(gridColor);
        SpectraRenderState.forceShaderLayerState();
        SpectraRenderState.useAlphaBlend();
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        shader.setUniform1f("uTime", ticks * 0.025F);
        shader.setUniform1f("uLayer", layer);
        shader.setUniform1f("uMode", shaderMode());
        shader.setUniform1f("uAlpha", alpha);
        shader.setUniform1f("uGridAlpha", gridAlpha);
        shader.setUniform1f("uPulse", pulse);
        shader.setUniform1f("uGridPulse", gridPulse);
        shader.setUniform3f("uPrimaryColor", primary[0], primary[1], primary[2]);
        shader.setUniform3f("uGridColor", grid[0], grid[1], grid[2]);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(mainRotation, 0.0F, 1.0F, 0.0F);
        if (tilt != 0.0F) {
            GlStateManager.rotate(tilt, tiltX, tiltY, tiltZ);
        }
        drawShaderSphere(radius, SPHERE_LAT_SEGMENTS, SPHERE_LON_SEGMENTS);
        GlStateManager.popMatrix();
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

    private static void addShaderSphereVertex(BufferBuilder buffer, double radius, double theta, double phi,
                                              double u, double v) {
        float normalX = (float) (Math.sin(theta) * Math.cos(phi));
        float normalY = (float) Math.cos(theta);
        float normalZ = (float) (Math.sin(theta) * Math.sin(phi));
        buffer.pos(normalX * radius, normalY * radius, normalZ * radius)
                .tex(u, v)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }

    private static float smoothWave(float time) {
        float phase = wave(time);
        return phase * phase * (3.0F - 2.0F * phase);
    }

    private static float wave(float time) {
        return 0.5F + 0.5F * (float) Math.sin(time);
    }

    private static float nestedWave(float time) {
        return 0.5F + 0.5F * smoothWave(time);
    }

    protected abstract int coreColor();

    protected abstract float coreAlpha();

    protected abstract int innerHaloColor();

    protected abstract int innerGridColor();

    protected abstract int outerHaloColor();

    protected abstract int outerGridColor();

    protected abstract double defaultRenderScale();

    protected float shaderMode() {
        return 0.0F;
    }

    protected float baseAnimationSpeed() {
        return BASE_ANIMATION_SPEED;
    }

    protected float innerAnimationSpeed() {
        return INNER_ANIMATION_SPEED;
    }

    protected float outerAnimationSpeed() {
        return OUTER_ANIMATION_SPEED;
    }

    protected float innerExpandFrequency() {
        return 0.8F;
    }

    protected float innerBrightnessFrequency() {
        return 0.6F;
    }

    protected float innerGridFrequency() {
        return 1.25F;
    }

    protected float outerExpandFrequency() {
        return 0.45F;
    }

    protected float outerBrightnessFrequency() {
        return 0.35F;
    }

    protected float outerGridFrequency() {
        return 0.7F;
    }

    protected float innerRotationSpeed() {
        return 0.45F;
    }

    protected float outerRotationSpeed() {
        return 0.35F;
    }

    protected float innerExpandBase() {
        return INNER_EXPAND_BASE;
    }

    protected float innerExpandRange() {
        return INNER_EXPAND_RANGE;
    }

    protected float outerExpandBase() {
        return OUTER_EXPAND_BASE;
    }

    protected float outerExpandRange() {
        return OUTER_EXPAND_RANGE;
    }

    protected float innerAlphaBase() {
        return 0.15F;
    }

    protected float innerAlphaRange() {
        return 0.35F;
    }

    protected float outerAlphaBase() {
        return 0.08F;
    }

    protected float outerAlphaRange() {
        return 0.18F;
    }

    protected float innerGridAlpha() {
        return 0.4F;
    }

    protected float outerGridAlpha() {
        return 0.12F;
    }
}
