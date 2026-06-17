package com.gingeryj.spectrablocks.client.render.shader;

import com.gingeryj.spectrablocks.ExampleMod;
import com.gingeryj.spectrablocks.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class ShaderProgram {

    private final String name;
    private final Map<String, Integer> uniformLocations = new HashMap<String, Integer>();
    private int programId;
    private int previousProgramId;
    private boolean loaded;
    private boolean valid;
    private boolean active;
    private String failureReason = "not loaded";

    ShaderProgram(String name) {
        this.name = name;
    }

    public boolean load() {
        if (loaded) {
            return valid;
        }

        loaded = true;
        int vertexShader = 0;
        int fragmentShader = 0;
        int linkedProgram = 0;

        try {
            String vertexSource = loadShaderSource(name + ".vsh");
            String fragmentSource = loadShaderSource(name + ".fsh");

            vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource, name + ".vsh");
            fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource, name + ".fsh");

            linkedProgram = GL20.glCreateProgram();
            GL20.glAttachShader(linkedProgram, vertexShader);
            GL20.glAttachShader(linkedProgram, fragmentShader);
            GL20.glLinkProgram(linkedProgram);

            if (GL20.glGetProgrami(linkedProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                throw new ShaderException("link failed for " + name + ": " + programInfoLog(linkedProgram));
            }

            programId = linkedProgram;
            valid = true;
            failureReason = "";
            return true;
        } catch (IOException e) {
            failureReason = "resource load failed for " + name + ": " + e.getMessage();
            ExampleMod.LOGGER.warn("Disabling shader program '{}': {}", name, failureReason);
        } catch (ShaderException e) {
            failureReason = e.getMessage();
            ExampleMod.LOGGER.warn("Disabling shader program '{}': {}", name, failureReason);
        } finally {
            if (!valid && linkedProgram != 0) {
                GL20.glDeleteProgram(linkedProgram);
            }
            if (vertexShader != 0) {
                GL20.glDeleteShader(vertexShader);
            }
            if (fragmentShader != 0) {
                GL20.glDeleteShader(fragmentShader);
            }
        }

        return false;
    }

    public boolean isUsable() {
        return valid && programId != 0;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public boolean begin() {
        if (!isUsable()) {
            return false;
        }
        previousProgramId = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(programId);
        active = true;
        return true;
    }

    public void end() {
        if (active) {
            GL20.glUseProgram(previousProgramId);
            previousProgramId = 0;
            active = false;
        }
    }

    public void setUniform1f(String uniformName, float value) {
        int location = uniformLocation(uniformName);
        if (location >= 0) {
            GL20.glUniform1f(location, value);
        }
    }

    public void setUniform2f(String uniformName, float x, float y) {
        int location = uniformLocation(uniformName);
        if (location >= 0) {
            GL20.glUniform2f(location, x, y);
        }
    }

    public void setUniform3f(String uniformName, float x, float y, float z) {
        int location = uniformLocation(uniformName);
        if (location >= 0) {
            GL20.glUniform3f(location, x, y, z);
        }
    }

    public void setUniform4f(String uniformName, float x, float y, float z, float w) {
        int location = uniformLocation(uniformName);
        if (location >= 0) {
            GL20.glUniform4f(location, x, y, z, w);
        }
    }

    public void setUniformMatrix4(String uniformName, boolean transpose, FloatBuffer matrix) {
        int location = uniformLocation(uniformName);
        if (location >= 0) {
            GL20.glUniformMatrix4(location, transpose, matrix);
        }
    }

    public void setUniformMatrix4(String uniformName, boolean transpose, float[] matrix) {
        if (matrix == null || matrix.length < 16) {
            return;
        }
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        buffer.put(matrix, 0, 16);
        buffer.flip();
        setUniformMatrix4(uniformName, transpose, buffer);
    }

    public void delete() {
        if (active) {
            end();
        }
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
            programId = 0;
        }
        valid = false;
        uniformLocations.clear();
    }

    private int uniformLocation(String uniformName) {
        Integer cached = uniformLocations.get(uniformName);
        if (cached != null) {
            return cached.intValue();
        }

        int location = GL20.glGetUniformLocation(programId, uniformName);
        uniformLocations.put(uniformName, Integer.valueOf(location));
        return location;
    }

    private static int compileShader(int type, String source, String shaderName) throws ShaderException {
        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = shaderInfoLog(shaderId);
            GL20.glDeleteShader(shaderId);
            throw new ShaderException("compile failed for " + shaderName + ": " + log);
        }

        return shaderId;
    }

    private static String loadShaderSource(String fileName) throws IOException {
        ResourceLocation location = new ResourceLocation(Reference.MOD_ID, "shaders/" + fileName);
        IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } finally {
            reader.close();
            resource.close();
        }
        return builder.toString();
    }

    private static String shaderInfoLog(int shaderId) {
        return trimLog(GL20.glGetShaderInfoLog(shaderId, 4096));
    }

    private static String programInfoLog(int programId) {
        return trimLog(GL20.glGetProgramInfoLog(programId, 4096));
    }

    private static String trimLog(String log) {
        if (log == null || log.trim().isEmpty()) {
            return "no driver log";
        }
        return log.trim();
    }

    private static final class ShaderException extends Exception {
        private ShaderException(String message) {
            super(message);
        }
    }
}
