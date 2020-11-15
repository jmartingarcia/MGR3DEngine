package com.mgr.myshooter.Map;

import com.mgr.engine.*;
import com.mgr.engine.collision.BoundingBox;
import org.joml.*;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {

    // MAKE it always divisible by 2
    protected final int numStripsPerWall = 10;

    protected final int roomWidth;
    protected final int roomHeight;
    protected final int roomDepth;
    protected final int doorWidth;
    protected final Map<WallOrientation, Material> materialsMap;

    protected int index;
    protected String type;

    public int getIndex() {
        return index;
    }

    public String getType() {

        return type;
    }


    protected float scale;

    public float getScale() {

        return scale;
    }

    public void setScale(float scale) {

        this.scale = scale;
    }


    protected Vector3f worldPosition;  // The center position in World Coordinates

    public void setWorldPosition(Vector3f worldPosition) {

        this.worldPosition = new Vector3f(worldPosition.x, worldPosition.y, worldPosition.z);
    }

    public Vector3f getWorldPosition() {

        return worldPosition;
    }

    protected PointLight[] pointLights = null;
    protected SpotLight[]  spotLights   = null;


    protected final HashMap<WallOrientation, Integer> adjacentRooms = new HashMap<>() {{
        put(WallOrientation.FRONT, -1);
        put(WallOrientation.BACK, -1);
        put(WallOrientation.RIGHT, -1);
        put(WallOrientation.LEFT, -1);
        put(WallOrientation.UP, -1); //Ceiling
        put(WallOrientation.DOWN, -1); //Floor
    }};

    public Map<WallOrientation, Integer> getAdjacentRooms() {
        return adjacentRooms;
    }

    // There could be multiple wall on each direction. When there is a door the wall is actually two smaller walls
    // with the door in the middle
    protected final HashMap<WallOrientation, List<Wall>> walls = new HashMap<>() {{
        put(WallOrientation.FRONT, null);
        put(WallOrientation.BACK, null);
        put(WallOrientation.RIGHT, null);
        put(WallOrientation.LEFT, null);
        put(WallOrientation.UP, null); //Ceiling
        put(WallOrientation.DOWN, null); //Floor
    }};


    protected List<Wall> getFrontWalls() {
        return walls.get(WallOrientation.FRONT);
    }

    protected List<Wall> getBackWalls() {
        return walls.get(WallOrientation.BACK);
    }

    protected List<Wall> getLeftWalls() {
        return walls.get(WallOrientation.LEFT);
    }

    protected List<Wall> getRightWalls() {
        return walls.get(WallOrientation.RIGHT);
    }

    protected List<Wall> getCeilingWalls() {
        return walls.get(WallOrientation.UP);
    }

    protected List<Wall> getFloorWalls() {
        return walls.get(WallOrientation.DOWN);
    }


    public Room(final Integer index, final float scale,
                final int maxPointLights, final int maxSpotLights,
                final int roomWidth, final int roomHeight, final int roomDepth,
                final int doorWidth, final Map<WallOrientation, Material> materialsMap){

        this.index         = index;
        this.scale         = scale;
        this.roomWidth     = roomWidth;
        this.roomHeight    = roomHeight;
        this.roomDepth     = roomDepth;
        this.materialsMap  = materialsMap;
        this.doorWidth     = doorWidth;

        // Create the lights with no intensity (that's what the shader expects if there is no lights, it needs the objects created)
        // based on the book, some cards might not like a flag that indicates of objects(light) exists or not. Not sure why yet, need to investigate.
        // TODO - Research
        //   * Max lights supported
        //   * How to manage better the existence of light or not
        pointLights = new PointLight[maxPointLights];
        for (int i=0;i<maxPointLights;i++){
            pointLights[i] = new PointLight(new Vector3f(1.0f,1.0f,1.0f), new Vector3f(0.0f, 0.0f, 0.0f), 0.0f);
        }
        spotLights  = new SpotLight[maxSpotLights];
        for (int i=0;i<maxSpotLights;i++){
            PointLight pl = new PointLight(new Vector3f(1.0f,1.0f,1.0f), new Vector3f(0.0f, 0.0f, 0.0f), 0.0f);
            spotLights[i] = new SpotLight(pl, new Vector3f(0.0f, -1.0f, 0.0f), 45.0f);
        }


    }

    public void setPathWithRoomIndex(final WallOrientation direction, final Integer roomIndex){

        adjacentRooms.replace(direction, roomIndex);
    }

    public void init() throws InvalidAttributeValueException {

        for (final WallOrientation dir : WallOrientation.values()){
            if (adjacentRooms.get(dir) != -1){ //There is a room next to this wall
                walls.put(dir,getWallWithDoor(dir));
            } else {
                walls.put(dir,getWallWithNoDoor(dir));
            }
        }
    }

    /*
    For a wall with a door what I'm really doing is creating two smaller walls leaving a hole on the center
    In that hole I will locate a door
     */
    private List<Wall> getWallWithDoor(WallOrientation direction) throws InvalidAttributeValueException {

        final ArrayList<Wall> walls = new ArrayList<>();
        int wallWidth  = (roomWidth - doorWidth) / 2;
        int wallDepth  = (roomDepth - doorWidth) / 2;
        int wallHeight = roomHeight;
        boolean clockwise = true;
        final Vector3f center1 = new Vector3f(0.0f, 0.0f, 0.0f);
        final Vector3f center2 = new Vector3f(0.0f, 0.0f, 0.0f);

        if (direction == WallOrientation.LEFT || direction == WallOrientation.RIGHT) {
            clockwise = false;
            wallWidth = wallDepth;  // When the wall is on the sides the depth of the room determines the width of the wall
            center1.x  = center2.x = -roomWidth/2;
            center1.y  = center2.y = roomHeight/2;
            center1.z  = - (doorWidth/2 + wallWidth/2);
            center2.z  =   (doorWidth/2 + wallWidth/2);
            if (direction == WallOrientation.RIGHT) {
                clockwise = true;
                center1.x = -center1.x;
                center2.x = -center2.x;
            }
        } else if (direction == WallOrientation.UP || direction == WallOrientation.DOWN) {
            wallHeight = roomDepth; // For the ceiling and floor the height of the wall is the depth of the room
            center1.z  = - (doorWidth/2 + wallHeight/2);
            center2.z  =   (doorWidth/2 + wallHeight/2);
            center1.x  = center2.x = roomWidth/2;
            center1.y  = center2.y = roomHeight;
            if (direction == WallOrientation.DOWN) {
                clockwise = false;
                center1.y  = center2.y = 0.0f;
            }
        } else if (direction == WallOrientation.FRONT || direction == WallOrientation.BACK) {
            center1.x  = - (doorWidth/2 + wallWidth/2);
            center2.x  =   (doorWidth/2 + wallWidth/2);
            center1.z  = center2.z = - roomDepth/2;
            center1.y  = center2.y = roomHeight/2;
            if (direction == WallOrientation.BACK) {
                clockwise = false;
                center1.z = -center1.z;
                center2.z = -center2.z;
            }
        }

        walls.add( new Wall(wallWidth, wallHeight, numStripsPerWall, direction, clockwise,
                center1, materialsMap.get(direction)));
        walls.add( new Wall(wallWidth, wallHeight, numStripsPerWall, direction, clockwise,
                center2, materialsMap.get(direction)));

        return walls;
    }

    protected List<Wall> getWallWithNoDoor(WallOrientation direction) throws InvalidAttributeValueException {

        final ArrayList<Wall> walls = new ArrayList<>();
        int wallWidth  = roomWidth;
        int wallHeight = roomHeight;
        boolean clockwise = true;
        final Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);

        if (direction == WallOrientation.LEFT || direction == WallOrientation.RIGHT) {
            clockwise = false;
            wallWidth = roomDepth;  // When the wall is on the sides the depth of the room determines the width of the wall
            center.x = -roomWidth/2;
            center.y = roomHeight/2;
            if (direction == WallOrientation.RIGHT) {
                clockwise = true;
                center.x = -center.x;
            }
        } else if (direction == WallOrientation.UP || direction == WallOrientation.DOWN) {
            wallHeight = roomDepth; // For the ceiling and floor the height of the wall is the depth of the room
            center.y = roomHeight;
            if (direction == WallOrientation.DOWN) {
                clockwise = false;
                center.y = 0.0f;
            }
        } else if (direction == WallOrientation.FRONT || direction == WallOrientation.BACK) {
            center.y = roomHeight/2;
            center.z = -roomDepth/2;
            if (direction == WallOrientation.BACK) {
                clockwise = false;
                center.z = -center.z;
            }
        }

        walls.add( new Wall(wallWidth, wallHeight, numStripsPerWall, direction, clockwise,
                           center, materialsMap.get(direction)));

        return walls;
    }

    public void cleanUp() {
        for (WallOrientation dir : WallOrientation.values()){
            List<Wall> selectedWalls = walls.get(dir);
            if (selectedWalls == null) continue;
            for (Wall wall : selectedWalls)
                wall.cleanUp();
        }
    }

    private void setupPointLights(final ShaderProgram shaderProgram, final Matrix4f viewMatrix) {

        PointLight[] currPointLights = new PointLight[pointLights.length];


        // Send Point Light data to the shader
        for (int i=0;i<pointLights.length;i++){
            // Convert PointLights position into view(camera) coordinates
            currPointLights[i] = new PointLight(pointLights[i]);
            Vector3f lightPos = currPointLights[i].getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            currPointLights[i].setPosition(lightPos);

        }
        shaderProgram.setUniform("pointLights", currPointLights);

    }

    private void setupSpotLights(final ShaderProgram shaderProgram, final Matrix4f viewMatrix) {

        SpotLight[]  currSpotLights  = new SpotLight[spotLights.length];

        for (int i=0;i<spotLights.length;i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            currSpotLights[i] = new SpotLight(spotLights[i]);
            Vector4f dir = new Vector4f(currSpotLights[i].getConeDirection(), 0);  // set w = 0 because we don't to translate the cone
            dir.mul(viewMatrix);
            currSpotLights[i].setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
            Vector3f lightPos = currSpotLights[i].getPointLight().getPosition();

            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            currSpotLights[i].getPointLight().setPosition(lightPos);
        }
        shaderProgram.setUniform("spotLights", currSpotLights);
    }

    // the shader program is expected to have the following uniforms
    // material
    public void render(final ShaderProgram shaderProgram, final Matrix4f viewMatrix) {

        setupPointLights(shaderProgram, viewMatrix);

        setupSpotLights(shaderProgram, viewMatrix);

        // Render all walls
        for (WallOrientation dir : WallOrientation.values()){
            List<Wall> selectedWalls = walls.get(dir);
            if (selectedWalls == null) continue;
            for (Wall wall : selectedWalls) {
                shaderProgram.setUniform("material", wall.getMaterial());
                wall.render();
            }
        }
    }

    public List<Planef> getAllPlanesFromWalls() {
        final ArrayList<Planef> list_planes = new ArrayList<>();
        for (final WallOrientation dir : WallOrientation.values()) {
            List<Wall> selectedWalls = walls.get(dir);
            if (selectedWalls == null) continue;
            for (final Wall wall : selectedWalls) {
                // Wall has it's vertices in local coords. I need to transform them to world coordinates
                // prior to calculate the plane for collision detection.
                final Vector3f wallVertex = wall.getFirstVertex();
                final Vector3f wallNormal = wall.getNormal();
                final Vector3f roomWorldPosition = getWorldPosition();
                // Translating the wall vertex to world position
                wallVertex.x += roomWorldPosition.x;
                wallVertex.y += roomWorldPosition.y;
                wallVertex.z += roomWorldPosition.z;
                // Create plane
                list_planes.add(new Planef(wallVertex, wallNormal));
            }
        }

        return list_planes;
    }

    public List<BoundingBox> getAABBFromAllWalls() {
        final ArrayList<BoundingBox> list_boxes = new ArrayList<>();
        for (final WallOrientation dir : WallOrientation.values()) {
            List<Wall> selectedWalls = walls.get(dir);
            if (selectedWalls == null) continue;
            for (final Wall wall : selectedWalls) {
                // Translating the bounding box to world coordinates
                final BoundingBox wallBox = wall.getBoundingBoxCopy();
                final Vector3f max = wallBox.getMaxVertex();
                final Vector3f min = wallBox.getMinVertex();
                final Vector3f roomWorldPosition = getWorldPosition();

                max.add(roomWorldPosition);
                min.add(roomWorldPosition);

                // Create plane
                list_boxes.add(new BoundingBox(min, max));
            }
        }

        return list_boxes;
    }



}
