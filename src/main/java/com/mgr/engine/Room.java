package com.mgr.engine;

import org.joml.Vector2f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Room {

    private final int numStripsPerWall = 3;
    private final int VAO_VERTICES_INDEX = 0;

    private Mesh model;

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

    private final HashMap<String, Integer> roomConnections = new HashMap<String, Integer>() {{
        put("N", -1);
        put("S", -1);
        put("E", -1);
        put("W", -1);
    }};



    public Room(Integer index){

        this.index = index;
    }

    public void setPathWithRoomIndex(String path, Integer roomIndex){

        roomConnections.replace(path, roomIndex);
    }

    public void createModel(final int maxWallSide) {

        final int stripDim = maxWallSide / numStripsPerWall;
        final int numVertices  = (numStripsPerWall + 1) * (numStripsPerWall + 1);
        final int numTriangles = numStripsPerWall * numStripsPerWall * 2;
        final int numIndices   = numTriangles * 3;  // Each triangle has 3 vertices so 3 indices
        final int centerX      = maxWallSide/2;
        final int centerY      = maxWallSide/2;
        final int centerZ      = maxWallSide/2;
        final float[] vertices = new float[numVertices*3]; // each coordinate x,y,z uses one space
        final int[] indices    = new int[numIndices];

        // Front Wall - Vertices
        int idxVertex = 0;
        for (int y = 0; y <= maxWallSide; y += stripDim)
            for (int x = 0; x <= maxWallSide; x += stripDim) {
                vertices[idxVertex]     = x - centerX;
                vertices[idxVertex + 1] = y - centerY;
                vertices[idxVertex + 2] = -50.0f;
                idxVertex += 3;
            }

        // Front Wall - Indices
        int indicesIdx = 0;
        // For each square
        for (int y = 0; y < maxWallSide; y += stripDim) {

            int idx_y = (y/stripDim) * (numStripsPerWall + 1);

            for (int x = 0; x < maxWallSide; x += stripDim) {

                int idx_x = x/stripDim;

                // Tri1
                indices[indicesIdx]     = (idx_x + idx_y + 1);
                indices[indicesIdx + 1] = (idx_x + idx_y);
                indices[indicesIdx + 2] = (idx_x + idx_y + 4);

                // Tri2
                indices[indicesIdx + 3] = indices[indicesIdx];
                indices[indicesIdx + 4] = indices[indicesIdx + 2];
                indices[indicesIdx + 5] = (idx_x + idx_y + 5);

                indicesIdx += 6;
            }
        }


        model = new Mesh(vertices, indices);

    }

    public void drawRoom(){

        GL43.glBindVertexArray(model.getVaoId());
        GL43.glEnableVertexAttribArray(VAO_VERTICES_INDEX);
        GL43.glDrawElements(GL43.GL_TRIANGLES, model.getVertexCount(), GL43.GL_UNSIGNED_INT, 0);
        GL43.glDisableVertexAttribArray(VAO_VERTICES_INDEX);
        GL43.glBindVertexArray(0);
    }


    public void cleanUp() {
        model.cleanUp();
    }

}
