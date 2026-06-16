package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileMiniatureGalaxy extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.35D;
    private static final int PARTICLE_INTERVAL = 7;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = 0.45D + world.rand.nextDouble() * 3.3D;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double particleX = Math.cos(angle) * radius;
        double particleZ = Math.sin(angle) * radius;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                centerX + particleX,
                centerY + (world.rand.nextDouble() - 0.5D) * 0.35D,
                centerZ + particleZ,
                -particleZ * 0.004D,
                (world.rand.nextDouble() - 0.5D) * 0.010D,
                particleX * 0.004D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
