package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TilePollenBreeze extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.15D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double WIND_LENGTH = 2.18D;
    private static final double WIND_WIDTH = 0.58D;
    private static final double WIND_HEIGHT = 1.18D;
    private static final double WIND_SPEED = 0.028D;
    private static final double POLLEN_RED = 0.86D;
    private static final double POLLEN_GREEN = 0.96D;
    private static final double POLLEN_BLUE = 0.24D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.28D;
        double centerZ = pos.getZ() + 0.5D;
        double y = world.rand.nextDouble() * WIND_HEIGHT;
        double z = (world.rand.nextDouble() - 0.5D) * WIND_WIDTH;
        double x = -WIND_LENGTH * 0.5D + world.rand.nextDouble() * WIND_LENGTH;

        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                centerX + x,
                centerY + y,
                centerZ + z,
                POLLEN_RED,
                POLLEN_GREEN,
                POLLEN_BLUE);

        if (world.rand.nextInt(4) == 0) {
            world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                    centerX + x,
                    centerY + y,
                    centerZ + z,
                    WIND_SPEED,
                    0.004D + world.rand.nextDouble() * 0.008D,
                    (world.rand.nextDouble() - 0.5D) * WIND_SPEED);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
