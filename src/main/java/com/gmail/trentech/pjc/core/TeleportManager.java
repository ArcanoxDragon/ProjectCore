package com.gmail.trentech.pjc.core;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

public class TeleportManager {

	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	public static Optional<Location<World>> getSafeLocation(Location<World> location) {
		TeleportHelper teleportHelper = Sponge.getGame().getTeleportHelper();
		
		first:
		for(int i = 0; i < 10; i++) {
			Optional<Location<World>> optionalLocation = teleportHelper.getSafeLocation(location);

			if (!optionalLocation.isPresent()) {
				continue;
			}
			Location<World> unsafeLocation = optionalLocation.get();

			BlockType blockType = unsafeLocation.getBlockType();

			if (!blockType.equals(BlockTypes.AIR) || !unsafeLocation.getRelative(Direction.UP).getBlockType().equals(BlockTypes.AIR)) {
				continue;
			}

			Location<World> floorLocation = unsafeLocation.getRelative(Direction.DOWN);
			
			for(int i2 = 0; i2 < 3; i2++) {
				BlockType floorBlockType = floorLocation.getBlockType();
				
				if (floorBlockType.equals(BlockTypes.WATER) || floorBlockType.equals(BlockTypes.LAVA) || floorBlockType.equals(BlockTypes.FLOWING_WATER) || floorBlockType.equals(BlockTypes.FLOWING_LAVA) || floorBlockType.equals(BlockTypes.FIRE)) {
					continue first;
				}
				floorLocation = floorLocation.getRelative(Direction.DOWN);
			}

			unsafeLocation.getExtent().loadChunk(unsafeLocation.getChunkPosition(), true);
			
			return optionalLocation;
		}
		
		return Optional.empty();
	}
	
	public static Optional<Location<World>> getRandomLocation(World world, int radius) {
		Location<World> spawnLocation = world.getSpawnLocation();

		radius = radius / 2;

		for(int i = 0; i < 20; i++) {
			double x = (random.nextDouble() * (radius * 2) - radius) + spawnLocation.getBlockX();
			double y = random.nextDouble(59, 200 + 1);
			double z = (random.nextDouble() * (radius * 2) - radius) + spawnLocation.getBlockZ();
			
			Optional<Location<World>> optionalLocation = getSafeLocation(world.getLocation(x, y, z));

			if (!optionalLocation.isPresent()) {
				continue;
			}
			return optionalLocation;
		}
		
		return Optional.empty();
	}

	public static Consumer<CommandSource> setUnsafeLocation(Location<World> location) {
		return (CommandSource src) -> {
			Player player = (Player) src;
			player.setLocation(location);
		};
	}
}
