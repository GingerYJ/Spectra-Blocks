package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileQuantumBubble extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 2.00D;
    private static final int PARTICLE_INTERVAL = 5;
    private static final double BUBBLE_RADIUS = 0.92D;
    private static final double PARTICLE_MOTION = 0.025D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double pitch = (world.rand.nextDouble() - 0.5D) * Math.PI;
        double horizontal = Math.cos(pitch);
        double px = Math.cos(yaw) * horizontal * BUBBLE_RADIUS * scale;
        double py = Math.sin(pitch) * BUBBLE_RADIUS * scale;
        double pz = Math.sin(yaw) * horizontal * BUBBLE_RADIUS * scale;
        world.spawnParticle(EnumParticleTypes.REDSTONE,
                pos.getX() + 0.5D + px,
                pos.getY() + 0.5D + py,
                pos.getZ() + 0.5D + pz,
                px * PARTICLE_MOTION,
                py * PARTICLE_MOTION,
                pz * PARTICLE_MOTION);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
