#version 120


// The current rendered image
// (name is hardcoded into the minecraft code,
// so if you want it to be updated properly,
// you must name it DiffuseSampler)
uniform sampler2D DiffuseSampler;
uniform sampler2D Layer2;

// texCoord is the current xy position of this fragment(pixel) on the screen.
// It ranges from 0 to 1.
// It is interpolated from between the positions of the positions sent to the vertex shader (that's what varying's do)
varying in vec2 texCoord;

void main() {
    vec4 color1 = texture2D(DiffuseSampler, texCoord);
    vec4 color2 = texture2D(Layer2,         texCoord);

    /*float r = max(color1.r, color2.r);
    float g = max(color1.g, color2.g);
    float b = max(color1.b, color2.b);
    float a = max(color1.a, color2.a);
    gl_FragColor = vec4(r, g, b, a);*/
    //gl_FragColor = clamp(color1 + color2, 0.0, 1.0);
    //gl_FragColor = color2;
    vec4 color = vec4(0.0);

    if (color2.a > 0) {
        if (color2.a < 1 && color1.a > 0) {
            color.rgb = clamp((color1.rgb * (color1.a)) + (color2.rgb * color2.a), 0.0, 1.0);
            color.a = clamp(color1.a + color2.a, 0.0, 1.0);
        } else color = vec4(color2);
    } else color = vec4(color1);

    gl_FragColor = color;
}