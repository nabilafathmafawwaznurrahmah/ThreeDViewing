
package com.grafkom.viewing3d;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh3D {

    private int vao;
    private int vbo;

    private boolean initialized = false;

    private static final int FLOATS_PER_VERTEX = 8;
    private static final int VERTEX_COUNT = 36;

    public void init() {

        if (initialized) {
            return;
        }

        float[] vertices = createCubeVertices();

        FloatBuffer buffer =
                BufferUtils.createFloatBuffer(vertices.length);

        buffer.put(vertices).flip();

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(
                GL_ARRAY_BUFFER,
                buffer,
                GL_STATIC_DRAW
        );

        // Position
        glVertexAttribPointer(
                0, 3, GL_FLOAT, false,
                FLOATS_PER_VERTEX * Float.BYTES, 0
        );

        glEnableVertexAttribArray(0);

        // Normal
        glVertexAttribPointer(
                1, 3, GL_FLOAT, false,
                FLOATS_PER_VERTEX * Float.BYTES,
                3L * Float.BYTES
        );

        glEnableVertexAttribArray(1);

        // Texture Coordinate
        glVertexAttribPointer(
                2, 2, GL_FLOAT, false,
                FLOATS_PER_VERTEX * Float.BYTES,
                6L * Float.BYTES
        );

        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        initialized = true;

        System.out.println(
                "Mesh cube berhasil dibuat!"
        );
    }

    public void drawCube() {

        if (!initialized) {
            throw new IllegalStateException(
                    "Mesh3D belum diinisialisasi!"
            );
        }

        glBindVertexArray(vao);

        glDrawArrays(
                GL_TRIANGLES,
                0,
                VERTEX_COUNT
        );

        glBindVertexArray(0);
    }

    public void cleanup() {

        if (initialized) {

            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);

            initialized = false;
        }
    }

    private float[] createCubeVertices() {

        float[] front = createFace(
                new float[]{-1, -1, 1},
                new float[]{1, -1, 1},
                new float[]{1, 1, 1},
                new float[]{-1, 1, 1},
                new float[]{0, 0, 1}
        );

        float[] back = createFace(
                new float[]{1, -1, -1},
                new float[]{-1, -1, -1},
                new float[]{-1, 1, -1},
                new float[]{1, 1, -1},
                new float[]{0, 0, -1}
        );

        float[] top = createFace(
                new float[]{-1, 1, 1},
                new float[]{1, 1, 1},
                new float[]{1, 1, -1},
                new float[]{-1, 1, -1},
                new float[]{0, 1, 0}
        );

        float[] bottom = createFace(
                new float[]{-1, -1, -1},
                new float[]{1, -1, -1},
                new float[]{1, -1, 1},
                new float[]{-1, -1, 1},
                new float[]{0, -1, 0}
        );

        float[] right = createFace(
                new float[]{1, -1, 1},
                new float[]{1, -1, -1},
                new float[]{1, 1, -1},
                new float[]{1, 1, 1},
                new float[]{1, 0, 0}
        );

        float[] left = createFace(
                new float[]{-1, -1, -1},
                new float[]{-1, -1, 1},
                new float[]{-1, 1, 1},
                new float[]{-1, 1, -1},
                new float[]{-1, 0, 0}
        );

        float[] result = new float[
                front.length + back.length +
                top.length + bottom.length +
                right.length + left.length
        ];

        int offset = 0;

        for (float[] face : new float[][]{
                front, back, top, bottom, right, left
        }) {

            System.arraycopy(
                    face,
                    0,
                    result,
                    offset,
                    face.length
            );

            offset += face.length;
        }

        return result;
    }

    private float[] createFace(
            float[] a,
            float[] b,
            float[] c,
            float[] d,
            float[] normal) {

        float[] uvA = {0, 0};
        float[] uvB = {1, 0};
        float[] uvC = {1, 1};
        float[] uvD = {0, 1};

        return new float[]{

            // Triangle 1
            a[0], a[1], a[2],
            normal[0], normal[1], normal[2],
            uvA[0], uvA[1],

            b[0], b[1], b[2],
            normal[0], normal[1], normal[2],
            uvB[0], uvB[1],

            c[0], c[1], c[2],
            normal[0], normal[1], normal[2],
            uvC[0], uvC[1],

            // Triangle 2
            a[0], a[1], a[2],
            normal[0], normal[1], normal[2],
            uvA[0], uvA[1],

            c[0], c[1], c[2],
            normal[0], normal[1], normal[2],
            uvC[0], uvC[1],

            d[0], d[1], d[2],
            normal[0], normal[1], normal[2],
            uvD[0], uvD[1]
        };
    }
}
