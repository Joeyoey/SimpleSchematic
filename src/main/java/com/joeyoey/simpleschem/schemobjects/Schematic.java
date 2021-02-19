package com.joeyoey.simpleschem.schemobjects;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class Schematic {


    private Map<Vector, String> blockDataMap; // block relative location and its data

    private transient Map<Vector, BlockData> trueMap = new HashMap<>();

    private int width; // X axis
    private int height; // y axis
    private int length; // z axis


    public Schematic(Map<Vector, String> blockDataMap, int width, int height, int length) {
        this.blockDataMap = blockDataMap;
        this.width = width;
        this.height = height;
        this.length = length;
    }


    public Map<Vector, String> getBlockDataMap() {
        return blockDataMap;
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
