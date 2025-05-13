package lucenforge.files;

import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.primitives.mesh.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class MeshFile {

    // Vertices
    private ArrayList<Vector3f> fileVertices        = null;
    private ArrayList<Vector2f> fileVertexTextures  = null;
    private ArrayList<Vector3f> fileVertexNormals   = null;
    // Indices
    private ArrayList<Vector3i> fileVertexIndices   = null;
    private ArrayList<Vector3i> fileTextureIndices  = null;
    private ArrayList<Vector3i> fileNormalIndices   = null;

    private int skippedOBJFaces = 0;

    public Mesh load(String name){
        return null;
    }

    // Parse mesh data from an obj string; todo redo everything
    // Only supports simple v and f right now, no normals or textures
    public void parseOBJ(String fileContents) {
        String[] lines = fileContents.split("\n");
        for (String line : lines) {
            // Lines starting with V (vertex)
            if (line.startsWith("v ")) {
                parseVertexOBJ(line);
            // Lines starting with vt (texture coordinates)
            } else if (line.startsWith("vt ")){
                parseVertexTextureOBJ(line);
            // Lines starting with vn (vertex normals)
            } else if (line.startsWith("vn ")) {
                parseVertexNormalOBJ(line);
            // Lines starting with f (face indices)
            } else if (line.startsWith("f ")) {
                parseFaceOBJ(line);
            }
        }
        if(skippedOBJFaces > 0)
            Log.writeln(Log.WARNING, skippedOBJFaces + " faces skipped due to out of bounds indices");
        if(fileVertexNormals == null && fileVertices != null)
            Mesh.computeNormals(true, fileVertices, fileVertexIndices);
    }

    private void parseFaceOBJ(String line){
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

    private void parseVertexOBJ(String line){
        String[] parts = line.split(" ");
        if (parts.length == 4) {
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float z = Float.parseFloat(parts[3]);
            if(fileVertices == null)
                fileVertices = new ArrayList<>();
            fileVertices.add(new Vector3f(x, y, z));
        }else {
            Log.writeln(Log.WARNING, "Invalid vertex line: " + line + "; Expected format: v x y z");
        }
    }

    private void parseVertexTextureOBJ(String line){
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
    }

    private void parseVertexNormalOBJ(String line){
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
    }

    // Triangulate a face with multiple vertices
    private Vector3i[] triangulateMultiFaces(int[] faceIndices){
        Vector3i[] triangles = new Vector3i[faceIndices.length - 2];
        for (int i = 0; i < faceIndices.length - 2; i++) {
            triangles[i] = new Vector3i(faceIndices[0], faceIndices[i + 1], faceIndices[i + 2]);
        }
        return triangles;
    }


    // Loads all obj files from the models directory
    public HashMap<String, Mesh> loadMeshFiles(){
        HashMap<String, Mesh> models = new HashMap<>();
        //Check if the models directory exists, if not, create it
        FileTools.createDirectory("src/main/resources/models");
        //Get list of all files in the models directory with the extension .obj
        ArrayList<Path> objFiles = FileTools.getFilesInDir("src/main/resources/models", ".obj");
        Log.write(Log.SYSTEM, "Models loaded: ");
        for(Path objFile : objFiles) {
            String modelName = objFile.getFileName().toString();
            modelName = modelName.substring(0, modelName.indexOf("."));
            Mesh mesh = new Mesh();
            parseOBJ(FileTools.readFile(objFile));
            models.put(modelName, mesh);
            Log.write(Log.SYSTEM, " (" + modelName + ", v=" + mesh.getNumVerts() + ", f="
                    + mesh.getNumFaces() + "), ");
        }
        Log.writeln("");
        return models;
    }

    // Loads a mesh from the master meshes list
    public static Mesh getMeshFile(String name, Mesh.Usage usage) {
        if (GraphicsManager.masterMeshes.containsKey(name)) {
            return GraphicsManager.masterMeshes.get(name).init(usage);
        } else {
            Log.writeln(Log.ERROR, "Mesh not found: " + name);
            return null;
        }
    }

    // Ensure no instantiation
    private MeshFile() {}
}
