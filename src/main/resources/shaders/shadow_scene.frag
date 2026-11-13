
#version 330 core

in vec3 vFragPosition;
in vec3 vNormal;
in vec2 vTexCoord;
in vec4 vFragPosLightSpace;

out vec4 FragColor;

uniform vec3 uLightPosition;
uniform vec3 uViewPosition;
uniform vec3 uObjectColor;

uniform sampler2D uTexture;
uniform sampler2D uShadowMap;

uniform bool uUseTexture;
uniform bool uUseBlinnPhong;
uniform bool uFlatShading;

float calculateShadow(vec4 fragPosLightSpace, vec3 normal) {

    vec3 projectedCoords =
        fragPosLightSpace.xyz / fragPosLightSpace.w;

    projectedCoords = projectedCoords * 0.5 + 0.5;

    if (projectedCoords.z > 1.0 ||
        projectedCoords.x < 0.0 ||
        projectedCoords.x > 1.0 ||
        projectedCoords.y < 0.0 ||
        projectedCoords.y > 1.0) {
        return 0.0;
    }

    float closestDepth =
        texture(uShadowMap, projectedCoords.xy).r;

    float currentDepth = projectedCoords.z;

    vec3 lightDirection =
        normalize(uLightPosition - vFragPosition);

    float bias = max(
        0.005 * (1.0 - dot(normal, lightDirection)),
        0.0005
    );

    return currentDepth - bias > closestDepth ? 0.65 : 0.0;
}

void main() {

    vec3 normal;

    if (uFlatShading) {
        normal = normalize(
            cross(
                dFdx(vFragPosition),
                dFdy(vFragPosition)
            )
        );
    } else {
        normal = normalize(vNormal);
    }

    vec3 baseColor = uObjectColor;

    if (uUseTexture) {
        baseColor = texture(uTexture, vTexCoord).rgb;
    }

    vec3 lightDirection =
        normalize(uLightPosition - vFragPosition);

    vec3 viewDirection =
        normalize(uViewPosition - vFragPosition);

    float ambient = 0.2;

    float diffuse =
        max(dot(normal, lightDirection), 0.0);

    float specular = 0.0;

    if (uUseBlinnPhong) {

        vec3 halfwayDirection =
            normalize(lightDirection + viewDirection);

        specular = pow(
            max(dot(normal, halfwayDirection), 0.0),
            32.0
        );

    } else {

        vec3 reflectDirection =
            reflect(-lightDirection, normal);

        specular = pow(
            max(dot(viewDirection, reflectDirection), 0.0),
            32.0
        );
    }

    float shadow =
        calculateShadow(vFragPosLightSpace, normal);

    vec3 lighting =
        (ambient + (1.0 - shadow) * diffuse) * baseColor
        + (1.0 - shadow) * vec3(0.3 * specular);

    FragColor = vec4(lighting, 1.0);
}
