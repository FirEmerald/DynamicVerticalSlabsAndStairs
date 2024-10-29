package com.firemerald.additionalplacements.generation;

import java.util.function.Function;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.*;
import com.firemerald.additionalplacements.block.interfaces.ISimpleRotationBlock;
import com.firemerald.additionalplacements.config.BlockBlacklist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;

public class APGenerationTypes implements RegistrationInitializer {
	public static SimpleRotatableGenerationType<SlabBlock, VerticalSlabBlock> SLAB;
	public static VerticalStairsGenerationType<StairBlock, VerticalStairBlock> STAIRS;
	public static SimpleRotatableGenerationType<CarpetBlock, AdditionalCarpetBlock> CARPET;
	public static SimpleRotatableGenerationType<PressurePlateBlock, AdditionalPressurePlateBlock> PRESSURE_PLATE;
	public static SimpleRotatableGenerationType<WeightedPressurePlateBlock, AdditionalWeightedPressurePlateBlock> WEIGHTED_PRESSURE_PLATE;
	
	private static <T extends Block, U extends AdditionalPlacementBlock<T> & ISimpleRotationBlock> SimpleRotatableGenerationType<T, U> get(IRegistration register, Class<T> clazz, String name, String description, Function<T, U> constructor) {
		return register.registerType(clazz, new ResourceLocation(AdditionalPlacementsMod.MOD_ID, name), description, new SimpleRotatableGenerationType.Builder<T, U>().constructor(constructor));
	}
	
	private static <T extends Block, U extends AdditionalPlacementBlock<T>, V extends GenerationType<T, U>> V get(IRegistration register, Class<T> clazz, String name, String description, GenerationTypeConstructor<V> typeConstructor) {
		return register.registerType(clazz, new ResourceLocation(AdditionalPlacementsMod.MOD_ID, name), description, typeConstructor);
	}

	@Override
	public void onInitializeRegistration(IRegistration register) {
		SLAB                    = get(register, SlabBlock.class                 , "slab"                   , "Slabs"                   , 
				new SimpleRotatableGenerationType.Builder<SlabBlock, VerticalSlabBlock>()
				.blacklistModelRotation(new BlockBlacklist.Builder()
						.blockBlacklist(
								"minecraft:sandstone_slab", 
								"minecraft:cut_sandstone_slab", 
								"minecraft:red_sandstone_slab", 
								"minecraft:cut_red_sandstone_slab")
						.build())
				.blacklistTextureRotation(new BlockBlacklist.Builder()
						.blockBlacklist("minecraft:smooth_stone_slab")
						.build())
				.constructor(VerticalSlabBlock::of));
		STAIRS                  = get(register, StairBlock.class                , "stairs"                 , "Stairs"                  , 
				new VerticalStairsGenerationType.Builder<StairBlock, VerticalStairBlock>()
				.blacklistModelRotation(new BlockBlacklist.Builder()
						.blockBlacklist(
								"minecraft:sandstone_stairs", 
								"minecraft:red_sandstone_stairs")
						.build())
				.constructor(VerticalStairBlock::of));
		CARPET                  = get(register, CarpetBlock.class               , "carpet"                 , "Carpets"                 , AdditionalCarpetBlock::of);
		PRESSURE_PLATE          = get(register, PressurePlateBlock.class        , "pressure_plate"         , "Regular pressure plates" , AdditionalPressurePlateBlock::of);
		WEIGHTED_PRESSURE_PLATE = get(register, WeightedPressurePlateBlock.class, "weighted_pressure_plate", "Weighted pressure plates", AdditionalWeightedPressurePlateBlock::of);
	}
}
