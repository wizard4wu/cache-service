package com.dev.wizard.cache.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 11:31
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 注册支持 Java 8 的各种模块
        OBJECT_MAPPER.registerModule(new ParameterNamesModule());
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        // 时间格式化：不写成时间戳，而是可读字符串
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        OBJECT_MAPPER.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        // 在反序列化时忽略未知字段
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 允许 JSON 中出现未加引号的字段名、单引号等（兼容性更高）
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // 序列化时忽略 null 值字段（可选）
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }
    public static String toJson(Object bean) {
        if(bean == null) {
            return null;
        }
        if (bean instanceof String) {
            return (String) bean;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.error("JsonUtils toJson error,bean:{}", bean, e);
        }
        return null;
    }

    public static <V> V fromJson(String json, Class<V> beanClass) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        if (beanClass == String.class) {
            return (V) json;
        }
        try {
            return OBJECT_MAPPER.readValue(json, beanClass);
        } catch (IOException e) {
            log.error("JsonUtils fromJson error,json:{},beanClass:{}", json, beanClass, e);
        }
        return null;
    }

    public static <V> V fromJson(String json, TypeReference<V> type) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {

        }
        return null;
    }

    public static <T> List<T> fromJsons(String json, Class<T> beanClass) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, beanClass);
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {

        }
        return null;
    }
}