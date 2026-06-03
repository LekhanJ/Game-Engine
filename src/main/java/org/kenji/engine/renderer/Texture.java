package org.kenji.engine.renderer;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

/*
    The shape we draw on the window, we will map our texture on that shape/quad
    The texture will have coordinates,
        (0,1)                (1,1)         (0,1)                (1,1)
            +----------------+                 1---------------2
            |                |                 |  \            |
            |    Texture     |      ---->      |       \       |
            |                |                 |           \   |
            +----------------+                 3---------------0
        (0,0)                (1,0)         (0,0)                (1,0)

    if the width or height of the texture does not match with the quad we can
    BLUR or PIXELATE the texture as we stretch or shrink it.
    if we have extra vertices which has texture coordinate that is not
    defined for our texture then we can also WRAP or STRETCH the texture.

    What we will do is,
    1. we store the texture in an array using the STB library.
        [r, g, b, a, r, g, b, a,...]
    2. then we bind the texture with the parameters that we passed, like WRAP etc.
    3. then we upload it to the GPU with the exact format (i.e, r,g,b,a width height GL_TEXTURE_2D)
*/

public class Texture {

    // Path to the image file on disk (kept for debugging purposes)
    private String filepath;

    // The ID OpenGL gives us to refer to this texture on the GPU
    private int textureId;

    public Texture(String filepath) {
        this.filepath = filepath;

        // Ask OpenGL to create a new empty texture slot on the GPU and give us its ID
        textureId = glGenTextures();

        // Bind (activate) this texture so all following texture settings apply to it
        glBindTexture(GL_TEXTURE_2D, textureId);

        // ─── Texture Wrapping ─────────────────────────────────────────────────
        // If UV coordinates go outside 0–1, repeat the texture tile instead of stretching the edge

        // GL_TEXTURE_WRAP_S = wrapping rule for the horizontal axis (U / left-right)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);

        // GL_TEXTURE_WRAP_T = wrapping rule for the vertical axis (V / up-down)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // ─── Texture Filtering ────────────────────────────────────────────────
        // GL_NEAREST = pick the single closest pixel — gives a sharp, pixelated look (good for pixel art)
        // GL_LINEAR would blend neighbouring pixels instead — gives a smoother/blurry look

        // MIN filter: what to do when the texture is displayed smaller than its actual size
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        // MAG filter: what to do when the texture is displayed larger than its actual size
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // ─── Load the image from disk using STB ───────────────────────────────
        // These are output buffers — stbi_load will write the image dimensions and channel count into them
        IntBuffer width    = BufferUtils.createIntBuffer(1);  // Will hold the image width in pixels
        IntBuffer height   = BufferUtils.createIntBuffer(1);  // Will hold the image height in pixels
        IntBuffer channels = BufferUtils.createIntBuffer(1);  // Will hold how many color channels (3=RGB, 4=RGBA)

        // Load the image file into a raw byte buffer: [r, g, b, a, r, g, b, a, ...]
        // The last argument '0' means "give me however many channels the file actually has"
        ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

        if (image != null) {
            if (channels.get(0) == 4) {
                // Image has 4 channels (RGB + Alpha) — upload it to the GPU as RGBA
                // Args: target, mipmap level, internal format, width, height, border (always 0), format, data type, data
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 3) {
                // Image has 3 channels (RGB only, no alpha) — upload it to the GPU as RGB
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else {
                // We only support 3 or 4 channel images — anything else is unexpected
                assert false : "Error: (Texture) Unknown number of channels " + channels.get(0);
            }
        } else {
            // stbi_load returned null — the file path is probably wrong or the file is corrupt
            assert false : "Error: (Texture) Could not load image " + filepath;
        }

        // Free the raw image bytes from Java/CPU memory — the GPU has its own copy now, so we don't need this anymore
        stbi_image_free(image);
    }

    // Bind this texture so it becomes the active texture OpenGL will use when drawing
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    // Unbind the texture — passing 0 tells OpenGL "no texture active" (good cleanup practice)
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}