package com.grafkom.viewing3d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMap {

    private int depthMapFBO;
    private int depthMap;

    private final int SHADOW_WIDTH = 2048;
    private final int SHADOW_HEIGHT = 2048;

    public void init() {

        // Membuat Framebuffer
        depthMapFBO = glGenFramebuffers();

        // Membuat Depth Texture
        depthMap = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthMap);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_DEPTH_COMPONENT,
                SHADOW_WIDTH,
                SHADOW_HEIGHT,
                0,
                GL_DEPTH_COMPONENT,
                GL_FLOAT,
                0L
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(
                GL_TEXTURE_2D,
                GL_TEXTURE_BORDER_COLOR,
                new float[]{1.0f, 1.0f, 1.0f, 1.0f}
        );

        // Menghubungkan Depth Texture ke Framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_TEXTURE_2D,
                depthMap,
                0
        );

        // Shadow map hanya menyimpan depth, tanpa warna
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER)
                != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Shadow Framebuffer tidak lengkap!");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        System.out.println("Shadow Map berhasil dibuat!");
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getDepthMap() {
        return depthMap;
    }

    public int getWidth() {
        return SHADOW_WIDTH;
    }

    public int getHeight() {
        return SHADOW_HEIGHT;
    }

    public void cleanup() {
        if (depthMap != 0) {
            glDeleteTextures(depthMap);
            depthMap = 0;
        }

        if (depthMapFBO != 0) {
            glDeleteFramebuffers(depthMapFBO);
            depthMapFBO = 0;
        }
    }
}
