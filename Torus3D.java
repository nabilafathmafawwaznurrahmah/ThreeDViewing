
package com.grafkom.viewing3d;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Torus3D {

    private int vao;
    private int vbo;
    private int vertexCount;
    private boolean initialized = false;

    public void init() {

        if (initialized) return;

        List<Float> vertices = new ArrayList<>();

        int majorSegments = 40;
        int minorSegments = 24;

        float majorRadius = 0.55f;
        float minorRadius = 0.22f;

        for (int i = 0; i < majorSegments; i++) {

            float u1 = (float) (2.0 * Math.PI * i / majorSegments);
            float u2 = (float) (2.0 * Math.PI * (i + 1) / majorSegments);

            for (int j = 0; j < minorSegments; j++) {

                float v1 = (float) (2.0 * Math.PI * j / minorSegments);
                float v2 = (float) (2.0 * Math.PI * (j + 1) / minorSegments);

                float[] a = point(u1, v1, majorRadius, minorRadius);
                float[] b = point(u2, v1, majorRadius, minorRadius);
                float[] c = point(u2, v2, majorRadius, minorRadius);
                float[] d = point(u1, v2, majorRadius, minorRadius);

                addTriangle(vertices, a, b, c);
                addTriangle(vertices, a, c, d);
            }
        }

        float[] data = new float[vertices.size()];

        for (int i = 0; i < data.length; i++) {
            data[i] = vertices.get(i);
        }

        vertexCount = data.length / 8;

        FloatBuffer buffer =
                BufferUtils.createFloatBuffer(data.length);

        buffer.put(data).flip();

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES,
                3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES,
                6L * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        initialized = true;

        System.out.println("Torus 3D berhasil dibuat!");
    }

    private float[] point(
            float u,
            float v,
            float majorRadius,
            float minorRadius) {

        float cosU = (float) Math.cos(u);
        float sinU = (float) Math.sin(u);
        float cosV = (float) Math.cos(v);
        float sinV = (float) Math.sin(v);

        float distance = majorRadius + minorRadius * cosV;

        float x = distance * cosU;
        float y = minorRadius * sinV;
        float z = distance * sinU;

        float nx = cosV * cosU;
        float ny = sinV;
        float nz = cosV * sinU;

        return new float[]{
            x, y, z,
            nx, ny, nz,
            u / (float) (2.0 * Math.PI),
            v / (float) (2.0 * Math.PI)
        };
    }

    private void addTriangle(
            List<Float> vertices,
            float[] a,
            float[] b,
            float[] c) {

        float[] ab = {
            b[0] - a[0], b[1] - a[1], b[2] - a[2]
        };

        float[] ac = {
            c[0] - a[0], c[1] - a[1], c[2] - a[2]
        };

        float nx = ab[1] * ac[2] - ab[2] * ac[1];
        float ny = ab[2] * ac[0] - ab[0] * ac[2];
        float nz = ab[0] * ac[1] - ab[1] * ac[0];

        float normalX = a[3] + b[3] + c[3];
        float normalY = a[4] + b[4] + c[4];
        float normalZ = a[5] + b[5] + c[5];

        if (nx * normalX + ny * normalY + nz * normalZ < 0) {
            addVertex(vertices, a);
            addVertex(vertices, c);
            addVertex(vertices, b);
        } else {
            addVertex(vertices, a);
            addVertex(vertices, b);
            addVertex(vertices, c);
        }
    }

    private void addVertex(List<Float> vertices, float[] p) {

        for (float value : p) {
            vertices.add(value);
        }
    }

    public void draw() {

        if (!initialized) return;

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {

        if (initialized) {
            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);
            initialized = false;
        }
    }
}
