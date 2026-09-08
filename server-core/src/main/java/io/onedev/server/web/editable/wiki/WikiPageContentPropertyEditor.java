package io.onedev.server.web.editable.wiki;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.markdown.BlobMarkdownEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.wiki.ProjectWikiPage;

public class WikiPageContentPropertyEditor extends PropertyEditor<byte[]> {

	private BlobMarkdownEditor input;

	public WikiPageContentPropertyEditor(String id, PropertyDescriptor descriptor, IModel<byte[]> model) {
		super(id, descriptor, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		input = findParent(ProjectWikiPage.class).newContentEditor("input", Model.of(getModelObject()));
		add(input);
	}

	@Override
	protected byte[] convertInputToValue() {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
}
