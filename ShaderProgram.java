package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;

    public ShaderProgram(String vertexPath, String fragmentPath) {

        int vertexShader = compileShader(
                GL_VERTEX_SHADER, vertexPath
        );

        int fragmentShader = compileShader(
                GL_FRAGMENT_SHADER, fragmentPath
        );

        programId = glCreateProgram();

        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {

            String error = glGetProgramInfoLog(programId);

            glDeleteProgram(programId);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);

            throw new RuntimeException(
                    "Shader linking gagal: " + error
            );
        }

        glDetachShader(programId, vertexShader);
        glDetachShader(programId, fragmentShader);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        System.out.println("Shader berhasil dibuat!");
    }

    private int compileShader(int type, String path) {

        String source = readFile(path);

        int shader = glCreateShader(type);

        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {

            String error = glGetShaderInfoLog(shader);

            glDeleteShader(shader);

            throw new RuntimeException(
                    "Shader compilation gagal (" +
                    path + "): " + error
            );
        }

        return shader;
    }

    private String readFile(String path) {

        try (InputStream input =
                getClass().getResourceAsStream(path)) {

            if (input == null) {
                throw new RuntimeException(
                        "File shader tidak ditemukan: " + path
                );
            }

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Gagal membaca shader: " + path, e
            );
        }
    }

    public void use() {
        glUseProgram(programId);
    }

    public void setBoolean(String name, boolean value) {

        glUniform1i(
                glGetUniformLocation(programId, name),
                value ? 1 : 0
        );
    }

    // Mengatur nilai integer pada uniform shader
    public void setInteger(String name, int value) {

        glUniform1i(
                glGetUniformLocation(programId, name),
                value
        );
    }

    public void setVector3f(
            String name,
            float x,
            float y,
            float z
    ) {

        glUniform3f(
                glGetUniformLocation(programId, name),
                x, y, z
        );
    }

    public void setMatrix4f(String name, Matrix4f matrix) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            FloatBuffer buffer = stack.mallocFloat(16);

            matrix.get(buffer);

            glUniformMatrix4fv(
                    glGetUniformLocation(programId, name),
                    false,
                    buffer
            );
        }
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}
