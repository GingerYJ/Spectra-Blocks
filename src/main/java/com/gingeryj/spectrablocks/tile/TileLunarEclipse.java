package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileLunarEclipse extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.60D;
    private static final int PARTICLE_INTERVAL = 5;
    private static final double PARTICLE_RADIUS = 1.22D;
    private static final double PARTICLE_DRIFT = 0.010D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = PARTICLE_RADIUS * (0.76D + world.rand.nextDouble() * 0.42D);
        double lift = (world.rand.nextDouble() - 0.5D) * 0.22D;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                centerX + Math.cos(angle) * radius,
                centerY + lift,
                centerZ + Math.sin(angle) * radius,
                0.82D + world.rand.nextDouble() * 0.12D,
                0.28D + world.rand.nextDouble() * 0.14D,
                0.12D + world.rand.nextDouble() * 0.08D);
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                centerX + Math.cos(angle) * radius * 0.70D,
                centerY + lift * 0.45D,
                centerZ + Math.sin(angle) * radius * 0.70D,
                Math.cos(angle) * PARTICLE_DRIFT,
                0.002D,
                Math.sin(angle) * PARTICLE_DRIFT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
