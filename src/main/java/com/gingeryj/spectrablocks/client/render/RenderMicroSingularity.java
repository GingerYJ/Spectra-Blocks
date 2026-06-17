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
    protected float innerAnimationSpeed() {
        return 0.62F;
    }

    @Override
    protected float outerAnimationSpeed() {
        return 0.24F;
    }

    @Override
    protected float outerExpandRange() {
        return 0.16F;
    }

    @Override
    protected float innerAlphaBase() {
        return 0.12F;
    }

    @Override
    protected float innerAlphaRange() {
        return 0.28F;
    }

    @Override
    protected float outerAlphaBase() {
        return 0.07F;
    }

    @Override
    protected float outerAlphaRange() {
        return 0.11F;
    }

    @Override
    protected float innerGridAlpha() {
        return 0.34F;
    }

    @Override
    protected float outerGridAlpha() {
        return 0.16F;
    }

    @Override
    protected float innerRotationSpeed() {
        return 0.38F;
    }

    @Override
    protected float outerRotationSpeed() {
        return 0.28F;
    }

    @Override
    protected double defaultRenderScale() {
        return ModConfig.microSingularityScale();
    }
}
