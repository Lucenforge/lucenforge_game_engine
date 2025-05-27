package lucenforge.graphics.text;

import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.graphics.primitives.Quadrilateral;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.stb.STBTruetype.*;

import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;



public class TextMesh extends Quadrilateral {

    public TextMesh(String fontName){

        //Todo: make font loading come from game
        //Todo: Modularize and split this whole thing up

        // Load the font file into a ByteBuffer
        ByteBuffer fontBuffer;
        try {
            FileTools.createDirectory("src/main/resources/fonts");
            // Ensure the font file exists in the specified path
            String fontPath = "src/main/resources/fonts/" + fontName + ".ttf"; // e.g., "fonts/arial.ttf"
            if(!FileTools.doesFileExist(fontPath)){
                Log.writeln(Log.ERROR, "Font file not found: " + fontPath);
            }
            fontPath = "fonts/" + fontName + ".ttf";

            URL url = Thread.currentThread().getContextClassLoader().getResource("fonts/Ariel_Rounded_MT_Bold.TTF");
            Log.writeln(Log.TELEMETRY, "Font resource URL: " + url);

            fontBuffer = ioResourceToByteBuffer(fontPath, 160 * 1024);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font file", e);
        }

        // Create a font info object
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontBuffer)) {
            throw new IllegalStateException("Failed to initialize font information.");
        }

        // Set the font size and scale
        float fontSize = 24.0f;
        float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);

        // Get the font metrics
        IntBuffer ascent = BufferUtils.createIntBuffer(1);
        IntBuffer descent = BufferUtils.createIntBuffer(1);
        IntBuffer lineGap = BufferUtils.createIntBuffer(1);
        stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);

        int bitmapWidth = 512;
        int bitmapHeight = 512;

        // Create a bitmap to hold the font glyphs
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmapWidth * bitmapHeight);
        STBTTPackContext packContext = STBTTPackContext.malloc();
        if (!stbtt_PackBegin(packContext, bitmap, bitmapWidth, bitmapHeight, 0, 1, MemoryUtil.NULL)) {
            throw new IllegalStateException("Failed to begin packing.");
        }
        stbtt_PackSetOversampling(packContext, 2, 2);
        STBTTPackedchar.Buffer charData = STBTTPackedchar.create(96); // 126 - 32 + 1 = 95 chars
        stbtt_PackFontRange(packContext, fontBuffer, 0, fontSize, 32, charData);
        stbtt_PackEnd(packContext);

        // Create a texture to hold the bitmap
        int texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, bitmapWidth, bitmapHeight, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Create a map to hold the glyphs
        Map<Character, Glyph> glyphMap = new HashMap<>();
        for (char c = 32; c <= 126; c++) {
            STBTTPackedchar packedChar = charData.get(c - 32);
            glyphMap.put(c, new Glyph(packedChar));
            Log.writeln(glyphMap.get(c));
        }
    }


    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        try (
                InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                ReadableByteChannel rbc = Channels.newChannel(source)
        ) {
            buffer = BufferUtils.createByteBuffer(bufferSize);

            while (true) {
                int bytes = rbc.read(buffer);
                if (bytes == -1) break;
                if (buffer.remaining() == 0) {
                    // Double the buffer size
                    ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }

            buffer.flip();
        }

        return buffer;
    }


}
