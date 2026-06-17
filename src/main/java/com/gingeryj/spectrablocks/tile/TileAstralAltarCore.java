package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileAstralAltarCore extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.10D;
    private static final int PARTICLE_INTERVAL = 3;
    private static final double PARTICLE_RADIUS = 1.95D;
    private static final double PARTICLE_SWIRL = 0.030D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = PARTICLE_RADIUS * (0.35D + world.rand.nextDouble() * 0.65D);
        double particleX = Math.cos(angle) * radius;
        double particleZ = Math.sin(angle) * radius;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.30D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.END_ROD,
                centerX + particleX,
                centerY + world.rand.nextDouble() * 0.55D,
                centerZ + particleZ,
                -Math.sin(angle) * PARTICLE_SWIRL,
                0.030D + world.rand.nextDouble() * 0.040D,
                Math.cos(angle) * PARTICLE_SWIRL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
