package com.webex.events.com.webex.events.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.error.ErrorResponse;
import com.webex.events.error.Extensions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ErrorResponseTest {

    @Test
    void testRecordInvalid() throws JsonProcessingException {
        HashMap<String, Object> data = new HashMap<>() {
            {
                put("message", "Record is invalid.");
                put("extensions", new HashMap<String, Object>(){{
                    put("code", "RECORD_INVALID");
                    put("errors", new HashMap<String, String[]>(){{
                        put("first_name", new String[]{"invalid", "taken"});
                    }});
                }});
            }
        };

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errResponse;
        errResponse = mapper.readValue(json, ErrorResponse.class);

        assertEquals("Record is invalid.", errResponse.getMessage());
        assertEquals("RECORD_INVALID", errResponse.getCode());
        HashMap<String, List<String>> errorList = new HashMap<>(){
            {
                List<String> errors = Arrays.asList("invalid", "taken");
               put("first_name", errors);
            }
        };

        Extensions extensions = errResponse.extensions;
        assertEquals(errorList.get("first_name"), extensions.getErrors().get("first_name"));
    }
}
