package lucenforge.graphics;

import lucenforge.output.Window;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Renderer {

    private static float[] clearColor;
    private static float[] vertices = {
            0.0f,  0.5f, 0.0f, // Top
            -0.5f, -0.5f, 0.0f, // Bottom Left
            0.5f, -0.5f, 0.0f  // Bottom Right
    };
    private static final String vertexShaderSource =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "void main() {\n" +
                    "    gl_Position = vec4(aPos, 1.0);\n" +
                    "}";

    private static final String fragmentShaderSource =
            "#version 330 core\n" +
                    "out vec4 FragColor;\n" +
                    "void main() {\n" +
                    "    FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n" + // Orange
                    "}";
    private static int shaderProgram, vao;

    public static void init(Window window){

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window.id(), pWidth, pHeight);
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window.id());
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        GL.createCapabilities();

        // Set the viewport to the correct size
        glViewport(0, 0, window.width(), window.height());

        //Set the clear color to a dark gray
        clearColor = new float[]{0.1f, 0.1f, 0.1f, 1.0f};

        setupShaders();

        // Enable v-sync
        glfwSwapInterval(1);
        // Make the window visible
        glfwShowWindow(window.id());
    }

    private static void setupShaders(){
        // Compile the vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Vertex shader compilation failed:\n" + glGetShaderInfoLog(vertexShader));
        }

        // Compile the fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Fragment shader compilation failed:\n" + glGetShaderInfoLog(fragmentShader));
        }

        // Link shaders into a program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed:\n" + glGetProgramInfoLog(shaderProgram));
        }

        // Delete shaders after linking
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        // === NEW: Set up VAO and VBO ===
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Vertex attribute pointer
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind VAO and VBO for safety
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static void loop(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);
        glBindVertexArray(vao); // You need to bind the VAO before drawing!
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);   // Optional, good habit
    }

    private Renderer() {} // Prevent instantiation
}
