package com.zclcs.common.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author zhouc
 */
public class MillisOrLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

    public MillisOrLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long value = parser.getValueAsLong();
            Instant instant = Instant.ofEpochMilli(value);

            return LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        }

        return super.deserialize(parser, context);
    }
}
