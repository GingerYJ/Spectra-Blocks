package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TilePlasmaStorm extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.75D;
    private static final int PARTICLE_INTERVAL = 2;
    private static final double PARTICLE_RADIUS = 2.15D;
    private static final double PARTICLE_SPEED = 0.095D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double height = (world.rand.nextDouble() - 0.5D) * 1.65D;
        double radius = PARTICLE_RADIUS + world.rand.nextDouble() * 0.65D;
        double particleX = Math.cos(angle) * radius;
        double particleZ = Math.sin(angle) * radius;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                centerX + particleX,
                centerY + height,
                centerZ + particleZ,
                -Math.sin(angle) * PARTICLE_SPEED,
                (world.rand.nextDouble() - 0.5D) * 0.050D,
                Math.cos(angle) * PARTICLE_SPEED);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
