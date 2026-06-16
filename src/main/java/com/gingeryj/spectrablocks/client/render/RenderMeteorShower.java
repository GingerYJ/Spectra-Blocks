package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.tile.TileMeteorShower;
import net.minecraft.client.renderer.GlStateManager;

public class RenderMeteorShower extends RenderCelestialEffectBase<TileMeteorShower> {

    private static final double FIELD_RADIUS = 2.12D;
    private static final double TRAIL_LENGTH = 0.78D;
    private static final double PATH_LENGTH = 4.55D;
    private static final double GOLDEN_ANGLE = 2.399963229728653D;
    private static final int METEOR_COUNT = 18;
    private static final int STAR_COUNT = 42;
    private static final float BASE_SPEED = 0.012F;

    @Override
    protected void renderCelestialEffect(TileMeteorShower te, float ticks) {
        drawFieldShell(ticks);
        drawBackgroundSparks(ticks);
        drawMeteors(ticks);
    }

    private void drawFieldShell(float ticks) {
        float pulse = wave(ticks * 0.030D);

        useAdditiveBlend();
        GlStateManager.glLineWidth(1.1F);
        RenderHelper.drawWireframeSphere(FIELD_RADIUS, 0x496DFF, 0.040F + pulse * 0.018F, 5, 12);
        RenderHelper.resetLineWidth();
        useAlphaBlend();
    }

    private void drawBackgroundSparks(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < STAR_COUNT; i++) {
            double yaw = i * GOLDEN_ANGLE + ticks * 0.002D;
            double yNorm = -0.82D + (i % 23) * (1.64D / 22.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
            double radius = FIELD_RADIUS * (0.24D + (i % 11) * 0.056D);
            double x = Math.cos(yaw) * horizontal * radius;
            double y = yNorm * radius + Math.sin(ticks * 0.017D + i) * 0.030D;
            double z = Math.sin(yaw) * horizontal * radius;
            float alpha = 0.055F + 0.11F * wave(ticks * 0.036D + i * 0.73D);

            drawSphereAt(x, y, z, 0.012D + (i % 3) * 0.004D,
                    i % 5 == 0 ? 0xFFFFFF : 0x88B8FF, alpha, 5, 5);
        }
        useAlphaBlend();
    }

    private void drawMeteors(float ticks) {
        useAdditiveBlend();
        for (int i = 0; i < METEOR_COUNT; i++) {
            double progress = fract(ticks * (BASE_SPEED + (i % 5) * 0.0014D) + i * 0.071D);
            float fade = (float) Math.sin(Math.PI * progress);
            double yaw = i * 1.731D + 0.37D;
            double pitch = -0.52D + (i % 7) * 0.105D;
            double dx = Math.cos(pitch) * Math.cos(yaw);
            double dy = Math.sin(pitch) - 0.32D;
            double dz = Math.cos(pitch) * Math.sin(yaw);
            double invLength = 1.0D / Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx *= invLength;
            dy *= invLength;
            dz *= invLength;

            double laneAngle = i * GOLDEN_ANGLE;
            double laneRadius = FIELD_RADIUS * (0.10D + (i % 6) * 0.065D);
            double laneX = Math.cos(laneAngle) * laneRadius;
            double laneY = Math.sin(laneAngle * 1.3D) * laneRadius * 0.48D;
            double laneZ = Math.sin(laneAngle) * laneRadius;
            double travel = (progress - 0.5D) * PATH_LENGTH;
            double headX = laneX + dx * travel;
            double headY = laneY + dy * travel;
            double headZ = laneZ + dz * travel;
            double length = TRAIL_LENGTH * (0.74D + (i % 4) * 0.10D) * (0.72D + fade * 0.44D);
            double tailX = headX - dx * length;
            double tailY = headY - dy * length;
            double tailZ = headZ - dz * length;
            int color = i % 4 == 0 ? 0xFFFFFF : (i % 2 == 0 ? 0x8EC8FF : 0xFFD28A);

            GlStateManager.glLineWidth(3.2F);
            RenderHelper.drawLine(tailX, tailY, tailZ, headX, headY, headZ,
                    color, fade * 0.22F);
            GlStateManager.glLineWidth(1.3F);
            RenderHelper.drawLine(lerp(tailX, headX, 0.38D), lerp(tailY, headY, 0.38D), lerp(tailZ, headZ, 0.38D),
                    headX, headY, headZ, 0xFFFFFF, fade * 0.42F);
            RenderHelper.resetLineWidth();
            drawSphereAt(headX, headY, headZ, 0.030D + fade * 0.025D, color, fade * 0.46F, 6, 6);
        }
        useAlphaBlend();
    }
}
