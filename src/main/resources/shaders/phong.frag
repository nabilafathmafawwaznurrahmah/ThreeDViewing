
#version 330 core

in vec3 vFragPosition;
in vec3 vNormal;
in vec2 vTexCoord;

out vec4 FragColor;

uniform vec3 uLightPosition;
uniform vec3 uViewPosition;
uniform vec3 uObjectColor;

uniform bool uUseBlinnPhong;
uniform bool uFlatShading;

// Texture mapping
uniform sampler2D uTexture;
uniform bool uUseTexture;

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

    // Mengambil warna dari texture
    vec3 baseColor = uObjectColor;

    if (uUseTexture) {
        baseColor = texture(
            uTexture,
            vTexCoord
        ).rgb;
    }

    // Arah cahaya
    vec3 lightDirection = normalize(
        uLightPosition - vFragPosition
    );

    vec3 viewDirection = normalize(
        uViewPosition - vFragPosition
    );

    // Ambient lighting
    float ambientStrength = 0.15;

    vec3 ambient =
            ambientStrength * baseColor;

    // Diffuse lighting
    float diffuseStrength = max(
            dot(normal, lightDirection),
            0.0
    );

    vec3 diffuse =
            diffuseStrength * baseColor;

    // Specular lighting
    float specularStrength = 0.5;

    float specular = 0.0;

    if (uUseBlinnPhong) {

        vec3 halfwayDirection = normalize(
                lightDirection + viewDirection
        );

        specular = pow(
                max(
                    dot(normal, halfwayDirection),
                    0.0
                ),
                32.0
        );

    } else {

        vec3 reflectDirection = reflect(
                -lightDirection,
                normal
        );

        specular = pow(
                max(
                    dot(viewDirection, reflectDirection),
                    0.0
                ),
                32.0
        );
    }

    vec3 specularColor =
            vec3(specularStrength * specular);

    // Final color
    vec3 result =
            ambient + diffuse + specularColor;

    FragColor = vec4(result, 1.0);
}
