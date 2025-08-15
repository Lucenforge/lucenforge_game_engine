package lucenforge.graphics.text;

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
    private String text = "ABC123";

    private ArrayList<Vector2f> uvOffsets = new ArrayList<>();
    private ArrayList<Vector2f> uvScales = new ArrayList<>();

    private ArrayList<Vector2f> offsets = new ArrayList<>();
    private ArrayList<Float> advance = new ArrayList<>();
    private ArrayList<Float> widths = new ArrayList<>();

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
        widths.clear();

        //Go through each character in the text
        float advanceSubtotal = 0f;
        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            char c = text.charAt(charIndex);
            loadStats(c);
            //Create a quad for it
            Quadrilateral characterMesh = new Quadrilateral();
            addMesh(new Quadrilateral());
            characterMesh.setCorners(
                    new Vector3f(0f, 0f, 0f),
                    new Vector3f(0f, -1f , 0f),
                    new Vector3f(widths.get(charIndex) , -1f , 0f),
                    new Vector3f(widths.get(charIndex) , 0f, 0f)
            );
            // Set the offset
            characterMesh.setPosition(new Vector3f(offsets.get(charIndex).x + advanceSubtotal,offsets.get(charIndex).y,0));
            advanceSubtotal += advance.get(charIndex);
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
        // Calculate the position of the quad in NDC
        offsets.add(new Vector2f(Tools.pxToNDC((int)glyph.xoff), Tools.pxToNDC((int)glyph.yoff)));
        advance.add(Tools.pxToNDC((int)glyph.xadvance));
        widths.add((x1 - x0)/(y1 - y0));
    }

}
