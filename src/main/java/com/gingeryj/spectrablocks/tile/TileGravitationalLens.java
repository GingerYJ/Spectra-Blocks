package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileGravitationalLens extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 2.35D;
    private static final int PARTICLE_INTERVAL = 6;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double scale = renderScale(1.0D);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                centerX + (world.rand.nextDouble() - 0.5D) * 1.65D * scale,
                centerY + (world.rand.nextDouble() - 0.5D) * 1.10D * scale,
                centerZ + (world.rand.nextDouble() - 0.5D) * 1.65D * scale,
                (world.rand.nextDouble() - 0.5D) * 0.18D,
                (world.rand.nextDouble() - 0.5D) * 0.10D,
                (world.rand.nextDouble() - 0.5D) * 0.18D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
