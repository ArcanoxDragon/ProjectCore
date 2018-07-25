package com.gmail.trentech.pjc;

import java.io.File;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjc.help.Help;

import ninja.leaping.configurate.ConfigurationNode;

public class EventListener {

	@Listener
	public void onTabCompleteEvent(TabCompleteEvent event) {
		String args = event.getRawMessage();

		if (args.equals("hm ") || args.equals("helpme ")) {
			for (Help help : Help.getParents()) {
				event.getTabCompletions().add(help.getCommand());
			}
		}
	}
	
	@Listener(order = Order.LAST)
	public void onClientConnectionEventJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();

		ConfigurationNode node = ConfigManager.get(Main.getPlugin()).getConfig().getNode("settings", "help-message");
		
		if(!node.getNode("enable").getBoolean()) {
			return;
		}
		String defaultWorld = Sponge.getServer().getDefaultWorld().get().getWorldName();

		boolean firstJoin = !new File(defaultWorld + File.separator + "playerdata", player.getUniqueId().toString() + ".dat").exists();

		if (!firstJoin && node.getNode("first-join-only").getBoolean()) {
			return;
		}

		player.sendMessage(Text.of(TextColors.YELLOW, "/helpme will show commands and other detailed information of Plugins linked to ProjectCore. Command lists are clickable for descriptions and examples, and arguments can be hovered over for even more details."));
	}
}
