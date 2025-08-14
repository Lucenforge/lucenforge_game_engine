package lucenforge.graphics;

import lucenforge.files.Log;
import lucenforge.graphics.shaders.Shader;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*; // for glGenerateMipmap
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public class Texture {

    private final ByteBuffer imageData;
    private final Vector2i imageDimensions = new Vector2i();
    private final int textureID;
    private final Vector2f uvScale = new Vector2f(1, 1);
    private final Vector2f uvOffset = new Vector2f(0, 0);

    public static void init(boolean flipVertically){
        stbi_set_flip_vertically_on_load(flipVertically);
    }

    public Texture(ByteBuffer image, int width, int height, int channels) {
        this.imageData = image;
        Log.writeln(" - Texture loaded: " + width + "x" + height + "x" + channels);
        this.imageDimensions.set(width, height);

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Filtering
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        setRepeat(false);

        // Format matching
        int format;
        if(channels == 4)
            format = GL_RGBA;
        else if(channels == 3)
            format = GL_RGB;
        else if(channels == 1)
            format = GL_RED;
        else {
            Log.writeln(Log.ERROR, "Unknown number of channels in texture: " + channels);
            return;
        }
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, imageData);

        // Mipmaps — disable if using GL_LINEAR above
        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void pushParamsToShader(Shader shader, int textureUnit) {
        if(shader.isUniformRequired("texture" + textureUnit)) {
            shader.requiredParameter("texture" + textureUnit).set(textureUnit); //todo texture unit or ID??
        }if(shader.isUniformRequired("uvScale"))
            shader.requiredParameter("uvScale").set(uvScale);
        if(shader.isUniformRequired("uvOffset"))
            shader.requiredParameter("uvOffset").set(uvOffset);
    }

    public void bind(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void cleanup(){
        glDeleteTextures(textureID);
    }

    public void setUvOffset(Vector2f offset) {
        this.uvOffset.set(offset);
    }

    public void setUvScale(Vector2f scale) {
        this.uvScale.set(scale);
    }

    public Texture setRepeat(boolean repeat) {
        return setRepeat(repeat, repeat);
    }
    public Texture setRepeat(boolean repeatX, boolean repeatY) {
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, repeatX ? GL_REPEAT : GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, repeatY ? GL_REPEAT : GL_CLAMP_TO_EDGE);
        return this;
    }

    public Vector2i getImageDimensions() {
        return imageDimensions;
    }
}