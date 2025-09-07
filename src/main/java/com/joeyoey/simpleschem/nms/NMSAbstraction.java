package com.joeyoey.simpleschem.nms;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.Chunk;

@SuppressWarnings("unused")
public interface NMSAbstraction {


    /**
     * Update the low-level chunk information for the given block to the new block ID and data.  This
     * change will not be propagated to clients until the chunk is refreshed to them.
     * @param block the target Bukkit block to modify
     * @param blockData the Bukkit BlockData to apply
     * @param applyPhysics whether to apply physics updates after setting the block
     */
    void setBlockSuperFast(Block block, BlockData blockData, boolean applyPhysics);

    /**
     * Get the raw NMS block data for a block (useful for tile entities)
     * @param block the block to get data for
     * @return raw NMS block data string
     */
    String getBlockDataString(Block block);

    /**
     * Set block data using raw NMS string (useful for complex block states)
     * @param block the block to set
     * @param nmsBlockData the raw NMS block data string
     * @param applyPhysics whether to apply physics
     */
    void setBlockFromNMSString(Block block, String nmsBlockData, boolean applyPhysics);

    /**
     * Refresh chunk for clients (send block updates)
     * @param chunk the chunk to refresh for clients
     */
    void refreshChunk(Chunk chunk);

    /**
     * Bulk set blocks in a chunk for maximum performance
     * @param chunk the chunk to modify
     * @param blockDataMap map of relative positions to block data within the chunk
     */
    void setBlocksInChunk(Chunk chunk, java.util.Map<org.bukkit.util.Vector, BlockData> blockDataMap);

    /**
     * Get tile entity data for a block (if it has one)
     * @param block the block to get tile entity data for
     * @return NBT data string, or null if no tile entity
     */
    String getTileEntityData(Block block);

    /**
     * Set tile entity data for a block
     * @param block the block to set tile entity data for
     * @param nbtData the NBT data string
     */
    void setTileEntityData(Block block, String nbtData);

}
