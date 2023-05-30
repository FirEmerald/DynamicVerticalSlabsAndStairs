package com.firemerald.additionalplacements.block;

import com.firemerald.additionalplacements.block.interfaces.IStairBlock;
import com.firemerald.additionalplacements.util.VoxelShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class VerticalStairBlock extends AdditionalPlacementLiquidBlock<StairsBlock> implements IStairBlock<StairsBlock>
{
	public static final EnumProperty<EnumPlacing> PLACING = EnumProperty.create("placing", EnumPlacing.class);
	public static final EnumProperty<EnumShape> SHAPE = EnumProperty.create("shape", EnumShape.class);
	public static final VoxelShape[][] SHAPE_CACHE = new VoxelShape[4][17];

	static
	{
		for (EnumPlacing placing : EnumPlacing.values())
		{
			VoxelShape[] shapes = SHAPE_CACHE[placing.ordinal()];
			shapes[EnumShape.STRAIGHT.ordinal()] = VoxelShapes.getStraightStairs(placing.clockWiseFront, placing.counterClockWiseFront);
			shapes[EnumShape.INNER_UP.ordinal()] = VoxelShapes.getInnerStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.UP);
			shapes[EnumShape.OUTER_UP_FROM_CW.ordinal()] = shapes[EnumShape.OUTER_UP_FROM_CCW.ordinal()] = shapes[EnumShape.OUTER_UP.ordinal()] = VoxelShapes.getOuterStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.UP);
			shapes[EnumShape.OUTER_FLAT_UP_CW.ordinal()] = shapes[EnumShape.OUTER_FLAT_UP_FROM_CW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.UP);
			shapes[EnumShape.OUTER_FLAT_UP_CCW.ordinal()] = shapes[EnumShape.OUTER_FLAT_UP_FROM_CCW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.counterClockWiseFront, placing.clockWiseFront, Direction.UP);
			shapes[EnumShape.INNER_DOWN.ordinal()] = VoxelShapes.getInnerStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_DOWN_FROM_CW.ordinal()] = shapes[EnumShape.OUTER_DOWN_FROM_CCW.ordinal()] = shapes[EnumShape.OUTER_DOWN.ordinal()] = VoxelShapes.getOuterStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_FLAT_DOWN_CW.ordinal()] = shapes[EnumShape.OUTER_FLAT_DOWN_FROM_CW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.clockWiseFront, placing.counterClockWiseFront, Direction.DOWN);
			shapes[EnumShape.OUTER_FLAT_DOWN_CCW.ordinal()] = shapes[EnumShape.OUTER_FLAT_DOWN_FROM_CCW.ordinal()] = VoxelShapes.getOuterFlatStairs(placing.counterClockWiseFront, placing.clockWiseFront, Direction.DOWN);
		}
	}

	@SuppressWarnings("deprecation")
	public VerticalStairBlock(StairsBlock stairs)
	{
		super(stairs);
		this.registerDefaultState(copyProperties(getModelState(), this.stateDefinition.any()).setValue(PLACING, EnumPlacing.NORTH_EAST).setValue(SHAPE, EnumShape.STRAIGHT));
		((IVanillaStairBlock) stairs).setOtherBlock(this);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(PLACING, SHAPE);
		super.createBlockStateDefinition(builder);
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context)
	{
		return SHAPE_CACHE[state.getValue(PLACING).ordinal()][state.getValue(SHAPE).ordinal()];
	}

	@Override
	public BlockState getDefaultVanillaState(BlockState currentState)
	{
		return currentState.is(parentBlock) ? currentState : copyProperties(currentState, parentBlock.defaultBlockState());
	}

	@Override
	public BlockState getDefaultAdditionalState(BlockState currentState)
	{
		return currentState.is(this) ? currentState : copyProperties(currentState, this.defaultBlockState());
	}

	@Override
	public String getTagTypeName()
	{
		return "stair";
	}

	@Override
	public String getTagTypeNamePlural()
	{
		return "stairs";
	}

    public static enum EnumPlacing implements IStringSerializable
    {
    	NORTH_EAST("north_east", Direction.NORTH, Direction.EAST),
    	EAST_SOUTH("east_south", Direction.EAST, Direction.SOUTH),
    	SOUTH_WEST("south_west", Direction.SOUTH, Direction.WEST),
    	WEST_NORTH("west_north", Direction.WEST, Direction.NORTH);

        private final String name;
        public final Direction counterClockWiseFront, clockWiseFront, counterClockWiseBack, clockWiseBack;

        private EnumPlacing(String name, Direction counterClockWise, Direction clockWise)
        {
            this.name = name;
            this.counterClockWiseFront = counterClockWise;
            this.clockWiseFront = clockWise;
            this.counterClockWiseBack = counterClockWise.getOpposite();
            this.clockWiseBack = clockWise.getOpposite();
        }

        @Override
		public String toString()
        {
            return this.name;
        }

        @Override
		public String getSerializedName()
        {
            return this.name;
        }
    }

    public static enum EnumShape implements IStringSerializable
    {
        STRAIGHT("straight", false, false, false, false),


        INNER_UP("inner_up", true, false, false, false),

        OUTER_UP("outer_up", true, false, false, false),
        OUTER_UP_FROM_CW("outer_up_from_clockwise", true, false, true, false),
        OUTER_UP_FROM_CCW("outer_up_from_counter_clockwise", true, false, false, true),

        OUTER_FLAT_UP_CW("outer_flat_up_clockwise", true, false, false, false),
        OUTER_FLAT_UP_FROM_CW("outer_flat_up_from_clockwise", true, false, true, false),

        OUTER_FLAT_UP_CCW("outer_flat_up_counter_clockwise", true, false, false, false),
        OUTER_FLAT_UP_FROM_CCW("outer_flat_up_from_counter_clockwise", true, false, false, true),


        INNER_DOWN("inner_down", false, true, false, false),

        OUTER_DOWN("outer_down", false, true, false, false),
        OUTER_DOWN_FROM_CW("outer_down_from_clockwise", false, true, true, false),
        OUTER_DOWN_FROM_CCW("outer_down_from_counter_clockwise", false, true, false, true),

        OUTER_FLAT_DOWN_CW("outer_flat_down_clockwise", false, true, false, false),
        OUTER_FLAT_DOWN_FROM_CW("outer_flat_down_from_clockwise", false, true, true, false),

        OUTER_FLAT_DOWN_CCW("outer_flat_down_counter_clockwise", false, true, false, false),
        OUTER_FLAT_DOWN_FROM_CCW("outer_flat_down_from_counter_clockwise", false, true, false, true);

        private final String name;
        public final boolean isUp, isDown, isClockwise, isCounterClockwise;

        private EnumShape(String name, boolean isUp, boolean isDown, boolean isClockwise, boolean isCounterClockwise)
        {
            this.name = name;
            this.isUp = isUp;
            this.isDown = isDown;
            this.isClockwise = isClockwise;
            this.isCounterClockwise = isCounterClockwise;
        }

        @Override
		public String toString()
        {
            return this.name;
        }

        @Override
		public String getSerializedName()
        {
            return this.name;
        }
    }
}
