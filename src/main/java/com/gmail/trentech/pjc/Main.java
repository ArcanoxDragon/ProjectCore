package com.gmail.trentech.pjc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import com.gmail.trentech.pjc.commands.CMDHelp;
import com.gmail.trentech.pjc.core.BungeeManager;
import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjc.utils.Resource;
import com.google.inject.Inject;

@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, description = Resource.DESCRIPTION, authors = Resource.AUTHOR, url = Resource.URL)
public class Main {

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path path;

	@Inject
	private Logger log;

	private static PluginContainer plugin;
	private static Main instance;

	@Listener(order = Order.EARLY)
	public void onGameConstructionEvent(GameConstructionEvent event) {
		plugin = Sponge.getPluginManager().getPlugin(Resource.ID).get();
		instance = this;

		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Listener
	public void onInitialization(GameInitializationEvent event) {
		ConfigManager.init(getPlugin());

		Sponge.getEventManager().registerListeners(this, new EventListener());
		Sponge.getCommandManager().register(this, CMDHelp.cmdHelp, "helpme", "hm");
	}
	
	@Listener
	public void onStartedServerEvent(GameStartedServerEvent event) {
		BungeeManager.init();
	}
	
	@Listener
	public void onReloadEvent(GameReloadEvent event) {
		ConfigManager.init(getPlugin());
	}
	
	public Logger getLog() {
		return log;
	}

	public Path getPath() {
		return path;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}

	public static Main instance() {
		return instance;
	}
}