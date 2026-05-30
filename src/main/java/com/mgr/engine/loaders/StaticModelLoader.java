package com.mgr.engine.loaders;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.Texture;
import com.mgr.engine.Utils;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class StaticModelLoader {

    public static Mesh[] load(final String resourcePath, final String texturesDir) throws Exception {
        return load(resourcePath, texturesDir, aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals);
    }

    public static Mesh[] load(final String resourcePath, final String texturesDir, final int flags) throws Exception {

        final AIScene aiScene = aiImportFile(resourcePath, flags);
        if (aiScene == null) {
            throw new Exception("Error loading model");
        }

        // Create Materials master list
        final int numMaterials = aiScene.mNumMaterials();
        final PointerBuffer aiMaterials = aiScene.mMaterials();
        final List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            final AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesDir);
        }

        // Create meshes (models)
        final int numMeshes = aiScene.mNumMeshes();
        final PointerBuffer aiMeshes = aiScene.mMeshes();
        final Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            final AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            final Mesh mesh = processMesh(aiMesh, materials);
            meshes[i] = mesh;
        }

        return meshes;
    }

    protected static void processMaterial(final AIMaterial aiMaterial, final List<Material> materials, final String texturesDir) throws Exception {

        AIColor4D color = AIColor4D.create();

        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture texture = null;
        if (textPath != null && textPath.length() > 0) {
            final TextureCache textCache = TextureCache.getInstance();
            texture = textCache.getTexture(texturesDir + "\\" + textPath);
        }

        Vector4f ambient = Material.DEFAULT_COLOR;
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);
        if (result == 0) {
            ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f diffuse = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
        if (result == 0) {
            diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f specular = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);
        if (result == 0) {
            specular = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Material material = new Material(ambient, diffuse, specular, 1.0f);
        material.setTexture(texture);
        materials.add(material);
    }

    protected static void processVertices(AIMesh aiMesh, List<Float> vertices) {
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }
    }

    protected static void processNormals(final AIMesh aiMesh, final List<Float> normals) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        if (aiNormals == null) return;
        while (aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }
    }

    protected static void processTextCoords(final AIMesh aiMesh, final List<Float> textCoords) {
       if (aiMesh.mNumUVComponents(0) == 0) return; // Exit if no texture coordinates
        AIVector3D.Buffer textCoordsBuffer = aiMesh.mTextureCoords(0);
        final int numTextCoords = textCoordsBuffer != null ? textCoordsBuffer.remaining() : 0;
        for (int i = 0; i < numTextCoords; i++) {
            AIVector3D textCoord = textCoordsBuffer.get(i);
            textCoords.add(textCoord.x());
            textCoords.add(1 - textCoord.y());
        }
    }


    protected static void processIndices(final AIMesh aiMesh, final List<Integer> indices) {
        final int numFaces = aiMesh.mNumFaces();
        final AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            final AIFace aiFace = aiFaces.get(i);
            final IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
    }

    protected static Mesh processMesh(final AIMesh aiMesh, final List<Material> materials) {
        List<Float> vertices = new ArrayList<>();
        List<Float> textureCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList();

        processVertices(aiMesh, vertices);
        processNormals(aiMesh, normals);
        processTextCoords(aiMesh, textureCoords);
        processIndices(aiMesh, indices);

        Mesh mesh = new Mesh();
        mesh.init(Utils.listFloatsToArray(vertices),
                Utils.listIntegersToArray(indices),
                Utils.listFloatsToArray(normals),
                Utils.listFloatsToArray(textureCoords),
                null, null);
        Material material;
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new Material();
        }
        mesh.setMaterial(material);

        return mesh;
    }
}
