package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileCrystalRefractionField extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 4.35D;
    private static final int PARTICLE_INTERVAL = 7;
    private static final double FIELD_RADIUS = 2.95D;
    private static final double FIELD_HEIGHT = 2.40D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = 0.45D + world.rand.nextDouble() * FIELD_RADIUS;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                centerX + Math.cos(angle) * radius,
                centerY + (world.rand.nextDouble() - 0.5D) * FIELD_HEIGHT,
                centerZ + Math.sin(angle) * radius,
                0.35D + world.rand.nextDouble() * 0.25D,
                0.65D + world.rand.nextDouble() * 0.25D,
                0.85D + world.rand.nextDouble() * 0.15D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
