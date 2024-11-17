package renderer;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;
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
        (0,0)                (0,1)         (0,0)                (0,1)

    if the width or height of the texture does not match with the quad we can
    BLUR or PIXELATE the texture as we stretch or shrink it.
    if we have extra vertices that which has texture coordinate that is not
    defined for our texture then we can also WRAP, STRETCH the texture.

    What we will do is,
    1. we store the texture in an array using the STB library.
        [r, g, b, a, r, g, b, a,...]
    2. then we bind the texture with the parameters that we passed, like WRAP etc.
    3. then we upload it to the GPU with the exact format (i.e, r,g,b,a width height GL_TEXTURE_2D)
*/
public class Texture {

    private String filepath;
    private int texID;

    public Texture(String filepath) {
        this.filepath = filepath;

        // Generate texture on GPU
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        // Set texture parameters
        // Repeat image in both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        // When stretching the image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        // When shrinking and image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

        if (image != null) {
            if (channels.get(0) == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "ERROR: (Texture) Unknown number of channels";
            }
        } else {
            assert false : "ERROR: (Texture) Could not load image '" + filepath + "'";
        }

        stbi_image_free(image);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texID);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
