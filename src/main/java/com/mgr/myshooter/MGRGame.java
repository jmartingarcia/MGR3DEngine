package com.mgr.myshooter;

import com.mgr.engine.IGameLogic;
import com.mgr.engine.Window;
import com.mgr.engine.*;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class MGRGame implements IGameLogic {

    private int direction_x,
                direction_y,
                direction_z,
                rotation_x,
                rotation_y,
                rotation_z  = 0;
    

    private final Renderer renderer;

    private final World world;

    private final Player player;

    private boolean showProfilerData = false;

    private Profiler profiler;

    private float elapsedTime = 0.0f;


    public MGRGame() {
        world    = new World();
        renderer = new Renderer();
        player   = new Player();
        profiler = new Profiler();
    }
    
    @Override
    public void init(Window window) throws Exception {
        renderer.init(window, profiler, world);
        player.setPosition(new Vector3f(0.0f, 20.0f, 0.0f));
        profiler.Init();
    }
    
    @Override
    public void input(Window window) {
        direction_x = 0;
        direction_y = 0;
        direction_z = 0;
        rotation_x  = 0;
        rotation_y  = 0;
        rotation_z  = 0;

        if ( window.isKeyPressed(GLFW_KEY_W) ) {
            direction_z = -1;
        } else if ( window.isKeyPressed(GLFW_KEY_S) ) {
            direction_z = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_A) ) {
            direction_x = -1;
        } else if ( window.isKeyPressed(GLFW_KEY_D) ) {
            direction_x = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_SPACE) ) {
            direction_y = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_V) ) {
            direction_y = -1;
        } else if ( window.isKeyPressed(GLFW_KEY_RIGHT) ) {
            rotation_y = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_LEFT) ) {
            rotation_y = -1;
        } else if ( window.isKeyPressed(GLFW_KEY_T)) {
            printProfileData();
        } else if ( window.isKeyPressed(GLFW_KEY_N)) { // Add 1 hour
            world.addTime(60);
        } else if ( window.isKeyPressed(GLFW_KEY_N)) { // Goes back 1 hour
            world.addTime(-60);
        }

    }

    @Override
    public void update(float interval) {

        // Set the time on the world
        elapsedTime += interval;
        if (elapsedTime >= 600) { // For now on the world every 10 min real time it's an hour on the virtual world
            elapsedTime = 0;
            world.addTime(60);
        }

        //Translation
        player.walk(interval, new Vector3f(direction_x, direction_y, direction_z));
        //Rotation
        player.turn(interval, new Vector3f(rotation_x, rotation_y, rotation_z));
    }

    @Override
    public void render(Window window) {

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //Send info to the profiler
        if (showProfilerData) {
            String text = "( X: " + player.getPosition().x + " , Y: " + player.getPosition().y + " , Z: " + player.getPosition().z + " , RX: " +
                    player.getRotation().x + " , RY: " + player.getRotation().y + " , RZ: " + player.getRotation().z + " )";
            profiler.setProfilerEntry("CAMERA", text);

            text = "Number of rooms: " + world.getNumberOfRooms();
            profiler.setProfilerEntry("NUM_ROOMS", text);
        }

        renderer.render(player.getCamera(), window, showProfilerData);
    }

    @Override
    public void cleanUp(){
        world.cleanUp();
        renderer.cleanUp();
    }

    private void printProfileData() {
        showProfilerData = !showProfilerData;
    }
}
