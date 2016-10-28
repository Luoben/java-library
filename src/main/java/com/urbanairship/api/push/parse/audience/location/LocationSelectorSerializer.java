/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.push.parse.audience.location;

import com.urbanairship.api.push.model.audience.location.LocationAlias;
import com.urbanairship.api.push.model.audience.location.LocationIdentifier;
import com.urbanairship.api.push.model.audience.location.LocationSelector;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class LocationSelectorSerializer extends JsonSerializer<LocationSelector> {
    @Override
    public void serialize(LocationSelector s, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        LocationIdentifier id = s.getLocationIdentifier();
        if (id.getAlias().isPresent()) {
            LocationAlias alias = id.getAlias().get();
            jgen.writeStringField(alias.getType(), alias.getValue());
        } else if (id.getId().isPresent()) {
            jgen.writeStringField("id", id.getId().get());
        }

        jgen.writeObjectField("date", s.getDateRange());

        jgen.writeEndObject();
    }
}
