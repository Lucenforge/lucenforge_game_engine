package lucenforge.files;

import lucenforge.graphics.Texture;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureFile {

    private static boolean initialized = false;
    private final static String modelsDir = "src/main/resources/textures/";
    private final static String[] extensions = {".png", ".jpg", ".bmp"};

    public static Texture load(String name){
        if(!initialized)
            init();

        String path = null;
        for (String ext : extensions) {
            path = modelsDir + name + ext;
            if(FileTools.doesFileExist(path))
                break;
        }
        Log.write("Loading: " + path);

        MemoryStack stack = MemoryStack.stackPush();

        IntBuffer width = stack.mallocInt(1);
        IntBuffer height = stack.mallocInt(1);
        IntBuffer channels = stack.mallocInt(1); // unused
        int desiredChannels = 4; // RGBA

        ByteBuffer image = STBImage.stbi_load(path, width, height, channels, desiredChannels);
        if (image == null) {
            Log.writeln(Log.ERROR, "Failed to load image: " + path + " because " + STBImage.stbi_failure_reason());
            return null;
        }
        return new Texture(image, width.get(), height.get(), desiredChannels);
    }

    private static void init(){
        FileTools.createDirectory(modelsDir);
        initialized = true;
    }

}
