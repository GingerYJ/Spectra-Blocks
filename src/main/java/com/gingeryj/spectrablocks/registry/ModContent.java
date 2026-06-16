package com.gingeryj.spectrablocks.registry;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.block.BlockAbyssalCore;
import com.gingeryj.spectrablocks.block.BlockArcaneStarRing;
import com.gingeryj.spectrablocks.block.BlockAstralAltarCore;
import com.gingeryj.spectrablocks.block.BlockAuroraVeil;
import com.gingeryj.spectrablocks.block.BlockBioluminescentSpores;
import com.gingeryj.spectrablocks.block.BlockCollapsingStar;
import com.gingeryj.spectrablocks.block.BlockCometTail;
import com.gingeryj.spectrablocks.block.BlockCosmicBackgroundRadiationField;
import com.gingeryj.spectrablocks.block.BlockCrystalRefractionField;
import com.gingeryj.spectrablocks.block.BlockDataStreamMatrix;
import com.gingeryj.spectrablocks.block.BlockDimensionalGate;
import com.gingeryj.spectrablocks.block.BlockDreamShards;
import com.gingeryj.spectrablocks.block.BlockEntropyCloud;
import com.gingeryj.spectrablocks.block.BlockFireflySwarm;
import com.gingeryj.spectrablocks.block.BlockFlameHeart;
import com.gingeryj.spectrablocks.block.BlockFrostCore;
import com.gingeryj.spectrablocks.block.BlockGravitationalLens;
import com.gingeryj.spectrablocks.block.BlockHexBarrier;
import com.gingeryj.spectrablocks.block.BlockImaginaryCube;
import com.gingeryj.spectrablocks.block.BlockLunarEclipse;
import com.gingeryj.spectrablocks.block.BlockMeteorShower;
import com.gingeryj.spectrablocks.block.BlockMicroSingularity;
import com.gingeryj.spectrablocks.block.BlockMicroStellarSource;
import com.gingeryj.spectrablocks.block.BlockMicroUniverse;
import com.gingeryj.spectrablocks.block.BlockMicroWhiteHole;
import com.gingeryj.spectrablocks.block.BlockMiniatureGalaxy;
import com.gingeryj.spectrablocks.block.BlockNebulaCore;
import com.gingeryj.spectrablocks.block.BlockNullField;
import com.gingeryj.spectrablocks.block.BlockPlasmaStorm;
import com.gingeryj.spectrablocks.block.BlockPollenBreeze;
import com.gingeryj.spectrablocks.block.BlockQuantumBubble;
import com.gingeryj.spectrablocks.block.BlockShieldDome;
import com.gingeryj.spectrablocks.block.BlockSolarCoronaBurst;
import com.gingeryj.spectrablocks.block.BlockSoulVortex;
import com.gingeryj.spectrablocks.block.BlockSpatialRift;
import com.gingeryj.spectrablocks.block.BlockSpectralPrism;
import com.gingeryj.spectrablocks.block.BlockStardustFountain;
import com.gingeryj.spectrablocks.block.BlockStellarHourglass;
import com.gingeryj.spectrablocks.block.BlockStormCore;
import com.gingeryj.spectrablocks.block.BlockTemporalRift;
import com.gingeryj.spectrablocks.block.BlockTerraBloom;
import com.gingeryj.spectrablocks.block.BlockVoidCrystal;
import com.gingeryj.spectrablocks.block.BlockVoidLotus;
import com.gingeryj.spectrablocks.block.BlockWormhole;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModContent {

    public static final BlockMicroSingularity MICRO_SINGULARITY = new BlockMicroSingularity();
    public static final BlockMicroWhiteHole MICRO_WHITE_HOLE = new BlockMicroWhiteHole();
    public static final BlockMicroUniverse MICRO_UNIVERSE = new BlockMicroUniverse();
    public static final BlockMicroStellarSource MICRO_STELLAR_SOURCE = new BlockMicroStellarSource();
    public static final BlockMiniatureGalaxy MINIATURE_GALAXY = new BlockMiniatureGalaxy();
    public static final BlockNebulaCore NEBULA_CORE = new BlockNebulaCore();
    public static final BlockCollapsingStar COLLAPSING_STAR = new BlockCollapsingStar();
    public static final BlockCosmicBackgroundRadiationField COSMIC_BACKGROUND_RADIATION_FIELD =
            new BlockCosmicBackgroundRadiationField();
    public static final BlockSpatialRift SPATIAL_RIFT = new BlockSpatialRift();
    public static final BlockWormhole WORMHOLE = new BlockWormhole();
    public static final BlockGravitationalLens GRAVITATIONAL_LENS = new BlockGravitationalLens();
    public static final BlockVoidCrystal VOID_CRYSTAL = new BlockVoidCrystal();
    public static final BlockArcaneStarRing ARCANE_STAR_RING = new BlockArcaneStarRing();
    public static final BlockPlasmaStorm PLASMA_STORM = new BlockPlasmaStorm();
    public static final BlockTemporalRift TEMPORAL_RIFT = new BlockTemporalRift();
    public static final BlockDimensionalGate DIMENSIONAL_GATE = new BlockDimensionalGate();
    public static final BlockQuantumBubble QUANTUM_BUBBLE = new BlockQuantumBubble();
    public static final BlockImaginaryCube IMAGINARY_CUBE = new BlockImaginaryCube();
    public static final BlockSpectralPrism SPECTRAL_PRISM = new BlockSpectralPrism();
    public static final BlockCrystalRefractionField CRYSTAL_REFRACTION_FIELD = new BlockCrystalRefractionField();
    public static final BlockDataStreamMatrix DATA_STREAM_MATRIX = new BlockDataStreamMatrix();
    public static final BlockStellarHourglass STELLAR_HOURGLASS = new BlockStellarHourglass();
    public static final BlockVoidLotus VOID_LOTUS = new BlockVoidLotus();
    public static final BlockAstralAltarCore ASTRAL_ALTAR_CORE = new BlockAstralAltarCore();
    public static final BlockSoulVortex SOUL_VORTEX = new BlockSoulVortex();
    public static final BlockStardustFountain STARDUST_FOUNTAIN = new BlockStardustFountain();
    public static final BlockAuroraVeil AURORA_VEIL = new BlockAuroraVeil();
    public static final BlockAbyssalCore ABYSSAL_CORE = new BlockAbyssalCore();
    public static final BlockStormCore STORM_CORE = new BlockStormCore();
    public static final BlockEntropyCloud ENTROPY_CLOUD = new BlockEntropyCloud();
    public static final BlockDreamShards DREAM_SHARDS = new BlockDreamShards();
    public static final BlockSolarCoronaBurst SOLAR_CORONA_BURST = new BlockSolarCoronaBurst();
    public static final BlockFlameHeart FLAME_HEART = new BlockFlameHeart();
    public static final BlockFrostCore FROST_CORE = new BlockFrostCore();
    public static final BlockTerraBloom TERRA_BLOOM = new BlockTerraBloom();
    public static final BlockFireflySwarm FIREFLY_SWARM = new BlockFireflySwarm();
    public static final BlockPollenBreeze POLLEN_BREEZE = new BlockPollenBreeze();
    public static final BlockBioluminescentSpores BIOLUMINESCENT_SPORES = new BlockBioluminescentSpores();
    public static final BlockShieldDome SHIELD_DOME = new BlockShieldDome();
    public static final BlockHexBarrier HEX_BARRIER = new BlockHexBarrier();
    public static final BlockNullField NULL_FIELD = new BlockNullField();
    public static final BlockLunarEclipse LUNAR_ECLIPSE = new BlockLunarEclipse();
    public static final BlockCometTail COMET_TAIL = new BlockCometTail();
    public static final BlockMeteorShower METEOR_SHOWER = new BlockMeteorShower();

    private ModContent() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                MICRO_SINGULARITY,
                MICRO_WHITE_HOLE,
                MICRO_UNIVERSE,
                MICRO_STELLAR_SOURCE,
                MINIATURE_GALAXY,
                NEBULA_CORE,
                COLLAPSING_STAR,
                COSMIC_BACKGROUND_RADIATION_FIELD,
                SPATIAL_RIFT,
                WORMHOLE,
                GRAVITATIONAL_LENS,
                VOID_CRYSTAL,
                ARCANE_STAR_RING,
                PLASMA_STORM,
                TEMPORAL_RIFT,
                DIMENSIONAL_GATE,
                QUANTUM_BUBBLE,
                IMAGINARY_CUBE,
                SPECTRAL_PRISM,
                CRYSTAL_REFRACTION_FIELD,
                DATA_STREAM_MATRIX,
                STELLAR_HOURGLASS,
                VOID_LOTUS,
                ASTRAL_ALTAR_CORE,
                SOUL_VORTEX,
                STARDUST_FOUNTAIN,
                AURORA_VEIL,
                ABYSSAL_CORE,
                STORM_CORE,
                ENTROPY_CLOUD,
                DREAM_SHARDS,
                SOLAR_CORONA_BURST,
                FLAME_HEART,
                FROST_CORE,
                TERRA_BLOOM,
                FIREFLY_SWARM,
                POLLEN_BREEZE,
                BIOLUMINESCENT_SPORES,
                SHIELD_DOME,
                HEX_BARRIER,
                NULL_FIELD,
                LUNAR_ECLIPSE,
                COMET_TAIL,
                METEOR_SHOWER
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                itemBlock(MICRO_SINGULARITY),
                itemBlock(MICRO_WHITE_HOLE),
                itemBlock(MICRO_UNIVERSE),
                itemBlock(MICRO_STELLAR_SOURCE),
                itemBlock(MINIATURE_GALAXY),
                itemBlock(NEBULA_CORE),
                itemBlock(COLLAPSING_STAR),
                itemBlock(COSMIC_BACKGROUND_RADIATION_FIELD),
                itemBlock(SPATIAL_RIFT),
                itemBlock(WORMHOLE),
                itemBlock(GRAVITATIONAL_LENS),
                itemBlock(VOID_CRYSTAL),
                itemBlock(ARCANE_STAR_RING),
                itemBlock(PLASMA_STORM),
                itemBlock(TEMPORAL_RIFT),
                itemBlock(DIMENSIONAL_GATE),
                itemBlock(QUANTUM_BUBBLE),
                itemBlock(IMAGINARY_CUBE),
                itemBlock(SPECTRAL_PRISM),
                itemBlock(CRYSTAL_REFRACTION_FIELD),
                itemBlock(DATA_STREAM_MATRIX),
                itemBlock(STELLAR_HOURGLASS),
                itemBlock(VOID_LOTUS),
                itemBlock(ASTRAL_ALTAR_CORE),
                itemBlock(SOUL_VORTEX),
                itemBlock(STARDUST_FOUNTAIN),
                itemBlock(AURORA_VEIL),
                itemBlock(ABYSSAL_CORE),
                itemBlock(STORM_CORE),
                itemBlock(ENTROPY_CLOUD),
                itemBlock(DREAM_SHARDS),
                itemBlock(SOLAR_CORONA_BURST),
                itemBlock(FLAME_HEART),
                itemBlock(FROST_CORE),
                itemBlock(TERRA_BLOOM),
                itemBlock(FIREFLY_SWARM),
                itemBlock(POLLEN_BREEZE),
                itemBlock(BIOLUMINESCENT_SPORES),
                itemBlock(SHIELD_DOME),
                itemBlock(HEX_BARRIER),
                itemBlock(NULL_FIELD),
                itemBlock(LUNAR_ECLIPSE),
                itemBlock(COMET_TAIL),
                itemBlock(METEOR_SHOWER)
        );
    }

    public static void registerTileEntities() {
        registerTileEntity(TileMicroSingularity.class, "micro_singularity");
        registerTileEntity(TileMicroWhiteHole.class, "micro_white_hole");
        registerTileEntity(TileMicroUniverse.class, "micro_universe");
        registerTileEntity(TileMicroStellarSource.class, "micro_stellar_source");
        registerTileEntity(TileMiniatureGalaxy.class, "miniature_galaxy");
        registerTileEntity(TileNebulaCore.class, "nebula_core");
        registerTileEntity(TileCollapsingStar.class, "collapsing_star");
        registerTileEntity(TileCosmicBackgroundRadiationField.class, "cosmic_background_radiation_field");
        registerTileEntity(TileSpatialRift.class, "spatial_rift");
        registerTileEntity(TileWormhole.class, "wormhole");
        registerTileEntity(TileGravitationalLens.class, "gravitational_lens");
        registerTileEntity(TileVoidCrystal.class, "void_crystal");
        registerTileEntity(TileArcaneStarRing.class, "arcane_star_ring");
        registerTileEntity(TilePlasmaStorm.class, "plasma_storm");
        registerTileEntity(TileTemporalRift.class, "temporal_rift");
        registerTileEntity(TileDimensionalGate.class, "dimensional_gate");
        registerTileEntity(TileQuantumBubble.class, "quantum_bubble");
        registerTileEntity(TileImaginaryCube.class, "imaginary_cube");
        registerTileEntity(TileSpectralPrism.class, "spectral_prism");
        registerTileEntity(TileCrystalRefractionField.class, "crystal_refraction_field");
        registerTileEntity(TileDataStreamMatrix.class, "data_stream_matrix");
        registerTileEntity(TileStellarHourglass.class, "stellar_hourglass");
        registerTileEntity(TileVoidLotus.class, "void_lotus");
        registerTileEntity(TileAstralAltarCore.class, "astral_altar_core");
        registerTileEntity(TileSoulVortex.class, "soul_vortex");
        registerTileEntity(TileStardustFountain.class, "stardust_fountain");
        registerTileEntity(TileAuroraVeil.class, "aurora_veil");
        registerTileEntity(TileAbyssalCore.class, "abyssal_core");
        registerTileEntity(TileStormCore.class, "storm_core");
        registerTileEntity(TileEntropyCloud.class, "entropy_cloud");
        registerTileEntity(TileDreamShards.class, "dream_shards");
        registerTileEntity(TileSolarCoronaBurst.class, "solar_corona_burst");
        registerTileEntity(TileFlameHeart.class, "flame_heart");
        registerTileEntity(TileFrostCore.class, "frost_core");
        registerTileEntity(TileTerraBloom.class, "terra_bloom");
        registerTileEntity(TileFireflySwarm.class, "firefly_swarm");
        registerTileEntity(TilePollenBreeze.class, "pollen_breeze");
        registerTileEntity(TileBioluminescentSpores.class, "bioluminescent_spores");
        registerTileEntity(TileShieldDome.class, "shield_dome");
        registerTileEntity(TileHexBarrier.class, "hex_barrier");
        registerTileEntity(TileNullField.class, "null_field");
        registerTileEntity(TileLunarEclipse.class, "lunar_eclipse");
        registerTileEntity(TileCometTail.class, "comet_tail");
        registerTileEntity(TileMeteorShower.class, "meteor_shower");
    }

    private static ItemBlock itemBlock(Block block) {
        return (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private static void registerTileEntity(Class<? extends TileEntity> tileClass, String name) {
        GameRegistry.registerTileEntity(tileClass, new ResourceLocation(Reference.MOD_ID, name));
    }
}
