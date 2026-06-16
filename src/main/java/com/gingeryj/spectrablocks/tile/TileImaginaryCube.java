package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileImaginaryCube extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 1.85D;
    private static final int PARTICLE_INTERVAL = 6;
    private static final double CUBE_RADIUS = 0.82D;
    private static final double PARTICLE_MOTION = 0.018D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double sideX = world.rand.nextBoolean() ? -1.0D : 1.0D;
        double sideY = world.rand.nextBoolean() ? -1.0D : 1.0D;
        double sideZ = world.rand.nextBoolean() ? -1.0D : 1.0D;
        double edgeA = (world.rand.nextDouble() - 0.5D) * CUBE_RADIUS * scale;
        double edgeB = (world.rand.nextDouble() - 0.5D) * CUBE_RADIUS * scale;
        int axis = world.rand.nextInt(3);
        double px = axis == 0 ? sideX * CUBE_RADIUS * scale : edgeA;
        double py = axis == 1 ? sideY * CUBE_RADIUS * scale : (axis == 0 ? edgeA : edgeB);
        double pz = axis == 2 ? sideZ * CUBE_RADIUS * scale : edgeB;
        world.spawnParticle(EnumParticleTypes.END_ROD,
                pos.getX() + 0.5D + px,
                pos.getY() + 0.5D + py,
                pos.getZ() + 0.5D + pz,
                -px * PARTICLE_MOTION,
                -py * PARTICLE_MOTION,
                -pz * PARTICLE_MOTION);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
