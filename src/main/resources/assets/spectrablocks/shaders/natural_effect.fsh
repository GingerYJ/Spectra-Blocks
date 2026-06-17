#version 120

uniform float uTime;
uniform float uMode;
uniform float uLayer;
uniform float uAlpha;
uniform float uPulse;
uniform float uIntensity;
uniform float uSeed;
uniform vec3 uPrimaryColor;
uniform vec3 uSecondaryColor;
uniform vec3 uAccentColor;

varying vec3 vNormal;
varying vec3 vViewPos;
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
        p = p * 2.08 + vec2(5.17, 9.31);
        amplitude *= 0.5;
    }
    return value;
}

float rings(vec2 uv, float scale, float speed) {
    vec2 centered = uv - vec2(0.5);
    float r = length(centered) * scale;
    return 0.5 + 0.5 * sin(r - uTime * speed + uLayer * 1.7);
}

vec4 stardust(float facing, float rim) {
    vec2 flow = vUv + vec2(uTime * 0.035, -uTime * 0.018 + uLayer * 0.07);
    float dust = fbm(flow * (9.0 + uLayer * 2.0));
    float fine = fbm(flow * 31.0 + dust * 3.0 + uSeed);
    float sparkle = smoothstep(0.74, 0.98, fine + 0.10 * sin(uTime * 7.0 + uSeed));
    float fountain = smoothstep(0.05, 0.95, vUv.y) * (1.0 - smoothstep(0.84, 1.0, abs(vUv.x - 0.5) * 2.0));
    float glow = 0.36 + dust * 0.52 + sparkle * 1.15 + rim * 0.8 + fountain * 0.35;
    vec3 color = mix(uPrimaryColor, uSecondaryColor, dust);
    color = mix(color, uAccentColor, sparkle * 0.86);
    float alpha = uAlpha * (0.28 + glow * 0.72) * (0.72 + uPulse * 0.35);
    return vec4(color * glow * uIntensity, alpha);
}

vec4 aurora(float facing, float rim) {
    vec2 uv = vUv;
    float curtain = sin((uv.x + fbm(vec2(uv.y * 1.8, uSeed)) * 0.15) * 18.0
            + uTime * (1.1 + uLayer * 0.12) + uSeed);
    float ribbons = smoothstep(-0.22, 0.95, curtain);
    float fine = fbm(vec2(uv.x * 6.0 + uTime * 0.12, uv.y * 3.6 - uTime * 0.07 + uLayer));
    float verticalFade = smoothstep(0.0, 0.18, uv.y) * (1.0 - smoothstep(0.82, 1.0, uv.y));
    float sideFade = smoothstep(0.0, 0.10, uv.x) * (1.0 - smoothstep(0.90, 1.0, uv.x));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, smoothstep(0.12, 0.92, uv.y + fine * 0.20));
    color = mix(color, uAccentColor, smoothstep(0.64, 1.0, ribbons + fine * 0.28));
    float energy = (0.42 + ribbons * 0.55 + fine * 0.38 + rim * 0.35) * uIntensity;
    float alpha = uAlpha * verticalFade * sideFade * (0.35 + ribbons * 0.75 + fine * 0.28);
    return vec4(color * energy, alpha);
}

vec4 abyssal(float facing, float rim) {
    vec2 flow = vec2(vUv.x + uTime * 0.025, vUv.y - uTime * 0.018);
    float current = fbm(flow * (5.0 + uLayer * 1.5) + uSeed);
    float caustic = smoothstep(0.56, 0.90, rings(vUv + current * 0.10, 42.0, 1.1));
    float plankton = smoothstep(0.72, 0.98, fbm(flow * 30.0 + current * 4.0));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, current);
    color = mix(color, uAccentColor, caustic * 0.55 + plankton * 0.65);
    float energy = (0.35 + current * 0.42 + caustic * 0.65 + plankton * 0.85 + rim * 0.55)
            * (0.82 + uPulse * 0.28) * uIntensity;
    float alpha = uAlpha * (0.34 + current * 0.40 + caustic * 0.34 + plankton * 0.28);
    return vec4(color * energy, alpha);
}

vec4 storm(float facing, float rim) {
    vec2 swirl = vUv - vec2(0.5);
    float angle = atan(swirl.y, swirl.x);
    float radius = length(swirl);
    float curl = fbm(vec2(angle * 2.1 + uTime * 0.42, radius * 8.5 - uTime * 0.24 + uSeed));
    float bands = 0.5 + 0.5 * sin(angle * (6.0 + uLayer) + radius * 19.0 - uTime * 1.8 + curl * 4.0);
    float lightning = smoothstep(0.86, 0.995, fbm(vec2(angle * 7.0 + uSeed, radius * 23.0 - uTime * 3.6)));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, curl);
    color = mix(color, uAccentColor, smoothstep(0.58, 0.98, bands) * 0.55 + lightning * 0.95);
    float energy = (0.28 + bands * 0.45 + curl * 0.32 + lightning * 1.25 + rim * 0.46)
            * (0.90 + uPulse * 0.24) * uIntensity;
    float alpha = uAlpha * (0.36 + bands * 0.35 + curl * 0.26 + lightning * 0.35);
    return vec4(color * energy, alpha);
}

