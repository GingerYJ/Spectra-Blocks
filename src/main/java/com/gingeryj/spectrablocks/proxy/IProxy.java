package com.gingeryj.spectrablocks.proxy;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {
    default void preInit(FMLPreInitializationEvent event) {
    }

    default void init() {
    }

    default void openEffectConfigurator(BlockPos pos, double renderScale, int planetCount) {
    }
}