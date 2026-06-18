package com.gingeryj.spectrablocks.registry;

import com.gingeryj.spectrablocks.Reference;
import com.gingeryj.spectrablocks.block.BlockAlchemyTransmutationRing;
import com.gingeryj.spectrablocks.block.BlockAbyssalCore;
import com.gingeryj.spectrablocks.block.BlockArcaneStarRing;
import com.gingeryj.spectrablocks.block.BlockAstralAltarCore;
import com.gingeryj.spectrablocks.block.BlockAuroraVeil;
import com.gingeryj.spectrablocks.block.BlockBioluminescentSpores;
import com.gingeryj.spectrablocks.block.BlockCelestialCompassCore;
import com.gingeryj.spectrablocks.block.BlockChronoAnchor;
import com.gingeryj.spectrablocks.block.BlockCollapsingStar;
import com.gingeryj.spectrablocks.block.BlockCosmicBackgroundRadiationField;
import com.gingeryj.spectrablocks.block.BlockCrystalHarmonicResonator;
import com.gingeryj.spectrablocks.block.BlockCrystalRefractionField;
import com.gingeryj.spectrablocks.block.BlockDataStreamMatrix;
import com.gingeryj.spectrablocks.block.BlockDimensionalGate;
import com.gingeryj.spectrablocks.block.BlockDreamShards;
import com.gingeryj.spectrablocks.block.BlockEchoingVoidBell;
import com.gingeryj.spectrablocks.block.BlockEmberBloom;
import com.gingeryj.spectrablocks.block.BlockEnergyNexus;
import com.gingeryj.spectrablocks.block.BlockEntropyCloud;
import com.gingeryj.spectrablocks.block.BlockEtherReactorCore;
import com.gingeryj.spectrablocks.block.BlockEventHorizonMirror;
import com.gingeryj.spectrablocks.block.BlockFrostCrystalMist;
import com.gingeryj.spectrablocks.block.BlockGravitationalLens;
import com.gingeryj.spectrablocks.block.BlockGravityWell;
import com.gingeryj.spectrablocks.block.BlockHologramField;
import com.gingeryj.spectrablocks.block.BlockImaginaryCube;
import com.gingeryj.spectrablocks.block.BlockInkMirageBasin;
import com.gingeryj.spectrablocks.block.BlockLiquidStarlightPool;
import com.gingeryj.spectrablocks.block.BlockLunarPhaseOrrery;
import com.gingeryj.spectrablocks.block.BlockMagneticFluxCage;
import com.gingeryj.spectrablocks.block.BlockMicroSingularity;
import com.gingeryj.spectrablocks.block.BlockMicroStellarSource;
import com.gingeryj.spectrablocks.block.BlockMicroUniverse;
import com.gingeryj.spectrablocks.block.BlockMicroWhiteHole;
import com.gingeryj.spectrablocks.block.BlockMiniatureGalaxy;
import com.gingeryj.spectrablocks.block.BlockMirrorShardField;
import com.gingeryj.spectrablocks.block.BlockNebulaCore;
import com.gingeryj.spectrablocks.block.BlockNeonCircuitCore;
import com.gingeryj.spectrablocks.block.BlockNovaBloom;
import com.gingeryj.spectrablocks.block.BlockPhantomEye;
import com.gingeryj.spectrablocks.block.BlockPhaseGearLoom;
import com.gingeryj.spectrablocks.block.BlockPlasmaStorm;
import com.gingeryj.spectrablocks.block.BlockPrismaticRainfall;
import com.gingeryj.spectrablocks.block.BlockQuantumBubble;
import com.gingeryj.spectrablocks.block.BlockRadiantSigilField;
import com.gingeryj.spectrablocks.block.BlockRuneObelisk;
import com.gingeryj.spectrablocks.block.BlockShadowFlameLantern;
import com.gingeryj.spectrablocks.block.BlockSingularityLattice;
import com.gingeryj.spectrablocks.block.BlockSolarCoronaBurst;
import com.gingeryj.spectrablocks.block.BlockSoulVortex;
import com.gingeryj.spectrablocks.block.BlockSoundwaveResonator;
import com.gingeryj.spectrablocks.block.BlockSpatialRift;
import com.gingeryj.spectrablocks.block.BlockSpectralPrism;
import com.gingeryj.spectrablocks.block.BlockSpectralHourglassGate;
import com.gingeryj.spectrablocks.block.BlockStardustFountain;
import com.gingeryj.spectrablocks.block.BlockStellarHourglass;
import com.gingeryj.spectrablocks.block.BlockStormCore;
import com.gingeryj.spectrablocks.block.BlockTectonicFaultCore;
import com.gingeryj.spectrablocks.block.BlockTemporalRift;
import com.gingeryj.spectrablocks.block.BlockThermalDistortionField;
import com.gingeryj.spectrablocks.block.BlockVoidCrystal;
import com.gingeryj.spectrablocks.block.BlockVoidLotus;
import com.gingeryj.spectrablocks.block.BlockWormhole;
import com.gingeryj.spectrablocks.item.ItemEffectConfigurator;
import com.gingeryj.spectrablocks.tile.TileAlchemyTransmutationRing;
import com.gingeryj.spectrablocks.tile.TileAbyssalCore;
import com.gingeryj.spectrablocks.tile.TileArcaneStarRing;
import com.gingeryj.spectrablocks.tile.TileAstralAltarCore;
import com.gingeryj.spectrablocks.tile.TileAuroraVeil;
import com.gingeryj.spectrablocks.tile.TileBioluminescentSpores;
import com.gingeryj.spectrablocks.tile.TileCelestialCompassCore;
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
import com.gingeryj.spectrablocks.tile.TileInkMirageBasin;
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
import com.gingeryj.spectrablocks.tile.TilePhaseGearLoom;
import com.gingeryj.spectrablocks.tile.TilePlasmaStorm;
import com.gingeryj.spectrablocks.tile.TilePrismaticRainfall;
import com.gingeryj.spectrablocks.tile.TileQuantumBubble;
import com.gingeryj.spectrablocks.tile.TileRadiantSigilField;
import com.gingeryj.spectrablocks.tile.TileRuneObelisk;
import com.gingeryj.spectrablocks.tile.TileShadowFlameLantern;
import com.gingeryj.spectrablocks.tile.TileSingularityLattice;
import com.gingeryj.spectrablocks.tile.TileSolarCoronaBurst;
import com.gingeryj.spectrablocks.tile.TileSoulVortex;
import com.gingeryj.spectrablocks.tile.TileSoundwaveResonator;
import com.gingeryj.spectrablocks.tile.TileSpatialRift;
import com.gingeryj.spectrablocks.tile.TileSpectralPrism;
import com.gingeryj.spectrablocks.tile.TileSpectralHourglassGate;
import com.gingeryj.spectrablocks.tile.TileStardustFountain;
import com.gingeryj.spectrablocks.tile.TileStellarHourglass;
import com.gingeryj.spectrablocks.tile.TileStormCore;
import com.gingeryj.spectrablocks.tile.TileTectonicFaultCore;
import com.gingeryj.spectrablocks.tile.TileTemporalRift;
import com.gingeryj.spectrablocks.tile.TileThermalDistortionField;
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
    public static final BlockEnergyNexus ENERGY_NEXUS = new BlockEnergyNexus();
    public static final BlockHologramField HOLOGRAM_FIELD = new BlockHologramField();
    public static final BlockRuneObelisk RUNE_OBELISK = new BlockRuneObelisk();
    public static final BlockBioluminescentSpores BIOLUMINESCENT_SPORES = new BlockBioluminescentSpores();
    public static final BlockChronoAnchor CHRONO_ANCHOR = new BlockChronoAnchor();
    public static final BlockFrostCrystalMist FROST_CRYSTAL_MIST = new BlockFrostCrystalMist();
    public static final BlockNeonCircuitCore NEON_CIRCUIT_CORE = new BlockNeonCircuitCore();
    public static final BlockMirrorShardField MIRROR_SHARD_FIELD = new BlockMirrorShardField();
    public static final BlockEmberBloom EMBER_BLOOM = new BlockEmberBloom();
    public static final BlockGravityWell GRAVITY_WELL = new BlockGravityWell();
    public static final BlockEchoingVoidBell ECHOING_VOID_BELL = new BlockEchoingVoidBell();
    public static final BlockPrismaticRainfall PRISMATIC_RAINFALL = new BlockPrismaticRainfall();
    public static final BlockMagneticFluxCage MAGNETIC_FLUX_CAGE = new BlockMagneticFluxCage();
    public static final BlockShadowFlameLantern SHADOW_FLAME_LANTERN = new BlockShadowFlameLantern();
    public static final BlockLunarPhaseOrrery LUNAR_PHASE_ORRERY = new BlockLunarPhaseOrrery();
    public static final BlockCrystalHarmonicResonator CRYSTAL_HARMONIC_RESONATOR =
            new BlockCrystalHarmonicResonator();
    public static final BlockPhantomEye PHANTOM_EYE = new BlockPhantomEye();
    public static final BlockRadiantSigilField RADIANT_SIGIL_FIELD = new BlockRadiantSigilField();
    public static final BlockLiquidStarlightPool LIQUID_STARLIGHT_POOL = new BlockLiquidStarlightPool();
    public static final BlockNovaBloom NOVA_BLOOM = new BlockNovaBloom();
    public static final BlockEtherReactorCore ETHER_REACTOR_CORE = new BlockEtherReactorCore();
    public static final BlockEventHorizonMirror EVENT_HORIZON_MIRROR = new BlockEventHorizonMirror();
    public static final BlockSingularityLattice SINGULARITY_LATTICE = new BlockSingularityLattice();
    public static final BlockSpectralHourglassGate SPECTRAL_HOURGLASS_GATE = new BlockSpectralHourglassGate();
    public static final BlockCelestialCompassCore CELESTIAL_COMPASS_CORE = new BlockCelestialCompassCore();
    public static final BlockSoundwaveResonator SOUNDWAVE_RESONATOR = new BlockSoundwaveResonator();
    public static final BlockThermalDistortionField THERMAL_DISTORTION_FIELD = new BlockThermalDistortionField();
    public static final BlockAlchemyTransmutationRing ALCHEMY_TRANSMUTATION_RING =
            new BlockAlchemyTransmutationRing();
    public static final BlockPhaseGearLoom PHASE_GEAR_LOOM = new BlockPhaseGearLoom();
    public static final BlockTectonicFaultCore TECTONIC_FAULT_CORE = new BlockTectonicFaultCore();
    public static final BlockInkMirageBasin INK_MIRAGE_BASIN = new BlockInkMirageBasin();
    public static final ItemEffectConfigurator EFFECT_CONFIGURATOR = new ItemEffectConfigurator();

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
                ENERGY_NEXUS,
                HOLOGRAM_FIELD,
                RUNE_OBELISK,
                BIOLUMINESCENT_SPORES,
                CHRONO_ANCHOR,
                FROST_CRYSTAL_MIST,
                NEON_CIRCUIT_CORE,
                MIRROR_SHARD_FIELD,
                EMBER_BLOOM,
                GRAVITY_WELL,
                ECHOING_VOID_BELL,
                PRISMATIC_RAINFALL,
                MAGNETIC_FLUX_CAGE,
                SHADOW_FLAME_LANTERN,
                LUNAR_PHASE_ORRERY,
                CRYSTAL_HARMONIC_RESONATOR,
                PHANTOM_EYE,
                RADIANT_SIGIL_FIELD,
                LIQUID_STARLIGHT_POOL,
                NOVA_BLOOM,
                ETHER_REACTOR_CORE,
                EVENT_HORIZON_MIRROR,
                SINGULARITY_LATTICE,
                SPECTRAL_HOURGLASS_GATE,
                CELESTIAL_COMPASS_CORE,
                SOUNDWAVE_RESONATOR,
                THERMAL_DISTORTION_FIELD,
                ALCHEMY_TRANSMUTATION_RING,
                PHASE_GEAR_LOOM,
                TECTONIC_FAULT_CORE,
                INK_MIRAGE_BASIN
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
                itemBlock(ENERGY_NEXUS),
                itemBlock(HOLOGRAM_FIELD),
                itemBlock(RUNE_OBELISK),
                itemBlock(BIOLUMINESCENT_SPORES),
                itemBlock(CHRONO_ANCHOR),
                itemBlock(FROST_CRYSTAL_MIST),
                itemBlock(NEON_CIRCUIT_CORE),
                itemBlock(MIRROR_SHARD_FIELD),
                itemBlock(EMBER_BLOOM),
                itemBlock(GRAVITY_WELL),
                itemBlock(ECHOING_VOID_BELL),
                itemBlock(PRISMATIC_RAINFALL),
                itemBlock(MAGNETIC_FLUX_CAGE),
                itemBlock(SHADOW_FLAME_LANTERN),
                itemBlock(LUNAR_PHASE_ORRERY),
                itemBlock(CRYSTAL_HARMONIC_RESONATOR),
                itemBlock(PHANTOM_EYE),
                itemBlock(RADIANT_SIGIL_FIELD),
                itemBlock(LIQUID_STARLIGHT_POOL),
                itemBlock(NOVA_BLOOM),
                itemBlock(ETHER_REACTOR_CORE),
                itemBlock(EVENT_HORIZON_MIRROR),
                itemBlock(SINGULARITY_LATTICE),
                itemBlock(SPECTRAL_HOURGLASS_GATE),
                itemBlock(CELESTIAL_COMPASS_CORE),
                itemBlock(SOUNDWAVE_RESONATOR),
                itemBlock(THERMAL_DISTORTION_FIELD),
                itemBlock(ALCHEMY_TRANSMUTATION_RING),
                itemBlock(PHASE_GEAR_LOOM),
                itemBlock(TECTONIC_FAULT_CORE),
                itemBlock(INK_MIRAGE_BASIN),
                EFFECT_CONFIGURATOR
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
        registerTileEntity(TileEnergyNexus.class, "energy_nexus");
        registerTileEntity(TileHologramField.class, "hologram_field");
        registerTileEntity(TileRuneObelisk.class, "rune_obelisk");
        registerTileEntity(TileBioluminescentSpores.class, "bioluminescent_spores");
        registerTileEntity(TileChronoAnchor.class, "chrono_anchor");
        registerTileEntity(TileFrostCrystalMist.class, "frost_crystal_mist");
        registerTileEntity(TileNeonCircuitCore.class, "neon_circuit_core");
        registerTileEntity(TileMirrorShardField.class, "mirror_shard_field");
        registerTileEntity(TileEmberBloom.class, "ember_bloom");
        registerTileEntity(TileGravityWell.class, "gravity_well");
        registerTileEntity(TileEchoingVoidBell.class, "echoing_void_bell");
        registerTileEntity(TilePrismaticRainfall.class, "prismatic_rainfall");
        registerTileEntity(TileMagneticFluxCage.class, "magnetic_flux_cage");
        registerTileEntity(TileShadowFlameLantern.class, "shadow_flame_lantern");
        registerTileEntity(TileLunarPhaseOrrery.class, "lunar_phase_orrery");
        registerTileEntity(TileCrystalHarmonicResonator.class, "crystal_harmonic_resonator");
        registerTileEntity(TilePhantomEye.class, "phantom_eye");
        registerTileEntity(TileRadiantSigilField.class, "radiant_sigil_field");
        registerTileEntity(TileLiquidStarlightPool.class, "liquid_starlight_pool");
        registerTileEntity(TileNovaBloom.class, "nova_bloom");
        registerTileEntity(TileEtherReactorCore.class, "ether_reactor_core");
        registerTileEntity(TileEventHorizonMirror.class, "event_horizon_mirror");
        registerTileEntity(TileSingularityLattice.class, "singularity_lattice");
        registerTileEntity(TileSpectralHourglassGate.class, "spectral_hourglass_gate");
        registerTileEntity(TileCelestialCompassCore.class, "celestial_compass_core");
        registerTileEntity(TileSoundwaveResonator.class, "soundwave_resonator");
        registerTileEntity(TileThermalDistortionField.class, "thermal_distortion_field");
        registerTileEntity(TileAlchemyTransmutationRing.class, "alchemy_transmutation_ring");
        registerTileEntity(TilePhaseGearLoom.class, "phase_gear_loom");
        registerTileEntity(TileTectonicFaultCore.class, "tectonic_fault_core");
        registerTileEntity(TileInkMirageBasin.class, "ink_mirage_basin");
    }

    private static ItemBlock itemBlock(Block block) {
        return (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private static void registerTileEntity(Class<? extends TileEntity> tileClass, String name) {
        GameRegistry.registerTileEntity(tileClass, new ResourceLocation(Reference.MOD_ID, name));
    }
}
