package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileFrostCore extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.80D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double FROST_RING_RADIUS = 2.05D;
    private static final double SNOW_RING_RADIUS = 2.60D;
    private static final double DRIFT_SPEED = 0.018D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = FROST_RING_RADIUS * (0.45D + world.rand.nextDouble() * 0.55D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL,
                centerX + Math.cos(angle) * radius,
                centerY + (world.rand.nextDouble() - 0.5D) * 1.08D,
                centerZ + Math.sin(angle) * radius,
                -Math.sin(angle) * DRIFT_SPEED,
                (world.rand.nextDouble() - 0.5D) * 0.010D,
                Math.cos(angle) * DRIFT_SPEED);

        if (world.rand.nextBoolean()) {
            double snowAngle = angle + world.rand.nextDouble() * 0.62D;
            double snowRadius = SNOW_RING_RADIUS * (0.52D + world.rand.nextDouble() * 0.48D);
            world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    centerX + Math.cos(snowAngle) * snowRadius,
                    centerY + (world.rand.nextDouble() - 0.5D) * 1.48D,
                    centerZ + Math.sin(snowAngle) * snowRadius,
                    0.58D + world.rand.nextDouble() * 0.12D,
                    0.78D + world.rand.nextDouble() * 0.16D,
                    1.0D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
