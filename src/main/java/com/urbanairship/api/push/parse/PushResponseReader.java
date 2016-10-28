/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.push.parse;

import com.urbanairship.api.common.parse.APIParsingException;
import com.urbanairship.api.common.parse.JsonObjectReader;
import com.urbanairship.api.push.model.PushResponse;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.List;

/*
Readers are the part of the deserialization process that actually builds and
return an object.
 */
public final class PushResponseReader implements JsonObjectReader<PushResponse> {

    private final PushResponse.Builder builder;

    public PushResponseReader() {
        this.builder = PushResponse.newBuilder();
    }

    public void readOperationId(JsonParser jsonParser) throws IOException {
        builder.setOperationId(jsonParser.readValueAs(String.class));
    }

    public void readPushIds(JsonParser jsonParser) throws IOException {
        List<String> list =
                jsonParser.readValueAs(new TypeReference<List<String>>() {
                });
        builder.addAllPushIds(list);
    }

    public void readOk(JsonParser jsonParser) throws IOException {
        builder.setOk(jsonParser.getBooleanValue());
    }

    public void readMessageIds(JsonParser jsonParser) throws IOException {
        List<String> list =
            jsonParser.readValueAs(new TypeReference<List<String>>() {
            });
        builder.addAllMessageIds(list);
    }

    public void readContentUrls(JsonParser jsonParser) throws IOException {
        List<String> list =
            jsonParser.readValueAs(new TypeReference<List<String>>() {
            });
        builder.addAllContentUrls(list);
    }

    @Override
    public PushResponse validateAndBuild() throws IOException {
        try {
            return builder.build();
        } catch (Exception e) {
            throw new APIParsingException(e.getMessage());
        }
    }
}
