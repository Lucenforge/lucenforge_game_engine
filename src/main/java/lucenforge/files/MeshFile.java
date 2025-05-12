package lucenforge.files;

import lucenforge.graphics.primitives.mesh.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public class MeshFile {

    // Vertexes
    private ArrayList<Vector3f> fileVertices;
    private ArrayList<Vector2f> fileVertexTextures;
    private ArrayList<Vector3f> fileVertexNormals;
    // Indices
    private ArrayList<Vector3i> fileVertexIndices;
    private ArrayList<Vector3i> fileTextureIndices;
    private ArrayList<Vector3i> fileNormalIndices;


    public static Mesh load(String name){
        return null;
    }


    // Parse mesh data from an obj string; todo redo everything
    // Only supports simple v and f right now, no normals or textures
    public void parseOBJ(String fileContents) {
        int skippedOBJFaces = 0;
        String[] lines = fileContents.split("\n");
        for (String line : lines) {
            // Lines starting with V (vertex)
            if (line.startsWith("v ")) {
                if(fileVertices == null)
                    fileVertices = new ArrayList<>();
                String[] parts = line.split(" ");
                if (parts.length == 4) {
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    fileVertices.add(new Vector3f(x, y, z));
                }else{
                    Log.writeln(Log.WARNING, "Invalid vertex line: " + line + "; Expected format: v x y z");
                }
                // Lines starting with vt (texture coordinates)
            } else if (line.startsWith("vt ")){
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    float u = Float.parseFloat(parts[1]);
                    float v = Float.parseFloat(parts[2]);
                    if(fileVertexTextures == null)
                        fileVertexTextures = new ArrayList<>();
                    fileVertexTextures.add(new Vector2f(u, v));
                }else{
                    Log.writeln(Log.WARNING, "Invalid texture line: " + line + "; Expected format: vt u v");
                }
                // Lines starting with vn (vertex normals)
            } else if (line.startsWith("vn ")) {
                String[] parts = line.split(" ");
                if (parts.length == 4) {
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    if(fileVertexNormals == null) {
                        fileVertexNormals = new ArrayList<>();
                    }
                    fileVertexNormals.add(new Vector3f(x, y, z));
                }else{
                    Log.writeln(Log.WARNING, "Invalid normal line: " + line + "; Expected format: vn x y z");
                }
                // Lines starting with f (face indices)
            } else if (line.startsWith("f ")) {
                // Split by space to get each index
                String[] parts = line.replace("\r","").split(" ");
                // check for invalid line
                if (parts.length < 4)
                    Log.writeln(Log.WARNING, "Invalid face line: " + line + "; Expected format: f v1 v2 v3 ...");
                // get each part for triangulation
                int[] vIndicesRaw = new int[parts.length - 1];
                int[] tIndicesRaw = new int[parts.length - 1];
                int[] nIndicesRaw = new int[parts.length - 1];
                for (int part = 1; part < parts.length; part++) {
                    String[] types = parts[part].split("/");
                    // Read the vertex index
                    int vIndex = Integer.parseInt(types[0]) - 1;
                    // Skip face if index not present
                    if(vIndex >= fileVertices.size()){
                        skippedOBJFaces++;
                        continue;
                    }
                    vIndicesRaw[part - 1] = vIndex;
                    // If of the format v/t/n, parse the texture and normal indices
                    if(types.length > 1){
                        // Make sure the texture indices is normalized and add the part to the list
                        if(fileTextureIndices == null)
                            fileTextureIndices = new ArrayList<>();
                        // Read the texture index
                        int tIndex = Integer.parseInt(types[1]) - 1;
                        // Skip face if index not present
                        if(tIndex >= fileVertexTextures.size()){
                            skippedOBJFaces++;
                            continue;
                        }
                        tIndicesRaw[part - 1] = tIndex;
                        // Make sure the normal indices is normalized and add the part to the list
                        if(fileNormalIndices == null)
                            fileNormalIndices = new ArrayList<>();
                        // Read normal index
                        int nIndex = Integer.parseInt(types[2]) - 1;
                        // Skip face if index not present
                        if(nIndex >= fileVertexNormals.size()){
                            Log.writeln(Log.DEBUG, "IT HAPPENED: " + nIndex + " " + fileVertexNormals.size());
                            skippedOBJFaces++;
                            continue;
                        }
                        nIndicesRaw[part - 1] = nIndex;
                    }
                }
                // Triangulate the face
                Vector3i[] vIndicesTri = triangulateMultiFaces(vIndicesRaw);
                Vector3i[] tIndicesTri = triangulateMultiFaces(tIndicesRaw);
                Vector3i[] nIndicesTri = triangulateMultiFaces(nIndicesRaw);
                // Add the indices to the list
                if(fileVertexIndices == null)
                    fileVertexIndices = new ArrayList<>();
                for(int triangle = 0; triangle < vIndicesTri.length; triangle++){
                    fileVertexIndices.add(vIndicesTri[triangle]);
                    if(fileTextureIndices != null)
                        fileTextureIndices.add(tIndicesTri[triangle]);
                    if(fileNormalIndices != null)
                        fileNormalIndices.add(nIndicesTri[triangle]);
                }
            }
        }
        if(skippedOBJFaces > 0)
            Log.writeln(Log.WARNING, skippedOBJFaces + " faces skipped due to out of bounds indices");
        if(fileVertexNormals == null)
            Mesh.computeNormals(true, fileVertexIndices, fileVertices, fileVertexNormals);
    }

    // Triangulate a face with multiple vertices
    private Vector3i[] triangulateMultiFaces(int[] faceIndices){
        Vector3i[] triangles = new Vector3i[faceIndices.length - 2];
        for (int i = 0; i < faceIndices.length - 2; i++) {
            triangles[i] = new Vector3i(faceIndices[0], faceIndices[i + 1], faceIndices[i + 2]);
        }
        return triangles;
    }


    // Ensure no instantiation
    private MeshFile() {}
}
