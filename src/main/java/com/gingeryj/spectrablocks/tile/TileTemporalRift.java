package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileTemporalRift extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 2.25D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double PARTICLE_SPREAD_XZ = 0.70D;
    private static final double PARTICLE_SPREAD_Y = 1.45D;
    private static final double PARTICLE_MOTION = 0.035D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double spreadXz = PARTICLE_SPREAD_XZ * scale;
        double spreadY = PARTICLE_SPREAD_Y * scale;
        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + (world.rand.nextDouble() - 0.5D) * spreadXz,
                centerY + (world.rand.nextDouble() - 0.5D) * spreadY,
                centerZ + (world.rand.nextDouble() - 0.5D) * spreadXz,
                (world.rand.nextDouble() - 0.5D) * PARTICLE_MOTION,
                (world.rand.nextDouble() - 0.5D) * PARTICLE_MOTION,
                (world.rand.nextDouble() - 0.5D) * PARTICLE_MOTION);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
