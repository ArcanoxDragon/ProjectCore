package com.gmail.trentech.pjc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.sql.SqlService;

import com.gmail.trentech.pjc.Main;

import ninja.leaping.configurate.ConfigurationNode;

public class SQLManager {

	private PluginContainer plugin;
	private String database;
	private boolean mySql;
	private String url;
	private String prefix;
	private String username;
	private String password;

	private SQLManager(PluginContainer plugin) {
		this.plugin = plugin;

		initSettings();
	}

	public void initSettings() {
		ConfigManager configManager = ConfigManager.get(plugin);
		ConfigurationNode config = configManager.getConfig();
		
		ConfigurationNode defaultConfig = ConfigManager.get(Main.getPlugin()).getConfig();
		
		if(config.getNode("settings", "sql", "enable").isVirtual()) {
			this.mySql = config.getNode("settings", "sql", "enable").setValue(defaultConfig.getNode("settings", "sql", "enable").getBoolean()).getBoolean();
		} else {
			this.mySql = config.getNode("settings", "sql", "enable").getBoolean();
		}
		
		if(config.getNode("settings", "sql", "prefix").isVirtual()) {
			this.prefix = config.getNode("settings", "sql", "prefix").setValue(defaultConfig.getNode("settings", "sql", "prefix").getString()).getString();
		} else {
			this.prefix = config.getNode("settings", "sql", "prefix").getString();
		}
		
		if(config.getNode("settings", "sql", "url").isVirtual()) {
			this.url = config.getNode("settings", "sql", "url").setValue(defaultConfig.getNode("settings", "sql", "url").getString()).getString();
		} else {
			this.url = config.getNode("settings", "sql", "url").getString();
		}
		
		if(config.getNode("settings", "sql", "username").isVirtual()) {
			this.username = config.getNode("settings", "sql", "username").setValue(defaultConfig.getNode("settings", "sql", "username").getString()).getString();
		} else {
			this.username = config.getNode("settings", "sql", "username").getString();
		}
		
		if(config.getNode("settings", "sql", "password").isVirtual()) {
			this.password = config.getNode("settings", "sql", "password").setValue(defaultConfig.getNode("settings", "sql", "password").getString()).getString();
		} else {
			this.password = config.getNode("settings", "sql", "password").getString();
		}
		
		if(config.getNode("settings", "sql", "database").isVirtual()) {
			this.database = config.getNode("settings", "sql", "database").setValue(plugin.getId()).getString();
		} else {
			this.database = config.getNode("settings", "sql", "database").getString();
		}
		
		configManager.save();
	}
	
	public static SQLManager get(PluginContainer plugin) {
		return new SQLManager(plugin);
	}

	public DataSource getDataSource() throws SQLException {
		SqlService sqlService = Sponge.getServiceManager().provide(SqlService.class).get();

		if (mySql) {
			Connection connection = sqlService.getDataSource("jdbc:mysql://" + url + "/?user=" + username + "&password=" + password).getConnection();
			PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS `" + database + "`");

			try {
				statement.executeUpdate();
			} finally {
				statement.close();
				connection.close();
			}

			return sqlService.getDataSource("jdbc:mysql://" + url + "/" + database + "?user=" + username + "&password=" + password);
		} else {
			return sqlService.getDataSource("jdbc:h2:./config/" + plugin.getId() + "/" + database);
		}
	}

	public String getPrefix(String table) {
		if (!prefix.equalsIgnoreCase("NONE") && mySql) {
			return ("`" + prefix + table + "`").toUpperCase();
		}
		return ("`" + table + "`").toUpperCase();
	}

	public String stripPrefix(String table) {
		if (!prefix.equalsIgnoreCase("NONE") && mySql) {
			return table.toUpperCase().replace(prefix.toUpperCase(), "").toUpperCase();
		}
		return table.toUpperCase();
	}
}