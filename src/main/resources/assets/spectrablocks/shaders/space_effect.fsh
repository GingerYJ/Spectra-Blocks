#version 120

uniform float uTime;
uniform float uEffect;
uniform float uLayer;
uniform float uAlpha;
uniform float uSeed;
uniform float uPulse;
uniform vec3 uPrimaryColor;
uniform vec3 uSecondaryColor;
uniform vec3 uAccentColor;
uniform vec3 uHighlightColor;

varying vec3 vNormal;
varying vec3 vView;
varying vec3 vLocal;
varying vec2 vUv;

const float PI = 3.141592653589793;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

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
        p = p * 2.03 + vec2(7.7, 4.1);
        amplitude *= 0.5;
    }
    return value;
}

float softRing(float radius, float target, float width) {
    return 1.0 - smoothstep(0.0, width, abs(radius - target));
}

float starCell(vec2 uv, vec2 cells, float threshold, float radius) {
    vec2 grid = uv * cells;
    vec2 id = floor(grid);
    vec2 local = fract(grid) - 0.5;
    float gate = step(threshold, hash(id + uSeed * 37.0));
    float sparkle = 1.0 - smoothstep(radius * 0.35, radius, length(local));
    float twinkle = 0.55 + 0.45 * sin(uTime * 7.0 + hash(id + 4.2) * 24.0);
    return gate * sparkle * twinkle;
}

float sphereRim() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vView);
    return pow(1.0 - saturate(abs(dot(normal, viewDir))), 2.0);
}

float sphereFacing() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vView);
    return saturate(abs(dot(normal, viewDir)));
}

vec4 spatialRift() {
    vec2 p = vUv * 2.0 - 1.0;
    float layer = floor(uLayer + 0.5);

    if (layer > 0.5) {
        vec3 n = normalize(vLocal);
        vec2 uv = vec2(atan(n.z, n.x) / (2.0 * PI) + 0.5, n.y * 0.5 + 0.5);
        float shards = starCell(uv + vec2(uTime * 0.055, -uTime * 0.018), vec2(32.0, 18.0), 0.67, 0.25);
        float streak = smoothstep(0.72, 0.97, fbm(vec2(uv.x * 8.0 + uTime * 0.8, uv.y * 22.0)));
        float rim = sphereRim();
        vec3 color = mix(uSecondaryColor, uAccentColor, hash(floor(uv * 24.0)));
        color = mix(color, uHighlightColor, shards * 0.82);
        float alpha = (shards * 0.68 + streak * 0.08 + rim * 0.10) * uAlpha;
        return vec4(color, saturate(alpha));
    }

    float y = p.y;
    float taper = sin((y * 0.5 + 0.5) * PI);
    float center = sin(y * 8.2 + uTime * 1.05 + uSeed * 11.0) * 0.075
            + sin(y * 21.0 + uSeed * 17.0) * 0.040;
    float jagged = fbm(vec2(y * 8.0 + uSeed * 9.0, uTime * 0.82));
    float width = (0.065 + jagged * 0.135) * (0.18 + 0.82 * taper);
    float dist = abs(p.x - center);
    float body = 1.0 - smoothstep(width * 0.72, width * 1.14, dist);
    float edge = 1.0 - smoothstep(0.0, 0.038, abs(dist - width));
    float flash = pow(max(0.0, sin(uTime * 12.0 + y * 23.0 + uSeed * 19.0)), 8.0);
    float rippleRadius = length(vec2((p.x - center) * 1.15, p.y * 0.68));
    float ripples = 0.0;
    for (int i = 0; i < 5; i++) {
        float age = fract(uTime * 0.17 + float(i) * 0.217 + uSeed);
        ripples += softRing(rippleRadius, 0.22 + age * 0.78, 0.018 + age * 0.018)
                * sin(age * PI) * (1.0 - smoothstep(0.92, 1.18, rippleRadius));
    }
    float speck = starCell(vUv + vec2(uTime * 0.04, 0.0), vec2(22.0, 54.0), 0.83, 0.22);
    vec3 voidColor = uPrimaryColor * (0.56 + jagged * 0.18);
    vec3 glowColor = mix(uSecondaryColor, uAccentColor, edge * 0.55 + flash * 0.45);
    vec3 color = mix(voidColor, glowColor, saturate(edge + ripples * 0.42 + speck * 0.6));
    color = mix(color, uHighlightColor, flash * edge);
    float alpha = (body * 0.74 + edge * 0.56 + ripples * 0.16 + speck * 0.28 + flash * 0.34) * uAlpha;
    return vec4(color, saturate(alpha));
}

