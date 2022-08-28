package it.test.support;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Utils {

	private Utils() { }

	private static final Gson G = new GsonBuilder()
		.registerTypeAdapter(OffsetDateTime.class, new GsonDateTimeAdapter())
		.create();

	private static final ObjectMapper J = new ObjectMapper().registerModule(new SimpleModule()
			.addSerializer(new JacksonDateTimeSerializer())
			.addDeserializer(OffsetDateTime.class, new JacksonDateTimeDeserializer()))
		.setSerializationInclusion(JsonInclude.Include.NON_NULL)
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	public static String gsonConvert(Object input) {
		return G.toJson(input);
	}

	public static <T> T gsonConvert(String json, Class<T> t) {
		return G.fromJson(json, t);
	}

	public static String jacksonConvert(Object input) {
		try {
			return J.writeValueAsString(input);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T> T jacksonConvert(String json, Class<T> t) {
		try {
			return J.readValue(json, t);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
