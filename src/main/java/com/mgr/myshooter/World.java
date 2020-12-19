package com.mgr.myshooter;

import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.*;
import com.mgr.engine.light.DirectionalLight;
import com.mgr.engine.collision.BoundingBox;
import com.mgr.engine.collision.CollisionDetector;
import com.mgr.myshooter.Map.DoomDoor;
import com.mgr.myshooter.Map.RandomMap;
import com.mgr.myshooter.Map.Room;
import com.mgr.myshooter.Map.RoomConnector;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;

public class World {

    private final Integer MAX_NUMBER_ROOMS = 50;

    // The shader only have space for 5 lights of each type (point light and sport light)
    // This limit is just by the code (constants), actually hardware might support much more.
    // Each room can decide to create some lights
    // I'm thinking when the player is not on the room, the lights will be off
    private final int MAX_POINT_LIGHTS = 5;
    private final int MAX_SPOT_LIGHTS = 5;


    private RandomMap map;
    private DirectionalLight sun;
    private int time; //In minutes: from 0 to 1440, where 720 is noon

    private ShaderProgram mapShaderProgram;



    public World(PropertiesLoader props) {
        map = new RandomMap(MAX_POINT_LIGHTS, MAX_SPOT_LIGHTS, props);

        //Assume time starts as noon, the sun is right on top of us
        sun = new DirectionalLight(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f), 0.5f);
        time = 720;
    }

    public void init() throws Exception {
        map.generateRandomMap(MAX_NUMBER_ROOMS);
        createMapShader();
        map.setShaderProgram(mapShaderProgram);
    }

    public Pair<Integer, int[][]> getMapPlanAsIntMatrix() {
        return map.getMapPlanAsIntMatrix();
    }

    public Vector2i getItemCellPosition(final Vector3f worldPosition) {
        return map.getItemCellPosition(worldPosition);
    }

    public void cleanUp() {
        map.cleanUp();
        mapShaderProgram.cleanup();
    }

    public void setTime(int minutesAfterMidnight) {
        time = minutesAfterMidnight;
        calcSunPositionAndLightColor();
    }

    public void addTime(int elapsedMinutes) {
        System.out.println(" Time is " + time / 60 + " hours");
        time += elapsedMinutes;
        if (time >= 1440) { // 24 hours
            time = time - 1440;
        } else if (time <= 0) { // In case I ever let the time rewind
            time = 1440 + time;
        }
        calcSunPositionAndLightColor();
    }

    public DirectionalLight getSunLight() {
        return sun;
    }

    public int getMAX_POINT_LIGHTS() {
        return MAX_POINT_LIGHTS;
    }

    public int getMAX_SPOT_LIGHTS() {
        return MAX_SPOT_LIGHTS;
    }

    private void createMapShader() throws Exception{
        mapShaderProgram = new ShaderProgram();
        mapShaderProgram.createVertexShader(Utils.loadResource("/Shaders/vertex.vs"));
        mapShaderProgram.createFragmentShader(Utils.loadResource("/Shaders/fragment.fs"));
        mapShaderProgram.link();

        mapShaderProgram.createUniform("projectionMatrix");
        mapShaderProgram.createUniform("modelViewMatrix");
        mapShaderProgram.createUniform("texture_sampler");
        mapShaderProgram.createUniform("specularPower");
        mapShaderProgram.createUniform("ambientLight");
        mapShaderProgram.createMaterialUniform("material");
        mapShaderProgram.createDirectionalLightUniform("directionalLight");
        mapShaderProgram.createPointLightListUniform("pointLights", getMAX_POINT_LIGHTS());
        mapShaderProgram.createSpotLightListUniform("spotLights", getMAX_SPOT_LIGHTS());
    }

    // Based on time calculates the position of the sun and color of the light
    private void calcSunPositionAndLightColor() {
        // When noon will be 0 degrees, when 6 am will be -90 and 6 pm 90
        float lightAngle = time / 4 - 180;

        if (lightAngle > 90 || lightAngle < -90) {
            sun.setIntensity(0); // night
        } else if (lightAngle <= -80 || lightAngle >= 80) { // As closer to dawn or dusk the intensity is different and the color too (if the light)
            float factor = 1 - (Math.abs(lightAngle) - 80) / 10.0f;
            sun.setIntensity(factor);
            sun.setColor(new Vector3f(sun.getColor().x, Math.max(factor, 0.9f), Math.max(factor, 0.5f)));
        } else {
            sun.setIntensity(1);
            sun.setColor(new Vector3f(1, 1, 1));
        }
        double angRad = Math.toRadians(lightAngle);
        sun.setDirection(new Vector3f((float) Math.sin(angRad), (float) Math.cos(angRad), sun.getDirection().z));
    }

    public int getNumberOfRooms() {
        return map.getRooms().size();
    }

    public boolean willPlayerCollideWithWall(final BoundingBox box, final Vector3f currentPosition) {

        // Get item current cell position map (in cells)
        final Vector2i itemCellPosition = getItemCellPosition(currentPosition);

        // Check collision against walls
        List<BoundingBox> allWallAABB = map.getAllWallAABBAtCellPosition(itemCellPosition);
        // Also against doors
        List<BoundingBox> allDoorsOnRoom = map.getAllDoorsAABBAtCellPosition(itemCellPosition);

        allWallAABB.addAll(allDoorsOnRoom);

        return CollisionDetector.willItemCollideAgainstAABB(box, allWallAABB);
    }


    public Vector3f getFirstRoomCenter() {
        return map.getCenterRoomByIndex(1);
    }

    public void playerInteractClosestItem(final Vector3f playerPosition) {

        // Can player interact with map items (like doors)
        map.playerInteractItems(playerPosition);

    }

    public void updateItemsStatus(final float interval) {
         // Update Doors Status
        map.updateDoorsStatus(interval);
    }

    public void render(final Camera camera, final Matrix4f projectionMatrix, final Transformation transformation) {

        final Vector3f ambientLight = new Vector3f(1.0f,1.0f,1.0f);
        final float specularPower = 1.0f;

        map.render(camera, projectionMatrix, transformation, ambientLight, specularPower, getSunLight());

    }
}
