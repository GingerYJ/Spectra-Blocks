package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroSingularity;

public class RenderMicroSingularity extends RenderSingularityBase<TileMicroSingularity> {

    @Override
    protected int coreColor() {
        return 0x000000;
    }

    @Override
    protected float coreAlpha() {
        return 0.99F;
    }

    @Override
    protected int innerHaloColor() {
        return 0x140029;
    }

    @Override
    protected int innerGridColor() {
        return 0x7700DD;
    }

    @Override
    protected int outerHaloColor() {
        return 0x05000D;
    }

    @Override
    protected int outerGridColor() {
        return 0x440088;
    }

    @Override
    protected double renderScale() {
        return ModConfig.microSingularityScale();
    }
}
