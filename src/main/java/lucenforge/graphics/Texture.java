package lucenforge.graphics;

import lucenforge.files.Log;
import lucenforge.graphics.shaders.Shader;
import org.joml.Vector2i;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*; // for glGenerateMipmap
import static org.lwjgl.opengl.GL46C.GL_MAX_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.opengl.GL46C.GL_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public class Texture {

    private final String name;
    private final ByteBuffer imageData;
    private final Vector2i dimensions;
    private final int channels;
    private final int textureID;
    private Shader shader;

    public static void init(boolean flipVertically){
        stbi_set_flip_vertically_on_load(flipVertically);
    }

    public Texture(String name, ByteBuffer image, int width, int height, int channels) {
        this.name = name;
        this.imageData = image;
        this.dimensions = new Vector2i(width, height);
        this.channels = channels;
        Log.writeln(name + " loaded with dimensions: " + dimensions.x + "x" + dimensions.y + " and channels: " + channels);

        // Upload once at creation
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Apply preferences (requires bound texture!)
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // Remove mipmap usage
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR); // Enable mipmap usage

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Upload image data once
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, dimensions.x, dimensions.y, 0,
                channels == 3 ? GL_RGB : GL_RGBA, GL_UNSIGNED_BYTE, imageData);

        float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY, maxAniso);

        glGenerateMipmap(GL_TEXTURE_2D); // Generate mipmaps ONCE

        glBindTexture(GL_TEXTURE_2D, 0); // Unbind
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public void cleanup(){
        glDeleteTextures(textureID);
    }
}