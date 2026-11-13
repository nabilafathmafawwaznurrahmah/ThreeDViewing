package com.grafkom.viewing3d;

import org.joml.Matrix4f;

public class House3D {

    private final Mesh3D cube = new Mesh3D();
    private boolean initialized = false;

    public void init() {
        if (initialized) {
            return;
        }

        cube.init();
        initialized = true;
    }

    // Membuat model setiap bagian rumah
    private Matrix4f createPartModel(
            Matrix4f baseModel,
            float x, float y, float z,
            float scaleX, float scaleY, float scaleZ,
            float rotationZ
    ) {

        return new Matrix4f(baseModel)
                .translate(x, y, z)
                .rotateZ((float) Math.toRadians(rotationZ))
                .scale(scaleX, scaleY, scaleZ);
    }

    // Menggambar satu bagian rumah pada scene utama
    private void drawPart(
            ShaderProgram shader,
            Matrix4f model,
            float red,
            float green,
            float blue,
            boolean useTexture
    ) {

        shader.setMatrix4f("uModel", model);

        shader.setVector3f(
                "uObjectColor",
                red, green, blue
        );

        shader.setBoolean("uUseTexture", useTexture);

        cube.drawCube();
    }

    // =========================================
    // SCENE RENDER
    // =========================================

    public void draw(ShaderProgram shader, Matrix4f baseModel) {

        if (!initialized) {
            return;
        }

        // BADAN RUMAH - menggunakan tekstur batu bata
        drawPart(
                shader,
                createPartModel(
                        baseModel,
                        0.0f, -0.45f, 0.0f,
                        0.70f, 0.65f, 0.60f,
                        0.0f
                ),
                1.0f, 1.0f, 1.0f,
                true
        );

        // ATAP KIRI
        drawPart(
                shader,
                createPartModel(
                        baseModel,
                        -0.34f, 0.22f, 0.0f,
                        0.48f, 0.13f, 0.72f,
                        35.0f
                ),
                0.35f, 0.20f, 0.14f,
                false
        );

        // ATAP KANAN
        drawPart(
                shader,
                createPartModel(
                        baseModel,
                        0.34f, 0.22f, 0.0f,
                        0.48f, 0.13f, 0.72f,
                        -35.0f
                ),
                0.35f, 0.20f, 0.14f,
                false
        );

        // PINTU
        drawPart(
                shader,
                createPartModel(
                        baseModel,
                        0.0f, -0.73f, 0.615f,
                        0.22f, 0.40f, 0.035f,
                        0.0f
                ),
                0.30f, 0.16f, 0.09f,
                false
        );

        // JENDELA KIRI
        drawPart(
                shader,
                createPartModel(
                        baseModel,
                        -0.40f, -0.25f, 0.615f,
                        0.18f, 0.18f, 0.035f,
                        0.0f
                ),
                0.65f, 0.82f, 0.88f,
                false
        );

        // JENDELA KANAN
        drawPart(
                shader,
                createPartModel(
                        baseModel,
                        0.40f, -0.25f, 0.615f,
                        0.18f, 0.18f, 0.035f,
                        0.0f
                ),
                0.65f, 0.82f, 0.88f,
                false
        );
    }

    // =========================================
    // SHADOW RENDER
    // =========================================

    public void drawShadow(
            ShaderProgram shadowShader,
            Matrix4f baseModel
    ) {

        if (!initialized) {
            return;
        }

        // Badan rumah
        drawShadowPart(
                shadowShader,
                createPartModel(
                        baseModel,
                        0.0f, -0.45f, 0.0f,
                        0.70f, 0.65f, 0.60f,
                        0.0f
                )
        );

        // Atap kiri
        drawShadowPart(
                shadowShader,
                createPartModel(
                        baseModel,
                        -0.34f, 0.22f, 0.0f,
                        0.48f, 0.13f, 0.72f,
                        35.0f
                )
        );

        // Atap kanan
        drawShadowPart(
                shadowShader,
                createPartModel(
                        baseModel,
                        0.34f, 0.22f, 0.0f,
                        0.48f, 0.13f, 0.72f,
                        -35.0f
                )
        );

        // Pintu
        drawShadowPart(
                shadowShader,
                createPartModel(
                        baseModel,
                        0.0f, -0.73f, 0.615f,
                        0.22f, 0.40f, 0.035f,
                        0.0f
                )
        );

        // Jendela kiri
        drawShadowPart(
                shadowShader,
                createPartModel(
                        baseModel,
                        -0.40f, -0.25f, 0.615f,
                        0.18f, 0.18f, 0.035f,
                        0.0f
                )
        );

        // Jendela kanan
        drawShadowPart(
                shadowShader,
                createPartModel(
                        baseModel,
                        0.40f, -0.25f, 0.615f,
                        0.18f, 0.18f, 0.035f,
                        0.0f
                )
        );
    }

    private void drawShadowPart(
            ShaderProgram shadowShader,
            Matrix4f model
    ) {

        shadowShader.setMatrix4f("uModel", model);
        cube.drawCube();
    }

    public void cleanup() {

        if (initialized) {
            cube.cleanup();
            initialized = false;
        }
    }
}
