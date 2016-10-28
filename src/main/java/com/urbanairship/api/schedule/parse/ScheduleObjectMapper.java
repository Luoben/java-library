package com.urbanairship.api.schedule.parse;

import com.urbanairship.api.push.parse.PushObjectMapper;
import com.urbanairship.api.schedule.model.ListAllSchedulesResponse;
import com.urbanairship.api.schedule.model.Schedule;
import com.urbanairship.api.schedule.model.ScheduleDetails;
import com.urbanairship.api.schedule.model.SchedulePayload;
import com.urbanairship.api.schedule.model.ScheduleResponse;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

public class ScheduleObjectMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleModule MODULE = new SimpleModule("Schedule API Module", new Version(1, 0, 0, null));

    static {
        MODULE
            .addDeserializer(SchedulePayload.class, SchedulePayloadDeserializer.INSTANCE)
            .addSerializer(SchedulePayload.class, ScheduledPayloadSerializer.INSTANCE)
            .addDeserializer(Schedule.class, ScheduleDeserializer.INSTANCE)
            .addSerializer(Schedule.class, ScheduleSerializer.INSTANCE)
            .addSerializer(ScheduleDetails.class, ScheduleDetailsSerializer.INSTANCE)
            .addDeserializer(ScheduleResponse.class, new ScheduleResponseDeserializer())
            .addDeserializer(ListAllSchedulesResponse.class, new ListSchedulesResponseDeserializer());

        MAPPER.registerModule(MODULE);
        MAPPER.registerModule(PushObjectMapper.getModule());
        MAPPER.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public static SimpleModule getModule() {
        return MODULE;
    }

    public static ObjectMapper getInstance() {
        return MAPPER;
    }

    private ScheduleObjectMapper() {}
}
