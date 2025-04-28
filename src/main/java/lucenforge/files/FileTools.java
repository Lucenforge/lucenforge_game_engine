package lucenforge.files;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

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
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //Prevent instantiation
    public FileTools(){}
}
