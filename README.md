# SimpleSchematic

This is a simple way to save structures and paste structures in your Spigot Minecraft world.

## Overview

The reason this is being made is due to the over complexity of using schematics without WorldEdit or one of its forks, as well as the added complexity that version 1.13 and above brought to the table by getting rid of the material ID system and instead moving towards namespace IDs. The goal of this project is to simplify the entire process and make it fast and reasonable.

## Features

✅ **Compact File Format**: Block profiling reduces file sizes dramatically through deduplication  
✅ **Fast Operations**: Binary serialization and optimized paste operations  
✅ **Version Compatibility**: NMS abstraction handles different Minecraft versions  
✅ **Extensibility**: Interface allows advanced users to customize behavior  
✅ **Error Resilience**: Comprehensive error handling and validation  

## Project Goals

1. ✅ Allow the saving of structures to a new .schem file
2. ✅ Allow the reading of structures from a new .schem file
3. ✅ Have the ability to paste structures
4. ✅ Ensure that the NMSAbstraction interface covers enough so that it can allow more advanced users to create their own pasting method
5. ✅ Ensure that the .schem file is as small and compact as possible while maintaining fast read/write
6. ✅ Making sure that pasting even without NMS is fast and fluid

## Installation

### Prerequisites
- Java 8 or higher
- Maven 3.6+
- Spigot server

### Building
```bash
git clone <repository-url>
cd SimpleSchematic
mvn clean compile
```

## Usage

### Basic Usage

```java
import com.joeyoey.simpleschem.SimpleSchem;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

public final class BasicUsageExample {
    public void run(World world) {
        // Create a schematic from block locations
        Set<Location> blockLocations = new HashSet<>();
        // TODO: populate blockLocations
        blockLocations.add(new Location(world, 0, 64, 0));
        blockLocations.add(new Location(world, 1, 64, 0));

        Location center = new Location(world, 0, 64, 0);
        Schematic schematic = SimpleSchem.schematicFromLocations(center, blockLocations);

        // Save to compact .schem file
        File schematicFile = new File("mystructure.schem");
        SimpleSchem.saveCompactSchematic(schematicFile, schematic);

        // Load from compact .schem file
        Schematic loadedSchematic = SimpleSchem.loadCompactSchematic(schematicFile);

        // Paste at a location
        Location pasteLocation = new Location(world, 10, 64, 10);
        boolean success = SimpleSchem.pasteSchematic(pasteLocation, loadedSchematic, false);
    }
}
```

### Advanced Usage with NMS

```java
import com.joeyoey.simpleschem.SimpleSchem;
import com.joeyoey.simpleschem.nms.NMSAbstraction;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.Location;

public final class AdvancedUsageExample {
    public void run(Location pasteLocation, Schematic loadedSchematic, NMSAbstraction nms) {
        // Use custom NMS implementation for faster pasting
        SimpleSchem.fastPaste(pasteLocation, loadedSchematic, nms);
    }
}
```

## API Reference

### Core Methods

#### `SimpleSchem.schematicFromLocations(Location center, Set<Location> locations)`
Creates a Schematic object from a set of block locations.
- **Parameters**: center location, set of block locations
- **Returns**: Schematic object with automatic dimension calculation

#### `SimpleSchem.saveCompactSchematic(File file, Schematic schematic)`
Saves a schematic to the compact .schem format.
- **Parameters**: target file, schematic object
- **Returns**: boolean indicating success

#### `SimpleSchem.loadCompactSchematic(File file)`
Loads a schematic from the compact .schem format.
- **Parameters**: source file
- **Returns**: Schematic object or null if loading fails

#### `SimpleSchem.pasteSchematic(Location center, Schematic schematic, boolean force)`
Enhanced paste operation with error handling and chunk loading.
- **Parameters**: center location, schematic, force paste flag
- **Returns**: boolean indicating success

#### `SimpleSchem.fastPaste(Location center, Schematic schematic, NMSAbstraction nms)`
High-performance paste using NMS abstraction.
- **Parameters**: center location, schematic, NMS implementation

### Schematic Class

The `Schematic` class stores structure data efficiently:

```java
public class Schematic {
    private Map<Vector, String> blockDataMap;        // Original block data
    private Map<Short, String> blockPalette;         // Short ID to block string mapping
    private Map<Vector, Short> compactBlockData;     // Vector to short ID mapping
    private int width, height, length;               // Structure dimensions
}
```

### NMS Abstraction Interface

The `NMSAbstraction` interface provides advanced Minecraft internals access:

```java
public interface NMSAbstraction {
    void setBlockSuperFast(Block block, BlockData blockData, boolean applyPhysics);
    String getBlockDataString(Block block);
    void setBlockFromNMSString(Block block, String nmsBlockData, boolean applyPhysics);
    void refreshChunk(Chunk chunk);
    void setBlocksInChunk(Chunk chunk, Map<Vector, BlockData> blockDataMap);
    String getTileEntityData(Block block);
    void setTileEntityData(Block block, String nbtData);
}
```

## Compact File Format

The .schem format is designed for maximum compactness:

### Structure
```
Magic Number (4 bytes): 0x12345678
Version (2 bytes): 1
Dimensions (12 bytes): width, height, length (4 bytes each, int32)
Block Palette:
  - Palette Size (2 bytes, short)
  - For each palette entry:
    - Block ID (2 bytes, short)
    - Block Data Length (2 bytes, short)
    - Block Data (UTF-8 bytes)
Block Data:
  - Block Count (4 bytes, int32)
  - For each block:
    - X, Y, Z coordinates (12 bytes, 3 x int32)
    - Block ID (2 bytes, short)
```

### Benefits
- **Block Deduplication**: Unique blocks stored once in palette
- **Short IDs**: 2-byte IDs instead of full block strings
- **Binary Format**: Fast read/write operations
- **Version Control**: Magic number and version for compatibility

## Performance Characteristics

- **File Size**: ~70-90% smaller than JSON format
- **Save Time**: Fast binary serialization
- **Load Time**: Optimized palette-based reconstruction
- **Paste Speed**: Chunk-aware loading and bulk operations
- **Memory Usage**: Efficient block data caching

## Contributing

### Implementing NMS Abstractions

To support additional Minecraft versions, implement the `NMSAbstraction` interface:

```java
public class NMSAbstraction_v1_16_R3 implements NMSAbstraction {
    @Override
    public void setBlockSuperFast(Block block, BlockData blockData, boolean applyPhysics) {
        // Version-specific implementation
    }
    // ... other methods
}
```

### Testing

```bash
mvn test
```

## License

This project is open source. See LICENSE file for details.

## Support

For issues and questions, please create an issue on the project repository.
