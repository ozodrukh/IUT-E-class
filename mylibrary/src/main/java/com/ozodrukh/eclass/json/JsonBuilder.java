package com.ozodrukh.eclass.json;

import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import static com.ozodrukh.eclass.json.JsonToken.*;

public class JsonBuilder {

  public static String translateJavaScriptObjectToJson(String input) {
    if(input.endsWith(";")){
      input = input.substring(0, input.length() - 1);
    }

    JsonBuilder builder = new JsonBuilder();
    JsonReader reader = new JsonReader(new StringReader(input));
    reader.setLenient(true);
    try {
      while (reader.peek() != END_DOCUMENT) {
        switch (reader.peek()) {
          case BEGIN_OBJECT:
            reader.beginObject();
            builder.beginObject();
            break;
          case END_OBJECT:
            reader.endObject();
            builder.endObject();
            break;
          case BEGIN_ARRAY:
            builder.beginArray();
            reader.beginArray();
            break;
          case END_ARRAY:
            builder.endArray();
            reader.endArray();
            break;
          case NAME:
            builder.nextName(reader.nextName());
            break;
          case STRING:
            builder.nextString(reader.nextString());
            break;
          case NUMBER:
            builder.nextNumber(reader.nextDouble());
            break;
          case BOOLEAN:
            builder.nextBoolean(reader.nextBoolean());
            break;
          case NULL:
            reader.nextNull();
            builder.nextNull();
            break;
        }
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  private StringBuilder builder;
  private Stack<JsonToken> stack;

  public JsonBuilder() {
    builder = new StringBuilder();
    stack = new Stack<>();
  }

  public JsonBuilder beginArray() {
    builder.append('[');
    stack.push(JsonToken.BEGIN_ARRAY);
    return this;
  }

  public JsonBuilder endArray() {
    builder.append(']');
    stack.push(END_ARRAY);
    return this;
  }

  public JsonBuilder beginObject() {
    if (!stack.isEmpty() && stack.lastElement() == END_OBJECT) {
      builder.append(',');
    }
    builder.append('{');
    stack.push(BEGIN_OBJECT);
    return this;
  }

  public JsonBuilder endObject() {
    builder.append('}');
    stack.push(END_OBJECT);
    return this;
  }

  public JsonBuilder nextName(String name) {
    if (stack.lastElement() != BEGIN_OBJECT) {
      builder.append(',');
    }
    builder.append('"')
      .append(name)
      .append('"')
      .append(':');
    stack.push(NAME);
    return this;
  }

  public JsonBuilder nextString(String value) {
    builder.append('"')
      .append(value)
      .append('"');
    stack.push(STRING);
    return this;
  }

  public JsonBuilder nextNumber(Number value) {
    builder.append(value);
    stack.push(NUMBER);
    return this;
  }

  public JsonBuilder nextBoolean(boolean value) {
    builder.append(value);
    stack.push(BOOLEAN);
    return this;
  }

  public JsonBuilder nextNull() {
    builder.append("null");
    stack.push(NULL);
    return this;
  }

  public String build() {
    return builder.toString();
  }

  private void popObject(){
    pop(BEGIN_OBJECT, END_OBJECT);
  }

  private void popArray(){
    pop(BEGIN_ARRAY, END_ARRAY);
  }

  private void pop(JsonToken begin, JsonToken end){
    if(stack.isEmpty()){
      return;
    }

    while (!stack.isEmpty() && stack.pop() != begin){
      if(stack.peek() == end){
        pop(begin, end);
      }
    }
  }
}