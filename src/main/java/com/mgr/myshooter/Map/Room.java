package com.mgr.myshooter.Map;

import com.mgr.engine.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.*;
import org.lwjgl.system.CallbackI;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {

    // MAKE is always divisible by 2
    private final int numStripsPerWall = 10;

    private final int roomWidth;
    private final int roomHeight;
    private final int roomDepth;
    private final Map<WallOrientation, Material> materialsMap;

    private int index;
    private String type;

    public int getIndex() {
        return index;
    }

    public String getType() {

        return type;
    }


    private float scale;

    public float getScale() {

        return scale;
    }

    public void setScale(float scale) {

        this.scale = scale;
    }


    private Vector3f worldPosition;  // The center position in World Coordinates

    public void setWorldPosition(Vector3f worldPosition) {

        this.worldPosition = new Vector3f(worldPosition.x, worldPosition.y, worldPosition.z);
    }

    public Vector3f getWorldPosition() {

        return worldPosition;
    }

    private PointLight[] pointLights = null;
    private SpotLight[]  spotLights   = null;


    private final HashMap<WallOrientation, Integer> roomConnections = new HashMap<>() {{
        put(WallOrientation.FRONT, -1);
        put(WallOrientation.BACK, -1);
        put(WallOrientation.RIGHT, -1);
        put(WallOrientation.LEFT, -1);
        put(WallOrientation.UP, -1); //Ceiling
        put(WallOrientation.DOWN, -1); //Floor
    }};

    // There could be multiple wall on each direction. When there is a door the wall is actually two smaller walls
    // with the door in the middle
    private final HashMap<WallOrientation, List<Wall>> walls = new HashMap<>() {{
        put(WallOrientation.FRONT, null);
        put(WallOrientation.BACK, null);
        put(WallOrientation.RIGHT, null);
        put(WallOrientation.LEFT, null);
        put(WallOrientation.UP, null); //Ceiling
        put(WallOrientation.DOWN, null); //Floor
    }};

    private List<Wall> getFrontWalls() {
        return walls.get(WallOrientation.FRONT);
    }

    private List<Wall> getBackWalls() {
        return walls.get(WallOrientation.BACK);
    }

    private List<Wall> getLeftWalls() {
        return walls.get(WallOrientation.LEFT);
    }

    private List<Wall> getRightWalls() {
        return walls.get(WallOrientation.RIGHT);
    }

    private List<Wall> getCeilingWalls() {
        return walls.get(WallOrientation.UP);
    }

    private List<Wall> getFloorWalls() {
        return walls.get(WallOrientation.DOWN);
    }


    public Room(final Integer index, final float scale,
                final int maxPointLights, final int maxSpotLights,
                final int roomWidth, final int roomHeight, final int roomDepth,
                final Map<WallOrientation, Material> materialsMap){

        this.index         = index;
        this.scale         = scale;
        this.roomWidth     = roomWidth;
        this.roomHeight    = roomHeight;
        this.roomDepth     = roomDepth;
        this.materialsMap  = materialsMap;

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

        roomConnections.replace(direction, roomIndex);
    }

    /*
    // Returns vertices, indices and textcoords that create a wall with a door opening on the middle
    private Triple<float[],int[], float[]> createWallWithDoorOpening(final int maxWidthWallSide, final int maxHeightWallSide,
                                                          final int maxFloorSide,
                                                          final int stripWidthDim, final int stripHeightDim,
                                                          final int stripFloorDim,
                                                          final Vector3f fixedPos, final boolean clockWise) {


        final int doorSpaceWidth  = maxWidthWallSide/5;
        final int sideWallWidth   = (maxWidthWallSide - doorSpaceWidth)/2;
        final int newStripWidthDim  = sideWallWidth / numStripsPerWall;

        final Vector3f leftWallPosition = new Vector3f(fixedPos);
        final Vector3f rightWallPosition = new Vector3f(fixedPos);

        // Calculate the position of the left side and right side of the walls to make sure is centered
        if (fixedPos.x !=0) {
            // if x is > 0 means this wall is laying on the x axis since all my wall are straight
            leftWallPosition.z  -= doorSpaceWidth/2;
            rightWallPosition.z += doorSpaceWidth/2;
        } else {
            // walls are sitting or on the x axis or z axis
            leftWallPosition.x  -= doorSpaceWidth/2;
            rightWallPosition.x += doorSpaceWidth/2;
        }

        // A wall with a space with a door will be two full walls with a separation on the middle
        final Triple<float[],int[],float[]> leftSideWallData = createFullWall(sideWallWidth, maxHeightWallSide, maxFloorSide, newStripWidthDim, stripHeightDim,stripFloorDim,
                leftWallPosition, clockWise);
        final Triple<float[],int[],float[]> rightSideWallData = createFullWall(sideWallWidth, maxHeightWallSide, maxFloorSide, newStripWidthDim, stripHeightDim,stripFloorDim,
                rightWallPosition, clockWise);

        // Increase the indices number on for the right side of the wall by the total number of vertices from the left side
        final int[] rightSideIndices = rightSideWallData.getMiddle();
        final int totalLeftSideVertices = leftSideWallData.getLeft().length/3;
        for (int i=0;i<rightSideIndices.length;i++){
            rightSideIndices[i] += totalLeftSideVertices;
        }

        final float[] allVertices  = ArrayUtils.addAll(leftSideWallData.getLeft(), rightSideWallData.getLeft());
        final int[]   allIndices   = ArrayUtils.addAll(leftSideWallData.getMiddle(), rightSideIndices);
        final float[] allTexCoords = ArrayUtils.addAll(leftSideWallData.getRight(), rightSideWallData.getRight());

        return Triple.of(allVertices, allIndices, allTexCoords);
    }


    // Returns vertices, indices and textcoords that create a full wall
    private Triple<float[],int[], float[]> createFullWall(final int maxWidthWallSide, final int maxHeightWallSide,
                                                 final int maxFloorSide,
                                                 final int stripWidthDim, final int stripHeightDim,
                                                 final int stripFloorDim,
                                                 final Vector3f fixedPos, final boolean clockWise) {

        final int numVertices_perWall  = (numStripsPerWall + 1) * (numStripsPerWall + 1); // Per wall
        final int numTriangles_perWall = numStripsPerWall * numStripsPerWall * 2;
        final int numIndices_perWall   = numTriangles_perWall * 3;  // Each triangle has 3 vertices so 3 indices

        float[] vertices = new float[numVertices_perWall*3]; // Each vertex has x, y and z
        int[] indices  = new int[numIndices_perWall];
        float[] texture_coords = new float[numVertices_perWall*2]; // Per vertex I have 2 coordinates (x,y)


        int idxVertex = 0;
        int textCoordsIdx = 0;

        if (fixedPos.z != 0.0f) {
            for (int y = 0; y <= maxHeightWallSide; y += stripHeightDim)
                for (int x = 0; x <= maxWidthWallSide; x += stripWidthDim) {
                    vertices[idxVertex] = x - maxWidthWallSide / 2;
                    vertices[idxVertex + 1] = y;
                    vertices[idxVertex + 2] = fixedPos.z;
                    idxVertex += 3;

                    // Texture Coordinates
                    texture_coords[textCoordsIdx]   =  (float)x/maxWidthWallSide;
                    texture_coords[textCoordsIdx+1] =  (float)y/maxHeightWallSide;
                    textCoordsIdx += 2;
                }
        } else if (fixedPos.x != 0.0f) {
            for (int y = 0; y <= maxHeightWallSide; y += stripHeightDim)
                for (int z = 0; z <= maxFloorSide; z += stripFloorDim) {
                    vertices[idxVertex] = fixedPos.x;
                    vertices[idxVertex + 1] = y;
                    vertices[idxVertex + 2] = z - maxFloorSide / 2;
                    idxVertex += 3;

                    // Texture Coordinates
                    texture_coords[textCoordsIdx]   =  (float)z/maxWidthWallSide;
                    texture_coords[textCoordsIdx+1] =  (float)y/maxHeightWallSide;
                    textCoordsIdx += 2;
                }
        } else { //Ceiling or floor
            for (int z = 0; z <= maxFloorSide; z += stripFloorDim)
                for (int x = 0; x <= maxWidthWallSide; x += stripWidthDim) {
                    vertices[idxVertex] = x - maxWidthWallSide / 2;
                    vertices[idxVertex + 1] = fixedPos.y;
                    vertices[idxVertex + 2] = z - maxFloorSide / 2;
                    idxVertex += 3;

                    texture_coords[textCoordsIdx]   =  (float)x/maxWidthWallSide;
                    texture_coords[textCoordsIdx+1] =  (float)z/maxHeightWallSide;
                    textCoordsIdx += 2;
                }
        }



        int indicesIdx = 0;

        for (int y = 0; y < maxHeightWallSide; y += stripHeightDim) {
            int idx_y = (y/stripHeightDim) * (numStripsPerWall + 1);
            for (int x = 0; x < maxWidthWallSide; x += stripWidthDim) {
                int idx_x = x/stripWidthDim;
                if (!clockWise) {
                    // Tri1
                    indices[indicesIdx] = (idx_x + idx_y + 1);
                    indices[indicesIdx + 1] = (idx_x + idx_y);
                    indices[indicesIdx + 2] = (idx_x + idx_y + (numStripsPerWall + 1));
                    // Tri2
                    indices[indicesIdx + 3] = indices[indicesIdx];
                    indices[indicesIdx + 4] = indices[indicesIdx + 2];
                    indices[indicesIdx + 5] = (idx_x + idx_y + (numStripsPerWall + 2));
                    indicesIdx += 6;

                } else {
                    // Tri1
                    indices[indicesIdx]     = (idx_x + idx_y);
                    indices[indicesIdx + 1] = (idx_x + idx_y + 1);
                    indices[indicesIdx + 2] = (idx_x + idx_y + (numStripsPerWall + 1));
                    // Tri2
                    indices[indicesIdx + 3] = indices[indicesIdx + 1];
                    indices[indicesIdx + 4] = (idx_x + idx_y + (numStripsPerWall + 2));
                    indices[indicesIdx + 5] = indices[indicesIdx + 2];
                    indicesIdx += 6;
                }
            }
        }

        return Triple.of(vertices, indices, texture_coords);
    }


    private float[] calculateNormals(float[] vertices, int[]indices) {

        HashMap<Integer, Vector3f> normalPerVertex = new HashMap<>();

        // Each face is a triangle with three vertices. Each index point to the actual triangle vertex
        for (int idx=0;idx<indices.length;idx+=3){
            Vector3f[] triangle = new Vector3f[3];
            for (int vertIdx=0;vertIdx<3;vertIdx++){
                final int vertexIdx_x = indices[idx + vertIdx]*3;
                final int vertexIdx_y = indices[idx + vertIdx]*3 + 1;
                final int vertexIdx_z = indices[idx + vertIdx]*3 + 2;
                triangle[vertIdx] = new Vector3f(vertices[vertexIdx_x],vertices[vertexIdx_y],vertices[vertexIdx_z]);
            }
            final Vector3f vector1 = triangle[0].sub(triangle[1]);
            final Vector3f vector2 = triangle[2].sub(triangle[1]);
            final Vector3f normal  = vector1.cross(vector2).normalize();


            if (!normalPerVertex.containsKey(indices[idx]*3)) { //Multiply by 3 to transform to vertex space, each index represent 3 vertex positions (x,y,z)
                normalPerVertex.put(indices[idx]*3, normal);
            }

            if (!normalPerVertex.containsKey(indices[idx + 1]*3)) {
                normalPerVertex.put(indices[idx + 1]*3, normal);
            }

            if (!normalPerVertex.containsKey(indices[idx + 2]*3)) {
                normalPerVertex.put(indices[idx + 2]*3, normal);
            }
        }

        // Create the float[]  with all normals for all vertices
        // It has to be in the same order that is on the vertices[] so from 0 .. to length of vertices/3 because each vertex has three components
        float[] normals = new float[vertices.length];
        int idxNormals = 0;
        for (int idx=0;idx<vertices.length;idx+=3){
            normals[idxNormals]   = normalPerVertex.get(idx).x;
            normals[idxNormals+1] = normalPerVertex.get(idx).y;
            normals[idxNormals+2] = normalPerVertex.get(idx).z;
            idxNormals+=3;
        }

        return normals;
    }

    public void createModel(final int maxWidthWallSide, final int maxHeightWallSide, final int floorSize,
                            final Texture frontWallText, final Texture backWallText,
                            final Texture leftWallText, final Texture rightWallText,
                            final Texture floorText, final Texture ceilingText) {

        final int stripWidthDim  = maxWidthWallSide / numStripsPerWall;
        final int stripHeightDim = maxHeightWallSide / numStripsPerWall;
        final int stripFloorDim  = floorSize / numStripsPerWall;
        final int centerX        = maxWidthWallSide/2;
        //final int centerY        = maxHeightWallSide/2;
        final int centerZ        = floorSize/2;


        // Create the walls triangles
        //Triple<float[],int[],float[]> frontWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
        //                                               new Vector3f(0.0f, 0.0f, -centerZ), true);

        Triple<float[],int[],float[]> frontWallData = createWallWithDoorOpening(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                                                       new Vector3f(0.0f, 0.0f, -centerZ), true);

        Triple<float[],int[],float[]> backWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, 0.0f, centerZ), false);

        Triple<float[],int[],float[]> leftWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(-centerX, 0.0f, 0.0f), false);

        Triple<float[],int[],float[]> rightWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(centerX, 0.0f, 0.0f), true);

        Triple<float[],int[],float[]> ceilingWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, maxHeightWallSide, 0.0f), true);

        Triple<float[],int[],float[]> floorWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, 0.0f, 0.0f), false);


        // Calculate Normals
        float[] frontNormals   =  calculateNormals(frontWallData.getLeft(), frontWallData.getMiddle());
        float[] backNormals    =  calculateNormals(backWallData.getLeft(), backWallData.getMiddle());
        float[] leftNormals    =  calculateNormals(leftWallData.getLeft(), leftWallData.getMiddle());
        float[] rightNormals   =  calculateNormals(rightWallData.getLeft(), rightWallData.getMiddle());
        float[] ceilingNormals =  calculateNormals(ceilingWallData.getLeft(), ceilingWallData.getMiddle());
        float[] floorNormals   =  calculateNormals(floorWallData.getLeft(), floorWallData.getMiddle());

        // Create the wall materials
        Material frontMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                                              new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                                              new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                                              frontWallText, 0.1f);
        Material backMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                backWallText, 0.6f);
        Material leftMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                leftWallText, 0.6f);
        Material rightMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                rightWallText, 0.6f);
        Material ceilingMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                ceilingText, 0.6f);
        Material floorMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                floorText, 0.6f);


        // Create the walls  Meshes
        frontWall   = new Mesh(frontWallData.getLeft(), frontWallData.getMiddle(), frontNormals, frontWallData.getRight());
        frontWall.setMaterial(frontMaterial);

        backWall    = new Mesh(backWallData.getLeft(), backWallData.getMiddle(), backNormals, backWallData.getRight());
        backWall.setMaterial(backMaterial);

        leftWall    = new Mesh(leftWallData.getLeft(), leftWallData.getMiddle(), leftNormals, leftWallData.getRight());
        leftWall.setMaterial(leftMaterial);

        rightWall   = new Mesh(rightWallData.getLeft(), rightWallData.getMiddle(), rightNormals, rightWallData.getRight());
        rightWall.setMaterial(rightMaterial);

        ceilingWall = new Mesh(ceilingWallData.getLeft(), ceilingWallData.getMiddle(), ceilingNormals, ceilingWallData.getRight());
        ceilingWall.setMaterial(ceilingMaterial);

        floorWall   = new Mesh(floorWallData.getLeft(), floorWallData.getMiddle(), floorNormals, floorWallData.getRight());
        floorWall.setMaterial(floorMaterial);

    }
    */

    public void init() throws InvalidAttributeValueException {

        for (final WallOrientation dir : WallOrientation.values()){
            if (roomConnections.get(dir) != -1){ //There is a room next to this wall
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

        final int doorEntryWidth = roomWidth / 5;
        final ArrayList<Wall> walls = new ArrayList<>();
        int wallWidth  = (roomWidth - doorEntryWidth) / 2;
        int wallDepth  = (roomDepth - doorEntryWidth) / 2;
        int wallHeight = roomHeight;
        boolean clockwise = true;
        final Vector3f center1 = new Vector3f(0.0f, 0.0f, 0.0f);
        final Vector3f center2 = new Vector3f(0.0f, 0.0f, 0.0f);

        if (direction == WallOrientation.LEFT || direction == WallOrientation.RIGHT) {
            wallWidth = wallDepth;  // When the wall is on the sides the depth of the room determines the width of the wall
            center1.z  = roomDepth/2 - doorEntryWidth/2 - wallWidth/2;
            center2.z  = roomDepth/2 + doorEntryWidth/2 + wallWidth/2;
            if (direction == WallOrientation.RIGHT) clockwise = false;
        } else if (direction == WallOrientation.UP || direction == WallOrientation.DOWN) {
            wallHeight = roomDepth; // For the ceiling and floor the height of the wall is the depth of the room
            center1.z  = roomDepth/2 - doorEntryWidth/2 - wallHeight/2;
            center2.z  = roomDepth/2 + doorEntryWidth/2 + wallHeight/2;
            if (direction == WallOrientation.DOWN) clockwise = false;
        } else if (direction == WallOrientation.FRONT|| direction == WallOrientation.BACK) {
            center1.x  = roomWidth/2 - doorEntryWidth/2 - wallWidth/2;
            center2.x  = roomWidth/2 + doorEntryWidth/2 + wallWidth/2;
            if (direction == WallOrientation.BACK) clockwise = false;
        }

        walls.add( new Wall(wallWidth, wallHeight, numStripsPerWall, direction, clockwise,
                center1, materialsMap.get(direction)));
        walls.add( new Wall(wallWidth, wallHeight, numStripsPerWall, direction, clockwise,
                center2, materialsMap.get(direction)));

        return walls;
    }

    private List<Wall> getWallWithNoDoor(WallOrientation direction) throws InvalidAttributeValueException {

        final ArrayList<Wall> walls = new ArrayList<>();
        int wallWidth  = roomWidth;
        int wallHeight = roomHeight;
        boolean clockwise = true;
        final Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);

        if (direction == WallOrientation.LEFT || direction == WallOrientation.RIGHT) {
            wallWidth = roomDepth;  // When the wall is on the sides the depth of the room determines the width of the wall
            center.z = roomDepth/2;
            if (direction == WallOrientation.RIGHT) clockwise = false;
        } else if (direction == WallOrientation.UP || direction == WallOrientation.DOWN) {
            wallHeight = roomDepth; // For the ceiling and floor the height of the wall is the depth of the room
            center.z = roomDepth/2;
            if (direction == WallOrientation.DOWN) clockwise = false;
        } else if (direction == WallOrientation.FRONT|| direction == WallOrientation.BACK) {
            center.x = roomWidth/2;
            if (direction == WallOrientation.BACK) clockwise = false;
        }

        walls.add( new Wall(wallWidth, wallHeight, numStripsPerWall, direction, clockwise,
                           center, materialsMap.get(direction)));

        return walls;
    }

    public void cleanUp() {
        for (WallOrientation dir : WallOrientation.values()){
            List<Wall> selectedWalls = walls.get(dir);
            for (Wall wall : selectedWalls)
                wall.cleanUp();
        }
    }

    // the shader program is expected to have the following uniforms
    // material
    public void render(ShaderProgram shaderProgram, Matrix4f viewMatrix) {

        PointLight[] currPointLights = new PointLight[pointLights.length];
        SpotLight[]  currSpotLights  = new SpotLight[spotLights.length];


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


        // Send Spot Light data to the shader
        for (int i=0;i<spotLights.length;i++){
            // Convert SpotLights rotation into view(camera) coordinates. We don't
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

}
