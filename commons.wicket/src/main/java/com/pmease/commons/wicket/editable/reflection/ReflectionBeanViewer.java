package com.pmease.commons.wicket.editable.reflection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.EditableResourceReference;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class ReflectionBeanViewer extends Panel {

	private final ReflectionBeanEditContext editContext;
	
	public ReflectionBeanViewer(String panelId, ReflectionBeanEditContext editContext) {
		super(panelId);
		
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PropertyEditContext<RenderContext>>("properties", editContext.getPropertyContexts()) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext<RenderContext>> item) {
				item.add(new Label("name", EditableUtils.getName(item.getModelObject().getPropertyGetter())));
				
				item.getModelObject().renderForView(new RenderContext(item, "value"));
			}

		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new EditableResourceReference()));
	}

}
