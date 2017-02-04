package de.enlightened.peris;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public final class GsonHelper {
  private GsonHelper() {
  }

  public static final Gson CUSTOM_GSON = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
      new ByteArrayToBase64TypeAdapter()).create();

  // Using Android's base64 libraries. This can be replaced with any base64 library.
  private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    public byte[] deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
      return json.getAsString().getBytes();
    }

    public JsonElement serialize(final byte[] src, final Type typeOfSrc, final JsonSerializationContext context) {
      return new JsonPrimitive(new String((byte[]) src));
    }
  }
}
