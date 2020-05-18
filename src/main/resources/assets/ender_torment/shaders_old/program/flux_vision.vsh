#version 110

attribute vec4 Position;
attribute vec4 Normal;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

varying out vec2 texCoord;
varying out vec2 oneTexel;

varying out vec4 VertPos;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    oneTexel = 1.0 / InSize;

    texCoord = Position.xy / OutSize;

    VertPos = vec4(Normal.xyz, 1.0);
}