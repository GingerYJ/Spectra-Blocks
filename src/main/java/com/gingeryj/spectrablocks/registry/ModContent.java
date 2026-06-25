package com.gingeryj.spectrablocks.registry;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.block.BlockMicroSingularity;
import com.gingeryj.spectrablocks.block.BlockMicroStellarSource;
import com.gingeryj.spectrablocks.block.BlockMicroUniverse;
import com.gingeryj.spectrablocks.block.BlockMicroWhiteHole;
import com.gingeryj.spectrablocks.item.ItemEffectConfigurator;
import com.gingeryj.spectrablocks.tile.TileMicroSingularity;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModContent {

    public static final BlockMicroSingularity MICRO_SINGULARITY = new BlockMicroSingularity();
    public static final BlockMicroWhiteHole MICRO_WHITE_HOLE = new BlockMicroWhiteHole();
    public static final BlockMicroUniverse MICRO_UNIVERSE = new BlockMicroUniverse();
    public static final BlockMicroStellarSource MICRO_STELLAR_SOURCE = new BlockMicroStellarSource();
    public static final ItemEffectConfigurator EFFECT_CONFIGURATOR = new ItemEffectConfigurator();

    private ModContent() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                MICRO_SINGULARITY,
                MICRO_WHITE_HOLE,
                MICRO_UNIVERSE,
                MICRO_STELLAR_SOURCE
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                itemBlock(MICRO_SINGULARITY),
                itemBlock(MICRO_WHITE_HOLE),
                itemBlock(MICRO_UNIVERSE),
                itemBlock(MICRO_STELLAR_SOURCE),
                EFFECT_CONFIGURATOR
        );
    }

    public static void registerTileEntities() {
        registerTileEntity(TileMicroSingularity.class, "micro_singularity");
        registerTileEntity(TileMicroWhiteHole.class, "micro_white_hole");
        registerTileEntity(TileMicroUniverse.class, "micro_universe");
        registerTileEntity(TileMicroStellarSource.class, "micro_stellar_source");
    }

    private static ItemBlock itemBlock(Block block) {
        return (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private static void registerTileEntity(Class<? extends TileEntity> tileClass, String name) {
        GameRegistry.registerTileEntity(tileClass, new ResourceLocation(Reference.MOD_ID, name));
    }
}
