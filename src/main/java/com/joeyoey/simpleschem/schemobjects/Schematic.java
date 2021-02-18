package com.joeyoey.simpleschem.schemobjects;

import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class Schematic {


    private Map<Vector, BlockData> blockDataMap = new HashMap<>(); // block relative location and its data

    private int width; // X axis
    private int height; // y axis
    private int length; // z axis


    public Schematic(Map<Vector, BlockData> blockDataMap, int width, int height, int length) {
        this.blockDataMap = blockDataMap;
        this.width = width;
        this.height = height;
        this.length = length;
    }


    public Map<Vector, BlockData> getBlockDataMap() {
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
}
