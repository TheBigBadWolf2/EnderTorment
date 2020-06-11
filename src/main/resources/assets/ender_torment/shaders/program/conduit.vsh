#version 110

attribute vec4 Position;
attribute vec4 Color;

uniform mat4 ProjMat;

//layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
//layout (location = 1) in vec3 aColor; // the color variable has attribute position 1

varying out vec3 ourColor; // output a color to the fragment shader

void main() {
    vec4 outPos = ProjMat * vec4(Position.xyz, 1.0);
    gl_Position = vec4(outPos.xyz, 1.0);
    //gl_Position = vec4(aPos, 1.0);
    ourColor = Color.rgb; // set ourColor to the input color we got from the vertex data
}