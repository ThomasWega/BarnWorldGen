package com.bof.barn.world_generator.generation;

import com.bof.barn.world_generator.WorldGenerator;
import com.bof.barn.world_generator.event.GridLoadedEvent;
import com.bof.barn.world_generator.listener.PlayerJoinOnLockListener;
import org.bukkit.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.File;
import java.util.Arrays;

import static com.bof.barn.world_generator.WorldGenerator.LOGGER;

public class WorldGeneratorHandler {
    private final WorldGenerator plugin;
    private final PlayerJoinOnLockListener lockListener;

    public WorldGeneratorHandler(WorldGenerator plugin) {
        this.plugin = plugin;
        this.lockListener = new PlayerJoinOnLockListener();
    }

    /**
     * Create a grid from the schematic and load it. Also set presets for the world,
     *
     * @param schematicName Name of the schematic file
     * @param gridSize      Size the grid will have (e.g. 3 = 3x3 grid)
     * @param gridSpacing   Space between each schematic
     */
    public void initiate(String schematicName, int gridSize, int gridSpacing) {
        lockServer();

        LOGGER.warn("Starting pasting of schematic grid...");

        long time = System.currentTimeMillis();

        World world = Bukkit.getWorld("world");
        if (world == null) {
            throw new RuntimeException("Couldn't find world named \"world\". Please make sure the default world wasn't renamed or set to a different one in server.properties");
        }

        this.setWorldPresets(world);

        new SchematicGenerator(plugin, new File(plugin.getDataFolder(), schematicName))
                .loadGrid(new GridGenerator(gridSize, gridSpacing)
                        .generateGrid(world.getSpawnLocation())
                )
                .thenRun(() -> {
                    // needs to run sync!
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        LOGGER.warn("Pasting of grid took finished ({} ms)", System.currentTimeMillis() - time);
                        unloadChunks(world);
                        unLockServer();
                    });
                });
    }

    private void setWorldPresets(World world) {
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    }

    private void unloadChunks(World world) {
        long time2 = System.currentTimeMillis();
        LOGGER.warn("Unloading all chunks...");
        Arrays.stream(world.getLoadedChunks()).forEach(Chunk::unload);
        LOGGER.warn("All chunks have been unloaded ({} ms)", System.currentTimeMillis() - time2);

    }

    private void lockServer() {
        LOGGER.warn("Locking the server while generating the grid...");
        Bukkit.getPluginManager().registerEvents(lockListener, plugin);
    }

    private void unLockServer() {
        LOGGER.warn("Unlocking the server...");
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(lockListener);
        Bukkit.getPluginManager().callEvent(new GridLoadedEvent());
    }
}
