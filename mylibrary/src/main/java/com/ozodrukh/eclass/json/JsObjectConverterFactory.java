package com.ozodrukh.eclass.json;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class JsObjectConverterFactory extends Converter.Factory {

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
    final Converter<ResponseBody, ?> nextConverter = retrofit.nextResponseBodyConverter(JsObjectConverterFactory.this, type, annotations);
    if(nextConverter == null){
      return null;
    }else {
      return new Converter<ResponseBody, Object>() {
        @Override public Object convert(ResponseBody value) throws IOException {
          try {
            ResponseBody transformedValue = ResponseBody.create(MediaType.parse("application/json;charset=utf-8"),
                JsonBuilder.translateJavaScriptObjectToJson(value.string()));
            return nextConverter.convert(transformedValue);
          }finally {
            value.close();
          }
        }
      };
    }
  }
}
