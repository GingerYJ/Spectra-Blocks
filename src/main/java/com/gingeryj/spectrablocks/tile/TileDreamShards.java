package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileDreamShards extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.95D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double PARTICLE_ORBIT_RADIUS = 2.45D;
    private static final double PARTICLE_ORBIT_SPEED = 0.031D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double height = (world.rand.nextDouble() - 0.5D) * 1.18D;
        double radius = PARTICLE_ORBIT_RADIUS + world.rand.nextDouble() * 0.34D;
        double particleX = Math.cos(angle) * radius;
        double particleZ = Math.sin(angle) * radius;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        if (world.rand.nextInt(3) == 0) {
            world.spawnParticle(EnumParticleTypes.END_ROD,
                    centerX + particleX,
                    centerY + height,
                    centerZ + particleZ,
                    -Math.sin(angle) * PARTICLE_ORBIT_SPEED,
                    0.006D + world.rand.nextDouble() * 0.016D,
                    Math.cos(angle) * PARTICLE_ORBIT_SPEED);
        } else {
            world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    centerX + particleX,
                    centerY + height,
                    centerZ + particleZ,
                    0.34D + world.rand.nextDouble() * 0.45D,
                    0.26D + world.rand.nextDouble() * 0.55D,
                    0.74D + world.rand.nextDouble() * 0.25D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
