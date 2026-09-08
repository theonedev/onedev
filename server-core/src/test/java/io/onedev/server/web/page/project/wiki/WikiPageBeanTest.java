package io.onedev.server.web.page.project.wiki;

import static org.junit.Assert.*;

import javax.validation.Validation;

import org.junit.Test;

import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.wiki.WikiPageContentEditSupport;

public class WikiPageBeanTest {

	@Test
	public void validatesPageNamesOnTheirProperty() {
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			var validator = factory.getValidator();
			for (String name : new String[] {null, "", "../outside", "/absolute", "docs/../../outside"}) {
				var violations = validator.validateValue(WikiPageBean.class, "name", name);
				assertFalse("Expected invalid name: " + name, violations.isEmpty());
				assertTrue(violations.stream().allMatch(it -> it.getPropertyPath().toString().equals("name")));
			}
			for (String name : new String[] {"Home", "Guides/Setup", "Getting started"})
				assertTrue(validator.validateValue(WikiPageBean.class, "name", name).isEmpty());
			assertTrue(validator.validateValue(WikiPageBean.class, "commitMessage", null).isEmpty());
		}
	}

	@Test
	public void usesCustomEditorOnlyForWikiContent() {
		var support = new WikiPageContentEditSupport();
		assertNotNull(support.getEditContext(new PropertyDescriptor(WikiPageBean.class, "content")));
		assertNull(support.getEditContext(new PropertyDescriptor(WikiPageBean.class, "name")));
		assertNull(support.getEditContext(new PropertyDescriptor(WikiPageBean.class, "commitMessage")));
	}
}
