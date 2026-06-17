#version 120

uniform float uTime;
uniform float uEffect;
uniform float uLayer;
uniform float uAlpha;
uniform float uIntensity;
uniform float uScale;
uniform vec3 uPrimaryColor;
uniform vec3 uSecondaryColor;
uniform vec3 uTertiaryColor;

varying vec3 vNormal;
varying vec3 vView;
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
        p = p * 2.03 + vec2(6.7, 3.9);
        amplitude *= 0.5;
    }
    return value;
}

vec3 hue(float h) {
    vec3 p = abs(fract(h + vec3(0.0, 0.6667, 0.3333)) * 6.0 - 3.0);
    return clamp(p - 1.0, 0.0, 1.0);
}

float lineMask(float value, float width) {
    float centered = abs(fract(value) - 0.5);
    return 1.0 - smoothstep(width, width + 0.018, centered);
}

float pulse(float phase) {
    return 0.5 + 0.5 * sin(phase);
}

vec4 plasmaStorm(float facing, float rim) {
    vec2 flow = vUv * vec2(4.0, 2.4) + vec2(uTime * 0.18, -uTime * 0.11);
    float cells = fbm(flow + vLocal.xz * 0.35);
    float fine = fbm(flow * 3.4 + cells * 2.6 + uTime * 0.17);
    float bands = pulse(vUv.x * 42.0 + vLocal.y * 5.0 - uTime * 2.1);
    float lightning = smoothstep(0.74, 0.96, fine) * pulse(vUv.x * 90.0 - uTime * 7.0);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, smoothstep(0.16, 0.90, cells));
    color = mix(color, uTertiaryColor, bands * 0.34 + lightning * 0.38);
    color += vec3(1.0) * lightning * 0.72;
    float layerBoost = 0.62 + uLayer * 0.14;
    float alpha = uAlpha * (0.42 + rim * 0.36 + fine * 0.28 + bands * 0.18) * layerBoost;
    if (uLayer < 0.5) {
        alpha = uAlpha * (0.78 + pulse(uTime * 3.0) * 0.26);
        color = mix(color, vec3(1.0), 0.42 + fine * 0.34);
    }
    return vec4(color * (0.92 + uIntensity * 0.45 + rim * 0.38), clamp(alpha, 0.0, 0.96));
}

vec4 quantumBubble(float facing, float rim) {
    float latitude = lineMask(vUv.y * (8.0 + uLayer * 1.5) + sin(vUv.x * 13.0 + uTime) * 0.045, 0.055);
    float longitude = lineMask(vUv.x * (13.0 + uLayer * 2.0) + sin(vUv.y * 19.0 - uTime * 0.8) * 0.035, 0.050);
    float jump = pulse(uTime * 2.8 + floor(vUv.x * 17.0) * 0.73 + floor(vUv.y * 11.0) * 0.41);
    float grid = max(latitude, longitude) * (0.45 + jump * 0.55);
    float interference = fbm(vUv * 12.0 + vec2(uTime * 0.22, -uTime * 0.16));
    vec3 color = mix(uPrimaryColor * 0.72, uSecondaryColor, interference);
    color += uTertiaryColor * grid * (0.40 + rim * 0.50);
    float alpha = uAlpha * (0.32 + rim * 0.55 + grid * 0.70 + interference * 0.13);
    if (uLayer > 2.5) {
        alpha *= 1.35 + jump * 0.55;
        color = mix(color, vec3(1.0), 0.35);
    }
    return vec4(color * (0.82 + uIntensity * 0.42), clamp(alpha, 0.0, 0.94));
}

vec4 imaginaryCube(float facing, float rim) {
    vec3 p = abs(vLocal) / max(uScale, 0.001);
    float edge = max(max(p.x, p.y), p.z);
    float phase = fbm(vUv * 10.0 + vec2(uTime * 0.22, -uTime * 0.18));
    float glitch = step(0.68, pulse((vLocal.x + vLocal.y - vLocal.z) * 14.0 + uTime * 3.2));
    float scan = lineMask(vLocal.y * 5.5 - uTime * 0.8 + phase, 0.070);
    vec3 color = mix(uPrimaryColor, uSecondaryColor, phase * 0.72 + glitch * 0.22);
    color = mix(color, uTertiaryColor, rim * 0.28 + scan * 0.18);
    float alpha = uAlpha * (0.34 + rim * 0.45 + scan * 0.30 + glitch * 0.18);
    if (uLayer < 0.5) {
        alpha *= 0.55 + smoothstep(0.36, 0.98, edge) * 0.35;
    } else if (uLayer > 1.5) {
        alpha *= 1.18 + glitch * 0.42;
    }
    return vec4(color * (0.85 + uIntensity * 0.46), clamp(alpha, 0.0, 0.90));
}

