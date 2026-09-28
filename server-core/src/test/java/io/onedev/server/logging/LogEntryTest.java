package io.onedev.server.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import io.onedev.server.util.jackson.ObjectMapperProvider;

public class LogEntryTest {

	@Test
	public void test() {
		LogEntry entry;
		
		var styleBuilder = new StyleBuilder();
		LogEntry.parse("\u001B[30;43ma\u001B[39;49m", styleBuilder);
		entry = LogEntry.parse("\u001B[34mb\u001B[39m", styleBuilder);
		assertEquals(Lists.newArrayList(
				new Message(new Style("34", Style.BACKGROUND_COLOR_DEFAULT, false), "b")
		), entry.getMessages());
		
		entry = LogEntry.parse("a\r\nb", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), "a\nb")
		), entry.getMessages());
		
		entry = LogEntry.parse("a\nb", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), "a\nb")
		), entry.getMessages());
		
		entry = LogEntry.parse("\u001b]0;This is the window title\u0007hello\u001b]8;link\u001b\\world\u001b]P1888888just\u001b\u0020\u0020\u0030do", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), "hellolinkworldjustdo")
			), entry.getMessages());
		
		entry = LogEntry.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[1K", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "5"),
				new Message(new Style("32", Style.BACKGROUND_COLOR_DEFAULT, false), "abcde")
			), entry.getMessages());
		
		entry = LogEntry.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[K", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "1234")
			), entry.getMessages());
		
		entry = LogEntry.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[34mxxxxxxx", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "1234"),
				new Message(new Style("34", Style.BACKGROUND_COLOR_DEFAULT, false), "xxxxxxx")
			), entry.getMessages());
		
		entry = LogEntry.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[6D\u001b[34mxy", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "1234"),
				new Message(new Style("34", Style.BACKGROUND_COLOR_DEFAULT, false), "xy"),
				new Message(new Style("32", Style.BACKGROUND_COLOR_DEFAULT, false), "bcde")
			), entry.getMessages());
		
		entry = LogEntry.parse("\u001b[31m12345\u001b[0m\u001b[32mabcde\u001b[m\u001b[10Dabcde12345", new StyleBuilder());
		assertEquals(Lists.newArrayList(
				new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), "abcde12345")
			), entry.getMessages());
		
		entry = LogEntry.parse("\u001b[31mRED\u001b[0m and \u001b[32mGREEN\u001b[m", new StyleBuilder());
		assertEquals(Lists.newArrayList(
					new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, false), "RED"),
					new Message(new Style(Style.FOREGROUND_COLOR_DEFAULT, Style.BACKGROUND_COLOR_DEFAULT, false), " and "),
					new Message(new Style("32", Style.BACKGROUND_COLOR_DEFAULT, false), "GREEN")
				), entry.getMessages());
	}

	@Test
	public void serializationPreservesPlainStyledAndEmptyEntries() throws Exception {
		var date = new Date(1234);
		var entries = List.of(
				new LogEntry(date, "plain 测试\nsecond line"),
				new LogEntry(date, ""),
				new LogEntry(date, new ArrayList<>()),
				new LogEntry(date, List.of(
						new Message(new StyleBuilder().build(), "first "),
						new Message(new StyleBuilder().build(), "second"))),
				new LogEntry(date, List.of(
						new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, true), "red"),
						new Message(new StyleBuilder().build(), " plain"))));
		var bytes = new ByteArrayOutputStream();
		try (var output = new ObjectOutputStream(bytes)) {
			for (var entry : entries)
				output.writeObject(entry);
		}
		try (var input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			for (var entry : entries) {
				var restored = (LogEntry) input.readObject();
				assertEquals(entry.getDate(), restored.getDate());
				assertEquals(entry.getMessageText(), restored.getMessageText());
				assertEquals(entry.render(), restored.render());
				if (entry.getMessages().stream().anyMatch(it -> !it.getStyle().isDefault()))
					assertEquals(entry.getMessages(), restored.getMessages());
				else
					assertTrue(restored.getMessages().stream().allMatch(it -> it.getStyle().isDefault()));
			}
		}
	}

	@Test
	public void plainEntriesUseCompactSerialization() throws Exception {
		var bytes = new ByteArrayOutputStream();
		try (var output = new ObjectOutputStream(bytes)) {
			output.writeObject(new LogEntry(new Date(1234), "plain"));
		}
		var serialized = bytes.toString(StandardCharsets.ISO_8859_1);
		assertFalse(serialized.contains(Message.class.getName()));
		assertFalse(serialized.contains(Style.class.getName()));
		assertFalse(serialized.contains(ArrayList.class.getName()));
	}

	@Test
	public void uiJsonIncludesEntryTextAndStyle() throws Exception {
		var mapper = new ObjectMapperProvider(Set.of(), Set.of(), null, Set.of()).get();
		var entry = new LogEntry(new Date(1234), List.of(
				new Message(new Style("31", Style.BACKGROUND_COLOR_DEFAULT, true), "red"),
				new Message(new StyleBuilder().build(), " plain")));
		var json = mapper.readTree(mapper.writeValueAsBytes(entry));
		assertTrue(json.hasNonNull("date"));
		assertEquals(2, json.path("messages").size());
		assertEquals("red", json.at("/messages/0/text").asText());
		assertEquals("31", json.at("/messages/0/style/color").asText());
		assertEquals(Style.BACKGROUND_COLOR_DEFAULT, json.at("/messages/0/style/backgroundColor").asText());
		assertTrue(json.at("/messages/0/style/bold").asBoolean());
		assertEquals(" plain", json.at("/messages/1/text").asText());
		assertEquals(Style.FOREGROUND_COLOR_DEFAULT, json.at("/messages/1/style/color").asText());
		assertFalse(json.at("/messages/1/style/bold").asBoolean());
	}

}
