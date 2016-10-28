/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.push.parse.notification.android;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.urbanairship.api.common.parse.FieldParser;
import com.urbanairship.api.common.parse.FieldParserRegistry;
import com.urbanairship.api.common.parse.MapFieldParserRegistry;
import com.urbanairship.api.common.parse.StandardObjectDeserializer;
import com.urbanairship.api.push.model.notification.android.PublicNotification;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;


public class PublicNotificationDeserializer extends JsonDeserializer<PublicNotification> {
    private static final FieldParserRegistry<PublicNotification, PublicNotificationReader> FIELD_PARSERS = new MapFieldParserRegistry<PublicNotification, PublicNotificationReader>(
            ImmutableMap.<String, FieldParser<PublicNotificationReader>>builder()
                    .put("title", new FieldParser<PublicNotificationReader>() {
                        public void parse(PublicNotificationReader reader, JsonParser json, DeserializationContext context) throws IOException {
                            reader.readTitle(json);
                        }
                    })
                    .put("summary", new FieldParser<PublicNotificationReader>() {
                        public void parse(PublicNotificationReader reader, JsonParser json, DeserializationContext context) throws IOException {
                            reader.readSummary(json);
                        }
                    })
                    .put("alert", new FieldParser<PublicNotificationReader>() {
                        public void parse(PublicNotificationReader reader, JsonParser json, DeserializationContext context) throws IOException {
                            reader.readAlert(json);
                        }
                    })
                    .build()
    );

    private final StandardObjectDeserializer<PublicNotification, ?> deserializer;

    public PublicNotificationDeserializer() {
        deserializer = new StandardObjectDeserializer<PublicNotification, PublicNotificationReader>(
                FIELD_PARSERS,
                new Supplier<PublicNotificationReader>() {
                    @Override
                    public PublicNotificationReader get() {
                        return new PublicNotificationReader();
                    }
                }
        );
    }

    @Override
    public PublicNotification deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return deserializer.deserialize(jp, ctxt);
    }

}
