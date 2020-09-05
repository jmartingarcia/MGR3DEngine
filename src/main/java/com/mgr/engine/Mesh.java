package com.mgr.engine;

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

public class Mesh {

    private final int vaoId;

    private final List<Integer> vboIdList;

    private final int vertexCount;

    private Material material;

    private Vector3f color = new Vector3f(0.0f, 1.0f, 0.0f);



    public Mesh(float[] positions, int[] indices, float[] normals, float[] textCoords) {

        vboIdList = new ArrayList<>();

        FloatBuffer posBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        FloatBuffer normalsBuffer = null;

        try {
            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int posVboId = glGenBuffers();
            vboIdList.add(posVboId);
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
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
            int normalVboId = glGenBuffers();
            vboIdList.add(normalVboId);
            normalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);


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

    public int getVertexCount() {

        return vertexCount;
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

        material.cleanup();
    }

    public void render() {
        Texture texture = material.getTexture();
        if (material.isTextured()) {
            // Activate firs texture bank
            glActiveTexture(GL_TEXTURE0);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glDrawElements(GL43.GL_TRIANGLES, vertexCount, GL43.GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
