package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileDataStreamMatrix extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.10D;
    private static final int PARTICLE_INTERVAL = 6;
    private static final double COLUMN_RADIUS = 2.70D;
    private static final double COLUMN_HEIGHT = 3.20D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = 0.30D + world.rand.nextDouble() * COLUMN_RADIUS;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.REDSTONE,
                centerX + Math.cos(angle) * radius,
                centerY + (world.rand.nextDouble() - 0.5D) * COLUMN_HEIGHT,
                centerZ + Math.sin(angle) * radius,
                0.0D,
                0.8D + world.rand.nextDouble() * 0.2D,
                0.9D + world.rand.nextDouble() * 0.1D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
