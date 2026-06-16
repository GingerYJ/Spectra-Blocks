package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileNullField extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.00D;
    private static final int PARTICLE_INTERVAL = 8;
    private static final double SHELL_RADIUS = 2.05D;
    private static final double SMOKE_MOTION = 0.010D;
    private static final double STATIC_MOTION = 0.018D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double pitch = Math.asin(world.rand.nextDouble() * 2.0D - 1.0D);
        double horizontal = Math.cos(pitch);
        double px = Math.cos(yaw) * horizontal * SHELL_RADIUS * scale;
        double py = Math.sin(pitch) * SHELL_RADIUS * scale;
        double pz = Math.sin(yaw) * horizontal * SHELL_RADIUS * scale;

        if (world.rand.nextBoolean()) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX() + 0.5D + px,
                    pos.getY() + 0.5D + py,
                    pos.getZ() + 0.5D + pz,
                    -px * SMOKE_MOTION,
                    -py * SMOKE_MOTION,
                    -pz * SMOKE_MOTION);
        } else {
            world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                    pos.getX() + 0.5D + px * 0.82D,
                    pos.getY() + 0.5D + py * 0.82D,
                    pos.getZ() + 0.5D + pz * 0.82D,
                    -px * STATIC_MOTION,
                    -py * STATIC_MOTION,
                    -pz * STATIC_MOTION);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
