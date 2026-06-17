package com.gingeryj.spectrablocks.proxy;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.client.gui.GuiEffectConfigurator;
import com.gingeryj.spectrablocks.client.render.RenderAbyssalCore;
import com.gingeryj.spectrablocks.client.render.RenderArcaneStarRing;
import com.gingeryj.spectrablocks.client.render.RenderAstralAltarCore;
import com.gingeryj.spectrablocks.client.render.RenderAuroraVeil;
import com.gingeryj.spectrablocks.client.render.RenderBioluminescentSpores;
import com.gingeryj.spectrablocks.client.render.RenderChronoAnchor;
import com.gingeryj.spectrablocks.client.render.RenderCollapsingStar;
import com.gingeryj.spectrablocks.client.render.RenderCosmicBackgroundRadiationField;
import com.gingeryj.spectrablocks.client.render.RenderCrystalHarmonicResonator;
import com.gingeryj.spectrablocks.client.render.RenderCrystalRefractionField;
import com.gingeryj.spectrablocks.client.render.RenderDataStreamMatrix;
import com.gingeryj.spectrablocks.client.render.RenderDimensionalGate;
import com.gingeryj.spectrablocks.client.render.RenderDreamShards;
import com.gingeryj.spectrablocks.client.render.RenderEchoingVoidBell;
import com.gingeryj.spectrablocks.client.render.RenderEmberBloom;
import com.gingeryj.spectrablocks.client.render.RenderEnergyNexus;
import com.gingeryj.spectrablocks.client.render.RenderEntropyCloud;
import com.gingeryj.spectrablocks.client.render.RenderEtherReactorCore;
import com.gingeryj.spectrablocks.client.render.RenderEventHorizonMirror;
import com.gingeryj.spectrablocks.client.render.RenderFrostCrystalMist;
import com.gingeryj.spectrablocks.client.render.RenderGravitationalLens;
import com.gingeryj.spectrablocks.client.render.RenderGravityWell;
import com.gingeryj.spectrablocks.client.render.RenderHologramField;
import com.gingeryj.spectrablocks.client.render.RenderImaginaryCube;
import com.gingeryj.spectrablocks.client.render.RenderLiquidStarlightPool;
import com.gingeryj.spectrablocks.client.render.RenderLunarPhaseOrrery;
import com.gingeryj.spectrablocks.client.render.RenderMagneticFluxCage;
import com.gingeryj.spectrablocks.client.render.RenderMicroSingularity;
import com.gingeryj.spectrablocks.client.render.RenderMicroStellarSource;
import com.gingeryj.spectrablocks.client.render.RenderMicroUniverse;
import com.gingeryj.spectrablocks.client.render.RenderMicroWhiteHole;
import com.gingeryj.spectrablocks.client.render.RenderMiniatureGalaxy;
import com.gingeryj.spectrablocks.client.render.RenderMirrorShardField;
import com.gingeryj.spectrablocks.client.render.RenderNebulaCore;
import com.gingeryj.spectrablocks.client.render.RenderNeonCircuitCore;
import com.gingeryj.spectrablocks.client.render.RenderNovaBloom;
import com.gingeryj.spectrablocks.client.render.RenderPhantomEye;
import com.gingeryj.spectrablocks.client.render.RenderPlasmaStorm;
import com.gingeryj.spectrablocks.client.render.RenderPrismaticRainfall;
import com.gingeryj.spectrablocks.client.render.RenderQuantumBubble;
import com.gingeryj.spectrablocks.client.render.RenderRadiantSigilField;
import com.gingeryj.spectrablocks.client.render.RenderRuneObelisk;
import com.gingeryj.spectrablocks.client.render.RenderShadowFlameLantern;
import com.gingeryj.spectrablocks.client.render.RenderSolarCoronaBurst;
import com.gingeryj.spectrablocks.client.render.RenderSoulVortex;
import com.gingeryj.spectrablocks.client.render.RenderSpatialRift;
import com.gingeryj.spectrablocks.client.render.RenderSpectralPrism;
import com.gingeryj.spectrablocks.client.render.RenderStardustFountain;
import com.gingeryj.spectrablocks.client.render.RenderStellarHourglass;
import com.gingeryj.spectrablocks.client.render.RenderStormCore;
import com.gingeryj.spectrablocks.client.render.RenderTemporalRift;
import com.gingeryj.spectrablocks.client.render.RenderVoidCrystal;
import com.gingeryj.spectrablocks.client.render.RenderVoidLotus;
import com.gingeryj.spectrablocks.client.render.RenderWormhole;
import com.gingeryj.spectrablocks.registry.ModContent;
import com.gingeryj.spectrablocks.tile.TileAbyssalCore;
import com.gingeryj.spectrablocks.tile.TileArcaneStarRing;
import com.gingeryj.spectrablocks.tile.TileAstralAltarCore;
import com.gingeryj.spectrablocks.tile.TileAuroraVeil;
import com.gingeryj.spectrablocks.tile.TileBioluminescentSpores;
import com.gingeryj.spectrablocks.tile.TileChronoAnchor;
import com.gingeryj.spectrablocks.tile.TileCollapsingStar;
import com.gingeryj.spectrablocks.tile.TileCosmicBackgroundRadiationField;
import com.gingeryj.spectrablocks.tile.TileCrystalHarmonicResonator;
import com.gingeryj.spectrablocks.tile.TileCrystalRefractionField;
import com.gingeryj.spectrablocks.tile.TileDataStreamMatrix;
import com.gingeryj.spectrablocks.tile.TileDimensionalGate;
import com.gingeryj.spectrablocks.tile.TileDreamShards;
import com.gingeryj.spectrablocks.tile.TileEchoingVoidBell;
import com.gingeryj.spectrablocks.tile.TileEmberBloom;
import com.gingeryj.spectrablocks.tile.TileEnergyNexus;
import com.gingeryj.spectrablocks.tile.TileEntropyCloud;
import com.gingeryj.spectrablocks.tile.TileEtherReactorCore;
import com.gingeryj.spectrablocks.tile.TileEventHorizonMirror;
import com.gingeryj.spectrablocks.tile.TileFrostCrystalMist;
import com.gingeryj.spectrablocks.tile.TileGravitationalLens;
import com.gingeryj.spectrablocks.tile.TileGravityWell;
import com.gingeryj.spectrablocks.tile.TileHologramField;
import com.gingeryj.spectrablocks.tile.TileImaginaryCube;
import com.gingeryj.spectrablocks.tile.TileLiquidStarlightPool;
import com.gingeryj.spectrablocks.tile.TileLunarPhaseOrrery;
import com.gingeryj.spectrablocks.tile.TileMagneticFluxCage;
import com.gingeryj.spectrablocks.tile.TileMicroSingularity;
import com.gingeryj.spectrablocks.tile.TileMicroStellarSource;
import com.gingeryj.spectrablocks.tile.TileMicroUniverse;
import com.gingeryj.spectrablocks.tile.TileMicroWhiteHole;
import com.gingeryj.spectrablocks.tile.TileMiniatureGalaxy;
import com.gingeryj.spectrablocks.tile.TileMirrorShardField;
import com.gingeryj.spectrablocks.tile.TileNebulaCore;
import com.gingeryj.spectrablocks.tile.TileNeonCircuitCore;
import com.gingeryj.spectrablocks.tile.TileNovaBloom;
import com.gingeryj.spectrablocks.tile.TilePhantomEye;
import com.gingeryj.spectrablocks.tile.TilePlasmaStorm;
import com.gingeryj.spectrablocks.tile.TilePrismaticRainfall;
import com.gingeryj.spectrablocks.tile.TileQuantumBubble;
import com.gingeryj.spectrablocks.tile.TileRadiantSigilField;
import com.gingeryj.spectrablocks.tile.TileRuneObelisk;
import com.gingeryj.spectrablocks.tile.TileShadowFlameLantern;
import com.gingeryj.spectrablocks.tile.TileSolarCoronaBurst;
import com.gingeryj.spectrablocks.tile.TileSoulVortex;
import com.gingeryj.spectrablocks.tile.TileSpatialRift;
import com.gingeryj.spectrablocks.tile.TileSpectralPrism;
import com.gingeryj.spectrablocks.tile.TileStardustFountain;
import com.gingeryj.spectrablocks.tile.TileStellarHourglass;
import com.gingeryj.spectrablocks.tile.TileStormCore;
import com.gingeryj.spectrablocks.tile.TileTemporalRift;
import com.gingeryj.spectrablocks.tile.TileVoidCrystal;
import com.gingeryj.spectrablocks.tile.TileVoidLotus;
import com.gingeryj.spectrablocks.tile.TileWormhole;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
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
        ClientRegistry.bindTileEntitySpecialRenderer(TileEnergyNexus.class, new RenderEnergyNexus());
        ClientRegistry.bindTileEntitySpecialRenderer(TileHologramField.class, new RenderHologramField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRuneObelisk.class, new RenderRuneObelisk());
        ClientRegistry.bindTileEntitySpecialRenderer(TileBioluminescentSpores.class,
                new RenderBioluminescentSpores());
        ClientRegistry.bindTileEntitySpecialRenderer(TileChronoAnchor.class, new RenderChronoAnchor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFrostCrystalMist.class, new RenderFrostCrystalMist());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNeonCircuitCore.class, new RenderNeonCircuitCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMirrorShardField.class, new RenderMirrorShardField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEmberBloom.class, new RenderEmberBloom());
        ClientRegistry.bindTileEntitySpecialRenderer(TileGravityWell.class, new RenderGravityWell());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEchoingVoidBell.class, new RenderEchoingVoidBell());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePrismaticRainfall.class, new RenderPrismaticRainfall());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMagneticFluxCage.class, new RenderMagneticFluxCage());
        ClientRegistry.bindTileEntitySpecialRenderer(TileShadowFlameLantern.class, new RenderShadowFlameLantern());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLunarPhaseOrrery.class, new RenderLunarPhaseOrrery());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCrystalHarmonicResonator.class,
                new RenderCrystalHarmonicResonator());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePhantomEye.class, new RenderPhantomEye());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRadiantSigilField.class, new RenderRadiantSigilField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLiquidStarlightPool.class, new RenderLiquidStarlightPool());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNovaBloom.class, new RenderNovaBloom());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEtherReactorCore.class, new RenderEtherReactorCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEventHorizonMirror.class, new RenderEventHorizonMirror());
    }

    @Override
    public void openEffectConfigurator(BlockPos pos, double renderScale) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEffectConfigurator(pos, renderScale));
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
        registerBlockItemModel(ModContent.ENERGY_NEXUS);
        registerBlockItemModel(ModContent.HOLOGRAM_FIELD);
        registerBlockItemModel(ModContent.RUNE_OBELISK);
        registerBlockItemModel(ModContent.BIOLUMINESCENT_SPORES);
        registerBlockItemModel(ModContent.CHRONO_ANCHOR);
        registerBlockItemModel(ModContent.FROST_CRYSTAL_MIST);
        registerBlockItemModel(ModContent.NEON_CIRCUIT_CORE);
        registerBlockItemModel(ModContent.MIRROR_SHARD_FIELD);
        registerBlockItemModel(ModContent.EMBER_BLOOM);
        registerBlockItemModel(ModContent.GRAVITY_WELL);
        registerBlockItemModel(ModContent.ECHOING_VOID_BELL);
        registerBlockItemModel(ModContent.PRISMATIC_RAINFALL);
        registerBlockItemModel(ModContent.MAGNETIC_FLUX_CAGE);
        registerBlockItemModel(ModContent.SHADOW_FLAME_LANTERN);
        registerBlockItemModel(ModContent.LUNAR_PHASE_ORRERY);
        registerBlockItemModel(ModContent.CRYSTAL_HARMONIC_RESONATOR);
        registerBlockItemModel(ModContent.PHANTOM_EYE);
        registerBlockItemModel(ModContent.RADIANT_SIGIL_FIELD);
        registerBlockItemModel(ModContent.LIQUID_STARLIGHT_POOL);
        registerBlockItemModel(ModContent.NOVA_BLOOM);
        registerBlockItemModel(ModContent.ETHER_REACTOR_CORE);
        registerBlockItemModel(ModContent.EVENT_HORIZON_MIRROR);
        registerItemModel(ModContent.EFFECT_CONFIGURATOR);
    }

    private static void registerBlockItemModel(Block block) {
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }

    private static void registerItemModel(Item item) {
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
