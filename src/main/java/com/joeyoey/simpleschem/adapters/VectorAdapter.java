package com.joeyoey.simpleschem.adapters;

import com.google.gson.*;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;

public class VectorAdapter implements JsonDeserializer<Vector>, JsonSerializer<Vector> {

    @Override
    public Vector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        return new Vector(jsonObject.get("x").getAsDouble(), jsonObject.get("y").getAsDouble(), jsonObject.get("z").getAsDouble());
    }

    @Override
    public JsonElement serialize(Vector src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", src.getX());
        jsonObject.addProperty("y", src.getY());
        jsonObject.addProperty("z", src.getZ());

        return jsonObject;
    }
}
