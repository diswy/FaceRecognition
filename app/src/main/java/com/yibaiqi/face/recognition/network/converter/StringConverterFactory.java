package com.yibaiqi.face.recognition.network.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class StringConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return type == String.class ? StringConverter.instance() : null;
    }

    public static StringConverterFactory create(){
        return new StringConverterFactory();
    }
}
