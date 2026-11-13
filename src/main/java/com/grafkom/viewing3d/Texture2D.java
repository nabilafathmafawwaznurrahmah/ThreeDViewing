package com.grafkom.viewing3d;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture2D {

    private int textureId;

    public void load(String resourcePath) {

        ByteBuffer imageData = null;
        ByteBuffer image = null;

        try {
            // Membaca gambar dari folder resources
            byte[] bytes;

            try (InputStream input =
                    getClass().getResourceAsStream(resourcePath)) {

                if (input == null) {
                    throw new RuntimeException(
                            "File tekstur tidak ditemukan: "
                            + resourcePath
                    );
                }

                bytes = input.readAllBytes();
            }

            imageData = MemoryUtil.memAlloc(bytes.length);
            imageData.put(bytes).flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {

                IntBuffer widthBuffer = stack.mallocInt(1);
                IntBuffer heightBuffer = stack.mallocInt(1);
                IntBuffer channelsBuffer = stack.mallocInt(1);

                // Membaca gambar menggunakan STBImage
                STBImage.stbi_set_flip_vertically_on_load(true);

                image = STBImage.stbi_load_from_memory(
                        imageData,
                        widthBuffer,
                        heightBuffer,
                        channelsBuffer,
                        4
                );

                if (image == null) {
                    throw new RuntimeException(
                            "Gagal memuat gambar: "
                            + STBImage.stbi_failure_reason()
                    );
                }

                int imageWidth = widthBuffer.get();
                int imageHeight = heightBuffer.get();

                // Membuat OpenGL texture
                textureId = glGenTextures();

                glBindTexture(GL_TEXTURE_2D, textureId);

                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

                glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA,
                        imageWidth,
                        imageHeight,
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        image
                );

                // Membuat mipmap
                glGenerateMipmap(GL_TEXTURE_2D);

                // Filtering awal
                setBilinearMipmap();

                System.out.println(
                        "Texture berhasil dimuat: "
                        + resourcePath
                );

                System.out.println(
                        "Ukuran texture: "
                        + imageWidth + " x " + imageHeight
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Gagal membaca file tekstur", e
            );

        } finally {

            if (image != null) {
                STBImage.stbi_image_free(image);
            }

            if (imageData != null) {
                MemoryUtil.memFree(imageData);
            }
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Nearest Filtering
    public void setNearest() {

        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MIN_FILTER,
                GL_NEAREST_MIPMAP_NEAREST
        );

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MAG_FILTER,
                GL_NEAREST
        );

        glBindTexture(GL_TEXTURE_2D, 0);

        System.out.println(
                "Texture Filtering: Nearest"
        );
    }

    // Bilinear Mipmap Filtering
    public void setBilinearMipmap() {

        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MIN_FILTER,
                GL_LINEAR_MIPMAP_NEAREST
        );

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MAG_FILTER,
                GL_LINEAR
        );

        glBindTexture(GL_TEXTURE_2D, 0);

        System.out.println(
                "Texture Filtering: Bilinear Mipmap"
        );
    }

    public void bind() {

        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {

        if (textureId != 0) {

            glDeleteTextures(textureId);

            textureId = 0;
        }
    }
}
