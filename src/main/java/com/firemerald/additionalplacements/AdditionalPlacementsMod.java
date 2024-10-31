package com.firemerald.additionalplacements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.firemerald.additionalplacements.config.APConfigs;
import com.firemerald.additionalplacements.generation.APGenerationTypes;
import com.firemerald.additionalplacements.generation.Registration;
import com.firemerald.additionalplacements.network.APNetwork;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AdditionalPlacementsMod.MOD_ID)
public class AdditionalPlacementsMod
{
	//TODO: fences walls panes bars
	public static final String MOD_ID = "additionalplacements";
	public static final String OLD_ID = "dvsas";
    public static final Logger LOGGER = LogManager.getLogger("Additional Placements");

    public static boolean dynamicRegistration = false;

    public AdditionalPlacementsMod()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener((FMLCommonSetupEvent event) -> APNetwork.register());
        bus.addListener(APConfigs::onModConfigsLoaded);
        bus.addListener(APConfigs::onModConfigsReloaded);
        Registration.addRegistration(new APGenerationTypes());
        LOGGER.warn("During block registration you may recieve several reports of \"Potentially Dangerous alternative prefix `additionalplacements`\". Ignore these, they are intended.");
    }
}
