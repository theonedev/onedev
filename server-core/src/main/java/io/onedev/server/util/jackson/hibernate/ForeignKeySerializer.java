package io.onedev.server.util.jackson.hibernate;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.onedev.server.model.AbstractEntity;

public final class ForeignKeySerializer extends StdSerializer<AbstractEntity> {

	private static final long serialVersionUID = 1L;

	public ForeignKeySerializer(Class<AbstractEntity> entityClass) {
		super(entityClass);
	}
	
	@Override
	public void serializeWithType(AbstractEntity value, JsonGenerator gen, SerializerProvider serializers,
			TypeSerializer typeSer) throws IOException {
		super.serializeWithType(value, gen, serializers, typeSer);
	}

	@Override
	public void serialize(AbstractEntity value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonGenerationException {
		if (value != null) {
			jgen.writeNumber(value.getId());
		} else {
			jgen.writeNull();
		}
	}
	
}