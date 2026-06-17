package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileSpatialRift extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 2.20D;
    private static final int PARTICLE_INTERVAL = 3;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double spread = 0.78D * scale;
        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + (world.rand.nextDouble() - 0.5D) * spread,
                centerY + (world.rand.nextDouble() - 0.5D) * spread * 1.85D,
                centerZ + (world.rand.nextDouble() - 0.5D) * spread,
                (world.rand.nextDouble() - 0.5D) * 0.10D,
                (world.rand.nextDouble() - 0.5D) * 0.05D,
                (world.rand.nextDouble() - 0.5D) * 0.10D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
