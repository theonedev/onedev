package io.onedev.server.util.jackson.git;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ObjectIdSerializer extends JsonSerializer<ObjectId> {

	@Override
	public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		byte[] bytes = new byte[20];
		value.copyRawTo(bytes, 0);
		jgen.writeBinary(bytes);
	}

}
