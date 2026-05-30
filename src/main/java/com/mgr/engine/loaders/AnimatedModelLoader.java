package com.mgr.engine.loaders;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.Utils;
import com.mgr.engine.items.AnimGameItem;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.assimp.Assimp.*;

public class AnimatedModelLoader extends StaticModelLoader {

    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }

    public static AnimGameItem loadAnimGameItem(String resourcePath, String texturesDir)
            throws Exception {
        return loadAnimGameItem(resourcePath, texturesDir, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                        | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
    }

    public static AnimGameItem loadAnimGameItem(String resourcePath, String texturesDir, int flags)
            throws Exception {

        final AIScene aiScene = aiImportFile(resourcePath, flags);
        if (aiScene == null) {
            throw new Exception("Error loading model");
        }

        final int numMaterials = aiScene.mNumMaterials();
        final PointerBuffer aiMaterials = aiScene.mMaterials();
        final List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            final AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesDir);
        }

        final List<Bone> boneList = new ArrayList<>();
        final int numMeshes = aiScene.mNumMeshes();
        final PointerBuffer aiMeshes = aiScene.mMeshes();
        final Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            final AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            final Mesh mesh = processMesh(aiMesh, materials, boneList);
            meshes[i] = mesh;
        }

        final AINode aiRootNode = aiScene.mRootNode();
        final Matrix4f rootTransformation = toMatrix(aiRootNode.mTransformation());
        final Node rootNode = processNodesHierarchy(aiRootNode, null);
        final Map<String, Animation> animations = processAnimations(aiScene, boneList, rootNode, rootTransformation);
        final AnimGameItem item = new AnimGameItem(meshes, animations);

        return item;
    }

    protected static Mesh processMesh(final AIMesh aiMesh, final List<Material> materials, final List<Bone> bones) {

        final List<Float> vertices = new ArrayList<>();
        final List<Float> textureCoords = new ArrayList<>();
        final List<Float> normals = new ArrayList<>();
        final List<Integer> indices = new ArrayList();
        final List<Integer> boneIds = new ArrayList<>();
        final List<Float> weights = new ArrayList<>();

        processVertices(aiMesh, vertices);
        processNormals(aiMesh, normals);
        processTextCoords(aiMesh, textureCoords);
        processIndices(aiMesh, indices);
        processBones(aiMesh, bones, boneIds, weights);

        final Mesh mesh = new Mesh();
        mesh.init(Utils.listFloatsToArray(vertices),
                Utils.listIntegersToArray(indices),
                Utils.listFloatsToArray(normals),
                Utils.listFloatsToArray(textureCoords),
                Utils.listIntegersToArray(boneIds),
                Utils.listFloatsToArray(weights));

        final Material material;
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new Material();
        }
        mesh.setMaterial(material);

        return mesh;
    }

    protected static void processBones(final AIMesh aiMesh, final List<Bone> boneList, final List<Integer> boneIds,
                                     final List<Float> weights) {

        final Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
        final int numBones = aiMesh.mNumBones();
        final PointerBuffer aiBones = aiMesh.mBones();
        for (int i = 0; i < numBones; i++) {
            final AIBone aiBone = AIBone.create(aiBones.get(i));
            final int id = boneList.size();
            final Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
            boneList.add(bone);
            final int numWeights = aiBone.mNumWeights();
            final AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
            for (int j = 0; j < numWeights; j++) {
                final AIVertexWeight aiWeight = aiWeights.get(j);
                final VertexWeight vw = new VertexWeight(bone.getBoneId(), aiWeight.mVertexId(),
                        aiWeight.mWeight());
                List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());
                if (vertexWeightList == null) {
                    vertexWeightList = new ArrayList<>();
                    weightSet.put(vw.getVertexId(), vertexWeightList);
                }
                vertexWeightList.add(vw);
            }
        }

        int numVertices = aiMesh.mNumVertices();
        for (int i = 0; i < numVertices; i++) {
            List<VertexWeight> vertexWeightList = weightSet.get(i);
            int size = vertexWeightList != null ? vertexWeightList.size() : 0;
            for (int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
                if (j < size) {
                    VertexWeight vw = vertexWeightList.get(j);
                    weights.add(vw.getWeight());
                    boneIds.add(vw.getBoneId());
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }
    }

    protected static Node processNodesHierarchy(AINode aiNode, Node parentNode) {
        String nodeName = aiNode.mName().dataString();
        Node node = new Node(nodeName, parentNode);

        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            Node childNode = processNodesHierarchy(aiChildNode, node);
            node.addChild(childNode);
        }

        return node;
    }

    protected static Map<String, Animation> processAnimations(AIScene aiScene, List<Bone> boneList,
                                                            Node rootNode, Matrix4f rootTransformation) {
        Map<String, Animation> animations = new HashMap<>();

        // Process all animations
        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnimations; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

            // Calculate transformation matrices for each node
            int numChanels = aiAnimation.mNumChannels();
            PointerBuffer aiChannels = aiAnimation.mChannels();
            for (int j = 0; j < numChanels; j++) {
                AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(j));
                String nodeName = aiNodeAnim.mNodeName().dataString();
                Node node = rootNode.findByName(nodeName);
                buildTransFormationMatrices(aiNodeAnim, node);
            }

            List<AnimatedFrame> frames = buildAnimationFrames(boneList, rootNode, rootTransformation);
            Animation animation = new Animation(aiAnimation.mName().dataString(), frames, aiAnimation.mDuration());
            animations.put(animation.getName(), animation);
        }
        return animations;
    }

    protected static List<AnimatedFrame> buildAnimationFrames(List<Bone> boneList, Node rootNode,
                                                            Matrix4f rootTransformation) {

        int numFrames = rootNode.getAnimationFrames();
        List<AnimatedFrame> frameList = new ArrayList<>();
        for (int i = 0; i < numFrames; i++) {
            AnimatedFrame frame = new AnimatedFrame();
            frameList.add(frame);

            int numBones = boneList.size();
            for (int j = 0; j < numBones; j++) {
                Bone bone = boneList.get(j);
                Node node = rootNode.findByName(bone.getBoneName());
                Matrix4f boneMatrix = Node.getParentTransforms(node, i);
                boneMatrix.mul(bone.getOffsetMatrix());
                boneMatrix = new Matrix4f(rootTransformation).mul(boneMatrix);
                frame.setMatrix(j, boneMatrix);
            }
        }

        return frameList;
    }

    protected static void buildTransFormationMatrices(AINodeAnim aiNodeAnim, Node node) {
        int numFrames = aiNodeAnim.mNumPositionKeys();
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        for (int i = 0; i < numFrames; i++) {
            AIVectorKey aiVecKey = positionKeys.get(i);
            AIVector3D vec = aiVecKey.mValue();

            Matrix4f transfMat = new Matrix4f().translate(vec.x(), vec.y(), vec.z());

            AIQuatKey quatKey = rotationKeys.get(i);
            AIQuaternion aiQuat = quatKey.mValue();
            Quaternionf quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
            transfMat.rotate(quat);

            if (i < aiNodeAnim.mNumScalingKeys()) {
                aiVecKey = scalingKeys.get(i);
                vec = aiVecKey.mValue();
                transfMat.scale(vec.x(), vec.y(), vec.z());
            }

            node.addTransformation(transfMat);
        }
    }
}
