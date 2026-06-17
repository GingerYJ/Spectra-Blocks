package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileSoulVortex extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.85D;
    private static final int PARTICLE_INTERVAL = 2;
    private static final double PARTICLE_RADIUS = 1.55D;
    private static final double PARTICLE_SWIRL = 0.052D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double height = world.rand.nextDouble() * 1.85D;
        double radius = PARTICLE_RADIUS * (1.0D - height * 0.32D / 1.85D);
        double particleX = Math.cos(angle) * radius;
        double particleZ = Math.sin(angle) * radius;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.12D;
        double centerZ = pos.getZ() + 0.5D;

            }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
