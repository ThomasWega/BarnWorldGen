package com.bof.barn.world_generator.generation;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class GridGenerator {
    private final int gridSize;
    private final int gridSpacing;

    public GridGenerator(int gridSize, int gridSpacing) {
        this.gridSize = gridSize;
        this.gridSpacing = gridSpacing;
    }

    public Set<Location> generateGrid(Location startLocation) {
        Set<Location> pasteLocations = new HashSet<>();
        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                int offsetX = x * gridSpacing;
                int offsetZ = z * gridSpacing;

                pasteLocations.add(new Location(
                        startLocation.getWorld(),
                        startLocation.getX() + offsetX,
                        startLocation.getY(),
                        startLocation.getZ() + offsetZ
                ));
            }
        }
        return pasteLocations;
    }
}

