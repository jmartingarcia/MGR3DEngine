package com.mgr.myshooter;

import com.mgr.configuration.GameProperties;
import com.mgr.configuration.ItemProperties;
import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.*;
import com.mgr.engine.items.GameItem;
import com.mgr.engine.items.IGameItem;
import com.mgr.engine.items.TextItem;
import com.mgr.engine.light.DirectionalLight;
import com.mgr.engine.collision.BoundingBox;
import com.mgr.engine.collision.CollisionDetector;
import com.mgr.engine.loaders.AnimatedModelLoader;
import com.mgr.engine.loaders.StaticModelLoader;
import com.mgr.engine.shaders.ShaderProgram;
import com.mgr.engine.shaders.StaticShaderFactory;
import com.mgr.myshooter.Map.RandomMap;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class World {

    private final Integer MAX_NUMBER_ROOMS = 50;

    // The shader only have space for 5 lights of each type (point light and sport light)
    // This limit is just by the code (constants), actually hardware might support much more.
    // Each room can decide to create some lights
    // I'm thinking when the player is not on the room, the lights will be off
    private final int MAX_POINT_LIGHTS = 5;
    private final int MAX_SPOT_LIGHTS = 5;

    private GameProperties gameProperties;
    private RandomMap map;
    private DirectionalLight sun;
    private int time; //In minutes: from 0 to 1440, where 720 is noon

    private ShaderProgram mapShaderProgram;

    private ArrayList<IGameItem> worldItems = new ArrayList<>();



    public World(GameProperties props) {
        this.gameProperties = props;
        map = new RandomMap(MAX_POINT_LIGHTS, MAX_SPOT_LIGHTS, props);

        //Assume time starts as noon, the sun is right on top of us
        sun = new DirectionalLight(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f), 0.5f);
        time = 720;
    }

    public void init() throws Exception {
        map.generateRandomMap(MAX_NUMBER_ROOMS);
        mapShaderProgram = StaticShaderFactory.getStandardWorldShader();
        map.setShaderProgram(mapShaderProgram);
        generateWorldItems(worldItems);
    }

    public Pair<Integer, int[][]> getMapPlanAsIntMatrix() {
        return map.getMapPlanAsIntMatrix();
    }

    public Vector2i getItemCellPosition(final Vector3f worldPosition) {
        return map.getItemCellPosition(worldPosition);
    }

    public void cleanUp() {
        map.cleanUp();
        for(IGameItem item : worldItems)
            item.cleanUp();
        worldItems.clear();
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

        // Get player current cell position map (in cells)
        final Vector2i playerCellPosition = getItemCellPosition(currentPosition);

        // Check collision against walls
        final List<BoundingBox> allWallAABB = map.getAllWallAABBAtCellPosition(playerCellPosition);
        // Also against doors
        final List<BoundingBox> allDoorsOnRoom = map.getAllDoorsAABBAtCellPosition(playerCellPosition);

        allWallAABB.addAll(allDoorsOnRoom);

        return CollisionDetector.willItemCollideAgainstAABB(box, allWallAABB);
    }

    private List<BoundingBox> getItemsBoundingBoxes(final List<IGameItem> items) {
        final ArrayList<BoundingBox> listBoxes = new ArrayList<>();

        for (IGameItem item : items) {
            final List<BoundingBox> boxes = item.getBoundingBox();
            for (BoundingBox box : boxes)
                listBoxes.add(box);
        }

        return listBoxes;
    }

    public boolean willPlayerCollideWithItem(final BoundingBox box, final Vector3f currentPosition) {

        // Get item current cell position map (in cells)
        final Vector2i itemCellPosition = getItemCellPosition(currentPosition);

        final List<IGameItem> itemsOnRoom = this.getItemsOnCurrentRoom(itemCellPosition);

        final List<BoundingBox> itemBoundingBoxes = this.getItemsBoundingBoxes(itemsOnRoom);

        return CollisionDetector.willItemCollideAgainstAABB(box, itemBoundingBoxes);
    }

    public List<IGameItem> getItemsOnCurrentRoom(final Vector2i cellPosition) {
        // TODO - We need to calculate all the items on the current room based on their position
        // TODO - For now just returning all items
        return worldItems;
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

        // Update view Matrix
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        map.render(projectionMatrix, viewMatrix, transformation, ambientLight, specularPower, getSunLight());

        for (IGameItem item : worldItems)
            item.render(projectionMatrix, viewMatrix, transformation, ambientLight, getSunLight());

    }

    private void generateWorldItems(final ArrayList<IGameItem> listItems) {
        // if the list of items is not empty, clean them up
        for (IGameItem item : listItems)
            item.cleanUp();
        listItems.clear();

        final ItemProperties itemProperties = new ItemProperties("hellknight");

        // Read Model
        try {
            IGameItem item = AnimatedModelLoader.loadAnimGameItem( this.gameProperties.getBaseModelsFolder() + itemProperties.getModelFileName(),
                    this.gameProperties.getBaseModelsFolder() + itemProperties.getModelTexturesFolderName());
            item.setScale(itemProperties.getModelScale());

            item.setShaderProgram(StaticShaderFactory.getShaderByType(itemProperties.getShader()));

            item.setPosition(itemProperties.getModelPosition());
            item.setRotation(itemProperties.getModelRotation());

            listItems.add(item);

        } catch (Exception ex) {
            System.out.println("Model could not be loaded  " + ex.getMessage());
        }

    }
}
