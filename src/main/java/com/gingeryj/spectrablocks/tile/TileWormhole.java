package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileWormhole extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 2.60D;
    private static final int PARTICLE_INTERVAL = 4;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double distance = (0.60D + world.rand.nextDouble() * 0.95D) * scale;
        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + Math.cos(angle) * distance,
                centerY + (world.rand.nextDouble() - 0.5D) * 0.55D * scale,
                centerZ + Math.sin(angle) * distance,
                -Math.cos(angle) * 0.15D,
                (world.rand.nextDouble() - 0.5D) * 0.03D,
                -Math.sin(angle) * 0.15D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
