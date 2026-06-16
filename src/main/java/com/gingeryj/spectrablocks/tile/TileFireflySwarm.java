package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileFireflySwarm extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.25D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double PARTICLE_RADIUS = 1.18D;
    private static final double PARTICLE_HEIGHT = 1.42D;
    private static final double PARTICLE_DRIFT = 0.010D;
    private static final double PARTICLE_RED = 1.0D;
    private static final double PARTICLE_GREEN = 0.82D;
    private static final double PARTICLE_BLUE = 0.16D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = PARTICLE_RADIUS * (0.25D + world.rand.nextDouble() * 0.75D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.22D;
        double centerZ = pos.getZ() + 0.5D;
        double particleX = Math.cos(angle) * radius;
        double particleY = world.rand.nextDouble() * PARTICLE_HEIGHT;
        double particleZ = Math.sin(angle) * radius;

        world.spawnParticle(EnumParticleTypes.REDSTONE,
                centerX + particleX,
                centerY + particleY,
                centerZ + particleZ,
                PARTICLE_RED,
                PARTICLE_GREEN,
                PARTICLE_BLUE);

        if (world.rand.nextInt(5) == 0) {
            world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                    centerX + particleX,
                    centerY + particleY,
                    centerZ + particleZ,
                    Math.cos(angle + Math.PI * 0.5D) * PARTICLE_DRIFT,
                    (world.rand.nextDouble() - 0.5D) * PARTICLE_DRIFT,
                    Math.sin(angle + Math.PI * 0.5D) * PARTICLE_DRIFT);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
