package io.onedev.server.web.editable.wiki;

import java.nio.charset.StandardCharsets;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.markdown.BlobMarkdownEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.wiki.ProjectWikiPage;

public class WikiPropertyEditor extends PropertyEditor<String> {

	private BlobMarkdownEditor input;

	public WikiPropertyEditor(String id, PropertyDescriptor descriptor, IModel<String> model) {
		super(id, descriptor, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		String content = getModelObject();
		input = findParent(ProjectWikiPage.class).newContentEditor("input", Model.of(
				content != null ? content.getBytes(StandardCharsets.UTF_8) : new byte[0]));
		add(input);
	}

	@Override
	protected String convertInputToValue() {
		return new String(input.getConvertedInput(), StandardCharsets.UTF_8);
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
}
