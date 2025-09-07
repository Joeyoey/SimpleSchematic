package com.joeyoey.simpleschem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.simpleschem.adapters.BlockDataAdapter;
import com.joeyoey.simpleschem.adapters.SchematicAdapter;
import com.joeyoey.simpleschem.adapters.VectorAdapter;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import com.joeyoey.simpleschem.nms.NMSAbstraction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleSchem {



    public static Gson gson;
    private static final Logger LOGGER = Logger.getLogger(SimpleSchem.class.getName());


    private static void initializeGson() {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Vector.class, new VectorAdapter())
                .registerTypeAdapter(BlockData.class, new BlockDataAdapter())
                .registerTypeAdapter(Schematic.class, new SchematicAdapter())
                .enableComplexMapKeySerialization()
                .create();
    }


    /**
     * This method converts the given schematic to a string
     * @param schematic the schematic to convert
     * @return the string object
     */
    public static String schematicToJson(Schematic schematic) {
        if (gson == null) {
            initializeGson();
        }

        return gson.toJson(schematic, new TypeToken<Schematic>(){}.getType());
    }


    /**
     * This method converts given string to a schematic
     * @param json the string to convert
     * @return the schematic object
     */
    public static Schematic schematicFromJson(String json) {
        if (gson == null) {
            initializeGson();
        }

        return gson.fromJson(json, new TypeToken<Schematic>(){}.getType());
    }

    /**
     * Jam a schematic into a file
     * @param file the file to jam the schematic into
     * @param schematic the schematic
     * @return whether or not the schematic was uploaded
     */
    public static boolean atomizeSchematicToFile(File file, Schematic schematic) {
        try (OutputStream out = java.nio.file.Files.newOutputStream(file.toPath())) {
            String toAtomize = schematicToJson(schematic);
            byte[] bytes = toAtomize.getBytes(StandardCharsets.US_ASCII);
            out.write(bytes);
            out.flush();
            return true;
        } catch (IOException err) {
            LOGGER.log(Level.SEVERE, "Failed to write schematic JSON to file", err);
            return false;
        }
    }


    /**
     * Pull a schematic out of a file
     * @param file the file to read
     * @return the new schematic from thin air
     */
    public static Schematic schematicFromAtoms(File file) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String str = new String(bytes, StandardCharsets.US_ASCII);
            return schematicFromJson(str);
        } catch (IOException err) {
            LOGGER.log(Level.SEVERE, "Failed to read schematic JSON from file", err);
            return null;
        }
    }

    /**
     * Save a schematic to compact .schem format
     * @param file the file to save to
     * @param schematic the schematic to save
     * @return whether the save was successful
     */
    public static boolean saveCompactSchematic(File file, Schematic schematic) {
        try (DataOutputStream dos = new DataOutputStream(java.nio.file.Files.newOutputStream(file.toPath()))) {
            // Write magic number and version
            dos.writeInt(0x12345678); // Magic number
            dos.writeShort(1); // Version
            
            // Write dimensions
            dos.writeInt(schematic.getWidth());
            dos.writeInt(schematic.getHeight());
            dos.writeInt(schematic.getLength());
            
            // Write block palette
            Map<Short, String> palette = schematic.getBlockPalette();
            dos.writeShort((short) palette.size());
            for (Map.Entry<Short, String> entry : palette.entrySet()) {
                dos.writeShort(entry.getKey());
                byte[] blockDataBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
                dos.writeShort((short) blockDataBytes.length);
                dos.write(blockDataBytes);
            }
            
            // Write compact block data
            Map<Vector, Short> compactData = schematic.getCompactBlockData();
            dos.writeInt(compactData.size());
            for (Map.Entry<Vector, Short> entry : compactData.entrySet()) {
                Vector vec = entry.getKey();
                dos.writeInt(vec.getBlockX());
                dos.writeInt(vec.getBlockY());
                dos.writeInt(vec.getBlockZ());
                dos.writeShort(entry.getValue());
            }
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save compact schematic", e);
            return false;
        }
    }
    /**
     * Load a schematic from compact .schem format
     * @param file the file to load from
     * @return the loaded schematic, or null if loading failed
     */
    public static Schematic loadCompactSchematic(File file) {
        try (DataInputStream dis = new DataInputStream(java.nio.file.Files.newInputStream(file.toPath()))) {
            // Read and verify magic number
            int magic = dis.readInt();
            if (magic != 0x12345678) {
                throw new IOException("Invalid file format - wrong magic number");
            }
            
            // Read version
            short version = dis.readShort();
            if (version != 1) {
                throw new IOException("Unsupported version: " + version);
            }
            
            // Read dimensions
            int width = dis.readInt();
            int height = dis.readInt();
            int length = dis.readInt();
            
            // Read block palette
            Map<Short, String> palette = new HashMap<>();
            short paletteSize = dis.readShort();
            for (int i = 0; i < paletteSize; i++) {
                short id = dis.readShort();
                short blockDataLength = dis.readShort();
                byte[] blockDataBytes = new byte[blockDataLength];
                dis.readFully(blockDataBytes);
                String blockData = new String(blockDataBytes, StandardCharsets.UTF_8);
                palette.put(id, blockData);
            }
            
            // Read compact block data
            Map<Vector, String> blockDataMap = new HashMap<>();
            int blockCount = dis.readInt();
            for (int i = 0; i < blockCount; i++) {
                int x = dis.readInt();
                int y = dis.readInt();
                int z = dis.readInt();
                short blockId = dis.readShort();
                
                Vector vector = new Vector(x, y, z);
                String blockData = palette.get(blockId);
                if (blockData != null) {
                    blockDataMap.put(vector, blockData);
                }
            }
            
            return new Schematic(blockDataMap, width, height, length);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load compact schematic", e);
            return null;
        }
    }

    public static Schematic schematicFromLocations(Location center, Set<Location> locations) {
        Map<Vector, String> blockDataMap = new HashMap<>();

        // Calculate bounding box
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Location location : locations) {
            Block block = location.getBlock();
            Vector relativeVector = location.clone().toVector().subtract(center.toVector());

            // Update bounding box
            minX = Math.min(minX, relativeVector.getBlockX());
            maxX = Math.max(maxX, relativeVector.getBlockX());
            minY = Math.min(minY, relativeVector.getBlockY());
            maxY = Math.max(maxY, relativeVector.getBlockY());
            minZ = Math.min(minZ, relativeVector.getBlockZ());
            maxZ = Math.max(maxZ, relativeVector.getBlockZ());

            // Store block data as string
            blockDataMap.put(relativeVector, block.getBlockData().getAsString());
        }

        // Calculate dimensions
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int length = maxZ - minZ + 1;

        return new Schematic(blockDataMap, width, height, length);
    }

    /**
     * @deprecated Use {@link #isAreaSuitable(Location, Schematic)} and paste safety checks instead.
     */
    @Deprecated
    private static boolean isSafe(Location center, Schematic schematic) {
        int width = schematic.getWidth();
        int height = schematic.getHeight();
        int length = schematic.getLength();
        Location clone = center.clone();
        clone.subtract(width/2.0,height/2.0,length/2.0);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y< height; y++) {
                for (int z = 0; z < length; z++) {
                    if (!clone.getBlock().getType().equals(Material.AIR)) {
                        return false;
                    }
                    clone.add(0,0,1);
                }
                clone.setZ(length/2.0);
                clone.add(0,1,0);
            }
            clone.setY(height/2.0);
            clone.add(1,0,0);
        }
        return true;
    }

    /**
     * Enhanced paste operation with optimizations for large structures
     * Processes blocks in batches to avoid timeout and memory issues
     * @param center the center of the paste
     * @param schematic the schematic to paste
     * @param force whether to force paste even if area is not clear
     * @param batchSize number of blocks to process per batch (default: 1000)
     * @return whether the schematic was successfully pasted
     */
    /**
     * @deprecated Prefer {@link #pasteSchematic(Location, Schematic, boolean)} or an async task scheduler.
     */
    @Deprecated
    public static boolean pasteLargeSchematic(Location center, Schematic schematic, boolean force, int batchSize) {
        try {
            // Pre-load all required chunks
            preloadChunksForLargeStructure(center, schematic);

            if (!force && !isAreaSuitable(center, schematic)) {
                return false;
            }

            // Process blocks in batches to avoid timeout
            Map<Vector, BlockData> blockData = schematic.getBlockData();
            int processedBlocks = 0;

            for (Map.Entry<Vector, BlockData> entry : blockData.entrySet()) {
                Location blockLocation = center.clone().add(entry.getKey());
                blockLocation.getBlock().setBlockData(entry.getValue(), false); // No physics for speed

                processedBlocks++;

                // Yield control every batchSize blocks to prevent timeout
                if (processedBlocks % batchSize == 0) {
                    try {
                        Thread.sleep(1); // Small yield to prevent server timeout
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to paste large schematic", e);
            return false;
        }
    }

    /**
     * Check if the paste area is suitable (not checking for air, just ensuring chunks are loaded)
     */
    private static boolean isAreaSuitable(Location center, Schematic schematic) {
        if (schematic == null || center == null || center.getWorld() == null) {
            return false;
        }
        // For now, just ensure the center chunk is loaded
        return center.getChunk().isLoaded();
    }
    private static void preloadChunksForLargeStructure(Location center, Schematic schematic) {
        int radius = Math.max(schematic.getWidth(), schematic.getHeight()) / 16 + 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (center.getWorld() == null) return;
                center.getWorld().getChunkAt(
                    center.getBlockX() >> 4,
                    center.getBlockZ() >> 4
                ).load(true); // Force load
            }
        }
    }

    /**
     * Memory-efficient schematic creation for very large structures
     * Processes blocks in chunks to avoid memory overflow
     * @param center the center location
     * @param locations set of all block locations
     * @param chunkSize number of blocks to process at once
     * @return the created schematic
     */
    /**
     * @deprecated Prefer streaming approaches tailored to your plugin scheduler.
     */
    @Deprecated
    public static Schematic createLargeSchematic(Location center, Set<Location> locations, int chunkSize) {
        Map<Vector, String> blockDataMap = new HashMap<>();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        // First pass: calculate bounds
        for (Location location : locations) {
            minX = Math.min(minX, location.getBlockX());
            maxX = Math.max(maxX, location.getBlockX());
            minY = Math.min(minY, location.getBlockY());
            maxY = Math.max(maxY, location.getBlockY());
            minZ = Math.min(minZ, location.getBlockZ());
            maxZ = Math.max(maxZ, location.getBlockZ());
        }

        // Process blocks in chunks to avoid memory issues
        Location[] locationArray = locations.toArray(new Location[0]);
        for (int i = 0; i < locationArray.length; i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, locationArray.length);

            for (int j = i; j < endIndex; j++) {
                Location location = locationArray[j];
                Vector relativeVector = location.clone().toVector().subtract(center.toVector());
                blockDataMap.put(relativeVector, location.getBlock().getBlockData().getAsString());
            }

            // Yield control periodically
            if (i % (chunkSize * 10) == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int length = maxZ - minZ + 1;

        return new Schematic(blockDataMap, width, height, length);
    }

    /**
     * Enhanced paste operation with better error handling and performance
     * @param center the center of the paste
     * @param schematic the schematic to paste
     * @param force whether to force paste even if area is not clear
     * @return whether the schematic was successfully pasted
     */
    public static boolean pasteSchematic(Location center, Schematic schematic, boolean force) {
        try {
            // Ensure chunks are loaded around the paste area
            loadChunksAround(center, schematic);

            if (!force && !isAreaSuitable(center, schematic)) {
                return false;
            }

            // Perform the paste operation
            for (Map.Entry<Vector, BlockData> entry : schematic.getBlockData().entrySet()) {
                Location blockLocation = center.clone().add(entry.getKey());
                blockLocation.getBlock().setBlockData(entry.getValue(), true);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to paste schematic with tile entities", e);
            return false;
        }
    }
    private static void loadChunksAround(Location center, Schematic schematic) {
        int radius = Math.max(schematic.getWidth(), Math.max(schematic.getHeight(), schematic.getLength())) / 16 + 1;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (center.getWorld() == null) return;
                center.getWorld().getChunkAt(center.getBlockX() >> 4, center.getBlockZ() >> 4).load();
            }
        }
    }

    /**
     * This method pastes a schematic at a desired location very fast/pretty fast
     * @param center the center of the paste
     * @param schematic the schematic to paste
     * @param nmsAbstraction the method to paste them using nms
     */
    public static void fastPaste(Location center, Schematic schematic, NMSAbstraction nmsAbstraction) {
        if (!center.getChunk().isLoaded()) {
            center.getChunk().load();
        }

        for (Map.Entry<Vector, BlockData> entry : schematic.getBlockData().entrySet()) {
            nmsAbstraction.setBlockSuperFast(center.clone().add(entry.getKey()).getBlock(), entry.getValue(), false);
        }
    }


    /**
     * Create a schematic with tile entity support from block locations
     * @param center the center location
     * @param locations set of block locations
     * @param nmsAbstraction NMS abstraction for tile entity access
     * @return schematic with tile entity data
     */
    public static Schematic schematicFromLocationsWithTileEntities(Location center, Set<Location> locations, NMSAbstraction nmsAbstraction) {
        Map<Vector, String> blockDataMap = new HashMap<>();
        Map<Vector, String> tileEntityData = new HashMap<>();

        // Calculate bounding box
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Location location : locations) {
            Block block = location.getBlock();
            Vector relativeVector = location.clone().toVector().subtract(center.toVector());

            // Update bounding box
            minX = Math.min(minX, relativeVector.getBlockX());
            maxX = Math.max(maxX, relativeVector.getBlockX());
            minY = Math.min(minY, relativeVector.getBlockY());
            maxY = Math.max(maxY, relativeVector.getBlockY());
            minZ = Math.min(minZ, relativeVector.getBlockZ());
            maxZ = Math.max(maxZ, relativeVector.getBlockZ());

            // Store block data
            blockDataMap.put(relativeVector, block.getBlockData().getAsString());

            // Capture tile entity data if available
            if (nmsAbstraction != null) {
                String tileData = nmsAbstraction.getTileEntityData(block);
                if (tileData != null) {
                    tileEntityData.put(relativeVector, tileData);
                }
            }
        }

        // Calculate dimensions
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int length = maxZ - minZ + 1;

        return new Schematic(blockDataMap, tileEntityData, width, height, length);
    }

    /**
     * Paste schematic with tile entity restoration
     * @param center the center of the paste
     * @param schematic the schematic to paste
     * @param nmsAbstraction NMS abstraction for tile entity restoration
     * @param force whether to force paste
     * @return whether the schematic was successfully pasted
     */
    public static boolean pasteSchematicWithTileEntities(Location center, Schematic schematic, NMSAbstraction nmsAbstraction, boolean force) {
        // First paste the blocks
        boolean blockPasteSuccess = pasteSchematic(center, schematic, force);
        if (!blockPasteSuccess) {
            return false;
        }

        // Then restore tile entities
        if (schematic.hasTileEntities() && nmsAbstraction != null) {
            for (Map.Entry<Vector, String> entry : schematic.getTileEntityData().entrySet()) {
                Vector relativeVector = entry.getKey();
                String tileData = entry.getValue();

                Location blockLocation = center.clone().add(relativeVector);
                nmsAbstraction.setTileEntityData(blockLocation.getBlock(), tileData);
            }
        }

        return true;
    }

    /**
     * Check if a block has a tile entity that should be preserved
     * @param block the block to check
     * @return true if the block has a preservable tile entity
     */
    /**
     * @deprecated Implementation-specific; prefer checking block states directly
     */
    @Deprecated
    public static boolean hasPreservableTileEntity(Block block) {
        Material material = block.getType();
        switch (material) {
            case CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
            case DROPPER:
            case DISPENSER:
            case HOPPER:
            case BREWING_STAND:
                return true;
            default:
                // Check for sign types dynamically since they may vary by version
                String name = material.name();
                return name.contains("SIGN") || name.contains("WALL_SIGN");
        }
    }


}
