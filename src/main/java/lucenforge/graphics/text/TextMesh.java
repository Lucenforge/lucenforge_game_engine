package lucenforge.graphics.text;

import lucenforge.files.Log;
import lucenforge.graphics.primitives.Quadrilateral;
import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.MeshGroup;
import lucenforge.misc.Tools;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;

public class TextMesh extends MeshGroup {

    private final FontTexture fontTexture;
    private String text = "abcdefghijklmnopqrstuvwxyz";//"Avyanna! Orion! Mama!";//

    private ArrayList<Vector2f> uvOffsets = new ArrayList<>();
    private ArrayList<Vector2f> uvScales = new ArrayList<>();

    private ArrayList<Vector2f> offsets = new ArrayList<>();
    private ArrayList<Float> advance = new ArrayList<>();
    private ArrayList<Vector2f> sizes = new ArrayList<>();

    public TextMesh(FontTexture fontTexture){
        this.fontTexture = fontTexture;
        setText(text);
    }

    public void setText(String text){
        this.text = text;

        //Clear existing data
        meshes.clear();
        uvOffsets.clear();
        uvScales.clear();
        offsets.clear();
        advance.clear();
        sizes.clear();

        //Go through each character in the text
        float advanceSubtotal = 0f;
        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            char c = text.charAt(charIndex);
            loadStats(c);
            //Create a quad for it
            Quadrilateral characterMesh = new Quadrilateral(
                    new Vector3f(0f                  , 0f                   , 0),
                    new Vector3f(0f                  , -sizes.get(charIndex).y , 0),
                    new Vector3f(sizes.get(charIndex).x , -sizes.get(charIndex).y , 0),
                    new Vector3f(sizes.get(charIndex).x , 0f                   , 0)
            );
            addMesh(characterMesh);
            // Set the offset
            characterMesh.setPosition(new Vector3f(offsets.get(charIndex).x + advanceSubtotal,offsets.get(charIndex).y,0));
            advanceSubtotal += advance.get(charIndex);
//            advanceSubtotal += widths.get(charIndex);
            // Set texture coordinates to cover the entire texture
            characterMesh.addTexture(fontTexture.texture());
        }
    }

    @Override
    public void render() {
        for(int characterIndex = 0; characterIndex < meshes.size(); characterIndex++) {

            Mesh characterQuad = meshes.get(characterIndex);

            characterQuad.textures().get(0).setUvOffset(uvOffsets.get(characterIndex));
            characterQuad.textures().get(0).setUvScale(uvScales.get(characterIndex));

            characterQuad.render();
        }
    }

    private void loadStats(char c){
        Vector2i textureSize = fontTexture.texture().getImageDimensions();
        Glyph glyph = fontTexture.getGlyph(c);
        float x0 = glyph.x0/textureSize.x;
        float y0 = glyph.y0/textureSize.y;
        float x1 = glyph.x1/textureSize.x;
        float y1 = glyph.y1/textureSize.y;

        uvOffsets.add(new Vector2f(x0, y0));
        uvScales.add(new Vector2f(x1 - x0, y1 - y0));

        // Use font ascent for vertical alignment
        float ascent = fontTexture.ascent; // distance from the baseline to the highest point of the font's glyphs.
        float descent = fontTexture.descent; // distance from the baseline to the lowest point of the glyphs, typically a negative value.
        float scale = ascent - descent; // total height of the font glyphs

        float width = glyph.x1 - glyph.x0;
        float height = glyph.y1 - glyph.y0;

        // Offset from baseline (ascent is positive down)
        float offsetXPx = glyph.xOff;
        float offsetYPx = ascent - glyph.yOff; // align to baseline

        // Normalize
        offsets.add(new Vector2f(offsetXPx / scale, -offsetYPx / scale));
        sizes.add(new Vector2f(width / scale, height / scale));
        advance.add(glyph.xAdvance / scale);
    }

}
