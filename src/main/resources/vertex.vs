#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec2 outTexCoord;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;


void main()
{
    vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;
    outTexCoord = texCoord;
    // We set the w = 0.0 to remove the translation operation from the multiplication with the modelViewMatrix
    // We are only interested on the direction of the vector. If we do the multiplication by hand we can see what happens
    // The .xyz is called Swizzling. Returns a vec3 with the components defined.
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal,0.0)).xyz;
    mvVertexPos = mvPos.xyz;
}