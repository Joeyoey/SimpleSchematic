package com.joeyoey.simpleschem;

import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SimpleSchemTest {

    @Mock
    private World mockWorld;

    @Mock
    private Block mockBlock;

    @Mock
    private Location mockLocation;

    @Test
    public void testSchematicFromLocations() {
        // Arrange
        Location center = new Location(mockWorld, 0, 0, 0);
        Set<Location> locations = new HashSet<>();

        // Create a 2x2x2 cube of blocks around center
        for (int x = -1; x <= 0; x++) {
            for (int y = -1; y <= 0; y++) {
                for (int z = -1; z <= 0; z++) {
                    Location loc = new Location(mockWorld, x, y, z);
                    locations.add(loc);
                }
            }
        }

        // Mock block data - ensure complete stubbing
        BlockData mockBlockData = mock(BlockData.class);
        when(mockBlockData.getAsString()).thenReturn("minecraft:stone");
        when(mockBlock.getBlockData()).thenReturn(mockBlockData);

        for (Location loc : locations) {
            when(mockWorld.getBlockAt(loc)).thenReturn(mockBlock);
        }

        // Act
        Schematic schematic = SimpleSchem.schematicFromLocations(center, locations);

        // Assert
        assertNotNull(schematic);
        assertEquals(2, schematic.getWidth());
        assertEquals(2, schematic.getHeight());
        assertEquals(2, schematic.getLength());
        assertEquals(8, schematic.getBlockDataMap().size()); // 2x2x2 = 8 blocks
    }

    @Test
    public void testCompactSchematicSaveAndLoad() throws IOException {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:dirt");
        blockData.put(new Vector(0, 1, 0), "minecraft:stone");

        Schematic original = new Schematic(blockData, 2, 2, 1);

        // Create temp file
        Path tempFile = Files.createTempFile("test_schematic", ".schem");
        File file = tempFile.toFile();

        try {
            // Act - Save
            boolean saveResult = SimpleSchem.saveCompactSchematic(file, original);
            assertTrue(saveResult);

            // Act - Load
            Schematic loaded = SimpleSchem.loadCompactSchematic(file);
            assertNotNull(loaded);

            // Assert
            assertEquals(original.getWidth(), loaded.getWidth());
            assertEquals(original.getHeight(), loaded.getHeight());
            assertEquals(original.getLength(), loaded.getLength());
            assertEquals(original.getBlockDataMap().size(), loaded.getBlockDataMap().size());

            // Verify block data integrity
            for (Map.Entry<Vector, String> entry : original.getBlockDataMap().entrySet()) {
                assertTrue(loaded.getBlockDataMap().containsKey(entry.getKey()));
                assertEquals(entry.getValue(), loaded.getBlockDataMap().get(entry.getKey()));
            }

        } finally {
            // Cleanup
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testSchematicToJsonAndFromJson() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        blockData.put(new Vector(1, 0, 0), "minecraft:dirt");

        Schematic original = new Schematic(blockData, 2, 1, 1);

        // Act
        String json = SimpleSchem.schematicToJson(original);
        Schematic loaded = SimpleSchem.schematicFromJson(json);

        // Assert
        assertNotNull(json);
        assertNotNull(loaded);
        assertEquals(original.getWidth(), loaded.getWidth());
        assertEquals(original.getHeight(), loaded.getHeight());
        assertEquals(original.getLength(), loaded.getLength());
        assertEquals(original.getBlockDataMap().size(), loaded.getBlockDataMap().size());
    }

    @Test
    public void testAtomizeSchematicToFile() throws IOException {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        Schematic schematic = new Schematic(blockData, 1, 1, 1);

        Path tempFile = Files.createTempFile("test_schematic_json", ".json");
        File file = tempFile.toFile();
        file.deleteOnExit(); // Ensure cleanup

        try {
            // Act
            boolean result = SimpleSchem.atomizeSchematicToFile(file, schematic);

            // Assert
            assertTrue(result);
            assertTrue(file.exists());
            assertTrue(file.length() > 0);

            // Verify we can read it back
            Schematic loaded = SimpleSchem.schematicFromAtoms(file);
            assertNotNull(loaded);
            assertEquals(schematic.getWidth(), loaded.getWidth());

        } finally {
            // Ensure file is deleted even if test fails
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    public void testSchematicCreationWithEmptyBlocks() {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        Schematic schematic = new Schematic(blockData, 0, 0, 0);

        // Assert
        assertNotNull(schematic);
        assertEquals(0, schematic.getWidth());
        assertEquals(0, schematic.getHeight());
        assertEquals(0, schematic.getLength());
        assertTrue(schematic.getBlockDataMap().isEmpty());
    }

    @Test
    public void testCompactFileFormatMagicNumber() throws IOException {
        // Arrange
        Map<Vector, String> blockData = new HashMap<>();
        blockData.put(new Vector(0, 0, 0), "minecraft:stone");
        Schematic schematic = new Schematic(blockData, 1, 1, 1);

        Path tempFile = Files.createTempFile("test_magic", ".schem");
        File file = tempFile.toFile();

        try {
            // Act
            SimpleSchem.saveCompactSchematic(file, schematic);

            // Read first 4 bytes to verify magic number
            byte[] bytes = Files.readAllBytes(tempFile);
            int magicNumber = ((bytes[0] & 0xFF) << 24) |
                             ((bytes[1] & 0xFF) << 16) |
                             ((bytes[2] & 0xFF) << 8) |
                             (bytes[3] & 0xFF);

            // Assert - Magic number should be 0x12345678
            assertEquals(0x12345678, magicNumber);

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
