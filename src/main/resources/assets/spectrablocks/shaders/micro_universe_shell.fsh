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
    float nebula = smoothstep(0.55, 0.94, cloud * 0.70 + filament * 0.22);

    vec3 starCell = floor((dir * 0.5 + 0.5) * 72.0);
    float starSeed = hash(starCell);
    float starShape = 1.0 - smoothstep(0.12, 0.35, length(fract((dir * 0.5 + 0.5) * 72.0) - 0.5));
    float star = step(0.962, starSeed) * starShape;
    float twinkleWave = 0.5 + 0.5 * sin(uTime * 0.085 + starSeed * 72.0);
    float twinkle = 0.56 + 0.62 * twinkleWave * twinkleWave;
    float brightStar = step(0.995, starSeed) * starShape;
    float fineSeed = hash(floor((dir * 0.5 + 0.5) * 112.0) + vec3(17.0, 3.0, 29.0));
    float fineStar = step(0.976, fineSeed) *
            (1.0 - smoothstep(0.10, 0.32, length(fract((dir * 0.5 + 0.5) * 112.0) - 0.5)));

    vec3 viewDir = normalize(-vView);
    float rim = pow(1.0 - max(dot(normalize(vNormal), viewDir), 0.0), 2.2);
    vec3 color = mix(uShellColor, uNebulaColor, nebula * 0.58);
    color += uStarColor * star * twinkle * 1.85;
    color += uStarColor * fineStar * (0.55 + twinkleWave * 0.35);
    color += vec3(0.74, 0.86, 1.0) * brightStar * (0.75 + twinkleWave * 0.80);
    color += vec3(0.035, 0.070, 0.17) * rim * (0.12 + uPulse * 0.08);

    float alpha = clamp(0.73 + nebula * 0.10 + star * 0.24 + fineStar * 0.16 + brightStar * 0.30 + rim * 0.06, 0.0, 0.96);
    gl_FragColor = vec4(color, alpha);
}
