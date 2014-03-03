package com.pmease.gitop.web.util;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.web.git.command.ListTagCommand;

public class TagParserTest {

	@Test public void testTagParser() throws IOException {
		URL url = Resources.getResource(TagParserTest.class, "tag.out");
		LineProcessor<Map<String, Commit>> tagConsumer = new ListTagCommand.TagConsumer();
		Resources.readLines(url, Charsets.UTF_8, tagConsumer);
		
		System.out.println(tagConsumer.getResult().get("v3.0.3").getMessage());
	}
}
