package com.gmail.trentech.pjc.core;

import java.util.List;
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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;

public class TeleportManager {

	private static final List<BlockType> unsafeBlocks = ImmutableList.of(BlockTypes.AIR, BlockTypes.CACTUS, BlockTypes.FIRE, BlockTypes.LAVA, BlockTypes.FLOWING_LAVA, BlockTypes.FLOWING_WATER, BlockTypes.WATER);

	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	public static Optional<Location<World>> getSafeLocation(Location<World> location) {
		TeleportHelper teleportHelper = Sponge.getGame().getTeleportHelper();

		Optional<Location<World>> optionalLocation = Optional.of(location);

		first: for (int i = 0; i < 10; i++) {
			if (!optionalLocation.isPresent()) {
				optionalLocation = teleportHelper.getSafeLocation(location);
				continue;
			}
			Location<World> unsafeLocation = optionalLocation.get();

			if (!isInWorldBorder(location)) {
				optionalLocation = teleportHelper.getSafeLocation(location);
				continue;
			}

			BlockType blockType = unsafeLocation.getBlockType();

			if (!blockType.equals(BlockTypes.AIR) || !unsafeLocation.getRelative(Direction.UP).getBlockType().equals(BlockTypes.AIR)) {
				optionalLocation = teleportHelper.getSafeLocation(location);
				continue;
			}

			Location<World> floorLocation = unsafeLocation.getRelative(Direction.DOWN);

			for (int i2 = 0; i2 < 4; i2++) {
				if (floorLocation.getBlockType().equals(BlockTypes.AIR)) {
					floorLocation = floorLocation.getRelative(Direction.DOWN);
					continue;
				}

				if (unsafeBlocks.contains(floorLocation.getBlockType())) {
					optionalLocation = teleportHelper.getSafeLocation(location);
					continue first;
				}

				break;
			}

			if (floorLocation.getBlockType().equals(BlockTypes.AIR)) {
				optionalLocation = teleportHelper.getSafeLocation(location);
				continue;
			}

			unsafeLocation.getExtent().loadChunk(unsafeLocation.getChunkPosition(), true);

			return optionalLocation;
		}

		return Optional.empty();
	}

	public static Optional<Location<World>> getRandomLocation(World world, int radius) {
		Location<World> spawnLocation = world.getSpawnLocation();

		radius = radius / 2;

		for (int i = 0; i < 20; i++) {
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

	public static boolean isInWorldBorder(Location<World> location) {
		World world = location.getExtent();

		long radius = (long) (world.getWorldBorder().getDiameter() / 2.0);
		Vector3d displacement = location.getPosition().sub(world.getWorldBorder().getCenter()).abs();

		return !(displacement.getX() > radius || displacement.getZ() > radius);
	}

	public static Consumer<CommandSource> setUnsafeLocation(Location<World> location) {
		return (CommandSource src) -> {
			Player player = (Player) src;
			player.setLocation(location);
		};
	}
}
