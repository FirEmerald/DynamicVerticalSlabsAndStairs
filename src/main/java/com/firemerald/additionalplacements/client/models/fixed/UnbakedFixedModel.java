package com.firemerald.additionalplacements.client.models.fixed;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import com.firemerald.additionalplacements.client.IModelBakerExtensions;
import com.firemerald.additionalplacements.client.models.Unwrapper;
import com.firemerald.additionalplacements.client.models.IUnbakedGeometry;
import com.firemerald.additionalplacements.util.BlockRotation;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

public class UnbakedFixedModel implements IUnbakedGeometry<UnbakedFixedModel>
{
	public final AdditionalPlacementBlock<?> block;
	public final ResourceLocation ourModelLocation;
	public final ModelResourceLocation theirModelLocation;
	public final BlockRotation modelRotation;

	public UnbakedFixedModel(AdditionalPlacementBlock<?> block, ResourceLocation ourModelLocation, ModelResourceLocation theirModelLocation, BlockRotation modelRotation)
	{
		this.block = block;
		this.ourModelLocation = ourModelLocation;
		this.theirModelLocation = theirModelLocation;
		this.modelRotation = modelRotation;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, BlockModel context)
	{
		modelGetter.apply(ourModelLocation);
	}

	@Override
	public BakedModel bake(
			BlockModel context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelTransform, ItemOverrides overrides)
	{
		IModelBakerExtensions bakeryExt = (IModelBakerExtensions) bakery;
		return get(block, 
				Unwrapper.unwrap(bakery.bake(ourModelLocation, modelTransform)), 
				Unwrapper.unwrap(bakeryExt.apBakeUncached(bakeryExt.apGetTopLevelModel(theirModelLocation), BlockModelRotation.X0_Y0)),
				modelRotation);
	}
	
	private static record ModelKey(AdditionalPlacementBlock<?> block, BakedModel ourModel, BakedModel theirModel, BlockRotation modelRotation) {}
	
	private static final Map<ModelKey, BakedFixedModel> MODEL_CACHE = new HashMap<>();
	
	public static BakedFixedModel get(AdditionalPlacementBlock<?> block, BakedModel ourModel, BakedModel theirModel, BlockRotation modelRotation) {
		return MODEL_CACHE.computeIfAbsent(new ModelKey(block, ourModel, theirModel, modelRotation), key -> new BakedFixedModel(block, ourModel, theirModel, modelRotation));
	}
	
	public static void clearCache() {
		MODEL_CACHE.clear();
	}
}
