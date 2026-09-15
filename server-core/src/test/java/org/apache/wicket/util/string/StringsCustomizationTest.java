package org.apache.wicket.util.string;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringsCustomizationTest {

	@Test
	public void diagnosticsKeepMessagesAndStackWithoutRenderingHtmlTemplate() {
		var cause = new RuntimeException("Missing component") {
			private static final long serialVersionUID = 1L;

			@Override
			public String toString() {
				return "<html>Entire page template</html>";
			}
		};
		cause.setStackTrace(new StackTraceElement[] {
				new StackTraceElement("ExamplePage", "render", "ExamplePage.java", 42)
		});
		var result = Strings.toString(new RuntimeException("Unable to render", cause));
		assertTrue(result.contains("Unable to render"));
		assertTrue(result.contains("Missing component"));
		assertTrue(result.contains("ExamplePage.render(ExamplePage.java:42)"));
		assertFalse(result.contains("Entire page template"));
	}
}
