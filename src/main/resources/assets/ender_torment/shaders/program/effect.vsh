#version 110

attribute vec4 Position;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

varying out vec2 texCoord;
varying out vec2 oneTexel;
varying out vec2 Centered;

void doData() {
    Centered = (texCoord - 0.5) * 2.0;
}

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    oneTexel = 1.0 / InSize;

    texCoord = Position.xy / OutSize;

    /*Centered = vec2((texCoord - 0.5) * 2.0);
    float x = abs(Centered.x);
    float y = abs(Centered.y);
    Centered.z = Dist = 1.0 - sqrt(x * x + y * y);*/
    doData();
}