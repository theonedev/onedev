package io.onedev.server.web.component.offcanvas;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class OffCanvasCardSupportPanel extends Panel {

	public OffCanvasCardSupportPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(newTitle("title"));
		
		add(newBody("body"));
		
		Component footer = newFooter("footer");
		if (footer != null)
			add(footer);
		else
			add(new WebMarkupContainer("footer").setVisible(false));
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	protected abstract Component newTitle(String componentId);
	
	protected abstract Component newBody(String componentId);
	
	@Nullable
	protected Component newFooter(String componentId) {
		return null;
	}

	protected abstract void onClose(AjaxRequestTarget target);

}
