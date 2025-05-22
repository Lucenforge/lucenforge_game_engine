package lucenforge.files;

import lucenforge.graphics.Texture;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureFile {

    private static boolean initialized = false;
    private final static String modelsDir = "src/main/resources/textures/";

    public static Texture load(String name){
        if(!initialized)
            init();

        String path = modelsDir + name + ".png";
        Log.writeln("Loading: " + path);

        MemoryStack stack = MemoryStack.stackPush();

        IntBuffer width = stack.mallocInt(1);
        IntBuffer height = stack.mallocInt(1);
        IntBuffer channels = stack.mallocInt(1);

        ByteBuffer image = STBImage.stbi_load(path, width, height, channels, 4);
        if (image == null) {
            Log.writeln(Log.ERROR, "Failed to load image: " + path + " because " + STBImage.stbi_failure_reason());
            return null;
        }
        return new Texture(name, image, width.get(), height.get(), channels.get());
    }

    private static void init(){
        FileTools.createDirectory(modelsDir);
        initialized = true;
    }

}
