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

float starLayer(vec3 dir, float density, float threshold, vec3 offset, out float seed) {
    vec3 coord = dir * 0.5 + 0.5 + offset;
    vec3 cell = floor(coord * density);
    vec3 local = fract(coord * density) - 0.5;
    seed = hash(cell);
    float shape = 1.0 - smoothstep(0.10, 0.34, length(local));
    return step(threshold, seed) * shape;
}

void main() {
    vec3 dir = normalize(vNormal);
    vec3 drift = vec3(uTime * 0.0045, -uTime * 0.0018, uTime * 0.0030);
    float cloud = fbm(dir * 3.2 + drift);
    float filament = fbm(dir * 7.6 - drift.zxy + cloud * 0.62);
    float nebula = smoothstep(0.62, 0.98, cloud * 0.62 + filament * 0.18);

    vec3 slowDrift = vec3(sin(uTime * 0.0014), cos(uTime * 0.0010), sin(uTime * 0.0012 + 1.7)) * 0.006;
    vec3 fastDrift = vec3(cos(uTime * 0.0022 + 0.6), sin(uTime * 0.0018), cos(uTime * 0.0016 + 2.1)) * 0.009;
    float starSeed;
    float fineSeed;
    float star = starLayer(dir, 84.0, 0.955, slowDrift, starSeed);
    float fineStar = starLayer(dir, 132.0, 0.972, fastDrift + vec3(0.13, 0.07, 0.19), fineSeed);
    float twinkleWave = 0.5 + 0.5 * sin(uTime * (0.022 + starSeed * 0.035) + starSeed * 91.0);
    float fineTwinkle = 0.5 + 0.5 * sin(uTime * (0.030 + fineSeed * 0.045) + fineSeed * 127.0);
    float twinkle = smoothstep(0.18, 1.0, twinkleWave);
    float fineFlash = smoothstep(0.42, 1.0, fineTwinkle);
    float brightStar = star * step(0.992, starSeed) * (0.45 + twinkle * 0.95);

    vec3 viewDir = normalize(-vView);
    float rim = pow(1.0 - max(dot(normalize(vNormal), viewDir), 0.0), 2.2);
    vec3 color = mix(uShellColor, uNebulaColor, nebula * 0.35);
    color += uStarColor * star * (0.18 + twinkle * 1.95);
    color += uStarColor * fineStar * (0.08 + fineFlash * 1.10);
    color += vec3(0.74, 0.86, 1.0) * brightStar * (0.90 + twinkle * 0.90);
    color += vec3(0.018, 0.036, 0.090) * rim * (0.08 + uPulse * 0.05);

    float alpha = clamp(0.80 + nebula * 0.06 + star * (0.05 + twinkle * 0.24)
            + fineStar * (0.02 + fineFlash * 0.13) + brightStar * 0.30 + rim * 0.04, 0.0, 0.97);
    gl_FragColor = vec4(color, alpha);
}
