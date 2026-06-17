package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

final class RenderQuality {

    private static final ThreadLocal<Integer> QUALITY_LEVEL = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> FRAME_CROWDED = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Double> DISTANCE_SQUARED = ThreadLocal.withInitial(() -> 0.0D);
    private static final double[] MEDIUM_DISTANCE_SQUARED = new double[]{576.0D, 324.0D, 196.0D};
    private static final double[] LOW_DISTANCE_SQUARED = new double[]{2304.0D, 1296.0D, 784.0D};
    private static final long FRAME_BUCKET_NANOS = 16666667L;
    private static int lastFrameCount = -1;
    private static int renderedThisFrame;

    private RenderQuality() {
    }

    static void update(double centerX, double centerY, double centerZ) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int frameCount = frameBucket();
        if (frameCount != lastFrameCount) {
            lastFrameCount = frameCount;
            renderedThisFrame = 0;
        }
        renderedThisFrame++;
        FRAME_CROWDED.set(renderedThisFrame > crowdedRenderThreshold());

        Entity view = minecraft.getRenderViewEntity();
        if (view == null) {
            QUALITY_LEVEL.set(0);
            DISTANCE_SQUARED.set(0.0D);
            return;
        }

        double dx = view.posX - centerX;
        double dy = view.posY - centerY;
        double dz = view.posZ - centerZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        DISTANCE_SQUARED.set(distanceSquared);
        int mode = performanceMode();
        if (distanceSquared > LOW_DISTANCE_SQUARED[mode]) {
            QUALITY_LEVEL.set(2);
        } else if (distanceSquared > MEDIUM_DISTANCE_SQUARED[mode]) {
            QUALITY_LEVEL.set(1);
        } else {
            QUALITY_LEVEL.set(0);
        }
    }

    static int level() {
        return QUALITY_LEVEL.get();
    }

    static boolean low() {
        return effectiveLevel() >= 2;
    }

    static boolean mediumOrLow() {
        return effectiveLevel() >= 1;
    }

    static boolean shouldRender(double centerX, double centerY, double centerZ) {
        update(centerX, centerY, centerZ);
        if (!low() || DISTANCE_SQUARED.get() < LOW_DISTANCE_SQUARED[performanceMode()] * 1.35D) {
            return true;
        }

        int mode = performanceMode();
        int stride = mode >= 2 || FRAME_CROWDED.get() ? 3 : 2;
        if (stride <= 1) {
            return true;
        }

        int frame = frameBucket();
        int hash = stableHash(centerX, centerY, centerZ);
        return Math.floorMod(frame + hash, stride) == 0;
    }

    static int scaleSegments(int value, int min, int max) {
        int clamped = Math.max(min, Math.min(value, max));
        int level = effectiveLevel();
        if (level == 1) {
            return Math.max(min, (int) Math.ceil(clamped * 0.58D));
        }
        if (level >= 2) {
            return Math.max(min, (int) Math.ceil(clamped * 0.32D));
        }
        return clamped;
    }

    static int detailCount(int value, int min) {
        int level = effectiveLevel();
        if (level == 1) {
            return Math.max(min, (int) Math.ceil(value * 0.62D));
        }
        if (level >= 2) {
            return Math.max(min, (int) Math.ceil(value * 0.34D));
        }
        return value;
    }

    static int detailStride() {
        int level = effectiveLevel();
        if (level == 1) {
            return 2;
        }
        if (level >= 2) {
            return 3;
        }
        return 1;
    }

    static float alphaMultiplier() {
        int level = effectiveLevel();
        if (level == 1) {
            return 0.76F;
        }
        if (level >= 2) {
            return 0.50F;
        }
        return 1.0F;
    }

    private static int effectiveLevel() {
        int crowdedBonus = FRAME_CROWDED.get() ? 1 : 0;
        return Math.min(2, level() + performanceMode() + crowdedBonus);
    }

    private static int performanceMode() {
        int mode = ModConfig.renderPerformanceMode();
        if (mode < 0) {
            return 0;
        }
        if (mode > 2) {
            return 2;
        }
        return mode;
    }

    private static int crowdedRenderThreshold() {
        int mode = performanceMode();
        if (mode >= 2) {
            return 18;
        }
        if (mode == 1) {
            return 28;
        }
        return 42;
    }

    private static int stableHash(double centerX, double centerY, double centerZ) {
        int x = (int) Math.floor(centerX);
        int y = (int) Math.floor(centerY);
        int z = (int) Math.floor(centerZ);
        int hash = x * 73428767 ^ y * 912931 ^ z * 42317861;
        return hash ^ (hash >>> 16);
    }

    private static int frameBucket() {
        return (int) (System.nanoTime() / FRAME_BUCKET_NANOS);
    }
}
