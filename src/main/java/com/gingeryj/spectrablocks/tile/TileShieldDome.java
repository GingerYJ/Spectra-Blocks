package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileShieldDome extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.30D;
    private static final int PARTICLE_INTERVAL = 6;
    private static final double DOME_RADIUS = 2.18D;
    private static final double DOME_BASE_Y = -0.42D;
    private static final double PARTICLE_COLOR_RED = 0.32D;
    private static final double PARTICLE_COLOR_GREEN = 0.82D;
    private static final double PARTICLE_COLOR_BLUE = 1.00D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double theta = world.rand.nextDouble() * Math.PI * 0.5D;
        double phi = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = DOME_RADIUS * scale;
        double horizontal = Math.sin(theta) * radius;
        double px = Math.cos(phi) * horizontal;
        double py = DOME_BASE_Y * scale + Math.cos(theta) * radius;
        double pz = Math.sin(phi) * horizontal;

        world.spawnParticle(EnumParticleTypes.REDSTONE,
                pos.getX() + 0.5D + px,
                pos.getY() + 0.5D + py,
                pos.getZ() + 0.5D + pz,
                PARTICLE_COLOR_RED,
                PARTICLE_COLOR_GREEN,
                PARTICLE_COLOR_BLUE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
