package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileDimensionalGate extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 2.75D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double PORTAL_WIDTH = 0.82D;
    private static final double PORTAL_HEIGHT = 1.72D;
    private static final double PARTICLE_MOTION = 0.055D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = Math.sqrt(world.rand.nextDouble());
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double px = Math.cos(angle) * PORTAL_WIDTH * radius * scale;
        double py = Math.sin(angle) * PORTAL_HEIGHT * radius * scale;
        double pz = (world.rand.nextDouble() - 0.5D) * 0.08D * scale;
        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + px,
                centerY + py,
                centerZ + pz,
                -px * PARTICLE_MOTION,
                -py * PARTICLE_MOTION * 0.45D,
                (world.rand.nextDouble() - 0.5D) * PARTICLE_MOTION);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