vec4 wormhole() {
    float layer = floor(uLayer + 0.5);
    float rim = sphereRim();
    float facing = sphereFacing();

    if (layer < 0.5) {
        vec2 uv = vUv + vec2(uTime * 0.18, -uTime * 0.06);
        float clouds = fbm(uv * 8.0 + uSeed * 4.0);
        float rings = 0.5 + 0.5 * sin((vUv.y + clouds * 0.14) * 46.0 - uTime * 2.7);
        vec3 color = mix(uPrimaryColor, uSecondaryColor * 0.42, clouds * 0.42);
        color += uAccentColor * rings * 0.07;
        float alpha = uAlpha * (0.78 + facing * 0.20);
        return vec4(color, saturate(alpha));
    }

    if (layer < 1.5 || layer > 2.5) {
        vec3 n = normalize(vLocal);
        vec2 uv = vec2(atan(n.z, n.x) / (2.0 * PI) + 0.5, n.y * 0.5 + 0.5);
        float flow = fbm(vec2(uv.x * 9.0 - uTime * 0.85, uv.y * 14.0 + uTime * 0.30));
        float band = 0.5 + 0.5 * sin(uv.x * 38.0 + flow * 6.0 - uTime * 3.0);
        float stars = starCell(uv + vec2(-uTime * 0.04, uTime * 0.02), vec2(38.0, 16.0), 0.78, 0.22);
        vec3 color = mix(uSecondaryColor * 0.55, uAccentColor, smoothstep(0.35, 0.92, flow));
        color = mix(color, uHighlightColor, stars * 0.85);
        float alpha = (rim * 0.28 + flow * 0.10 + band * 0.12 + stars * 0.42) * uAlpha;
        return vec4(color, saturate(alpha));
    }

    vec2 p = vUv * 2.0 - 1.0;
    float r = length(p);
    float angle = atan(p.y, p.x);
    float mask = 1.0 - smoothstep(0.82, 1.0, r);
    float sink = 1.0 - smoothstep(0.0, 0.30, r);
    float twist = angle * 5.0 - r * 16.0 + uTime * 4.0 + uSeed * 9.0;
    float arms = pow(max(0.0, 0.5 + 0.5 * sin(twist)), 3.0);
    float fine = fbm(vec2(angle * 2.0 + uTime, r * 18.0 - uTime * 1.5));
    float ring = softRing(r, 0.48 + sin(uTime + uSeed) * 0.04, 0.028)
            + softRing(r, 0.72, 0.022) * 0.7;
    float particles = starCell(vUv + vec2(-uTime * 0.10, uTime * 0.04), vec2(30.0, 30.0), 0.74, 0.22);
    vec3 color = mix(uSecondaryColor, uAccentColor, arms * 0.68 + fine * 0.24);
    color = mix(color, uHighlightColor, sink * 0.45 + particles * 0.70);
    float alpha = (arms * 0.42 + ring * 0.30 + particles * 0.35 + sink * 0.20) * mask * uAlpha;
    return vec4(color, saturate(alpha));
}

