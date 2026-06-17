#version 120

uniform float uTime;
uniform float uEffect;
uniform float uLayer;
uniform float uAlpha;
uniform float uIntensity;
uniform float uSeed;
uniform vec3 uPrimaryColor;
uniform vec3 uAccentColor;

varying vec3 vNormal;
varying vec3 vView;
varying vec3 vLocalPos;
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
        p = p * 2.03 + vec2(7.1, 3.9);
        amplitude *= 0.5;
    }
    return value;
}

float lineMask(float position, float width) {
    float centered = abs(fract(position) - 0.5);
    return 1.0 - smoothstep(width, width + 0.030, centered);
}

vec4 galaxyColor(float facing, float rim) {
    vec2 centered = vUv - vec2(0.5);
    float radius = length(centered);
    float angle = atan(centered.y, centered.x);
    float swirl = fbm(vec2(angle * 1.8 + uTime * 0.55 + uSeed, radius * 16.0 - uTime * 0.8));
    float arms = 0.5 + 0.5 * sin(angle * 4.0 - radius * 18.0 + uTime * 1.6 + swirl * 3.8 + uSeed);
    float dust = fbm(vUv * (12.0 + uLayer) + vec2(uTime * 0.45, -uTime * 0.22) + swirl);
    float star = smoothstep(0.90, 0.995, hash(floor(vUv * 90.0 + uSeed * 17.0)));
    float stream = lineMask(vUv.x * (8.0 + uLayer * 0.55) + uTime * 0.35 + uSeed, 0.040);

    vec3 color = mix(uPrimaryColor * 0.55, uAccentColor, smoothstep(0.22, 0.92, arms * 0.65 + dust * 0.35));
    color += vec3(1.0, 0.96, 0.86) * star * (0.35 + rim * 0.65);
    color += uAccentColor * stream * 0.18;
    color *= uIntensity * (0.72 + dust * 0.40 + rim * 0.35 + facing * 0.14);

    float alpha = uAlpha * (0.54 + dust * 0.28 + arms * 0.22 + rim * 0.30);
    if (uLayer > 6.5) {
        alpha = uAlpha * (0.74 + star * 0.34 + rim * 0.28);
        color += vec3(1.0) * star * 0.65;
    }
    if (uLayer > 7.5 && uLayer < 9.5) {
        alpha *= 0.55 + stream * 0.45;
        color += uAccentColor * stream * 0.50;
    }

    return vec4(color, clamp(alpha, 0.0, 0.95));
}

vec4 nebulaColor(float facing, float rim) {
    vec2 flow = vUv + vec2(uTime * (0.10 + uSeed * 0.03), -uTime * 0.045);
    float cloudA = fbm(flow * (5.5 + uLayer * 0.8) + uSeed * 5.0);
    float cloudB = fbm(vec2(flow.y, flow.x) * (9.0 + uLayer * 0.35) - uTime * 0.20);
    float filaments = smoothstep(0.55, 0.90, cloudA + cloudB * 0.45);
    float sparks = smoothstep(0.92, 0.995, hash(floor(vUv * (68.0 + uLayer * 4.0) + uSeed * 31.0)));

    vec3 color = mix(uPrimaryColor * 0.62, uAccentColor, smoothstep(0.25, 0.88, cloudA));
    color += uAccentColor * filaments * 0.30;
    color += vec3(1.0, 0.94, 0.86) * sparks * (0.45 + rim * 0.35);
    color *= uIntensity * (0.70 + cloudB * 0.46 + rim * 0.38 + facing * 0.12);

    float alpha = uAlpha * (0.46 + cloudA * 0.35 + filaments * 0.22 + rim * 0.32);
    if (uLayer > 5.5) {
        alpha *= 0.58 + sparks * 0.70;
    }
    if (uLayer > 7.5) {
        float stream = lineMask(vUv.x * 7.0 + cloudA * 1.5 + uTime * 0.30 + uSeed, 0.055);
        color += vec3(1.0) * stream * 0.25;
        alpha *= 0.62 + stream * 0.45;
    }

    return vec4(color, clamp(alpha, 0.0, 0.92));
}

