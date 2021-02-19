package com.joeyoey.simpleschem.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.simpleschem.SimpleSchem;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Type;

public class BlockDataAdapter implements JsonDeserializer<BlockData>, JsonSerializer<BlockData> {

    @Override
    public BlockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        BlockData blockData = Bukkit.getServer().createBlockData(jsonObject.get("bd").getAsString());

        return blockData.clone();
    }

    @Override
    public JsonElement serialize(BlockData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        String str = src.getAsString();
        jsonObject.addProperty("bd", str);



        return jsonObject;
    }
}
