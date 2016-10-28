/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.push.parse.notification.android;

import com.urbanairship.api.common.parse.APIParsingException;
import com.urbanairship.api.common.parse.JsonObjectReader;
import com.urbanairship.api.common.parse.StringFieldDeserializer;
import com.urbanairship.api.push.model.notification.android.BigPictureStyle;
import org.codehaus.jackson.JsonParser;

import java.io.IOException;

public class BigPictureStyleReader implements JsonObjectReader<BigPictureStyle> {

    private final BigPictureStyle.Builder builder;

    public BigPictureStyleReader() {
        this.builder = BigPictureStyle.newBuilder();
    }

    public void readSummary(JsonParser parser) throws IOException {
        builder.setSummary(StringFieldDeserializer.INSTANCE.deserialize(parser, "summary"));
    }

    public void readTitle(JsonParser parser) throws IOException {
        builder.setTitle(StringFieldDeserializer.INSTANCE.deserialize(parser, "title"));
    }

    public void readContent(JsonParser parser) throws IOException {
        builder.setContent(StringFieldDeserializer.INSTANCE.deserialize(parser, "big_picture"));
    }

    @Override
    public BigPictureStyle validateAndBuild() throws IOException {
        try {
            return builder.build();
        } catch (Exception e) {
            throw new APIParsingException(e.getMessage(), e);
        }
    }
}
