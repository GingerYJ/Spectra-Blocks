package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileCosmicBackgroundRadiationField extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 6.15D;
    private static final int PARTICLE_INTERVAL = 11;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double y = -1.0D + world.rand.nextDouble() * 2.0D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double radius = 5.35D + world.rand.nextDouble() * 0.35D;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double particleX = Math.cos(yaw) * horizontal * radius;
        double particleY = y * radius;
        double particleZ = Math.sin(yaw) * horizontal * radius;

            }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
