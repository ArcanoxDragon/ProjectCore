package com.gmail.trentech.pjc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.sql.SqlService;

import com.gmail.trentech.pjc.Main;

import ninja.leaping.configurate.ConfigurationNode;

public class SQLManager {

	private static ConcurrentHashMap<String, ConcurrentHashMap<String, SQLManager>> sqlManagers = new ConcurrentHashMap<>();

	private PluginContainer plugin;
	private String database;
	private boolean mySql;
	private String url;
	private String prefix;
	private String username;
	private String password;
	
	private SQLManager(PluginContainer plugin, String database) {
		this.plugin = plugin;
		this.database = database;
		
		ConfigurationNode config = ConfigManager.get(Main.getPlugin()).getConfig();

		this.mySql = config.getNode("settings", "sql", "enable").getBoolean();
		this.prefix = config.getNode("settings", "sql", "prefix").getString();
		this.url = config.getNode("settings", "sql", "url").getString();
		this.username = config.getNode("settings", "sql", "username").getString();
		this.password = config.getNode("settings", "sql", "password").getString();
	}

	public static SQLManager get(PluginContainer plugin, String database) {
		if(!sqlManagers.containsKey(plugin.getId()) || !sqlManagers.get(plugin.getId()).containsKey(database)) {
			return init(plugin, database);
		}
		
		return sqlManagers.get(plugin.getId()).get(database);
	}

	public static SQLManager get(PluginContainer plugin) {
		return SQLManager.get(plugin, plugin.getId());
	}
	
	private static SQLManager init(PluginContainer plugin, String database) {
		SQLManager sqlManager = new SQLManager(plugin, database);

		ConcurrentHashMap<String, SQLManager> hash;
		
		if(!sqlManagers.containsKey(plugin.getId())) {
			hash = new ConcurrentHashMap<>();
		} else {
			hash = sqlManagers.get(plugin.getId());
		}
		
		hash.put(database, sqlManager);
		
		sqlManagers.put(plugin.getId(), hash);
		
		return sqlManager;
	}
	
	public DataSource getDataSource() throws SQLException {
		SqlService sqlService = Sponge.getServiceManager().provide(SqlService.class).get();
		
		if (mySql) {
	        Connection connection = sqlService.getDataSource("jdbc:mysql://" + url + "/?user=" + username + "&password=" + password).getConnection();
	        
	        PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + database);
			statement.executeUpdate();
			
			connection.close();

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