package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileMeteorShower extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.20D;
    private static final int PARTICLE_INTERVAL = 2;
    private static final double PARTICLE_FIELD_RADIUS = 1.92D;
    private static final double PARTICLE_DROP = 0.060D;
    private static final double PARTICLE_SIDE_SPEED = 0.035D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = world.rand.nextDouble() * PARTICLE_FIELD_RADIUS;
        double height = (world.rand.nextDouble() - 0.5D) * PARTICLE_FIELD_RADIUS;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                centerX + Math.cos(yaw) * radius,
                centerY + height,
                centerZ + Math.sin(yaw) * radius,
                Math.cos(yaw + 0.76D) * PARTICLE_SIDE_SPEED,
                -PARTICLE_DROP,
                Math.sin(yaw + 0.76D) * PARTICLE_SIDE_SPEED);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
