package com.mgr.engine;

import com.mgr.engine.collision.BoundingBox;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengles.GLES20.GL_TEXTURE1;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;

    protected int vaoId;
    protected final List<Integer> vboIdList;
    protected int vertexCount;
    protected Material material;
    protected Vector3f color;
    protected BoundingBox box;
    protected int numberVertices;
    protected int numberIndices;


    public Mesh() {
        vboIdList = new ArrayList<>();
        color = new Vector3f(0.0f, 1.0f, 0.0f);
        box = new BoundingBox();
    }


    public void init(float[] vertices, int[] indices, float[] normals, float[] textCoords, int[] bones, float[] weights) {

        numberVertices = vertices.length/3; //x,y,z makes one vertex
        numberIndices  = indices.length;

        // Calculate the bounding box
        for (int i=0;i<vertices.length;i+=3){
            final Vector3f point = new Vector3f(vertices[i+0],vertices[i+1],vertices[i+2]);
            box.add(point);
        }

        FloatBuffer posBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        FloatBuffer normalsBuffer = null;
        FloatBuffer weightsBuffer = null;
        IntBuffer bonesIndicesBuffer = null;


        try {
            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int posVboId = glGenBuffers();
            vboIdList.add(posVboId);
            posBuffer = MemoryUtil.memAllocFloat(vertices.length);
            posBuffer.put(vertices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Index VBO
            int idxVboId = glGenBuffers();
            vboIdList.add(idxVboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            // Normals VBO
            if (normals.length > 0) {
                int normalVboId = glGenBuffers();
                vboIdList.add(normalVboId);
                normalsBuffer = MemoryUtil.memAllocFloat(normals.length);
                normalsBuffer.put(normals).flip();
                glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
                glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
            }

            // Texture coordinates VBO
            if (textCoords.length > 0) {
                int textboId = glGenBuffers();
                vboIdList.add(textboId);
                textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
                textCoordsBuffer.put(textCoords).flip();
                glBindBuffer(GL_ARRAY_BUFFER, textboId);
                glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            }

            // Weights
            if (weights != null && weights.length > 0) {
                int weightsVboId = glGenBuffers();
                vboIdList.add(weightsVboId);
                weightsBuffer = MemoryUtil.memAllocFloat(weights.length);
                weightsBuffer.put(weights).flip();
                glBindBuffer(GL_ARRAY_BUFFER, weightsVboId);
                glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(3);
                glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);
            }


            // Joint indices
            if (bones != null && bones.length > 0) {
                int bonesVboId = glGenBuffers();
                vboIdList.add(bonesVboId);
                bonesIndicesBuffer = MemoryUtil.memAllocInt(bones.length);
                bonesIndicesBuffer.put(bones).flip();
                glBindBuffer(GL_ARRAY_BUFFER, bonesVboId);
                glBufferData(GL_ARRAY_BUFFER, bonesIndicesBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(4);
                glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);
            }

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }

            if (textCoordsBuffer != null){
                MemoryUtil.memFree(textCoordsBuffer);
            }

            if (normalsBuffer != null) {
                MemoryUtil.memFree(normalsBuffer);
            }

            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }


    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public int getVaoId() {

        return vaoId;
    }

    public int getNumberVertices() {
        return numberVertices;
    }

    public int getNumberIndices() {
        return numberIndices;
    }

    public BoundingBox getBoundingBoxCopy() {
        // Return a copy of the bounding box since I don't want it to be modified
        return new BoundingBox(box.getMinVertex(), box.getMaxVertex());
    }

    public boolean isTextured() {
        return this.material.isTextured();
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        for (Integer vbId : vboIdList) {
            glDeleteBuffers(vbId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render() {

        Texture texture = material.getTexture();
        if (material.isTextured()) {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE0);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, texture.getId());


            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }

        Texture normalMap = material.getNormalMap();
        if ( normalMap != null ) {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE1);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, normalMap.getId());
        }

        glBindVertexArray(vaoId);
        //glEnableVertexAttribArray(0);
        glDrawElements(GL43.GL_TRIANGLES, vertexCount, GL43.GL_UNSIGNED_INT, 0);
        //glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
