package com.gingeryj.spectrablocks.block;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.creative.ModCreativeTabs;
import com.gingeryj.spectrablocks.tile.TileCometTail;
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

public class BlockCometTail extends Block {

    private static final AxisAlignedBB BOX = new AxisAlignedBB(
            0.18D, 0.18D, 0.18D,
            0.82D, 0.82D, 0.82D
    );

    public BlockCometTail() {
        super(Material.IRON);
        setRegistryName(Reference.MOD_ID, "comet_tail");
        setTranslationKey(Reference.MOD_ID + ".comet_tail");
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setLightLevel(1.0F);
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
        return new TileCometTail();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        EffectBlockHelper.applyRenderScaleFromStack(world, pos, stack);
    }
}
