package lucenforge.graphics.text;

import lucenforge.files.Log;
import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.primitives.Quadrilateral;
import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.MeshGroup;
import lucenforge.graphics.shaders.Shader;
import lucenforge.graphics.shaders.ShaderParameter;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;


public class TextMesh extends MeshGroup {

    private final FontTexture fontTexture;
    private String text = "ABC-123!";
    private Vector2f textSize = new Vector2f(0, 0); // Width of the text in world units

    private ArrayList<Vector2f> uvOffsets = new ArrayList<>();
    private ArrayList<Vector2f> uvScales = new ArrayList<>();

    private ArrayList<Vector2f> offsets = new ArrayList<>();
    private ArrayList<Float> advances = new ArrayList<>();
    private ArrayList<Vector2f> sizes = new ArrayList<>();

    private Vector2f alignment = new Vector2f(0, 0); // Alignment vector (0, 0) is left-bottom, (1, 1) is right-top

    public TextMesh(FontTexture fontTexture){
        this.fontTexture = fontTexture;
        setText(text);
    }

    public void setText(String text){
        this.text = text;

        // Temporarily hold the shader and usage type
        Mesh.Usage oldUsage = null;
        Shader oldShader = null;
        if(!meshes.isEmpty()) {
            oldUsage = meshes.get(0).usage();
            oldShader = meshes.get(0).shader();
        }

        //Clear existing data
        meshes.clear();
        super.cleanup();
        uvOffsets.clear();
        uvScales.clear();
        offsets.clear();
        advances.clear();
        sizes.clear();

        //Go through each character in the text
        Vector2f pen = new Vector2f();
        int numSkippedChars = 0;
        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            char c = text.charAt(charIndex);
            if(fontTexture.getGlyph(c) == null){
                numSkippedChars++;
                continue;
            }
            loadStats(c);

            Vector2f size = sizes.get(charIndex - numSkippedChars);
            Vector2f offset = offsets.get(charIndex - numSkippedChars);
            float advance = advances.get(charIndex - numSkippedChars);

            // Create quad at (0, 0) to (width, height)
            // todo fix multiple alpha issue
            Quadrilateral characterMesh = new Quadrilateral(
                    new Vector3f(0f  , size.y, 0),
                    new Vector3f(0f  , 0f , 0),
                    new Vector3f(size.x , 0f , 0),
                    new Vector3f(size.x , size.y, 0)
            );

            // Position quad at (penX + offsetX, penY + offsetY)
            characterMesh.setPosition(new Vector3f(pen.x + offset.x, pen.y + offset.y, 0));
            addMesh(characterMesh);

            // Set texture coordinates
            characterMesh.addTexture(fontTexture.texture());

            // Advance pen position
            pen.x += advance + 0.05f;
        }

        // Calculate text size based on the last character's position
        textSize.x = pen.x;
        textSize.y = 1;

        // Init if already done
        if (oldShader != null && oldUsage != null) {
            init(oldUsage, oldShader);
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
        // Load glyph data for the character
        Vector2i textureSize = fontTexture.texture().getImageDimensions();
        Glyph glyph = fontTexture.getGlyph(c);
        if(glyph == null){
            Log.writeln(Log.ERROR, "Glyph not found for character: '" + c + "'");
            // Use a default glyph if the character is not found
            glyph = fontTexture.getGlyph('?'); // Fallback to a common glyph
        }

        // Calculate UV offsets and scales based on the glyph's texture coordinates
        // Normalize UV coordinates to the texture size
        float x0 = glyph.x0 / textureSize.x;
        float y0 = glyph.y0 / textureSize.y;
        float x1 = glyph.x1 / textureSize.x;
        float y1 = glyph.y1 / textureSize.y;

        uvOffsets.add(new Vector2f(x0, y0));
        uvScales.add(new Vector2f(x1 - x0, y1 - y0));

        // Normalize all by fontSize for consistent layout
        float descent = fontTexture.descent / fontTexture.fontSize;
        float width  = (glyph.x1 - glyph.x0) / fontTexture.maxGlyphHeightPx;
        float height = (glyph.y1 - glyph.y0) / fontTexture.maxGlyphHeightPx;
        float offsetX = glyph.xOff / fontTexture.fontSize;
        float offsetY = - glyph.yOff / fontTexture.fontSize - height - descent;
        float advance = glyph.xAdvance / fontTexture.fontSize;

        offsets.add(new Vector2f(offsetX, offsetY));
        sizes.add(new Vector2f(width, height));
        advances.add(advance);
    }

    public void setAlignment(Vector2f newAlignment) {
        // Recalculate positions based on new alignment
        Vector3f translation = new Vector3f(
                textSize.x * (-newAlignment.x + alignment.x),
                -newAlignment.y + alignment.y,
                0f  // Z position can be adjusted if needed
        );
        for (Mesh mesh : meshes) {
            mesh.translate(translation);
        }
        this.alignment = newAlignment;
    }

    // Get Text Size in world units
    public Vector2f getTextSize() {
        return textSize;
    }

}
