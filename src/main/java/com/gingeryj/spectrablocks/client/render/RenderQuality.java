package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

final class RenderQuality {

    private static final ThreadLocal<Integer> QUALITY_LEVEL = ThreadLocal.withInitial(() -> 0);
    private static final double MEDIUM_DISTANCE_SQUARED = 4096.0D;
    private static final double LOW_DISTANCE_SQUARED = 16384.0D;

    private RenderQuality() {
    }

    static void update(double centerX, double centerY, double centerZ) {
        Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        if (view == null) {
            QUALITY_LEVEL.set(0);
            return;
        }

        double dx = view.posX - centerX;
        double dy = view.posY - centerY;
        double dz = view.posZ - centerZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared > LOW_DISTANCE_SQUARED) {
            QUALITY_LEVEL.set(2);
        } else if (distanceSquared > MEDIUM_DISTANCE_SQUARED) {
            QUALITY_LEVEL.set(1);
        } else {
            QUALITY_LEVEL.set(0);
        }
    }

    static int level() {
        return QUALITY_LEVEL.get();
    }

    static boolean low() {
        return level() >= 2;
    }

    static int scaleSegments(int value, int min, int max) {
        int clamped = Math.max(min, Math.min(value, max));
        int level = level();
        if (level == 1) {
            return Math.max(min, (int) Math.ceil(clamped * 0.72D));
        }
        if (level >= 2) {
            return Math.max(min, (int) Math.ceil(clamped * 0.48D));
        }
        return clamped;
    }

    static float alphaMultiplier() {
        int level = level();
        if (level == 1) {
            return 0.82F;
        }
        if (level >= 2) {
            return 0.62F;
        }
        return 1.0F;
    }
}
