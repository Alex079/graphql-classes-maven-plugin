package it.test.support;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public class JacksonDateTimeDeserializer extends StdScalarDeserializer<OffsetDateTime> {

    public JacksonDateTimeDeserializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return OffsetDateTime.parse(p.getText());
    }
}
