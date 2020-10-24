package com.mgr.myshooter;

import com.mgr.engine.*;
import com.mgr.myshooter.Map.Room;
import com.mgr.myshooter.Map.RoomConnector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;


public class Renderer  {

    private ShaderProgram shaderProgram;

    private ShaderProgram profilerProgram;

    private World world;

    // Field of view in radians
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private Matrix4f projectionMatrix;

    private final Transformation transformation;

    private final float specularPower = 1.0f;

    private Profiler profiler;


    public Renderer() {

        transformation = new Transformation();
    }

    public World getWorld() {

        return world;
    }

    public void setWorld(World world) {

        this.world = world;
    }

    public void init(Window window, Profiler profiler, World world) throws Exception {

        this.world = world;
        this.world.init();

        this.profiler = profiler;

        projectionMatrix =  transformation.getProjectionMatrix(FOV, (float) window.getWidth(),  (float)window.getHeight(),  Z_NEAR, Z_FAR);

        setupSceneShader();
        setupProfilerShader();
    }

    public void setupSceneShader() throws Exception {

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/Shaders/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/Shaders/fragment.fs"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createDirectionalLightUniform("directionalLight");
        shaderProgram.createPointLightListUniform("pointLights",world.getMAX_POINT_LIGHTS());
        shaderProgram.createSpotLightListUniform("spotLights",world.getMAX_SPOT_LIGHTS());

    }

    public void setupProfilerShader() throws Exception {

        profilerProgram = new ShaderProgram();
        profilerProgram.createVertexShader(Utils.loadResource("/Shaders/profiler_vertex.vs"));
        profilerProgram.createFragmentShader(Utils.loadResource("/Shaders/profiler_fragment.fs"));
        profilerProgram.link();

        // Create uniforms for Ortographic-model projection matrix and base color
        profilerProgram.createUniform("projModelMatrix");
        profilerProgram.createUniform("color");
        profilerProgram.createUniform("texture_sampler");

    }

    public void clear() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    public void render(final Camera camera, final Window window, final boolean showProfilerData) {

        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
            projectionMatrix =  transformation.getProjectionMatrix(FOV, (float) window.getWidth(),  (float)window.getHeight(),  Z_NEAR, Z_FAR);
        }


        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);


        renderScene(camera, window);

        // Finally show debugging data on screen if enabled
        if (showProfilerData) {
            renderProfiler(window);
        }

    }

    private void renderScene(final Camera camera, final Window window) {

        Vector3f ambientLight = new Vector3f(1.0f,1.0f,1.0f);

        shaderProgram.bind();

        shaderProgram.setUniform("projectionMatrix",projectionMatrix);
        shaderProgram.setUniform("texture_sampler", 0);

        // Update view Matrix
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Update Light Uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);


        // Get position of the sun on the world and set it as a directional light
        DirectionalLight currDirLight = new DirectionalLight(world.getSunLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0); // Do not translate, only rotate (that's what the w=0 does)
        // Convert direction of the light to camera/view  coordinates
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        shaderProgram.setUniform("directionalLight", currDirLight);


        // Get all rooms from the map to draw
        for (Room room : world.getMapRooms()){
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(room, viewMatrix) ;
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            room.render(shaderProgram, viewMatrix);
        }

        // Get all rooms connectors from the map to draw
        for (RoomConnector connector : world.getMapRoomConnectors()){
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(connector, viewMatrix) ;
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            connector.render(shaderProgram, viewMatrix);
        }

        shaderProgram.unbind();
    }

    private void renderProfiler(final Window window) {
        profilerProgram.bind();

        profilerProgram.setUniform("texture_sampler", 0);

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, (float)window.getWidth(), (float)window.getHeight(), 0);

        for (TextItem textItem : profiler.getTextItems()) {
            Mesh mesh = textItem.getMesh();
            // Set orthographic and model matrix for this text item
            Matrix4f projModelMatrix = transformation.getOrtoProjModelMatrix(textItem, ortho);
            profilerProgram.setUniform("projModelMatrix", projModelMatrix);
            profilerProgram.setUniform("color", textItem.getMesh().getMaterial().getAmbientColor());

            // Render the mesh for this HUD item
            mesh.render();
        }

        profilerProgram.unbind();
    }

    public void cleanUp() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (profilerProgram != null) {
            profilerProgram.cleanup();
        }
    }

}
