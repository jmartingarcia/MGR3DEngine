package com.mgr.myshooter;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.ShaderProgram;
import com.mgr.engine.Texture;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Vector;

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
        int normalIdx = 0;
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
        final int stripFloorDim = floorSize / numStripsPerWall;
        final int centerX        = maxWidthWallSide/2;
        final int centerY        = maxHeightWallSide/2;
        final int centerZ        = floorSize/2;

        // Calculate the wall position on world coordinates
        calcWorldPositionFromMapPosition(maxWidthWallSide, maxHeightWallSide, centerX, centerY, centerZ);

        // Create the walls triangles
        Triple<float[],int[],float[]> frontWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                                                       new Vector3f(0.0f, 0.0f, -centerZ), false);

        Triple<float[],int[],float[]> backWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, 0.0f, centerZ), true);

        Triple<float[],int[],float[]> leftWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(-centerX, 0.0f, 0.0f), true);

        Triple<float[],int[],float[]> rightWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(centerX, 0.0f, 0.0f), false);

        Triple<float[],int[],float[]> ceilingWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, maxHeightWallSide, 0.0f), false);

        Triple<float[],int[],float[]> floorWallData = createFullWall(maxWidthWallSide, maxHeightWallSide, floorSize, stripWidthDim, stripHeightDim,stripFloorDim,
                new Vector3f(0.0f, 0.0f, 0.0f), true);


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


    public void cleanUp() {
        frontWall.cleanUp();
        backWall.cleanUp();
        leftWall.cleanUp();
        rightWall.cleanUp();
        ceilingWall.cleanUp();
        floorWall.cleanUp();
    }

    // the shader program is expected to have the following uniforms
    // color
    // useColor (Y if is not textured)
    // material
    public void render(ShaderProgram shaderProgram) {

        //Front Wall
        shaderProgram.setUniform("material", frontWall.getMaterial());
        frontWall.render();

        //Back Wall
        shaderProgram.setUniform("material", backWall.getMaterial());
        backWall.render();

        //Left Wall
        shaderProgram.setUniform("material", leftWall.getMaterial());
        leftWall.render();

        //Right wall
        shaderProgram.setUniform("material", rightWall.getMaterial());
        rightWall.render();

        //Ceiling wall
        shaderProgram.setUniform("material", ceilingWall.getMaterial());
        ceilingWall.render();

        //Floor wall
        shaderProgram.setUniform("material", floorWall.getMaterial());
        floorWall.render();
    }

}
