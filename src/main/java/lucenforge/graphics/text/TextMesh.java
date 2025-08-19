package lucenforge.graphics.text;

import lucenforge.files.Log;
import lucenforge.graphics.primitives.Quadrilateral;
import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.MeshGroup;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;

public class TextMesh extends MeshGroup {

    private final FontTexture fontTexture;
    private String text = "Avyanna! Orion! Mama! Papi!!!!!!";// "abcdefghijklmnopqrstuvwxyz";////

    private ArrayList<Vector2f> uvOffsets = new ArrayList<>();
    private ArrayList<Vector2f> uvScales = new ArrayList<>();

    private ArrayList<Vector2f> offsets = new ArrayList<>();
    private ArrayList<Float> advances = new ArrayList<>();
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
        advances.clear();
        sizes.clear();

        //Go through each character in the text
        Vector2f pen = new Vector2f();
        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            char c = text.charAt(charIndex);
            loadStats(c);

            Vector2f size = sizes.get(charIndex);
            Vector2f offset = offsets.get(charIndex);
            float advance = advances.get(charIndex);

            // Create quad at (0, 0) to (width, height)
            float zOffset = (2 * (charIndex % 2) - 1) * 0.01f; // Slight offset for visibility
            Quadrilateral characterMesh = new Quadrilateral(
                    new Vector3f(0f  , size.y, zOffset),
                    new Vector3f(0f  , 0f , zOffset),
                    new Vector3f(size.x , 0f , zOffset),
                    new Vector3f(size.x , size.y, zOffset)
            );

            // Position quad at (penX + offsetX, penY + offsetY)
            characterMesh.setPosition(new Vector3f(pen.x + offset.x, pen.y + offset.y, 0));
            addMesh(characterMesh);

            // Set texture coordinates
            characterMesh.addTexture(fontTexture.texture());

            // Advance pen position
            pen.x += advance + 0.05f;
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
        float x0 = glyph.x0 / textureSize.x;
        float y0 = glyph.y0 / textureSize.y;
        float x1 = glyph.x1 / textureSize.x;
        float y1 = glyph.y1 / textureSize.y;

        uvOffsets.add(new Vector2f(x0, y0));
        uvScales.add(new Vector2f(x1 - x0, y1 - y0));

        // Normalize all by fontSize for consistent layout
        float width  = (glyph.x1 - glyph.x0) / fontTexture.maxGlyphHeightPx;
        float height = (glyph.y1 - glyph.y0) / fontTexture.maxGlyphHeightPx;
        float offsetX = glyph.xOff / fontTexture.fontSize;
        float offsetY = - glyph.yOff / fontTexture.fontSize - height + 1f;
        float advance = glyph.xAdvance / fontTexture.fontSize;

        Log.writeln(Log.DEBUG, "Character: " + c + ", width: " + width + ", height: " + height +
                ", xoff: " + offsetX + ", yoff: " + offsetY + ", xadvance: " + advance);

        offsets.add(new Vector2f(offsetX, offsetY));
        sizes.add(new Vector2f(width, height));
        advances.add(advance);
    }

}
