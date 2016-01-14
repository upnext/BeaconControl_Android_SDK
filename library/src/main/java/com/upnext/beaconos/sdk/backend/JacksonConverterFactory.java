/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.lang.reflect.Type;

import retrofit.Converter;

class JacksonConverterFactory implements Converter.Factory {

    public static JacksonConverterFactory create(ObjectMapper objectMapper) {
        return new JacksonConverterFactory(objectMapper);
    }

    private final ObjectMapper objectMapper;

    public JacksonConverterFactory(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new NullPointerException("objectMapper is null.");
        }
        this.objectMapper = objectMapper;
    }

    @Override
    public Converter<?> get(Type type) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(type);
        ObjectWriter writer = objectMapper.writerWithType(javaType);
        ObjectReader reader = objectMapper.reader(javaType);
        return new JacksonConverter<>(writer, reader);
    }
}
