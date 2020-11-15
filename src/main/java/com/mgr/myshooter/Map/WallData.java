package com.mgr.myshooter.Map;

import org.joml.Planef;

public class WallData {

    private final float[] vertices;
    private final int[] indices;
    private final float[] textCoords;
    private final float[] normals;

    public WallData(final float[] vertices, final int[] indices, final float[] textCoords, final float[] normals){
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