vec4 gravitationalLens() {
    float layer = floor(uLayer + 0.5);
    float rim = sphereRim();

    if (layer < 1.5) {
        vec3 n = normalize(vLocal);
        vec2 uv = vec2(atan(n.z, n.x) / (2.0 * PI) + 0.5, n.y * 0.5 + 0.5);
        float grid = softRing(fract(uv.x * 12.0 + uTime * 0.05), 0.5, 0.020)
                + softRing(fract(uv.y * 7.0 - uTime * 0.03), 0.5, 0.020);
        float caustic = smoothstep(0.56, 0.96, fbm(vec2(uv.x * 10.0 + uTime * 0.4, uv.y * 18.0 - uTime * 0.2)));
        vec3 color = mix(uPrimaryColor, uSecondaryColor, rim * 0.65 + caustic * 0.18);
        color += uAccentColor * grid * 0.25;
        float alpha = (rim * 0.34 + caustic * 0.08 + grid * 0.10) * uAlpha;
        return vec4(color, saturate(alpha));
    }

    if (layer < 2.5) {
        vec2 p = vUv * 2.0 - 1.0;
        float r = length(vec2(p.x, p.y * 1.72));
        float angle = atan(p.y * 1.72, p.x);
        float arcs = 0.0;
        for (int i = 0; i < 8; i++) {
            float band = softRing(r, 0.16 + float(i) * 0.088, 0.012);
            float gate = smoothstep(0.15, 0.80, sin(angle * (2.0 + float(i) * 0.21) + uTime * 0.9 + float(i)));
            arcs += band * gate * (1.0 - smoothstep(0.86, 1.08, r));
        }
        float shimmer = smoothstep(0.62, 0.96, fbm(vec2(angle * 3.0 + uTime, r * 22.0)));
        vec3 color = mix(uAccentColor, uSecondaryColor, shimmer * 0.55);
        color = mix(color, uHighlightColor, arcs * 0.28);
        return vec4(color, saturate((arcs * 0.44 + shimmer * 0.08) * uAlpha));
    }

    vec2 p = vUv * 2.0 - 1.0;
    float r = length(p);
    float flare = (1.0 - smoothstep(0.0, 0.88, r))
            + (1.0 - smoothstep(0.0, 0.055, abs(p.x))) * (1.0 - smoothstep(0.0, 0.92, abs(p.y))) * 0.42
            + (1.0 - smoothstep(0.0, 0.055, abs(p.y))) * (1.0 - smoothstep(0.0, 0.92, abs(p.x))) * 0.34;
    vec3 color = mix(uAccentColor, uHighlightColor, flare * 0.65);
    return vec4(color, saturate(flare * uAlpha));
}

vec4 dimensionalGate() {
    float layer = floor(uLayer + 0.5);

    if (layer > 0.5) {
        vec3 n = normalize(vLocal);
        float ringPlane = 1.0 - smoothstep(0.020, 0.105, abs(n.z));
        float angle = atan(n.y, n.x);
        float ticks = pow(max(0.0, 0.5 + 0.5 * sin(angle * 28.0 + uTime * 2.2 + uSeed * 8.0)), 12.0);
        float longTicks = pow(max(0.0, 0.5 + 0.5 * sin(angle * 14.0 - uTime * 1.4)), 18.0);
        float runes = smoothstep(0.62, 0.96, fbm(vec2(angle * 5.0 + uTime * 0.4, n.z * 18.0 + uSeed * 3.0)));
        vec3 color = mix(uSecondaryColor, uAccentColor, runes * 0.55 + longTicks * 0.35);
        color = mix(color, uHighlightColor, ticks * 0.55);
        float alpha = ringPlane * (ticks * 0.52 + longTicks * 0.34 + runes * 0.15) * uAlpha;
        return vec4(color, saturate(alpha));
    }

    vec2 p = vUv * 2.0 - 1.0;
    float r = length(p);
    float ellipse = 1.0 - smoothstep(0.92, 1.0, r);
    float edge = softRing(r, 0.92 + sin(uTime + uSeed) * 0.010, 0.028);
    float innerRing = softRing(r, 0.64, 0.018) * 0.42 + softRing(r, 0.36, 0.018) * 0.28;
    float angle = atan(p.y, p.x);
    float swirl = fbm(vec2(angle * 1.6 + uTime * 0.70 + uSeed, r * 12.0 - uTime * 0.9));
    float stars = starCell(vUv + vec2(uTime * 0.010, -uTime * 0.020), vec2(24.0, 42.0), 0.78, 0.22);
    float flow = pow(max(0.0, 0.5 + 0.5 * sin(angle * 5.0 + r * 13.0 - uTime * 2.3)), 2.0);
    vec3 core = mix(uPrimaryColor, uSecondaryColor, swirl * 0.72);
    core = mix(core, uAccentColor, flow * 0.18 + edge * 0.42);
    core = mix(core, uHighlightColor, stars * 0.80);
    float alpha = ellipse * (0.24 + swirl * 0.18 + stars * 0.42 + innerRing * 0.32) + edge * 0.50;
    return vec4(core, saturate(alpha * uAlpha));
}

