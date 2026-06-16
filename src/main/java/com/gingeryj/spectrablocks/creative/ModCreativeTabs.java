package com.gingeryj.spectrablocks.creative;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.registry.ModContent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class ModCreativeTabs {

    public static final CreativeTabs SPECTRA_BLOCKS = new CreativeTabs(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModContent.MICRO_SINGULARITY);
        }
    };

    private ModCreativeTabs() {
    }
}
