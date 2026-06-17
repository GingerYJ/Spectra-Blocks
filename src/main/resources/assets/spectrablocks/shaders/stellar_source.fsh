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

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vWorld);
    float rim = pow(1.0 - max(dot(normal, viewDir), 0.0), 2.4) * uRimIntensity;

    vec2 flowUv = vUv;
    flowUv.x += uTime * uNoiseSpeed * 0.11;
    flowUv.y += sin(vUv.x * 18.0 + uTime * 0.7) * 0.025;
    float cells = fbm(flowUv * 8.0 + vec2(uTime * 0.37, -uTime * 0.21));
    float filaments = smoothstep(0.42, 0.92, fbm(flowUv * 18.0 + cells * 2.6));
    float bands = 0.5 + 0.5 * sin((vUv.y + cells * 0.22) * 38.0 + uTime * 2.0);

    vec3 cyan = uBaseColor;
    vec3 whiteHot = vec3(1.0, 0.98, 0.86);
    vec3 flare = vec3(1.0, 0.72, 0.26);
    vec3 plasma = mix(cyan, whiteHot, smoothstep(0.30, 0.95, cells));
    plasma = mix(plasma, flare, filaments * (0.18 + bands * 0.30));

    float pulse = 0.86 + 0.14 * sin(uTime * 2.8);
    float energy = (0.35 + cells * 0.58 + filaments * 0.45 + rim * 0.80) * uPulseAmount * pulse;
    float alpha = clamp(0.72 + rim * 0.22 + filaments * 0.10, 0.0, 1.0);

    gl_FragColor = vec4(plasma * energy, alpha);
}
