package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileAbyssalCore extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.25D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double PLANKTON_RADIUS = 2.95D;
    private static final double BUBBLE_RADIUS = 1.35D;
    private static final double BUBBLE_SPEED = 0.026D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double yNorm = -0.85D + world.rand.nextDouble() * 1.70D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - yNorm * yNorm));
        double radius = PLANKTON_RADIUS * (0.58D + world.rand.nextDouble() * 0.42D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                centerX + Math.cos(yaw) * horizontal * radius,
                centerY + yNorm * radius * 0.72D,
                centerZ + Math.sin(yaw) * horizontal * radius,
                0.12D + world.rand.nextDouble() * 0.12D,
                0.68D + world.rand.nextDouble() * 0.22D,
                0.72D + world.rand.nextDouble() * 0.20D);

        if (world.rand.nextBoolean()) {
            double bubbleYaw = world.rand.nextDouble() * Math.PI * 2.0D;
            double bubbleRadius = BUBBLE_RADIUS * world.rand.nextDouble();
            world.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                    centerX + Math.cos(bubbleYaw) * bubbleRadius,
                    centerY - 0.58D + world.rand.nextDouble() * 0.80D,
                    centerZ + Math.sin(bubbleYaw) * bubbleRadius,
                    0.0D,
                    BUBBLE_SPEED + world.rand.nextDouble() * BUBBLE_SPEED,
                    0.0D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