vec4 temporalRift() {
    float layer = floor(uLayer + 0.5);

    if (layer < 1.5) {
        vec2 p = vUv * 2.0 - 1.0;
        float y = p.y;
        float taper = sin((y * 0.5 + 0.5) * PI);
        float center = sin(y * 7.6 - uTime * 1.02 + uSeed * 8.0) * 0.070
                + cos(y * 19.0 + uSeed * 11.0) * 0.032;
        float chips = fbm(vec2(y * 9.0 - uTime * 0.7, uSeed * 9.0));
        float width = (0.052 + chips * 0.150) * (0.12 + 0.88 * taper);
        float dist = abs(p.x - center);
        float body = 1.0 - smoothstep(width * 0.70, width * 1.10, dist);
        float edge = 1.0 - smoothstep(0.0, 0.034, abs(dist - width));
        float rippleRadius = length(vec2((p.x - center) * 1.1, p.y * 0.72));
        float ripples = 0.0;
        for (int i = 0; i < 4; i++) {
            float age = fract(uTime * 0.20 + float(i) * 0.223 + uSeed);
            ripples += softRing(rippleRadius, 0.20 + age * 0.82, 0.020 + age * 0.012)
                    * sin(age * PI) * (1.0 - smoothstep(0.90, 1.12, rippleRadius));
        }
        float reverseTicks = pow(max(0.0, 0.5 + 0.5 * sin(atan(p.y, p.x) * 18.0 - uTime * 3.0)), 12.0);
        vec3 color = mix(uPrimaryColor, uSecondaryColor, edge + ripples * 0.25);
        color = mix(color, uAccentColor, reverseTicks * 0.32 + uPulse * 0.18);
        color = mix(color, uHighlightColor, edge * reverseTicks * 0.48);
        float ghostScale = layer > 0.5 ? 0.54 : 1.0;
        float alpha = (body * 0.66 + edge * 0.56 + ripples * 0.20 + reverseTicks * 0.10) * uAlpha * ghostScale;
        return vec4(color, saturate(alpha));
    }

    vec3 n = normalize(vLocal);
    vec2 uv = vec2(atan(n.y, n.x), n.z);

    if (layer < 2.5) {
        float ringPlane = 1.0 - smoothstep(0.030, 0.145, abs(n.z));
        float majorTicks = pow(max(0.0, 0.5 + 0.5 * sin(uv.x * 24.0 - uTime * 2.5)), 14.0);
        float minorTicks = pow(max(0.0, 0.5 + 0.5 * sin(uv.x * 60.0 + uTime * 1.4)), 20.0);
        float halo = smoothstep(0.46, 0.96, fbm(vec2(uv.x * 3.0 - uTime * 0.3, uv.y * 11.0)));
        vec3 color = mix(uSecondaryColor, uAccentColor, majorTicks * 0.38 + halo * 0.30);
        color = mix(color, uHighlightColor, minorTicks * 0.28);
        float alpha = ringPlane * (majorTicks * 0.42 + minorTicks * 0.22 + halo * 0.16) * uAlpha;
        return vec4(color, saturate(alpha));
    }

    vec2 cellUv = vec2(atan(n.z, n.x) / (2.0 * PI) + 0.5, n.y * 0.5 + 0.5);
    vec2 grid = cellUv * vec2(22.0, 13.0);
    vec2 id = floor(grid);
    vec2 local = fract(grid) - 0.5;
    float gate = step(0.70, hash(id + uSeed * 31.0));
    float shard = (1.0 - smoothstep(0.04, 0.40, abs(local.x + local.y * 0.45)))
            * (1.0 - smoothstep(0.10, 0.46, abs(local.y - local.x * 0.25)));
    float pulse = 0.58 + 0.42 * sin(uTime * 4.4 + hash(id + 3.0) * 20.0);
    vec3 color = mix(uSecondaryColor, uAccentColor, hash(id + 8.0));
    color = mix(color, uHighlightColor, pulse * 0.32);
    return vec4(color, saturate(gate * shard * pulse * uAlpha));
}

void main() {
    vec4 color;
    if (uEffect < 0.5) {
        color = spatialRift();
    } else if (uEffect < 1.5) {
        color = wormhole();
    } else if (uEffect < 2.5) {
        color = gravitationalLens();
    } else if (uEffect < 3.5) {
        color = dimensionalGate();
    } else {
        color = temporalRift();
    }

    if (color.a <= 0.004) {
        discard;
    }
    gl_FragColor = color;
}
