package com.mgr.engine;

import org.javatuples.Pair;
import org.joml.Vector2f;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Room {

    private final int numStripsPerWall = 3;


    // There is a mesh per wall side, floor and ceiling
    private Mesh frontWall;
    private Mesh backWall;
    private Mesh leftWall;
    private Mesh rightWall;
    private Mesh ceilingWall;
    private Mesh floorWall;


    private int index;
    private String type;

    public int getIndex() {
        return index;
    }

    public String getType() {

        return type;
    }


    private Vector2f mapPosition;  // Contains the position of the room on the map grid

    public void setMapPosition(int posX, int posY) {

        mapPosition = new Vector2f(posX, posY);
    }


    private float scale;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }


    private Vector3f worldPosition;  // The position in World Coordinates

    public Vector3f getWorldPosition() {
        return worldPosition;
    }



    private final HashMap<String, Integer> roomConnections = new HashMap<String, Integer>() {{
        put("N", -1);
        put("S", -1);
        put("E", -1);
        put("W", -1);
    }};



    public Room(Integer index, float scale){

        this.index = index;
        this.scale = scale;
    }

    public void setPathWithRoomIndex(String path, Integer roomIndex){

        roomConnections.replace(path, roomIndex);
    }

    private void calcWorldPositionFromMapPosition(final int maxWidthWallSide, final int maxHeightWallSide, final int centerX, final int centerY, final int centerZ) {

        if (mapPosition == null) {
            throw new NullPointerException("Map Position value not set");
        }

        // The room are squares (for now)
        // The map position is a vector2f with x and y field but the y represents the z coordinate.
        // For now, rooms are flat at 0.0f on Y
        //worldPosition = new Vector3f(centerX + (mapPosition.x * maxWallSide), centerY, centerZ + (mapPosition.y * maxWallSide));
        worldPosition = new Vector3f(0.0f, 0.0f, -5.0f);
    }

    // Returns vertices and indices that create a full wall
    private Pair<float[],int[]> createFullWall(final int maxWidthWallSide, final int maxHeightWallSide,
                                               final int maxFloorSide,
                                               final int stripWidthDim, final int stripHeightDim,
                                               final int stripFloorDim,
                                               final Vector3f fixedPos, final boolean clockWise) {

        final int numVertices_perWall  = (numStripsPerWall + 1) * (numStripsPerWall + 1); // Per wall
        final int numTriangles_perWall = numStripsPerWall * numStripsPerWall * 2;
        final int numIndices_perWall   = numTriangles_perWall * 3;  // Each triangle has 3 vertices so 3 indices

        float[] vertices = new float[numVertices_perWall*3]; // Each vertex has x, y and z
        int[] indices  = new int[numIndices_perWall];


        int idxVertex = 0;

        if (fixedPos.z != 0.0f) {
            for (int y = 0; y <= maxHeightWallSide; y += stripHeightDim)
                for (int x = 0; x <= maxWidthWallSide; x += stripWidthDim) {
                    vertices[idxVertex] = x - maxWidthWallSide / 2;
                    vertices[idxVertex + 1] = y;
                    vertices[idxVertex + 2] = fixedPos.z;
                    idxVertex += 3;
                }
        } else if (fixedPos.x != 0.0f) {
            for (int y = 0; y <= maxHeightWallSide; y += stripHeightDim)
                for (int z = 0; z <= maxFloorSide; z += stripFloorDim) {
                    vertices[idxVertex] = fixedPos.x;
                    vertices[idxVertex + 1] = y;
                    vertices[idxVertex + 2] = z - maxFloorSide / 2;
                    idxVertex += 3;
                }
        } else { //Ceiling or floor
            for (int z = 0; z <= maxFloorSide; z += stripFloorDim)
                for (int x = 0; x <= maxWidthWallSide; x += stripWidthDim) {
                    vertices[idxVertex] = x - maxWidthWallSide / 2;
                    vertices[idxVertex + 1] = fixedPos.y;
                    vertices[idxVertex + 2] = z - maxFloorSide / 2;
                    idxVertex += 3;
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

        return new Pair<>(vertices, indices);
    }


    public void createModel(final int maxWidthWallSide, final int maxHeightWallSide, final int floorSize) {

        final int stripWidthDim  = maxWidthWallSide / numStripsPerWall;
        final int stripHeightDim = maxHeightWallSide / numStripsPerWall;
        final int stripFloorDim = floorSize / numStripsPerWall;
        final int centerX        = maxWidthWallSide/2;
        final int centerY        = maxHeightWallSide/2;
        final int centerZ        = floorSize/2;

        // Calculate the wall position on world coordinates
        calcWorldPositionFromMapPosition(maxWidthWallSide, maxHeightWallSide, centerX, centerY, centerZ);

        Pair<float[],int[]> frontWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                                                       new Vector3f(0.0f, 0.0f, -centerZ), false);

        Pair<float[],int[]> backWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, 0.0f, centerZ), true);

        Pair<float[],int[]> leftWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(-centerX, 0.0f, 0.0f), true);

        Pair<float[],int[]> rightWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(centerX, 0.0f, 0.0f), false);

        Pair<float[],int[]> ceilingWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, maxHeightWallSide, 0.0f), false);

        Pair<float[],int[]> floorWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, 0.0f, 0.0f), true);


        frontWall   = new Mesh(frontWallData.getValue0(), frontWallData.getValue1());
        backWall    = new Mesh(backWallData.getValue0(), backWallData.getValue1());
        leftWall    = new Mesh(leftWallData.getValue0(), leftWallData.getValue1());
        rightWall   = new Mesh(rightWallData.getValue0(), rightWallData.getValue1());
        ceilingWall = new Mesh(ceilingWallData.getValue0(), ceilingWallData.getValue1());
        floorWall   = new Mesh(floorWallData.getValue0(), floorWallData.getValue1());

    }


    public void cleanUp() {
        frontWall.cleanUp();
        backWall.cleanUp();
        leftWall.cleanUp();
        rightWall.cleanUp();
        ceilingWall.cleanUp();
        floorWall.cleanUp();
    }

    public void render() {
        frontWall.render();
        backWall.render();
        leftWall.render();
        rightWall.render();
        ceilingWall.render();
        floorWall.render();
    }

}
