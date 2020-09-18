package it.test.support;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

public class JacksonDateTimeSerializer extends StdScalarSerializer<OffsetDateTime> {

    public JacksonDateTimeSerializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
