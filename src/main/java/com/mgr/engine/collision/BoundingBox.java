package com.mgr.engine.collision;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Planef;
import org.joml.Vector3f;

import java.util.Vector;

public class BoundingBox {

    private Vector3f min;
    private Vector3f max;
    private boolean empty;


    public BoundingBox(){
        min = null;
        max = null;
        empty = true;
    }

    public BoundingBox(final Vector3f min, final Vector3f max){
        this.min = min;
        this.max = max;
        empty = false;
    }

    public Vector3f size() {
        final Vector3f copyMax = new Vector3f(max);
        return copyMax.sub(min);
    }

    public float xSize() {
        return max.x - min.x;
    }

    public float ySize() {
        return max.y - min.y;
    }

    public float zSize() {
        return max.z - min.z;
    }

    public Vector3f center() {
        final Vector3f copyMin = new Vector3f(min);
        return copyMin.add(max).mul(0.5f);
    }

    public boolean isEmpty() {
        return empty;
    }

    public Vector3f getMinVertex() {
        return new Vector3f(min);
    }

    public Vector3f getMaxVertex() {
        return new Vector3f(max);
    }

    // Increases the size of the box to include the vertex
    public void add(final Vector3f vertex) {

        if (min == null) {
            min = new Vector3f(vertex);
        }

        if (max == null) {
            max = new Vector3f(vertex);
        }

        if (vertex.x < min.x) min.x = vertex.x;
        if (vertex.x > max.x) max.x = vertex.x;
        if (vertex.y < min.y) min.y = vertex.y;
        if (vertex.y > max.y) max.y = vertex.y;
        if (vertex.z < min.z) min.z = vertex.z;
        if (vertex.z > max.z) max.z = vertex.z;
    }

    private Pair<Float,Float> getValueOffsetFromMatrixValue(final float matrixValue, final float boxMinAxisValue, final float boxMaxAxisValue) {

        float min_number = 0.0f, max_number = 0.0f;
        if (matrixValue > 0.0f) {
            min_number += matrixValue * boxMinAxisValue;
            max_number += matrixValue * boxMaxAxisValue;
        } else {
            min_number += matrixValue * boxMaxAxisValue;
            max_number += matrixValue * boxMinAxisValue;
        }

        return Pair.of(min_number, max_number);
    }

    // Transforming a vertex using a matrix ends on the following formulas for non-translation transformation
    //   x' = m00 * X + m10 * Y + m20 * Z
    //   y' = m01 * X + m11 * Y + m21 * Z
    //   z' = m02 * X + m12 * Y + m22 * Z
    // If the m00 .. m22 is positive then the minimum vector will give you the lower number but
    // if m00 .. m22 is negative then the maximum vector will give you the lower number.
    // Then translation is done just by adding the original box position rotated with the world translation from the
    // transformation matrix.
    public void setToTransformedBox(final BoundingBox box, final Matrix4f transformationMatrix) {

        if (box.isEmpty()) {
            empty = true;
            return;
        }

        // Extract the translation option
        min = transformationMatrix.getTranslation(min);
        max = transformationMatrix.getTranslation(max);

        //Compute the new AABB

        // X'
        Pair<Float, Float> deltas = getValueOffsetFromMatrixValue(transformationMatrix.m00(), box.getMinVertex().x, box.getMaxVertex().x);
        min.x += deltas.getLeft();
        max.x += deltas.getRight();
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m01(), box.getMinVertex().x, box.getMaxVertex().x);
        min.y += deltas.getLeft();
        max.y += deltas.getRight();
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m02(), box.getMinVertex().x, box.getMaxVertex().x);
        min.z += deltas.getLeft();
        max.z += deltas.getRight();

        // Y'
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m10(), box.getMinVertex().y, box.getMaxVertex().y);
        min.x += deltas.getLeft();
        max.x += deltas.getRight();
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m11(), box.getMinVertex().y, box.getMaxVertex().y);
        min.y += deltas.getLeft();
        max.y += deltas.getRight();
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m12(), box.getMinVertex().y, box.getMaxVertex().y);
        min.z += deltas.getLeft();
        max.z += deltas.getRight();

        // Z'
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m20(), box.getMinVertex().z, box.getMaxVertex().z);
        min.x += deltas.getLeft();
        max.x += deltas.getRight();
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m21(), box.getMinVertex().z, box.getMaxVertex().z);
        min.y += deltas.getLeft();
        max.y += deltas.getRight();
        deltas = getValueOffsetFromMatrixValue(transformationMatrix.m22(), box.getMinVertex().z, box.getMaxVertex().z);
        min.z += deltas.getLeft();
        max.z += deltas.getRight();
    }

    // Test AABB collision against an infinite plane
    public boolean intersectPlane(final Planef plane, final Vector3f moveDir) {

        final Vector3f center = center();
        final Vector3f copyMax = new Vector3f(max);
        final Vector3f distanceCenterMax = copyMax.sub(center);
        final Vector3f unsignedPlaneNormal = new Vector3f(Math.abs(plane.a), Math.abs(plane.b), Math.abs(plane.c));
        final Vector3f planeNormal = new Vector3f(Math.abs(plane.a), Math.abs(plane.b), Math.abs(plane.c));

        // Project the distance from AABB center to max point into plane normal
        float r = distanceCenterMax.dot(unsignedPlaneNormal);

        // Calculate distance from AABB center to plane
        float s = Math.abs(planeNormal.dot(center) - plane.d);

        return (s<=r);
    }

    public boolean intersectAABB(final BoundingBox box) {
        return  (min.x <= box.getMaxVertex().x && max.x >= box.getMinVertex().x) &&
                (min.y <= box.getMaxVertex().y && max.y >= box.getMinVertex().y) &&
                (min.z <= box.getMaxVertex().z && max.z >= box.getMinVertex().z);
    }

}
