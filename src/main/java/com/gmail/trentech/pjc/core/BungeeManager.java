package com.gmail.trentech.pjc.core;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class BungeeManager {

	public static ChannelBinding.RawDataChannel channel;
	public static DataListener listener;

	public static void connect(Player player, String server) {
		channel.sendTo(player, buffer -> buffer.writeUTF("Connect").writeUTF(server));
	}

	public static void connectOther(String player, String server, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("ConnectOther").writeUTF(player).writeUTF(server));
	}

	public static void kickPlayer(String player, Text reason, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("KickPlayer").writeUTF(player).writeUTF(TextSerializers.FORMATTING_CODE.serialize(reason)));
	}
	
	public static void message(String player, Text message, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("Message").writeUTF(player).writeUTF(TextSerializers.FORMATTING_CODE.serialize(message)));
	}

	public static void playerCount(String server, IntConsumer consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("PlayerCount").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("PlayerCount") && buffer.readUTF().equals(server), buffer -> consumer.accept(buffer.readInteger()));
	}

	public static void playerList(String server, Consumer<List<String>> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("PlayerList").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("PlayerList") && buffer.readUTF().equals(server), buffer -> consumer.accept(ImmutableList.<String>builder().add(buffer.readUTF().split(", ")).build()));
	}

	public static void ip(Player player, Consumer<InetSocketAddress> consumer) {
		channel.sendTo(player, buffer -> buffer.writeUTF("IP"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("IP"), buffer -> consumer.accept(new InetSocketAddress(buffer.readUTF(), buffer.readInteger())));
	}
	
	public static void getServers(Consumer<List<String>> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("GetServers"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("GetServers"), buffer -> consumer.accept(ImmutableList.<String>builder().add(buffer.readUTF().split(", ")).build()));
	}

	public static void getServer(Consumer<String> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("GetServer"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("GetServer"), buffer -> consumer.accept(buffer.readUTF()));
	}

	public static void serverIP(String server, Consumer<InetSocketAddress> consumer, Player reference) {
		channel.sendTo(reference, buffer -> buffer.writeUTF("ServerIP").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("ServerIP") && buffer.readUTF().equals(server), buffer -> consumer.accept(new InetSocketAddress(buffer.readUTF(), buffer.readShort())));
	}

	public static class DataListener implements RawDataListener {
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
