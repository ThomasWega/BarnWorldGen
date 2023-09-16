package com.bof.barn.world_generator.data;

import lombok.Getter;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SchematicsStorage {
    @Getter
    private static final List<BoundingBox> pastedRegions = new ArrayList<>();
}
