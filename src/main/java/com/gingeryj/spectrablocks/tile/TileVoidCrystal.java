package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileVoidCrystal extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.25D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double PARTICLE_RADIUS = 1.65D;
    private static final double PARTICLE_PULL = 0.045D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double pitch = -0.62D + world.rand.nextDouble() * 1.24D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - pitch * pitch));
        double particleX = Math.cos(yaw) * horizontal * PARTICLE_RADIUS;
        double particleY = pitch * PARTICLE_RADIUS;
        double particleZ = Math.sin(yaw) * horizontal * PARTICLE_RADIUS;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + particleX,
                centerY + particleY,
                centerZ + particleZ,
                -particleX * PARTICLE_PULL,
                -particleY * PARTICLE_PULL,
                -particleZ * PARTICLE_PULL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
