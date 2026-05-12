#version 330

uniform sampler2D Sample0Sampler;
uniform sampler2D Sample1Sampler;

layout(std140) uniform MotionBlurUniforms {
    vec4 BlendParams;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 curr = texture(Sample0Sampler, texCoord);
    vec4 prev = texture(Sample1Sampler, texCoord);

    float f = BlendParams.x;
    float expMode = BlendParams.z;

    float linearF = f;
    float expF = 1.0 - pow(1.0 - f, 2.5);
    float eff = mix(linearF, expF, expMode);

    fragColor = vec4(mix(curr.rgb, prev.rgb, eff), 1.0);
}
