package it.test;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        System.out.printf("Transforming %s using Gson%n", input);
        String json = G.toJson(input);
        Map<?, ?> output = G.fromJson(json, Map.class);
        System.out.printf("To JSON:%n%s%nTo Map:%n%s%n%n", json, output);
        return output;
    }

    private Map<?, ?> testJackson(Object input) throws JsonProcessingException {
        System.out.printf("Transforming %s using Jackson%n", input);
        String json = J.writeValueAsString(input);
        Map<?, ?> output = J.readValue(json, Map.class);
        System.out.printf("To JSON:%n%s%nTo Map:%n%s%n%n", json, output);
        return output;
    }

    @Test
    void testEmptyType() {
        it.gson.types.EmptyType emptyType1WithGson = new it.gson.types.EmptyType();
        it.gson.types.EmptyType emptyType2WithGson = new it.gson.types.EmptyType();
        System.out.printf("Comparing %s to %s%n", emptyType1WithGson, emptyType2WithGson);
        Assertions.assertEquals(emptyType1WithGson, emptyType2WithGson);
        System.out.println();

        it.jackson.types.EmptyType emptyType1WithJackson = new it.jackson.types.EmptyType();
        it.jackson.types.EmptyType emptyType2WithJackson = new it.jackson.types.EmptyType();
        System.out.printf("Comparing %s to %s%n", emptyType1WithJackson, emptyType2WithJackson);
        Assertions.assertEquals(emptyType1WithJackson, emptyType2WithJackson);
        System.out.println();
    }

    @Test
    void testUnnamedQuery() throws JsonProcessingException {
        Map<?, ?> g = testGson(new it.gson.UnnamedQuery().getVariables());
        Map<?, ?> j = testJackson(new it.jackson.UnnamedQuery().getVariables());
        Assertions.assertEquals(g.entrySet(), j.entrySet());
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
                .setPaidWith(it.gson.types.PaymentType.CASH));
        Map<?, ?> j = testJackson(new it.jackson.ItemsInDeliveryByPaymentQuery().getVariables()
                .setPaidWith(it.jackson.types.PaymentType.CASH));
        Assertions.assertEquals(g.entrySet(), j.entrySet());
    }

    @Test
    void testDeliveredMutation() throws JsonProcessingException {
        Map<?, ?> g = testGson(new it.gson.DeliveredMutation().getVariables()
                .setItem(new it.gson.types.SoldItem()
                        .setItem("Item")
                        .setPaidWith(it.gson.types.PaymentType.CASH)
                        .setTrack("Track")));
        Map<?, ?> j = testJackson(new it.jackson.DeliveredMutation().getVariables()
                .setItem(new it.jackson.types.SoldItem()
                        .setItem("Item")
                        .setPaidWith(it.jackson.types.PaymentType.CASH)
                        .setTrack("Track")));
        Assertions.assertEquals(g.entrySet(), j.entrySet());
    }

}