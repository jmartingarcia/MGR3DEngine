package com.mgr.myshooter.Map;

public class WallData {

    private final float[] vertices;
    private final int[] indices;
    private final float[] textCoords;
    private final float[] normals;

    public WallData(float[] vertices, int[] indices, float[] textCoords, float[] normals){
        this.vertices = vertices;
        this.indices = indices;
        this.textCoords = textCoords;
        this.normals = normals;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTextCoords() {
        return textCoords;
    }

    public float[] getNormals() {
        return normals;
    }
}
