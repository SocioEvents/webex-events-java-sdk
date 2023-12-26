package com.webex.events.com.webex.events.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.error.ErrorResponse;
import com.webex.events.error.Extensions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorResponseTest {

    @Test
    void testRecordInvalid() throws JsonProcessingException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("message", "Record is invalid.");

        HashMap<String, Object> extensions = new HashMap<>();
        extensions.put("code", "RECORD_INVALID");

        HashMap<String, Object> errors = new HashMap<>();
        errors.put("first_name", new String[]{"invalid", "taken"});
        extensions.put("errors", errors);
        data.put("extensions", extensions);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errResponse;
        errResponse = mapper.readValue(json, ErrorResponse.class);

        assertEquals("Record is invalid.", errResponse.getMessage());
        assertEquals("RECORD_INVALID", errResponse.getCode());


        HashMap<String, List<String>> errorList = new HashMap<>();
        List<String> _errors = Arrays.asList("invalid", "taken");
        errorList.put("first_name", _errors);

        Extensions ext = errResponse.extensions;
        assertEquals(errorList.get("first_name"), ext.getErrors().get("first_name"));
    }

    @Test
    void testServerError() throws JsonProcessingException {

        HashMap<String, Object> data = new HashMap<>();
        data.put("message", "Server Error");
        HashMap<String, Object> extensions = new HashMap<>();
        extensions.put("code", "INTERNAL_SERVER_ERROR");
        extensions.put("referenceId", "somereferenceid");

        data.put("extensions", extensions);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errResponse;
        errResponse = mapper.readValue(json, ErrorResponse.class);

        assertEquals("Server Error", errResponse.getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", errResponse.getCode());
        assertEquals("somereferenceid", errResponse.extensions.getReferenceId());
    }

    @Test
    void testRateLimitingDetails() throws JsonProcessingException {

        HashMap<String, Object> data = new HashMap<>();
        data.put("message", "Max cost exceed.");

        HashMap<String, Object> extensions = new HashMap<>();
        extensions.put("code", "MAX_COST_EXCEEDED");
        extensions.put("cost", 45);
        extensions.put("availableCost", 5);
        extensions.put("threshold", 50);
        extensions.put("dailyThreshold", 200);
        extensions.put("dailyAvailableCost", 190);

        data.put("extensions", extensions);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errResponse;
        errResponse = mapper.readValue(json, ErrorResponse.class);

        assertEquals("Max cost exceed.", errResponse.getMessage());
        assertEquals("MAX_COST_EXCEEDED", errResponse.getCode());
        assertEquals(45, errResponse.extensions.getCost());
        assertEquals(5, errResponse.extensions.getAvailableCost());
        assertEquals(50, errResponse.extensions.getThreshold());
        assertEquals(200, errResponse.extensions.getDailyThreshold());
        assertEquals(190, errResponse.extensions.getDailyAvailableCost());
    }
}
