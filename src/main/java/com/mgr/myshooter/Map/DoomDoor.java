package com.mgr.myshooter.Map;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.ShaderProgram;
import com.mgr.engine.items.GameItem;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.naming.directory.InvalidAttributeValueException;
import java.util.HashMap;

public class DoomDoor extends GameItem {

    private final WallOrientation doorOrientation;
    private final int numVertices_perDoor;
    private final int numTriangles_perDoor;
    private final int numIndices_perDoor;
    private DoorStatus status;
    private float remainOpenSeconds;
    private float elapsedTime;
    private final Vector3f center;



    public DoomDoor(final Vector3f center, final float width, final float height, final float depth, final Material material,
                    final WallOrientation doorOrientation) throws InvalidAttributeValueException {

        setPosition(center);
        this.center = new Vector3f(center);
        this.doorOrientation = doorOrientation;
        this.width = width;
        this.height = height;
        this.depth = depth;
        status = DoorStatus.CLOSED;
        radius = 2.0f;

        speed = 0.5f; // 12 inches per second
        remainOpenSeconds = 8;
        elapsedTime = 0;

        // A door is basically a rectangle with some depth
        numVertices_perDoor  = 8;
        numTriangles_perDoor = 12;
        numIndices_perDoor   = 36;

        final Pair<float[], float[]> doorCoords = createDoorVerticesAndTextCoords();
        final int[] indices = calculateDoorIndices();
        final float[] normals = calculateNormals(doorCoords.getLeft(), indices);

        mesh = new Mesh();
        mesh.setMaterial(material);
        mesh.init(doorCoords.getLeft(), indices, normals, doorCoords.getRight());

    }

    private float[] getCubeVertices(final float width, final float height, final float depth) {

        // All faces should be counter-clockwise
        // Indices on the vertices array go 0, 3, 6, etc ... because each vertex has 3 coordinates: x,y and z
            /*
                      12               15
                        \              /
                         X------------X
                   0    /|      9    /|
                     \ / |       \  / |
                      X-----------X   |
                      |  X--------|-- X
                      | / \       |  / \
                      |/  21      | /   18
                      X-----------X
                     /             \
                   3                6
             */

        final float[] vertices       = new float[numVertices_perDoor*3]; // Each vertex has x, y and z

        // Front face
        vertices[0] = - width/2;
        vertices[1] = - height/2;
        vertices[2] = depth/2;

        vertices[3] = - width/2;
        vertices[4] = height/2;
        vertices[5] = depth/2;

        vertices[6] = width/2;
        vertices[7] = height/2;
        vertices[8] = depth/2;

        vertices[9] = width/2;
        vertices[10] = - height/2;
        vertices[11] = depth/2;

        // Back Face
        vertices[12] = - width/2;
        vertices[13] = - height/2;
        vertices[14] = - depth/2;

        vertices[15] = width/2;
        vertices[16] = - height/2;
        vertices[17] = - depth/2;

        vertices[18] = width/2;
        vertices[19] = height/2;
        vertices[20] = - depth/2;

        vertices[21] = - width/2;
        vertices[22] = height/2;
        vertices[23] = - depth/2;

        return vertices;
    }

    private Pair<float[], float[]> createDoorVerticesAndTextCoords()  {

        float[] vertices       = null;
        final float[] texture_coords = new float[numVertices_perDoor*2]; // Per vertex,  2 coordinates (x,y)


        if (doorOrientation == WallOrientation.LEFT || doorOrientation == WallOrientation.RIGHT) {
            // when the door is on the left or right side, the width and depth are switched
            final float tempNumber = depth;
            depth = width;
            width = tempNumber;
        }

        vertices = getCubeVertices(width, height, depth);


        // Texture Coordinates - FRONT FACE
        texture_coords[0]   =  0.0f;
        texture_coords[1]   =  1.0f;

        texture_coords[2]   =  0.0f;
        texture_coords[3]   =  0.0f;

        texture_coords[4]   =  1.0f;
        texture_coords[5]   =  0.0f;

        texture_coords[6]   =  1.0f;
        texture_coords[7]   =  1.0f;

        // Texture Coordinates - BACK FACE
        texture_coords[8]   =  1.0f;
        texture_coords[9]   =  1.0f;

        texture_coords[10]   =  0.0f;
        texture_coords[11]   =  1.0f;

        texture_coords[12]   =  0.0f;
        texture_coords[13]   =  0.0f;

        texture_coords[14]   =  1.0f;
        texture_coords[15]   =  0.0f;

        return Pair.of(vertices, texture_coords);
    }

