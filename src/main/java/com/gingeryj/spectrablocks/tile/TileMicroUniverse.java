package com.gingeryj.spectrablocks.tile;

import com.gingeryj.spectrablocks.config.ModConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileMicroUniverse extends TileScalableEffect {

    public static final String TAG_PLANET_COUNT = "PlanetCount";
    public static final int MIN_PLANET_COUNT = 0;
    public static final int MAX_PLANET_COUNT = 8;

    private static final double RENDER_RADIUS = 5.75D;

    private int planetCount = MIN_PLANET_COUNT;

    public int getPlanetCount() {
        return planetCount;
    }

    public void setPlanetCount(int planetCount) {
        this.planetCount = clampPlanetCount(planetCount);
        markDirty();
        syncToClient();
    }

    public static int clampPlanetCount(int planetCount) {
        if (planetCount < MIN_PLANET_COUNT) {
            return MIN_PLANET_COUNT;
        }
        if (planetCount > MAX_PLANET_COUNT) {
            return MAX_PLANET_COUNT;
        }
        return planetCount;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_PLANET_COUNT)) {
            planetCount = clampPlanetCount(compound.getInteger(TAG_PLANET_COUNT));
        } else {
            planetCount = MIN_PLANET_COUNT;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger(TAG_PLANET_COUNT, planetCount);
        return compound;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderBounds.centered(pos, RENDER_RADIUS, renderScale(ModConfig.microUniverseScale()));
    }
}
