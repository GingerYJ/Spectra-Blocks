package com.gingeryj.spectrablocks.registry;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.block.BlockMicroSingularity;
import com.gingeryj.spectrablocks.block.BlockMicroStellarSource;
import com.gingeryj.spectrablocks.block.BlockMicroUniverse;
import com.gingeryj.spectrablocks.block.BlockMicroWhiteHole;
import com.gingeryj.spectrablocks.tile.TileMicroSingularity;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
                new ItemBlock(MICRO_SINGULARITY).setRegistryName(MICRO_SINGULARITY.getRegistryName()),
                new ItemBlock(MICRO_WHITE_HOLE).setRegistryName(MICRO_WHITE_HOLE.getRegistryName()),
                new ItemBlock(MICRO_UNIVERSE).setRegistryName(MICRO_UNIVERSE.getRegistryName()),
                new ItemBlock(MICRO_STELLAR_SOURCE).setRegistryName(MICRO_STELLAR_SOURCE.getRegistryName())
        );
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileMicroSingularity.class,
                new ResourceLocation(Reference.MOD_ID, "micro_singularity"));
        GameRegistry.registerTileEntity(TileMicroWhiteHole.class,
                new ResourceLocation(Reference.MOD_ID, "micro_white_hole"));
        GameRegistry.registerTileEntity(TileMicroUniverse.class,
                new ResourceLocation(Reference.MOD_ID, "micro_universe"));
        GameRegistry.registerTileEntity(TileMicroStellarSource.class,
                new ResourceLocation(Reference.MOD_ID, "micro_stellar_source"));
    }
}
