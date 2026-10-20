package io.onedev.server.util.jackson.git;

import java.io.IOException;

import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PersonIdentSerializer extends JsonSerializer<PersonIdent> {

	@Override
	public void serialize(PersonIdent value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {
		// Preserve the JSON format from before JGit switched to java.time fields.
		jgen.writeStartObject();
		jgen.writeStringField("name", value.getName());
		jgen.writeStringField("emailAddress", value.getEmailAddress());
		jgen.writeNumberField("when", value.getWhenAsInstant().toEpochMilli());
		jgen.writeNumberField("tzOffset", value.getZoneOffset().getTotalSeconds() / 60);
		jgen.writeEndObject();
	}

}
