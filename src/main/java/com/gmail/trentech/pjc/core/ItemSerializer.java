package com.gmail.trentech.pjc.core;

import java.io.IOException;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

public class ItemSerializer {

	public static Optional<String> serialize(ItemStack item) {
		try {
			return Optional.of(DataFormats.JSON.write(item.toContainer()));
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public static Optional<ItemStack> deserialize(String item) {	
		try {
			DataContainer container = DataFormats.JSON.read(item);
			
			return Optional.of(Sponge.getDataManager().deserialize(ItemStack.class, container).get());
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
