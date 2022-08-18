package com.onefly.gateway.utils.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * author 田尘殇(Sean sean.snow@live.com)
 * date 2015/7/5
 * time 11:34
 */
public class Json {

    private static final ObjectMapper defaultObjectMapper = new ObjectMapper();
    private static volatile ObjectMapper objectMapper = null;

    // Ensures that there always is *a* object mapper
    private static ObjectMapper mapper() {
        if (objectMapper == null) {
            return defaultObjectMapper;
        } else {
            return objectMapper;
        }
    }


    /**
     * Convert an object to JsonNode.
     *
     * @param data Value to convert in Json.
     * @return JsonNode
     */
    public static JsonNode toJson(final Object data) {
        try {
            return mapper().valueToTree(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Convert a JsonNode to a Java value
     *
     * @param json  Json value to convert.
     * @param clazz Expected Java value type.
     * @param <A>   Expected Java value type.
     * @return 预期的Java对象
     */
    public static <A> A fromJson(JsonNode json, Class<A> clazz) {
        try {
            return mapper().treeToValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <A> A fromJson(String json, Class<A> clazz) {
        try {
            JsonNode jsonNode = parse(json);
            return mapper().treeToValue(jsonNode, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <A> A fromJson(String json, TypeReference<A> typeReference) {
        try {
            return mapper().readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建一个空的ObjectNode对象
     *
     * @return ObjectNode
     */
    public static ObjectNode newObject() {
        return mapper().createObjectNode();
    }


    /**
     * JsonNode转换为字符串表示。
     *
     * @param json JsonNode
     * @return json 字符串
     */
    public static String stringify(JsonNode json) {
        return json.toString();
    }

    /**
     * 解析一个字符串为JsonNode对象
     *
     * @param src Json字符串
     * @return return it as a JsonNode.
     */
    public static JsonNode parse(String src) {
        try {
            return mapper().readValue(src, JsonNode.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Parse a InputStream representing a json, and return it as a JsonNode.
     *
     * @param src 源数据流
     * @return JsonNode json对象
     */
    public static JsonNode parse(java.io.InputStream src) {
        try {
            return mapper().readValue(src, JsonNode.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Inject the object mapper to use.
     * <p>
     * This is intended to be used when Play starts up. By default, Play will
     * inject its own object mapper here, but this mapper can be overridden
     * either by a custom plugin or from Global.onStart.
     *
     * @param mapper mapper
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }
}
