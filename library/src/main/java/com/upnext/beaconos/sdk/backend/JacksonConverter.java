/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

import retrofit.Converter;

class JacksonConverter<T> implements Converter<T> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    private final ObjectWriter writer;
    private final ObjectReader reader;

    public JacksonConverter(ObjectWriter writer, ObjectReader reader) {
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    public T fromBody(ResponseBody body) throws IOException {
        InputStream is = body.byteStream();
        try {
            T value = reader.readValue(is);
            if (value instanceof Validable) {
                if (! ((Validable) value).isValid()) {
                    throw new IOException();
                }
            }
            return value;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public RequestBody toBody(T value) {
        try {
            byte[] bytes = writer.writeValueAsBytes(value);
            return RequestBody.create(MEDIA_TYPE, bytes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}