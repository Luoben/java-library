/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.push.parse.notification.actions;

import com.urbanairship.api.push.model.notification.actions.TagActionData;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public final class TagActionDataSerializer extends JsonSerializer<TagActionData> {
    @Override
    public void serialize(TagActionData tagActionData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(tagActionData.isSingle()) {
            jsonGenerator.writeString(tagActionData.getSingleTag());
        }
        else {
            jsonGenerator.writeStartArray();
            for(String tag : tagActionData.getTagSet()) {
                jsonGenerator.writeString(tag);
            }
            jsonGenerator.writeEndArray();
        }
    }
}
