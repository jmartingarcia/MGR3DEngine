package com.mgr.engine;

import org.joml.Vector4f;

public class Material {

    public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColor;

    private Vector4f diffuseColor;

    private Vector4f specularColor;

    private float reflectance;

    private Texture texture;

    private Texture normalMap;


    public Material() {
        this.ambientColor = DEFAULT_COLOR;
        this.diffuseColor = DEFAULT_COLOR;
        this.specularColor = DEFAULT_COLOR;
        this.texture = null;
        this.reflectance = 0;
    }

    public Material(final Vector4f color, final float reflectance) {
        this(color, color, color, null, reflectance);
    }

    public Material(final Texture texture) {
        this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, 0);
    }

    public Material(final Texture texture, final float reflectance) {
        this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, reflectance);
    }

    public Material(final Vector4f ambientColour, final Vector4f diffuseColour, final Vector4f specularColour, final float reflectance) {
        this.ambientColor = ambientColour;
        this.diffuseColor = diffuseColour;
        this.specularColor = specularColour;
        this.texture = null;
        this.reflectance = reflectance;
    }

    public Material(final Vector4f ambientColor, final Vector4f diffuseColor, final Vector4f specularColor, final Texture texture, final float reflectance) {
        this.ambientColor  = ambientColor;
        this.diffuseColor  = diffuseColor;
        this.specularColor = specularColor;
        this.texture       = texture;
        this.reflectance   = reflectance;
    }

    public Vector4f getAmbientColor() {

        return ambientColor;
    }

    public void setAmbientColor(final Vector4f ambientColor) {
        this.ambientColor = ambientColor;
    }

    public Vector4f getDiffuseColor() {

        return diffuseColor;
    }

    public void setDiffuseColor(final Vector4f diffuseColor) {

        this.diffuseColor = diffuseColor;
    }

    public Vector4f getSpecularColour() {

        return specularColor;
    }

    public void setSpecularColour(final Vector4f specularColor) {
        this.specularColor = specularColor;
    }

    public float getReflectance() {
        return reflectance;
    }

    public void setReflectance(final float reflectance) {
        this.reflectance = reflectance;
    }

    public boolean isTextured() {

        return this.texture != null;
    }

    public Texture getTexture() {

        return texture;
    }

    public void setTexture(final Texture texture) {

        this.texture = texture;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(final Texture normalMap) {
        this.normalMap = normalMap;
    }

    public boolean hasNormalMap(){
        return this.normalMap != null;
    }

    public void cleanUp() {

        if (isTextured()) texture.cleanup();
        if (hasNormalMap()) normalMap.cleanup();
    }

}
