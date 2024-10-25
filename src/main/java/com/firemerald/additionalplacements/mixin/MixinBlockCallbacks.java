package com.firemerald.additionalplacements.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.generation.Registration;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;

@Mixin(targets = "net.minecraftforge.registries.GameData$BlockCallbacks", remap = false)
public class MixinBlockCallbacks
{
	@Inject(method = "onAdd", at = @At("HEAD"), remap = false)
	private void onAdd(IForgeRegistryInternal<Block> owner, RegistryManager stage, int id, Block block, @Nullable Block oldBlock, CallbackInfo ci)
    {
		if (AdditionalPlacementsMod.dynamicRegistration)
		{
			Registration.tryApply(block, block.getRegistryName(), (blockId, obj) -> owner.register(obj));
		}
    }
}
