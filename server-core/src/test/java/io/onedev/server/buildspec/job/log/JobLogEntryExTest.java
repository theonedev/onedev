package io.onedev.server.buildspec.job.log;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

import io.onedev.server.job.log.StyleBuilder;

public class JobLogEntryExTest {

	@Test
	public void test() {
		JobLogEntryEx entry;
		
		entry = JobLogEntryEx.parse("\u001b]0;This is the window title\u0007hello\u001b]8;link\u001b\\world\u001b]P1888888just\u001b\u0020\u0020\u0030do", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), "hellolinkworldjustdo")
			), entry.getMessages());
		
		entry = JobLogEntryEx.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[1K", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "5"),
				new Message(new Style("32", Style.BACKGROUND_COLOR_DEFAULT, false), "abcde")
			), entry.getMessages());
		
		entry = JobLogEntryEx.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[K", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "1234")
			), entry.getMessages());
		
		entry = JobLogEntryEx.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[34mxxxxxxx", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "1234"),
				new Message(new Style("34", Style.BACKGROUND_COLOR_DEFAULT, false), "xxxxxxx")
			), entry.getMessages());
		
		entry = JobLogEntryEx.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[34mxy", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "1234"),
				new Message(new Style("34", Style.BACKGROUND_COLOR_DEFAULT, false), "xy"),
				new Message(new Style("32", Style.BACKGROUND_COLOR_DEFAULT, false), "bcde")
			), entry.getMessages());
		
		entry = JobLogEntryEx.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[10Dabcde12345", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), "abcde12345")
			), entry.getMessages());
		
		entry = JobLogEntryEx.parse("\u001b[31mRED\u001b[0m and \u001b[32mGREEN\u001b[m", new StyleBuilder());
		assertEquals(Lists.newArrayList(
					new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "RED"),
					new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), " and "),
					new Message(new Style("32", Style.BACKGROUND_COLOR_DEFAULT, false), "GREEN")
				), entry.getMessages());
	}

}
