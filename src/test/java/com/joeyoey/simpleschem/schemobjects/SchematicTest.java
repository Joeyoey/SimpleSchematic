package com.joeyoey.simpleschem.schemobjects;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.junit.Test;
import org.junit.Assume;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class SchematicTest {

    @Test
    public void testSchematicCreation() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:dirt");
        blockData.put(new Vector(0, 1, 0), "minecraft:stone");

        // Act
        Schematic schematic = new Schematic(blockData, 2, 2, 1);

        // Assert
        assertNotNull(schematic);
        assertEquals(2, schematic.getWidth());
        assertEquals(2, schematic.getHeight());
        assertEquals(1, schematic.getLength());
        assertEquals(3, schematic.getBlockDataMap().size());
        assertNotNull(schematic.getBlockPalette());
        assertNotNull(schematic.getCompactBlockData());
    }

    @Test
    public void testBlockPaletteCreation() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:stone"); // Duplicate block
        blockData.put(new Vector(0, 1, 0), "minecraft:dirt");

        // Act
        Schematic schematic = new Schematic(blockData, 2, 2, 1);

        // Assert
        Map<Short, String> palette = schematic.getBlockPalette();
        assertNotNull(palette);
        assertEquals(2, palette.size()); // Should have 2 unique blocks
        assertTrue(palette.containsValue("minecraft:stone"));
        assertTrue(palette.containsValue("minecraft:dirt"));
    }

    @Test
    public void testCompactBlockData() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:dirt");

        // Act
        Schematic schematic = new Schematic(blockData, 2, 1, 1);

        // Assert
        Map<Vector, Short> compactData = schematic.getCompactBlockData();
        assertNotNull(compactData);
        assertEquals(2, compactData.size());

        // Verify that all vectors have corresponding short IDs
        for (Vector vector : blockData.keySet()) {
            assertTrue(compactData.containsKey(vector));
            Short blockId = compactData.get(vector);
            assertNotNull(blockId);
            assertTrue(blockId >= 0);
        }
    }

    @Test
    public void testGetBlockDataLazyLoading() {
        // Skip if no Bukkit server initialized
        Assume.assumeTrue(Bukkit.getServer() != null);
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:dirt");

        Schematic schematic = new Schematic(blockData, 2, 1, 1);

        // Act - First call should create BlockData objects
        Map<Vector, BlockData> blockDataMap = schematic.getBlockData();

        // Assert
        assertNotNull(blockDataMap);
        assertEquals(2, blockDataMap.size());

        for (Map.Entry<Vector, String> entry : blockData.entrySet()) {
            assertTrue(blockDataMap.containsKey(entry.getKey()));
            BlockData blockDataObj = blockDataMap.get(entry.getKey());
            assertNotNull(blockDataObj);
            assertEquals(entry.getValue(), blockDataObj.getAsString());
        }

        // Act - Second call should return cached data
        Map<Vector, BlockData> cachedBlockData = schematic.getBlockData();

        // Assert - Should be the same instance
        assertSame(blockDataMap, cachedBlockData);
    }

    @Test
    public void testEmptySchematic() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();

        // Act
        Schematic schematic = new Schematic(blockData, 0, 0, 0);

        // Assert
        assertNotNull(schematic);
        assertEquals(0, schematic.getWidth());
        assertEquals(0, schematic.getHeight());
        assertEquals(0, schematic.getLength());
        assertTrue(schematic.getBlockDataMap().isEmpty());
        assertTrue(schematic.getBlockPalette().isEmpty());
        assertTrue(schematic.getCompactBlockData().isEmpty());
    }

    @Test
    public void testLargeSchematicDimensions() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");

        int largeWidth = 1000;
        int largeHeight = 500;
        int largeLength = 2000;

        // Act
        Schematic schematic = new Schematic(blockData, largeWidth, largeHeight, largeLength);

        // Assert
        assertEquals(largeWidth, schematic.getWidth());
        assertEquals(largeHeight, schematic.getHeight());
        assertEquals(largeLength, schematic.getLength());
    }

    @Test
    public void testBlockPaletteUniqueness() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:stone");
        blockData.put(new Vector(2, 0, 0), "minecraft:stone");
        blockData.put(new Vector(3, 0, 0), "minecraft:dirt");
        blockData.put(new Vector(4, 0, 0), "minecraft:dirt");

        // Act
        Schematic schematic = new Schematic(blockData, 5, 1, 1);

        // Assert
        Map<Short, String> palette = schematic.getBlockPalette();
        assertEquals(2, palette.size()); // Only 2 unique blocks

        // Verify all IDs are unique
        assertEquals(palette.size(), new HashSet<>(palette.keySet()).size());
    }

    @Test
    public void testGetBlockDataMapReturnsOriginal() {
        // Arrange
        Map<Vector, String> originalBlockData = new HashMap<>();
        originalBlockData.put(new Vector(0, 0, 0), "minecraft:stone");
        originalBlockData.put(new Vector(1, 0, 0), "minecraft:dirt");

        Schematic schematic = new Schematic(originalBlockData, 2, 1, 1);

        // Act
        Map<Vector, String> retrievedBlockData = schematic.getBlockDataMap();

        // Assert
        assertSame(originalBlockData, retrievedBlockData);
        assertEquals(originalBlockData.size(), retrievedBlockData.size());
        assertEquals(originalBlockData.get(new Vector(0, 0, 0)), retrievedBlockData.get(new Vector(0, 0, 0)));
    }
}
