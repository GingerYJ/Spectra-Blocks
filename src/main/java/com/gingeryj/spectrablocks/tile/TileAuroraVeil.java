package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileAuroraVeil extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.35D;
    private static final int PARTICLE_INTERVAL = 5;
    private static final double PARTICLE_WIDTH = 2.25D;
    private static final double PARTICLE_HEIGHT = 2.85D;
    private static final double PARTICLE_DRIFT = 0.018D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double width = (world.rand.nextDouble() - 0.5D) * PARTICLE_WIDTH;
        double rise = world.rand.nextDouble() * PARTICLE_HEIGHT;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double particleX = Math.cos(angle) * width;
        double particleZ = Math.sin(angle) * width;

                world.spawnParticle(EnumParticleTypes.END_ROD,
                centerX + particleX * 0.72D,
                centerY + rise * 0.72D,
                centerZ + particleZ * 0.72D,
                Math.cos(angle + Math.PI * 0.5D) * PARTICLE_DRIFT,
                0.020D + world.rand.nextDouble() * 0.015D,
                Math.sin(angle + Math.PI * 0.5D) * PARTICLE_DRIFT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
