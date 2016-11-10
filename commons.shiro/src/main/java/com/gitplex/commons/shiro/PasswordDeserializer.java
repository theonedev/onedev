package com.gitplex.commons.shiro;

import java.io.IOException;

import org.apache.shiro.authc.credential.PasswordService;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gitplex.commons.loader.AppLoader;

@SuppressWarnings("serial")
public class PasswordDeserializer extends StdDeserializer<String> {

	public PasswordDeserializer() {
		super(String.class);
	}

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String password = jp.readValueAs(String.class);
		if (password == null || password.startsWith(AbstractUser.HASH_PREFIX))
			return password;
		else
			return AppLoader.getInstance(PasswordService.class).encryptPassword(password);
	}

}
