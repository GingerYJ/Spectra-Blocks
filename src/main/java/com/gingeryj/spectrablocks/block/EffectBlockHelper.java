package com.gingeryj.spectrablocks.block;

import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class EffectBlockHelper {

    private EffectBlockHelper() {
    }

    static void applyRenderScaleFromStack(World world, BlockPos pos, ItemStack stack) {
        if (world == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(TileScalableEffect.TAG_RENDER_SCALE)) {
            return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileScalableEffect) {
            ((TileScalableEffect) tile).setCustomRenderScale(tag.getDouble(TileScalableEffect.TAG_RENDER_SCALE));
        }
    }
}
