#version 120

uniform float uTime;
uniform float uPulse;
uniform vec3 uShellColor;
uniform vec3 uNebulaColor;
uniform vec3 uStarColor;

varying vec3 vNormal;
varying vec3 vView;

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
    vec3 drift = vec3(uTime * 0.0045, -uTime * 0.0018, uTime * 0.0030);
    float cloud = fbm(dir * 3.2 + drift);
    float filament = fbm(dir * 7.6 - drift.zxy + cloud * 0.62);
    float nebula = smoothstep(0.48, 0.88, cloud * 0.76 + filament * 0.26);

    vec3 starCell = floor((dir * 0.5 + 0.5) * 34.0);
    float starSeed = hash(starCell);
    float starShape = 1.0 - smoothstep(0.18, 0.42, length(fract((dir * 0.5 + 0.5) * 34.0) - 0.5));
    float star = step(0.9865, starSeed) * starShape;
    float twinkle = 0.82 + 0.18 * sin(uTime * 0.045 + starSeed * 48.0);

    vec3 viewDir = normalize(-vView);
    float rim = pow(1.0 - max(dot(normalize(vNormal), viewDir), 0.0), 2.2);
    vec3 color = mix(uShellColor, uNebulaColor, nebula);
    color += uStarColor * star * twinkle * 1.25;
    color += vec3(0.10, 0.18, 0.38) * rim * (0.22 + uPulse * 0.14);

    float alpha = clamp(0.40 + nebula * 0.12 + star * 0.24 + rim * 0.07, 0.0, 0.82);
    gl_FragColor = vec4(color, alpha);
}
