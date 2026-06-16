package com.gingeryj.spectrablocks.tile;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileBioluminescentSpores extends TileScalableEffect implements ITickable {

    private static final double RENDER_RADIUS = 3.75D;
    private static final int PARTICLE_INTERVAL = 4;
    private static final double SPORE_RADIUS = 1.08D;
    private static final double SPORE_LIFT = 0.026D;
    private static final double SPORE_DRIFT = 0.012D;
    private static final double SPORE_RED = 0.34D;
    private static final double SPORE_GREEN = 0.95D;
    private static final double SPORE_BLUE = 0.90D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }

        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double radius = SPORE_RADIUS * world.rand.nextDouble();
        double particleX = Math.cos(angle) * radius;
        double particleZ = Math.sin(angle) * radius;
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.12D;
        double centerZ = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                centerX + particleX,
                centerY + world.rand.nextDouble() * 0.80D,
                centerZ + particleZ,
                SPORE_RED,
                SPORE_GREEN,
                SPORE_BLUE);

        if (world.rand.nextInt(3) == 0) {
            world.spawnParticle(EnumParticleTypes.END_ROD,
                    centerX + particleX,
                    centerY + world.rand.nextDouble() * 0.80D,
                    centerZ + particleZ,
                    (world.rand.nextDouble() - 0.5D) * SPORE_DRIFT,
                    SPORE_LIFT + world.rand.nextDouble() * 0.012D,
                    (world.rand.nextDouble() - 0.5D) * SPORE_DRIFT);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(1.0D));
    }
}
