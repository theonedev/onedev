package com.pmease.gitop.web.util;

import static org.junit.Assert.assertEquals;

import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.junit.Test;

public class MimeTypeUtilsTest {

	@Test public void testGuessSourceType() throws MimeTypeException {
		String type = "text/x-vbdotnet";
		MimeType m = MimeTypes.getDefaultMimeTypes().getRegisteredMimeType(type);
		
		assertEquals(MimeTypeUtils.guessSourceType(m), "vbdotnet");
		
		type = "application/json";
		m = MimeTypes.getDefaultMimeTypes().getRegisteredMimeType(type);
		assertEquals(MimeTypeUtils.guessSourceType(m), "json");
	}
}
