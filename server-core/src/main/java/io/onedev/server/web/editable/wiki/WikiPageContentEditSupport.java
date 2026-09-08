package io.onedev.server.web.editable.wiki;

import java.nio.charset.StandardCharsets;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.page.project.wiki.WikiPageBean;

/** Retains repository-aware Markdown editing inside the standard bean editor. */
public class WikiPageContentEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (descriptor.getBeanClass() != WikiPageBean.class || !descriptor.getPropertyName().equals("content"))
			return null;
		return new PropertyContext<byte[]>(descriptor) {
			@Override
			public PropertyEditor<byte[]> renderForEdit(String id, IModel<byte[]> model) {
				return new WikiPageContentPropertyEditor(id, descriptor, model);
			}

			@Override
			public PropertyViewer renderForView(String id, IModel<byte[]> model) {
				return new PropertyViewer(id, descriptor) {
					@Override
					protected Component newContent(String id, PropertyDescriptor descriptor) {
						return new MarkdownViewer(id, Model.of(model.getObject() != null
								? new String(model.getObject(), StandardCharsets.UTF_8) : ""), null);
					}
				};
			}
		};
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
}
