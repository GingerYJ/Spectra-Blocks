package com.gingeryj.spectrablocks.block;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.creative.ModCreativeTabs;
import com.gingeryj.spectrablocks.tile.TileFrostCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockFrostCore extends Block {

    private static final AxisAlignedBB BOX = new AxisAlignedBB(
            0.18D, 0.12D, 0.18D,
            0.82D, 0.86D, 0.82D
    );

    public BlockFrostCore() {
        super(Material.ICE);
        setRegistryName(Reference.MOD_ID, "frost_core");
        setTranslationKey(Reference.MOD_ID + ".frost_core");
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setLightLevel(0.86F);
        setCreativeTab(ModCreativeTabs.SPECTRA_BLOCKS);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOX;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFrostCore();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        EffectBlockHelper.applyRenderScaleFromStack(world, pos, stack);
    }
}
