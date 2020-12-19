package com.mgr.myshooter;

import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.IGameLogic;
import com.mgr.engine.Window;
import com.mgr.engine.*;
import com.mgr.engine.debug.Profiler;
import com.mgr.engine.player.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Collections;

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
    private final Vector3f defaultPositionPlayer = new Vector3f(0.0f, 30.0f, 0.0f);
    private final float MOUSE_SENSITIVITY = 0.2f;
    private float fps;



    public MGRGame() throws Exception {
        properties = new PropertiesLoader();
        world      = new World(properties);
        renderer   = new Renderer();
        player     = new Player(true);
        profiler   = new Profiler();
    }
    
    @Override
    public void init(Window window) throws Exception {
        properties.init();
        world.init();
        renderer.init(window, profiler, world);
        player.setPosition(defaultPositionPlayer);
        profiler.init(properties);
        window.setKeyListener(this);

        setPlayerInitialPosition();

        final Texture crosshairTexture = new Texture(properties.getBaseTexturesFolder() + "\\crosshair.png");
        player.createCrosshair(crosshairTexture);
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
        } else if ( window.isKeyPressed(GLFW_KEY_M)) { // Goes back 1 hour
            world.addTime(-60);
        }

    }

    @Override
    public void update(final float interval, final MouseInput mouseInput) {

        // Set the time on the world
        elapsedTime += interval;
        if (elapsedTime >= 600) { // For now on the world every 10 min real time it's an hour on the virtual world
            elapsedTime = 0;
            world.addTime(60);
        }

        // Update player status
        movePlayer(interval, mouseInput);

        // Update all items status
        world.updateItemsStatus(interval);

    }

    private void movePlayer(final float interval, final MouseInput mouseInput){

        //Translation
        Vector3f moveDirection = new Vector3f(direction_x, direction_y, direction_z);
        if (direction_x != 0 || direction_y != 0 || direction_z != 0) {
            if (willPlayerCollideWithWall(interval, moveDirection)) {
                moveDirection = new Vector3f(0.0f, 0.0f, 0.0f); // Cannot move so set translation to zero
            }
        }

        // Rotation
        Vector3f rotation = new Vector3f(rotation_x, rotation_y, rotation_z); // by default keyboard rotation
        // Update camera based on mouse
        if (mouseInput.isRightButtonPressed()) { // if mouse rotation then ignore keyboard
            Vector2f rotVec = mouseInput.getDisplVec();
            rotation = new Vector3f(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        player.updatePlayer(interval, moveDirection, rotation);
    }

    private boolean willPlayerCollideWithWall(final float interval, final Vector3f direction) {
        final Vector3f playerCurrentPosition = new Vector3f(player.getPosition());
        final Vector3f playerNextPosition = new Vector3f(player.getNextPosition(interval, direction));
        return world.willPlayerCollideWithWall(player.getBoundingBoxAtPosition(playerNextPosition), playerCurrentPosition);
    }

    @Override
    public void render(Window window) {

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        if (showProfilerData) {
            prepareProfileData();
        }

        final Camera currentCamera = player.getCamera();

        renderer.render(Collections.singletonList(player), currentCamera, window, showProfilerData);
    }

    private void prepareProfileData() {

        // FPS Info
        String text = "FPS: " + fps;
        profiler.setProfilerEntry("FPS",text);

        // Player position data
        text = "( X: " + player.getPosition().x + " , Y: " + player.getPosition().y + " , Z: " + player.getPosition().z + " , RX: " +
                player.getRotation().x + " , RY: " + player.getRotation().y + " , RZ: " + player.getRotation().z + " )";
        profiler.setProfilerEntry("CAMERA", text);

        // Number of rooms
        text = "Number of rooms: " + world.getNumberOfRooms();
        profiler.setProfilerEntry("NUM_ROOMS", text);

        // Map
        // Get player position on the map cell
        final Vector2i playerCellPosition = world.getItemCellPosition(player.getPosition());
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

    private void setPlayerInitialPosition() {
        // Set player initial position inside a world room
        final Vector3f firstRoomCenter = world.getFirstRoomCenter();
        // We keep the player height
        player.setPosition(new Vector3f(firstRoomCenter.x, player.getPosition().y, firstRoomCenter.z));
    }

    private void createNewMap() {
        if (world != null) {
            try {
                world.cleanUp();
                world.init();
                setPlayerInitialPosition();
            } catch (final Exception ex) {
                System.out.println("Could not create Map " + ex.getMessage());
            }
        }
    }

    private void playerInteractClosestObject() {
         world.playerInteractClosestItem(player.getPosition());
    }

    @Override
    public void cleanUp(){
        player.cleanUp();
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
        } else if (keyCode == GLFW_KEY_I) {
            createNewMap();
        } else if (keyCode == GLFW_KEY_E) {
            playerInteractClosestObject();
        }
    }

    @Override
    public void setActualFramesPerSecond(final float fps){
        this.fps = fps;
    }

}
