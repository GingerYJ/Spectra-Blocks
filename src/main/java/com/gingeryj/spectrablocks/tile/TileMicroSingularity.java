package com.gingeryj.spectrablocks.tile;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileMicroSingularity extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.25D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(4) != 0) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.PORTAL,
                centerX + (world.rand.nextDouble() - 0.5D) * 0.8D,
                centerY + (world.rand.nextDouble() - 0.5D) * 0.8D,
                centerZ + (world.rand.nextDouble() - 0.5D) * 0.8D,
                (world.rand.nextDouble() - 0.5D) * 0.2D,
                (world.rand.nextDouble() - 0.5D) * 0.2D,
                (world.rand.nextDouble() - 0.5D) * 0.2D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(ModConfig.microSingularityScale()));
    }
}
