package com.gingeryj.spectrablocks.client.render;

import com.gingeryj.spectrablocks.config.ModConfig;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;

public class RenderMicroWhiteHole extends RenderSingularityBase<TileMicroWhiteHole> {

    @Override
    protected int coreColor() {
        return 0xFFFFF4;
    }

    @Override
    protected float coreAlpha() {
        return 0.94F;
    }

    @Override
    protected int innerHaloColor() {
        return 0xFFF1B8;
    }

    @Override
    protected int innerGridColor() {
        return 0xFFFFFF;
    }

    @Override
    protected int outerHaloColor() {
        return 0xFFE8A0;
    }

    @Override
    protected int outerGridColor() {
        return 0xFFC65A;
    }

    @Override
    protected float baseAnimationSpeed() {
        return 1.35F;
    }

    @Override
    protected float innerAnimationSpeed() {
        return 0.62F;
    }

    @Override
    protected float outerAnimationSpeed() {
        return 0.28F;
    }

    @Override
    protected float innerExpandFrequency() {
        return 0.68F;
    }

    @Override
    protected float innerBrightnessFrequency() {
        return 0.52F;
    }

    @Override
    protected float outerExpandFrequency() {
        return 0.34F;
    }

    @Override
    protected float outerBrightnessFrequency() {
        return 0.28F;
    }

    @Override
    protected float innerRotationSpeed() {
        return 0.5F;
    }

    @Override
    protected float outerRotationSpeed() {
        return 0.22F;
    }

    @Override
    protected float innerExpandBase() {
        return 0.86F;
    }

    @Override
    protected float innerExpandRange() {
        return 0.26F;
    }

    @Override
    protected float outerExpandBase() {
        return 0.93F;
    }

    @Override
    protected float outerExpandRange() {
        return 0.18F;
    }

    @Override
    protected float innerAlphaBase() {
        return 0.2F;
    }

    @Override
    protected float innerAlphaRange() {
        return 0.22F;
    }

    @Override
    protected float outerAlphaBase() {
        return 0.12F;
    }

    @Override
    protected float outerAlphaRange() {
        return 0.13F;
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
    protected double defaultRenderScale() {
        return ModConfig.microWhiteHoleScale();
    }

    @Override
    protected float shaderMode() {
        return 1.0F;
    }
}
