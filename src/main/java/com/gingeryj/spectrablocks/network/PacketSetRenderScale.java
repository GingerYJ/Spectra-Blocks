package com.gingeryj.spectrablocks.network;

import com.gingeryj.spectrablocks.tile.TileScalableEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetRenderScale implements IMessage {

    private BlockPos pos;
    private double renderScale;

    @SuppressWarnings("unused")
    public PacketSetRenderScale() {
    }

    public PacketSetRenderScale(BlockPos pos, double renderScale) {
        this.pos = pos;
        this.renderScale = renderScale;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        renderScale = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeDouble(renderScale);
    }

    public static class Handler implements IMessageHandler<PacketSetRenderScale, IMessage> {

        @Override
        public IMessage onMessage(PacketSetRenderScale message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> apply(message, player));
            return null;
        }

        private static void apply(PacketSetRenderScale message, EntityPlayerMP player) {
            if (message.pos == null || player.getDistanceSq(message.pos) > 64.0D) {
                return;
            }

            TileEntity tile = player.world.getTileEntity(message.pos);
            if (tile instanceof TileScalableEffect) {
                ((TileScalableEffect) tile).setCustomRenderScale(message.renderScale);
            }
        }
    }
}
