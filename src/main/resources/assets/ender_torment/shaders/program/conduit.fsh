#version 420

in vec3 ourColor;

vec4 avgColor(vec4 colorIn) {
    float brightness = (colorIn.r + colorIn.g + colorIn.b) / 3.0;
    return vec4(vec3(brightness), colorIn.a);
}

vec4 autoClamp(vec4 vec) {
    return clamp(vec, 0.0, 1.0);
}

vec4 greyOut(vec4 color, float greyness) {
    return autoClamp(avgColor(color) * greyness);
}

void main() {
    gl_FragColor = greyOut(vec4(ourColor, 1.0), 1.0);
}