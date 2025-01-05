package com.firemerald.additionalplacements.block.interfaces;

import javax.annotation.Nullable;

import com.firemerald.additionalplacements.block.AdditionalBasePressurePlateBlock;
import com.firemerald.additionalplacements.block.AdditionalFloorBlock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface IBasePressurePlateBlock<T extends Block> extends IFloorBlock<T>
{
	public static interface IVanillaBasePressurePlateBlock<T extends AdditionalBasePressurePlateBlock<?>> extends IBasePressurePlateBlock<T>, IVanillaBlock<T> {}

	@Override
	public default BlockState forPlacing(Direction dir, BlockState blockState)
	{
    	if (dir == Direction.DOWN) return getDefaultVanillaState(blockState);
    	else return getDefaultAdditionalState(blockState).setValue(AdditionalFloorBlock.PLACING, dir);
	}

	@Override
	@Nullable
	public default Direction getPlacing(BlockState blockState)
	{
		if (blockState.getBlock() instanceof PressurePlateBlock) return Direction.DOWN;
		else return blockState.getValue(AdditionalFloorBlock.PLACING);
	}
}