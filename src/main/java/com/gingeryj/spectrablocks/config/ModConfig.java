package com.gingeryj.spectrablocks.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class ModConfig {

    private static final String CATEGORY_RENDERING = "rendering";
    private static final double DEFAULT_RENDER_SCALE = 1.0D;
    private static final double MIN_RENDER_SCALE = 0.25D;
    private static final double MAX_RENDER_SCALE = 4.0D;

    private static double microSingularityScale = DEFAULT_RENDER_SCALE;
    private static double microWhiteHoleScale = DEFAULT_RENDER_SCALE;
    private static double microUniverseScale = DEFAULT_RENDER_SCALE;
    private static double microStellarSourceScale = DEFAULT_RENDER_SCALE;

    private ModConfig() {
    }

    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        try {
            config.load();
            config.setCategoryComment(CATEGORY_RENDERING,
                    "\u6e32\u67d3\u7f29\u653e\u914d\u7f6e\u3002\u6bcf\u4e2a\u503c\u53ea\u5f71\u54cd\u5bf9\u5e94\u7684\u89c6\u89c9\u7279\u6548\u65b9\u5757\uff1b1.0 \u4e3a\u9ed8\u8ba4\u5927\u5c0f\uff0c0.5 \u4e3a\u7f29\u5c0f\u5230\u4e00\u534a\uff0c2.0 \u4e3a\u653e\u5927\u4e24\u500d\u3002 / Rendering scale configuration. Each value only affects its matching visual effect block; 1.0 keeps the default size, 0.5 halves it, and 2.0 doubles it.");
            microSingularityScale = readScale(config, "microSingularityScale",
                    "\u5fae\u578b\u9ed1\u6d1e\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro Singularity rendering.");
            microWhiteHoleScale = readScale(config, "microWhiteHoleScale",
                    "\u5fae\u578b\u767d\u6d1e\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro White Hole rendering.");
            microUniverseScale = readScale(config, "microUniverseScale",
                    "\u5fae\u7f29\u5b87\u5b99\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro Universe rendering.");
            microStellarSourceScale = readScale(config, "microStellarSourceScale",
                    "\u5fae\u7f29\u6052\u661f\u6e90\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro Stellar Source rendering.");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    private static double readScale(Configuration config, String name, String comment) {
        return config.getFloat(
                name,
                CATEGORY_RENDERING,
                (float) DEFAULT_RENDER_SCALE,
                (float) MIN_RENDER_SCALE,
                (float) MAX_RENDER_SCALE,
                comment + " 1.0 \u4e3a\u9ed8\u8ba4\u5927\u5c0f\u3002 / 1.0 keeps the default size."
        );
    }

    public static double microSingularityScale() {
        return microSingularityScale;
    }

    public static double microWhiteHoleScale() {
        return microWhiteHoleScale;
    }

    public static double microUniverseScale() {
        return microUniverseScale;
    }

    public static double microStellarSourceScale() {
        return microStellarSourceScale;
    }

    public static double clampRenderScale(double scale) {
        if (Double.isNaN(scale) || Double.isInfinite(scale)) {
            return DEFAULT_RENDER_SCALE;
        }
        if (scale < MIN_RENDER_SCALE) {
            return MIN_RENDER_SCALE;
        }
        if (scale > MAX_RENDER_SCALE) {
            return MAX_RENDER_SCALE;
        }
        return scale;
    }
}
