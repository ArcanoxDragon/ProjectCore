package com.gmail.trentech.pjc.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.ItemStack;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

public class ItemSerializer {

	public static String serialize(ItemStack itemStack) {
		ConfigurationNode node = DataTranslators.CONFIGURATION_NODE.translate(itemStack.toContainer());

		StringWriter stringWriter = new StringWriter();
		
		try {
			HoconConfigurationLoader.builder().setSink(() -> new BufferedWriter(stringWriter)).build().save(node);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stringWriter.toString();
	}

	public static ItemStack deserialize(String item) {
		ConfigurationNode node = null;
		
		try {
			node = HoconConfigurationLoader.builder().setSource(() -> new BufferedReader(new StringReader(item))).build().load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		DataView dataView = DataTranslators.CONFIGURATION_NODE.translate(node);

		return Sponge.getDataManager().deserialize(ItemStack.class, dataView).get();
	}

}
