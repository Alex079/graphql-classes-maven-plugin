package integration.test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import integration.test.types.*;

public class Test {

    public static void main() throws IOException {
        test(new UnnamedQuery()
                .getVariables());
        test(new TrackingInfoQuery()
                .getVariables()
                .setItemId("ID"));
        test(new ItemsInDeliveryByPaymentQuery()
                .getVariables()
                .setPaidBy(PaymentType.CASH));
        test(new DeliveredMutation()
                .getVariables()
                .setItem(new SoldItem()
                        .setItem("Item")
                        .setPaidBy(PaymentType.OTHER)
                        .setTrack("Track")));
    }

    public static void test(Object data) throws IOException {
        System.out.println(data);

        Gson gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new GsonDateTimeAdapter()).create();
        String jsonGson = gson.toJson(data);
        System.out.println(jsonGson);
        Data dataGson = gson.fromJson(jsonGson, Data.class);
        System.out.println(dataGson);

        ObjectMapper jackson = new ObjectMapper().registerModule(new SimpleModule()
                .addSerializer(new JacksonDateTimeSerializer())
                .addDeserializer(OffsetDateTime.class, new JacksonDateTimeDeserializer()));
        String jsonJackson = jackson.writeValueAsString(data);
        System.out.println(jsonJackson);
        Data dataJackson = jackson.readValue(jsonJackson, Data.class);
        System.out.println(dataJackson);

        if (!Objects.equals(jsonGson, jsonJackson)) throw new AssertionError();
        if (!Objects.equals(dataGson.toString(), dataJackson.toString())) throw new AssertionError();
    }
}