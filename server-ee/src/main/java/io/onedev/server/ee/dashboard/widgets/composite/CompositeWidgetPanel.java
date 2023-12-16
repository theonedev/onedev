package io.onedev.server.ee.dashboard.widgets.composite;

import io.onedev.server.model.support.Widget;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

public class CompositeWidgetPanel extends Panel {
	
	private final List<Widget> widgets;
	
	public CompositeWidgetPanel(String id, List<Widget> widgets) {
		super(id);
		this.widgets = widgets;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var tabs = new ArrayList<Tab>();
		for (var widget: widgets) {
			tabs.add(new AjaxActionTab(Model.of(widget.getTitle())) {
				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					var rendered = widget.render("content", false);	
					rendered.setOutputMarkupId(true);
					CompositeWidgetPanel.this.replace(rendered);
					target.add(rendered);
				}
				
			});
		}
		add(new Tabbable("tabs", tabs));
		add(widgets.iterator().next().render("content", false).setOutputMarkupId(true));
	}
}
