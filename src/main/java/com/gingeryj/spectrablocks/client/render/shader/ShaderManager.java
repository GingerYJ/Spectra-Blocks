package com.gingeryj.spectrablocks.client.render.shader;

import com.gingeryj.spectrablocks.ExampleMod;
import net.minecraft.client.renderer.OpenGlHelper;

import java.util.HashMap;
import java.util.Map;

public final class ShaderManager {

    private static final Map<String, ShaderProgram> PROGRAMS = new HashMap<String, ShaderProgram>();
    private static boolean globallyDisabled;
    private static String disabledReason = "";

    private ShaderManager() {
    }

    public static boolean areShaderEffectsUsable() {
        return !globallyDisabled && OpenGlHelper.shadersSupported;
    }

    public static String getDisabledReason() {
        if (!OpenGlHelper.shadersSupported) {
            return "OpenGL shader support is unavailable";
        }
        return disabledReason;
    }

    public static ShaderProgram getProgram(String name) {
        if (!areShaderEffectsUsable()) {
            return null;
        }

        ShaderProgram program = PROGRAMS.get(name);
        if (program == null) {
            program = new ShaderProgram(name);
            PROGRAMS.put(name, program);
        }

        if (!program.load()) {
            disableShaders("program '" + name + "' failed: " + program.getFailureReason());
            return null;
        }

        return program.isUsable() ? program : null;
    }

    public static void disableShaders(String reason) {
        globallyDisabled = true;
        disabledReason = reason == null ? "unknown shader failure" : reason;
        ExampleMod.LOGGER.warn("Shader effects disabled: {}", disabledReason);
        releaseAll();
    }

    public static void releaseAll() {
        for (ShaderProgram program : PROGRAMS.values()) {
            program.delete();
        }
        PROGRAMS.clear();
    }

    public static void resetForResourceReload() {
        globallyDisabled = false;
        disabledReason = "";
        releaseAll();
    }
}
