package com.mgr.engine;

import com.mgr.engine.items.GameItem;
import com.mgr.engine.items.IGameItem;
import com.mgr.myshooter.Map.Room;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

        private final Matrix4f projectionMatrix;
        private final Matrix4f worldMatrix;
        private final Matrix4f viewMatrix;
        private final Matrix4f modelViewMatrix;
        private final Matrix4f orthoMatrix;

        public Transformation() {
            worldMatrix = new Matrix4f();
            projectionMatrix = new Matrix4f();
            viewMatrix = new Matrix4f();
            modelViewMatrix = new Matrix4f();
            orthoMatrix = new Matrix4f();
        }

        public final Matrix4f getProjectionMatrix(final float fov, final float width, final float height, final float zNear, final float zFar) {
            float aspectRatio = width / height;
            projectionMatrix.identity();
            projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
            return projectionMatrix;
        }


        public Matrix4f getViewMatrix(final Camera camera) {
            Vector3f cameraPos = camera.getPosition();
            Vector3f rotation = camera.getRotation();
            viewMatrix.identity();
            // First do the rotation so camera rotates over its position
            viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                      .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
            // Then do the translation
            viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            return viewMatrix;
        }

        public Matrix4f getModelViewMatrix(final Room room, final Matrix4f viewMatrix) {
            modelViewMatrix.identity().translate(room.getWorldPosition())
                    .scale(room.getScale());
            Matrix4f viewCurr = new Matrix4f(viewMatrix);
            return viewCurr.mul(modelViewMatrix);
        }

        public Matrix4f getModelViewMatrix(final IGameItem item, final Matrix4f viewMatrix) {
            modelViewMatrix.identity().translate(item.getPosition())
                    .scale(item.getScale());
            Matrix4f viewCurr = new Matrix4f(viewMatrix);
            return viewCurr.mul(modelViewMatrix);
        }

        public final Matrix4f getOrthoProjectionMatrix(final float left, final float right, final float bottom, final float top) {
            orthoMatrix.identity();
            orthoMatrix.setOrtho2D(left, right, bottom, top);
            return orthoMatrix;
        }

        public Matrix4f getOrtoProjModelMatrix(final IGameItem gameItem, final Matrix4f orthoMatrix) {
            Vector3f rotation = gameItem.getRotation();
            Matrix4f modelMatrix = new Matrix4f();
            modelMatrix.identity().translate(gameItem.getPosition()).
                    rotateX((float)Math.toRadians(-rotation.x)).
                    rotateY((float)Math.toRadians(-rotation.y)).
                    rotateZ((float)Math.toRadians(-rotation.z)).
                    scale(gameItem.getScale());
            Matrix4f orthoMatrixCurr = new Matrix4f(orthoMatrix);
            orthoMatrixCurr.mul(modelMatrix);
            return orthoMatrixCurr;
        }

    }

