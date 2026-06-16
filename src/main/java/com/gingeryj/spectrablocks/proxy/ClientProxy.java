package com.gingeryj.spectrablocks.proxy;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.client.render.RenderAbyssalCore;
import com.gingeryj.spectrablocks.client.render.RenderArcaneStarRing;
import com.gingeryj.spectrablocks.client.render.RenderAstralAltarCore;
import com.gingeryj.spectrablocks.client.render.RenderAuroraVeil;
import com.gingeryj.spectrablocks.client.render.RenderBioluminescentSpores;
import com.gingeryj.spectrablocks.client.render.RenderCollapsingStar;
import com.gingeryj.spectrablocks.client.render.RenderCometTail;
import com.gingeryj.spectrablocks.client.render.RenderCosmicBackgroundRadiationField;
import com.gingeryj.spectrablocks.client.render.RenderCrystalRefractionField;
import com.gingeryj.spectrablocks.client.render.RenderDataStreamMatrix;
import com.gingeryj.spectrablocks.client.render.RenderDimensionalGate;
import com.gingeryj.spectrablocks.client.render.RenderDreamShards;
import com.gingeryj.spectrablocks.client.render.RenderEntropyCloud;
import com.gingeryj.spectrablocks.client.render.RenderFireflySwarm;
import com.gingeryj.spectrablocks.client.render.RenderFlameHeart;
import com.gingeryj.spectrablocks.client.render.RenderFrostCore;
import com.gingeryj.spectrablocks.client.render.RenderGravitationalLens;
import com.gingeryj.spectrablocks.client.render.RenderHexBarrier;
import com.gingeryj.spectrablocks.client.render.RenderImaginaryCube;
import com.gingeryj.spectrablocks.client.render.RenderLunarEclipse;
import com.gingeryj.spectrablocks.client.render.RenderMeteorShower;
import com.gingeryj.spectrablocks.client.render.RenderMicroSingularity;
import com.gingeryj.spectrablocks.client.render.RenderMicroStellarSource;
import com.gingeryj.spectrablocks.client.render.RenderMicroUniverse;
import com.gingeryj.spectrablocks.client.render.RenderMicroWhiteHole;
import com.gingeryj.spectrablocks.client.render.RenderMiniatureGalaxy;
import com.gingeryj.spectrablocks.client.render.RenderNebulaCore;
import com.gingeryj.spectrablocks.client.render.RenderNullField;
import com.gingeryj.spectrablocks.client.render.RenderPlasmaStorm;
import com.gingeryj.spectrablocks.client.render.RenderPollenBreeze;
import com.gingeryj.spectrablocks.client.render.RenderQuantumBubble;
import com.gingeryj.spectrablocks.client.render.RenderShieldDome;
import com.gingeryj.spectrablocks.client.render.RenderSolarCoronaBurst;
import com.gingeryj.spectrablocks.client.render.RenderSoulVortex;
import com.gingeryj.spectrablocks.client.render.RenderSpatialRift;
import com.gingeryj.spectrablocks.client.render.RenderSpectralPrism;
import com.gingeryj.spectrablocks.client.render.RenderStardustFountain;
import com.gingeryj.spectrablocks.client.render.RenderStellarHourglass;
import com.gingeryj.spectrablocks.client.render.RenderStormCore;
import com.gingeryj.spectrablocks.client.render.RenderTemporalRift;
import com.gingeryj.spectrablocks.client.render.RenderTerraBloom;
import com.gingeryj.spectrablocks.client.render.RenderVoidCrystal;
import com.gingeryj.spectrablocks.client.render.RenderVoidLotus;
import com.gingeryj.spectrablocks.client.render.RenderWormhole;
import com.gingeryj.spectrablocks.registry.ModContent;
import com.gingeryj.spectrablocks.tile.TileAbyssalCore;
import com.gingeryj.spectrablocks.tile.TileArcaneStarRing;
import com.gingeryj.spectrablocks.tile.TileAstralAltarCore;
import com.gingeryj.spectrablocks.tile.TileAuroraVeil;
import com.gingeryj.spectrablocks.tile.TileBioluminescentSpores;
import com.gingeryj.spectrablocks.tile.TileCollapsingStar;
import com.gingeryj.spectrablocks.tile.TileCometTail;
import com.gingeryj.spectrablocks.tile.TileCosmicBackgroundRadiationField;
import com.gingeryj.spectrablocks.tile.TileCrystalRefractionField;
import com.gingeryj.spectrablocks.tile.TileDataStreamMatrix;
import com.gingeryj.spectrablocks.tile.TileDimensionalGate;
import com.gingeryj.spectrablocks.tile.TileDreamShards;
import com.gingeryj.spectrablocks.tile.TileEntropyCloud;
import com.gingeryj.spectrablocks.tile.TileFireflySwarm;
import com.gingeryj.spectrablocks.tile.TileFlameHeart;
import com.gingeryj.spectrablocks.tile.TileFrostCore;
import com.gingeryj.spectrablocks.tile.TileGravitationalLens;
import com.gingeryj.spectrablocks.tile.TileHexBarrier;
import com.gingeryj.spectrablocks.tile.TileImaginaryCube;
import com.gingeryj.spectrablocks.tile.TileLunarEclipse;
import com.gingeryj.spectrablocks.tile.TileMeteorShower;
import com.gingeryj.spectrablocks.tile.TileMicroSingularity;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;
import com.gingeryj.spectrablocks.tile.TileMiniatureGalaxy;
import com.gingeryj.spectrablocks.tile.TileNebulaCore;
import com.gingeryj.spectrablocks.tile.TileNullField;
import com.gingeryj.spectrablocks.tile.TilePlasmaStorm;
import com.gingeryj.spectrablocks.tile.TilePollenBreeze;
import com.gingeryj.spectrablocks.tile.TileQuantumBubble;
import com.gingeryj.spectrablocks.tile.TileShieldDome;
import com.gingeryj.spectrablocks.tile.TileSolarCoronaBurst;
import com.gingeryj.spectrablocks.tile.TileSoulVortex;
import com.gingeryj.spectrablocks.tile.TileSpatialRift;
import com.gingeryj.spectrablocks.tile.TileSpectralPrism;
import com.gingeryj.spectrablocks.tile.TileStardustFountain;
import com.gingeryj.spectrablocks.tile.TileStellarHourglass;
import com.gingeryj.spectrablocks.tile.TileStormCore;
import com.gingeryj.spectrablocks.tile.TileTemporalRift;
import com.gingeryj.spectrablocks.tile.TileTerraBloom;
import com.gingeryj.spectrablocks.tile.TileVoidCrystal;
import com.gingeryj.spectrablocks.tile.TileVoidLotus;
import com.gingeryj.spectrablocks.tile.TileWormhole;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroSingularity.class, new RenderMicroSingularity());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroWhiteHole.class, new RenderMicroWhiteHole());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroUniverse.class, new RenderMicroUniverse());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMicroStellarSource.class, new RenderMicroStellarSource());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMiniatureGalaxy.class, new RenderMiniatureGalaxy());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNebulaCore.class, new RenderNebulaCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCollapsingStar.class, new RenderCollapsingStar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCosmicBackgroundRadiationField.class,
                new RenderCosmicBackgroundRadiationField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileSpatialRift.class, new RenderSpatialRift());
        ClientRegistry.bindTileEntitySpecialRenderer(TileWormhole.class, new RenderWormhole());
        ClientRegistry.bindTileEntitySpecialRenderer(TileGravitationalLens.class, new RenderGravitationalLens());
        ClientRegistry.bindTileEntitySpecialRenderer(TileVoidCrystal.class, new RenderVoidCrystal());
        ClientRegistry.bindTileEntitySpecialRenderer(TileArcaneStarRing.class, new RenderArcaneStarRing());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePlasmaStorm.class, new RenderPlasmaStorm());
        ClientRegistry.bindTileEntitySpecialRenderer(TileTemporalRift.class, new RenderTemporalRift());
        ClientRegistry.bindTileEntitySpecialRenderer(TileDimensionalGate.class, new RenderDimensionalGate());
        ClientRegistry.bindTileEntitySpecialRenderer(TileQuantumBubble.class, new RenderQuantumBubble());
        ClientRegistry.bindTileEntitySpecialRenderer(TileImaginaryCube.class, new RenderImaginaryCube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileSpectralPrism.class, new RenderSpectralPrism());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCrystalRefractionField.class,
                new RenderCrystalRefractionField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileDataStreamMatrix.class, new RenderDataStreamMatrix());
        ClientRegistry.bindTileEntitySpecialRenderer(TileStellarHourglass.class, new RenderStellarHourglass());
        ClientRegistry.bindTileEntitySpecialRenderer(TileVoidLotus.class, new RenderVoidLotus());
        ClientRegistry.bindTileEntitySpecialRenderer(TileAstralAltarCore.class, new RenderAstralAltarCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileSoulVortex.class, new RenderSoulVortex());
        ClientRegistry.bindTileEntitySpecialRenderer(TileStardustFountain.class, new RenderStardustFountain());
        ClientRegistry.bindTileEntitySpecialRenderer(TileAuroraVeil.class, new RenderAuroraVeil());
        ClientRegistry.bindTileEntitySpecialRenderer(TileAbyssalCore.class, new RenderAbyssalCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileStormCore.class, new RenderStormCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntropyCloud.class, new RenderEntropyCloud());
        ClientRegistry.bindTileEntitySpecialRenderer(TileDreamShards.class, new RenderDreamShards());
        ClientRegistry.bindTileEntitySpecialRenderer(TileSolarCoronaBurst.class, new RenderSolarCoronaBurst());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFlameHeart.class, new RenderFlameHeart());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFrostCore.class, new RenderFrostCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileTerraBloom.class, new RenderTerraBloom());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFireflySwarm.class, new RenderFireflySwarm());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePollenBreeze.class, new RenderPollenBreeze());
        ClientRegistry.bindTileEntitySpecialRenderer(TileBioluminescentSpores.class, new RenderBioluminescentSpores());
        ClientRegistry.bindTileEntitySpecialRenderer(TileShieldDome.class, new RenderShieldDome());
        ClientRegistry.bindTileEntitySpecialRenderer(TileHexBarrier.class, new RenderHexBarrier());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNullField.class, new RenderNullField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLunarEclipse.class, new RenderLunarEclipse());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCometTail.class, new RenderCometTail());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMeteorShower.class, new RenderMeteorShower());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerBlockItemModel(ModContent.MICRO_SINGULARITY);
        registerBlockItemModel(ModContent.MICRO_WHITE_HOLE);
        registerBlockItemModel(ModContent.MICRO_UNIVERSE);
        registerBlockItemModel(ModContent.MICRO_STELLAR_SOURCE);
        registerBlockItemModel(ModContent.MINIATURE_GALAXY);
        registerBlockItemModel(ModContent.NEBULA_CORE);
        registerBlockItemModel(ModContent.COLLAPSING_STAR);
        registerBlockItemModel(ModContent.COSMIC_BACKGROUND_RADIATION_FIELD);
        registerBlockItemModel(ModContent.SPATIAL_RIFT);
        registerBlockItemModel(ModContent.WORMHOLE);
        registerBlockItemModel(ModContent.GRAVITATIONAL_LENS);
        registerBlockItemModel(ModContent.VOID_CRYSTAL);
        registerBlockItemModel(ModContent.ARCANE_STAR_RING);
        registerBlockItemModel(ModContent.PLASMA_STORM);
        registerBlockItemModel(ModContent.TEMPORAL_RIFT);
        registerBlockItemModel(ModContent.DIMENSIONAL_GATE);
        registerBlockItemModel(ModContent.QUANTUM_BUBBLE);
        registerBlockItemModel(ModContent.IMAGINARY_CUBE);
        registerBlockItemModel(ModContent.SPECTRAL_PRISM);
        registerBlockItemModel(ModContent.CRYSTAL_REFRACTION_FIELD);
        registerBlockItemModel(ModContent.DATA_STREAM_MATRIX);
        registerBlockItemModel(ModContent.STELLAR_HOURGLASS);
        registerBlockItemModel(ModContent.VOID_LOTUS);
        registerBlockItemModel(ModContent.ASTRAL_ALTAR_CORE);
        registerBlockItemModel(ModContent.SOUL_VORTEX);
        registerBlockItemModel(ModContent.STARDUST_FOUNTAIN);
        registerBlockItemModel(ModContent.AURORA_VEIL);
        registerBlockItemModel(ModContent.ABYSSAL_CORE);
        registerBlockItemModel(ModContent.STORM_CORE);
        registerBlockItemModel(ModContent.ENTROPY_CLOUD);
        registerBlockItemModel(ModContent.DREAM_SHARDS);
        registerBlockItemModel(ModContent.SOLAR_CORONA_BURST);
        registerBlockItemModel(ModContent.FLAME_HEART);
        registerBlockItemModel(ModContent.FROST_CORE);
        registerBlockItemModel(ModContent.TERRA_BLOOM);
        registerBlockItemModel(ModContent.FIREFLY_SWARM);
        registerBlockItemModel(ModContent.POLLEN_BREEZE);
        registerBlockItemModel(ModContent.BIOLUMINESCENT_SPORES);
        registerBlockItemModel(ModContent.SHIELD_DOME);
        registerBlockItemModel(ModContent.HEX_BARRIER);
        registerBlockItemModel(ModContent.NULL_FIELD);
        registerBlockItemModel(ModContent.LUNAR_ECLIPSE);
        registerBlockItemModel(ModContent.COMET_TAIL);
        registerBlockItemModel(ModContent.METEOR_SHOWER);
    }

    private static void registerBlockItemModel(Block block) {
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }
}
