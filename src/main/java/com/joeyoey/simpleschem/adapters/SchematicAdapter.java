package com.joeyoey.simpleschem.adapters;

import com.google.gson.*;
import com.joeyoey.simpleschem.schemobjects.Schematic;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

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
     * @param context the deserialization context to use for nested elements
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public Schematic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Map<Vector, String> blocks = new HashMap<>();
        JsonElement blocksEl = jsonObject.get("blocks");
        if (blocksEl != null && blocksEl.isJsonArray()) {
            // Preferred format: array of entries {x,y,z,bd}
            JsonArray array = blocksEl.getAsJsonArray();
            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();
                double x = obj.get("x").getAsDouble();
                double y = obj.get("y").getAsDouble();
                double z = obj.get("z").getAsDouble();
                String bd = obj.get("bd").getAsString();
                blocks.put(new Vector(x, y, z), bd);
            }
        } else if (blocksEl != null && blocksEl.isJsonObject()) {
            // Backward-compat: object map with stringified keys
            JsonObject obj = blocksEl.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                // Expect key like "x,y,z"
                String[] parts = entry.getKey().split(",");
                if (parts.length == 3) {
                    try {
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        double z = Double.parseDouble(parts[2]);
                        String bd = entry.getValue().getAsString();
                        blocks.put(new Vector(x, y, z), bd);
                    } catch (NumberFormatException e) {
                        throw new JsonParseException("Invalid vector key: " + entry.getKey(), e);
                    }
                } else {
                    throw new JsonParseException("Invalid vector key format: " + entry.getKey());
                }
            }
        }

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
     * @param context   the serialization context to use for nested elements
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(Schematic src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        // Serialize blocks as array of entries to avoid complex map key reliance
        JsonArray blocksArr = new JsonArray();
        for (Map.Entry<Vector, String> entry : src.getBlockDataMap().entrySet()) {
            Vector v = entry.getKey();
            JsonObject obj = new JsonObject();
            obj.addProperty("x", v.getX());
            obj.addProperty("y", v.getY());
            obj.addProperty("z", v.getZ());
            obj.addProperty("bd", entry.getValue());
            blocksArr.add(obj);
        }
        jsonObject.add("blocks", blocksArr);

        jsonObject.addProperty("width", src.getWidth());
        jsonObject.addProperty("height", src.getHeight());
        jsonObject.addProperty("length", src.getLength());

        return jsonObject;
    }
}
