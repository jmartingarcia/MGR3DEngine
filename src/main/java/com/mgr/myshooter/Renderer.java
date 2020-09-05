package com.mgr.myshooter;

import com.mgr.engine.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;


public class Renderer {

    private ShaderProgram shaderProgram;

    private World world;

    // Field of view in radians
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private Matrix4f projectionMatrix;

    private final Transformation transformation;

    private final float specularPower = 1.0f;


    public Renderer() {

        transformation = new Transformation();
    }

    public World getWorld() {

        return world;
    }

    public void setWorld(World world) {

        this.world = world;
    }

    public void init(Window window, Profiler profiler) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
        shaderProgram.link();

        projectionMatrix =  transformation.getProjectionMatrix(FOV, (float) window.getWidth(),  (float)window.getHeight(),  Z_NEAR, Z_FAR);

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createPointLightUniform("pointLight");
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createDirectionalLightUniform("directionalLight");

        // Create uniform for default colour and the flag that controls it
        //shaderProgram.createUniform("color");
        //shaderProgram.createUniform("useColor");
    }



    public void clear() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final Camera camera, final Window window) {

        clear();

        Vector3f ambientLight = new Vector3f(1.0f,1.0f,1.0f);
        PointLight pointLight  = new PointLight(new Vector3f(1.0f,1.0f, 1.0f), new Vector3f(0.0f,5.0f, 0.0f), 0.5f);


        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
            projectionMatrix =  transformation.getProjectionMatrix(FOV, (float) window.getWidth(),  (float)window.getHeight(),  Z_NEAR, Z_FAR);
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glFrontFace(GL_CCW);
        //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );

        shaderProgram.bind();

        shaderProgram.setUniform("projectionMatrix",projectionMatrix);
        shaderProgram.setUniform("texture_sampler", 0);

        // Update view Matrix
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Update Light Uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);

        // Get a copy of the point light object and transform its position to view coordinates
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        shaderProgram.setUniform("pointLight", currPointLight);

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

            room.render(shaderProgram);
        }

        shaderProgram.unbind();
    }

    public void cleanUp() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
