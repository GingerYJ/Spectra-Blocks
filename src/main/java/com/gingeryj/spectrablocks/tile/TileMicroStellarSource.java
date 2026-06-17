package com.gingeryj.spectrablocks.tile;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileMicroStellarSource extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 5.75D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(5) != 0) {
            return;
        }

        double yaw = world.rand.nextDouble() * Math.PI * 2.0D;
        double y = -0.85D + world.rand.nextDouble() * 1.7D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double radius = 5.1D + world.rand.nextDouble() * 0.25D;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        double particleX = Math.cos(yaw) * horizontal * radius;
        double particleY = y * radius;
        double particleZ = Math.sin(yaw) * horizontal * radius;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                centerX + particleX,
                centerY + particleY,
                centerZ + particleZ,
                particleX * 0.015D,
                particleY * 0.015D,
                particleZ * 0.015D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(ModConfig.microStellarSourceScale()));
    }
}
