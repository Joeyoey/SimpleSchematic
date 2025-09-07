package com.joeyoey.simpleschem.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.junit.Test;
import org.junit.Assume;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class AdaptersTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Vector.class, new VectorAdapter())
            .registerTypeAdapter(BlockData.class, new BlockDataAdapter())
            .registerTypeAdapter(Schematic.class, new SchematicAdapter())
            .create();

    @Test
    public void testVectorAdapter() {
        // Arrange
        Vector original = new Vector(1.5, 2.25, -3.75);

        // Act
        String json = gson.toJson(original);
        Vector deserialized = gson.fromJson(json, Vector.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getX(), deserialized.getX(), 0.001);
        assertEquals(original.getY(), deserialized.getY(), 0.001);
        assertEquals(original.getZ(), deserialized.getZ(), 0.001);
    }

    @Test
    public void testBlockDataAdapter() {
        // Skip if no Bukkit server initialized
        Assume.assumeTrue(Bukkit.getServer() != null);
        // Arrange
        BlockData original = org.bukkit.Bukkit.createBlockData(Material.STONE);

        // Act
        String json = gson.toJson(original);
        BlockData deserialized = gson.fromJson(json, BlockData.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original.getAsString(), deserialized.getAsString());
        assertEquals(original.getMaterial(), deserialized.getMaterial());
    }

    @Test
    public void testSchematicAdapter() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:dirt");

        Schematic original = new Schematic(blockData, 2, 1, 1);

        // Act
        String json = gson.toJson(original);
        Schematic deserialized = gson.fromJson(json, Schematic.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original.getWidth(), deserialized.getWidth());
        assertEquals(original.getHeight(), deserialized.getHeight());
        assertEquals(original.getLength(), deserialized.getLength());
        assertEquals(original.getBlockDataMap().size(), deserialized.getBlockDataMap().size());

        // Verify block data integrity
        for (Map.Entry<Vector, String> entry : original.getBlockDataMap().entrySet()) {
            assertTrue(deserialized.getBlockDataMap().containsKey(entry.getKey()));
            assertEquals(entry.getValue(), deserialized.getBlockDataMap().get(entry.getKey()));
        }
    }

    @Test
    public void testVectorAdapterWithZeroValues() {
        // Arrange
        Vector original = new Vector(0, 0, 0);

        // Act
        String json = gson.toJson(original);
        Vector deserialized = gson.fromJson(json, Vector.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original, deserialized);
    }

    @Test
    public void testVectorAdapterWithNegativeValues() {
        // Arrange
        Vector original = new Vector(-5, -10, -15);

        // Act
        String json = gson.toJson(original);
        Vector deserialized = gson.fromJson(json, Vector.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original, deserialized);
    }

    @Test
    public void testBlockDataAdapterWithComplexBlockData() {
        // Skip if no Bukkit server initialized
        Assume.assumeTrue(Bukkit.getServer() != null);
        // Arrange - Create a block with more complex data
        BlockData original = org.bukkit.Bukkit.createBlockData(Material.OAK_STAIRS);

        // Act
        String json = gson.toJson(original);
        BlockData deserialized = gson.fromJson(json, BlockData.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original.getAsString(), deserialized.getAsString());
    }

    @Test
    public void testSchematicAdapterWithEmptyBlocks() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        Schematic original = new Schematic(blockData, 0, 0, 0);

        // Act
        String json = gson.toJson(original);
        Schematic deserialized = gson.fromJson(json, Schematic.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(0, deserialized.getWidth());
        assertEquals(0, deserialized.getHeight());
        assertEquals(0, deserialized.getLength());
        assertTrue(deserialized.getBlockDataMap().isEmpty());
    }

    @Test
    public void testAdaptersRoundTripConsistency() {
        // Skip if no Bukkit server initialized
        Assume.assumeTrue(Bukkit.getServer() != null);
        // Arrange
        Vector originalVector = new Vector(1, 2, 3);
        BlockData originalBlockData = org.bukkit.Bukkit.createBlockData(Material.DIAMOND_BLOCK);

        // Act - Multiple round trips
        String vectorJson = gson.toJson(originalVector);
        Vector vector1 = gson.fromJson(vectorJson, Vector.class);
        String vectorJson2 = gson.toJson(vector1);
        Vector vector2 = gson.fromJson(vectorJson2, Vector.class);

        String blockJson = gson.toJson(originalBlockData);
        BlockData block1 = gson.fromJson(blockJson, BlockData.class);
        String blockJson2 = gson.toJson(block1);
        BlockData block2 = gson.fromJson(blockJson2, BlockData.class);

        // Assert - Should be consistent across multiple serializations
        assertEquals(originalVector, vector1);
        assertEquals(vector1, vector2);
        assertEquals(originalBlockData.getAsString(), block1.getAsString());
        assertEquals(block1.getAsString(), block2.getAsString());
    }
}
