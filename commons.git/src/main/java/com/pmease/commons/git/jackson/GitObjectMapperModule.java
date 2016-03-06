package com.pmease.commons.git.jackson;

import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Singleton
public class GitObjectMapperModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	@Override
	public void setupModule(SetupContext context) {
		addSerializer(ObjectId.class, new ObjectIdSerializer());
		addDeserializer(ObjectId.class, new ObjectIdDeserializer());
	}

	@Override
	public String getModuleName() {
		return "GitModule";
	}

	@Override
	public Version version() {
		return Version.unknownVersion();
	}

}
