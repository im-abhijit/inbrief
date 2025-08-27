package io.github.abhijit.inbrief.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ObjectMapperUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperUtil.class);

    public static String getStringFromObject(Object object) {
        try{
            return objectMapper.writeValueAsString(object);
        }
        catch (Exception e){
            LOGGER.error("Failed to serialize object to JSON. Error: {}", e.toString());
        }
        return null;
    }

    public static <T> T getObjectFromString(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize JSON to {}. Error: {}. Payload: {}", clazz.getSimpleName(), e.toString(), json);
            return null;
        }
    }

    // Optional: generic method for TypeReference (for Map<String,Object> or List<T>)
    public static <T> T getObjectFromString(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize JSON to TypeReference. Error: {}. Payload: {}", e.toString(), json);
            return null;
        }
    }
}
