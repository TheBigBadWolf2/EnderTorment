#version 120


// The current rendered image
// (name is hardcoded into the minecraft code,
// so if you want it to be updated properly,
// you must name it DiffuseSampler)
uniform sampler2D DiffuseSampler;
uniform sampler2D Outlines;

// texCoord is the current xy position of this fragment(pixel) on the screen.
// It ranges from 0 to 1.
// It is interpolated from between the positions of the positions sent to the vertex shader (that's what varying's do)
varying in vec2 texCoord;

void main() {
    vec4 main = texture2D(DiffuseSampler, texCoord);
    vec4 outline = texture2D(Outlines, texCoord);

    vec4 color = vec4(0.0);

    if (outline.a > 0) {
        if (outline.a < 1) {
            color.rgb = clamp((main.rgb * (1 - outline.a)) + (outline.rgb * outline.a), 0.0, 1.0);
            color.a = 1.0;
        } else color = vec4(outline);
    } else color = vec4(main);

    gl_FragColor = color;
    /*outline.a = 1.0;
    gl_FragColor = outline;*/

}