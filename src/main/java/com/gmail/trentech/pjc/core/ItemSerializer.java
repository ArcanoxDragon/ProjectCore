package com.gmail.trentech.pjc.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

public class ItemSerializer {

	public static Optional<String> serialize(ItemStack item) {
		try {
			StringWriter sink = new StringWriter();
			HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
			loader.save(loader.createEmptyNode().setValue(TypeToken.of(ItemStack.class), item));

			return Optional.of(sink.toString());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static Optional<ItemStack> deserialize(String item) {
		try {
			HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setSource(() -> new BufferedReader(new StringReader(item))).build();

			return Optional.of(loader.load().getValue(TypeToken.of(ItemStack.class)));
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
