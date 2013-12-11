package com.pmease.gitop.web.util;

import org.junit.Test;

import com.pmease.gitop.web.service.impl.Languages;

public class LanguageTest {

	@Test public void testLanguage() {
		System.out.println(Languages.INSTANCE.findLanguageByExtension(".c"));
	}
}
