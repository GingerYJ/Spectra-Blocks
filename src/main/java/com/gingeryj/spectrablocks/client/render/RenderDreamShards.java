package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.client.render.shader.ShaderProgram;
import com.gingeryj.spectrablocks.tile.TileDreamShards;
import net.minecraft.client.renderer.GlStateManager;

public class RenderDreamShards extends RenderArcaneShaderTile<TileDreamShards> {

    private static final int SHARD_COUNT = 34;
    private static final int GLIMMER_COUNT = 64;
    private static final double FLOAT_RADIUS = 2.48D;
    private static final double RING_RADIUS = 2.16D;
    private static final double RING_CYCLE_SPEED = 0.0085D;
    private static final int[] SHARD_COLORS = new int[]{
            0x86F7FF, 0xB889FF, 0xFF91D8, 0xFFE99D, 0xA6FFCB
    };

    @Override
    protected void renderShaderLayers(TileDreamShards te, float ticks, ShaderProgram shader) {
        float ringPulse = ringPulse(ticks);

        drawSoftCenter(shader, ticks, ringPulse);
        drawAssemblingRing(shader, ticks, ringPulse);
        drawShards(shader, ticks, ringPulse);
        drawGlimmers(shader, ticks);
    }

    private void drawSoftCenter(ShaderProgram shader, float ticks, float ringPulse) {
        float pulse = wave(ticks * 0.045D);

        useAdditiveBlend();
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.38D + pulse * 0.06D, 0xFFFFFF, 0xFFE99D,
                0.30F + ringPulse * 0.12F, 1.55F, 18.0F, 5.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_CORE, 16, 16);
        ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                0.90D + pulse * 0.18D, 0xBEEBFF, 0xB889FF,
                0.11F + ringPulse * 0.08F, 1.20F, 12.0F, 7.0F, pulse,
                ArcaneShaderEffectRenderer.LAYER_AURA, 20, 20);
        useAlphaBlend();
    }

    private void drawAssemblingRing(ShaderProgram shader, float ticks, float ringPulse) {
        if (ringPulse <= 0.01F) {
            return;
        }

        useAdditiveBlend();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(68.0F, 1.0F, 0.0F, 0.20F);
        GlStateManager.rotate(ticks * 0.19F, 0.0F, 1.0F, 0.0F);
        ArcaneShaderEffectRenderer.drawCircleRibbonLayer(shader, ticks, RING_RADIUS,
                0.090D, 0xF6FBFF, 0xB889FF, 0.24F * ringPulse,
                1.25F, 18.0F, 17.0F, ringPulse, 128);
        GlStateManager.glLineWidth(1.8F);
        ArcaneShaderEffectRenderer.drawLatitudeCircleLayer(shader, ticks,
                RING_RADIUS, 0.0D, 0xB889FF, 0xFFFFFF,
                0.32F * ringPulse, 1.35F, 22.0F, 23.0F, ringPulse, 128);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.popMatrix();
        useAlphaBlend();
    }

    private void drawShards(ShaderProgram shader, float ticks, float ringPulse) {
        useAlphaBlend();
        for (int i = 0; i < SHARD_COUNT; i++) {
            double driftYaw = i * GOLDEN_ANGLE + ticks * (0.006D + (i % 6) * 0.0011D);
            double yNorm = -0.78D + (i % 17) * (1.56D / 16.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double wobble = Math.sin(ticks * 0.041D + i * 0.79D) * 0.24D;
            double driftRadius = FLOAT_RADIUS + wobble + (i % 4) * 0.035D;
            double driftX = Math.cos(driftYaw) * horizontal * driftRadius;
            double driftY = yNorm * driftRadius * 0.62D + Math.sin(ticks * 0.028D + i) * 0.16D;
            double driftZ = Math.sin(driftYaw) * horizontal * driftRadius;

            double ringAngle = i * Math.PI * 2.0D / SHARD_COUNT + ticks * 0.012D;
            double ringX = Math.cos(ringAngle) * RING_RADIUS;
            double ringY = Math.sin(i * 0.70D + ticks * 0.055D) * 0.055D;
            double ringZ = Math.sin(ringAngle) * RING_RADIUS;
            double shardX = lerp(driftX, ringX, ringPulse);
            double shardY = lerp(driftY, ringY, ringPulse);
            double shardZ = lerp(driftZ, ringZ, ringPulse);
            int color = SHARD_COLORS[i % SHARD_COLORS.length];
            int accentColor = SHARD_COLORS[(i + 2) % SHARD_COLORS.length];
            float alpha = 0.30F + 0.26F * wave(ticks * 0.052D + i * 0.42D) + ringPulse * 0.16F;
            double size = 0.20D + (i % 5) * 0.028D;

            GlStateManager.pushMatrix();
            GlStateManager.translate(shardX, shardY, shardZ);
            GlStateManager.rotate((float) (ticks * (1.55D + (i % 4) * 0.24D) + i * 31.0D),
                    0.34F + (i % 3) * 0.18F, 1.0F, 0.25F);
            GlStateManager.rotate((float) (i * 23.0D + ringPulse * 180.0D), 0.0F, 0.0F, 1.0F);
            ArcaneShaderEffectRenderer.drawShardLayer(shader, ticks, size,
                    0.38D + (i % 4) * 0.035D, color, accentColor,
                    alpha, 1.30F, 20.0F, i * 19.0F, ringPulse);
            useAdditiveBlend();
            drawShardGlints(shader, ticks, size, 0.38D + (i % 4) * 0.035D,
                    color, alpha * 0.24F, i * 23.0F);
            useAlphaBlend();
            GlStateManager.popMatrix();
        }
    }

    private void drawGlimmers(ShaderProgram shader, float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < GLIMMER_COUNT; i++) {
            double yaw = i * GOLDEN_ANGLE - ticks * (0.004D + (i % 4) * 0.0007D);
            double yNorm = -0.86D + (i % 29) * (1.72D / 28.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = 1.15D + (i % 11) * 0.14D + wave(ticks * 0.035D + i) * 0.32D;
            double glimmerX = Math.cos(yaw) * horizontal * radius;
            double glimmerY = yNorm * radius * 0.78D;
            double glimmerZ = Math.sin(yaw) * horizontal * radius;
            int color = SHARD_COLORS[(i + 2) % SHARD_COLORS.length];
            float alpha = 0.12F + 0.18F * wave(ticks * 0.065D + i * 0.71D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(glimmerX, glimmerY, glimmerZ);
            ArcaneShaderEffectRenderer.drawSphereLayer(shader, ticks,
                    0.018D + (i % 3) * 0.006D, color, 0xFFFFFF,
                    alpha, 1.45F, 14.0F, i * 7.0F, alpha,
                    ArcaneShaderEffectRenderer.LAYER_MOTE, 5, 5);
            GlStateManager.popMatrix();
        }
        useAlphaBlend();
    }

    private void drawShardGlints(ShaderProgram shader, float ticks, double width,
                                 double height, int color, float alpha, float seed) {
        GlStateManager.glLineWidth(1.2F);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, height * 0.58D, 0.0D,
                -width * 0.42D, -height * 0.34D, width * 0.10D,
                0xFFFFFF, color, alpha, 1.35F, 18.0F, seed, alpha);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, height * 0.58D, 0.0D,
                width * 0.36D, -height * 0.18D, width * 0.16D,
                0xFFFFFF, color, alpha * 0.92F, 1.35F, 18.0F, seed + 1.0F, alpha);
        ArcaneShaderEffectRenderer.drawLineLayer(shader, ticks,
                0.0D, height * 0.58D, 0.0D,
                width * 0.12D, -height * 0.46D, -width * 0.38D,
                0xFFFFFF, color, alpha * 0.84F, 1.35F, 18.0F, seed + 2.0F, alpha);
        GlStateManager.glLineWidth(1.0F);
    }

    private static float ringPulse(float ticks) {
        double cycle = fract(ticks * RING_CYCLE_SPEED);
        double center = 0.68D;
        double width = 0.105D;
        double distance = Math.abs(cycle - center);
        if (distance > width) {
            return 0.0F;
        }

        double normalized = 1.0D - distance / width;
        return (float) (normalized * normalized * (3.0D - 2.0D * normalized));
    }
}
