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
    float rim = pow(1.0 - facing, 2.15) * uRimIntensity;

    float t = uTime * uNoiseSpeed;
    vec3 flowA = local + vec3(
        sin(local.y * 6.0 + t * 0.55),
        cos(local.z * 5.2 - t * 0.42),
        sin(local.x * 6.8 + t * 0.36)
    ) * 0.055;
    vec3 flowB = local + vec3(t * 0.030, -t * 0.021, t * 0.026);
    vec3 flowC = normalize(mix(flowA, flowB, 0.45));

    float cells = seamlessFbm(flowC + vec3(t * 0.018, -t * 0.012, t * 0.015), 4.2);
    float fineCells = seamlessFbm(flowC + cells * 0.32 + vec3(-t * 0.020, t * 0.016, -t * 0.014), 10.5);
    float filamentNoise = seamlessFbm(flowC + cells * 0.52 + vec3(t * 0.012, t * 0.018, -t * 0.011), 18.0);
    float hotNoise = seamlessFbm(flowC + fineCells * 0.45 + vec3(-t * 0.026, t * 0.024, t * 0.018), 7.2);
    float veinNoise = seamlessFbm(flowC - cells * 0.26 + vec3(t * 0.015, -t * 0.018, t * 0.010), 15.5);
    float filaments = smoothstep(0.48, 0.82, filamentNoise);
    float hotSpots = smoothstep(0.72, 0.95, hotNoise);
    float darkVeins = smoothstep(0.56, 0.86, veinNoise);
    float latitudeFlow = sin((local.y + cells * 0.18) * 18.0 + t * 1.55);
    float bands = 0.5 + 0.5 * latitudeFlow;

    vec3 cyan = uBaseColor;
    vec3 deepCyan = vec3(0.015, 0.48, 0.64);
    vec3 whiteHot = vec3(1.0, 0.98, 0.82);
    vec3 flare = vec3(1.0, 0.67, 0.20);
    vec3 plasma = mix(deepCyan, cyan, smoothstep(0.20, 0.82, cells));
    plasma = mix(plasma, whiteHot, smoothstep(0.48, 0.96, fineCells) * 0.72);
    plasma = mix(plasma, flare, filaments * (0.16 + bands * 0.34));
    plasma = mix(plasma, vec3(1.0, 1.0, 0.94), hotSpots * 0.78);
    plasma *= 1.0 - darkVeins * 0.10 * (1.0 - hotSpots);

    float pulse = 0.94 + 0.06 * sin(uTime * 1.65);
    float limb = 0.78 + pow(facing, 0.45) * 0.30;
    float corona = rim * (0.52 + filaments * 0.35 + hotSpots * 0.28);
    float energy = (0.58 + cells * 0.42 + fineCells * 0.36 + filaments * 0.58 + hotSpots * 0.78 + corona)
            * uPulseAmount * pulse * limb;
    float alpha = 1.0;

    gl_FragColor = vec4(plasma * energy, alpha);
}
