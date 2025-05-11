package lucenforge.files;

import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.Shader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class FileTools {

    // Create directory if it doesn't exist
    public static void createDirectory(String path) {
        Path dir = Paths.get(path);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            }catch(Exception e) {
                Log.writeln(Log.ERROR, "Error creating log directory: " + e.getMessage());
            }
        }
    }

    // Get the number of files in a directory
    public static long getNumFilesInDir(String path){
        Path dir = Paths.get(path);
        try {
            return Files.list(dir).count();
        } catch (Exception e) {
            Log.writeln(Log.ERROR, "Error getting number of files in directory: " + e.getMessage());
            return -1;
        }
    }

    // Limit the number of files in a directory, deleting the oldest files
    public static void limitNumFilesInDir(String path, int maxFiles){
        Path dir = Paths.get(path);
        long numFiles = getNumFilesInDir(path);
        try {
            while (numFiles >= maxFiles) {
                //Delete the oldest log file
                java.nio.file.Path oldestLog = Files.list(dir).min(Comparator.comparingLong((Path a) -> {
                            try {
                                return Files.getLastModifiedTime(a).toMillis();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }))
                        .orElse(null);
                if (oldestLog != null) {
                    Files.delete(oldestLog);
                    numFiles--;
                }
            }
        }catch(IOException e) {
            Log.writeln(Log.ERROR, "Error limiting number of files in directory: " + e.getMessage());
        }
    }

    // Get all files in a directory (with a specific search)
    public static ArrayList<Path> getFilesInDir(String path, String search) {
        Path dir = Paths.get(path);
        ArrayList<Path> files = new ArrayList<>();
        try {
            Files.list(dir)
                .filter(file -> file.toString().contains(search))
                .forEach(files::add);
            return files;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Read full file content
    public static String readFile(String path) {
        return readFile(Paths.get(path));
    }
    public static String readFile(Path path){
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Loads all shaders from the shaders directory
    public static HashMap<String, Shader> loadShaderFiles(){
        HashMap<String, Shader> shaders;
        shaders = new HashMap<>();
        //Check if the shaders directory exists, if not, create it
        FileTools.createDirectory("src/main/resources/shaders");
        //Get list of all files in the shaders directory with the extension .vert.glsl or .frag.glsl
        ArrayList<Path> vertFiles = FileTools.getFilesInDir("src/main/resources/shaders", ".vert.glsl");
        ArrayList<Path> fragFiles = FileTools.getFilesInDir("src/main/resources/shaders", ".frag.glsl");
        assert(vertFiles.size() == fragFiles.size()) : "Number of vertex and fragment shaders do not match!";
        //Check that the vertex and fragment shader names match
        Log.write(Log.SYSTEM, "Shaders loaded: ");
        for(int i = 0; i < vertFiles.size(); i++){
            String vertFilePath = vertFiles.get(i).toString();
            String fragFilePath = fragFiles.get(i).toString();
            String vertFileName = vertFilePath.substring(vertFilePath.lastIndexOf("\\") + 1, vertFilePath.indexOf("."));
            String fragFileName = fragFilePath.substring(fragFilePath.lastIndexOf("\\") + 1, fragFilePath.indexOf("."));
            //Check that the vertex and fragment shader names match
            if(!vertFileName.equals(fragFileName)) {
                Log.writeln(Log.WARNING, "Vertex and fragment shader names do not match; Skipping: ("
                        + vertFileName + ", " + fragFileName + ")");
                continue;
            }
            //Read the shader files
            String vertFileContents = FileTools.readFile(vertFilePath);
            String fragFileContents = FileTools.readFile(fragFilePath);
            //Create the shader program
            Shader shader = new Shader(vertFileName, vertFileContents, fragFileContents);
            //Load it into the shader lookup table
            shaders.put(vertFileName, shader);
            Log.write(Log.SYSTEM, " " + fragFileName + ",");
        }
        Log.writeln("");
        return shaders;
    }

    // Loads all obj files from the models directory
    public static HashMap<String, Mesh> loadMeshFiles(){
        HashMap<String, Mesh> models = new HashMap<>();
        //Check if the models directory exists, if not, create it
        createDirectory("src/main/resources/models");
        //Get list of all files in the models directory with the extension .obj
        ArrayList<Path> objFiles = getFilesInDir("src/main/resources/models", ".obj");
        Log.write(Log.SYSTEM, "Models loaded: ");
        for(Path objFile : objFiles) {
            String modelName = objFile.getFileName().toString();
            modelName = modelName.substring(0, modelName.indexOf("."));
            Mesh mesh = new Mesh();
            mesh.parseOBJ(readFile(objFile));
            models.put(modelName, mesh);
            Log.write(Log.SYSTEM, " (" + modelName + ", v=" + mesh.getNumVerts() + ", f="
                    + mesh.getNumFaces() + "), ");
        }
        Log.writeln("");
        return models;
    }


    public static Mesh getMeshFile(String name, Mesh.Usage usage) {
        if (GraphicsManager.masterMeshes.containsKey(name)) {
            return GraphicsManager.masterMeshes.get(name).init(usage);
        } else {
            Log.writeln(Log.ERROR, "Mesh not found: " + name);
            return null;
        }
    }


    //Prevent instantiation
    public FileTools(){}
}