    private int[] calculateDoorIndices() {

        final int[] indices     = new int[numIndices_perDoor];

        // Front face
        indices[0] = 1;
        indices[1] = 0;
        indices[2] = 2;
        indices[3] = 3;
        indices[4] = 2;
        indices[5] = 0;

        // Back face
        indices[6] = 4;
        indices[7] = 7;
        indices[8] = 6;
        indices[9] = 4;
        indices[10] = 6;
        indices[11] = 5;

        // Left face
        indices[12] = 1;
        indices[13] = 7;
        indices[14] = 4;
        indices[15] = 4;
        indices[16] = 0;
        indices[17] = 1;

        // Right face
        indices[18] = 6;
        indices[19] = 2;
        indices[20] = 3;
        indices[21] = 6;
        indices[22] = 3;
        indices[23] = 5;

        // Bottom face
        indices[24] = 0;
        indices[25] = 4;
        indices[26] = 3;
        indices[27] = 4;
        indices[28] = 5;
        indices[29] = 3;

        // Top face
        indices[30] = 7;
        indices[31] = 1;
        indices[32] = 2;
        indices[33] = 6;
        indices[34] = 7;
        indices[35] = 2;

        return indices;
    }

    private float[] calculateNormals(final float[] vertices, final int[]indices) {

        HashMap<Integer, Vector3f> normalPerVertex = new HashMap<>();

        // Each face is a triangle with three vertices. Each index point to the actual triangle vertex
        for (int idx=0;idx<indices.length;idx+=3){
            Vector3f[] triangle = new Vector3f[3];
            for(int idx_tri=0;idx_tri<3;idx_tri++) {
                triangle[idx_tri] = new Vector3f(vertices[indices[idx+idx_tri]*3], vertices[indices[idx+idx_tri]*3 + 1], vertices[indices[idx+idx_tri]*3 + 2]);
            }

            final Vector3f vector1 = triangle[0].sub(triangle[1]);
            final Vector3f vector2 = triangle[2].sub(triangle[1]);
            final Vector3f normal  = vector1.cross(vector2).normalize();

            if (!normalPerVertex.containsKey(indices[idx]*3)) {
                normalPerVertex.put(indices[idx]*3, normal);
            }
            if (!normalPerVertex.containsKey(indices[idx+1]*3)) {
                normalPerVertex.put(indices[idx+1]*3, normal);
            }
            if (!normalPerVertex.containsKey(indices[idx+2]*3)) {
                normalPerVertex.put(indices[idx+2]*3, normal);
            }

        }

        // Create the float[]  with all normals for all vertices
        // It has to be in the same order that is on the vertices[] so from 0 .. to length of vertices/3 because each vertex has three components
        final float[] normals = new float[vertices.length];
        int idxNormals = 0;
        for (int idx=0;idx<vertices.length;idx+=3){
            normals[idxNormals]   = normalPerVertex.get(idx).x;
            normals[idxNormals+1] = normalPerVertex.get(idx).y;
            normals[idxNormals+2] = normalPerVertex.get(idx).z;
            idxNormals+=3;
        }

        return normals;
    }

    public void open() {
        status = DoorStatus.OPENING;
        elapsedTime = 0;
    }

    public void close() {
        status = DoorStatus.CLOSING;
    }

    public void update(final float interval) {
        // If status is CLOSED, nothing to do
        if (status == DoorStatus.CLOSED) return;

        // If door is opened, it should stay opened for "remainOpenTime" seconds
        if (status == DoorStatus.OPEN) {
            elapsedTime += interval;
            if (elapsedTime >= remainOpenSeconds) close();
            return;
        }

        // The door opens up at a specific speed of inches per second
        float direction_y = 1.0f;
        if (status == DoorStatus.CLOSING) direction_y = -1.0f;

        // If it's opening the direction will be positive and moving up
        position.y += (direction_y*speed);

        // If the center (position) of the door is all the way to the top then the door is completely open
        // also, if the center is at the center of door then the door is closed
        if (position.y >= (center.y + height - 1.0f))  {
            status = DoorStatus.OPEN;
            position.y = center.y + height - 1.0f;
        }
        else if (position.y <= center.y) {
            status = DoorStatus.CLOSED;
            position.y = center.y;
        }

    }

    // the shader program is expected to have the following uniforms
    // material
    public void render(final ShaderProgram shaderProgram, final Matrix4f viewMatrix) {
        shaderProgram.setUniform("material", getMaterial());
        render();
    }

    public boolean canPlayerInteractAtDistance(final float playerDistance) {
         return (playerDistance<=interactionDistance);
    }

    public void changeStatus() {
        if (status == DoorStatus.CLOSED) open();
        else if (status == DoorStatus.OPEN) close();
    }

}
