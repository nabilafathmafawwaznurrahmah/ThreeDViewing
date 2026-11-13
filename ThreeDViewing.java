
package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import org.lwjgl.opengl.GL;

public class ThreeDViewing {

    private long window;

    private final Camera3D camera = new Camera3D();
    private final PerspectiveProjection perspective = new PerspectiveProjection();
    private final ProjectionTaxonomyManager projection =
            new ProjectionTaxonomyManager();

    // =========================================
    // OBJEK 3D
    // =========================================

    private final Teapot3D teapot = new Teapot3D();
    private final Sphere3D sphere = new Sphere3D();
    private final House3D house = new House3D();
    private final GroundPlane ground = new GroundPlane();

    // =========================================
    // TEXTURE
    // =========================================

    private final Texture2D texture = new Texture2D();
    private final Texture2D groundTexture = new Texture2D();

    // =========================================
    // SHADOW MAPPING
    // =========================================

    private final ShadowMap shadowMap = new ShadowMap();

    private ShaderProgram shader;
    private ShaderProgram shadowShader;

    // =========================================
    // RENDERING SETTINGS
    // =========================================

    private boolean mousePressed = false;

    private boolean useBlinnPhong = false;
    private boolean flatShading = false;
    private boolean depthTestEnabled = true;
    private boolean cullingEnabled = false;
    private boolean nearestFiltering = false;

    private double lastX;
    private double lastY;

    private int width = 1000;
    private int height = 700;

    private final Vector3f lightPosition =
            new Vector3f(4.0f, 6.0f, 4.0f);

    // =========================================
    // RUN
    // =========================================

    public void run() {

        try {
            init();
            loop();

        } finally {

            texture.cleanup();
            groundTexture.cleanup();
            shadowMap.cleanup();

            if (shader != null) {
                shader.cleanup();
            }

            if (shadowShader != null) {
                shadowShader.cleanup();
            }

            teapot.cleanup();
            sphere.cleanup();
            house.cleanup();
            ground.cleanup();

            if (window != 0) {
                glfwDestroyWindow(window);
            }

            glfwTerminate();
        }
    }

    // =========================================
    // INITIALIZATION
    // =========================================

