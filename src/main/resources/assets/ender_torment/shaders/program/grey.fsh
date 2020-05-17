#version 120

uniform sampler2D DiffuseSampler;

varying in vec2 texCoord;

void main() {
    vec4 color = texture2D(DiffuseSampler, texCoord);

    float brightness = (color.r + color.g + color.b) / 3.0;
    color.r = brightness;
    color.g = brightness;
    color.b = brightness;

    gl_FragColor = vec4(color.rgb, 1.0);
}