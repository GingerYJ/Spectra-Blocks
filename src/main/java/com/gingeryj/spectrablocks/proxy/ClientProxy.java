package com.gingeryj.spectrablocks.proxy;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.client.gui.GuiEffectConfigurator;
import com.gingeryj.spectrablocks.client.render.RenderMicroSingularity;
import com.gingeryj.spectrablocks.client.render.RenderMicroStellarSource;
import com.gingeryj.spectrablocks.client.render.RenderMicroUniverse;
import com.gingeryj.spectrablocks.client.render.RenderMicroWhiteHole;
import com.gingeryj.spectrablocks.registry.ModContent;
import com.gingeryj.spectrablocks.tile.TileMicroSingularity;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroSingularity.class, new RenderMicroSingularity());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroWhiteHole.class, new RenderMicroWhiteHole());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroUniverse.class, new RenderMicroUniverse());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroStellarSource.class, new RenderMicroStellarSource());
    }

    @Override
    public void openEffectConfigurator(BlockPos pos, double renderScale, int planetCount) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEffectConfigurator(pos, renderScale, planetCount));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerBlockItemModel(ModContent.MICRO_SINGULARITY);
        registerBlockItemModel(ModContent.MICRO_WHITE_HOLE);
        registerBlockItemModel(ModContent.MICRO_UNIVERSE);
        registerBlockItemModel(ModContent.MICRO_STELLAR_SOURCE);
        registerItemModel(ModContent.EFFECT_CONFIGURATOR);
    }

    private static void registerBlockItemModel(Block block) {
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }

    private static void registerItemModel(Item item) {
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}