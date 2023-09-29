package com.bof.barn.world_generator.generation;

import com.bof.barn.world_generator.WorldGenerator;
import com.bof.barn.world_generator.data.SchematicsStorage;
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
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class SchematicGenerator {
    @NotNull
    private final WorldGenerator plugin;
    @NotNull
    private final File schematicFile;

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
        // paste the schematics async
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(FaweAPI.getWorld(world.getName()))
                    .fastMode(true)
                    .combineStages(true)
                    .build()
            ) {
                gridLocations.forEach(location -> {
                    // Get the dimensions of the clipboard
                    BlockVector3 dimensions = schematic.getDimensions();

                    BlockVector3 loc = BlockVector3.at(location.getX(), location.getY(), location.getZ());

                    // Calculate the bounding box of the pasted schematic
                    BoundingBox boundingBox = new BoundingBox(
                            loc.getX() - 1,
                            loc.getY(),
                            loc.getZ(),
                            loc.getX() + dimensions.getX() + 1,
                            loc.getY() + dimensions.getY(),
                            loc.getZ() - dimensions.getZ()
                    );

                    // Do something with the bounding box, e.g., store it in a data structure
                    SchematicsStorage.getPastedRegions().add(boundingBox);

                    // Do something with the bounding box, e.g., store it in a data structure
                    SchematicsStorage.getPastedRegions().add(boundingBox);

                    Operation operation = new ClipboardHolder(schematic)
                            .createPaste(editSession)
                            .ignoreAirBlocks(true)
                            .copyBiomes(true)
                            .copyEntities(false)
                            .to(loc)
                            .build();
                    Operations.complete(operation);
                });
            }
            future.complete(null);
        });
        return future;
    }
}