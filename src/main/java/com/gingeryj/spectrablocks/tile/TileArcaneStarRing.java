package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileArcaneStarRing extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.25D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double PARTICLE_ORBIT_RADIUS = 2.55D;
    private static final double PARTICLE_ORBIT_SPEED = 0.026D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double height = (world.rand.nextDouble() - 0.5D) * 0.75D;
        double particleX = Math.cos(angle) * PARTICLE_ORBIT_RADIUS;
        double particleZ = Math.sin(angle) * PARTICLE_ORBIT_RADIUS;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.END_ROD,
                centerX + particleX,
                centerY + height,
                centerZ + particleZ,
                -Math.sin(angle) * PARTICLE_ORBIT_SPEED,
                0.012D + world.rand.nextDouble() * 0.018D,
                Math.cos(angle) * PARTICLE_ORBIT_SPEED);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
