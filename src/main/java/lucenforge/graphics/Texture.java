package lucenforge.graphics;

import lucenforge.files.Log;
import lucenforge.graphics.shaders.Shader;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*; // for glGenerateMipmap
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public class Texture {

    private final ByteBuffer imageData;
    private final int textureID;
    private final Vector2f uvScale = new Vector2f(1, 1);
    private final Vector2f uvOffset = new Vector2f(0, 0);

    public static void init(boolean flipVertically){
        stbi_set_flip_vertically_on_load(flipVertically);
    }

    public Texture(ByteBuffer image, int width, int height, int channels) {
        this.imageData = image;
        Log.writeln(" - loaded: " + width + "x" + height + "x" + channels);

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Filtering
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        setRepeat(false);

        // Format matching
        int format = (channels == 3) ? GL_RGB : GL_RGBA;
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, imageData);

        // Mipmaps — disable if using GL_LINEAR above
         glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void pushParamsToShader(Shader shader, int textureUnit) {
        shader.requiredParameter("texture"+ textureUnit).set(textureUnit);
        shader.requiredParameter("uvScale").set(uvScale);
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
        glBindTexture(GL_TEXTURE_2D, 0);
        return this;
    }
}