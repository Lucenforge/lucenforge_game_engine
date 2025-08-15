package lucenforge.graphics.text;

import org.lwjgl.stb.STBTTPackedchar;

public class Glyph {

    public final float x0, y0, x1, y1;
    public final float xOff, yOff;
    public final float xAdvance;

    public Glyph(STBTTPackedchar c) {
        // Texture pixels
        x0 = c.x0();
        y0 = c.y0();
        x1 = c.x1();
        y1 = c.y1();
        // Based on font size
        xOff = c.xoff();
        yOff = c.yoff();
        xAdvance = c.xadvance();
    }

}
