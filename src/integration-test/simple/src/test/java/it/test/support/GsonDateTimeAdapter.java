package it.test.support;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonDateTimeAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return OffsetDateTime.parse(json.getAsString());
    }

    @Override
    public JsonElement serialize(OffsetDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
