package io.onedev.server.web.editable.wiki;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.annotation.Wiki;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

public class WikiEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (descriptor.getPropertyGetter().getReturnType() != String.class
				|| descriptor.getPropertyGetter().getAnnotation(Wiki.class) == null)
			return null;
		return new PropertyContext<String>(descriptor) {
			@Override
			public PropertyEditor<String> renderForEdit(String id, IModel<String> model) {
				return new WikiPropertyEditor(id, descriptor, model);
			}

			@Override
			public PropertyViewer renderForView(String id, IModel<String> model) {
				return new PropertyViewer(id, descriptor) {
					@Override
					protected Component newContent(String id, PropertyDescriptor descriptor) {
						return new MarkdownViewer(id, Model.of(model.getObject()), null);
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
