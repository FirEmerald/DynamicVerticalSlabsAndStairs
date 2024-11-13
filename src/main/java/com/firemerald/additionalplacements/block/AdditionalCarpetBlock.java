package com.firemerald.additionalplacements.block;

import com.firemerald.additionalplacements.block.interfaces.ICarpetBlock;
import com.firemerald.additionalplacements.client.models.definitions.CarpetModels;
import com.firemerald.additionalplacements.client.models.definitions.StateModelDefinition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AdditionalCarpetBlock extends AdditionalFloorBlock<CarpetBlock> implements ICarpetBlock<CarpetBlock>
{	
	public static final VoxelShape[] SHAPES = {
			Block.box(0, 15, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 16, 1),
			Block.box(0, 0, 15, 16, 16, 16),
			Block.box(0, 0, 0, 1, 16, 16),
			Block.box(15, 0, 0, 16, 16, 16)
	};

	public static AdditionalCarpetBlock of(CarpetBlock carpet)
	{
		return new AdditionalCarpetBlock(carpet);
	}

	private AdditionalCarpetBlock(CarpetBlock carpet)
	{
		super(carpet);
		this.registerDefaultState(copyProperties(getOtherBlockState(), this.stateDefinition.any()).setValue(PLACING, Direction.NORTH));
		((IVanillaCarpetBlock) carpet).setOtherBlock(this);
	}

	@Override
	public VoxelShape getShapeInternal(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context)
	{
		return SHAPES[state.getValue(PLACING).ordinal() - 1];
	}

	@Override
	public String getTagTypeName()
	{
		return "carpet";
	}

	@Override
	public String getTagTypeNamePlural()
	{
		return "carpets";
	}

	@Override
	public BlockState updateShapeImpl(BlockState thisState, Direction updatedDirection, BlockState otherState, IWorld level, BlockPos thisPos, BlockPos otherPos)
	{
		return !thisState.canSurvive(level, thisPos) ? Blocks.AIR.defaultBlockState() : thisState;
	}

	@Override
	@Deprecated
	public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos)
	{
		return !level.isEmptyBlock(pos.relative(state.getValue(PLACING)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getBaseModelPrefix() {
		return CarpetModels.BASE_MODEL_FOLDER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getDynamicModelPrefix() {
		return CarpetModels.DYNAMIC_MODEL_FOLDER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public StateModelDefinition getModelDefinition(BlockState state) {
		return CarpetModels.getModel(state);
	}
}
