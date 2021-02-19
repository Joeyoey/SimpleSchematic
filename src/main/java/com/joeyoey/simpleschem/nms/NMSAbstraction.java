package com.joeyoey.simpleschem.nms;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public interface NMSAbstraction {


    /**
     * Update the low-level chunk information for the given block to the new block ID and data.  This
     * change will not be propagated to clients until the chunk is refreshed to them.
     * @param block
     * @param blockData
     * @param applyPhysics
     */
    void setBlockSuperFast(Block block, BlockData blockData, boolean applyPhysics);

}
