package com.pmease.gitop.web.util;

import java.io.File;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.pmease.gitop.web.service.impl.TikaFileTypeRegistry;

public class GitBlobTest {

	static final File repoDir = new File("/Users/zhenyu/temp/aaa");
	
	Injector injector = Guice.createInjector(new TikaFileTypeRegistry.Module());
	
	@Test public void testBlob() {
		
	}
}
