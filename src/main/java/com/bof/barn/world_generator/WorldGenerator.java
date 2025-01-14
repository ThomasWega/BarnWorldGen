package com.bof.barn.world_generator;

import com.bof.barn.world_generator.generation.EmptyChunkGenerator;
import com.bof.barn.world_generator.generation.WorldGeneratorHandler;
import com.bof.toolkit.file.FileLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public final class WorldGenerator extends JavaPlugin {
    public static ComponentLogger LOGGER;
    @SuppressWarnings("DataFlowIssue")
    @NotNull
    public static World WORLD = null;

    @Override
    public void onEnable() {
        LOGGER = getComponentLogger();
        loadFiles();
        // schedule this so it runs only after world is loaded
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            WORLD = Objects.requireNonNull(Bukkit.getWorld("world"), "The world needs to be named \"world\"");
            new WorldGeneratorHandler(this)
                    .initiate(getConfig().getString("schematic.path"), getConfig().getInt("grid.size"), getConfig().getInt("grid.spacing"));
        });
    }

    @Override
    public void onDisable() {
    }

    private void loadFiles() {
        try {
            FileLoader.loadAllFiles(getClass().getClassLoader(), getDataFolder(),
                    "config.yml"
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load one or more config files", e);
        }
    }


    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        return new EmptyChunkGenerator();
    }
}
