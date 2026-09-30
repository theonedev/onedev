package io.onedev.server.web.component.progress;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.onedev.server.web.translation.TranslationStringResourceLoader;

class TimeProgressBarTest {
	private WicketTester tester;

	@BeforeEach
	void setUp() {
		tester = new WicketTester();
		tester.getApplication().getResourceSettings().getStringResourceLoaders()
				.add(0, new TranslationStringResourceLoader());
	}

	@AfterEach
	void tearDown() {
		tester.destroy();
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void rendersConstructorOptionsAndInitialElapsedTime(boolean roundCorner) {
		var progress = new TimeProgressBar("progress", 60, "#123456", roundCorner).setElapsed(30);
		var element = render(progress);
		assertTrue(element.hasClass("caller-style"));
		assertTrue(element.hasClass("time-progress"));
		assertEquals(roundCorner, element.hasClass("time-progress-rounded"));
		assertTrue(element.attr("style").contains("--time-progress-color: #123456"));
		assertEquals("60.0", element.attr("data-estimated-duration"));
		assertEquals("30.0", element.attr("data-elapsed"));
		assertEquals("50", element.attr("aria-valuenow"));
		assertEquals("Custom progress", element.attr("aria-label"));
		assertFalse(element.hasAttr("hidden"));
		assertNotNull(element.selectFirst(".time-progress-fill"));
		assertTrue(tester.getLastResponseAsString().contains("onedev.server.timeProgress.onDomReady("));
	}

	@Test
	void hidesMissingEstimateAndCapsOverdueProgress() {
		var progress = new TimeProgressBar("progress", 0, "var(--primary)", false);
		assertTrue(render(progress).hasAttr("hidden"));
		var element = render(new TimeProgressBar("progress", 10, "var(--primary)", false).setElapsed(20));
		assertFalse(element.hasAttr("hidden"));
		assertEquals("99", element.attr("aria-valuenow"));
	}

	private Element render(TimeProgressBar progress) {
		tester.startPage(progress.getParent() != null ? progress.getPage() : new TestPage(progress));
		return Jsoup.parse(tester.getLastResponseAsString()).getElementById(progress.getMarkupId());
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider {
		private static final long serialVersionUID = 1L;

		TestPage(TimeProgressBar progress) {
			add(progress);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
			return new StringResourceStream("<html><head></head><body>"
					+ "<div wicket:id='progress' class='caller-style' aria-label='Custom progress'></div>"
					+ "</body></html>");
		}
	}
}
