package com.bof.barn.world_generator.generation;

import com.bof.barn.world_generator.WorldGenerator;
import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SchematicGenerator {

    private final WorldGenerator plugin;
    private final File schematicFile;

    public SchematicGenerator(@NotNull WorldGenerator plugin, @NotNull File schematicFile) {
        this.plugin = plugin;
        this.schematicFile = schematicFile;
    }

    public CompletableFuture<Void> loadGrid(@NotNull Set<Location> gridLocations) {
        Clipboard clipboard = getSchematicClipboard();
        return pasteGridAsync(clipboard, gridLocations);
    }

    private Clipboard getSchematicClipboard() {
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException("Failed to paste the grid of schematics. " +
                    "Make sure the the schematic file is in the plugins folder and that the path in config is correct", e);
        }
    }

    private CompletableFuture<Void> pasteGridAsync(Clipboard schematic, Set<Location> gridLocations) {
        // world should be the same on every location, so it doesn't matter
        World world = gridLocations.toArray(Location[]::new)[0].getWorld();

        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(FaweAPI.getWorld(world.getName()))
                    .fastMode(true)
                    .combineStages(true)
                    .build()
            ) {
                gridLocations.forEach(location -> {
                    Operation operation = new ClipboardHolder(schematic)
                            .createPaste(editSession)
                            .ignoreAirBlocks(true)
                            .copyBiomes(true)
                            .copyEntities(false)
                            .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                            .build();
                    Operations.complete(operation);
                });
            }
            future.complete(null);
        });
        return future;
    }
}