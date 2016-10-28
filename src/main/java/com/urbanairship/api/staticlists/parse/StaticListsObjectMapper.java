/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.staticlists.parse;

import com.urbanairship.api.staticlists.model.StaticListListingResponse;
import com.urbanairship.api.staticlists.model.StaticListView;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

public class StaticListsObjectMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleModule MODULE = new SimpleModule("Static Lists API Module", new Version(1, 0, 0, null));

    static {
        MODULE.addDeserializer(StaticListView.class, new StaticListViewDeserializer());
        MODULE.addDeserializer(StaticListListingResponse.class, new StaticListListingResponseDeserializer());

        MAPPER.registerModule(MODULE);
    }

    public static SimpleModule getModule() {
        return MODULE;
    }

    public static ObjectMapper getInstance() {
        return MAPPER;
    }

    private StaticListsObjectMapper() {
    }

}
