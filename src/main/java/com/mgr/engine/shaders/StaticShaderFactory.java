package com.mgr.engine.shaders;

import com.mgr.engine.Utils;

import java.util.HashMap;

import static com.mgr.engine.loaders.AnimatedFrame.MAX_JOINTS;


public class StaticShaderFactory {

    private static HashMap<ShaderType, ShaderProgram> shadersMap = new HashMap<>();



    public static void createAllShaders(final int maxPointLights, final int maxSpotLights) throws Exception {
        shadersMap.put(ShaderType.SimpleShaderOrthographic, createSimpleShaderOrthographic());
        shadersMap.put(ShaderType.StandardWorldShader, createStandardWorldShader(maxPointLights, maxSpotLights));
        shadersMap.put(ShaderType.AnimatedItemShader, createAnimatedItemShader(maxPointLights, maxSpotLights));
    }

    public static ShaderProgram getSimpleShaderOrthographic() {
        return shadersMap.get(ShaderType.SimpleShaderOrthographic);
    }

    public static ShaderProgram getStandardWorldShader() {
        return shadersMap.get(ShaderType.StandardWorldShader);
    }

    public static ShaderProgram getAnimatedItemShader() {
        return shadersMap.get(ShaderType.AnimatedItemShader);
    }

    public static ShaderProgram getShaderByType(final ShaderType shaderType) throws IllegalArgumentException {

        if (shaderType == ShaderType.StandardWorldShader) {
            return getStandardWorldShader();
        } else if (shaderType == ShaderType.SimpleShaderOrthographic) {
            return getSimpleShaderOrthographic();
        } else if (shaderType == ShaderType.AnimatedItemShader) {
            return getAnimatedItemShader();
        } else {
            throw new IllegalArgumentException("Provide Shader Type not valid");
        }


    }

    public static void cleanUp() {
        for (ShaderProgram shader : shadersMap.values()) {
            shader.cleanup();
        }
        shadersMap.clear();
    }

    private static ShaderProgram createSimpleShaderOrthographic() throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/Shaders/simple_vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/Shaders/simple_fragment.fs"));
        shaderProgram.link();

        // Create uniforms for Ortographic-model projection matrix and base color
        shaderProgram.createUniform("projModelMatrix");
        shaderProgram.createUniform("color");
        shaderProgram.createUniform("texture_sampler");

        return shaderProgram;
    }

    private static ShaderProgram createStandardWorldShader(final int maxPointLights, final int maxSpotLights) throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/Shaders/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/Shaders/fragment.fs"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("normalMap");
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createDirectionalLightUniform("directionalLight");
        shaderProgram.createPointLightListUniform("pointLights", maxPointLights);
        shaderProgram.createSpotLightListUniform("spotLights", maxSpotLights);

        return shaderProgram;
    }

    private static ShaderProgram createAnimatedItemShader(final int maxPointLights, final int maxSpotLights) throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/Shaders/animated_vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/Shaders/fragment.fs"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("modelLightViewMatrix");
        shaderProgram.createUniform("orthoProjectionMatrix");
        shaderProgram.createSimpleArrayUniform("jointsMatrix",MAX_JOINTS);

        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("normalMap");
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createDirectionalLightUniform("directionalLight");
        shaderProgram.createPointLightListUniform("pointLights", maxPointLights);
        shaderProgram.createSpotLightListUniform("spotLights", maxSpotLights);

        return shaderProgram;
    }
}
