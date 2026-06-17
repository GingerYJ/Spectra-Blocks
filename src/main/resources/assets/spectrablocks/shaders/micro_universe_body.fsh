#version 120

uniform float uTime;
uniform float uBodyType;
uniform float uStyle;
uniform float uAlpha;
uniform float uBrightness;
uniform vec3 uBaseColor;
uniform vec3 uAccentColor;

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
        p = p * 2.03 + vec2(5.7, 9.1);
        amplitude *= 0.5;
    }
    return value;
}

vec3 sunColor(vec2 uv, float facing, float rim) {
    vec2 flow = uv + vec2(uTime * 0.42, sin(uv.x * 24.0 + uTime * 2.3) * 0.035);
    float cells = fbm(flow * 10.5 + vec2(uTime * 0.55, -uTime * 0.28));
    float fine = fbm(flow * 31.0 + cells * 4.0 - uTime * 0.22);
    float hot = smoothstep(0.56, 0.94, fine);
    float bands = 0.5 + 0.5 * sin((uv.y + cells * 0.22) * 34.0 + uTime * 2.8);
    vec3 color = mix(vec3(1.0, 0.44, 0.08), uBaseColor, smoothstep(0.18, 0.82, cells));
    color = mix(color, vec3(1.0, 0.96, 0.62), hot * 0.82);
    color += uAccentColor * bands * 0.18;
    color *= (0.88 + cells * 0.28 + hot * 0.36 + rim * 0.45) * uBrightness;
    color *= 0.82 + pow(facing, 0.42) * 0.38;
    return color;
}

vec3 planetColor(vec2 uv, float facing, float rim) {
    float style = floor(uStyle + 0.5);
    vec2 flow = uv + vec2(uTime * (0.030 + style * 0.003), 0.0);
    float bands = 0.5 + 0.5 * sin((flow.y + fbm(flow * 5.0) * 0.16) * (18.0 + style * 2.7));
    float clouds = fbm(flow * (9.0 + style) + vec2(uTime * 0.08, -uTime * 0.035));
    float spots = smoothstep(0.64, 0.92, fbm(flow * (24.0 + style * 1.8) + clouds * 2.5));
    float land = smoothstep(0.46, 0.72, fbm(flow * 13.0 + vec2(style * 2.0, -style)));
    vec3 color = mix(uBaseColor * 0.62, uBaseColor, bands * 0.55 + clouds * 0.35);
    color = mix(color, uAccentColor, land * (0.22 + step(1.5, style) * 0.28));
    color += uAccentColor * spots * 0.34;
    color *= 0.68 + facing * 0.50 + rim * 0.18;
    color += uAccentColor * 0.055;
    return color * uBrightness;
}

vec3 meteorColor(vec2 uv, float facing, float rim) {
    float spark = fbm(uv * 20.0 + vec2(uTime * 2.0, -uTime * 0.7));
    vec3 color = mix(uBaseColor, uAccentColor, smoothstep(0.24, 0.86, spark));
    color *= 0.8 + pow(facing, 0.35) * 0.5 + rim * 0.5;
    return color * uBrightness;
}

vec3 bodyColor(vec2 uv, float facing, float rim) {
    if (uBodyType < 0.5) {
        return sunColor(uv, facing, rim);
    } else if (uBodyType < 1.5) {
        return planetColor(uv, facing, rim);
    }
    return meteorColor(uv, facing, rim);
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(-vView);
    float facing = max(dot(normal, viewDir), 0.0);
    float rim = pow(1.0 - facing, 2.0);

    vec3 color = bodyColor(vUv, facing, rim);
    float edgeDistance = min(vUv.x, 1.0 - vUv.x);
    float seamBlend = (1.0 - smoothstep(0.0, 0.075, edgeDistance)) * 0.5;
    float wrapDirection = mix(1.0, -1.0, step(0.5, vUv.x));
    vec3 wrappedColor = bodyColor(vUv + vec2(wrapDirection, 0.0), facing, rim);
    color = mix(color, wrappedColor, seamBlend);

    float alpha = uAlpha * (0.88 + rim * 0.12);
    gl_FragColor = vec4(color, clamp(alpha, 0.0, 1.0));
}