    private void init() {

        if (!glfwInit()) {
            throw new IllegalStateException(
                    "Unable to initialize GLFW"
            );
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );

        window = glfwCreateWindow(
                width,
                height,
                "3D Visual Realism Suite",
                0,
                0
        );

        if (window == 0) {
            throw new RuntimeException(
                    "Failed to create window"
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        System.out.println(
                "OpenGL Version: " + glGetString(GL_VERSION)
        );

        glViewport(0, 0, width, height);

        // Z-Buffer
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        // Back-Face Culling
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glDisable(GL_CULL_FACE);

        // Background abu-abu hangat
        glClearColor(
                0.78f, 0.76f, 0.72f, 1.0f
        );

        // Shader utama
        shader = new ShaderProgram(
                "/shaders/shadow_scene.vert",
                "/shaders/shadow_scene.frag"
        );

        // Shader Shadow Mapping
        shadowShader = new ShaderProgram(
                "/shaders/shadow.vert",
                "/shaders/shadow.frag"
        );

        // Inisialisasi objek
        teapot.init();
        sphere.init();
        house.init();
        ground.init();

        // Tekstur batu bata untuk rumah
        texture.load("/textures/batu_bata.png");
        texture.setBilinearMipmap();

        // Tekstur ground Light Oak
        groundTexture.load("/textures/light_oak.png");
        groundTexture.setBilinearMipmap();

        // Shadow Map
        shadowMap.init();

        setupCallbacks();

        System.out.println("Semua objek berhasil dimuat!");
        System.out.println("Teapot: Ivory");
        System.out.println("Sphere: Terracotta");
        System.out.println("House: Brick Texture");
        System.out.println("Ground: Light Oak");
        System.out.println("Shadow Mapping berhasil diinisialisasi!");
    }

    // =========================================
    // CALLBACKS
    // =========================================

    private void setupCallbacks() {

        // Window resize
        glfwSetFramebufferSizeCallback(
                window,
                (w, newWidth, newHeight) -> {

                    width = newWidth;
                    height = Math.max(newHeight, 1);

                    glViewport(0, 0, width, height);
                }
        );

        // Mouse click
        glfwSetMouseButtonCallback(
                window,
                (w, button, action, mods) -> {

                    if (button == GLFW_MOUSE_BUTTON_LEFT) {

                        mousePressed = action == GLFW_PRESS;

                        if (mousePressed) {

                            double[] x = new double[1];
                            double[] y = new double[1];

                            glfwGetCursorPos(window, x, y);

                            lastX = x[0];
                            lastY = y[0];
                        }
                    }
                }
        );

        // Mouse movement
        glfwSetCursorPosCallback(
                window,
                (w, xpos, ypos) -> {

                    if (mousePressed) {

                        float dx = (float) (xpos - lastX);
                        float dy = (float) (ypos - lastY);

                        camera.rotate(dx, dy);

                        lastX = xpos;
                        lastY = ypos;
                    }
                }
        );

        // Mouse scroll
        glfwSetScrollCallback(
                window,
                (w, xoffset, yoffset) -> {
                    camera.zoom((float) yoffset * 0.4f);
                }
        );

        // Keyboard
        glfwSetKeyCallback(
                window,
                (w, key, scancode, action, mods) -> {

                    if (action != GLFW_PRESS) {
                        return;
                    }

                    switch (key) {

                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, true);
                            break;

                        // Orthographic
                        case GLFW_KEY_1:

                            projection.setProjection(
                                    ProjectionTaxonomyManager.ProjectionType.ORTHOGRAPHIC
                            );

                            System.out.println("Projection: Orthographic");
                            break;

                        // Isometric
                        case GLFW_KEY_2:

                            projection.setProjection(
                                    ProjectionTaxonomyManager.ProjectionType.ISOMETRIC
                            );

                            System.out.println("Projection: Isometric");
                            break;

                        // Oblique Cavalier
                        case GLFW_KEY_3:

                            projection.setProjection(
                                    ProjectionTaxonomyManager.ProjectionType.OBLIQUE_CAVALIER
                            );

                            System.out.println("Projection: Oblique Cavalier");
                            break;

                        // 1 Point Perspective
                        case GLFW_KEY_4:

                            projection.setProjection(
                                    ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT
                            );

                            System.out.println("Projection: 1 Point Perspective");
                            break;

                        // 2 Point Perspective
                        case GLFW_KEY_5:

                            projection.setProjection(
                                    ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_2POINT
                            );

                            System.out.println("Projection: 2 Point Perspective");
                            break;

                        // 3 Point Perspective
                        case GLFW_KEY_6:

                            projection.setProjection(
                                    ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_3POINT
                            );

                            System.out.println("Projection: 3 Point Perspective");
                            break;

                        // Phong / Blinn-Phong
                        case GLFW_KEY_B:

                            useBlinnPhong = !useBlinnPhong;

                            System.out.println(
                                    "Lighting: " +
                                    (useBlinnPhong
                                            ? "Blinn-Phong"
                                            : "Phong")
                            );

                            break;

                        // Flat / Smooth Shading
                        case GLFW_KEY_F:

                            flatShading = !flatShading;

                            System.out.println(
                                    "Shading: " +
                                    (flatShading ? "Flat" : "Smooth")
                            );

                            break;

                        // Z-Buffer
                        case GLFW_KEY_Z:

                            depthTestEnabled = !depthTestEnabled;

                            if (depthTestEnabled) {
                                glEnable(GL_DEPTH_TEST);
                            } else {
                                glDisable(GL_DEPTH_TEST);
                            }

                            System.out.println(
                                    "Z-Buffer: " +
                                    (depthTestEnabled ? "ON" : "OFF")
                            );

                            break;

                        // Back-Face Culling
                        case GLFW_KEY_C:

                            cullingEnabled = !cullingEnabled;

                            if (cullingEnabled) {
                                glEnable(GL_CULL_FACE);
                            } else {
                                glDisable(GL_CULL_FACE);
                            }

                            System.out.println(
                                    "Back-Face Culling: " +
                                    (cullingEnabled ? "ON" : "OFF")
                            );

                            break;

                        // Nearest Filtering
                        case GLFW_KEY_N:

                            nearestFiltering = true;

                            texture.setNearest();
                            groundTexture.setNearest();

                            System.out.println(
                                    "Texture: Nearest Filtering"
                            );

                            break;

                        // Bilinear Mipmap Filtering
                        case GLFW_KEY_M:

                            nearestFiltering = false;

                            texture.setBilinearMipmap();
                            groundTexture.setBilinearMipmap();

                            System.out.println(
                                    "Texture: Bilinear Mipmap Filtering"
                            );

                            break;

                        // FOV Zoom In
                        case GLFW_KEY_EQUAL:
                        case GLFW_KEY_KP_ADD:

                            perspective.setFovDegree(
                                    perspective.getFovDegree() - 5
                            );

                            break;

                        // FOV Zoom Out
                        case GLFW_KEY_MINUS:
                        case GLFW_KEY_KP_SUBTRACT:

                            perspective.setFovDegree(
                                    perspective.getFovDegree() + 5
                            );

                            break;
                    }
                }
        );
    }

    // =========================================
    // MAIN LOOP
    // =========================================

    private void loop() {

        while (!glfwWindowShouldClose(window)) {

            float aspect = (float) width / height;

            perspective.setAspectRatio(aspect);

            renderScene();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    // =========================================
    // PROJECTION ROTATION
    // =========================================

    private Matrix4f applyProjectionRotation(Matrix4f model) {

        switch (projection.getCurrentType()) {

            case ISOMETRIC:

                model.rotateX(
                        (float) Math.toRadians(35.264)
                );

                model.rotateY(
                        (float) Math.toRadians(45)
                );

                break;

            case OBLIQUE_CAVALIER:

                model.rotateY(
                        (float) Math.toRadians(30)
                );

                break;

            case PERSPECTIVE_2POINT:

                model.rotateY(
                        (float) Math.toRadians(20)
                );

                break;

            case PERSPECTIVE_3POINT:

                model.rotateY(
                        (float) Math.toRadians(20)
                );

                model.rotateX(
                        (float) Math.toRadians(15)
                );

                break;

            default:
                break;
        }

        return model;
    }

    // =========================================
    // MODEL MATRIX
    // =========================================

    // Teapot di sebelah kiri
    private Matrix4f createTeapotModel() {

        Matrix4f model = new Matrix4f()
                .translate(-2.15f, -0.77f, 0.0f)
                .scale(0.62f);

        return applyProjectionRotation(model);
    }

    // Sphere di tengah
    private Matrix4f createSphereModel() {

        Matrix4f model = new Matrix4f()
                .translate(0.0f, -0.3f, 0.0f);

        return applyProjectionRotation(model);
    }

    // House di sebelah kanan
    private Matrix4f createHouseModel() {

        Matrix4f model = new Matrix4f()
                .translate(2.15f, 0.0f, 0.0f);

        return applyProjectionRotation(model);
    }

    // =========================================
    // PROJECTION MATRIX
    // =========================================

    private Matrix4f createProjectionMatrix() {

        float aspect = (float) width / height;

        if (projection.getCurrentType()
                == ProjectionTaxonomyManager.ProjectionType.ORTHOGRAPHIC) {

            return new Matrix4f().ortho(
                    -3.0f * aspect,
                    3.0f * aspect,
                    -3.0f,
                    3.0f,
                    0.1f,
                    50.0f
            );
        }

        return perspective.calculateProjectionMatrix();
    }

    // =========================================
    // LIGHT SPACE MATRIX
    // =========================================

    private Matrix4f createLightSpaceMatrix() {

        Matrix4f lightProjection =
                new Matrix4f().ortho(
                        -10.0f,
                        10.0f,
                        -10.0f,
                        10.0f,
                        1.0f,
                        20.0f
                );

        Matrix4f lightView =
                new Matrix4f().lookAt(
                        lightPosition,
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(0.0f, 1.0f, 0.0f)
                );

        return lightProjection.mul(lightView);
    }

    // =========================================
    // PASS 1: SHADOW MAPPING
    // =========================================

    private void renderShadowPass(
            Matrix4f teapotModel,
            Matrix4f sphereModel,
            Matrix4f houseModel,
            Matrix4f lightSpaceMatrix
    ) {

        shadowMap.bind();

        glViewport(
                0,
                0,
                shadowMap.getWidth(),
                shadowMap.getHeight()
        );

        glClear(GL_DEPTH_BUFFER_BIT);

        shadowShader.use();

        shadowShader.setMatrix4f(
                "uLightSpaceMatrix",
                lightSpaceMatrix
        );

        // -------------------------------------
        // TEAPOT SHADOW
        // -------------------------------------

        shadowShader.setMatrix4f(
                "uModel",
                teapotModel
        );

        teapot.draw();

        // -------------------------------------
        // SPHERE SHADOW
        // -------------------------------------

        shadowShader.setMatrix4f(
                "uModel",
                sphereModel
        );

        sphere.draw();

        // -------------------------------------
        // HOUSE SHADOW
        // Semua bagian rumah ikut dihitung
        // -------------------------------------

        house.drawShadow(
                shadowShader,
                houseModel
        );

        // -------------------------------------
        // GROUND SHADOW
        // -------------------------------------

        shadowShader.setMatrix4f(
                "uModel",
                new Matrix4f()
        );

        ground.draw();

        shadowMap.unbind();
    }

    // =========================================
    // PASS 2: RENDER SCENE
    // =========================================

    private void renderScenePass(
            Matrix4f teapotModel,
            Matrix4f sphereModel,
            Matrix4f houseModel,
            Matrix4f lightSpaceMatrix
    ) {

        glViewport(0, 0, width, height);

        glClear(
                GL_COLOR_BUFFER_BIT |
                GL_DEPTH_BUFFER_BIT
        );

        Matrix4f view =
                camera.calculateLookAtMatrix();

        Matrix4f projectionMatrix =
                createProjectionMatrix();

        shader.use();

        shader.setMatrix4f(
                "uView",
                view
        );

        shader.setMatrix4f(
                "uProjection",
                projectionMatrix
        );

        shader.setMatrix4f(
                "uLightSpaceMatrix",
                lightSpaceMatrix
        );

        Vector3f eye = camera.getEye();

        shader.setVector3f(
                "uLightPosition",
                lightPosition.x,
                lightPosition.y,
                lightPosition.z
        );

        shader.setVector3f(
                "uViewPosition",
                eye.x,
                eye.y,
                eye.z
        );

        shader.setBoolean(
                "uUseBlinnPhong",
                useBlinnPhong
        );

        shader.setBoolean(
                "uFlatShading",
                flatShading
        );

        // =====================================
        // SHADOW MAP - TEXTURE UNIT 1
        // =====================================

        glActiveTexture(GL_TEXTURE1);

        glBindTexture(
                GL_TEXTURE_2D,
                shadowMap.getDepthMap()
        );

        shader.setInteger("uShadowMap", 1);

        // =====================================
        // GROUND PLANE - LIGHT OAK
        // =====================================

        shader.setMatrix4f(
                "uModel",
                new Matrix4f()
        );

        shader.setVector3f(
                "uObjectColor",
                1.0f, 1.0f, 1.0f
        );

        shader.setBoolean(
                "uUseTexture",
                true
        );

        glActiveTexture(GL_TEXTURE0);

        groundTexture.bind();

        shader.setInteger("uTexture", 0);

        ground.draw();

        // =====================================
        // TEAPOT - IVORY / CREAM
        // =====================================

        shader.setMatrix4f(
                "uModel",
                teapotModel
        );

        shader.setVector3f(
                "uObjectColor",
                0.91f, 0.84f, 0.70f
        );

        shader.setBoolean(
                "uUseTexture",
                false
        );

        teapot.draw();

        // =====================================
        // SPHERE - TERRACOTTA
        // =====================================

        shader.setMatrix4f(
                "uModel",
                sphereModel
        );

        shader.setVector3f(
                "uObjectColor",
                0.72f, 0.38f, 0.25f
        );

        shader.setBoolean(
                "uUseTexture",
                false
        );

        sphere.draw();

        // =====================================
        // HOUSE - BRICK TEXTURE
        // =====================================

        glActiveTexture(GL_TEXTURE0);

        texture.bind();

        shader.setInteger("uTexture", 0);

        house.draw(
                shader,
                houseModel
        );

        // =====================================
        // RELEASE TEXTURES
        // =====================================

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);
    }

    // =========================================
    // RENDER SCENE
    // =========================================

    private void renderScene() {

        Matrix4f teapotModel =
                createTeapotModel();

        Matrix4f sphereModel =
                createSphereModel();

        Matrix4f houseModel =
                createHouseModel();

        Matrix4f lightSpaceMatrix =
                createLightSpaceMatrix();

        // PASS 1: SHADOW MAPPING
        renderShadowPass(
                teapotModel,
                sphereModel,
                houseModel,
                lightSpaceMatrix
        );

        // PASS 2: SCENE RENDERING
        renderScenePass(
                teapotModel,
                sphereModel,
                houseModel,
                lightSpaceMatrix
        );
    }

    // =========================================
    // MAIN METHOD
    // =========================================

    public static void main(String[] args) {
        new ThreeDViewing().run();
    }
}
