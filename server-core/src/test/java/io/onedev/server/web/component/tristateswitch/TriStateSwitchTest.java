package io.onedev.server.web.component.tristateswitch;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TriStateSwitchTest {

	private WicketTester tester;

	@BeforeEach
	void setUp() {
		tester = new WicketTester();
		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
	}

	@AfterEach
	void tearDown() {
		tester.destroy();
	}

	@Test
	void rendersAndSubmitsAllThreeStates() {
		Boolean[] states = {false, null, true};
		for (int i = 0; i < states.length; i++) {
			var input = new TriStateSwitch("switch", Model.of(states[i]));
			tester.startPage(new TestPage(input));
			var element = Jsoup.parse(tester.getLastResponseAsString()).getElementById(input.getMarkupId());
			assertEquals(String.valueOf(i), element.attr("value"));
			assertEquals("range", element.attr("type"));
			assertEquals("Setting", element.attr("aria-label"));
			for (int j = 0; j < states.length; j++) {
				var form = tester.newFormTester("form");
				form.setValue("switch", String.valueOf(j));
				form.submit();
				tester.assertNoErrorMessage();
				assertEquals(states[j], input.getModelObject());
			}
		}
	}

	@Test
	void ajaxChangesCanClearTheModel() {
		var input = new TriStateSwitch("switch", Model.of(true));
		input.add(new AjaxFormComponentUpdatingBehavior("change") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(getComponent());
			}
		});
		tester.startPage(new TestPage(input));
		tester.getRequest().setParameter(input.getInputName(), "1");
		tester.executeAjaxEvent(input, "change");
		tester.assertNoErrorMessage();
		assertNull(input.getModelObject());
		tester.assertComponentOnAjaxResponse(input);
	}

	@Test
	void rejectsInvalidValuesAndIgnoresDisabledInput() {
		var input = new TriStateSwitch("switch", Model.of(true));
		tester.startPage(new TestPage(input));
		var form = tester.newFormTester("form");
		form.setValue("switch", "3");
		form.submit();
		assertFalse(input.isValid());
		assertEquals(Boolean.TRUE, input.getModelObject());

		input.setEnabled(false);
		tester.startPage(input.getPage());
		var element = Jsoup.parse(tester.getLastResponseAsString()).getElementById(input.getMarkupId());
		assertTrue(element.hasAttr("disabled"));
		tester.getRequest().setParameter(input.getInputName(), "0");
		tester.submitForm("form");
		assertEquals(Boolean.TRUE, input.getModelObject());
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider {
		private static final long serialVersionUID = 1L;

		TestPage(TriStateSwitch input) {
			add(new Form<Void>("form").add(input));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
			return new StringResourceStream("<html><head></head><body><form wicket:id='form'>"
					+ "<input wicket:id='switch' aria-label='Setting'>"
					+ "</form></body></html>");
		}
	}
}
