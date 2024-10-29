package com.firemerald.additionalplacements.config;

import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.firemerald.additionalplacements.generation.GenerationType;
import com.firemerald.additionalplacements.generation.Registration;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class APConfigs {
    private static StartupConfig startup;
    private static ModConfigSpec startupSpec;
    private static CommonConfig common;
    private static ModConfigSpec commonSpec;
    private static ServerConfig server;
    private static ModConfigSpec serverSpec;
    private static ClientConfig client;
    private static ModConfigSpec clientSpec;
    
    public static void init() {
        final Pair<StartupConfig, ModConfigSpec> startupSpecPair = new ModConfigSpec.Builder().configure(StartupConfig::new);
        startup = startupSpecPair.getLeft();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.STARTUP, startupSpec = startupSpecPair.getRight());
        final Pair<CommonConfig, ModConfigSpec> commonSpecPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        common = commonSpecPair.getLeft();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, commonSpec = commonSpecPair.getRight());
        final Pair<ServerConfig, ModConfigSpec> serverSpecPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        server = serverSpecPair.getLeft();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, serverSpec = serverSpecPair.getRight());
        final Pair<ClientConfig, ModConfigSpec> clientSpecPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        client = clientSpecPair.getLeft();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, clientSpec = clientSpecPair.getRight());}
    
    public static StartupConfig bootup() {
    	return startup;
    }
    
    public static CommonConfig common() {
    	return common;
    }
    
    public static ServerConfig server() {
    	return server;
    }
    
    public static boolean serverLoaded() {
    	return serverSpec.isLoaded();
    }
    
    public static ClientConfig client() {
    	return client;
    }
    
    public static void onModConfigsLoaded(ModConfigEvent.Loading event) {
    	if (event.getConfig().getSpec() == startupSpec) sendConfigEvent(GenerationType::onBootupConfigLoaded);
    	else if (event.getConfig().getSpec() == commonSpec) sendConfigEvent(GenerationType::onCommonConfigLoaded);
    	else if (event.getConfig().getSpec() == serverSpec) sendConfigEvent(GenerationType::onServerConfigLoaded);
    	else if (event.getConfig().getSpec() == clientSpec) sendConfigEvent(GenerationType::onClientConfigLoaded);
    }
    
    public static void onModConfigsReloaded(ModConfigEvent.Reloading event) {
    	if (event.getConfig().getSpec() == commonSpec) sendConfigEvent(GenerationType::onCommonConfigReloaded);
    	else if (event.getConfig().getSpec() == serverSpec) sendConfigEvent(GenerationType::onServerConfigReloaded);
    	else if (event.getConfig().getSpec() == clientSpec) sendConfigEvent(GenerationType::onClientConfigReloaded);
    }
    
    public static void sendConfigEvent(Consumer<GenerationType<?, ?>> action) {
    	Registration.forEach((name, type) -> action.accept(type));
    }
}