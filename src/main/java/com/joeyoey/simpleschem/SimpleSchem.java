package com.joeyoey.simpleschem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.simpleschem.adapters.BlockDataAdapter;
import com.joeyoey.simpleschem.adapters.VectorAdapter;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.io.*;

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


}
