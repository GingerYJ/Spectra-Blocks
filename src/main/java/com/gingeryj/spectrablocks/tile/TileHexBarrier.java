package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileHexBarrier extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.25D;
    private static final int PARTICLE_INTERVAL = 5;
    private static final double FIELD_HALF_WIDTH = 1.92D;
    private static final double FIELD_HALF_HEIGHT = 1.48D;
    private static final double FIELD_CENTER_Y = 0.34D;
    private static final double FIELD_DEPTH = 0.07D;
    private static final double PARTICLE_COLOR_RED = 0.26D;
    private static final double PARTICLE_COLOR_GREEN = 0.94D;
    private static final double PARTICLE_COLOR_BLUE = 1.00D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double px = (world.rand.nextDouble() * 2.0D - 1.0D) * FIELD_HALF_WIDTH * scale;
        double py = (FIELD_CENTER_Y + (world.rand.nextDouble() * 2.0D - 1.0D) * FIELD_HALF_HEIGHT) * scale;
        double pz = (world.rand.nextDouble() * 2.0D - 1.0D) * FIELD_DEPTH * scale;

        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
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
