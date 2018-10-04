package com.gmail.trentech.pjc.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.gmail.trentech.pjc.Main;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class BungeeManager {

	private static DataListener listener = new BungeeManager.DataListener();

	public static void init() {
		Sponge.getChannelRegistrar().getOrCreateRaw(Main.getPlugin(), "BungeeCord").addListener(Platform.Type.SERVER, listener);
	}

	private static ChannelBinding.RawDataChannel getChannel() {
		ChannelRegistrar channelRegistrar = Sponge.getChannelRegistrar();

		ChannelBinding.RawDataChannel channel;
		if (!channelRegistrar.getChannel("BungeeCord").isPresent()) {
			channel = channelRegistrar.getOrCreateRaw(Main.getPlugin(), "BungeeCord");
			channel.addListener(Platform.Type.SERVER, listener);
		} else {
			channel = channelRegistrar.getOrCreateRaw(Main.getPlugin(), "BungeeCord");
		}

		return channel;
	}

	public static void forward(Player player, String server, String channel, String data) {
		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		try {
			msgout.writeUTF(data);
		} catch (IOException exception){
			exception.printStackTrace();
		}
		
		getChannel().sendTo(player, buffer -> buffer.writeUTF("Forward").writeUTF(server).writeUTF(channel).writeByteArray(msgbytes.toByteArray()));
	}
	
	public static void forwardResponse(String server, Consumer<byte[]> consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("Forward").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("Forward") && buffer.readUTF().equals(server), buffer -> consumer.accept(buffer.readByteArray()));
	}
	
	public static void forwardPlayer(Player player, String server, String channel, String data) {
		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		try {
			msgout.writeUTF(data);
		} catch (IOException exception){
			exception.printStackTrace();
		}
		
		getChannel().sendTo(player, buffer -> buffer.writeUTF("ForwardToPlayer").writeUTF(player.getName()).writeUTF(channel).writeByteArray(msgbytes.toByteArray()));
	}
	
	public static void forwardPlayerResponse(String server, Consumer<byte[]> consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("ForwardToPlayer").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("ForwardToPlayer") && buffer.readUTF().equals(server), buffer -> consumer.accept(buffer.readByteArray()));
	}
	
	public static void connect(Player player, String server) {
		getChannel().sendTo(player, buffer -> buffer.writeUTF("Connect").writeUTF(server));
	}

	public static void connectOther(String player, String server, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("ConnectOther").writeUTF(player).writeUTF(server));
	}

	public static void kickPlayer(String player, Text reason, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("KickPlayer").writeUTF(player).writeUTF(TextSerializers.FORMATTING_CODE.serialize(reason)));
	}

	public static void message(String player, Text message, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("Message").writeUTF(player).writeUTF(TextSerializers.FORMATTING_CODE.serialize(message)));
	}

	public static void playerCount(String server, IntConsumer consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("PlayerCount").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("PlayerCount") && buffer.readUTF().equals(server), buffer -> consumer.accept(buffer.readInteger()));
	}

	public static void playerList(String server, Consumer<List<String>> consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("PlayerList").writeUTF(server));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("PlayerList") && buffer.readUTF().equals(server), buffer -> consumer.accept(ImmutableList.<String>builder().add(buffer.readUTF().split(", ")).build()));
	}

	public static void ip(Player player, Consumer<InetSocketAddress> consumer) {
		getChannel().sendTo(player, buffer -> buffer.writeUTF("IP"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("IP"), buffer -> consumer.accept(new InetSocketAddress(buffer.readUTF(), buffer.readInteger())));
	}

	public static void getServers(Consumer<List<String>> consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("GetServers"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("GetServers"), buffer -> consumer.accept(ImmutableList.<String>builder().add(buffer.readUTF().split(", ")).build()));
	}

	public static void getServer(Consumer<String> consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("GetServer"));
		listener.map.put(buffer -> buffer.resetRead().readUTF().equals("GetServer"), buffer -> consumer.accept(buffer.readUTF()));
	}

	public static void serverIP(String server, Consumer<InetSocketAddress> consumer, Player reference) {
		getChannel().sendTo(reference, buffer -> buffer.writeUTF("ServerIP").writeUTF(server));
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
