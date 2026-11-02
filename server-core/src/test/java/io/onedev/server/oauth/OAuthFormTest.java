package io.onedev.server.oauth;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.wicket.markup.Markup;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.onedev.server.web.page.oauth.OAuthForm;

class OAuthFormTest {

	private WicketTester tester;
	private boolean submitted;

	@BeforeEach
	void setUp() {
		tester = new WicketTester();
		var form = new OAuthForm("form") {
			@Override protected void onSubmit() { submitted = true; }
		};
		tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><input wicket:id='csrf' type='hidden'></form>"));
	}

	@AfterEach
	void tearDown() {
		tester.destroy();
	}

	@Test
	void acceptsTheRenderedToken() {
		tester.newFormTester("form").submit();
		assertTrue(submitted);
	}

	@Test
	void rejectsMissingToken() {
		var form = tester.newFormTester("form");
		form.setValue("csrf", "");
		form.submit();
		assertFalse(submitted);
	}

	@Test
	void rejectsForgedToken() {
		var form = tester.newFormTester("form");
		form.setValue("csrf", "forged");
		form.submit();
		assertFalse(submitted);
		tester.assertErrorMessages("Invalid form token");
	}
}
