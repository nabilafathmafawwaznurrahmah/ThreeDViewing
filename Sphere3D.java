
package com.grafkom.viewing3d;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Sphere3D {

    private int vao;
    private int vbo;
    private int vertexCount;
    private boolean initialized = false;

    public void init() {

        if (initialized) return;

        List<Float> vertices = new ArrayList<>();

        int stacks = 32;
        int sectors = 32;
        float radius = 0.8f;

        for (int i = 0; i < stacks; i++) {

            float theta1 = (float) (Math.PI * i / stacks);
            float theta2 = (float) (Math.PI * (i + 1) / stacks);

            for (int j = 0; j < sectors; j++) {

                float phi1 = (float) (2.0 * Math.PI * j / sectors);
                float phi2 = (float) (2.0 * Math.PI * (j + 1) / sectors);

                float[] a = point(theta1, phi1, radius);
                float[] b = point(theta2, phi1, radius);
                float[] c = point(theta2, phi2, radius);
                float[] d = point(theta1, phi2, radius);

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

        System.out.println("Sphere 3D berhasil dibuat!");
    }

    private float[] point(float theta, float phi, float radius) {

        float x = radius * (float) Math.sin(theta) * (float) Math.cos(phi);
        float y = radius * (float) Math.cos(theta);
        float z = radius * (float) Math.sin(theta) * (float) Math.sin(phi);

        return new float[]{x, y, z};
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

        float centerX = (a[0] + b[0] + c[0]) / 3.0f;
        float centerY = (a[1] + b[1] + c[1]) / 3.0f;
        float centerZ = (a[2] + b[2] + c[2]) / 3.0f;

        if (nx * centerX + ny * centerY + nz * centerZ < 0) {
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

        float length = (float) Math.sqrt(
                p[0] * p[0] + p[1] * p[1] + p[2] * p[2]
        );

        float nx = p[0] / length;
        float ny = p[1] / length;
        float nz = p[2] / length;

        vertices.add(p[0]);
        vertices.add(p[1]);
        vertices.add(p[2]);

        vertices.add(nx);
        vertices.add(ny);
        vertices.add(nz);

        vertices.add((float) (Math.atan2(p[2], p[0]) / (2 * Math.PI) + 0.5));
        vertices.add((float) (Math.acos(ny) / Math.PI));
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
