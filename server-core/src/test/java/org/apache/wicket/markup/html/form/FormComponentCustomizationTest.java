package org.apache.wicket.markup.html.form;

import static org.junit.Assert.*;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FormComponentCustomizationTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}

	@After
	public void tearDown() {
		tester.destroy();
	}

	@Test
	public void dependentEditorsCanReadInitialInputBeforeSubmission() {
		var input = new TextField<>("input", Model.of("initial"));
		assertEquals("initial", input.getConvertedInput());
	}

	@Test
	public void uncommittedInputSurvivesDetachAndPageSerialization() {
		var input = new TextField<>("input", Model.of("saved"));
		input.setConvertedInput("edited");
		input.detach();
		assertEquals("edited", input.getConvertedInput());

		var restored = SerializationUtils.clone(input);
		assertEquals("edited", restored.getConvertedInput());
		assertEquals("saved", restored.getModelObject());
	}

	@Test
	public void errorsOnOrdinaryChildrenInvalidateCompositeEditor() {
		var editor = new FormComponentPanel<String>("editor") {
			private static final long serialVersionUID = 1L;
		};
		var child = new Label("child");
		editor.add(child);
		child.warn("A warning should not prevent submission");
		assertTrue(editor.isValid());

		child.error("Invalid nested value");
		assertFalse(editor.isValid());
	}
}
