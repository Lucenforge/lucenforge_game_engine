package lucenforge.graphics.text;

import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.graphics.Texture;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryUtil;

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



public class FontTexture{

    private Map<Character, Glyph> glyphMap = new HashMap<>();
    private Texture fontTexture;
    float fontSize = 24.0f; // Default font size
    float ascent;
    float descent;
    float lineGap;
    float maxGlyphHeightPx = 0f; // Maximum height of any glyph in the font

    public FontTexture(String fontName) {
        super();
        loadFont(fontName);
    }

    public void loadFont(String fontName){

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

        // Set the font size and scale (NOT SURE IF NEEDED)
//        float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);

        // Get the font metrics
        IntBuffer ascent = BufferUtils.createIntBuffer(1);
        IntBuffer descent = BufferUtils.createIntBuffer(1);
        IntBuffer lineGap = BufferUtils.createIntBuffer(1);
        stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);
        this.ascent = ascent.get(0) / 64.0f; // Convert from font units to pixels
        this.descent = descent.get(0) / 64.0f; // Convert from font units to pixels
        this.lineGap = lineGap.get(0) / 64.0f; // Convert from font units to pixels
        Log.writeln(Log.DEBUG, "Font metrics - Ascent: " + this.ascent + ", Descent: " + this.descent + ", Line Gap: " + this.lineGap);

        int glyphCount = 95; // ASCII 32-126
        float estimatedGlyphArea = fontSize * fontSize * 1.2f; // 1.2 fudge factor for spacing
        float totalArea = glyphCount * estimatedGlyphArea;
        int side = (int)Math.ceil(Math.sqrt(totalArea));
        int bitmapSize = Integer.highestOneBit(side - 1) << 1;

        // Create a bitmap to hold the font glyphs
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmapSize * bitmapSize);
        STBTTPackContext packContext = STBTTPackContext.malloc();
        if (!stbtt_PackBegin(packContext, bitmap, bitmapSize, bitmapSize, 0, 1, MemoryUtil.NULL)) {
            throw new IllegalStateException("Failed to begin packing.");
        }
        stbtt_PackSetOversampling(packContext, 2, 2);
        STBTTPackedchar.Buffer charData = STBTTPackedchar.create(96); // 126 - 32 + 1 = 95 chars
        stbtt_PackFontRange(packContext, fontBuffer, 0, fontSize, 32, charData);
        stbtt_PackEnd(packContext);

        // Create a map to hold the glyphs
        for (char c = 32; c <= 126; c++) {
            STBTTPackedchar packedChar = charData.get(c - 32);
            Glyph glyph = new Glyph(packedChar);
            glyphMap.put(c, glyph);
            maxGlyphHeightPx = Math.max(maxGlyphHeightPx, glyph.y1 - glyph.y0);
            Log.writeln(c + " - width: " + (glyph.x1 - glyph.x0) + ", height: " + (glyph.y1 - glyph.y0) + ", xoff: " + glyph.xOff + ", yoff: " + glyph.yOff + ", xadvance: " + glyph.xAdvance);
        }

        fontTexture = new Texture(bitmap, bitmapSize, bitmapSize, 1);
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

    public Texture texture() {
        return fontTexture;
    }

    public Glyph getGlyph(char c) {
        return glyphMap.get(c);
    }


}
