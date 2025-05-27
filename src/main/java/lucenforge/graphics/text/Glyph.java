package lucenforge.graphics.text;

import org.lwjgl.stb.STBTTPackedchar;

public class Glyph {

    public final float x0, y0, x1, y1;
    public final float xoff, yoff;
    public final float xadvance;

    public Glyph(STBTTPackedchar c) {
        x0 = c.x0();
        y0 = c.y0();
        x1 = c.x1();
        y1 = c.y1();
        xoff = c.xoff();
        yoff = c.yoff();
        xadvance = c.xadvance();
    }

}
