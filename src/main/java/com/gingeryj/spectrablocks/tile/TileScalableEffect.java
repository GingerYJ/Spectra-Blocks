package com.gingeryj.spectrablocks.tile;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public abstract class TileScalableEffect extends TileEntity {

    public static final String TAG_RENDER_SCALE = "RenderScale";
    private static final double BASE_RENDER_DISTANCE_SQUARED = 4096.0D;
    private static final double MAX_RENDER_DISTANCE_SQUARED = 262144.0D;

    private boolean hasCustomRenderScale;
    private double customRenderScale = 1.0D;

    public double renderScale(double defaultScale) {
        return hasCustomRenderScale ? customRenderScale : ModConfig.clampRenderScale(defaultScale);
    }

    public void setCustomRenderScale(double renderScale) {
        this.hasCustomRenderScale = true;
        this.customRenderScale = ModConfig.clampRenderScale(renderScale);
        markDirty();
        syncToClient();
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        double scale = Math.max(1.0D, renderScale(1.0D));
        return Math.min(MAX_RENDER_DISTANCE_SQUARED, BASE_RENDER_DISTANCE_SQUARED * scale * scale);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_RENDER_SCALE)) {
            hasCustomRenderScale = true;
            customRenderScale = ModConfig.clampRenderScale(compound.getDouble(TAG_RENDER_SCALE));
        } else {
            hasCustomRenderScale = false;
            customRenderScale = 1.0D;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (hasCustomRenderScale) {
            compound.setDouble(TAG_RENDER_SCALE, customRenderScale);
        }
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    private void syncToClient() {
        if (world == null || world.isRemote || pos == null) {
            return;
        }

        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }
}
