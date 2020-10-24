package com.mgr.myshooter.Map;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

import javax.management.InvalidAttributeValueException;
import java.util.HashMap;

public class Wall extends Mesh {

    private final int width;
    private final int height;
    private final Vector3f center;
    private final int numberStrips;
    private final int stripWidthDim;
    private final int stripHeightDim;
    private final int numVertices_perWall;
    private final int numTriangles_perWall;
    private final int numIndices_perWall;
    private final WallOrientation orientation;
    private final boolean clockwise;


    /*
     Creates the mesh of a wall
     For simplicity we expect everything to be integers. This means the width and height should be divisible by the
     number of strips.

     @param width Integer defining the width dimension of the wall
     @param height Integer defining the height dimension of the wall
     @param numberStrips The wall is created using a strip of triangles. This Integer defines how many strips to divide the wall.
     @param center The center of the wall
     */
    public  Wall(final int width, final int height, final int numberStrips, final WallOrientation orientation,
                 final boolean clockwise, final Vector3f center, final Material material) throws InvalidAttributeValueException{

        this.width = width;
        this.height = height;
        this.center = center;
        this.numberStrips = numberStrips;
        stripWidthDim  = width / numberStrips;
        stripHeightDim = height / numberStrips;
        this.orientation = orientation;
        this.clockwise = clockwise;
        this.material = material;


        //Validate dimensions are valid and divisible by the number of strips
        if (width % numberStrips !=0 || height % numberStrips !=0)
            throw new InvalidAttributeValueException("Wall dimensions should be divisible by the number of strips");

        numVertices_perWall  = (numberStrips + 1) * (numberStrips + 1); // Per wall
        numTriangles_perWall = numberStrips * numberStrips * 2;
        numIndices_perWall   = numTriangles_perWall * 3;  // Each triangle has 3 vertices so 3 indices


        WallData wallData = createFullWall();

        init(wallData.getVertices(), wallData.getIndices(), wallData.getNormals(), wallData.getTextCoords());
    }


    private Pair<float[], float[]> createWallVerticesAndTextCoords(final int stripWidthDim, final int stripHeightDim) {

        final float[] vertices  = new float[numVertices_perWall*3]; // Each vertex has x, y and z
        final float[] texture_coords = new float[numVertices_perWall*2]; // Per vertex,  2 coordinates (x,y)

        int idxVertex = 0;
        int textCoordsIdx = 0;

        if (orientation == WallOrientation.FRONT || orientation == WallOrientation.BACK) {
            for (int y = -height/2; y <= height/2; y += stripHeightDim)
                for (int x = -width/2; x <= width/2; x += stripWidthDim) {
                    vertices[idxVertex] = center.x + x;
                    vertices[idxVertex + 1] = center.y + y;
                    vertices[idxVertex + 2] = center.z;
                    idxVertex += 3;

                    // Texture Coordinates
                    texture_coords[textCoordsIdx]   =  (float)x/width;
                    texture_coords[textCoordsIdx+1] =  (float)y/height;
                    textCoordsIdx += 2;
                }
        } else if (orientation == WallOrientation.LEFT || orientation == WallOrientation.RIGHT) {
            for (int y = -height/2; y <= height/2; y += stripHeightDim)
                for (int z = -width/2; z <= width/2; z += stripWidthDim) {
                    vertices[idxVertex] = center.x;
                    vertices[idxVertex + 1] = center.y + y;
                    vertices[idxVertex + 2] = center.z + z;
                    idxVertex += 3;

                    // Texture Coordinates
                    texture_coords[textCoordsIdx]   =  (float)z/width;
                    texture_coords[textCoordsIdx+1] =  (float)y/height;
                    textCoordsIdx += 2;
                }
        } else { //Ceiling or floor (orientation == WallOrientation.UP || orientation == WallOrientation.DOWN)
            for (int z = -height/2; z <= height/2; z += stripHeightDim)
                for (int x = -width/2; x <= width/2; x += stripWidthDim) {
                    vertices[idxVertex] = center.x + x;
                    vertices[idxVertex + 1] = center.y;
                    vertices[idxVertex + 2] = center.z + z;
                    idxVertex += 3;

                    texture_coords[textCoordsIdx]   =  (float)x/width;
                    texture_coords[textCoordsIdx+1] =  (float)z/height;
                    textCoordsIdx += 2;
                }
        }

        return Pair.of(vertices, texture_coords);
    }


    private int[] calculateWallIndices(){

        final int[] indices     = new int[numIndices_perWall];

        int indicesIdx = 0;

        for (int y = 0; y < height; y += stripHeightDim) {
            int idx_y = (y/stripHeightDim) * (numberStrips + 1);
            for (int x = 0; x < width; x += stripWidthDim) {
                int idx_x = x/stripWidthDim;
                if (!clockwise) {
                    // Tri1
                    indices[indicesIdx] = (idx_x + idx_y + 1);
                    indices[indicesIdx + 1] = (idx_x + idx_y);
                    indices[indicesIdx + 2] = (idx_x + idx_y + (numberStrips + 1));
                    // Tri2
                    indices[indicesIdx + 3] = indices[indicesIdx];
                    indices[indicesIdx + 4] = indices[indicesIdx + 2];
                    indices[indicesIdx + 5] = (idx_x + idx_y + (numberStrips + 2));
                    indicesIdx += 6;

                } else {
                    // Tri1
                    indices[indicesIdx]     = (idx_x + idx_y);
                    indices[indicesIdx + 1] = (idx_x + idx_y + 1);
                    indices[indicesIdx + 2] = (idx_x + idx_y + (numberStrips + 1));
                    // Tri2
                    indices[indicesIdx + 3] = indices[indicesIdx + 1];
                    indices[indicesIdx + 4] = (idx_x + idx_y + (numberStrips + 2));
                    indices[indicesIdx + 5] = indices[indicesIdx + 2];
                    indicesIdx += 6;
                }
            }
        }

        return indices;

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

    private WallData createFullWall() {

        final Pair<float[], float[]> wallVertices = createWallVerticesAndTextCoords(stripWidthDim, stripHeightDim);

        final float[] vertices   = wallVertices.getLeft();
        final float[] textCoords = wallVertices.getRight();
        final int[]   indices    = calculateWallIndices();
        final float[] normals    = calculateNormals(vertices, indices);

        return new WallData(vertices,indices,textCoords,normals);

    }
}
