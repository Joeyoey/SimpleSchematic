package com.joeyoey.simpleschem.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.joeyoey.simpleschem.SimpleSchem;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;
import java.util.Map;

public class SchematicAdapter implements JsonDeserializer<Schematic>, JsonSerializer<Schematic> {

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public Schematic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();


        Map<Vector, String> blocks = SimpleSchem.gson.fromJson(jsonObject.get("blocks"), new TypeToken<Map<Vector, String>>(){}.getType());

        int width = jsonObject.get("width").getAsInt();
        int height = jsonObject.get("height").getAsInt();
        int length = jsonObject.get("length").getAsInt();



        return new Schematic(blocks, width, height, length);
    }

    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
     * non-trivial field of the {@code src} object. However, you should never invoke it on the
     * {@code src} object itself since that will cause an infinite loop (Gson will call your
     * call-back method again).</p>
     *
     * @param src       the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @param context
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(Schematic src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("blocks", SimpleSchem.gson.toJsonTree(src.getBlockDataMap(), new TypeToken<Map<Vector, String>>(){}.getType()));

        jsonObject.addProperty("width", src.getWidth());
        jsonObject.addProperty("height", src.getHeight());
        jsonObject.addProperty("length", src.getLength());

        return jsonObject;
    }
}