vec4 entropy(float facing, float rim) {
    vec2 uv = vUv + vec2(uTime * 0.012, -uTime * 0.009 + uLayer * 0.04);
    float soft = fbm(uv * (4.0 + uLayer * 1.3) + uSeed);
    float grain = fbm(uv * 35.0 + soft * 6.0 - uTime * 0.20);
    float cracks = smoothstep(0.76, 0.91, grain) * (1.0 - smoothstep(0.91, 1.0, grain));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, soft);
    color = mix(color, uAccentColor, smoothstep(0.66, 0.98, soft + grain * 0.22));
    color *= 1.0 - cracks * 0.78;
    float energy = (0.20 + soft * 0.42 + grain * 0.18 + rim * 0.38) * uIntensity;
    float alpha = uAlpha * (0.44 + soft * 0.42 + cracks * 0.10) * (0.84 + uPulse * 0.20);
    return vec4(color * energy, alpha);
}

vec4 solar(float facing, float rim) {
    vec2 flow = vUv + vec2(uTime * 0.060, sin(vUv.x * 14.0 + uTime) * 0.025);
    float cell = fbm(flow * (8.0 + uLayer * 2.0) + uSeed);
    float filament = smoothstep(0.52, 0.92, fbm(flow * 28.0 + cell * 4.8 + uTime * 0.22));
    float flare = smoothstep(0.70, 0.98, fbm(vec2(vUv.y * 18.0, vUv.x * 9.0 - uTime * 0.42 + uSeed)));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, cell);
    color = mix(color, uAccentColor, filament * 0.72 + flare * 0.58 + rim * 0.35);
    float energy = (0.62 + cell * 0.45 + filament * 0.95 + flare * 0.82 + rim * 1.10)
            * (0.92 + uPulse * 0.40) * uIntensity;
    float alpha = uAlpha * (0.55 + filament * 0.28 + flare * 0.30 + rim * 0.22);
    return vec4(color * energy, alpha);
}

vec4 hourglass(float facing, float rim) {
    vec2 uv = vUv;
    float pinch = abs(uv.y - 0.5) * 2.0;
    float dustChannel = 1.0 - smoothstep(0.07, 0.38, abs(uv.x - 0.5) + (1.0 - pinch) * 0.22);
    float nebula = fbm(uv * vec2(8.0, 5.0) + vec2(uTime * 0.030, uSeed));
    float stars = smoothstep(0.77, 0.99, fbm(uv * 38.0 + nebula * 5.0 - uTime * 0.18));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, smoothstep(0.20, 0.82, uv.y));
    color = mix(color, uAccentColor, stars * 0.80 + dustChannel * 0.35);
    float energy = (0.30 + nebula * 0.42 + stars * 0.90 + dustChannel * 0.55 + rim * 0.45)
            * (0.88 + uPulse * 0.22) * uIntensity;
    float alpha = uAlpha * (0.34 + nebula * 0.42 + stars * 0.24 + dustChannel * 0.22);
    return vec4(color * energy, alpha);
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vViewPos);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.0);

    vec4 color;
    if (uMode < 0.5) {
        color = stardust(facing, rim);
    } else if (uMode < 1.5) {
        color = aurora(facing, rim);
    } else if (uMode < 2.5) {
        color = abyssal(facing, rim);
    } else if (uMode < 3.5) {
        color = storm(facing, rim);
    } else if (uMode < 4.5) {
        color = entropy(facing, rim);
    } else if (uMode < 5.5) {
        color = solar(facing, rim);
    } else {
        color = hourglass(facing, rim);
    }

    float edgeDistance = min(vUv.x, 1.0 - vUv.x);
    float seamBlend = (1.0 - smoothstep(0.0, 0.065, edgeDistance)) * 0.5;
    vec2 wrapUv = vUv + vec2(mix(1.0, -1.0, step(0.5, vUv.x)), 0.0);
    vec2 originalUv = vUv;
    vec4 wrapColor = color;
    if (seamBlend > 0.001 && uMode != 1.0) {
        wrapColor.rgb *= 0.95 + 0.05 * fbm(wrapUv * 8.0 + originalUv.yx);
    }

    gl_FragColor = mix(color, wrapColor, seamBlend);
}
