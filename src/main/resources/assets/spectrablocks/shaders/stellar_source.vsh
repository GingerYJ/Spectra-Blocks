#version 120

varying vec3 vNormal;
varying vec3 vWorld;
varying vec3 vLocal;
varying vec2 vUv;

void main() {
    vec4 viewPos = gl_ModelViewMatrix * gl_Vertex;
    vNormal = normalize(gl_NormalMatrix * gl_Normal);
    vWorld = viewPos.xyz;
    vLocal = normalize(gl_Vertex.xyz);
    vUv = gl_MultiTexCoord0.xy;
    gl_Position = gl_ProjectionMatrix * viewPos;
}
