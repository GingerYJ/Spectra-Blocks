package com.gingeryj.spectrablocks.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class ModConfig {

    private static final String CATEGORY_RENDERING = "rendering";
    private static final double DEFAULT_RENDER_SCALE = 1.0D;
    private static final double MIN_RENDER_SCALE = 0.01D;
    private static final double MAX_RENDER_SCALE = 50.0D;
    private static final int DEFAULT_VISUAL_TILE_ENTITY_RENDER_DISTANCE = 32;
    private static final int MIN_VISUAL_TILE_ENTITY_RENDER_DISTANCE = 1;
    private static final int MAX_VISUAL_TILE_ENTITY_RENDER_DISTANCE = 256;
    private static final boolean DEFAULT_ENABLE_SHADER_EFFECTS = true;

    private static double microSingularityScale = DEFAULT_RENDER_SCALE;
    private static double microWhiteHoleScale = DEFAULT_RENDER_SCALE;
    private static double microUniverseScale = DEFAULT_RENDER_SCALE;
    private static double microStellarSourceScale = DEFAULT_RENDER_SCALE;
    private static int visualTileEntityRenderDistance = DEFAULT_VISUAL_TILE_ENTITY_RENDER_DISTANCE;
    private static boolean enableShaderEffects = DEFAULT_ENABLE_SHADER_EFFECTS;

    private ModConfig() {
    }

    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        try {
            config.load();
            config.setCategoryComment(CATEGORY_RENDERING,
                    "\u6e32\u67d3\u7f29\u653e\u4e0e\u8ddd\u79bb\u914d\u7f6e\u3002\u7f29\u653e\u503c\u53ea\u5f71\u54cd\u5bf9\u5e94\u7684\u89c6\u89c9\u7279\u6548\u65b9\u5757\uff1b1.0 \u4e3a\u9ed8\u8ba4\u5927\u5c0f\uff0c0.5 \u4e3a\u7f29\u5c0f\u5230\u4e00\u534a\uff0c2.0 \u4e3a\u653e\u5927\u4e24\u500d\u3002 / Rendering scale and distance configuration. Scale values only affect their matching visual effect blocks; 1.0 keeps the default size, 0.5 halves it, and 2.0 doubles it.");
            visualTileEntityRenderDistance = config.getInt(
                    "visualTileEntityRenderDistance",
                    CATEGORY_RENDERING,
                    DEFAULT_VISUAL_TILE_ENTITY_RENDER_DISTANCE,
                    MIN_VISUAL_TILE_ENTITY_RENDER_DISTANCE,
                    MAX_VISUAL_TILE_ENTITY_RENDER_DISTANCE,
                    "\u89c6\u89c9 TileEntity \u7684\u6e32\u67d3\u8ddd\u79bb\uff08\u683c\uff09\u3002\u9ed8\u8ba4 32\uff0c\u5373 32 \u683c\u5185\u6e32\u67d3\uff0c32 \u683c\u5916\u4e0d\u6e32\u67d3\uff1b\u6b64\u8ddd\u79bb\u4e0d\u4f1a\u968f RenderScale \u653e\u5927\u3002 / Render distance in blocks for visual TileEntities. Defaults to 32, rendering within 32 blocks and culling beyond 32 blocks; this distance does not expand with RenderScale."
            );
            microSingularityScale = readScale(config, "microSingularityScale",
                    "\u5fae\u578b\u9ed1\u6d1e\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro Singularity rendering.");
            microWhiteHoleScale = readScale(config, "microWhiteHoleScale",
                    "\u5fae\u578b\u767d\u6d1e\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro White Hole rendering.");
            microUniverseScale = readScale(config, "microUniverseScale",
                    "\u5fae\u7f29\u5b87\u5b99\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro Universe rendering.");
            microStellarSourceScale = readScale(config, "microStellarSourceScale",
                    "\u5fae\u7f29\u6052\u661f\u6e90\u6e32\u67d3\u7f29\u653e\u3002 / Scale for Micro Stellar Source rendering.");
            enableShaderEffects = config.getBoolean(
                    "enableShaderEffects",
                    CATEGORY_RENDERING,
                    DEFAULT_ENABLE_SHADER_EFFECTS,
                    "\u542f\u7528 shader \u89c6\u89c9\u7279\u6548\u6e32\u67d3\u3002\u9ed8\u8ba4\u5f00\u542f\uff1b\u5173\u95ed\u540e shader \u7279\u6548\u65b9\u5757\u5c06\u4e0d\u6e32\u67d3\uff0c\u4e0d\u518d\u4f7f\u7528\u56fa\u5b9a\u7ba1\u7ebf\u56de\u9000\u6e32\u67d3\u3002 / Enables shader visual effect rendering. Enabled by default; when disabled, shader effect blocks are not rendered and no fixed-pipeline fallback is used."
            );
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

    public static double visualTileEntityRenderDistanceSquared() {
        return visualTileEntityRenderDistance * visualTileEntityRenderDistance;
    }

    public static boolean enableShaderEffects() {
        return enableShaderEffects;
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
