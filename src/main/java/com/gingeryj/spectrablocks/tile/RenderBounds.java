package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

final class RenderBounds {

    private RenderBounds() {
    }

    static AxisAlignedBB centered(BlockPos pos, double radius, double scale) {
        double scaledRadius = radius * scale;
        return new AxisAlignedBB(
                pos.getX() + 0.5D - scaledRadius,
                pos.getY() + 0.5D - scaledRadius,
                pos.getZ() + 0.5D - scaledRadius,
                pos.getX() + 0.5D + scaledRadius,
                pos.getY() + 0.5D + scaledRadius,
                pos.getZ() + 0.5D + scaledRadius
        );
    }
}
