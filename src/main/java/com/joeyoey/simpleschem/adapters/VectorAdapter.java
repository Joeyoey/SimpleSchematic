package com.joeyoey.simpleschem.adapters;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;

public class VectorAdapter implements JsonDeserializer<Vector>, JsonSerializer<Vector> {

    @Override
    public Vector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        return new Vector(jsonObject.get("x").getAsInt(), jsonObject.get("y").getAsInt(), jsonObject.get("z").getAsInt());
    }

    @Override
    public JsonElement serialize(Vector src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        Bukkit.getLogger().severe("vector_data 1");
        jsonObject.addProperty("x", src.getBlockX());
        jsonObject.addProperty("y", src.getBlockY());
        jsonObject.addProperty("z", src.getBlockZ());
        Bukkit.getLogger().severe("vector_data 2");

        return jsonObject;
    }
}
