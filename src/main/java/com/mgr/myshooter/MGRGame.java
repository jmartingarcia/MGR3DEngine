package com.mgr.myshooter;

import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.IGameLogic;
import com.mgr.engine.Window;
import com.mgr.engine.*;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class MGRGame implements IGameLogic, IKeyListener {

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

    private PropertiesLoader properties;

    private float elapsedTime = 0.0f;


    public MGRGame() {
        properties = new PropertiesLoader();
        world      = new World(properties);
        renderer   = new Renderer();
        player     = new Player();
        profiler   = new Profiler();
    }
    
    @Override
    public void init(Window window) throws Exception {
        properties.init();
        renderer.init(window, profiler, world);
        player.setPosition(new Vector3f(0.0f, 20.0f, 0.0f));
        profiler.init(properties);
        window.setKeyListener(this);
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

        if (showProfilerData) {
            prepareProfileData();
        }

        renderer.render(player.getCamera(), window, showProfilerData);
    }

    private void prepareProfileData() {

        // Player position data
        String text = "( X: " + player.getPosition().x + " , Y: " + player.getPosition().y + " , Z: " + player.getPosition().z + " , RX: " +
                player.getRotation().x + " , RY: " + player.getRotation().y + " , RZ: " + player.getRotation().z + " )";
        profiler.setProfilerEntry("CAMERA", text);

        // Number of rooms
        text = "Number of rooms: " + world.getNumberOfRooms();
        profiler.setProfilerEntry("NUM_ROOMS", text);

        // Map
        // Get player position on the map cell
        final Vector2i playerCellPosition = world.getPLayerCellPosition(player.getPosition());
        final Pair<Integer,int[][]> charMapData = world.getMapPlanAsIntMatrix();
        Integer mapLineSize = charMapData.getLeft();
        int[][] charMap     = charMapData.getRight();
        for (int y=0;y<mapLineSize;y++) {
            String line = "";
            for (int x=0;x<mapLineSize;x++) {
                if (playerCellPosition.x != x || playerCellPosition.y != y) {
                    line += charMap[x][y] + "     ";
                } else {
                    line += "X     ";
                }
            }
            profiler.setProfilerEntry("MAP" + y, line);
        }

    }

    @Override
    public void cleanUp(){
        world.cleanUp();
        renderer.cleanUp();
        profiler.cleanUp();
    }

    private void printProfileData() {
        showProfilerData = !showProfilerData;
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void keyReleased(int keyCode) {
        if (keyCode == GLFW_KEY_T) {
            printProfileData();
        }
    }

}
