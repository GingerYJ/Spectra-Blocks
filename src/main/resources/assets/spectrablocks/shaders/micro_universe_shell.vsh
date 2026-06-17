#version 120

varying vec3 vNormal;
varying vec3 vView;

void main() {
    vec4 viewPos = gl_ModelViewMatrix * gl_Vertex;
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vView = viewPos.xyz;
    gl_Position = gl_ProjectionMatrix * viewPos;
}
