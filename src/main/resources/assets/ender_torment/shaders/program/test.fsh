#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D Test;


varying in vec2 texCoord;
varying in vec2 oneTexel;

void main() {
    gl_FragColor = clamp(texture2D(DiffuseSampler, texCoord) + texture2D(Test, texCoord), 0.0, 1.0);
}