#version 120

uniform sampler2D DiffuseSampler;

uniform float Limit;

varying in vec2 texCoord;
varying in vec2 oneTexel;

vec4 redo(vec4 vec) {
    if (vec.r <= Limit) vec.r = 0;
    if (vec.g <= Limit) vec.g = 0;
    if (vec.b <= Limit) vec.b = 0;
    return vec;
}

vec4 avg(vec4 vec) {
    float brightness = (vec.r + vec.g + vec.b) / 3.0;
    return vec4(vec3(brightness), 1.0);
}

void main() {
    vec4 center = texture2D(DiffuseSampler, texCoord);
    vec4 left   = texture2D(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right  = texture2D(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up     = texture2D(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down   = texture2D(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));

    vec4 leftDiff  = redo(center - left);
    vec4 rightDiff = redo(center - right);
    vec4 upDiff    = redo(center - up);
    vec4 downDiff  = redo(center - down);

    float greyness = 0.125;

    vec4 diffs = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);
    vec4 greyScale = clamp(avg(center) * greyness, 0.0, 1.0);

    vec4 total = clamp(diffs + greyScale, 0.0, 1.0);

    gl_FragColor = vec4(diffs.rgb, 1.0);
}