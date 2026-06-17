#version 120

uniform float uTime;
uniform float uLayerMode;
uniform float uAlpha;
uniform float uIntensity;
uniform float uNoiseScale;
uniform float uSeed;
uniform float uPulse;
uniform vec3 uPrimaryColor;
uniform vec3 uSecondaryColor;

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
        p = p * 2.02 + vec2(3.7 + uSeed * 0.13, 5.1 - uSeed * 0.07);
        amplitude *= 0.5;
    }
    return value;
}

float starPulse(float phase) {
    return 0.5 + 0.5 * sin(phase);
}

vec4 auraLayer(float facing, float rim, float pattern, vec2 uv) {
    float verticalFade = smoothstep(0.02, 0.42, uv.y) * (1.0 - smoothstep(0.72, 1.0, uv.y) * 0.35);
    float breathe = 0.72 + 0.28 * starPulse(uTime * 2.0 + uSeed);
    vec3 color = mix(uPrimaryColor * 0.55, uSecondaryColor, smoothstep(0.28, 0.92, pattern + rim * 0.55));
    float alpha = uAlpha * (0.42 + rim * 0.78 + pattern * 0.24) * verticalFade;
    return vec4(color * uIntensity * breathe * (0.75 + rim * 0.55), alpha);
}

vec4 coreLayer(float facing, float rim, float pattern, vec2 uv) {
    float hot = smoothstep(0.34, 0.92, pattern);
    float glow = 0.85 + 0.15 * sin(uTime * 4.4 + uSeed);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, hot * 0.82 + rim * 0.35);
    color = mix(color, vec3(1.0), smoothstep(0.76, 1.0, hot + facing * 0.20) * 0.45);
    return vec4(color * uIntensity * glow * (0.72 + hot * 0.58 + rim * 0.30), uAlpha);
}

vec4 ringLayer(float facing, float rim, float pattern, vec2 uv) {
    float band = smoothstep(0.04, 0.32, uv.y) * (1.0 - smoothstep(0.68, 0.96, uv.y));
    float runes = smoothstep(0.72, 0.98, starPulse(uv.x * 84.0 + uTime * 2.0 + uSeed));
    float edge = pow(abs(uv.y - 0.5) * 2.0, 2.2);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, max(runes, edge) * 0.82 + pattern * 0.20);
    float alpha = uAlpha * band * (0.70 + runes * 0.55 + rim * 0.20);
    return vec4(color * uIntensity * (0.82 + runes * 0.48), alpha);
}

vec4 moteLayer(float facing, float rim, float pattern, vec2 uv) {
    vec2 centered = uv - vec2(0.5);
    float radial = 1.0 - smoothstep(0.04, 0.72, length(centered));
    float sparkle = smoothstep(0.58, 0.98, pattern + starPulse(uTime * 8.0 + uSeed) * 0.18);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, sparkle * 0.82 + rim * 0.25);
    float alpha = uAlpha * (0.35 + radial * 0.85 + sparkle * 0.45);
    return vec4(color * uIntensity * (0.85 + sparkle * 0.75), alpha);
}

vec4 filamentLayer(float facing, float rim, float pattern, vec2 uv) {
    float traveling = smoothstep(0.56, 0.98, starPulse(uv.x * 18.0 - uTime * 7.0 + uSeed));
    float center = 1.0 - smoothstep(0.0, 0.52, abs(uv.y - 0.5) * 2.0);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, traveling * 0.65 + pattern * 0.25);
    float alpha = uAlpha * (0.42 + traveling * 0.62) * max(0.30, center);
    return vec4(color * uIntensity * (0.90 + traveling * 0.70), alpha);
}

vec4 petalLayer(float facing, float rim, float pattern, vec2 uv) {
    float tip = smoothstep(0.05, 1.0, uv.y);
    float vein = smoothstep(0.70, 0.98, starPulse((uv.x - 0.5) * 18.0 + uv.y * 7.0 + uTime * 2.2 + uSeed));
    float petalEdge = smoothstep(0.34, 0.98, abs(uv.x - 0.5) * 2.0);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, tip * 0.78 + vein * 0.20);
    color = mix(color, uSecondaryColor * 1.25, petalEdge * 0.26 + rim * 0.20);
    float alpha = uAlpha * (0.72 + tip * 0.22 + vein * 0.16);
    return vec4(color * uIntensity * (0.82 + vein * 0.32), alpha);
}

vec4 shardLayer(float facing, float rim, float pattern, vec2 uv) {
    float facet = smoothstep(0.42, 0.92, pattern + abs(uv.x - 0.5) * 0.35);
    float edge = smoothstep(0.74, 0.98, max(abs(uv.x - 0.5) * 2.0, uv.y));
    float glint = smoothstep(0.86, 0.99, starPulse(uv.y * 20.0 + uTime * 5.6 + uSeed));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, facet * 0.62 + glint * 0.25);
    color = mix(color, vec3(1.0), max(edge * 0.24, glint * 0.50));
    float alpha = uAlpha * (0.62 + facet * 0.24 + rim * 0.20);
    return vec4(color * uIntensity * (0.90 + glint * 0.55), alpha);
}

vec4 crystalLayer(float facing, float rim, float pattern, vec2 uv) {
    float vertical = smoothstep(0.0, 1.0, uv.y);
    float vein = smoothstep(0.58, 0.96, fbm(vec2(uv.x * uNoiseScale + uSeed, uv.y * uNoiseScale * 1.6 - uTime * 1.7)));
    float edge = smoothstep(0.62, 0.98, rim + abs(uv.x - 0.5) * 0.55);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, vertical * 0.45 + vein * 0.34 + edge * 0.42);
    color = mix(color, vec3(1.0, 0.90, 1.0), edge * 0.20);
    float alpha = uAlpha * (0.72 + edge * 0.22);
    return vec4(color * uIntensity * (0.82 + vein * 0.32 + edge * 0.36), alpha);
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vViewPos);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.0);
    vec2 flowUv = vUv + vec2(uTime * 0.06 + uSeed * 0.017, sin(uTime + uSeed) * 0.025);
    float pattern = fbm(flowUv * max(uNoiseScale, 0.1) + vec2(uSeed, -uSeed * 0.37));

    vec4 color;
    if (uLayerMode < 0.5) {
        color = auraLayer(facing, rim, pattern, vUv);
    } else if (uLayerMode < 1.5) {
        color = coreLayer(facing, rim, pattern, vUv);
    } else if (uLayerMode < 2.5) {
        color = ringLayer(facing, rim, pattern, vUv);
    } else if (uLayerMode < 3.5) {
        color = moteLayer(facing, rim, pattern, vUv);
    } else if (uLayerMode < 4.5) {
        color = filamentLayer(facing, rim, pattern, vUv);
    } else if (uLayerMode < 5.5) {
        color = petalLayer(facing, rim, pattern, vUv);
    } else if (uLayerMode < 6.5) {
        color = shardLayer(facing, rim, pattern, vUv);
    } else {
        color = crystalLayer(facing, rim, pattern, vUv);
    }

    color.a *= clamp(0.82 + uPulse * 0.30, 0.45, 1.35);
    if (color.a <= 0.01) {
        discard;
    }
    gl_FragColor = color;
}
