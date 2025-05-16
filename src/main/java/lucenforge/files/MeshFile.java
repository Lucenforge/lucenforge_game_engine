package lucenforge.files;

import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    // Load a mesh file and return a Mesh object
    public Mesh load(String name, Mesh.Usage usage){
        Log.writeln("Starting to load \"" + name + ".obj\"");

        // Load the mesh file
        String meshFileContents = loadMeshFile(name);
        // Check if the file was loaded successfully
        if(meshFileContents == null) {
            Log.writeln(Log.ERROR, "Mesh file not found: \"" + name + "\"");
            return null;
        }
        // Parse the mesh file
        parseOBJ(meshFileContents);
        Mesh mesh = convertToMesh(usage);
        Log.writeln("\"" + name + "\"" + " obj file loaded successfully");

        return mesh;
    }

    // Convert the parsed data into a Mesh object
    private Mesh convertToMesh(Mesh.Usage usage) {
        Mesh mesh = new Mesh();
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        Map<Vertex, Integer> vertexMap = new HashMap<>();

        for (int i = 0; i < fileVertexIndices.size(); i++) {
            Vector3i vertexIndex = fileVertexIndices.get(i);
            Vector3f[] positions = new Vector3f[3];
            positions[0] = fileVertices.get(vertexIndex.x);
            positions[1] = fileVertices.get(vertexIndex.y);
            positions[2] = fileVertices.get(vertexIndex.z);

            Vector3i textureIndex = fileTextureIndices != null ? fileTextureIndices.get(i) : null;
            Vector2f[] textures = new Vector2f[3];
            textures[0] = (textureIndex != null) ? fileVertexTextures.get(textureIndex.x) : null;
            textures[1] = (textureIndex != null) ? fileVertexTextures.get(textureIndex.y) : null;
            textures[2] = (textureIndex != null) ? fileVertexTextures.get(textureIndex.z) : null;

            Vector3i normalIndex = fileNormalIndices != null ? fileNormalIndices.get(i) : null;
            Vector3f[] normals = new Vector3f[3];
            normals[0] = (normalIndex != null) ? fileVertexNormals.get(normalIndex.x) : null;
            normals[1] = (normalIndex != null) ? fileVertexNormals.get(normalIndex.y) : null;
            normals[2] = (normalIndex != null) ? fileVertexNormals.get(normalIndex.z) : null;

            for(int vIndex = 0; vIndex < 3; vIndex++){
                Vertex v = new Vertex(positions[vIndex], textures[vIndex], normals[vIndex]);
                Integer existingVertex = vertexMap.get(v);
                if (existingVertex != null) {
                    indices.add(existingVertex);
                } else {
                    int newIndex = vertices.size();
                    vertices.add(v);
                    vertexMap.put(v, newIndex);
                    indices.add(newIndex);
                }
            }
        }

        if (indices.size() % 3 != 0) {
            Log.writeln(Log.WARNING, "Index list is not divisible by 3 (" + indices.size() + "). Dropping incomplete triangle.");
        }

        ArrayList<Vector3i> faces = new ArrayList<>(indices.size() / 3);
        for(int i = 0; i < indices.size(); i += 3) {
            faces.add(new Vector3i(indices.get(i), indices.get(i + 1), indices.get(i + 2)));
        }

        mesh.init(vertices, faces, usage);
        return mesh;
    }

    // Parse mesh data from an obj string
    // Only supports simple v and f right now, no normals or textures
    private void parseOBJ(String fileContents) {
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
            fileVertexNormals = Mesh.computeNormals(true, fileVertices, fileVertexIndices);
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

    private void parseFaceOBJ(String line){
        // Split by space to get each index
        String[] parts = line.replace("\r","").split(" ");
        // check for invalid line
        if (parts.length < 4)
            Log.writeln(Log.WARNING, "Invalid face line: " + line + "; Expected format: f v1 v2 v3 ...");
        // get each part for triangulation
        Integer[] vIndicesRaw = new Integer[parts.length - 1];
        Integer[] tIndicesRaw = null;
        Integer[] nIndicesRaw = null;
        for (int part = 1; part < parts.length; part++) {
            // Split by / to get vertex, texture, and normal indices
            String[] types = parts[part].split("/");
            vIndicesRaw[part - 1] = parseVertexIndices(types[0]);
            if(types.length > 1) {
                if(tIndicesRaw == null)
                    tIndicesRaw = new Integer[parts.length - 1];
                tIndicesRaw[part - 1] = parseTextureIndex(types[1]);
            }
            if(types.length > 2) {
                if(nIndicesRaw == null)
                    nIndicesRaw = new Integer[parts.length - 1];
                nIndicesRaw[part - 1] = parseNormalIndices(types[2]);
            }
        }
        // Ensure initialization of proper indices
        fileVertexIndices = new ArrayList<>();
        if(tIndicesRaw != null)
            fileTextureIndices = new ArrayList<>();
        if(nIndicesRaw != null)
            fileNormalIndices = new ArrayList<>();
        // Triangulate and add the face to the indices list
        triangulateMultiFaces(vIndicesRaw, fileVertexIndices);
        triangulateMultiFaces(tIndicesRaw, fileTextureIndices);
        triangulateMultiFaces(nIndicesRaw, fileNormalIndices);
    }

    private Integer parseVertexIndices(String indices){
        // Check for empty indices
        if(!indices.isEmpty()){
            // Read the vertex index
            int vIndex = Integer.parseInt(indices) - 1;
            // Skip face if index not present
            if(vIndex >= fileVertices.size()){
                skippedOBJFaces++;
                return null;
            }
            return vIndex;
        }else
            return null;
    }
    private Integer parseTextureIndex(String indices){
        // Check for empty texture indices
        if(!indices.isEmpty()){
            // Read the texture index
            int tIndex = Integer.parseInt(indices) - 1;
            // Skip face if index not present
            if(tIndex >= fileVertexTextures.size()){
                skippedOBJFaces++;
                return null;
            }
            return tIndex;
        }else
            return null;
    }
    private Integer parseNormalIndices(String indices){
        // Check for empty normal indices
        if(!indices.isEmpty()){
            // Read normal index
            int nIndex = Integer.parseInt(indices) - 1;
            // Skip face if index not present
            if(nIndex >= fileVertexNormals.size()){
                skippedOBJFaces++;
                return null;
            }
            return nIndex;
        }else
            return null;
    }

    // Triangulate a face with multiple vertices
    private void triangulateMultiFaces(Integer[] faceIndices, ArrayList<Vector3i> indicesList){
        if(faceIndices == null)
            return;
        for (int i = 0; i < faceIndices.length - 2; i++) {
            if(faceIndices[0] == null || faceIndices[i + 1] == null || faceIndices[i + 2] == null)
                continue;
            indicesList.add(new Vector3i(faceIndices[0], faceIndices[i + 1], faceIndices[i + 2]));
        }
    }

    // Loads a mesh file and returns the string for the file
    private String loadMeshFile(String name) {
        String modelsDir = "src/main/resources/models";
        String objFilePath = modelsDir + "/" + name + ".obj";
        // Ensure the models directory exists
        FileTools.createDirectory(modelsDir);
        //check OBJ file existance
        if(FileTools.doesFileExist(objFilePath)){
            return FileTools.readFile(objFilePath);
        }else{
            Log.writeln(Log.ERROR, "Mesh file not found: \"" + objFilePath + "\"");
            return null;
        }
    }
}
