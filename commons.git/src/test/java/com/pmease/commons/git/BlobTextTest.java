package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;

import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.extensionpoint.TextConverterProvider;
import com.pmease.commons.loader.AppLoader;

public class BlobTextTest extends AbstractGitTest {

	@Test
	public void test() {
		Mockito.when(AppLoader.getExtensions(TextConverterProvider.class)).thenReturn(new HashSet<TextConverterProvider>());
		
		BlobText result = BlobText.from(" hello \tworld \t \r".getBytes());
		assertNotNull(result);
		assertEquals(" hello \tworld \t ", result.ignoreEOL().getLines().get(0));
		assertEquals(" hello \tworld", result.ignoreEOLSpaces().getLines().get(0));
		assertEquals("hello world", result.ignoreChangeSpaces().getLines().get(0));
	}

}
