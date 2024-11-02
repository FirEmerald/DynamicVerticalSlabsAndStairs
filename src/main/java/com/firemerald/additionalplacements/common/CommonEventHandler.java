package com.firemerald.additionalplacements.common;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.interfaces.IPlacementBlock;
import com.firemerald.additionalplacements.commands.CommandExportTags;
import com.firemerald.additionalplacements.commands.CommandGenerateStairsDebugger;
import com.firemerald.additionalplacements.config.APConfigs;
import com.firemerald.additionalplacements.network.CheckDataConfigurationTask;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent.UpdateCause;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.network.GatherLoginConfigurationTasksEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler
{
	public static boolean misMatchedTags = false, autoGenerateFailed = false;
	protected static boolean reloadedFromChecker = false;

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event)
	{
		if (event.getItemStack().getItem() instanceof BlockItem)
		{
			Block block = ((BlockItem) event.getItemStack().getItem()).getBlock();
			if (block instanceof IPlacementBlock)
			{
				IPlacementBlock<?> verticalBlock = ((IPlacementBlock<?>) block);
				if (verticalBlock.hasAdditionalStates()) verticalBlock.appendHoverTextImpl(event.getItemStack(), event.getEntity() == null ? null : event.getEntity().level(), event.getToolTip(), event.getFlags());
			}
		}
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event)
	{
		CommandExportTags.register(event.getDispatcher());
		CommandGenerateStairsDebugger.register(event.getDispatcher(), event.getBuildContext());
	}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event)
	{
		if (event.getUpdateCause() == UpdateCause.SERVER_DATA_LOAD) {
			boolean fromAutoGenerate;
			if (reloadedFromChecker) {
				reloadedFromChecker = false;
				fromAutoGenerate = true;
			} else fromAutoGenerate = false;
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null) possiblyCheckTags(server, fromAutoGenerate);
		}
	}
	
	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		possiblyCheckTags(event.getServer(), false);
	}
	
	private static void possiblyCheckTags(MinecraftServer server, boolean fromAutoGenerate) {
		misMatchedTags = false;
		autoGenerateFailed = false;
		if (APConfigs.common().checkTags.get() && APConfigs.server().checkTags.get())
			TagMismatchChecker.startChecker(server, !fromAutoGenerate && APConfigs.common().autoRebuildTags.get() && APConfigs.server().autoRebuildTags.get(), fromAutoGenerate); //TODO halt on datapack reload
	}

	@SubscribeEvent
	public static void onMissingBlockMappings(MissingMappingsEvent event)
	{
		event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), AdditionalPlacementsMod.OLD_ID).forEach(mapping -> {
			String oldPath = mapping.getKey().getPath();
			if (oldPath.indexOf('.') < 0) //remap original format
			{
				String newPath = "minecraft." + oldPath;
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(AdditionalPlacementsMod.MOD_ID, newPath));
				if (block != Blocks.AIR)
				{
					mapping.remap(block);
					return;
				}
			}
			else //remap old mod ID
			{
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(AdditionalPlacementsMod.MOD_ID, oldPath));
				if (block != Blocks.AIR)
				{
					mapping.remap(block);
					return;
				}
			}
		});
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent event)
	{
		if (misMatchedTags && TagMismatchChecker.canGenerateTags(event.getEntity())) event.getEntity().sendSystemMessage(autoGenerateFailed ? TagMismatchChecker.FAILED : TagMismatchChecker.MESSAGE);
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event)
	{
		TagMismatchChecker.stopChecker();
		reloadedFromChecker = false;
	}
	
	@SubscribeEvent
	public static void onGetherLoginConfigurationTasks(GatherLoginConfigurationTasksEvent event) {
		event.addTask(new CheckDataConfigurationTask());
	}
}