package com.gingeryj.spectrablocks.proxy;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {
    default void preInit(FMLPreInitializationEvent event) {
    }

    default void init() {
    }
}
