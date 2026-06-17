#version 120

uniform float uTime;
uniform vec3 uBaseColor;
uniform float uRimIntensity;
uniform float uPulseAmount;
uniform float uNoiseSpeed;

varying vec3 vNormal;
varying vec3 vWorld;
varying vec2 vUv;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
        mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
        u.y
    );
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += noise(p) * amplitude;
        p = p * 2.03 + vec2(7.1, 3.7);
        amplitude *= 0.5;
    }
    return value;
}

vec4 stellarColor(vec2 uv, float facing, float rim) {
    vec2 flowUv = uv;
    flowUv.x += uTime * uNoiseSpeed * 0.090;
    flowUv.y += sin(uv.x * 20.0 + uTime * 0.65) * 0.030;
    vec2 swirlUv = vec2(
        flowUv.x + sin(flowUv.y * 18.0 + uTime * 0.9) * 0.045,
        flowUv.y + cos(flowUv.x * 16.0 - uTime * 0.7) * 0.038
    );
    float cells = fbm(swirlUv * 9.5 + vec2(uTime * 0.31, -uTime * 0.18));
    float fineCells = fbm(swirlUv * 26.0 + cells * 3.7 + vec2(-uTime * 0.22, uTime * 0.27));
    float filaments = smoothstep(0.42, 0.88, fineCells);
    float hotSpots = smoothstep(0.70, 0.97, fbm(swirlUv * 15.0 + cells * 4.5 + uTime * 0.38));
    float darkVeins = smoothstep(0.50, 0.83, fbm(swirlUv * 34.0 - cells * 2.0 - uTime * 0.16));
    float bands = 0.5 + 0.5 * sin((uv.y + cells * 0.24) * 42.0 + uTime * 2.3);

    vec3 cyan = uBaseColor;
    vec3 deepCyan = vec3(0.015, 0.48, 0.64);
    vec3 whiteHot = vec3(1.0, 0.98, 0.82);
    vec3 flare = vec3(1.0, 0.67, 0.20);
    vec3 plasma = mix(deepCyan, cyan, smoothstep(0.20, 0.82, cells));
    plasma = mix(plasma, whiteHot, smoothstep(0.48, 0.96, fineCells) * 0.72);
    plasma = mix(plasma, flare, filaments * (0.16 + bands * 0.34));
    plasma = mix(plasma, vec3(1.0, 1.0, 0.94), hotSpots * 0.78);
    plasma *= 1.0 - darkVeins * 0.18 * (1.0 - hotSpots);

    float pulse = 0.88 + 0.12 * sin(uTime * 2.4);
    float limb = 0.70 + pow(facing, 0.45) * 0.42;
    float corona = rim * (0.75 + filaments * 0.55 + hotSpots * 0.35);
    float energy = (0.58 + cells * 0.42 + fineCells * 0.36 + filaments * 0.58 + hotSpots * 0.78 + corona)
            * uPulseAmount * pulse * limb;
    float alpha = 1.0;

    return vec4(plasma * energy, alpha);
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vWorld);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.0) * uRimIntensity;

    vec4 color = stellarColor(vUv, facing, rim);

    float edgeDistance = min(vUv.x, 1.0 - vUv.x);
    float seamBlend = (1.0 - smoothstep(0.0, 0.070, edgeDistance)) * 0.5;
    float wrapDirection = mix(1.0, -1.0, step(0.5, vUv.x));
    vec4 wrappedColor = stellarColor(vUv + vec2(wrapDirection, 0.0), facing, rim);

    gl_FragColor = mix(color, wrappedColor, seamBlend);
}
