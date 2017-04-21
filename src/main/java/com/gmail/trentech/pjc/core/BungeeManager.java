package com.gmail.trentech.pjc.core;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.gmail.trentech.pjc.Main;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class BungeeManager {

	private static BungeeManager bungeeManager;
	
	private ChannelBinding.RawDataChannel channel;
	private DataListener listener;

	private BungeeManager() {
		channel = Sponge.getChannelRegistrar().createRawChannel(Main.getPlugin(), "PJC");
		listener = new BungeeManager.DataListener();
				
		channel.addListener(Platform.Type.SERVER, listener);
	}
	
	public static BungeeManager get() {
		if(bungeeManager == null) {
			bungeeManager = new BungeeManager();
		}
		
		return bungeeManager;
	}

	public void connect(Player player, String server) {
		channel.sendTo(player, buffer -> buffer.writeUTF("Connect").writeUTF(server));
	}

	public void connectOther(String player, String server, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("ConnectOther").writeUTF(player).writeUTF(server));
	}

	public void kickPlayer(String player, Text reason, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("KickPlayer").writeUTF(player).writeUTF(TextSerializers.FORMATTING_CODE.serialize(reason)));
	}
	
	public void message(String player, Text message, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("Message").writeUTF(player).writeUTF(TextSerializers.FORMATTING_CODE.serialize(message)));
	}

	public void playerCount(String server, IntConsumer consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("PlayerCount").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("PlayerCount") && buffer.readUTF().equals(server), buffer -> consumer.accept(buffer.readInteger()));
	}

	public void playerList(String server, Consumer<List<String>> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("PlayerList").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("PlayerList") && buffer.readUTF().equals(server), buffer -> consumer.accept(ImmutableList.<String>builder().add(buffer.readUTF().split(", ")).build()));
	}

	public void ip(Player player, Consumer<InetSocketAddress> consumer) {
		channel.sendTo(player, buffer -> buffer.writeUTF("IP"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("IP"), buffer -> consumer.accept(new InetSocketAddress(buffer.readUTF(), buffer.readInteger())));
	}
	
	public void getServers(Consumer<List<String>> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("GetServers"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("GetServers"), buffer -> consumer.accept(ImmutableList.<String>builder().add(buffer.readUTF().split(", ")).build()));
	}

	public void getServer(Consumer<String> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("GetServer"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("GetServer"), buffer -> consumer.accept(buffer.readUTF()));
	}

	public void serverIP(String server, Consumer<InetSocketAddress> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("ServerIP").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("ServerIP") && buffer.readUTF().equals(server), buffer -> consumer.accept(new InetSocketAddress(buffer.readUTF(), buffer.readShort())));
	}

	private static class DataListener implements RawDataListener {
		ConcurrentMap<Predicate<ChannelBuf>, Consumer<ChannelBuf>> map = Maps.newConcurrentMap();

		@Override
		public void handlePayload(ChannelBuf buffer, RemoteConnection connection, Platform.Type type) {
			for (Map.Entry<Predicate<ChannelBuf>, Consumer<ChannelBuf>> entry : map.entrySet()) {
				if (entry.getKey().test(buffer)) {
					entry.getValue().accept(buffer);
					map.remove(entry.getKey());
					return;
				}
			}
		}
	}
}
