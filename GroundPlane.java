package com.grafkom.viewing3d;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GroundPlane {

    private int vao;
    private int vbo;

    private static final int FLOATS_PER_VERTEX = 8;
    private static final int VERTEX_COUNT = 6;

    public void init() {

        float[] vertices = {
            // Posisi XYZ          Normal XYZ       Texture UV

            -5f, -1.1f, -5f,       0f, 1f, 0f,      0f, 0f,
            -5f, -1.1f,  5f,       0f, 1f, 0f,      0f, 5f,
             5f, -1.1f,  5f,       0f, 1f, 0f,      5f, 5f,

            -5f, -1.1f, -5f,       0f, 1f, 0f,      0f, 0f,
             5f, -1.1f,  5f,       0f, 1f, 0f,      5f, 5f,
             5f, -1.1f, -5f,       0f, 1f, 0f,      5f, 0f
        };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = FLOATS_PER_VERTEX * Float.BYTES;

        // Posisi
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // Normal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Texture Coordinate
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        System.out.println("Ground Plane berhasil dibuat!");
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);
        glBindVertexArray(0);
    }

    public void cleanup() {
        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }

        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }
    }
}
