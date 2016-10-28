/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.push.parse.notification;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.urbanairship.api.common.parse.FieldParser;
import com.urbanairship.api.common.parse.FieldParserRegistry;
import com.urbanairship.api.common.parse.MapFieldParserRegistry;
import com.urbanairship.api.common.parse.StandardObjectDeserializer;
import com.urbanairship.api.push.model.notification.Interactive;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class InteractiveDeserializer extends JsonDeserializer<Interactive> {

    private static final FieldParserRegistry<Interactive, InteractiveReader> FIELD_PARSERS =
        new MapFieldParserRegistry<Interactive, InteractiveReader>(
            ImmutableMap.<String, FieldParser<InteractiveReader>>builder()
                .put("type", new FieldParser<InteractiveReader>() {
                    @Override
                    public void parse(InteractiveReader reader, JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                        reader.readType(jsonParser);
                    }
                })
                .put("button_actions", new FieldParser<InteractiveReader>() {
                    @Override
                    public void parse(InteractiveReader reader, JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                        reader.readButtonActions(jsonParser);
                    }
                })
                .build());

    private final StandardObjectDeserializer<Interactive, ?> deserializer;

    public InteractiveDeserializer() {
        deserializer = new StandardObjectDeserializer<Interactive, InteractiveReader>(
            FIELD_PARSERS,
            new Supplier<InteractiveReader>() {
                @Override
                public InteractiveReader get() {
                    return new InteractiveReader();
                }
            }
        );
    }

    @Override
    public Interactive deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserializer.deserialize(parser, deserializationContext);
    }
}

