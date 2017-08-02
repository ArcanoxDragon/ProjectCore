package com.gmail.trentech.pjc.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;

import com.flowpowered.math.vector.Vector2i;
import com.gmail.trentech.pjc.Main;
import com.gmail.trentech.pjc.crafting.ShapedRecipe;
import com.gmail.trentech.pjc.utils.InvalidItemTypeException;
import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class RecipeManager {

	public static void register(ConfigurationNode node, ItemStack itemStack) {
		Main.instance().getLog().info("Registering recipe for " + itemStack.getTranslation().get());

		try {
			Sponge.getRegistry().getCraftingRecipeRegistry().register(getShapedRecipe(node, itemStack));
		} catch (InvalidItemTypeException e) {
			e.printStackTrace();
		}
	}

	public static ShapedCraftingRecipe getShapedRecipe(ConfigurationNode node, ItemStack result) throws InvalidItemTypeException {
		HashMap<Vector2i, Ingredient> ingredients = new HashMap<>();
		List<String> worlds = new ArrayList<>();
		int width = 0;
		int height = 0;
		String name = null;
		
		for (Entry<Object, ? extends ConfigurationNode> child : node.getChildrenMap().entrySet()) {
			ConfigurationNode childNode = child.getValue();

			String key = childNode.getKey().toString();

			if (key.equals("enable") || key.equals("result") ) {
				continue;
			} else if(key.equals("name")) {
				name = childNode.getString();
			} else if(key.equals("worlds")) { 
				try {
					worlds = childNode.getList(TypeToken.of(String.class));
				} catch (ObjectMappingException e) {
					e.printStackTrace();
				}
			} else if (key.equals("grid_size")) {
				String[] size = childNode.getString().split("x");
				
				width = Integer.parseInt(size[0]);
				height = Integer.parseInt(size[1]);
			} else {
				String itemId = childNode.getString();
				String[] args = itemId.split(":");

				Optional<ItemType> optionalItemType = Sponge.getRegistry().getType(ItemType.class, itemId);

				if (optionalItemType.isPresent()) {
					ItemStack itemStack = ItemStack.builder().itemType(optionalItemType.get()).build();

					if (args.length == 3) {
						DataContainer container = itemStack.toContainer();
						DataQuery query = DataQuery.of('/', "UnsafeDamage");
						container.set(query, Integer.parseInt(args[2]));
						itemStack.setRawData(container);
					}
					String[] grid = key.split("x");
					
					ingredients.put(new Vector2i(Integer.parseInt(grid[0]), Integer.parseInt(grid[1])), Ingredient.builder().with(itemStack).build());
				} else {
					throw new InvalidItemTypeException("ItemType in config.conf at " + childNode.getKey().toString() + " is invalid");
				}
			}
		}
		
		return new ShapedRecipe(name, width, height, ingredients, worlds, result);
	}
}
