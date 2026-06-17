package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileStormCore extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.60D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double CLOUD_RADIUS = 2.50D;
    private static final double SPARK_RADIUS = 2.85D;
    private static final double SPIN_SPEED = 0.040D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = CLOUD_RADIUS * (0.62D + world.rand.nextDouble() * 0.38D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.CLOUD,
                centerX + Math.cos(angle) * radius,
                centerY + (world.rand.nextDouble() - 0.5D) * 1.10D,
                centerZ + Math.sin(angle) * radius,
                -Math.sin(angle) * SPIN_SPEED,
                (world.rand.nextDouble() - 0.5D) * 0.018D,
                Math.cos(angle) * SPIN_SPEED);

        if (world.rand.nextInt(3) == 0) {
            double sparkAngle = angle + world.rand.nextDouble() * 0.8D;
            world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                    centerX + Math.cos(sparkAngle) * SPARK_RADIUS,
                    centerY + (world.rand.nextDouble() - 0.5D) * 1.75D,
                    centerZ + Math.sin(sparkAngle) * SPARK_RADIUS,
                    -Math.sin(sparkAngle) * 0.060D,
                    (world.rand.nextDouble() - 0.5D) * 0.040D,
                    Math.cos(sparkAngle) * 0.060D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
