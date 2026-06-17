#version 120

uniform float alpha;
uniform vec4 tint;

void main() {
    vec4 color = gl_Color * tint;
    color.a *= alpha;
    gl_FragColor = color;
}
