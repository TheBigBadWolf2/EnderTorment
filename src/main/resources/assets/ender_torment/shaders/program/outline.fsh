#version 120

uniform sampler2D DiffuseSampler;

uniform vec3 OutlineColor;

const float ColorDull = 0.2;
const float AlphaDull = 0.5;

varying in vec2 texCoord;
varying in vec2 oneTexel;
varying in vec2 Centered;

float getDist() {
    float x = abs(Centered.x);
    float y = abs(Centered.y);
    return 1.0 - sqrt(x * x + y * y);
}

void main() {
    vec4 center = texture2D(DiffuseSampler, texCoord.xy);
    vec4 left = texture2D(DiffuseSampler, texCoord.xy - vec2(oneTexel.x, 0.0));
    vec4 right = texture2D(DiffuseSampler, texCoord.xy + vec2(oneTexel.x, 0.0));
    vec4 up = texture2D(DiffuseSampler, texCoord.xy - vec2(0.0, oneTexel.y));
    vec4 down = texture2D(DiffuseSampler, texCoord.xy + vec2(0.0, oneTexel.y));

    float leftDiff  = abs(center.a - left.a);
    float rightDiff = abs(center.a - right.a);
    float upDiff    = abs(center.a - up.a);
    float downDiff  = abs(center.a - down.a);

    float total = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);

    //vec3 outColor = center.rgb * center.a + left.rgb * left.a + right.rgb * right.a + up.rgb * up.a + down.rgb * down.a;

    if (center.a >= total) gl_FragColor = vec4(center.rgb * ColorDull * getDist(), center.a * AlphaDull);
    else gl_FragColor = vec4(OutlineColor * getDist(), total);
    //gl_FragColor = vec4(outColor * 0.2, total);

    /*if (total > 0) gl_FragColor = vec4(OutlineColor, total);
    else gl_FragColor = vec4(0.0);*/
}