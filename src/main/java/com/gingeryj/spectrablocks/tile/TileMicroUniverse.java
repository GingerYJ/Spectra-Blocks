package com.gingeryj.spectrablocks.tile;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileMicroUniverse extends TileEntity implements ITickable {

    private static final double RENDER_RADIUS = 5.75D;

    @Override
    public void update() {
        if (world == null || !world.isRemote || world.rand.nextInt(7) != 0) {
            return;
        }

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                centerX + (world.rand.nextDouble() - 0.5D) * 4.4D,
                centerY + (world.rand.nextDouble() - 0.5D) * 4.4D,
                centerZ + (world.rand.nextDouble() - 0.5D) * 4.4D,
                0.15D + world.rand.nextDouble() * 0.25D,
                0.18D + world.rand.nextDouble() * 0.25D,
                0.35D + world.rand.nextDouble() * 0.45D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, ModConfig.microUniverseScale());
    }
}
