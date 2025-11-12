package com.dev.wizard.cache.redis.data.domain;

import com.dev.wizard.cache.redis.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Author: wizard.wu
 * Date: 2025/9/6 11:20
 */

public class Payload<T> {

    private static final Payload<String> EMPTY_PAYLOAD = new Builder().value("@@#@@").build();
    private static final Payload NULL_PAYLOAD = new Builder().value(null).build();

    private final T value;

    private Payload(Builder<T> builder) {
        this.value = builder.value;
    }

    public T getValue() {
        return value;
    }


    public static Payload empty() {
        return EMPTY_PAYLOAD;
    }

    public static <T> Payload<T> newPayload(T value, Boolean preventCachePenetration) {
        if (Boolean.TRUE.equals(preventCachePenetration) && null == value) return empty();
        return new Builder().value(value).build();
    }

    public static <T> Payload<T> deserializes(String value, Class<T> clazz) {
        if (EMPTY_PAYLOAD.getValue().equalsIgnoreCase(value)) {
            return NULL_PAYLOAD;
        }
        return new Builder().value(JsonUtils.fromJson(value, clazz)).build();
    }


    public static <T> Payload<T> deserialize(String value, Class<T> clazz) {
        if (EMPTY_PAYLOAD.getValue().equalsIgnoreCase(value)) {
            return NULL_PAYLOAD;
        }
        return new Builder().value(JsonUtils.fromJson(value, clazz)).build();
    }
    public static <T> Payload<T> deserialize(String value, TypeReference<T> type) {
        if (EMPTY_PAYLOAD.getValue().equalsIgnoreCase(value)) {
            return NULL_PAYLOAD;
        }
        if(type.getType().equals(String.class)){
            return new Builder().value(value).build();
        }
        return new Builder().value(JsonUtils.fromJson(value, type)).build();
    }



    public boolean fromEmpty() {
        return this == EMPTY_PAYLOAD;
    }

    public String serialize() {
        return JsonUtils.toJson(this.getValue());
    }







    public static class Builder<T> {
        private T value;

        public Builder() {
        }

        public Builder value(T value) {
            this.value = value;
            return this; // 支持链式调用
        }

        public Payload build() {
            return new Payload(this);
        }
    }
}
