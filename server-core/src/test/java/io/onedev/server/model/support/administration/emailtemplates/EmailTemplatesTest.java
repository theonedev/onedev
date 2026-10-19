package io.onedev.server.model.support.administration.emailtemplates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.onedev.server.oem.OemUtils;
import io.onedev.server.util.GroovyUtils;

class EmailTemplatesTest {

	private static final List<String> BRANDED_DEFAULTS = List.of(
			EmailTemplates.DEFAULT_USER_INVITATION, EmailTemplates.DEFAULT_EMAIL_VERIFICATION,
			EmailTemplates.DEFAULT_STOPWATCH_OVERDUE, EmailTemplates.DEFAULT_ALERT);

	private static final String URL = "https://OneDev.example/OneDev";

	private static final Map<String, Object> BINDINGS = Map.of(
			"setupAccountUrl", URL,
			"user", Map.of("name", "OneDev"),
			"emailAddress", "OneDev@example.com",
			"verificationUrl", URL,
			"stopwatch", Map.of("issue", Map.of("FQN", "OneDev#1", "url", URL)),
			"alert", Map.of("subject", "OneDev alert", "detail", "io.onedev.server.OneDev"),
			"serverUrl", URL);

	@Test
	void rendersConfiguredNameWithoutChangingUserContentOrLinks() {
		try (var oem = mockStatic(OemUtils.class)) {
			oem.when(OemUtils::getProductName).thenReturn("Acme & <Delivery> ${name}");
			for (var template : BRANDED_DEFAULTS) {
				var html = EmailTemplates.evalTemplate(true, template, BINDINGS);
				assertTrue(html.contains("Acme &amp; &lt;Delivery&gt; ${name}"));
				assertFalse(html.contains("Acme & <Delivery>"));
				assertTrue(html.contains(URL));
				var text = EmailTemplates.evalTemplate(false, template, BINDINGS);
				assertTrue(text.contains("Acme & <Delivery> ${name}"));
				assertTrue(text.contains(URL));
			}
			assertTrue(EmailTemplates.evalTemplate(false, EmailTemplates.DEFAULT_EMAIL_VERIFICATION,
					BINDINGS).contains("account \"OneDev\""));
			assertTrue(EmailTemplates.evalTemplate(false, EmailTemplates.DEFAULT_ALERT,
					BINDINGS).contains("io.onedev.server.OneDev"));
			assertFalse(BINDINGS.containsKey("productName"));
		}
	}

	@Test
	void upgradesSavedDefaultsAndPreservesOriginalDefaultOutput() {
		try (var oem = mockStatic(OemUtils.class)) {
			for (var template : BRANDED_DEFAULTS) {
				var savedDefault = template.replace("${productName}", "OneDev");
				for (var htmlVersion : List.of(true, false)) {
					oem.when(OemUtils::getProductName).thenReturn("Acme");
					assertEquals(EmailTemplates.evalTemplate(htmlVersion, template, BINDINGS),
							EmailTemplates.evalTemplate(htmlVersion, savedDefault, BINDINGS));
					oem.when(OemUtils::getProductName).thenReturn("OneDev");
					var originalBindings = new HashMap<String, Object>(BINDINGS);
					originalBindings.put("htmlVersion", htmlVersion);
					assertEquals(GroovyUtils.evalTemplate(savedDefault, originalBindings),
							EmailTemplates.evalTemplate(htmlVersion, template, BINDINGS));
				}
			}
		}
		var templates = new EmailTemplates();
		templates.setUserInvitation(EmailTemplates.DEFAULT_USER_INVITATION.replace("${productName}", "OneDev"));
		templates.setEmailVerification(EmailTemplates.DEFAULT_EMAIL_VERIFICATION.replace("${productName}", "OneDev"));
		templates.setStopwatchOverdue(EmailTemplates.DEFAULT_STOPWATCH_OVERDUE.replace("${productName}", "OneDev"));
		templates.setAlert(EmailTemplates.DEFAULT_ALERT.replace("${productName}", "OneDev"));
		assertEquals(EmailTemplates.DEFAULT_USER_INVITATION, templates.getUserInvitation());
		assertEquals(EmailTemplates.DEFAULT_EMAIL_VERIFICATION, templates.getEmailVerification());
		assertEquals(EmailTemplates.DEFAULT_STOPWATCH_OVERDUE, templates.getStopwatchOverdue());
		assertEquals(EmailTemplates.DEFAULT_ALERT, templates.getAlert());
	}

	@Test
	void keepsCustomTemplateSourceAndBindingsIntact() {
		try (var oem = mockStatic(OemUtils.class)) {
			oem.when(OemUtils::getProductName).thenReturn("Acme");
			var customTemplate = "<% import io.onedev.server.OneDev %>"
					+ "${OneDev.class.name}: OneDev ${content} ${productName}";
			var bindings = Map.<String, Object>of("content", "OneDev user content");
			assertEquals("io.onedev.server.OneDev: OneDev OneDev user content Acme",
					EmailTemplates.evalTemplate(false, customTemplate, bindings));
			var templates = new EmailTemplates();
			templates.setUserInvitation(customTemplate);
			assertEquals(customTemplate, templates.getUserInvitation());
			templates.setUserInvitation(null);
			assertNull(templates.getUserInvitation());
		}
	}

}
