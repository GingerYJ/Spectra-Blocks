package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileTerraBloom extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.65D;
    private static final int PARTICLE_INTERVAL = 5;
    private static final double POLLEN_RADIUS = 2.20D;
    private static final double LEAF_RING_RADIUS = 1.72D;
    private static final double RISE_SPEED = 0.026D;
    private static final double ORBIT_SPEED = 0.020D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = POLLEN_RADIUS * (0.38D + world.rand.nextDouble() * 0.62D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.30D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                centerX + Math.cos(angle) * radius,
                centerY + world.rand.nextDouble() * 0.95D,
                centerZ + Math.sin(angle) * radius,
                -Math.sin(angle) * ORBIT_SPEED,
                RISE_SPEED + world.rand.nextDouble() * 0.020D,
                Math.cos(angle) * ORBIT_SPEED);

        if (world.rand.nextInt(3) == 0) {
            double leafAngle = angle + world.rand.nextDouble() * 0.50D;
            double leafRadius = LEAF_RING_RADIUS * (0.62D + world.rand.nextDouble() * 0.38D);
            world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    centerX + Math.cos(leafAngle) * leafRadius,
                    centerY + 0.10D + world.rand.nextDouble() * 0.78D,
                    centerZ + Math.sin(leafAngle) * leafRadius,
                    0.42D + world.rand.nextDouble() * 0.16D,
                    0.84D + world.rand.nextDouble() * 0.10D,
                    0.30D + world.rand.nextDouble() * 0.10D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
