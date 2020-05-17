#version 120


// The current rendered image
// (name is hardcoded into the minecraft code,
// so if you want it to be updated properly,
// you must name it DiffuseSampler)
uniform sampler2D DiffuseSampler;
uniform float Limit;
uniform float Time;

// texCoord is the current xy position of this fragment(pixel) on the screen.
// It ranges from 0 to 1.
// It is interpolated from between the positions of the positions sent to the vertex shader (that's what varying's do)
varying in vec2 texCoord;
varying in vec2 oneTexel;
varying in vec4 VertPos;

int time;
float lastTime;

void tick() {
    if (Time < lastTime) time++;
    lastTime = Time;
}

float getTime() {
    return time + Time;
}

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

void a() {
    // This is the current colour of the rendering
    //vec4 CurrTexel = texture2D(DiffuseSampler, texCoord);
    vec4 center = texture2D(DiffuseSampler, texCoord);
    vec4 left   = texture2D(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right  = texture2D(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up     = texture2D(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down   = texture2D(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));

    vec4 leftDiff  = redo(center - left);
    vec4 rightDiff = redo(center - right);
    vec4 upDiff    = redo(center - up);
    vec4 downDiff  = redo(center - down);

    float greyness = (sin((getTime() * 3.1415926535) / 10) * 0.5) + 0.5;


    vec4 total = clamp(clamp(/*center + */(leftDiff + rightDiff + upDiff + downDiff), 0.0, 1.0) + (avg(center) * greyness), 0, 1);

    // Average the colours, and use that as the brightness value


    gl_FragColor = vec4(total.rgb, 1.0);
    //gl_FragColor = CurrTexel;
}
void b() {
    vec4 center = texture2D(DiffuseSampler, texCoord);
    vec4 left = texture2D(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right = texture2D(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up = texture2D(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down = texture2D(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));
    float leftDiff  = abs(center.a - left.a);
    float rightDiff = abs(center.a - right.a);
    float upDiff    = abs(center.a - up.a);
    float downDiff  = abs(center.a - down.a);
    float total = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);
    vec3 outColor = center.rgb * center.a + left.rgb * left.a + right.rgb * right.a + up.rgb * up.a + down.rgb * down.a;
    gl_FragColor = vec4(outColor * 0.2, total);
}

void main() {
    tick();
    b();
}