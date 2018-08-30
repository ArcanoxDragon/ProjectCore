package com.gmail.trentech.pjc.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.Main;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {

	private Path path;
	private CommentedConfigurationNode config;
	private ConfigurationLoader<CommentedConfigurationNode> loader;

	private static ConcurrentHashMap<String, ConcurrentHashMap<String, ConfigManager>> configManagers = new ConcurrentHashMap<>();

	private ConfigManager(PluginContainer plugin, String configName) {
		try {
			path = Sponge.getGame().getConfigManager().getPluginConfig(plugin).getDirectory().resolve(configName + ".conf");
			if (!Files.exists(path)) {
				Files.createFile(path);
				Main.instance().getLog().info("Creating new " + path.getFileName() + " file for " + plugin.getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		load();
	}

	public static ConfigManager init(PluginContainer plugin) {
		return init(plugin, plugin.getId());
	}

	public static ConfigManager init(PluginContainer plugin, String configName) {
		ConfigManager configManager = new ConfigManager(plugin, configName);
		CommentedConfigurationNode config = configManager.getConfig();

		if (configName.equalsIgnoreCase(Main.getPlugin().getId())) {
			if (config.getNode("theme", "pagination", "title").isVirtual()) {
				config.getNode("theme", "pagination", "title", "color").setValue(TextColors.GREEN.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				config.getNode("theme", "pagination", "title", "style").setValue("UNDERLINE").setComment("One or more(Comma seperated) of the following: BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED");
			}
			if (config.getNode("theme", "pagination", "padding").isVirtual()) {
				config.getNode("theme", "pagination", "padding", "color").setValue(TextColors.DARK_GREEN.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				;
				config.getNode("theme", "pagination", "padding", "style").setValue("STRIKETHROUGH,BOLD").setComment("One or more(Comma seperated) of the following: BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED");
				config.getNode("theme", "pagination", "padding", "text").setValue("=").setComment("Characters the padding will be made of");
			}
			if (config.getNode("theme", "list").isVirtual()) {
				config.getNode("theme", "list", "color").setValue(TextColors.GREEN.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				;
				config.getNode("theme", "list", "style").setValue("ITALIC").setComment("One or more(Comma seperated) of the following: 'BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED'");
				;
			}
			if (config.getNode("theme", "content").isVirtual()) {
				config.getNode("theme", "content", "color").setValue(TextColors.WHITE.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				;
				config.getNode("theme", "content", "style").setValue("NONE").setComment("One or more(Comma seperated) of the following: 'BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED'");
				;
			}
			if (config.getNode("theme", "headers").isVirtual()) {
				config.getNode("theme", "headers", "color").setValue(TextColors.GREEN.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				;
				config.getNode("theme", "headers", "style").setValue("BOLD").setComment("One or more(Comma seperated) of the following: 'BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED'");
				;
			}
			if (config.getNode("theme", "keys").isVirtual()) {
				config.getNode("theme", "keys", "color").setValue(TextColors.GREEN.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				;
				config.getNode("theme", "keys", "style").setValue("NONE").setComment("One or more(Comma seperated) of the following: 'BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED'");
				;
			}
			if (config.getNode("theme", "values").isVirtual()) {
				config.getNode("theme", "values", "color").setValue(TextColors.WHITE.getKey().toString()).setComment("One of the following: AQUA,BLACK,BLUE,DARK_AQUA,DARK_BLUE,DARK_GRAY,DARK_GREEN,DARK_PURPLE,DARK_RED,GOLD,GRAY,GREEN,LIGHT_PURPLE,RED,WHITE,YELLOW");
				;
				config.getNode("theme", "values", "style").setValue("NONE").setComment("One or more(Comma seperated) of the following: 'BOLD,ITALIC,UNDERLINE,STRIKETHROUGH,OBFUSCATED'");
				;
			}
			if (config.getNode("settings", "sql").isVirtual()) {
				config.getNode("settings", "sql").setComment("Default database values for all PJP dependant plugins. Can be overridden in specified plugin configs");
				config.getNode("settings", "sql", "enable").setValue(false);
				config.getNode("settings", "sql", "prefix").setValue("NONE");
				config.getNode("settings", "sql", "url").setValue("localhost:3306");
				config.getNode("settings", "sql", "username").setValue("root");
				config.getNode("settings", "sql", "password").setValue("password");			
			}
			if (config.getNode("settings", "help-message").isVirtual()) {
				config.getNode("settings", "help-message", "enable").setValue("true");
				config.getNode("settings", "help-message", "first-join-only").setValue("false");
			}
		}

		configManager.save();

		ConcurrentHashMap<String, ConfigManager> hash;

		if (!configManagers.containsKey(plugin.getId())) {
			hash = new ConcurrentHashMap<>();
		} else {
			hash = configManagers.get(plugin.getId());
		}

		hash.put(configName, configManager);

		configManagers.put(plugin.getId(), hash);

		return configManager;
	}

	public static ConfigManager get(PluginContainer plugin, String configName) {
		if (!configManagers.containsKey(plugin.getId()) || !configManagers.get(plugin.getId()).containsKey(configName)) {
			return init(plugin, configName);
		}

		return configManagers.get(plugin.getId()).get(configName);
	}

	public static ConfigManager get(PluginContainer plugin) {
		return ConfigManager.get(plugin, plugin.getId());
	}

	public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
		return loader;
	}

	public CommentedConfigurationNode getConfig() {
		return config;
	}

	public void save() {
		try {
			loader.save(config);
		} catch (IOException e) {
			Main.instance().getLog().error("Failed to save config");
			e.printStackTrace();
		}
	}

	private void load() {
		loader = HoconConfigurationLoader.builder().setPath(path).build();
		try {
			config = loader.load();
		} catch (IOException e) {
			Main.instance().getLog().error("Failed to load config");
			e.printStackTrace();
		}
	}
}
