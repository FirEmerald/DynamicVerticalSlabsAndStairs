package com.firemerald.additionalplacements.block.interfaces;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.firemerald.additionalplacements.generation.APGenerationTypes;
import com.firemerald.additionalplacements.generation.GenerationType;
import com.firemerald.additionalplacements.util.*;
import com.firemerald.additionalplacements.util.stairs.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.firemerald.additionalplacements.block.VerticalStairBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IStairBlock<T extends Block> extends IPlacementBlock<T>
{
	public static interface IVanillaStairBlock extends IStairBlock<VerticalStairBlock>, IVanillaBlock<VerticalStairBlock>
	{
		public BlockState getModelStateImpl();
	}

	public default BlockState getBlockState(ComplexFacing facing, StairShape shape, BlockState currentState)
	{
		if (facing.stairsModelRotation == BlockRotation.IDENTITY && shape.getVanillaShape(facing) != null) {
			return getDefaultVanillaState(currentState)
					.setValue(StairsBlock.FACING, facing.vanillaStairsFacing)
					.setValue(StairsBlock.HALF, facing.vanillaStairsHalf)
					.setValue(StairsBlock.SHAPE, shape.getVanillaShape(facing));
		} else {
			Pair<CompressedStairFacing, StairFacingType> pair = CompressedStairFacing.getCompressedFacing(facing);
			return getDefaultAdditionalState(currentState)
					.setValue(VerticalStairBlock.FACING, pair.getLeft())
					.setValue(shapeProperty(), CompressedStairShape.getCompressedShape(pair.getRight(), shape));
		}
	}

	@Override
	public default BlockState transform(BlockState blockState, Function<Direction, Direction> transform)
	{
		Pair<ComplexFacing, StairShape> state = StairStateHelper.getFullState(blockState);
		ComplexFacing oldFacing = state.getLeft();
		ComplexFacing newFacing = ComplexFacing.forFacing(transform.apply(oldFacing.forward), transform.apply(oldFacing.up));
		return getBlockState(newFacing, state.getRight(), blockState);
	}
	
	public StairConnections allowedConnections();

	public EnumProperty<CompressedStairShape> shapeProperty();

	@Override
	public default BlockState updateShapeImpl(BlockState state, Direction direction, BlockState otherState, IWorld level, BlockPos pos, BlockPos otherPos)
	{
		ComplexFacing facing = StairStateHelper.getFacing(state);
		return facing == null ? state : getBlockState(facing, getShape(facing, level, pos), state);
	}

	@Override
	public default BlockState getStateForPlacementImpl(BlockItemUseContext context, BlockState blockState)
	{
		ComplexFacing facing = getFacing(context);
		return getBlockState(facing, getShape(facing, context.getLevel(), context.getClickedPos()), blockState);
	}
	
	//SIMPLE
	//SIMPLE_EXPANDED
	//COMPLEX
	public default StairShape getShape(ComplexFacing facing, IWorld level, BlockPos pos) {
		StairConnections connections = allowedConnections();
		//prioritize left, right and back, bottom, front, top
		boolean connectedLeft = false, connectedRight = false;
		{ //checking left for connection
			BlockState left = level.getBlockState(pos.relative(facing.left));
			ComplexFacing leftFacing = StairStateHelper.getFacing(left);
			if (leftFacing != null) {
				if (leftFacing == facing) connectedLeft = true;
				else if (connections.allowMixed && leftFacing.flipped() == facing) connectedLeft = true;
			}
		}
		{ //checking right for connection
			BlockState right = level.getBlockState(pos.relative(facing.right));
			ComplexFacing rightFacing = StairStateHelper.getFacing(right);
			if (rightFacing != null) {
				if (rightFacing == facing) connectedRight = true;
				else if (connections.allowMixed && rightFacing.flipped() == facing) connectedRight = true;
			}
		}
		if (connectedLeft && connectedRight) return StairShape.STRAIGHT;
		{ //checking behind for outer corner
			BlockState behind = level.getBlockState(pos.relative(facing.backward));
			ComplexFacing behindFacing = StairStateHelper.getFacing(behind);
			if (behindFacing != null) {
				if (connections.allowMixed && behindFacing.up != facing.up) behindFacing = behindFacing.flipped();
				if (behindFacing.up == facing.up) {
					if (connections.allowMixed) { //checking below for outer corner
						BlockState below = level.getBlockState(pos.relative(facing.down));
						ComplexFacing belowFacing = StairStateHelper.getFacing(below);
						if (belowFacing != null) {
							if (belowFacing.forward != facing.forward) belowFacing = belowFacing.flipped();
							if (belowFacing.forward == facing.forward) {
								if (belowFacing.up == behindFacing.forward) {
									if (belowFacing.up == facing.left) {
										if (!connectedLeft) return StairShape.OUTER_BOTH_LEFT;
									}
									else if (belowFacing.up == facing.right) {
										if (!connectedRight) return StairShape.OUTER_BOTH_RIGHT;
									}
								} else if (!connectedLeft && !connectedRight && belowFacing.down == behindFacing.forward) {
									if (belowFacing.down == facing.left) return StairShape.OUTER_BACK_RIGHT_BOTTOM_LEFT;
									else if (belowFacing.down == facing.right) return StairShape.OUTER_BACK_LEFT_BOTTOM_RIGHT;
								}
							}
						}
					}
					if (behindFacing.forward == facing.left) {
						if (!connectedLeft) return StairShape.OUTER_BACK_LEFT;
					}
					else if (behindFacing.forward == facing.right) {
						if (!connectedRight) return StairShape.OUTER_BACK_RIGHT;
					}
				}
			}
		}
		if (connections.allowVertical) { //checking below for outer corner
			BlockState below = level.getBlockState(pos.relative(facing.down));
			ComplexFacing belowFacing = StairStateHelper.getFacing(below);
			if (belowFacing != null) {
				if (connections.allowMixed && belowFacing.forward != facing.forward) belowFacing = belowFacing.flipped();
				if (belowFacing.forward == facing.forward) {
					if (belowFacing.up == facing.left) {
						if (!connectedLeft) return StairShape.OUTER_BOTTOM_LEFT;
					}
					else if (belowFacing.up == facing.right) {
						if (!connectedRight) return StairShape.OUTER_BOTTOM_RIGHT;
					}
				}
			}
		}
		{ //checking front for inner corner
			BlockState inFront = level.getBlockState(pos.relative(facing.forward));
			ComplexFacing inFrontFacing = StairStateHelper.getFacing(inFront);
			if (inFrontFacing != null) {
				if (connections.allowMixed && inFrontFacing.up != facing.up) inFrontFacing = inFrontFacing.flipped();
				if (inFrontFacing.up == facing.up) {
					if (connections.allowMixed) { //checking top for inner corner
						BlockState above = level.getBlockState(pos.relative(facing.up));
						ComplexFacing aboveFacing = StairStateHelper.getFacing(above);
						if (aboveFacing != null) {
							if (aboveFacing.forward != facing.forward) aboveFacing = aboveFacing.flipped();
							if (aboveFacing.forward == facing.forward && aboveFacing.up == inFrontFacing.forward) {
								if (aboveFacing.up == facing.left) return StairShape.INNER_BOTH_LEFT;
								else if (aboveFacing.up == facing.right) return StairShape.INNER_BOTH_RIGHT;
							}
						}
					}
					if (inFrontFacing.forward == facing.left) return StairShape.INNER_FRONT_LEFT;
					else if (inFrontFacing.forward == facing.right) return StairShape.INNER_FRONT_RIGHT;
				}
			}
		}
		if (connections.allowVertical) { //checking top for inner corner
			BlockState above = level.getBlockState(pos.relative(facing.up));
			ComplexFacing aboveFacing = StairStateHelper.getFacing(above);
			if (aboveFacing != null) {
				if (connections.allowMixed && aboveFacing.forward != facing.forward) aboveFacing = aboveFacing.flipped();
				if (aboveFacing.forward == facing.forward) {
					if (aboveFacing.up == facing.left) return StairShape.INNER_TOP_LEFT;
					else if (aboveFacing.up == facing.right) return StairShape.INNER_TOP_RIGHT;
				}
			}
		}
		return StairShape.STRAIGHT;
	}

	public static final float ARROW_OFFSET = -0.4375f;
	public static final float ARROW_OUTER = 0.375f;
	public static final float ARROW_INNER = 0.125f;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public default void renderPlacementPreview(MatrixStack pose, IVertexBuilder vertexConsumer, PlayerEntity player, BlockRayTraceResult result, float partial) {
		ComplexFacing facing = getFacing(result);
		//z is up
		//y is forward
		//x is right
		pose.pushPose();
		Matrix4f mat = new Matrix4f(new float[] {
				facing.right.getStepX(), facing.forward.getStepX(), facing.up.getStepX(), 0,
				facing.right.getStepY(), facing.forward.getStepY(), facing.up.getStepY(), 0,
				facing.right.getStepZ(), facing.forward.getStepZ(), facing.up.getStepZ(), 0,
				0, 0, 0, 1 
				});
		pose.last().pose().multiply(mat);
		pose.last().normal().mul(new Matrix3f(mat));
		Matrix4f poseMat = pose.last().pose();
		Matrix3f normMat = pose.last().normal();
		vertexConsumer.vertex(poseMat,  0          ,  ARROW_OUTER, ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  ARROW_OUTER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  ARROW_OUTER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  ARROW_INNER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();

		vertexConsumer.vertex(poseMat,  ARROW_INNER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  ARROW_INNER, -ARROW_OUTER, ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();

		vertexConsumer.vertex(poseMat,  ARROW_INNER, -ARROW_OUTER, ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -ARROW_INNER, -ARROW_OUTER, ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();

		vertexConsumer.vertex(poseMat, -ARROW_INNER, -ARROW_OUTER, ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -ARROW_INNER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();

		vertexConsumer.vertex(poseMat, -ARROW_INNER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -ARROW_OUTER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -ARROW_OUTER,  0          , ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  0          ,  ARROW_OUTER, ARROW_OFFSET).color(1, 1, 1, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		pose.popPose();
	}

	static final float OUTER_EDGE = .5f;
	static final float INNER_EDGE = .25f;

	@Override
	@OnlyIn(Dist.CLIENT)
	public default void renderPlacementHighlight(MatrixStack pose, IVertexBuilder vertexConsumer, PlayerEntity player, BlockRayTraceResult result, float partial)
	{
		Matrix4f poseMat = pose.last().pose();
		Matrix3f normMat = pose.last().normal();
		
		//outer box
		vertexConsumer.vertex(poseMat, -OUTER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  OUTER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  OUTER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  OUTER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  OUTER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -OUTER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -OUTER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -OUTER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		//inner box
		vertexConsumer.vertex(poseMat, -INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		//middle cross
		vertexConsumer.vertex(poseMat, -OUTER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  OUTER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -OUTER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  OUTER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		//corners
		vertexConsumer.vertex(poseMat, -OUTER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -INNER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  OUTER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  INNER_EDGE, -INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  INNER_EDGE, -OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -OUTER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat, -INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat, -INNER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  OUTER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		
		vertexConsumer.vertex(poseMat,  INNER_EDGE,  INNER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
		vertexConsumer.vertex(poseMat,  INNER_EDGE,  OUTER_EDGE, -OUTER_EDGE).color(0, 0, 0, 0.4f).normal(normMat, 0, 0, 1).endVertex();
	}
    
	@Override
	public default GenerationType<?, ?> getGenerationType() {
		return APGenerationTypes.stairs();
	}

	public default ComplexFacing getFacing(BlockRayTraceResult hitResult)
	{
		return getFacing(hitResult.getDirection(), hitResult.getLocation(), hitResult.getBlockPos());
	}

	public default ComplexFacing getFacing(BlockItemUseContext context)
	{
		return getFacing(context.getClickedFace(), context.getClickLocation(), context.getClickedPos());
	}

	public default ComplexFacing getFacing(Direction out, Vector3d hitPos, Vector3i blockPos)
	{
		return getFacing(out, 
				(float) (hitPos.x - blockPos.getX() - .5), 
				(float) (hitPos.y - blockPos.getY() - .5), 
				(float) (hitPos.z - blockPos.getZ() - .5));
	}

	public default ComplexFacing getFacing(Direction out, float hitX, float hitY, float hitZ)
	{
		switch (out.getAxis()) {
		case X:
			return getFacingFromSide((float) hitZ, (float) hitY, Direction.SOUTH, Direction.UP, out);
		case Y:
			return getFacingFromSide((float) hitX, (float) hitZ, Direction.EAST, Direction.SOUTH, out);
		case Z:
			return getFacingFromSide((float) hitX, (float) hitY, Direction.EAST, Direction.UP, out);
		default: 
			return ComplexFacing.SOUTH_UP;
		}
	}
	
	public default ComplexFacing getFacingFromSide(float localX, float localY, Direction localRight, Direction localUp, Direction localOut) {
		if (localY > localX) { //top-left half
			if (localY > -localX) { //top quarter
				return getFacingFromQuarter(localX, localY, localRight, localUp, localOut);
			} else { //left quarter
				return getFacingFromQuarter(localY, -localX, localUp, localRight.getOpposite(), localOut);
			}
		} else { //bottom-right half
			if (localY > -localX) { //right quarter
				return getFacingFromQuarter(-localY, localX, localUp.getOpposite(), localRight, localOut);
			} else { //bottom quarter
				return getFacingFromQuarter(-localX, -localY, localRight.getOpposite(), localUp.getOpposite(), localOut);
			}
		}
	}
	
	public default ComplexFacing getFacingFromQuarter(float localX, float localY, Direction localRight, Direction localUp, Direction localOut) {
		Direction forward, up;
		if (localY > INNER_EDGE) { //top half
			up = localUp.getOpposite();
			if (localX < -INNER_EDGE) { //top-left corner
				forward = localRight;
			} else if (localX > INNER_EDGE) { //top-right corner
				forward = localRight.getOpposite();
			} else { //top edge
				forward = localOut;
			}
		} else { //back corner
			up = localOut;
			forward = localUp.getOpposite();
		}
		return ComplexFacing.forFacing(forward, up);
	}

    @Override
	@OnlyIn(Dist.CLIENT)
	public default void addPlacementTooltip(ItemStack stack, @Nullable IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		tooltip.add(new TranslationTextComponent("tooltip.additionalplacements.vertical_placement"));
		tooltip.add(new TranslationTextComponent(allowedConnections().translationKey));
	}
}