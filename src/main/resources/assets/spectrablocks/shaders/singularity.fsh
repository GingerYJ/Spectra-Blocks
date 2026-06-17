#version 120

uniform float uTime;
uniform float uLayer;
uniform float uMode;
uniform float uAlpha;
uniform float uGridAlpha;
uniform float uPulse;
uniform float uGridPulse;
uniform vec3 uPrimaryColor;
uniform vec3 uGridColor;

varying vec3 vNormal;
varying vec3 vView;
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
        p = p * 2.02 + vec2(9.7, 4.3);
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vView);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.25);

    vec2 centered = vUv - vec2(0.5);
    float angle = atan(centered.y, centered.x);
    float radius = length(centered);
    float swirl = fbm(vec2(angle * 1.85 + uTime * (1.0 + uLayer * 0.22), radius * 15.0 - uTime * 1.35));
    float fine = fbm(vUv * (18.0 + uLayer * 7.0) + vec2(uTime * 0.8, -uTime * 0.45) + swirl);
    float rings = 0.5 + 0.5 * sin(radius * (60.0 + uLayer * 15.0) - uTime * (2.6 + uLayer));
    float filamentA = smoothstep(0.72, 0.96, fbm(vec2(angle * 2.4 + uTime * 0.85, radius * 18.0 + swirl * 2.0)));
    float filamentB = smoothstep(0.78, 0.98, fbm(vec2(angle * -1.7 - uTime * 0.55, radius * 24.0 - fine * 2.5)));
    float grid = (filamentA * 0.55 + filamentB * 0.35 + rings * 0.10) * (0.45 + 0.30 * uGridPulse);

    float isWhite = step(0.5, uMode);
    vec3 blackHalo = mix(uPrimaryColor * 0.78, uGridColor * 0.92, smoothstep(0.36, 0.92, swirl));
    blackHalo += uGridColor * (fine * 0.18 + rings * 0.10);
    vec3 whiteHalo = mix(uPrimaryColor * 0.88, vec3(1.0, 0.97, 0.74), smoothstep(0.22, 0.88, swirl));
    whiteHalo += vec3(1.0, 0.86, 0.40) * (fine * 0.24 + rings * 0.14);
    vec3 color = mix(blackHalo, whiteHalo, isWhite);

    if (uLayer < 0.5) {
        float coreFalloff = smoothstep(0.0, 0.42, facing);
        vec3 blackCore = vec3(0.0, 0.0, 0.0);
        vec3 whiteCore = vec3(1.0, 0.98, 0.82) * (1.18 + fine * 0.44);
        color = mix(blackCore, whiteCore, isWhite);
        float coreAlpha = mix(0.98, 0.92, isWhite) * coreFalloff;
        gl_FragColor = vec4(color, coreAlpha * uAlpha);
        return;
    }

    color += uGridColor * grid * uGridAlpha * (0.52 + isWhite * 0.22);
    float glow = rim * (0.38 + uLayer * 0.18) + swirl * 0.10 + rings * 0.055;
    float alpha = uAlpha * (0.62 + uPulse * 0.24 + glow);
    alpha += grid * uGridAlpha * 0.22;
    alpha *= mix(0.84, 1.08, isWhite);

    gl_FragColor = vec4(color, clamp(alpha, 0.0, 0.92));
}
