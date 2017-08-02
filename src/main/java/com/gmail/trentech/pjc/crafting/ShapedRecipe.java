package com.gmail.trentech.pjc.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector2i;

public class ShapedRecipe implements ShapedCraftingRecipe {

	private String name;
	private int width;
	private int height;
	private HashMap<Vector2i, Ingredient> ingredients = new HashMap<>();
	private List<String> worlds = new ArrayList<>();
	private ItemStack result;
	
	public ShapedRecipe(String name, int width, int height, HashMap<Vector2i, Ingredient> ingredients, List<String> worlds, ItemStack result) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.ingredients = ingredients;
		this.result = result;
	}
	
	@Override
	public boolean isValid(CraftingGridInventory grid, World world) {
		Vector2i dimensions = grid.getDimensions();
		
		if(dimensions.getX() < width || dimensions.getY() < height) {
			return false;
		}
		
		if(!worlds.contains("all") && !worlds.contains(world.getName())) {
			return false;
		}
		
		for(Entry<Vector2i, Ingredient> entry : ingredients.entrySet()) {
			Vector2i vector = entry.getKey();
			Ingredient ingredient = entry.getValue();
			
			Optional<ItemStack> optionalItemStack = grid.peek(vector.getX(), vector.getY());
			
			if(!optionalItemStack.isPresent()) {
				return false;
			}
			ItemStack itemStack = optionalItemStack.get();
			
			if(!ingredient.test(itemStack)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public ItemStackSnapshot getResult(CraftingGridInventory grid) {
		return result.createSnapshot();
	}

	@Override
	public List<ItemStackSnapshot> getRemainingItems(CraftingGridInventory grid) {
		return new ArrayList<>();
	}

	@Override
	public Optional<String> getGroup() {
		return Optional.empty();
	}

	@Override
	public ItemStackSnapshot getExemplaryResult() {
		return result.createSnapshot();
	}

	@Override
	public String getId() {
		return "pjc:" + getName().toLowerCase().replace(" ", "_");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Ingredient getIngredient(int x, int y) {
		return ingredients.get(new Vector2i(x,y));
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
