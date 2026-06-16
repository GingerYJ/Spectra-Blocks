package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileCometTail extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 5.10D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double PARTICLE_ORBIT_RADIUS = 1.34D;
    private static final double PARTICLE_TAIL_LENGTH = 2.35D;
    private static final double PARTICLE_SPEED = 0.028D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double time = world.getTotalWorldTime() * PARTICLE_SPEED;
        double angle = time + world.rand.nextDouble() * 0.26D;
        double trail = world.rand.nextDouble() * PARTICLE_TAIL_LENGTH;
        double radius = PARTICLE_ORBIT_RADIUS - trail * 0.11D;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double tailAngle = angle - trail;
        double x = Math.cos(tailAngle) * radius;
        double y = Math.sin(tailAngle * 0.72D) * 0.32D + (world.rand.nextDouble() - 0.5D) * 0.14D;
        double z = Math.sin(tailAngle) * radius * 0.72D;

        world.spawnParticle(EnumParticleTypes.END_ROD,
                centerX + x,
                centerY + y,
                centerZ + z,
                -Math.sin(angle) * 0.020D,
                0.010D + world.rand.nextDouble() * 0.012D,
                Math.cos(angle) * 0.015D);
        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                centerX + x * 0.92D,
                centerY + y * 0.92D,
                centerZ + z * 0.92D,
                -Math.cos(angle) * 0.014D,
                0.004D,
                -Math.sin(angle) * 0.014D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
