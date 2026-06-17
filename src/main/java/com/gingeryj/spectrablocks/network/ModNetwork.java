package com.gingeryj.spectrablocks.network;

import com.gingeryj.spectrablocks.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ModNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    private ModNetwork() {
    }

    public static void registerMessages() {
        CHANNEL.registerMessage(PacketSetRenderScale.Handler.class, PacketSetRenderScale.class, 0, Side.SERVER);
    }
}