vec4 collapsingStarColor(float facing, float rim) {
    vec2 centered = vUv - vec2(0.5);
    float radius = length(centered);
    float angle = atan(centered.y, centered.x);
    float vortex = fbm(vec2(angle * 2.3 - uTime * 1.25 + uSeed, radius * 20.0 + uTime * 0.85));
    float heatBands = 0.5 + 0.5 * sin(radius * 48.0 - angle * 2.0 + uTime * 3.0 + vortex * 3.0);
    float sparks = smoothstep(0.88, 0.99, hash(floor(vUv * 80.0 + uSeed * 19.0)));

    vec3 color = mix(uPrimaryColor * 0.62, uAccentColor, smoothstep(0.22, 0.88, heatBands * 0.55 + vortex * 0.45));
    color += vec3(1.0, 0.92, 0.72) * sparks * 0.38;
    color *= uIntensity * (0.74 + vortex * 0.35 + rim * 0.46 + facing * 0.10);

    float alpha = uAlpha * (0.55 + heatBands * 0.22 + vortex * 0.26 + rim * 0.38);
    if (uLayer < 1.0) {
        float coreShade = smoothstep(0.0, 0.45, facing);
        color = mix(vec3(0.0), uPrimaryColor * 0.08, rim);
        alpha = uAlpha * coreShade;
    } else if (uLayer > 7.5) {
        float trail = lineMask(vUv.x * 5.5 - uTime * 0.65 + uSeed, 0.070);
        color += uAccentColor * trail * 0.45;
        alpha *= 0.56 + trail * 0.44;
    }

    return vec4(color, clamp(alpha, 0.0, 0.96));
}

vec4 backgroundRadiationColor(float facing, float rim) {
    vec2 drift = vUv + vec2(uTime * 0.035 + uSeed * 0.07, -uTime * 0.020);
    float low = fbm(drift * 7.0);
    float high = fbm(drift * 28.0 + low * 2.0);
    float contourA = lineMask((vUv.y + low * 0.18) * 13.0 + uTime * 0.07 + uSeed, 0.045);
    float contourB = lineMask((vUv.x + high * 0.08) * 9.0 - uTime * 0.04 + uSeed, 0.035);
    float speckle = smoothstep(0.86, 0.995, hash(floor(vUv * 110.0 + uSeed * 23.0)));
    float temperature = low - high * 0.34;

    vec3 cold = vec3(0.55, 0.70, 1.0);
    vec3 warm = vec3(1.0, 0.72, 0.48);
    vec3 color = mix(cold, warm, smoothstep(0.20, 0.82, temperature));
    color = mix(color, uPrimaryColor, 0.30);
    color += uAccentColor * (contourA * 0.16 + contourB * 0.12);
    color += vec3(1.0) * speckle * 0.22;
    color *= uIntensity * (0.66 + low * 0.32 + rim * 0.42 + facing * 0.10);

    float alpha = uAlpha * (0.50 + low * 0.22 + rim * 0.38);
    if (uLayer > 2.5 && uLayer < 5.5) {
        alpha *= 0.58 + (contourA + contourB) * 0.32;
    }
    if (uLayer > 5.5) {
        alpha *= 0.52 + speckle * 0.62;
    }

    return vec4(color, clamp(alpha, 0.0, 0.86));
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vView);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.1);

    vec4 color;
    if (uEffect < 0.5) {
        color = galaxyColor(facing, rim);
    } else if (uEffect < 1.5) {
        color = nebulaColor(facing, rim);
    } else if (uEffect < 2.5) {
        color = collapsingStarColor(facing, rim);
    } else {
        color = backgroundRadiationColor(facing, rim);
    }

    gl_FragColor = color;
}
