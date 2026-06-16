package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;

public class RenderMicroWhiteHole extends RenderSingularityBase<TileMicroWhiteHole> {

    @Override
    protected int coreColor() {
        return 0xFFF9E6;
    }

    @Override
    protected float coreAlpha() {
        return 0.88F;
    }

    @Override
    protected int innerHaloColor() {
        return 0xFFE8A8;
    }

    @Override
    protected int innerGridColor() {
        return 0xFFFFFF;
    }

    @Override
    protected int outerHaloColor() {
        return 0xFFF4C7;
    }

    @Override
    protected int outerGridColor() {
        return 0xFFD66B;
    }

    @Override
    protected float innerAlphaBase() {
        return 0.18F;
    }

    @Override
    protected float innerAlphaRange() {
        return 0.28F;
    }

    @Override
    protected float outerAlphaBase() {
        return 0.08F;
    }

    @Override
    protected float outerAlphaRange() {
        return 0.16F;
    }

    @Override
    protected double defaultRenderScale() {
        return ModConfig.microWhiteHoleScale();
    }
}
