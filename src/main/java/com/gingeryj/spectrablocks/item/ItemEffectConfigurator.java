package com.gingeryj.spectrablocks.item;

import com.gingeryj.spectrablocks.ExampleMod;
import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.creative.ModCreativeTabs;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEffectConfigurator extends Item {

    public ItemEffectConfigurator() {
        setRegistryName(Reference.MOD_ID, "effect_configurator");
        setTranslationKey(Reference.MOD_ID + ".effect_configurator");
        setMaxStackSize(1);
        setCreativeTab(ModCreativeTabs.SPECTRA_BLOCKS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileScalableEffect)) {
            if (!world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation("message.spectrablocks.no_configurable_effect"),
                        true);
            }
            return EnumActionResult.FAIL;
        }

        if (world.isRemote) {
            int planetCount = -1;
            if (tile instanceof TileMicroUniverse) {
                planetCount = ((TileMicroUniverse) tile).getPlanetCount();
            }
            ExampleMod.proxy.openEffectConfigurator(pos,
                    ((TileScalableEffect) tile).renderScale(1.0D), planetCount);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn,
                               List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(net.minecraft.client.resources.I18n.format("tooltip.spectrablocks.effect_configurator"));
    }
}