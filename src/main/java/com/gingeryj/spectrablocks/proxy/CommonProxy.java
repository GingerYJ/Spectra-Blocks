package com.gingeryj.spectrablocks.proxy;

import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.registry.ModContent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy implements IProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.load(event.getSuggestedConfigurationFile());
        ModContent.registerTileEntities();
    }
}
