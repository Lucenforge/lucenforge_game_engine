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
        // Load the mesh file
        String meshFileContents = loadMeshFile(name);
        // Check if the file was loaded successfully
        if(meshFileContents == null) {
            Log.writeln(Log.ERROR, "Mesh file not found: \"" + name + "\"");
            return null;
        }
        // Parse the mesh file
        parseOBJ(meshFileContents);
        Log.writeln("\"" + name + "\"" + " obj file loaded");

        return convertToMesh();
    }

    // Convert the parsed data into a Mesh object
    private Mesh convertToMesh() {
        Mesh mesh = new Mesh();
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        Map<Vertex, Integer> vertexMap = new HashMap<>();

        for (Vector3i vertexIndex : fileVertexIndices) {
            Vector3f position = fileVertices.get(vertexIndex.x);
            Vector2f texture = (fileVertexTextures != null) ? fileVertexTextures.get(vertexIndex.y) : null;
            Vector3f normal = (fileVertexNormals != null) ? fileVertexNormals.get(vertexIndex.z) : null;

            Vertex v = new Vertex(position, texture, normal);

            Integer existingIndex = vertexMap.get(v);
            if (existingIndex != null) {
                indices.add(existingIndex);
            } else {
                int newIndex = vertices.size();
                vertices.add(v);
                vertexMap.put(v, newIndex);
                indices.add(newIndex);
            }
        }

        mesh.setVertices(vertices);
        mesh.setIndices(indices);
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
        Integer[] tIndicesRaw = new Integer[parts.length - 1];
        Integer[] nIndicesRaw = new Integer[parts.length - 1];
        for (int part = 1; part < parts.length; part++) {
            // Split by / to get vertex, texture, and normal indices
            String[] types = parts[part].split("/");
            vIndicesRaw[part - 1] = parseVertexIndices(types[0]);
            if(types.length > 1)
                tIndicesRaw[part - 1] = parseTextureIndex(types[1]);
            if(types.length > 2)
                nIndicesRaw[part - 1] = parseNormalIndices(types[2]);
        }
        // Ensure initialization of proper indices
        if(fileVertexIndices == null)
            fileVertexIndices = new ArrayList<>();
        if(fileTextureIndices == null)
            fileTextureIndices = new ArrayList<>();
        if(fileNormalIndices == null)
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
