package com.github.maracas.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

import java.io.IOException;

public class CtElementSerializer extends StdSerializer<CtElement> {
  public CtElementSerializer() {
    this(null);
  }

  public CtElementSerializer(Class<CtElement> t) {
    super(t);
  }

  @Override
  public void serialize(CtElement element, JsonGenerator json, SerializerProvider serializerProvider) throws IOException {
    SourcePosition pos = element.getPosition();
    json.writeStartObject();

    if (pos != null && pos.isValidPosition()) {
      json.writeStringField("file", pos.getFile().getAbsolutePath());
      json.writeNumberField("startLine", pos.getLine());
      json.writeNumberField("endLine", pos.getEndLine());
      json.writeNumberField("startColumn", pos.getColumn());
      json.writeNumberField("endColumn", pos.getEndColumn());
    }

    json.writeEndObject();
  }
}
