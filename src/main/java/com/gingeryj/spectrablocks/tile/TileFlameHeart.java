package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileFlameHeart extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.10D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double EMBER_RING_RADIUS = 1.95D;
    private static final double SPARK_BURST_RADIUS = 2.85D;
    private static final double ORBIT_SPEED = 0.048D;
    private static final double RISE_SPEED = 0.044D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = EMBER_RING_RADIUS * (0.38D + world.rand.nextDouble() * 0.62D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.FLAME,
                centerX + Math.cos(angle) * radius,
                centerY - 0.28D + world.rand.nextDouble() * 0.95D,
                centerZ + Math.sin(angle) * radius,
                -Math.sin(angle) * ORBIT_SPEED,
                RISE_SPEED + world.rand.nextDouble() * 0.030D,
                Math.cos(angle) * ORBIT_SPEED);

        if (world.rand.nextInt(4) == 0) {
            double sparkAngle = angle + (world.rand.nextDouble() - 0.5D) * 0.72D;
            double sparkRadius = SPARK_BURST_RADIUS * (0.56D + world.rand.nextDouble() * 0.44D);
            world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                    centerX + Math.cos(sparkAngle) * sparkRadius,
                    centerY + (world.rand.nextDouble() - 0.5D) * 1.25D,
                    centerZ + Math.sin(sparkAngle) * sparkRadius,
                    Math.cos(sparkAngle) * 0.072D,
                    0.020D + world.rand.nextDouble() * 0.044D,
                    Math.sin(sparkAngle) * 0.072D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
