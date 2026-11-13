
#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out vec3 vFragPosition;
out vec3 vNormal;
out vec2 vTexCoord;
out vec4 vFragPosLightSpace;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uLightSpaceMatrix;

void main() {

    vec4 worldPosition =
        uModel * vec4(aPosition, 1.0);

    vFragPosition = worldPosition.xyz;

    vNormal =
        mat3(transpose(inverse(uModel))) * aNormal;

    vTexCoord = aTexCoord;

    vFragPosLightSpace =
        uLightSpaceMatrix * worldPosition;

    gl_Position =
        uProjection * uView * worldPosition;
}
