package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileStardustFountain extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.25D;
    private static final int PARTICLE_INTERVAL = 2;
    private static final double PARTICLE_SPREAD = 0.46D;
    private static final double PARTICLE_LIFT = 0.105D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double spread = world.rand.nextDouble() * PARTICLE_SPREAD;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.16D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                centerX + Math.cos(angle) * spread,
                centerY,
                centerZ + Math.sin(angle) * spread,
                Math.cos(angle) * 0.018D,
                PARTICLE_LIFT + world.rand.nextDouble() * 0.045D,
                Math.sin(angle) * 0.018D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
