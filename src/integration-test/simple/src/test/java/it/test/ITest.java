package it.test;

import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.test.support.GsonDateTimeAdapter;
import it.test.support.JacksonDateTimeDeserializer;
import it.test.support.JacksonDateTimeSerializer;

class ITest {

    private static final Gson G = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new GsonDateTimeAdapter())
            .create();
    private static final ObjectMapper J = new ObjectMapper().registerModule(new SimpleModule()
            .addSerializer(new JacksonDateTimeSerializer())
            .addDeserializer(OffsetDateTime.class, new JacksonDateTimeDeserializer()))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private Map<?, ?> testGson(Object input) {
        System.out.println(input);
        String json = G.toJson(input);
        Map<?, ?> output = G.fromJson(json, Map.class);
        System.out.printf("To JSON:%n%s%nTo Map:%n%s%n%n", json, output);
        return output;
    }

    private Map<?, ?> testJackson(Object input) throws JsonProcessingException {
        System.out.println(input);
        String json = J.writeValueAsString(input);
        Map<?, ?> output = J.readValue(json, Map.class);
        System.out.printf("To JSON:%n%s%nTo Map:%n%s%n%n", json, output);
        return output;
    }

    @Test
    void testUnnamedQuery() throws JsonProcessingException {
        Map<?, ?> g = testGson(new it.gson.UnnamedQuery().getVariables());
        Map<?, ?> j = testJackson(new it.jackson.UnnamedQuery().getVariables());
        Assertions.assertIterableEquals(g.entrySet(), j.entrySet());
    }

    @Test
    void testTrackingInfoQuery() throws JsonProcessingException {
        Map<?, ?> g = testGson(new it.gson.TrackingInfoQuery().getVariables()
                .setItemId("ID"));
        Map<?, ?> j = testJackson(new it.jackson.TrackingInfoQuery().getVariables()
                .setItemId("ID"));
        Assertions.assertEquals(g.entrySet(), j.entrySet());
    }

    @Test
    void testItemsInDeliveryByPaymentQuery() throws JsonProcessingException {
        Map<?, ?> g = testGson(new it.gson.ItemsInDeliveryByPaymentQuery().getVariables()
                .setPaidBy(it.gson.types.PaymentType.CASH));
        Map<?, ?> j = testJackson(new it.jackson.ItemsInDeliveryByPaymentQuery().getVariables()
                .setPaidBy(it.jackson.types.PaymentType.CASH));
        Assertions.assertEquals(g.entrySet(), j.entrySet());
    }

    @Test
    void testDeliveredMutation() throws JsonProcessingException {
        Map<?, ?> g = testGson(new it.gson.DeliveredMutation().getVariables()
                .setItem(new it.gson.types.SoldItem()
                        .setItem("Item")
                        .setPaidBy(it.gson.types.PaymentType.CASH)
                        .setTrack("Track")));
        Map<?, ?> j = testJackson(new it.jackson.DeliveredMutation().getVariables()
                .setItem(new it.jackson.types.SoldItem()
                        .setItem("Item")
                        .setPaidBy(it.jackson.types.PaymentType.CASH)
                        .setTrack("Track")));
        Assertions.assertEquals(g.entrySet(), j.entrySet());
    }

}