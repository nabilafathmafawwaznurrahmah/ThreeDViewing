
package com.grafkom.viewing3d;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Teapot3D {

    private int vao;
    private int vbo;
    private int vertexCount;
    private boolean initialized = false;

    private static final int FLOATS_PER_VERTEX = 8;

    private final List<Float> vertices = new ArrayList<>();

    public void init() {
        if (initialized) {
            return;
        }

        vertices.clear();

        // BADAN TEKO - profil lebih membulat
        addLathe(
            new float[][] {
                {0.00f, -0.55f},
                {0.20f, -0.55f},
                {0.34f, -0.52f},
                {0.46f, -0.43f},
                {0.55f, -0.27f},
                {0.59f, -0.08f},
                {0.59f, 0.12f},
                {0.55f, 0.30f},
                {0.45f, 0.43f},
                {0.30f, 0.49f},
                {0.00f, 0.49f}
            },
            96
        );

        // TUTUP TEKO
        addLathe(
            new float[][] {
                {0.00f, 0.47f},
                {0.20f, 0.47f},
                {0.31f, 0.49f},
                {0.36f, 0.53f},
                {0.34f, 0.56f},
                {0.25f, 0.60f},
                {0.00f, 0.60f}
            },
            96
        );

        // KNOB TUTUP
        addLathe(
            new float[][] {
                {0.00f, 0.59f},
                {0.055f, 0.59f},
                {0.085f, 0.62f},
                {0.09f, 0.68f},
                {0.065f, 0.72f},
                {0.00f, 0.73f}
            },
            48
        );

        // CERAT - dibuat lebih panjang dan melengkung
        addTube(
            new float[][] {
                {0.48f, 0.18f, 0.0f},
                {0.62f, 0.21f, 0.0f},
                {0.75f, 0.29f, 0.0f},
                {0.87f, 0.42f, 0.0f},
                {1.00f, 0.54f, 0.0f},
                {1.12f, 0.59f, 0.0f}
            },
            0.105f,
            32
        );

        // PEGANGAN - lengkungan lebih halus
        addTube(
            new float[][] {
                {-0.48f, 0.30f, 0.0f},
                {-0.65f, 0.38f, 0.0f},
                {-0.84f, 0.36f, 0.0f},
                {-0.99f, 0.23f, 0.0f},
                {-1.04f, 0.02f, 0.0f},
                {-1.01f, -0.20f, 0.0f},
                {-0.87f, -0.39f, 0.0f},
                {-0.66f, -0.45f, 0.0f},
                {-0.48f, -0.37f, 0.0f}
            },
            0.075f,
            32
        );

        uploadMesh();
        initialized = true;
    }

    // MEMBUAT PERMUKAAN PUTAR DENGAN NORMAL HALUS
    private void addLathe(float[][] profile, int segments) {

        for (int i = 0; i < profile.length - 1; i++) {

            float r1 = profile[i][0];
            float y1 = profile[i][1];

            float r2 = profile[i + 1][0];
            float y2 = profile[i + 1][1];

            float dr = r2 - r1;
            float dy = y2 - y1;

            float normalLength = (float) Math.sqrt(dr * dr + dy * dy);

            if (normalLength < 0.000001f) {
                continue;
            }

            float nr = dy / normalLength;
            float ny = -dr / normalLength;

            for (int j = 0; j < segments; j++) {

                float a1 = (float) (2.0 * Math.PI * j / segments);
                float a2 = (float) (2.0 * Math.PI * (j + 1) / segments);

                float[] p1 = point(r1, y1, a1);
                float[] p2 = point(r1, y1, a2);
                float[] p3 = point(r2, y2, a2);
                float[] p4 = point(r2, y2, a1);

                float[] n1 = latheNormal(nr, ny, a1);
                float[] n2 = latheNormal(nr, ny, a2);

                addSmoothTriangle(p1, n1, p3, n2, p2, n1);
                addSmoothTriangle(p1, n1, p4, n2, p3, n2);
            }
        }
    }

    private float[] latheNormal(float nr, float ny, float angle) {

        return new float[] {
            nr * (float) Math.cos(angle),
            ny,
            nr * (float) Math.sin(angle)
        };
    }

    private float[] point(float radius, float y, float angle) {

        return new float[] {
            radius * (float) Math.cos(angle),
            y,
            radius * (float) Math.sin(angle)
        };
    }

    // MEMBUAT TABUNG MELENGKUNG DENGAN SAMBUNGAN HALUS
    private void addTube(
            float[][] path,
            float radius,
            int segments
    ) {

        int count = path.length;

        float[][] sides = new float[count][3];
        float[][] ups = new float[count][3];

        // Menghitung arah dan kerangka setiap titik
        for (int i = 0; i < count; i++) {

            float[] tangent = new float[3];

            if (i == 0) {
                tangent = subtract(path[1], path[0]);
            } else if (i == count - 1) {
                tangent = subtract(path[i], path[i - 1]);
            } else {
                tangent = subtract(path[i + 1], path[i - 1]);
            }

            normalize(tangent);

            // Arah samping
            float[] reference = {0.0f, 0.0f, 1.0f};

            float[] side = cross(tangent, reference);

            if (length(side) < 0.0001f) {
                reference = new float[] {0.0f, 1.0f, 0.0f};
                side = cross(tangent, reference);
            }

            normalize(side);

            float[] up = cross(tangent, side);
            normalize(up);

            sides[i] = side;
            ups[i] = up;
        }

        // Membuat seluruh lingkaran tabung
        float[][][] rings = new float[count][segments][3];

        for (int i = 0; i < count; i++) {

            for (int j = 0; j < segments; j++) {

                float angle = (float) (2.0 * Math.PI * j / segments);

                float c = (float) Math.cos(angle);
                float s = (float) Math.sin(angle);

                rings[i][j] = new float[] {
                    path[i][0] + radius * (sides[i][0] * c + ups[i][0] * s),
                    path[i][1] + radius * (sides[i][1] * c + ups[i][1] * s),
                    path[i][2] + radius * (sides[i][2] * c + ups[i][2] * s)
                };
            }
        }

        // Menghubungkan setiap lingkaran
        for (int i = 0; i < count - 1; i++) {

            for (int j = 0; j < segments; j++) {

                int next = (j + 1) % segments;

                float[] p1 = rings[i][j];
                float[] p2 = rings[i][next];
                float[] p3 = rings[i + 1][next];
                float[] p4 = rings[i + 1][j];

                float[] n1 = radialNormal(sides[i], ups[i], j, segments);
                float[] n2 = radialNormal(sides[i], ups[i], next, segments);
                float[] n3 = radialNormal(sides[i + 1], ups[i + 1], next, segments);
                float[] n4 = radialNormal(sides[i + 1], ups[i + 1], j, segments);

                addSmoothTriangle(p1, n1, p2, n2, p3, n3);
                addSmoothTriangle(p1, n1, p3, n3, p4, n4);
            }
        }
    }

    private float[] radialNormal(
            float[] side,
            float[] up,
            int segment,
            int segments
    ) {

        float angle = (float) (2.0 * Math.PI * segment / segments);

        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        float[] normal = {
            side[0] * c + up[0] * s,
            side[1] * c + up[1] * s,
            side[2] * c + up[2] * s
        };

        normalize(normal);
        return normal;
    }

    private float[] subtract(float[] a, float[] b) {

        return new float[] {
            a[0] - b[0],
            a[1] - b[1],
            a[2] - b[2]
        };
    }

    private float[] cross(float[] a, float[] b) {

        return new float[] {
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }

    private float length(float[] v) {

        return (float) Math.sqrt(
            v[0] * v[0] +
            v[1] * v[1] +
            v[2] * v[2]
        );
    }

    private void normalize(float[] v) {

        float len = length(v);

        if (len < 0.000001f) {
            return;
        }

        v[0] /= len;
        v[1] /= len;
        v[2] /= len;
    }

    // MENAMBAHKAN SEGITIGA DENGAN NORMAL MASING-MASING TITIK
    private void addSmoothTriangle(
            float[] a, float[] na,
            float[] b, float[] nb,
            float[] c, float[] nc
    ) {

        addVertex(a, na);
        addVertex(b, nb);
        addVertex(c, nc);
    }

    private void addVertex(float[] position, float[] normal) {

        vertices.add(position[0]);
        vertices.add(position[1]);
        vertices.add(position[2]);

        vertices.add(normal[0]);
        vertices.add(normal[1]);
        vertices.add(normal[2]);

        vertices.add(0.0f);
        vertices.add(0.0f);
    }

    // MENGIRIM DATA MESH KE GPU
    private void uploadMesh() {

        vertexCount = vertices.size() / FLOATS_PER_VERTEX;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(
            vertices.size()
        );

        for (float value : vertices) {
            buffer.put(value);
        }

        buffer.flip();

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(
            GL_ARRAY_BUFFER,
            buffer,
            GL_STATIC_DRAW
        );

        int stride = FLOATS_PER_VERTEX * Float.BYTES;

        glVertexAttribPointer(
            0, 3, GL_FLOAT, false, stride, 0
        );
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(
            1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES
        );
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(
            2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES
        );
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void draw() {

        if (!initialized) {
            return;
        }

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {

        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }

        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }

        initialized = false;
    }
}