vec4 spectralPrism(float facing, float rim) {
    float spectral = fract(vUv.x + vLocal.y * 0.13 + uTime * 0.018 + uLayer * 0.09);
    vec3 spectrum = hue(spectral);
    float facet = lineMask(vUv.x * 6.0 + vUv.y * 1.5, 0.060);
    float caustic = fbm(vUv * 18.0 + vec2(uTime * 0.20, -uTime * 0.13));
    vec3 color = mix(uPrimaryColor, spectrum, 0.72);
    color = mix(color, uSecondaryColor, facet * 0.30 + rim * 0.20);
    color += uTertiaryColor * smoothstep(0.66, 0.96, caustic) * 0.35;
    float alpha = uAlpha * (0.30 + facing * 0.25 + rim * 0.34 + facet * 0.28);
    if (uLayer > 2.5) {
        alpha *= 0.78 + smoothstep(0.04, 0.98, 1.0 - vUv.x) * 0.55;
    }
    return vec4(color * (0.88 + uIntensity * 0.50), clamp(alpha, 0.0, 0.92));
}

vec4 crystalField(float facing, float rim) {
    float shard = fbm(vUv * vec2(9.0, 18.0) + vec2(uTime * 0.10, -uTime * 0.09));
    float causticA = lineMask(vUv.x * 7.0 + shard * 0.8 + uTime * 0.20, 0.070);
    float causticB = lineMask((vUv.y + vUv.x * 0.35) * 8.0 - uTime * 0.16, 0.060);
    float prism = smoothstep(0.28, 0.92, shard);
    vec3 color = mix(uPrimaryColor * 0.72, uSecondaryColor, prism);
    color = mix(color, uTertiaryColor, (causticA + causticB) * 0.24);
    color += vec3(1.0) * max(causticA, causticB) * 0.24;
    float alpha = uAlpha * (0.24 + rim * 0.45 + prism * 0.22 + max(causticA, causticB) * 0.40);
    return vec4(color * (0.82 + uIntensity * 0.40), clamp(alpha, 0.0, 0.86));
}

vec4 dataStream(float facing, float rim) {
    vec2 cell = floor(vUv * vec2(9.0, 18.0));
    float code = hash(cell + floor(uTime * 5.0));
    float glyph = lineMask(vUv.x * 5.0 + code * 3.0, 0.080);
    glyph = max(glyph, lineMask(vUv.y * 9.0 + code * 4.0 - uTime * 0.8, 0.070));
    float scan = pulse(vLocal.y * 5.0 - uTime * 2.2 + code * 6.283);
    float core = fbm(vUv * 14.0 + vec2(0.0, -uTime * 0.22));
    vec3 color = mix(uPrimaryColor, uSecondaryColor, code * 0.65 + core * 0.35);
    color = mix(color, uTertiaryColor, glyph * 0.35 + step(0.88, scan) * 0.28);
    float alpha = uAlpha * (0.22 + glyph * 0.65 + rim * 0.22 + scan * 0.20);
    return vec4(color * (0.82 + uIntensity * 0.48), clamp(alpha, 0.0, 0.94));
}

vec4 energyNexus(float facing, float rim) {
    float conduit = lineMask(vUv.x * 6.0 - uTime * 0.42 + uLayer * 0.31, 0.080);
    float reactor = fbm(vUv * 12.0 + vec2(uTime * 0.18, -uTime * 0.15));
    float heartbeat = pulse(uTime * 2.3 + uLayer);
    float hot = smoothstep(0.58, 0.96, reactor) + conduit * 0.45;
    vec3 color = mix(uPrimaryColor * 0.78, uSecondaryColor, reactor);
    color = mix(color, uTertiaryColor, hot * 0.32 + heartbeat * 0.16);
    color += vec3(1.0) * hot * 0.18;
    float alpha = uAlpha * (0.28 + rim * 0.36 + reactor * 0.28 + conduit * 0.38 + heartbeat * 0.18);
    if (uLayer < 0.5) {
        alpha *= 1.08 + heartbeat * 0.30;
    }
    return vec4(color * (0.86 + uIntensity * 0.52), clamp(alpha, 0.0, 0.96));
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vView);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.0);

    vec4 color;
    if (uEffect < 0.5) {
        color = plasmaStorm(facing, rim);
    } else if (uEffect < 1.5) {
        color = quantumBubble(facing, rim);
    } else if (uEffect < 2.5) {
        color = imaginaryCube(facing, rim);
    } else if (uEffect < 3.5) {
        color = spectralPrism(facing, rim);
    } else if (uEffect < 4.5) {
        color = crystalField(facing, rim);
    } else if (uEffect < 5.5) {
        color = dataStream(facing, rim);
    } else {
        color = energyNexus(facing, rim);
    }

    gl_FragColor = color;
}
