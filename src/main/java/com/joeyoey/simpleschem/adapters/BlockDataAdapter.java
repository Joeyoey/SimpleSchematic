package com.joeyoey.simpleschem.adapters;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Type;

public class BlockDataAdapter implements JsonDeserializer<BlockData>, JsonSerializer<BlockData> {

    @Override
    public BlockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        return Bukkit.getServer().createBlockData(jsonObject.get("bd").getAsString());
    }

    @Override
    public JsonElement serialize(BlockData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("bd", src.getAsString());
        return jsonObject;
    }
}
