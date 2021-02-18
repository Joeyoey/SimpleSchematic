package com.joeyoey.simpleschem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.simpleschem.adapters.BlockDataAdapter;
import com.joeyoey.simpleschem.adapters.VectorAdapter;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.Map;

public final class SimpleSchem {



    private static Gson gson;


    private static void initializeGson() {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Vector.class, new VectorAdapter())
                .registerTypeAdapter(BlockData.class, new BlockDataAdapter())
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

        return gson.toJson(schematic);
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
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            String toAtomize = schematicToJson(schematic);

            objectOutputStream.writeChars(toAtomize);

            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (IOException err) {
            err.printStackTrace();
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
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            return schematicFromJson(objectInputStream.readUTF());

        } catch (IOException err) {
            err.printStackTrace();
            return null;
        }
    }


    private static boolean isSafe(Location center, Schematic schematic) {
        int width = schematic.getWidth();
        int height = schematic.getHeight();
        int length = schematic.getLength();
        Location clone = center.clone();
        clone.subtract(width/2,height/2,length/2);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y< height; y++) {
                for (int z = 0; z < length; z++) {
                    if (!clone.getBlock().getType().equals(Material.AIR)) {
                        return false;
                    }
                    clone.add(0,0,1);
                }
                clone.setZ(length/2);
                clone.add(0,1,0);
            }
            clone.setY(height/2);
            clone.add(1,0,0);
        }
        return true;
    }


    /**
     * This method pastes a schematic at a desired location after checking that the area is big enough and open enough for the schematic
     * @param center the center of the paste
     * @param schematic the schematic to paste
     * @return whether the schematic was successfully pasted or not
     */
    public static boolean slowPaste(Location center, Schematic schematic) {
        if (!isSafe(center, schematic)) {
            return false;
        } else {
            Location clone;
            for (Map.Entry<Vector, BlockData> entry : schematic.getBlockDataMap().entrySet()) {
                clone = center.clone();
                clone.add(entry.getKey()).getBlock().setBlockData(entry.getValue(),true);
            }
            return true;
        }
    }



}
