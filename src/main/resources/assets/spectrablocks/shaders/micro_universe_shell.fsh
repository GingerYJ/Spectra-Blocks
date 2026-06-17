#version 120

uniform float uTime;
uniform float uPulse;
uniform vec3 uShellColor;
uniform vec3 uNebulaColor;
uniform vec3 uStarColor;

varying vec3 vNormal;
varying vec3 vView;
varying vec2 vUv;

float hash(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float n000 = hash(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash(i + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    float nxy0 = mix(nx00, nx10, f.y);
    float nxy1 = mix(nx01, nx11, f.y);
    return mix(nxy0, nxy1, f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += noise(p) * amplitude;
        p = p * 2.08 + vec3(11.7, 3.9, 6.2);
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec3 dir = normalize(vNormal);
    vec3 drift = vec3(uTime * 0.010, -uTime * 0.004, uTime * 0.007);
    float cloud = fbm(dir * 3.4 + drift);
    float filament = fbm(dir * 9.0 - drift.zxy + cloud);
    float nebula = smoothstep(0.43, 0.86, cloud * 0.72 + filament * 0.34);

    vec2 cell = floor(vUv * vec2(96.0, 48.0));
    float starSeed = hash(vec3(cell, floor(dir.y * 19.0)));
    float star = step(0.986, starSeed);
    float twinkle = 0.72 + 0.28 * sin(uTime * 0.11 + starSeed * 48.0);

    vec3 viewDir = normalize(-vView);
    float rim = pow(1.0 - max(dot(normalize(vNormal), viewDir), 0.0), 2.2);
    vec3 color = mix(uShellColor, uNebulaColor, nebula);
    color += uStarColor * star * twinkle * 1.45;
    color += vec3(0.12, 0.20, 0.42) * rim * (0.25 + uPulse * 0.18);

    float alpha = clamp(0.44 + nebula * 0.14 + star * 0.32 + rim * 0.08, 0.0, 0.92);
    gl_FragColor = vec4(color, alpha);
}
