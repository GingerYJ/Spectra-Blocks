package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntropyCloud extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.35D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double PARTICLE_RADIUS = 2.35D;
    private static final double PARTICLE_DRIFT = 0.018D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double y = -0.82D + world.rand.nextDouble() * 1.64D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double radius = PARTICLE_RADIUS + world.rand.nextDouble() * 0.55D;
        double particleX = Math.cos(yaw) * horizontal * radius;
        double particleY = y * radius * 0.72D;
        double particleZ = Math.sin(yaw) * horizontal * radius;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(world.rand.nextInt(5) == 0 ? EnumParticleTypes.PORTAL : EnumParticleTypes.SMOKE_LARGE,
                centerX + particleX,
                centerY + particleY,
                centerZ + particleZ,
                -particleX * PARTICLE_DRIFT,
                (world.rand.nextDouble() - 0.5D) * PARTICLE_DRIFT,
                -particleZ * PARTICLE_DRIFT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
