package com.gitplex.server.util.jackson.hibernate;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gitplex.server.model.AbstractEntity;

public final class ManyToOneSerializer extends StdSerializer<AbstractEntity> {

	public ManyToOneSerializer(Class<AbstractEntity> entityClass) {
		super(entityClass);
	}
	
	@Override
	public void serialize(AbstractEntity value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonGenerationException {
		if (value != null)
			jgen.writeNumber(value.getId());
		else
			jgen.writeNull();
	}
	
}