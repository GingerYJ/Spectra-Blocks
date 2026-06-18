package com.gingeryj.spectrablocks.client.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;

public final class SpectraRenderState {

    private SpectraRenderState() {
    }

    public static State beginIsolated() {
        State state = new State();
        forceBaseState();
        useAlphaBlend();
        GlStateManager.disableCull();
        return state;
    }

    public static void forceShaderLayerState() {
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(false);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableFog();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
    }

    public static void forceBaseState() {
        forceShaderLayerState();
        RenderHelper.resetLineWidth();
    }

    public static void useAlphaBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    public static void useAdditiveBlend() {
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    public static final class State implements AutoCloseable {
        private final boolean blendEnabled;
        private final boolean cullEnabled;
        private final boolean alphaEnabled;
        private final boolean textureEnabled;
        private final boolean lightingEnabled;
        private final boolean depthEnabled;
        private final boolean fogEnabled;
        private final boolean depthMask;
        private final boolean colorMaskRed;
        private final boolean colorMaskGreen;
        private final boolean colorMaskBlue;
        private final boolean colorMaskAlpha;
        private final int depthFunc;
        private final int cullFace;
        private final int shadeModel;
        private final int activeTexture;
        private final int currentProgram;
        private final float lineWidth;
        private boolean closed;

        private State() {
            blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
            depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            fogEnabled = GL11.glIsEnabled(GL11.GL_FOG);
            depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            ByteBuffer colorMask = BufferUtils.createByteBuffer(4);
            GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, colorMask);
            colorMaskRed = colorMask.get(0) != 0;
            colorMaskGreen = colorMask.get(1) != 0;
            colorMaskBlue = colorMask.get(2) != 0;
            colorMaskAlpha = colorMask.get(3) != 0;
            depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
            cullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
            shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
            activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            lineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;

            GL20.glUseProgram(currentProgram);
            GL13.glActiveTexture(activeTexture);
            restoreDepth();
            restoreBlend();
            restoreCull();
            restoreAlpha();
            restoreTexture();
            restoreLighting();
            restoreFog();
            GlStateManager.colorMask(colorMaskRed, colorMaskGreen, colorMaskBlue, colorMaskAlpha);
            GlStateManager.shadeModel(shadeModel);
            GlStateManager.glLineWidth(lineWidth);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private void restoreDepth() {
            if (depthEnabled) {
                GlStateManager.enableDepth();
            } else {
                GlStateManager.disableDepth();
            }
            GlStateManager.depthFunc(depthFunc);
            GlStateManager.depthMask(depthMask);
        }

        private void restoreBlend() {
            if (blendEnabled) {
                GlStateManager.enableBlend();
            } else {
                GlStateManager.disableBlend();
            }
            useAlphaBlend();
        }

        private void restoreCull() {
            if (cullEnabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            GlStateManager.cullFace(cullFace == GL11.GL_FRONT
                    ? GlStateManager.CullFace.FRONT
                    : GlStateManager.CullFace.BACK);
        }

        private void restoreAlpha() {
            if (alphaEnabled) {
                GlStateManager.enableAlpha();
            } else {
                GlStateManager.disableAlpha();
            }
        }

        private void restoreTexture() {
            if (textureEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
        }

        private void restoreLighting() {
            if (lightingEnabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
        }

        private void restoreFog() {
            if (fogEnabled) {
                GlStateManager.enableFog();
            } else {
                GlStateManager.disableFog();
            }
        }
    }
}
