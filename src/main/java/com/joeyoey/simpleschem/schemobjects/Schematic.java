package com.joeyoey.simpleschem.schemobjects;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Schematic {


    private final Map<Vector, String> blockDataMap; // block relative location and its data
    private Map<Short, String> blockPalette; // short ID to block data string mapping
    private Map<Vector, Short> compactBlockData; // block relative location to short ID

    private final Map<Vector, String> tileEntityData; // block relative location to tile entity NBT data

    private final transient Map<Vector, BlockData> trueMap = new HashMap<>();

    private final int width; // X axis
    private final int height; // y axis
    private final int length; // z axis


    public Schematic(Map<Vector, String> blockDataMap, int width, int height, int length) {
        this.blockDataMap = blockDataMap;
        this.width = width;
        this.height = height;
        this.length = length;
        this.tileEntityData = new HashMap<>();
        buildCompactFormat();
    }

    public Schematic(Map<Vector, String> blockDataMap, Map<Vector, String> tileEntityData, int width, int height, int length) {
        this.blockDataMap = blockDataMap;
        this.tileEntityData = tileEntityData != null ? tileEntityData : new HashMap<>();
        this.width = width;
        this.height = height;
        this.length = length;
        buildCompactFormat();
    }

    private void buildCompactFormat() {
        // Build block palette
        this.blockPalette = new HashMap<>();
        this.compactBlockData = new HashMap<>();
        
        Set<String> uniqueBlocks = new HashSet<>(blockDataMap.values());
        short nextId = 0;
        
        for (String blockData : uniqueBlocks) {
            blockPalette.put(nextId, blockData);
            nextId++;
        }
        
        // Convert block data to compact format
        for (Map.Entry<Vector, String> entry : blockDataMap.entrySet()) {
            short blockId = getBlockId(entry.getValue());
            compactBlockData.put(entry.getKey(), blockId);
        }
    }
    
    private short getBlockId(String blockData) {
        for (Map.Entry<Short, String> entry : blockPalette.entrySet()) {
            if (entry.getValue().equals(blockData)) {
                return entry.getKey();
            }
        }
        return -1; // Should not happen
    }


    public Map<Vector, String> getBlockDataMap() {
        return blockDataMap;
    }

    public Map<Short, String> getBlockPalette() {
        return blockPalette;
    }

    public Map<Vector, Short> getCompactBlockData() {
        return compactBlockData;
    }

    public Map<Vector, String> getTileEntityData() {
        return tileEntityData;
    }

    public boolean hasTileEntities() {
        return tileEntityData != null && !tileEntityData.isEmpty();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }



    public Map<Vector, BlockData> getBlockData() {
        if (trueMap.isEmpty()) {
            for (Map.Entry<Vector, String> entry : this.blockDataMap.entrySet()) {
                this.trueMap.put(entry.getKey(), Bukkit.createBlockData(entry.getValue()));
            }
        }
        return trueMap;
    }


}
