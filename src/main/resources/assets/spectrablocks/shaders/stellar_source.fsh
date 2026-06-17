#version 120

uniform float uTime;
uniform vec3 uBaseColor;
uniform float uRimIntensity;
uniform float uPulseAmount;
uniform float uNoiseSpeed;

varying vec3 vNormal;
varying vec3 vWorld;
varying vec3 vLocal;
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

float seamlessFbm(vec3 p, float scale) {
    vec3 q = p * scale;
    vec3 weights = abs(normalize(p));
    weights = pow(weights, vec3(3.0));
    weights /= weights.x + weights.y + weights.z + 0.0001;

    float xy = fbm(q.xy);
    float yz = fbm(q.yz + vec2(19.1, 7.3));
    float zx = fbm(q.zx + vec2(3.7, 23.6));
    return xy * weights.z + yz * weights.x + zx * weights.y;
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 local = normalize(vLocal);
    vec3 viewDir = normalize(-vWorld);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 1.75) * uRimIntensity;

    float t = uTime * uNoiseSpeed;
    vec3 flowA = local + vec3(
        sin(local.y * 7.8 + t * 0.85),
        cos(local.z * 6.4 - t * 0.64),
        sin(local.x * 7.2 + t * 0.58)
    ) * 0.095;
    vec3 flowB = local + vec3(t * 0.042, -t * 0.031, t * 0.036);
    vec3 flowC = normalize(mix(flowA, flowB, 0.38));

    float cells = seamlessFbm(flowC + vec3(t * 0.026, -t * 0.017, t * 0.020), 3.4);
    float midCells = seamlessFbm(flowC + cells * 0.48 + vec3(-t * 0.030, t * 0.021, -t * 0.019), 7.8);
    float fineCells = seamlessFbm(flowC + midCells * 0.58 + vec3(t * 0.018, t * 0.025, -t * 0.015), 17.5);
    float filamentNoise = seamlessFbm(flowC + cells * 0.76 + fineCells * 0.22 + vec3(t * 0.020, -t * 0.016, t * 0.013), 23.0);
    float hotNoise = seamlessFbm(flowC + fineCells * 0.62 + vec3(-t * 0.040, t * 0.035, t * 0.028), 9.6);
    float veinNoise = seamlessFbm(flowC - cells * 0.42 + vec3(t * 0.023, -t * 0.026, t * 0.016), 15.0);
    float flowBandA = 0.5 + 0.5 * sin((local.y + cells * 0.34 + midCells * 0.18) * 24.0 + t * 2.45);
    float flowBandB = 0.5 + 0.5 * sin((local.x * 0.65 + local.z * 0.78 + midCells * 0.42) * 20.0 - t * 2.05);
    float filaments = smoothstep(0.36, 0.76, filamentNoise * 0.58 + flowBandA * 0.36 + flowBandB * 0.28);
    float hotSpots = smoothstep(0.60, 0.90, hotNoise + filaments * 0.24);
    float darkVeins = smoothstep(0.60, 0.88, veinNoise) * (1.0 - hotSpots * 0.55);
    float bands = clamp(flowBandA * 0.62 + flowBandB * 0.38, 0.0, 1.0);

    vec3 cyan = uBaseColor;
    vec3 deepCyan = vec3(0.00, 0.32, 0.46);
    vec3 brightCyan = vec3(0.10, 0.96, 1.00);
    vec3 whiteHot = vec3(0.96, 1.00, 0.96);
    vec3 warmCore = vec3(1.00, 0.92, 0.55);
    vec3 plasma = mix(deepCyan, brightCyan, smoothstep(0.18, 0.76, cells + midCells * 0.32));
    plasma = mix(plasma, whiteHot, filaments * (0.52 + bands * 0.34));
    plasma = mix(plasma, warmCore, hotSpots * 0.38);
    plasma = mix(plasma, vec3(1.0, 1.0, 1.0), hotSpots * 0.42);
    plasma *= 1.0 - darkVeins * 0.18;

    float pulse = 0.92 + 0.08 * sin(uTime * 1.90 + cells * 1.4);
    float limb = 0.84 + pow(facing, 0.58) * 0.22;
    float corona = rim * (0.62 + filaments * 0.42 + hotSpots * 0.25);
    float surface = 0.74 + cells * 0.30 + midCells * 0.28 + fineCells * 0.22
            + filaments * 0.90 + hotSpots * 1.08 - darkVeins * 0.18;
    float energy = (surface + corona)
            * uPulseAmount * pulse * limb;
    float alpha = 1.0;

    gl_FragColor = vec4(plasma * energy, alpha);
}
