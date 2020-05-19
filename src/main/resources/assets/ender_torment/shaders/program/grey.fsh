#version 120

uniform sampler2D DiffuseSampler;

varying in vec2 texCoord;
varying in vec2 oneTexel;
varying in vec3 Centered;
varying flat in float Dist;

const float Limit = 0.125;
const float Greyness = 0.125;

vec4 limitColor(vec4 colorIn) {
    vec4 colorOut = vec4(0.0);
    if (colorIn.r > Limit) colorOut.r = colorIn.r;
    if (colorIn.g > Limit) colorOut.g = colorIn.g;
    if (colorIn.b > Limit) colorOut.b = colorIn.b;
    colorOut.a = colorIn.a;
    return colorOut;
}

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
    /*vec4 color = texture2D(DiffuseSampler, texCoord);

    vec4 center = texture2D(DiffuseSampler, texCoord);
    vec4 left   = texture2D(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right  = texture2D(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up     = texture2D(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down   = texture2D(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));

    vec4 leftDiff  = limitColor(center - left);
    vec4 rightDiff = limitColor(center - right);
    vec4 upDiff    = limitColor(center - up);
    vec4 downDiff  = limitColor(center - down);


    vec4 line = autoClamp(leftDiff + rightDiff + upDiff + downDiff);
    vec4 grey = greyOut(center, Greyness * Dist);
    line.rgb *= Dist;

    vec4 total = autoClamp(line + grey);
    gl_FragColor = vec4(total.rgb, 1.0);*/
    //gl_FragColor = vec4(vec3(clamp(Dist, 0.0, 1.0)), 1.0);
    gl_FragColor = vec4(
        abs(Centered.xy),
        Dist,
        1.0
    );

    /*float brightness = (color.r + color.g + color.b) / 3.0;
    color.r = brightness;
    color.g = brightness;
    color.b = brightness;

    gl_FragColor = vec4(color.rgb, 1.0);*/
}